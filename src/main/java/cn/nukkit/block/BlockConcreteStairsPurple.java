package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsPurple extends BlockConcreteStairs {

    public BlockConcreteStairsPurple() {
        this(0);
    }

    public BlockConcreteStairsPurple(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PURPLE_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PURPLE;
    }
}
