package cn.nukkit.item.enchantment.spear;

import cn.nukkit.item.enchantment.EnchantmentRarity;

public class EnchantmentLunge extends EnchantmentSpear {
    public EnchantmentLunge() {
        super(ID_LUNGE, NAME_LUNGE, "lunge", EnchantmentRarity.UNCOMMON);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
