package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockPetrifiedSlab extends BlockSlab {
    public BlockPetrifiedSlab() {
        this(0);
    }

    public BlockPetrifiedSlab(int meta) {
        super(meta, PETRIFIED_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Petrified Slab";
    }

    @Override
    public int getId() {
        return PETRIFIED_SLAB;
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
        return BlockColor.WOOD_BLOCK_COLOR;
    }
}
