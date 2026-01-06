package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemNautilusArmor extends StringItemBase {

    public ItemNautilusArmor(String namespaceId, String name) {
        super(namespaceId, name);
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_130;
    }
}
