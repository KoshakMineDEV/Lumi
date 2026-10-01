package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockStrippedPoplarLog extends BlockLogStripped {

    public BlockStrippedPoplarLog() {
        this(0);
    }

    public BlockStrippedPoplarLog(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return STRIPPED_POPLAR_LOG;
    }

    @Override
    public String getName() {
        return "Stripped Poplar Log";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
