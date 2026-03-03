package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockSpruceSlab extends BlockWoodenSlab {
    public BlockSpruceSlab() {
        this(0);
    }

    public BlockSpruceSlab(int meta) {
        super(meta, SPRUCE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return SPRUCE_SLAB;
    }

    @Override
    public String getName() {
        return "Spruce Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
