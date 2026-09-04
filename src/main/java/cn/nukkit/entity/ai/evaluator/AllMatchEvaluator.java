package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;
import cn.nukkit.entity.EntityIntelligent;

/**
 * AND-logic composite evaluator. Returns {@code true} only if all
 * inner evaluators return {@code true}.
 *
 * @author daoge_cmd
 */
public class AllMatchEvaluator implements BehaviorEvaluator {

    protected final BehaviorEvaluator[] evaluators;

    public AllMatchEvaluator(BehaviorEvaluator... evaluators) {
        this.evaluators = evaluators;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        for (var evaluator : evaluators) {
            if (!evaluator.evaluate(entity)) {
                return false;
            }
        }
        return true;
    }
}
