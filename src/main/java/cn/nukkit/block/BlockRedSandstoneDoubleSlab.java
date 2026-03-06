package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockRedSandstoneDoubleSlab extends BlockSolidMeta {

    public BlockRedSandstoneDoubleSlab() {
        this(0);
    }

    public BlockRedSandstoneDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_SANDSTONE_DOUBLE_SLAB;
    }

    @Override
    public double getResistance() {
        return 30;
    }

    @Override
    public double getHardness() {
        return 2;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public String getName() {
        return "Double Red Sandstone Slab";
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isPickaxe()) {
            return new Item[]{
                    Item.get(ItemNamespaceId.RED_SANDSTONE_SLAB, 0, 2)
            };
        } else {
            return Item.EMPTY_ARRAY;
        }
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }
}
