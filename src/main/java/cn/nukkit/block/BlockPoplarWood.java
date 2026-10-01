package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockPoplarWood extends BlockLog {

    public BlockPoplarWood() {
        this(0);
    }

    public BlockPoplarWood(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_WOOD;
    }

    @Override
    public String getName() {
        return "Poplar Wood";
    }

    @Override
    public int getStrippedId() {
        return STRIPPED_POPLAR_WOOD;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
