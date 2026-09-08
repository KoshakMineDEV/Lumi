package cn.nukkit.entity.ai.route.finder;

import cn.nukkit.entity.ai.route.Node;
import cn.nukkit.entity.ai.route.posevaluator.SpacePosEvaluator;

/**
 * Three-dimensional A* pathfinder for flying and swimming entities. Neighbor
 * clearance follows Java Edition's FlyNodeEvaluator rules: diagonal movement
 * is only allowed when every required orthogonal intermediate cell is open.
 *
 * @author daoge_cmd
 */
public class SpaceAStarRouteFinder extends FlatAStarRouteFinder {

    protected static final double SQRT3_MINUS_SQRT2 = Math.sqrt(3) - Math.sqrt(2);

    /** Java FlyNodeEvaluator neighbor order. */
    protected static final int[][] SPACE_NEIGHBORS = {
            {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0},
            {0, 1, 1}, {-1, 1, 0}, {1, 1, 0}, {0, 1, -1},
            {0, -1, 1}, {-1, -1, 0}, {1, -1, 0}, {0, -1, -1},
            {1, 0, -1}, {1, 0, 1}, {-1, 0, -1}, {-1, 0, 1},
            {1, 1, -1}, {1, 1, 1}, {-1, 1, -1}, {-1, 1, 1},
            {1, -1, -1}, {1, -1, 1}, {-1, -1, -1}, {-1, -1, 1}
    };

    protected final SpacePosEvaluator spacePosEvaluator;

    public SpaceAStarRouteFinder(SpacePosEvaluator spacePosEvaluator) {
        this(spacePosEvaluator, 100, 3);
    }

    public SpaceAStarRouteFinder(SpacePosEvaluator spacePosEvaluator, int maxExpandedNodes, int maxFallDistance) {
        super(maxExpandedNodes, maxFallDistance);
        this.spacePosEvaluator = spacePosEvaluator;
    }

    @Override
    public boolean usesThreeDimensionalWaypoints() {
        return true;
    }

    @Override
    protected void expandNeighbors(SearchSession session) {
        int currentX = session.currentX();
        int currentY = (int) Math.floor(session.currentY());
        int currentZ = session.currentZ();
        long passabilityMask = 0;

        // Evaluate each candidate exactly once for this expansion. Checks from
        // adjacent expanded nodes are served by the per-search cache.
        for (int[] offset : SPACE_NEIGHBORS) {
            int dx = offset[0];
            int dy = offset[1];
            int dz = offset[2];
            if (session.isPositionPassable(currentX + dx, currentY + dy, currentZ + dz)) {
                passabilityMask |= positionBit(dx, dy, dz);
            }
        }

        for (int[] offset : SPACE_NEIGHBORS) {
            int dx = offset[0];
            int dy = offset[1];
            int dz = offset[2];
            if (isPassable(passabilityMask, dx, dy, dz)
                    && hasRequiredClearance(passabilityMask, dx, dy, dz)) {
                session.offerNeighbor(currentX + dx, currentY + dy, currentZ + dz);
            }
        }
    }

    static boolean hasRequiredClearance(long passabilityMask, int dx, int dy, int dz) {
        if (dx != 0 && !isPassable(passabilityMask, dx, 0, 0)) {
            return false;
        }
        if (dy != 0 && !isPassable(passabilityMask, 0, dy, 0)) {
            return false;
        }
        if (dz != 0 && !isPassable(passabilityMask, 0, 0, dz)) {
            return false;
        }
        if (dx != 0 && dy != 0 && dz != 0) {
            return isPassable(passabilityMask, dx, dy, 0)
                    && isPassable(passabilityMask, dx, 0, dz)
                    && isPassable(passabilityMask, 0, dy, dz);
        }
        return true;
    }

    static long positionBit(int dx, int dy, int dz) {
        return 1L << (((dy + 1) * 9) + ((dz + 1) * 3) + dx + 1);
    }

    static boolean isPassable(long passabilityMask, int dx, int dy, int dz) {
        return (passabilityMask & positionBit(dx, dy, dz)) != 0;
    }

    @Override
    protected boolean isPositionPassable(int x, int y, int z, SearchSession session) {
        byte cached = session.getCachedPassability(x, y, z);
        if (cached != 0) {
            return cached == 2;
        }

        boolean result = session.dimension().isYInRange(y)
                && session.dimension().isChunkLoaded(x >> 4, z >> 4);
        if (result) {
            var block = session.dimension().getTickCachedBlock(x, y, z, false);
            result = block.canPassThrough()
                    && spacePosEvaluator.evaluate(session.entity(), x + 0.5, y, z + 0.5);
        }
        session.cachePassability(x, y, z, result);
        return result;
    }

    @Override
    protected boolean isTargetReached(int x, double y, int z,
                                      int targetX, double targetY, int targetZ) {
        double dx = x - targetX;
        double dy = y - targetY;
        double dz = z - targetZ;
        return dx * dx + dy * dy + dz * dz < 1.5;
    }

    @Override
    protected boolean hasBarrier(Node a, Node b, SearchSession session) {
        int startX = (int) Math.floor(a.getVector().x);
        int startY = (int) Math.floor(a.getVector().y);
        int startZ = (int) Math.floor(a.getVector().z);
        int endX = (int) Math.floor(b.getVector().x);
        int endY = (int) Math.floor(b.getVector().y);
        int endZ = (int) Math.floor(b.getVector().z);

        int deltaX = endX - startX;
        int deltaY = endY - startY;
        int deltaZ = endZ - startZ;
        int steps = Math.max(Math.abs(deltaX), Math.max(Math.abs(deltaY), Math.abs(deltaZ)));
        if (steps == 0) {
            return false;
        }

        int previousX = startX;
        int previousY = startY;
        int previousZ = startZ;
        for (int step = 1; step <= steps; step++) {
            int x = startX + (int) Math.round((double) deltaX * step / steps);
            int y = startY + (int) Math.round((double) deltaY * step / steps);
            int z = startZ + (int) Math.round((double) deltaZ * step / steps);
            int stepX = x - previousX;
            int stepY = y - previousY;
            int stepZ = z - previousZ;
            if (!isTransitionClear(previousX, previousY, previousZ,
                    stepX, stepY, stepZ, session)) {
                return true;
            }
            previousX = x;
            previousY = y;
            previousZ = z;
        }
        return false;
    }

    private static boolean isTransitionClear(int x, int y, int z,
                                             int dx, int dy, int dz,
                                             SearchSession session) {
        if (dx == 0 && dy == 0 && dz == 0) {
            return true;
        }
        if (!session.isPositionPassable(x + dx, y + dy, z + dz)) {
            return false;
        }
        if (dx != 0 && !session.isPositionPassable(x + dx, y, z)) {
            return false;
        }
        if (dy != 0 && !session.isPositionPassable(x, y + dy, z)) {
            return false;
        }
        if (dz != 0 && !session.isPositionPassable(x, y, z + dz)) {
            return false;
        }
        if (dx != 0 && dy != 0 && dz != 0) {
            return session.isPositionPassable(x + dx, y + dy, z)
                    && session.isPositionPassable(x + dx, y, z + dz)
                    && session.isPositionPassable(x, y + dy, z + dz);
        }
        return true;
    }

    @Override
    protected double estimateCost(int x, double y, int z,
                                  int targetX, double targetY, int targetZ) {
        double dx = Math.abs(x - targetX);
        double dy = Math.abs(y - targetY);
        double dz = Math.abs(z - targetZ);
        // 3D Octile distance: admissible for 26-directional movement
        double max = Math.max(dx, Math.max(dy, dz));
        double min = Math.min(dx, Math.min(dy, dz));
        double mid = dx + dy + dz - max - min;
        return SQRT3_MINUS_SQRT2 * min + SQRT2_MINUS_1 * mid + max;
    }
}
