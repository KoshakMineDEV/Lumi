package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erik Miller | EinBexiii
 */
public class EntityStrider extends EntityCreature {

    public final static int NETWORK_ID = 125;

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    public EntityStrider(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(20);

        super.initEntity();
        this.fireProof = true;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 1.7f;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(ItemNamespaceId.STRING, 0, Utils.rand(2, 5)));

        return drops.toArray(Item.EMPTY_ARRAY);
    }
}
