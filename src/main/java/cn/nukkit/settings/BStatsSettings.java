package cn.nukkit.settings;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
public class BStatsSettings extends OkaeriConfig {
    @Comment("bStats (https://bStats.org) collects some basic information for plugin authors, like how")
    @Comment("many people use their plugin and their total player count. It's recommended to keep bStats")
    @Comment("enabled, but if you're not comfortable with this, you can turn this setting off. There is no")
    @Comment("performance penalty associated with having metrics enabled, and data sent to bStats is fully anonymous.")
    private boolean enable = true;

    @CustomKey("server-uuid")
    private String serverUUID = UUID.randomUUID().toString();

    @CustomKey("log-failed-requests")
    private boolean logFailedRequests = false;

    @CustomKey("log-sent-data")
    private boolean logSentData = false;

    @CustomKey("log-response-status-text")
    private boolean logResponseStatusText = false;
}
