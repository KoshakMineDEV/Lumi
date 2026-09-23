package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsPink extends BlockConcreteStairs {

    public BlockConcreteStairsPink() {
        this(0);
    }

    public BlockConcreteStairsPink(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PINK_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PINK;
    }
}
