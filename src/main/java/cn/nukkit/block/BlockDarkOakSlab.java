package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockDarkOakSlab extends BlockWoodenSlab {
    public BlockDarkOakSlab() {
        this(0);
    }

    public BlockDarkOakSlab(int meta) {
        super(meta, DARK_OAK_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return DARK_OAK_SLAB;
    }

    @Override
    public String getName() {
        return "Dark Oak Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BROWN_BLOCK_COLOR;
    }
}
