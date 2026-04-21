package cn.nukkit.dispenser.impl;

import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBoneMeal;
import cn.nukkit.math.BlockFace;

public class BoneMealDispenseBehavior extends DefaultDispenseBehavior {

    @Override
    public Item dispense(BlockDispenser block, BlockFace face, Item item) {
        Block target = block.getSide(face);

        if (item instanceof ItemBoneMeal) {
            if (target instanceof BlockCrops || target instanceof BlockSapling || target instanceof BlockTallGrass || target instanceof BlockDoublePlant || target instanceof BlockMushroom || target instanceof BlockSugarcane || target instanceof BlockBamboo || target instanceof BlockBambooSapling) {
                target.onActivate(item);
            } else {
                this.success = false;
            }
            return null;
        }

        return super.dispense(block, face, item);
    }
}
