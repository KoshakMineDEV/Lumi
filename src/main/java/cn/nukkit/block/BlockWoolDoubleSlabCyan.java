package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabCyan extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabCyan() {
        this(0);
    }

    public BlockWoolDoubleSlabCyan(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CYAN_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return CYAN_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.CYAN;
    }
}
