package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;

import java.util.Objects;

/**
 * Follows an entity stored directly in memory.
 *
 * @author daoge_cmd
 */
public class FollowEntityExecutor implements BehaviorExecutor {

    protected final MemoryType<? extends Entity> entityMemory;
   
    protected final float speed;
    protected final double maxRangeSq;
    protected final double minRangeSq;

    protected Vector3 lastTargetPos;
    protected Vector3 ownedRouteTarget;

    public FollowEntityExecutor(MemoryType<? extends Entity> entityMemory,
                                float speed, double maxRangeSq, double minRangeSq) {
        this.entityMemory = entityMemory;
        this.speed = speed;
        this.maxRangeSq = maxRangeSq;
        this.minRangeSq = minRangeSq;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        entity.setBaseMovementSpeed(speed);
        lastTargetPos = null;
        ownedRouteTarget = null;
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        Entity targetEntity = entity.getMemoryStorage().get(entityMemory);
        if (!isTargetValid(entity, targetEntity)) {
            entity.getMemoryStorage().clear(entityMemory);
            return false;
        }

        double dx = entity.x - targetEntity.x;
        double dy = entity.y - targetEntity.y;
        double dz = entity.z - targetEntity.z;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > maxRangeSq) return false;

        if (distSq > minRangeSq) {
            // Re-path only when target moved >1 block
            if (lastTargetPos == null
                    || lastTargetPos.distanceSquared(targetEntity) > 1.0
                    || !ownsCurrentRoute(entity)) {
                var targetPos = new Vector3(targetEntity.x, targetEntity.y, targetEntity.z);
                EntityControlHelper.setRouteTarget(entity, targetPos);
                lastTargetPos = targetPos;
                ownedRouteTarget = entity.getMoveTarget();
            }
        } else {
            clearOwnedRoute(entity);
            // Re-entering the follow range must schedule a route immediately,
            // even when the target itself has not moved since the last route.
            lastTargetPos = null;
        }

        EntityControlHelper.setLookTarget(entity, new Vector3(
                targetEntity.x, targetEntity.y + targetEntity.getEyeHeight(), targetEntity.z
        ));

        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        clearOwnedRoute(entity);
        EntityControlHelper.removeLookTarget(entity);
        lastTargetPos = null;
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }

    protected boolean isTargetValid(EntityIntelligent owner, Entity target) {
        if (target == null || target.closed || !target.isAlive() || target.getLevel() != owner.getLevel()) {
            return false;
        }
        if (target instanceof Player player) {
            return player.spawned && player.isOnline() && !player.isSpectator();
        }
        return true;
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
}
