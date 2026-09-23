package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteStairsMagenta extends BlockConcreteStairs {

    public BlockConcreteStairsMagenta() {
        this(0);
    }

    public BlockConcreteStairsMagenta(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return MAGENTA_CONCRETE_STAIRS;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.MAGENTA;
    }
}
