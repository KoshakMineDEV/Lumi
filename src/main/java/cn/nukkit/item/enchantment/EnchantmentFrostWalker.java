package cn.nukkit.item.enchantment;

public class EnchantmentFrostWalker extends Enchantment {

    public EnchantmentFrostWalker() {
        super(ID_FROST_WALKER, NAME_FROST_WALKER, "frostwalker", EnchantmentRarity.RARE, EnchantmentType.ARMOR_FEET);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return level * 10;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return this.getMinEnchantAbility(level) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && !(enchantment instanceof EnchantmentWaterWorker);
    }
}
