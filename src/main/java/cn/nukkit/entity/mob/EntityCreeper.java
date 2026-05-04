package cn.nukkit.entity.mob;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityCreeper extends EntityCreature {

    public static final int NETWORK_ID = 33;

    public EntityCreeper(FullChunk chunk, CompoundTag nbt) {
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
        return 1.7f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);

        super.initEntity();

        if (this.namedTag.contains("powered")) {
            this.setPowered(this.namedTag.getBoolean("powered"));
        }
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(ItemNamespaceId.GUNPOWDER, 0, Utils.rand(0, 2)));

        if (this.lastDamageCause instanceof EntityDamageByEntityEvent) {
            Entity killer = ((EntityDamageByEntityEvent) this.lastDamageCause).getDamager();

            if (killer instanceof EntityCreeper creeper) {
                if(creeper.isPowered()) {
                    drops.add(Item.get(255 - Block.CREEPER_HEAD, 0, 1));
                }
            }
        }

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    public boolean isPowered() {
        return this.getDataFlag(DATA_FLAGS, DATA_FLAG_POWERED);
    }

    public void setPowered(boolean charged) {
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_POWERED, charged);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean("powered", this.isPowered());
    }

    @Override
    public void onStruckByLightning(Entity lightning) {
        if (this.attack(new EntityDamageByEntityEvent(lightning, this, EntityDamageEvent.DamageCause.LIGHTNING, 5))) {
            if (this.fireTicks < 160) {
                this.setOnFire(8);
            }
            this.setPowered(true);
        }
    }
}
