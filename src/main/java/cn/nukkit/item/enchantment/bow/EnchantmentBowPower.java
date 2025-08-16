package cn.nukkit.item.enchantment.bow;

import cn.nukkit.item.enchantment.EnchantmentRarity;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentBowPower extends EnchantmentBow {

    public EnchantmentBowPower() {
        super(ID_BOW_POWER, NAME_BOW_POWER, "arrowDamage", EnchantmentRarity.COMMON);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return this.getMinEnchantAbility(level) + 15;
    }
}
