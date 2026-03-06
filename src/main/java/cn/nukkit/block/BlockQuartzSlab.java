package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockQuartzSlab extends BlockSlab {
    public BlockQuartzSlab() {
        this(0);
    }

    public BlockQuartzSlab(int meta) {
        super(meta, QUARTZ_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Quartz Slab";
    }

    @Override
    public int getId() {
        return QUARTZ_SLAB;
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
        return BlockColor.QUARTZ_BLOCK_COLOR;
    }
}
