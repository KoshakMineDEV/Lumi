package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SettingsCommandPacket;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * @author SocialMoods
 */
@Slf4j
public class SettingsCommandProcessor extends DataPacketProcessor<SettingsCommandPacket> {

    public static SettingsCommandProcessor INSTANCE = new SettingsCommandProcessor();

    @Override
    public void handle(@NotNull PlayerHandle playerHandle, @NotNull SettingsCommandPacket pk) {
        Player player = playerHandle.player.asPlayer();

        String command = pk.command.toLowerCase(Locale.ENGLISH).replace("/", "");
        Server.getInstance().dispatchCommand(player, command);
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.SETTINGS_COMMAND_PACKET;
    }

    @Override
    public Class<? extends DataPacket> getPacketClass() {
        return SettingsCommandPacket.class;
    }
}