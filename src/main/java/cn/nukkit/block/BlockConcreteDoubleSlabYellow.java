package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabYellow extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabYellow() {
        this(0);
    }

    public BlockConcreteDoubleSlabYellow(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return YELLOW_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return YELLOW_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.YELLOW;
    }
}
