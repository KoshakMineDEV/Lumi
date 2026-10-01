package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabYellow extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabYellow() {
        this(0);
    }

    public BlockWoolDoubleSlabYellow(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return YELLOW_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return YELLOW_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.YELLOW;
    }
}
