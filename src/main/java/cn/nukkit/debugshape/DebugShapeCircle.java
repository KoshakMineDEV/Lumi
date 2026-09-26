package cn.nukkit.debugshape;

import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class DebugShapeCircle extends DebugShape {

    /**
     * The scale of the circle, which is a multiplier for the size of the circle.
     * <p>
     * Can be {@code null}, and in that case that the scale will be set to 1 client-side.
     */
    protected Float scale;
    /**
     * The segments of the circle, which determines how many segments the circle will be divided into.
     * <p>
     * Can be {@code null}, and in that case that the segments will be set to 20 client-side.
     */
    protected Integer segments;

    /**
     * Gets the scale of the circle.
     *
     * @return the scale of the circle.
     */
    public float getScale() {
        return scale != null ? scale : 1.0f;
    }

    /**
     * Sets the scale of the circle.
     *
     * @param scale the scale of the circle, which is a multiplier for the size of the circle.
     */
    public void setScale(Float scale) {
        this.scale = scale;
    }

    /**
     * Gets the segments of the circle.
     *
     * @return the segments of the circle.
     */
    public int getSegments() {
        return segments != null ? segments : 20;
    }

    /**
     * Sets the segments of the circle.
     *
     * @param segments the segments of the circle, which determines how many segments the circle will be divided into.
     */
    public void setSegments(Integer segments) {
        this.segments = segments;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.CIRCLE;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return commonNetworkData()
                .scale(scale)
                .segments(segments)
                .build();
    }
}