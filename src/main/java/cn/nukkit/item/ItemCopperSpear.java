package cn.nukkit.item;

public class ItemCopperSpear extends ItemSpear {

    public ItemCopperSpear() {
        super(COPPER_SPEAR, "Copper Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_COPPER;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_COPPER;
    }

    @Override
    public int getAttackDamage() {
        return 3;
    }
}