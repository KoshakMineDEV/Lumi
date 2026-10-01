package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabBlue extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabBlue() {
        this(0);
    }

    public BlockConcreteDoubleSlabBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLUE_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return BLUE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLUE;
    }
}
