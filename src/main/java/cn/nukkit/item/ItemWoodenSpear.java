package cn.nukkit.item;

public class ItemWoodenSpear extends ItemSpear {

    public ItemWoodenSpear() {
        super(WOODEN_SPEAR, "Wooden Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_WOODEN;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public int getAttackDamage() {
        return 2;
    }

    @Override
    public int getChargeDelay() {
        return 15;
    }

    @Override
    public int getJabCooldown() {
        return 13;
    }

    @Override
    protected double getChargeDamageMultiplier() {
        return 0.70;
    }

    @Override
    protected int getChargeDamageDuration() {
        return 300;
    }

    @Override
    protected int getChargeKnockbackDuration() {
        return 200;
    }

    @Override
    protected int getChargeDismountDuration() {
        return 100;
    }

    @Override
    protected double getChargeDismountSpeed() {
        return 14.0;
    }
}
