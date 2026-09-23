package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockPoplarFence extends BlockFence {

    public BlockPoplarFence() {
        this(0);
    }

    public BlockPoplarFence(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Poplar Fence";
    }

    @Override
    public int getId() {
        return POPLAR_FENCE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
