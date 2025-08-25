package cn.nukkit.item;

import cn.nukkit.item.trim.ItemTrimMaterialType;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class ItemIngotCopper extends StringItemBase implements ItemTrimMaterial {

    public ItemIngotCopper() {
        super(COPPER_INGOT, "Copper Ingot");
    }

    @Override
    public ItemTrimMaterialType getMaterial() {
        return ItemTrimMaterialType.MATERIAL_COPPER;
    }
}
