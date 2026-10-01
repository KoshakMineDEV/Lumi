package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedPink extends BlockGlassPaneStained {
    public BlockGlassPaneStainedPink() {
        this(0);
    }

    public BlockGlassPaneStainedPink(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PINK_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.PINK;
    }
}
