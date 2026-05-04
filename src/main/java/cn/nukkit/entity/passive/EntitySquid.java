package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemInkSac;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.utils.Utils;

public class EntitySquid extends EntityCreature {

    public static final int NETWORK_ID = 17;

    public EntitySquid(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.95f;
    }

    @Override
    public float getHeight() {
        return 0.95f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(10);
        super.initEntity();
    }

    @Override
    public Item[] getDrops() {
        ItemInkSac item = new ItemInkSac();
        item.setCount(Utils.rand(1, 3));
        return new Item[]{item};
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        boolean att =  super.attack(source);
        if (source.isCancelled()) {
            return att;
        }

        EntityEventPacket pk = new EntityEventPacket();
        pk.eid = this.getId();
        pk.event = EntityEventPacket.SQUID_INK_CLOUD;
        this.level.addChunkPacket(this.getChunkX() >> 4, this.getChunkZ() >> 4, pk);
        return att;
    }
}
