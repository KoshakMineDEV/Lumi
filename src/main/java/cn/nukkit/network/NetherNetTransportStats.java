package cn.nukkit.network;

import org.cloudburstmc.netty.channel.nethernet.config.NetherChannelMetrics;
import tel.schich.libdatachannel.IceState;
import tel.schich.libdatachannel.PeerState;

import java.util.concurrent.atomic.LongAdder;

/**
 * Passive aggregate funnel counters for NetherNet joins. Lock-free LongAdders shared by
 * three writer domains (native metrics callbacks, the signaling event loop, the main thread);
 * events carry no connection id so the aggregate is global and counts state arrivals, which
 * may overcount connections — zero stays exact. The recent-window view is a one-minute
 * differential ring (one hour) sampled on the main thread, weakly consistent for readers.
 * <p>
 * Diagnosis grid: joinAccepted rising while iceConnected stays zero means the media UDP window
 * is unreachable; iceConnected rising with few rtcConnected points at DTLS trouble; a high
 * postConnectDrops after rtcConnected points at NAT timeouts or jitter.
 */
public final class NetherNetTransportStats implements NetherChannelMetrics {

    static final int HISTORY_MINUTES = 60;

    private final LongAdder joinAccepted = new LongAdder();
    private final LongAdder peerConnecting = new LongAdder();
    private final LongAdder iceConnected = new LongAdder();
    private final LongAdder iceFailed = new LongAdder();
    private final LongAdder rtcConnected = new LongAdder();

    private final LongAdder postConnectDrops = new LongAdder();
    private final LongAdder loginTimeouts = new LongAdder();
    private final LongAdder bytesIn = new LongAdder();
    private final LongAdder bytesOut = new LongAdder();

    private final MinuteSample[] history = new MinuteSample[HISTORY_MINUTES];
    private long lastSampleMinute = -1;
    private Counts lastCounts;

    @Override
    public void peerStateChange(PeerState state) {
        switch (state) {
            case RTC_CONNECTING -> this.peerConnecting.increment();
            case RTC_CONNECTED -> this.rtcConnected.increment();
            case RTC_DISCONNECTED -> this.postConnectDrops.increment();
            default -> { }
        }
    }

    @Override
    public void iceStateChange(IceState state) {
        switch (state) {
            case RTC_ICE_CONNECTED, RTC_ICE_COMPLETED -> this.iceConnected.increment();
            case RTC_ICE_FAILED -> this.iceFailed.increment();
            default -> { }
        }
    }

    @Override
    public void bytesIn(int bytes) {
        if (bytes > 0) {
            this.bytesIn.add(bytes);
        }
    }

    @Override
    public void bytesOut(int bytes) {
        if (bytes > 0) {
            this.bytesOut.add(bytes);
        }
    }

    void onJoinAccepted() {
        this.joinAccepted.increment();
    }

    void onLoginTimeout() {
        this.loginTimeouts.increment();
    }

    /**
     * Differential sampling, main thread only; gaps of several minutes fill with zero
     * samples and the counts accumulated during them land in the first sample after.
     */
    void sampleMinute(long nowMillis) {
        long minute = Math.floorDiv(nowMillis, 60_000L);
        if (this.lastSampleMinute < 0) {
            this.lastSampleMinute = minute;
            this.lastCounts = Counts.of(this);
            return;
        }
        for (long m = this.lastSampleMinute + 1; m <= minute; m++) {
            Counts current = Counts.of(this);
            this.history[(int) Math.floorMod(m, HISTORY_MINUTES)] = new MinuteSample(m, current.minus(this.lastCounts));
            this.lastCounts = current;
        }
        this.lastSampleMinute = minute;
    }

    /**
     * Differential totals over the last {@code minutes} minutes;
     * the two traffic fields are totals since startup.
     */
    public TransportSnapshot snapshot(int minutes, long nowMillis) {
        long nowMinute = Math.floorDiv(nowMillis, 60_000L);
        Counts sum = Counts.ZERO;
        for (int i = 0; i < Math.min(minutes, HISTORY_MINUTES); i++) {
            MinuteSample sample = this.history[(int) Math.floorMod(nowMinute - i, HISTORY_MINUTES)];
            if (sample == null || sample.minute() > nowMinute || nowMinute - sample.minute() >= HISTORY_MINUTES) {
                continue;
            }
            sum = sum.plus(sample.delta());
        }
        return new TransportSnapshot(sum, this.bytesIn.sum(), this.bytesOut.sum());
    }

    private record Counts(long joinAccepted, long peerConnecting, long iceConnected, long iceFailed,
                          long rtcConnected, long postConnectDrops, long loginTimeouts) {

        static final Counts ZERO = new Counts(0, 0, 0, 0, 0, 0, 0);

        static Counts of(NetherNetTransportStats stats) {
            return new Counts(stats.joinAccepted.sum(), stats.peerConnecting.sum(), stats.iceConnected.sum(),
                    stats.iceFailed.sum(), stats.rtcConnected.sum(), stats.postConnectDrops.sum(),
                    stats.loginTimeouts.sum());
        }

        Counts minus(Counts other) {
            return new Counts(this.joinAccepted - other.joinAccepted, this.peerConnecting - other.peerConnecting,
                    this.iceConnected - other.iceConnected, this.iceFailed - other.iceFailed,
                    this.rtcConnected - other.rtcConnected, this.postConnectDrops - other.postConnectDrops,
                    this.loginTimeouts - other.loginTimeouts);
        }

        Counts plus(Counts other) {
            return new Counts(this.joinAccepted + other.joinAccepted, this.peerConnecting + other.peerConnecting,
                    this.iceConnected + other.iceConnected, this.iceFailed + other.iceFailed,
                    this.rtcConnected + other.rtcConnected, this.postConnectDrops + other.postConnectDrops,
                    this.loginTimeouts + other.loginTimeouts);
        }
    }

    private record MinuteSample(long minute, Counts delta) {
    }

    /**
     * Funnel window view: the first seven fields are window differentials, traffic is cumulative.
     */
    public record TransportSnapshot(long joinAccepted, long peerConnecting, long iceConnected, long iceFailed,
                                    long rtcConnected, long postConnectDrops, long loginTimeouts,
                                    long bytesIn, long bytesOut) {

        TransportSnapshot(Counts counts, long bytesIn, long bytesOut) {
            this(counts.joinAccepted(), counts.peerConnecting(), counts.iceConnected(), counts.iceFailed(),
                    counts.rtcConnected(), counts.postConnectDrops(), counts.loginTimeouts(), bytesIn, bytesOut);
        }
    }
}
