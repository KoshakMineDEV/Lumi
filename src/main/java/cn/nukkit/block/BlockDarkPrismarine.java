package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockDarkPrismarine extends BlockPrismarine {
    public BlockDarkPrismarine() {
        super();
    }

    @Override
    public int getId() {
        return DARK_PRISMARINE;
    }

    @Override
    public String getName() {
        return "Dark Prismarine";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.DIAMOND_BLOCK_COLOR;
    }
}
