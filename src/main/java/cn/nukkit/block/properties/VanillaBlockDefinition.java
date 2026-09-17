package cn.nukkit.block.properties;

import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Value;

@Value
public class VanillaBlockDefinition {
    private final String name;
    private final CompoundTag properties;
}