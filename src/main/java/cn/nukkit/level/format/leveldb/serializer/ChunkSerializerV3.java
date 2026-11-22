package cn.nukkit.level.format.leveldb.serializer;

import cn.nukkit.level.DimensionData;
import cn.nukkit.level.format.Chunk;
import cn.nukkit.level.format.generic.EmptyChunkSection;
import cn.nukkit.level.format.leveldb.BlockStateMapping;
import cn.nukkit.level.format.leveldb.LevelDBKey;
import cn.nukkit.level.format.leveldb.structure.ChunkBuilder;
import cn.nukkit.level.format.leveldb.structure.LevelDBChunkSection;
import cn.nukkit.level.format.leveldb.structure.StateBlockStorage;
import cn.nukkit.level.ChunkException;
import cn.nukkit.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import static cn.nukkit.level.format.leveldb.LevelDBConstants.CURRENT_LEVEL_SUBCHUNK_VERSION;

public class ChunkSerializerV3 implements ChunkSerializer {

    public static final ChunkSerializer INSTANCE = new ChunkSerializerV3();

    @Override
    public void serializer(WriteBatch writeBatch, Chunk chunk) {
        DimensionData dimensionData = chunk.getProvider().getLevel().getDimensionData();
        for (int ySection = dimensionData.getMinSectionY(); ySection <= dimensionData.getMaxSectionY(); ++ySection) {
            byte[] key = LevelDBKey.SUB_CHUNK_PREFIX.getKey(
                    chunk.getX(), chunk.getZ(), ySection, dimensionData.getDimensionId()
            );

            LevelDBChunkSection section = (LevelDBChunkSection) chunk.getSection(ySection);
            if (section == null) {
                writeBatch.delete(key);
                continue;
            }

            if (!section.isDirty()) {
                continue;
            }

            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
            try {
                byteBuf.writeByte(CURRENT_LEVEL_SUBCHUNK_VERSION);
                ChunkSectionSerializers.serializer(byteBuf, section.getStorages(), ySection, CURRENT_LEVEL_SUBCHUNK_VERSION);
                writeBatch.put(key, Utils.convertByteBuf2Array(byteBuf));
            } finally {
                byteBuf.release();
            }

            byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
            try {
                byte[] blockLight = section.getLightArray();
                if (blockLight != EmptyChunkSection.EMPTY_LIGHT_ARR) {
                    writeBatch.put(LevelDBKey.NUKKIT_BLOCK_LIGHT.getKey(chunk.getX(), chunk.getZ(), ySection, chunk.getProvider().getLevel().getDimension()), blockLight);
                }
            } finally {
                byteBuf.release();
            }
        }
    }

    @Override
    public void deserialize(DB db, ChunkBuilder chunkBuilder) {
        int chunkX = chunkBuilder.getChunkX();
        int chunkZ = chunkBuilder.getChunkZ();

        Int2ShortOpenHashMap extraBlocks = null;
        byte[] extraRawData = db.get(LevelDBKey.BLOCK_EXTRA_DATA.getKey(chunkX, chunkZ, chunkBuilder.getDimensionData().getDimensionId()));
        if (extraRawData != null) {
            extraBlocks = new Int2ShortOpenHashMap();
            ByteBuf extraData = Unpooled.wrappedBuffer(extraRawData);
            int count = extraData.readIntLE();
            for (int i = 0; i < count; ++i) {
                extraBlocks.put(extraData.readIntLE(), extraData.readShortLE());
            }
        }

        DimensionData dimensionInfo = chunkBuilder.getDimensionData();
        LevelDBChunkSection[] sections = new LevelDBChunkSection[dimensionInfo.getHeight() >> 4];
        for (int ySection = dimensionInfo.getMinSectionY(); ySection <= dimensionInfo.getMaxSectionY(); ++ySection) {
            StateBlockStorage[] stateBlockStorageArray;
            byte[] bytes = db.get(LevelDBKey.SUB_CHUNK_PREFIX.getKey(chunkX, chunkZ, ySection, chunkBuilder.getDimensionData().getDimensionId()));
            if (bytes != null) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
                if (!byteBuf.isReadable()) {
                    throw new ChunkException("Chunk section " + ySection + " is empty");
                }
                short subChunkVersion = byteBuf.readUnsignedByte();
                stateBlockStorageArray = ChunkSectionSerializers.deserialize(byteBuf, chunkBuilder, subChunkVersion);

                if (stateBlockStorageArray[1] == null) {
                    stateBlockStorageArray[1] = new StateBlockStorage();
                }

                if (extraBlocks != null && !extraBlocks.isEmpty()) {
                    restoreExtraBlocks(extraBlocks, stateBlockStorageArray[1], ySection);
                }

                byte[] blockLight = db.get(LevelDBKey.NUKKIT_BLOCK_LIGHT.getKey(chunkX, chunkZ, ySection, chunkBuilder.getLevel().getDimension()));

                // Overworld (dimensionId=0) has sky light, Nether/End do not
                boolean hasSkyLight = dimensionInfo.getDimensionId() == 0;

                sections[ySection + dimensionInfo.getSectionOffset()] = new LevelDBChunkSection(chunkBuilder.getLevel(), ySection, stateBlockStorageArray, blockLight, hasSkyLight);
            }
        }
        chunkBuilder.sections(sections);
    }

    static void restoreExtraBlocks(Int2ShortOpenHashMap extraBlocks, StateBlockStorage storage, int ySection) {
        int sectionBaseY = (ySection << 4) & 0x7F;
        for (Int2ShortMap.Entry entry : extraBlocks.int2ShortEntrySet()) {
            int rawIndex = entry.getIntKey();
            short packedIndex = (short) rawIndex;
            // The previous lookup widened a short to int, so only sign-extended short keys matched.
            if (rawIndex != packedIndex) {
                continue;
            }

            int packed = packedIndex & 0xFFFF;
            int localY = ((packed >>> 9 & 0x7F) - sectionBaseY) & 0x7F;
            if (localY >= 16) {
                continue;
            }

            int x = packed & 0xF;
            int z = packed >>> 4 & 0xF;
            short fullBlock = entry.getShortValue();
            int blockId = fullBlock & 0xFF;
            int blockData = fullBlock >>> 8 & 0xF;
            storage.set(StateBlockStorage.elementIndex(x, localY, z), BlockStateMapping.get().getState(blockId, blockData));
        }
    }
}
