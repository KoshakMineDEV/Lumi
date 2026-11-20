package cn.nukkit.inventory.transaction;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.TradeInventory;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.inventory.transaction.action.TradeAction;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TradingTransaction extends InventoryTransaction {

    private final TradeInventory tradeInventory;
    private int receivedActions;

    public TradingTransaction(Player source, List<InventoryAction> actions) {
        super(source, actions, false);
        this.tradeInventory = source.getTradeInventory();
        this.init(source, actions);
    }

    @Override
    public void addAction(InventoryAction action) {
        if (++this.receivedActions > 50) {
            this.invalid = true;
            return;
        }
        super.addAction(action);
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    public boolean isFor(TradeInventory inventory) {
        return this.tradeInventory == inventory;
    }

    @Override
    public boolean canExecute() {
        return !this.hasExecuted && this.validate() != null;
    }

    @Override
    public boolean execute() {
        if (!this.canExecute() || !this.callExecuteEvent()) {
            this.sendInventories();
            return false;
        }

        Settlement trade = this.validate();
        if (trade == null) {
            this.sendInventories();
            return false;
        }

        // Every slot was checked against its original stack after the events. Apply
        // all slot changes before sending updates so payment and delivery stay together.
        for (SlotChangeAction change : trade.changes()) {
            change.getInventory().setItemForce(change.getSlot(), change.getTargetItem());
        }
        this.hasExecuted = true;
        if (trade.offer() != null) {
            this.tradeInventory.completeTrade(this.source, trade.offer(), trade.times());
        }
        for (SlotChangeAction change : trade.changes()) {
            change.onExecuteSuccess(this.source);
        }
        return true;
    }

    private Settlement validate() {
        if (this.invalid || this.tradeInventory == null || this.source.isSpectator()
                || !this.tradeInventory.getViewers().contains(this.source)) {
            return null;
        }

        List<SlotChangeAction> changes = new ArrayList<>();
        TradeAction result = null;
        boolean usesInputs = false;
        Set<Slot> slots = new HashSet<>();
        for (InventoryAction action : this.actions) {
            if (action instanceof TradeAction marker && marker.getInventory() == this.tradeInventory) {
                if (marker.getType() == NetworkInventoryAction.SOURCE_TYPE_TRADING_OUTPUT) {
                    if (result != null && !result.getSourceItemUnsafe().equalsExact(marker.getSourceItemUnsafe())) {
                        return null;
                    }
                    result = marker;
                } else if (marker.getType() == NetworkInventoryAction.SOURCE_TYPE_TRADING_USE_INPUTS) {
                    usesInputs = true;
                }
            } else if (action instanceof SlotChangeAction change && this.isTradeSlot(change)
                    && slots.add(this.getSlot(change)) && change.isValid(this.source)) {
                changes.add(change);
            } else {
                return null;
            }
        }

        if (result == null) {
            if (!usesInputs && !changes.isEmpty()
                    && new InventoryTransaction(this.source, new ArrayList<>(changes)).canExecute()) {
                return new Settlement(null, 0, changes);
            }
            return null;
        }

        Item claimedOutput = result.getSourceItem();
        if (claimedOutput.isNull()) {
            return null;
        }

        for (TradeInventory.Offer offer : this.tradeInventory.getAssignedOffers()) {
            Item output = offer.getResult();
            if (!output.equals(claimedOutput, true, true) || claimedOutput.getCount() % output.getCount() != 0) {
                continue;
            }
            int times = this.matchTrade(offer, output, changes);
            if (times >= claimedOutput.getCount() / output.getCount() && this.tradeInventory.canUse(offer, times)) {
                return new Settlement(offer, times, changes);
            }
        }
        return null;
    }

    private boolean isTradeSlot(SlotChangeAction change) {
        Inventory inventory = change.getInventory();
        int slot = change.getSlot();
        if (inventory == this.tradeInventory) {
            return slot >= 0 && slot < 2 && slot < inventory.getSize();
        }
        if (inventory == this.source.getInventory()) {
            return slot >= 0 && slot < inventory.getSize();
        }
        return slot == 0 && (inventory == this.source.getUIInventory() || inventory == this.source.getCursorInventory());
    }

    private Slot getSlot(SlotChangeAction change) {
        Inventory inventory = change.getInventory();
        return new Slot(inventory == this.source.getCursorInventory() ? this.source.getUIInventory() : inventory, change.getSlot());
    }

    private int matchTrade(TradeInventory.Offer offer, Item output, List<SlotChangeAction> changes) {
        int[] consumed = new int[2];
        int gained = 0;
        for (SlotChangeAction change : changes) {
            Item before = change.getSourceItem();
            Item after = change.getTargetItem();
            int delta = after.getCount() - before.getCount();
            if (change.getInventory() == this.tradeInventory) {
                int slot = change.getSlot();
                if (delta >= 0 || !offer.matchesInput(slot, before) || !after.isNull() && !after.equals(before, true, true)) {
                    return 0;
                }
                consumed[slot] = -delta;
            } else {
                if (delta <= 0 || !output.equals(after, true, true) || !before.isNull() && !before.equals(output, true, true)) {
                    return 0;
                }
                gained += delta;
            }
        }
        int times = gained / output.getCount();
        return gained % output.getCount() == 0 && consumed[0] == offer.getInputCount(0) * times
                && consumed[1] == offer.getInputCount(1) * times ? times : 0;
    }

    private record Slot(Inventory inventory, int index) {
    }

    private record Settlement(TradeInventory.Offer offer, int times, List<SlotChangeAction> changes) {
    }
}
