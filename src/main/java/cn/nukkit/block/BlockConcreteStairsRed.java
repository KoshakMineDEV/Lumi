package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsRed extends BlockConcreteStairs {

    public BlockConcreteStairsRed() {
        this(0);
    }

    public BlockConcreteStairsRed(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.RED;
    }
}
