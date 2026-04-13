package cn.nukkit.item;

import cn.nukkit.item.trim.ItemTrimMaterialType;
import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemResinBrick extends StringItemBase implements ItemTrimMaterial {
    public ItemResinBrick() {
        super(RESIN_BRICK, "Resin Brick");
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_50;
    }

    @Override
    public ItemTrimMaterialType getMaterial() {
        return ItemTrimMaterialType.MATERIAL_RESIN;
    }
}
