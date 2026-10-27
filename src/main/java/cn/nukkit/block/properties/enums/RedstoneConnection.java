package cn.nukkit.block.properties.enums;

public enum RedstoneConnection {
    NONE,
    SIDE,
    UP;

    @Override
    public String toString() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
