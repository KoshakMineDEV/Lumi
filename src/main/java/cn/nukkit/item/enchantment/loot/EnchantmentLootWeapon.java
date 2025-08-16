package cn.nukkit.item.enchantment.loot;

import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentLootWeapon extends EnchantmentLoot {

    public EnchantmentLootWeapon() {
        super(ID_LOOTING, NAME_LOOTING, "lootBonus", EnchantmentRarity.RARE, EnchantmentType.SWORD);
    }
}
