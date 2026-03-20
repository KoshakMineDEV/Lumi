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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreativeItemRegistry implements IRegistry<Integer, CreativeItemRegistry.CreativeItems, CreativeItemRegistry.CreativeItems> {

    private static final CreativeItems CREATIVE_ITEMS = new CreativeItems();

    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;

        if (!Server.getInstance().isVersionSupported(ProtocolInfo.CURRENT_PROTOCOL)) {
            return;
        }
        try (InputStream stream = CreativeItemRegistry.class.getClassLoader()
                .getResourceAsStream("gamedata/item/creative_items.json")) {
            if (stream == null) {
                return;
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            this.parseNewItems(root);
            this.parseOldItems(root);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load gamedata/item/creative_items.json", e);
        }
    }

    private void parseNewItems(JsonObject object) {
        RuntimeItemMapping mapping = RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL);

        JsonArray groups = object.getAsJsonObject().get("groups").getAsJsonArray();
        if (groups.isEmpty()) {
            throw new RuntimeException("Creative groups empty");
        }

        int creativeGroupId = 0;
        for (JsonElement element : groups.asList()) {
            JsonObject group = element.getAsJsonObject();

            Item icon = mapping.parseCreativeItem(group.get("icon").getAsJsonObject(), true, ProtocolInfo.CURRENT_PROTOCOL);
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

            this.register(creativeGroup);
        }
    }

    private void parseOldItems(JsonObject object) {
        RuntimeItemMapping mapping = RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL);

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
            this.register(creativeGroup);

            // Try to parse the same item through mapping
            Item mappedIcon = mapping.parseCreativeItem(item, true, ProtocolInfo.CURRENT_PROTOCOL);
            if (mappedIcon != null && !Item.UNKNOWN_STR.equals(mappedIcon.getName())) {
                CreativeItemGroup targetGroup = null;
                if (item.has("groupId")) {
                    targetGroup = CREATIVE_ITEMS.getGroups()
                            .get(item.get("groupId").getAsInt());
                }
                this.register(mappedIcon, targetGroup);
            }
        }
    }

    @Override
    @Deprecated
    public void register(Integer key, CreativeItems value) {
        //Do nothing
    }

    public void register(CreativeItemGroup group) {
        CREATIVE_ITEMS.addGroup(group);
    }

    public void register(Item icon) {
        CREATIVE_ITEMS.add(icon);
    }

    public void register(Item icon, CreativeItemGroup group) {
        CREATIVE_ITEMS.add(icon, group);
    }


    public void remove(Item icon) {
        CREATIVE_ITEMS.getItems().remove(icon);
        CREATIVE_ITEMS.getContents().remove(icon);
    }

    @Override
    @Deprecated
    public CreativeItems get(Integer protocol) {
        return get();
    }

    public CreativeItems get() {
        return CREATIVE_ITEMS;
    }

    public boolean isCreativeItem(Item item) {
        for (Item creativeItem : CREATIVE_ITEMS.getItems()) {
            if (item.equals(creativeItem, !item.isTool())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void trim() {
        //Do nothing
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

        public List<CreativeItemData> getCreativeItemData(int protocol) {
            int creativeNetId = 1;
            ObjectArrayList<CreativeItemData> list = new ObjectArrayList<>();
            for (Map.Entry<Item, CreativeItemGroup> entry : contents.entrySet()) {
                if (entry.getKey().isSupportedOn(protocol)) {
                    list.add(new CreativeItemData(
                            entry.getKey(), creativeNetId++,
                            entry.getValue() != null ? entry.getValue().getGroupId() : 0
                    ));
                }
            }
            return list;
        }
    }
}
