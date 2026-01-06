package cn.nukkit.item;

public class ItemIronNautilusArmor extends ItemNautilusArmor {

    public ItemIronNautilusArmor() {
        super(IRON_NAUTILUS_ARMOR, "Iron Nautilus Armor");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_IRON;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_IRON;
    }

}