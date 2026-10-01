package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabBrown extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabBrown() {
        this(0);
    }

    public BlockConcreteDoubleSlabBrown(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BROWN_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return BROWN_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BROWN;
    }
}
