package cn.nukkit.block;

import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;


public class BlockPrismarine extends BlockSolid {
    public BlockPrismarine() {
        super();
    }

    @Override
    public int getId() {
        return PRISMARINE;
    }

    @Override
    public String getName() {
        return "Prismarine";
    }

    @Override
    public double getHardness() {
        return 1.5;
    }

    @Override
    public double getResistance() {
        return 6;
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
        return BlockColor.CYAN_BLOCK_COLOR;
    }
}
