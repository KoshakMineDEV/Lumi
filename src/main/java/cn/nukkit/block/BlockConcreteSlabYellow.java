package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabYellow extends BlockConcreteSlab {

    public BlockConcreteSlabYellow() {
        this(0);
    }

    public BlockConcreteSlabYellow(int meta) {
        super(meta, YELLOW_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return YELLOW_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.YELLOW;
    }
}
