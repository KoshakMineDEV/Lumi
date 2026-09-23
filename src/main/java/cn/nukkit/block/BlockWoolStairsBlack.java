package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsBlack extends BlockWoolStairs {

    public BlockWoolStairsBlack() {
        this(0);
    }

    public BlockWoolStairsBlack(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLACK_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLACK;
    }
}
