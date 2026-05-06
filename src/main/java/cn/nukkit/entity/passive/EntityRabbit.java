package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityRabbit extends EntityCreature {

    public static final int NETWORK_ID = 18;

    public EntityRabbit(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.402f;
    }

    @Override
    public float getLength() {
        return 0.402f;
    }

    @Override
    public float getHeight() {
        return 0.402f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(3);
        super.initEntity();
        this.setScale(0.65f);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(Item.RABBIT_HIDE, 0, Utils.rand(0, 1)));
        drops.add(Item.get(this.isOnFire() ? Item.COOKED_RABBIT : Item.RAW_RABBIT, 0, Utils.rand(0, 1)));
        drops.add(Item.get(Item.RABBIT_FOOT, 0, Utils.rand(0, 101) <= 9 ? 1 : 0));

        return drops.toArray(Item.EMPTY_ARRAY);
    }
}
