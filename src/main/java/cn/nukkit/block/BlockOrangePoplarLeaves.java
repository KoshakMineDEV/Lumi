package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockOrangePoplarLeaves extends BlockPoplarLeaves {

    public BlockOrangePoplarLeaves() {
        this(0);
    }

    public BlockOrangePoplarLeaves(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ORANGE_POPLAR_LEAVES;
    }

    @Override
    public String getName() {
        return "Orange Poplar Leaves";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }
}
