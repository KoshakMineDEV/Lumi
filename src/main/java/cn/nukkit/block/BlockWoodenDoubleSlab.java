package cn.nukkit.block;

import cn.nukkit.item.ItemTool;

public abstract class BlockWoodenDoubleSlab extends BlockSolidMeta {

    public BlockWoodenDoubleSlab() {
        this(0);
    }

    public BlockWoodenDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public double getHardness() {
        return 2;
    }

    @Override
    public double getResistance() {
        return 15;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }
}
