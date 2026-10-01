package cn.nukkit.block;

import cn.nukkit.item.data.DyeColor;

public class BlockGlassPaneStainedCyan extends BlockGlassPaneStained {
    public BlockGlassPaneStainedCyan() {
        this(0);
    }

    public BlockGlassPaneStainedCyan(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CYAN_STAINED_GLASS_PANE;
    }

    @Override
    public DyeColor getDyeColor() {
        return DyeColor.CYAN;
    }
}
