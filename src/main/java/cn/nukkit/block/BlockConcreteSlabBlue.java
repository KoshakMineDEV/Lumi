package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabBlue extends BlockConcreteSlab {

    public BlockConcreteSlabBlue() {
        this(0);
    }

    public BlockConcreteSlabBlue(int meta) {
        super(meta, BLUE_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return BLUE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLUE;
    }
}
