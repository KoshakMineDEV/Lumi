package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsGray extends BlockConcreteStairs {

    public BlockConcreteStairsGray() {
        this(0);
    }

    public BlockConcreteStairsGray(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GRAY_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GRAY;
    }
}
