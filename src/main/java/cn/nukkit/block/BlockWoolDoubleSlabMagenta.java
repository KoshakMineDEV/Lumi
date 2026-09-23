package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabMagenta extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabMagenta() {
        this(0);
    }

    public BlockWoolDoubleSlabMagenta(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return MAGENTA_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return MAGENTA_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.MAGENTA;
    }
}
