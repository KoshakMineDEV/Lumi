package cn.nukkit.item.customitem;

import cn.nukkit.Player;
import cn.nukkit.item.ItemFood;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.StringItem;
import cn.nukkit.math.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author lt_name
 */
public abstract class ItemCustomFood extends ItemFood implements CustomItem {
    private final String id;
    private final String textureName;

    public ItemCustomFood(@NotNull String id, @Nullable String name) {
        super(ItemID.STRING_IDENTIFIED_ITEM, 0, 1, StringItem.checkNotEmpty(name));
        this.id = id;
        this.textureName = name;
    }

    public ItemCustomFood(@NotNull String id, @Nullable String name, @NotNull String textureName) {
        super(ItemID.STRING_IDENTIFIED_ITEM, 0, 1, StringItem.checkNotEmpty(name));
        this.id = id;
        this.textureName = textureName;
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        if (player.getFoodData().isHungry() || player.isCreative() || isRequiresHunger()) {
            return true;
        }
        player.getFoodData().sendFood();
        return false;
    }

    @Override
    public String getTextureName() {
        return textureName;
    }

    @Override
    public String getNamespaceId() {
        return id;
    }

    @Override
    public String getNamespaceId(int protocolId) {
        return this.getNamespaceId();
    }

    @Override
    public final int getId() {
        return CustomItem.super.getId();
    }

    public boolean isDrink() {
        return false;
    }
}