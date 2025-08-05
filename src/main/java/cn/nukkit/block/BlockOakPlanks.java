package cn.nukkit.block;

import cn.nukkit.item.ItemTool;
import cn.nukkit.utils.BlockColor;

public class BlockOakPlanks extends BlockPlanks {

    public BlockOakPlanks() {}

    @Override
    public String getName() {
        return "Oak Planks";
    }

    @Override
    public int getId() {
        return OAK_PLANKS;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.WOOD_BLOCK_COLOR;
    }
}
