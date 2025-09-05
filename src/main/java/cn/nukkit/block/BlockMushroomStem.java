package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;

public class BlockMushroomStem extends BlockSolidMeta {

    public BlockMushroomStem() {
        this(0);
    }

    public BlockMushroomStem(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Mushroom Stem";
    }

    @Override
    public int getId() {
        return MUSHROOM_STEM;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    public double getHardness() {
        return 0.2;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item != null && item.hasEnchantment(Enchantment.ID_SILK_TOUCH)) {
            return new Item[]{new ItemBlock(Block.get(MUSHROOM_STEM), 0, 1)};
        }

        return Item.EMPTY_ARRAY;
    }

    @Override
    public boolean canSilkTouch() {
        return true;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BROWN_BLOCK_COLOR;
    }
}
