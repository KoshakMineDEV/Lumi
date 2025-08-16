package cn.nukkit.blockentity.impl;

import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class BlockEntityItemFrameGlow extends BlockEntityItemFrame {

    public BlockEntityItemFrameGlow(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public String getName() {
        return "Glow Item Frame";
    }
}
