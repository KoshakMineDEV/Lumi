package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityWarden extends EntityCreature {
    public static final int NETWORK_ID = 131;

    public EntityWarden(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getHeight() {
        return 2.9f;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(500);
        super.initEntity();
    }

    @Override
    public String getName() {
        return "Warden";
    }
}