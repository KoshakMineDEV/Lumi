package cn.nukkit.item;

public class ItemNetheriteSpear extends ItemSpear {

    public ItemNetheriteSpear() {
        super(NETHERITE_SPEAR, "Netherite Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_NETHERITE;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_NETHERITE;
    }

    @Override
    public int getAttackDamage() {
        return 6;
    }

    @Override
    public int getChargeDelay() {
        return 8;
    }

    @Override
    public int getJabCooldown() {
        return 23;
    }

    @Override
    protected double getChargeDamageMultiplier() {
        return 1.20;
    }

    @Override
    protected int getChargeDamageDuration() {
        return 175;
    }

    @Override
    protected int getChargeKnockbackDuration() {
        return 110;
    }

    @Override
    protected int getChargeDismountDuration() {
        return 50;
    }

    @Override
    protected double getChargeDismountSpeed() {
        return 9.0;
    }

    @Override
    public boolean isLavaResistant() {
        return true;
    }
}
