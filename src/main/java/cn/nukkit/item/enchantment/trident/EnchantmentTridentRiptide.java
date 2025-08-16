package cn.nukkit.item.enchantment.trident;

import cn.nukkit.item.enchantment.EnchantmentRarity;

public class EnchantmentTridentRiptide extends EnchantmentTrident {

    public EnchantmentTridentRiptide() {
        super(ID_TRIDENT_RIPTIDE, NAME_TRIDENT_RIPTIDE, "tridentRiptide", EnchantmentRarity.RARE);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 7 * level + 10;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
