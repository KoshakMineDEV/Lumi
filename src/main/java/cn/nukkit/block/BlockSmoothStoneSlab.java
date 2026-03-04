package cn.nukkit.block;

import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockSmoothStoneSlab extends BlockSlab {
    public BlockSmoothStoneSlab() {
        this(0);
    }

    public BlockSmoothStoneSlab(int meta) {
        super(meta, SMOOTH_STONE_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Smooth Stone Slab";
    }

    @Override
    public int getId() {
        return SMOOTH_STONE_SLAB;
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
