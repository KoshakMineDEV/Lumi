package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerTransferItemEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.DropAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestActionType;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;

import java.util.ArrayList;
import java.util.List;

public class DropActionProcessor implements ItemStackRequestActionProcessor<DropAction> {

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.DROP;
    }

    @Override
    public ActionResponse handle(DropAction action, Player player, ItemStackRequestContext context) {
        ItemStackRequestSlotData src = action.getSource();
        Inventory inventory = NetworkMapping.getInventory(player, src);
        if (inventory == null) {
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
        boolean createdOutput = src.getContainer() == ContainerSlotType.CREATED_OUTPUT;
        if (createdOutput ? !context.hasRecipe() : validateStackNetworkId(item.getStackNetId(), src.getStackNetworkId())) {
            return context.error();
        }

        if (!drop(player, inventory, slot, item, count, createdOutput, context)) {
            return context.error();
        }

        ItemStackResponseContainer container = TransferItemActionProcessor.buildContainer(inventory, slot, src);
        return context.success(List.of(container));
    }

    public static boolean handleHotbarDrop(Player player, int slot, Item previous, Item remaining, Item dropped) {
        PlayerInventory inventory = player.getInventory();
        if (slot < 0 || slot >= inventory.getHotbarSize() || dropped.isNull()) {
            return false;
        }

        Item item = inventory.getItem(slot);
        int count = dropped.getCount();
        int remainingCount = remaining.isNull() ? 0 : remaining.getCount();
        if (item.isNull() || !item.equalsExact(previous) || item.getCount() != remainingCount + count
                || !item.equals(dropped, true, false) || (!remaining.isNull() && !item.equals(remaining, true, false))) {
            return false;
        }

        // Hotbar drops arrive without an ItemStackRequest or a response ID.
        ItemStackRequestContext context = new ItemStackRequestContext(null);
        if (!drop(player, inventory, slot, item, count, false, context)) {
            context.rollback();
            return false;
        }
        if (!context.commit()) {
            return false;
        }

        inventory.sendSlot(slot, player);
        if (inventory.getHeldItemIndex() == slot) {
            inventory.sendHeldItem(player.getViewers().values());
        }
        return true;
    }

    private static boolean drop(Player player, Inventory inventory, int slot, Item item, int count,
                                boolean createdOutput, ItemStackRequestContext context) {
        Item dropItem = item.clone();
        dropItem.setCount(count);

        if (!TransferItemActionProcessor.fireTransferEvent(player, PlayerTransferItemEvent.Type.DROP,
                inventory, slot, null, -1, item, null, count)) {
            return false;
        }

        PlayerDropItemEvent event = new PlayerDropItemEvent(player, dropItem);
        player.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        List<InventoryAction> transactionActions = new ArrayList<>();
        Item targetItem;
        if (item.getCount() == count) {
            targetItem = Item.get(Item.AIR);
        } else {
            targetItem = item.clone();
            targetItem.setCount(item.getCount() - count);
        }
        transactionActions.add(new SlotChangeAction(inventory, slot, item, targetItem));
        var transaction = new TransferItemActionProcessor.EventOnlyInventoryTransaction(player, transactionActions);
        if (!transaction.execute() || !player.spawned || !player.isAlive()) {
            return false;
        }

        boolean removed = createdOutput
                ? context.takeOutput(inventory, slot, item, targetItem)
                : context.writeSlot(inventory, slot, item, targetItem);
        if (!removed) {
            return false;
        }

        Item committedDrop = event.getItem().clone();
        context.onCommit(() -> player.dropItem(committedDrop));
        return true;
    }
}
