package cn.nukkit.item.enchantment.loot;

import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentLootFishing extends EnchantmentLoot {

    public EnchantmentLootFishing() {
        super(ID_FORTUNE_FISHING, NAME_FORTUNE_FISHING, "lootBonusFishing", EnchantmentRarity.RARE, EnchantmentType.FISHING_ROD);
    }
}
