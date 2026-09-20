package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedLightGray extends BlockGlassPaneStained {
    public BlockGlassPaneStainedLightGray() {
        this(0);
    }

    public BlockGlassPaneStainedLightGray(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIGHT_GRAY_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_GRAY;
    }
}
