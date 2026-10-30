package cn.nukkit.network;

/** Main-thread-only accounting shared by all drains in one simulation tick. */
public final class NetworkPacketBudget {
    private final int maxPackets;
    private final long maxBytes;
    private int tick;
    private int packets;
    private long bytes;

    public NetworkPacketBudget(int maxPackets, long maxBytes) {
        this.maxPackets = maxPackets;
        this.maxBytes = maxBytes;
    }

    public boolean allows(int currentTick, int packetBytes) {
        if (tick != currentTick) {
            tick = currentTick;
            packets = 0;
            bytes = 0;
        }
        return packets < maxPackets && packetBytes <= maxBytes - bytes;
    }

    public void record(int packetBytes) {
        packets++;
        bytes += packetBytes;
    }
}
