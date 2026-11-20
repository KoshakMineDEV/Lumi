package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.inventory.BundleInventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.transaction.BundleTransaction;
import cn.nukkit.inventory.transaction.action.DropItemAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemBundle extends StringItemBase implements InventoryHolder {

    public static final String TAG_BUNDLE_ID = "bundle_id";
    public static final String TAG_STORAGE_ITEM_COMPONENT_CONTENT = "storage_item_component_content";

    private static final AtomicInteger NEXT_BUNDLE_ID = new AtomicInteger(1);

    private int bundleId;
    private int sourceBundleId;
    private BundleInventory inventory;

    public ItemBundle() {
        this(BUNDLE, "Bundle");
    }

    public ItemBundle(String namespaceId, String name) {
        super(namespaceId, name);
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_40;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public int getBundleId() {
        return ensureBundleTag().getInt(TAG_BUNDLE_ID);
    }

    public boolean matchesBundleIdentity(int bundleId) {
        if (bundleId <= 0) {
            return false;
        }
        if (this.bundleId == bundleId || this.sourceBundleId == bundleId) {
            return true;
        }
        CompoundTag tag = this.hasCompoundTag() ? this.getNamedTag() : null;
        return tag != null && tag.contains(TAG_BUNDLE_ID) && tag.getInt(TAG_BUNDLE_ID) == bundleId;
    }

    @Override
    public BundleInventory getInventory() {
        if (this.inventory == null) {
            ensureBundleTag();
            this.inventory = new BundleInventory(this);
        }
        return this.inventory;
    }

    public void saveNBT() {
        CompoundTag tag = ensureBundleTag();
        ListTag<CompoundTag> storedItems = new ListTag<>(TAG_STORAGE_ITEM_COMPONENT_CONTENT);
        for (Map.Entry<Integer, Item> entry : this.getInventory().getContents().entrySet()) {
            Item item = entry.getValue();
            if (item != null && !item.isNull()) {
                storedItems.add(NBTIO.putItemHelper(item, entry.getKey()));
            }
        }
        tag.putList(storedItems);
        super.setNamedTag(tag);
    }

    @Override
    public Item setNamedTag(CompoundTag tag) {
        this.inventory = null;
        this.bundleId = 0;
        this.sourceBundleId = 0;
        return super.setNamedTag(tag);
    }

    @Override
    public Item setCompoundTag(byte[] tags) {
        this.inventory = null;
        this.bundleId = 0;
        this.sourceBundleId = 0;
        return super.setCompoundTag(tags);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        Item before = player.getInventory().getItemInHand();
        Map.Entry<Integer, Item> entry = this.getInventory().getContents().entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isNull())
                .min(Map.Entry.comparingByKey())
                .orElse(null);
        if (entry == null) {
            return false;
        }

        Item item = entry.getValue().clone();
        if (!this.getInventory().clear(entry.getKey(), false)) {
            return false;
        }
        BundleTransaction transaction = new BundleTransaction(player, List.of(
                new SlotChangeAction(player.getInventory(), player.getInventory().getHeldItemIndex(), before, this.clone()),
                new DropItemAction(Item.AIR_ITEM.clone(), item)));
        if (!transaction.execute()) {
            this.getInventory().setItem(entry.getKey(), entry.getValue(), false);
            return false;
        }
        if (player.getLevel() != null) {
            player.getLevel().addSound(player, Sound.BUNDLE_DROP_CONTENTS);
        }
        return true;
    }

    @Override
    public ItemBundle clone() {
        if (this.inventory == null && this.getStackNetId() > 0) {
            this.getInventory();
        }
        ItemBundle cloned = (ItemBundle) super.clone();
        cloned.inventory = this.inventory == null ? null : new BundleInventory(cloned, this.inventory);
        return cloned;
    }

    public void assignNewBundleId() {
        CompoundTag tag = this.hasCompoundTag() ? this.getNamedTag() : new CompoundTag();
        int oldBundleId = this.bundleId > 0 ? this.bundleId
                : tag.contains(TAG_BUNDLE_ID) ? tag.getInt(TAG_BUNDLE_ID) : this.sourceBundleId;
        this.sourceBundleId = oldBundleId;
        this.bundleId = nextBundleId();
        tag.putInt(TAG_BUNDLE_ID, this.bundleId);
        if (!tag.containsList(TAG_STORAGE_ITEM_COMPONENT_CONTENT)) {
            tag.putList(new ListTag<>(TAG_STORAGE_ITEM_COMPONENT_CONTENT));
        }
        super.setNamedTag(tag);
    }

    private CompoundTag ensureBundleTag() {
        CompoundTag tag = this.hasCompoundTag() ? this.getNamedTag() : new CompoundTag();
        boolean dirty = !this.hasCompoundTag();

        if (this.bundleId <= 0) {
            int storedId = tag.containsInt(TAG_BUNDLE_ID) ? tag.getInt(TAG_BUNDLE_ID) : 0;
            if (storedId > 0) {
                reserveBundleId(storedId);
                this.bundleId = storedId;
            } else {
                this.bundleId = nextBundleId();
            }
            tag.putInt(TAG_BUNDLE_ID, this.bundleId);
            dirty = true;
        } else if (!tag.contains(TAG_BUNDLE_ID) || tag.getInt(TAG_BUNDLE_ID) != this.bundleId) {
            tag.putInt(TAG_BUNDLE_ID, this.bundleId);
            dirty = true;
        } else {
            reserveBundleId(this.bundleId);
        }
        if (!tag.containsList(TAG_STORAGE_ITEM_COMPONENT_CONTENT)) {
            tag.putList(new ListTag<>(TAG_STORAGE_ITEM_COMPONENT_CONTENT));
            dirty = true;
        }
        if (dirty) {
            super.setNamedTag(tag);
        }
        return tag;
    }

    private static int nextBundleId() {
        return NEXT_BUNDLE_ID.getAndUpdate(current -> current == Integer.MAX_VALUE ? 1 : current + 1);
    }

    private static void reserveBundleId(int bundleId) {
        if (bundleId < Integer.MAX_VALUE) {
            NEXT_BUNDLE_ID.updateAndGet(current -> Math.max(current, bundleId + 1));
        }
    }
}
