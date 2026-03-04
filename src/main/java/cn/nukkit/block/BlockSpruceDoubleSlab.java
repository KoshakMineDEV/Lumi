package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockSpruceDoubleSlab extends BlockWoodenDoubleSlab {
    public BlockSpruceDoubleSlab() {
        this(0);
    }

    public BlockSpruceDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Double Spruce Slab";
    }

    @Override
    public int getId() {
        return SPRUCE_DOUBLE_SLAB;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.SPRUCE_SLAB, 0, 2)
        };
    }
}
