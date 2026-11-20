package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Translate between the network-level {@link ContainerSlotType} / slot index pair
 * and the server-side {@link Inventory} / internal slot pair. Used throughout the
 * ItemStackRequest processor chain to resolve action targets.
 * <p>
 * Adapted from PowerNukkitX (<a href="https://github.com/PowerNukkitX/PowerNukkitX">PowerNukkitX</a>)
 */
public final class NetworkMapping {

    private static final int WORKBENCH_RESULT_SLOT = 9;

    @Nullable
    public static Inventory getInventory(Player player, ItemStackRequestSlotData data) {
        if (data == null || data.getContainer() == null || data.getSlot() < 0) {
            return null;
        }
        ContainerSlotType type = data.getContainer();
        int wireSlot = data.getSlot();
        boolean valid = switch (type) {
            case HOTBAR -> wireSlot < 9;
            case INVENTORY -> wireSlot >= 9 && wireSlot < 36;
            case HOTBAR_AND_INVENTORY -> wireSlot < 36;
            case ARMOR -> wireSlot < 4;
            case OFFHAND -> wireSlot <= 1;
            case CURSOR -> wireSlot == 0;
            case CRAFTING_OUTPUT -> wireSlot == 0 || wireSlot == WORKBENCH_RESULT_SLOT
                    || wireSlot == PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT;
            case CREATED_OUTPUT -> wireSlot == 0 || wireSlot == PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT;
            case TRADE2_INGREDIENT_1 -> wireSlot == 0 || wireSlot == TradeInventory.TRADE_INPUT1_UI_SLOT;
            case TRADE2_INGREDIENT_2 -> wireSlot == 1 || wireSlot == TradeInventory.TRADE_INPUT2_UI_SLOT;
            case TRADE_INGREDIENT_1 -> wireSlot == 0 || wireSlot == TradeInventory.LEGACY_TRADE_INPUT1_UI_SLOT;
            case TRADE_INGREDIENT_2 -> wireSlot == 1 || wireSlot == TradeInventory.LEGACY_TRADE_INPUT2_UI_SLOT;
            case TRADE_RESULT, TRADE2_RESULT -> wireSlot == 2 || wireSlot == PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT;
            case DYNAMIC_CONTAINER -> player.protocol >= ProtocolInfo.v1_21_40
                    && data.getDynamicId() != null && data.getDynamicId() > 0;
            default -> true;
        };
        if (!valid) {
            return null;
        }
        Inventory inventory = getInventory(player, type, data.getDynamicId());
        if (inventory == null || inventory instanceof BaseInventory base && base.destroyed) {
            return null;
        }
        int slot = toInternalSlot(type, wireSlot);
        int size = inventory.getSize() + (type == ContainerSlotType.ARMOR ? 4 : 0);
        if (type == ContainerSlotType.CRAFTING_INPUT && inventory.getType() == InventoryType.WORKBENCH) {
            size = Math.min(size, WORKBENCH_RESULT_SLOT);
        }
        return slot >= 0 && slot < size ? inventory : null;
    }

    @Nullable
    private static Inventory getInventory(Player player, ContainerSlotType type, @Nullable Integer dynamicId) {
        Inventory topWindow = player.getTopWindow().orElse(null);
        if (topWindow instanceof FakeBlockUIComponent fakeUI && isSlotTypeCompatibleWithFakeUI(fakeUI, type)) {
            return fakeUI;
        }

        PlayerUIInventory ui = player.getUIInventory();
        return switch (type) {
            case CURSOR -> ui.getCursorInventory();
            case CREATED_OUTPUT -> ui; // slot 50 of PlayerUIInventory hosts the created output
            case CRAFTING_INPUT -> {
                if (topWindow instanceof CraftingGrid) {
                    yield topWindow;
                }
                Inventory workbench = viewedContainer(player, topWindow, InventoryType.WORKBENCH);
                yield workbench != null ? workbench : player.getCraftingGrid();
            }
            case CRAFTING_OUTPUT -> viewedContainer(player, topWindow, InventoryType.WORKBENCH);
            case HOTBAR, INVENTORY, HOTBAR_AND_INVENTORY, ARMOR -> player.getInventory();
            case OFFHAND -> player.getOffhandInventory();
            case TRADE_INGREDIENT_1, TRADE_INGREDIENT_2, TRADE2_INGREDIENT_1, TRADE2_INGREDIENT_2 ->
                    topWindow instanceof TradeInventory trade && trade.getViewers().contains(player) ? trade : null;
            case TRADE_RESULT, TRADE2_RESULT ->
                    topWindow instanceof TradeInventory trade && trade.getViewers().contains(player) ? ui : null;
            case ANVIL_INPUT, ANVIL_MATERIAL, ANVIL_RESULT -> typedContainer(topWindow, AnvilInventory.class);
            case ENCHANTING_INPUT, ENCHANTING_MATERIAL -> typedContainer(topWindow, EnchantInventory.class);
            case GRINDSTONE_INPUT, GRINDSTONE_ADDITIONAL, GRINDSTONE_RESULT ->
                    typedContainer(topWindow, GrindstoneInventory.class);
            case SMITHING_TABLE_INPUT, SMITHING_TABLE_MATERIAL, SMITHING_TABLE_RESULT, SMITHING_TABLE_TEMPLATE ->
                    typedContainer(topWindow, SmithingInventory.class);
            case LOOM_INPUT, LOOM_DYE, LOOM_MATERIAL, LOOM_RESULT -> typedContainer(topWindow, LoomInventory.class);
            case STONECUTTER_INPUT, STONECUTTER_RESULT -> typedContainer(topWindow, StonecutterInventory.class);
            case CARTOGRAPHY_INPUT, CARTOGRAPHY_ADDITIONAL, CARTOGRAPHY_RESULT ->
                    topWindow instanceof CartographyTableInventory ? topWindow : null;
            case BEACON_PAYMENT -> typedContainer(topWindow, BeaconInventory.class);
            case COMPOUND_CREATOR_INPUT, COMPOUND_CREATOR_OUTPUT,
                 ELEMENT_CONSTRUCTOR_OUTPUT,
                 MATERIAL_REDUCER_INPUT, MATERIAL_REDUCER_OUTPUT,
                 LAB_TABLE_INPUT -> null;
            case FURNACE_FUEL, FURNACE_INGREDIENT, FURNACE_RESULT -> topWindow instanceof FurnaceInventory
                    ? topWindow : viewedContainer(player, topWindow, InventoryType.FURNACE);
            case BLAST_FURNACE_INGREDIENT, SMOKER_INGREDIENT -> typedContainer(topWindow, FurnaceInventory.class);
            case BREWING_INPUT, BREWING_RESULT, BREWING_FUEL -> topWindow instanceof BrewingInventory
                    ? topWindow : viewedContainer(player, topWindow, InventoryType.BREWING_STAND);
            case SHULKER_BOX -> topWindow instanceof ShulkerBoxInventory
                    ? topWindow : viewedContainer(player, topWindow, InventoryType.SHULKER_BOX);
            case BARREL -> typedContainer(topWindow, BarrelInventory.class);
            case LEVEL_ENTITY -> topWindow instanceof PlayerUIComponent ? null : topWindow;
            case DYNAMIC_CONTAINER -> resolveDynamicContainer(player, dynamicId);
            default -> null;
        };
    }

    @Nullable
    private static Inventory typedContainer(@Nullable Inventory topWindow, Class<? extends Inventory> expected) {
        return expected.isInstance(topWindow) ? topWindow : null;
    }

    @Nullable
    private static Inventory viewedContainer(Player player, @Nullable Inventory inventory, InventoryType type) {
        return inventory != null && inventory.getType() == type && inventory.getViewers().contains(player)
                ? inventory : null;
    }

    private static boolean isSlotTypeCompatibleWithFakeUI(FakeBlockUIComponent fakeUI, ContainerSlotType type) {
        return switch (fakeUI.getFakeBlockType()) {
            case ANVIL -> type == ContainerSlotType.ANVIL_INPUT
                    || type == ContainerSlotType.ANVIL_MATERIAL
                    || type == ContainerSlotType.ANVIL_RESULT;
            case ENCHANT_TABLE -> type == ContainerSlotType.ENCHANTING_INPUT
                    || type == ContainerSlotType.ENCHANTING_MATERIAL;
            case BEACON -> type == ContainerSlotType.BEACON_PAYMENT;
            case LOOM -> type == ContainerSlotType.LOOM_INPUT
                    || type == ContainerSlotType.LOOM_DYE
                    || type == ContainerSlotType.LOOM_MATERIAL
                    || type == ContainerSlotType.LOOM_RESULT;
            case SMITHING_TABLE -> type == ContainerSlotType.SMITHING_TABLE_INPUT
                    || type == ContainerSlotType.SMITHING_TABLE_MATERIAL
                    || type == ContainerSlotType.SMITHING_TABLE_RESULT
                    || type == ContainerSlotType.SMITHING_TABLE_TEMPLATE;
            case GRINDSTONE -> type == ContainerSlotType.GRINDSTONE_INPUT
                    || type == ContainerSlotType.GRINDSTONE_ADDITIONAL
                    || type == ContainerSlotType.GRINDSTONE_RESULT;
            case STONECUTTER -> type == ContainerSlotType.STONECUTTER_INPUT
                    || type == ContainerSlotType.STONECUTTER_RESULT;
            case CARTOGRAPHY -> type == ContainerSlotType.CARTOGRAPHY_INPUT
                    || type == ContainerSlotType.CARTOGRAPHY_ADDITIONAL
                    || type == ContainerSlotType.CARTOGRAPHY_RESULT;
            default -> false;
        };
    }

    public static int toInternalSlot(ContainerSlotType type, int networkSlot) {
        return switch (type) {
            case HOTBAR, HOTBAR_AND_INVENTORY, INVENTORY -> networkSlot;
            case ARMOR -> networkSlot + 36;
            case CURSOR, OFFHAND, BEACON_PAYMENT -> 0;
            case CREATED_OUTPUT -> 50;
            case CRAFTING_OUTPUT -> WORKBENCH_RESULT_SLOT;
            case TRADE_INGREDIENT_1, TRADE2_INGREDIENT_1 -> 0;
            case TRADE_INGREDIENT_2, TRADE2_INGREDIENT_2 -> 1;
            case TRADE_RESULT, TRADE2_RESULT -> PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT;
            case CRAFTING_INPUT -> {
                if (networkSlot >= 32 && networkSlot <= 40) {
                    yield networkSlot - 32;
                }
                if (networkSlot >= 28 && networkSlot <= 31) {
                    yield networkSlot - 28;
                }
                yield networkSlot;
            }
            case ANVIL_INPUT, ANVIL_MATERIAL, ANVIL_RESULT -> mapRange(networkSlot, 1, 3);
            case STONECUTTER_INPUT, STONECUTTER_RESULT -> mapRange(networkSlot, 3, 4);
            case LOOM_INPUT, LOOM_DYE, LOOM_MATERIAL, LOOM_RESULT -> mapRange(networkSlot, 9, 12);
            case CARTOGRAPHY_INPUT, CARTOGRAPHY_ADDITIONAL, CARTOGRAPHY_RESULT -> mapRange(networkSlot, 12, 14);
            case ENCHANTING_INPUT, ENCHANTING_MATERIAL -> mapRange(networkSlot, 14, 15);
            case GRINDSTONE_INPUT, GRINDSTONE_ADDITIONAL, GRINDSTONE_RESULT -> mapRange(networkSlot, 16, 18);
            case SMITHING_TABLE_INPUT, SMITHING_TABLE_MATERIAL, SMITHING_TABLE_TEMPLATE, SMITHING_TABLE_RESULT ->
                    mapRange(networkSlot, 51, 54);
            default -> networkSlot;
        };
    }

    private static int mapRange(int networkSlot, int firstNetworkSlot, int lastNetworkSlot) {
        if (networkSlot >= firstNetworkSlot && networkSlot <= lastNetworkSlot) {
            return networkSlot - firstNetworkSlot;
        }
        return networkSlot;
    }

    @Nullable
    private static Inventory resolveDynamicContainer(Player player, @Nullable Integer dynamicId) {
        BundleHolderRef ref = dynamicId == null ? null : findBundleHolder(player, dynamicId);
        return ref == null ? null : ref.chain().getLast().getInventory();
    }

    public record BundleHolderRef(Inventory outer, int outerSlot, List<ItemBundle> chain) {
    }

    @Nullable
    public static BundleHolderRef findBundleHolder(Player player, int dynamicId) {
        LinkedHashSet<Inventory> inventories = new LinkedHashSet<>();
        player.getTopWindow().ifPresent(inventories::add);
        inventories.add(player.getInventory());
        inventories.add(player.getOffhandInventory());
        inventories.add(player.getCursorInventory());
        inventories.add(player.getCraftingGrid());

        Set<ItemBundle> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<BundleHolderRef> matches = new ArrayList<>(2);
        List<ItemBundle> chain = new ArrayList<>();
        for (Inventory inventory : inventories) {
            if (inventory == null || inventory instanceof BaseInventory base && base.destroyed) {
                continue;
            }
            for (int slot = 0; slot < inventory.getSize() && matches.size() < 2; slot++) {
                findBundle(inventory.getItemFast(slot), dynamicId, inventory, slot, seen, chain, matches);
            }
        }
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private static void findBundle(Item item, int dynamicId, Inventory outer, int outerSlot,
                                   Set<ItemBundle> seen, List<ItemBundle> chain, List<BundleHolderRef> matches) {
        if (!(item instanceof ItemBundle bundle) || !seen.add(bundle) || chain.size() >= BundleInventory.MAX_FILL / 4) {
            return;
        }
        chain.add(bundle);
        if (bundle.getBundleId() == dynamicId) {
            matches.add(new BundleHolderRef(outer, outerSlot, List.copyOf(chain)));
        }
        for (Item nested : bundle.getInventory().slots.values()) {
            if (matches.size() >= 2) {
                break;
            }
            findBundle(nested, dynamicId, outer, outerSlot, seen, chain, matches);
        }
        chain.removeLast();
    }
}
