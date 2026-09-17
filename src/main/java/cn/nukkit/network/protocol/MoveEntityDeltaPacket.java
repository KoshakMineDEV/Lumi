package cn.nukkit.network.protocol;

import lombok.ToString;

@ToString
public class MoveEntityDeltaPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.MOVE_ENTITY_DELTA_PACKET;

    public static final int FLAG_HAS_X = 0b1;
    public static final int FLAG_HAS_Y = 0b10;
    public static final int FLAG_HAS_Z = 0b100;
    public static final int FLAG_HAS_PITCH = 0B1000;
    public static final int FLAG_HAS_YAW = 0B10000;
    public static final int FLAG_HAS_HEAD_YAW = 0B100000;
    public static final int FLAG_ON_GROUND = 0B1000000;
    public static final int FLAG_TELEPORTING = 0B10000000;
    public static final int FLAG_FORCE_MOVE_LOCAL_ENTITY = 0B100000000;
    public static final int FLAG_FORCE_COMPLETION = 0B1000000000;

    public long eid;
    public int flags = 0;
    public float x = 0;
    public float y = 0;
    public float z = 0;
    public double yawDelta = 0;
    public double headYawDelta = 0;
    public double pitchDelta = 0;
    public boolean onGround;
    public boolean forceMove;
    public boolean forceMoveLocalEntity;
    public boolean forceCompletion;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        if (protocol >= ProtocolInfo.v1_26_40) {
            this.eid = this.getEntityRuntimeId();
            this.flags = 0;
            if (this.getBoolean()) { this.flags |= FLAG_HAS_X; this.x = this.getLFloat(); }
            if (this.getBoolean()) { this.flags |= FLAG_HAS_Y; this.y = this.getLFloat(); }
            if (this.getBoolean()) { this.flags |= FLAG_HAS_Z; this.z = this.getLFloat(); }
            if (this.getBoolean()) { this.flags |= FLAG_HAS_PITCH; this.pitchDelta = this.getByte() * 1.40625; }
            if (this.getBoolean()) { this.flags |= FLAG_HAS_YAW; this.yawDelta = this.getByte() * 1.40625; }
            if (this.getBoolean()) { this.flags |= FLAG_HAS_HEAD_YAW; this.headYawDelta = this.getByte() * 1.40625; }
            this.onGround = this.getBoolean();
            if (this.onGround) this.flags |= FLAG_ON_GROUND;
            this.forceMove = this.getBoolean();
            this.forceMoveLocalEntity = this.getBoolean();
            if (this.forceMoveLocalEntity) this.flags |= FLAG_FORCE_MOVE_LOCAL_ENTITY;
            this.forceCompletion = this.getBoolean();
            if (this.forceCompletion) this.flags |= FLAG_FORCE_COMPLETION;
            if (protocol >= ProtocolInfo.v1_26_50) {
                // trailing ticks added in v2192
                this.getUnsignedVarLong();
            }
        } else {
            this.eid = this.getEntityRuntimeId();
            this.flags = this.getLShort();
            this.x = getCoordinate(FLAG_HAS_X);
            this.y = getCoordinate(FLAG_HAS_Y);
            this.z = getCoordinate(FLAG_HAS_Z);
            this.pitchDelta = getRotation(FLAG_HAS_PITCH);
            this.yawDelta = getRotation(FLAG_HAS_YAW);
            this.headYawDelta = getRotation(FLAG_HAS_HEAD_YAW);
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        if (protocol >= ProtocolInfo.v1_26_40) {
            putOptionalCoordinate(FLAG_HAS_X, this.x);
            putOptionalCoordinate(FLAG_HAS_Y, this.y);
            putOptionalCoordinate(FLAG_HAS_Z, this.z);
            putOptionalRotation(FLAG_HAS_PITCH, this.pitchDelta);
            putOptionalRotation(FLAG_HAS_YAW, this.yawDelta);
            putOptionalRotation(FLAG_HAS_HEAD_YAW, this.headYawDelta);
            this.putBoolean((this.flags & FLAG_ON_GROUND) != 0 || this.onGround);
            this.putBoolean(this.forceMove);
            this.putBoolean((this.flags & FLAG_FORCE_MOVE_LOCAL_ENTITY) != 0 || this.forceMoveLocalEntity);
            this.putBoolean((this.flags & FLAG_FORCE_COMPLETION) != 0 || this.forceCompletion);
            if (protocol >= ProtocolInfo.v1_26_50) {
                // trailing ticks added in v2192; always 0 while client-side prediction is unused
                this.putUnsignedVarLong(0);
            }
        } else {
            this.putLShort(this.flags);
            putCoordinate(FLAG_HAS_X, this.x);
            putCoordinate(FLAG_HAS_Y, this.y);
            putCoordinate(FLAG_HAS_Z, this.z);
            putRotation(FLAG_HAS_PITCH, this.pitchDelta);
            putRotation(FLAG_HAS_YAW, this.yawDelta);
            putRotation(FLAG_HAS_HEAD_YAW, this.headYawDelta);
        }
    }

    private void putOptionalCoordinate(int flag, float value) {
        boolean present = (this.flags & flag) != 0;
        this.putBoolean(present);
        if (present) this.putLFloat(value);
    }

    private void putOptionalRotation(int flag, double value) {
        boolean present = (this.flags & flag) != 0;
        this.putBoolean(present);
        if (present) this.putByte((byte) (value / 1.40625));
    }
    private float getCoordinate(int flag) {
        if ((flags & flag) != 0) {
            return this.getLFloat();
        }
        return 0;
    }

    private double getRotation(int flag) {
        if ((flags & flag) != 0) {
            return this.getByte() * 1.40625;
        }
        return 0d;
    }

    private void putCoordinate(int flag, float value) {
        if ((flags & flag) != 0) {
            this.putLFloat(value);
        }
    }

    private void putRotation(int flag, double value) {
        if ((flags & flag) != 0) {
            this.putByte((byte) (value / 1.40625));
        }
    }
}
