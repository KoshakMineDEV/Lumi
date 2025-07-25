package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;

/**
 * Created by Snake1999 on 2016/1/14.
 * Package cn.nukkit.item in project nukkit.
 */
public class ItemRottenFlesh extends ItemFood {

    public ItemRottenFlesh() {
        this(0, 1);
    }

    public ItemRottenFlesh(Integer meta) {
        this(meta, 1);
    }

    public ItemRottenFlesh(Integer meta, int count) {
        super(ROTTEN_FLESH, meta, count, "Rotten Flesh");
    }

    @Override
    public int getFoodRestore() {
        return 4;
    }

    @Override
    public float getSaturationRestore() {
        return 0.8F;
    }

    @Override
    public boolean onEaten(Player player) {
        if(0.8F >= Math.random()) {
            player.addEffect(Effect.get(EffectType.HUNGER).setDuration(30 * 20));
        }

        return true;
    }
}
