package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabBlack extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabBlack() {
        this(0);
    }

    public BlockConcreteDoubleSlabBlack(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLACK_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return BLACK_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLACK;
    }
}
