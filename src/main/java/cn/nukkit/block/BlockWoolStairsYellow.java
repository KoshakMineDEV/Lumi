package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsYellow extends BlockWoolStairs {

    public BlockWoolStairsYellow() {
        this(0);
    }

    public BlockWoolStairsYellow(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return YELLOW_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.YELLOW;
    }
}
