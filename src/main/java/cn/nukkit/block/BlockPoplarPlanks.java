package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockPoplarPlanks extends BlockPlanks {

    @Override
    public String getName() {
        return "Poplar Planks";
    }

    @Override
    public int getId() {
        return POPLAR_PLANKS;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
