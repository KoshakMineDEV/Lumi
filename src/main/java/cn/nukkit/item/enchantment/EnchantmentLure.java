package cn.nukkit.item.enchantment;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentLure extends Enchantment {

    public EnchantmentLure() {
        super(ID_LURE, NAME_LURE, "fishingSpeed", EnchantmentRarity.RARE, EnchantmentType.FISHING_ROD);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return level + 8 * level + 6;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
