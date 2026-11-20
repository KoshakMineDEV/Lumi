package cn.nukkit.network.process.processor.common;

import cn.nukkit.PlayerHandle;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ItemStackRequestPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.NotNull;

public class ItemStackRequestProcessor extends DataPacketProcessor<ItemStackRequestPacket> {

    public static final ItemStackRequestProcessor INSTANCE = new ItemStackRequestProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull ItemStackRequestPacket packet) {
        handle.player.handleItemStackRequests(packet.getRequests());
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.toNewProtocolID(ProtocolInfo.ITEM_STACK_REQUEST_PACKET);
    }

    @Override
    public Class<? extends DataPacket> getPacketClass() {
        return ItemStackRequestPacket.class;
    }

    @Override
    public boolean isSupported(int protocol) {
        return protocol >= ProtocolInfo.v1_20_0_23;
    }
}
