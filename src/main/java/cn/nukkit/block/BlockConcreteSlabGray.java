package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabGray extends BlockConcreteSlab {

    public BlockConcreteSlabGray() {
        this(0);
    }

    public BlockConcreteSlabGray(int meta) {
        super(meta, GRAY_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return GRAY_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GRAY;
    }
}
