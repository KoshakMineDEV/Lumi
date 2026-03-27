package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemTool;

public class BlockDarkPrismarineDoubleSlab extends BlockSolidMeta {

    public BlockDarkPrismarineDoubleSlab() {
        this(0);
    }

    public BlockDarkPrismarineDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return DARK_PRISMARINE_DOUBLE_SLAB;
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
        return "Double Dark Prismarine Slab";
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isPickaxe()) {
            return new Item[]{
                    Item.get(ItemNamespaceId.DARK_PRISMARINE_SLAB, 0, 2)
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
        return BlockColor.DIAMOND_BLOCK_COLOR;
    }
}
