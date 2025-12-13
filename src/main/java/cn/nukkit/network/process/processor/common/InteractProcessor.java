package cn.nukkit.network.process.processor.common;

import cn.nukkit.PlayerHandle;
import cn.nukkit.entity.*;
import cn.nukkit.entity.item.EntityChestBoat;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.event.player.PlayerMouseOverEntityEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @author SocialMoods
 */
public class InteractProcessor extends DataPacketProcessor<InteractPacket> {

    public static final InteractProcessor INSTANCE = new InteractProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull InteractPacket packet) {
        if (!handle.isSpawned() || !handle.isAlive()) return;

        if (packet.target == 0 && packet.action == InteractPacket.ACTION_MOUSEOVER) {
            handle.setButtonText("");
            return;
        }

        Entity targetEntity = packet.target == handle.getId()
                ? handle.player
                : handle.player.level.getEntity(packet.target);

        if (packet.action != InteractPacket.ACTION_OPEN_INVENTORY &&
            (targetEntity == null || !handle.isAlive() || !targetEntity.isAlive())) {
            return;
        }

        if (packet.action != InteractPacket.ACTION_OPEN_INVENTORY &&
            (targetEntity instanceof EntityItem || targetEntity instanceof EntityArrow || targetEntity instanceof EntityXPOrb)) {
            handle.player.getServer().getLogger().warning(handle.player.getUsername() + " attempted to interact with invalid entity");
            return;
        }

        switch (packet.action) {
            case InteractPacket.ACTION_OPEN_INVENTORY -> {
                if (!handle.isInventoryOpen()) {
                    if (handle.getRiding() instanceof EntityChestBoat && handle.getRiding() == targetEntity) {
                        handle.openInventory((InventoryHolder) targetEntity);
                    } else if (handle.getProtocol() >= 407) {
                        if (handle.player.getInventory().open(handle.player)) {
                            handle.setInventoryOpen(true);
                        }
                    }
                }
            }
            case InteractPacket.ACTION_MOUSEOVER -> {
                if (packet.target == 0) return;

                String buttonText = "";
                if (targetEntity instanceof EntityInteractable) {
                    buttonText = ((EntityInteractable) targetEntity).getInteractButtonText(handle.player);
                    if (buttonText == null) buttonText = "";
                }
                handle.setButtonText(buttonText);
                handle.callEvent(new PlayerMouseOverEntityEvent(handle.player, targetEntity));
            }
            case InteractPacket.ACTION_VEHICLE_EXIT -> {
                if (!(targetEntity instanceof EntityRideable) || handle.getRiding() != targetEntity) return;
                ((EntityRideable) handle.getRiding()).dismountEntity(handle.player);
            }
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.INTERACT_PACKET;
    }

    @Override
    public Class<? extends InteractPacket> getPacketClass() {
        return InteractPacket.class;
    }
}
