package cn.nukkit.item.enchantment.crossbow;

import cn.nukkit.item.enchantment.EnchantmentRarity;

public class EnchantmentCrossbowQuickCharge extends EnchantmentCrossbow {

    public EnchantmentCrossbowQuickCharge() {
        super(ID_CROSSBOW_QUICK_CHARGE, NAME_CROSSBOW_QUICK_CHARGE, "crossbowQuickCharge", EnchantmentRarity.UNCOMMON);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 12 + 20 * (level - 1);
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return 50 + this.getMinEnchantAbility(level);
    }
}
