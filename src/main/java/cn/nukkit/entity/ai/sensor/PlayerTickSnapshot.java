package cn.nukkit.entity.ai.sensor;

import cn.nukkit.Player;
import cn.nukkit.level.Level;

import java.util.Arrays;
import java.util.Map;

/**
 * Main-tick player membership snapshots shared by player sensors. Player
 * objects remain live; only the level membership iteration is cached.
 */
final class PlayerTickSnapshot {

    private static final Cache PRIMARY_CACHE = new Cache();
    private static final ThreadLocal<Cache> ASYNC_CACHE = ThreadLocal.withInitial(Cache::new);

    private PlayerTickSnapshot() {
    }

    static Player[] get(Level level) {
        var server = level.getServer();
        Cache cache = cacheFor(server.isPrimaryThread());
        return cache.get(level, server.getTick(), level.getPlayers());
    }

    static Cache cacheFor(boolean primaryThread) {
        return primaryThread ? PRIMARY_CACHE : ASYNC_CACHE.get();
    }

    static final class Cache {
        private static final int INITIAL_LEVEL_CAPACITY = 2;

        private int tick = Integer.MIN_VALUE;
        private Object[] levels = new Object[INITIAL_LEVEL_CAPACITY];
        private Player[][] snapshots = new Player[INITIAL_LEVEL_CAPACITY][];
        private int[] playerCounts = new int[INITIAL_LEVEL_CAPACITY];
        private int levelCount;

        Player[] get(Object level, int currentTick, Map<Long, Player> players) {
            if (tick != currentTick) {
                clear();
                tick = currentTick;
            }

            int currentPlayerCount = players.size();
            for (int i = 0; i < levelCount; i++) {
                if (levels[i] == level) {
                    if (playerCounts[i] != currentPlayerCount) {
                        snapshots[i] = snapshot(players);
                        playerCounts[i] = snapshots[i].length;
                    }
                    return snapshots[i];
                }
            }

            ensureCapacity(levelCount + 1);
            Player[] snapshot = snapshot(players);
            levels[levelCount] = level;
            snapshots[levelCount] = snapshot;
            playerCounts[levelCount] = snapshot.length;
            levelCount++;
            return snapshot;
        }

        private static Player[] snapshot(Map<Long, Player> players) {
            return players.values().toArray(Player.EMPTY_ARRAY);
        }

        private void clear() {
            Arrays.fill(levels, 0, levelCount, null);
            Arrays.fill(snapshots, 0, levelCount, null);
            levelCount = 0;
        }

        private void ensureCapacity(int requiredCapacity) {
            if (requiredCapacity <= levels.length) {
                return;
            }
            int newCapacity = Math.max(requiredCapacity, levels.length << 1);
            levels = Arrays.copyOf(levels, newCapacity);
            snapshots = Arrays.copyOf(snapshots, newCapacity);
            playerCounts = Arrays.copyOf(playerCounts, newCapacity);
        }
    }
}
