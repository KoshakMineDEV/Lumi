package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemTool;

public class BlockPetrifiedDoubleSlab extends BlockSolidMeta {
    public BlockPetrifiedDoubleSlab() {
        this(0);
    }

    public BlockPetrifiedDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PETRIFIED_DOUBLE_SLAB;
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
        return "Double Petrified Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.WOOD_BLOCK_COLOR;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.PETRIFIED_OAK_SLAB, 0, 2)
        };
    }
}
