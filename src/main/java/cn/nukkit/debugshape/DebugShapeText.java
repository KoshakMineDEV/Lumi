package cn.nukkit.debugshape;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import lombok.Getter;

import java.awt.*;

public class DebugShapeText extends DebugShape {

    /**
     * The text to display.
     */
    @Getter
    protected String text;

    /**
     * Creates a DebugShapeText with the specified position, color, and text.
     *
     * @param position The position of the text in the world.
     * @param color    The color of the text.
     * @param text     The text to display.
     */
    public DebugShapeText(Vector3f position, Color color, int dimensionId, String text) {
        super(position, color, dimensionId);
        this.text = text;
    }

    /**
     * Sets the text to display.
     *
     * @param text The text to display.
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.TEXT;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return new ScriptDebugShape(
                id, getType(), position, null,
                null, null, color,
                null, dimensionId, text, null, null,
                null, null, null
        );
    }
}