package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

public class EntityMagmaCube extends EntityCreature {

    public static final int NETWORK_ID = 42;

    private int size = 1;

    public EntityMagmaCube(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getHeight() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getLength() {
        return 0.51f + size * 0.51f;
    }

    @Override
    protected void initEntity() {
        if (this.namedTag.contains("Size")) {
            this.size = this.namedTag.getInt("Size");
        } else {
            this.size = Utils.rand(1, 3);
        }

        this.setScale(0.51f + size * 0.51f);
        this.setMaxHealth(1);
        super.initEntity();

        this.fireProof = true;
        this.noFallDamage = true;

        this.setScale(0.51f + size * 0.51f);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, 1))};
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Magma Cube";
    }
}
