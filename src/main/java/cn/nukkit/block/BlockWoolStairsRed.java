package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsRed extends BlockWoolStairs {

    public BlockWoolStairsRed() {
        this(0);
    }

    public BlockWoolStairsRed(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.RED;
    }
}
