package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabPurple extends BlockWoolSlab {

    public BlockWoolSlabPurple() {
        this(0);
    }

    public BlockWoolSlabPurple(int meta) {
        super(meta, PURPLE_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return PURPLE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PURPLE;
    }
}
