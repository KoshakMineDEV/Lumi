package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockSpruceFence extends BlockFence {
    public BlockSpruceFence() {
        super();
    }

    @Override
    public String getName() {
        return "Spruce Fence";
    }

    @Override
    public int getId() {
        return SPRUCE_FENCE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
