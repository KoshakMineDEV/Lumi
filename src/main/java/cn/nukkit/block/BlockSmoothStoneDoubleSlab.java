package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockSmoothStoneDoubleSlab extends BlockSolidMeta {
    public BlockSmoothStoneDoubleSlab() {
        this(0);
    }

    public BlockSmoothStoneDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return SMOOTH_STONE_DOUBLE_SLAB;
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
        return "Double Smooth Stone Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.STONE_BLOCK_COLOR;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.SMOOTH_STONE_SLAB, 0, 2)
        };
    }
}
