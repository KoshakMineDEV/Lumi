package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

public class EntityCat extends EntityCreature {

    public static final int NETWORK_ID = 75;

    public EntityCat(FullChunk chunk, CompoundTag nbt) {
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
        return 0.7f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(10);
        super.initEntity();
        this.noFallDamage = true;
    }

    @Override
    public Item[] getDrops() {
        int c = Utils.rand(0, 2);
        if (c > 0) {
            return new Item[]{Item.get(ItemNamespaceId.STRING, 0, c)};
        }

        return Item.EMPTY_ARRAY;
    }

}
