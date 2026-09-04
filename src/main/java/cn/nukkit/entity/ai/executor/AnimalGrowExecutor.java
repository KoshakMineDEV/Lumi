package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityIntelligent;

/**
 * Simple executor that grows a baby animal into an adult.
 *
 * @author daoge_cmd
 */
public class AnimalGrowExecutor implements BehaviorExecutor {

    @Override
    public boolean execute(EntityIntelligent entity) {
        if (entity instanceof EntityAgeable ageable) {
            ageable.setBaby(false);
        }
        return false;
    }
}
