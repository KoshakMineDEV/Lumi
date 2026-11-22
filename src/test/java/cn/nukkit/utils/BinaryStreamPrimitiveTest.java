package cn.nukkit.utils;

import cn.nukkit.network.protocol.DataPacket;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BinaryStreamPrimitiveTest {

    @Test
    void primitiveWritesMatchExistingBinaryEncoders() {
        int intValue = 0x89abcdef;
        long longValue = 0x89abcdef01234567L;
        int shortValue = 0xfedc;
        int triadValue = 0xfedcba;
        float floatValue = -123.625f;

        assertArrayEquals(Binary.writeInt(intValue), encode(stream -> stream.putInt(intValue)));
        assertArrayEquals(Binary.writeLInt(intValue), encode(stream -> stream.putLInt(intValue)));
        assertArrayEquals(Binary.writeLong(longValue), encode(stream -> stream.putLong(longValue)));
        assertArrayEquals(Binary.writeLLong(longValue), encode(stream -> stream.putLLong(longValue)));
        assertArrayEquals(Binary.writeShort(shortValue), encode(stream -> stream.putShort(shortValue)));
        assertArrayEquals(Binary.writeLShort(shortValue), encode(stream -> stream.putLShort(shortValue)));
        assertArrayEquals(Binary.writeTriad(triadValue), encode(stream -> stream.putTriad(triadValue)));
        assertArrayEquals(Binary.writeLTriad(triadValue), encode(stream -> stream.putLTriad(triadValue)));
        assertArrayEquals(Binary.writeFloat(floatValue), encode(stream -> stream.putFloat(floatValue)));
        assertArrayEquals(Binary.writeLFloat(floatValue), encode(stream -> stream.putLFloat(floatValue)));

        UUID uuid = UUID.fromString("01234567-89ab-cdef-fedc-ba9876543210");
        assertArrayEquals(Binary.writeUUID(uuid), encode(stream -> stream.putUUID(uuid)));
    }

    @Test
    void primitiveReadsMatchExistingBinaryDecoders() {
        int intValue = 0x89abcdef;
        long longValue = 0x89abcdef01234567L;
        int shortValue = 0xfedc;
        int triadValue = 0xfedcba;
        float floatValue = -123.625f;

        assertEquals(Binary.readInt(Binary.writeInt(intValue)), read(Binary.writeInt(intValue), BinaryStream::getInt));
        assertEquals(Binary.readLInt(Binary.writeLInt(intValue)), read(Binary.writeLInt(intValue), BinaryStream::getLInt));
        assertEquals(Binary.readLong(Binary.writeLong(longValue)), read(Binary.writeLong(longValue), BinaryStream::getLong));
        assertEquals(Binary.readLLong(Binary.writeLLong(longValue)), read(Binary.writeLLong(longValue), BinaryStream::getLLong));
        assertEquals(Binary.readShort(Binary.writeShort(shortValue)), read(Binary.writeShort(shortValue), BinaryStream::getShort));
        assertEquals(Binary.readLShort(Binary.writeLShort(shortValue)), read(Binary.writeLShort(shortValue), BinaryStream::getLShort));
        assertEquals(Binary.readTriad(Binary.writeTriad(triadValue)), read(Binary.writeTriad(triadValue), BinaryStream::getTriad));
        assertEquals(Binary.readLTriad(Binary.writeLTriad(triadValue)), read(Binary.writeLTriad(triadValue), BinaryStream::getLTriad));
        assertEquals(Float.floatToIntBits(Binary.readFloat(Binary.writeFloat(floatValue))),
                Float.floatToIntBits(read(Binary.writeFloat(floatValue), BinaryStream::getFloat)));
        assertEquals(Float.floatToIntBits(Binary.readLFloat(Binary.writeLFloat(floatValue))),
                Float.floatToIntBits(read(Binary.writeLFloat(floatValue), BinaryStream::getLFloat)));

        UUID uuid = UUID.fromString("01234567-89ab-cdef-fedc-ba9876543210");
        BinaryStream uuidStream = new BinaryStream(Binary.writeUUID(uuid));
        assertEquals(uuid, uuidStream.getUUID());
    }

    @Test
    void packetSliceDoesNotReadIntoFollowingFrame() {
        byte[] batchData = {0x11, 0x12, 0x01, 0x2a, 0x33, 0x7f};
        TestPacket packet = new TestPacket();
        packet.setBufferSlice(batchData, 2, 3, 5);

        assertEquals(3, packet.getSerializedLength());
        assertEquals(1, packet.getOffset());
        assertEquals(3, packet.getCount());
        assertArrayEquals(new byte[]{0x01, 0x2a, 0x33}, packet.getBuffer());
        assertEquals(0x2a, packet.getByte());
        assertEquals(0x33, packet.getByte());
        assertThrows(IndexOutOfBoundsException.class, packet::getByte);
        assertArrayEquals(new byte[]{0x01, 0x2a, 0x33}, packet.clone().getBuffer());

        BinaryStream batch = new BinaryStream();
        packet.writeLengthPrefixedTo(batch);
        assertArrayEquals(new byte[]{0x03, 0x01, 0x2a, 0x33}, batch.getBuffer());

        packet.reset();
        assertArrayEquals(new byte[]{0}, packet.getBuffer());
        assertArrayEquals(new byte[]{0x11, 0x12, 0x01, 0x2a, 0x33, 0x7f}, batchData);
    }

    private static byte[] encode(Consumer<BinaryStream> writer) {
        BinaryStream stream = new BinaryStream();
        writer.accept(stream);
        return stream.getBuffer();
    }

    private static <T> T read(byte[] data, Reader<T> reader) {
        return reader.read(new BinaryStream(data));
    }

    @FunctionalInterface
    private interface Reader<T> {
        T read(BinaryStream stream);
    }

    private static final class TestPacket extends DataPacket {
        @Override
        public byte pid() {
            return 0;
        }

        @Override
        public void decode() {
        }

        @Override
        public void encode() {
        }
    }
}
