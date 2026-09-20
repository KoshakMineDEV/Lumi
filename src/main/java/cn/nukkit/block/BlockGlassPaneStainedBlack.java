package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedBlack extends BlockGlassPaneStained {
    public BlockGlassPaneStainedBlack() {
        this(0);
    }

    public BlockGlassPaneStainedBlack(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BLACK_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.BLACK;
    }
}
