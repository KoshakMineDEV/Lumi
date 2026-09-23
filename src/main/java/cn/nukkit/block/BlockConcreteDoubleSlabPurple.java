package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabPurple extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabPurple() {
        this(0);
    }

    public BlockConcreteDoubleSlabPurple(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PURPLE_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return PURPLE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PURPLE;
    }
}
