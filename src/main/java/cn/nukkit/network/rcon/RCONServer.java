package cn.nukkit.network.rcon;

import cn.nukkit.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Modern, thread-safe, robust NIO implementation of Source RCON Server.
 * Refactored for issue #283 (fixes NPE, stream fragmentation, multi-packet responses, and concurrency).
 *
 * @author Tee7even, Refactored for Lumi
 */
public class RCONServer extends Thread {

    public static final int SERVERDATA_AUTH = 3;
    public static final int SERVERDATA_AUTH_RESPONSE = 2;
    public static final int SERVERDATA_EXECCOMMAND = 2;
    public static final int SERVERDATA_RESPONSE_VALUE = 0;

    private static final int MAX_PACKET_PAYLOAD = 4000;

    private volatile boolean running;

    private final ServerSocketChannel serverChannel;
    private final Selector selector;

    private final String password;
    private final Set<SocketChannel> rconSessions = ConcurrentHashMap.newKeySet();

    private final Queue<RCONCommand> receiveQueue = new ConcurrentLinkedQueue<>();
    private final Map<SocketChannel, Queue<RCONPacket>> sendQueues = new ConcurrentHashMap<>();
    private final Map<SocketChannel, ByteBuffer> clientBuffers = new ConcurrentHashMap<>();

    public RCONServer(String address, int port, String password) throws IOException {
        this.setName("RCON-Thread");
        this.running = true;

        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.socket().bind(new InetSocketAddress(address, port));

        this.selector = Selector.open();
        this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        this.password = password != null ? password : "";
    }

    public RCONCommand receive() {
        return this.receiveQueue.poll();
    }

    public void respond(SocketChannel channel, int id, String response) {
        if (channel == null || !channel.isOpen()) return;

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_PACKET_PAYLOAD) {
            this.send(channel, new RCONPacket(id, SERVERDATA_RESPONSE_VALUE, bytes));
        } else {
            // Valve RCON Multi-packet response fragmentation
            int offset = 0;
            while (offset < bytes.length) {
                int length = Math.min(bytes.length - offset, MAX_PACKET_PAYLOAD);
                byte[] chunk = new byte[length];
                System.arraycopy(bytes, offset, chunk, 0, length);
                this.send(channel, new RCONPacket(id, SERVERDATA_RESPONSE_VALUE, chunk));
                offset += length;
            }
        }
    }

    public void send(SocketChannel channel, RCONPacket packet) {
        if (channel == null || !channel.isOpen()) return;

        Queue<RCONPacket> queue = this.sendQueues.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<>());
        queue.add(packet);

        SelectionKey key = channel.keyFor(this.selector);
        if (key != null && key.isValid()) {
            try {
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            } catch (Exception ignored) {}
        }

        this.selector.wakeup();
    }

    public void close() {
        this.running = false;
        this.selector.wakeup();
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                // Update interest ops for pending writes
                for (Map.Entry<SocketChannel, Queue<RCONPacket>> entry : this.sendQueues.entrySet()) {
                    SocketChannel channel = entry.getKey();
                    Queue<RCONPacket> queue = entry.getValue();
                    if (channel != null && channel.isOpen() && queue != null && !queue.isEmpty()) {
                        SelectionKey key = channel.keyFor(this.selector);
                        if (key != null && key.isValid()) {
                            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                    }
                }

                int readyChannels = this.selector.select(500);
                if (readyChannels == 0) continue;

                Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) {
                            this.accept(key);
                        }
                        if (key.isValid() && key.isReadable()) {
                            this.read(key);
                        }
                        if (key.isValid() && key.isWritable()) {
                            this.write(key);
                        }
                    } catch (Exception e) {
                        SocketChannel ch = key.channel() instanceof SocketChannel ? (SocketChannel) key.channel() : null;
                        this.closeChannel(ch);
                    }
                }
            } catch (Exception exception) {
                if (this.running) {
                    Server.getInstance().getLogger().logException(exception);
                }
            }
        }

        // Clean up all resources on thread termination
        this.cleanup();
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            socketChannel.register(this.selector, SelectionKey.OP_READ);
            ByteBuffer buf = ByteBuffer.allocate(8192);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            this.clientBuffers.put(socketChannel, buf);
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = this.clientBuffers.computeIfAbsent(channel, k -> {
            ByteBuffer b = ByteBuffer.allocate(8192);
            b.order(ByteOrder.LITTLE_ENDIAN);
            return b;
        });

        int bytesRead;
        try {
            bytesRead = channel.read(buffer);
        } catch (IOException e) {
            this.closeChannel(channel);
            return;
        }

        if (bytesRead == -1) {
            this.closeChannel(channel);
            return;
        }

        buffer.flip();
        while (buffer.remaining() >= 4) {
            buffer.mark();
            int packetLength = buffer.getInt();
            if (packetLength < 10 || packetLength > 14576) {
                // Malformed / invalid length - close connection
                this.closeChannel(channel);
                return;
            }

            if (buffer.remaining() < packetLength) {
                buffer.reset();
                break;
            }

            byte[] packetBytes = new byte[packetLength];
            buffer.get(packetBytes);

            ByteBuffer packetBuffer = ByteBuffer.wrap(packetBytes);
            packetBuffer.order(ByteOrder.LITTLE_ENDIAN);

            int id = packetBuffer.getInt();
            int type = packetBuffer.getInt();
            byte[] payload = new byte[packetLength - 10]; // 4 bytes id + 4 bytes type + 2 null terminator bytes
            packetBuffer.get(payload);

            this.handle(channel, new RCONPacket(id, type, payload));
        }

        buffer.compact();
    }

    private void handle(SocketChannel channel, RCONPacket packet) {
        switch (packet.getType()) {
            case SERVERDATA_AUTH:
                byte[] payload = new byte[0];
                String inputPass = new String(packet.getPayload(), StandardCharsets.UTF_8).trim();

                if (inputPass.equals(this.password)) {
                    this.rconSessions.add(channel);
                    this.send(channel, new RCONPacket(packet.getId(), SERVERDATA_AUTH_RESPONSE, payload));
                    try {
                        Server.getInstance().getLogger().info("[RCON] " + channel.getRemoteAddress().toString() + " connected");
                    } catch (Exception ignored) {}
                    return;
                }

                try {
                    Server.getInstance().getLogger().info("[RCON] Authentication failed for " + channel.getRemoteAddress().toString());
                } catch (Exception ignored) {}
                this.send(channel, new RCONPacket(-1, SERVERDATA_AUTH_RESPONSE, payload));
                break;

            case SERVERDATA_EXECCOMMAND:
                if (!this.rconSessions.contains(channel)) {
                    return;
                }

                String command = new String(packet.getPayload(), StandardCharsets.UTF_8).trim();
                this.receiveQueue.add(new RCONCommand(channel, packet.getId(), command));
                break;
        }
    }

    private void write(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        Queue<RCONPacket> queue = this.sendQueues.get(channel);

        if (queue == null || queue.isEmpty()) {
            if (key.isValid()) {
                key.interestOps(SelectionKey.OP_READ);
            }
            return;
        }

        while (!queue.isEmpty()) {
            RCONPacket packet = queue.peek();
            if (packet == null) {
                queue.poll();
                continue;
            }

            ByteBuffer buffer = packet.toBuffer();
            try {
                channel.write(buffer);
                if (buffer.hasRemaining()) {
                    // Buffer not fully written - wait for next OP_WRITE
                    break;
                }
                queue.poll();
            } catch (IOException e) {
                this.closeChannel(channel);
                return;
            }
        }

        if (queue.isEmpty()) {
            this.sendQueues.remove(channel);
            if (key.isValid()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void closeChannel(SocketChannel channel) {
        if (channel == null) return;

        this.rconSessions.remove(channel);
        this.sendQueues.remove(channel);
        this.clientBuffers.remove(channel);

        try {
            SelectionKey key = channel.keyFor(this.selector);
            if (key != null) {
                key.cancel();
            }
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (Exception ignored) {}
    }

    private void cleanup() {
        for (SocketChannel ch : new HashSet<>(this.rconSessions)) {
            this.closeChannel(ch);
        }
        for (SocketChannel ch : new HashSet<>(this.clientBuffers.keySet())) {
            this.closeChannel(ch);
        }

        try {
            if (this.serverChannel != null && this.serverChannel.isOpen()) {
                SelectionKey key = this.serverChannel.keyFor(this.selector);
                if (key != null) key.cancel();
                this.serverChannel.close();
            }
            if (this.selector != null && this.selector.isOpen()) {
                this.selector.close();
            }
        } catch (IOException ignored) {}
    }
}