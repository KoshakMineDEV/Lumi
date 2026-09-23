package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.data.DyeColor;

public abstract class BlockConcreteDoubleSlab extends BlockSolidMeta {

    protected BlockConcreteDoubleSlab() {
        this(0);
    }

    protected BlockConcreteDoubleSlab(int meta) {
        super(meta);
    }

    public abstract DyeColor getDyeColor();

    protected abstract int getSingleSlabId();

    @Override
    public String getName() {
        return getDyeColor().getName() + " Concrete Double Slab";
    }

    @Override
    public double getHardness() {
        return 1.8;
    }

    @Override
    public double getResistance() {
        return 9;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public BlockColor getColor() {
        return getDyeColor().getBlockColor();
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(getSingleSlabId()), 0);
    }

    @Override
    public Item[] getDrops(Item item) {
        return item.getTier() >= ItemTool.TIER_WOODEN
                ? new Item[]{new ItemBlock(Block.get(getSingleSlabId()), 0, 2)}
                : Item.EMPTY_ARRAY;
    }
}
