package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsBrown extends BlockConcreteStairs {

    public BlockConcreteStairsBrown() {
        this(0);
    }

    public BlockConcreteStairsBrown(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BROWN_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BROWN;
    }
}
