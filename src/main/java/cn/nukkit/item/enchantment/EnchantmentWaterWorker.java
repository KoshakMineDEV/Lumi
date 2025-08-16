package cn.nukkit.item.enchantment;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentWaterWorker extends Enchantment {

    public EnchantmentWaterWorker() {
        super(ID_WATER_WORKER, NAME_WATER_WORKER, "waterWorker", EnchantmentRarity.RARE, EnchantmentType.ARMOR_HEAD);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 1;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return this.getMinEnchantAbility(level) + 40;
    }
}
