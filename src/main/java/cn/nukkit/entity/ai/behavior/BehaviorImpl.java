package cn.nukkit.entity.ai.behavior;

import lombok.Builder;
import lombok.Getter;
import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;
import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.EntityIntelligent;

/**
 * A simple behavior that delegates evaluation and execution to separate
 * {@link BehaviorEvaluator} and {@link BehaviorExecutor} instances.
 *
 * @author daoge_cmd
 */
@Getter
@Builder
public class BehaviorImpl extends AbstractBehavior {

    protected BehaviorExecutor executor;
    protected BehaviorEvaluator evaluator;
    @Builder.Default
    protected int priority = 0;
    @Builder.Default
    protected int weight = 1;
    @Builder.Default
    protected int period = 1;

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        return evaluator.evaluate(entity);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        return executor.execute(entity);
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        executor.onInterrupt(entity);
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        executor.onStart(entity);
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        executor.onStop(entity);
    }
}
