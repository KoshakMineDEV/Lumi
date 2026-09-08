package cn.nukkit.entity.ai.route.posevaluator;

import cn.nukkit.block.Block;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;

/**
 * Evaluates collision-free positions for flying entities. X/Z identify the
 * entity centre while Y identifies the entity's feet, matching route node
 * coordinates used by Java Edition's flying navigation.
 *
 * @author daoge_cmd
 */
public class FlyingPosEvaluator implements SpacePosEvaluator {

    private static final ThreadLocal<SimpleAxisAlignedBB> ENTITY_BOX = ThreadLocal.withInitial(
            () -> new SimpleAxisAlignedBB(0, 0, 0, 0, 0, 0)
    );

    @Override
    public boolean evaluate(EntityIntelligent entity, Vector3 pos) {
        return evaluate(entity, pos.x, pos.y, pos.z);
    }

    @Override
    public boolean evaluate(EntityIntelligent entity, double x, double y, double z) {
        SimpleAxisAlignedBB box = ENTITY_BOX.get();
        setEntityBox(
                box,
                x,
                y,
                z,
                entity.getWidth() * entity.getScale(),
                entity.getHeight() * entity.getScale()
        );
        return !hasCollisionTickCachedBlocks(entity.level, box);
    }

    static void setEntityBox(SimpleAxisAlignedBB box, double x, double y, double z,
                             double width, double height) {
        double radius = width * 0.5;
        box.setMinX(x - radius);
        box.setMinY(y);
        box.setMinZ(z - radius);
        box.setMaxX(x + radius);
        box.setMaxY(y + height);
        box.setMaxZ(z + radius);
    }

    public static boolean hasCollisionTickCachedBlocks(Level level, AxisAlignedBB bb) {
        int minX = NukkitMath.floorDouble(Math.min(bb.getMinX(), bb.getMaxX()));
        int minY = NukkitMath.floorDouble(Math.min(bb.getMinY(), bb.getMaxY()));
        int minZ = NukkitMath.floorDouble(Math.min(bb.getMinZ(), bb.getMaxZ()));
        int maxX = NukkitMath.ceilDouble(Math.max(bb.getMinX(), bb.getMaxX()));
        int maxY = NukkitMath.ceilDouble(Math.max(bb.getMinY(), bb.getMaxY()));
        int maxZ = NukkitMath.ceilDouble(Math.max(bb.getMinZ(), bb.getMaxZ()));

        if (minY < level.getMinBlockY() || maxY > level.getMaxBlockY() + 1) {
            return true;
        }

        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = NukkitMath.floorDouble(Math.nextDown(Math.max(bb.getMinX(), bb.getMaxX()))) >> 4;
        int maxChunkZ = NukkitMath.floorDouble(Math.nextDown(Math.max(bb.getMinZ(), bb.getMaxZ()))) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.isChunkLoaded(chunkX, chunkZ)) {
                    return true;
                }
            }
        }

        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = level.getTickCachedBlock(x, y, z, false);
                    if (!block.canPassThrough() && block.collidesWithBB(bb)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Int3Predicate {
        boolean test(int x, int y, int z);
    }

    public static boolean anyBlockPos(AxisAlignedBB bb, boolean ceilMax, Int3Predicate predicate) {
        int minX = NukkitMath.floorDouble(Math.min(bb.getMinX(), bb.getMaxX()));
        int minY = NukkitMath.floorDouble(Math.min(bb.getMinY(), bb.getMaxY()));
        int minZ = NukkitMath.floorDouble(Math.min(bb.getMinZ(), bb.getMaxZ()));
        double maxXValue = Math.max(bb.getMinX(), bb.getMaxX());
        double maxYValue = Math.max(bb.getMinY(), bb.getMaxY());
        double maxZValue = Math.max(bb.getMinZ(), bb.getMaxZ());
        int maxX = ceilMax ? NukkitMath.ceilDouble(maxXValue) : NukkitMath.floorDouble(maxXValue);
        int maxY = ceilMax ? NukkitMath.ceilDouble(maxYValue) : NukkitMath.floorDouble(maxYValue);
        int maxZ = ceilMax ? NukkitMath.ceilDouble(maxZValue) : NukkitMath.floorDouble(maxZValue);
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    if (predicate.test(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
