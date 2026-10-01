package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabPink extends BlockWoolSlab {

    public BlockWoolSlabPink() {
        this(0);
    }

    public BlockWoolSlabPink(int meta) {
        super(meta, PINK_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return PINK_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PINK;
    }
}
