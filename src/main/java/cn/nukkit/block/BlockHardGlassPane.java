package cn.nukkit.block;

import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.item.Item;
import cn.nukkit.block.data.BlockColor;

/**
 * Created by PetteriM1
 */
public class BlockHardGlassPane extends BlockThin {

    private static final BlockProperties PROPERTIES = new BlockProperties();

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public boolean updateConnections() {
        return false;
    }

    @Override
    public String getName() {
        return "Hardened Glass Pane";
    }

    @Override
    public int getId() {
        return HARD_GLASS_PANE;
    }

    @Override
    public double getResistance() {
        return 1.5;
    }

    @Override
    public double getHardness() {
        return 0.3;
    }

    @Override
    public Item[] getDrops(Item item) {
        return Item.EMPTY_ARRAY;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.AIR_BLOCK_COLOR;
    }

    @Override
    public boolean canSilkTouch() {
        return true;
    }
}
