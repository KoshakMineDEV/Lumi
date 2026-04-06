package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockAcaciaFence extends BlockFence {
    public BlockAcaciaFence() {
        super();
    }

    @Override
    public String getName() {
        return "Acacia Fence";
    }

    @Override
    public int getId() {
        return ACACIA_FENCE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }
}
