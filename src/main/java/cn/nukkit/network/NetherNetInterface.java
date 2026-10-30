package cn.nukkit.network;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerCreationEvent;
import cn.nukkit.event.server.QueryRegenerateEvent;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.session.NetherNetPlayerSession;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.settings.NetherNetSettings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.log4j.Log4j2;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChannelFactory;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChildChannel;
import org.cloudburstmc.netty.channel.nethernet.config.NetherChannelOption;
import org.cloudburstmc.netty.channel.nethernet.signaling.JoinRefusal;
import org.cloudburstmc.netty.channel.nethernet.signaling.NetherNetHTTPServerSignaling;
import org.cloudburstmc.netty.channel.nethernet.signaling.NetherNetServerSignaling;
import org.cloudburstmc.netty.channel.nethernet.signaling.PongData;
import org.cloudburstmc.netty.util.nethernet.NetherNetLogging;
import org.cloudburstmc.netty.util.nethernet.TokenTrust;
import org.cloudburstmc.netty.util.nethernet.TransportIdentityBinding;
import tel.schich.libdatachannel.LibDataChannelArchDetect;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WebRTC transport with HTTP signaling and an optional relay on RakNet's UDP listener.
 * Adapted from Nukkit-MOT and PowerNukkitX NetherNet support (built on CloudburstMC netty-transport-nethernet
 * and WaterdogPE's signaling work).
 */
@Log4j2
public class NetherNetInterface implements AdvancedSourceInterface {

    private static final int DEFAULT_HANDSHAKE_TIMEOUT_SECONDS = 30;

    /**
     * Media-reachability alarm: fires once when attempts reach the floor with zero ICE
     * successes in the window, recovers once when a connection succeeds, no repeat while held.
     */
    static final int MEDIA_ALERT_WINDOW_MINUTES = 5;
    static final long MEDIA_ALERT_MIN_ATTEMPTS = 5;

    private final Server server;
    private Network network;

    private final Channel channel;
    private final EventLoopGroup eventLoopGroup;
    private final NetherNetHTTPServerSignaling signaling;
    private final NetherNetTransportStats stats = new NetherNetTransportStats();
    /**
     * The in-process relay when media shares the existing RakNet listener, null otherwise.
     */
    private final NetherNetMediaRelay relay;

    private final String mediaDescription;
    private long nextMediaAlertMinute = -1;
    private boolean mediaAlarmRaised;
    private final Map<InetSocketAddress, NetherNetPlayerSession> sessions = new HashMap<>();
    private final Queue<NetherNetPlayerSession> sessionCreationQueue = PlatformDependent.newMpscQueue();
    private final NetworkPacketBudget inboundBudget = new NetworkPacketBudget(2048, 6L * 1024 * 1024);
    private final NetworkEventQueue.Task creationTask = new NetworkEventQueue.Task() {
        @Override
        public NetworkEventQueue.Result process(long deadlineNanos) {
            createPendingSessions(deadlineNanos);
            return hasWork() ? NetworkEventQueue.Result.MORE : NetworkEventQueue.Result.DONE;
        }

        @Override
        public boolean hasWork() {
            return !sessionCreationQueue.isEmpty();
        }
    };
    private final Set<NetherNetPlayerSession> pendingSessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<InetAddress, Long> blockedAddresses = new ConcurrentHashMap<>();

    private final AtomicReference<String> pongNonce = new AtomicReference<>(String.format("%016x", new SecureRandom().nextLong()));

    private volatile boolean accepting = true;

    public NetherNetInterface(Server server, NetherNetSettings settings) {
        this(server, settings, null);
    }

    /** @param rakNet existing RakNet listener, required when media shares its UDP port */
    public NetherNetInterface(Server server, NetherNetSettings settings, RakNetInterface rakNet) {
        this.server = server;
        try {
            LibDataChannelArchDetect.initialize();
        } catch (Throwable t) {
            throw new IllegalStateException(server.getLanguage().translateString("nukkit.nethernet.nativeInit.failed"), t);
        }
        NetherNetLogging.setNativeLogLevel("WARN");
        NetherNetUdpPorts mediaPorts = resolveMediaPorts(server);
        boolean sharedPort = mediaPorts != null && mediaPorts.sharesPort(server.getPort());
        Channel boundChannel = null;
        EventLoopGroup group = null;
        NetherNetMediaRelay mediaRelay = null;
        NetherNetHTTPServerSignaling httpSignaling = null;
        try {
            if (sharedPort) {
                List<Channel> listeners = rakNet == null ? List.of() : rakNet.getDatagramChannels();
                if (listeners.isEmpty()) {
                    throw new IllegalStateException(server.getLanguage().translateString(
                            "nukkit.nethernet.sharedPort.noRakNet", udpPortsEntry(server), server.getPort()));
                }
                DatagramSocket reserved = reserveMediaPort(server, mediaPorts);
                if (mediaPorts.pinsOnly(server.getPort())) mediaPorts = mediaPorts.relayedThrough(reserved.getLocalPort());
                mediaRelay = new NetherNetMediaRelay(reserved, Math.max(256, server.getMaxPlayers() * 4), this::isAddressBlocked);
                for (Channel listener : listeners) mediaRelay.attach(listener);
            }
            this.relay = mediaRelay;
            httpSignaling = new NetherNetHTTPServerSignaling.Builder()
                    .setIdentity(NetherNetIdentity.load(server, settings))
                    .setServeHttp(true)
                    .setIceOnLocalPort(false)
                    .setStunServers(stunServers(settings))
                    .setAdvertisedAddresses(mediaPorts == null ? Set.of() : mediaPorts.advertisedAddresses())
                    .setTokenTrust(TokenTrust.ANY)
                    .setMotdProvider((host, remoteAddress, client) -> this.buildPong())
                    .setPlayerFilter((host, player) -> this.refusePlayer(player))
                    .build();
            this.signaling = httpSignaling;
            group = new NioEventLoopGroup(2);
            this.eventLoopGroup = group;
            this.mediaDescription = describeMedia(server, mediaPorts, sharedPort);
            InetSocketAddress bindAddress = new InetSocketAddress(server.getIp().isBlank() ? "0.0.0.0" : server.getIp(), server.getPort());
            NetherNetServerSignaling channelSignaling = httpSignaling;
            if (sharedPort) {
                boolean ipv6 = bindAddress.getAddress() instanceof java.net.Inet6Address;
                NetherNetSharedPortSdp.ExternalPorts external = new NetherNetSharedPortSdp.ExternalPorts(
                        ipv6 ? -1 : server.getPort(), ipv6 ? server.getPort() : -1);
                int internalPort = mediaPorts.begin();
                channelSignaling = mediaRelay.decorate(httpSignaling, sdp -> NetherNetSharedPortSdp.rewriteAnswer(
                        sdp, internalPort, this.publishedAddresses(), external));
            } else if (mediaPorts != null) {
                channelSignaling = mediaPorts.decorate(httpSignaling);
            }
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(group)
                    .channelFactory(NetherNetChannelFactory.server(channelSignaling))
                    .option(NetherChannelOption.NETHER_SERVER_RTC_HANDSHAKE_TIMEOUT_SECONDS,
                            handshakeTimeoutSeconds(server.getSettings().network().timeoutMilliseconds()))
                    .option(NetherChannelOption.NETHER_METRICS, this.stats)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            NetherNetPlayerSession session = new NetherNetPlayerSession(NetherNetInterface.this, (NetherNetChildChannel) channel);
                            pendingSessions.add(session);
                            channel.pipeline().addLast("nukkit-handler", session);
                        }
                    });
            if (sharedPort) {
                bootstrap.option(NetherChannelOption.NETHER_PEER_CONNECTION_CONFIG, mediaPorts.peerConfig(mediaRelay.mediaEndpoint()));
                bootstrap.option(NetherChannelOption.NETHER_INFER_PEER_CANDIDATES, false);
            } else if (mediaPorts != null) {
                bootstrap.option(NetherChannelOption.NETHER_PEER_CONNECTION_CONFIG, mediaPorts.peerConfig(bindAddress));
            }
            var future = bootstrap.bind(bindAddress).awaitUninterruptibly();
            boundChannel = future.channel();
            if (!future.isSuccess()) throw new IllegalStateException("Failed to bind NetherNet signaling on " + bindAddress, future.cause());
            this.channel = boundChannel;
            log.info(server.getLanguage().translateString(mediaPorts == null ? "nukkit.nethernet.listening.auto" : "nukkit.nethernet.listening.pinned",
                    bindAddress.getPort(), this.mediaDescription));
        } catch (Throwable t) {
            if (boundChannel != null) boundChannel.close().awaitUninterruptibly();
            if (httpSignaling != null) httpSignaling.close();
            if (mediaRelay != null) mediaRelay.shutdown();
            if (group != null) group.shutdownGracefully();
            throw new IllegalStateException("Unable to start NetherNet", t);
        }
    }

    private static String describeMedia(Server server, NetherNetUdpPorts mediaPorts, boolean sharedPort) {
        if (mediaPorts == null) {
            return server.getLanguage().translateString("nukkit.nethernet.media.systemAssigned");
        }
        if (!sharedPort) {
            return mediaPorts.toString();
        }
        // In shared mode a single-port mapping always holds (pinsOnly was relayedThrough first),
        // the external port being the internal one plus the offset
        int externalPort = mediaPorts.begin() + mediaPorts.offset();
        String description = server.getLanguage().translateString("nukkit.nethernet.media.sharedPort",
                externalPort, mediaPorts.begin());
        return mediaPorts.advertisedAddresses().isEmpty()
                ? description : description + " via " + mediaPorts.advertisedAddresses();
    }

    /**
     * Addresses published in a shared-port answer, enumerated per answer (joins are rare and
     * interface changes take effect at once).
     */
    private List<InetAddress> publishedAddresses() {
        String ip = this.server.getIp();
        boolean ipv6 = ip.contains(":");
        List<InetAddress> addresses = NetherNetSharedPortSdp.localAddresses(ipv6 ? "" : ip, ipv6 ? ip : "", ipv6);
        if (addresses.isEmpty()) log.warn(this.server.getLanguage().translateString("nukkit.nethernet.sharedPort.noAddress"));
        return addresses;
    }

    private boolean isAddressBlocked(InetAddress address) {
        Long expiry = this.blockedAddresses.get(address);
        return expiry != null && expiry >= System.currentTimeMillis();
    }

    /**
     * Since CR6 the ICE configuration is STUN discovery only (upstream dropped TURN);
     * URLs pass through to the signaling layer as-is.
     */
    private static List<String> stunServers(NetherNetSettings settings) {
        List<String> urls = settings.iceServers();
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }
        return List.copyOf(urls);
    }

    /**
     * The handshake budget shares the login timeout's window so neither clock truncates the
     * other; a disabled login timeout falls back so abandoned handshakes are still reaped.
     */
    static int handshakeTimeoutSeconds(int loginTimeoutMillis) {
        return loginTimeoutMillis <= 0
                ? DEFAULT_HANDSHAKE_TIMEOUT_SECONDS
                : Math.max(loginTimeoutMillis / 1000, 1);
    }

    /**
     * The general.server-udp-ports setting defaults to 19132, sharing RakNet on its default port.
     */
    private static String udpPortsEntry(Server server) {
        return server.getSettings().general().serverUdpPorts();
    }

    /** Reject overlapping port windows; only a single port may share the RakNet listener. */
    private static NetherNetUdpPorts resolveMediaPorts(Server server) {
        String value = udpPortsEntry(server);
        NetherNetUdpPorts ports = NetherNetUdpPorts.parse(value, server.getLanguage());
        if (ports == null || ports.pinsOnly(server.getPort())) return ports;
        if (ports.contains(server.getPort())) {
            throw new IllegalArgumentException(server.getLanguage().translateString(
                    "nukkit.nethernet.udpPorts.coversServerPort", value, server.getPort()));
        }
        if (ports.offset() != 0 && !ports.publishesOn(server.getPort())
                && ports.begin() + ports.offset() <= server.getPort() && server.getPort() <= ports.end() + ports.offset()) {
            throw new IllegalArgumentException(server.getLanguage().translateString(
                    "nukkit.nethernet.udpPorts.sharedWindow", value, server.getPort(), ports.begin()));
        }
        return ports;
    }

    /**
     * Reserves the internal loopback port for the shared port ({@link NetherNetMediaRelay#reserveMediaPort()}):
     * a mapped entry's pinned internal port already in use aborts startup; otherwise the OS assigns
     * it on loopback (the ephemeral range), so instances on one host spread out naturally with no
     * window to exhaust — the trade-off being an ephemeral-range port that another ephemeral bind
     * could theoretically grab between the release and libjuice's rebind, both already happening
     * in one new-connection callback, a negligible gap.
     */
    private static DatagramSocket reserveMediaPort(Server server, NetherNetUdpPorts ports) {
        try {
            return ports.publishesOn(server.getPort()) ? NetherNetMediaRelay.reserveMediaPort(ports.begin()) : NetherNetMediaRelay.reserveMediaPort();
        } catch (IOException e) {
            String key = ports.publishesOn(server.getPort()) ? "nukkit.nethernet.sharedPort.internalInUse" : "nukkit.nethernet.sharedPort.systemPortFailed";
            throw new IllegalStateException(server.getLanguage().translateString(key, udpPortsEntry(server),
                    ports.publishesOn(server.getPort()) ? ports.begin() : server.getPort()), e);
        }
    }

    private PongData buildPong() {
        QueryRegenerateEvent info = this.server.getQueryInformation();
        return new PongData.Builder()
                .setServerName(this.server.getSettings().general().motd())
                .setProtocol(ProtocolInfo.CURRENT_PROTOCOL)
                .setVersion(ProtocolInfo.MINECRAFT_VERSION_NETWORK)
                .setLevelName(this.server.getSettings().general().subMotd())
                .setPlayerCount(info.getPlayerCount())
                .setMaxPlayerCount(info.getMaxPlayerCount())
                .setGameType(this.server.getDefaultGamemode() == 1 ? 1 : 0)
                .setOnlineAuth(this.server.getSettings().network().xboxAuth())
                .setSelfSignedAuth(!this.server.getSettings().network().xboxAuth())
                .setNonce(this.pongNonce.get())
                .build();
    }

    private JoinRefusal refusePlayer(org.cloudburstmc.netty.util.nethernet.PlayerInfo player) {
        if (!this.accepting) {
            return JoinRefusal.REJECTED;
        }
        if (this.server.getOnlinePlayers().size() >= this.server.getMaxPlayers()) {
            return JoinRefusal.FULL;
        }
        InetSocketAddress remote = player.remoteAddress();
        if (remote != null) {
            Long expiry = this.blockedAddresses.get(remote.getAddress());
            if (expiry != null) {
                if (expiry < System.currentTimeMillis()) {
                    this.blockedAddresses.remove(remote.getAddress());
                } else {
                    return JoinRefusal.REJECTED;
                }
            }
        }
        this.stats.onJoinAccepted();
        return null;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public void processMaintenance() {
        this.expireLoginSessions();
        this.sampleStatsAndEvaluateMediaAlarm();
        Iterator<NetherNetPlayerSession> iterator = this.sessions.values().iterator();
        while (iterator.hasNext()) {
            NetherNetPlayerSession nukkitSession = iterator.next();
            Player player = nukkitSession.getPlayer();
            if (nukkitSession.getDisconnectReason() != null) {
                nukkitSession.stopProcessing();
                try {
                    if (player != null) {
                        player.close(player.getLeaveMessage(), nukkitSession.getDisconnectReason(), false);
                    }
                } catch (Exception e) {
                    nukkitSession.disconnect("Internal error");
                    log.error("Exception closing player " + player.getName(), e);
                }
                iterator.remove();
            } else if (!nukkitSession.getChannel().isActive()) {
                nukkitSession.disconnect("transport:closed_by_remote_peer");
            }
        }
    }

    private void createPendingSessions(long deadlineNanos) {
        NetherNetPlayerSession session;
        int created = 0;
        while (created++ < 32 && System.nanoTime() < deadlineNanos
                && (session = this.sessionCreationQueue.poll()) != null) {
            this.pendingSessions.remove(session);
            if (session.getDisconnectReason() != null || !session.getChannel().isActive()) {
                continue;
            }
            // Netty caches the remote from the session's first read as the library's signaling
            // source (the real client); it never turns into the leg address once ICE connects,
            // so player creation, bans and logs can use it as is
            InetSocketAddress address = (InetSocketAddress) session.getChannel().remoteAddress();
            try {
                PlayerCreationEvent event = new PlayerCreationEvent(this, Player.class, Player.class, null, address);
                event.call();

                NetherNetPlayerSession replaced = this.sessions.put(event.getSocketAddress(), session);
                if (replaced != null && replaced != session && replaced.getDisconnectReason() == null) {
                    replaced.disconnect("Logged in from another location");
                }

                Constructor<? extends Player> constructor = event.getPlayerClass().getConstructor(SourceInterface.class, Long.class, InetSocketAddress.class);
                Player player = constructor.newInstance(this, event.getClientId(), event.getSocketAddress());
                player.raknetProtocol = NetherNetPlayerSession.RAKNET_PROTOCOL;
                session.setPlayer(player);
                this.server.addPlayer(address, player);
            } catch (Exception e) {
                Server.getInstance().getLogger().error("Failed to create Player", e);
                session.disconnect("Internal Server Error");
                this.sessions.remove(address);
            }
        }

    }

    public NetworkEventQueue.Result processSession(NetherNetPlayerSession session, long deadlineNanos) {
        Player player = session.getPlayer();
        if (player == null) return NetworkEventQueue.Result.DONE;
        if (session.getDisconnectReason() != null) {
            session.stopProcessing();
            if (this.sessions.get(player.getSocketAddress()) != session) return NetworkEventQueue.Result.DONE;
            try {
                player.close(player.getLeaveMessage(), session.getDisconnectReason(), false);
            } catch (Exception e) {
                session.disconnect("Internal error");
                log.error("Exception closing player " + player.getName(), e);
            }
            this.sessions.remove(player.getSocketAddress(), session);
            return NetworkEventQueue.Result.DONE;
        }
        return session.processInbound(deadlineNanos, this.inboundBudget);
    }

    public void queueSessionForPlayerCreation(NetherNetPlayerSession session) {
        if (!this.accepting || !session.markQueuedForPlayerCreation()) return;
        this.pendingSessions.add(session);
        this.sessionCreationQueue.offer(session);
        this.server.getNetwork().events().signal(this.creationTask);
    }

    private void expireLoginSessions() {
        int timeoutMillis = this.server.getSettings().network().timeoutMilliseconds();

        long nowNanos = System.nanoTime();
        this.pendingSessions.removeIf(session -> {
            if (session.getDisconnectReason() != null || !session.getChannel().isOpen()) {
                return true;
            }
            if (timeoutMillis <= 0) {
                return false;
            }
            if (!session.isPendingLoginTimedOut(nowNanos, timeoutMillis)) {
                return false;
            }

            log.warn("Disconnecting timed out pending NetherNet session {}", session.getChannel().remoteAddress());
            this.stats.onLoginTimeout();
            session.disconnect("disconnectionScreen.timeout");
            return true;
        });

        if (timeoutMillis <= 0) {
            return;
        }

        for (NetherNetPlayerSession session : this.sessions.values()) {
            if (session.getDisconnectReason() != null || !session.isLoginPhaseTimedOut(nowNanos, timeoutMillis)) {
                continue;
            }
            log.warn("Disconnecting timed out NetherNet session {}", session.getChannel().remoteAddress());
            this.stats.onLoginTimeout();
            session.disconnect("disconnectionScreen.timeout");
        }
    }

    /**
     * Main-thread tick hook: advances the minute ring, then evaluates media reachability
     * per alarm cadence (the first check waits one full window so it never fires on partial data).
     */
    private void sampleStatsAndEvaluateMediaAlarm() {
        long nowMillis = System.currentTimeMillis();
        this.stats.sampleMinute(nowMillis);

        long minute = Math.floorDiv(nowMillis, 60_000L);
        if (this.nextMediaAlertMinute < 0) {
            this.nextMediaAlertMinute = minute + MEDIA_ALERT_WINDOW_MINUTES;
            return;
        }
        if (minute < this.nextMediaAlertMinute) {
            return;
        }
        this.nextMediaAlertMinute = minute + MEDIA_ALERT_WINDOW_MINUTES;

        NetherNetTransportStats.TransportSnapshot snapshot =
                this.stats.snapshot(MEDIA_ALERT_WINDOW_MINUTES, nowMillis);
        if (mediaUnreachable(snapshot)) {
            if (!this.mediaAlarmRaised) {
                this.mediaAlarmRaised = true;
                log.warn(this.server.getLanguage().translateString("nukkit.nethernet.stats.mediaUnreachable",
                        snapshot.peerConnecting(), snapshot.iceConnected(), MEDIA_ALERT_WINDOW_MINUTES, this.mediaDescription));
            }
        } else if (this.mediaAlarmRaised) {
            this.mediaAlarmRaised = false;
            // Only a real success reports recovery; traffic drying up just clears silently.
            if (snapshot.iceConnected() > 0) {
                log.info(this.server.getLanguage().translateString("nukkit.nethernet.stats.mediaRecovered",
                        snapshot.iceConnected(), MEDIA_ALERT_WINDOW_MINUTES));
            }
        }
    }

    /**
     * Unreachable verdict: attempts at the floor with zero ICE successes in the window
     * (zero stays exact, see the stats class notes).
     */
    static boolean mediaUnreachable(NetherNetTransportStats.TransportSnapshot snapshot) {
        return snapshot.peerConnecting() >= MEDIA_ALERT_MIN_ATTEMPTS && snapshot.iceConnected() == 0;
    }

    /**
     * /status lines localized through lang.ini; two in simple mode, four in full plus a relay line
     * while the port is shared, session counts are read from the main-thread command path only.
     */
    public List<String> buildStatusLines(int windowMinutes, boolean full) {
        NetherNetTransportStats.TransportSnapshot snapshot =
                this.stats.snapshot(windowMinutes, System.currentTimeMillis());
        List<String> lines = new ArrayList<>(5);
        lines.add(TextFormat.GOLD + this.server.getLanguage().translateString("nukkit.nethernet.stats.summary",
                this.mediaDescription, this.sessions.size(), this.pendingSessions.size()));
        lines.add(TextFormat.GOLD + this.server.getLanguage().translateString("nukkit.nethernet.stats.funnel",
                snapshot.joinAccepted(), snapshot.iceConnected(), snapshot.peerConnecting(),
                snapshot.rtcConnected(), windowMinutes));
        if (full) {
            lines.add(TextFormat.GOLD + this.server.getLanguage().translateString("nukkit.nethernet.stats.traffic",
                    formatBytes(snapshot.bytesIn()), formatBytes(snapshot.bytesOut())));
            lines.add(TextFormat.GOLD + this.server.getLanguage().translateString("nukkit.nethernet.stats.dropped",
                    snapshot.postConnectDrops(), snapshot.loginTimeouts(), windowMinutes));
            if (this.relay != null) {
                NetherNetMediaRelay.Snapshot relaySnapshot = this.relay.snapshot();
                lines.add(TextFormat.GOLD + this.server.getLanguage().translateString("nukkit.nethernet.stats.relay",
                        relaySnapshot.legs(), relaySnapshot.legsOpened(),
                        formatBytes(relaySnapshot.bytesIn()), formatBytes(relaySnapshot.bytesOut()),
                        relaySnapshot.rejectedUfrag(), relaySnapshot.rejectedLimit() + relaySnapshot.rejectedBlocked()));
            }
        }
        return lines;
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024f);
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f MB", bytes / 1024f / 1024f);
        }
        return String.format(Locale.ROOT, "%.2f GB", bytes / 1024f / 1024f / 1024f);
    }

    public NetherNetTransportStats getTransportStats() {
        return this.stats;
    }

    /**
     * Ties the login chain to the identity that opened the transport: RakNet's encryption handshake
     * does this on its own, NetherNet skips it, so the signaling assertion binds it instead.
     *
     * @return why the login must be refused, or null when the two agree (or nothing is enforced)
     */
    public String checkIdentityBinding(NetworkPlayerSession session, String identityPublicKeyBase64) {
        if (!(session instanceof NetherNetPlayerSession netherNetSession)) {
            return null;
        }
        Channel channel = netherNetSession.getChannel();
        if (!this.server.getSettings().network().xboxAuth()) {
            // Offline mode has given up on proving identity, no key worth comparing
            return TransportIdentityBinding.acceptForwardedIdentity(channel);
        }

        PublicKey identityKey = parseIdentityKey(identityPublicKeyBase64);
        if (identityKey == null) {
            return "the login chain carries no usable identity key";
        }
        return TransportIdentityBinding.mismatch(channel, identityKey);
    }

    private static PublicKey parseIdentityKey(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        try {
            return KeyFactory.getInstance("EC")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getNetworkLatency(Player player) {
        return (int) player.getNetworkSession().getPing();
    }

    @Override
    public NetworkPlayerSession getSession(InetSocketAddress address) {
        return this.sessions.get(address);
    }

    @Override
    public void close(Player player) {
        this.close(player, "unknown reason");
    }

    @Override
    public void close(Player player, String reason) {
        NetworkPlayerSession playerSession = this.getSession(player.getSocketAddress());
        if (playerSession != null) {
            playerSession.disconnect(reason);
        }
    }

    @Override
    public void shutdown() {
        this.accepting = false;
        this.pendingSessions.forEach(session -> session.disconnect("Shutdown"));
        this.sessions.values().forEach(session -> session.disconnect("Shutdown"));
        this.channel.close().awaitUninterruptibly();
        if (this.relay != null) {
            this.relay.shutdown();
        }
        this.signaling.close();
        this.sessionCreationQueue.clear();
        this.pendingSessions.clear();
        this.sessions.clear();
        this.eventLoopGroup.shutdownGracefully();
    }

    @Override
    public void emergencyShutdown() {
        this.shutdown();
    }

    @Override
    public void blockAddress(InetAddress address) {
        this.blockedAddresses.put(address, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(100));
    }

    @Override
    public void blockAddress(InetAddress address, int timeout) {
        this.blockedAddresses.put(address, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeout < 0 ? TimeUnit.DAYS.toSeconds(100) : Math.max(timeout, 1)));
    }

    @Override
    public void unblockAddress(InetAddress address) {
        this.blockedAddresses.remove(address);
    }

    /**
     * Connection-oriented: no raw datagram channel to write to, raw packets keep flowing over RakNet.
     */
    @Override
    public void sendRawPacket(InetSocketAddress socketAddress, ByteBuf payload) {
        // Network.sendPacket hands one buffer to every interface and RakNet's pipeline write
        // consumes its only refcount; releasing here would double-free the query reply
    }

    /**
     * The advertisement is built per request through the motd provider, nothing cached to refresh.
     */
    @Override
    public void setName(String name) {
    }

    public Network getNetwork() {
        return this.network;
    }
}
