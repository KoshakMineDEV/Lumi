package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockGraniteSlab extends BlockSlab {
    public BlockGraniteSlab() {
        this(0);
    }

    public BlockGraniteSlab(int meta) {
        super(meta, GRANITE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return GRANITE_SLAB;
    }

    @Override
    public String getName() {
        return "Granite Slab";
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
