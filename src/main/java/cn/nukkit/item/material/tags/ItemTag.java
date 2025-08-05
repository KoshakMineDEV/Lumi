package cn.nukkit.item.material.tags;

import cn.nukkit.item.material.ItemType;
import cn.nukkit.item.material.tags.impl.SimpleItemTag;

import java.util.Set;

public interface ItemTag {

    Set<ItemType> getItemTypes();

    default boolean has(ItemType blockType) {
        return this.getItemTypes().contains(blockType);
    }

    static ItemTag of(ItemType... blockTypes) {
        return new SimpleItemTag(blockTypes);
    }
}
