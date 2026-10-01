package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsBlue extends BlockWoolStairs {

    public BlockWoolStairsBlue() {
        this(0);
    }

    public BlockWoolStairsBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLUE_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLUE;
    }
}
