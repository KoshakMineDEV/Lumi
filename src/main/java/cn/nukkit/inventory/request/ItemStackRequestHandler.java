package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.event.inventory.ItemStackRequestActionEvent;
import cn.nukkit.inventory.BundleInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerUIComponent;
import cn.nukkit.inventory.PlayerUIInventory;
import cn.nukkit.inventory.TradeInventory;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.network.protocol.ItemStackResponsePacket;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.*;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponse;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseSlot;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Central dispatcher for incoming ItemStackRequest payloads. Iterates the action
 * chain in each request, delegates each action to its registered processor, and
 * emits a single ItemStackResponsePacket summarising success/failure per request.
 * <p>
 * Adapted from PowerNukkitX (<a href="https://github.com/PowerNukkitX/PowerNukkitX">PowerNukkitX</a>)
 */
@Slf4j
public final class ItemStackRequestHandler {

    private static final EnumMap<ItemStackRequestActionType, ItemStackRequestActionProcessor<?>> PROCESSORS =
            new EnumMap<>(ItemStackRequestActionType.class);

    static {
        register(new TakeActionProcessor());
        register(new PlaceActionProcessor());
        register(new TakeFromItemContainerActionProcessor());
        register(new PlaceInItemContainerActionProcessor());
        register(new SwapActionProcessor());
        register(new DropActionProcessor());
        register(new DestroyActionProcessor());
        register(new ConsumeActionProcessor());
        register(new CreateActionProcessor());
        register(new CraftRecipeActionProcessor());
        register(new CraftRecipeAutoProcessor());
        register(new CraftCreativeActionProcessor());
        register(new CraftRecipeOptionalProcessor());
        register(new CraftGrindstoneActionProcessor());
        register(new CraftLoomActionProcessor());
        register(new CraftNonImplementedActionProcessor());
        register(new CraftResultDeprecatedActionProcessor());
        register(new MineBlockActionProcessor());
        register(new BeaconPaymentActionProcessor());
    }

    public static void register(ItemStackRequestActionProcessor<?> processor) {
        PROCESSORS.put(processor.getType(), processor);
    }

    public static void handleRequests(Player player, List<ItemStackRequest> requests) {
        TradeInventory trade = player.getTradeInventory();
        if (trade == null) {
            processRequests(player, requests);
        } else {
            trade.withInputUpdates(() -> processRequests(player, requests));
        }
    }

    @SuppressWarnings("unchecked")
    private static void processRequests(Player player, List<ItemStackRequest> requests) {
        List<ItemStackResponse> responses = new ArrayList<>(requests.size());
        Set<Inventory> resyncInventories = new LinkedHashSet<>();
        Set<Inventory> viewerInventories = new LinkedHashSet<>();
        Set<Integer> updatedBundles = new LinkedHashSet<>();
        Set<ItemStackRequestContext.EquipmentChange> equipmentChanges =
                EnumSet.noneOf(ItemStackRequestContext.EquipmentChange.class);
        boolean resync = false;

        for (ItemStackRequest request : requests) {
            ItemStackRequestAction[] actions = request.getActions();
            ItemStackRequestContext context = new ItemStackRequestContext(request);
            List<ItemStackResponseContainer> responseContainers = new ArrayList<>();
            Set<Inventory> affectedInventories = new LinkedHashSet<>();
            Set<Integer> affectedBundles = new LinkedHashSet<>();
            boolean error = player.isSpectator() || actions.length == 0 || actions.length > 128;

            for (int i = 0; !error && i < actions.length; i++) {
                ItemStackRequestAction action = actions[i];
                context.setCurrentActionIndex(i);
                affectedInventories.addAll(resolveAffectedInventories(player, action));
                Set<NetworkMapping.BundleHolderRef> bundleRoots = resolveAffectedBundleOuters(player, action);
                for (NetworkMapping.BundleHolderRef root : bundleRoots) {
                    context.rememberBundleRoot(root);
                    affectedBundles.add(root.chain().getLast().getBundleId());
                }

                ItemStackRequestActionProcessor<ItemStackRequestAction> processor =
                        (ItemStackRequestActionProcessor<ItemStackRequestAction>) PROCESSORS.get(action.getType());

                if (processor == null) {
                    // An unregistered action type must not be skipped: later actions would build on a
                    // step that never happened, silently desyncing client and server; fail and roll back.
                    log.warn("{}: unhandled item stack request action {}, refusing request {}",
                            player.getName(), action.getType(), request.getRequestId());
                    error = true;
                    break;
                }

                try {
                    ItemStackRequestActionEvent event = new ItemStackRequestActionEvent(player, action, i);
                    player.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        error = true;
                        break;
                    }
                    if (event.getResponse() != null) {
                        if (!event.getResponse().success()) {
                            error = true;
                            break;
                        }
                        responseContainers.addAll(event.getResponse().containers());
                        continue;
                    }

                    if (startsRecipe(action) && !context.beginRecipe()) {
                        error = true;
                        break;
                    }
                    ActionResponse response = processor.handle(action, player, context);
                    for (NetworkMapping.BundleHolderRef root : bundleRoots) {
                        if (root.chain().getFirst().getInventory().getWeight() > BundleInventory.MAX_FILL) {
                            error = true;
                            break;
                        }
                        saveBundleChain(root);
                    }
                    if (error) {
                        break;
                    }
                    if (response == null) {
                        continue;
                    }
                    if (!response.success()) {
                        error = true;
                        break;
                    }
                    responseContainers.addAll(response.containers());
                } catch (Exception e) {
                    log.error("{}: error processing item stack request action {}", player.getName(), action.getType(), e);
                    error = true;
                    break;
                }
            }

            if (!error && context.hasRecipe()) {
                error = !context.finishOutput(player.getUIInventory(), player.isCreative());
            }
            if (!error) {
                if (!context.commit()) {
                    log.warn("{}: item stack request {} commit partially failed", player.getName(), request.getRequestId());
                }
            }

            if (error) {
                context.rollback();
                resync = true;
                resyncInventories.addAll(affectedInventories);
            } else {
                context.collectEquipmentChanges(player, equipmentChanges);
            }

            viewerInventories.addAll(affectedInventories);
            updatedBundles.addAll(affectedBundles);
            ItemStackResponseStatus status = error ? ItemStackResponseStatus.ERROR : ItemStackResponseStatus.OK;
            responses.add(new ItemStackResponse(
                    status,
                    request.getRequestId(),
                    error ? List.of() : compactContainers(responseContainers)
            ));
        }

        ItemStackResponsePacket packet = new ItemStackResponsePacket();
        packet.entries.addAll(responses);
        packet.protocol = player.protocol;
        player.dataPacket(packet);
        if (resync) {
            resyncActor(player, resyncInventories);
        }
        for (ItemStackRequestContext.EquipmentChange change : equipmentChanges) {
            switch (change) {
                case ARMOR -> player.getInventory().sendArmorContents(player.getViewers().values());
                case HAND -> player.getInventory().sendHeldItem(player.getViewers().values());
                case OFFHAND -> player.getOffhandInventory().sendContents(player.getViewers().values());
            }
        }
        syncAffectedInventories(player, viewerInventories);
        syncAffectedBundleOuters(player, updatedBundles);
    }

    private static Set<Inventory> resolveAffectedInventories(Player player, ItemStackRequestAction action) {
        LinkedHashSet<Inventory> affected = new LinkedHashSet<>();

        try {
            if (action instanceof TransferItemStackRequestAction transfer) {
                addAffectedInventory(affected, player, transfer.getSource());
                addAffectedInventory(affected, player, transfer.getDestination());
            } else if (action instanceof SwapAction swap) {
                addAffectedInventory(affected, player, swap.getSource());
                addAffectedInventory(affected, player, swap.getDestination());
            } else if (action instanceof DropAction drop) {
                addAffectedInventory(affected, player, drop.getSource());
            } else if (action instanceof DestroyAction destroy) {
                addAffectedInventory(affected, player, destroy.getSource());
            } else if (action instanceof ConsumeAction consume) {
                addAffectedInventory(affected, player, consume.getSource());
            } else if (writesCreatedOutput(action)) {
                affected.add(player.getUIInventory());
            }
        } catch (Throwable t) {
            log.debug("{}: failed to resolve affected inventories for action {}", player.getName(), action.getType(), t);
        }

        return affected;
    }

    private static void addAffectedInventory(Set<Inventory> affected, Player player, ItemStackRequestSlotData slotData) {
        Inventory inventory = NetworkMapping.getInventory(player, slotData);
        Inventory canonical = canonicalizeInventory(inventory);
        if (canonical != null) {
            affected.add(canonical);
        }
    }

    private static boolean startsRecipe(ItemStackRequestAction action) {
        return action instanceof CraftRecipeAction
                || action instanceof AutoCraftRecipeAction
                || action instanceof CraftCreativeAction
                || action instanceof CraftRecipeOptionalAction
                || action instanceof CraftGrindstoneAction
                || action instanceof CraftLoomAction;
    }

    private static boolean writesCreatedOutput(ItemStackRequestAction action) {
        return startsRecipe(action) || action instanceof CreateAction || action instanceof CraftResultsDeprecatedAction;
    }

    static Inventory canonicalizeInventory(Inventory inventory) {
        if (inventory instanceof PlayerUIComponent component) {
            return component.getBackingInventory();
        }
        return inventory;
    }

    private static void resyncActor(Player actor, Collection<Inventory> inventories) {
        actor.getCursorInventory().sendContents(actor);
        actor.sendAllInventories();
        actor.getInventory().sendHeldItem(actor);
        actor.getInventory().sendArmorContents(actor);
        actor.getOffhandInventory().sendContents(actor);

        for (Inventory inventory : inventories) {
            if (inventory == null || inventory instanceof PlayerInventory || inventory instanceof PlayerUIInventory) {
                continue;
            }
            inventory.sendContents(actor);
        }
    }

    private static Set<NetworkMapping.BundleHolderRef> resolveAffectedBundleOuters(Player player, ItemStackRequestAction action) {
        LinkedHashSet<NetworkMapping.BundleHolderRef> refs = new LinkedHashSet<>();

        try {
            if (action instanceof TransferItemStackRequestAction transfer) {
                collectBundleOuter(refs, player, transfer.getSource());
                collectBundleOuter(refs, player, transfer.getDestination());
            } else if (action instanceof SwapAction swap) {
                collectBundleOuter(refs, player, swap.getSource());
                collectBundleOuter(refs, player, swap.getDestination());
            } else if (action instanceof DropAction drop) {
                collectBundleOuter(refs, player, drop.getSource());
            } else if (action instanceof DestroyAction destroy) {
                collectBundleOuter(refs, player, destroy.getSource());
            } else if (action instanceof ConsumeAction consume) {
                collectBundleOuter(refs, player, consume.getSource());
            }
        } catch (Throwable t) {
            log.debug("{}: failed to resolve bundle outer for action {}", player.getName(), action.getType(), t);
        }

        return refs;
    }

    private static void collectBundleOuter(Set<NetworkMapping.BundleHolderRef> refs, Player player, ItemStackRequestSlotData slotData) {
        if (slotData == null || slotData.getContainer() != ContainerSlotType.DYNAMIC_CONTAINER) {
            return;
        }
        Integer dynamicId = slotData.getDynamicId();
        if (dynamicId == null) {
            return;
        }
        NetworkMapping.BundleHolderRef ref = NetworkMapping.findBundleHolder(player, dynamicId);
        if (ref != null) {
            refs.add(ref);
        }
    }

    private static void syncAffectedInventories(Player actor, Set<Inventory> inventories) {
        for (Inventory inventory : inventories) {
            try {
                InventoryObserverSync.syncOtherViewers(actor, inventory);
            } catch (Throwable t) {
                log.debug("{}: failed to sync observers for inventory {}", actor.getName(), inventory.getClass().getName(), t);
            }
        }
    }

    private static void saveBundleChain(NetworkMapping.BundleHolderRef root) {
        for (ItemBundle bundle : root.chain().reversed()) {
            bundle.saveNBT();
        }
    }

    private static void syncAffectedBundleOuters(Player actor, Set<Integer> bundles) {
        for (int bundleId : bundles) {
            NetworkMapping.BundleHolderRef root = NetworkMapping.findBundleHolder(actor, bundleId);
            if (root != null) {
                InventoryObserverSync.resendOuterSlot(actor, root.outer(), root.outerSlot());
            }
        }
    }

    private static List<ItemStackResponseContainer> compactContainers(List<ItemStackResponseContainer> containers) {
        LinkedHashMap<FullContainerName, LinkedHashMap<Long, ItemStackResponseSlot>> merged = new LinkedHashMap<>();

        for (ItemStackResponseContainer container : containers) {
            FullContainerName containerName = container.getContainerName();
            LinkedHashMap<Long, ItemStackResponseSlot> items = merged.computeIfAbsent(containerName, ignored -> new LinkedHashMap<>());
            for (ItemStackResponseSlot slot : container.getItems()) {
                long key = ((long) slot.getSlot() << 32) | (slot.getHotbarSlot() & 0xFFFFFFFFL);
                items.put(key, slot);
            }
        }

        List<ItemStackResponseContainer> compacted = new ArrayList<>(merged.size());
        for (var entry : merged.entrySet()) {
            compacted.add(new ItemStackResponseContainer(
                    entry.getKey().getContainer(),
                    new ArrayList<>(entry.getValue().values()),
                    entry.getKey().getDynamicId()
            ));
        }
        return compacted;
    }
}
