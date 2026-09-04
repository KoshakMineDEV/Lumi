package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.passive.EntitySheep;

/**
 * Extended grass eating executor for sheep that regrows wool after eating.
 *
 * @author daoge_cmd
 */
public class SheepEatGrassExecutor extends EatGrassExecutor {

    @Override
    protected void onEatGrass(EntityIntelligent entity) {
        super.onEatGrass(entity);
        //TODO
        /*if (entity instanceof EntitySheep sheep && sheep.isSheared()) {
            sheep.setSheared(false);
        }*/
    }
}
