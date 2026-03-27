package cn.nukkit.debugshape;

import cn.nukkit.Player;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author AllayMC, Koshak_Mine
 */
@Getter
public abstract class DebugShape {
    protected static final AtomicLong DEBUG_SHAPE_ID_COUNTER = new AtomicLong(0);
    protected static final Vector3f ZERO_VECTOR = new Vector3f(0, 0, 0);

    /**
     * The id of this debug shape.
     */
    @Getter
    protected final long id;
    /**
     * The viewers of this debug shape.
     */
    @Getter
    protected final Map<Long, Player> viewers;
    /**
     * The position of the shape. For most shapes this is the centre of the shape, except
     * {@link DebugShapeLine} and {@link DebugShapeArrow} where this represents the start point of the line.
     * <p>
     * Can be {@code null}, and in that case that the position will be set to (0, 0, 0) client-side.
     */
    protected Vector3f position;
    /**
     * The color of the shape.
     * <p>
     * Can be {@code null}, and in that case that the color will be set to white client-side.
     */
    protected Color color;
    /**
     * The id of this debug shape.
     */
    @Getter
    protected final int dimensionId;

    /**
     * Creates a new debug shape with the specified position, rotation, color, and scale.
     *
     * @param position The position of the shape.
     * @param color    the color of the shape.
     */
    public DebugShape(Vector3f position, Color color, int dimensionId) {
        this.id = DEBUG_SHAPE_ID_COUNTER.getAndIncrement();
        this.viewers = new Long2ObjectOpenHashMap<>();
        this.position = position;
        this.color = color;
        this.dimensionId = dimensionId;
    }

    /**
     * Gets the position of this debug shape.
     *
     * @return the position of this debug shape.
     */
    public Vector3f getPosition() {
        return this.position != null ? this.position : ZERO_VECTOR;
    }

    /**
     * Sets the position of this debug shape.
     *
     * @param position the new position of this debug shape.
     */
    public void setPosition(Vector3f position) {
        this.position = position;
    }

    /**
     * Gets the color of this debug shape.
     *
     * @return the color of this debug shape.
     */
    public Color getColor() {
        return this.color != null ? this.color : Color.WHITE;
    }

    /**
     * Sets the color of this debug shape.
     *
     * @param color the new color of this debug shape.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Gets the type of this debug shape.
     *
     * @return the type of this debug shape
     */
    public abstract ScriptDebugShapeType getType();

    /**
     * Creates a removal notice for this debug shape.
     * This is used to notify the client that this debug shape should be removed.
     *
     * @return a removal notice for this debug shape.
     */
    public ScriptDebugShape createRemovalNotice() {
        return new ScriptDebugShape(
                this.id, null, null,
                null, null, null,
                null, null, dimensionId,null, null,
                null, null, null, null
        );
    }

    /**
     * Converts this debug shape to a network data representation.
     *
     * @return the network data representation of this debug shape.
     */
    public abstract ScriptDebugShape toNetworkData();

}