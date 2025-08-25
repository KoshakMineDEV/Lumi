package cn.nukkit.item.enchantment;

public class EnchantmentBindingCurse extends Enchantment {

    public EnchantmentBindingCurse() {
        super(ID_BINDING_CURSE, NAME_BINDING_CURSE, "curse.binding", EnchantmentRarity.VERY_RARE, EnchantmentType.WEARABLE);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 25;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return 30;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
}
