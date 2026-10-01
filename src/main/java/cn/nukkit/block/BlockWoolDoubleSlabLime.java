package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabLime extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabLime() {
        this(0);
    }

    public BlockWoolDoubleSlabLime(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIME_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return LIME_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIME;
    }
}
