package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.data.DyeColor;

public abstract class BlockConcreteStairs extends BlockStairs {

    protected BlockConcreteStairs() {
        this(0);
    }

    protected BlockConcreteStairs(int meta) {
        super(meta);
    }

    public abstract DyeColor getDyeColor();

    @Override
    public String getName() {
        return getDyeColor().getName() + " Concrete Stairs";
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
    public Item[] getDrops(Item item) {
        return item.getTier() >= ItemTool.TIER_WOODEN ? new Item[]{toItem()} : Item.EMPTY_ARRAY;
    }
}
