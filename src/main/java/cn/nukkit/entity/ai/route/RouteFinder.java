package cn.nukkit.entity.ai.route;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

import java.util.List;

/**
 * Finds a route from the entity's current position to a target position.
 *
 * @author daoge_cmd
 */
public interface RouteFinder {

    /**
     * Search for a route from the entity's position to the target.
     *
     * @param entity the entity
     * @param target the target position
     *
     * @return the list of route nodes (empty if no path found)
     */
    List<Node> search(EntityIntelligent entity, Vector3 target);
}
