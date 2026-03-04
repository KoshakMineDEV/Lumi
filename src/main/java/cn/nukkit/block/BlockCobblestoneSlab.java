package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockCobblestoneSlab extends BlockSlab {
    public BlockCobblestoneSlab() {
        this(0);
    }

    public BlockCobblestoneSlab(int meta) {
        super(meta, COBBLESTONE_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Cobblestone Slab";
    }

    @Override
    public int getId() {
        return COBBLESTONE_SLAB;
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
