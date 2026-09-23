package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabBlack extends BlockConcreteSlab {

    public BlockConcreteSlabBlack() {
        this(0);
    }

    public BlockConcreteSlabBlack(int meta) {
        super(meta, BLACK_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return BLACK_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLACK;
    }
}
