package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
public class BlockStairsPoplar extends BlockStairsOak {

    public BlockStairsPoplar() {
        this(0);
    }

    public BlockStairsPoplar(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_STAIRS;
    }

    @Override
    public String getName() {
        return "Poplar Wood Stairs";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
