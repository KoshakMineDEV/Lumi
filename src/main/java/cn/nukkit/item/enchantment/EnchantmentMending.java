package cn.nukkit.item.enchantment;

import cn.nukkit.item.enchantment.bow.EnchantmentBowInfinity;

/**
 * @author Rover656
 */
public class EnchantmentMending extends Enchantment {

    public EnchantmentMending() {
        super(ID_MENDING, NAME_MENDING, "mending", EnchantmentRarity.RARE, EnchantmentType.BREAKABLE);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 25 * level;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return this.getMinEnchantAbility(level) + 50;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && !(enchantment instanceof EnchantmentBowInfinity);
    }
}