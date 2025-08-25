package cn.nukkit.item.enchantment.mace;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.damage.EnchantmentDamage;

public class EnchantmentMaceBreach extends EnchantmentMace {

    public EnchantmentMaceBreach() {
        super(ID_BREACH, NAME_BREACH, "heavy_weapon.breach", EnchantmentRarity.RARE);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return level + 8 * (level + 7);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment.getId() != Enchantment.ID_DENSITY && !(enchantment instanceof EnchantmentDamage);
    }
}