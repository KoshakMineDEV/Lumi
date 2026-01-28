package cn.nukkit.item;

import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemHarness extends StringItemBase {

    public ItemHarness(@NotNull String namespaceId, @Nullable String name) {
        super(namespaceId, name);
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_90;
    }
}
