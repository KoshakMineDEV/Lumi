package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.data.DyeColor;

public abstract class BlockWoolStairs extends BlockStairs {

    protected BlockWoolStairs() {
        this(0);
    }

    protected BlockWoolStairs(int meta) {
        super(meta);
    }

    public abstract DyeColor getDyeColor();

    @Override
    public String getName() {
        return getDyeColor().getName() + " Wool Stairs";
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
    public Item[] getDrops(Item item) {
        return new Item[]{toItem()};
    }
}
