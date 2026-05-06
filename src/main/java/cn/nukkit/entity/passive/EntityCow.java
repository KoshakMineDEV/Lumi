package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityCow extends EntityCreature implements EntityClimateVariant {
    public static final int NETWORK_ID = 11;

    public EntityCow(FullChunk chunk, CompoundTag nbt) {
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

        if (namedTag.contains("variant")) {
            setVariant(Variant.get(namedTag.getString("variant")));
        } else {
            setVariant(getBiomeVariant(getLevel().getBiomeId(getFloorX(), getFloorZ())));
        }
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
        drops.add(Item.get(this.isOnFire() ? Item.STEAK : Item.RAW_BEEF, 0, Utils.rand(1, 3)));

        return drops.toArray(Item.EMPTY_ARRAY);
    }
}
