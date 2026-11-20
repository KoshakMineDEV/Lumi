package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerTransferItemEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestActionType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.SwapAction;

import java.util.ArrayList;
import java.util.List;

public class SwapActionProcessor implements ItemStackRequestActionProcessor<SwapAction> {

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.SWAP;
    }

    @Override
    public ActionResponse handle(SwapAction action, Player player, ItemStackRequestContext context) {
        ItemStackRequestSlotData src = action.getSource();
        ItemStackRequestSlotData dst = action.getDestination();

        Inventory srcInv = NetworkMapping.getInventory(player, src);
        Inventory dstInv = NetworkMapping.getInventory(player, dst);
        if (srcInv == null || dstInv == null) {
            return context.error();
        }

        int srcSlot = NetworkMapping.toInternalSlot(src.getContainer(), src.getSlot());
        int dstSlot = NetworkMapping.toInternalSlot(dst.getContainer(), dst.getSlot());

        if (ItemStackRequestContext.Slot.of(srcInv, srcSlot).equals(ItemStackRequestContext.Slot.of(dstInv, dstSlot))
                || TransferItemActionProcessor.isCreatedOutput(srcInv, srcSlot)
                || TransferItemActionProcessor.isCreatedOutput(dstInv, dstSlot)) {
            return context.error();
        }

        Item sourceItem = srcInv.getUnclonedItem(srcSlot);
        Item destItem = dstInv.getUnclonedItem(dstSlot);

        if (validateStackNetworkId(sourceItem.getStackNetId(), src.getStackNetworkId())) {
            return context.error();
        }
        if (validateStackNetworkId(destItem.getStackNetId(), dst.getStackNetworkId())) {
            return context.error();
        }

        if (!TransferItemActionProcessor.isSlotCompatible(srcInv, srcSlot, destItem)) {
            return context.error();
        }
        if (!TransferItemActionProcessor.isSlotCompatible(dstInv, dstSlot, sourceItem)) {
            return context.error();
        }

        // Swap is structurally a bi-directional transfer; emit one event with
        // both sides populated so plugins can veto or observe it consistently
        // with TAKE/PLACE and DROP. count uses the source stack size — there
        // is no partial swap on the wire.
        int count = sourceItem.isNull() ? destItem.getCount() : sourceItem.getCount();
        if (!TransferItemActionProcessor.fireTransferEvent(player, PlayerTransferItemEvent.Type.SWAP,
                srcInv, srcSlot, dstInv, dstSlot, sourceItem, destItem, count)) {
            return context.error();
        }

        List<InventoryAction> transactionActions = new ArrayList<>();
        transactionActions.add(new SlotChangeAction(srcInv, srcSlot, sourceItem, destItem));
        transactionActions.add(new SlotChangeAction(dstInv, dstSlot, destItem, sourceItem));
        var transaction = new TransferItemActionProcessor.EventOnlyInventoryTransaction(player, transactionActions);
        if (!transaction.execute()) {
            return context.error();
        }

        if (!context.writeSwap(srcInv, srcSlot, sourceItem, dstInv, dstSlot, destItem)) {
            return context.error();
        }

        return context.success(List.of(TransferItemActionProcessor.buildContainer(srcInv, srcSlot, src),
                TransferItemActionProcessor.buildContainer(dstInv, dstSlot, dst)));
    }
}
