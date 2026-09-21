package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.block.data.BlockColor;

public class BlockFenceCherry extends BlockFence {

    public BlockFenceCherry() {
        this(0);
    }

    public BlockFenceCherry(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CHERRY_FENCE;
    }

    @Override
    public String getName() {
        return "Cherry Fence";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.CHERRY_BLOCK_COLOR;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(this.getId(), 0), 0);
    }
}
