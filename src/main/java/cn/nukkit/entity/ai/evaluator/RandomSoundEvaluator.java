package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.EntityIntelligent;

import java.util.concurrent.ThreadLocalRandom;

public class RandomSoundEvaluator extends AllMatchEvaluator {

    private int value, range;

    public RandomSoundEvaluator() {
        this(160, 320);
    }

    public RandomSoundEvaluator(int value, int range) {
        this.value = value;
        this.range = range;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
       return entity.getLevel().getCurrentTick() % (value + ThreadLocalRandom.current().nextInt(range)) == 0;
    }
}