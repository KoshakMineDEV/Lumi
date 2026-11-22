package cn.nukkit.network;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.server.BatchPacketsEvent;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.compression.SnappyCompression;
import cn.nukkit.utils.compression.Zlib;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主要处理服务器广播数据包
 * 因为每个玩家的协议版本不同，所以在这个线程提前根据玩家协议进行编码
 */
public class BatchingHelper {

    private final ExecutorService threadedExecutor;

    public BatchingHelper() {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("Batching Executor");
        this.threadedExecutor = Executors.newSingleThreadExecutor(builder.build());
    }

    public void batchPackets(Player[] players, DataPacket[] packets) {
        if (players == null || packets == null || players.length == 0 || packets.length == 0) {
            return;
        }

        BatchPacketsEvent ev = new BatchPacketsEvent(players, packets);
        if (!ev.call()) {
            return;
        }

        this.threadedExecutor.execute(() -> this.batchAndSendPackets(players, packets));
    }

    private void batchAndSendPackets(Player[] players, DataPacket[] packets) {
        //只有一个玩家时直接发送
        //未知原因 注释掉会导致客户端容易闪退
        if (players.length == 1) {
            for (DataPacket packet : packets) {
                packet.protocol = players[0].protocol;
                players[0].getNetworkSession().sendPacket(packet);
           }
           return;
        }

        Int2ObjectMap<ObjectList<Player>> targets = new Int2ObjectOpenHashMap<>();
        for (Player player : players) {
            targets.computeIfAbsent(player.protocol, i -> new ObjectArrayList<>()).add(player);
        }

        int compressionLevel = Server.getInstance().getSettings().network().compression().compressionLevel();
        boolean useSnappy = Server.getInstance().getSettings().network().compression().useSnappyCompression();
        for (Int2ObjectMap.Entry<ObjectList<Player>> target : targets.int2ObjectEntrySet()) {
            int protocolId = target.getIntKey();
            ObjectList<Player> finalTargets = target.getValue();
            BinaryStream batched = new BinaryStream();
            for (DataPacket packet : packets) {
                if (packet instanceof BatchPacket) {
                    throw new RuntimeException("Cannot batch BatchPacket");
                }
                DataPacket encodedPacket = packet.clone();
                encodedPacket.protocol = protocolId;
                encodedPacket.tryEncode();
                encodedPacket.writeLengthPrefixedTo(batched);
            }

            try {
                byte[] bytes = batched.getBuffer();
                BatchPacket pk = new BatchPacket();
                if (useSnappy) {
                    pk.payload = SnappyCompression.compress(bytes);
                } else {
                    pk.payload = Zlib.deflateRaw(bytes, compressionLevel);
                }
                for (Player player : finalTargets) {
                    CompressionProvider compressionProvider = player.getNetworkSession().getCompression();
                    if (compressionProvider == CompressionProvider.NONE) {
                        BatchPacket batchPacket = new BatchPacket();
                        batchPacket.payload = bytes;
                        player.dataPacket(batchPacket);
                    }else {
                        player.dataPacket(pk);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void shutdown() {
        this.threadedExecutor.shutdownNow();
    }
}
