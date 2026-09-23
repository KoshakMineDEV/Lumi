package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabPink extends BlockConcreteSlab {

    public BlockConcreteSlabPink() {
        this(0);
    }

    public BlockConcreteSlabPink(int meta) {
        super(meta, PINK_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return PINK_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PINK;
    }
}
