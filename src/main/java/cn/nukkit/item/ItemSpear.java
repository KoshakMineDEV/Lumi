package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;

public abstract class ItemSpear extends StringItemToolBase {

    public ItemSpear(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean isSpear() {
        return true;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_130;
    }
}