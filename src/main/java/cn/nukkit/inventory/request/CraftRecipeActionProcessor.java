package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.inventory.EnchantItemEvent;
import cn.nukkit.event.inventory.SmithingTableEvent;
import cn.nukkit.event.inventory.StonecutterItemEvent;
import cn.nukkit.inventory.*;
import cn.nukkit.recipe.*;
import cn.nukkit.recipe.impl.*;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.registry.Registries;
import cn.nukkit.recipe.impl.special.RepairItemRecipe;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.ItemStackRequestCraftingTransaction;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.PlayerEnchantOptionsPacket;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.*;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseSlot;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
/**
 * Adapted from PowerNukkitX (<a href="https://github.com/PowerNukkitX/PowerNukkitX">PowerNukkitX</a>)
 */
@Slf4j
public class CraftRecipeActionProcessor implements ItemStackRequestActionProcessor<CraftRecipeAction> {

    public static final String RECIPE_NET_ID_KEY = "recipeNetId";
    public static final String ENCH_RECIPE_KEY = "enchRecipe";
    public static final String TIMES_CRAFTED_KEY = "timesCrafted";
    public static final String EVENT_AUTHORED_MULTI_OUTPUT_KEY = "eventAuthoredMultiOutput";
    private static final String WORKSTATION_RECIPE_KEY = "paidWorkstationRecipe";

    static boolean reserveWorkstationRecipe(ItemStackRequestContext context) {
        if (context.has(WORKSTATION_RECIPE_KEY)) {
            return false;
        }
        context.put(WORKSTATION_RECIPE_KEY, Boolean.TRUE);
        return true;
    }

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_RECIPE;
    }

    @Override
    public ActionResponse handle(CraftRecipeAction action, Player player, ItemStackRequestContext context) {
        int recipeNetId = action.getRecipeNetworkId();
        context.put(RECIPE_NET_ID_KEY, recipeNetId);

        if (recipeNetId >= TradeInventory.FIRST_RECIPE_NETWORK_ID) {
            return handleTrade(action, player, context);
        }
        if (recipeNetId >= PlayerEnchantOptionsPacket.ENCH_RECIPEID) {
            return handleEnchant(action, player, context);
        }
        if (recipeNetId == CraftingDataPacket.SMITHING_ARMOR_TRIM_NETWORK_ID) {
            return handleSmithingTrim(player, context);
        }

        Recipe recipe = Registries.RECIPE.getNetworkRecipe(player.protocol, recipeNetId);
        if (recipe == null) {
            return context.error();
        }

        if (recipe instanceof StonecutterRecipe stonecutterRecipe) {
            return handleStonecutter(player, stonecutterRecipe, action, context);
        }

        // MultiRecipe output is dynamic and is handled after the client result is
        // server-validated below. Never expose a null primary output to listeners.
        int times = Math.max(1, action.getNumberOfRequestedCrafts());
        Item recipeResult = recipe instanceof MultiRecipe ? null : recipe.getResult();
        if (recipe instanceof ShulkerBoxDyeRecipe dyeRecipe) {
            List<ConsumeAction> consumes = CraftRecipeAutoProcessor.findAllConsumeActions(
                    context.getItemStackRequest().getActions(), context.getCurrentActionIndex() + 1);
            recipeResult = prepareShulkerDyeResult(player, context, dyeRecipe, times, consumes,
                    CraftRecipeAutoProcessor.collectConsumedItems(player, consumes));
            if (recipeResult == null) {
                return context.error();
            }
        } else if (recipeResult != null) {
            recipeResult = fireCraftItemEvent(player, recipe, recipeResult);
            if (recipeResult == null) {
                return context.error();
            }
        }

        context.put(CreateActionProcessor.RECIPE_DATA_KEY, recipe);

        if (recipe instanceof MultiRecipe multiRecipe) {
            if (multiRecipe instanceof RepairItemRecipe && action.getNumberOfRequestedCrafts() != 1) {
                return context.error();
            }
            context.put(TIMES_CRAFTED_KEY, times);
            CraftResultsDeprecatedAction resultsAction = findCraftResultsAction(
                    context.getItemStackRequest().getActions(),
                    context.getCurrentActionIndex() + 1
            );
            if (resultsAction == null || resultsAction.getResultItems() == null || resultsAction.getResultItems().length == 0) {
                return context.error();
            }
            Item output = resultsAction.getResultItems()[0];
            if (output == null || output.isNull()) {
                return context.error();
            }
            if (!validateCraftingRecipe(player, multiRecipe, output, 1)) {
                return context.error();
            }
            if (!validateMultiRecipeConsumePlan(player, multiRecipe, output, context)) {
                return context.error();
            }
            Item authoritativeOutput = fireCraftItemEvent(player, recipe, output);
            if (authoritativeOutput == null) {
                return context.error();
            }
            context.put(EVENT_AUTHORED_MULTI_OUTPUT_KEY, authoritativeOutput);
            return context.success();
        }

        // Smithing dispatch: trim recipes delegate to the inventory's trim logic;
        // transform recipes preserve the equipment's NBT onto the result.
        if (recipe instanceof SmithingRecipe smithingTransform) {
            return handleSmithingUpgrade(smithingTransform, player, context);
        }

        if (recipeResult == null || recipeResult.isNull()) {
            return null;
        }
        if (!validateCraftingRecipe(player, recipe, recipeResult, times)) {
            return context.error();
        }
        if (recipe instanceof CraftingRecipe craftingRecipe && !(recipe instanceof ShulkerBoxDyeRecipe)
                && !validateCraftingConsumePlan(player, craftingRecipe, times, context)) {
            return context.error();
        }

        if (recipe instanceof CraftingRecipe multiOutput && !multiOutput.getExtraResults().isEmpty()) {
            if (times != 1) {
                log.debug("{}: rejected multi-output craft with numberOfRequestedCrafts={}",
                        player.getName(), times);
                return context.error();
            }
            List<Item> outputs = scaleItems(multiOutput.getAllResults(), times);
            Item authoritativePrimary = recipeResult.clone();
            authoritativePrimary.setCount(authoritativePrimary.getCount() * times);
            outputs.set(0, authoritativePrimary);
            context.put(CreateActionProcessor.RECIPE_OUTPUTS_KEY, outputs);
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

    private ActionResponse handleTrade(CraftRecipeAction action, Player player, ItemStackRequestContext context) {
        if (!(player.getTopWindow().orElse(null) instanceof TradeInventory inventory) || !inventory.getViewers().contains(player)) {
            return context.error();
        }
        TradeInventory.Offer offer = inventory.getAssignedOffer(action.getRecipeNetworkId());
        int times = Math.max(1, action.getNumberOfRequestedCrafts());
        if (offer == null || times > 255 || !inventory.canUse(offer, times)) {
            return context.error();
        }

        Item output = offer.getResult();
        if ((long) output.getCount() * times > output.getMaxStackSize()) {
            return context.error();
        }
        List<ConsumeAction> consumes = CraftRecipeAutoProcessor.findAllConsumeActions(
                context.getItemStackRequest().getActions(), context.getCurrentActionIndex() + 1);
        List<Item> consumedItems = CraftRecipeAutoProcessor.collectConsumedItems(player, consumes);
        if (consumedItems == null) {
            return context.error();
        }
        int[] consumed = new int[2];
        for (int i = 0; i < consumes.size(); i++) {
            ConsumeAction consume = consumes.get(i);
            var source = consume.getSource();
            if (NetworkMapping.getInventory(player, source) != inventory) {
                return context.error();
            }
            int slot = NetworkMapping.toInternalSlot(source.getContainer(), source.getSlot());
            if (slot >= consumed.length || !offer.matchesInput(slot, consumedItems.get(i))) {
                return context.error();
            }
            consumed[slot] += consume.getCount();
        }
        if (consumed[0] != offer.getInputCount(0) * times || consumed[1] != offer.getInputCount(1) * times) {
            return context.error();
        }

        output.setCount(output.getCount() * times);
        output.autoAssignStackNetworkId();
        if (!context.writeOutput(player.getUIInventory(), output)) {
            return context.error();
        }
        context.expectConsumes(consumes, consumedItems);
        context.requireOutputTaken();
        context.put(CreateActionProcessor.RECIPE_OUTPUTS_KEY, List.of(output));
        context.onCommit(() -> inventory.completeTrade(player, offer, times));
        ItemStackResponseSlot slot = new ItemStackResponseSlot(
                PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT, PlayerUIComponent.CREATED_ITEM_OUTPUT_UI_SLOT,
                output.getCount(), output.getStackNetId(), output.getCustomName(), output.getDamage());
        return context.success(List.of(new ItemStackResponseContainer(ContainerSlotType.CREATED_OUTPUT, List.of(slot), null)));
    }

    private ActionResponse handleEnchant(CraftRecipeAction action, Player player, ItemStackRequestContext context) {
        Inventory inventory = player.getTopWindow().orElse(null);
        if (!(inventory instanceof EnchantInventory enchantInventory)) {
            return context.error();
        }
        if (!reserveWorkstationRecipe(context)) {
            return context.error();
        }
        PlayerEnchantOptionsPacket.EnchantOptionData option =
                enchantInventory.getPublishedOption(action.getRecipeNetworkId());
        if (option == null) {
            log.warn("{}: unknown enchant recipe netId {}", player.getName(), action.getRecipeNetworkId());
            return context.error();
        }
        Item first = enchantInventory.getInputSlot();
        if (first.isNull()) {
            return context.error();
        }
        List<Enchantment> enchantments = new ArrayList<>();
        for (PlayerEnchantOptionsPacket.EnchantData data : option.getEnchants0()) {
            Enchantment enchantment = Enchantment.getEnchantment(data.getType());
            if (enchantment != null) {
                if (enchantment.isTreasure()) {
                    return context.error();
                }
                if (!isApplicableEnchant(enchantment, first)) {
                    return context.error();
                }
                enchantments.add(enchantment.setLevel(data.getLevel()));
            }
        }
        int cost = option.getPrimarySlot() + 1;
        if (!player.isCreative()) {
            if (player.getExperienceLevel() < cost || player.getExperienceLevel() < option.getMinLevel()) {
                return context.error();
            }
            Item reagent = enchantInventory.getReagentSlot();
            if (reagent.isNull() || reagent.getCount() < cost || !reagent.equals(Item.get("minecraft:lapis_lazuli"), true, false)) {
                return context.error();
            }
            List<Item> expectedConsumes = new ArrayList<>(2);
            addExpectedConsumeItem(expectedConsumes, first, 1);
            addExpectedConsumeItem(expectedConsumes, reagent, cost);
            if (!validateExpectedConsumePlan(player, expectedConsumes, context)) {
                return context.error();
            }
        }

        Item output = first.clone();
        if (output.getId() == Item.BOOK) {
            output = Item.get(Item.ENCHANTED_BOOK);
        }
        output.setCount(1);
        if (!enchantments.isEmpty()) {
            output.addEnchantment(enchantments.toArray(Enchantment.EMPTY_ARRAY));
        }
        output.autoAssignStackNetworkId();

        EnchantItemEvent event = new EnchantItemEvent(enchantInventory, first.clone(), output, cost, player);
        Server.getInstance().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return context.error();
        }

        Item finalOutput = event.getNewItem();
        if (finalOutput == null || finalOutput.isNull()) {
            return context.error();
        }
        int finalCost = event.getXpCost();
        if (finalCost < 0 || !player.isCreative() && finalCost > player.getExperienceLevel()) {
            return context.error();
        }

        if (!player.isCreative()) {
            context.onCommit(() -> player.setExperience(player.getExperience(), player.getExperienceLevel() - finalCost));
        }
        // Keep the predicted result in CREATED_OUTPUT until the following transfer.
        if (!context.writeOutput(player.getUIInventory(), finalOutput)) {
            return context.error();
        }
        context.onCommit(() -> enchantInventory.releasePublishedOption(action.getRecipeNetworkId()));
        context.put(ENCH_RECIPE_KEY, true);

        return null;
    }

    private static boolean isApplicableEnchant(Enchantment enchantment, Item input) {
        return input.getId() == Item.BOOK || enchantment.canEnchant(input);
    }

    private static Item fireCraftItemEvent(Player player, Recipe recipe, Item output) {
        return fireCraftItemEvent(player, recipe, output, collectCraftingInputList(player));
    }

    private static Item fireCraftItemEvent(Player player, Recipe recipe, Item output, List<Item> inputs) {
        if (output == null || output.isNull()) {
            return null;
        }
        CraftingTransaction snapshot = new ItemStackRequestCraftingTransaction(
                player, inputs, output, recipe);
        CraftItemEvent craftEvent = new CraftItemEvent(snapshot);
        Server.getInstance().getPluginManager().callEvent(craftEvent);
        if (craftEvent.isCancelled()) {
            return null;
        }
        Item eventOutput = snapshot.getPrimaryOutput();
        return eventOutput == null || eventOutput.isNull() ? null : eventOutput.clone();
    }

    static Item prepareShulkerDyeResult(Player player, ItemStackRequestContext context, ShulkerBoxDyeRecipe recipe,
                                        int times, List<ConsumeAction> consumes, List<Item> consumedItems) {
        if (times != 1 || consumedItems == null || !matchesIngredients(consumedItems, recipe, times, true)) {
            return null;
        }
        Item output = fireCraftItemEvent(player, recipe, recipe.getResult(consumedItems), consumedItems);
        if (output != null) {
            context.expectConsumes(consumes, consumedItems);
        }
        return output;
    }

    static List<Item> collectCraftingInputList(Player player) {
        CraftingGrid grid = getActiveCraftingGrid(player);
        int size = grid.getSize();
        List<Item> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Item item = grid.getUnclonedItem(i);
            if (item != null && !item.isNull()) {
                items.add(item.clone());
            }
        }
        return items;
    }

    private static CraftingGrid getActiveCraftingGrid(Player player) {
        Inventory top = player.getTopWindow().orElse(null);
        if (top instanceof CraftingGrid openGrid) {
            return openGrid;
        }
        CraftingGrid grid = player.getCraftingGrid();
        return grid != null ? grid : player.getUIInventory().getCraftingGrid();
    }

    static boolean validateCraftingRecipe(Player player, Recipe recipe, Item output, int multiplier) {
        List<Item> inputs = collectCraftingInputList(player);
        if (recipe instanceof MultiRecipe multiRecipe) {
            return multiRecipe.canExecute(player, output.clone(), descriptors(inputs));
        }
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            return true;
        }

        if (craftingRecipe.requiresCraftingTable() && getActiveCraftingGrid(player).getSize() < 9) {
            return false;
        }
        return matchesIngredients(inputs, craftingRecipe, multiplier, false);
    }

    static List<ItemDescriptor> descriptors(List<Item> items) {
        return items.stream().map(item -> (ItemDescriptor) new DefaultDescriptor(item)).toList();
    }

    static boolean matchesIngredients(List<Item> inputs, CraftingRecipe recipe, int times, boolean exact) {
        List<Item> remaining = scaleItems(inputs, 1);
        for (ItemDescriptor descriptor : recipe.getIngredientList()) {
            if (descriptor == null || descriptor instanceof DefaultDescriptor value && value.getItem().isNull()) {
                continue;
            }
            int count = descriptor instanceof DefaultDescriptor value ? value.getItem().getCount() : 1;
            int needed = count * times;
            for (Item item : remaining) {
                if (item.getCount() > 0 && descriptor.equals(item)) {
                    int taken = Math.min(item.getCount(), needed);
                    item.setCount(item.getCount() - taken);
                    needed -= taken;
                }
                if (needed == 0) {
                    break;
                }
            }
            if (needed != 0) {
                return false;
            }
        }
        return !exact || remaining.stream().allMatch(Item::isNull);
    }

    static List<Item> scaleItems(List<Item> items, int multiplier) {
        List<Item> scaled = new ArrayList<>(items.size());
        for (Item item : items) {
            if (item == null || item.isNull()) {
                continue;
            }
            Item clone = item.clone();
            clone.setCount(clone.getCount() * multiplier);
            scaled.add(clone);
        }
        return scaled;
    }

    private static boolean validateCraftingConsumePlan(Player player, CraftingRecipe recipe, int times, ItemStackRequestContext context) {
        List<ConsumeAction> consumes = CraftRecipeAutoProcessor.findAllConsumeActions(
                context.getItemStackRequest().getActions(), context.getCurrentActionIndex() + 1);
        List<Item> consumed = CraftRecipeAutoProcessor.collectConsumedItems(player, consumes);
        return consumed != null && matchesIngredients(consumed, recipe, times, true);
    }

    static boolean validateMultiRecipeConsumePlan(Player player, MultiRecipe recipe, Item output, ItemStackRequestContext context) {
        CraftingGrid grid = getActiveCraftingGrid(player);
        Set<Integer> occupiedSlots = new HashSet<>();
        for (int slot = 0; slot < grid.getSize(); slot++) {
            Item item = grid.getUnclonedItem(slot);
            if (item != null && !item.isNull()) {
                occupiedSlots.add(slot);
            }
        }
        if (occupiedSlots.isEmpty()) {
            return false;
        }

        List<ConsumeAction> consumeActions = CraftRecipeAutoProcessor.findAllConsumeActions(
                context.getItemStackRequest().getActions(),
                context.getCurrentActionIndex() + 1);
        if (consumeActions.size() != occupiedSlots.size()) {
            return false;
        }

        Set<Integer> consumedSlots = new HashSet<>();
        List<Item> consumedItems = new ArrayList<>(consumeActions.size());
        for (ConsumeAction consume : consumeActions) {
            if (consume.getCount() <= 0) {
                return false;
            }
            var source = consume.getSource();
            if (source == null) {
                return false;
            }
            var inventory = NetworkMapping.getInventory(player, source);
            if (inventory != grid) {
                return false;
            }
            int slot = NetworkMapping.toInternalSlot(source.getContainer(), source.getSlot());
            if (!occupiedSlots.contains(slot) || !consumedSlots.add(slot)) {
                return false;
            }
            Item item = inventory.getItem(slot);
            if (item.isNull() || item.getCount() < consume.getCount()) {
                return false;
            }
            if (hasStackNetworkIdMismatch(item.getStackNetId(), source.getStackNetworkId())) {
                return false;
            }
            Item consumed = item.clone();
            consumed.setCount(consume.getCount());
            consumedItems.add(consumed);
        }

        return consumedSlots.size() == occupiedSlots.size()
                && recipe.canExecute(player, output.clone(), descriptors(consumedItems));
    }

    private static boolean hasStackNetworkIdMismatch(int serverNetId, int clientNetId) {
        return serverNetId > 0 && clientNetId > 0 && serverNetId != clientNetId;
    }

    static void addExpectedConsumeItem(List<Item> expected, Item item, int count) {
        if (item == null || item.isNull() || count <= 0) {
            return;
        }
        Item expectedItem = item.clone();
        expectedItem.setCount(count);
        expected.add(expectedItem);
    }

    static boolean validateExpectedConsumePlan(Player player, List<Item> expected, ItemStackRequestContext context) {
        List<Item> expectedConsumes = new ArrayList<>(expected.size());
        for (Item item : expected) {
            if (item == null || item.isNull() || item.getCount() <= 0) {
                continue;
            }
            expectedConsumes.add(item.clone());
        }
        if (expectedConsumes.isEmpty()) {
            return true;
        }

        List<ConsumeAction> consumeActions = CraftRecipeAutoProcessor.findAllConsumeActions(
                context.getItemStackRequest().getActions(),
                context.getCurrentActionIndex() + 1);

        List<Item> actual = new ArrayList<>();
        for (ConsumeAction consume : consumeActions) {
            if (consume.getCount() <= 0) {
                return false;
            }
            var source = consume.getSource();
            var inventory = NetworkMapping.getInventory(player, source);
            if (inventory == null) {
                return false;
            }
            int slot = NetworkMapping.toInternalSlot(source.getContainer(), source.getSlot());
            Item item = inventory.getItem(slot);
            if (item.isNull() || item.getCount() < consume.getCount()) {
                return false;
            }
            Item consumed = item.clone();
            consumed.setCount(consume.getCount());
            actual.add(consumed);
        }

        return Recipe.matchItemList(actual, descriptors(expectedConsumes));
    }

    private static CraftResultsDeprecatedAction findCraftResultsAction(ItemStackRequestAction[] actions, int startIndex) {
        for (int i = startIndex; i < actions.length; i++) {
            if (actions[i] instanceof CraftResultsDeprecatedAction craftResults) {
                return craftResults;
            }
        }
        return null;
    }

    private ActionResponse handleSmithingUpgrade(SmithingRecipe recipe, Player player, ItemStackRequestContext context) {
        Inventory inventory = player.getTopWindow().orElse(null);
        if (!(inventory instanceof SmithingInventory smithingInventory)) {
            return context.error();
        }
        Item equipment = smithingInventory.getEquipment();
        Item ingredient = smithingInventory.getIngredient();
        Item template = smithingInventory.getTemplate();
        SmithingRecipe matchedRecipe = Registries.RECIPE.matchSmithingRecipe(new ArrayList<>(List.of(equipment, ingredient, template)));
        if (matchedRecipe != recipe) {
            return context.error();
        }
        Item result = recipe.getFinalResult(equipment, template);
        if (result == null || result.isNull()) {
            return context.error();
        }
        if (!validateSmithingConsumePlan(player, context, equipment, ingredient, template)) {
            return context.error();
        }
        result = fireSmithingEvent(smithingInventory, result, player);
        if (result == null) {
            return context.error();
        }
        result.autoAssignStackNetworkId();
        if (!context.writeOutput(player.getUIInventory(), result)) {
            return context.error();
        }
        return buildCreatedOutputResponse(context, result);
    }

    private ActionResponse handleSmithingTrim(Player player, ItemStackRequestContext context) {
        Inventory inventory = player.getTopWindow().orElse(null);
        if (!(inventory instanceof SmithingInventory smithingInventory)) {
            return context.error();
        }
        Item result = smithingInventory.getTrimOutPutItem();
        if (result == null || result.isNull()) {
            return context.error();
        }
        if (!validateSmithingConsumePlan(player, context,
                smithingInventory.getEquipment(), smithingInventory.getIngredient(), smithingInventory.getTemplate())) {
            return context.error();
        }
        result = fireSmithingEvent(smithingInventory, result, player);
        if (result == null) {
            return context.error();
        }
        result.autoAssignStackNetworkId();
        if (!context.writeOutput(player.getUIInventory(), result)) {
            return context.error();
        }
        return buildCreatedOutputResponse(context, result);
    }

    private static boolean validateSmithingConsumePlan(Player player, ItemStackRequestContext context,
                                                       Item equipment, Item ingredient, Item template) {
        List<Item> expectedConsumes = new ArrayList<>(3);
        addExpectedConsumeItem(expectedConsumes, equipment, 1);
        addExpectedConsumeItem(expectedConsumes, ingredient, 1);
        addExpectedConsumeItem(expectedConsumes, template, 1);
        return validateExpectedConsumePlan(player, expectedConsumes, context);
    }

    private static Item fireSmithingEvent(SmithingInventory inventory, Item result, Player player) {
        SmithingTableEvent event = new SmithingTableEvent(
                inventory,
                inventory.getEquipment().clone(),
                result.clone(),
                inventory.getIngredient().clone(),
                inventory.getTemplate().clone(),
                player
        );
        Server.getInstance().getPluginManager().callEvent(event);
        Item eventOutput = event.getResultItem();
        return event.isCancelled() || eventOutput == null || eventOutput.isNull()
                ? null
                : eventOutput.clone();
    }

    private ActionResponse handleStonecutter(Player player, StonecutterRecipe recipe, CraftRecipeAction action, ItemStackRequestContext context) {
        Inventory top = player.getTopWindow().orElse(null);
        if (!(top instanceof StonecutterInventory stonecutterInventory)) {
            return context.error();
        }
        Item input = stonecutterInventory.getInput().clone();
        if (input.isNull()) {
            return context.error();
        }
        int times = Math.max(1, action.getNumberOfRequestedCrafts());
        if (recipe.getIngredientList().size() != 1 || !matchesIngredients(List.of(input), recipe, times, false)) {
            return context.error();
        }
        ItemDescriptor descriptor = recipe.getIngredientList().iterator().next();
        int ingredientCount = descriptor instanceof DefaultDescriptor ingredient ? ingredient.getItem().getCount() : 1;
        if (input.getCount() < ingredientCount * times) {
            return context.error();
        }
        Item output = recipe.getResult();
        output.setCount(output.getCount() * times);

        List<Item> expectedConsumes = new ArrayList<>(1);
        addExpectedConsumeItem(expectedConsumes, input, ingredientCount * times);
        if (!validateExpectedConsumePlan(player, expectedConsumes, context)) {
            return context.error();
        }

        StonecutterItemEvent event = new StonecutterItemEvent(stonecutterInventory, input, output.clone(), player);
        Server.getInstance().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return context.error();
        }
        Item eventOutput = event.getOutputItem();
        if (eventOutput == null || eventOutput.isNull()) {
            return context.error();
        }
        output = eventOutput.clone();

        context.put(CreateActionProcessor.RECIPE_DATA_KEY, recipe);
        output.autoAssignStackNetworkId();
        if (!context.writeOutput(player.getUIInventory(), output)) {
            return context.error();
        }
        return buildCreatedOutputResponse(context, output);
    }

    private ActionResponse buildCreatedOutputResponse(ItemStackRequestContext context, Item output) {
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
