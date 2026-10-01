package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedGreen extends BlockGlassPaneStained {
    public BlockGlassPaneStainedGreen() {
        this(0);
    }

    public BlockGlassPaneStainedGreen(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return GREEN_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.GREEN;
    }
}
