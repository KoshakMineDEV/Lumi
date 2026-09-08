package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Looks at an entity stored directly in memory for a duration.
 *
 * @author daoge_cmd
 */
public class LookAtEntityExecutor implements BehaviorExecutor {

    protected final MemoryType<? extends Entity> entityMemory;
    protected final int minDuration;
    protected final int maxDuration;
    protected final double maxRangeSquared;

    protected int tickCounter;
    protected int currentDuration;

    public LookAtEntityExecutor(MemoryType<? extends Entity> entityMemory, int duration) {
        this(entityMemory, duration, duration, Double.POSITIVE_INFINITY);
    }

    /**
     * Creates an entity look behavior with an inclusive random duration range.
     */
    public LookAtEntityExecutor(MemoryType<? extends Entity> entityMemory,
                                int minDuration, int maxDuration, double maxRange) {
        if (minDuration < 0 || maxDuration < minDuration) {
            throw new IllegalArgumentException("Invalid look duration range");
        }
        if (Double.isNaN(maxRange) || maxRange < 0) {
            throw new IllegalArgumentException("maxRange must be non-negative");
        }
        this.entityMemory = entityMemory;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.maxRangeSquared = maxRange * maxRange;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        tickCounter = 0;
        currentDuration = minDuration == maxDuration
                ? minDuration
                : ThreadLocalRandom.current().nextInt(minDuration, maxDuration + 1);
        entity.setPitchEnabled(true);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        tickCounter++;
        if (tickCounter > currentDuration) return false;

        Entity targetEntity = entity.getMemoryStorage().get(entityMemory);
        if (!isTargetValid(entity, targetEntity)) {
            entity.getMemoryStorage().clear(entityMemory);
            return false;
        }

        double dx = targetEntity.x - entity.x;
        double dy = targetEntity.y - entity.y;
        double dz = targetEntity.z - entity.z;
        if (dx * dx + dy * dy + dz * dz > maxRangeSquared) {
            return false;
        }

        EntityControlHelper.setLookTarget(entity, new Vector3(
                targetEntity.x, targetEntity.y + targetEntity.getEyeHeight(), targetEntity.z
        ));

        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        EntityControlHelper.removeLookTarget(entity);
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
}
