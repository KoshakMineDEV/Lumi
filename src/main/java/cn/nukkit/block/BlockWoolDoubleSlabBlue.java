package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabBlue extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabBlue() {
        this(0);
    }

    public BlockWoolDoubleSlabBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLUE_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return BLUE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLUE;
    }
}
