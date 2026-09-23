package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.data.DyeColor;

public abstract class BlockWoolDoubleSlab extends BlockSolidMeta {

    protected BlockWoolDoubleSlab() {
        this(0);
    }

    protected BlockWoolDoubleSlab(int meta) {
        super(meta);
    }

    public abstract DyeColor getDyeColor();

    protected abstract int getSingleSlabId();

    @Override
    public String getName() {
        return getDyeColor().getName() + " Wool Double Slab";
    }

    @Override
    public double getHardness() {
        return 0.8;
    }

    @Override
    public double getResistance() {
        return 0.8;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_SHEARS;
    }

    @Override
    public int getBurnChance() {
        return 30;
    }

    @Override
    public int getBurnAbility() {
        return 60;
    }

    @Override
    public boolean canHarvestWithHand() {
        return true;
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
        return new Item[]{new ItemBlock(Block.get(getSingleSlabId()), 0, 2)};
    }
}
