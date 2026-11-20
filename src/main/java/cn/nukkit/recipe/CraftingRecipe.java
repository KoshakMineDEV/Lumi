package cn.nukkit.recipe;

import cn.nukkit.recipe.impl.data.RecipeUnlockingRequirement;
import cn.nukkit.item.Item;

import cn.nukkit.recipe.descriptor.ItemDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author CreeperFace
 */
public interface CraftingRecipe extends Recipe {

    String getRecipeId();

    UUID getId();

    void setId(UUID id);

    boolean requiresCraftingTable();

    List<Item> getExtraResults();

    default Collection<ItemDescriptor> getIngredientList() {
        return List.of();
    }

    default List<Item> getAllResults() {
        List<Item> results = new ArrayList<>();
        results.add(this.getResult());
        results.addAll(this.getExtraResults());
        return results;
    }

    int getPriority();

    /**
     * Returns whether the specified list of crafting grid inputs and outputs matches this recipe. Outputs DO NOT
     * include the primary result item.
     *
     * @param inputList list of items taken from the crafting grid
     * @param extraOutputList list of items put back into the crafting grid (secondary results)
     * @return bool
     */
    boolean matchItems(List<Item> inputList, List<Item> extraOutputList);

    RecipeUnlockingRequirement getRequirement();
}
