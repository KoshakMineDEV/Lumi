package cn.nukkit.recipe.impl;

import cn.nukkit.block.BlockShulkerBox;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.recipe.RecipeType;
import cn.nukkit.recipe.descriptor.ItemDescriptor;

import java.util.Collection;
import java.util.List;

public class ShulkerBoxDyeRecipe extends ShapelessRecipe {

    public ShulkerBoxDyeRecipe(String recipeId, int priority, Item result, Collection<ItemDescriptor> ingredients) {
        super(recipeId, priority, result, ingredients);
    }

    @Override
    public RecipeType getType() {
        return RecipeType.SHULKER_BOX;
    }

    public Item getResult(List<Item> consumedItems) {
        Item shulkerBox = null;
        for (Item item : consumedItems) {
            if (item instanceof ItemBlock block && block.getBlockUnsafe() instanceof BlockShulkerBox) {
                if (shulkerBox != null || item.getCount() != 1) {
                    return Item.get(Item.AIR);
                }
                shulkerBox = item;
            }
        }
        if (shulkerBox == null) {
            return Item.get(Item.AIR);
        }
        return this.getResult().setCompoundTag(shulkerBox.getCompoundTag().clone());
    }
}
