package cn.nukkit.item;

public class ItemGoldenSpear extends ItemSpear {

    public ItemGoldenSpear() {
        super(GOLDEN_SPEAR, "Golden Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_GOLD;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_GOLD;
    }

    @Override
    public int getAttackDamage() {
        return 2;
    }

    @Override
    public int getChargeDelay() {
        return 14;
    }

    @Override
    public int getJabCooldown() {
        return 19;
    }

    @Override
    protected double getChargeDamageMultiplier() {
        return 0.70;
    }

    @Override
    protected int getChargeDamageDuration() {
        return 275;
    }

    @Override
    protected int getChargeKnockbackDuration() {
        return 170;
    }

    @Override
    protected int getChargeDismountDuration() {
        return 70;
    }

    @Override
    protected double getChargeDismountSpeed() {
        return 13.0;
    }
}
