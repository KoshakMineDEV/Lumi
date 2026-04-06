package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockStoneBrickSlab extends BlockSlab {
    public BlockStoneBrickSlab() {
        this(0);
    }

    public BlockStoneBrickSlab(int meta) {
        super(meta, STONE_BRICK_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Brick Slab";
    }

    @Override
    public int getId() {
        return STONE_BRICK_SLAB;
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
