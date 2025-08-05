package cn.nukkit.item.material;

import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.item.customitem.CustomItem;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.Data;

@Data
public final class CustomItemType implements ItemType {
    private final String identifier;

    public CustomItemType(CustomItem customItem) {
        this.identifier = customItem.getDefinition().identifier();
    }

    @Override
    public int getRuntimeId() {
        return Item.STRING_IDENTIFIED_ITEM;
    }

    @Override
    public Item createItem(int count, int meta) {
        var mapping = RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL);
        var item = mapping.getItemByNamespaceId(this.identifier, count);
        item.setDamage(meta);
        return item;
    }
}
