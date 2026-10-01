package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabBlack extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabBlack() {
        this(0);
    }

    public BlockWoolDoubleSlabBlack(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLACK_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return BLACK_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLACK;
    }
}
