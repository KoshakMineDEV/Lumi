package cn.nukkit.item.enchantment.loot;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public abstract class EnchantmentLoot extends Enchantment {

    protected EnchantmentLoot(int id, String identifier, String name, EnchantmentRarity rarity, EnchantmentType type) {
        super(id, identifier, name, rarity, type);
    }

    protected EnchantmentLoot(int id, Identifier identifier, String name, EnchantmentRarity rarity, EnchantmentType type) {
        super(id, identifier, name, rarity, type);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return this.getMinEnchantAbility(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment.getId() != Enchantment.ID_SILK_TOUCH;
    }
}
