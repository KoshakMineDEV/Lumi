package cn.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.UpdateTradePacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.ContainerType;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.recipe.impl.TradeInventoryRecipe;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeInventory extends BaseInventory {

    public static final int TRADE_INPUT1_UI_SLOT = 4;
    public static final int TRADE_INPUT2_UI_SLOT = 5;
    public static final int LEGACY_TRADE_INPUT1_UI_SLOT = 6;
    public static final int LEGACY_TRADE_INPUT2_UI_SLOT = 7;
    public static final int FIRST_RECIPE_NETWORK_ID = 0x20000000;

    private static final AtomicInteger NEXT_RECIPE_ID = new AtomicInteger(FIRST_RECIPE_NETWORK_ID);

    private final long traderEntityId;
    private final String displayName;
    private final int tradeTier;
    private final int[] tierExpRequirements;
    private final List<Offer> offers;
    private final Map<Integer, Offer> assignedOffers = new LinkedHashMap<>();
    private int inputUpdates;
    private Player closingPlayer;

    public TradeInventory(InventoryHolder holder) {
        this(holder, List.of());
    }

    public TradeInventory(InventoryHolder holder, Collection<TradeInventoryRecipe> recipes) {
        this(holder, holder instanceof Entity entity ? entity.getId() : 0,
                holder instanceof Entity entity ? entity.getName() : InventoryType.TRADING.getDefaultTitle(),
                recipes, 1, 0);
    }

    public TradeInventory(InventoryHolder holder, long traderEntityId, String displayName,
                          Collection<TradeInventoryRecipe> recipes, int tradeTier, int... tierExpRequirements) {
        super(holder, InventoryType.TRADING, Collections.emptyMap(), 2, displayName);
        this.traderEntityId = traderEntityId;
        this.displayName = displayName;
        this.offers = recipes.stream().map(Offer::new).toList();
        this.tradeTier = tradeTier;
        this.tierExpRequirements = tierExpRequirements.clone();
    }

    @Override
    public boolean open(Player who) {
        return !this.destroyed && this.closingPlayer == null && who.getTradeInventory() == null
                && this.getViewers().isEmpty() && super.open(who);
    }

    @Override
    public void onOpen(Player who) {
        this.assignedOffers.clear();
        for (Offer offer : this.offers) {
            if (offer.isSupportedOn(who.protocol)) {
                this.assignedOffers.put(NEXT_RECIPE_ID.incrementAndGet(), offer);
            }
        }
        super.onOpen(who);
        this.sendOffers(who);
        this.sendContents(who);
    }

    private void sendOffers(Player who) {
        ListTag<CompoundTag> recipes = new ListTag<>("Recipes");
        this.assignedOffers.forEach((id, offer) -> recipes.add(offer.toNetworkTag(id, who.protocol)));

        ListTag<CompoundTag> requirements = new ListTag<>("TierExpRequirements");
        for (int i = 0; i < this.tierExpRequirements.length; i++) {
            requirements.add(new CompoundTag().putInt(Integer.toString(i), this.tierExpRequirements[i]));
        }

        UpdateTradePacket packet = new UpdateTradePacket();
        packet.windowId = (byte) who.getWindowId(this);
        packet.tradeTier = this.tradeTier;
        packet.traderUniqueEntityId = this.traderEntityId;
        packet.playerUniqueEntityId = who.getId();
        packet.displayName = this.displayName;
        packet.newTradingUi = true;
        packet.usingEconomyTrade = true;
        try {
            packet.offers = NBTIO.write(new CompoundTag().putList(recipes).putList(requirements),
                    ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        who.dataPacket(packet);
    }

    @Override
    public void onClose(Player who) {
        if (!this.getViewers().contains(who)) {
            return;
        }
        super.onClose(who);
        this.assignedOffers.clear();
        this.closingPlayer = who;

        int windowId = who.getWindowId(this);
        if (windowId != ContainerIds.NONE && who.getClosingWindowId() != Integer.MAX_VALUE) {
            ContainerClosePacket packet = new ContainerClosePacket();
            packet.windowId = windowId;
            packet.wasServerInitiated = who.getClosingWindowId() != (byte) windowId;
            packet.type = ContainerType.TRADE;
            who.dataPacket(packet);
        }

        if (this.inputUpdates == 0) {
            this.returnClosedInputs();
        }
    }

    public void withInputUpdates(Runnable update) {
        this.inputUpdates++;
        try {
            update.run();
        } finally {
            if (--this.inputUpdates == 0 && this.closingPlayer != null) {
                this.returnClosedInputs();
            }
        }
    }

    private void returnClosedInputs() {
        Player player = this.closingPlayer;
        Item first = this.getItem(0);
        Item second = this.getItem(1);
        this.setItemForce(0, Item.AIR_ITEM);
        this.setItemForce(1, Item.AIR_ITEM);
        this.closingPlayer = null;
        player.giveItem(first, second);
    }

    @Override
    public void sendContents(Player... players) {
        this.sendSlot(0, players);
        this.sendSlot(1, players);
    }

    @Override
    public void sendSlot(int index, Player player) {
        this.sendSlot(index, new Player[]{player});
    }

    @Override
    public void sendSlot(int index, Player... players) {
        if (index < 0 || index >= this.getSize()) {
            return;
        }
        for (Player player : players) {
            if (!this.getViewers().contains(player)) {
                continue;
            }
            InventorySlotPacket packet = new InventorySlotPacket();
            packet.inventoryId = ContainerIds.UI;
            packet.slot = TRADE_INPUT1_UI_SLOT + index;
            packet.item = this.getItem(index);
            packet.containerNameData = FullContainerName.of(player.isInventoryServerAuthoritative()
                    ? index == 0 ? ContainerSlotType.TRADE2_INGREDIENT_1 : ContainerSlotType.TRADE2_INGREDIENT_2
                    : ContainerSlotType.ANVIL_INPUT);
            player.dataPacket(packet);
        }
    }

    public Offer getAssignedOffer(int recipeNetId) {
        return this.assignedOffers.get(recipeNetId);
    }

    public Collection<Offer> getAssignedOffers() {
        return Collections.unmodifiableCollection(this.assignedOffers.values());
    }

    public boolean canUse(Offer offer, int times) {
        return times > 0 && offer.data.getInt("tier") < this.tradeTier
                && (long) offer.uses + times <= offer.data.getInt("maxUses");
    }

    public void completeTrade(Player player, Offer offer, int times) {
        offer.uses += times;
        int reward = offer.data.getInt("rewardExp");
        if (reward > 0 && player.getLevel() != null) {
            player.getLevel().dropExpOrb(player, reward * times);
        }
    }

    public static class Offer {

        private final Item firstInput;
        private final Item secondInput;
        private final Item result;
        private final CompoundTag data;
        private int uses;

        private Offer(TradeInventoryRecipe recipe) {
            this.firstInput = recipe.getBuyItem().clone();
            Item second = recipe.getSecondBuyItem();
            this.secondInput = second == null ? Item.get(Item.AIR) : second.clone();
            this.result = recipe.getSellItem().clone();
            if (this.firstInput.isNull() || this.result.isNull()) {
                throw new IllegalArgumentException("Trade offers require an input and a result");
            }
            this.data = recipe.toNBT();
            this.uses = this.data.getInt("uses");
        }

        public Item getResult() {
            return this.result.clone();
        }

        public int getInputCount(int slot) {
            Item input = slot == 0 ? this.firstInput : this.secondInput;
            return input.isNull() ? 0 : input.getCount();
        }

        public boolean matchesInput(int slot, Item item) {
            Item input = slot == 0 ? this.firstInput : this.secondInput;
            return input.isNull() || item.equals(input, true, input.hasCompoundTag());
        }

        private boolean isSupportedOn(int protocol) {
            return this.firstInput.isSupportedOn(protocol) && this.secondInput.isSupportedOn(protocol)
                    && this.result.isSupportedOn(protocol);
        }

        private CompoundTag toNetworkTag(int id, int protocol) {
            CompoundTag tag = this.data.copy().putInt("netId", id).putInt("uses", this.uses)
                    .putCompound("buyA", NBTIO.putItemHelper(this.firstInput, -1, protocol, true))
                    .putCompound("sell", NBTIO.putItemHelper(this.result, -1, protocol, true));
            if (this.secondInput.isNull()) {
                tag.remove("buyB");
            } else {
                tag.putCompound("buyB", NBTIO.putItemHelper(this.secondInput, -1, protocol, true));
            }
            return tag;
        }
    }
}
