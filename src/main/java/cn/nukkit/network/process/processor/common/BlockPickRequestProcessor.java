package cn.nukkit.network.process.processor.common;

import cn.nukkit.PlayerHandle;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.event.player.PlayerBlockPickEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.BlockPickRequestPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @author SocialMoods
 */
public class BlockPickRequestProcessor extends DataPacketProcessor<BlockPickRequestPacket> {

    public static final BlockPickRequestProcessor INSTANCE = new BlockPickRequestProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull BlockPickRequestPacket packet) {
        if (!handle.isSpawned() || !handle.isAlive() || handle.player.getInventory() == null || handle.isInventoryOpen()) {
            return;
        }

        Block block = handle.player.getLevel().getBlock(packet.x, packet.y, packet.z, false);
        if (block.distanceSquared(handle.player) > 1000) {
            return;
        }

        Item item = block.toItem();

        if (packet.addUserData) {
            BlockEntity blockEntity = handle.player.getLevel().getBlockEntityIfLoaded(block);
            if (blockEntity != null) {
                CompoundTag nbt = blockEntity.getCleanedNBT();
                if (nbt != null) {
                    item.setCustomBlockData(nbt);
                    item.setLore("+(DATA)");
                }
            }
        }

        PlayerBlockPickEvent pickEvent = new PlayerBlockPickEvent(handle.player, block, item);
        if (handle.player.isSpectator()) pickEvent.setCancelled();

        handle.callEvent(pickEvent);

        if (!pickEvent.isCancelled()) {
            PlayerInventory inventory = handle.player.getInventory();
            boolean itemExists = false;
            int itemSlot = -1;

            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (inventory.getItem(slot).equals(pickEvent.getItem())) {
                    if (slot < inventory.getHotbarSize()) {
                        inventory.setHeldItemSlot(slot);
                    } else {
                        itemSlot = slot;
                    }
                    itemExists = true;
                    break;
                }
            }

            for (int slot = 0; slot < inventory.getHotbarSize(); slot++) {
                if (inventory.getItem(slot).isNull()) {
                    if (!itemExists && handle.isCreative()) {
                        inventory.setHeldItemSlot(slot);
                        inventory.setItemInHand(pickEvent.getItem());
                        return;
                    } else if (itemSlot > -1) {
                        inventory.setHeldItemSlot(slot);
                        inventory.setItemInHand(inventory.getItem(itemSlot));
                        inventory.clear(itemSlot, true);
                        return;
                    }
                }
            }

            if (!itemExists && handle.isCreative()) {
                Item itemInHand = inventory.getItemInHand();
                inventory.setItemInHand(pickEvent.getItem());
                if (!inventory.isFull()) {
                    for (int slot = 0; slot < inventory.getSize(); slot++) {
                        if (inventory.getItem(slot).isNull()) {
                            inventory.setItem(slot, itemInHand);
                            break;
                        }
                    }
                }
            } else if (itemSlot > -1) {
                Item itemInHand = inventory.getItemInHand();
                inventory.setItemInHand(inventory.getItem(itemSlot));
                inventory.setItem(itemSlot, itemInHand);
            }
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.BLOCK_PICK_REQUEST_PACKET;
    }

    @Override
    public Class<? extends BlockPickRequestPacket> getPacketClass() {
        return BlockPickRequestPacket.class;
    }
}
