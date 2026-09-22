package cn.nukkit.network.protocol;

import cn.nukkit.debugshape.DebugShapeText;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import cn.nukkit.utils.BinaryStream;
import lombok.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerScriptDebugDrawerPacket extends DataPacket {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_SCRIPT_DEBUG_DRAWER_PACKET;
    public List<ScriptDebugShape> shapes = new ArrayList<>();

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public byte pid() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void decode() {
        // client never sends this btw
        this.decodeUnsupported();
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(shapes.size());

        if (protocol >= ProtocolInfo.v1_21_120) {
            for (ScriptDebugShape shape : shapes) {
                writeShapeNew(shape);
            }
        } else {
            for (ScriptDebugShape shape : shapes) {
                putUnsignedVarLong(shape.getId());
                this.putOptionalNull(shape.getType(), this::writeScriptDebugShapeType);
                this.putOptionalNull(shape.getPosition(), this::putVector3f);
                this.putOptionalNull(shape.getScale(), this::putLFloat);
                this.putOptionalNull(shape.getRotation(), this::putVector3f);
                this.putOptionalNull(shape.getTotalTimeLeft(), this::putLFloat);
                this.putOptionalNull(shape.getColor(), this::putColor);
                this.putOptionalNull(shape.getText(), this::putString);
                this.putOptionalNull(shape.getBoxBounds(), this::putVector3f);
                this.putOptionalNull(shape.getLineEndPosition(), this::putVector3f);
                this.putOptionalNull(shape.getArrowHeadLength(), this::putLFloat);
                this.putOptionalNull(shape.getArrowHeadRadius(), this::putLFloat);
                this.putOptionalNull(shape.getSegments(), (buffer, segments) -> buffer.putByte(segments.byteValue()));
            }
        }
    }

    private void writeShapeNew(ScriptDebugShape shape) {
        this.putUnsignedVarLong(shape.getId());

        this.putOptionalNull(shape.getType(), (buffer, type) -> {
            buffer.putByte((byte) type.ordinal());
        });

        this.putOptionalNull(shape.getPosition(), this::putVector3f);

        this.putOptionalNull(shape.getScale(), this::putLFloat);

        this.putOptionalNull(shape.getRotation(), this::putVector3f);

        this.putOptionalNull(shape.getTotalTimeLeft(), this::putLFloat);

        if (this.protocol >= ProtocolInfo.v1_26_20_26) {
            this.putOptionalNull(shape.getMaximumRenderDistance(), this::putLFloat);
        }

        this.putOptionalNull(shape.getColor(), (buffer, color) -> {
            int argb = (color.getAlpha() << 24) |
                    (color.getRed() << 16) |
                    (color.getGreen() << 8) |
                    color.getBlue();
            buffer.putLInt(argb);
        });

        if (this.protocol >= ProtocolInfo.v1_26_0) {
            // v924: dimension is optional
            this.putBoolean(true);
            this.putVarInt(shape.getDimensionId());
        } else {
            this.putVarInt(shape.getDimensionId());
        }
        if (this.protocol >= ProtocolInfo.v1_26_0) {
            this.putOptionalNull(shape.getAttachedToEntityId(), val -> this.putUnsignedVarLong(val));
        }

        writeShapePayload(shape);
    }

    private void writeShapePayload(ScriptDebugShape shape) {
        ScriptDebugShapeType type = shape.getType();

        if (type == null) {
            this.putUnsignedVarInt(ScriptDebugShapeType.PAYLOAD_TYPE_NONE);
            return;
        }

        int payloadType = type.getPayloadType();

        boolean hasPayloadData = switch (type) {
            case ARROW -> shape.getLineEndPosition() != null ||
                    shape.getArrowHeadLength() != null ||
                    shape.getArrowHeadRadius() != null ||
                    shape.getSegments() != null;
            case TEXT -> shape.getText() != null;
            case BOX -> shape.getBoxBounds() != null;
            case LINE -> shape.getLineEndPosition() != null;
            case CIRCLE, SPHERE -> shape.getSegments() != null;
        };

        if (!hasPayloadData) {
            this.putUnsignedVarInt(ScriptDebugShapeType.PAYLOAD_TYPE_NONE);
        } else {
            this.putUnsignedVarInt(payloadType);

            switch (type) {
                case ARROW:
                    this.putOptionalNull(shape.getLineEndPosition(), this::putVector3f);
                    this.putOptionalNull(shape.getArrowHeadLength(), this::putLFloat);
                    this.putOptionalNull(shape.getArrowHeadRadius(), this::putLFloat);
                    this.putOptionalNull(shape.getSegments(), (buffer, seg) -> buffer.putByte(seg.byteValue()));
                    break;

                case TEXT:
                    if (shape.getText() != null) {
                        this.putString(shape.getText());
                    }
                    if (this.protocol >= ProtocolInfo.v1_26_20_26) {
                        this.putBoolean(shape.getUseRotation() != null ? shape.getUseRotation() : false);
                        this.putOptionalNull(shape.getBackgroundColor(), (buffer, color) -> buffer.putLInt(color.getRGB()));
                        if (this.protocol >= ProtocolInfo.v1_26_50) {
                            this.putLFloat(shape.getLineGapHeight() != null ? shape.getLineGapHeight() : 0f);
                        }
                        this.putBoolean(shape.getDepthTest() != null ? shape.getDepthTest() : false);
                        this.putBoolean(shape.getShowBackface() != null ? shape.getShowBackface() : true);
                        this.putBoolean(shape.getShowTextBackface() != null ? shape.getShowTextBackface() : true);
                    }
                    break;

                case BOX:
                    if (shape.getBoxBounds() != null) {
                        this.putVector3f(shape.getBoxBounds());
                    }
                    break;

                case LINE:
                    if (shape.getLineEndPosition() != null) {
                        this.putVector3f(shape.getLineEndPosition());
                    }
                    break;

                case CIRCLE:
                case SPHERE:
                    if (shape.getSegments() != null) {
                        this.putByte(shape.getSegments().byteValue());
                    }
                    break;

                default:
                    this.putUnsignedVarInt(ScriptDebugShapeType.PAYLOAD_TYPE_NONE);
                    break;
            }
        }
    }
}