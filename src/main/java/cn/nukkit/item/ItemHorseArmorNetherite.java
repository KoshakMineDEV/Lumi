package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemHorseArmorNetherite extends StringItemBase {
    public ItemHorseArmorNetherite() {
        super(NETHERITE_HORSE_ARMOR, "Netherite Horse Armor");
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_130;
    }
}