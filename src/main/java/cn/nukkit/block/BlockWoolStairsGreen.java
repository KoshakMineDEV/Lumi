package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsGreen extends BlockWoolStairs {

    public BlockWoolStairsGreen() {
        this(0);
    }

    public BlockWoolStairsGreen(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GREEN_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GREEN;
    }
}
