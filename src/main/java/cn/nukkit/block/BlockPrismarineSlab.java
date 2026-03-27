package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockPrismarineSlab extends BlockSlab {
    public BlockPrismarineSlab() {
        this(0);
    }

    public BlockPrismarineSlab(int meta) {
        super(meta, PRISMARINE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return PRISMARINE_SLAB;
    }

    @Override
    public String getName() {
        return "Prismarine Slab";
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
