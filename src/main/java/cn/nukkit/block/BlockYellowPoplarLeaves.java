package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockYellowPoplarLeaves extends BlockPoplarLeaves {

    public BlockYellowPoplarLeaves() {
        this(0);
    }

    public BlockYellowPoplarLeaves(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return YELLOW_POPLAR_LEAVES;
    }

    @Override
    public String getName() {
        return "Yellow Poplar Leaves";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.YELLOW_BLOCK_COLOR;
    }
}
