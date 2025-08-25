package cn.nukkit.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockToItemRegistry implements IRegistry<String, String, String> {

    private static final Object2ObjectOpenHashMap<String, String> BLOCK_TO_ITEM = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, String> ITEM_TO_BLOCK = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Integer, String> LEGACY_ITEM_IDS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, Map<Integer, String>> ITEM_MAPPING = new Object2ObjectOpenHashMap<>();
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;

        try (InputStream stream = BlockToItemRegistry.class.getClassLoader().getResourceAsStream("block_id_to_item_id_map.json")) {
            if (stream == null) {
                throw new RuntimeException("Failed to load block_id_to_item_id_map.json");
            }
            JsonObject object = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            object.entrySet().forEach(entry -> {
                register(entry.getValue().getAsString(), entry.getKey());
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load block_id_to_item_id_map.json");
        }

        try (InputStream stream = BlockToItemRegistry.class.getClassLoader().getResourceAsStream("legacy_item_ids.json")) {
            if (stream == null) {
                throw new RuntimeException("Failed to load runtime_item_states.json");
            }
            JsonObject object = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            object.entrySet().forEach(entry -> {
                LEGACY_ITEM_IDS.put(entry.getValue().getAsInt(), entry.getKey());
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load runtime_item_states.json");
        }

        try (InputStream stream = BlockToItemRegistry.class.getClassLoader().getResourceAsStream("item_mappings.json")) {
            if (stream == null) {
                throw new RuntimeException("Failed to load item_mappings.json");
            }
            JsonObject object = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            object.entrySet().forEach(entry -> {
                JsonObject obj = entry.getValue().getAsJsonObject();
                Map<Integer, String> innerMap = new HashMap<>();

                for (Map.Entry<String, JsonElement> inner : obj.entrySet()) {
                    String key = inner.getKey();
                    if (key.matches("\\d+")) {
                        innerMap.put(Integer.parseInt(key), inner.getValue().getAsString());
                    }
                }

                ITEM_MAPPING.put(entry.getKey(), innerMap);
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load item_mappings.json");
        }
    }

    @Override
    public void register(String key, String value) {
        BLOCK_TO_ITEM.put(key, value);
        ITEM_TO_BLOCK.put(value, key);
    }

    @Override
    public String get(String key) {
        return BLOCK_TO_ITEM.get(key);
    }

    public String get(int id, int damage) {
        String identifier = LEGACY_ITEM_IDS.get(id);
        var mapping = ITEM_MAPPING.get(identifier);
        if (mapping != null) {
            return BLOCK_TO_ITEM.get(mapping.getOrDefault(damage, identifier));
        }
        return BLOCK_TO_ITEM.get(identifier);
    }

    public String getItem(String id) {
        return ITEM_TO_BLOCK.get(id);
    }

    @Override
    public void trim() {
        BLOCK_TO_ITEM.trim();
        ITEM_TO_BLOCK.trim();
        LEGACY_ITEM_IDS.trim();
        ITEM_MAPPING.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        BLOCK_TO_ITEM.clear();
        ITEM_TO_BLOCK.clear();
        LEGACY_ITEM_IDS.clear();
        ITEM_MAPPING.clear();
        init();
    }
}
