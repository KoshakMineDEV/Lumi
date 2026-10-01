package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabYellow extends BlockWoolSlab {

    public BlockWoolSlabYellow() {
        this(0);
    }

    public BlockWoolSlabYellow(int meta) {
        super(meta, YELLOW_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return YELLOW_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.YELLOW;
    }
}
