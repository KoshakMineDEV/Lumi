package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockPurpurSlab extends BlockSlab {
    public BlockPurpurSlab() {
        this(0);
    }

    public BlockPurpurSlab(int meta) {
        super(meta, PURPUR_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return PURPUR_SLAB;
    }

    @Override
    public String getName() {
        return "Purpur Slab";
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
        return BlockColor.MAGENTA_BLOCK_COLOR;
    }
}
