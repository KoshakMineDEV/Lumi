package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabGreen extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabGreen() {
        this(0);
    }

    public BlockConcreteDoubleSlabGreen(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GREEN_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return GREEN_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GREEN;
    }
}
