package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.data.DyeColor;

/**
 * Created by PetteriM1
 */
public class BlockHardGlassPaneStained extends BlockHardGlassPane {

    public BlockHardGlassPaneStained() {
        this(0);
    }

    public BlockHardGlassPaneStained(int meta) {
        setDamage(meta);
    }

    @Override
    public int getId() {
        return HARD_STAINED_GLASS_PANE;
    }

    @Override
    public String getName() {
        return getDyeColor().getName() + " Hardened Stained Glass Pane";
    }

    @Override
    public BlockColor getColor() {
        return getDyeColor().getBlockColor();
    }

    public DyeColor getDyeColor() {
        return DyeColor.getByWoolData(getDamage());
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, getDamage());
    }
}
