package cn.nukkit.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
@Getter
public class Identifier {

    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String DEFAULT_NAMESPACE = "minecraft";

    private final String namespace;
    private final String path;

    protected Identifier(String[] id) {
        this.namespace = id[0].isEmpty() ? DEFAULT_NAMESPACE : id[0];
        this.path = id[1];
        if (!Identifier.isNamespaceValid(this.namespace)) {
            throw new InvalidIdentifierException("Non [a-z0-9_.-] character in namespace of location: " + this.namespace + ":" + this.path);
        }
        if (!Identifier.isPathValid(this.path)) {
            throw new InvalidIdentifierException("Non [a-z0-9/._-] character in path of location: " + this.namespace + ":" + this.path);
        }
    }

    public Identifier(String id) {
        this(Identifier.split(id, NAMESPACE_SEPARATOR));
    }

    public Identifier(String namespace, String path) {
        this(new String[]{namespace, path});
    }

    public static @Nullable Identifier tryParse(String id) {
        try {
            return new Identifier(id);
        } catch (InvalidIdentifierException lv) {
            return null;
        }
    }

    public static @Nullable Identifier of(String namespace, String path) {
        try {
            return new Identifier(namespace, path);
        } catch (InvalidIdentifierException e) {
            return null;
        }
    }

    private static boolean isPathValid(String path) {
        for (int i = 0; i < path.length(); ++i) {
            if (Identifier.isPathCharacterValid(path.charAt(i))) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isNamespaceValid(String namespace) {
        for (int i = 0; i < namespace.length(); ++i) {
            if (Identifier.isNamespaceCharacterValid(namespace.charAt(i))) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean isPathCharacterValid(char character) {
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '/' || character == '.';
    }

    private static boolean isNamespaceCharacterValid(char character) {
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
    }

    public static boolean isValid(String id) {
        String[] strings = Identifier.split(id, NAMESPACE_SEPARATOR);
        return Identifier.isNamespaceValid(strings[0].isEmpty() ? DEFAULT_NAMESPACE : strings[0]) && Identifier.isPathValid(strings[1]);
    }

    public static void assertValid(String id) {
        String[] strings = Identifier.split(id, NAMESPACE_SEPARATOR);
        var namespace = strings[0].isEmpty() ? DEFAULT_NAMESPACE : strings[0];
        var path = strings[1];
        if (!Identifier.isNamespaceValid(namespace)) {
            throw new InvalidIdentifierException("Non [a-z0-9_.-] character in namespace of location: " + namespace + ":" + path);
        }
        if (!Identifier.isPathValid(path)) {
            throw new InvalidIdentifierException("Non [a-z0-9/._-] character in path of location: " + namespace + ":" + path);
        }
    }

    private static String[] split(String id, String delimiter) {
        String[] strings = new String[]{DEFAULT_NAMESPACE, id};
        int i = id.indexOf(delimiter);
        if (i >= 0) {
            strings[1] = id.substring(i + 1);
            if (i >= 1) {
                strings[0] = id.substring(0, i);
            }
        }
        return strings;
    }

    @Override
    public String toString() {
        return this.namespace + NAMESPACE_SEPARATOR + this.path;
    }
}

