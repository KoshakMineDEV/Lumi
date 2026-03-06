package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockOakDoubleSlab extends BlockWoodenDoubleSlab {
    public BlockOakDoubleSlab() {
        this(0);
    }

    public BlockOakDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Double Oak Slab";
    }

    @Override
    public int getId() {
        return OAK_DOUBLE_SLAB;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.WOOD_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.OAK_SLAB, 0, 2)
        };
    }
}
