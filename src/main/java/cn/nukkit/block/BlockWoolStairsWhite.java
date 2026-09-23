package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsWhite extends BlockWoolStairs {

    public BlockWoolStairsWhite() {
        this(0);
    }

    public BlockWoolStairsWhite(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return WHITE_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.WHITE;
    }
}
