package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;

public class BlockMossyStoneBrickSlab extends BlockSlab {

    public BlockMossyStoneBrickSlab() {
        this(0);
    }

    public BlockMossyStoneBrickSlab(int meta) {
        super(meta, MOSSY_STONE_BRICK_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return MOSSY_STONE_BRICK_SLAB;
    }

    @Override
    public String getName() {
        return "Mossy Stone Brick Slab";
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
