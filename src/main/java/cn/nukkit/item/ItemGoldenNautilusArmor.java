package cn.nukkit.item;

public class ItemGoldenNautilusArmor extends ItemNautilusArmor {

    public ItemGoldenNautilusArmor() {
        super(GOLDEN_NAUTILUS_ARMOR, "Golden Nautilus Armor");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_GOLD;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_GOLD;
    }

}