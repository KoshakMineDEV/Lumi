package cn.nukkit.network.protocol.types;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Value
public class DimensionDefinition {
    String id;
    int maximumHeight;
    int minimumHeight;
    int generatorType;
    int dimensionType;
    /**
     * @since v2168 1.26.40
     */
    UUID packId;
    /**
     * @since v2192 1.26.50
     */
    @Nullable
    String defaultBiome;
    /**
     * @since v2225
     */
    int cloudHeight;
    boolean renderClouds;

    public DimensionDefinition(String id, int maximumHeight, int minimumHeight, int generatorType, int dimensionType,
                               UUID packId, @Nullable String defaultBiome) {
        this(id, maximumHeight, minimumHeight, generatorType, dimensionType, packId, defaultBiome, 128, true);
    }

    public DimensionDefinition(String id, int maximumHeight, int minimumHeight, int generatorType, int dimensionType,
                               UUID packId, @Nullable String defaultBiome, int cloudHeight, boolean renderClouds) {
        this.id = id;
        this.maximumHeight = maximumHeight;
        this.minimumHeight = minimumHeight;
        this.generatorType = generatorType;
        this.dimensionType = dimensionType;
        this.packId = packId;
        this.defaultBiome = defaultBiome;
        this.cloudHeight = cloudHeight;
        this.renderClouds = renderClouds;
    }
}