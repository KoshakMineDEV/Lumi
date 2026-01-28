package cn.nukkit.recipe.impl.special;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemGunpowder;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemPaper;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.impl.MultiRecipe;

import java.util.Collection;

public class FireworkRecipe extends MultiRecipe {

    public FireworkRecipe() {
        super(TYPE_FIREWORKS);
    }

    @Override
    public boolean canExecute(Player player, Item outputItem, Collection<ItemDescriptor> inputs) {
        if (outputItem.getId() == ItemID.FIREWORKS) {
            boolean hasPaper = false;
            int powder = 0;
            for (ItemDescriptor input : inputs) {
                if(input instanceof DefaultDescriptor descriptor) {
                    Item item = descriptor.getItem();
                    if (item instanceof ItemGunpowder) {
                        powder += item.getCount();
                    } else if (item instanceof ItemPaper) {
                        hasPaper = true;
                    }
                }
            }
            if(powder != outputItem.getNamedTag().getCompound("Fireworks").getByte("Flight")) {
                return false;
            }
            if (!hasPaper) {
                return false;
            }
            return true;
        }
        return false;
    }
}