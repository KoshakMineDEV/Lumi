package cn.nukkit.item.enchantment.crossbow;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;

public abstract class EnchantmentCrossbow extends Enchantment {

    protected EnchantmentCrossbow(int id, String identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.CROSSBOW);
    }

    protected EnchantmentCrossbow(int id, Identifier identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.CROSSBOW);
    }
}
