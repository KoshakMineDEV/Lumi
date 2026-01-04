package cn.nukkit.item;

public class ItemDiamondSpear extends ItemSpear {

    public ItemDiamondSpear() {
        super(DIAMOND_SPEAR, "Diamond Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_DIAMOND;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_DIAMOND;
    }

    @Override
    public int getAttackDamage() {
        return 5;
    }
}