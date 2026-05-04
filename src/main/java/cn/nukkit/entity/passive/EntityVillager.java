package cn.nukkit.entity.passive;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.entity.data.profession.Profession;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.TradeInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import lombok.Getter;

public class EntityVillager extends EntityCreature implements InventoryHolder {
    public EntityVillager(FullChunk chunk, CompoundTag nbt) {
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
        return 1.9f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
        this.setProfession(0);
    }

    public void setProfession(int profession) {
        this.setDataProperty(new IntEntityData(DATA_VARIANT, profession));
        this.namedTag.putInt("profession", profession);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
