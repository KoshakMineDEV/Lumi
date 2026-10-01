package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockStairsBamboo extends BlockStairsOak {

    public BlockStairsBamboo() {
        this(0);
    }

    public BlockStairsBamboo(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BAMBOO_STAIRS;
    }

    @Override
    public String getName() {
        return "Bamboo Stairs";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BAMBOO_BLOCK_COLOR;
    }
}
