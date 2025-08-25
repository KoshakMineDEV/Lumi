package cn.nukkit.item.enchantment.bow;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public abstract class EnchantmentBow extends Enchantment {

    protected EnchantmentBow(int id, String identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.BOW);
    }

    protected EnchantmentBow(int id, Identifier identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.BOW);
    }
}
