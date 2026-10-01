package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsOrange extends BlockWoolStairs {

    public BlockWoolStairsOrange() {
        this(0);
    }

    public BlockWoolStairsOrange(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ORANGE_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.ORANGE;
    }
}
