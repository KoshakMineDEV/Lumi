package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsLightGray extends BlockWoolStairs {

    public BlockWoolStairsLightGray() {
        this(0);
    }

    public BlockWoolStairsLightGray(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIGHT_GRAY_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_GRAY;
    }
}
