package cn.nukkit.item;

public class ItemCopperSpear extends ItemSpear {

    public ItemCopperSpear() {
        super(COPPER_SPEAR, "Copper Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_COPPER;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_COPPER;
    }

    @Override
    public int getAttackDamage() {
        return 3;
    }

    @Override
    public int getChargeDelay() {
        return 13;
    }

    @Override
    public int getJabCooldown() {
        return 17;
    }

    @Override
    protected double getChargeDamageMultiplier() {
        return 0.82;
    }

    @Override
    protected int getChargeDamageDuration() {
        return 250;
    }

    @Override
    protected int getChargeKnockbackDuration() {
        return 165;
    }

    @Override
    protected int getChargeDismountDuration() {
        return 80;
    }

    @Override
    protected double getChargeDismountSpeed() {
        return 12.0;
    }
}
