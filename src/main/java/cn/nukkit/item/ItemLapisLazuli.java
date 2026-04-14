package cn.nukkit.item;

import cn.nukkit.item.trim.ItemTrimMaterialType;

public class ItemLapisLazuli extends StringItemBase implements ItemTrimMaterial {

    public ItemLapisLazuli() {
        super(LAPIS_LAZULI, "Lapis Lazuli");
    }

    @Override
    public ItemTrimMaterialType getMaterial() {
        return ItemTrimMaterialType.MATERIAL_LAPIS;
    }
}
