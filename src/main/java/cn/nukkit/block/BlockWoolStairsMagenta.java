package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolStairsMagenta extends BlockWoolStairs {

    public BlockWoolStairsMagenta() {
        this(0);
    }

    public BlockWoolStairsMagenta(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return MAGENTA_WOOL_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.MAGENTA;
    }
}
