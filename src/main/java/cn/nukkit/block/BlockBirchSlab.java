package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockBirchSlab extends BlockWoodenSlab {
    public BlockBirchSlab() {
        this(0);
    }

    public BlockBirchSlab(int meta) {
        super(meta, BIRCH_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return BIRCH_SLAB;
    }

    @Override
    public String getName() {
        return "Birch Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SAND_BLOCK_COLOR;
    }
}
