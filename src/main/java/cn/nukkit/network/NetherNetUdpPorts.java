package cn.nukkit.network;

import cn.nukkit.lang.BaseLang;
import lombok.extern.log4j.Log4j2;
import org.cloudburstmc.netty.channel.nethernet.signaling.NetherNetServerSignaling;
import org.cloudburstmc.netty.util.nethernet.EndpointAddress;
import tel.schich.libdatachannel.PeerConnectionConfiguration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Parses server-udp-ports from settings.yml (BDS syntax): 0=system-assigned;
 * {@code [ip:]internal[-internal]} pins the local port/window;
 * {@code [ip:]external[-external]:internal[-internal]} additionally publishes an
 * externally reachable mapping, range lengths on each side of the colon must match.
 * Comma-separated entries accumulate IPs while window and offset must agree.
 * A single port pinned exactly on a RakNet listener port (server-port or the IPv6 listener,
 * {@link #pinsOnly}) or a single-port mapping onto one ({@link #publishesOn}) shares that
 * port with RakNet, the former leaving the internal loopback port for the OS to assign.
 */
@Log4j2
public final class NetherNetUdpPorts {

    private final int begin;
    private final int end;

    private final int offset;

    private final Set<String> advertisedAddresses;

    private NetherNetUdpPorts(int begin, int end, int offset, Set<String> advertisedAddresses) {
        this.begin = begin;
        this.end = end;
        this.offset = offset;
        this.advertisedAddresses = advertisedAddresses;
    }

    /**
     * Parses the configured value; null means system-assigned (0 or blank); any invalid
     * entry throws an {@link IllegalArgumentException} carrying a localized message —
     * settings.yml is critical config, so errors abort instead of falling back.
     */
    public static NetherNetUdpPorts parse(String value, BaseLang lang) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if ("0".equals(trimmed)) {
            return null;
        }

        int begin = -1;
        int end = -1;
        int offset = 0;
        Set<String> addresses = new LinkedHashSet<>();
        for (String entryText : trimmed.split(",")) {
            Entry entry = parseEntry(entryText, value, lang);
            if (begin < 0) {
                begin = entry.internalBegin;
                end = entry.internalEnd;
                offset = entry.offset;
            } else if (entry.internalBegin != begin || entry.internalEnd != end) {
                throw invalid(lang, "nukkit.nethernet.udpPorts.windowMismatch", value, entryText.trim(), begin, end);
            } else if (entry.hasMapping && entry.offset != offset) {
                throw invalid(lang, "nukkit.nethernet.udpPorts.offsetMismatch", value, entryText.trim(), offset);
            }
            if (entry.address != null) {
                addresses.add(entry.address);
            }
        }
        return begin < 0 ? null : new NetherNetUdpPorts(begin, end, offset, addresses);
    }

    private static IllegalArgumentException invalid(BaseLang lang, String key, Object... params) {
        return new IllegalArgumentException(lang.translateString(key, params));
    }

    /**
     * Single entry {@code [ip:]range[:range]}; IPv6 requires brackets, a dotted first group is
     * an IPv4 prefix (ports never carry dots).
     */
    private static Entry parseEntry(String text, String whole, BaseLang lang) {
        String t = text.trim();
        String address = null;

        if (t.startsWith("[")) {
            int close = t.indexOf(']');
            if (close < 0 || close + 1 >= t.length() || t.charAt(close + 1) != ':') {
                throw invalid(lang, "nukkit.nethernet.udpPorts.badIpv6", whole, t);
            }
            address = canonicalAddress(t.substring(1, close), whole, t, lang);
            t = t.substring(close + 2);
        }

        String[] groups = t.split(":", -1);
        String externalText = null;
        String internalText;
        switch (groups.length) {
            case 1 -> internalText = groups[0];
            case 2 -> {
                if (groups[0].contains(".")) {
                    address = canonicalAddress(groups[0], whole, t, lang);
                    internalText = groups[1];
                } else {
                    externalText = groups[0];
                    internalText = groups[1];
                }
            }
            case 3 -> {
                address = canonicalAddress(groups[0], whole, t, lang);
                externalText = groups[1];
                internalText = groups[2];
            }
            default -> throw invalid(lang, "nukkit.nethernet.udpPorts.badShape", whole, t);
        }

        int[] internal = parseRange(internalText, whole, t, lang);
        if (externalText == null) {
            return new Entry(internal[0], internal[1], 0, false, address);
        }

        int[] external = parseRange(externalText, whole, t, lang);
        int externalSpan = external[1] - external[0];
        int internalSpan = internal[1] - internal[0];
        if (externalSpan != internalSpan) {
            throw invalid(lang, "nukkit.nethernet.udpPorts.rangeLengthMismatch", whole, t);
        }
        return new Entry(internal[0], internal[1], external[0] - internal[0], true, address);
    }

    private static String canonicalAddress(String text, String whole, String entry, BaseLang lang) {
        try {
            return EndpointAddress.parse(text).getHostAddress();
        } catch (UnknownHostException e) {
            throw invalid(lang, "nukkit.nethernet.udpPorts.notIpLiteral", whole, text, entry);
        }
    }

    private static int[] parseRange(String text, String whole, String entry, BaseLang lang) {
        int dash = text.indexOf('-');
        try {
            if (dash >= 0) {
                return validatedRange(Integer.parseInt(text.substring(0, dash).trim()),
                        Integer.parseInt(text.substring(dash + 1).trim()), text, whole, entry, lang);
            }
            int port = Integer.parseInt(text.trim());
            return validatedRange(port, port, text, whole, entry, lang);
        } catch (NumberFormatException e) {
            throw invalid(lang, "nukkit.nethernet.udpPorts.notPortRange", whole, text, entry);
        }
    }

    private static int[] validatedRange(int begin, int end, String text, String whole, String entry, BaseLang lang) {
        if (begin < 1 || end < 1 || begin > 65535 || end > 65535 || begin > end) {
            throw invalid(lang, "nukkit.nethernet.udpPorts.invalidRange", whole, text, entry);
        }
        return new int[]{begin, end};
    }

    private record Entry(int internalBegin, int internalEnd, int offset, boolean hasMapping, String address) {
    }

    public boolean contains(int port) {
        return port >= this.begin && port <= this.end;
    }

    int begin() {
        return this.begin;
    }

    int end() {
        return this.end;
    }

    int offset() {
        return this.offset;
    }

    public Set<String> advertisedAddresses() {
        return this.advertisedAddresses;
    }

    /**
     * The pinning peer connection config: mux + the window; a concrete bind address also
     * constrains ICE, a wildcard one keeps gathering on every interface (same rule the
     * library's own bindIce pinning path applies).
     */
    public PeerConnectionConfiguration peerConfig(InetSocketAddress bindAddress) {
        PeerConnectionConfiguration config = PeerConnectionConfiguration.DEFAULT
                .withEnableIceUdpMux(true)
                .withPortRangeBegin(this.begin)
                .withPortRangeEnd(this.end);
        InetAddress host = bindAddress.getAddress();
        if (host != null && !host.isAnyLocalAddress()) {
            config = config.withBindAddress(host);
        }
        return config;
    }

    /**
     * Shifts the port of host/UDP candidate lines into the external window
     * (returns the input unchanged when the offset is 0). srflx/relay candidates
     * are STUN/TURN discoveries and stay; candidate lines take no part in the
     * identity signature (it covers a=fingerprint only), and the library's own
     * address rewriting happens after signing just the same.
     */
    public String rewriteSdp(String sdp) {
        if (this.offset == 0) {
            return sdp;
        }
        StringBuilder out = new StringBuilder(sdp.length());
        for (String line : sdp.split("\r\n|\n")) {
            // A trailing empty line makes libwebrtc reject the whole answer, drop it like SdpUtil does
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("a=candidate:")) {
                String rewritten = rewriteCandidate(line);
                if (rewritten != null) {
                    line = rewritten;
                }
            }
            out.append(line).append("\r\n");
        }
        return out.toString();
    }

    /**
     * Candidate tokens split on spaces put the port sixth
     * (foundation/component/transport/priority/address/port).
     */
    private String rewriteCandidate(String line) {
        String[] parts = line.split(" ");
        if (parts.length < 8 || !"udp".equalsIgnoreCase(parts[2]) || !"typ".equals(parts[6]) || !"host".equals(parts[7])) {
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(parts[5]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (!this.contains(port)) {
            log.warn("A gathered host candidate port {} sits outside the configured window {}-{}, leaving it unchanged", port, this.begin, this.end);
            return null;
        }
        parts[5] = Integer.toString(port + this.offset);
        return String.join(" ", parts);
    }

    public NetherNetServerSignaling decorate(NetherNetServerSignaling delegate) {
        return this.offset == 0 ? delegate : new PortRewritingSignaling(delegate);
    }

    /**
     * A single-port mapping whose external port is {@code port} (a RakNet listener port): media is
     * published on that port's UDP side and {@link NetherNetMediaRelay} forwards it in-process
     * instead of relying on an external NAT.
     */
    public boolean publishesOn(int port) {
        return this.offset != 0 && this.begin == this.end && this.begin + this.offset == port;
    }

    /**
     * A single unmapped port that is exactly {@code port} (a RakNet listener port): sharing with
     * RakNet as well, only the internal loopback port is left for the OS to assign, after which
     * {@link #relayedThrough} turns this into the {@link #publishesOn} single-port mapping.
     */
    public boolean pinsOnly(int port) {
        return this.offset == 0 && this.begin == this.end && this.begin == port;
    }

    /**
     * Either single-port shape sharing the RakNet listener on {@code port} (pinned or mapped),
     * treating the IPv4 and IPv6 listeners alike.
     */
    public boolean sharesPort(int port) {
        return this.pinsOnly(port) || this.publishesOn(port);
    }

    /**
     * Turns a {@link #pinsOnly} single port into the mapping relayed through {@code internalPort}
     * ({@code ext:internalPort}), keeping any address prefix.
     */
    NetherNetUdpPorts relayedThrough(int internalPort) {
        if (this.offset != 0 || this.begin != this.end) {
            throw new IllegalStateException("only a single unmapped port can be relayed, got " + this);
        }
        return new NetherNetUdpPorts(internalPort, internalPort, this.begin - internalPort, this.advertisedAddresses);
    }

    private final class PortRewritingSignaling extends NetherNetDelegatingSignaling {

        private PortRewritingSignaling(NetherNetServerSignaling delegate) {
            super(delegate);
        }

        @Override
        public void sendDescription(String targetNetworkId, String sdp) {
            this.delegate.sendDescription(targetNetworkId, NetherNetUdpPorts.this.rewriteSdp(sdp));
        }
    }

    @Override
    public String toString() {
        String window = this.begin == this.end ? String.valueOf(this.begin) : this.begin + "-" + this.end;
        StringBuilder out = new StringBuilder("udp/").append(window);
        if (this.offset != 0) {
            int externalBegin = this.begin + this.offset;
            int externalEnd = this.end + this.offset;
            out.append(" published as ").append(externalBegin == externalEnd
                    ? String.valueOf(externalBegin) : externalBegin + "-" + externalEnd);
        }
        if (!this.advertisedAddresses.isEmpty()) {
            out.append(" via ").append(this.advertisedAddresses);
        }
        return out.toString();
    }
}
