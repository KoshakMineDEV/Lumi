package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabMagenta extends BlockWoolSlab {

    public BlockWoolSlabMagenta() {
        this(0);
    }

    public BlockWoolSlabMagenta(int meta) {
        super(meta, MAGENTA_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return MAGENTA_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.MAGENTA;
    }
}
