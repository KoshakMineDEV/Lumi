package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabLightBlue extends BlockConcreteSlab {

    public BlockConcreteSlabLightBlue() {
        this(0);
    }

    public BlockConcreteSlabLightBlue(int meta) {
        super(meta, LIGHT_BLUE_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return LIGHT_BLUE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
