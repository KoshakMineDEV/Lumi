package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityCaveSpider extends EntityCreature {

    public static final int NETWORK_ID = 40;

    public EntityCaveSpider(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 0.5f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(12);
        super.initEntity();
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Cave Spider";
    }

    @Override
    public boolean canBeAffected(EffectType type) {
        if (type == EffectType.POISON) {
            return false;
        }
        return super.canBeAffected(type);
    }
}
