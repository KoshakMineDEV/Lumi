package cn.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityHumanType;
import cn.nukkit.event.entity.EntityArmorChangeEvent;
import cn.nukkit.event.entity.EntityInventoryChangeEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemMap;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.inventory.ContainerType;
import cn.nukkit.registry.Registries;

import java.util.Collection;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class PlayerInventory extends BaseInventory {

    protected int itemInHandIndex = 0;

    public PlayerInventory(EntityHumanType player) {
        super(player, InventoryType.PLAYER);
    }

    @Override
    public int getSize() {
        return super.getSize() - 4;
    }

    @Override
    public void setSize(int size) {
        super.setSize(size + 4);
        this.sendContents(this.getViewers());
    }

    /**
     * Called when a client equips a hotbar inventorySlot. This method should not be used by plugins.
     * This method will call PlayerItemHeldEvent.
     *
     * @param slot hotbar slot Number of the hotbar slot to equip.
     * @return boolean if the equipment change was successful, false if not.
     */
    public boolean equipItem(int slot) {
        if (!isHotbarSlot(slot)) {
            this.sendContents((Player) this.getHolder());
            return false;
        }

        if (this.getHolder() instanceof Player player) {
            Item item = this.getItem(slot);
            PlayerItemHeldEvent ev = new PlayerItemHeldEvent(player, item, slot);
            this.getHolder().getLevel().getServer().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                this.sendContents(this.getViewers());
                return false;
            }

            if (player.fishing != null) {
                if (!item.equals(player.fishing.rod)) {
                    player.stopFishing(false);
                }
            }
            if (item instanceof ItemMap itemMap) {
                itemMap.trySendImage(player);
            }
        }

        this.setHeldItemIndex(slot, false);
        return true;
    }

    private boolean isHotbarSlot(int slot) {
        return slot >= 0 && slot <= this.getHotbarSize();
    }

    public int getHotbarSlotIndex(int index) {
        return index;
    }

    public void setHotbarSlotIndex(int index, int slot) {

    }

    public int getHeldItemIndex() {
        return this.itemInHandIndex;
    }

    public void setHeldItemIndex(int index) {
        setHeldItemIndex(index, true);
    }

    public void setHeldItemIndex(int index, boolean send) {
        if (index >= 0 && index < this.getHotbarSize()) {
            this.itemInHandIndex = index;

            if (this.getHolder() instanceof Player && send) {
                this.sendHeldItem((Player) this.getHolder());
            }

            this.sendHeldItem(this.getHolder().getViewers().values());
        }
    }

    public Item getItemInHand() {
        Item item = this.getItem(this.itemInHandIndex);
        if (item != null) {
            return item;
        } else {
            return new ItemBlock(Block.get(BlockID.AIR), 0, 0);
        }
    }

    public Item getItemInHandFast() {
        Item item = this.getItemFast(this.getHeldItemIndex());
        if (item != null) {
            return item;
        } else {
            return air;
        }
    }

    public boolean setItemInHand(Item item) {
        return this.setItem(this.itemInHandIndex, item);
    }

    public int getHeldItemSlot() {
        return this.itemInHandIndex;
    }

    public void setHeldItemSlot(int slot) {
        if (!isHotbarSlot(slot)) {
            return;
        }

        this.itemInHandIndex = slot;

        if (this.getHolder() instanceof Player) {
            this.sendHeldItem((Player) this.getHolder());
        }

        this.sendHeldItem(this.getViewers());
    }

    public void sendHeldItem(Player... players) {
        Item item = this.getItemInHand();

        MobEquipmentPacket packet = new MobEquipmentPacket();
        packet.item = item;
        packet.inventorySlot = packet.hotbarSlot = this.itemInHandIndex;

        for (Player player : players) {
            packet.eid = this.getHolder().getId();
            if (player.equals(this.getHolder())) {
                packet.eid = player.getId();
                this.sendSlot(this.itemInHandIndex, player);
            }

            player.dataPacket(packet);
        }
    }

    public void sendHeldItemIfNotAir(Player player) {
        Item item = this.getItemInHand();
        if (item.getId() != 0) {
            MobEquipmentPacket packet = new MobEquipmentPacket();
            packet.item = item;
            packet.inventorySlot = packet.hotbarSlot = this.itemInHandIndex;
            packet.eid = player.getId();
            this.sendSlot(this.itemInHandIndex, player);
            player.dataPacket(packet);
        }
    }

    public void sendHeldItem(Collection<Player> players) {
        this.sendHeldItem(players.toArray(Player.EMPTY_ARRAY));
    }

    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        EntityHuman holder = this.getHolder();
        if (holder instanceof Player && !((Player) holder).spawned) {
            return;
        }

        if (index >= this.getSize()) {
            this.sendArmorSlot(index, this.getViewers());
            this.sendArmorSlot(index, this.getHolder().getViewers().values());
        } else {
            super.onSlotChange(index, before, send);
        }
    }

    public int getHotbarSize() {
        return 9;
    }

    public Item getArmorItem(int index) {
        return this.getItem(this.getSize() + index);
    }

    public boolean setArmorItem(int index, Item item) {
        return this.setArmorItem(index, item, false);
    }

    public boolean setArmorItem(int index, Item item, boolean ignoreArmorEvents) {
        return this.setItem(this.getSize() + index, item, ignoreArmorEvents);
    }

    public Item getHelmet() {
        return this.getItem(this.getSize());
    }

    public Item getHelmetFast() {
        return this.getItemFast(36);
    }

    public Item getChestplate() {
        return this.getItem(this.getSize() + 1);
    }

    public Item getChestplateFast() {
        return this.getItemFast(37);
    }

    public Item getLeggings() {
        return this.getItem(this.getSize() + 2);
    }

    public Item getLeggingsFast() {
        return this.getItemFast(38);
    }

    public Item getBoots() {
        return this.getItem(this.getSize() + 3);
    }

    public Item getBootsFast() {
        return this.getItemFast(39);
    }

    public boolean setHelmet(Item helmet) {
        return this.setItem(this.getSize(), helmet);
    }

    public boolean setChestplate(Item chestplate) {
        return this.setItem(this.getSize() + 1, chestplate);
    }

    public boolean setLeggings(Item leggings) {
        return this.setItem(this.getSize() + 2, leggings);
    }

    public boolean setBoots(Item boots) {
        return this.setItem(this.getSize() + 3, boots);
    }

    @Override
    public boolean setItem(int index, Item item) {
        return setItem(index, item, true);
    }

    @Override
    public boolean setItem(int index, Item item, boolean send) {
        if (index < 0 || index >= this.size) {
            return false;
        } else if (item.getId() == 0 || item.getCount() <= 0) {
            return this.clear(index, send);
        }

        if (index >= this.getSize()) { // Armor change
            EntityArmorChangeEvent event = new EntityArmorChangeEvent(this.getHolder(), this.getItem(index), item, index);
            if (!event.call() && this.getHolder() != null) {
                this.sendArmorSlot(index, this.getViewers());
                return false;
            }
            item = event.getNewItem();
        } else {
            EntityInventoryChangeEvent event = new EntityInventoryChangeEvent(this.getHolder(), this.getItem(index), item, index);
            if (!event.call()) {
                this.sendSlot(index, this.getViewers());
                return false;
            }
            item = event.getNewItem();
        }

        Item old = this.getItem(index);
        this.slots.put(index, item.clone());
        this.onSlotChange(index, old, send);
        return true;
    }

    @Override
    public boolean clear(int index, boolean send) {
        if (this.slots.containsKey(index)) {
            Item item = new ItemBlock(Block.get(BlockID.AIR), null, 0);
            Item old = this.slots.get(index);
            if (index >= this.getSize() && index < this.size) {
                EntityArmorChangeEvent event = new EntityArmorChangeEvent(this.getHolder(), old, item, index);
                if (!event.call()) {
                    if (index >= this.size) {
                        this.sendArmorSlot(index, this.getViewers());
                    } else {
                        this.sendSlot(index, this.getViewers());
                    }
                    return false;
                }
                item = event.getNewItem();
            } else {
                EntityInventoryChangeEvent event = new EntityInventoryChangeEvent(this.getHolder(), old, item, index);
                if (!event.call()) {
                    if (index >= this.size) {
                        this.sendArmorSlot(index, this.getViewers());
                    } else {
                        this.sendSlot(index, this.getViewers());
                    }
                    return false;
                }
                item = event.getNewItem();
            }

            if (item.getId() != Item.AIR) {
                this.slots.put(index, item.clone());
            } else {
                this.slots.remove(index);
            }

            this.onSlotChange(index, old, send);
        }

        return true;
    }

    public Item[] getArmorContents() {
        Item[] armor = new Item[4];
        for (int i = 0; i < 4; i++) {
            armor[i] = this.getItem(this.getSize() + i);
        }

        return armor;
    }

    @Override
    public void clearAll() {
        int limit = this.getSize() + 4;
        for (int index = 0; index < limit; ++index) {
            this.clear(index);
        }
        this.getHolder().getOffhandInventory().clearAll();
    }

    public void sendArmorContents(Player player) {
        this.sendArmorContents(new Player[]{player});
    }

    public void sendArmorContents(Player[] players) {
        Item[] armor = this.getArmorContents();

        MobArmorEquipmentPacket equipmentPacket = new MobArmorEquipmentPacket();
        equipmentPacket.eid = this.getHolder().getId();
        equipmentPacket.slots = armor;

        for (Player player : players) {
            if (player.equals(this.getHolder())) {
                InventoryContentPacket contentPacket = new InventoryContentPacket();
                contentPacket.inventoryId = InventoryContentPacket.SPECIAL_ARMOR;
                contentPacket.slots = armor;
                player.dataPacket(contentPacket);
            } else {
                player.dataPacket(equipmentPacket);
            }
        }
    }

    public void sendArmorContentsIfNotAr(Player player) {
        Item[] armor = this.getArmorContents();
        if (armor[0].getId() != 0 || armor[1].getId() != 0 || armor[2].getId() != 0 || armor[3].getId() != 0) {
            MobArmorEquipmentPacket packet = new MobArmorEquipmentPacket();
            packet.eid = this.getHolder().getId();
            packet.slots = armor;
            player.dataPacket(packet);
        }
    }

    public void setArmorContents(Item[] items) {
        if (items.length < 4) {
            Item[] newItems = new Item[4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }

        for (int i = 0; i < 4; ++i) {
            if (items[i] == null) {
                items[i] = new ItemBlock(Block.get(BlockID.AIR), null, 0);
            }

            if (items[i].getId() == Item.AIR) {
                this.clear(this.getSize() + i);
            } else {
                this.setItem(this.getSize() + i, items[i]);
            }
        }
    }

    public void sendArmorContents(Collection<Player> players) {
        this.sendArmorContents(players.toArray(Player.EMPTY_ARRAY));
    }

    public void sendArmorSlot(int index, Player player) {
        this.sendArmorSlot(index, new Player[]{player});
    }

    public void sendArmorSlot(int index, Player[] players) {
        Item[] armor = this.getArmorContents();

        MobArmorEquipmentPacket equipmentPacket = new MobArmorEquipmentPacket();
        equipmentPacket.eid = this.getHolder().getId();
        equipmentPacket.slots = armor;

        for (Player player : players) {
            if (player.equals(this.getHolder())) {
                InventorySlotPacket slotPacket = new InventorySlotPacket();
                slotPacket.inventoryId = InventoryContentPacket.SPECIAL_ARMOR;
                slotPacket.slot = index - this.getSize();
                slotPacket.item = this.getItem(index);
                player.dataPacket(slotPacket);
            } else {
                player.dataPacket(equipmentPacket);
            }
        }
    }

    public void sendArmorSlot(int index, Collection<Player> players) {
        this.sendArmorSlot(index, players.toArray(Player.EMPTY_ARRAY));
    }

    @Override
    public void sendContents(Player player) {
        this.sendContents(new Player[]{player});
    }

    @Override
    public void sendContents(Collection<Player> players) {
        this.sendContents(players.toArray(Player.EMPTY_ARRAY));
    }

    @Override
    public void sendContents(Player[] players) {
        InventoryContentPacket packet = new InventoryContentPacket();
        packet.slots = new Item[this.getSize()];
        for (int i = 0; i < this.getSize(); ++i) {
            packet.slots[i] = this.getItem(i);
        }

        for (Player player : players) {
            int id = player.getWindowId(this);
            if (id == -1) {
                if (this.getHolder() != player) {
                    this.close(player);
                }
                continue;
            }
            packet.inventoryId = id;
            player.dataPacket(packet.clone());
        }
    }

    @Override
    public void sendSlot(int index, Player player) {
        this.sendSlot(index, new Player[]{player});
    }

    @Override
    public void sendSlot(int index, Collection<Player> players) {
        this.sendSlot(index, players.toArray(Player.EMPTY_ARRAY));
    }

    @Override
    public void sendSlot(int index, Player... players) {
        if (players.length == 0 && this.getHolder() instanceof Player player) {
            players = new Player[]{player};
        }

        InventorySlotPacket packet = new InventorySlotPacket();
        packet.slot = index;
        packet.item = this.getItem(index).clone();

        for (Player player : players) {
            if (player.equals(this.getHolder())) {
                packet.inventoryId = ContainerIds.INVENTORY;
                player.dataPacket(packet);
            } else {
                int id = player.getWindowId(this);
                if (id == -1) {
                    this.close(player);
                    continue;
                }
                packet.inventoryId = id;
                player.dataPacket(packet.clone());
            }
        }
    }

    public void sendCreativeContents() {
        if (!(this.getHolder() instanceof Player player)) {
            return;
        }

        CreativeContentPacket packet = new CreativeContentPacket();
        if (!player.isSpectator()) {
            packet.creativeItems = Registries.CREATIVE_ITEM.get(player.protocol);
        }
        player.dataPacket(packet);
    }

    @Override
    public EntityHuman getHolder() {
        return (EntityHuman) super.getHolder();
    }

    @Override
    public void onOpen(Player who) {
        super.onOpen(who);
        ContainerOpenPacket packet = new ContainerOpenPacket();
        packet.windowId = who.getWindowId(this);
        packet.type = this.getType().getNetworkType();
        packet.x = who.getFloorX();
        packet.y = who.getFloorY();
        packet.z = who.getFloorZ();
        packet.entityId = who.getId();
        who.dataPacket(packet);
    }

    @Override
    public void onClose(Player who) {
        if (who.getClosingWindowId() != Integer.MAX_VALUE) {
            ContainerClosePacket packet = new ContainerClosePacket();
            packet.windowId = who.getWindowId(this);
            packet.wasServerInitiated = who.getClosingWindowId() != packet.windowId;
            packet.type = ContainerType.from(this.type.getNetworkType());
            who.dataPacket(packet);
        }

        // Player can never stop viewing their own inventory
        if (who != holder) {
            super.onClose(who);
        }
    }
}