package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestActionType;
import org.jetbrains.annotations.Nullable;

public interface ItemStackRequestActionProcessor<T extends ItemStackRequestAction> {

    ItemStackRequestActionType getType();

    @Nullable
    ActionResponse handle(T action, Player player, ItemStackRequestContext context);

    default boolean validateStackNetworkId(int serverNetId, int clientNetId) {
        return clientNetId > 0 && serverNetId != clientNetId;
    }
}
