package cn.nukkit.network.protocol.types.inventory.itemstack.request;

import cn.nukkit.item.Item;
import cn.nukkit.item.material.tags.ItemTag;

public record CraftIngredient(Item item, ItemTag tag, int count) {

    public boolean matches(Item candidate) {
        if (this.tag != null) {
            return this.tag.has(candidate.getItemType());
        }
        return this.item != null && this.item.equals(candidate, this.item.getDamage() != -1, false);
    }
}
