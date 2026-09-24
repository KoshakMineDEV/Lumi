package cn.nukkit.debugshape;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class DebugShapeBox extends DebugShape {

    protected static final Vector3f DEFAULT_BOX_BOUNDS = new Vector3f(1, 1, 1);

    /**
     * The scale of the box, which is a multiplier for the size of the box.
     * <p>
     * Can be {@code null}, and in that case that the scale will be set to 1 client-side.
     */
    protected Float scale;
    /**
     * The bounds of the box, which is a vector representing the size of the box in each dimension (x, y and z).
     * <p>
     * Can be {@code null}, and in that case that the boxBounds will be set to (1, 1, 1) client-side.
     */
    protected Vector3f boxBounds;

    /**
     * Gets the scale of the box.
     *
     * @return the scale of the box.
     */
    public float getScale() {
        return scale != null ? scale : 1.0f;
    }

    /**
     * Sets the scale of the box.
     *
     * @param scale the scale of the box, which is a multiplier for the size of the box.
     */
    public void setScale(Float scale) {
        this.scale = scale;
    }

    /**
     * Gets the bounds of the box.
     *
     * @return the bounds of the box.
     */
    public Vector3f getBoxBounds() {
        return boxBounds != null ? boxBounds : DEFAULT_BOX_BOUNDS;
    }

    /**
     * Sets the bounds of the box.
     *
     * @param boxBounds the bounds of the box, which is a vector representing the size of the box in each dimension (x, y and z).
     */
    public void setBoxBounds(Vector3f boxBounds) {
        this.boxBounds = boxBounds;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.BOX;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return commonNetworkData()
                .scale(scale)
                .boxBounds(boxBounds)
                .build();
    }
}