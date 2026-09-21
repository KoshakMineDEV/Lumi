package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockDarkOakFence extends BlockFence {
    public BlockDarkOakFence() {
        this(0);
    }

    public BlockDarkOakFence(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Dark Oak Fence";
    }

    @Override
    public int getId() {
        return DARK_OAK_FENCE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BROWN_BLOCK_COLOR;
    }
}
