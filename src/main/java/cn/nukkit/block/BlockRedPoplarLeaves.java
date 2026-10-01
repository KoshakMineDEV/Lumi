package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockRedPoplarLeaves extends BlockPoplarLeaves {

    public BlockRedPoplarLeaves() {
        this(0);
    }

    public BlockRedPoplarLeaves(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_POPLAR_LEAVES;
    }

    @Override
    public String getName() {
        return "Red Poplar Leaves";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.RED_BLOCK_COLOR;
    }
}
