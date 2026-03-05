package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockDarkPrismarineSlab extends BlockSlab {
    public BlockDarkPrismarineSlab() {
        this(0);
    }

    public BlockDarkPrismarineSlab(int meta) {
        super(meta, DARK_PRISMARINE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return DARK_PRISMARINE_SLAB;
    }

    @Override
    public String getName() {
        return "Dark Prismarine Slab";
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
        return BlockColor.DIAMOND_BLOCK_COLOR;
    }
}
