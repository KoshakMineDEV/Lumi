package cn.nukkit.entity.ai.behaviorgroup;

import cn.nukkit.Server;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import cn.nukkit.entity.ai.behavior.Behavior;
import cn.nukkit.entity.ai.behavior.BehaviorState;
import cn.nukkit.entity.ai.controller.Controller;
import cn.nukkit.entity.ai.memory.MemoryStorage;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.ai.route.Node;
import cn.nukkit.entity.ai.route.RouteFinder;
import cn.nukkit.entity.ai.sensor.Sensor;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.MemoryStorageImpl;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Full AI orchestrator with priority-based behavior selection, period timers,
 * and route management.
 *
 * @author daoge_cmd
 */
@Builder
public class BehaviorGroupImpl implements BehaviorGroup {

    protected static final int ROUTE_UPDATE_CYCLE = 20;

    @Singular
    @Getter
    protected final Set<Behavior> coreBehaviors;

    @Singular
    @Getter
    protected final Set<Behavior> behaviors;

    @Singular
    @Getter
    protected final Set<Sensor> sensors;

    @Singular
    @Getter
    protected final Set<Controller> controllers;

    @Getter
    protected final RouteFinder routeFinder;

    @Getter
    @Setter
    protected boolean routeUpdateRequired;

    // Route state managed locally (RouteFinder is stateless)
    protected transient List<Node> route;
    protected transient int nodeIndex;
    protected transient int routeUpdateTick;
    protected transient int routeSearchCooldown;
    protected transient CompletableFuture<List<Node>> pendingRoute;
    protected transient Vector3 pendingRouteTarget;
    protected transient Level pendingRouteLevel;
    protected transient long routeRequestGeneration;
    protected transient long pendingRouteGeneration;

    @Builder.Default
    @Getter
    protected MemoryStorage memoryStorage = new MemoryStorageImpl();

    // Hot-path snapshots preserve builder/set iteration order without allocating iterators every tick.
    protected transient Sensor[] sensorArray;
    protected transient Behavior[] coreBehaviorArray;
    protected transient Behavior[] behaviorArray;
    protected transient Controller[] controllerArray;

    // Primitive period counters are aligned with the component snapshots above.
    protected transient int[] sensorPeriodCounters;
    protected transient int[] coreBehaviorPeriodCounters;
    protected transient int[] behaviorPeriodCounters;

    // Running/candidate behaviors are stored as indexes into the snapshots. This avoids
    // LinkedHashSet iterators, hashing and per-tick temporary collections.
    protected transient boolean[] coreBehaviorRunning;
    protected transient boolean[] behaviorRunning;
    protected transient int[] runningCoreBehaviorIndexes;
    protected transient int runningCoreBehaviorCount;
    protected transient int[] runningBehaviorIndexes;
    protected transient int runningBehaviorCount;
    protected transient int[] evaluatedBehaviorIndexes;
    protected transient int evaluatedBehaviorCount;

    protected transient EntityIntelligent entity;

    /**
     * Called when this behavior group is assigned to an entity.
     * Initializes period counters and injects entity reference into memory storage.
     *
     * @param entity the entity this behavior group is assigned to
     */
    @Override
    public void setEntity(EntityIntelligent entity) {
        this.entity = entity;
        this.routeUpdateTick = initialRouteUpdateTick(entity.getId());
        initPeriodCounters();
    }

    /** Runs the AI pipeline on the owning server tick. */
    @Override
    public void tick() {
        if (entity == null) {
            return;
        }

        collectSensorData(entity);
        evaluateCoreBehaviors(entity);
        evaluateBehaviors(entity);
        tickRunningCoreBehaviors(entity);
        tickRunningBehaviors(entity);
        updateRoute(entity);
        applyController(entity);
    }

    protected void initPeriodCounters() {
        sensorArray = sensors.toArray(new Sensor[0]);
        coreBehaviorArray = coreBehaviors.toArray(new Behavior[0]);
        behaviorArray = behaviors.toArray(new Behavior[0]);
        controllerArray = controllers.toArray(new Controller[0]);

        sensorPeriodCounters = new int[sensorArray.length];
        coreBehaviorPeriodCounters = new int[coreBehaviorArray.length];
        behaviorPeriodCounters = new int[behaviorArray.length];

        coreBehaviorRunning = new boolean[coreBehaviorArray.length];
        behaviorRunning = new boolean[behaviorArray.length];
        runningCoreBehaviorIndexes = new int[coreBehaviorArray.length];
        runningCoreBehaviorCount = 0;
        runningBehaviorIndexes = new int[behaviorArray.length];
        runningBehaviorCount = 0;
        evaluatedBehaviorIndexes = new int[behaviorArray.length];
        evaluatedBehaviorCount = 0;
    }

    protected void collectSensorData(EntityIntelligent entity) {
        if (sensorPeriodCounters == null) return;
        for (int i = 0; i < sensorArray.length; i++) {
            Sensor sensor = sensorArray[i];
            int counter = sensorPeriodCounters[i] + 1;
            if (counter >= sensor.getPeriod()) {
                sensor.sense(entity);
                counter = 0;
            }
            sensorPeriodCounters[i] = counter;
        }
    }

    /**
     * Core behaviors are independent of priority — any that evaluate to true
     * will start running alongside others. They are never interrupted by priority.
     */
    protected void evaluateCoreBehaviors(EntityIntelligent entity) {
        if (coreBehaviorPeriodCounters == null) return;
        for (int i = 0; i < coreBehaviorArray.length; i++) {
            // Skip already running core behaviors
            if (coreBehaviorRunning[i]) continue;
            Behavior behavior = coreBehaviorArray[i];
            int counter = coreBehaviorPeriodCounters[i] + 1;
            coreBehaviorPeriodCounters[i] = counter;
            if (counter < behavior.getPeriod()) continue;
            coreBehaviorPeriodCounters[i] = 0;
            if (behavior.evaluate(entity)) {
                behavior.onStart(entity);
                behavior.setBehaviorState(BehaviorState.ACTIVE);
                coreBehaviorRunning[i] = true;
                runningCoreBehaviorIndexes[runningCoreBehaviorCount++] = i;
            }
        }
    }

    /**
     * Normal behaviors use priority-based selection. Higher priority number = higher priority.
     * Only the highest-priority evaluated behaviors are considered.
     * A running behavior is only interrupted by a strictly higher priority.
     */
    protected void evaluateBehaviors(EntityIntelligent entity) {
        if (behaviorPeriodCounters == null) return;
        evaluatedBehaviorCount = 0;
        int highestPriority = Integer.MIN_VALUE;
        for (int i = 0; i < behaviorArray.length; i++) {
            // Skip already running behaviors
            if (behaviorRunning[i]) continue;
            Behavior behavior = behaviorArray[i];
            int counter = behaviorPeriodCounters[i] + 1;
            behaviorPeriodCounters[i] = counter;
            if (counter < behavior.getPeriod()) continue;
            behaviorPeriodCounters[i] = 0;
            if (behavior.evaluate(entity)) {
                if (behavior.getPriority() > highestPriority) {
                    evaluatedBehaviorCount = 0;
                    highestPriority = behavior.getPriority();
                } else if (behavior.getPriority() < highestPriority) {
                    continue;
                }
                evaluatedBehaviorIndexes[evaluatedBehaviorCount++] = i;
            }
        }
        if (evaluatedBehaviorCount == 0) return;

        int runningPriority = runningBehaviorCount == 0
                ? Integer.MIN_VALUE
                : behaviorArray[runningBehaviorIndexes[0]].getPriority();

        if (highestPriority < runningPriority) {
            // New behaviors have lower priority than running — do nothing
        } else if (highestPriority > runningPriority) {
            // New behaviors have higher priority — interrupt and replace
            interruptRunningBehaviors(entity);
            startEvaluatedBehaviors(entity);
        } else {
            // Same priority — add alongside
            startEvaluatedBehaviors(entity);
        }
    }

    protected void interruptRunningBehaviors(EntityIntelligent entity) {
        for (int i = 0; i < runningBehaviorCount; i++) {
            int behaviorIndex = runningBehaviorIndexes[i];
            Behavior behavior = behaviorArray[behaviorIndex];
            behavior.onInterrupt(entity);
            behavior.setBehaviorState(BehaviorState.STOP);
            behaviorRunning[behaviorIndex] = false;
        }
        runningBehaviorCount = 0;
    }

    private void startEvaluatedBehaviors(EntityIntelligent entity) {
        for (int i = 0; i < evaluatedBehaviorCount; i++) {
            int behaviorIndex = evaluatedBehaviorIndexes[i];
            Behavior behavior = behaviorArray[behaviorIndex];
            behavior.onStart(entity);
            behavior.setBehaviorState(BehaviorState.ACTIVE);
            behaviorRunning[behaviorIndex] = true;
            runningBehaviorIndexes[runningBehaviorCount++] = behaviorIndex;
        }
    }

    protected void tickRunningCoreBehaviors(EntityIntelligent entity) {
        if (runningCoreBehaviorIndexes == null) return;
        int i = 0;
        while (i < runningCoreBehaviorCount) {
            int behaviorIndex = runningCoreBehaviorIndexes[i];
            Behavior behavior = coreBehaviorArray[behaviorIndex];
            if (!behavior.execute(entity)) {
                behavior.onStop(entity);
                behavior.setBehaviorState(BehaviorState.STOP);
                coreBehaviorRunning[behaviorIndex] = false;
                removeRunningIndex(runningCoreBehaviorIndexes, i, --runningCoreBehaviorCount);
            } else {
                i++;
            }
        }
    }

    protected void tickRunningBehaviors(EntityIntelligent entity) {
        if (runningBehaviorIndexes == null) return;
        int i = 0;
        while (i < runningBehaviorCount) {
            int behaviorIndex = runningBehaviorIndexes[i];
            Behavior behavior = behaviorArray[behaviorIndex];
            if (!behavior.execute(entity)) {
                behavior.onStop(entity);
                behavior.setBehaviorState(BehaviorState.STOP);
                behaviorRunning[behaviorIndex] = false;
                removeRunningIndex(runningBehaviorIndexes, i, --runningBehaviorCount);
            } else {
                i++;
            }
        }
    }

    private static void removeRunningIndex(int[] indexes, int removedIndex, int newSize) {
        int moved = newSize - removedIndex;
        if (moved > 0) {
            System.arraycopy(indexes, removedIndex + 1, indexes, removedIndex, moved);
        }
    }

    protected void updateRoute(EntityIntelligent entity) {
        if (routeFinder == null) return;

        Vector3 moveTarget = memoryStorage.get(MemoryTypes.MOVE_TARGET);
        discardObsoleteRouteSearch(entity, moveTarget);
        applyCompletedRoute(entity);
        if (routeSearchCooldown > 0) {
            routeSearchCooldown--;
        }

        if (moveTarget == null) {
            route = null;
            nodeIndex = 0;
            routeSearchCooldown = 0;
            entity.setMoveDirectionStart(null);
            entity.setMoveDirectionEnd(null);
            return;
        }

        // Periodically force route recalculation so stale paths get refreshed.
        // The initial counter is staggered by entity ID to avoid synchronized
        // A* bursts from entities created during the same tick.
        if (hasNextNode()) {
            routeUpdateTick++;
            if (routeUpdateTick >= ROUTE_UPDATE_CYCLE) {
                routeUpdateTick = 0;
                routeUpdateRequired = true;
            }
        }

        if ((routeUpdateRequired || (!hasNextNode() && routeSearchCooldown == 0)) && pendingRoute == null) {
            routeUpdateRequired = false;
            scheduleRouteSearch(entity, moveTarget);
        }

        // Auto-advance waypoint when entity is close enough (Paper: followThePath)
        // This runs BEFORE controllers, so WalkController always has a valid ahead-target
        if (!entity.shouldUpdateMoveDirection() && entity.hasMoveDirection()) {
            var end = entity.getMoveDirectionEnd();
            if (end != null) {
                double dx = end.x - entity.x;
                double dy = end.y - entity.y;
                double dz = end.z - entity.z;
                double waypointDistSq = waypointDistanceSquared(
                        routeFinder != null && routeFinder.usesThreeDimensionalWaypoints(),
                        dx,
                        dy,
                        dz
                );
                // Paper: maxDistanceToWaypoint = bbWidth > 0.75 ? bbWidth / 2 : 0.75 - bbWidth / 2
                var aabb = entity.getAABB();
                double bbWidth = aabb.getMaxX() - aabb.getMinX();
                double maxDist = bbWidth > 0.75 ? bbWidth / 2.0 : 0.75 - bbWidth / 2.0;
                if (waypointDistSq < maxDist * maxDist) {
                    entity.setShouldUpdateMoveDirection(true);
                }
            }
        }

        // Consume next direction node when needed
        if (entity.shouldUpdateMoveDirection() || !entity.hasMoveDirection()) {
            if (hasNextNode()) {
                updateMoveDirection(entity);
                entity.setShouldUpdateMoveDirection(false);
            } else if (entity.shouldUpdateMoveDirection()) {
                entity.setMoveDirectionStart(null);
                entity.setMoveDirectionEnd(null);
                entity.setShouldUpdateMoveDirection(false);
            }
        }
    }

    static double waypointDistanceSquared(boolean threeDimensional, double dx, double dy, double dz) {
        double distanceSquared = dx * dx + dz * dz;
        return threeDimensional ? distanceSquared + dy * dy : distanceSquared;
    }

    private void scheduleRouteSearch(EntityIntelligent entity, Vector3 moveTarget) {
        Vector3 targetSnapshot = moveTarget.clone();
        CompletableFuture<List<Node>> routeResult = new CompletableFuture<>();
        Level requestLevel = entity.getLevel();
        long generation = ++this.routeRequestGeneration;
        this.pendingRoute = routeResult;
        this.pendingRouteTarget = targetSnapshot;
        this.pendingRouteLevel = requestLevel;
        this.pendingRouteGeneration = generation;

        requestLevel.scheduleEntityAsyncPrepare(entity.getId(), generation, () -> {
            if (routeResult.isDone()) {
                return;
            }
            if (entity.closed || entity.getLevel() != requestLevel) {
                routeResult.cancel(false);
                return;
            }
            try {
                routeResult.complete(routeFinder.search(entity, targetSnapshot));
            } catch (Throwable throwable) {
                routeResult.completeExceptionally(throwable);
            }
        });
    }

    private void discardObsoleteRouteSearch(EntityIntelligent entity, Vector3 moveTarget) {
        CompletableFuture<List<Node>> routeResult = this.pendingRoute;
        Vector3 requestedTarget = this.pendingRouteTarget;
        if (routeResult == null || Objects.equals(moveTarget, requestedTarget)) {
            return;
        }

        Level requestLevel = this.pendingRouteLevel;
        long generation = this.pendingRouteGeneration;
        if (requestLevel != null) {
            requestLevel.cancelEntityAsyncPrepare(entity.getId(), generation);
        }
        routeResult.cancel(false);
        clearPendingRoute();
        this.routeUpdateRequired = moveTarget != null;
    }

    private void applyCompletedRoute(EntityIntelligent entity) {
        CompletableFuture<List<Node>> routeResult = this.pendingRoute;
        if (routeResult == null || !routeResult.isDone()) {
            return;
        }

        Vector3 requestedTarget = this.pendingRouteTarget;
        Level requestedLevel = this.pendingRouteLevel;
        long generation = this.pendingRouteGeneration;
        clearPendingRoute();

        try {
            List<Node> calculatedRoute = routeResult.join();
            Vector3 currentTarget = memoryStorage.get(MemoryTypes.MOVE_TARGET);
            if (generation != this.routeRequestGeneration
                    || entity.closed
                    || entity.getLevel() != requestedLevel
                    || currentTarget == null
                    || requestedTarget == null
                    || !currentTarget.equals(requestedTarget)) {
                this.routeUpdateRequired = currentTarget != null;
                return;
            }

            this.route = calculatedRoute == null ? List.of() : calculatedRoute;
            this.nodeIndex = 0;
            this.routeSearchCooldown = this.route.isEmpty() ? ROUTE_UPDATE_CYCLE : 0;
        } catch (CancellationException ignored) {
            this.routeUpdateRequired = memoryStorage.get(MemoryTypes.MOVE_TARGET) != null;
            this.routeSearchCooldown = 0;
        } catch (CompletionException exception) {
            Vector3 currentTarget = memoryStorage.get(MemoryTypes.MOVE_TARGET);
            this.routeUpdateRequired = currentTarget != null
                    && requestedTarget != null
                    && !currentTarget.equals(requestedTarget);
            this.routeSearchCooldown = ROUTE_UPDATE_CYCLE;
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            Server.getInstance().getLogger().error("Could not calculate a route for entity " + entity.getId(), cause);
        }
    }

    private void clearPendingRoute() {
        this.pendingRoute = null;
        this.pendingRouteTarget = null;
        this.pendingRouteLevel = null;
        this.pendingRouteGeneration = 0;
    }

    static int initialRouteUpdateTick(long entityId) {
        return Math.floorMod(entityId, ROUTE_UPDATE_CYCLE);
    }

    protected boolean hasNextNode() {
        return route != null && nodeIndex < route.size();
    }

    protected Node nextNode() {
        if (!hasNextNode()) return null;
        return route.get(nodeIndex++);
    }

    protected void updateMoveDirection(EntityIntelligent entity) {
        var end = entity.getMoveDirectionEnd();
        var next = nextNode();
        if (next != null) {
            entity.setMoveDirectionStart(end != null ? end : new Vector3(entity.x, entity.y, entity.z));
            entity.setMoveDirectionEnd(next.getVector());
        }
    }

    protected void applyController(EntityIntelligent entity) {
        Controller[] activeControllers = controllerArray;
        if (activeControllers == null) return;
        for (int i = 0; i < activeControllers.length; i++) {
            activeControllers[i].control(entity);
        }
    }
}
