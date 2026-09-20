package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedYellow extends BlockGlassPaneStained {
    public BlockGlassPaneStainedYellow() {
        this(0);
    }

    public BlockGlassPaneStainedYellow(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return YELLOW_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.YELLOW;
    }
}
