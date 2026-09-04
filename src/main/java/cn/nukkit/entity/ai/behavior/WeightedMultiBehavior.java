package cn.nukkit.entity.ai.behavior;

import lombok.Getter;
import cn.nukkit.entity.ai.behavior.Behavior;
import cn.nukkit.entity.EntityIntelligent;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A composite behavior that uses weighted random selection among the highest-priority
 * children that evaluate to {@code true}.
 *
 * @author daoge_cmd
 */
@Getter
public class WeightedMultiBehavior extends AbstractBehavior {

    protected final Set<Behavior> behaviors;
    protected final int priority;
    protected final int weight;
    protected final int period;

    protected Behavior activeBehavior;

    public WeightedMultiBehavior(Set<Behavior> behaviors, int priority, int weight, int period) {
        this.behaviors = Collections.unmodifiableSet(new LinkedHashSet<>(behaviors));
        this.priority = priority;
        this.weight = weight;
        this.period = period;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        // Evaluate each child exactly once. Some evaluators are probabilistic or
        // stateful, so repeating evaluation can produce an inconsistent selection.
        int highestPriority = Integer.MIN_VALUE;
        var successful = new ArrayList<Behavior>();
        for (var behavior : behaviors) {
            if (!behavior.evaluate(entity)) {
                continue;
            }
            if (behavior.getPriority() > highestPriority) {
                successful.clear();
                highestPriority = behavior.getPriority();
            }
            if (behavior.getPriority() == highestPriority) {
                successful.add(behavior);
            }
        }
        if (successful.isEmpty()) {
            return false;
        }

        int totalWeight = successful.stream().mapToInt(Behavior::getWeight).sum();
        if (totalWeight <= 0) return false;

        // Weighted random selection
        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;
        for (var behavior : successful) {
            currentWeight += behavior.getWeight();
            if (randomWeight < currentWeight) {
                activeBehavior = behavior;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        if (activeBehavior != null) {
            return activeBehavior.execute(entity);
        }
        return false;
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        if (activeBehavior != null) {
            activeBehavior.onInterrupt(entity);
            activeBehavior = null;
        }
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        if (activeBehavior != null) {
            activeBehavior.onStart(entity);
        }
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        if (activeBehavior != null) {
            activeBehavior.onStop(entity);
            activeBehavior = null;
        }
    }

    public static class Builder {
        private final Set<Behavior> behaviors = new LinkedHashSet<>();
        private int priority;
        private int weight = 1;
        private int period = 1;

        public Builder behavior(Behavior behavior) {
            this.behaviors.add(behavior);
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder period(int period) {
            this.period = period;
            return this;
        }

        public WeightedMultiBehavior build() {
            return new WeightedMultiBehavior(behaviors, priority, weight, period);
        }
    }
}
