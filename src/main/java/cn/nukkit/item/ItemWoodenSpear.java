package cn.nukkit.item;

public class ItemWoodenSpear extends ItemSpear {

    public ItemWoodenSpear() {
        super(WOODEN_SPEAR, "Wooden Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_WOODEN;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public int getAttackDamage() {
        return 2;
    }
}