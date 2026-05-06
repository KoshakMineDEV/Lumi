package cn.nukkit.dispenser.impl;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockBeehive;
import cn.nukkit.block.BlockDispenser;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;

public class ShearsDispenseBehaviour extends DefaultDispenseBehavior {

    @Override
    public Item dispense(BlockDispenser block, BlockFace face, Item item) {
        Block target = block.getSide(face);
        item = item.clone();
        for (Entity entity : block.getLevel().getNearbyEntities(new SimpleAxisAlignedBB(
                target.x,
                target.y,
                target.z,
                target.x + 1,
                target.y + 1,
                target.z + 1
        ))) {
            // TODO: uncomment when new sheep ai will be done
            /*if (entity instanceof EntitySheep sheep) {
                if (!sheep.isSheared()) {
                    sheep.shear(true);
                    item.useOn(entity);
                    return item.getDamage() >= item.getMaxDurability() ? null : item;
                }
            }*/
        }

        if (target instanceof BlockBeehive && target.onActivate(item, null)) {
            return item.getDamage() >= item.getMaxDurability() ? null : item;
        }

        return item;
    }
}