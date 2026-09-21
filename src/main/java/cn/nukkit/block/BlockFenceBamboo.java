package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.block.data.BlockColor;

public class BlockFenceBamboo extends BlockFence {

    public BlockFenceBamboo() {
        this(0);
    }

    public BlockFenceBamboo(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BAMBOO_FENCE;
    }

    @Override
    public String getName() {
        return "Bamboo Fence";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BAMBOO_BLOCK_COLOR;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(this.getId(), 0), 0);
    }
}
