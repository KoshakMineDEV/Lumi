package cn.nukkit.network.protocol;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import lombok.ToString;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.zip.Deflater;

/**
 * @since v712
 */
@ToString
public class JigsawStructureDataPacket extends DataPacket {

    public static final int NETWORK_ID = ProtocolInfo.JIGSAW_STRUCTURE_DATA_PACKET;

    public CompoundTag nbt;

    private static final BatchPacket CACHED_PACKET;

    static {
        JigsawStructureDataPacket pk = new JigsawStructureDataPacket();
        // empty data set = no custom structure generation
        pk.nbt = new CompoundTag("")
                .putList(new ListTag<>("jigsaws"))
                .putList(new ListTag<>("processors"))
                .putList(new ListTag<>("structure_sets"))
                .putList(new ListTag<>("template_pools"));
        pk.protocol = ProtocolInfo.v1_21_120;
        pk.tryEncode();
        CACHED_PACKET = pk.compress(Deflater.BEST_COMPRESSION);
    }

    public static BatchPacket getCachedPacket() {
        return CACHED_PACKET;
    }

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public byte pid() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void decode() {
        this.nbt = this.getTag();
    }

    @Override
    public void encode() {
        this.reset();
        try {
            this.put(NBTIO.writeNetwork(this.nbt != null ? this.nbt : new CompoundTag()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
