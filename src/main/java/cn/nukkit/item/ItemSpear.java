package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.EnumMap;
import java.util.Map;

public abstract class ItemSpear extends StringItemToolBase {

    private static final double MIN_REACH = 2.0;
    private static final double MAX_REACH = 4.5;
    private static final double CREATIVE_MAX_REACH = 7.5;
    private static final double MIN_RELATIVE_SPEED = 4.6;
    private static final double MIN_KNOCKBACK_SPEED = 5.1;

    public ItemSpear(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean isSpear() {
        return true;
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        player.getLevel().addLevelSoundEvent(player, this.getUseSound());
        return true;
    }

    @Override
    public boolean canRelease() {
        return true;
    }

    @Override
    public boolean onRelease(Player player, int ticksUsed) {
        return true;
    }

    public double getMaximumReach(boolean creative) {
        return creative ? CREATIVE_MAX_REACH : MAX_REACH;
    }

    public abstract int getChargeDelay();

    public abstract int getJabCooldown();

    protected abstract double getChargeDamageMultiplier();

    protected abstract int getChargeDamageDuration();

    protected abstract int getChargeKnockbackDuration();

    protected abstract int getChargeDismountDuration();

    protected abstract double getChargeDismountSpeed();

    public boolean canDealChargeDamage(int ticksUsed, double relativeSpeed) {
        return ticksUsed >= this.getChargeDelay()
                && ticksUsed <= this.getChargeDamageDuration()
                && relativeSpeed >= MIN_RELATIVE_SPEED;
    }

    public boolean canChargeKnockBack(int ticksUsed, double forwardSpeed) {
        return ticksUsed >= this.getChargeDelay()
                && ticksUsed <= this.getChargeKnockbackDuration()
                && forwardSpeed >= MIN_KNOCKBACK_SPEED;
    }

    public boolean canChargeDismount(int ticksUsed, double forwardSpeed) {
        return ticksUsed >= this.getChargeDelay()
                && ticksUsed <= this.getChargeDismountDuration()
                && forwardSpeed >= this.getChargeDismountSpeed();
    }
    
    public int getChargeDamage(double relativeSpeed) {
        return this.getAttackDamage() + (int) Math.floor(relativeSpeed * this.getChargeDamageMultiplier());
    }

    public int attackInView(Player player, boolean kinetic) {
        Vector3 start = player.getEyePosition();
        Vector3 direction = player.getDirectionVector();
        double maximumReach = this.getMaximumReach(player.isCreative());
        Vector3 end = start.add(direction.multiply(maximumReach));
        int ticksUsed = kinetic ? player.getServer().getTick() - player.getStartActionTick() : 0;
        int hitCount = 0;

        for (Entity target : player.getLevel().getEntities()) {
            if (target == player || !target.isAlive()) {
                continue;
            }

            if (target instanceof Player targetPlayer
                    && (targetPlayer.isSpectator() || !player.getLevel().getGameRules().getBoolean(GameRule.PVP))) {
                continue;
            }

            AxisAlignedBB hitbox = target.boundingBox.grow(0.25, 0.25, 0.25);
            MovingObjectPosition collision = hitbox.calculateIntercept(start, end);
            if (collision == null) {
                continue;
            }

            double distance = start.distance(collision.hitVector);
            if (distance < MIN_REACH || distance > maximumReach) {
                continue;
            }

            Vector3 playerVelocity = player.speed == null
                    ? player.getMotion().multiply(20)
                    : player.speed.multiply(-20);
            Vector3 targetVelocity = target.getMotion().multiply(20);
            double forwardSpeed = playerVelocity.dot(direction);
            double relativeSpeed = playerVelocity.subtract(targetVelocity).dot(direction);
            boolean chargedHit = kinetic && this.canDealChargeDamage(ticksUsed, relativeSpeed);
            if (kinetic && !chargedHit) {
                continue;
            }

            float damage = chargedHit ? this.getChargeDamage(relativeSpeed) : this.getAttackDamage(player);
            Enchantment[] enchantments = this.getEnchantments();
            for (Enchantment enchantment : enchantments) {
                damage += (float) enchantment.getDamageBonus(target, player);
            }

            Map<EntityDamageEvent.DamageModifier, Float> modifiers = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
            modifiers.put(EntityDamageEvent.DamageModifier.BASE, damage);
            float knockBack = chargedHit && !this.canChargeKnockBack(ticksUsed, forwardSpeed) ? 0 : 0.3f;
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
                    player, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, modifiers, knockBack, enchantments
            );
            event.setBreakShield(this.canBreakShield());

            if (!target.attack(event)) {
                continue;
            }

            if (chargedHit && this.canChargeDismount(ticksUsed, forwardSpeed) && target.getRiding() != null) {
                target.getRiding().dismountEntity(target);
            }

            player.getLevel().addLevelSoundEvent(target, this.getAttackHitSound());
            for (Enchantment enchantment : enchantments) {
                enchantment.doPostAttack(player, target);
            }

            if (!player.isCreative()) {
                this.useOn(target);
                player.getInventory().setItemInHand(this);
            }
            hitCount++;
        }

        return hitCount;
    }

    public int getAttackHitSound() {
        return switch (this.getTier()) {
            case ItemTool.TIER_WOODEN -> LevelSoundEventPacket.SOUND_WOODEN_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_STONE -> LevelSoundEventPacket.SOUND_STONE_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_COPPER -> LevelSoundEventPacket.SOUND_COPPER_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_IRON -> LevelSoundEventPacket.SOUND_IRON_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_GOLD -> LevelSoundEventPacket.SOUND_GOLDEN_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_DIAMOND -> LevelSoundEventPacket.SOUND_DIAMOND_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_NETHERITE -> LevelSoundEventPacket.SOUND_NETHERITE_SPEAR_ATTACK_HIT;
            default -> LevelSoundEventPacket.SOUND_SPEAR_ATTACK_HIT;
        };
    }

    public int getAttackMissSound() {
        return switch (this.getTier()) {
            case ItemTool.TIER_WOODEN -> LevelSoundEventPacket.SOUND_WOODEN_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_STONE -> LevelSoundEventPacket.SOUND_STONE_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_COPPER -> LevelSoundEventPacket.SOUND_COPPER_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_IRON -> LevelSoundEventPacket.SOUND_IRON_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_GOLD -> LevelSoundEventPacket.SOUND_GOLDEN_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_DIAMOND -> LevelSoundEventPacket.SOUND_DIAMOND_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_NETHERITE -> LevelSoundEventPacket.SOUND_NETHERITE_SPEAR_ATTACK_MISS;
            default -> LevelSoundEventPacket.SOUND_SPEAR_ATTACK_MISS;
        };
    }

    private int getUseSound() {
        return switch (this.getTier()) {
            case ItemTool.TIER_WOODEN -> LevelSoundEventPacket.SOUND_WOODEN_SPEAR_USE;
            case ItemTool.TIER_STONE -> LevelSoundEventPacket.SOUND_STONE_SPEAR_USE;
            case ItemTool.TIER_COPPER -> LevelSoundEventPacket.SOUND_COPPER_SPEAR_USE;
            case ItemTool.TIER_IRON -> LevelSoundEventPacket.SOUND_IRON_SPEAR_USE;
            case ItemTool.TIER_GOLD -> LevelSoundEventPacket.SOUND_GOLDEN_SPEAR_USE;
            case ItemTool.TIER_DIAMOND -> LevelSoundEventPacket.SOUND_DIAMOND_SPEAR_USE;
            case ItemTool.TIER_NETHERITE -> LevelSoundEventPacket.SOUND_NETHERITE_SPEAR_USE;
            default -> LevelSoundEventPacket.SOUND_SPEAR_USE;
        };
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_130;
    }
}
