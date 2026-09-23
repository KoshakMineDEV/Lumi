package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsLime extends BlockConcreteStairs {

    public BlockConcreteStairsLime() {
        this(0);
    }

    public BlockConcreteStairsLime(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIME_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIME;
    }
}
