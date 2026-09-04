package cn.nukkit.level.util;

import cn.nukkit.block.Block;

/**
 * Chunks implementing this interface should have a block cache that can be accessed concurrently,
 * and clear is typically called every tick.
 */
public interface TickCachedBlockStore {
    void clearCachedStore();

    void saveIntoCachedStore(Block block, int x, int y, int z, int layer);

    Block getFromCachedStore(int x, int y, int z, int layer);

    /**
     * Equivalent to computeIfAbsent.
     */
    Block computeFromCachedStore(int x, int y, int z, int layer, CachedBlockComputer cachedBlockComputer);

    interface CachedBlockComputer {
        Block compute();
    }
}