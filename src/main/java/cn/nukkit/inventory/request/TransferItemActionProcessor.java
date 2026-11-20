package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerTransferItemEvent;
import cn.nukkit.inventory.*;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestActionType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.TransferItemStackRequestAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseSlot;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Base handler for TAKE and PLACE actions — both structurally a partial/full
 * transfer between two slots. Centralises the count math, stack merging rules
 * and response container construction.
 * <p>
 * Adapted from PowerNukkitX (<a href="https://github.com/PowerNukkitX/PowerNukkitX">PowerNukkitX</a>)
 */
@Slf4j
public abstract class TransferItemActionProcessor<T extends TransferItemStackRequestAction>
        implements ItemStackRequestActionProcessor<T> {

    protected ActionResponse doTransfer(T action, Player player, ItemStackRequestContext context) {
        ItemStackRequestSlotData src = action.getSource();
        ItemStackRequestSlotData dst = action.getDestination();

        Inventory srcInv = NetworkMapping.getInventory(player, src);
        Inventory dstInv = NetworkMapping.getInventory(player, dst);

        int srcSlot = srcInv == null ? -1 : NetworkMapping.toInternalSlot(src.getContainer(), src.getSlot());
        int dstSlot = dstInv == null ? -1 : NetworkMapping.toInternalSlot(dst.getContainer(), dst.getSlot());
        int count = action.getCount();

        if (srcInv == null || dstInv == null || isCreatedOutput(dstInv, dstSlot)) {
            return context.error();
        }

        if (sameInventorySlot(srcInv, srcSlot, dstInv, dstSlot)) {
            return context.error();
        }

        if (count <= 0) {
            return context.error();
        }

        Item sourceItem = srcInv.getUnclonedItem(srcSlot);
        if (sourceItem.isNull() || sourceItem.getCount() < count) {
            return context.error();
        }

        boolean fullTransfer = sourceItem.getCount() == count;
        boolean srcIsCreatedOutput = isCreatedOutput(srcInv, srcSlot);
        boolean creativeCreatedOutputTransfer = player.isCreative()
                && srcIsCreatedOutput
                && Boolean.TRUE.equals(context.get(CraftCreativeActionProcessor.CRAFT_CREATIVE_KEY));

        // CREATED_OUTPUT (UI slot 50) is always populated server-side — by creative item
        // creation, crafting, enchanting, anvil, grindstone, etc. — and each write assigns a
        // fresh server-side stackNetworkId. The client cannot know that id, so the
        // stackNetworkId it sends on the follow-up PLACE/TAKE is its own prediction and will
        // never match. Validating it rejects every take of a server-produced item — the items
        // would appear then vanish as the request is rolled back. Skip the source-id check
        // for any CREATED_OUTPUT source and rely on the count/item checks above.
        if (!srcIsCreatedOutput
                && validateStackNetworkId(sourceItem.getStackNetId(), src.getStackNetworkId())) {
            return context.error();
        }

        Item destItem = dstInv.getUnclonedItem(dstSlot);
        if (validateStackNetworkId(destItem.getStackNetId(), dst.getStackNetworkId())) {
            return context.error();
        }

        if (creativeCreatedOutputTransfer) {
            return transferCreativeCreatedOutput(action, player, context, srcInv, srcSlot, dstInv, dstSlot,
                    sourceItem, destItem, fullTransfer);
        }

        if (!destItem.isNull() && !destItem.equals(sourceItem, true, true)) {
            return context.error();
        }

        int destCount = destItem.isNull() ? 0 : destItem.getCount();
        if (destCount + count > Math.min(sourceItem.getMaxStackSize(), dstInv.getMaxStackSize())) {
            return context.error();
        }

        if (!isSlotCompatible(dstInv, dstSlot, sourceItem)) {
            return context.error();
        }

        // Equipment containers (OFFHAND/ARMOR) must emit network packets so other
        // players see MobEquipment/MobArmor updates; other containers can stay
        // quiet to avoid double-send with the ItemStackResponse echo.

        // stackNetId allocation strategy aligned with Allay/PNX:
        // - full transfer + empty dst   : keep source stackId (whole stack moves)
        // - any transfer + non-empty dst: keep dest stackId (merge into existing)
        // - partial transfer + empty dst: assign new stackId (split creates new stack)
        // - CREATED_OUTPUT full transfer: assign new stackId so every creative take
        //   is an independent stack (prevents id reuse across creative sessions).
        Item newDest;
        if (destItem.isNull()) {
            newDest = sourceItem.clone();
            newDest.setCount(count);
            if (!fullTransfer || srcIsCreatedOutput) {
                newDest.autoAssignStackNetworkId();
            }
        } else {
            newDest = destItem.clone();
            newDest.setCount(destCount + count);
        }

        Item newSrc;
        if (fullTransfer) {
            newSrc = Item.get(Item.AIR);
        } else {
            newSrc = sourceItem.clone();
            newSrc.setCount(sourceItem.getCount() - count);
        }

        if (!fireTransferEvent(player, srcInv, srcSlot, dstInv, dstSlot, sourceItem, destItem, count)) {
            return context.error();
        }

        List<InventoryAction> transactionActions = new ArrayList<>();
        transactionActions.add(new SlotChangeAction(srcInv, srcSlot, sourceItem, newSrc));
        if (srcInv != dstInv || srcSlot != dstSlot) {
            transactionActions.add(new SlotChangeAction(dstInv, dstSlot, destItem, newDest));
        }
        InventoryTransaction transaction = new EventOnlyInventoryTransaction(player, transactionActions);
        if (!transaction.execute()) {
            return context.error();
        }


        boolean removed = isCreatedOutput(srcInv, srcSlot)
                ? context.takeOutput(srcInv, srcSlot, sourceItem, newSrc)
                : context.writeSlot(srcInv, srcSlot, sourceItem, newSrc);
        if (!removed || !context.writeSlot(dstInv, dstSlot, destItem, newDest)) {
            playBundleSound(player, dstInv, Sound.BUNDLE_INSERT_FAIL);
            return context.error();
        }

        context.onCommit(() -> playBundleSound(player, dstInv, Sound.BUNDLE_INSERT));
        context.onCommit(() -> playBundleSound(player, srcInv, Sound.BUNDLE_REMOVE_ONE));

        List<ItemStackResponseContainer> containers = new ArrayList<>();
        // CREATED_OUTPUT has no client-side source slot to acknowledge.
        if (!srcIsCreatedOutput) {
            containers.add(buildContainer(srcInv, srcSlot, src));
        }
        if (!sameNetworkSlot(src, dst)) {
            containers.add(buildContainer(dstInv, dstSlot, dst));
        }
        return context.success(containers);
    }

    private ActionResponse transferCreativeCreatedOutput(T action, Player player, ItemStackRequestContext context,
                                                         Inventory srcInv, int srcSlot,
                                                         Inventory dstInv, int dstSlot,
                                                         Item sourceItem, Item destItem,
                                                         boolean fullTransfer) {
        ItemStackRequestSlotData dst = action.getDestination();
        int count = action.getCount();
        if (srcInv == dstInv && srcSlot == dstSlot) {
            return context.error();
        }
        if (!isSlotCompatible(dstInv, dstSlot, sourceItem)) {
            return context.error();
        }


        Item newDest = sourceItem.clone();
        newDest.setCount(count);
        newDest.autoAssignStackNetworkId();

        Item newSrc;
        if (fullTransfer) {
            newSrc = Item.get(Item.AIR);
        } else {
            newSrc = sourceItem.clone();
            newSrc.setCount(sourceItem.getCount() - count);
        }

        if (!fireTransferEvent(player, srcInv, srcSlot, dstInv, dstSlot, sourceItem, destItem, count)) {
            return context.error();
        }

        List<InventoryAction> transactionActions = new ArrayList<>();
        transactionActions.add(new SlotChangeAction(srcInv, srcSlot, sourceItem, newSrc));
        transactionActions.add(new SlotChangeAction(dstInv, dstSlot, destItem, newDest));
        InventoryTransaction transaction = new EventOnlyInventoryTransaction(player, transactionActions);
        if (!transaction.execute()) {
            return context.error();
        }

        boolean removed = isCreatedOutput(srcInv, srcSlot)
                ? context.takeOutput(srcInv, srcSlot, sourceItem, newSrc)
                : context.writeSlot(srcInv, srcSlot, sourceItem, newSrc);
        if (!removed || !context.writeSlot(dstInv, dstSlot, destItem, newDest)) {
            playBundleSound(player, dstInv, Sound.BUNDLE_INSERT_FAIL);
            return context.error();
        }

        context.onCommit(() -> playBundleSound(player, dstInv, Sound.BUNDLE_INSERT));
        context.onCommit(() -> playBundleSound(player, srcInv, Sound.BUNDLE_REMOVE_ONE));

        return context.success(List.of(buildContainer(dstInv, dstSlot, dst)));
    }

    private static void playBundleSound(Player player, Inventory inventory, Sound sound) {
        if (!(inventory instanceof BundleInventory) || player.getLevel() == null) {
            return;
        }
        player.getLevel().addSound(player, sound);
    }

    private static boolean isEquipmentSlot(ContainerSlotType type) {
        return type == ContainerSlotType.OFFHAND || type == ContainerSlotType.ARMOR;
    }

    static boolean isSlotCompatible(Inventory inventory, int slot, Item item) {
        if (inventory instanceof PlayerOffhandInventory) {
            return item == null || item.isNull() || inventory.allowedToAdd(item);
        }
        if (inventory instanceof PlayerInventory playerInv) {
            int size = playerInv.getSize();
            if (slot == size) {
                return item.canBePutInHelmetSlot();
            } else if (slot == size + 1) {
                return item.isChestplate();
            } else if (slot == size + 2) {
                return item.isLeggings();
            } else if (slot == size + 3) {
                return item.isBoots();
            }
        }
        return true;
    }

    static boolean isCreatedOutput(Inventory inventory, int slot) {
        return inventory instanceof PlayerUIInventory && slot == PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT;
    }

    static boolean fireTransferEvent(Player actor, Inventory sourceInventory, int sourceSlot,
                                     Inventory destinationInventory, int destinationSlot,
                                     Item sourceItem, Item destinationItem, int count) {
        return fireTransferEvent(actor, PlayerTransferItemEvent.Type.TRANSFER,
                sourceInventory, sourceSlot, destinationInventory, destinationSlot,
                sourceItem, destinationItem, count);
    }

    static boolean fireTransferEvent(Player actor, PlayerTransferItemEvent.Type type,
                                     Inventory sourceInventory, int sourceSlot,
                                     Inventory destinationInventory, int destinationSlot,
                                     Item sourceItem, Item destinationItem, int count) {
        PlayerTransferItemEvent event = new PlayerTransferItemEvent(
                actor,
                type,
                sourceInventory,
                sourceSlot,
                destinationInventory,
                destinationSlot,
                sourceItem.clone(),
                destinationItem == null ? Item.get(Item.AIR) : destinationItem.clone(),
                count
        );
        Server.getInstance().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    static boolean sameNetworkSlot(ItemStackRequestSlotData first, ItemStackRequestSlotData second) {
        return first.getContainer() == second.getContainer()
                && first.getSlot() == second.getSlot()
                && Objects.equals(first.getDynamicId(), second.getDynamicId());
    }

    private static boolean sameInventorySlot(Inventory first, int firstSlot, Inventory second, int secondSlot) {
        return ItemStackRequestContext.Slot.of(first, firstSlot)
                .equals(ItemStackRequestContext.Slot.of(second, secondSlot));
    }

    static class EventOnlyInventoryTransaction extends InventoryTransaction {
        EventOnlyInventoryTransaction(Player source, List<InventoryAction> actions) {
            super(source, actions);
        }

        @Override
        public boolean execute() {
            return !this.invalid && !this.actions.isEmpty() && this.callExecuteEvent();
        }
    }

    static ItemStackResponseContainer buildContainer(Inventory inv, int internalSlot, ItemStackRequestSlotData slotData) {
        Item current = inv.getUnclonedItem(internalSlot);
        int networkSlot = slotData.getSlot();
        ItemStackResponseSlot slot = new ItemStackResponseSlot(
                networkSlot,
                networkSlot,
                current.isNull() ? 0 : current.getCount(),
                current.getStackNetId(),
                current.getCustomName(),
                current.getDamage()
        );
        return new ItemStackResponseContainer(slotData.getContainer(), List.of(slot), slotData.getDynamicId());
    }

    @Override
    public abstract ItemStackRequestActionType getType();
}
