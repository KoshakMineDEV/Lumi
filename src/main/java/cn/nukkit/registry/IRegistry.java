package cn.nukkit.registry;

public interface IRegistry<K, V, R> {

    void init();

    void register(K key, R value);

    V get(K key);

    void trim();

    void reload();
}
