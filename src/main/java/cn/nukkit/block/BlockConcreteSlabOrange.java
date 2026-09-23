package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabOrange extends BlockConcreteSlab {

    public BlockConcreteSlabOrange() {
        this(0);
    }

    public BlockConcreteSlabOrange(int meta) {
        super(meta, ORANGE_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return ORANGE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.ORANGE;
    }
}
