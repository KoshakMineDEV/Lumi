package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockBirchFence extends BlockFence {
    public BlockBirchFence() {
        this(0);
    }

    public BlockBirchFence(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Birch Fence";
    }

    @Override
    public int getId() {
        return BIRCH_FENCE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SAND_BLOCK_COLOR;
    }
}
