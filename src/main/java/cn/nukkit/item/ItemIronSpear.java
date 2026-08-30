package cn.nukkit.item;

public class ItemIronSpear extends ItemSpear {

    public ItemIronSpear() {
        super(IRON_SPEAR, "Iron Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_IRON;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_IRON;
    }

    @Override
    public int getAttackDamage() {
        return 4;
    }

    @Override
    public int getChargeDelay() {
        return 12;
    }

    @Override
    public int getJabCooldown() {
        return 19;
    }

    @Override
    protected double getChargeDamageMultiplier() {
        return 0.95;
    }

    @Override
    protected int getChargeDamageDuration() {
        return 225;
    }

    @Override
    protected int getChargeKnockbackDuration() {
        return 135;
    }

    @Override
    protected int getChargeDismountDuration() {
        return 50;
    }

    @Override
    protected double getChargeDismountSpeed() {
        return 11.0;
    }
}
