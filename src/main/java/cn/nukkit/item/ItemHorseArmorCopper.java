package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemHorseArmorCopper extends StringItemBase {
    public ItemHorseArmorCopper() {
        super(COPPER_HORSE_ARMOR, "Copper Horse Armor");
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_111;
    }
}
