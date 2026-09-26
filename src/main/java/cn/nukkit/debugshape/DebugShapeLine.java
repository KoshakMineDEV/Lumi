package cn.nukkit.debugshape;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import lombok.experimental.SuperBuilder;

import java.awt.Color;

@SuperBuilder(toBuilder = true)
public class DebugShapeLine extends DebugShape {

    /**
     * The end position of the line.
     * <p>
     * Can be {@code null}, and in that case that the position will be set to (0, 0, 0) client-side.
     */
    protected Vector3f endPosition;

    /**
     * Gets the end position of the line.
     *
     * @return the end position of the line.
     */
    public Vector3f getEndPosition() {
        return endPosition != null ? endPosition : ZERO_VECTOR;
    }

    /**
     * Sets the end position of the line.
     *
     * @param endPosition the new end position of the line.
     */
    public void setEndPosition(Vector3f endPosition) {
        this.endPosition = endPosition;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.LINE;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return commonNetworkData()
                .lineEndPosition(endPosition)
                .build();
    }
}