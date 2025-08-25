package cn.nukkit.item.enchantment.bow;

import cn.nukkit.item.enchantment.EnchantmentRarity;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentBowInfinity extends EnchantmentBow {

    public EnchantmentBowInfinity() {
        super(ID_BOW_INFINITY, NAME_BOW_INFINITY, "arrowInfinite", EnchantmentRarity.VERY_RARE);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 20;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return 50;
    }
}
