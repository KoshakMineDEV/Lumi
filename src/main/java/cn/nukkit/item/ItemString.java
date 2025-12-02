package cn.nukkit.item;

import cn.nukkit.block.Block;

public class ItemString extends StringItemBase {
    public ItemString() {
        super(ItemNamespaceId.STRING, "String");
        this.block = Block.get(TRIPWIRE);
    }
}
