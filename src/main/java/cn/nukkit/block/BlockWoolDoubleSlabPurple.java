package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabPurple extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabPurple() {
        this(0);
    }

    public BlockWoolDoubleSlabPurple(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PURPLE_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return PURPLE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PURPLE;
    }
}
