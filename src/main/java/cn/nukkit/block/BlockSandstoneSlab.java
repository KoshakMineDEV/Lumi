package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockSandstoneSlab extends BlockSlab {
    public BlockSandstoneSlab() {
        this(0);
    }

    public BlockSandstoneSlab(int meta) {
        super(meta, SANDSTONE_DOUBLE_SLAB);
    }

    @Override
    public String getName() {
        return "Sandstone Slab";
    }

    @Override
    public int getId() {
        return SANDSTONE_SLAB;
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
        return BlockColor.SAND_BLOCK_COLOR;
    }
}
