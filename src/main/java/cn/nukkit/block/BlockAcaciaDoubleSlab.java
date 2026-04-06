package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockAcaciaDoubleSlab extends BlockWoodenDoubleSlab {
    public BlockAcaciaDoubleSlab() {
        this(0);
    }

    public BlockAcaciaDoubleSlab(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Double Acacia Slab";
    }

    @Override
    public int getId() {
        return ACACIA_DOUBLE_SLAB;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                Item.get(ItemNamespaceId.ACACIA_SLAB, 0, 2)
        };
    }
}
