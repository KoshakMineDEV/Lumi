package cn.nukkit.item;

public class ItemStoneSpear extends ItemSpear {

    public ItemStoneSpear() {
        super(STONE_SPEAR, "Stone Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_STONE;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_STONE;
    }

    @Override
    public int getAttackDamage() {
        return 3;
    }
}