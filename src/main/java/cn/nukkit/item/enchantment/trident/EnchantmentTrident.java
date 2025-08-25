package cn.nukkit.item.enchantment.trident;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;

public abstract class EnchantmentTrident extends Enchantment {

    protected EnchantmentTrident(int id, String identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.TRIDENT);
    }

    protected EnchantmentTrident(int id, Identifier identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.TRIDENT);
    }
}
