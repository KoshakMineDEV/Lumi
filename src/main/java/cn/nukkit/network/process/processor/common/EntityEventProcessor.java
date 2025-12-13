package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.inventory.AnvilInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @author SocialMoods
 */
public class EntityEventProcessor extends DataPacketProcessor<EntityEventPacket> {

    public static final EntityEventProcessor INSTANCE = new EntityEventProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull EntityEventPacket packet) {
        if (!handle.isSpawned() || !handle.isAlive()) {
            return;
        }

        if (packet.event != EntityEventPacket.ENCHANT) {
            handle.setCraftingType(Player.CRAFTING_SMALL);
        }

        switch (packet.event) {
            case EntityEventPacket.EATING_ITEM:
                if (packet.data == 0 || packet.eid != handle.getId()) {
                    return;
                }

                if (Server.getInstance().getTick() - handle.getLastEating() < 4) {
                    return;
                }

                Item hand = handle.player.getInventory().getItemInHand();
                if (!hand.canRelease()) {
                    return;
                }

                handle.setLastEating(0);
                packet.isEncoded = false;
                packet.originProtocol = handle.getProtocol();
                packet.data = (hand.getNetworkId(handle.getProtocol()) << 16) | hand.getDamage();
                handle.player.dataPacket(packet);
                Server.broadcastPacket(handle.player.getViewers().values(), packet);
                break;

            case EntityEventPacket.ENCHANT:
                if (packet.eid != handle.getId()) {
                    return;
                }

                Inventory inventory = handle.player.getWindowById(Player.ANVIL_WINDOW_ID);
                if (inventory instanceof AnvilInventory) {
                    ((AnvilInventory) inventory).setCost(-packet.data);
                }
                break;
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.ENTITY_EVENT_PACKET;
    }

    @Override
    public Class<? extends EntityEventPacket> getPacketClass() {
        return EntityEventPacket.class;
    }
}
