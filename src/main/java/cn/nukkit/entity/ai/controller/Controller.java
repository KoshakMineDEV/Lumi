package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.EntityIntelligent;

/**
 * A controller applies movement and rotation changes to an entity based on memory data.
 *
 * @author daoge_cmd
 */
@FunctionalInterface
public interface Controller {

    /**
     * Apply control logic to the given entity.
     *
     * @param entity the entity to control
     * @return {@code true} if control was applied
     */
    boolean control(EntityIntelligent entity);
}
