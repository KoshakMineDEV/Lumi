package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedRed extends BlockGlassPaneStained {
    public BlockGlassPaneStainedRed() {
        this(0);
    }

    public BlockGlassPaneStainedRed(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.RED;
    }
}
