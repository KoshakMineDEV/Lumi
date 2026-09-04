package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

/**
 * Utility class with static helper methods for controlling entity movement
 * and look targets through memory storage.
 *
 * @author daoge_cmd
 */
public final class EntityControlHelper {

    private EntityControlHelper() {
    }

    public static void setRouteTarget(EntityIntelligent entity, Vector3 target) {
        entity.setMoveTarget(target == null ? null : target.clone());
        entity.getBehaviorGroup().setRouteUpdateRequired(true);
    }

    public static void setLookTarget(EntityIntelligent entity, Vector3 target) {
        entity.setLookTarget(target == null ? null : target.clone());
    }

    public static void removeRouteTarget(EntityIntelligent entity) {
        entity.getMemoryStorage().clear(MemoryTypes.MOVE_TARGET);
        entity.setMoveDirectionStart(null);
        entity.setMoveDirectionEnd(null);
    }

    public static void removeLookTarget(EntityIntelligent entity) {
        entity.getMemoryStorage().clear(MemoryTypes.LOOK_TARGET);
    }
}
