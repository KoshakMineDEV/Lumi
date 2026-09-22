package cn.nukkit.debugshape;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@SuperBuilder(toBuilder = true)
public class DebugShapeText extends DebugShape {

    /**
     * The text to display.
     */
    @Getter
    protected String text;
    /**
     * @since v975
     */
    protected Boolean useRotation;
    /**
     * @since v975
     */
    @Nullable
    protected Color backgroundColor;
    /**
     * @since v975
     */
    protected Boolean depthTest;
    /**
     * @since v975
     */
    protected Boolean showBackface;
    /**
     * @since v975
     */
    protected Boolean showTextBackface;
    /**
     * @since v2192
     */
    protected Float lineGapHeight;

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
        return commonNetworkData()
                .text(text)
                .useRotation(useRotation)
                .backgroundColor(backgroundColor)
                .lineGapHeight(lineGapHeight)
                .depthTest(depthTest)
                .showBackface(showBackface)
                .showTextBackface(showTextBackface)
                .build();
    }
}