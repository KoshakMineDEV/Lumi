package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockPolishedAndesiteSlab extends BlockSlab {
    public BlockPolishedAndesiteSlab() {
        this(0);
    }

    public BlockPolishedAndesiteSlab(int meta) {
        super(meta, POLISHED_ANDESITE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return POLISHED_ANDESITE_SLAB;
    }

    @Override
    public String getName() {
        return "Polished Andesite Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.STONE_BLOCK_COLOR;
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
