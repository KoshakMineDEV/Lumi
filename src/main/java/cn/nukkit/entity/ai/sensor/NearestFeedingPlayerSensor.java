package cn.nukkit.entity.ai.sensor;

import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.ai.sensor.Sensor;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.Player;
import cn.nukkit.item.Item;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Scans for the nearest player holding a breeding item for this animal.
 *
 * @author daoge_cmd
 */
public class NearestFeedingPlayerSensor implements Sensor {

    protected final double range;
    protected final double rangeSquared;
    protected final int period;
    protected final Predicate<Item> breedingItemPredicate;

    public NearestFeedingPlayerSensor(double range, int period, Predicate<Item> breedingItemPredicate) {
        this.range = range;
        this.rangeSquared = range * range;
        this.period = period;
        this.breedingItemPredicate = Objects.requireNonNull(breedingItemPredicate, "breedingItemPredicate");
    }

    public NearestFeedingPlayerSensor(double range, Predicate<Item> breedingItemPredicate) {
        this(range, 1, breedingItemPredicate);
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
            if (distSq > rangeSquared) continue;

            var itemInHand = player.getInventory().getItemInHand();
            if (!breedingItemPredicate.test(itemInHand)) continue;

            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = player;
            }
        }

        entity.getMemoryStorage().put(MemoryTypes.NEAREST_FEEDING_PLAYER, nearest != null ? nearest.getId() : null);
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
