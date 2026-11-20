package cn.nukkit.inventory.transaction.action;

import cn.nukkit.Player;
import cn.nukkit.inventory.TradeInventory;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.item.Item;
import lombok.Getter;

@Getter
public class TradeAction extends InventoryAction {

    private final TradeInventory inventory;
    private final int type;

    public TradeAction(Item sourceItem, Item targetItem, int windowId, TradeInventory inventory) {
        super(sourceItem, targetItem);
        this.type = windowId;
        this.inventory = inventory;
    }

    @Override
    public boolean isValid(Player source) {
        return this.inventory.getViewers().contains(source);
    }

    @Override
    public boolean execute(Player source) {
        return true;
    }

    @Override
    public void onExecuteSuccess(Player source) {
    }

    @Override
    public void onExecuteFail(Player source) {
    }

    @Override
    public void onAddToTransaction(InventoryTransaction transaction) {
        transaction.addInventory(this.inventory);
    }
}
