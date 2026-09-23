package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsCyan extends BlockWoolStairs {

    public BlockWoolStairsCyan() {
        this(0);
    }

    public BlockWoolStairsCyan(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CYAN_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.CYAN;
    }
}
