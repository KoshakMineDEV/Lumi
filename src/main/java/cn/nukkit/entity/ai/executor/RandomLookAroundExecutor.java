package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.math.Vector3;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Looks in a random horizontal direction for a bounded number of ticks.
 */
public class RandomLookAroundExecutor implements BehaviorExecutor {

    private final int minDuration;
    private final int maxDuration;

    private double directionX;
    private double directionZ;
    private int remainingTicks;

    public RandomLookAroundExecutor() {
        this(20, 39);
    }

    /**
     * @param minDuration minimum duration in ticks, inclusive
     * @param maxDuration maximum duration in ticks, inclusive
     */
    public RandomLookAroundExecutor(int minDuration, int maxDuration) {
        if (minDuration < 0 || maxDuration < minDuration) {
            throw new IllegalArgumentException("Invalid look duration range");
        }
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = Math.PI * 2.0 * random.nextDouble();
        directionX = Math.cos(angle);
        directionZ = Math.sin(angle);
        remainingTicks = minDuration == maxDuration
                ? minDuration
                : random.nextInt(minDuration, maxDuration + 1);
        entity.setPitchEnabled(true);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        if (remainingTicks-- <= 0) {
            return false;
        }
        EntityControlHelper.setLookTarget(entity, new Vector3(
                entity.x + directionX,
                entity.y + entity.getEyeHeight(),
                entity.z + directionZ
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
}
