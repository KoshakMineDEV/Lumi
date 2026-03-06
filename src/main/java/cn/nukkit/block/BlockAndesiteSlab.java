package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockAndesiteSlab extends BlockSlab {
    public BlockAndesiteSlab() {
        this(0);
    }

    public BlockAndesiteSlab(int meta) {
        super(meta, ANDESITE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return ANDESITE_SLAB;
    }

    @Override
    public String getName() {
        return "Andesite Slab";
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
