package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabOrange extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabOrange() {
        this(0);
    }

    public BlockWoolDoubleSlabOrange(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ORANGE_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return ORANGE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.ORANGE;
    }
}
