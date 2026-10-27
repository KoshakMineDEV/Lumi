package cn.nukkit.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.log4j.Log4j2;
import org.cloudburstmc.netty.channel.nethernet.signaling.NetherNetServerSignaling;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * In-process UDP relay so NetherNet media shares server-port with RakNet. libjuice owns its own
 * socket with no injection API, so the two transports cannot share one UDP socket; instead STUN/DTLS
 * datagrams arriving on the RakNet listener (RFC 7983 first-byte demux, STUN additionally checked
 * for the magic cookie to tell it from RakNet's 0x01/0x02 pings) are forwarded per client over a
 * loopback "leg" to libjuice's ICE mux port, and its replies travel back the same leg onto the
 * RakNet socket. libjuice demultiplexes by source address and ufrag only, never by destination, so
 * the leg passes as the peer. A leg opens only for a STUN Binding Request carrying the local ufrag
 * of a pending answer; anything else from an unknown source is dropped. Legs are capped in total
 * and per address and reaped when idle. The child channel's remote is whatever the library derives
 * from the signaling source (the real client; Netty caches it from the first read and never
 * re-reads), so no swap is needed; {@link #clientFor} maps a leg address back to its client for
 * diagnostics. The internal port can be held through {@link #reserveMediaPort} until the first offer.
 */
@Log4j2
public final class NetherNetMediaRelay {

    static final String HANDLER_NAME = "nethernet-media-relay";
    static final long DEFAULT_IDLE_TTL_NANOS = TimeUnit.SECONDS.toNanos(60);
    static final long SWEEP_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(10);
    /**
     * One address may hide a LAN party and every ICE retry changes the source port; the ufrag gate
     * already stops abuse, this is only a backstop.
     */
    static final int MAX_LEGS_PER_ADDRESS = 64;

    static final long UFRAG_TTL_NANOS = TimeUnit.SECONDS.toNanos(120);

    private static final int STUN_MAGIC_COOKIE = 0x2112A442;
    private static final int STUN_HEADER_LENGTH = 20;
    private static final int STUN_BINDING_REQUEST = 0x0001;
    private static final int STUN_ATTR_USERNAME = 0x0006;
    private static final int STUN_MAX_USERNAME_LENGTH = 512;
    private static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();

    enum Kind {
        STUN, DTLS, OTHER
    }

    public record Snapshot(int legs, long legsOpened, long packetsIn, long bytesIn, long packetsOut, long bytesOut,
                           long rejectedUfrag, long rejectedLimit, long rejectedBlocked, long droppedNoLeg) {
    }

    private final InetSocketAddress mediaEndpoint;
    private final int maxLegs;
    private final long idleTtlNanos;
    private final Predicate<InetAddress> addressBlocked;

    private final Map<InetSocketAddress, Leg> legsByClient = new ConcurrentHashMap<>();
    private final Map<Integer, Leg> legsByLocalPort = new ConcurrentHashMap<>();
    private final Map<InetAddress, Integer> legsPerAddress = new ConcurrentHashMap<>();

    private final Map<String, Long> expectedUfrags = new ConcurrentHashMap<>();
    private final List<Channel> listeners = new CopyOnWriteArrayList<>();

    private final LongAdder legsOpened = new LongAdder();
    private final LongAdder packetsIn = new LongAdder();
    private final LongAdder bytesIn = new LongAdder();
    private final LongAdder packetsOut = new LongAdder();
    private final LongAdder bytesOut = new LongAdder();
    private final LongAdder rejectedUfrag = new LongAdder();
    private final LongAdder rejectedLimit = new LongAdder();
    private final LongAdder rejectedBlocked = new LongAdder();
    private final LongAdder droppedNoLeg = new LongAdder();

    private volatile ScheduledFuture<?> sweepTask;
    private volatile boolean closed;

    private final AtomicReference<DatagramSocket> reservation = new AtomicReference<>();

    public NetherNetMediaRelay(int mediaPort, int maxLegs, Predicate<InetAddress> addressBlocked) {
        this(mediaPort, maxLegs, addressBlocked, DEFAULT_IDLE_TTL_NANOS);
    }

    /**
     * Takes a port reserved through {@link #reserveMediaPort} as the internal port and owns the
     * reservation, letting go right before the first offer enters the library or on shutdown.
     */
    public NetherNetMediaRelay(DatagramSocket reservedMediaPort, int maxLegs, Predicate<InetAddress> addressBlocked) {
        this(reservedMediaPort.getLocalPort(), maxLegs, addressBlocked, DEFAULT_IDLE_TTL_NANOS);
        this.reservation.set(reservedMediaPort);
    }

    NetherNetMediaRelay(int mediaPort, int maxLegs, Predicate<InetAddress> addressBlocked, long idleTtlNanos) {
        this.mediaEndpoint = new InetSocketAddress(LOOPBACK, mediaPort);
        this.maxLegs = maxLegs;
        this.addressBlocked = addressBlocked;
        this.idleTtlNanos = idleTtlNanos;
    }

    /**
     * Reserves the internal media port on loopback. libjuice binds it only when the first
     * PeerConnection gathers candidates, leaving it free for anyone else (a second instance on
     * the host, say) to grab until then; a plain UDP socket both surfaces a port already in use
     * (throws {@link IOException}) and keeps it from being taken between startup and the first
     * player. libjuice sets no SO_REUSEADDR so it cannot bind while the reservation stands; the
     * release and its bind happen in the same new-connection callback, a negligible gap.
     */
    public static DatagramSocket reserveMediaPort(int port) throws IOException {
        return new DatagramSocket(new InetSocketAddress(LOOPBACK, port));
    }

    /**
     * Reserves a system-assigned loopback port (the ephemeral range): instances on one
     * host spread out naturally with no fixed window to exhaust; reservation semantics
     * as in {@link #reserveMediaPort(int)}.
     */
    public static DatagramSocket reserveMediaPort() throws IOException {
        return new DatagramSocket(new InetSocketAddress(LOOPBACK, 0));
    }

    void releaseMediaPort() {
        DatagramSocket held = this.reservation.getAndSet(null);
        if (held != null) {
            held.close();
        }
    }

    boolean holdsMediaPort() {
        return this.reservation.get() != null;
    }

    public InetSocketAddress mediaEndpoint() {
        return this.mediaEndpoint;
    }

    /**
     * Hooks into RakNet's raw datagram pipeline before its packet decoder.
     */
    public void attach(Channel listener) {
        // A handler added from off the event loop gets its handlerAdded deferred to a later
        // event-loop task, and messages dispatched before it runs skip the handler straight
        // into RakNet's arms; mount on the event loop and wait for it to take effect
        Runnable mount = () -> {
            ChannelPipeline pipeline = listener.pipeline();
            Demux demux = new Demux(listener);
            pipeline.addFirst(HANDLER_NAME, demux);
        };
        if (listener.eventLoop().inEventLoop()) {
            mount.run();
        } else {
            listener.eventLoop().submit(mount).syncUninterruptibly();
        }
        this.listeners.add(listener);
        if (this.sweepTask == null) {
            this.sweepTask = listener.eventLoop().scheduleAtFixedRate(this::sweep,
                    SWEEP_INTERVAL_MILLIS, SWEEP_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Wraps the signaling: offers lose their candidates (unreachable from loopback), answers pass
     * through {@code answerRewriter} and register their local ufrag.
     */
    public NetherNetServerSignaling decorate(NetherNetServerSignaling delegate, UnaryOperator<String> answerRewriter) {
        return new SharedPortSignaling(delegate, answerRewriter);
    }

    public void expectUfrag(String ufrag) {
        if (ufrag == null || ufrag.isEmpty()) {
            return;
        }
        this.expectedUfrags.put(ufrag, System.nanoTime() + UFRAG_TTL_NANOS);
    }

    boolean isExpected(String ufrag) {
        Long expiry = this.expectedUfrags.get(ufrag);
        return expiry != null && System.nanoTime() - expiry < 0;
    }

    /**
     * The real client behind a loopback leg address, or null when the address is not a leg.
     * A matching port alone is not enough: the queried address must equal the leg's bound
     * address, so other loopback sockets sharing the port number never map. Diagnostics and
     * tests only.
     */
    public InetSocketAddress clientFor(SocketAddress address) {
        if (!(address instanceof InetSocketAddress inet) || inet.getAddress() == null || !inet.getAddress().isLoopbackAddress()) {
            return null;
        }
        Leg leg = this.legsByLocalPort.get(inet.getPort());
        if (leg == null) {
            return null;
        }
        return leg.channel.localAddress() instanceof InetSocketAddress bound
                && bound.getAddress() != null && bound.getAddress().equals(inet.getAddress())
                ? leg.client : null;
    }

    public Snapshot snapshot() {
        return new Snapshot(this.legsByClient.size(), this.legsOpened.sum(), this.packetsIn.sum(), this.bytesIn.sum(),
                this.packetsOut.sum(), this.bytesOut.sum(), this.rejectedUfrag.sum(), this.rejectedLimit.sum(),
                this.rejectedBlocked.sum(), this.droppedNoLeg.sum());
    }

    int legCount() {
        return this.legsByClient.size();
    }

    public void shutdown() {
        this.closed = true;
        this.releaseMediaPort();
        ScheduledFuture<?> sweep = this.sweepTask;
        if (sweep != null) {
            sweep.cancel(false);
        }
        for (Channel listener : this.listeners) {
            ChannelPipeline pipeline = listener.pipeline();
            if (pipeline.get(HANDLER_NAME) != null) {
                try {
                    pipeline.remove(HANDLER_NAME);
                } catch (Exception ignored) {
                }
            }
        }
        this.listeners.clear();
        for (Leg leg : new ArrayList<>(this.legsByClient.values())) {
            leg.close();
        }
        this.expectedUfrags.clear();
    }

    static Kind classify(ByteBuf buf) {
        int readable = buf.readableBytes();
        if (readable == 0) {
            return Kind.OTHER;
        }
        int first = buf.getUnsignedByte(buf.readerIndex());
        if (first <= 3) {
            // RakNet's 0x01/0x02 pings carry a timestamp here, the magic cookie tells them apart
            return readable >= STUN_HEADER_LENGTH && buf.getInt(buf.readerIndex() + 4) == STUN_MAGIC_COOKIE
                    ? Kind.STUN : Kind.OTHER;
        }
        if (first >= 20 && first <= 63) {
            return Kind.DTLS;
        }
        return Kind.OTHER;
    }

    /**
     * The local half of a STUN Binding Request USERNAME ("local:remote"); null for anything else.
     */
    static String stunLocalUfrag(ByteBuf buf) {
        int start = buf.readerIndex();
        int readable = buf.readableBytes();
        if (readable < STUN_HEADER_LENGTH || buf.getUnsignedShort(start) != STUN_BINDING_REQUEST) {
            return null;
        }
        int length = buf.getUnsignedShort(start + 2);
        if (STUN_HEADER_LENGTH + length > readable) {
            return null;
        }
        int offset = start + STUN_HEADER_LENGTH;
        int end = offset + length;
        while (offset + 4 <= end) {
            int type = buf.getUnsignedShort(offset);
            int valueLength = buf.getUnsignedShort(offset + 2);
            offset += 4;
            if (offset + valueLength > end) {
                return null;
            }
            if (type == STUN_ATTR_USERNAME) {
                if (valueLength > STUN_MAX_USERNAME_LENGTH) {
                    return null;
                }
                String username = buf.toString(offset, valueLength, StandardCharsets.UTF_8);
                int colon = username.indexOf(':');
                return colon <= 0 ? null : username.substring(0, colon);
            }
            offset += (valueLength + 3) & ~3;
        }
        return null;
    }

    private void forward(Channel listener, InetSocketAddress client, ByteBuf content, Kind kind, Set<Leg> touched) {
        Leg leg = this.legsByClient.get(client);
        if (leg == null) {
            if (kind != Kind.STUN) {
                this.droppedNoLeg.increment();
                return;
            }
            String ufrag = stunLocalUfrag(content);
            if (ufrag == null || !this.isExpected(ufrag)) {
                this.rejectedUfrag.increment();
                return;
            }
            if (this.addressBlocked.test(client.getAddress())) {
                this.rejectedBlocked.increment();
                return;
            }
            leg = this.openLeg(listener, client);
            if (leg == null) {
                this.rejectedLimit.increment();
                return;
            }
        }
        this.packetsIn.increment();
        this.bytesIn.add(content.readableBytes());
        leg.send(content.retain());
        touched.add(leg);
    }

    private Leg openLeg(Channel listener, InetSocketAddress client) {
        if (this.closed || this.legsByClient.size() >= this.maxLegs
                || this.legsPerAddress.getOrDefault(client.getAddress(), 0) >= MAX_LEGS_PER_ADDRESS) {
            return null;
        }

        Leg leg = new Leg(listener, client);
        // The loopback may be ::1 under preferIPv6Addresses; the leg must match its family to bind/connect
        InternetProtocolFamily family = this.mediaEndpoint.getAddress() instanceof Inet6Address
                ? InternetProtocolFamily.IPv6 : InternetProtocolFamily.IPv4;
        ChannelFactory<? extends DatagramChannel> factory = listener instanceof EpollDatagramChannel
                ? () -> new EpollDatagramChannel(family)
                : () -> new NioDatagramChannel(family);
        ChannelFuture bound = new Bootstrap()
                .group(listener.eventLoop())
                .channelFactory(factory)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        channel.pipeline().addLast(new LegHandler(leg));
                    }
                })
                .bind(new InetSocketAddress(LOOPBACK, 0));
        leg.channel = bound.channel();
        // Registration on the listener's loop is synchronous, so unregistered means creation failed
        // (fd exhaustion, say) and the close future must not be listened on
        if (!leg.channel.isRegistered()) {
            log.warn("Unable to create a NetherNet relay leg for {}", client, bound.cause());
            leg.channel.close();
            return null;
        }
        this.legsPerAddress.merge(client.getAddress(), 1, Integer::sum);
        this.legsByClient.put(client, leg);
        this.legsOpened.increment();
        leg.channel.closeFuture().addListener(future -> this.onLegClosed(leg));

        bound.addListener(bind -> {
            if (!bind.isSuccess()) {
                log.warn("Unable to open a NetherNet relay leg for {}", client, bind.cause());
                leg.close();
                return;
            }
            InetSocketAddress local = (InetSocketAddress) leg.channel.localAddress();
            this.legsByLocalPort.put(local.getPort(), leg);
            leg.channel.connect(this.mediaEndpoint).addListener(connect -> {
                if (!connect.isSuccess()) {
                    log.warn("Unable to connect a NetherNet relay leg to {}", this.mediaEndpoint, connect.cause());
                    leg.close();
                    return;
                }
                leg.ready();
            });
        });
        return leg;
    }

    private void onLegClosed(Leg leg) {
        this.legsByClient.remove(leg.client, leg);
        if (leg.channel.localAddress() instanceof InetSocketAddress local) {
            this.legsByLocalPort.remove(local.getPort(), leg);
        }
        this.legsPerAddress.computeIfPresent(leg.client.getAddress(), (address, held) -> held <= 1 ? null : held - 1);
        leg.releasePending();
    }

    void sweep() {
        long now = System.nanoTime();
        for (Leg leg : this.legsByClient.values()) {
            if (now - leg.lastActivityNanos > this.idleTtlNanos) {
                leg.close();
            }
        }
        this.expectedUfrags.values().removeIf(expiry -> now - expiry >= 0);
    }

    /**
     * One loopback leg to libjuice. Touched only on the listener's event loop, which the leg
     * channel also registers on, so no locking; the sweep only calls the thread-safe close.
     */
    private final class Leg {
        final Channel listener;
        final InetSocketAddress client;
        Channel channel;
        volatile long lastActivityNanos = System.nanoTime();
        private final List<ByteBuf> pending = new ArrayList<>(4);
        private boolean ready;

        Leg(Channel listener, InetSocketAddress client) {
            this.listener = listener;
            this.client = client;
        }

        void send(ByteBuf payload) {
            this.lastActivityNanos = System.nanoTime();
            if (!this.ready) {
                if (!this.channel.isOpen()) {
                    payload.release();
                    return;
                }
                this.pending.add(payload);
                return;
            }
            this.channel.write(payload);
        }

        void flush() {
            if (this.ready) {
                this.channel.flush();
            }
        }

        void ready() {
            this.ready = true;
            for (ByteBuf payload : this.pending) {
                this.channel.write(payload);
            }
            this.pending.clear();
            this.channel.flush();
        }

        void releasePending() {
            for (ByteBuf payload : this.pending) {
                payload.release();
            }
            this.pending.clear();
        }

        void close() {
            this.channel.close();
        }
    }

    private final class LegHandler extends ChannelInboundHandlerAdapter {
        private final Leg leg;

        LegHandler(Leg leg) {
            this.leg = leg;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (!(msg instanceof DatagramPacket packet)) {
                ReferenceCountUtil.release(msg);
                return;
            }
            try {
                ByteBuf content = packet.content();
                this.leg.lastActivityNanos = System.nanoTime();
                NetherNetMediaRelay.this.packetsOut.increment();
                NetherNetMediaRelay.this.bytesOut.add(content.readableBytes());
                this.leg.listener.write(new DatagramPacket(content.retain(), this.leg.client));
            } finally {
                packet.release();
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            this.leg.listener.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof PortUnreachableException) {
                // The media socket is gone (libjuice closed), the leg has nothing to talk to
                this.leg.close();
                return;
            }
            log.debug("NetherNet relay leg for {} failed", this.leg.client, cause);
        }
    }

    /**
     * Demultiplexer on the listener: NetherNet datagrams go to legs, everything else continues to
     * RakNet untouched; legs written during one read loop flush once on readComplete.
     */
    private final class Demux extends ChannelInboundHandlerAdapter {
        private final Channel listener;
        private final Set<Leg> touched = new LinkedHashSet<>();

        Demux(Channel listener) {
            this.listener = listener;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (!(msg instanceof DatagramPacket packet)) {
                ctx.fireChannelRead(msg);
                return;
            }
            Kind kind = classify(packet.content());
            if (kind == Kind.OTHER) {
                ctx.fireChannelRead(msg);
                return;
            }
            try {
                NetherNetMediaRelay.this.forward(this.listener, packet.sender(), packet.content(), kind, this.touched);
            } finally {
                packet.release();
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            for (Leg leg : this.touched) {
                leg.flush();
            }
            this.touched.clear();
            ctx.fireChannelReadComplete();
        }
    }

    private final class SharedPortSignaling extends NetherNetDelegatingSignaling {
        private final UnaryOperator<String> answerRewriter;

        SharedPortSignaling(NetherNetServerSignaling delegate, UnaryOperator<String> answerRewriter) {
            super(delegate);
            this.answerRewriter = answerRewriter;
        }

        @Override
        public void setNewConnectionHandler(NewConnectionHandler handler) {
            this.delegate.setNewConnectionHandler((connectionId, remoteNetworkId, payload, clientAddress, player) -> {
                // The library creates the PeerConnection and binds the internal port inside this
                // callback, so the reservation has to step aside first
                NetherNetMediaRelay.this.releaseMediaPort();
                handler.onConnect(connectionId, remoteNetworkId, NetherNetSharedPortSdp.stripCandidates(payload),
                        clientAddress, player);
            });
        }

        @Override
        public void sendDescription(String targetNetworkId, String sdp) {
            NetherNetMediaRelay.this.expectUfrag(NetherNetSharedPortSdp.iceUfrag(sdp));
            this.delegate.sendDescription(targetNetworkId, this.answerRewriter.apply(sdp));
        }
    }
}
