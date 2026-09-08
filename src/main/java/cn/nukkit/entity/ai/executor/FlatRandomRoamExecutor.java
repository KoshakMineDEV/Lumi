package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

import static cn.nukkit.entity.ai.executor.EntityControlHelper.setLookTarget;
import static cn.nukkit.entity.ai.executor.EntityControlHelper.setRouteTarget;

/**
 * Random wandering executor. Picks random XZ targets within range
 * and sets route/look targets.
 *
 * @author daoge_cmd
 */
public class FlatRandomRoamExecutor implements BehaviorExecutor {

    protected static final double TARGET_REACHED_DISTANCE_SQUARED = 1.0;
    protected static final double MIN_PROGRESS_DISTANCE_SQUARED = 0.0625;
    protected static final int MAX_STUCK_TICKS = 40;

   
    protected final float speed;
    protected final int maxRoamRange;
    protected final int frequency;
    protected final boolean calNextTargetImmediately;
    protected final int runningTime;
    protected final boolean avoidWater;
    protected final int maxRetryTime;

    protected int durationTick;
    protected int targetCalTick;
    protected int retryCount;
    protected int stuckTick;
    protected double bestDistanceSquared;
    protected boolean hadMoveDirection;
    protected boolean hasTarget;

    public FlatRandomRoamExecutor(float speed, int maxRoamRange, int frequency,
                                  boolean calNextTargetImmediately, int runningTime,
                                  boolean avoidWater, int maxRetryTime) {
        if (maxRoamRange < 0) {
            throw new IllegalArgumentException("maxRoamRange must be non-negative");
        }
        this.speed = speed;
        this.maxRoamRange = maxRoamRange;
        this.frequency = frequency;
        this.calNextTargetImmediately = calNextTargetImmediately;
        this.runningTime = runningTime;
        this.avoidWater = avoidWater;
        this.maxRetryTime = maxRetryTime;
    }

    public FlatRandomRoamExecutor(float speed, int maxRoamRange, int frequency) {
        this(speed, maxRoamRange, frequency, true, 100, false, 10);
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        durationTick = 0;
        targetCalTick = 0;
        retryCount = 0;
        stuckTick = 0;
        bestDistanceSquared = Double.MAX_VALUE;
        hadMoveDirection = false;
        hasTarget = false;
        entity.setBaseMovementSpeed(speed);
        entity.setPitchEnabled(isPitchEnabledDuringRoam());
        findNewTarget(entity);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        durationTick++;
        targetCalTick++;

        // Check if we've reached the target
        var moveTarget = entity.getMoveTarget();
        if (moveTarget != null) {
            double distanceSquared = distanceSquaredToTarget(entity, moveTarget.x, moveTarget.y, moveTarget.z);
            if (entity.hasMoveDirection()) {
                hadMoveDirection = true;
            }
            if (distanceSquared < TARGET_REACHED_DISTANCE_SQUARED) {
                hasTarget = false;
                stuckTick = 0;
                bestDistanceSquared = Double.MAX_VALUE;
                hadMoveDirection = false;
                if (!calNextTargetImmediately) {
                    EntityControlHelper.removeRouteTarget(entity);
                    EntityControlHelper.removeLookTarget(entity);
                }
            } else if (hadMoveDirection && !entity.hasMoveDirection()) {
                abandonTarget(entity);
            } else if (distanceSquared + MIN_PROGRESS_DISTANCE_SQUARED < bestDistanceSquared) {
                bestDistanceSquared = distanceSquared;
                stuckTick = 0;
            } else if (++stuckTick >= MAX_STUCK_TICKS) {
                abandonTarget(entity);
            }
        } else if (hasTarget) {
            // Another behavior may temporarily own and then remove MOVE_TARGET.
            // Drop our stale bookkeeping so this core behavior can schedule a
            // fresh roaming target after its normal frequency delay.
            hasTarget = false;
            stuckTick = 0;
            bestDistanceSquared = Double.MAX_VALUE;
            hadMoveDirection = false;
        }

        if (!hasTarget) {
            if (calNextTargetImmediately) {
                findNewTarget(entity);
            } else if (targetCalTick >= frequency) {
                findNewTarget(entity);
            }
        }

        return runningTime <= 0 || durationTick <= runningTime;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        abandonTarget(entity);
        entity.setPitchEnabled(true);
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        abandonTarget(entity);
        entity.setPitchEnabled(true);
    }

    protected void findNewTarget(EntityIntelligent entity) {
        if (retryCount >= maxRetryTime) {
            return;
        }

        Vector3 target = nextTarget(entity);
        if (target == null) {
            retryCount++;
            return;
        }

        setRouteTarget(entity, target);
        setLookTarget(entity, target);
        hasTarget = true;
        stuckTick = 0;
        bestDistanceSquared = distanceSquaredToTarget(entity, target.x, target.y, target.z);
        hadMoveDirection = false;
        targetCalTick = 0;
        retryCount = 0;
    }

    /**
     * Chooses the next roaming target. Subclasses can replace the target
     * geometry without duplicating the executor lifecycle.
     */
    protected Vector3 nextTarget(EntityIntelligent entity) {
        RandomGenerator random = random();
        double targetX = entity.x + random.nextInt(-maxRoamRange, maxRoamRange + 1);
        double targetZ = entity.z + random.nextInt(-maxRoamRange, maxRoamRange + 1);
        double targetY = entity.y;

        if (!isCandidateValid(entity, targetX, targetY, targetZ)) {
            return null;
        }
        return new Vector3(targetX, targetY, targetZ);
    }

    /**
     * Validates a generated target. The flat executor deliberately keeps its
     * historic semantics and only rejects water beneath the target when
     * water avoidance is enabled.
     */
    protected boolean isCandidateValid(EntityIntelligent entity, double x, double y, double z) {
        if (!avoidWater) {
            return true;
        }
        return !entity.getLevel().getBlock(
                (int) Math.floor(x),
                (int) Math.floor(y) - 1,
                (int) Math.floor(z)
        ).isWater();
    }

    /**
     * Distance metric used for target completion and stuck detection.
     */
    protected double distanceSquaredToTarget(EntityIntelligent entity, double x, double y, double z) {
        double dx = x - entity.x;
        double dz = z - entity.z;
        return dx * dx + dz * dz;
    }

    /**
     * Whether look controllers may update pitch while this executor runs.
     */
    protected boolean isPitchEnabledDuringRoam() {
        return false;
    }

    /**
     * Random source hook used by specialised executors and deterministic tests.
     */
    protected RandomGenerator random() {
        return ThreadLocalRandom.current();
    }

    protected void abandonTarget(EntityIntelligent entity) {
        EntityControlHelper.removeRouteTarget(entity);
        EntityControlHelper.removeLookTarget(entity);
        hasTarget = false;
        stuckTick = 0;
        bestDistanceSquared = Double.MAX_VALUE;
        hadMoveDirection = false;
        targetCalTick = frequency;
    }
}
