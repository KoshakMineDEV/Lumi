package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedPurple extends BlockGlassPaneStained {
    public BlockGlassPaneStainedPurple() {
        this(0);
    }

    public BlockGlassPaneStainedPurple(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PURPLE_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PURPLE;
    }
}
