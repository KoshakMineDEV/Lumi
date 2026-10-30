package cn.nukkit.network;

import io.netty.util.internal.PlatformDependent;
import cn.nukkit.utils.GameLoop;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/** Multi-producer, main-thread consumer queue. A task occupies at most one queue entry. */
public final class NetworkEventQueue {
    public enum Result { DONE, MORE, NEXT_TICK }

    public abstract static class Task {
        private final AtomicBoolean scheduled = new AtomicBoolean();

        public abstract Result process(long deadlineNanos);

        public abstract boolean hasWork();
    }

    private final Queue<Task> ready = PlatformDependent.newMpscQueue();
    private final ArrayDeque<Task> deferred = new ArrayDeque<>();
    private final GameLoop gameLoop;
    private int tick;

    public NetworkEventQueue(Thread owner) {
        this.gameLoop = GameLoop.builder().ownerThread(owner).hasPendingTasks(this::hasReadyTasks).build();
    }

    public NetworkEventQueue(GameLoop gameLoop) {
        this.gameLoop = java.util.Objects.requireNonNull(gameLoop);
    }

    public void signal(Task task) {
        if (task.scheduled.compareAndSet(false, true)) {
            ready.offer(task);
            wake();
        }
    }

    public void wake() {
        gameLoop.wakeUp();
    }

    /** Called once per simulation tick, never for a network wakeup. */
    public void beginTick() {
        tick++;
        Task task;
        while ((task = deferred.pollFirst()) != null) {
            ready.offer(task);
        }
    }

    public int tick() {
        return tick;
    }

    public boolean hasReadyTasks() {
        return !ready.isEmpty();
    }

    public void process(long deadlineNanos) {
        // Count and wall-clock bounds both matter for a continuously refilled queue.
        for (int i = 0; i < 1024 && System.nanoTime() < deadlineNanos; i++) {
            Task task = ready.poll();
            if (task == null) return;
            Result result = Result.DONE;
            try {
                result = task.process(deadlineNanos);
            } finally {
                switch (result) {
                    case MORE -> ready.offer(task);
                    case NEXT_TICK -> deferred.addLast(task);
                    case DONE -> {
                        task.scheduled.set(false);
                        // Publish idle before checking work: a racing producer either schedules
                        // the task itself, or its publication is picked up by this check.
                        if (task.hasWork()) signal(task);
                    }
                }
            }
        }
    }

    public void await(long deadlineNanos) {
        gameLoop.await(deadlineNanos);
    }
}
