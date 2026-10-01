package cn.nukkit.network.protocol;

import cn.nukkit.level.DimensionData;
import cn.nukkit.level.DimensionEnum;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.network.protocol.types.DimensionDefinition;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@ToString
public class DimensionDataPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.DIMENSION_DATA_PACKET;

    public List<DimensionDefinition> definitions = new ObjectArrayList<>();

    /**
     * Creates a packet containing only modified dimension definitions.
     * Returns null if no dimensions have been modified.
     */
    @Nullable
    public static DimensionDataPacket createIfModified() {
        DimensionDataPacket packet = new DimensionDataPacket();
        for (DimensionEnum dimension : DimensionEnum.values()) {
            if (!dimension.isVanillaBounds()) {
                packet.definitions.add(fromDimensionEnum(dimension));
            }
        }
        return packet.definitions.isEmpty() ? null : packet;
    }

    private static DimensionDefinition fromDimensionEnum(DimensionEnum dimension) {
        var dimensionData = dimension.getDimensionData();
        return new DimensionDefinition(
                dimension.getIdentifier(),
                // Bedrock dimension definitions use an open upper bound.
                dimensionData.getMaxHeight() + 1,
                dimensionData.getMinHeight(),
                getGeneratorType(dimension),
                3, //force 3 because currently we don't support anything except overworld
                new UUID(0, 0),
                null
        );
    }

    private static int getGeneratorType(DimensionEnum dimension) {
        return dimension.isVanillaBounds() ? dimension.getGeneratorType() : Generator.TYPE_VOID;
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.decodeUnsupported();
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(definitions.size());
        for (DimensionDefinition definition : definitions) {
            this.putString(definition.getId());
            if (this.protocol >= ProtocolInfo.v1_26_50) {
                this.putVarInt(definition.getMinimumHeight());
                this.putVarInt(definition.getMaximumHeight() - definition.getMinimumHeight());
            } else {
                this.putVarInt(definition.getMaximumHeight());
                this.putVarInt(definition.getMinimumHeight());
            }
            this.putVarInt(definition.getGeneratorType());
            if(protocol >= ProtocolInfo.v1_26_20_26) {
                this.putVarInt(definition.getDimensionType());
                if (protocol >= ProtocolInfo.v1_26_40) {
                    this.putUUID(definition.getPackId());
                    if (this.protocol >= ProtocolInfo.v1_26_50) {
                        // v2192 新增默认生物群系 / default biome added in v2192
                        this.putString(definition.getDefaultBiome() != null ? definition.getDefaultBiome() : "");
                    }
                }
            }
        }
    }
}
