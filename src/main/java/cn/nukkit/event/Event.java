package cn.nukkit.event;

import cn.nukkit.Server;
import lombok.Getter;
import org.densy.eventbus.api.CallResult;
import org.densy.eventbus.api.EventBus;
import org.densy.eventbus.api.exception.EventException;

/**
 * Base class for all server events.
 */
public abstract class Event implements org.densy.eventbus.api.Event {

    @Getter
    private boolean cancelled = false;

    /**
     * Dispatches this event through the server event bus.
     *
     * @return {@code true} if the event was not cancelled after handling, otherwise {@code false}
     */
    public boolean call() {
        return call(Server.getInstance().getEventBus());
    }

    /**
     * Dispatches this event through the given event bus.
     *
     * @param eventBus event bus to use
     * @return {@code true} if the event was not cancelled after handling, otherwise {@code false}
     */
    public boolean call(EventBus eventBus) {
        CallResult result = eventBus.callParents(this);

        for(Throwable ex : result.getExceptions().values()) {
            Server.getInstance().getLogger().logException(ex);
        }

        return !this.cancelled;
    }

    /**
     * Marks this event as cancelled or not cancelled.
     *
     * @param value {@code true} to cancel the event
     * @throws EventException if the event does not support cancellation
     */
    public void setCancelled(boolean value) {
        if (!(this instanceof Cancellable)) {
            throw new EventException("Event is not Cancellable");
        }
        this.cancelled = value;
    }

    /**
     * Cancels this event.
     *
     * @throws EventException if the event does not support cancellation
     */
    public void setCancelled() {
        this.setCancelled(true);
    }

    /**
     * Cancels this event.
     *
     * @throws EventException if the event does not support cancellation
     */
    public void cancel() {
        this.setCancelled(true);
    }
}
