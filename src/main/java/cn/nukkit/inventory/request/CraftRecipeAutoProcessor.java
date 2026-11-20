package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.inventory.*;
import cn.nukkit.recipe.*;
import cn.nukkit.recipe.impl.*;
import cn.nukkit.registry.Registries;
import cn.nukkit.inventory.transaction.ItemStackRequestCraftingTransaction;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.CraftIngredient;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.*;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseSlot;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Auto-craft variant of CraftRecipeAction (triggered by shift-click on recipe
 * book). Unlike CRAFT_RECIPE, the client ships the concrete ingredient
 * descriptors it intends to consume, so the server validates against the
 * resolved recipe and the follow-up CONSUME chain.
 * <p>
 * Adapted from PowerNukkitX (<a href="https://github.com/PowerNukkitX/PowerNukkitX">PowerNukkitX</a>)
 */
@Slf4j
public class CraftRecipeAutoProcessor implements ItemStackRequestActionProcessor<AutoCraftRecipeAction> {

    public static final String TIMES_CRAFTED_KEY = "timesCrafted";

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_RECIPE_AUTO;
    }

    @Override
    public ActionResponse handle(AutoCraftRecipeAction action, Player player, ItemStackRequestContext context) {
        Recipe recipe = Registries.RECIPE.getNetworkRecipe(player.protocol, action.getRecipeNetworkId());
        if (recipe == null) {
            return context.error();
        }

        List<CraftIngredient> ingredients = action.getIngredients();
        if (ingredients == null) {
            ingredients = List.of();
        }
        // Read-only snapshot so getTransaction() matches the legacy path
        Item recipeResult = recipe instanceof MultiRecipe ? null : recipe.getResult();
        if (!(recipe instanceof ShulkerBoxDyeRecipe)) {
            List<Item> eventItems = ingredients.stream()
                    .map(CraftRecipeAutoProcessor::toEventItem)
                    .filter(item -> item != null && !item.isNull())
                    .toList();
            CraftItemEvent craftItemEvent = new CraftItemEvent(new ItemStackRequestCraftingTransaction(
                    player, eventItems, recipeResult, recipe));
            player.getServer().getPluginManager().callEvent(craftItemEvent);
            if (craftItemEvent.isCancelled()) {
                return context.error();
            }
        }

        context.put(CraftRecipeActionProcessor.RECIPE_NET_ID_KEY, action.getRecipeNetworkId());
        context.put(CreateActionProcessor.RECIPE_DATA_KEY, recipe);
        context.put(TIMES_CRAFTED_KEY, action.getTimesCrafted());

        int consumeActionCountNeeded = 0;
        for (CraftIngredient ingredient : ingredients) {
            if (ingredient != null && ingredient.count() > 0) {
                consumeActionCountNeeded++;
            }
        }
        List<ConsumeAction> consumeActions = findAllConsumeActions(
                context.getItemStackRequest().getActions(),
                context.getCurrentActionIndex() + 1);
        if (consumeActions.size() < consumeActionCountNeeded) {
            log.warn("{}: auto-craft consume action count mismatch. expected={} actual={}",
                    player.getName(), consumeActionCountNeeded, consumeActions.size());
            return context.error();
        }

        List<Item> consumedItems = collectConsumedItems(player, consumeActions);
        int times = Math.max(1, action.getTimesCrafted());
        if (consumedItems == null) {
            return context.error();
        }
        if (!validateConsumePlan(ingredients, consumedItems, 1)) {
            if (!(recipe instanceof CraftingRecipe) || times <= 1
                    || !validateConsumePlan(ingredients, consumedItems, times)) {
                return context.error();
            }

        }

        if (recipe instanceof MultiRecipe multiRecipe) {
            context.put(CraftRecipeActionProcessor.TIMES_CRAFTED_KEY, Math.max(1, action.getTimesCrafted()));
            CraftResultsDeprecatedAction resultsAction = findCraftResultsAction(
                    context.getItemStackRequest().getActions(), context.getCurrentActionIndex() + 1);
            if (resultsAction == null || resultsAction.getResultItems() == null || resultsAction.getResultItems().length == 0) {
                return context.error();
            }
            Item output = resultsAction.getResultItems()[0];
            if (output == null || output.isNull()
                    || !validateAutoCraftingRecipe(player, multiRecipe, output, 1, consumedItems)) {
                return context.error();
            }
            CraftItemEvent event = new CraftItemEvent(new ItemStackRequestCraftingTransaction(
                    player, consumedItems, output, recipe));
            if (!event.call()) {
                return context.error();
            }
            context.put(CraftRecipeActionProcessor.EVENT_AUTHORED_MULTI_OUTPUT_KEY,
                    event.getTransaction().getPrimaryOutput());
            return context.success();
        }

        if (recipeResult == null || recipeResult.isNull()) {
            return null;
        }
        if (recipe instanceof ShulkerBoxDyeRecipe dyeRecipe) {
            recipeResult = CraftRecipeActionProcessor.prepareShulkerDyeResult(
                    player, context, dyeRecipe, times, consumeActions, consumedItems);
            if (recipeResult == null) {
                return context.error();
            }
        } else if (!validateAutoCraftingRecipe(player, recipe, recipeResult, times, consumedItems)) {
            return context.error();
        }

        if (recipe instanceof CraftingRecipe multiOutput && !multiOutput.getExtraResults().isEmpty()) {
            if (times != 1) {
                log.debug("{}: rejected multi-output auto-craft with timesCrafted={}",
                        player.getName(), times);
                return context.error();
            }
            context.put(CreateActionProcessor.RECIPE_OUTPUTS_KEY,
                    CraftRecipeActionProcessor.scaleItems(multiOutput.getAllResults(), times));
            context.put(CreateActionProcessor.CREATED_SLOTS_KEY, new HashSet<>());
            return context.success();
        }

        Item output = recipeResult.clone();
        output.setCount(output.getCount() * times);
        output.autoAssignStackNetworkId();
        if (!context.writeOutput(player.getUIInventory(), output)) {
            return context.error();
        }
        if (recipe instanceof ShulkerBoxDyeRecipe) {
            context.put(CreateActionProcessor.RECIPE_OUTPUTS_KEY, List.of(output));
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

    static List<ConsumeAction> findAllConsumeActions(ItemStackRequestAction[] actions, int startIndex) {
        List<ConsumeAction> found = new ArrayList<>();
        for (int i = startIndex; i < actions.length; i++) {
            if (actions[i] instanceof ConsumeAction consume) {
                found.add(consume);
            }
        }
        return found;
    }

    private static CraftResultsDeprecatedAction findCraftResultsAction(ItemStackRequestAction[] actions, int startIndex) {
        for (int i = startIndex; i < actions.length; i++) {
            if (actions[i] instanceof CraftResultsDeprecatedAction craftResults) {
                return craftResults;
            }
        }
        return null;
    }

    private static Item toEventItem(CraftIngredient ingredient) {
        if (ingredient == null || (ingredient.item() == null && ingredient.tag() == null)) {
            return Item.get(Item.AIR);
        }
        Item item = ingredient.item() == null ? null : ingredient.item().clone();
        if (item != null && !item.isNull() && ingredient.count() > 0) {
            item.setCount(ingredient.count());
        }
        return item;
    }

    static List<Item> collectConsumedItems(Player player, List<ConsumeAction> consumeActions) {
        List<Item> actual = new ArrayList<>();
        for (ConsumeAction consume : consumeActions) {
            if (consume.getCount() <= 0) {
                return null;
            }
            var source = consume.getSource();
            if (source == null) {
                return null;
            }
            var inventory = NetworkMapping.getInventory(player, source);
            if (inventory == null) {
                return null;
            }
            int slot = NetworkMapping.toInternalSlot(source.getContainer(), source.getSlot());
            Item item = inventory.getItem(slot);
            if (item.isNull() || item.getCount() < consume.getCount()) {
                return null;
            }
            if (hasStackNetworkIdMismatch(item.getStackNetId(), source.getStackNetworkId())) {
                return null;
            }
            Item consumed = item.clone();
            consumed.setCount(consume.getCount());
            actual.add(consumed);
        }
        return actual;
    }

    private static boolean validateConsumePlan(List<CraftIngredient> ingredients,
                                               List<Item> consumedItems, int multiplier) {
        List<Item> actual = cloneItems(consumedItems);
        for (CraftIngredient ingredient : ingredients) {
            if (ingredient == null || (ingredient.item() == null && ingredient.tag() == null) || ingredient.count() <= 0) {
                continue;
            }
            // Newer clients describe one recipe; CONSUME contains the full craft count.
            // Keep the already-total form compatible, but require exact coverage in both forms.
            long required = (long) ingredient.count() * multiplier;
            if (required > Integer.MAX_VALUE) {
                return false;
            }
            int remaining = (int) required;
            for (Item actualItem : new ArrayList<>(actual)) {
                if (!matchesDescriptor(ingredient, actualItem)) {
                    continue;
                }
                int amount = Math.min(actualItem.getCount(), remaining);
                actualItem.setCount(actualItem.getCount() - amount);
                remaining -= amount;
                if (actualItem.getCount() == 0) {
                    actual.remove(actualItem);
                }
                if (remaining == 0) {
                    break;
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return actual.isEmpty();
    }

    private static boolean matchesDescriptor(CraftIngredient ingredient, Item item) {
        if (item == null || item.isNull()) {
            return false;
        }
        return ingredient.matches(item);
    }

    private static boolean validateAutoCraftingRecipe(Player player, Recipe recipe, Item output, int multiplier, List<Item> consumedItems) {
        List<Item> inputs = cloneItems(consumedItems);
        if (recipe instanceof MultiRecipe multiRecipe) {
            return multiRecipe.canExecute(player, output.clone(), CraftRecipeActionProcessor.descriptors(inputs));
        }
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            return true;
        }

        return CraftRecipeActionProcessor.matchesIngredients(inputs, craftingRecipe, multiplier, true);
    }

    private static List<Item> cloneItems(List<Item> items) {
        List<Item> cloned = new ArrayList<>(items.size());
        for (Item item : items) {
            if (item != null && !item.isNull()) {
                cloned.add(item.clone());
            }
        }
        return cloned;
    }

    private static boolean hasStackNetworkIdMismatch(int serverNetId, int clientNetId) {
        return serverNetId > 0 && clientNetId > 0 && serverNetId != clientNetId;
    }
}
