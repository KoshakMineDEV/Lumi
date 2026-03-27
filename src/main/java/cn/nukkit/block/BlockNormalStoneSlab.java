package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.ItemTool;

public class BlockNormalStoneSlab extends BlockSlab {

    public BlockNormalStoneSlab() {
        this(0);
    }

    public BlockNormalStoneSlab(int meta) {
        super(meta, NORMAL_STONE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return NORMAL_STONE_SLAB;
    }

    @Override
    public String getName() {
        return "Normal Stone Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.STONE_BLOCK_COLOR;
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
