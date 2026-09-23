package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabGreen extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabGreen() {
        this(0);
    }

    public BlockWoolDoubleSlabGreen(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GREEN_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return GREEN_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GREEN;
    }
}
