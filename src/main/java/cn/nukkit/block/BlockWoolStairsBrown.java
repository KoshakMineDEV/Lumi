package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsBrown extends BlockWoolStairs {

    public BlockWoolStairsBrown() {
        this(0);
    }

    public BlockWoolStairsBrown(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BROWN_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BROWN;
    }
}
