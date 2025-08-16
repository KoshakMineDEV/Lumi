package cn.nukkit.utils;

public class DynamicClassLoader extends ClassLoader {

    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}