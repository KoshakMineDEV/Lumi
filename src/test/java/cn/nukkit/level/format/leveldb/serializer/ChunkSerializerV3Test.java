package cn.nukkit.level.format.leveldb.serializer;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.format.leveldb.BlockStateMapping;
import cn.nukkit.level.format.leveldb.structure.StateBlockStorage;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkSerializerV3Test {

    @Test
    void restoresSparseExtraBlocksAtLocalSubChunkCoordinates() {
        int x = 3;
        int z = 11;
        int worldY = 38;
        int ySection = 2;
        int localY = worldY - (ySection << 4);
        short packedIndex = (short) (x | z << 4 | (worldY & 0xFF) << 9);
        short fullBlock = (short) (BlockID.STONE | (2 << 8));

        Int2ShortOpenHashMap extraBlocks = new Int2ShortOpenHashMap();
        extraBlocks.put(packedIndex, fullBlock);
        StateBlockStorage storage = new StateBlockStorage();

        ChunkSerializerV3.restoreExtraBlocks(extraBlocks, storage, ySection);

        assertEquals(BlockStateMapping.get().getState(BlockID.STONE, 2), storage.getBlockState(x, localY, z));
        assertEquals(BlockID.STONE << Block.DATA_BITS | 2, storage.get(x, localY, z));
    }

    @Test
    void restoresNegativeWorldYInTheMatchingSubChunk() {
        int x = 7;
        int z = 4;
        int worldY = -46;
        int ySection = -3;
        int localY = worldY - (ySection << 4);
        short packedIndex = (short) (x | z << 4 | (worldY & 0xFF) << 9);

        Int2ShortOpenHashMap extraBlocks = new Int2ShortOpenHashMap();
        extraBlocks.put(packedIndex, (short) BlockID.STONE);
        StateBlockStorage storage = new StateBlockStorage();

        ChunkSerializerV3.restoreExtraBlocks(extraBlocks, storage, ySection);

        assertEquals(BlockStateMapping.get().getState(BlockID.STONE, 0), storage.getBlockState(x, localY, z));
    }
}
