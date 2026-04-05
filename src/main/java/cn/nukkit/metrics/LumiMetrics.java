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
 * This class is used to send metrics to bStats to the Lumi personal resource:
 * https://bstats.org/plugin/server-implementation/Lumi/30587
 */
@Slf4j
public class LumiMetrics {

    public static void start() {
        Server server = Server.getInstance();

        BStatsSettings settings = server.getSettings().bStats();
        if (!settings.enable()) {
            return;
        }

        MetricsBase metrics = new MetricsBase(
                "server-implementation",
                settings.serverUUID(),
                30587, // https://bstats.org/plugin/server-implementation/Lumi/30587
                true,
                LumiMetrics::appendPlatformData,
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

        metrics.addCustomChart(new SimplePie("lumi_api_version", () -> Nukkit.API_VERSION));
        metrics.addCustomChart(new SimplePie("lumi_server_version", () -> Nukkit.VERSION));
        metrics.addCustomChart(new SimplePie("xbox_auth", () -> server.getSettings().network().xboxAuth() ? "Required" : "Not required"));

        metrics.addCustomChart(new SingleLineChart("players", server::getOnlinePlayersCount));
        metrics.addCustomChart(new AdvancedPie("player_game_platform", new PlayerPlatformRetriever()));
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
