package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class StringItemProjectileBase extends ItemProjectile implements StringItem {

    private final String namespaceId;

    public StringItemProjectileBase(@NotNull String namespaceId, @Nullable String name) {
        super(STRING_IDENTIFIED_ITEM, 0, 1, StringItem.checkNotEmpty(name));
        Preconditions.checkNotNull(namespaceId, "id can't be null");
        Preconditions.checkArgument(namespaceId.contains(":"), "The ID must be a namespaced ID, like minecraft:stone");
        this.namespaceId = namespaceId;
        this.clearNamedTag();
    }

    @Override
    public String getNamespaceId() {
        return this.namespaceId;
    }

    @Override
    public String getNamespaceId(int protocolId) {
        return this.getNamespaceId();
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_16_100;
    }
}