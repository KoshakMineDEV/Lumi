package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabRed extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabRed() {
        this(0);
    }

    public BlockConcreteDoubleSlabRed(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return RED_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.RED;
    }
}
