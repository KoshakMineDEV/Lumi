package cn.nukkit.item;


public class ItemDiamondNautilusArmor extends ItemNautilusArmor {

    public ItemDiamondNautilusArmor() {
        super(DIAMOND_NAUTILUS_ARMOR, "Diamond Nautilus Armor");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_DIAMOND;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_DIAMOND;
    }

}