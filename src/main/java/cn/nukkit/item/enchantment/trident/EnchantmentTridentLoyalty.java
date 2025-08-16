package cn.nukkit.item.enchantment.trident;

import cn.nukkit.item.enchantment.EnchantmentRarity;

public class EnchantmentTridentLoyalty extends EnchantmentTrident {

    public EnchantmentTridentLoyalty() {
        super(ID_TRIDENT_LOYALTY, NAME_TRIDENT_LOYALTY, "tridentLoyalty", EnchantmentRarity.UNCOMMON);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 7 * level + 5;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
