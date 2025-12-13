package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.PlayerEnderChestInventory;
import cn.nukkit.inventory.TradeInventory;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.ContainerIds;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * @author SocialMoods
 */
public class ContainerCloseProcessor extends DataPacketProcessor<ContainerClosePacket> {

    public static final ContainerCloseProcessor INSTANCE = new ContainerCloseProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull ContainerClosePacket packet) {
        if (!handle.isSpawned()) {
            return;
        }

        if (packet.windowId == -1) {
            if (handle.isInventoryOpen()) {
                handle.setInventoryOpen(false);

                if (handle.getCraftingType() == Player.CRAFTING_SMALL) {
                    for (Entry<cn.nukkit.inventory.Inventory, Integer> open : new ArrayList<>(handle.getWindows().entrySet())) {
                        if (open.getKey() instanceof ContainerInventory || open.getKey() instanceof PlayerEnderChestInventory) {
                            Server.getInstance().getPluginManager().callEvent(new InventoryCloseEvent(open.getKey(), handle.player));
                            handle.setClosingWindowId(Integer.MAX_VALUE);
                            handle.removeWindow(open.getKey(), true);
                            handle.setClosingWindowId(Integer.MIN_VALUE);
                        }
                    }
                    return;
                }
            }

            handle.setCraftingType(Player.CRAFTING_SMALL);
            handle.resetCraftingGridType();
            handle.player.addWindow(handle.player.getCraftingGrid(), ContainerIds.NONE);

            if (handle.getProtocol() >= 407) {
                ContainerClosePacket pk = new ContainerClosePacket();
                pk.windowId = -1;
                pk.wasServerInitiated = false;
                handle.player.dataPacket(pk);
            }

            TradeInventory tradeInventory = handle.player.getTradeInventory();
            if (tradeInventory != null) {
                handle.removeWindow(tradeInventory, true);
            }

        } else if (handle.getWindowIndex().containsKey(packet.windowId)) {
            handle.setInventoryOpen(false);
            cn.nukkit.inventory.Inventory inn = handle.getWindowIndex().get(packet.windowId);
            Server.getInstance().getPluginManager().callEvent(new InventoryCloseEvent(inn, handle.player));
            handle.setClosingWindowId(packet.windowId);
            handle.removeWindow(inn, true);
            handle.setClosingWindowId(Integer.MIN_VALUE);
        } else {
            ContainerClosePacket pk = new ContainerClosePacket();
            pk.windowId = packet.windowId;
            pk.wasServerInitiated = false;
            handle.player.dataPacket(pk);
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.CONTAINER_CLOSE_PACKET;
    }

    @Override
    public Class<? extends ContainerClosePacket> getPacketClass() {
        return ContainerClosePacket.class;
    }
}
