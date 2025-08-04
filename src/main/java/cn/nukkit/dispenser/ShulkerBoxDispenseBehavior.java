package cn.nukkit.dispenser;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDispenser;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.item.Item;
import cn.nukkit.level.vibration.VanillaVibrationTypes;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;

public class ShulkerBoxDispenseBehavior extends DefaultDispenseBehavior {

    @Override
    public Item dispense(BlockDispenser block, BlockFace face, Item item) {
        Block shulkerBox = Block.get(BlockID.SHULKER_BOX);
        Block target = block.getSide(face);

        this.success = block.level.getCollidingEntities(shulkerBox.getBoundingBox()).length == 0;

        if (this.success) {
            BlockFace shulkerBoxFace = target.down().getId() == BlockID.AIR ? face : BlockFace.UP;

            CompoundTag nbt = BlockEntity.getDefaultCompound(target, BlockEntity.SHULKER_BOX);
            nbt.putByte("facing", shulkerBoxFace.getIndex());

            if (item.hasCustomName()) {
                nbt.putString("CustomName", item.getCustomName());
            }

            CompoundTag tag = item.getNamedTag();

            if (tag != null) {
                if (tag.contains("Items")) {
                    nbt.putList(tag.getList("Items"));
                }
            }

            BlockEntity.createBlockEntity(BlockEntity.SHULKER_BOX, block.level.getChunk(target.getChunkX(), target.getChunkZ()), nbt);
            block.level.updateComparatorOutputLevel(target);
            block.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, target.add(0.5, 0.5 , 0.5), VanillaVibrationTypes.BLOCK_PLACE));
        }

        return null;
    }
}
