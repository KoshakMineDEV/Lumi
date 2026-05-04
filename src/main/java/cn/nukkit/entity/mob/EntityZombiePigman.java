package cn.nukkit.entity.mob;

import cn.nukkit.Difficulty;
import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemSwordGold;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityZombiePigman extends EntityCreature {

    public static final int NETWORK_ID = 36;

    private int angry = 0;

    public EntityZombiePigman(FullChunk chunk, CompoundTag nbt) {
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

        if (this.namedTag.contains("Angry")) {
            this.angry = this.namedTag.getInt("Angry");
        }

        this.fireProof = true;
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        MobEquipmentPacket pk = new MobEquipmentPacket();
        pk.eid = this.getId();
        pk.item = new ItemSwordGold();
        pk.inventorySlot = 0;
        player.dataPacket(pk);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(0, 1)));
        drops.add(Item.get(Item.GOLD_NUGGET, 0, Utils.rand(0, 1)));
        drops.add(Item.get(Item.GOLD_SWORD, Utils.rand(20, 30), Utils.rand(0, 101) <= 9 ? 1 : 0));

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Zombified Piglin";
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (this.server.getDifficulty() == Difficulty.PEACEFUL) {
            this.close();
            return true;
        }

        if (this.angry > 0) {
            this.angry--;
        }

        return super.entityBaseTick(tickDiff);
    }
}
