package cn.nukkit.metrics;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import cn.nukkit.metrics.chart.JavaVersionRetriever;
import cn.nukkit.metrics.chart.PlayerPlatformRetriever;
import cn.nukkit.metrics.chart.PlayerVersionRetriever;
import cn.nukkit.settings.BStatsSettings;
import lombok.extern.slf4j.Slf4j;
import org.bstats.MetricsBase;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bstats.json.JsonObjectBuilder;

/**
 * This class is used to send metrics to bStats to the Nukkit resource:
 * https://bstats.org/plugin/server-implementation/Nukkit/10277
 */
@Slf4j
public class NukkitMetrics {

    public static void start() {
        Server server = Server.getInstance();

        BStatsSettings settings = server.getSettings().bStats();
        if (!settings.enable()) {
            return;
        }

        MetricsBase metrics = new MetricsBase(
                "server-implementation",
                settings.serverUUID(),
                10277, // https://bstats.org/plugin/server-implementation/Nukkit/10277
                true,
                NukkitMetrics::appendPlatformData,
                builder -> {},
                null,
                server::isRunning,
                log::error,
                log::info,
                settings.logFailedRequests(),
                settings.logSentData(),
                settings.logResponseStatusText(),
                true
        );

        metrics.addCustomChart(new SingleLineChart("players", server::getOnlinePlayersCount));
        metrics.addCustomChart(new SimplePie("codename", () -> "Lumi"));
        metrics.addCustomChart(new SimplePie("nukkit_version", () -> "Lumi " + Nukkit.API_VERSION));
        metrics.addCustomChart(new SimplePie("xbox_auth", () -> server.getSettings().network().xboxAuth() ? "Required" : "Not required"));

        metrics.addCustomChart(new AdvancedPie("player_platform", new PlayerPlatformRetriever()));
        metrics.addCustomChart(new AdvancedPie("player_game_version", new PlayerVersionRetriever()));

        metrics.addCustomChart(new DrilldownPie("java_version", new JavaVersionRetriever()));
    }

    private static void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }
}
