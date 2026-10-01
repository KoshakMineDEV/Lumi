package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockStrippedPoplarWood extends BlockLogStripped {

    public BlockStrippedPoplarWood() {
        this(0);
    }

    public BlockStrippedPoplarWood(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return STRIPPED_POPLAR_WOOD;
    }

    @Override
    public String getName() {
        return "Stripped Poplar Wood";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
