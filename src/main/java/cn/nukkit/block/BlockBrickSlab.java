package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockBrickSlab extends BlockSlab {
    public BlockBrickSlab() {
        this(0);
    }

    public BlockBrickSlab(int meta) {
        super(meta, BRICK_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Brick Slab";
    }

    @Override
    public int getId() {
        return BRICK_SLAB;
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
        return BlockColor.RED_BLOCK_COLOR;
    }
}
