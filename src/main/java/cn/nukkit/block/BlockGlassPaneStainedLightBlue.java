package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedLightBlue extends BlockGlassPaneStained {
    public BlockGlassPaneStainedLightBlue() {
        this(0);
    }

    public BlockGlassPaneStainedLightBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return LIGHT_BLUE_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
