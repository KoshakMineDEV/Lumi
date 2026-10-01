package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.data.DyeColor;

/**
 * Created by CreeperFace on 7.8.2017.
 */
public abstract class BlockGlassPaneStained extends BlockGlassPane {

    public BlockGlassPaneStained() {
        this(0);
    }

    public BlockGlassPaneStained(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return getDyeColor().getName() + " Stained Glass Pane";
    }

    @Override
    public BlockColor getColor() {
        return getDyeColor().getBlockColor();
    }

    public abstract DyeColor getDyeColor();
}
