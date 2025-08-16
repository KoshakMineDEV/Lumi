package cn.nukkit.item.enchantment.mace;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMace;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;

public abstract class EnchantmentMace extends Enchantment {

    protected EnchantmentMace(int id, String identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.MACE);
    }

    protected EnchantmentMace(int id, Identifier identifier, String name, EnchantmentRarity rarity) {
        super(id, identifier, name, rarity, EnchantmentType.MACE);
    }

    @Override
    public boolean canEnchant(Item item) {
        return item instanceof ItemMace;
    }
}