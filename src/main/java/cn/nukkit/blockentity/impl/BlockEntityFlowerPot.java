package cn.nukkit.blockentity.impl;

import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * Created by Snake1999 on 2016/2/4.
 * Package cn.nukkit.blockentity in project Nukkit.
 */
public class BlockEntityFlowerPot extends BlockEntitySpawnable {
    public BlockEntityFlowerPot(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        if (!namedTag.contains("PlantBlock")) {
            namedTag.putCompound("PlantBlock", new CompoundTag().putString("name", ItemNamespaceId.AIR));
        }
        super.initBlockEntity();
    }

    @Override
    public CompoundTag getSpawnCompound() {
        CompoundTag tag = new CompoundTag()
                .putString("id", BlockEntity.FLOWER_POT)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z);

        tag.putCompound("PlantBlock", namedTag.getCompound("PlantBlock"));
        return tag;
    }
}