package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockSmoothRedSandstoneSlab extends BlockSlab {
    public BlockSmoothRedSandstoneSlab() {
        this(0);
    }

    public BlockSmoothRedSandstoneSlab(int meta) {
        super(meta, SMOOTH_RED_SANDSTONE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return SMOOTH_RED_SANDSTONE_SLAB;
    }

    @Override
    public String getName() {
        return "End Stone Brick Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
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
