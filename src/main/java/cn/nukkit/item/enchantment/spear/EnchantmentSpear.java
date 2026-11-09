package cn.nukkit.item.enchantment.spear;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemSpear;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;

public abstract class EnchantmentSpear extends Enchantment {

    protected EnchantmentSpear(int id, String identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.SPEAR);
    }

    protected EnchantmentSpear(int id, Identifier identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.SPEAR);
    }

    @Override
    public boolean canEnchant(Item item) {
        return item instanceof ItemSpear;
    }
}