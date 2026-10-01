package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockButtonPoplar extends BlockButtonWooden {

    public BlockButtonPoplar() {
        this(0);
    }

    public BlockButtonPoplar(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Poplar Button";
    }

    @Override
    public int getId() {
        return POPLAR_BUTTON;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
