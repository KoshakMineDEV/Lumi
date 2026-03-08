package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockJungleSlab extends BlockWoodenSlab {
    public BlockJungleSlab() {
        this(0);
    }

    public BlockJungleSlab(int meta) {
        super(meta, JUNGLE_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return JUNGLE_SLAB;
    }

    @Override
    public String getName() {
        return "Jungle Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.DIRT_BLOCK_COLOR;
    }
}
