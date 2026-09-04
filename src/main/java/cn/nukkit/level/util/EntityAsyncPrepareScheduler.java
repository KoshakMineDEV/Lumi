package cn.nukkit.level.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Main-thread-owned scheduler for read-only entity async preparations.
 *
 * <p>Only the latest generation for an entity is retained. This is important
 * for route searches: a mob changing its target must replace the obsolete
 * request instead of appending another task behind it.</p>
 */
public final class EntityAsyncPrepareScheduler {

    private final Long2ObjectOpenHashMap<PendingPreparation> pendingPreparations = new Long2ObjectOpenHashMap<>();

    public void schedule(long entityId, long generation, @NotNull Runnable preparation) {
        this.pendingPreparations.put(entityId,
                new PendingPreparation(generation, Objects.requireNonNull(preparation, "preparation")));
    }

    public boolean cancel(long entityId, long generation) {
        PendingPreparation pending = this.pendingPreparations.get(entityId);
        if (pending == null || pending.generation != generation) {
            return false;
        }

        this.pendingPreparations.remove(entityId);
        return true;
    }

    public int getPendingCount() {
        return this.pendingPreparations.size();
    }

    /**
     * Executes every currently pending preparation and leaves newly scheduled
     * preparations for the next invocation.
     *
     * @return number of preparations in the executed batch
     */
    public int runAll(@NotNull ForkJoinPool pool, @NotNull Consumer<Throwable> errorHandler) {
        Objects.requireNonNull(pool, "pool");
        Objects.requireNonNull(errorHandler, "errorHandler");

        if (this.pendingPreparations.isEmpty()) {
            return 0;
        }

        PendingPreparation[] batch = this.pendingPreparations.values().toArray(PendingPreparation[]::new);
        this.pendingPreparations.clear();

        int workerCount = Math.min(Math.max(1, pool.getParallelism()), batch.length);
        if (workerCount == 1) {
            for (PendingPreparation pending : batch) {
                runSafely(pending.preparation, errorHandler);
            }
            return batch.length;
        }

        AtomicInteger cursor = new AtomicInteger();
        List<ForkJoinTask<?>> workers = new ArrayList<>(workerCount);
        for (int worker = 0; worker < workerCount; worker++) {
            workers.add(pool.submit(() -> {
                int index;
                while ((index = cursor.getAndIncrement()) < batch.length) {
                    runSafely(batch[index].preparation, errorHandler);
                }
            }));
        }

        for (ForkJoinTask<?> worker : workers) {
            worker.join();
        }
        return batch.length;
    }

    private static void runSafely(Runnable preparation, Consumer<Throwable> errorHandler) {
        try {
            preparation.run();
        } catch (Throwable throwable) {
            try {
                errorHandler.accept(throwable);
            } catch (Throwable ignored) {
                // A broken error handler must not terminate a worker lane and
                // starve the rest of the batch.
            }
        }
    }

    private record PendingPreparation(long generation, Runnable preparation) {
    }
}
