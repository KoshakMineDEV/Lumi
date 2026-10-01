package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabPink extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabPink() {
        this(0);
    }

    public BlockWoolDoubleSlabPink(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PINK_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return PINK_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PINK;
    }
}
