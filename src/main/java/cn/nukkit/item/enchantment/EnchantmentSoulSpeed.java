package cn.nukkit.item.enchantment;

public class EnchantmentSoulSpeed extends Enchantment {

    public EnchantmentSoulSpeed() {
        super(ID_SOUL_SPEED, NAME_SOUL_SPEED, "soul_speed", EnchantmentRarity.VERY_RARE, EnchantmentType.ARMOR_FEET);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 10 * level;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
}
