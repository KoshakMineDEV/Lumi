package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsYellow extends BlockConcreteStairs {

    public BlockConcreteStairsYellow() {
        this(0);
    }

    public BlockConcreteStairsYellow(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return YELLOW_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.YELLOW;
    }
}
