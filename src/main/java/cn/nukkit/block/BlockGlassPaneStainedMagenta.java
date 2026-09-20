package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedMagenta extends BlockGlassPaneStained {
    public BlockGlassPaneStainedMagenta() {
        this(0);
    }

    public BlockGlassPaneStainedMagenta(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return MAGENTA_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.MAGENTA;
    }
}
