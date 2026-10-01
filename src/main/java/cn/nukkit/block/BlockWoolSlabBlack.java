package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabBlack extends BlockWoolSlab {

    public BlockWoolSlabBlack() {
        this(0);
    }

    public BlockWoolSlabBlack(int meta) {
        super(meta, BLACK_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return BLACK_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLACK;
    }
}
