package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedBrown extends BlockGlassPaneStained {
    public BlockGlassPaneStainedBrown() {
        this(0);
    }

    public BlockGlassPaneStainedBrown(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BROWN_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BROWN;
    }
}
