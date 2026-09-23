package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabPink extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabPink() {
        this(0);
    }

    public BlockConcreteDoubleSlabPink(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PINK_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return PINK_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PINK;
    }
}
