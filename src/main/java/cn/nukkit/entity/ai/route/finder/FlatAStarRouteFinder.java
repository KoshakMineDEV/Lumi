package cn.nukkit.entity.ai.route.finder;

import cn.nukkit.entity.ai.route.Node;
import cn.nukkit.entity.ai.route.RouteFinder;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.level.Level;
import cn.nukkit.entity.ai.route.posevaluator.GroundPosEvaluator;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 2D A* pathfinder for ground-walking entities.
 *
 * @author daoge_cmd
 */
public class FlatAStarRouteFinder implements RouteFinder {

    protected static final double SQRT2_MINUS_1 = Math.sqrt(2) - 1;

    protected static final int[][] FLAT_NEIGHBORS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    protected final GroundPosEvaluator groundPosEvaluator;
    protected final int maxExpandedNodes;
    protected final int maxFallDistance;

    private static final ThreadLocal<SearchContext> SEARCH_CONTEXT = ThreadLocal.withInitial(SearchContext::new);

    public FlatAStarRouteFinder(GroundPosEvaluator groundPosEvaluator) {
        this(groundPosEvaluator, 100, 3);
    }

    public FlatAStarRouteFinder(GroundPosEvaluator groundPosEvaluator, int maxExpandedNodes, int maxFallDistance) {
        this.groundPosEvaluator = groundPosEvaluator;
        this.maxExpandedNodes = maxExpandedNodes;
        this.maxFallDistance = maxFallDistance;
    }

    protected FlatAStarRouteFinder(int maxExpandedNodes, int maxFallDistance) {
        this.groundPosEvaluator = null;
        this.maxExpandedNodes = maxExpandedNodes;
        this.maxFallDistance = maxFallDistance;
    }

    @Override
    public List<Node> search(EntityIntelligent entity, Vector3 target) {
        SearchContext context = SEARCH_CONTEXT.get();
        SearchSession session = context.session;
        session.prepare(this, entity, target, context);
        try {
            return doSearch(session);
        } finally {
            session.clear();
            context.clear();
        }
    }

    private List<Node> doSearch(SearchSession session) {
        EntityIntelligent entity = session.entity;
        SearchState state = session.state;
        int startX = (int) Math.floor(entity.x);
        int startZ = (int) Math.floor(entity.z);
        double startY = entity.y;

        int startRecord = state.addRecord(startX, startZ, startY,
                0, estimateCost(startX, startY, startZ,
                        session.targetX, session.targetY, session.targetZ), -1);
        state.setNodeState(startX, startY, startZ, startRecord + 1);
        state.addToHeap(startRecord);

        int bestRecord = startRecord;
        int depth = 0;
        while (!state.isHeapEmpty() && depth < maxExpandedNodes) {
            int current = state.pollHeap();
            if (state.getNodeState(state.x[current], state.standingY[current], state.z[current]) != current + 1) {
                continue;
            }
            state.setNodeState(state.x[current], state.standingY[current], state.z[current],
                    SearchState.CLOSED);

            if (isTargetReached(state.x[current], state.standingY[current], state.z[current],
                    session.targetX, session.targetY, session.targetZ)) {
                return floydSmooth(materializePath(state, current), session);
            }

            depth++;
            if (state.h[current] < state.h[bestRecord]) {
                bestRecord = current;
            }

            session.currentRecord = current;
            expandNeighbors(session);
        }

        if (bestRecord != startRecord) {
            return floydSmooth(materializePath(state, bestRecord), session);
        }
        return List.of();
    }

    protected void expandNeighbors(SearchSession session) {
        SearchState state = session.state;
        int current = session.currentRecord;
        int cx = state.x[current];
        int cy = (int) Math.floor(state.standingY[current]);
        int cz = state.z[current];

        for (int[] offset : FLAT_NEIGHBORS) {
            int dx = offset[0];
            int dz = offset[1];
            int nx = cx + dx;
            int nz = cz + dz;

            if (dx != 0 && dz != 0
                    && (!hasPassableAt(cx + dx, cy, cz, session)
                    || !hasPassableAt(cx, cy, cz + dz, session))) {
                continue;
            }

            for (int dy = 1; dy >= -maxFallDistance; dy--) {
                int walkY = cy + dy;
                if (!isPositionPassable(nx, walkY, nz, session)) {
                    continue;
                }
                session.offerNeighbor(nx, getNodeY(nx, walkY, nz, session), nz);
                break;
            }
        }
    }

    private void considerNeighbor(SearchSession session, int x, double standingY, int z) {
        SearchState state = session.state;
        int current = session.currentRecord;
        int existingState = state.getNodeState(x, standingY, z);
        if (existingState == SearchState.CLOSED) {
            return;
        }

        double dx = state.x[current] - x;
        double dy = state.standingY[current] - standingY;
        double dz = state.z[current] - z;
        double tentativeG = state.g[current] + Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (existingState == SearchState.UNSEEN) {
            int record = state.addRecord(x, z, standingY, tentativeG,
                    estimateCost(x, standingY, z,
                            session.targetX, session.targetY, session.targetZ), current);
            state.setNodeState(x, standingY, z, record + 1);
            state.addToHeap(record);
        } else {
            int existing = existingState - 1;
            if (tentativeG < state.g[existing]) {
                // Keep the old heap record immutable and supersede it, exactly
                // like the legacy implementation does with a new Node object.
                int updated = state.addRecord(x, z, standingY, tentativeG,
                        state.h[existing], current);
                state.setNodeState(x, standingY, z, updated + 1);
                state.addToHeap(updated);
            }
        }
    }

    protected double estimateCost(int x, double y, int z,
                                  int targetX, double targetY, int targetZ) {
        double dx = Math.abs(x - targetX);
        double dz = Math.abs(z - targetZ);
        return Math.max(dx, dz) + SQRT2_MINUS_1 * Math.min(dx, dz);
    }

    protected boolean isTargetReached(int x, double y, int z,
                                      int targetX, double targetY, int targetZ) {
        double dx = x - targetX;
        double dy = y - targetY;
        double dz = z - targetZ;
        return dx * dx + dz * dz < 1.0 && Math.abs(dy) <= maxFallDistance;
    }

    private static List<Node> materializePath(SearchState state, int endRecord) {
        int count = 0;
        for (int current = endRecord; current != -1; current = state.parent[current]) {
            count++;
        }
        state.ensurePathCapacity(count);

        int cursor = count;
        for (int current = endRecord; current != -1; current = state.parent[current]) {
            state.pathScratch[--cursor] = current;
        }

        ArrayList<Node> path = new ArrayList<>(count);
        Node parent = null;
        for (int i = 0; i < count; i++) {
            int record = state.pathScratch[i];
            Node node = new Node(state.x[record] + 0.5, state.standingY[record], state.z[record] + 0.5);
            node.setG(state.g[record]);
            node.setH(state.h[record]);
            node.setParent(parent);
            parent = node;
            path.add(node);
        }
        if (path.size() > 1) {
            path.removeFirst();
        }
        return path;
    }

    private boolean hasPassableAt(int x, int cy, int z, SearchSession session) {
        for (int dy = 1; dy >= -maxFallDistance; dy--) {
            if (isPositionPassable(x, cy + dy, z, session)) return true;
        }
        return false;
    }

    protected static long packPos(int x, int y, int z) {
        return (((long) x & 0x3FFFFFFL) << 38) | (((long) y & 0xFFFL) << 26) | ((long) z & 0x3FFFFFFL);
    }

    protected boolean isPositionPassable(int x, int y, int z, SearchSession session) {
        byte cached = session.getCachedPassability(x, y, z);
        if (cached != 0) return cached == 2;

        SearchContext context = session.context;
        Level dimension = session.dimension;
        EntityIntelligent entity = session.entity;
        var groundBlock = dimension.getTickCachedBlock(x, y - 1, z, false);
        boolean result = groundPosEvaluator != null && groundPosEvaluator.evaluate(entity, groundBlock);
        if (result) {
            double standingY = getNodeY(x, y, z, session);
            double halfWidth = (entity.getLivingEntity().getWidth() / 2.0);
            SimpleAxisAlignedBB entityBox = context.entityBox;
            entityBox.setMinX(x + 0.5 - halfWidth);
            entityBox.setMinY(standingY);
            entityBox.setMinZ(z + 0.5 - halfWidth);
            entityBox.setMaxX(x + 0.5 + halfWidth);
            entityBox.setMaxY(standingY + entity.getLivingEntity().getHeight());
            entityBox.setMaxZ(z + 0.5 + halfWidth);
            result = !dimension.hasTickCachedCollision(entity.getLivingEntity(), entityBox, false);
        }
        session.cachePassability(x, y, z, result);
        return result;
    }

    /**
     * Compute the actual Y position an entity would stand at for the walkable
     * position (x, y, z). Uses the ground block's collision shape top rather
     * than the integer block Y, so nodes on partial-height blocks (e.g. slabs)
     * carry the correct standing height.
     */
    protected double getNodeY(int x, int y, int z, SearchSession session) {
        var groundBlock = session.dimension.getTickCachedBlock(x, y - 1, z, false);
        var box = groundBlock.getCollisionBoundingBox();
        if (box != null) {
            return box.getMaxY();
        }
        return y;
    }

    protected List<Node> floydSmooth(List<Node> path, SearchSession session) {
        if (path.size() <= 2) return path;

        // Clear first node's parent so reconstruction stops here
        path.getFirst().setParent(null);

        int current = 0;
        int total = 2;
        while (total < path.size()) {
            if (hasBarrier(path.get(current), path.get(total), session)) {
                path.get(total - 1).setParent(path.get(current));
                current = total - 1;
            }
            total++;
        }
        // Connect the last node directly to the final pivot
        path.getLast().setParent(path.get(current));

        // Reconstruct smoothed path by following parent links from end
        var result = new ArrayList<Node>();
        var node = path.getLast();
        result.add(node);
        while (node.getParent() != null) {
            result.add(node = node.getParent());
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * Check if there is an unwalkable block between two nodes using Bresenham's line algorithm.
     */
    protected boolean hasBarrier(Node a, Node b, SearchSession session) {
        int x1 = (int) Math.floor(a.getVector().x);
        int z1 = (int) Math.floor(a.getVector().z);
        int x2 = (int) Math.floor(b.getVector().x);
        int z2 = (int) Math.floor(b.getVector().z);

        if (x1 == x2 && z1 == z2) return false;

        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = Integer.signum(x2 - x1);
        int sz = Integer.signum(z2 - z1);
        int err = dx - dz;
        int x = x1, z = z1;
        // Use ceil to recover the "walkable Y level" from non-integer standing Y.
        // For a slab at block y=64 (standingY=64.5), ceil gives 65 → isWalkable(x,65,z)
        // checks ground at y=64 (the slab). floor would give 64 → wrong ground block.
        int prevY = (int) Math.ceil(a.getVector().y);

        while (x != x2 || z != z2) {
            int e2 = 2 * err;
            boolean stepX = e2 > -dz;
            boolean stepZ = e2 < dx;
            if (stepX) { err -= dz; x += sx; }
            if (stepZ) { err += dx; z += sz; }

            // Don't check the endpoint (already validated by A*)
            if (x == x2 && z == z2) break;

            // Interpolate Y along the line
            double t = dx >= dz
                    ? (double) (x - x1) / (x2 - x1)
                    : (double) (z - z1) / (z2 - z1);
            int y = (int) Math.ceil(a.getVector().y + t * (b.getVector().y - a.getVector().y));

            if (!isPositionPassable(x, y, z, session)) return true;

            // For diagonal steps, also check both orthogonal cells to prevent corner-cutting
            if (stepX && stepZ) {
                if (!isPositionPassable(x - sx, y, z, session)
                        && !isPositionPassable(x, y, z - sz, session)) {
                    return true;
                }
            }

            // Verify vertical constraints: max +1 climb, max -maxFallDistance drop
            if (y - prevY > 1 || prevY - y > maxFallDistance) return true;
            prevY = y;
        }

        // Also check vertical constraints to the endpoint
        int endY = (int) Math.ceil(b.getVector().y);
        return endY - prevY > 1 || prevY - endY > maxFallDistance;
    }

    private static final class SearchContext {
        private final Long2ByteOpenHashMap walkableCache = new Long2ByteOpenHashMap(1024);
        private final SimpleAxisAlignedBB entityBox = new SimpleAxisAlignedBB(0, 0, 0, 0, 0, 0);
        private final SearchState state = new SearchState();
        private final SearchSession session = new SearchSession();

        private void clear() {
            walkableCache.clear();
            state.clear();
        }
    }

    protected static final class SearchSession {
        private FlatAStarRouteFinder owner;
        private SearchContext context;
        private SearchState state;
        private EntityIntelligent entity;
        private Level dimension;
        private int targetX;
        private double targetY;
        private int targetZ;
        private int currentRecord = -1;

        private void prepare(FlatAStarRouteFinder owner, EntityIntelligent entity,
                             Vector3 target, SearchContext context) {
            this.owner = owner;
            this.context = context;
            this.state = context.state;
            this.entity = entity;
            this.dimension = entity.getLevel();
            this.targetX = (int) Math.floor(target.x);
            this.targetY = target.y;
            this.targetZ = (int) Math.floor(target.z);
        }

        private void clear() {
            owner = null;
            context = null;
            state = null;
            entity = null;
            dimension = null;
            currentRecord = -1;
        }

        public int currentX() {
            return state.x[currentRecord];
        }

        public double currentY() {
            return state.standingY[currentRecord];
        }

        public int currentZ() {
            return state.z[currentRecord];
        }

        public Level dimension() {
            return dimension;
        }

        public EntityIntelligent entity() {
            return entity;
        }

        public boolean isPositionPassable(int x, int y, int z) {
            return owner.isPositionPassable(x, y, z, this);
        }

        public byte getCachedPassability(int x, int y, int z) {
            return context.walkableCache.get(packPos(x, y, z));
        }

        public void cachePassability(int x, int y, int z, boolean passable) {
            context.walkableCache.put(packPos(x, y, z), passable ? (byte) 2 : (byte) 1);
        }

        public double getNodeY(int x, int y, int z) {
            return owner.getNodeY(x, y, z, this);
        }

        public void offerNeighbor(int x, double y, int z) {
            owner.considerNeighbor(this, x, y, z);
        }
    }

    static final class SearchState {
        private static final int INITIAL_CAPACITY = 1024;
        private static final float STATE_LOAD_FACTOR = 0.6f;
        private static final int UNSEEN = 0;
        private static final int CLOSED = -1;

        private int[] x = new int[INITIAL_CAPACITY];
        private int[] z = new int[INITIAL_CAPACITY];
        private double[] standingY = new double[INITIAL_CAPACITY];
        private double[] g = new double[INITIAL_CAPACITY];
        private double[] h = new double[INITIAL_CAPACITY];
        private int[] parent = new int[INITIAL_CAPACITY];
        private int[] heap = new int[INITIAL_CAPACITY];
        private int[] pathScratch = new int[INITIAL_CAPACITY];
        private int nodeCount;
        private int heapSize;

        // Open-addressed node table matching Node.equals(): exact x/y/z values.
        // A zero value denotes an unused slot, -1 a closed node, and positive
        // values point at the currently active immutable heap record.
        private int[] stateX = new int[INITIAL_CAPACITY];
        private int[] stateZ = new int[INITIAL_CAPACITY];
        private long[] stateYBits = new long[INITIAL_CAPACITY];
        private int[] stateValues = new int[INITIAL_CAPACITY];
        private int stateSize;
        private int stateResizeAt = (int) (INITIAL_CAPACITY * STATE_LOAD_FACTOR);

        private void clear() {
            Arrays.fill(stateValues, UNSEEN);
            stateSize = 0;
            nodeCount = 0;
            heapSize = 0;
        }

        private int addRecord(int x, int z, double standingY,
                              double g, double h, int parent) {
            ensureNodeCapacity(nodeCount + 1);
            int record = nodeCount++;
            this.x[record] = x;
            this.z[record] = z;
            this.standingY[record] = standingY;
            this.g[record] = g;
            this.h[record] = h;
            this.parent[record] = parent;
            return record;
        }

        private void ensureNodeCapacity(int required) {
            if (required <= x.length) {
                return;
            }
            int capacity = Math.max(required, x.length << 1);
            x = Arrays.copyOf(x, capacity);
            z = Arrays.copyOf(z, capacity);
            standingY = Arrays.copyOf(standingY, capacity);
            g = Arrays.copyOf(g, capacity);
            h = Arrays.copyOf(h, capacity);
            parent = Arrays.copyOf(parent, capacity);
            heap = Arrays.copyOf(heap, capacity);
        }

        private int getNodeState(int x, double standingY, int z) {
            long yBits = Double.doubleToLongBits(standingY);
            int mask = stateValues.length - 1;
            int slot = nodeHash(x, yBits, z) & mask;
            while (stateValues[slot] != UNSEEN) {
                if (stateX[slot] == x && stateZ[slot] == z && stateYBits[slot] == yBits) {
                    return stateValues[slot];
                }
                slot = (slot + 1) & mask;
            }
            return UNSEEN;
        }

        private void setNodeState(int x, double standingY, int z, int value) {
            long yBits = Double.doubleToLongBits(standingY);
            int mask = stateValues.length - 1;
            int slot = nodeHash(x, yBits, z) & mask;
            while (stateValues[slot] != UNSEEN) {
                if (stateX[slot] == x && stateZ[slot] == z && stateYBits[slot] == yBits) {
                    stateValues[slot] = value;
                    return;
                }
                slot = (slot + 1) & mask;
            }

            if (stateSize >= stateResizeAt) {
                resizeStateTable();
                setNodeState(x, standingY, z, value);
                return;
            }
            stateX[slot] = x;
            stateZ[slot] = z;
            stateYBits[slot] = yBits;
            stateValues[slot] = value;
            stateSize++;
        }

        private void resizeStateTable() {
            int[] oldX = stateX;
            int[] oldZ = stateZ;
            long[] oldYBits = stateYBits;
            int[] oldValues = stateValues;
            int capacity = oldValues.length << 1;
            stateX = new int[capacity];
            stateZ = new int[capacity];
            stateYBits = new long[capacity];
            stateValues = new int[capacity];
            stateSize = 0;
            stateResizeAt = (int) (capacity * STATE_LOAD_FACTOR);
            for (int i = 0; i < oldValues.length; i++) {
                if (oldValues[i] != UNSEEN) {
                    putResizedState(oldX[i], oldYBits[i], oldZ[i], oldValues[i]);
                }
            }
        }

        private void putResizedState(int x, long yBits, int z, int value) {
            int mask = stateValues.length - 1;
            int slot = nodeHash(x, yBits, z) & mask;
            while (stateValues[slot] != UNSEEN) {
                slot = (slot + 1) & mask;
            }
            stateX[slot] = x;
            stateZ[slot] = z;
            stateYBits[slot] = yBits;
            stateValues[slot] = value;
            stateSize++;
        }

        private static int nodeHash(int x, long yBits, int z) {
            long bits = Double.doubleToLongBits(x + 0.5);
            bits = 31 * bits + yBits;
            bits = 31 * bits + Double.doubleToLongBits(z + 0.5);
            int hash = Long.hashCode(bits);
            hash ^= hash >>> 16;
            return hash;
        }

        private void ensurePathCapacity(int required) {
            if (required > pathScratch.length) {
                pathScratch = Arrays.copyOf(pathScratch, Math.max(required, pathScratch.length << 1));
            }
        }

        private boolean isHeapEmpty() {
            return heapSize == 0;
        }

        private void addToHeap(int record) {
            if (heapSize == heap.length) {
                heap = Arrays.copyOf(heap, heap.length << 1);
            }
            int index = heapSize++;
            while (index > 0) {
                int parentIndex = (index - 1) >>> 1;
                int parentRecord = heap[parentIndex];
                if (compare(record, parentRecord) >= 0) {
                    break;
                }
                heap[index] = parentRecord;
                index = parentIndex;
            }
            heap[index] = record;
        }

        private int pollHeap() {
            int result = heap[0];
            int newSize = --heapSize;
            if (newSize != 0) {
                int moved = heap[newSize];
                int index = 0;
                int half = newSize >>> 1;
                while (index < half) {
                    int child = (index << 1) + 1;
                    int childRecord = heap[child];
                    int right = child + 1;
                    if (right < newSize && compare(childRecord, heap[right]) > 0) {
                        child = right;
                        childRecord = heap[right];
                    }
                    if (compare(moved, childRecord) <= 0) {
                        break;
                    }
                    heap[index] = childRecord;
                    index = child;
                }
                heap[index] = moved;
            }
            return result;
        }

        private int compare(int left, int right) {
            return Double.compare(g[left] + h[left], g[right] + h[right]);
        }
    }
}
