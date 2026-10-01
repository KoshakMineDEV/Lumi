package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabGreen extends BlockConcreteSlab {

    public BlockConcreteSlabGreen() {
        this(0);
    }

    public BlockConcreteSlabGreen(int meta) {
        super(meta, GREEN_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return GREEN_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GREEN;
    }
}
