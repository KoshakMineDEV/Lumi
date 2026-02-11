package cn.nukkit.debugshape;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;

import java.awt.*;

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
     * Creates a new DebugShapeBox with the default position, color, scale and box bounds.
     *
     * @param position  the position of the box in the world.
     * @param color     the color of the box.
     * @param scale     the scale of the box, which is a multiplier for the size of the box.
     * @param boxBounds the bounds of the box, which is a vector representing the size of the box in each dimension (x, y and z).
     */
    public DebugShapeBox(Vector3f position, Color color, int dimensionId, Float scale, Vector3f boxBounds) {
        super(position, color, dimensionId);
        this.scale = scale;
        this.boxBounds = boxBounds;
    }

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
        return new ScriptDebugShape(
                id, getType(), position, scale,
                null, null, color,
                null, dimensionId,null, boxBounds, null,
                null, null, null
        );
    }
}