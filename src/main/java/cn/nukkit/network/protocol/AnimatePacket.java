package cn.nukkit.network.protocol;

import cn.nukkit.network.protocol.types.SwingSource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.ToString;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Nukkit Project Team
 */
@ToString
public class AnimatePacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.ANIMATE_PACKET;

    public long eid;
    public Action action;
    public float data = 0.0f;
    /**
     * @deprecated since 898
     */
    @Deprecated
    public float rowingTime = 0.0f;
    /**
     * @since 898
     */
    public SwingSource swingSource = SwingSource.NONE;

    @Override
    public void decode() {
        if (protocol >= ProtocolInfo.v1_21_130) {
            this.action = Action.fromId(this.getByte() & 0xFF);
        } else {
            this.action = Action.fromId(this.getVarInt());
        }

        this.eid = getEntityRuntimeId();

        if (protocol >= ProtocolInfo.v1_21_120) {
            this.data = this.getLFloat();
        }

        if (protocol < ProtocolInfo.v1_21_124 &&
                (this.action == Action.ROW_RIGHT || this.action == Action.ROW_LEFT)) {
            this.rowingTime = this.getLFloat();
        }

        if (protocol >= ProtocolInfo.v1_21_130) {
            this.swingSource = this.getOptional(SwingSource.NONE, (stream) -> SwingSource.from(stream.getString()));
        }
    }

    @Override
    public void encode() {
        this.reset();

        if (protocol >= ProtocolInfo.v1_21_130) {
            this.putByte((byte) this.action.getId());
        } else {
            this.putVarInt(this.action.getId());
        }

        this.putEntityRuntimeId(this.eid);

        if (protocol >= ProtocolInfo.v1_21_120) {
            this.putLFloat(this.data);
        }

        if (protocol < ProtocolInfo.v1_21_124 &&
                (this.action == Action.ROW_RIGHT || this.action == Action.ROW_LEFT)) {
            this.putLFloat(this.rowingTime);
        }

        if (protocol >= ProtocolInfo.v1_21_130) {
            this.putOptional((swing) -> swing != null && swing != SwingSource.NONE, this.swingSource, (stream, source) -> stream.putString(source.getName()));
        }
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    public enum Action {
        NO_ACTION(0),
        SWING_ARM(1),
        WAKE_UP(3),
        CRITICAL_HIT(4),
        MAGIC_CRITICAL_HIT(5),
        ROW_RIGHT(128),
        ROW_LEFT(129);

        private static final Int2ObjectMap<Action> ID_LOOKUP = new Int2ObjectOpenHashMap<>();

        static {
            for (Action value : values()) {
                ID_LOOKUP.put(value.id, value);
            }
        }

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Action fromId(int id) {
            return ID_LOOKUP.get(id);
        }
    }
}