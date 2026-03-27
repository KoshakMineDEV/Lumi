package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockSmoothQuartzSlab extends BlockSlab {

    public BlockSmoothQuartzSlab() {
        this(0);
    }

    public BlockSmoothQuartzSlab(int meta) {
        super(meta, SMOOTH_QUARTZ_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return SMOOTH_QUARTZ_SLAB;
    }

    @Override
    public String getName() {
        return "Smooth Quartz Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.QUARTZ_BLOCK_COLOR;
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
}
