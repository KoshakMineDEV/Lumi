package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.route.posevaluator.FlyingPosEvaluator;
import cn.nukkit.level.Level;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;

import java.util.random.RandomGenerator;

/**
 * Three-dimensional random roaming with Java Edition-style target sampling.
 * The executor keeps the lifecycle, retry and stuck handling of
 * {@link FlatRandomRoamExecutor}, but evaluates progress in all three axes.
 *
 * @author daoge_cmd
 */
public class SpaceRandomRoamExecutor extends FlatRandomRoamExecutor {

    private static final int RANDOM_POS_ATTEMPTS = 10;
    private static final double MAX_DIRECTION_ANGLE = Math.PI * 0.5;
    private static final double SQRT_TWO = Math.sqrt(2.0);
    private static final int FLYING_HEIGHT_BIAS = -2;
    private static final int FALLBACK_VERTICAL_RANGE = 4;
    private static final int MIN_HOVER_HEIGHT = 1;
    private static final int MAX_HOVER_HEIGHT = 3;

    protected final int maxYRoamRange;
    private final SimpleAxisAlignedBB candidateBox = new SimpleAxisAlignedBB(0, 0, 0, 0, 0, 0);

    public SpaceRandomRoamExecutor(float speed, int maxXZRoamRange, int maxYRoamRange, int frequency) {
        this(speed, maxXZRoamRange, maxYRoamRange, frequency, false, 100);
    }

    public SpaceRandomRoamExecutor(float speed, int maxXZRoamRange, int maxYRoamRange, int frequency,
                                   boolean calNextTargetImmediately, int runningTime) {
        this(speed, maxXZRoamRange, maxYRoamRange, frequency,
                calNextTargetImmediately, runningTime, true, 10);
    }

    public SpaceRandomRoamExecutor(float speed, int maxXZRoamRange, int maxYRoamRange, int frequency,
                                   boolean calNextTargetImmediately, int runningTime,
                                   boolean avoidWater, int maxRetryTime) {
        super(speed, maxXZRoamRange, frequency, calNextTargetImmediately, runningTime, avoidWater, maxRetryTime);
        if (maxYRoamRange < 0) {
            throw new IllegalArgumentException("maxYRoamRange must be non-negative");
        }
        this.maxYRoamRange = maxYRoamRange;
    }

    @Override
    protected Vector3 nextTarget(EntityIntelligent entity) {
        RandomGenerator random = random();
        if (avoidWater) {
            Vector3 hoverTarget = findBestTarget(entity, random, maxYRoamRange, 0, true);
            if (hoverTarget != null) {
                return hoverTarget;
            }
            return findBestTarget(
                    entity,
                    random,
                    Math.min(maxYRoamRange, FALLBACK_VERTICAL_RANGE),
                    maxYRoamRange == 0 ? 0 : FLYING_HEIGHT_BIAS,
                    false
            );
        }
        return findBestTarget(
                entity,
                random,
                maxYRoamRange,
                maxYRoamRange == 0 ? 0 : FLYING_HEIGHT_BIAS,
                false
        );
    }

    private Vector3 findBestTarget(EntityIntelligent entity, RandomGenerator random,
                                   int verticalRange, int flyingHeight, boolean hover) {
        Level level = entity.getLevel();
        int baseBlockX = entity.getFloorX();
        int baseBlockY = entity.getFloorY();
        int baseBlockZ = entity.getFloorZ();

        double yawRadians = Math.toRadians(entity.yaw);
        double viewX = -Math.sin(yawRadians);
        double viewZ = Math.cos(yawRadians);
        double directionAngle = Math.atan2(viewZ, viewX) - Math.PI * 0.5;

        double bestWeight = Double.NEGATIVE_INFINITY;
        int bestX = 0;
        int bestY = 0;
        int bestZ = 0;
        boolean found = false;

        for (int attempt = 0; attempt < RANDOM_POS_ATTEMPTS; attempt++) {
            double angle = directionAngle + (random.nextFloat() * 2.0 - 1.0) * MAX_DIRECTION_ANGLE;
            double distance = Math.sqrt(random.nextDouble()) * maxRoamRange * SQRT_TWO;
            double offsetX = -distance * Math.sin(angle);
            double offsetZ = distance * Math.cos(angle);
            if (Math.abs(offsetX) > maxRoamRange || Math.abs(offsetZ) > maxRoamRange) {
                continue;
            }

            int candidateX = NukkitMath.floorDouble(entity.x + offsetX);
            int candidateZ = NukkitMath.floorDouble(entity.z + offsetZ);
            if (Math.abs(candidateX - baseBlockX) > maxRoamRange
                    || Math.abs(candidateZ - baseBlockZ) > maxRoamRange
                    || !level.isChunkLoaded(candidateX >> 4, candidateZ >> 4)) {
                continue;
            }

            int candidateY = baseBlockY + random.nextInt(-verticalRange, verticalRange + 1) + flyingHeight;
            if (hover) {
                candidateY = findHoverY(entity, candidateX, candidateY, candidateZ, random);
            } else {
                candidateY = moveUpOutOfSolid(entity, candidateX, candidateY, candidateZ);
            }
            if (!isInsideRoamBounds(baseBlockY, candidateY)
                    || !isCandidateValid(entity, candidateX + 0.5, candidateY, candidateZ + 0.5)) {
                continue;
            }

            double weight = getPositionWeight(entity, candidateX, candidateY, candidateZ);
            if (weight > bestWeight) {
                bestWeight = weight;
                bestX = candidateX;
                bestY = candidateY;
                bestZ = candidateZ;
                found = true;
            }
        }

        return found ? new Vector3(bestX + 0.5, bestY, bestZ + 0.5) : null;
    }

    private int findHoverY(EntityIntelligent entity, int x, int initialY, int z, RandomGenerator random) {
        Level level = entity.getLevel();
        int minY = Math.max(level.getMinBlockY(), entity.getFloorY() - maxYRoamRange);
        int maxY = Math.min(level.getMaxBlockY(), entity.getFloorY() + maxYRoamRange);
        int y = NukkitMath.clamp(initialY, minY, maxY);

        while (y >= minY && !level.getBlock(x, y, z, false).isSolid()) {
            y--;
        }
        if (y < minY) {
            return Integer.MIN_VALUE;
        }

        while (y <= maxY && level.getBlock(x, y, z, false).isSolid()) {
            y++;
        }
        if (y > maxY) {
            return Integer.MIN_VALUE;
        }

        return y + random.nextInt(MIN_HOVER_HEIGHT, MAX_HOVER_HEIGHT + 1);
    }

    private int moveUpOutOfSolid(EntityIntelligent entity, int x, int initialY, int z) {
        Level level = entity.getLevel();
        if (initialY < level.getMinBlockY() || initialY > level.getMaxBlockY()) {
            return Integer.MIN_VALUE;
        }
        int y = initialY;
        while (y <= level.getMaxBlockY() && level.getBlock(x, y, z, false).isSolid()) {
            y++;
        }
        return y;
    }

    private boolean isInsideRoamBounds(int baseBlockY, int y) {
        return y != Integer.MIN_VALUE && Math.abs(y - baseBlockY) <= maxYRoamRange;
    }

    @Override
    protected boolean isCandidateValid(EntityIntelligent entity, double x, double y, double z) {
        Level level = entity.getLevel();
        double width = entity.getWidth() * entity.getScale();
        double height = entity.getHeight() * entity.getScale();
        double radius = width * 0.5;
        double maxY = y + height;
        if (y < level.getMinBlockY() || maxY > level.getMaxBlockY() + 1.0) {
            return false;
        }

        int minChunkX = NukkitMath.floorDouble(x - radius) >> 4;
        int minChunkZ = NukkitMath.floorDouble(z - radius) >> 4;
        int maxChunkX = NukkitMath.floorDouble(Math.nextDown(x + radius)) >> 4;
        int maxChunkZ = NukkitMath.floorDouble(Math.nextDown(z + radius)) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.isChunkLoaded(chunkX, chunkZ)) {
                    return false;
                }
            }
        }

        if (avoidWater && level.getBlock(
                NukkitMath.floorDouble(x),
                NukkitMath.floorDouble(y),
                NukkitMath.floorDouble(z),
                false
        ).isWater()) {
            return false;
        }

        SimpleAxisAlignedBB box = candidateBox;
        box.setMinX(x - radius);
        box.setMinY(y);
        box.setMinZ(z - radius);
        box.setMaxX(x + radius);
        box.setMaxY(maxY);
        box.setMaxZ(z + radius);
        return !FlyingPosEvaluator.hasCollisionTickCachedBlocks(level, box);
    }

    /**
     * Candidate preference hook equivalent to Java Edition's
     * {@code PathfinderMob#getWalkTargetValue}. The default value deliberately
     * gives every valid position equal weight, retaining the first one found.
     */
    protected double getPositionWeight(EntityIntelligent entity, int x, int y, int z) {
        return 0.0;
    }

    @Override
    protected double distanceSquaredToTarget(EntityIntelligent entity, double x, double y, double z) {
        double dx = x - entity.x;
        double dy = y - entity.y;
        double dz = z - entity.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    protected boolean isPitchEnabledDuringRoam() {
        return true;
    }
}
