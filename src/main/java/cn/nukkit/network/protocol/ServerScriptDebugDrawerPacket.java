package cn.nukkit.network.protocol;

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
        long shapeCount = getUnsignedVarInt();
        if (protocol >= ProtocolInfo.v1_21_120) {
            for (int i = 0; i < shapeCount; i++) {
                    ScriptDebugShape shape = new ScriptDebugShape(
                    getUnsignedVarLong(), getOptional(null, BinaryStream::getScriptDebugShapeType),
                    getOptional(null, BinaryStream::getVector3f), getOptional(null, BinaryStream::getLFloat),
                    getOptional(null, BinaryStream::getVector3f),  getOptional(null, BinaryStream::getLFloat),
                    getOptional(null, BinaryStream::getColor), null /* 1.26.0+ */, 0 /* 1.21.120+ */, getOptional(null, BinaryStream::getString),
                    getOptional(null, BinaryStream::getVector3f), getOptional(null, BinaryStream::getVector3f),
                    getOptional(null, BinaryStream::getLFloat), getOptional(null, BinaryStream::getLFloat),
                    getOptional(null, BinaryStream::getByte)
                );

                shapes.add(shape);
            }
        } else {
            for (int i = 0; i < shapeCount; i++) {
                shapes.add(readShapeNew());
            }
        }
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

    private ScriptDebugShape readShapeNew() {
        long id = getUnsignedVarLong();

        ScriptDebugShapeType type = null;
        Integer typeOrdinal = getOptional(null, BinaryStream::getByte);
        if (typeOrdinal != null && typeOrdinal >= 0) {
            ScriptDebugShapeType[] values = ScriptDebugShapeType.values();
            if (typeOrdinal < values.length) {
                type = values[typeOrdinal];
            }
        }

        Vector3f position = getOptional(null, BinaryStream::getVector3f);
        Float scale = getOptional(null, BinaryStream::getLFloat);
        Vector3f rotation = getOptional(null, BinaryStream::getVector3f);
        Float totalTimeLeft = getOptional(null, BinaryStream::getLFloat);

        Color color = null;
        Integer argb = getOptional(null, BinaryStream::getLInt);
        if (argb != null) {
            int alpha = (argb >> 24) & 0xFF;
            int red = (argb >> 16) & 0xFF;
            int green = (argb >> 8) & 0xFF;
            int blue = argb & 0xFF;
            color = new Color(red, green, blue, alpha);
        }

        int dimensionId = this.getVarInt();

        long payloadType = getUnsignedVarInt();

        if (type != null && payloadType != type.getPayloadType() && payloadType != ScriptDebugShapeType.PAYLOAD_TYPE_NONE) {
            throw new IllegalStateException("Unexpected payload type " + payloadType +
                    " for provided shape type " + type.name());
        }
        if (type == null && payloadType != ScriptDebugShapeType.PAYLOAD_TYPE_NONE) {
            throw new IllegalStateException("Unexpected payload type " + payloadType + " when shape type is not set");
        }

        String text = null;
        Vector3f boxBounds = null;
        Vector3f lineEndPosition = null;
        Float arrowHeadLength = null;
        Float arrowHeadRadius = null;
        Integer segments = null;

        switch ((int) payloadType) {
            case ScriptDebugShapeType.PAYLOAD_TYPE_NONE:
                break;

            case ScriptDebugShapeType.PAYLOAD_TYPE_ARROW:
                lineEndPosition = getOptional(null, BinaryStream::getVector3f);
                arrowHeadLength = getOptional(null, BinaryStream::getLFloat);
                arrowHeadRadius = getOptional(null, BinaryStream::getLFloat);
                segments = getOptional(null, BinaryStream::getByte);
                break;

            case ScriptDebugShapeType.PAYLOAD_TYPE_TEXT:
                text = this.getString();
                break;

            case ScriptDebugShapeType.PAYLOAD_TYPE_BOX:
                boxBounds = this.getVector3f();
                break;

            case ScriptDebugShapeType.PAYLOAD_TYPE_LINE:
                lineEndPosition = this.getVector3f();
                break;

            case ScriptDebugShapeType.PAYLOAD_TYPE_CIRCLE_OR_SPHERE:
                segments = this.getByte();
                break;

            default:
                throw new IllegalStateException("Unexpected shape payload type " + payloadType);
        }

        return new ScriptDebugShape(
                id, type, position, scale, rotation, totalTimeLeft,
                color, null, dimensionId, text, boxBounds, lineEndPosition,
                arrowHeadLength, arrowHeadRadius, segments
        );
    }
}