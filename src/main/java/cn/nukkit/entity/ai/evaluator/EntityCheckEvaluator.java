package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;
import cn.nukkit.entity.ai.memory.MemoryType;

public class EntityCheckEvaluator implements BehaviorEvaluator {
    private final MemoryType<? extends Entity> memoryType;

    public EntityCheckEvaluator(MemoryType<? extends Entity> type) {
        this.memoryType = type;
    }
    @Override
    public boolean evaluate(EntityIntelligent entity) {
        Entity target = entity.getMemoryStorage().get(memoryType);
        if (target == null
                || target.closed
                || !target.isAlive()
                || target.getLevel() != entity.getLevel()) {
            if (target != null) {
                entity.getMemoryStorage().clear(memoryType);
            }
            return false;
        }
        if (target instanceof Player player) {
            if (!player.spawned || !player.isOnline()) {
                entity.getMemoryStorage().clear(memoryType);
                return false;
            }
            return !player.isSpectator()
                    && (player.isSurvival() || player.isAdventure());
        }
        return true;
    }
}
