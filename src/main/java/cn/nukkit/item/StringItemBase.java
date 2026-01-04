package cn.nukkit.item;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class StringItemBase extends Item implements StringItem {

    private final String namespaceId;

    public StringItemBase(@NotNull String namespaceId, @Nullable String name) {
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
    public final int getId() {
        return StringItem.super.getId();
    }

    @Override
    public StringItemBase clone() {
        return (StringItemBase) super.clone();
    }
}
