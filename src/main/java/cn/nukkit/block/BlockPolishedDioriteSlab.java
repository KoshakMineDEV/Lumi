package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockPolishedDioriteSlab extends BlockSlab {
    public BlockPolishedDioriteSlab() {
        this(0);
    }

    public BlockPolishedDioriteSlab(int meta) {
        super(meta, POLISHED_DIORITE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return POLISHED_DIORITE_SLAB;
    }

    @Override
    public String getName() {
        return "Polished Diorite Slab";
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
