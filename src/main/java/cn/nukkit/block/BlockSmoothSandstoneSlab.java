package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockSmoothSandstoneSlab extends BlockSlab {
    public BlockSmoothSandstoneSlab() {
        this(0);
    }

    public BlockSmoothSandstoneSlab(int meta) {
        super(meta, SMOOTH_SANDSTONE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return SMOOTH_SANDSTONE_SLAB;
    }

    @Override
    public String getName() {
        return "Smooth Sandstone Slab";
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
        return BlockColor.SAND_BLOCK_COLOR;
    }
}
