package cn.nukkit.item.enchantment.bow;

import cn.nukkit.item.enchantment.EnchantmentRarity;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentBowKnockback extends EnchantmentBow {

    public EnchantmentBowKnockback() {
        super(ID_BOW_KNOCKBACK, NAME_BOW_KNOCKBACK, "arrowKnockback", EnchantmentRarity.RARE);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 12 + (level - 1) * 20;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return this.getMinEnchantAbility(level) + 25;
    }
}
