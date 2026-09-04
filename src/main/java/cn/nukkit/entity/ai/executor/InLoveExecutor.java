package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.level.particle.HeartParticle;


/**
 * Sets the entity in love mode for a specified duration and sends
 * love particles to viewers periodically.
 *
 * @author daoge_cmd
 */
public class InLoveExecutor implements BehaviorExecutor {

    protected final int duration;
    protected int tickCounter;

    public InLoveExecutor(int duration) {
        this.duration = duration;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        tickCounter = 0;
        entity.getMemoryStorage().put(MemoryTypes.IS_IN_LOVE, true);
        entity.getMemoryStorage().put(MemoryTypes.LAST_IN_LOVE_TIME, entity.getTick());
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        tickCounter++;
        if (tickCounter > duration || !entity.getMemoryStorage().get(MemoryTypes.IS_IN_LOVE)) {
            return false;
        }

        // Send love particles every 10 ticks
        if (tickCounter % 10 == 0) {
            entity.getLevel().addParticle(new HeartParticle(entity.getLocation().add(0, entity.getEyeHeight(), 0)));
        }

        return true;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        entity.getMemoryStorage().put(MemoryTypes.IS_IN_LOVE, false);
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        entity.getMemoryStorage().put(MemoryTypes.IS_IN_LOVE, false);
    }
}
