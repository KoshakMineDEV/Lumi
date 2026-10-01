package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockWoolSlabWhite extends BlockWoolSlab {

    public BlockWoolSlabWhite() {
        this(0);
    }

    public BlockWoolSlabWhite(int meta) {
        super(meta, WHITE_WOOL_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return WHITE_WOOL_SLAB;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.WHITE;
    }
}
