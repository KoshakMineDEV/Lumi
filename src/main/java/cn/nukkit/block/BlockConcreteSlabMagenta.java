package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabMagenta extends BlockConcreteSlab {

    public BlockConcreteSlabMagenta() {
        this(0);
    }

    public BlockConcreteSlabMagenta(int meta) {
        super(meta, MAGENTA_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return MAGENTA_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.MAGENTA;
    }
}
