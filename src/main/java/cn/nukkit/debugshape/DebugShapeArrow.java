package cn.nukkit.debugshape;

import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;

import java.awt.Color;

public class DebugShapeArrow extends DebugShape {

    /**
     * The end position of the arrow.
     * <p>
     * Can be {@code null}, and in that case that the position will be set to (0, 0, 0) client-side.
     */
    protected Vector3f endPosition;
    /**
     * The length of the arrow head.
     * <p>
     * Can be {@code null}, and in that case that the arrow head length will be set to 1 client-side.
     */
    protected Float arrowHeadLength;
    /**
     * The radius of the arrow head.
     * <p>
     * Can be {@code null}, and in that case that the arrow head radius will be set to 0.5 client-side.
     */
    protected Float arrowHeadRadius;
    /**
     * The segments of the arrow head, which determines how many segments the arrow head will be divided into.
     * <p>
     * Can be {@code null}, and in that case that the segments will be set to 4 client-side.
     */
    protected Integer arrowHeadSegments;

    /**
     * Creates a new DebugShapeArrow instance with the specified parameters.
     *
     * @param position          the starting position of the arrow.
     * @param color             the color of the arrow.
     * @param endPosition       the end position of the arrow.
     * @param arrowHeadLength   the length of the arrow head.
     * @param arrowHeadRadius   the radius of the arrow head.
     * @param arrowHeadSegments the number of segments in the arrow head.
     */
    public DebugShapeArrow(Vector3f position, Color color, int dimensionId, Vector3f endPosition, Float arrowHeadLength, Float arrowHeadRadius, Integer arrowHeadSegments) {
        super(position, color, dimensionId);
        this.endPosition = endPosition;
        this.arrowHeadLength = arrowHeadLength;
        this.arrowHeadRadius = arrowHeadRadius;
        this.arrowHeadSegments = arrowHeadSegments;
    }

    /**
     * Gets the end position of the arrow.
     *
     * @return the end position of the arrow.
     */
    public Vector3f getEndPosition() {
        return endPosition != null ? endPosition : ZERO_VECTOR;
    }

    /**
     * Sets the end position of the arrow.
     *
     * @param endPosition the end position of the arrow.
     */
    public void setEndPosition(Vector3f endPosition) {
        this.endPosition = endPosition;
    }

    /**
     * Gets the length of the arrow head.
     *
     * @return the length of the arrow head.
     */
    public float getArrowHeadLength() {
        return arrowHeadLength != null ? arrowHeadLength : 1.0f;
    }

    /**
     * Sets the length of the arrow head.
     *
     * @param arrowHeadLength the length of the arrow head.
     */
    public void setArrowHeadLength(Float arrowHeadLength) {
        this.arrowHeadLength = arrowHeadLength;
    }

    /**
     * Gets the radius of the arrow head.
     *
     * @return the radius of the arrow head.
     */
    public float getArrowHeadRadius() {
        return arrowHeadRadius != null ? arrowHeadRadius : 0.5f;
    }

    /**
     * Sets the radius of the arrow head.
     *
     * @param arrowHeadRadius the radius of the arrow head.
     */
    public void setArrowHeadRadius(Float arrowHeadRadius) {
        this.arrowHeadRadius = arrowHeadRadius;
    }

    /**
     * Gets the segments of the arrow head.
     *
     * @return the segments of the arrow head.
     */
    public int getArrowHeadSegments() {
        return arrowHeadSegments != null ? arrowHeadSegments : 4;
    }

    /**
     * Sets the segments of the arrow head.
     *
     * @param arrowHeadSegments the number of segments in the arrow head.
     */
    public void setArrowHeadSegments(Integer arrowHeadSegments) {
        this.arrowHeadSegments = arrowHeadSegments;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.ARROW;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return new ScriptDebugShape(
                id, getType(), position, null,
                null, null, color,
                null, dimensionId, null, null, null,
                arrowHeadLength, arrowHeadRadius, arrowHeadSegments
        );
    }
}