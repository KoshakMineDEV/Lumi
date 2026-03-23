package cn.nukkit.player;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.format.generic.serializer.NetworkChunkSerializer;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ChunkRadiusUpdatedPacket;
import cn.nukkit.network.protocol.LevelChunkPacket;
import cn.nukkit.network.protocol.NetworkChunkPublisherUpdatePacket;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.longs.*;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

import static com.google.common.base.Preconditions.checkArgument;

@Log4j2
public class PlayerChunkManager {

    private final Player player;
    private final LongSet viewChunks = new LongOpenHashSet();
    private final LongSet shellSentChunks = new LongOpenHashSet();
    private final LongSet readyChunks = new LongOpenHashSet();
    private final Long2IntMap pendingSubChunks = new Long2IntOpenHashMap();
    private final Long2ObjectMap<LevelChunkPacket> sendQueue = new Long2ObjectOpenHashMap<>();

    private final AtomicLong chunksSentCounter = new AtomicLong();
    private final LongConsumer removeChunkLoader;

    private volatile int radius;

    public PlayerChunkManager(Player player) {
        this.player = player;
        this.removeChunkLoader = chunkKey -> {
            final int chunkX = Level.getHashX(chunkKey);
            final int chunkZ = Level.getHashZ(chunkKey);

            BaseFullChunk chunk = this.player.getLevel().getChunkIfLoaded(chunkX, chunkZ);
            if (chunk != null) {
                this.player.level.unregisterChunkLoader(this.player, chunkX, chunkZ);
                for (Entity entity : chunk.getEntities().values()) {
                    entity.despawnFrom(this.player);
                }
            }
        };
    }

    public synchronized void sendQueued() {
        int chunksPerTick = this.player.getServer().getSettings().world().chunk().sendingPerTick();

        LongList keysToDiscard = new LongArrayList();
        for (Long2ObjectMap.Entry<LevelChunkPacket> entry : this.sendQueue.long2ObjectEntrySet()) {
            if (!this.viewChunks.contains(entry.getLongKey())) {
                keysToDiscard.add(entry.getLongKey());
            }
        }

        for (long key : keysToDiscard) {
            this.sendQueue.remove(key);
            this.removeChunkLoader.accept(key);
        }

        int centerX = this.player.getPosition().getFloorX() >> 4;
        int centerZ = this.player.getPosition().getFloorZ() >> 4;

        LongList list = new LongArrayList(this.sendQueue.keySet());

        list.unstableSort((a, b) -> {
            int ax = Level.getHashX(a) - centerX;
            int az = Level.getHashZ(a) - centerZ;
            int bx = Level.getHashX(b) - centerX;
            int bz = Level.getHashZ(b) - centerZ;
            return Integer.compare(ax * ax + az * az, bx * bx + bz * bz);
        });

        for (long key : list.toLongArray()) {
            if (chunksPerTick <= 0) {
                break;
            }

            LevelChunkPacket packet = this.sendQueue.get(key);
            if (packet == null) {
                continue;
            }

            this.sendQueue.remove(key);
            this.player.dataPacket(packet);

            int subChunkLimit = packet.subChunkLimit;
            this.shellSentChunks.add(key);

            if (subChunkLimit < 0) {
                this.readyChunks.add(key);

                BaseFullChunk chunk = this.player.getLevel().getChunkIfLoaded(Level.getHashX(key), Level.getHashZ(key));
                checkArgument(
                        chunk != null,
                        "Attempted to send unloaded chunk (%s, %s) to %s",
                        Level.getHashX(key),
                        Level.getHashZ(key),
                        this.player.getName()
                );

                for (Entity entity : chunk.getEntities().values()) {
                    if (entity != this.player && !entity.isClosed() && entity.isAlive()) {
                        entity.spawnTo(this.player);
                    }
                }
            } else {
                int pending = subChunkLimit + 1;
                this.pendingSubChunks.put(key, pending);
            }

            chunksPerTick--;
            this.chunksSentCounter.incrementAndGet();
        }
    }

    public synchronized void recordSubChunkServed(int chunkX, int chunkZ, int sectionsServed) {
        long key = Level.chunkHash(chunkX, chunkZ);

        if (!this.pendingSubChunks.containsKey(key)) {
            return;
        }

        int remaining = this.pendingSubChunks.get(key) - sectionsServed;
        if (remaining > 0) {
            this.pendingSubChunks.put(key, remaining);
            return;
        }

        this.pendingSubChunks.remove(key);
        this.readyChunks.add(key);

        BaseFullChunk chunk = this.player.getLevel().getChunkIfLoaded(Level.getHashX(key), Level.getHashZ(key));
        if (chunk == null) {
            return;
        }

        for (Entity entity : chunk.getEntities().values()) {
            if (entity != this.player && !entity.isClosed() && entity.isAlive()) {
                entity.spawnTo(this.player);
            }
        }
    }

    public void queueNewChunks() {
        this.queueNewChunks(this.player.getPosition());
    }

    public void queueNewChunks(Vector3 pos) {
        this.queueNewChunks(pos.getChunkX(), pos.getChunkZ());
    }

    private LevelChunkPacket createChunkPacket(BaseFullChunk chunk) {
        if(chunk == null) return null;

        LevelChunkPacket pk = new LevelChunkPacket();
        pk.chunkX = chunk.getX();
        pk.chunkZ = chunk.getZ();
        pk.dimension = player.getLevel().getDimension();

        NetworkChunkSerializer.serialize(new IntArraySet(List.of(player.protocol)), (BaseChunk) chunk, (callback) -> {
            pk.subChunkCount = callback.getSubchunks();
            pk.data = callback.getStream().getBuffer();
        }, player.getLevel().isAntiXrayEnabled(), player.getLevel().getDimensionData());

        return pk;
    }

    public synchronized void queueNewChunks(int chunkX, int chunkZ) {
        int radius = this.getChunkRadius();
        int radiusSqr = radius * radius;

        LongSet chunksForRadius = new LongOpenHashSet();
        LongSet previousView = new LongOpenHashSet(this.viewChunks);
        LongList chunksToLoad = new LongArrayList();

        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                if ((x * x) + (z * z) > radiusSqr) {
                    continue;
                }

                int cx = chunkX + x;
                int cz = chunkZ + z;
                long key = Level.chunkHash(cx, cz);

                chunksForRadius.add(key);
                if (this.viewChunks.add(key)) {
                    chunksToLoad.add(key);
                }
            }
        }

        boolean viewChanged = this.viewChunks.retainAll(chunksForRadius);

        this.shellSentChunks.retainAll(this.viewChunks);
        this.readyChunks.retainAll(this.viewChunks);
        this.pendingSubChunks.keySet().retainAll(this.viewChunks);

        if (viewChanged || !chunksToLoad.isEmpty()) {
            NetworkChunkPublisherUpdatePacket publisherPacket = new NetworkChunkPublisherUpdatePacket();
            publisherPacket.position = this.player.getPosition().asBlockVector3();
            publisherPacket.radius = this.radius;
            this.player.dataPacket(publisherPacket);
        }

        chunksToLoad.unstableSort((a, b) -> {
            int ax = Level.getHashX(a) - chunkX;
            int az = Level.getHashZ(a) - chunkZ;
            int bx = Level.getHashX(b) - chunkX;
            int bz = Level.getHashZ(b) - chunkZ;

            return Integer.compare(ax * ax + az * az, bx * bx + bz * bz);
        });

        for (long key : chunksToLoad.toLongArray()) {
            final int cx = Level.getHashX(key);
            final int cz = Level.getHashZ(key);

            if (this.sendQueue.putIfAbsent(key, null) == null) {
                this.player.getLevel().getChunkFuture(cx, cz, true)
                        .thenApplyAsync(chunk -> {
                            if(chunk == null) return null;

                            this.player.getLevel().registerChunkLoader(this.player, cx, cz);
                            return chunk;
                        })
                        .thenApplyAsync(this::createChunkPacket)
                        .whenCompleteAsync((packet, throwable) -> {
                            if(packet == null) {
                                viewChunks.remove(Level.chunkHash(cx, cz));
                                sendQueue.remove(key);
                                return;
                            }

                            synchronized (PlayerChunkManager.this) {
                                if (throwable != null) {
                                    if (this.sendQueue.remove(key, null)) {
                                        this.viewChunks.remove(key);
                                    }
                                    log.error(
                                            "Unable to create chunk packet for {}",
                                            this.player.getName(),
                                            throwable
                                    );
                                } else if (!this.sendQueue.replace(key, null, packet)) {
                                    if (this.sendQueue.containsKey(key)) {
                                        log.warn(
                                                "Chunk ({},{}) already queued for {}, dropping duplicate",
                                                cx, cz,
                                                this.player.getName()
                                        );
                                    }
                                }
                            }
                        });
            }
        }

        previousView.removeAll(chunksForRadius);
        previousView.forEach(this.removeChunkLoader);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        if (this.radius != radius) {
            this.radius = radius;
            ChunkRadiusUpdatedPacket packet = new ChunkRadiusUpdatedPacket();
            packet.radius = radius >> 4;
            this.player.dataPacket(packet);
            this.queueNewChunks();
        }
    }

    public int getChunkRadius() {
        return this.radius >> 4;
    }

    public void setChunkRadius(int chunkRadius) {
        chunkRadius = Math.clamp(
                chunkRadius,
                3,
                this.player.getServer().getSettings().world().viewDistance()
        );
        this.setRadius(chunkRadius << 4);
    }

    public boolean isChunkSent(int x, int z) {
        return this.isChunkSent(Level.chunkHash(x, z));
    }

    public synchronized boolean isChunkSent(long key) {
        return this.readyChunks.contains(key);
    }

    public boolean isChunkShellSent(int x, int z) {
        return this.isChunkShellSent(Level.chunkHash(x, z));
    }

    public synchronized boolean isChunkShellSent(long key) {
        return this.shellSentChunks.contains(key);
    }

    public boolean isChunkInView(int x, int z) {
        return this.isChunkInView(Level.chunkHash(x, z));
    }

    public synchronized boolean isChunkInView(long key) {
        return this.viewChunks.contains(key);
    }

    public long getChunksSent() {
        return this.chunksSentCounter.get();
    }

    public LongSet getViewChunks() {
        return LongSets.unmodifiable(this.viewChunks);
    }

    public LongSet getReadyChunks() {
        return LongSets.unmodifiable(this.readyChunks);
    }

    public synchronized void resendChunk(int chunkX, int chunkZ) {
        long key = Level.chunkHash(chunkX, chunkZ);
        this.viewChunks.remove(key);
        this.shellSentChunks.remove(key);
        this.readyChunks.remove(key);
        this.pendingSubChunks.remove(key);
        this.removeChunkLoader.accept(key);
    }

    public void prepareRegion(Vector3f pos) {
        this.prepareRegion(pos.getFloorX() >> 4, pos.getFloorZ() >> 4);
    }

    public void prepareRegion(int chunkX, int chunkZ) {
        this.clear();
        this.queueNewChunks(chunkX, chunkZ);
    }

    public synchronized void clear() {
        this.sendQueue.clear();
        this.viewChunks.forEach(this.removeChunkLoader);
        this.viewChunks.clear();
        this.shellSentChunks.clear();
        this.readyChunks.clear();
        this.pendingSubChunks.clear();
    }
}