package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsWhite extends BlockConcreteStairs {

    public BlockConcreteStairsWhite() {
        this(0);
    }

    public BlockConcreteStairsWhite(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return WHITE_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.WHITE;
    }
}
