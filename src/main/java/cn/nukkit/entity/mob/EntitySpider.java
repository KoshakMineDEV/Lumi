package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityArthropod;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntitySpider extends EntityCreature implements EntityArthropod {

    public static final int NETWORK_ID = 35;

    public EntitySpider(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 1.4f;
    }

    @Override
    public float getHeight() {
        return 0.9f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(16);
        super.initEntity();
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(ItemNamespaceId.STRING, 0, Utils.rand(0, 2)));
        drops.add(Item.get(Item.SPIDER_EYE, 0, Utils.rand(0, 2) == 0 ? 1 : 0));

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public boolean canBeAffected(EffectType type) {
        if (type == EffectType.POISON) {
            return false;
        }
        return super.canBeAffected(type);
    }
}
