package cn.nukkit.item.enchantment.crossbow;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;

public class EnchantmentCrossbowMultishot extends EnchantmentCrossbow {

    public EnchantmentCrossbowMultishot() {
        super(ID_CROSSBOW_MULTISHOT, NAME_CROSSBOW_MULTISHOT, "crossbowMultishot", EnchantmentRarity.RARE);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 20;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return 50 + this.getMinEnchantAbility(level);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment.getId() != ID_CROSSBOW_PIERCING;
    }
}
