package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.inventory.BeaconInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.DestroyAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestActionType;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;

import java.util.ArrayList;
import java.util.List;

public class DestroyActionProcessor implements ItemStackRequestActionProcessor<DestroyAction> {

    public static final String NO_RESPONSE_DESTROY_KEY = "noResponseForDestroyAction";

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.DESTROY;
    }

    @Override
    public ActionResponse handle(DestroyAction action, Player player, ItemStackRequestContext context) {
        boolean suppressResponse = Boolean.TRUE.equals(context.get(NO_RESPONSE_DESTROY_KEY));

        ItemStackRequestSlotData src = action.getSource();
        Inventory inventory = NetworkMapping.getInventory(player, src);
        if (inventory == null) {
            return context.error();
        }
        boolean isCreatedOutputCleanup = suppressResponse
                && src.getContainer() == ContainerSlotType.CREATED_OUTPUT;
        if (!isCreatedOutputCleanup && !player.isCreative() && !(inventory instanceof BeaconInventory)) {
            return context.error();
        }

        int slot = NetworkMapping.toInternalSlot(src.getContainer(), src.getSlot());
        int count = action.getCount();
        if (count <= 0) {
            return context.error();
        }

        Item item = inventory.getUnclonedItem(slot);
        if (item.isNull() || item.getCount() < count) {
            return context.error();
        }
        if (validateStackNetworkId(item.getStackNetId(), src.getStackNetworkId())) {
            return context.error();
        }

        Item targetItem;
        if (item.getCount() == count) {
            targetItem = Item.get(Item.AIR);
        } else {
            targetItem = item.clone();
            targetItem.setCount(item.getCount() - count);
        }

        List<InventoryAction> transactionActions = new ArrayList<>();
        transactionActions.add(new SlotChangeAction(inventory, slot, item, targetItem));
        var transaction = new TransferItemActionProcessor.EventOnlyInventoryTransaction(player, transactionActions);
        if (!transaction.execute()) {
            return context.error();
        }

        if (!context.writeSlot(inventory, slot, item, targetItem)) {
            return context.error();
        }

        ItemStackResponseContainer container = TransferItemActionProcessor.buildContainer(inventory, slot, src);
        if (suppressResponse) {
            return null;
        }
        return context.success(List.of(container));
    }
}
