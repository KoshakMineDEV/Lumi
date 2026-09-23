package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabGreen extends BlockWoolSlab {

    public BlockWoolSlabGreen() {
        this(0);
    }

    public BlockWoolSlabGreen(int meta) {
        super(meta, GREEN_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return GREEN_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GREEN;
    }
}
