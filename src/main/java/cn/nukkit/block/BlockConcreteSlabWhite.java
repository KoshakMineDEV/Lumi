package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteSlabWhite extends BlockConcreteSlab {

    public BlockConcreteSlabWhite() {
        this(0);
    }

    public BlockConcreteSlabWhite(int meta) {
        super(meta, WHITE_CONCRETE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return WHITE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.WHITE;
    }
}
