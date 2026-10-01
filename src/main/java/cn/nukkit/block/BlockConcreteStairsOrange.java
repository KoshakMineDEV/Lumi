package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsOrange extends BlockConcreteStairs {

    public BlockConcreteStairsOrange() {
        this(0);
    }

    public BlockConcreteStairsOrange(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ORANGE_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.ORANGE;
    }
}
