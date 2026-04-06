package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockRedNetherBrickSlab extends BlockSlab {
    public BlockRedNetherBrickSlab() {
        this(0);
    }

    public BlockRedNetherBrickSlab(int meta) {
        super(meta, RED_NETHER_BRICK_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return RED_NETHER_BRICK_SLAB;
    }

    @Override
    public String getName() {
        return "Red Nether Brick Slab";
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
        return BlockColor.NETHER_BLOCK_COLOR;
    }
}
