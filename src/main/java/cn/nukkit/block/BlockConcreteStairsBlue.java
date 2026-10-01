package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsBlue extends BlockConcreteStairs {

    public BlockConcreteStairsBlue() {
        this(0);
    }

    public BlockConcreteStairsBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLUE_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLUE;
    }
}
