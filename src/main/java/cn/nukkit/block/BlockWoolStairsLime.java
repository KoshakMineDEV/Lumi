package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsLime extends BlockWoolStairs {

    public BlockWoolStairsLime() {
        this(0);
    }

    public BlockWoolStairsLime(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIME_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIME;
    }
}
