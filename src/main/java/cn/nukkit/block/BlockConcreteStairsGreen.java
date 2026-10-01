package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsGreen extends BlockConcreteStairs {

    public BlockConcreteStairsGreen() {
        this(0);
    }

    public BlockConcreteStairsGreen(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GREEN_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GREEN;
    }
}
