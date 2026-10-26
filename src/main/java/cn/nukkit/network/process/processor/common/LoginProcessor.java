package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.entity.data.skin.Skin;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.network.encryption.PrepareEncryptionTask;
import cn.nukkit.network.session.NetherNetPlayerSession;
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

public class LoginProcessor extends DataPacketProcessor<LoginPacket> {

    public static final LoginProcessor INSTANCE = new LoginProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull LoginPacket packet) {
        Player p = handle.player;
        Server server = p.getServer();

        if (handle.isLoginPacketReceived()) {
            handle.close("", "Invalid login packet");
            return;
        }

        handle.setLoginPacketReceived(true);
        handle.setProtocol(packet.getProtocol());

        String cleanName = TextFormat.clean(packet.username);
        switch (server.getSettings().player().spaceNameMode()) {
            case DISABLED -> {
                if (cleanName.contains(" ")) {
                    handle.close("", "Invalid name (please remove spaces)");
                    return;
                }
                handle.setUnverifiedUsername(cleanName);
            }
            case REPLACING -> handle.setUnverifiedUsername(cleanName.replace(" ", "_"));
            default -> handle.setUnverifiedUsername(cleanName);
        }

        int protocol = handle.getProtocol();

        if (!ProtocolInfo.SUPPORTED_PROTOCOLS.contains(protocol)) {
            handle.close("", "You are running unsupported Minecraft version");
            server.getLogger().debug(handle.getUnverifiedUsername() +
                    " disconnected with protocol (SupportedProtocols) " + protocol);
            return;
        }

        int min = server.getSettings().general().multiversion().minProtocol();
        int max = server.getSettings().general().multiversion().maxProtocol();

        if (protocol < min) {
            handle.close("", "Multiversion support for this Minecraft version is disabled");
            server.getLogger().debug(handle.getUnverifiedUsername() +
                    " disconnected with protocol (minProtocol) " + protocol);
            return;
        }

        if (max >= Math.max(0, min) && protocol > max) {
            handle.close("", "Support for this Minecraft version is not enabled");
            server.getLogger().debug(handle.getUnverifiedUsername() +
                    " disconnected with protocol (maxProtocol) " + protocol);
            return;
        }

        if (packet.skin == null) {
            handle.close("", "disconnectionScreen.invalidSkin");
            return;
        }

        if (server.getOnlinePlayers().size() >= server.getMaxPlayers()
                && p.kick(PlayerKickEvent.Reason.SERVER_FULL,
                "disconnectionScreen.serverFull", false)) {
            return;
        }

        LoginData login = new LoginData(packet.username, packet.clientUUID, packet.clientId, packet.skin);
        handle.beginLoginVerification(packet, chainData -> continueVerifiedLogin(handle, login, chainData));
    }

    private void continueVerifiedLogin(PlayerHandle handle, LoginData login, ClientChainData chainData) {
        Player p = handle.player;
        Server server = p.getServer();

        handle.setLoginChainData(chainData);

        if (!chainData.isXboxAuthed() && server.getSettings().network().xboxAuth()) {
            handle.close("", "disconnectionScreen.notAuthenticated");
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

        if (handle.getNetworkSession() instanceof NetherNetPlayerSession session) {
            String refusal = session.checkIdentityBinding(chainData.getIdentityPublicKey());
            if (refusal != null) {
                server.getLogger().warning("Refusing NetherNet login from " + p.getSocketAddress() + ": " + refusal);
                handle.close("", "disconnectionScreen.notAuthenticated");
                return;
            }
        }

        if (server.getSettings().general().useWaterdog()
                && chainData.getWaterdogIP() != null) {
            handle.setSocketAddress(new InetSocketAddress(
                    chainData.getWaterdogIP(), p.getRawPort()
            ));
        }

        handle.setVersion(chainData.getGameVersion());

        String username = handle.getUnverifiedUsername();
        handle.setUsername(username);
        handle.setUnverifiedUsername(null);
        handle.setDisplayName(username);
        handle.setIusername(username.toLowerCase(Locale.ROOT));
        p.setDataProperty(new StringEntityData(DATA_NAMETAG, username), false);

        handle.setRandomClientId(login.clientId());

        UUID loginUuid = login.clientUuid();
        UUID finalUuid = server.lookupName(username).orElse(
                chainData.isXboxAuthed()
                        ? loginUuid
                        : UUID.nameUUIDFromBytes(username.getBytes())
        );

        handle.setLoginUuid(loginUuid);
        handle.setUuid(finalUuid);
        handle.setRawUUID(Binary.writeUUID(finalUuid));

        Matcher matcher = PLAYER_NAME_PATTERN.matcher(login.username());
        if (!matcher.matches()
                || Objects.equals(handle.getIusername(), "rcon")
                || Objects.equals(handle.getIusername(), "console")) {
            handle.close("", "disconnectionScreen.invalidName");
            return;
        }

        if (!login.skin().isValid()) {
            handle.close("", "disconnectionScreen.invalidSkin");
            return;
        }

        Skin skin = login.skin();
        p.setSkin(
                skin.isPersona()
                        && !server.getSettings().player().personaSkins()
                        ? Skin.NO_PERSONA_SKIN
                        : skin
        );

        PlayerPreLoginEvent event =
                new PlayerPreLoginEvent(p, "Plugin reason");

        if (!event.call()) {
            handle.close("", event.getKickMessage());
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
                                handle.setAwaitingEncryptionHandshake(true);
                                handle.getNetworkSession().setEncryption(
                                        getEncryptionKey(),
                                        getEncryptionCipher(),
                                        getDecryptionCipher()
                                );
                            });
                        }
                    }
            );
        } else {
            handle.processPreLogin();
        }
    }

    private record LoginData(String username, UUID clientUuid, long clientId, Skin skin) {
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
