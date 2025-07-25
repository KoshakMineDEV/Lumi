package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntitySmite;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityZombieHorse extends EntityHorseBase implements EntitySmite {

    public static final int NETWORK_ID = 27;

    public EntityZombieHorse(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.6982f;
        }
        return 1.3965f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.8f;
        }
        return 1.6f;
    }

    @Override
    public boolean isUndead() {
        return true;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(15);

        super.initEntity();
    }

    @Override
    public boolean isFeedItem(Item item) {
        return false;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
            drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(0, 2)));
        }

        if (this.isSaddled()) {
            drops.add(Item.get(Item.SADDLE, 0, 1));
        }

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public String getName() {
        return "Zombie Horse";
    }
}
