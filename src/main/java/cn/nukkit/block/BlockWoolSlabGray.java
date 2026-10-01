package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabGray extends BlockWoolSlab {

    public BlockWoolSlabGray() {
        this(0);
    }

    public BlockWoolSlabGray(int meta) {
        super(meta, GRAY_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return GRAY_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GRAY;
    }
}
