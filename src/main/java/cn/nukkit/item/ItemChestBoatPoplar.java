package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemChestBoatPoplar extends ItemChestBoatBase {

    public ItemChestBoatPoplar() {
        this(0, 1);
    }

    public ItemChestBoatPoplar(Integer meta) {
        this(meta, 1);
    }

    public ItemChestBoatPoplar(Integer meta, int count) {
        super(POPLAR_CHEST_BOAT, meta, count, "Poplar Boat with Chest");
    }

    @Override
    public int getBoatId() {
        return 10;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_26_40;
    }
}
