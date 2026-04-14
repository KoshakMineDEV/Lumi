package cn.nukkit.level;

import lombok.Data;

@Data
public class DimensionData {
    private final int dimensionId;
    private final int minHeight;
    private final int maxHeight;
    private final int height;

    public DimensionData(int dimensionId, int minHeight, int maxHeight) {
        this.dimensionId = dimensionId;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;

        this.height = maxHeight - minHeight + 1;
    }

    public int getSectionOffset() {
        return -this.getMinSectionY();
    }

    public int getMinSectionY() {
        return this.minHeight >> 4;
    }

    public int getMaxSectionY() {
        return this.maxHeight >> 4;
    }
}
