package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedLime extends BlockGlassPaneStained {
    public BlockGlassPaneStainedLime() {
        this(0);
    }

    public BlockGlassPaneStainedLime(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIME_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIME;
    }
}
