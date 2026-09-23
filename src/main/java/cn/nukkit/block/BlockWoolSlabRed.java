package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabRed extends BlockWoolSlab {

    public BlockWoolSlabRed() {
        this(0);
    }

    public BlockWoolSlabRed(int meta) {
        super(meta, RED_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return RED_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.RED;
    }
}
