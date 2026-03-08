package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockNetherBrickSlab extends BlockSlab {
    public BlockNetherBrickSlab() {
        this(0);
    }

    public BlockNetherBrickSlab(int meta) {
        super(meta, NETHER_BRICK_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Nether Brick Slab";
    }

    @Override
    public int getId() {
        return NETHER_BRICK_SLAB;
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
        return BlockColor.NETHERRACK_BLOCK_COLOR;
    }
}
