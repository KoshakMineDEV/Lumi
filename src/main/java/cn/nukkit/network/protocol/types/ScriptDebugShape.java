package cn.nukkit.network.protocol.types;

import cn.nukkit.math.Vector3f;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

@Value
public class ScriptDebugShape {
    long id;
    @Nullable
    ScriptDebugShapeType type;
    @Nullable
    Vector3f position;
    @Nullable
    Float scale;
    @Nullable
    Vector3f rotation;
    @Nullable
    Float totalTimeLeft;
    @Nullable
    Color color;
    @Nullable
    private final Long attachedToEntityId;
    int dimensionId;
    @Nullable
    String text;
    @Nullable
    Vector3f boxBounds;
    @Nullable
    Vector3f lineEndPosition;
    @Nullable
    Float arrowHeadLength;
    @Nullable
    Float arrowHeadRadius;
    @Nullable
    Integer segments;
}
