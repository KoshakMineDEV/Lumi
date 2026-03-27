package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockCutRedSandstoneSlab extends BlockSlab {

    public BlockCutRedSandstoneSlab() {
        this(0);
    }

    public BlockCutRedSandstoneSlab(int meta) {
        super(meta, CUT_RED_SANDSTONE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return CUT_RED_SANDSTONE_SLAB;
    }

    @Override
    public String getName() {
        return "Cut Red Sandstone Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
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
}
