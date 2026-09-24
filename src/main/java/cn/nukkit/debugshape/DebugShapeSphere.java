package cn.nukkit.debugshape;

import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class DebugShapeSphere extends DebugShape {

    /**
     * The scale of the sphere, which is a multiplier for the size of the sphere.
     * <p>
     * Can be {@code null}, and in that case that the scale will be set to 1 client-side.
     */
    protected Float scale;
    /**
     * The segments of the sphere, which determines how many segments the sphere will be divided into.
     * <p>
     * Can be {@code null}, and in that case that the segments will be set to 20 client-side.
     */
    protected Integer segments;

    /**
     * Gets the scale of the sphere.
     *
     * @return the scale of the sphere.
     */
    public float getScale() {
        return scale != null ? scale : 1.0f;
    }

    /**
     * Sets the scale of the sphere.
     *
     * @param scale the scale of the sphere, which is a multiplier for the size of the sphere.
     */
    public void setScale(Float scale) {
        this.scale = scale;
    }

    /**
     * Gets the segments of the sphere.
     *
     * @return the segments of the sphere.
     */
    public int getSegments() {
        return segments != null ? segments : 20;
    }

    /**
     * Sets the segments of the sphere.
     *
     * @param segments the segments of the sphere, which determines how many segments the sphere will be divided into.
     */
    public void setSegments(Integer segments) {
        this.segments = segments;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.SPHERE;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return commonNetworkData()
                .scale(scale)
                .segments(segments)
                .build();
    }
}