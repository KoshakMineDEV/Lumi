package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

/**
 * Follows an entity whose runtime ID is stored in memory.
 *
 * @author daoge_cmd
 */
public class FollowEntityExecutor implements BehaviorExecutor {

    protected final MemoryType<Long> entityIdMemory;
   
    protected final float speed;
    protected final double maxRangeSq;
    protected final double minRangeSq;

    protected Vector3 lastTargetPos;

    public FollowEntityExecutor(MemoryType<Long> entityIdMemory, float speed, double maxRangeSq, double minRangeSq) {
        this.entityIdMemory = entityIdMemory;
        this.speed = speed;
        this.maxRangeSq = maxRangeSq;
        this.minRangeSq = minRangeSq;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        entity.setBaseMovementSpeed(speed);
        lastTargetPos = null;
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        var targetId = entity.getMemoryStorage().get(entityIdMemory);
        if (targetId == null) return false;

        var targetEntity = entity.getLevel().getEntity(targetId);
        if (targetEntity == null) return false;

        double dx = entity.x - targetEntity.x;
        double dy = entity.y - targetEntity.y;
        double dz = entity.z - targetEntity.z;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > maxRangeSq) return false;

        if (distSq > minRangeSq) {
            // Re-path only when target moved >1 block
            if (lastTargetPos == null || lastTargetPos.distanceSquared(targetEntity) > 1.0) {
                var targetPos = new Vector3(targetEntity.x, targetEntity.y, targetEntity.z);
                EntityControlHelper.setRouteTarget(entity, targetPos);
                lastTargetPos = targetPos;
            }
        } else {
            EntityControlHelper.removeRouteTarget(entity);
        }

        EntityControlHelper.setLookTarget(entity, new Vector3(
                targetEntity.x, targetEntity.y + targetEntity.getEyeHeight(), targetEntity.z
        ));

        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        EntityControlHelper.removeRouteTarget(entity);
        EntityControlHelper.removeLookTarget(entity);
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }
}
