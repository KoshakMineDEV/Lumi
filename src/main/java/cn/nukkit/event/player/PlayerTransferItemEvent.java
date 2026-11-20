package cn.nukkit.event.player;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import org.jetbrains.annotations.Nullable;

public class PlayerTransferItemEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public enum Type {
        TRANSFER,
        SWAP,
        DROP
    }

    private final Type type;
    private final Inventory sourceInventory;
    private final int sourceSlot;
    @Nullable
    private final Inventory destinationInventory;
    private final int destinationSlot;
    private final Item sourceItem;
    private final Item destinationItem;
    private final int count;

    public PlayerTransferItemEvent(Player player, Inventory sourceInventory, int sourceSlot,
                                   @Nullable Inventory destinationInventory, int destinationSlot,
                                   Item sourceItem, Item destinationItem, int count) {
        this(player, Type.TRANSFER, sourceInventory, sourceSlot,
                destinationInventory, destinationSlot, sourceItem, destinationItem, count);
    }

    public PlayerTransferItemEvent(Player player, Type type,
                                   Inventory sourceInventory, int sourceSlot,
                                   @Nullable Inventory destinationInventory, int destinationSlot,
                                   Item sourceItem, Item destinationItem, int count) {
        this.player = player;
        this.type = type;
        this.sourceInventory = sourceInventory;
        this.sourceSlot = sourceSlot;
        this.destinationInventory = destinationInventory;
        this.destinationSlot = destinationSlot;
        this.sourceItem = sourceItem;
        this.destinationItem = destinationItem;
        this.count = count;
    }

    public Type getType() {
        return type;
    }

    public Inventory getSourceInventory() {
        return sourceInventory;
    }

    public int getSourceSlot() {
        return sourceSlot;
    }

    @Nullable
    public Inventory getDestinationInventory() {
        return destinationInventory;
    }

    public int getDestinationSlot() {
        return destinationSlot;
    }

    public Item getSourceItem() {
        return sourceItem;
    }

    public Item getDestinationItem() {
        return destinationItem;
    }

    public int getCount() {
        return count;
    }
}
