package cn.nukkit.entity.ai.sensor;

import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.ai.sensor.Sensor;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.Player;

import java.util.Objects;
import java.util.function.Predicate;

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
    protected final MemoryType<? super Player> resultMemory;
    protected final Predicate<Player> predicate;

    public NearestPlayerSensor(double range, double minRange, int period) {
        this(MemoryTypes.NEAREST_PLAYER, range, minRange, period, player -> true);
    }

    public NearestPlayerSensor(MemoryType<? super Player> resultMemory,
                               double range, double minRange, int period,
                               Predicate<Player> predicate) {
        this.range = range;
        this.minRange = minRange;
        this.rangeSquared = range * range;
        this.minRangeSquared = minRange * minRange;
        this.period = period;
        this.resultMemory = Objects.requireNonNull(resultMemory, "resultMemory");
        this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    public NearestPlayerSensor(double range) {
        this(range, 0, 1);
    }

    @Override
    public void sense(EntityIntelligent entity) {
        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (Player player : PlayerTickSnapshot.get(entity.getLevel())) {
            if (player.closed || !player.isAlive() || !player.spawned
                    || !player.isOnline() || player.isSpectator()) continue;
            if (!predicate.test(player)) continue;

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

        entity.getMemoryStorage().put(resultMemory, nearest);
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
