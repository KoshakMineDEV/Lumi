package cn.nukkit.item.enchantment.bow;

import cn.nukkit.item.enchantment.EnchantmentRarity;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentBowFlame extends EnchantmentBow {

    public EnchantmentBowFlame() {
        super(ID_BOW_FLAME, NAME_BOW_FLAME, "arrowFire", EnchantmentRarity.RARE);
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
