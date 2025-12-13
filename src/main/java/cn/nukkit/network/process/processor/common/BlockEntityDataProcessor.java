package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author SocialMoods
 */
public class BlockEntityDataProcessor extends DataPacketProcessor<BlockEntityDataPacket> {

    public static final BlockEntityDataProcessor INSTANCE = new BlockEntityDataProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull BlockEntityDataPacket packet) {
        if (!handle.isSpawned() || !handle.isAlive()) {
            return;
        }

        handle.setCraftingType(Player.CRAFTING_SMALL);
        handle.resetCraftingGridType();

        var pos = handle.getTemporalVector().setComponents(packet.x, packet.y, packet.z);
        if (pos.distanceSquared(handle.player) > 10000) {
            return;
        }

        BlockEntity t = handle.player.getLevel().getBlockEntity(pos);
        if (t instanceof BlockEntitySpawnable spawnable) {
            CompoundTag nbt;
            try {
                nbt = NBTIO.read(new ByteArrayInputStream(packet.namedTag), ByteOrder.LITTLE_ENDIAN, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!spawnable.updateCompoundTag(nbt, handle.player)) {
                spawnable.spawnTo(handle.player);
            }
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.BLOCK_ENTITY_DATA_PACKET;
    }

    @Override
    public Class<? extends BlockEntityDataPacket> getPacketClass() {
        return BlockEntityDataPacket.class;
    }
}
