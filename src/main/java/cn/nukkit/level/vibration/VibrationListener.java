package cn.nukkit.level.vibration;

import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector3;

/**
 * Vibration listener.
 */
public interface VibrationListener {

    /**
     * Returns the position of the vibration listener.
     *
     * @return Vector3
     */
    Vector3 getListenerVector();

    /**
     * Whether to respond to this vibration.
     * If responded to, a sound wave will be emitted from the source to the listener's position,
     * and the onVibrationArrive() method will be called upon arrival.
     * Note: If this method is called, the sound wave is guaranteed to reach the listener.
     *
     * @param event The vibration event
     * @return boolean
     */
    boolean onVibrationOccur(VibrationEvent event);


    /**
     * Called when sound event is received
     *
     * @param event VibrationEvent
     */
    void onVibrationArrive(VibrationEvent event);

    /**
     * Return vibration monitoring radius.
     *
     * @return Vibration monitoring radius
     */
    double getListenRange();

    /**
     * Is it an entity?
     * If it is an entity, it will use the entity-specific nbt tag when sending sound wave particles.
     * If not, this listener will be treated as a block (e.g., sonar sensor).
     *
     * @return True if entity, otherwise false
     */
    default boolean isEntity() {
        return this instanceof Entity;
    }

    /**
     * If isEntity() is true, return the entity object corresponding to this vibration listener.
     *
     * @return The entity
     */
    default Entity asEntity() {
        return (Entity) this;
    }
}
