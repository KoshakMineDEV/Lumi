package cn.nukkit.utils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Fixed-rate ticks and event-driven idle work on one existing owner thread.
 * Starting a loop never creates a thread. Wakeups do not advance simulation time.
 */
public final class GameLoop {
    private final Thread ownerThread;
    private final long nanosPerTick;
    private final BooleanSupplier running;
    private final BooleanSupplier hasPendingTasks;
    private final Runnable onStart;
    private final Runnable onTick;
    private final LongConsumer onIdle;
    private final LongConsumer afterTick;
    private final LongConsumer onDeadline;
    private final Runnable onStop;
    private final Consumer<RuntimeException> onException;
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean wakeRequested = new AtomicBoolean();
    private final AtomicBoolean sleeping = new AtomicBoolean();
    private volatile boolean stopped;
    private volatile boolean active;
    private volatile long nextTickNanos;
    private volatile long tick;

    private GameLoop(Builder builder) {
        ownerThread = builder.ownerThread;
        nanosPerTick = 1_000_000_000L / builder.loopCountPerSec;
        running = builder.running;
        hasPendingTasks = builder.hasPendingTasks;
        onStart = builder.onStart;
        onTick = builder.onTick;
        onIdle = builder.onIdle;
        afterTick = builder.afterTick;
        onDeadline = builder.onDeadline;
        onStop = builder.onStop;
        onException = builder.onException;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Runs synchronously on the configured owner. A loop instance can be started once.
     */
    public void startLoop() {
        checkOwnerThread();
        if (!started.compareAndSet(false, true)) throw new IllegalStateException("GameLoop already started");
        active = true;
        try {
            setNextTick(System.nanoTime());
            onStart.run();
            while (canRun()) {
                try {
                    long now = System.nanoTime();
                    if (now - nextTickNanos >= 0) {
                        try {
                            onTick.run();
                        } finally {
                            tick++;
                            long deadline = nextTickNanos + nanosPerTick;
                            now = System.nanoTime();
                            // Preserve Lumi's existing five-tick catch-up limit.
                            if (deadline - now < -5 * nanosPerTick) deadline = now;
                            setNextTick(deadline);
                        }
                        if (canRun() && nextTickNanos - System.nanoTime() > 0) afterTick.accept(nextTickNanos);
                    } else {
                        wakeRequested.set(false);
                        onIdle.accept(nextTickNanos);
                        await(nextTickNanos);
                    }
                } catch (RuntimeException e) {
                    onException.accept(e);
                }
            }
        } finally {
            active = false;
            sleeping.set(false);
            onStop.run();
        }
    }

    /** Coalesces notifications and unparks only when the owner is preparing to sleep. */
    public void wakeUp() {
        if (wakeRequested.compareAndSet(false, true) && sleeping.compareAndSet(true, false)) {
            LockSupport.unpark(ownerThread);
        }
    }

    /** Allows existing consumers of the network queue to use the same wait primitive. */
    public void await(long deadlineNanos) {
        checkOwnerThread();
        sleeping.set(true);
        try {
            // Work is published before wakeUp(). Check it after publishing sleeping, then
            // consume the notification. A later arrival leaves an unpark permit for park.
            if (canRun() && !hasPendingTasks.getAsBoolean() && !wakeRequested.getAndSet(false)) {
                long remaining = deadlineNanos - System.nanoTime();
                if (remaining > 0) LockSupport.parkNanos(this, remaining);
            }
        } finally {
            sleeping.set(false);
        }
    }

    public void stop() {
        stopped = true;
        wakeUp();
    }

    public boolean isRunning() {
        return active && canRun();
    }

    public Thread getOwnerThread() {
        return ownerThread;
    }

    public long getNextTickNanos() {
        return nextTickNanos;
    }

    public long getTick() {
        return tick;
    }

    private boolean canRun() {
        return !stopped && !ownerThread.isInterrupted() && running.getAsBoolean();
    }

    private void checkOwnerThread() {
        if (Thread.currentThread() != ownerThread) throw new IllegalStateException("GameLoop must run on its owner thread");
    }

    private void setNextTick(long deadline) {
        nextTickNanos = deadline;
        onDeadline.accept(deadline);
    }

    public static final class Builder {
        private Thread ownerThread = Thread.currentThread();
        private int loopCountPerSec = 20;
        private BooleanSupplier running = () -> true;
        private BooleanSupplier hasPendingTasks = () -> false;
        private Runnable onStart = () -> {};
        private Runnable onTick = () -> {};
        private LongConsumer onIdle = deadline -> {};
        private LongConsumer afterTick = deadline -> {};
        private LongConsumer onDeadline = deadline -> {};
        private Runnable onStop = () -> {};
        private Consumer<RuntimeException> onException = e -> { throw e; };

        public Builder ownerThread(Thread thread) {
            ownerThread = Objects.requireNonNull(thread);
            return this;
        }

        public Builder loopCountPerSec(int ticksPerSecond) {
            if (ticksPerSecond < 1 || ticksPerSecond > 1000) throw new IllegalArgumentException("Tick rate must be between 1 and 1000");
            loopCountPerSec = ticksPerSecond;
            return this;
        }

        public Builder running(BooleanSupplier condition) {
            running = condition;
            return this;
        }

        public Builder hasPendingTasks(BooleanSupplier condition) {
            hasPendingTasks = condition;
            return this;
        }

        public Builder onStart(Runnable onStart) {
            this.onStart = onStart;
            return this;
        }

        public Builder onTick(Runnable onTick) {
            this.onTick = onTick;
            return this;
        }

        public Builder onIdle(LongConsumer onIdle) {
            this.onIdle = onIdle;
            return this;
        }

        public Builder afterTick(LongConsumer afterTick) {
            this.afterTick = afterTick;
            return this;
        }

        public Builder onDeadline(LongConsumer callback) {
            onDeadline = callback;
            return this;
        }

        public Builder onStop(Runnable onStop) {
            this.onStop = onStop;
            return this;
        }

        public Builder onException(Consumer<RuntimeException> callback) {
            onException = callback;
            return this;
        }

        public GameLoop build() {
            return new GameLoop(this);
        }
    }
}
