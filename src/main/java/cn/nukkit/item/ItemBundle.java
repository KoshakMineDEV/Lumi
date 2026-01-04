package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemBundle extends StringItemBase {

    public ItemBundle() {
        this(BUNDLE, "Bundle");
    }

    public ItemBundle(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_40;
    }
}