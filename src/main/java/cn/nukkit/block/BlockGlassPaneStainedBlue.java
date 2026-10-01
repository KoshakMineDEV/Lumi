package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedBlue extends BlockGlassPaneStained {
    public BlockGlassPaneStainedBlue() {
        this(0);
    }

    public BlockGlassPaneStainedBlue(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLUE_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLUE;
    }
}
