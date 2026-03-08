package cn.nukkit.block;

import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockEndStoneBrickSlab extends BlockSlab {
    public BlockEndStoneBrickSlab() {
        this(0);
    }

    public BlockEndStoneBrickSlab(int meta) {
        super(meta, END_STONE_BRICK_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return END_STONE_BRICK_SLAB;
    }

    @Override
    public String getName() {
        return "End Stone Brick Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SAND_BLOCK_COLOR;
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
