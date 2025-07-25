package cn.nukkit.event.potion;

import cn.nukkit.entity.effect.PotionType;
import cn.nukkit.entity.item.EntityPotionSplash;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * @author Snake1999
 * @since 2016/1/12
 */
public class PotionCollideEvent extends PotionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final EntityPotionSplash thrownPotion;

    public PotionCollideEvent(PotionType potion, EntityPotionSplash thrownPotion) {
        super(potion);
        this.thrownPotion = thrownPotion;
    }

    public EntityPotionSplash getThrownPotion() {
        return thrownPotion;
    }
}
