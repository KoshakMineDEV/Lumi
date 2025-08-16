package cn.nukkit.item.enchantment.damage;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;
import lombok.Getter;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
@Getter
public abstract class EnchantmentDamage extends Enchantment {

    protected Type damageType;

    protected EnchantmentDamage(int id, String identifier, String name, EnchantmentRarity rarity, Type type) {
        super(id, identifier, name, rarity, EnchantmentType.SWORD);
        this.damageType = type;
    }

    protected EnchantmentDamage(int id, Identifier identifier, String name, EnchantmentRarity rarity, Type type) {
        super(id, identifier, name, rarity, EnchantmentType.SWORD);
        this.damageType = type;
    }

    @Override
    public String getName() {
        return "%enchantment.damage." + this.name;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean isMajor() {
        return true;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return !(enchantment instanceof EnchantmentDamage);
    }

    @Override
    public boolean canEnchant(Item item) {
        return item.isAxe() || super.canEnchant(item);
    }

    public enum Type {
        ALL,
        SMITE,
        ARTHROPODS
    }
}
