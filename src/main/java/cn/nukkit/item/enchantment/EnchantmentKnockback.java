package cn.nukkit.item.enchantment;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentKnockback extends Enchantment {

    public EnchantmentKnockback() {
        super(ID_KNOCKBACK, NAME_KNOCKBACK, "knockback", EnchantmentRarity.UNCOMMON, EnchantmentType.SWORD);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 5 + (level - 1) * 20;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return super.getMinEnchantAbility(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}
