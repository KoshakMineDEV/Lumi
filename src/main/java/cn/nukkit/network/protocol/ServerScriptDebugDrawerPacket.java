package cn.nukkit.network.protocol;

import cn.nukkit.debugshape.DebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import cn.nukkit.utils.BinaryStream;
import lombok.*;

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
        for (int i = 0; i < shapeCount; i++) {
            ScriptDebugShape shape = new ScriptDebugShape(
                    getUnsignedVarLong(), getOptional(null, BinaryStream::getScriptDebugShapeType),
                    getOptional(null, BinaryStream::getVector3f), getOptional(null, BinaryStream::getLFloat),
                    getOptional(null, BinaryStream::getVector3f),  getOptional(null, BinaryStream::getLFloat),
                    getOptional(null, BinaryStream::getColor), getOptional(null, BinaryStream::getInt),
                    getOptional(null, BinaryStream::getString), getOptional(null, BinaryStream::getVector3f),
                    getOptional(null, BinaryStream::getVector3f), getOptional(null, BinaryStream::getLFloat),
                    getOptional(null, BinaryStream::getLFloat), getOptional(null, BinaryStream::getByte)
            );

            shapes.add(shape);
        }
    }

    @Override
    public void encode() {
        this.reset();
        putUnsignedVarInt(shapes.size());
        
        for (ScriptDebugShape shape : shapes) {
            this.putUnsignedVarLong(shape.getId());
            this.putOptionalNull(shape.getType(), this::writeScriptDebugShapeType);
            this.putOptionalNull(shape.getPosition(), this::putVector3f);
            this.putOptionalNull(shape.getScale(), this::putLFloat);
            this.putOptionalNull(shape.getRotation(), this::putVector3f);
            this.putOptionalNull(shape.getTotalTimeLeft(), this::putLFloat);
            this.putOptionalNull(shape.getColor(), this::putColor);
            this.putVarInt(shape.getDimensionID());
            this.putOptionalNull(shape.getText(), this::putString);
            this.putOptionalNull(shape.getBoxBounds(), this::putVector3f);
            this.putOptionalNull(shape.getLineEndPosition(), this::putVector3f);
            this.putOptionalNull(shape.getArrowHeadLength(), this::putLFloat);
            this.putOptionalNull(shape.getArrowHeadRadius(), this::putLFloat);
            this.putOptionalNull(shape.getSegments(), (buffer, segments) -> buffer.putByte(segments.byteValue()));
        }
    }


}
