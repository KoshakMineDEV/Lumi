package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.block.Block;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.math.Vector3;

/**
 * Checks whether a position-like memory value is within an inclusive distance range.
 */
public class DistanceEvaluator implements BehaviorEvaluator {

    private final MemoryType<? extends Vector3> memoryType;
    private final double maxDistanceSquared;
    private final double minDistanceSquared;
    private final boolean hasMinimum;

    public DistanceEvaluator(MemoryType<? extends Vector3> memoryType, double maxDistance) {
        this(memoryType, maxDistance, -1);
    }

    public DistanceEvaluator(MemoryType<? extends Vector3> memoryType,
                             double maxDistance, double minDistance) {
        this.memoryType = memoryType;
        this.maxDistanceSquared = maxDistance * maxDistance;
        this.hasMinimum = minDistance >= 0;
        this.minDistanceSquared = minDistance * minDistance;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        Vector3 position = entity.getMemoryStorage().get(memoryType);
        if (position == null) {
            return false;
        }

        double targetX = position.x;
        double targetY = position.y;
        double targetZ = position.z;
        if (position instanceof Block) {
            targetX += 0.5;
            targetZ += 0.5;
        }

        double dx = entity.x - targetX;
        double dy = entity.y - targetY;
        double dz = entity.z - targetZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared <= maxDistanceSquared
                && (!hasMinimum || distanceSquared >= minDistanceSquared);
    }
}
