package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabGray extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabGray() {
        this(0);
    }

    public BlockWoolDoubleSlabGray(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GRAY_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return GRAY_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GRAY;
    }
}
