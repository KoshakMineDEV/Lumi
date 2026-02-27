package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockPrismarineBricks extends BlockPrismarine {
    public BlockPrismarineBricks() {
        super();
    }

    @Override
    public int getId() {
        return PRISMARINE_BRICKS;
    }

    @Override
    public String getName() {
        return "Prismarine Bricks";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.DIAMOND_BLOCK_COLOR;
    }
}
