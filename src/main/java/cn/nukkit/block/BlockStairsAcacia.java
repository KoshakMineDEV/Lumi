package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class BlockStairsAcacia extends BlockStairsOak {

    public BlockStairsAcacia() {
        this(0);
    }

    public BlockStairsAcacia(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ACACIA_WOOD_STAIRS;
    }

    @Override
    public String getName() {
        return "Acacia Wood Stairs";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }
}
