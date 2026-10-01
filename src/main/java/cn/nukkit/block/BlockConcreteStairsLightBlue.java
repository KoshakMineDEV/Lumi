package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsLightBlue extends BlockConcreteStairs {

    public BlockConcreteStairsLightBlue() {
        this(0);
    }

    public BlockConcreteStairsLightBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIGHT_BLUE_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
