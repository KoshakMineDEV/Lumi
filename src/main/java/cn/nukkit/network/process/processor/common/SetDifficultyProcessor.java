package cn.nukkit.network.process.processor.common;

import cn.nukkit.Difficulty;
import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetDifficultyPacket;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @author glorydark
 * @date {2024/1/10} {12:28}
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SetDifficultyProcessor extends DataPacketProcessor<SetDifficultyPacket> {

    public static final SetDifficultyProcessor INSTANCE = new SetDifficultyProcessor();

    @Override
    public void handle(@NotNull PlayerHandle playerHandle, @NotNull SetDifficultyPacket pk) {
        Player player = playerHandle.player;
        if (!player.spawned || !player.hasPermission("nukkit.command.difficulty")) {
            return;
        }
        Difficulty difficulty = Difficulty.byId(pk.difficulty);
        if (difficulty == null) {
            return;
        }
        player.getServer().setDifficulty(difficulty);

        SetDifficultyPacket difficultyPacket = new SetDifficultyPacket();
        difficultyPacket.difficulty = difficulty.ordinal();
        Server.broadcastPacket(player.getServer().getOnlinePlayers().values(), difficultyPacket);
        Command.broadcastCommandMessage(player, new TranslationContainer("commands.difficulty.success", difficulty.getTranslationKey()));
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.toNewProtocolID(ProtocolInfo.SET_DIFFICULTY_PACKET);
    }

    @Override
    public Class<? extends DataPacket> getPacketClass() {
        return SetDifficultyPacket.class;
    }
}
