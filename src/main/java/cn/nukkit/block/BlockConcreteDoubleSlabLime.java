package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabLime extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabLime() {
        this(0);
    }

    public BlockConcreteDoubleSlabLime(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIME_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return LIME_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIME;
    }
}
