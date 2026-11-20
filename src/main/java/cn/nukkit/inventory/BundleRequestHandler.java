package cn.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.transaction.BundleTransaction;
import cn.nukkit.inventory.transaction.action.CreativeInventoryAction;
import cn.nukkit.inventory.transaction.action.DropItemAction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.ItemStackResponsePacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.DestroyAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.DropAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.SwapAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.TransferItemStackRequestAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponse;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseSlot;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseStatus;

import java.util.*;

public final class BundleRequestHandler {

    private final Player player;
    private final Map<RootSlot, Item> originals = new LinkedHashMap<>();
    private final Map<RootSlot, Item> staged = new LinkedHashMap<>();
    private final Map<ItemStackRequestSlotData, Slot> changed = new LinkedHashMap<>();
    private final Set<Sound> sounds = new LinkedHashSet<>();

    private BundleRequestHandler(Player player) {
        this.player = player;
        addInventory(player.getInventory());
        addInventory(player.getOffhandInventory());
        addInventory(player.getUIInventory());
        player.getTopWindow().ifPresent(this::addInventory);
    }

    public static void handle(Player player, List<ItemStackRequest> requests) {
        if (player.protocol < ProtocolInfo.v1_21_40 || !player.spawned || !player.isAlive()) {
            return;
        }
        for (ItemStackRequest request : requests) {
            ItemStackResponsePacket response = new ItemStackResponsePacket();
            BundleRequestHandler handler = new BundleRequestHandler(player);
            boolean success = !player.isSpectator() && handler.process(request);
            response.entries.add(new ItemStackResponse(success ? ItemStackResponseStatus.OK : ItemStackResponseStatus.ERROR,
                    request.getRequestId(), success ? handler.responses() : List.of()));
            player.dataPacket(response);
            handler.synchronize(success);
        }
    }

    private void addInventory(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            RootSlot slot = new RootSlot(inventory, i);
            Item item = inventory.getItem(i);
            originals.put(slot, item);
            staged.put(slot, item.clone());
        }
    }

    private boolean process(ItemStackRequest request) {
        if (request.getActions().length == 0 || request.getActions().length > 128) {
            return false;
        }
        boolean bundleTransfer = false;
        List<InventoryAction> actions = new ArrayList<>();
        for (ItemStackRequestAction action : request.getActions()) {
            if (action instanceof DropAction || action instanceof DestroyAction) {
                ItemStackRequestSlotData data = action instanceof DropAction drop ? drop.getSource() : ((DestroyAction) action).getSource();
                int count = action instanceof DropAction drop ? drop.getCount() : ((DestroyAction) action).getCount();
                Slot source = resolve(data);
                if (source == null || count <= 0 || !actions.isEmpty() || (action instanceof DestroyAction && !player.isCreative())) {
                    return false;
                }
                Item from = source.getItem();
                if (from.isNull() || count > from.getCount()) {
                    return false;
                }
                bundleTransfer |= !source.chain.isEmpty() || from instanceof ItemBundle;
                Item dropped = from.clone();
                dropped.setCount(count);
                from.setCount(from.getCount() - count);
                if (!source.setItem(from)) {
                    return false;
                }
                actions.add(action instanceof DropAction ? new DropItemAction(Item.AIR_ITEM.clone(), dropped)
                        : new CreativeInventoryAction(Item.AIR_ITEM.clone(), dropped, CreativeInventoryAction.TYPE_DELETE_ITEM));
                changed.put(data, source);
                continue;
            }
            if (action instanceof SwapAction swap) {
                Slot source = resolve(swap.getSource());
                Slot destination = resolve(swap.getDestination());
                if (source == null || destination == null || source.sameSlot(destination)) {
                    return false;
                }
                Item from = source.getItem();
                Item to = destination.getItem();
                bundleTransfer |= !source.chain.isEmpty() || !destination.chain.isEmpty()
                        || from instanceof ItemBundle || to instanceof ItemBundle;
                if (!source.setItem(Item.AIR_ITEM.clone()) || !destination.setItem(Item.AIR_ITEM.clone())
                        || !source.setItem(to) || !destination.setItem(from)) {
                    return false;
                }
                changed.put(swap.getSource(), source);
                changed.put(swap.getDestination(), destination);
                continue;
            }
            if (!(action instanceof TransferItemStackRequestAction transfer)) {
                return false;
            }
            Slot source = resolve(transfer.getSource());
            Slot destination = resolve(transfer.getDestination());
            if (source == null || destination == null || source.sameSlot(destination)) {
                return false;
            }
            Item from = source.getItem();
            Item to = destination.getItem();
            bundleTransfer |= !source.chain.isEmpty() || !destination.chain.isEmpty() || from instanceof ItemBundle;
            int count = transfer.getCount();
            if (count <= 0 || from.isNull() || count > from.getCount()
                    || (!to.isNull() && !from.equals(to))
                    || to.getCount() + count > from.getMaxStackSize()) {
                return false;
            }
            Item newSource = from.clone();
            newSource.setCount(from.getCount() - count);
            Item newDestination = from.clone();
            newDestination.setCount((to.isNull() ? 0 : to.getCount()) + count);
            if (!source.setItem(newSource) || !destination.setItem(newDestination)) {
                return false;
            }
            changed.put(transfer.getSource(), source);
            changed.put(transfer.getDestination(), destination);
            if (!source.chain.isEmpty()) {
                sounds.add(Sound.BUNDLE_REMOVE_ONE);
            }
            if (!destination.chain.isEmpty()) {
                sounds.add(Sound.BUNDLE_INSERT);
            }
        }
        if (!bundleTransfer) {
            return false;
        }

        for (var entry : staged.entrySet()) {
            RootSlot slot = entry.getKey();
            Item original = originals.get(slot);
            if (!original.equalsExact(entry.getValue())) {
                actions.add(new SlotChangeAction(slot.inventory, slot.slot, original, entry.getValue()));
            }
        }
        return new BundleTransaction(player, actions).execute();
    }

    private Slot resolve(ItemStackRequestSlotData data) {
        // Lumi's client inventory advertises the same dummy network ID for every non-empty stack.
        if (data.getContainer() == null || data.getSlot() < 0 || data.getStackNetworkId() > 1) {
            return null;
        }
        if (data.getContainer() == ContainerSlotType.DYNAMIC_CONTAINER) {
            if (data.getDynamicId() == null || data.getDynamicId() <= 0 || data.getSlot() >= BundleInventory.MAX_FILL) {
                return null;
            }
            Slot found = null;
            for (var entry : staged.entrySet()) {
                List<ItemBundle> chain = findBundle(entry.getValue(), data.getDynamicId(), new ArrayList<>(), new HashSet<>());
                if (chain != null) {
                    if (found != null) {
                        return null;
                    }
                    found = new Slot(entry.getKey(), chain, data.getSlot());
                }
            }
            return found;
        }

        Inventory inventory;
        int index = data.getSlot();
        switch (data.getContainer()) {
            case HOTBAR, INVENTORY, HOTBAR_AND_INVENTORY -> inventory = player.getInventory();
            case OFFHAND -> inventory = player.getOffhandInventory();
            case CURSOR -> {
                if (index != 0) {
                    return null;
                }
                inventory = player.getUIInventory();
            }
            case LEVEL_ENTITY, BARREL, SHULKER_BOX -> {
                inventory = player.getTopWindow().orElse(null);
                if (inventory == null || !isStorageInventory(inventory)) {
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
        RootSlot root = new RootSlot(inventory, index);
        return staged.containsKey(root) ? new Slot(root, List.of(), index) : null;
    }

    private boolean isStorageInventory(Inventory inventory) {
        return switch (inventory.getType()) {
            case CHEST, DOUBLE_CHEST, ENDER_CHEST, BARREL, SHULKER_BOX, HOPPER, DISPENSER, DROPPER,
                    CHEST_BOAT, MINECART_CHEST, MINECART_HOPPER -> true;
            default -> false;
        };
    }

    private List<ItemBundle> findBundle(Item item, int id, List<ItemBundle> parents, Set<Integer> visited) {
        if (!(item instanceof ItemBundle bundle) || !visited.add(bundle.getBundleId()) || parents.size() > BundleInventory.MAX_FILL / 4) {
            return null;
        }
        List<ItemBundle> chain = new ArrayList<>(parents);
        chain.add(bundle);
        if (bundle.getBundleId() == id) {
            return chain;
        }
        for (Item nested : bundle.getInventory().getContents().values()) {
            List<ItemBundle> found = findBundle(nested, id, chain, visited);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private List<ItemStackResponseContainer> responses() {
        Map<FullContainerName, List<ItemStackResponseSlot>> containers = new LinkedHashMap<>();
        for (var entry : changed.entrySet()) {
            ItemStackRequestSlotData data = entry.getKey();
            Item item = entry.getValue().getItem();
            ItemStackResponseSlot slot = new ItemStackResponseSlot(data.getSlot(), data.getSlot(), item.getCount(),
                    item.isNull() ? 0 : 1, item.getCustomName(), item.getDamage());
            containers.computeIfAbsent(new FullContainerName(data.getContainer(), data.getDynamicId()), key -> new ArrayList<>()).add(slot);
        }
        return containers.entrySet().stream().map(entry -> new ItemStackResponseContainer(entry.getKey().getContainer(),
                entry.getValue(), entry.getKey().getDynamicId())).toList();
    }

    private void synchronize(boolean success) {
        for (var entry : staged.entrySet()) {
            RootSlot root = entry.getKey();
            if (!originals.get(root).equalsExact(entry.getValue())) {
                root.inventory.sendSlot(root.slot, player);
            }
        }
        if (success && player.getLevel() != null) {
            for (Sound sound : sounds) {
                player.getLevel().addSound(player, sound);
            }
        }
    }

    private record RootSlot(Inventory inventory, int slot) {
    }

    private final class Slot {
        private final RootSlot root;
        private final List<ItemBundle> chain;
        private final int slot;

        private Slot(RootSlot root, List<ItemBundle> chain, int slot) {
            this.root = root;
            this.chain = chain;
            this.slot = slot;
        }

        private Item getItem() {
            return chain.isEmpty() ? staged.get(root).clone() : chain.getLast().getInventory().getItem(slot);
        }

        private boolean setItem(Item item) {
            if (chain.isEmpty()) {
                staged.put(root, item.isNull() ? Item.AIR_ITEM.clone() : item);
                return true;
            }
            if (!chain.getLast().getInventory().setItem(slot, item, false)) {
                return false;
            }
            for (int i = chain.size() - 1; i >= 0; i--) {
                ItemBundle bundle = chain.get(i);
                if (bundle.getInventory().getWeight() > BundleInventory.MAX_FILL) {
                    return false;
                }
                bundle.saveNBT();
            }
            return true;
        }

        private boolean sameSlot(Slot other) {
            return root.equals(other.root) && slot == other.slot && chain.equals(other.chain);
        }
    }
}
