package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsBlack extends BlockConcreteStairs {

    public BlockConcreteStairsBlack() {
        this(0);
    }

    public BlockConcreteStairsBlack(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLACK_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLACK;
    }
}
