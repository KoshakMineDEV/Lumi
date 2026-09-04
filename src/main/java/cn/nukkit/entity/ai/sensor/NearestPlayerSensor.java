package cn.nukkit.entity.ai.sensor;

import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.ai.sensor.Sensor;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.Player;

/**
 * Scans for the nearest player within range and stores it in memory.
 *
 * @author daoge_cmd
 */
public class NearestPlayerSensor implements Sensor {

    protected final double range;
    protected final double minRange;
    protected final double rangeSquared;
    protected final double minRangeSquared;
    protected final int period;

    public NearestPlayerSensor(double range, double minRange, int period) {
        this.range = range;
        this.minRange = minRange;
        this.rangeSquared = range * range;
        this.minRangeSquared = minRange * minRange;
        this.period = period;
    }

    public NearestPlayerSensor(double range) {
        this(range, 0, 1);
    }

    @Override
    public void sense(EntityIntelligent entity) {
        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (Player player : PlayerTickSnapshot.get(entity.getLevel())) {
            if (!player.isAlive() || player.isSpectator()) continue;

            double dx = entity.x - player.x;
            double dy = entity.y - player.y;
            double dz = entity.z - player.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < minRangeSquared || distSq > rangeSquared) continue;

            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = player;
            }
        }

        entity.getMemoryStorage().put(MemoryTypes.NEAREST_PLAYER, nearest != null ? nearest.getId() : null);
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
