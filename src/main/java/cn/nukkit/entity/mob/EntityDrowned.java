package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityDrowned extends EntityCreature {

    public static final int NETWORK_ID = 110;

    protected Item tool;

    public EntityDrowned(FullChunk chunk, CompoundTag nbt) {
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
    public boolean isUndead() {
        return true;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();

        if (this.namedTag.contains("Item")) {
            this.tool = NBTIO.getItemHelper(this.namedTag.getCompound("Item"));
        } else if (!this.namedTag.getBoolean("HandItemSet")) {
            this.setRandomTool();
        }
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(Item.ROTTEN_FLESH, 0, Utils.rand(0, 2)));

        if (Utils.rand(1, 100) <= 11) {
            drops.add(Item.get(ItemNamespaceId.COPPER_INGOT));
        }

        if (tool != null && Utils.rand(1, 4) == 1) {
            drops.add(tool);
        } else if (tool == null && Utils.rand(1, 80) <= 3) {
            drops.add(Item.get(Item.TRIDENT, Utils.rand(230, 246), 1));
        }

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    private void setRandomTool() {
        switch (Utils.rand(1, 3)) {
            case 1:
                if (Utils.rand(1, 100) <= 15) {
                    this.tool = Item.get(Item.TRIDENT, Utils.rand(200, 246), 1);
                }
                return;
            case 2:
                if (Utils.rand(1, 100) == 1) {
                    this.tool = Item.get(Item.FISHING_ROD, Utils.rand(51, 61), 1);
                }
                return;
            case 3:
                if (Utils.rand(1, 100) <= 8) {
                    this.tool = Item.get(Item.NAUTILUS_SHELL, 0, 1);
                }
        }
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        if (this.tool != null) {
            MobEquipmentPacket pk = new MobEquipmentPacket();
            pk.eid = this.getId();
            pk.hotbarSlot = 0;
            pk.item = this.tool;
            player.dataPacket(pk);
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean("HandItemSet", true); // Can be air but don't try to set a new tool next time
        if (tool != null) {
            this.namedTag.put("Item", NBTIO.putItemHelper(tool, true));
        }
    }

    public Item getTool() {
        return this.tool;
    }
}
