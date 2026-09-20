package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedOrange extends BlockGlassPaneStained {
    public BlockGlassPaneStainedOrange() {
        this(0);
    }

    public BlockGlassPaneStainedOrange(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ORANGE_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.ORANGE;
    }
}
