package cn.nukkit.registry;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemData;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemGroup;
import cn.nukkit.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreativeItemRegistry implements IRegistry<Integer, CreativeItemRegistry.CreativeItems, CreativeItemRegistry.CreativeItems> {

    private static final Object2ObjectOpenHashMap<Integer, CreativeItems> CREATIVE_ITEMS = new Object2ObjectOpenHashMap<>();

    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    public static final List<Integer> CREATIVE_ITEMS_PROTOCOLS = List.of(
            ProtocolInfo.v1_20_0,
            ProtocolInfo.v1_20_10,
            ProtocolInfo.v1_20_30,
            ProtocolInfo.v1_20_40,
            ProtocolInfo.v1_20_50,
            ProtocolInfo.v1_20_60,
            ProtocolInfo.v1_20_70,
            ProtocolInfo.v1_20_80,
            ProtocolInfo.v1_21_0,
            ProtocolInfo.v1_21_20,
            ProtocolInfo.v1_21_30,
            ProtocolInfo.v1_21_40,
            ProtocolInfo.v1_21_50,
            ProtocolInfo.v1_21_60,
            ProtocolInfo.v1_21_70,
            ProtocolInfo.v1_21_80,
            ProtocolInfo.v1_21_90,
            ProtocolInfo.v1_21_93,
            ProtocolInfo.v1_21_100,
            ProtocolInfo.v1_21_111,
            ProtocolInfo.v1_21_120,
            ProtocolInfo.v1_21_124,
            ProtocolInfo.v1_21_130,
            ProtocolInfo.v1_26_0
    );

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;

        CREATIVE_ITEMS_PROTOCOLS.forEach(protocol -> {
            if (!Server.getInstance().isVersionSupported(protocol)) {
                return;
            }
            try (InputStream stream = CreativeItemRegistry.class.getClassLoader()
                    .getResourceAsStream("gamedata/item/creative/creative_items_" + protocol + ".json")) {
                if (stream == null) {
                    return;
                }

                JsonObject root = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                if (protocol >= ProtocolInfo.v1_21_60) {
                    this.initNewItems(root, protocol);
                }
                this.initOldItems(root, protocol);

            } catch (Exception e) {
                throw new RuntimeException("Failed to load gamedata/item/creative/creative_items_" + protocol + ".json", e);
            }
        });
    }

    private void initNewItems(JsonObject object, int protocol) {
        RuntimeItemMapping mapping = RuntimeItems.getMapping(protocol);

        JsonArray groups = object.getAsJsonObject().get("groups").getAsJsonArray();
        if (groups.isEmpty()) {
            throw new RuntimeException("Creative groups empty");
        }

        int creativeGroupId = 0;
        for (JsonElement element : groups.asList()) {
            JsonObject group = element.getAsJsonObject();

            Item icon = mapping.parseCreativeItem(group.get("icon").getAsJsonObject(), true, protocol);
            if (icon == null) {
                icon = Item.get(ItemNamespaceId.AIR);
            }

            CreativeItemGroup creativeGroup = new CreativeItemGroup(creativeGroupId++,
                    CreativeItemCategory.valueOf(
                            group.get("category").getAsString().toUpperCase(Locale.ROOT)
                    ),
                    group.get("name").getAsString(),
                    icon
            );

            this.register(protocol, creativeGroup);
        }
    }

    private void initOldItems(JsonObject object, int protocol) {
        RuntimeItemMapping mapping = RuntimeItems.getMapping(protocol);

        JsonArray items = object.getAsJsonArray("items");

        int creativeGroupId = 0;
        for (JsonElement element : items) {
            JsonObject item = element.getAsJsonObject();

            String id = item.get("id").getAsString();
            if (!Utils.hasItemOrBlock(id)) {
                continue;
            }

            byte[] nbtBytes;
            if (item.has("nbt_b64")) {
                nbtBytes = Base64.getDecoder().decode(item.get("nbt_b64").getAsString());
            } else if (item.has("nbt_hex")) {
                nbtBytes = Utils.parseHexBinary(item.get("nbt_hex").getAsString());
            } else {
                nbtBytes = new byte[0];
            }

            int damage = item.has("damage") ? item.get("damage").getAsInt() : 0;
            int count = item.has("count") ? item.get("count").getAsInt() : 1;

            Item icon = Item.get(id, damage);
            icon.setCount(count);
            icon.setCompoundTag(nbtBytes);

            CreativeItemGroup creativeGroup = new CreativeItemGroup(
                    creativeGroupId++, CreativeItemCategory.ITEMS, "", icon
            );
            this.register(protocol, creativeGroup);

            // Try to parse the same item through mapping
            Item mappedIcon = mapping.parseCreativeItem(item, true, protocol);
            if (mappedIcon != null && !Item.UNKNOWN_STR.equals(mappedIcon.getName())) {
                CreativeItemGroup targetGroup = null;
                if (protocol >= ProtocolInfo.v1_21_60 && item.has("groupId")) {
                    targetGroup = this.get(protocol).getGroups()
                            .get(item.get("groupId").getAsInt());
                }
                this.register(protocol, mappedIcon, targetGroup);
            }
        }
    }

    @Override
    public void register(Integer protocol, CreativeItems creativeItems) {
        CREATIVE_ITEMS.put(protocol, creativeItems);
    }

    public void register(Integer protocol, CreativeItemGroup group) {
        CREATIVE_ITEMS.computeIfAbsent(protocol, p -> new CreativeItems()).addGroup(group);
    }

    public void register(Integer protocol, Item icon, CreativeItemGroup group) {
        CREATIVE_ITEMS.computeIfAbsent(protocol, p -> new CreativeItems()).add(icon, group);
    }

    public void register(Integer protocol, Item icon, CreativeItemCategory category, String group) {
        CREATIVE_ITEMS.computeIfAbsent(protocol, p -> new CreativeItems()).add(icon, category, group);
    }

    public void register(Integer protocol, Item icon) {
        CREATIVE_ITEMS.computeIfAbsent(protocol, p -> new CreativeItems()).add(icon);
    }

    public void register(CreativeItemGroup group) {
        CREATIVE_ITEMS.keySet().forEach(protocol -> register(protocol, group));
    }

    public void register(Item icon, CreativeItemGroup group) {
        CREATIVE_ITEMS.keySet().forEach(protocol -> register(protocol, icon, group));
    }

    public void register(Item icon) {
        CREATIVE_ITEMS.keySet().forEach(protocol -> register(protocol, icon));
    }

    public void remove(Integer protocol, Item icon) {
        CreativeItems creativeItems = CREATIVE_ITEMS.get(protocol);
        if (creativeItems != null) {
            creativeItems.getItems().remove(icon);
            creativeItems.getContents().remove(icon);
        }
    }

    public void remove(Item icon) {
        CREATIVE_ITEMS.keySet().forEach(protocol -> remove(protocol, icon));
    }

    @Override
    public CreativeItems get(Integer protocol) {
        CreativeItems items = CREATIVE_ITEMS.get(protocol);
        if (items != null) return items;

        return CREATIVE_ITEMS.keySet().stream()
                .filter(p -> p < protocol)
                .max(Comparator.naturalOrder())
                .map(CREATIVE_ITEMS::get)
                .orElse(null);
    }

    public Map<Integer,CreativeItems> getCreativeItems() {
        return Collections.unmodifiableMap(CREATIVE_ITEMS);
    }

    public boolean isCreativeItem(int protocol, Item item) {
        for (Item creativeItem : this.get(protocol).getItems()) {
            if (item.equals(creativeItem, !item.isTool())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void trim() {
        CREATIVE_ITEMS.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        CREATIVE_ITEMS.clear();
        init();
    }

    @Getter
    public static class CreativeItems {

        private final List<CreativeItemGroup> groups = new ArrayList<>();
        private final Map<Item, CreativeItemGroup> contents = new LinkedHashMap<>();

        public void clear() {
            groups.clear();
            contents.clear();
        }

        public void add(Item item) {
            add(item, CreativeItemCategory.ITEMS, ""); // TODO: vanilla items back to correct categories & groups
        }

        public void add(Item item, CreativeItemGroup group) {
            contents.put(item, group);
        }

        public void add(Item item, CreativeItemCategory category, String group) {
            CreativeItemGroup creativeGroup = null;

            for (CreativeItemGroup existing : groups) {
                if (existing.category == category && existing.name.equals(group)) {
                    creativeGroup = existing;
                    break;
                }
            }

            if (creativeGroup == null) {
                creativeGroup = new CreativeItemGroup(groups.size(), category, group, item);
                groups.add(creativeGroup);
            }

            contents.put(item, creativeGroup);
        }

        public void addGroup(CreativeItemGroup creativeGroup) {
            groups.add(creativeGroup);
        }

        public Collection<Item> getItems() {
            return contents.keySet();
        }

        public List<CreativeItemData> getCreativeItemData() {
            int creativeNetId = 1; // 0 is not indexed by client
            var list = new ObjectArrayList<CreativeItemData>(this.getContents().size());
            for (Map.Entry<Item, CreativeItemGroup> entry : this.getContents().entrySet()) {
                list.add(new CreativeItemData(entry.getKey(), creativeNetId++, entry.getValue() != null ? entry.getValue().getGroupId() : 0));
            }
            return list;
        }
    }
}
