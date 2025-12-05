package cn.nukkit.network.process.processor.common;

import cn.nukkit.PlayerHandle;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.ICommandBlock;
import cn.nukkit.blockentity.impl.BlockEntityCommandBlock;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.CommandBlockUpdatePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommandBlockUpdateProcessor extends DataPacketProcessor<CommandBlockUpdatePacket> {

    public static final CommandBlockUpdateProcessor INSTANCE = new CommandBlockUpdateProcessor();

    @Override
    public void handle(@NotNull PlayerHandle playerHandle, @NotNull CommandBlockUpdatePacket pk) {
        var player = playerHandle.player;
        if (player == null || !player.isOp() || !player.isCreative()) {
            return;
        }

        if (pk.isBlock) {
            BlockEntity blockEntity = player.level.getBlockEntity(new Vector3(pk.x, pk.y, pk.z));
            if (blockEntity instanceof BlockEntityCommandBlock commandBlock) {
                cn.nukkit.block.Block cmdBlock = commandBlock.getLevelBlock();

                int targetBlockId = switch (pk.commandBlockMode) {
                    case ICommandBlock.MODE_REPEATING -> cn.nukkit.block.BlockID.REPEATING_COMMAND_BLOCK;
                    case ICommandBlock.MODE_CHAIN -> cn.nukkit.block.BlockID.CHAIN_COMMAND_BLOCK;
                    default -> cn.nukkit.block.BlockID.COMMAND_BLOCK;
                };

                if (cmdBlock.getId() != targetBlockId) {
                    int damage = cmdBlock.getDamage();
                    cn.nukkit.block.Block newBlock = cn.nukkit.block.Block.get(targetBlockId, damage);
                    newBlock.x = cmdBlock.x;
                    newBlock.y = cmdBlock.y;
                    newBlock.z = cmdBlock.z;
                    newBlock.level = cmdBlock.level;
                    cmdBlock = newBlock;
                    
                    if (pk.commandBlockMode == ICommandBlock.MODE_REPEATING) {
                        commandBlock.scheduleUpdate();
                    }
                }

                boolean conditional = pk.isConditional;
                int damage = cmdBlock.getDamage();
                int facingBits = damage & 0x07;
                int conditionalBit = conditional ? 0x08 : 0;
                int newDamage = facingBits | conditionalBit;
                
                if (damage != newDamage) {
                    cmdBlock.setDamage(newDamage);
                }

                commandBlock.setCommand(pk.command);
                commandBlock.setName(pk.name);
                commandBlock.setTrackOutput(pk.shouldTrackOutput);
                commandBlock.setConditional(conditional);
                commandBlock.setTickDelay(pk.tickDelay);
                commandBlock.setExecutingOnFirstTick(pk.executingOnFirstTick);

                boolean isRedstoneMode = pk.isRedstoneMode;
                commandBlock.setAuto(!isRedstoneMode);
                
                player.level.setBlock(commandBlock, cmdBlock, true, true);
                
                if (!isRedstoneMode && pk.commandBlockMode == ICommandBlock.MODE_NORMAL) {
                    commandBlock.trigger();
                }

                for (int side = 0; side <= 5; side++) {
                    cn.nukkit.block.Block sideBlock = commandBlock.getLevelBlock().getSide(cn.nukkit.math.BlockFace.fromIndex(side));
                    sideBlock.onUpdate(Level.BLOCK_UPDATE_REDSTONE);
                }
            }
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.toNewProtocolID(ProtocolInfo.COMMAND_BLOCK_UPDATE_PACKET);
    }

    @Override
    public Class<? extends DataPacket> getPacketClass() {
        return CommandBlockUpdatePacket.class;
    }
}

