package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.inventory.DoubleChestInventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.inventory.PlayerUIComponent;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ConsumeAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Per-request processing context shared across all action processors of a single
 * ItemStackRequest. Carries the original request, the current action index and an
 * opaque scratchpad for cross-action state (e.g. the recipe resolved by
 * CraftRecipeAction that CreateAction / CraftResultsDeprecated will consume).
 */
@Slf4j
public class ItemStackRequestContext {

    @Getter
    private final ItemStackRequest itemStackRequest;
    @Getter
    @Setter
    private int currentActionIndex;
    private final Map<String, Object> extraData = new HashMap<>();
    private final List<Runnable> commitActions = new ArrayList<>();
    private final Set<Integer> usedOutputs = new HashSet<>();
    private final Map<Slot, Item> originalItems = new LinkedHashMap<>();
    private Map<ConsumeAction, Item> expectedConsumes;
    private boolean recipeStarted;
    private boolean requireOutputTaken;
    private int outputIndex;

    public ItemStackRequestContext(ItemStackRequest itemStackRequest) {
        this.itemStackRequest = itemStackRequest;
    }

    public void put(String key, Object value) {
        extraData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) extraData.get(key);
    }

    public boolean has(String key) {
        return extraData.containsKey(key);
    }

    public void onCommit(Runnable action) {
        if (action != null) {
            commitActions.add(action);
        }
    }

    public boolean writeSlot(Inventory inventory, int index, Item expected, Item value) {
        if (!sameStack(inventory.getItemFast(index), expected)) {
            return false;
        }
        Slot slot = Slot.of(inventory, index);
        boolean remembered = this.originalItems.containsKey(slot);
        this.remember(slot, expected);
        boolean accepted = value.isNull() ? inventory.clear(index, false) : inventory.setItem(index, value, false);
        if (!accepted && !remembered) {
            this.originalItems.remove(slot);
        }
        return accepted && sameStack(inventory.getItemFast(index), value);
    }

    public boolean writeSwap(Inventory source, int sourceSlot, Item first, Inventory destination, int destinationSlot, Item second) {
        if (first instanceof ItemBundle && second.isNull()) {
            return this.writeSlot(source, sourceSlot, first, second)
                    && this.writeSlot(destination, destinationSlot, second, first);
        }
        if (second instanceof ItemBundle && first.isNull()) {
            return this.writeSlot(destination, destinationSlot, second, first)
                    && this.writeSlot(source, sourceSlot, first, second);
        }
        if (first instanceof ItemBundle || second instanceof ItemBundle) {
            Item air = Item.get(Item.AIR);
            return this.writeSlot(source, sourceSlot, first, air)
                    && this.writeSlot(destination, destinationSlot, second, first)
                    && this.writeSlot(source, sourceSlot, air, second);
        }
        return this.writeSlot(source, sourceSlot, first, second)
                && this.writeSlot(destination, destinationSlot, second, first);
    }

    boolean beginRecipe() {
        if (this.recipeStarted) {
            return false;
        }
        this.recipeStarted = true;
        return true;
    }

    boolean hasRecipe() {
        return this.recipeStarted;
    }

    void expectConsumes(List<ConsumeAction> actions, List<Item> items) {
        this.expectedConsumes = new IdentityHashMap<>();
        for (int i = 0; i < actions.size(); i++) {
            this.expectedConsumes.put(actions.get(i), items.get(i));
        }
    }

    boolean matchesExpectedConsume(ConsumeAction action, Item item) {
        if (this.expectedConsumes == null) {
            return true;
        }
        Item expected = this.expectedConsumes.get(action);
        return expected != null && expected.getStackNetId() == item.getStackNetId() && expected.equals(item, true, true);
    }

    void requireOutputTaken() {
        this.requireOutputTaken = true;
    }

    boolean finishOutput(Inventory inventory, boolean creative) {
        int index = PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT;
        Item output = inventory.getItemFast(index);
        if (output.isNull()) {
            return true;
        }
        return creative && !this.requireOutputTaken && this.writeSlot(inventory, index, output, Item.get(Item.AIR));
    }

    public boolean writeOutput(Inventory inventory, Item item) {
        return this.writeOutput(inventory, item, 0);
    }

    public boolean writeOutput(Inventory inventory, Item item, int resultIndex) {
        int index = PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT;
        Item current = inventory.getItemFast(index);
        if (item.isNull() || item.getCount() > 255 || this.usedOutputs.contains(resultIndex)
                || (!current.isNull() && this.outputIndex != resultIndex)) {
            return false;
        }
        this.outputIndex = resultIndex;
        return this.writeSlot(inventory, index, current, item);
    }

    boolean takeOutput(Inventory inventory, int index, Item expected, Item remainder) {
        if (!this.recipeStarted || !this.writeSlot(inventory, index, expected, remainder)) {
            return false;
        }
        this.usedOutputs.add(this.outputIndex);
        return true;
    }

    public void rollback() {
        List<Map.Entry<Slot, Item>> entries = new ArrayList<>(originalItems.entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            var entry = entries.get(i);
            Slot slot = entry.getKey();
            slot.inventory().setItemForce(slot.index(), entry.getValue());
        }
    }

    void collectEquipmentChanges(Player player, Set<EquipmentChange> changes) {
        for (var entry : this.originalItems.entrySet()) {
            Slot slot = entry.getKey();
            if (slot.inventory().getHolder() != player
                    || sameStack(slot.inventory().getItemFast(slot.index()), entry.getValue())) {
                continue;
            }
            if (slot.inventory() instanceof PlayerInventory inventory) {
                if (slot.index() >= inventory.getSize()) {
                    changes.add(EquipmentChange.ARMOR);
                } else if (slot.index() == inventory.getHeldItemIndex()) {
                    changes.add(EquipmentChange.HAND);
                }
            } else if (slot.inventory() instanceof PlayerOffhandInventory) {
                changes.add(EquipmentChange.OFFHAND);
            }
        }
    }

    enum EquipmentChange {
        ARMOR,
        HAND,
        OFFHAND
    }

    static boolean sameStack(Item first, Item second) {
        return first.isNull() && second.isNull()
                || first.getStackNetId() == second.getStackNetId() && first.equalsExact(second);
    }

    record Slot(Inventory inventory, int index) {

        static Slot of(Inventory inventory, int index) {
            if (inventory instanceof PlayerUIComponent component) {
                return new Slot(component.getBackingInventory(), index + component.getOffset());
            }
            if (inventory instanceof DoubleChestInventory chest) {
                int leftSize = chest.getLeftSide().getSize();
                return index < leftSize ? new Slot(chest.getLeftSide(), index)
                        : new Slot(chest.getRightSide(), index - leftSize);
            }
            return new Slot(inventory, index);
        }
    }

    public boolean commit() {
        boolean allOk = true;
        for (Runnable action : commitActions) {
            try {
                action.run();
            } catch (Throwable t) {
                log.error("Failed to execute item stack request commit action", t);
                allOk = false;
            }
        }
        commitActions.clear();
        return allOk;
    }

    void rememberBundleRoot(NetworkMapping.BundleHolderRef ref) {
        Slot slot = Slot.of(ref.outer(), ref.outerSlot());
        this.remember(slot, slot.inventory().getItemFast(slot.index()));
    }

    private void remember(Slot slot, Item item) {
        if (!this.originalItems.containsKey(slot)) {
            this.originalItems.put(slot, item.clone());
        }
    }

    public ActionResponse error() {
        return ActionResponse.error();
    }

    public ActionResponse success(List<ItemStackResponseContainer> containers) {
        return ActionResponse.ok(containers);
    }

    public ActionResponse success() {
        return ActionResponse.ok(Collections.emptyList());
    }
}
