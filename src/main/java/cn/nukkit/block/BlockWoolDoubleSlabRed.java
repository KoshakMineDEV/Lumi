package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabRed extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabRed() {
        this(0);
    }

    public BlockWoolDoubleSlabRed(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return RED_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.RED;
    }
}
