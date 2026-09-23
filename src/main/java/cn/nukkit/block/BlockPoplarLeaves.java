package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;

public abstract class BlockPoplarLeaves extends BlockLeaves {

    protected BlockPoplarLeaves() {
        this(0);
    }

    protected BlockPoplarLeaves(int meta) {
        super(meta);
    }

    @Override
    protected Item getSapling() {
        return new ItemBlock(Block.get(POPLAR_SAPLING));
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(getId()), 0);
    }

    @Override
    protected boolean canDropApple() {
        return false;
    }
}
