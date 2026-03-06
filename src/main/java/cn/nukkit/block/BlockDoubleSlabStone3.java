package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockDoubleSlabStone3 extends BlockSlab {
    public BlockDoubleSlabStone3() {
        this(0);
    }

    public BlockDoubleSlabStone3(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return SMOOTH_RED_SANDSTONE_DOUBLE_SLAB;
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
        String[] names = new String[]{
                "End Stone Brick",
                "Smooth Red Sandstone",
                "Polished Andesite",
                "Andesite",
                "Diorite",
                "Polished Diorite",
                "Granite",
                "Polisehd Granite"
        };
        return "Double " + names[this.getDamage() & 0x07] + " Slab";
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isPickaxe() && item.getTier() >= ItemTool.TIER_WOODEN) {
            return new Item[] {
                    Item.get(ItemNamespaceId.SMOOTH_STONE_SLAB, 0, 2)
            };
        } else {
            return Item.EMPTY_ARRAY;
        }
    }

    @Override
    public BlockColor getColor() {
        switch (this.getDamage() & 0x07) {
            case END_STONE_BRICKS:
                return BlockColor.SAND_BLOCK_COLOR;
            case SMOOTH_RED_SANDSTONE:
                return BlockColor.ORANGE_BLOCK_COLOR;
            default:
            case POLISHED_ANDESITE:
            case ANDESITE:
                return BlockColor.STONE_BLOCK_COLOR;
            case DIORITE:
            case POLISHED_DIORITE:
                return BlockColor.QUARTZ_BLOCK_COLOR;
            case GRANITE:
            case POLISHED_GRANITE:
                return BlockColor.DIRT_BLOCK_COLOR;
        }
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }
}
