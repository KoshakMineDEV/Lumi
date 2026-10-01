package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabCyan extends BlockConcreteSlab {

    public BlockConcreteSlabCyan() {
        this(0);
    }

    public BlockConcreteSlabCyan(int meta) {
        super(meta, CYAN_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return CYAN_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.CYAN;
    }
}
