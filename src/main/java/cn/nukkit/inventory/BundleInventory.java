package cn.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BundleInventory extends BaseInventory {

    public static final int MAX_FILL = 64;
    public static final int DYNAMIC_REGISTRY_WINDOW_ID = 125;

    public BundleInventory(ItemBundle holder) {
        super(holder, InventoryType.CHEST, Map.of(), MAX_FILL, "Bundle");
        loadFromHolder(holder);
    }

    public BundleInventory(ItemBundle holder, BundleInventory source) {
        super(holder, InventoryType.CHEST, Map.of(), MAX_FILL, "Bundle");
        source.slots.forEach((slot, item) -> this.slots.put(slot, item.clone()));
    }

    public static void sendContents(Player player, DataPacket packet) {
        if (player.protocol < ProtocolInfo.v1_21_40) {
            return;
        }
        if (packet instanceof InventoryContentPacket content && content.inventoryId != DYNAMIC_REGISTRY_WINDOW_ID) {
            Set<Integer> sent = new HashSet<>();
            for (Item item : content.slots) {
                sendContents(player, item, sent);
            }
        } else if (packet instanceof InventorySlotPacket slot && slot.inventoryId != DYNAMIC_REGISTRY_WINDOW_ID) {
            sendContents(player, slot.item, new HashSet<>());
        }
    }

    private static void sendContents(Player player, Item item, Set<Integer> sent) {
        if (!(item instanceof ItemBundle bundle) || !sent.add(bundle.getBundleId())) {
            return;
        }
        BundleInventory inventory = bundle.getInventory();
        for (Item nested : inventory.getContents().values()) {
            sendContents(player, nested, sent);
        }
        inventory.sendContents(player);
    }

    @Override
    public boolean setItem(int index, Item item, boolean send) {
        if (index < 0 || index >= this.getSize() || !canStore(item)) {
            return false;
        }
        if (wouldCreateBundleCycle(item)) {
            return false;
        }

        int newWeight = getWeight() - getWeight(this.getItemFast(index)) + getWeight(item);
        if (newWeight > MAX_FILL) {
            return false;
        }

        return super.setItem(index, item, send);
    }

    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        getHolder().saveNBT();
        super.onSlotChange(index, before, send);
    }

    @Override
    public void sendContents(Player... players) {
        InventoryContentPacket pk = new InventoryContentPacket();
        pk.slots = new Item[this.getSize()];
        for (int i = 0; i < this.getSize(); ++i) {
            pk.slots[i] = this.getItem(i);
        }
        pk.containerNameData = new FullContainerName(ContainerSlotType.DYNAMIC_CONTAINER, getHolder().getBundleId());
        pk.dynamicContainerSize = this.getSize();
        pk.storageItem = getHolder().clone();

        for (Player player : players) {
            if (player.protocol < ProtocolInfo.v1_21_40) {
                continue;
            }
            pk.inventoryId = DYNAMIC_REGISTRY_WINDOW_ID;
            player.dataPacket(pk);
        }
    }

    @Override
    public void sendSlot(int index, Player... players) {
        InventorySlotPacket pk = new InventorySlotPacket();
        pk.slot = index;
        pk.item = this.getItem(index);
        pk.containerNameData = new FullContainerName(ContainerSlotType.DYNAMIC_CONTAINER, getHolder().getBundleId());
        pk.dynamicContainerSize = this.getSize();
        pk.storageItem = getHolder().clone();

        for (Player player : players) {
            if (player.protocol < ProtocolInfo.v1_21_40) {
                continue;
            }
            pk.inventoryId = DYNAMIC_REGISTRY_WINDOW_ID;
            player.dataPacket(pk);
        }
    }

    public int getWeight() {
        return getWeight(new HashSet<>());
    }

    @Override
    public boolean canAddItem(Item item) {
        return canStore(item) && !wouldCreateBundleCycle(item) && getWeight() + getWeight(item) <= MAX_FILL;
    }

    @Override
    public Item[] addItem(Item... items) {
        List<Item> remaining = new ArrayList<>();
        for (Item item : items) {
            if (item == null || item.isNull()) {
                continue;
            }
            Item rest = item.clone();
            Item unit = item.clone();
            unit.setCount(1);
            if (canStore(unit) && !wouldCreateBundleCycle(unit)) {
                int unitWeight = getWeight(unit);
                for (int pass = 0; pass < 2 && rest.getCount() > 0; pass++) {
                    for (int i = 0; i < getSize() && rest.getCount() > 0; i++) {
                        Item stored = getItem(i);
                        if (pass == 0 ? stored.isNull() || !stored.equals(unit) : !stored.isNull()) {
                            continue;
                        }
                        int count = Math.min(rest.getCount(), Math.min((MAX_FILL - getWeight()) / unitWeight,
                                unit.getMaxStackSize() - (stored.isNull() ? 0 : stored.getCount())));
                        if (count <= 0) {
                            continue;
                        }
                        Item updated = unit.clone();
                        updated.setCount((stored.isNull() ? 0 : stored.getCount()) + count);
                        if (setItem(i, updated)) {
                            rest.setCount(rest.getCount() - count);
                        }
                    }
                }
            }
            if (!rest.isNull()) {
                remaining.add(rest);
            }
        }
        return remaining.toArray(Item.EMPTY_ARRAY);
    }

    public static boolean addContentItems(Item item, List<Item> items) {
        return addContentItems(item, items, new HashSet<>(), 0) >= 0;
    }

    private static int addContentItems(Item item, List<Item> items, Set<Integer> bundleIds, int depth) {
        if (item.isNull()) {
            return 0;
        }
        if (!(item instanceof ItemBundle bundle)) {
            items.add(item);
            return Math.max(1, MAX_FILL / Math.max(1, item.getMaxStackSize())) * item.getCount();
        }
        CompoundTag original = bundle.getNamedTag();
        if (original != null && original.contains(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT)
                && !original.containsList(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT)) {
            return -1;
        }
        if (depth > MAX_FILL / 4 || item.getCount() != 1 || !bundleIds.add(bundle.getBundleId())) {
            return -1;
        }

        CompoundTag tag = bundle.getNamedTag();
        if (!tag.containsList(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT)) {
            return -1;
        }
        ListTag<?> contents = tag.getList(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT);
        if (contents.size() > MAX_FILL) {
            return -1;
        }
        Set<Integer> slots = new HashSet<>();
        int weight = 0;
        for (Tag entry : contents.getAll()) {
            if (!(entry instanceof CompoundTag itemTag) || !itemTag.containsByte("Slot")) {
                return -1;
            }
            int slot = itemTag.getByte("Slot") & 0xff;
            Item stored = NBTIO.getItemHelper(itemTag);
            if (slot >= MAX_FILL || !slots.add(slot) || stored.isNull()
                    || stored.getCount() > stored.getMaxStackSize()
                    || stored.getId() == BlockID.SHULKER_BOX || stored.getId() == BlockID.UNDYED_SHULKER_BOX) {
                return -1;
            }
            int storedWeight = addContentItems(stored, items, bundleIds, depth + 1);
            if (storedWeight < 0 || (weight += storedWeight) > MAX_FILL) {
                return -1;
            }
        }
        Item shell = bundle.clone();
        shell.setNamedTag(tag.copy().remove(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT).remove(ItemBundle.TAG_BUNDLE_ID));
        items.add(shell);
        return weight + 4;
    }

    public ItemBundle getHolder() {
        return (ItemBundle) holder;
    }

    private void loadFromHolder(ItemBundle holder) {
        holder.getBundleId();
        CompoundTag tag = holder.getNamedTag();
        if (tag == null || !tag.containsList(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT)) {
            return;
        }

        ListTag<CompoundTag> items = tag.getList(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT, CompoundTag.class);
        for (CompoundTag itemTag : items.getAll()) {
            int slot = itemTag.getByte("Slot") & 0xFF;
            if (slot >= this.getSize()) {
                continue;
            }

            Item item = NBTIO.getItemHelper(itemTag);
            if (!item.isNull() && canStore(item) && !wouldCreateBundleCycle(item)) {
                int newWeight = getWeight() + getWeight(item);
                if (newWeight <= MAX_FILL) {
                    this.slots.put(slot, item.autoAssignStackNetworkId());
                }
            }
        }
    }

    private boolean canStore(Item item) {
        if (item == null || item.isNull()) {
            return true;
        }
        return item.getCount() <= item.getMaxStackSize()
                && item.getId() != BlockID.SHULKER_BOX && item.getId() != BlockID.UNDYED_SHULKER_BOX;
    }

    private int getWeight(Set<Integer> visitedBundleIds) {
        int weight = 0;
        for (Item item : this.slots.values()) {
            weight += getWeight(item, visitedBundleIds);
        }
        return weight;
    }

    private int getWeight(Item item) {
        return getWeight(item, new HashSet<>());
    }

    private int getWeight(Item item, Set<Integer> visitedBundleIds) {
        if (item == null || item.isNull() || item.getCount() <= 0) {
            return 0;
        }

        if (item instanceof ItemBundle bundle) {
            int bundleId = bundle.getBundleId();
            if (!visitedBundleIds.add(bundleId)) {
                return 0;
            }
            return bundle.getInventory().getWeight(visitedBundleIds) + 4;
        }

        return Math.max(1, MAX_FILL / Math.max(1, item.getMaxStackSize())) * item.getCount();
    }

    private boolean wouldCreateBundleCycle(Item item) {
        if (!(item instanceof ItemBundle bundle)) {
            return false;
        }
        return containsBundleId(bundle, getHolder().getBundleId(), new HashSet<>());
    }

    private boolean containsBundleId(ItemBundle bundle, int targetBundleId, Set<Integer> visitedBundleIds) {
        int bundleId = bundle.getBundleId();
        if (!visitedBundleIds.add(bundleId)) {
            return false;
        }
        if (bundle.matchesBundleIdentity(targetBundleId)) {
            return true;
        }

        for (Item nested : bundle.getInventory().getContents().values()) {
            if (nested instanceof ItemBundle nestedBundle
                    && containsBundleId(nestedBundle, targetBundleId, visitedBundleIds)) {
                return true;
            }
        }
        return false;
    }
}
