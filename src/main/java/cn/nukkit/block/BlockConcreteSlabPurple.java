package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabPurple extends BlockConcreteSlab {

    public BlockConcreteSlabPurple() {
        this(0);
    }

    public BlockConcreteSlabPurple(int meta) {
        super(meta, PURPLE_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return PURPLE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PURPLE;
    }
}
