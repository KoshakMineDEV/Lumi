package cn.nukkit.entity.ai.memory;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Identifier;

/**
 * Core memory types used by the AI framework.
 *
 * @author daoge_cmd
 */
public final class MemoryTypes {

    /**
     * Entity look target position.
     */
    public static final MemoryType<Vector3> LOOK_TARGET =
            new MemoryType<>(new Identifier("minecraft:look_target"));

    /**
     * Entity move target position.
     */
    public static final MemoryType<Vector3> MOVE_TARGET =
            new MemoryType<>(new Identifier("minecraft:move_target"));

    /**
     * Entity movement direction start position.
     */
    public static final MemoryType<Vector3> MOVE_DIRECTION_START =
            new MemoryType<>(new Identifier("minecraft:move_direction_start"));

    /**
     * Entity movement direction end position.
     */
    public static final MemoryType<Vector3> MOVE_DIRECTION_END =
            new MemoryType<>(new Identifier("minecraft:move_direction_end"));

    /**
     * Whether the entity needs to update its move direction.
     */
    public static final MemoryType<Boolean> SHOULD_UPDATE_MOVE_DIRECTION =
            new MemoryType<>(new Identifier("minecraft:should_update_move_direction"), () -> false);

    /**
     * Whether pitch rotation is enabled for the entity.
     */
    public static final MemoryType<Boolean> ENABLE_PITCH =
            new MemoryType<>(new Identifier("minecraft:enable_pitch"), () -> true);

    /**
     * Entity movement speed.
     */
    public static final MemoryType<Float> MOVEMENT_SPEED =
            new MemoryType<>(new Identifier("minecraft:movement_speed"), () -> 0.1f);

    /**
     * Whether the lift force controller is enabled for the entity.
     */
    public static final MemoryType<Boolean> ENABLE_LIFT_FORCE =
            new MemoryType<>(new Identifier("minecraft:enable_lift_force"), () -> true);

    /**
     * Whether the dive force controller is enabled for the entity.
     */
    public static final MemoryType<Boolean> ENABLE_DIVE_FORCE =
            new MemoryType<>(new Identifier("minecraft:enable_dive_force"), () -> true);

    /**
     * The entity tick when this entity was last fed.
     */
    public static final MemoryType<Long> LAST_BE_FEED_TIME =
            new MemoryType<>(new Identifier("minecraft:last_be_feed_time"), () -> -1L);

    /**
     * The entity tick when this entity last entered love mode.
     */
    public static final MemoryType<Long> LAST_IN_LOVE_TIME =
            new MemoryType<>(new Identifier("minecraft:last_in_love_time"), () -> -1L);

    /**
     * Whether the entity is currently in love mode.
     */
    public static final MemoryType<Boolean> IS_IN_LOVE =
            new MemoryType<>(new Identifier("minecraft:is_in_love"), () -> false);

    /**
     * The entity tick when this entity was spawned.
     */
    public static final MemoryType<Long> ENTITY_SPAWN_TIME =
            new MemoryType<>(new Identifier("minecraft:entity_spawn_time"), () -> 0L);

    /**
     * Nearest player holding a breeding item.
     * Written by {@code NearestFeedingPlayerSensor}.
     */
    public static final MemoryType<Player> NEAREST_FEEDING_PLAYER =
            new MemoryType<>(new Identifier("minecraft:nearest_feeding_player"));

    /**
     * Nearest player.
     * Written by {@code NearestPlayerSensor}.
     */
    public static final MemoryType<Player> NEAREST_PLAYER =
            new MemoryType<>(new Identifier("minecraft:nearest_player"));

    /**
     * The last tick of the entity being attacked
     */
    public static final MemoryType<Long> LAST_BE_ATTACKED_TIME =
            new MemoryType<>(new Identifier("minecraft:last_be_attacked_time"), () -> -65536L);

    /**
     * Entity this mob is currently targeting.
     */
    public static final MemoryType<Entity> ATTACK_TARGET =
            new MemoryType<>(new Identifier("minecraft:attack_target"));

    /**
     * The entity tick when this entity last completed an attack.
     */
    public static final MemoryType<Long> LAST_ATTACK_TIME =
            new MemoryType<>(new Identifier("minecraft:last_attack_time"), () -> -65536L);

    /**
     * Entity most recently attacked by this entity.
     */
    public static final MemoryType<Entity> LAST_ATTACK_ENTITY =
            new MemoryType<>(new Identifier("minecraft:last_attack_entity"));

    /**
     * This entity's breeding spouse.
     */
    public static final MemoryType<Entity> ENTITY_SPOUSE =
            new MemoryType<>(new Identifier("minecraft:entity_spouse"));

    /**
     * Player who last fed this entity.
     */
    public static final MemoryType<Player> LAST_FEED_PLAYER =
            new MemoryType<>(new Identifier("minecraft:last_feed_player"));

    /**
     * The entity tick when this entity last spawned an egg.
     * Currently only used by chicken.
     */
    public static final MemoryType<Long> LAST_EGG_SPAWN_TIME =
            new MemoryType<>(new Identifier("minecraft:last_egg_spawn_time"), () -> 0L);

    private MemoryTypes() {
    }
}
