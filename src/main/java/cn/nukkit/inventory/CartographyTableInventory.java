package cn.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;

public class CartographyTableInventory extends FakeBlockUIComponent {

    public static final int CARTOGRAPHY_INPUT_UI_SLOT = 12;
    public static final int CARTOGRAPHY_ADDITIONAL_UI_SLOT = 13;

    public CartographyTableInventory(PlayerUIInventory playerUI, Position position) {
        super(playerUI, InventoryType.CARTOGRAPHY, CARTOGRAPHY_INPUT_UI_SLOT, position);
    }

    @Override
    public void onClose(Player who) {
        super.onClose(who);

        for (int slot = 0; slot < 2; slot++) {
            Item item = this.getItem(slot);
            if (!item.isNull() && this.clear(slot, false)) {
                who.giveItem(item);
            }
        }

        who.resetCraftingGridType();
    }

    public Item getInput() {
        return this.getItem(0);
    }

    public Item getAdditional() {
        return this.getItem(1);
    }
}
