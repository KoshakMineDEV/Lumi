package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabLime extends BlockWoolSlab {

    public BlockWoolSlabLime() {
        this(0);
    }

    public BlockWoolSlabLime(int meta) {
        super(meta, LIME_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return LIME_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIME;
    }
}
