package cn.nukkit.entity.passive;

import cn.nukkit.Server;
import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityChicken extends EntityCreature implements EntityClimateVariant {

    public static final int NETWORK_ID = 10;

    public EntityChicken(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.4f;
    }

    @Override
    public float getHeight() {
        return 0.7f;
    }

    @Override
    public float getDrag() {
        return 0.2f;
    }

    @Override
    public float getGravity() {
        //Should be lower but that breaks jumping
        return 0.08f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(4);
        super.initEntity();

        if (namedTag.contains("variant")) {
            setVariant(Variant.get(namedTag.getString("variant")));
        } else {
            setVariant(getBiomeVariant(getLevel().getBiomeId(getFloorX(), getFloorZ())));
        }

        this.noFallDamage = true;
    }

    private Item getEgg() {
        if (Server.getInstance().getSettings().features().enableNewChickenEggsLaying()) {
            if (getVariant() == Variant.COLD) return Item.get(ItemNamespaceId.BLUE_EGG);
            if (getVariant() == Variant.WARM) return Item.get(ItemNamespaceId.BROWN_EGG);
        }
        return Item.get(Item.EGG, 0, 1);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(ItemNamespaceId.FEATHER, 0, Utils.rand(0, 2)));
        drops.add(Item.get(this.isOnFire() ? Item.COOKED_CHICKEN : Item.RAW_CHICKEN, 0, 1));

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        if (ev.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return super.attack(ev);
        }

        return false;
    }
}
