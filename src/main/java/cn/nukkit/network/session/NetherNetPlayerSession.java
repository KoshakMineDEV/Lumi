package cn.nukkit.network.session;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.CompressionProvider;
import cn.nukkit.network.NetherNetInterface;
import cn.nukkit.network.Network;
import cn.nukkit.network.NetworkEventQueue;
import cn.nukkit.network.NetworkPacketBudget;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.DisconnectPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.RequestNetworkSettingsPacket;
import cn.nukkit.network.protocol.ResourcePacksInfoPacket;
import cn.nukkit.network.protocol.ResourcePackChunkDataPacket;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import cn.nukkit.network.protocol.ResourcePackChunkRequestPacket;
import cn.nukkit.utils.BinaryStream;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.message.FormattedMessage;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChildChannel;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WebRTC batches use the RakNet batch body without its 0xFE frame marker or Bedrock encryption.
 * Adapted from Nukkit-MOT and Lumi's RakNet session.
 */
@Log4j2
public class NetherNetPlayerSession extends SimpleChannelInboundHandler<ByteBuf> implements NetworkPlayerSession {

    /** NetherNet clients are all on the newest RakNet protocol revision. */
    public static final int RAKNET_PROTOCOL = 11;

    static final int MAX_INBOUND_PACKETS_PER_SERVER_TICK = 120;
    /** Maximum decoded payload work one player may force onto one server tick. */
    static final long MAX_INBOUND_BYTES_PER_SERVER_TICK = 6L * 1024L * 1024L;
    /** One maximum-size legal decoded batch may wait per session. */
    static final int MAX_QUEUED_INBOUND_PACKETS = 1300;
    static final long MAX_QUEUED_INBOUND_BYTES = 6L * 1024L * 1024L;
    static final int INBOUND_LOW_WATER_PACKETS = MAX_QUEUED_INBOUND_PACKETS / 2;
    static final long INBOUND_LOW_WATER_BYTES = MAX_QUEUED_INBOUND_BYTES / 2;

    static final int MAX_INBOUND_WIRE_BYTES = 12582912; // 12 MiB
    /**
     * Permit short bursts of small movement and interaction batches while bounding sustained
     * wire traffic. Decode bytes and framed-packet tokens independently limit processing work.
     */
    static final double MAX_INGRESS_BATCH_TOKENS = 1200D;
    static final double INGRESS_BATCH_TOKENS_PER_SECOND = 300D;
    static final double MAX_INGRESS_COMPRESSED_BYTES = 48D * 1024D * 1024D;
    static final double INGRESS_COMPRESSED_BYTES_PER_SECOND = 6D * 1024D * 1024D;
    static final double MAX_INGRESS_DECODE_BYTES = 24D * 1024D * 1024D;
    static final double INGRESS_DECODE_BYTES_PER_SECOND = 6D * 1024D * 1024D;
    static final double DECODE_BYTES_RESERVATION = 12D * 1024D * 1024D;
    static final double MAX_INGRESS_FRAME_TOKENS = 5200D;
    static final double INGRESS_FRAME_TOKENS_PER_SECOND = 1300D;
    static final double FRAME_TOKENS_RESERVATION = 2600D;
    /** How many malformed batches a playing session may send inside the sliding window. */
    static final int MAX_MALFORMED_BATCHES_WHILE_PLAYING = 32;
    static final long MALFORMED_BATCH_WINDOW_NANOS = TimeUnit.MINUTES.toNanos(5);
    static final long DIAGNOSTIC_LOG_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(30);

    private final NetherNetInterface server;
    private final NetherNetChildChannel channel;

    private final Queue<DataPacket> inbound = PlatformDependent.newSpscQueue();
    private final InboundBatchBacklog inboundBacklog = new InboundBatchBacklog();
    private final AtomicInteger queuedInboundPackets = new AtomicInteger();
    private final AtomicLong queuedInboundBytes = new AtomicLong();
    private final Queue<DataPacket> outbound = PlatformDependent.newMpscQueue();
    private final ScheduledFuture<?> tickFuture;
    private final AtomicBoolean queuedForPlayerCreation = new AtomicBoolean();
    private final NetworkPacketBudget inboundBudget = new NetworkPacketBudget(
            MAX_INBOUND_PACKETS_PER_SERVER_TICK, MAX_INBOUND_BYTES_PER_SERVER_TICK);
    private volatile boolean processingClosed;
    private final NetworkEventQueue.Task pendingTask = new NetworkEventQueue.Task() {
        @Override
        public NetworkEventQueue.Result process(long deadlineNanos) {
            return processingClosed ? NetworkEventQueue.Result.DONE : server.processSession(NetherNetPlayerSession.this, deadlineNanos);
        }

        @Override
        public boolean hasWork() {
            return !processingClosed && player != null && (disconnectReason != null || !inbound.isEmpty());
        }
    };
    private final long acceptedAtNanos = System.nanoTime();
    private volatile long lastLoginActivityNanos = this.acceptedAtNanos;
    private volatile long resourcePackStartedNanos;
    private volatile boolean loginReceived;

    private volatile Player player;

    private volatile String disconnectReason = null;
    /**
     * A once-active channel going inactive is a remote close: the library flips inactive before
     * close(), so Netty never fires channelInactive and the session must notice on its own.
     */
    private volatile boolean transportWasActive;

    private CompressionProvider compressionIn;
    private CompressionProvider compressionOut;
    private boolean compressionInitialized;

    private final ArrayDeque<Long> malformedBatches = new ArrayDeque<>();
    private long lastMalformedLogNanos;
    private long lastInboundThrottleLogNanos;
    private long throttledInboundBatches;
    private volatile boolean inboundThrottled;
    private double ingressBatchTokens = NetherNetPlayerSession.MAX_INGRESS_BATCH_TOKENS;
    private double ingressCompressedBytes = NetherNetPlayerSession.MAX_INGRESS_COMPRESSED_BYTES;
    private double ingressDecodeBytes = NetherNetPlayerSession.MAX_INGRESS_DECODE_BYTES;
    private double ingressFrameTokens = NetherNetPlayerSession.MAX_INGRESS_FRAME_TOKENS;
    private long lastIngressRefillNanos;

    public NetherNetPlayerSession(NetherNetInterface server, NetherNetChildChannel channel) {
        this.server = server;
        this.channel = channel;
        this.tickFuture = channel.eventLoop().scheduleAtFixedRate(this::networkTick, 0, 20, TimeUnit.MILLISECONDS);
        // The completed close marks the session disconnected: channelInactive is unreliable here,
        // while the close future always fires
        channel.closeFuture().addListener(future -> this.disconnect("transport:closed_by_remote_peer"));

        // RakNet protocol 11: batches arrive raw until NetworkSettings negotiates compression
        this.compressionIn = CompressionProvider.NONE;
        this.compressionOut = CompressionProvider.NONE;

        long acceptedAt = System.nanoTime();
        this.lastIngressRefillNanos = acceptedAt;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf payload) throws Exception {
        if (this.disconnectReason != null) {
            return;
        }
        int len = payload.readableBytes();
        if (len > NetherNetPlayerSession.MAX_INBOUND_WIRE_BYTES) {
            log.warn("[{}] Closing session: inbound message of {} bytes exceeds hard limit {}",
                    this.playerLabel(), len, NetherNetPlayerSession.MAX_INBOUND_WIRE_BYTES);
            this.disconnect("Too big packet");
            this.channel.close();
            return;
        }

        long ingressNowNanos = System.nanoTime();
        if (!this.reserveWireIngressBudget(len, ingressNowNanos)) {
            this.rejectWireIngress(len);
            return;
        }

        boolean ci = this.shouldUsePrefixedCompression();
        CompressionProvider receivedCompression = this.compressionIn;

        // Recovered consumer capacity may free a full retained backlog before admission.
        this.drainDeferredInbound();
        if (this.disconnectReason != null) return;
        int payloadBytes = len;
        if (!this.inboundBacklog.canOffer(payloadBytes)) {
            this.rejectInboundBacklog(payloadBytes);
            return;
        }
        byte[] packetBuffer = new byte[payloadBytes];
        payload.readBytes(packetBuffer);
        if (!this.inboundBacklog.offer(packetBuffer, ci, receivedCompression)) {
            // Disconnect may close the backlog while the event loop copies this frame.
            return;
        }
        this.drainDeferredInbound();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.server.queueSessionForPlayerCreation(this);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.disconnect("transport:closed_by_remote_peer");
    }

    @Override
    public synchronized void disconnect(String reason) {
        if (this.disconnectReason != null) {
            return;
        }

        this.disconnectReason = reason;
        this.server.getNetwork().events().signal(this.pendingTask);
        this.inboundBacklog.close();
        if (this.tickFuture != null) {
            this.tickFuture.cancel(false);
        }

        // Give it a short time to make sure the disconnect message is delivered
        if (this.channel.isOpen()) {
            try {
                this.channel.eventLoop().schedule(() -> this.channel.close(), 10, TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException e) {
                this.channel.close();
            }
        }
    }

    @Override
    public void sendPacket(DataPacket packet) {
        if (!this.channel.isActive()) {
            return;
        }
        if (!(packet instanceof BatchPacket)) packet.tryEncode();
        if (packet instanceof ResourcePacksInfoPacket && this.resourcePackStartedNanos == 0) {
            this.resourcePackStartedNanos = System.nanoTime();
        }
        if (packet instanceof ResourcePackChunkDataPacket) this.lastLoginActivityNanos = System.nanoTime();
        this.outbound.offer(packet);
    }

    @Override
    public void sendImmediatePacket(DataPacket packet, Runnable callback) {
        if (!this.channel.isActive()) return;

        // Queue first so cached batches keep their payload and later packets cannot overtake them.
        this.sendPacket(packet);
        this.channel.eventLoop().execute(() -> {
            this.networkTick();
            if (callback != null) callback.run();
        });
    }

    private void networkTick() {
        if (!this.channel.isActive()) {
            // Inactive after once active is a remote close; never-active sessions belong to the
            // login timeout instead
            if (this.transportWasActive) {
                this.disconnect("transport:closed_by_remote_peer");
            }
            return;
        }
        this.transportWasActive = true;

        try {
            this.drainDeferredInbound();
            List<DataPacket> toBatch = new ObjectArrayList<>();
            DataPacket packet;
            while ((packet = this.outbound.poll()) != null) {
                if (packet instanceof DisconnectPacket) {
                    packet.tryEncode();
                    BinaryStream batched = new BinaryStream();
                    byte[] buf = packet.getBuffer();
                    batched.putUnsignedVarInt(buf.length);
                    batched.put(buf);

                    try {
                        this.sendPacket(this.compressionOut.compress(batched, Server.getInstance().getSettings().network().compression().compressionLevel()));
                    } catch (Exception e) {
                        log.error("Unable to compress disconnect packet", e);
                    }
                    return; // Disconnected
                } else if (packet instanceof BatchPacket) {
                    if (!toBatch.isEmpty()) {
                        this.sendPackets(toBatch);
                        toBatch.clear();
                    }

                    this.sendPacket(((BatchPacket) packet).payload);
                } else {
                    toBatch.add(packet);
                }
            }

            if (!toBatch.isEmpty()) {
                this.sendPackets(toBatch);
            }
        } catch (Throwable e) {
            log.error("[{}] Failed to tick NetherNetPlayerSession", this.channel.remoteAddress(), e);
        }
    }

    public NetherNetPlayerSession.InboundDrain serverTick(int packetBudget, long byteBudget) {
        return this.drainInbound(packetBudget, byteBudget, Long.MAX_VALUE, null);
    }

    public void stopProcessing() {
        this.processingClosed = true;
        this.inbound.clear();
    }

    public NetworkEventQueue.Result processInbound(long deadlineNanos, NetworkPacketBudget transportBudget) {
        this.drainInbound(32, MAX_INBOUND_BYTES_PER_SERVER_TICK, deadlineNanos, transportBudget);
        DataPacket next = this.inbound.peek();
        if (this.disconnectReason != null) return NetworkEventQueue.Result.MORE;
        if (next == null) return NetworkEventQueue.Result.DONE;
        int tick = this.server.getNetwork().events().tick();
        return !this.inboundBudget.allows(tick, packetBytes(next)) || !transportBudget.allows(tick, packetBytes(next))
                ? NetworkEventQueue.Result.NEXT_TICK : NetworkEventQueue.Result.MORE;
    }

    private InboundDrain drainInbound(int packetBudget, long byteBudget, long deadlineNanos, NetworkPacketBudget transportBudget) {
        DataPacket packet;
        int handled = 0;
        long handledBytes = 0L;
        int tick = this.server.getNetwork().events().tick();
        int allowedPackets = Math.min(NetherNetPlayerSession.MAX_INBOUND_PACKETS_PER_SERVER_TICK, Math.max(packetBudget, 0));
        long allowedBytes = Math.min(NetherNetPlayerSession.MAX_INBOUND_BYTES_PER_SERVER_TICK, Math.max(byteBudget, 0L));
        while (this.disconnectReason == null && handled < allowedPackets && System.nanoTime() < deadlineNanos
                && (packet = this.inbound.peek()) != null
                && handledBytes + packetBytes(packet) <= allowedBytes) {
            if (transportBudget != null && (!this.inboundBudget.allows(tick, packetBytes(packet))
                    || !transportBudget.allows(tick, packetBytes(packet)))) break;
            this.inbound.poll();
            this.queuedInboundPackets.decrementAndGet();
            int packetBytes = packetBytes(packet);
            this.queuedInboundBytes.addAndGet(-packetBytes);
            handled++;
            handledBytes += packetBytes;
            if (transportBudget != null) {
                this.inboundBudget.record(packetBytes);
                transportBudget.record(packetBytes);
            }
            try {
                if (packet instanceof LoginPacket) this.loginReceived = true;
                if (packet instanceof LoginPacket || packet instanceof RequestNetworkSettingsPacket
                        || packet instanceof ResourcePackClientResponsePacket || packet instanceof ResourcePackChunkRequestPacket) {
                    this.lastLoginActivityNanos = System.nanoTime();
                }
                this.player.handleDataPacket(packet);
            } catch (Throwable e) {
                log.error(new FormattedMessage("An error occurred whilst handling {} for {}",
                        new Object[]{packet.getClass().getSimpleName(), this.player.getName()}, e));
            }
        }
        if (this.inboundThrottled
                && this.queuedInboundPackets.get() <= NetherNetPlayerSession.INBOUND_LOW_WATER_PACKETS
                && this.queuedInboundBytes.get() <= NetherNetPlayerSession.INBOUND_LOW_WATER_BYTES) {
            this.inboundThrottled = false;
            this.resumeDeferredInbound();
        }
        return new NetherNetPlayerSession.InboundDrain(handled, handledBytes);
    }

    private void sendPackets(Collection<DataPacket> packets) {
        BinaryStream batched = new BinaryStream();
        for (DataPacket packet : packets) {
            if (packet instanceof BatchPacket) {
                throw new IllegalArgumentException("Cannot batch BatchPacket");
            }
            packet.tryEncode();

            byte[] buf = packet.getBuffer();
            if (batched.getCount() + buf.length > 3145728) { // 3 * 1024 * 1024
                this.sendPackets(batched);
                batched = new BinaryStream();
            }
            batched.putUnsignedVarInt(buf.length);
            batched.put(buf);
        }

        this.sendPackets(batched);
    }

    private void sendPackets(BinaryStream batched) {
        try {
            this.sendPacket(this.compressionOut.compress(batched, Server.getInstance().getSettings().network().compression().compressionLevel()));
        } catch (Exception e) {
            log.error("Unable to compress batched packets", e);
        }
    }

    /**
     * Writes one compressed batch: exactly the bytes RakNet puts behind its {@code 0xFE} frame id.
     */
    private ChannelFuture sendPacket(byte[] compressedPayload) {
        boolean ci = this.shouldUsePrefixedCompression();

        ByteBuf finalPayload = Unpooled.directBuffer((ci ? 1 : 0) + compressedPayload.length);
        if (ci) {
            finalPayload.writeByte(this.compressionOut.getPrefix());
        }
        finalPayload.writeBytes(compressedPayload);

        return this.channel.writeAndFlush(finalPayload);
    }

    @Override
    public void setCompression(CompressionProvider compression) {
        Preconditions.checkNotNull(compression, "compression");
        this.compressionIn = compression;
        this.compressionOut = compression;
        this.compressionInitialized = true;
    }

    @Override
    public CompressionProvider getCompression() {
        return this.compressionOut;
    }

    public void setPlayer(Player player) {
        Preconditions.checkArgument(this.player == null && player != null);
        this.player = player;
        this.lastLoginActivityNanos = System.nanoTime();
        this.server.getNetwork().events().signal(this.pendingTask);
        this.resumeDeferredInbound();
    }

    public boolean markQueuedForPlayerCreation() {
        return this.queuedForPlayerCreation.compareAndSet(false, true);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public String checkIdentityBinding(String identityPublicKey) {
        return this.server.checkIdentityBinding(this, identityPublicKey);
    }

    public NetherNetChildChannel getChannel() {
        return this.channel;
    }

    public String getDisconnectReason() {
        return this.disconnectReason;
    }

    public boolean isPendingLoginTimedOut(long nowNanos, int timeoutMillis) {
        return timeoutMillis > 0 && this.disconnectReason == null
                && nowNanos - this.acceptedAtNanos >= TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    }

    public boolean isLoginPhaseTimedOut(long nowNanos, int timeoutMillis) {
        if (timeoutMillis <= 0 || this.disconnectReason != null || this.isPlayingSession()) return false;
        // An active resource pack transfer can outlive the ordinary login timeout.
        if (this.resourcePackStartedNanos != 0 && this.channel.isActive()
                && nowNanos - this.resourcePackStartedNanos < TimeUnit.MINUTES.toNanos(10)) return false;
        return nowNanos - this.lastLoginActivityNanos >= TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    }

    /**
     * Bedrock packet encryption must never run here: DTLS already secures the session.
     */
    @Override
    public void setEncryption(javax.crypto.SecretKey encryptionKey, javax.crypto.Cipher encryptionCipher, javax.crypto.Cipher decryptionCipher) {
        log.warn("Ignoring Bedrock packet encryption for {}: NetherNet already carries the session inside DTLS", this.playerLabel());
    }

    @Override
    public long getPing() {
        return this.channel.getPing();
    }

    private int getBedrockProtocol() {
        return this.player == null ? Integer.MAX_VALUE : this.player.protocol;
    }

    private boolean shouldUsePrefixedCompression() {
        int protocol = this.getBedrockProtocol();
        return this.compressionInitialized
                && protocol != Integer.MAX_VALUE
                && protocol >= ProtocolInfo.v1_20_60;
    }

    private String playerLabel() {
        return this.player == null ? String.valueOf(this.channel.remoteAddress()) : this.player.getName();
    }

    private InboundBatchDecodeResult processInboundBatch(byte[] packetBuffer, boolean prefixed, CompressionProvider compression) {
        if (prefixed) {
            if (packetBuffer.length < 2) return InboundBatchDecodeResult.failed(0, 0, null);
            try {
                compression = CompressionProvider.byPrefix(packetBuffer[0], RAKNET_PROTOCOL);
            } catch (IllegalArgumentException e) {
                return InboundBatchDecodeResult.failed(0, 0, e);
            }
            packetBuffer = Arrays.copyOfRange(packetBuffer, 1, packetBuffer.length);
        }
        List<DataPacket> decoded = new ObjectArrayList<>();
        Network.BatchProcessResult measured = this.server.getNetwork().processBatchMeasured(
                packetBuffer, decoded, compression, RAKNET_PROTOCOL, this.player, false);
        if (!measured.success()) {
            this.logMalformedBatch(measured.failure(), "[{}] Failed to decode batch ({} bytes)", this.playerLabel(), packetBuffer.length);
            return InboundBatchDecodeResult.failed(measured.decompressedBytes(), measured.framedPackets(), measured.failure());
        }
        this.compressionIn = compression;
        return InboundBatchDecodeResult.legacy(compression, decoded, measured.decompressedBytes(), measured.framedPackets());
    }

    private boolean isPlayingSession() {
        return this.player != null && this.player.loggedIn && this.disconnectReason == null;
    }

    boolean keepPlayingSessionAfterMalformedBatch(long nowNanos) {
        if (!this.isPlayingSession()) return false;
        long cutoff = nowNanos - MALFORMED_BATCH_WINDOW_NANOS;
        while (!this.malformedBatches.isEmpty() && this.malformedBatches.peekFirst() <= cutoff) {
            this.malformedBatches.removeFirst();
        }
        this.malformedBatches.addLast(nowNanos);
        return this.malformedBatches.size() <= MAX_MALFORMED_BATCHES_WHILE_PLAYING;
    }

    boolean enqueueDecodedBatch(List<DataPacket> packets) {
        if (packets.isEmpty()) {
            return true;
        }
        long batchBytes = 0L;
        for (DataPacket packet : packets) {
            batchBytes += packetBytes(packet);
        }
        int queuedPackets = this.queuedInboundPackets.get();
        long queuedBytes = this.queuedInboundBytes.get();
        if (queuedPackets + packets.size() > NetherNetPlayerSession.MAX_QUEUED_INBOUND_PACKETS
                || queuedBytes + batchBytes > NetherNetPlayerSession.MAX_QUEUED_INBOUND_BYTES) {
            this.inboundThrottled = true;
            this.logInboundThrottle(queuedPackets, queuedBytes, packets.size(), batchBytes);
            return false;
        }

        this.queuedInboundPackets.addAndGet(packets.size());
        this.queuedInboundBytes.addAndGet(batchBytes);
        this.inbound.addAll(packets);
        if (queuedPackets + packets.size() >= NetherNetPlayerSession.MAX_QUEUED_INBOUND_PACKETS
                || queuedBytes + batchBytes >= NetherNetPlayerSession.MAX_QUEUED_INBOUND_BYTES) {
            this.inboundThrottled = true;
        }
        this.server.getNetwork().events().signal(this.pendingTask);
        return true;
    }

    boolean reserveWireIngressBudget(int wireBytes, long nowNanos) {
        this.refillIngressBudget(nowNanos);
        if (this.ingressBatchTokens < 1D
                || this.ingressCompressedBytes < wireBytes) {
            return false;
        }
        this.ingressBatchTokens -= 1D;
        this.ingressCompressedBytes -= wireBytes;
        return true;
    }

    boolean reserveDecodeIngressBudget(long nowNanos) {
        this.refillIngressBudget(nowNanos);
        if (this.ingressDecodeBytes < NetherNetPlayerSession.DECODE_BYTES_RESERVATION
                || this.ingressFrameTokens < NetherNetPlayerSession.FRAME_TOKENS_RESERVATION) {
            return false;
        }
        this.ingressDecodeBytes -= NetherNetPlayerSession.DECODE_BYTES_RESERVATION;
        this.ingressFrameTokens -= NetherNetPlayerSession.FRAME_TOKENS_RESERVATION;
        return true;
    }

    void settleIngressBudget(int decodedBytes, int framedPackets, long nowNanos) {
        this.refillIngressBudget(nowNanos);
        this.ingressDecodeBytes = Math.max(0D, Math.min(NetherNetPlayerSession.MAX_INGRESS_DECODE_BYTES,
                this.ingressDecodeBytes + NetherNetPlayerSession.DECODE_BYTES_RESERVATION - decodedBytes));
        this.ingressFrameTokens = Math.max(0D, Math.min(NetherNetPlayerSession.MAX_INGRESS_FRAME_TOKENS,
                this.ingressFrameTokens + NetherNetPlayerSession.FRAME_TOKENS_RESERVATION - framedPackets));
    }

    private void refillIngressBudget(long nowNanos) {
        if (nowNanos <= this.lastIngressRefillNanos) {
            return;
        }
        double elapsedSeconds = (nowNanos - this.lastIngressRefillNanos) / 1_000_000_000D;
        this.lastIngressRefillNanos = nowNanos;
        this.ingressBatchTokens = Math.min(NetherNetPlayerSession.MAX_INGRESS_BATCH_TOKENS,
                this.ingressBatchTokens + elapsedSeconds * NetherNetPlayerSession.INGRESS_BATCH_TOKENS_PER_SECOND);
        this.ingressCompressedBytes = Math.min(NetherNetPlayerSession.MAX_INGRESS_COMPRESSED_BYTES,
                this.ingressCompressedBytes + elapsedSeconds * NetherNetPlayerSession.INGRESS_COMPRESSED_BYTES_PER_SECOND);
        this.ingressDecodeBytes = Math.min(NetherNetPlayerSession.MAX_INGRESS_DECODE_BYTES,
                this.ingressDecodeBytes + elapsedSeconds * NetherNetPlayerSession.INGRESS_DECODE_BYTES_PER_SECOND);
        this.ingressFrameTokens = Math.min(NetherNetPlayerSession.MAX_INGRESS_FRAME_TOKENS,
                this.ingressFrameTokens + elapsedSeconds * NetherNetPlayerSession.INGRESS_FRAME_TOKENS_PER_SECOND);
    }

    private void rejectWireIngress(int wireBytes) {
        log.warn("[{}] Wire ingress budget exhausted (wireBytes={}); closing",
                this.playerLabel(), wireBytes);
        this.disconnect("Too much inbound data");
        this.channel.close();
    }

    private static int packetBytes(DataPacket packet) {
        return Math.max(packet.getCount(), 1);
    }

    /** Resume decoding promptly when main-thread consumption frees queue capacity. */
    private void resumeDeferredInbound() {
        try {
            this.channel.eventLoop().execute(this::drainDeferredInbound);
        } catch (RejectedExecutionException ignored) {
            // Event loop shutdown owns the remaining backlog.
        }
    }

    /** Called only on the event loop. Never decrypt retained payloads a second time. */
    void drainDeferredInbound() {
        if (this.player == null) return;
        while (this.disconnectReason == null) {
            List<DataPacket> pending = this.inboundBacklog.pendingDecoded();
            if (pending != null) {
                if (!this.enqueueDecodedBatch(pending)) return;
                this.inboundBacklog.clearDecoded();
            }
            if (this.inboundThrottled || this.inboundBacklog.isEmpty()) return;
            long nowNanos = System.nanoTime();
            if (!this.reserveDecodeIngressBudget(nowNanos)) return;
            InboundBatchBacklog.EncodedBatch batch = this.inboundBacklog.poll();
            if (batch == null) return;
            NetherNetPlayerSession.InboundBatchDecodeResult decoded = this.processInboundBatch(batch.payload(), batch.prefixed(), batch.compression());
            this.settleIngressBudget(decoded.workBytes(), decoded.framedPackets(), nowNanos);
            if (decoded.success()) {
                if (!this.enqueueDecodedBatch(decoded.packets())) {
                    // At most one legal batch: 1300 packets / 6 MiB. Later batches stay encoded.
                    this.inboundBacklog.holdDecoded(decoded.packets());
                    return;
                }
            } else {
                this.handleMalformedInboundBatch(nowNanos);
            }
        }
    }

    private void handleMalformedInboundBatch(long nowNanos) {
        if (this.isPlayingSession()) {
            if (!this.keepPlayingSessionAfterMalformedBatch(nowNanos)) this.disconnect("Sent malformed packet");
            return;
        }
        InetAddress address = this.channel.remoteAddress() instanceof java.net.InetSocketAddress remote
                ? remote.getAddress() : null;
        this.disconnect("Sent malformed packet");
        if (this.loginReceived && address != null && !address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
            this.server.blockAddress(address, 60);
        }
    }

    private void rejectInboundBacklog(int payloadBytes) {
        log.warn("[{}] Reliable inbound backlog exhausted (payloadBytes={}, batches={}, bytes={}); closing session",
                this.playerLabel(), payloadBytes, this.inboundBacklog.batchCount(), this.inboundBacklog.byteCount());
        this.disconnect("Reliable inbound backlog exhausted");
        this.channel.close();
    }

    private void logInboundThrottle(int queuedPackets, long queuedBytes, int batchPackets, long batchBytes) {
        this.throttledInboundBatches++;
        long nowNanos = System.nanoTime();
        if (this.lastInboundThrottleLogNanos != 0L
                && nowNanos - this.lastInboundThrottleLogNanos < NetherNetPlayerSession.DIAGNOSTIC_LOG_INTERVAL_NANOS) {
            return;
        }
        this.lastInboundThrottleLogNanos = nowNanos;
        log.warn("[{}] Inbound backpressure deferred decoded batch (queuedPackets={}, queuedBytes={}, batchPackets={}, batchBytes={}, deferredAttempts={})",
                this.playerLabel(), queuedPackets, queuedBytes, batchPackets, batchBytes,
                this.throttledInboundBatches);
    }

    private void logMalformedBatch(Throwable failure, String message, Object... arguments) {
        long nowNanos = System.nanoTime();
        if (this.lastMalformedLogNanos != 0L
                && nowNanos - this.lastMalformedLogNanos < NetherNetPlayerSession.DIAGNOSTIC_LOG_INTERVAL_NANOS) {
            return;
        }
        this.lastMalformedLogNanos = nowNanos;
        Object[] withCause = Arrays.copyOf(arguments, arguments.length + 1);
        withCause[arguments.length] = failure == null ? "unknown" : failure.toString();
        log.warn(message + ", cause={}", withCause);
    }

    public record InboundDrain(int packets, long bytes) {
    }

    record InboundBatchDecodeResult(boolean success, CompressionProvider compression, boolean prefixed,
                                    List<DataPacket> packets, int workBytes, int framedPackets,
                                    Throwable failure) {

        static InboundBatchDecodeResult failed(int workBytes, int framedPackets, Throwable failure) {
            return new InboundBatchDecodeResult(
                    false, null, false, List.of(), workBytes, framedPackets, failure);
        }

        static InboundBatchDecodeResult prefixed(CompressionProvider compression, List<DataPacket> packets,
                                                 int workBytes, int framedPackets) {
            return new InboundBatchDecodeResult(
                    true, compression, true, packets, workBytes, framedPackets, null);
        }

        static InboundBatchDecodeResult legacy(CompressionProvider compression, List<DataPacket> packets,
                                               int workBytes, int framedPackets) {
            return new InboundBatchDecodeResult(
                    true, compression, false, packets, workBytes, framedPackets, null);
        }
    }
}
