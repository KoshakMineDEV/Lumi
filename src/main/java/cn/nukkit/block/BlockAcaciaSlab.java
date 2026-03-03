package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockAcaciaSlab extends BlockWoodenSlab {
    public BlockAcaciaSlab() {
        this(0);
    }

    public BlockAcaciaSlab(int meta) {
        super(meta, ACACIA_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return ACACIA_SLAB;
    }

    @Override
    public String getName() {
        return "Acacia Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }
}
