package cn.nukkit.entity.effect;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;

import java.awt.*;

public class EffectPoison extends Effect {

    public EffectPoison() {
        super(EffectType.POISON, "%potion.poison", new Color(135, 163, 99), true);
    }

    @Override
    public boolean canTick() {
        int interval = 25 >> this.getAmplifier();
        return interval > 0 && this.getDuration() % interval == 0;
    }

    @Override
    public void onApply(Entity entity, double tickCount) {
        if(entity instanceof EntitySmite) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(new EntityRegainHealthEvent(entity, 1, EntityRegainHealthEvent.CAUSE_MAGIC));
            }
        } else {
            if (entity.getHealth() > 1) {
                entity.attack(new EntityDamageEvent(entity, EntityDamageEvent.DamageCause.MAGIC, 1));
            }
        }
    }
}
