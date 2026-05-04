package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.effect.EffectType;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemSwordStone;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityWitherSkeleton extends EntityCreature {

    public static final int NETWORK_ID = 48;

    public EntityWitherSkeleton(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
        this.fireProof = true;
    }

    @Override
    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 2.4f;
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        MobEquipmentPacket pk = new MobEquipmentPacket();
        pk.eid = this.getId();
        pk.item = new ItemSwordStone();
        pk.hotbarSlot = 0;
        player.dataPacket(pk);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(ItemNamespaceId.BONE, 0, Utils.rand(0, 2)));

        if (Utils.rand(1, 3) == 1) {
            drops.add(Item.get(ItemNamespaceId.COAL, 0, 1));
        }

        if (Utils.rand(1, 40) == 1) {
            drops.add(Item.get(255 - Block.WITHER_SKELETON_SKULL, 0, 1));
        }

        if (Utils.rand(1, 200) <= 17) {
            drops.add(Item.get(Item.STONE_SWORD, Utils.rand(0, 131), 1));
        }

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Wither Skeleton";
    }

    @Override
    public boolean canBeAffected(EffectType type) {
        if (type == EffectType.WITHER) {
            return false;
        }
        return super.canBeAffected(type);
    }
}
