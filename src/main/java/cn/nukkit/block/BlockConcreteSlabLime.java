package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabLime extends BlockConcreteSlab {

    public BlockConcreteSlabLime() {
        this(0);
    }

    public BlockConcreteSlabLime(int meta) {
        super(meta, LIME_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return LIME_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIME;
    }
}
