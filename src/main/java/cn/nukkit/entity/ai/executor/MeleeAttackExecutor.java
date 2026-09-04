package cn.nukkit.entity.ai.executor;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

/**
 * Executes a simple melee chase-and-attack behavior against a target entity stored in memory.
 */
public class MeleeAttackExecutor implements BehaviorExecutor {

    protected static final double DEFAULT_ATTACK_RANGE = Math.sqrt(2.5);

    protected final MemoryType<Long> targetIdMemory;
   
    protected final float speed;
    protected final double maxSenseRangeSquared;
    protected final boolean clearTargetAfterLose;
    protected final int coolDown;
    protected final double attackRangeSquared;

    protected int attackTick;
    protected Vector3 lastTargetPos;

    /**
     * Creates a melee attack executor that keeps the target memory when the behavior stops.
     *
     * @param targetIdMemory the memory entry that stores the target entity runtime id.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param coolDown the attack cooldown in ticks.
     */
    public MeleeAttackExecutor(MemoryType<Long> targetIdMemory, float speed, double maxSenseRange, int coolDown) {
        this(targetIdMemory, speed, maxSenseRange, false, coolDown, DEFAULT_ATTACK_RANGE);
    }

    /**
     * Creates a melee attack executor with a custom attack range.
     *
     * @param targetIdMemory the memory entry that stores the target entity runtime id.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param coolDown the attack cooldown in ticks.
     * @param attackRange the maximum melee attack range in blocks.
     */
    public MeleeAttackExecutor(MemoryType<Long> targetIdMemory, float speed, double maxSenseRange,
                               int coolDown, double attackRange) {
        this(targetIdMemory, speed, maxSenseRange, false, coolDown, attackRange);
    }

    /**
     * Creates a melee attack executor with configurable target clearing behavior.
     *
     * @param targetIdMemory the memory entry that stores the target entity runtime id.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param clearTargetAfterLose whether to clear the target memory when the behavior stops.
     * @param coolDown the attack cooldown in ticks.
     */
    public MeleeAttackExecutor(MemoryType<Long> targetIdMemory, float speed, double maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown) {
        this(targetIdMemory, speed, maxSenseRange, clearTargetAfterLose, coolDown, DEFAULT_ATTACK_RANGE);
    }

    /**
     * Creates a melee attack executor with configurable target clearing behavior and attack range.
     *
     * @param targetIdMemory the memory entry that stores the target entity runtime id.
     * @param speed the movement speed while chasing the target.
     * @param maxSenseRange the maximum target tracking range in blocks.
     * @param clearTargetAfterLose whether to clear the target memory when the behavior stops.
     * @param coolDown the attack cooldown in ticks.
     * @param attackRange the maximum melee attack range in blocks.
     */
    public MeleeAttackExecutor(MemoryType<Long> targetIdMemory, float speed, double maxSenseRange,
                               boolean clearTargetAfterLose, int coolDown, double attackRange) {
        this.targetIdMemory = targetIdMemory;
        this.speed = speed;
        this.maxSenseRangeSquared = maxSenseRange * maxSenseRange;
        this.clearTargetAfterLose = clearTargetAfterLose;
        this.coolDown = coolDown;
        this.attackRangeSquared = attackRange * attackRange;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        attackTick = 0;
        lastTargetPos = null;
        entity.setBaseMovementSpeed(speed);
        entity.setPitchEnabled(true);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        attackTick++;

        var targetId = entity.getMemoryStorage().get(targetIdMemory);
        if (targetId == null) {
            return false;
        }

        var targetEntity = entity.getLevel().getEntity(targetId);
        if (!(targetEntity instanceof EntityLiving targetLiving) || !isTargetValid(targetEntity)) {
            return false;
        }

        var targetLoc = targetEntity.getLocation();
        var distanceSquared = entity.getLocation().distanceSquared(targetLoc);
        if (distanceSquared > maxSenseRangeSquared) {
            return false;
        }

        if (!entity.isPitchEnabled()) {
            entity.setPitchEnabled(true);
        }
        if (entity.getMovementSpeed() != speed) {
            entity.setBaseMovementSpeed(speed);
        }

        var targetPos = new Vector3(targetLoc.x, targetLoc.y, targetLoc.z);
        entity.setMoveTarget(targetPos);
        if (lastTargetPos == null || isInDifferentBlock(lastTargetPos, targetPos)) {
            entity.getBehaviorGroup().setRouteUpdateRequired(true);
        }
        lastTargetPos = targetPos;

        EntityControlHelper.setLookTarget(entity, new Vector3(
                targetLoc.x, targetLoc.y + targetEntity.getEyeHeight(), targetLoc.z
        ));

        if (distanceSquared <= attackRangeSquared && attackTick > coolDown) {
            var damage = getAttackDamage(entity, targetLiving);
            if (damage <= 0) {
                return false;
            }

            var attackSuccess = targetLiving.attack(new EntityDamageByEntityEvent(
                    entity.getLivingEntity(), targetLiving, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage));
            var animation = new AnimatePacket();
            animation.eid = entity.getRuntimeId();
            animation.action = AnimatePacket.Action.SWING_ARM;
            Server.broadcastPacket(entity.getLivingEntity().getViewers().values(), animation);
            entity.getLevel().addLevelSoundEvent(targetLoc,
                    attackSuccess ? LevelSoundEventPacket.SOUND_ATTACK_STRONG : LevelSoundEventPacket.SOUND_ATTACK_NODAMAGE);
            if (attackSuccess) {
                attackTick = 0;
            }
        }

        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        EntityControlHelper.removeRouteTarget(entity);
        EntityControlHelper.removeLookTarget(entity);
        entity.setPitchEnabled(false);
        entity.setBaseMovementSpeed(MemoryTypes.MOVEMENT_SPEED.defaultData().get());
        lastTargetPos = null;
        if (clearTargetAfterLose) {
            entity.getMemoryStorage().clear(targetIdMemory);
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

    protected boolean isTargetValid(Entity targetEntity) {
        if (!targetEntity.isAlive()) {
            return false;
        }

        if (targetEntity instanceof Player player) {
            return player.isSurvival() || player.isAdventure();
        }

        return true;
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
