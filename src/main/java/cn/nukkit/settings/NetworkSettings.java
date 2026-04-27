package cn.nukkit.settings;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Base64;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
public class NetworkSettings extends OkaeriConfig {

    @CustomKey("xbox-auth")
    @Comment("Enable authorization by Xbox (for Waterdog this parameter should be disabled)")
    private boolean xboxAuth = true;

    @CustomKey("xbox-auth-temp-ip-ban-on-failed")
    @Comment("Will the player be temporarily blocked when the Xbox authorization fails")
    private boolean xboxAuthTempIpBan = false;

    @CustomKey("strong-ip-bans")
    private boolean strongIpBans = false;

    @CustomKey("enable-rak-send-cookie")
    private boolean enableRakSendCookie = true;

    @CustomKey("rak-packet-limit")
    private int rakPacketLimit = 120;

    @CustomKey("timeout-milliseconds")
    private int timeoutMilliseconds = 25000;

    @CustomKey("enable-query")
    @Comment("Enable query sending")
    private boolean enableQuery = true;

    @CustomKey("query-plugins")
    @Comment("Enable display of plugins list in query")
    private boolean queryPlugins = false;

    @Comment("Connection encryption (recommended if proxy is not used)")
    private boolean encryption;

    @Setter(value = AccessLevel.NONE)
    @Comment("RCON settings")
    private RconSettings rcon = new RconSettings();

    @Setter(value = AccessLevel.NONE)
    @Comment("Compression settings")
    private CompressionSettings compression = new CompressionSettings();

    @Getter
    @Setter
    public static class RconSettings extends OkaeriConfig {

        private boolean enable = false;

        private String password = Base64.getEncoder()
                .encodeToString(UUID.randomUUID().toString().replace("-", "").getBytes())
                .substring(3, 13);

        private int port = 19132;
    }

    @Getter
    @Setter
    public static class CompressionSettings extends OkaeriConfig {

        @CustomKey("zlib-provider")
        private int zlibProvider = 3;

        @CustomKey("compression-level")
        private int compressionLevel = 5;

        @CustomKey("compression-threshold")
        private int compressionThreshold = 256;

        @CustomKey("use-snappy-compression")
        private boolean useSnappyCompression = false;
    }
}
