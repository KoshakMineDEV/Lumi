package cn.nukkit.item;

public class ItemNetheriteNautilusArmor extends ItemNautilusArmor {

    public ItemNetheriteNautilusArmor() {
        super(NETHERITE_NAUTILUS_ARMOR, "Netherite Nautilus Armor");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_NETHERITE;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_NETHERITE;
    }

}