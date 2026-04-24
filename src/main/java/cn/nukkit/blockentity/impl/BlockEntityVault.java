package cn.nukkit.blockentity.impl;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.command.selector.SelectorType;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BlockEntityVault extends BlockEntitySpawnable {
    public static final String TAG_CONFIG = "config";
    public static final String DATA = "data";

    public static final String TAG_LOOT_TABLE = "loot_table";
    public static final String TAG_OVERRIDE_LOOT_TABLE_TO_DISPLAY = "override_loot_table_to_display";
    public static final String TAG_ACTIVATION_RANGE = "activation_range";
    public static final String TAG_DEACTIVATION_RANGE = "deactivation_range";
    public static final String TAG_KEY_ITEM = "key_item";
    public static final String TAG_DISPLAY_ITEM = "display_item";
    public static final String TAG_CONNECTED_PLAYERS = "connected_players";
    public static final String TAG_CONNECTED_PARTICLES_RANGE = "connected_particles_range";

    public static final String DEFAULT_LOOT_TABLE = "minecraft:chests/trial_chambers/reward";
    public static final double DEFAULT_ACTIVATION_RANGE = 4.0d;
    public static final double DEFAULT_DEACTIVATION_RANGE = 4.5d;
    public static final double DEFAULT_CONNECTED_PARTICLES_RANGE = 4.5d;

    private String lootTable = DEFAULT_LOOT_TABLE;
    private String overrideLootTableToDisplay = "";
    private double activationRange = DEFAULT_ACTIVATION_RANGE;
    private double deactivationRange = DEFAULT_DEACTIVATION_RANGE;
    private Item keyItem = Item.get(ItemNamespaceId.TRIAL_KEY);

    private LinkedHashSet<String> rewardedPlayers;
    private List<Item> itemsToEject;

    private Item displayItem = Item.get(ItemNamespaceId.AIR);
    private LinkedHashSet<String> connectedPlayers;
    private double connectedParticlesRange = DEFAULT_CONNECTED_PARTICLES_RANGE;

    public BlockEntityVault(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return getDefaultCompound(this, BlockEntityID.VAULT)
                .putCompound(TAG_CONFIG, createConfigTag())
                .putCompound(DATA, createSharedDataTag());
    }

    private void ensureCollections() {
        if (rewardedPlayers == null) {
            rewardedPlayers = new LinkedHashSet<>();
        }
        if (itemsToEject == null) {
            itemsToEject = new ArrayList<>();
        }
        if (connectedPlayers == null) {
            connectedPlayers = new LinkedHashSet<>();
        }
    }

    private static ListTag<StringTag> writeStringSet(Set<String> values) {
        ListTag<StringTag> tags = new ListTag<>();
        for (String value : values) {
            tags.add(new StringTag(value));
        }
        return tags;
    }

    private static ListTag<CompoundTag> writeItemList(List<Item> items) {
        ListTag<CompoundTag> tags = new ListTag<>();
        for (Item item : items) {
            tags.add(NBTIO.putItemHelper(item));
        }
        return tags;
    }

    private CompoundTag createConfigTag() {
        CompoundTag config = new CompoundTag()
                .putString(TAG_LOOT_TABLE, lootTable)
                .putDouble(TAG_ACTIVATION_RANGE, activationRange)
                .putDouble(TAG_DEACTIVATION_RANGE, deactivationRange)
                .putCompound(TAG_KEY_ITEM, NBTIO.putItemHelper(keyItem));
        if (!overrideLootTableToDisplay.isEmpty()) {
            config.putString(TAG_OVERRIDE_LOOT_TABLE_TO_DISPLAY, overrideLootTableToDisplay);
        }
        return config;
    }

    private CompoundTag createSharedDataTag() {
        ensureCollections();
        return new CompoundTag()
                .putCompound(TAG_DISPLAY_ITEM, NBTIO.putItemHelper(displayItem))
                .putList(TAG_CONNECTED_PLAYERS, writeStringSet(connectedPlayers))
                .putDouble(TAG_CONNECTED_PARTICLES_RANGE, connectedParticlesRange);
    }
}
