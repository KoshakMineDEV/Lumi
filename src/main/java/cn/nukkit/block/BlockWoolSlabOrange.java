package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabOrange extends BlockWoolSlab {

    public BlockWoolSlabOrange() {
        this(0);
    }

    public BlockWoolSlabOrange(int meta) {
        super(meta, ORANGE_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return ORANGE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.ORANGE;
    }
}
