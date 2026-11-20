package cn.nukkit.network.protocol.types.inventory.itemstack.request.action;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * CraftCreativeStackRequestActionData is sent by the client when it takes an item out of the creative inventory.
 * The item is thus not really crafted, but instantly created.
 */
@Value
@AllArgsConstructor
public class CraftCreativeAction implements ItemStackRequestAction {
    /**
     * creativeItemNetworkId is the network ID of the creative item that is being created. This is one of the
     * creative item network IDs sent in the CreativeContent packet.
     */
    int creativeItemNetworkId;

    int numberOfRequestedCrafts;

    public CraftCreativeAction(int creativeItemNetworkId) {
        this(creativeItemNetworkId, 1);
    }

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_CREATIVE;
    }
}
