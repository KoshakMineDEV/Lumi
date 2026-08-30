package cn.nukkit.item;

public class ItemStoneSpear extends ItemSpear {

    public ItemStoneSpear() {
        super(STONE_SPEAR, "Stone Spear");
    }

    @Override
    public int getMaxDurability() {
        return ItemTool.DURABILITY_STONE;
    }

    @Override
    public int getTier() {
        return ItemTool.TIER_STONE;
    }

    @Override
    public int getAttackDamage() {
        return 3;
    }

    @Override
    public int getChargeDelay() {
        return 14;
    }

    @Override
    public int getJabCooldown() {
        return 15;
    }

    @Override
    protected double getChargeDamageMultiplier() {
        return 0.82;
    }

    @Override
    protected int getChargeDamageDuration() {
        return 275;
    }

    @Override
    protected int getChargeKnockbackDuration() {
        return 180;
    }

    @Override
    protected int getChargeDismountDuration() {
        return 90;
    }

    @Override
    protected double getChargeDismountSpeed() {
        return 13.0;
    }
}
