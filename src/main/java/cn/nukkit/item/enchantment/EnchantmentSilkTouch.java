package cn.nukkit.item.enchantment;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.loot.EnchantmentLootDigging;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentSilkTouch extends Enchantment {

    public EnchantmentSilkTouch() {
        super(ID_SILK_TOUCH, NAME_SILK_TOUCH, "untouching", EnchantmentRarity.VERY_RARE, EnchantmentType.DIGGER);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 15;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return super.getMinEnchantAbility(level) + 50;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && !(enchantment instanceof EnchantmentLootDigging);
    }

    @Override
    public boolean canEnchant(Item item) {
        return item.isShears() || super.canEnchant(item);
    }
}
