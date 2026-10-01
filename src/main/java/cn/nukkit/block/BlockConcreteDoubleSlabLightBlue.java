package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockConcreteDoubleSlabLightBlue extends BlockConcreteDoubleSlab {

    public BlockConcreteDoubleSlabLightBlue() {
        this(0);
    }

    public BlockConcreteDoubleSlabLightBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIGHT_BLUE_CONCRETE_DOUBLE_SLAB;
    }

    @Override
    protected int getSingleSlabId() {
        return LIGHT_BLUE_CONCRETE_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
