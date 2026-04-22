package cn.nukkit.recipe.impl.special;

import cn.nukkit.Player;
import cn.nukkit.block.BlockShulkerBox;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemDye;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.recipe.Recipe;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.impl.MultiRecipe;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class ShulkerBoxRecipe extends MultiRecipe {
    public ShulkerBoxRecipe() {
        super(UUID.randomUUID());
    }

    private static final Set<String> DYE_ITEMS = Set.of(
            ItemNamespaceId.LAPIS_LAZULI,
            ItemNamespaceId.COCOA_BEANS,
            ItemNamespaceId.INK_SAC,
            ItemNamespaceId.BONE_MEAL
    );

    @Override
    public boolean canExecute(Player player, Item outputItem, Collection<ItemDescriptor> inputs) {
        boolean dye = false;
        boolean shulkerBox = false;

        for(ItemDescriptor itemDescriptor : inputs) {
            if(itemDescriptor instanceof DefaultDescriptor descriptor) {
                Item item = descriptor.getItem();

                if(item instanceof ItemDye || DYE_ITEMS.contains(item.getNamespaceId())) {
                    dye = true;
                }

                if(item instanceof ItemBlock itemBlock && itemBlock.getBlock() instanceof BlockShulkerBox) {
                    shulkerBox = true;
                }
            }
        }

        return dye && shulkerBox;
    }

    @Override
    public boolean hideRecipe() {
        return true;
    }

    @Override
    public Recipe toRecipe(Item outputItem, Collection<ItemDescriptor> inputs) {
        for(ItemDescriptor itemDescriptor : inputs) {
            if(itemDescriptor instanceof DefaultDescriptor descriptor && descriptor.getItem() instanceof ItemBlock itemBlock) {
                if(itemBlock.getBlock() instanceof BlockShulkerBox) {
                    outputItem.setCompoundTag(itemBlock.getCompoundTag());
                }
            }
        }

        return super.toRecipe(outputItem, inputs);
    }
}
