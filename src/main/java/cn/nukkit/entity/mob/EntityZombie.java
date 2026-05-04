package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityZombie extends EntityCreature {

    public static final int NETWORK_ID = 32;

    public EntityZombie(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.95f;
    }

    @Override
    public boolean isUndead() {
        return true;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        for (int i = 0; i < Utils.rand(0, 2); i++) {
            drops.add(Item.get(Item.ROTTEN_FLESH, 0, 1));
        }

        if (Utils.rand(1, 3) == 1) {
            switch (Utils.rand(1, 3)) {
                case 1:
                    drops.add(Item.get(Item.IRON_INGOT, 0, Utils.rand(0, 1)));
                    break;
                case 2:
                    drops.add(Item.get(Item.CARROT, 0, Utils.rand(0, 1)));
                    break;
                case 3:
                    drops.add(Item.get(Item.POTATO, 0, Utils.rand(0, 1)));
                    break;
            }
        }

        return drops.toArray(Item.EMPTY_ARRAY);
    }
}
