package cn.nukkit.entity.passive;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityEffectUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

public class EntityPufferfish extends EntityFish {

    public static final int NETWORK_ID = 108;

    protected int puffed = 0;

    public EntityPufferfish(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected int getBucketMeta() {
        return 5;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.35f;
    }

    @Override
    public float getHeight() {
        return 0.35f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(3);

        super.initEntity();
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.PUFFERFISH, 0, 1), Item.get(Item.BONE, 0, Utils.rand(0, 2))};
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);

        if (ev.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && ev instanceof EntityDamageByEntityEvent event) {
            Entity damager = event.getDamager();
            if (damager instanceof Player) {
                if (this.puffed > 0) {
                    return true;
                }
                this.puffed = 200;
                damager.addEffect(Effect.get(EffectType.POISON).setDuration(200), EntityEffectUpdateEvent.Cause.ATTACK);
                this.setDataProperty(new ByteEntityData(DATA_PUFFERFISH_SIZE, 2));
            }
        }

        return true;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (puffed == 0) {
            if (this.getDataPropertyByte(DATA_PUFFERFISH_SIZE) == 2) {
                this.setDataProperty(new ByteEntityData(DATA_PUFFERFISH_SIZE, 0));
            }
        }

        if (puffed > 0) {
            puffed--;
        }

        return super.entityBaseTick(tickDiff);
    }

    public boolean isPuffed() {
        return this.puffed > 0;
    }
}
