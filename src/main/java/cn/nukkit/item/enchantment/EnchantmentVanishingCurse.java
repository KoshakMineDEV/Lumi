package cn.nukkit.item.enchantment;

import cn.nukkit.block.BlockSkull;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemCompass;

public class EnchantmentVanishingCurse extends Enchantment {

    public EnchantmentVanishingCurse() {
        super(ID_VANISHING_CURSE, NAME_VANISHING_CURSE, "curse.vanishing", EnchantmentRarity.VERY_RARE, EnchantmentType.BREAKABLE);
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean canEnchant(Item item) {
        return item.getBlock() instanceof BlockSkull || item instanceof ItemCompass || super.canEnchant(item);
    }
}
