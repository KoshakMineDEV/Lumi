package cn.nukkit.entity.effect;

import cn.nukkit.entity.Entity;

import java.awt.*;

public class EffectHealthBoost extends Effect {

    public EffectHealthBoost() {
        super(EffectType.HEALTH_BOOST, "%potion.healthBoost", new Color(248, 125, 35));
    }

    public void onAdd(Entity entity) {
        entity.setMaxHealth(entity.getRealMaxHealth());
    }

    public void onRemove(Entity entity) {
        entity.setMaxHealth(entity.getRealMaxHealth());

        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
}
