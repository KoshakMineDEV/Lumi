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

import java.util.Arrays;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
@Log4j2
public abstract class DataPacket extends BinaryStream implements Cloneable {

    private int packetBufferStart;
    private boolean packetBufferSlice;

    public int protocol = Integer.MAX_VALUE;

    public volatile boolean isEncoded = false;

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
        if (this.packetBufferSlice) {
            this.setBuffer(this.getBuffer());
        }
        this.packetBufferStart = 0;
        this.packetBufferSlice = false;
        super.reset();
        this.putUnsignedVarInt(this.packetId());
        return this;
    }

    public DataPacket clean() {
        this.setBuffer(null);
        this.setOffset(0);
        this.isEncoded = false;
        return this;
    }

    @Override
    public void setBuffer(byte[] buffer) {
        super.setBuffer(buffer);
        this.packetBufferStart = 0;
        this.packetBufferSlice = false;
    }

    /** Sets a bounded, zero-copy view of one packet within a decoded batch. */
    public void setBufferSlice(byte[] buffer, int packetStart, int offset, int packetEnd) {
        super.setBuffer(buffer);
        this.packetBufferStart = packetStart;
        this.packetBufferSlice = true;
        this.offset = offset;
        this.count = packetEnd;
    }

    public int getPacketBufferStart() {
        return this.packetBufferStart;
    }

    @Override
    public int getOffset() {
        return this.packetBufferSlice ? this.offset - this.packetBufferStart : super.getOffset();
    }

    @Override
    public void setOffset(int offset) {
        super.setOffset(this.packetBufferSlice ? this.packetBufferStart + offset : offset);
    }

    @Override
    public int getCount() {
        return this.packetBufferSlice ? this.count - this.packetBufferStart : super.getCount();
    }

    @Override
    public void setCount(int count) {
        super.setCount(this.packetBufferSlice ? this.packetBufferStart + count : count);
    }

    @Override
    public byte[] getBuffer() {
        if (this.packetBufferStart == 0) {
            return super.getBuffer();
        }
        return Arrays.copyOfRange(this.getBufferUnsafe(), this.packetBufferStart, this.count);
    }

    public int getSerializedLength() {
        return this.count - this.packetBufferStart;
    }

    public void writeLengthPrefixedTo(BinaryStream target) {
        int length = this.getSerializedLength();
        if (length < 0) {
            throw new IllegalStateException("Packet has no serialized buffer");
        }
        target.putUnsignedVarInt(length);
        target.put(this.getBufferUnsafe(), this.packetBufferStart, length);
    }

    @Override
    public byte getSingedByte() {
        this.checkPacketSliceReadable();
        return super.getSingedByte();
    }

    @Override
    public int getByte() {
        this.checkPacketSliceReadable();
        return super.getByte();
    }

    private void checkPacketSliceReadable() {
        if (this.packetBufferSlice && this.offset >= this.count) {
            throw new IndexOutOfBoundsException("Packet frame exhausted at offset " + this.offset);
        }
    }

    @Override
    public DataPacket clone() {
        try {
            DataPacket packet = (DataPacket) super.clone();
            if (!this.isEncoded && this.count == 0) {
                byte[] buffer = this.getBufferUnsafe();
                packet.setBuffer(new byte[buffer == null ? 32 : buffer.length]);
                packet.offset = 0;
                packet.count = 0;
            } else if (this.count >= 0) {
                packet.setBuffer(this.getBuffer()); // prevent reflecting same buffer instance
                packet.offset = this.offset - this.packetBufferStart;
                packet.count = this.count - this.packetBufferStart;
            } else if (this.getBufferUnsafe() != null) {
                packet.setBuffer(this.getBufferUnsafe().clone());
                packet.offset = this.offset;
                packet.count = this.count;
            } else {
                packet.setBuffer(new byte[32]);
                packet.offset = this.offset;
                packet.count = this.count;
            }
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
        this.writeLengthPrefixedTo(stream);
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
