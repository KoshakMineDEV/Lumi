package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockPoplarDoubleSlab extends BlockWoodenDoubleSlab {

    public BlockPoplarDoubleSlab() {
        this(0);
    }

    public BlockPoplarDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Double Poplar Slab";
    }

    @Override
    public int getId() {
        return POPLAR_DOUBLE_SLAB;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.POPLAR_SLAB, 0, 2)
        };
    }
}
