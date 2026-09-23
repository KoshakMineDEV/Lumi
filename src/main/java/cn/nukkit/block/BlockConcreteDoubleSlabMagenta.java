package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabMagenta extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabMagenta() {
        this(0);
    }

    public BlockConcreteDoubleSlabMagenta(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return MAGENTA_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return MAGENTA_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.MAGENTA;
    }
}
