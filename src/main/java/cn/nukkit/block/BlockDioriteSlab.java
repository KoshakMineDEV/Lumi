package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockDioriteSlab extends BlockSlab {
    public BlockDioriteSlab() {
        this(0);
    }

    public BlockDioriteSlab(int meta) {
        super(meta, DIORITE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return DIORITE_SLAB;
    }

    @Override
    public String getName() {
        return "Diorite Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.QUARTZ_BLOCK_COLOR;
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
