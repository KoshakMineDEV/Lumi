package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabRed extends BlockConcreteSlab {

    public BlockConcreteSlabRed() {
        this(0);
    }

    public BlockConcreteSlabRed(int meta) {
        super(meta, RED_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return RED_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.RED;
    }
}
