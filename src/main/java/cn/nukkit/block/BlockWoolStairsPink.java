package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsPink extends BlockWoolStairs {

    public BlockWoolStairsPink() {
        this(0);
    }

    public BlockWoolStairsPink(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PINK_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PINK;
    }
}
