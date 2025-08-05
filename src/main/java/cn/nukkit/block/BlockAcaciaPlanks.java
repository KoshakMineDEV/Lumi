package cn.nukkit.block;

import cn.nukkit.utils.BlockColor;

public class BlockAcaciaPlanks extends BlockPlanks {

    public BlockAcaciaPlanks() {}

    @Override
    public String getName() {
        return "Acacia Planks";
    }

    @Override
    public int getId() {
        return ACACIA_PLANKS;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }
}
