package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erik Miller | EinBexiii
 */
public class EntityHoglin extends EntityCreature {

    public final static int NETWORK_ID = 124;

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    public EntityHoglin(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }


    @Override
    protected void initEntity() {
        this.setMaxHealth(40);
        super.initEntity();
    }

    @Override
    public float getWidth() {
        return 1.3965f;
    }

    @Override
    public float getHeight() {
        return 1.4f;
    }


    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        for (int i = 0; i < Utils.rand(2, 4); i++) {
            drops.add(Item.get(this.isOnFire() ? Item.COOKED_PORKCHOP : Item.RAW_PORKCHOP, 0, 1));
        }

        if (Utils.rand()) {
            drops.add(Item.get(Item.LEATHER));
        }

        return drops.toArray(Item.EMPTY_ARRAY);
    }
}
