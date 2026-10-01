package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabLightGray extends BlockConcreteSlab {

    public BlockConcreteSlabLightGray() {
        this(0);
    }

    public BlockConcreteSlabLightGray(int meta) {
        super(meta, LIGHT_GRAY_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return LIGHT_GRAY_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_GRAY;
    }
}
