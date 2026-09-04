package cn.nukkit.entity.ai.memory;

import cn.nukkit.entity.ai.memory.MemoryStorage;
import cn.nukkit.entity.ai.memory.MemoryType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Server-tick-owned implementation of {@link MemoryStorage}.
 *
 * @author daoge_cmd
 */
public class MemoryStorageImpl implements MemoryStorage {

    private static final Object ABSENT = new Object();

    private final Object2ObjectOpenHashMap<MemoryType<?>, Object> storage = new Object2ObjectOpenHashMap<>();
    private final Map<MemoryType<?>, Object> unmodifiableStorage = Collections.unmodifiableMap(storage);

    @Override
    public <T> void put(MemoryType<T> type, T value) {
        storage.put(type, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(MemoryType<T> type) {
        Object value = storage.getOrDefault(type, ABSENT);
        if (value == ABSENT) {
            // Not present in map, return default
            var defaultData = type.defaultData().get();
            if (defaultData != null) {
                storage.put(type, defaultData);
            }
            return defaultData;
        }
        return (T) value;
    }

    @Override
    @UnmodifiableView
    public Map<MemoryType<?>, Object> getAll() {
        return unmodifiableStorage;
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public <T> void clear(MemoryType<T> type) {
        storage.remove(type);
    }

    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    @Override
    public <T> boolean isEmpty(MemoryType<T> type) {
        return storage.get(type) == null;
    }

    @Override
    public <T> boolean putIfAbsent(MemoryType<T> type, T value) {
        if (storage.get(type) != null) {
            return false;
        }
        storage.put(type, value);
        return true;
    }

    @Override
    public <T> boolean compareDataTo(MemoryType<T> type, T value) {
        return Objects.equals(get(type), value);
    }
}
