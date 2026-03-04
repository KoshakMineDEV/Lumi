package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockJungleDoubleSlab extends BlockWoodenDoubleSlab {
    public BlockJungleDoubleSlab() {
        this(0);
    }

    public BlockJungleDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Double Jungle Slab";
    }

    @Override
    public int getId() {
        return JUNGLE_DOUBLE_SLAB;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.DIRT_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.JUNGLE_SLAB, 0, 2)
        };
    }
}
