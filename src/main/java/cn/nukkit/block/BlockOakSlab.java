package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;

public class BlockOakSlab extends BlockWoodenSlab {
    public BlockOakSlab() {
        this(0);
    }

    public BlockOakSlab(int meta) {
        super(meta, OAK_DOUBLE_SLAB);
    }

    @Override
    public int getId() {
        return OAK_SLAB;
    }

    @Override
    public String getName() {
        return "Oak Slab";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.WOOD_BLOCK_COLOR;
    }
}
