package cn.nukkit.entity.passive;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityMooshroom extends EntityCreature {

    public static final int NETWORK_ID = 16;

    public EntityMooshroom(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 1.4f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(10);

        super.initEntity();

        if (this.namedTag.contains("Variant")) {
            this.setBrown(this.namedTag.getInt("Variant") == 1);
        }
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
        drops.add(Item.get(this.isOnFire() ? Item.STEAK : Item.RAW_BEEF, 0, Utils.rand(1, 3)));

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public void onStruckByLightning(Entity entity) {
        this.setBrown(!this.isBrown());
        super.onStruckByLightning(entity);
    }

    public boolean isBrown() {
        return this.getDataPropertyInt(DATA_VARIANT) == 1;
    }

    public void setBrown(boolean brown) {
        this.setDataProperty(new IntEntityData(DATA_VARIANT, brown ? 1 : 0));
    }
}
