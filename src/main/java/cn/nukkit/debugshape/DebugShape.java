package cn.nukkit.debugshape;

import cn.nukkit.Player;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author AllayMC, Koshak_Mine
 */
@Getter
@SuperBuilder(toBuilder = true)
public abstract class DebugShape {
    protected static final AtomicLong DEBUG_SHAPE_ID_COUNTER = new AtomicLong(1);
    protected static final Vector3f ZERO_VECTOR = new Vector3f(0, 0, 0);

    /**
     * The id of this debug shape.
     */
    @lombok.Builder.Default
    protected long id = DEBUG_SHAPE_ID_COUNTER.getAndIncrement();
    /**
     * The viewers of this debug shape.
     */
    @Getter
    @lombok.Builder.Default
    protected final Map<Long, Player> viewers = new Long2ObjectOpenHashMap<>();
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
    protected int dimensionId;

    /**
     * Gets the position of this debug shape.
     *
     * @return the position of this debug shape.
     */
    public Vector3f getPosition() {
        return this.position != null ? this.position : ZERO_VECTOR;
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
        return ScriptDebugShape.builder()
                .id(this.id)
                .dimensionId(this.dimensionId)
                .build();
    }

    /**
     * Converts this debug shape to a network data representation.
     *
     * @return the network data representation of this debug shape.
     */
    public abstract ScriptDebugShape toNetworkData();

    protected ScriptDebugShape.ScriptDebugShapeBuilder commonNetworkData() {
        return ScriptDebugShape.builder()
                .id(this.id)
                .type(this.getType())
                .position(this.position)
                .color(this.color)
                .dimensionId(this.dimensionId);
    }
}