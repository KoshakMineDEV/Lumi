package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabLightBlue extends BlockWoolSlab {

    public BlockWoolSlabLightBlue() {
        this(0);
    }

    public BlockWoolSlabLightBlue(int meta) {
        super(meta, LIGHT_BLUE_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return LIGHT_BLUE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
