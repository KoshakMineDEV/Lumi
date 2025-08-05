package cn.nukkit.block;

import cn.nukkit.utils.BlockColor;

public class BlockDarkOakPlanks extends BlockPlanks {

    public BlockDarkOakPlanks() {}

    @Override
    public String getName() {
        return "Dark Oak Planks";
    }

    @Override
    public int getId() {
        return DARK_OAK_PLANKS;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BROWN_BLOCK_COLOR;
    }
}
