package cn.nukkit.metrics.chart;

import cn.nukkit.Server;
import cn.nukkit.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This class is used to collect information for the player platform chart.
 */
public class PlayerPlatformRetriever implements Callable<Map<String, Integer>> {

    @Override
    public Map<String, Integer> call() throws Exception {
        Map<String, Integer> valueMap = new HashMap<>();

        Server.getInstance().getOnlinePlayers().forEach((uuid, player) -> {
            String deviceOS = Utils.getOS(player);
            if (!valueMap.containsKey(deviceOS)) {
                valueMap.put(deviceOS, 1);
            } else {
                valueMap.put(deviceOS, valueMap.get(deviceOS) + 1);
            }
        });

        return valueMap;
    }
}
