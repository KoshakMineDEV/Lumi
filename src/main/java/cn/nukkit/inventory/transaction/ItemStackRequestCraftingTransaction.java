package cn.nukkit.inventory.transaction;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.recipe.Recipe;

import java.util.Collections;
import java.util.List;

public class ItemStackRequestCraftingTransaction extends CraftingTransaction {

    public ItemStackRequestCraftingTransaction(Player source, List<Item> inputs, Item primaryOutput, Recipe recipe) {
        super(source, Collections.emptyList());
        for (Item input : inputs) {
            if (input != null && !input.isNull()) {
                setInput(input);
            }
        }
        setTransactionRecipe(recipe);
        if (primaryOutput != null && !primaryOutput.isNull()) {
            this.primaryOutput = primaryOutput.clone();
        }
    }

    @Override
    public void setInput(Item item) {
        for (Item existing : this.inputs) {
            if (existing.equals(item, true, true)) {
                existing.setCount(existing.getCount() + item.getCount());
                return;
            }
        }
        this.inputs.add(item.clone());
    }

    @Override
    public boolean canExecute() {
        throw new UnsupportedOperationException(
                "ItemStackRequestCraftingTransaction is a read-only snapshot; canExecute() is not supported");
    }

    @Override
    public boolean execute() {
        throw new UnsupportedOperationException(
                "ItemStackRequestCraftingTransaction is a read-only snapshot; execute() is not supported");
    }
}
