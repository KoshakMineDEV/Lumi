package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsPurple extends BlockWoolStairs {

    public BlockWoolStairsPurple() {
        this(0);
    }

    public BlockWoolStairsPurple(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PURPLE_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PURPLE;
    }
}
