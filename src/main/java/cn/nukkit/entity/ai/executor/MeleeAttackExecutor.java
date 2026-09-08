package cn.nukkit.entity.ai.executor;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.Player;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.EntityInventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Executes a simple melee chase-and-attack behavior against a target entity stored in memory.
 */
public class MeleeAttackExecutor implements BehaviorExecutor {

    protected static final double DEFAULT_ATTACK_RANGE = Math.sqrt(2.5);
    protected static final double RETREAT_MARGIN = 0.25;
    protected static final double RETREAT_TARGET_REFRESH_DISTANCE_SQUARED = 0.0625;
    protected static final double MIN_HORIZONTAL_DIRECTION_SQUARED = 1.0e-8;
    private static final Effect[] NO_EFFECTS = new Effect[0];

    protected final MemoryType<? extends Entity> targetMemory;
   
    protected final float speed;
    protected final double maxSenseRangeSquared;
    protected final boolean clearTargetAfterLose;
    protected final int coolDown;
    protected final double attackRangeSquared;
    protected final double minimumDistanceSquared;
    protected final double minimumDistance;
    protected final Effect[] effects;

    protected int attackTick;
    protected Vector3 lastTargetPos;
    protected Vector3 lastRetreatSourcePos;
    protected Vector3 ownedRouteTarget;
    protected float previousMovementSpeed;

    /**
     * Creates a melee attack executor that keeps the target memory when the behavior stops.
     *
     * @param targetMemory the memory entry that stores the target entity.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param coolDown the attack cooldown in ticks.
     */
    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, double maxSenseRange, int coolDown) {
        this(targetMemory, speed, maxSenseRange, false, coolDown, DEFAULT_ATTACK_RANGE, NO_EFFECTS);
    }

    /**
     * Creates a melee attack executor with a custom attack range.
     *
     * @param targetMemory the memory entry that stores the target entity.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param coolDown the attack cooldown in ticks.
     * @param attackRange the maximum melee attack range in blocks.
     */
    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, double maxSenseRange,
                               int coolDown, double attackRange) {
        this(targetMemory, speed, maxSenseRange, false, coolDown, attackRange, NO_EFFECTS);
    }

    /**
     * Creates a melee attack executor with configurable target clearing behavior.
     *
     * @param targetMemory the memory entry that stores the target entity.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param clearTargetAfterLose whether to clear the target memory when the behavior stops.
     * @param coolDown the attack cooldown in ticks.
     */
    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, double maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown) {
        this(targetMemory, speed, maxSenseRange, clearTargetAfterLose, coolDown,
                DEFAULT_ATTACK_RANGE, NO_EFFECTS);
    }

    /**
     * Creates a melee attack executor with configurable target clearing behavior and attack range.
     *
     * @param targetMemory the memory entry that stores the target entity.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param clearTargetAfterLose whether to clear the target memory when the behavior stops.
     * @param coolDown the attack cooldown in ticks.
     * @param attackRange the maximum melee attack range in blocks.
     */
    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, double maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown, double attackRange) {
        this(targetMemory, speed, maxSenseRange, clearTargetAfterLose, coolDown, attackRange, NO_EFFECTS);
    }

    /**
     * PNX-compatible constructor with effects applied after a successful hit.
     */
    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, int maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown, Effect... effects) {
        this(targetMemory, speed, maxSenseRange, clearTargetAfterLose, coolDown,
                DEFAULT_ATTACK_RANGE, effects);
    }

    /**
     * PNX-compatible constructor with a custom attack range and hit effects.
     */
    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, int maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown,
                               float attackRange, Effect... effects) {
        this(targetMemory, speed, (double) maxSenseRange, clearTargetAfterLose, coolDown,
                attackRange, effects);
    }

    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, double maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown,
                               double attackRange, Effect... effects) {
        this(targetMemory, speed, maxSenseRange, clearTargetAfterLose, coolDown,
                attackRange, -1, effects);
    }

    /**
     * Creates a melee executor that can maintain a minimum distance from its target.
     * A negative {@code minimumDistance} keeps the legacy chase behavior.
     */
    public MeleeAttackExecutor(MemoryType<? extends Entity> targetMemory, float speed, double maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown,
                               double attackRange, double minimumDistance, Effect... effects) {
        if (minimumDistance > attackRange) {
            throw new IllegalArgumentException("minimumDistance cannot exceed attackRange");
        }
        this.targetMemory = targetMemory;
        this.speed = speed;
        this.maxSenseRangeSquared = maxSenseRange * maxSenseRange;
        this.clearTargetAfterLose = clearTargetAfterLose;
        this.coolDown = coolDown;
        this.attackRangeSquared = attackRange * attackRange;
        this.minimumDistance = minimumDistance;
        this.minimumDistanceSquared = minimumDistance < 0
                ? -1
                : minimumDistance * minimumDistance;
        this.effects = effects == null || effects.length == 0 ? NO_EFFECTS : effects.clone();
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        attackTick = 0;
        lastTargetPos = null;
        lastRetreatSourcePos = null;
        ownedRouteTarget = null;
        previousMovementSpeed = entity.getMovementSpeed();
        entity.setBaseMovementSpeed(speed);
        entity.setPitchEnabled(true);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        attackTick++;

        Entity targetEntity = entity.getMemoryStorage().get(targetMemory);
        if (!(targetEntity instanceof EntityLiving targetLiving) || !isTargetValid(entity, targetEntity)) {
            if (targetEntity != null && isStaleTarget(entity, targetEntity)) {
                entity.getMemoryStorage().clear(targetMemory);
            }
            return false;
        }

        double dx = targetEntity.x - entity.x;
        double dy = targetEntity.y - entity.y;
        double dz = targetEntity.z - entity.z;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared > maxSenseRangeSquared) {
            return false;
        }

        if (!entity.isPitchEnabled()) {
            entity.setPitchEnabled(true);
        }
        if (entity.getMovementSpeed() != speed) {
            entity.setBaseMovementSpeed(speed);
        }

        updateMovementTarget(entity, targetEntity, distanceSquared);

        entity.setLookTarget(new Vector3(
                targetEntity.x, targetEntity.y + targetEntity.getEyeHeight(), targetEntity.z
        ));

        if (distanceSquared <= attackRangeSquared && attackTick > coolDown) {
            Item item = entity instanceof EntityInventoryHolder holder
                    ? holder.getItemInHand()
                    : Item.AIR_ITEM;
            Enchantment[] enchantments = item.applyEnchantments()
                    ? item.getEnchantments()
                    : Enchantment.EMPTY_ARRAY;

            float baseDamage = getAttackDamage(entity, targetLiving);
            if (baseDamage <= 0) {
                return false;
            }
            float damageValue = baseDamage + item.getAttackDamage(entity);
            for (Enchantment enchantment : enchantments) {
                damageValue += (float) enchantment.getDamageBonus(targetEntity, entity);
            }
            if (damageValue <= 0) {
                return false;
            }

            Map<EntityDamageEvent.DamageModifier, Float> damage =
                    new EnumMap<>(EntityDamageEvent.DamageModifier.class);
            damage.put(EntityDamageEvent.DamageModifier.BASE, damageValue);

            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
                    entity.getLivingEntity(),
                    targetLiving,
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                    damage,
                    0.3f,
                    enchantments
            );
            event.setBreakShield(item.canBreakShield());
            if (targetLiving.attack(event)) {
                for (Effect effect : effects) {
                    targetLiving.addEffect(effect.clone());
                }
                playAttackAnimation(entity);
                entity.getLevel().addLevelSoundEvent(targetEntity, LevelSoundEventPacket.SOUND_ATTACK_STRONG);
                entity.getMemoryStorage().put(MemoryTypes.LAST_ATTACK_TIME, entity.getTick());
                entity.getMemoryStorage().put(MemoryTypes.LAST_ATTACK_ENTITY, targetEntity);
                attackTick = 0;
            }
        }

        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        clearOwnedRoute(entity);
        EntityControlHelper.removeLookTarget(entity);
        entity.setPitchEnabled(false);
        entity.setBaseMovementSpeed(previousMovementSpeed);
        lastTargetPos = null;
        lastRetreatSourcePos = null;
        if (clearTargetAfterLose) {
            entity.getMemoryStorage().clear(targetMemory);
        }
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }

    protected boolean isInDifferentBlock(Vector3 oldTargetPos, Vector3 newTargetPos) {
        return Math.floor(oldTargetPos.x) != Math.floor(newTargetPos.x) ||
               Math.floor(oldTargetPos.y) != Math.floor(newTargetPos.y) ||
               Math.floor(oldTargetPos.z) != Math.floor(newTargetPos.z);
    }

    protected void updateMovementTarget(EntityIntelligent entity, Entity target, double distanceSquared) {
        if (minimumDistanceSquared >= 0) {
            if (distanceSquared < minimumDistanceSquared) {
                updateRetreatTarget(entity, target);
                lastTargetPos = null;
                return;
            }
            if (distanceSquared <= attackRangeSquared) {
                clearOwnedRoute(entity);
                lastTargetPos = null;
                lastRetreatSourcePos = null;
                return;
            }
        }

        lastRetreatSourcePos = null;
        if (lastTargetPos == null
                || !ownsCurrentRoute(entity)
                || isInDifferentBlock(lastTargetPos, target)) {
            lastTargetPos = new Vector3(target.x, target.y, target.z);
            setOwnedRoute(entity, lastTargetPos);
        }
    }

    protected void updateRetreatTarget(EntityIntelligent entity, Entity target) {
        if (lastRetreatSourcePos != null
                && lastRetreatSourcePos.distanceSquared(target) <= RETREAT_TARGET_REFRESH_DISTANCE_SQUARED
                && ownsCurrentRoute(entity)) {
            return;
        }

        Vector3 retreatTarget = calculateRetreatTarget(
                entity.x,
                entity.y,
                entity.z,
                target.x,
                target.z,
                entity.yaw,
                minimumDistance
        );
        lastRetreatSourcePos = new Vector3(target.x, target.y, target.z);
        setOwnedRoute(entity, retreatTarget);
    }

    static Vector3 calculateRetreatTarget(double entityX, double entityY, double entityZ,
                                          double targetX, double targetZ, double entityYaw,
                                          double minimumDistance) {
        double awayX = entityX - targetX;
        double awayZ = entityZ - targetZ;
        double horizontalSquared = awayX * awayX + awayZ * awayZ;
        if (horizontalSquared < MIN_HORIZONTAL_DIRECTION_SQUARED) {
            double yawRadians = Math.toRadians(entityYaw);
            awayX = Math.sin(yawRadians);
            awayZ = -Math.cos(yawRadians);
        } else {
            double inverseLength = 1.0 / Math.sqrt(horizontalSquared);
            awayX *= inverseLength;
            awayZ *= inverseLength;
        }

        double desiredDistance = minimumDistance + RETREAT_MARGIN;
        double retreatX = targetX + awayX * desiredDistance;
        double retreatZ = targetZ + awayZ * desiredDistance;
        int entityBlockX = (int) Math.floor(entityX);
        int entityBlockZ = (int) Math.floor(entityZ);
        if ((int) Math.floor(retreatX) == entityBlockX
                && (int) Math.floor(retreatZ) == entityBlockZ) {
            if (Math.abs(awayX) >= Math.abs(awayZ)) {
                retreatX = entityBlockX + (awayX >= 0 ? 1.5 : -0.5);
                retreatZ = entityZ;
            } else {
                retreatX = entityX;
                retreatZ = entityBlockZ + (awayZ >= 0 ? 1.5 : -0.5);
            }
        }
        return new Vector3(retreatX, entityY, retreatZ);
    }

    protected void setOwnedRoute(EntityIntelligent entity, Vector3 target) {
        EntityControlHelper.setRouteTarget(entity, target);
        ownedRouteTarget = entity.getMoveTarget();
    }

    protected boolean ownsCurrentRoute(EntityIntelligent entity) {
        return ownedRouteTarget != null
                && Objects.equals(entity.getMoveTarget(), ownedRouteTarget);
    }

    protected void clearOwnedRoute(EntityIntelligent entity) {
        if (ownsCurrentRoute(entity)) {
            EntityControlHelper.removeRouteTarget(entity);
        }
        ownedRouteTarget = null;
    }

    protected boolean isTargetValid(EntityIntelligent owner, Entity targetEntity) {
        if (isStaleTarget(owner, targetEntity)) {
            return false;
        }

        if (targetEntity instanceof Player player) {
            return player.spawned && player.isOnline()
                    && !player.isSpectator()
                    && (player.isSurvival() || player.isAdventure());
        }

        return true;
    }

    protected boolean isStaleTarget(EntityIntelligent owner, Entity targetEntity) {
        if (targetEntity.closed || !targetEntity.isAlive() || targetEntity.getLevel() != owner.getLevel()) {
            return true;
        }
        return targetEntity instanceof Player player
                && (!player.spawned || !player.isOnline());
    }

    protected void playAttackAnimation(EntityIntelligent entity) {
        AnimatePacket animation = new AnimatePacket();
        animation.eid = entity.getRuntimeId();
        animation.action = AnimatePacket.Action.SWING_ARM;
        Server.broadcastPacket(entity.getLivingEntity().getViewers().values(), animation);
    }

    protected float getAttackDamage(EntityIntelligent entity, EntityLiving victim) {
        return switch (Server.getInstance().getDifficulty()) {
            case PEACEFUL -> 0f;
            case EASY -> 2.5f;
            case NORMAL -> 3f;
            case HARD -> 4.5f;
        };
    }
}
