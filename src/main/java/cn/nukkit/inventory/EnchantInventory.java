package cn.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.EnchantmentHelper;
import cn.nukkit.network.protocol.PlayerEnchantOptionsPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import cn.nukkit.level.Position;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantInventory extends FakeBlockUIComponent {

    public static final int ENCHANT_INPUT_ITEM_UI_SLOT = 14;
    public static final int ENCHANT_REAGENT_UI_SLOT = 15;

    private static final AtomicInteger NEXT_OPTION_ID = new AtomicInteger(PlayerEnchantOptionsPacket.ENCH_RECIPEID);
    private final Map<Integer, PlayerEnchantOptionsPacket.EnchantOptionData> publishedOptions = new HashMap<>();

    public EnchantInventory(PlayerUIInventory playerUI, Position position) {
        super(playerUI, InventoryType.ENCHANT_TABLE, 14, position);
    }

    @Override
    public void onOpen(Player who) {
        super.onOpen(who);
        who.craftingType = Player.ENCHANT_WINDOW_ID;
    }

    @Override
    public void onClose(Player who) {
        super.onClose(who);
        if (who.isInventoryServerAuthoritative()) {
            this.publishedOptions.clear();
            for (int slot = 0; slot < 2; slot++) {
                Item item = this.getItem(slot);
                if (!item.isNull() && this.clear(slot, false)) {
                    who.giveItem(item);
                }
            }
        } else if (this.getViewers().isEmpty()) {
            for (int i = 0; i < 2; ++i) {
                who.getInventory().addItem(this.getItem(i));
                this.clear(i);
            }
        }
        who.craftingType = Player.CRAFTING_SMALL;
        who.resetCraftingGridType();
    }

    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        super.onSlotChange(index, before, send);
        Player player = this.playerUIHolder();
        if (index != 0 || !player.isInventoryServerAuthoritative() || player.getTopWindow().orElse(null) != this) {
            return;
        }

        Item input = this.getInputSlot();
        if (before.equalsExact(input)) {
            return;
        }
        this.publishedOptions.clear();
        PlayerEnchantOptionsPacket packet = new PlayerEnchantOptionsPacket();
        for (var option : EnchantmentHelper.generateOptions(this.getHolder(), input, player.getId() ^ input.getStackNetId())) {
            if (option.getEnchants0().isEmpty()) {
                continue;
            }
            int id = NEXT_OPTION_ID.getAndUpdate(value -> value == 0x1fffffff
                    ? PlayerEnchantOptionsPacket.ENCH_RECIPEID : value + 1);
            var published = new PlayerEnchantOptionsPacket.EnchantOptionData(option.getMinLevel(),
                    option.getPrimarySlot(), option.getEnchants0(), option.getEnchants1(), option.getEnchants2(),
                    option.getEnchantName(), id);
            this.publishedOptions.put(id, published);
            packet.options.add(published);
        }
        player.dataPacket(packet);
    }

    public PlayerEnchantOptionsPacket.EnchantOptionData getPublishedOption(int id) {
        return this.publishedOptions.get(id);
    }

    public void releasePublishedOption(int id) {
        this.publishedOptions.remove(id);
    }

    public Item getInputSlot() {
        return this.getItem(0);
    }

    public Item getOutputSlot() {
        return this.getItem(0);
    }

    public Item getReagentSlot() {
        return this.getItem(1);
    }
}
