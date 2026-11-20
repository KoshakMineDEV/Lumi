package cn.nukkit.network.protocol.types.inventory.itemstack.request.action;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class CraftGrindstoneAction implements ItemStackRequestAction {
    int recipeNetworkId;
    int repairCost;

    int numberOfRequestedCrafts;

    public CraftGrindstoneAction(int recipeNetworkId, int repairCost) {
        this(recipeNetworkId, repairCost, 1);
    }

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_REPAIR_AND_DISENCHANT;
    }
}
