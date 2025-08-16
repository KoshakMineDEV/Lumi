package cn.nukkit.item.enchantment.loot;

import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentLootDigging extends EnchantmentLoot {

    public EnchantmentLootDigging() {
        super(ID_FORTUNE_DIGGING, NAME_FORTUNE_DIGGING, "lootBonusDigger", EnchantmentRarity.RARE, EnchantmentType.DIGGER);
    }
}
