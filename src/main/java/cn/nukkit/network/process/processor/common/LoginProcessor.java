package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.entity.data.skin.Skin;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.network.encryption.PrepareEncryptionTask;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ServerToClientHandshakePacket;
import cn.nukkit.plugin.InternalPlugin;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.ClientChainData;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;

import static cn.nukkit.Player.PLAYER_NAME_PATTERN;
import static cn.nukkit.entity.Entity.DATA_NAMETAG;

/**
 * @author SocialMoods
 */
public class LoginProcessor extends DataPacketProcessor<LoginPacket> {

    public static final LoginProcessor INSTANCE = new LoginProcessor();

    @Override
    public void handle(@NotNull PlayerHandle h, @NotNull LoginPacket pk) {
        Player p = h.player;
        Server server = p.getServer();

        if (h.isLoginPacketReceived()) {
            h.close("", "Invalid login packet");
            return;
        }

        h.setLoginPacketReceived(true);
        h.setProtocol(pk.getProtocol());

        String cleanName = TextFormat.clean(pk.username);
        switch (server.getSettings().player().spaceNameMode()) {
            case DISABLED -> {
                if (cleanName.contains(" ")) {
                    h.close("", "Invalid name (please remove spaces)");
                    return;
                }
                h.setUnverifiedUsername(cleanName);
            }
            case REPLACING -> h.setUnverifiedUsername(cleanName.replace(" ", "_"));
            default -> h.setUnverifiedUsername(cleanName);
        }

        int protocol = h.getProtocol();

        if (!ProtocolInfo.SUPPORTED_PROTOCOLS.contains(protocol)) {
            h.close("", "You are running unsupported Minecraft version");
            server.getLogger().debug(h.getUnverifiedUsername() +
                    " disconnected with protocol (SupportedProtocols) " + protocol);
            return;
        }

        int min = server.getSettings().general().multiversion().minProtocol();
        int max = server.getSettings().general().multiversion().maxProtocol();

        if (protocol < min) {
            h.close("", "Multiversion support for this Minecraft version is disabled");
            server.getLogger().debug(h.getUnverifiedUsername() +
                    " disconnected with protocol (minProtocol) " + protocol);
            return;
        }

        if (max >= Math.max(0, min) && protocol > max) {
            h.close("", "Support for this Minecraft version is not enabled");
            server.getLogger().debug(h.getUnverifiedUsername() +
                    " disconnected with protocol (maxProtocol) " + protocol);
            return;
        }

        if (pk.skin == null) {
            h.close("", "disconnectionScreen.invalidSkin");
            return;
        }

        if (server.getOnlinePlayers().size() >= server.getMaxPlayers()
                && p.kick(PlayerKickEvent.Reason.SERVER_FULL,
                "disconnectionScreen.serverFull", false)) {
            return;
        }

        ClientChainData chainData;
        try {
            chainData = ClientChainData.read(pk);
        } catch (ClientChainData.TooBigSkinException e) {
            h.close("", "disconnectionScreen.invalidSkin");
            return;
        }

        h.setLoginChainData(chainData);

        if (!chainData.isXboxAuthed() && server.getSettings().network().xboxAuth()) {
            h.close("", "disconnectionScreen.notAuthenticated");
            if (server.getSettings().network().xboxAuthTempIpBan()) {
                server.getNetwork().blockAddress(
                        p.getSocketAddress().getAddress(), 5
                );
                server.getLogger().notice(
                        "Blocked " + p.getAddress() + " for 5 seconds due to failed Xbox auth"
                );
            }
            return;
        }

        if (server.getSettings().general().useWaterdog()
                && chainData.getWaterdogIP() != null) {
            h.setSocketAddress(new InetSocketAddress(
                    chainData.getWaterdogIP(), p.getRawPort()
            ));
        }

        h.setVersion(chainData.getGameVersion());

        String username = h.getUnverifiedUsername();
        h.setUsername(username);
        h.setUnverifiedUsername(null);
        h.setDisplayName(username);
        h.setIusername(username.toLowerCase(Locale.ROOT));
        p.setDataProperty(new StringEntityData(DATA_NAMETAG, username), false);

        h.setRandomClientId(pk.clientId);

        UUID loginUuid = pk.clientUUID;
        UUID finalUuid = server.lookupName(username).orElse(
                chainData.isXboxAuthed()
                        ? loginUuid
                        : UUID.nameUUIDFromBytes(username.getBytes())
        );

        h.setLoginUuid(loginUuid);
        h.setUuid(finalUuid);
        h.setRawUUID(Binary.writeUUID(finalUuid));

        Matcher matcher = PLAYER_NAME_PATTERN.matcher(pk.username);
        if (!matcher.matches()
                || Objects.equals(h.getIusername(), "rcon")
                || Objects.equals(h.getIusername(), "console")) {
            h.close("", "disconnectionScreen.invalidName");
            return;
        }

        if (!pk.skin.isValid()) {
            h.close("", "disconnectionScreen.invalidSkin");
            return;
        }

        Skin skin = pk.skin;
        p.setSkin(
                skin.isPersona()
                        && !server.getSettings().player().personaSkins()
                        ? Skin.NO_PERSONA_SKIN
                        : skin
        );

        PlayerPreLoginEvent ev =
                new PlayerPreLoginEvent(p, "Plugin reason");
        server.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            h.close("", ev.getKickMessage());
            return;
        }

        if (p.isEnableNetworkEncryption()) {
            server.getScheduler().scheduleAsyncTask(
                    InternalPlugin.INSTANCE,
                    new PrepareEncryptionTask(p) {
                        @Override
                        public void onCompletion(Server server) {
                            if (!p.isConnected()) {
                                return;
                            }

                            if (getHandshakeJwt() == null
                                    || getEncryptionKey() == null
                                    || getEncryptionCipher() == null
                                    || getDecryptionCipher() == null) {
                                p.close("", "Network Encryption error");
                                return;
                            }

                            ServerToClientHandshakePacket out =
                                    new ServerToClientHandshakePacket();
                            out.setJwt(getHandshakeJwt());
                            p.forceDataPacket(out, () -> {
                                h.setAwaitingEncryptionHandshake(true);
                                h.getNetworkSession().setEncryption(
                                        getEncryptionKey(),
                                        getEncryptionCipher(),
                                        getDecryptionCipher()
                                );
                            });
                        }
                    }
            );
        } else {
            h.processPreLogin();
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.LOGIN_PACKET;
    }

    @Override
    public Class<LoginPacket> getPacketClass() {
        return LoginPacket.class;
    }
}
