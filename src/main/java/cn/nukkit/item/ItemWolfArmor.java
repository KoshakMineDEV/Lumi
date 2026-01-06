package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemWolfArmor extends StringItemColorArmorBase {

    public ItemWolfArmor() {
        super(WOLF_ARMOR, "Wolf Armor");
    }

    @Override
    public int getMaxDurability() {
        return 64;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_80;
    }
}
