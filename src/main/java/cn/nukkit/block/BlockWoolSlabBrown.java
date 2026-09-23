package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabBrown extends BlockWoolSlab {

    public BlockWoolSlabBrown() {
        this(0);
    }

    public BlockWoolSlabBrown(int meta) {
        super(meta, BROWN_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return BROWN_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BROWN;
    }
}
