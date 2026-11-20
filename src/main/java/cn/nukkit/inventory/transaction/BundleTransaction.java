package cn.nukkit.inventory.transaction;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.BundleInventory;
import cn.nukkit.inventory.DoubleChestInventory;
import cn.nukkit.inventory.PlayerCursorInventory;
import cn.nukkit.inventory.transaction.action.CreativeInventoryAction;
import cn.nukkit.inventory.transaction.action.DropItemAction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class BundleTransaction extends InventoryTransaction {

    public BundleTransaction(Player source, List<InventoryAction> actions) {
        super(source, actions);
    }

    @Override
    protected boolean matchItems(List<Item> needItems, List<Item> haveItems) {
        int drops = 0;
        for (InventoryAction action : this.actions) {
            if (!(action instanceof SlotChangeAction || action instanceof DropItemAction || action instanceof CreativeInventoryAction)
                    || (action instanceof DropItemAction && ++drops > 1)
                    || !action.isValid(this.source)
                    || !BundleInventory.addContentItems(action.getTargetItem(), needItems)
                    || !BundleInventory.addContentItems(action.getSourceItem(), haveItems)) {
                this.invalid = true;
                return false;
            }
        }
        for (Item needItem : new ArrayList<>(needItems)) {
            for (Item haveItem : new ArrayList<>(haveItems)) {
                if (!needItem.equals(haveItem)) {
                    continue;
                }
                int count = Math.min(needItem.getCount(), haveItem.getCount());
                needItem.setCount(needItem.getCount() - count);
                haveItem.setCount(haveItem.getCount() - count);
                if (haveItem.getCount() == 0) {
                    haveItems.remove(haveItem);
                }
                if (needItem.getCount() == 0) {
                    needItems.remove(needItem);
                    break;
                }
            }
        }
        return !this.invalid && needItems.isEmpty() && haveItems.isEmpty();
    }

    @Override
    public boolean execute() {
        if (this.hasExecuted || this.invalid || !canExecute() || !callExecuteEvent()) {
            sendInventories();
            return false;
        }
        for (InventoryAction action : this.actions) {
            if (!action.onPreExecute(this.source)) {
                sendInventories();
                return false;
            }
        }
        if (!canExecute()) {
            sendInventories();
            return false;
        }

        List<SlotChangeAction> applied = new ArrayList<>();
        // Apply inventory writes before world drops, so a rejected write cannot duplicate an item.
        for (InventoryAction action : this.actions) {
            if (!(action instanceof SlotChangeAction slot)) {
                continue;
            }
            if (!slot.execute(this.source)
                    || !sameResult(slot.getInventory().getItem(slot.getSlot()), slot.getTargetItem())) {
                applied.add(slot);
                for (int i = applied.size() - 1; i >= 0; i--) {
                    restore(applied.get(i));
                }
                sendInventories();
                return false;
            }
            applied.add(slot);
        }
        for (InventoryAction action : this.actions) {
            if (!(action instanceof SlotChangeAction) && !action.execute(this.source)) {
                for (int i = applied.size() - 1; i >= 0; i--) {
                    restore(applied.get(i));
                }
                sendInventories();
                return false;
            }
        }
        for (InventoryAction action : this.actions) {
            action.onExecuteSuccess(this.source);
        }
        this.hasExecuted = true;
        sendInventories();
        return true;
    }

    private boolean sameResult(Item actual, Item expected) {
        if (actual.equalsExact(expected)) {
            return true;
        }
        if (!(actual instanceof ItemBundle) || !(expected instanceof ItemBundle) || !actual.equals(expected, true, false)
                || actual.getCount() != expected.getCount()) {
            return false;
        }
        return withoutBundleIds(actual).equals(withoutBundleIds(expected));
    }

    private CompoundTag withoutBundleIds(Item item) {
        CompoundTag tag = item.getOrCreateNamedTag().copy().remove(ItemBundle.TAG_BUNDLE_ID);
        for (CompoundTag entry : tag.getList(ItemBundle.TAG_STORAGE_ITEM_COMPONENT_CONTENT, CompoundTag.class).getAll()) {
            Item nested = NBTIO.getItemHelper(entry);
            if (nested instanceof ItemBundle) {
                entry.putCompound("tag", withoutBundleIds(nested));
            }
        }
        return tag;
    }

    private void restore(SlotChangeAction action) {
        if (!(action.getInventory() instanceof BaseInventory inventory) || inventory.destroyed) {
            return;
        }
        int slot = action.getSlot();
        if (inventory instanceof DoubleChestInventory chest) {
            if (slot < chest.getLeftSide().getSize()) {
                inventory = chest.getLeftSide();
            } else {
                slot -= chest.getLeftSide().getSize();
                inventory = chest.getRightSide();
            }
        }
        if (inventory instanceof PlayerCursorInventory cursor) {
            inventory = cursor.getHolder().getUIInventory();
            slot = 0;
        }
        if (inventory.destroyed || (inventory.getHolder() instanceof BlockEntity entity && entity.closed)) {
            return;
        }
        Item before = inventory.getItem(slot);
        Item original = action.getSourceItem();
        if (original.isNull()) {
            inventory.slots.remove(slot);
        } else {
            inventory.slots.put(slot, original);
        }
        if (inventory.getHolder() instanceof BlockEntity entity) {
            entity.setDirty();
        }
        inventory.onSlotChange(slot, before, false);
    }
}
