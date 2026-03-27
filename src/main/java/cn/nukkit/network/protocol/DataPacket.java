package cn.nukkit.network.protocol;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.exception.DataPacketDecodeException;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.compression.SnappyCompression;
import cn.nukkit.utils.compression.Zlib;
import lombok.extern.log4j.Log4j2;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
@Log4j2
public abstract class DataPacket extends BinaryStream implements Cloneable {

    public int protocol = Integer.MAX_VALUE;

    public volatile boolean isEncoded = false;
    private int channel = Network.CHANNEL_NONE;

    public int packetId() {
        return ProtocolInfo.toNewProtocolID(this.pid());
    }

    public abstract byte pid();

    public abstract void decode() throws DataPacketDecodeException;

    public abstract void encode();

    public final void tryEncode() {
        if (!this.isEncoded) {
            this.isEncoded = true;
            this.encode();
        }
    }

    @Override
    public DataPacket reset() {
        super.reset();
        this.putUnsignedVarInt(this.packetId());
        return this;
    }

    @Deprecated
    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Deprecated
    public int getChannel() {
        return channel;
    }

    public DataPacket clean() {
        this.setBuffer(null);
        this.setOffset(0);
        this.isEncoded = false;
        return this;
    }

    @Override
    public DataPacket clone() {
        try {
            DataPacket packet = (DataPacket) super.clone();
            if (this.count >= 0) {
                packet.setBuffer(this.getBuffer()); // prevent reflecting same buffer instance
            } else if (this.getBufferUnsafe() != null) {
                packet.setBuffer(this.getBufferUnsafe().clone());
            } else {
                packet.setBuffer(new byte[32]);
            }
            packet.offset = this.offset;
            packet.count = this.count;
            return packet;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public BatchPacket compress() {
        return compress(Server.getInstance().getSettings().network().compression().compressionLevel());
    }

    public BatchPacket compress(int level) {
        BinaryStream stream = new BinaryStream();
        byte[] buf = this.getBuffer();
        stream.putUnsignedVarInt(buf.length);
        stream.put(buf);
        try {
            BatchPacket batched = new BatchPacket();
            if (Server.getInstance().getSettings().network().compression().useSnappyCompression()) {
                batched.payload = SnappyCompression.compress(stream.getBuffer());
            } else {
                batched.payload = Zlib.deflateRaw(stream.getBuffer(), level);
            }
            return batched;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void decodeUnsupported() {
        if (Nukkit.DEBUG > 1) {
            Server.getInstance().getLogger().debug("Warning: decode() not implemented for " + this.getClass().getName());
        }
    }

    void encodeUnsupported() {
        if (Nukkit.DEBUG > 1) {
            Server.getInstance().getLogger().debug("Warning: encode() not implemented for " + this.getClass().getName());
            Thread.dumpStack();
        }
    }

    @Override
    public BlockVector3 getBlockVector3() {
        return super.getBlockVector3(this.protocol);
    }

    @Override
    public void putBlockVector3(BlockVector3 v) {
        super.putBlockVector3(this.protocol, v);
    }

    @Override
    public void putBlockVector3(int x, int y, int z) {
        super.putBlockVector3(this.protocol, x, y, z);
    }
}
