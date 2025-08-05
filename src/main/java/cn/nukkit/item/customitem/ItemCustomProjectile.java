package cn.nukkit.item.customitem;

import cn.nukkit.item.StringItem;
import cn.nukkit.item.StringItemProjectileBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author MEFRREEX
 */
public abstract class ItemCustomProjectile extends StringItemProjectileBase implements CustomItem {

    private final String textureName;

    public ItemCustomProjectile(@NotNull String id, @Nullable String name) {
        super(id, StringItem.checkNotEmpty(name));
        this.textureName = name;
    }

    public ItemCustomProjectile(@NotNull String id, @Nullable String name, @NotNull String textureName) {
        super(id, StringItem.checkNotEmpty(name));
        this.textureName = textureName;
    }

    @Override
    public String getTextureName() {
        return textureName;
    }
}