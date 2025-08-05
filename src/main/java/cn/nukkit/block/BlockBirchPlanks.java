package cn.nukkit.block;

import cn.nukkit.utils.BlockColor;

public class BlockBirchPlanks extends BlockPlanks {

    public BlockBirchPlanks() {}

    @Override
    public String getName() {
        return "Birch Planks";
    }

    @Override
    public int getId() {
        return BIRCH_PLANKS;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SAND_BLOCK_COLOR;
    }
}
