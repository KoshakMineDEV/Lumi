package cn.nukkit.block.material;

import cn.nukkit.block.customblock.CustomBlock;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.Data;

@Data
public final class CustomBlockType implements BlockType {
    private final String identifier;
    private final int runtimeId;

    public CustomBlockType(CustomBlock customBlock) {
        var mappings = RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL);
        var entry = mappings.toRuntime(255 - customBlock.getId(), customBlock.getDamage());
        this.identifier = entry.getIdentifier();
        this.runtimeId = entry.getRuntimeId();
    }
}
