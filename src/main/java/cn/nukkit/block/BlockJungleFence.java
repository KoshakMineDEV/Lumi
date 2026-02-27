package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockJungleFence extends BlockFence {
    public BlockJungleFence() {
        super();
    }

    @Override
    public String getName() {
        return "Jungle Fence";
    }

    @Override
    public int getId() {
        return JUNGLE_FENCE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.DIRT_BLOCK_COLOR;
    }
}
