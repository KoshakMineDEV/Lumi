package cn.nukkit.debugshape;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;

import java.awt.Color;

public class DebugShapeLine extends DebugShape {

    /**
     * The end position of the line.
     * <p>
     * Can be {@code null}, and in that case that the position will be set to (0, 0, 0) client-side.
     */
    protected Vector3f endPosition;

    /**
     * Creates a new DebugLine with the specified position, color, and end position.
     *
     * @param position    the starting position of the line.
     * @param color       the color of the line.
     * @param endPosition the end position of the line.
     */
    public DebugShapeLine(Vector3f position, Color color, int dimensionId, Vector3f endPosition) {
        super(position, color, dimensionId);
        this.endPosition = endPosition;
    }

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
        return new ScriptDebugShape(
                id, getType(), position, null,
                null, null, color,
                null, dimensionId,null, null, endPosition,
                null, null, null
        );
    }
}