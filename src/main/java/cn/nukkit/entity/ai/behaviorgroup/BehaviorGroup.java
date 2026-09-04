package cn.nukkit.entity.ai.behaviorgroup;

import cn.nukkit.entity.ai.behavior.Behavior;
import cn.nukkit.entity.ai.controller.Controller;
import cn.nukkit.entity.ai.memory.MemoryStorage;
import cn.nukkit.entity.ai.route.RouteFinder;
import cn.nukkit.entity.ai.sensor.Sensor;
import cn.nukkit.entity.EntityIntelligent;

import java.util.Set;

/**
 * Orchestrates the AI tick pipeline: sensors, evaluators, executors,
 * route finding, and controllers.
 *
 * @author daoge_cmd
 */
public interface BehaviorGroup {

    /** Assign this behavior group to its owning entity. */
    void setEntity(EntityIntelligent entity);

    /** Run one complete AI cycle on the server tick thread. */
    void tick();

    /**
     * Get all core behaviors.
     *
     * @return the set of core behaviors
     */
    Set<Behavior> getCoreBehaviors();

    /**
     * Get all non-core behaviors.
     *
     * @return the set of behaviors
     */
    Set<Behavior> getBehaviors();

    /**
     * Get all sensors.
     *
     * @return the set of sensors
     */
    Set<Sensor> getSensors();

    /**
     * Get all controllers.
     *
     * @return the set of controllers
     */
    Set<Controller> getControllers();

    /**
     * Get the route finder.
     *
     * @return the route finder, or {@code null} if none
     */
    RouteFinder getRouteFinder();

    /**
     * Get the memory storage.
     *
     * @return the memory storage
     */
    MemoryStorage getMemoryStorage();

    /**
     * Check if the route needs to be updated on the next tick.
     *
     * @return {@code true} if a route update is needed
     */
    boolean isRouteUpdateRequired();

    /**
     * Set whether the route needs to be updated on the next tick.
     *
     * @param routeUpdateRequired whether a route update is needed
     */
    void setRouteUpdateRequired(boolean routeUpdateRequired);

}
