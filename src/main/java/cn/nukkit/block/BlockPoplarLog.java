package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockPoplarLog extends BlockLog {

    public BlockPoplarLog() {
        this(0);
    }

    public BlockPoplarLog(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_LOG;
    }

    @Override
    public String getName() {
        return "Poplar Log";
    }

    @Override
    public int getStrippedId() {
        return STRIPPED_POPLAR_LOG;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
