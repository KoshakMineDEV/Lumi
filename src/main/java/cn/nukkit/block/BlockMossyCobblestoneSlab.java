package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockMossyCobblestoneSlab extends BlockSlab {
    public BlockMossyCobblestoneSlab() {
        this(0);
    }

    public BlockMossyCobblestoneSlab(int meta) {
        super(meta, MOSSY_COBBLESTONE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return MOSSY_COBBLESTONE_SLAB;
    }

    @Override
    public String getName() {
        return "Mossy Cobblestone Slab";
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
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.STONE_BLOCK_COLOR;
    }
}
