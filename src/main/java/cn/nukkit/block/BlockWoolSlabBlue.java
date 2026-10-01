package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabBlue extends BlockWoolSlab {

    public BlockWoolSlabBlue() {
        this(0);
    }

    public BlockWoolSlabBlue(int meta) {
        super(meta, BLUE_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return BLUE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLUE;
    }
}
