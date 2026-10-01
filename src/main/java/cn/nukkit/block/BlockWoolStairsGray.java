package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsGray extends BlockWoolStairs {

    public BlockWoolStairsGray() {
        this(0);
    }

    public BlockWoolStairsGray(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GRAY_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GRAY;
    }
}
