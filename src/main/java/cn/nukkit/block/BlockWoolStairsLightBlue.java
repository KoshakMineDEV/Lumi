package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsLightBlue extends BlockWoolStairs {

    public BlockWoolStairsLightBlue() {
        this(0);
    }

    public BlockWoolStairsLightBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIGHT_BLUE_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
