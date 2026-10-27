package cn.nukkit.level.redstone;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockRedstoneDiode;
import cn.nukkit.block.BlockRedstoneWire;
import cn.nukkit.block.properties.enums.RedstoneConnection;
import cn.nukkit.event.block.BlockRedstoneEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.utils.Hash;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Calculates a connected wire network before publishing its final signal levels.
 * Network snapshots, signal buckets and HORIZONTAL_FIRST_OUTWARD ordering follow
 * Alternate Current's design (Space Walker, licensed under MIT);
 */
public final class WireUpdateHandler {
    // These indices are deliberately independent of BlockFace's enum ordinals.
    private static final BlockFace[] DIRECTIONS = {
            BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP
    };
    private static final int[] FLOW = {-1, 0, 1, 1, 2, -1, 2, 1, 3, 0, -1, 0, 3, 3, 2, -1};
    private static final int[][] HORIZONTAL = {{0, 2, 1, 3}, {1, 3, 2, 0}, {2, 0, 3, 1}, {3, 1, 0, 2}};

    private final Level level;
    private final Long2ObjectOpenHashMap<Node> nodes = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectLinkedOpenHashMap<Change> pending = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectLinkedOpenHashMap<Change> roots = new Long2ObjectLinkedOpenHashMap<>();
    private final Buckets updates = new Buckets();
    private final ArrayDeque<Node> transmit = new ArrayDeque<>();
    private final ArrayDeque<Node>[] signals;
    private boolean updating;
    private boolean writing;
    private long writingPosition;
    private int writingState;
    private int epoch;

    @SuppressWarnings("unchecked")
    public WireUpdateHandler(Level level) {
        this.level = level;
        this.signals = new ArrayDeque[16];
        for (int i = 0; i < this.signals.length; i++) {
            this.signals[i] = new ArrayDeque<>();
        }
    }

    public void update(BlockRedstoneWire wire, boolean topologyChanged) {
        request(wire.getFloorX(), wire.getFloorY(), wire.getFloorZ(), topologyChanged, false);
        drain();
    }

    public void added(BlockRedstoneWire wire) {
        request(wire.getFloorX(), wire.getFloorY(), wire.getFloorZ(), true, true);
        drain();
    }

    public void removed(BlockRedstoneWire wire) {
        request(wire.getFloorX(), wire.getFloorY(), wire.getFloorZ(), true, true);
        drain();
    }

    // Called before Level.setBlock callbacks so reentrant changes cannot leave a stale snapshot
    public void onBlockChanged(Block previous, Block current) {
        if (!this.updating) {
            return;
        }
        long key = Hash.hashBlock(current.getFloorX(), current.getFloorY(), current.getFloorZ());
        if (this.writing && key == this.writingPosition && current.getFullId() == this.writingState) {
            return;
        }
        request(current.getFloorX(), current.getFloorY(), current.getFloorZ(), true,
                previous.getId() == BlockID.REDSTONE_WIRE || current.getId() == BlockID.REDSTONE_WIRE);
    }

    // Numeric Level setters also participate in snapshot invalidation during plugin callbacks
    public void onBlockChanged(int x, int y, int z) {
        if (this.updating) {
            Node cached = this.nodes.get(Hash.hashBlock(x, y, z));
            if (cached == null || !cached.valid || cached.block.getFullId() != this.level.getFullBlock(x, y, z)) {
                request(x, y, z, true, false);
            }
        }
    }

    // Cached blocks are read-only and only live for the current synchronous update
    public Block getBlock(int x, int y, int z) {
        return this.updating ? node(x, y, z).block : this.level.getBlock(x, y, z, 0, false);
    }

    private void request(int x, int y, int z, boolean topology, boolean force) {
        long key = Hash.hashBlock(x, y, z);
        Change change = this.pending.get(key);
        if (change == null) {
            this.pending.put(key, new Change(x, y, z, topology, force));
        } else {
            change.topology |= topology;
            change.force |= force;
        }
    }

    private Node node(int x, int y, int z) {
        long key = Hash.hashBlock(x, y, z);
        Node node = this.nodes.get(key);
        if (node == null) {
            node = new Node(x, y, z);
            this.nodes.put(key, node);
        }
        if (!node.valid) {
            node.block = this.level.getBlock(x, y, z, 0, false);
            node.wire = node.block.getId() == BlockID.REDSTONE_WIRE && node.block instanceof BlockRedstoneWire;
            node.normal = node.block.isNormalBlock();
            node.valid = true;
        }
        return node;
    }

    private Node neighbor(Node node, int direction) {
        BlockFace face = DIRECTIONS[direction];
        return node(node.x + face.getXOffset(), node.y + face.getYOffset(), node.z + face.getZOffset());
    }

    private void drain() {
        if (this.updating) {
            return;
        }
        this.updating = true;
        try {
            while (!this.pending.isEmpty() || !this.updates.isEmpty()) {
                if (!this.pending.isEmpty()) {
                    prepare();
                    continue;
                }
                Node next = this.updates.poll();
                if (next.powerUpdate) {
                    commit(next);
                } else {
                    Block current = this.level.getBlock(next.x, next.y, next.z, 0, false);
                    if (current.getId() != BlockID.AIR && current.getId() != BlockID.REDSTONE_WIRE) {
                        this.level.updateRedstoneBlock(current);
                    }
                    // Reentrant wire requests and Level.setBlock changes invalidate the snapshot in prepare().
                }
            }
        } finally {
            this.updating = false;
            this.writing = false;
            this.updates.clear();
            this.pending.clear();
            this.roots.clear();
            this.nodes.clear();
            this.transmit.clear();
            for (ArrayDeque<Node> queue : this.signals) {
                queue.clear();
            }
        }
    }

    private void prepare() {
        this.epoch++;
        // Outstanding component notifications remain queued when a callback changes a network.
        this.updates.removePowerUpdates();
        this.transmit.clear();
        for (Node node : this.nodes.values()) {
            node.valid = false;
        }
        for (Change change : this.pending.values()) {
            long key = Hash.hashBlock(change.x, change.y, change.z);
            Change existing = this.roots.get(key);
            if (existing == null) {
                this.roots.put(key, change);
            } else {
                existing.topology |= change.topology;
                existing.force |= change.force;
            }
        }
        this.pending.clear();

        List<Node> network = new ArrayList<>();
        ArrayDeque<Node> search = new ArrayDeque<>();
        boolean topology = false;
        for (Change root : this.roots.values()) {
            topology |= root.topology;
            Node origin = node(root.x, root.y, root.z);
            origin.force |= root.force;
            if (origin.wire) {
                if (origin.force || needsRefresh(origin, root.topology)) {
                    discover(origin, 0, true, network, search);
                }
                root.force = false;
                continue;
            }
            // Removal can split the network into disconnected parts, including slopes.
            for (int direction : HORIZONTAL[0]) {
                Node adjacent = neighbor(origin, direction);
                seed(adjacent, direction, root, network, search);
                seed(neighbor(adjacent, 4), direction, root, network, search);
                seed(neighbor(adjacent, 5), direction, root, network, search);
            }
            if (!origin.wire && origin.force) {
                findLinks(origin);
                origin.connectionFlow = connectionFlow(origin);
                origin.powerUpdate = true;
                origin.power = 0;
                origin.flow = origin.connectionFlow >= 0 ? origin.connectionFlow : 0;
                this.updates.offer(origin, 0);
            }
            root.force = false;
        }
        while (!search.isEmpty()) {
            Node wire = search.removeFirst();
            if (!this.pending.isEmpty()) {
                return;
            }
            for (int direction : HORIZONTAL[wire.flow]) {
                Node peer = wire.links[direction];
                if (peer != null && peer.epoch != this.epoch && needsRefresh(peer, topology)) {
                    discover(peer, direction, false, network, search);
                }
            }
        }

        for (Node wire : network) {
            wire.seedPower = findPower(wire, true);
            wire.power = wire.seedPower;
            if (wire.power > 0) {
                this.signals[wire.power].addLast(wire);
            }
        }
        for (int power = 15; power > 0; power--) {
            ArrayDeque<Node> queue = this.signals[power];
            while (!queue.isEmpty()) {
                Node wire = queue.removeFirst();
                if (wire.power != power) {
                    continue;
                }
                for (int direction : HORIZONTAL[wire.flow]) {
                    Node peer = wire.links[direction];
                    if (peer != null && peer.epoch == this.epoch && !peer.pinned && peer.power < power - 1) {
                        peer.power = power - 1;
                        this.signals[peer.power].addLast(peer);
                    }
                }
            }
        }
        for (Node wire : network) {
            int incoming = 0;
            for (int direction = 0; direction < 4; direction++) {
                Node peer = wire.links[direction];
                if (wire.power > 0 && peer != null && (peer.knownEpoch == this.epoch
                        ? peer.power : ((BlockRedstoneWire) peer.block).getSignal()) - 1 == wire.power) {
                    incoming |= 1 << ((direction + 2) & 3);
                }
            }
            wire.incoming = incoming;
            wire.flow = FLOW[incoming] >= 0 ? FLOW[incoming]
                    : wire.connectionFlow >= 0 ? wire.connectionFlow : wire.flow;
            wire.meta = topology && !wire.pinned ? connectionMeta(wire) : wire.block.getDamage();
            wire.meta = (wire.meta & ~15) | wire.power;
            wire.activated = false;
            wire.committed = false;
        }
        for (Node wire : network) {
            if (wire.root || wire.seedPower > 0 || wire.force
                    || (wire.meta & ~15) != (wire.block.getDamage() & ~15)) {
                activate(wire);
            }
        }
        transmitUnchanged();
    }

    private void discover(Node wire, int flow, boolean root, List<Node> network, ArrayDeque<Node> search) {
        if (wire.wire && wire.epoch != this.epoch) {
            initialize(wire);
            wire.power = findPower(wire, false);
            wire.epoch = this.epoch;
            wire.flow = root && wire.connectionFlow >= 0 ? wire.connectionFlow : flow;
            wire.root = root;
            network.add(wire);
            search.addLast(wire);
        }
    }

    private void findLinks(Node wire) {
        Arrays.fill(wire.links, null);
        boolean openAbove = !neighbor(wire, 5).normal;
        for (int direction = 0; direction < 4; direction++) {
            Node adjacent = neighbor(wire, direction);
            Node connected = adjacent.wire ? adjacent
                    : adjacent.normal && openAbove ? neighbor(adjacent, 5)
                    : !adjacent.normal ? neighbor(adjacent, 4) : null;
            if (connected != null && connected.wire) wire.links[direction] = connected;
        }
    }

    private static int connectionFlow(Node wire) {
        int mask = 0;
        for (int direction = 0; direction < 4; direction++) if (wire.links[direction] != null) mask |= 1 << direction;
        return FLOW[mask];
    }

    private void activate(Node wire) {
        if (wire.activated) return;
        wire.activated = true;
        if (wire.meta != wire.block.getDamage() || wire.force) {
            wire.powerUpdate = true;
            this.updates.offer(wire, wire.power);
        } else {
            this.transmit.addLast(wire);
        }
    }

    private void transmitUnchanged() {
        while (!this.transmit.isEmpty()) transmit(this.transmit.removeFirst());
    }

    private void transmit(Node wire) {
        for (int direction : HORIZONTAL[wire.flow]) {
            Node peer = wire.links[direction];
            if (peer == null || peer.epoch != this.epoch
                    || (wire.power == 0 ? peer.power != 0 : peer.power != wire.power - 1)) continue;
            if (wire.power == 0 && !peer.committed) {
                peer.incoming |= 1 << direction;
                if (FLOW[peer.incoming] >= 0) peer.flow = FLOW[peer.incoming];
            }
            activate(peer);
        }
    }

    private void authorize(Node wire) {
        if (wire.eventEpoch != this.epoch) {
            wire.eventEpoch = this.epoch;
            wire.pinned = !new RedstoneUpdateEvent(wire.block).call();
        }
    }

    private void initialize(Node wire) {
        if (wire.knownEpoch != this.epoch) {
            wire.knownEpoch = this.epoch;
            wire.current = ((BlockRedstoneWire) wire.block).getSignal();
            wire.power = wire.current;
            wire.external = -1;
            authorize(wire);
            if (!((BlockRedstoneWire) wire.block).canBePlacedOn(neighbor(wire, 4).block)) {
                this.level.useBreakOn(wire.block, null, null, true);
                wire.pinned = true;
            }
            findLinks(wire);
            wire.connectionFlow = connectionFlow(wire);
        }
    }

    private int findPower(Node wire, boolean ignoreSearched) {
        if (wire.pinned) return wire.current;
        if (wire.external < 0) wire.external = externalPower(wire);
        int power = wire.external;
        for (Node peer : wire.links) {
            if (peer != null && (!ignoreSearched || peer.epoch != this.epoch)) {
                int peerPower = peer.knownEpoch == this.epoch ? peer.power : ((BlockRedstoneWire) peer.block).getSignal();
                power = Math.max(power, peerPower - 1);
            }
        }
        return power;
    }

    private void seed(Node wire, int flow, Change root, List<Node> network, ArrayDeque<Node> search) {
        if (wire.wire && (root.topology || root.force || needsRefresh(wire, false))) {
            discover(wire, flow, !root.force, network, search);
        }
    }

    // An unchanged input must not cause a traversal of an otherwise stable network
    private boolean needsRefresh(Node wire, boolean topology) {
        initialize(wire);
        wire.power = findPower(wire, false);
        return wire.power != wire.current || topology && connectionMeta(wire) != wire.block.getDamage();
    }

    private int externalPower(Node wire) {
        int power = 0;
        for (int direction = 0; direction < DIRECTIONS.length; direction++) {
            Node adjacent = neighbor(wire, direction);
            if (adjacent.wire) {
                continue;
            }
            if (adjacent.normal) {
                for (int side = 0; side < DIRECTIONS.length; side++) {
                    Node source = neighbor(adjacent, side);
                    if (!source.wire) {
                        power = Math.max(power, source.block.getStrongPower(DIRECTIONS[side]));
                    }
                }
            }
            power = Math.max(power, adjacent.block.getWeakPower(DIRECTIONS[direction]));
            if (power >= 15) {
                return 15;
            }
        }
        return Math.max(0, power);
    }

    private int connectionMeta(Node wire) {
        boolean openAbove = !neighbor(wire, 5).normal;
        return BlockRedstoneWire.connectionMeta(wire.block.getDamage(),
                connection(wire, 1, openAbove), connection(wire, 3, openAbove),
                connection(wire, 0, openAbove), connection(wire, 2, openAbove));
    }

    private RedstoneConnection connection(Node wire, int direction, boolean openAbove) {
        Node adjacent = neighbor(wire, direction);
        if (openAbove && adjacent.normal && neighbor(adjacent, 5).wire) {
            return RedstoneConnection.UP;
        }
        if (connects(adjacent.block, DIRECTIONS[direction]) || !adjacent.normal && neighbor(adjacent, 4).wire) {
            return RedstoneConnection.SIDE;
        }
        return RedstoneConnection.NONE;
    }

    private static boolean connects(Block block, BlockFace side) {
        if (block.getId() == BlockID.REDSTONE_WIRE) {
            return true;
        }
        if (block instanceof BlockRedstoneDiode diode) {
            return diode.getFacing() == side || diode.getFacing().getOpposite() == side;
        }
        return block.isPowerSource();
    }

    private void commit(Node node) {
        node.committed = true;
        transmit(node);
        transmitUnchanged();
        boolean notify = node.force;
        boolean shapeUpdate = node.force;
        node.force = false;
        Block current = this.level.getBlock(node.x, node.y, node.z, 0, false);
        if (current.getId() == BlockID.REDSTONE_WIRE && current instanceof BlockRedstoneWire wire) {
            int previous = wire.getSignal();
            if (previous != node.power) {
                new BlockRedstoneEvent(wire, previous, node.power).call();
                if (!this.pending.isEmpty()) {
                    return;
                }
            }
            if (wire.getDamage() != node.meta) {
                wire.setDamage(node.meta);
                this.writing = true;
                this.writingPosition = Hash.hashBlock(node.x, node.y, node.z);
                this.writingState = wire.getFullId();
                try {
                    this.level.setBlock(wire, wire, previous == node.power, false);
                } finally {
                    this.writing = false;
                }
                node.block = wire;
                node.valid = true;
                notify |= previous != node.power;
                shapeUpdate = true;
            }
        }
        if (notify) {
            notifyNeighbors(node);
        }
        if (shapeUpdate) {
            // As in Alternate Current, shape notifications have their own fixed order.
            for (int direction : HORIZONTAL[0]) {
                updateShapeNeighbor(node, direction);
            }
            updateShapeNeighbor(node, 4);
            updateShapeNeighbor(node, 5);
        }
    }

    private void updateShapeNeighbor(Node source, int direction) {
        Node neighbor = neighbor(source, direction);
        if (!neighbor.wire && neighbor.block.getId() != BlockID.AIR) {
            Block current = this.level.getBlock(neighbor.x, neighbor.y, neighbor.z, 0, false);
            current.onNeighborChange(DIRECTIONS[direction].getOpposite());
        }
    }

    // Alternate Current's HORIZONTAL_FIRST_OUTWARD: direct, diagonal, then distance-two neighbors
    private void notifyNeighbors(Node source) {
        int f = source.flow, r = (f + 1) & 3, b = (f + 2) & 3, l = (f + 3) & 3;
        Node front = neighbor(source, f), back = neighbor(source, b);
        Node right = neighbor(source, r), left = neighbor(source, l);
        Node down = neighbor(source, 4), up = neighbor(source, 5);
        notify(front, source); notify(back, source); notify(right, source); notify(left, source);
        notify(down, source); notify(up, source);
        notify(neighbor(front, r), source); notify(neighbor(back, l), source);
        notify(neighbor(front, l), source); notify(neighbor(back, r), source);
        notify(neighbor(front, 4), source); notify(neighbor(back, 5), source);
        notify(neighbor(front, 5), source); notify(neighbor(back, 4), source);
        notify(neighbor(right, 4), source); notify(neighbor(left, 5), source);
        notify(neighbor(right, 5), source); notify(neighbor(left, 4), source);
        notify(neighbor(front, f), source); notify(neighbor(back, b), source);
        notify(neighbor(right, r), source); notify(neighbor(left, l), source);
        notify(neighbor(down, 4), source); notify(neighbor(up, 5), source);
    }

    private void notify(Node node, Node source) {
        if (!node.wire && node.block.getId() != BlockID.AIR) {
            node.powerUpdate = false;
            this.updates.offer(node, source.priority);
        }
    }

    /** 
     * A pending world change in the Alternate Current-style network update. 
    */
    private static final class Change {
        final int x, y, z;
        boolean topology, force;

        Change(int x, int y, int z, boolean topology, boolean force) {
            this.x = x; this.y = y; this.z = z; this.topology = topology; this.force = force;
        }
    }

    /** 
     * A short-lived world snapshot and queue node, following Alternate Current's node model.
    */
    private static final class Node {
        final int x, y, z;
        final Node[] links = new Node[4];
        Block block;
        boolean valid, wire, normal, pinned, force, powerUpdate, queued, root, activated, committed;
        int epoch, knownEpoch, eventEpoch, current, power, external, seedPower, meta, incoming, flow, connectionFlow, priority;
        Node previous, next;

        Node(int x, int y, int z) {
            this.x = x; this.y = y; this.z = z;
        }
    }

    /** 
     * Alternate Current-style intrusive FIFO buckets avoid a queue entry allocation per notification. 
    */
    private static final class Buckets {
        private final Node[] heads = new Node[16], tails = new Node[16];
        private int occupied;

        boolean isEmpty() {
            return this.occupied == 0;
        }

        void offer(Node node, int priority) {
            if (node.queued) {
                if (node.priority == priority) return;
                remove(node);
            }
            node.priority = priority;
            node.previous = this.tails[priority];
            node.next = null;
            if (node.previous == null) this.heads[priority] = node;
            else node.previous.next = node;
            this.tails[priority] = node;
            this.occupied |= 1 << priority;
            node.queued = true;
        }

        Node poll() {
            if (isEmpty()) return null;
            int priority = 31 - Integer.numberOfLeadingZeros(this.occupied);
            Node node = this.heads[priority];
            remove(node);
            return node;
        }

        void remove(Node node) {
            if (node.previous == null) this.heads[node.priority] = node.next;
            else node.previous.next = node.next;
            if (node.next == null) this.tails[node.priority] = node.previous;
            else node.next.previous = node.previous;
            if (this.heads[node.priority] == null) this.occupied &= ~(1 << node.priority);
            node.previous = node.next = null;
            node.queued = false;
        }

        void removePowerUpdates() {
            for (Node head : this.heads) {
                for (Node node = head; node != null;) {
                    Node next = node.next;
                    if (node.powerUpdate) remove(node);
                    node = next;
                }
            }
        }

        void clear() {
            while (!isEmpty()) poll();
        }
    }
}
