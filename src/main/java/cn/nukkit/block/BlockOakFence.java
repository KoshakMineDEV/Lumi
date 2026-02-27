package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockOakFence extends BlockFence {
    public BlockOakFence() {
        super();
    }

    @Override
    public String getName() {
        return "Oak Fence";
    }

    @Override
    public int getId() {
        return OAK_FENCE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.WOOD_BLOCK_COLOR;
    }
}
