package cn.nukkit.entity.ai.route.posevaluator;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

/**
 * Evaluates 3D positions during pathfinding to determine their suitability for movement.
 *
 * @author daoge_cmd
 */
@FunctionalInterface
public interface SpacePosEvaluator {

    /**
     * Evaluate a 3D position for movement suitability (e.g., flying or swimming).
     *
     * @param entity the entity
     * @param pos    the position to evaluate
     * @return {@code true} if the position is suitable
     */
    boolean evaluate(EntityIntelligent entity, Vector3 pos);

    /**
     * Primitive-coordinate variant used by the built-in route finders. The
     * default implementation preserves source and binary compatibility for
     * existing evaluators and lambdas; allocation-sensitive implementations
     * can override it directly.
     *
     * @param entity the entity
     * @param x      entity center X at the evaluated node
     * @param y      entity feet Y at the evaluated node
     * @param z      entity center Z at the evaluated node
     * @return {@code true} if the position is suitable
     */
    default boolean evaluate(EntityIntelligent entity, double x, double y, double z) {
        return evaluate(entity, new Vector3(x, y, z));
    }
}
