package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabBrown extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabBrown() {
        this(0);
    }

    public BlockWoolDoubleSlabBrown(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BROWN_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return BROWN_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BROWN;
    }
}
