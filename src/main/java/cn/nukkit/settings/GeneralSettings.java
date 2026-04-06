package cn.nukkit.settings;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class GeneralSettings extends OkaeriConfig {

    @Comment("Server MOTD, displayed in the server list")
    private String motd = "An Lumi server";

    @CustomKey("sub-motd")
    @Comment("Server sub-MOTD, usually displayed in the LAN interface or on monitorings")
    private String subMotd = "Powered by Lumi";

    @CustomKey("server-port")
    private int serverPort = 19132;

    @CustomKey("server-ip")
    private String serverIp = "0.0.0.0";

    @CustomKey("max-players")
    private int maxPlayers = 50;

    @Comment("Server language by default")
    private String language = "eng";

    @CustomKey("force-language")
    private boolean forceLanguage = false;

    @CustomKey("force-resources")
    @Comment("Force the player to accept server resources")
    private boolean forceResources = false;

    @CustomKey("force-resources-allow-client-packs")
    @Comment("Force the player to use own resource packs")
    private boolean forceResourcesAllowClientPacks = true;

    @CustomKey("shutdown-message")
    private String shutdownMessage = "Server closed";

    @CustomKey("debug-level")
    @Comment("Logger debug level, can be from 1 to 3")
    private int debugLevel = 1;

    @CustomKey("enable-spark")
    @Comment("Enable automatic spark installer")
    private boolean enableSpark = false;

    @CustomKey("use-waterdog")
    @Comment("Enable WaterdogPE support, requires xbox-auth: false to work properly")
    private boolean useWaterdog = false;

    @CustomKey("server-authoritative-movement")
    @Comment("Whether to enable server authoritative movement")
    @Comment("Allowed values: \"client-auth\", \"server-auth\", \"server-auth-with-rewind\"")
    private ServerAuthoritativeMovement serverAuthoritativeMovement = ServerAuthoritativeMovement.SERVER_AUTH;

    @CustomKey("server-authoritative-block-breaking")
    @Comment("Whether to enable server authoritative block breaking")
    private boolean serverAuthoritativeBlockBreaking = true;

    @Setter(value = AccessLevel.NONE)
    @Comment("Multiversion settings")
    private MultiversionSettings multiversion = new MultiversionSettings();

    public enum ServerAuthoritativeMovement {
        CLIENT_AUTH,
        SERVER_AUTH,
        SERVER_AUTH_WITH_REWIND
    }

    @Getter
    @Setter
    public static class MultiversionSettings extends OkaeriConfig {

        @CustomKey("min-protocol")
        private int minProtocol = 0;

        @CustomKey("max-protocol")
        private int maxProtocol = -1;
    }
}
