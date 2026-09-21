package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;

public class BlockFencePaleOak extends BlockFence {

    public BlockFencePaleOak() {
        this(0);
    }

    public BlockFencePaleOak(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PALE_OAK_FENCE;
    }

    @Override
    public String getName() {
        return "Pale Oak Fence";
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(this.getId(), 0), 0);
    }
}
