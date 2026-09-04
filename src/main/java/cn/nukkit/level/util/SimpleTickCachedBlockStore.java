package cn.nukkit.level.util;

import cn.nukkit.block.Block;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.Level;
import cn.nukkit.utils.collection.nb.Int2ObjectNonBlockingMap;

import java.util.concurrent.atomic.AtomicLong;

public final class SimpleTickCachedBlockStore implements TickCachedBlockStore {
    private static final int INITIAL_CACHE_SIZE = 256;

    private final Int2ObjectNonBlockingMap<Block> tickCachedBlockStore;
    private final DimensionData dimensionData;
    private final AtomicLong lastQueuedTick = new AtomicLong(Long.MIN_VALUE);

    public SimpleTickCachedBlockStore(Level level) {
        this(level.getDimensionData());
    }

    SimpleTickCachedBlockStore(DimensionData dimensionData) {
        this.tickCachedBlockStore = new Int2ObjectNonBlockingMap<>(INITIAL_CACHE_SIZE);
        this.dimensionData = dimensionData;
    }

    @Override
    public void clearCachedStore() {
        // The computation barrier has completed before this is called, so the
        // non-atomic clear is safe and retains the already allocated hash arrays.
        tickCachedBlockStore.clear(true);
    }

    /**
     * Marks this store as used during a level tick.
     *
     * @return {@code true} exactly once for each tick, even with concurrent callers
     */
    public boolean markUsed(long tick) {
        long observed = lastQueuedTick.get();
        while (observed != tick) {
            if (lastQueuedTick.compareAndSet(observed, tick)) {
                return true;
            }
            observed = lastQueuedTick.get();
        }
        return false;
    }

    @Override
    public void saveIntoCachedStore(Block block, int x, int y, int z, int layer) {
        int hash = Level.localBlockHash(x, y, z, layer, dimensionData);
        tickCachedBlockStore.put(hash, block);
    }

    @Override
    public Block getFromCachedStore(int x, int y, int z, int layer) {
        return tickCachedBlockStore.get(Level.localBlockHash(x, y, z, layer, dimensionData));
    }

    @Override
    public Block computeFromCachedStore(int x, int y, int z, int layer,
                                        CachedBlockComputer computer) {
        int hash = Level.localBlockHash(x, y, z, layer, dimensionData);
        return tickCachedBlockStore.computeIfAbsent(hash, k -> computer.compute());
    }
}
