package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabGray extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabGray() {
        this(0);
    }

    public BlockConcreteDoubleSlabGray(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GRAY_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return GRAY_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GRAY;
    }
}
