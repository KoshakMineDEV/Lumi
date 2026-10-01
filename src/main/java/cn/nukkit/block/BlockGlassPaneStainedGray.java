package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedGray extends BlockGlassPaneStained {
    public BlockGlassPaneStainedGray() {
        this(0);
    }

    public BlockGlassPaneStainedGray(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GRAY_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GRAY;
    }
}
