package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabLightGray extends BlockWoolSlab {

    public BlockWoolSlabLightGray() {
        this(0);
    }

    public BlockWoolSlabLightGray(int meta) {
        super(meta, LIGHT_GRAY_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return LIGHT_GRAY_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_GRAY;
    }
}
