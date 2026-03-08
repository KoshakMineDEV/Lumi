package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockEndStoneBrickDoubleSlab extends BlockSolidMeta {
    public BlockEndStoneBrickDoubleSlab() {
        this(0);
    }

    public BlockEndStoneBrickDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return END_STONE_BRICK_DOUBLE_SLAB;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_WOODEN;
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
    public String getName() {
        return "Double End Stone Brick Slab";
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isPickaxe() && item.getTier() >= ItemTool.TIER_WOODEN) {
            return new Item[] {
                    Item.get(ItemNamespaceId.END_STONE_BRICK_SLAB, 0, 2)
            };
        } else {
            return Item.EMPTY_ARRAY;
        }
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SAND_BLOCK_COLOR;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }
}
