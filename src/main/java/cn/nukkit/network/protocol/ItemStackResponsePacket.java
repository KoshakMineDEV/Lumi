package cn.nukkit.network.protocol;

import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponse;
import cn.nukkit.network.protocol.types.inventory.itemstack.response.ItemStackResponseStatus;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@NoArgsConstructor
public class ItemStackResponsePacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.ITEM_STACK_RESPONSE_PACKET;

    public final List<ItemStackResponse> entries = new ArrayList<>();

    @Override
    public void encode() {
        this.reset();
        this.putArray(entries, response -> {
            this.putByte((byte) response.getResult().ordinal());
            this.putVarInt(response.getRequestId());
            if (this.protocol >= ProtocolInfo.v1_26_40) {
                if (this.protocol < ProtocolInfo.v1_26_50) {
                    this.putBoolean(true);
                }
                boolean present = !response.getContainers().isEmpty();
                this.putBoolean(present);
                if (!present) return;
            } else if (response.getResult() != ItemStackResponseStatus.OK) {
                return;
            }
            this.putArray(response.getContainers(), container -> {
                if (this.protocol >= ProtocolInfo.v1_21_30) {
                    this.writeFullContainerName(new FullContainerName(container.getContainer(), container.getDynamicId()));
                } else {
                    this.putByte((byte) container.getContainer().getId());
                    if (this.protocol >= ProtocolInfo.v1_21_20) {
                        this.putLInt(container.getDynamicId() == null ? 0 : container.getDynamicId());
                    }
                }
                this.putArray(container.getItems(), item -> {
                    this.putByte((byte) item.getSlot());
                    this.putByte((byte) item.getHotbarSlot());
                    this.putByte((byte) item.getCount());
                    if (this.protocol >= ProtocolInfo.v1_26_40) {
                        if (this.protocol < ProtocolInfo.v1_26_50) {
                            this.putBoolean(true);
                        }
                        boolean present = item.getStackNetworkId() != 0;
                        this.putBoolean(present);
                        if (present) this.putVarInt(item.getStackNetworkId());
                    } else {
                        this.putVarInt(item.getStackNetworkId());
                    }
                    this.putString(item.getCustomName());
                    if (this.protocol >= ProtocolInfo.v1_26_40) {
                        this.putOptionalNull(item.getFilteredCustomName(), this::putString);
                    } else if (this.protocol >= ProtocolInfo.v1_21_50) {
                        this.putString(item.getFilteredCustomName() == null ? "" : item.getFilteredCustomName());
                    }
                    this.putVarInt(item.getDurabilityCorrection());
                });
            });
        });
    }
    @Override
    public void decode() {
        throw new UnsupportedOperationException();//client bound
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }
}
