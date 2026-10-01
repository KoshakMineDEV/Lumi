package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsCyan extends BlockConcreteStairs {

    public BlockConcreteStairsCyan() {
        this(0);
    }

    public BlockConcreteStairsCyan(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CYAN_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.CYAN;
    }
}
