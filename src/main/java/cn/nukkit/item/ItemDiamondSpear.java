package cn.nukkit.item;

public class ItemDiamondSpear extends ItemSpear {

    public ItemDiamondSpear() {
        super(DIAMOND_SPEAR, "Diamond Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_DIAMOND;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_DIAMOND;
    }

    @Override
    public int getAttackDamage() {
        return 5;
    }

    @Override
    public int getChargeDelay() {
        return 10;
    }

    @Override
    public int getJabCooldown() {
        return 21;
    }

    @Override
    protected double getChargeDamageMultiplier() {
        return 1.075;
    }

    @Override
    protected int getChargeDamageDuration() {
        return 200;
    }

    @Override
    protected int getChargeKnockbackDuration() {
        return 130;
    }

    @Override
    protected int getChargeDismountDuration() {
        return 60;
    }

    @Override
    protected double getChargeDismountSpeed() {
        return 10.0;
    }
}
