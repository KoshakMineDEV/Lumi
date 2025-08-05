package cn.nukkit.block.material;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockUnknown;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.network.protocol.ProtocolInfo;

public interface BlockType {

    String getIdentifier();

    int getRuntimeId();

    default Block createBlock() {
        try {
            var mapping = RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL);
            var entry = mapping.fromRuntime(this.getRuntimeId());
            var block = Block.get(entry.getLegacyId(), entry.getDamage());
            return block instanceof BlockUnknown ? Block.get(Block.AIR) : block;
        } catch (IllegalArgumentException e) {
            return Block.get(Block.AIR);
        }
    }
}
