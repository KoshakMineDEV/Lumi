package cn.nukkit.entity.ai.executor;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.entity.projectile.EntitySmallFireBall;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.concurrent.ThreadLocalRandom;

/**
 * PNX-style Blaze ranged attack with a charged wind-up and a three-fireball sequence.
 */
public class BlazeShootExecutor implements BehaviorExecutor {

    static final int FIREBALL_COUNT = 3;
    static final int FIREBALL_INTERVAL = 6;
    static final int FIRE_SEQUENCE_DURATION = 20;

    protected final MemoryType<? extends Entity> targetMemory;
    protected final float speed;
    protected final double maxShootDistanceSquared;
    protected final boolean clearTargetAfterLose;
    protected final int coolDownTick;
    protected final int fireTick;
    protected final boolean manageMovement;

    protected Entity target;
    protected int coolDownCounter;
    protected int windUpCounter;
    protected int fireSequenceTick;
    protected int fireballsShot;
    protected float previousMovementSpeed;
    protected Vector3 lastRouteTarget;

    public BlazeShootExecutor(MemoryType<? extends Entity> targetMemory, float speed,
                              int maxShootDistance, boolean clearTargetAfterLose,
                              int coolDownTick, int fireTick) {
        this(targetMemory, speed, maxShootDistance, clearTargetAfterLose,
                coolDownTick, fireTick, true);
    }

    public BlazeShootExecutor(MemoryType<? extends Entity> targetMemory, float speed,
                              int maxShootDistance, boolean clearTargetAfterLose,
                              int coolDownTick, int fireTick, boolean manageMovement) {
        if (maxShootDistance < 0 || coolDownTick < 0 || fireTick < 0) {
            throw new IllegalArgumentException("Distances and tick counts must be non-negative");
        }
        this.targetMemory = targetMemory;
        this.speed = speed;
        this.maxShootDistanceSquared = (double) maxShootDistance * maxShootDistance;
        this.clearTargetAfterLose = clearTargetAfterLose;
        this.coolDownTick = coolDownTick;
        this.fireTick = fireTick;
        this.manageMovement = manageMovement;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        target = null;
        lastRouteTarget = null;
        if (manageMovement) {
            previousMovementSpeed = entity.getMovementSpeed();
            entity.setBaseMovementSpeed(speed);
        }
        entity.setPitchEnabled(true);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        Entity newTarget = entity.getMemoryStorage().get(targetMemory);
        if (!isTargetValid(entity, newTarget)) {
            if (newTarget != null && isStaleTarget(entity, newTarget)) {
                entity.getMemoryStorage().clear(targetMemory);
            }
            return false;
        }
        boolean targetChanged = target != newTarget;
        if (targetChanged) {
            target = newTarget;
            if (isCharged()) {
                setCharged(entity, true);
            }
        }

        if (windUpCounter == 0) {
            coolDownCounter++;
        }
        if (!entity.isPitchEnabled()) {
            entity.setPitchEnabled(true);
        }
        if (manageMovement && entity.getMovementSpeed() != speed) {
            entity.setBaseMovementSpeed(speed);
        }

        double dx = target.x - entity.x;
        double dy = target.y - entity.y;
        double dz = target.z - entity.z;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (manageMovement) {
            if (distanceSquared > maxShootDistanceSquared) {
                Vector3 currentRouteTarget = entity.getMoveTarget();
                if (targetChanged || lastRouteTarget == null || currentRouteTarget != lastRouteTarget
                        || isInDifferentBlock(lastRouteTarget, target)) {
                    lastRouteTarget = new Vector3(target.x, target.y, target.z);
                    entity.setMoveTarget(lastRouteTarget);
                    entity.getBehaviorGroup().setRouteUpdateRequired(true);
                }
            } else {
                if (entity.getMoveTarget() != null) {
                    EntityControlHelper.removeRouteTarget(entity);
                }
                lastRouteTarget = null;
            }
        } else {
            lastRouteTarget = null;
        }
        entity.setLookTarget(new Vector3(target.x, target.y, target.z));

        tickFireSequence(entity);

        if (windUpCounter == 0 && fireSequenceTick == 0 && coolDownCounter > coolDownTick) {
            if (distanceSquared <= maxShootDistanceSquared) {
                coolDownCounter = 0;
                windUpCounter = 1;
                setCharged(entity, true);
            }
        } else if (windUpCounter != 0) {
            windUpCounter++;
            if (windUpCounter > fireTick) {
                startFireSequence();
                tickFireSequence(entity);
                windUpCounter = 0;
                return target.isAlive();
            }
        }
        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        reset(entity);
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        reset(entity);
    }

    protected void reset(EntityIntelligent entity) {
        if (manageMovement) {
            EntityControlHelper.removeRouteTarget(entity);
            entity.setBaseMovementSpeed(previousMovementSpeed);
        }
        EntityControlHelper.removeLookTarget(entity);
        if (clearTargetAfterLose) {
            entity.getMemoryStorage().clear(targetMemory);
        }
        entity.setPitchEnabled(false);
        setCharged(entity, false);
        windUpCounter = 0;
        resetFireSequence();
        target = null;
        lastRouteTarget = null;
    }

    protected void startFireSequence() {
        fireSequenceTick = 1;
        fireballsShot = 0;
    }

    protected void tickFireSequence(EntityIntelligent entity) {
        if (fireSequenceTick == 0) {
            return;
        }

        int elapsedTick = fireSequenceTick - 1;
        if (fireballsShot < FIREBALL_COUNT && elapsedTick == fireballsShot * FIREBALL_INTERVAL) {
            shootFireball(entity);
            fireballsShot++;
        }

        if (elapsedTick >= FIRE_SEQUENCE_DURATION) {
            setCharged(entity, false);
            resetFireSequence();
            return;
        }
        fireSequenceTick++;
    }

    protected void resetFireSequence() {
        fireSequenceTick = 0;
        fireballsShot = 0;
    }

    protected void shootFireball(EntityIntelligent entity) {
        double yawRadians = Math.toRadians(entity.headYaw);
        double pitchRadians = Math.toRadians(entity.pitch);
        double cosPitch = Math.cos(pitchRadians);
        Vector3 motion = new Vector3(
                -Math.sin(yawRadians) * cosPitch,
                -Math.sin(pitchRadians),
                Math.cos(yawRadians) * cosPitch
        );

        double spawnOffsetScale = 1.0 + ThreadLocalRandom.current().nextFloat(0.2f);
        Location spawnLocation = entity.getLocation();
        spawnLocation.y = entity.y + entity.getEyeHeight() + motion.y * spawnOffsetScale;
        float rotationYaw = (entity.headYaw > 180 ? 360 : 0) - (float) entity.headYaw;
        float rotationPitch = (float) -entity.pitch;
        CompoundTag nbt = Entity.getDefaultNBT(spawnLocation, motion, rotationYaw, rotationPitch)
                .putDouble("damage", 2);

        Entity projectile = Entity.createEntity(
                "SmallFireBall",
                entity.getLevel().getChunk(entity.getChunkX(), entity.getChunkZ()),
                nbt
        );
        if (!(projectile instanceof EntitySmallFireBall fireball)) {
            if (projectile != null) {
                projectile.close();
            }
            return;
        }
        fireball.shootingEntity = entity;

        ProjectileLaunchEvent launchEvent = new ProjectileLaunchEvent(fireball);
        if (!launchEvent.call()) {
            fireball.close();
            return;
        }
        fireball.spawnToAll();
        entity.getLevel().addSound(entity, Sound.MOB_BLAZE_SHOOT);
    }

    protected void setCharged(EntityIntelligent entity, boolean charged) {
        long targetId = charged && target != null ? target.getId() : 0;
        entity.setDataProperty(new LongEntityData(Entity.DATA_TARGET_EID, targetId));
        entity.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_CHARGED, charged);
    }

    protected boolean isCharged() {
        return windUpCounter != 0 || fireSequenceTick != 0;
    }

    protected boolean isInDifferentBlock(Vector3 oldPosition, Entity candidate) {
        return Math.floor(oldPosition.x) != Math.floor(candidate.x)
                || Math.floor(oldPosition.y) != Math.floor(candidate.y)
                || Math.floor(oldPosition.z) != Math.floor(candidate.z);
    }

    protected boolean isTargetValid(EntityIntelligent owner, Entity candidate) {
        if (isStaleTarget(owner, candidate)) {
            return false;
        }
        if (candidate instanceof Player player) {
            return !player.isSpectator() && (player.isSurvival() || player.isAdventure());
        }
        return true;
    }

    protected boolean isStaleTarget(EntityIntelligent owner, Entity candidate) {
        if (candidate == null || candidate.closed || !candidate.isAlive()
                || candidate.getLevel() != owner.getLevel()) {
            return true;
        }
        if (candidate instanceof Player player) {
            return !player.spawned || !player.isOnline();
        }
        return false;
    }
}
