package cn.nukkit.item.enchantment.spear;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;

/**
 * @author xRookieFight
 * @since 09/01/2026
 */
public abstract class EnchantmentSpear extends Enchantment {

    protected EnchantmentSpear(int id, String identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.SPEAR);
    }

    protected EnchantmentSpear(int id, Identifier identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.SPEAR);
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return 50;
    }
}