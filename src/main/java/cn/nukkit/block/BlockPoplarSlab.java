package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockPoplarSlab extends BlockWoodenSlab {

    public BlockPoplarSlab() {
        this(0);
    }

    public BlockPoplarSlab(int meta) {
        super(meta, POPLAR_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return POPLAR_SLAB;
    }

    @Override
    public String getName() {
        return "Poplar Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
