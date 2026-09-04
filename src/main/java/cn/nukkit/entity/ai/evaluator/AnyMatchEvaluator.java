package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;
import cn.nukkit.entity.EntityIntelligent;

/**
 * OR-logic composite evaluator. Returns {@code true} if any
 * inner evaluator returns {@code true}.
 *
 * @author daoge_cmd
 */
public class AnyMatchEvaluator implements BehaviorEvaluator {

    protected final BehaviorEvaluator[] evaluators;

    public AnyMatchEvaluator(BehaviorEvaluator... evaluators) {
        this.evaluators = evaluators;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        for (var evaluator : evaluators) {
            if (evaluator.evaluate(entity)) {
                return true;
            }
        }
        return false;
    }
}
