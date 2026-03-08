package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockCutSandstoneSlab extends BlockSlab {

    public BlockCutSandstoneSlab() {
        this(0);
    }

    public BlockCutSandstoneSlab(int meta) {
        super(meta, CUT_SANDSTONE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return CUT_SANDSTONE_SLAB;
    }

    @Override
    public String getName() {
        return "Cut Sandstone Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SAND_BLOCK_COLOR;
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
