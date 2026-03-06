package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockPrismarineBrickSlab extends BlockSlab {
    public BlockPrismarineBrickSlab() {
        this(0);
    }

    public BlockPrismarineBrickSlab(int meta) {
        super(meta, PRISMARINE_BRICK_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return PRISMARINE_BRICK_SLAB;
    }

    @Override
    public String getName() {
        return "Prismarine Brick Slab";
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
        return BlockColor.CYAN_BLOCK_COLOR;
    }
}
