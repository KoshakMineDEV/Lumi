package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.registry.Registries;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.CraftCreativeAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestActionType;

/**
 * Handles creative-mode item creation. The client sends this when it wants to
 * take a stack out of the creative catalog; the server materialises the item
 * into CREATED_OUTPUT for a subsequent TAKE action to move elsewhere.
 * <p>
 * Adapted from PowerNukkitX (<a href="https://github.com/PowerNukkitX/PowerNukkitX">PowerNukkitX</a>)
 */
public class CraftCreativeActionProcessor implements ItemStackRequestActionProcessor<CraftCreativeAction> {

    public static final String CRAFT_CREATIVE_KEY = "craft_creative_key";

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_CREATIVE;
    }

    @Override
    public ActionResponse handle(CraftCreativeAction action, Player player, ItemStackRequestContext context) {
        if (!player.isCreative()) {
            return context.error();
        }

        Item item = Registries.CREATIVE_ITEM.get(player.protocol).getCreativeItemData(player.protocol).stream()
                .filter(entry -> entry.getNetId() == action.getCreativeItemNetworkId())
                .map(entry -> entry.getItem()).findFirst().orElse(null);
        if (item == null || item.isNull()) {
            return context.error();
        }

        item = item.clone();
        item.setCount(item.getMaxStackSize());
        item.autoAssignStackNetworkId();

        if (!context.writeOutput(player.getUIInventory(), item)) {
            return context.error();
        }
        context.put(CRAFT_CREATIVE_KEY, true);
        return null;
    }
}
