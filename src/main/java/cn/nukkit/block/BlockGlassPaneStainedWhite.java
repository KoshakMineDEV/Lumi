package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedWhite extends BlockGlassPaneStained {
    public BlockGlassPaneStainedWhite() {
        this(0);
    }

    public BlockGlassPaneStainedWhite(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return WHITE_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.WHITE;
    }
}
