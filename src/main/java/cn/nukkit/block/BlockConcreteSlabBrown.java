package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabBrown extends BlockConcreteSlab {

    public BlockConcreteSlabBrown() {
        this(0);
    }

    public BlockConcreteSlabBrown(int meta) {
        super(meta, BROWN_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return BROWN_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BROWN;
    }
}
