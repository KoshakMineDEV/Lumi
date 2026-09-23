package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolDoubleSlabWhite extends BlockWoolDoubleSlab {

    public BlockWoolDoubleSlabWhite() {
        this(0);
    }

    public BlockWoolDoubleSlabWhite(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return WHITE_WOOL_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return WHITE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.WHITE;
    }
}
