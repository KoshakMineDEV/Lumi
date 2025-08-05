package cn.nukkit.block;

import cn.nukkit.utils.BlockColor;

public class BlockJunglePlanks extends BlockPlanks {

    public BlockJunglePlanks() {}

    @Override
    public String getName() {
        return "Jungle Planks";
    }

    @Override
    public int getId() {
        return JUNGLE_PLANKS;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.DIRT_BLOCK_COLOR;
    }
}
