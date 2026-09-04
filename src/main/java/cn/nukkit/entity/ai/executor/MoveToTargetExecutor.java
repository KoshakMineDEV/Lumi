package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

import static cn.nukkit.entity.ai.executor.EntityControlHelper.removeLookTarget;
import static cn.nukkit.entity.ai.executor.EntityControlHelper.removeRouteTarget;

/**
 * Moves the entity toward a position stored in a memory type.
 *
 * @author daoge_cmd
 */
public class MoveToTargetExecutor implements BehaviorExecutor {

    protected final MemoryType<? extends Vector3> memoryType;
   
    protected final float speed;
    protected final boolean updateRouteImmediately;
    protected final double maxFollowRangeSquared;
    protected final double minFollowRangeSquared;

    protected Vector3 lastTarget;

    public MoveToTargetExecutor(MemoryType<? extends Vector3> memoryType, float speed,
                                boolean updateRouteImmediately) {
        this(memoryType, speed, updateRouteImmediately, 256.0, 0.0);
    }

    public MoveToTargetExecutor(MemoryType<? extends Vector3> memoryType, float speed,
                                boolean updateRouteImmediately,
                                double maxFollowRangeSquared, double minFollowRangeSquared) {
        this.memoryType = memoryType;
        this.speed = speed;
        this.updateRouteImmediately = updateRouteImmediately;
        this.maxFollowRangeSquared = maxFollowRangeSquared;
        this.minFollowRangeSquared = minFollowRangeSquared;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        lastTarget = null;
        entity.setBaseMovementSpeed(speed);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        var target = entity.getMemoryStorage().get(memoryType);
        if (target == null) {
            return false;
        }

        double dx = entity.x - target.x;
        double dy = entity.y - target.y;
        double dz = entity.z - target.z;
        double distSq = dx * dx + dy * dy + dz * dz;

        // Check range limits
        if (distSq > maxFollowRangeSquared || distSq < minFollowRangeSquared) {
            return false;
        }

        // Update route if target changed
        if (lastTarget == null || target.distanceSquared(lastTarget) > 1.0) {
            EntityControlHelper.setRouteTarget(entity, target);
            EntityControlHelper.setLookTarget(entity, target);
            if (updateRouteImmediately) {
                entity.getBehaviorGroup().setRouteUpdateRequired(true);
            }
            lastTarget = target.clone();
        }

        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        lastTarget = null;
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }
}
