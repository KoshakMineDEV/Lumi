package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockPolishedGraniteSlab extends BlockSlab {
    public BlockPolishedGraniteSlab() {
        this(0);
    }

    public BlockPolishedGraniteSlab(int meta) {
        super(meta, POLISHED_GRANITE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return POLISHED_GRANITE_SLAB;
    }

    @Override
    public String getName() {
        return "Polished Granite Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.DIRT_BLOCK_COLOR;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }
}
