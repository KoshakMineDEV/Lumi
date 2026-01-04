package cn.nukkit.item;

public class ItemCopperNautilusArmor extends ItemNautilusArmor {

    public ItemCopperNautilusArmor() {
        super(COPPER_NAUTILUS_ARMOR, "Copper Nautilus Armor");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_COPPER;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_COPPER;
    }

}