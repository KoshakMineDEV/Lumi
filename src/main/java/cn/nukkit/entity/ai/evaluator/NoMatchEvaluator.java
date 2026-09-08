package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;

/**
 * AND-logic composite evaluator. Returns {@code true} only if all
 * inner evaluators return {@code true}.
 *
 * @author daoge_cmd
 */
public class NoMatchEvaluator implements BehaviorEvaluator {

    protected final BehaviorEvaluator[] evaluators;

    public NoMatchEvaluator(BehaviorEvaluator... evaluators) {
        this.evaluators = evaluators;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        for (var evaluator : evaluators) {
            if (!evaluator.evaluate(entity)) {
                return true;
            }
        }
        return false;
    }
}
