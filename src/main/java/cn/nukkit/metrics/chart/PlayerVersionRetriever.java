package cn.nukkit.metrics.chart;

import cn.nukkit.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This class is used to collect information for the player version chart.
 */
public class PlayerVersionRetriever implements Callable<Map<String, Integer>> {

    @Override
    public Map<String, Integer> call() throws Exception {
        Map<String, Integer> valueMap = new HashMap<>();

        Server.getInstance().getOnlinePlayers().forEach((uuid, player) -> {
            String gameVersion = player.getLoginChainData().getGameVersion();
            if (!valueMap.containsKey(gameVersion)) {
                valueMap.put(gameVersion, 1);
            } else {
                valueMap.put(gameVersion, valueMap.get(gameVersion) + 1);
            }
        });

        return valueMap;
    }
}
