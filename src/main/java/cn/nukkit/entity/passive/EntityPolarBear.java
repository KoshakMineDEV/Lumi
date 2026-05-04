package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityPolarBear extends EntityCreature {

    public static final int NETWORK_ID = 28;

    public EntityPolarBear(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 1.3f;
    }

    @Override
    public float getHeight() {
        return 1.4f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(30);

        super.initEntity();
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(Item.RAW_FISH, 0, Utils.rand(0, 2)));
        drops.add(Item.get(Item.RAW_SALMON, 0, Utils.rand(0, 2)));

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Polar Bear";
    }
}
