package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockBirchDoubleSlab extends BlockWoodenDoubleSlab {
    public BlockBirchDoubleSlab() {
        this(0);
    }

    public BlockBirchDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Double Birch Slab";
    }

    @Override
    public int getId() {
        return BIRCH_DOUBLE_SLAB;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SAND_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.BIRCH_SLAB, 0, 2)
        };
    }
}
