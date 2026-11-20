package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.recipe.CraftingRecipe;
import cn.nukkit.recipe.impl.MultiRecipe;
import cn.nukkit.inventory.PlayerUIComponent;
import cn.nukkit.recipe.Recipe;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.CreateAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestActionType;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseSlot;

import java.util.*;

/**
 * CreateAction is sent by the client for multi-output recipes to pick a specific
 * result slot (firework star variants, banner patterns) and to collect recipe
 * leftovers such as the empty buckets returned when milk is used to craft a cake.
 * Resolves the recipe cached by CraftRecipeAction and writes the chosen result
 * into CREATED_OUTPUT.
 */
public class CreateActionProcessor implements ItemStackRequestActionProcessor<CreateAction> {

    public static final String RECIPE_DATA_KEY = "recipe";

    public static final String RECIPE_OUTPUTS_KEY = "recipeOutputs";

    public static final String CREATED_SLOTS_KEY = "createdSlots";

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CREATE;
    }

    @Override
    public ActionResponse handle(CreateAction action, Player player, ItemStackRequestContext context) {
        Recipe recipe = context.get(RECIPE_DATA_KEY);
        List<Item> storedOutputs = context.get(RECIPE_OUTPUTS_KEY);
        if (recipe == null && storedOutputs == null) {
            return context.error();
        }

        List<Item> results;
        if (recipe instanceof MultiRecipe) {
            results = context.get(CraftResultDeprecatedActionProcessor.MULTI_RESULTS_KEY);
            if (results == null || results.isEmpty()) {
                return context.error();
            }
        } else if (storedOutputs != null) {
            results = storedOutputs;
        } else if (recipe instanceof CraftingRecipe craftingRecipe) {
            results = craftingRecipe.getAllResults();
        } else {
            results = List.of(recipe.getResult());
        }
        int slot = action.getSlot();
        if (slot < 0 || slot >= results.size()) {
            return context.error();
        }

        Set<Integer> createdSlots = context.get(CREATED_SLOTS_KEY);
        if (createdSlots == null) {
            createdSlots = new HashSet<>();
            context.put(CREATED_SLOTS_KEY, createdSlots);
        }
        if (!createdSlots.add(slot)) {
            return context.error();
        }

        Item output = results.get(slot).clone();
        output.autoAssignStackNetworkId();
        if (!context.writeOutput(player.getUIInventory(), output, slot)) {
            return context.error();
        }

        ItemStackResponseSlot responseSlot = new ItemStackResponseSlot(
                PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT,
                PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT,
                output.getCount(), output.getStackNetId(),
                output.hasCustomName() ? output.getCustomName() : "",
                output.getDamage(), null
        );
        return context.success(List.of(new ItemStackResponseContainer(
                ContainerSlotType.CREATED_OUTPUT,
                List.of(responseSlot),
                null
        )));
    }
}
