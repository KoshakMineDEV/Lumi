package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.EntityIntelligent;

/**
 * Checks if a specific memory type has a value stored (is not empty).
 *
 * @author daoge_cmd
 */
public class MemoryCheckNotEmptyEvaluator implements BehaviorEvaluator {

    protected final MemoryType<?> type;

    public MemoryCheckNotEmptyEvaluator(MemoryType<?> type) {
        this.type = type;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        return entity.getMemoryStorage().notEmpty(type);
    }
}
