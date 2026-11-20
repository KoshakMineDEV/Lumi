package cn.nukkit.network.protocol;

import cn.nukkit.item.Item;
import lombok.ToString;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
@ToString
public class MobArmorEquipmentPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.MOB_ARMOR_EQUIPMENT_PACKET;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    public long eid;
    public Item[] slots = new Item[4];
    /**
     * @since v712
     */
    public Item body = Item.AIR_ITEM;

    @Override
    public void decode() {
        this.eid = this.getEntityRuntimeId();
        this.slots = new Item[4];
        this.slots[0] = this.protocol >= ProtocolInfo.v1_26_30 ? this.getNetworkItemStackDescriptor(this.protocol) : this.getSlot(this.protocol);
        this.slots[1] = this.protocol >= ProtocolInfo.v1_26_30 ? this.getNetworkItemStackDescriptor(this.protocol) : this.getSlot(this.protocol);
        this.slots[2] = this.protocol >= ProtocolInfo.v1_26_30 ? this.getNetworkItemStackDescriptor(this.protocol) : this.getSlot(this.protocol);
        this.slots[3] = this.protocol >= ProtocolInfo.v1_26_30 ? this.getNetworkItemStackDescriptor(this.protocol) : this.getSlot(this.protocol);
        if (this.protocol >= ProtocolInfo.v1_21_20) {
            this.body = this.protocol >= ProtocolInfo.v1_26_30 ? this.getNetworkItemStackDescriptor(this.protocol) : this.getSlot(this.protocol);
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        for (int i = 0; i < 4; i++) {
            this.putEquipmentItem(this.slots[i]);
        }
        if (this.protocol >= ProtocolInfo.v1_21_20) {
            this.putEquipmentItem(this.body);
        }
    }

    private void putEquipmentItem(Item item) {
        if (this.protocol >= ProtocolInfo.v1_26_30) {
            this.putNetworkItemStackDescriptor(this.protocol, item);
        } else {
            this.putSlot(this.protocol, item);
        }
    }
}
