package cn.nukkit.network.protocol.types.inventory.itemstack.request.action;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class CraftLoomAction implements ItemStackRequestAction {
    String patternId;

    int timesCrafted;

    public CraftLoomAction(String patternId) {
        this(patternId, 1);
    }

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_LOOM;
    }
}
