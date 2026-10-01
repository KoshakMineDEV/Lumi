package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabCyan extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabCyan() {
        this(0);
    }

    public BlockConcreteDoubleSlabCyan(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CYAN_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return CYAN_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.CYAN;
    }
}
