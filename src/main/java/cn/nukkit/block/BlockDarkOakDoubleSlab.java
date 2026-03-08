package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockDarkOakDoubleSlab extends BlockWoodenDoubleSlab {
    public BlockDarkOakDoubleSlab() {
        this(0);
    }

    public BlockDarkOakDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Double Dark Oak Slab";
    }

    @Override
    public int getId() {
        return DARK_OAK_DOUBLE_SLAB;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BROWN_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.DARK_OAK_SLAB, 0, 2)
        };
    }
}
