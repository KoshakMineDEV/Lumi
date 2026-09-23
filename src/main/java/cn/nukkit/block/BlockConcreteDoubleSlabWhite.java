package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabWhite extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabWhite() {
        this(0);
    }

    public BlockConcreteDoubleSlabWhite(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return WHITE_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return WHITE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.WHITE;
    }
}
