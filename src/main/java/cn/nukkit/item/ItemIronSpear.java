package cn.nukkit.item;

public class ItemIronSpear extends ItemSpear {

    public ItemIronSpear() {
        super(IRON_SPEAR, "Iron Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_IRON;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_IRON;
    }

    @Override
    public int getAttackDamage() {
        return 4;
    }
}