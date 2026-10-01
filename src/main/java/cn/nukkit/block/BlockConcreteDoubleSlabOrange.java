package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabOrange extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabOrange() {
        this(0);
    }

    public BlockConcreteDoubleSlabOrange(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ORANGE_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return ORANGE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.ORANGE;
    }
}
