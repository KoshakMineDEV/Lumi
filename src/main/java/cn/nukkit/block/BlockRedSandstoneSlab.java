package cn.nukkit.block;

import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockRedSandstoneSlab extends BlockSlab {
    public BlockRedSandstoneSlab() {
        this(0);
    }

    public BlockRedSandstoneSlab(int meta) {
        super(meta, RED_SANDSTONE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return RED_SANDSTONE_SLAB;
    }

    @Override
    public String getName() {
        return "Red Sandstone Slab";
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
        return BlockColor.ORANGE_BLOCK_COLOR;
    }
}
