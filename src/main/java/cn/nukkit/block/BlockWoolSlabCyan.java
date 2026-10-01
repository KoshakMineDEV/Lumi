package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabCyan extends BlockWoolSlab {

    public BlockWoolSlabCyan() {
        this(0);
    }

    public BlockWoolSlabCyan(int meta) {
        super(meta, CYAN_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return CYAN_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.CYAN;
    }
}
