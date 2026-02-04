package cn.nukkit.item;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@UtilityClass
public class RuntimeItems {

    private static final Map<String, Integer> legacyString2LegacyInt = new HashMap<>();
    private final Map<Integer, Map<String, String>> downgradeMappings = new HashMap<>();
    private final Gson gson = new Gson();

    private static RuntimeItemMapping mapping589;
    private static RuntimeItemMapping mapping594;
    private static RuntimeItemMapping mapping618;
    private static RuntimeItemMapping mapping630;
    private static RuntimeItemMapping mapping649;
    private static RuntimeItemMapping mapping662;
    private static RuntimeItemMapping mapping671;
    private static RuntimeItemMapping mapping685;
    private static RuntimeItemMapping mapping712;
    private static RuntimeItemMapping mapping729;
    private static RuntimeItemMapping mapping748;
    private static RuntimeItemMapping mapping766;
    private static RuntimeItemMapping mapping776;
    private static RuntimeItemMapping mapping786;
    private static RuntimeItemMapping mapping800;
    private static RuntimeItemMapping mapping818;
    private static RuntimeItemMapping mapping819;
    private static RuntimeItemMapping mapping827;
    private static RuntimeItemMapping mapping844;
    private static RuntimeItemMapping mapping859;
    private static RuntimeItemMapping mapping898;

    public static RuntimeItemMapping[] VALUES;

    private static boolean initialized;

    public static void init() {
        if (initialized) {
            throw new IllegalStateException("RuntimeItems were already generated!");
        }
        initialized = true;
        log.debug("Loading runtime items...");
        InputStream itemIdsStream = Server.class.getClassLoader().getResourceAsStream("internal/legacy_item_ids.json");
        if (itemIdsStream == null) {
            throw new AssertionError("Unable to load legacy_item_ids.json");
        }

        JsonObject json = JsonParser.parseReader(new InputStreamReader(itemIdsStream)).getAsJsonObject();
        for (String identifier : json.keySet()) {
            legacyString2LegacyInt.put(identifier, json.get(identifier).getAsInt());
        }

        InputStream mappingStream = Server.class.getClassLoader().getResourceAsStream("internal/item_mappings.json");
        if (mappingStream == null) {
            throw new AssertionError("Unable to load item_mappings.json");
        }
        JsonObject itemMapping = JsonParser.parseReader(new InputStreamReader(mappingStream)).getAsJsonObject();

        Map<String, MappingEntry> mappingEntries = new HashMap<>();
        for (String legacyName : itemMapping.keySet()) {
            JsonObject convertData = itemMapping.getAsJsonObject(legacyName);
            int protocol = 0;
            try {
                protocol = convertData.get("protocol").getAsInt();
            } catch (Exception ignored) {

            }
            for (String key : convertData.keySet()) {
                if ("protocol".equalsIgnoreCase(key)) {
                    continue;
                }
                String identifier = convertData.get(key).getAsString();
                int damage = Integer.parseInt(key);
                mappingEntries.put(identifier, new MappingEntry(legacyName, damage, protocol));
            }
        }

        mapping589 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_20_0);
        mapping594 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_20_10);
        mapping618 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_20_30);
        mapping630 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_20_50);
        mapping649 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_20_60);
        mapping662 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_20_70);
        mapping671 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_20_80);
        mapping685 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_0);
        mapping712 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_20);
        mapping729 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_30);
        mapping748 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_40);
        mapping766 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_50);
        mapping776 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_60);
        mapping786 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_70);
        mapping800 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_80);
        mapping818 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_90);
        mapping819 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_93);
        mapping827 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_100);
        mapping844 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_111);
        mapping859 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_120);
        mapping898 = new RuntimeItemMapping(mappingEntries, ProtocolInfo.v1_21_130);

        VALUES = new RuntimeItemMapping[]{
                mapping589,
                mapping594,
                mapping618,
                mapping630,
                mapping649,
                mapping662,
                mapping671,
                mapping685,
                mapping712,
                mapping729,
                mapping748,
                mapping766,
                mapping776,
                mapping786,
                mapping800,
                mapping818,
                mapping819,
                mapping827,
                mapping844,
                mapping859,
                mapping898
        };

        for (int protocol : ProtocolInfo.SUPPORTED_PROTOCOLS) {
            String resource = "internal/downgrade/" + protocol + ".json";
            try (InputStream is = Server.class.getClassLoader().getResourceAsStream(resource)) {
                if (is != null) {
                    Type type = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> mappings = gson.fromJson(new InputStreamReader(is), type);
                    if (mappings != null) {
                        downgradeMappings.put(protocol, mappings);
                    }
                }
            } catch (Exception e) {
                //Do nothing because not all protocols should have mappings
            }
        }
    }

    public static String downgradeIdentifier(String identifier, int targetProtocol) {
        String currentId = identifier;

        for (int protocol : ProtocolInfo.SUPPORTED_PROTOCOLS.reversed()) {
            if (protocol >= targetProtocol) {
                Map<String, String> mapping = downgradeMappings.get(protocol);
                if (mapping != null && mapping.containsKey(currentId)) {
                    currentId = mapping.get(currentId);
                }
            }
        }

        return currentId.equals(identifier) ? null : currentId;
    }


    public static RuntimeItemMapping getMapping(int protocolId) {
        if (protocolId >= ProtocolInfo.v1_21_130) {
            return mapping898;
        } else if (protocolId >= ProtocolInfo.v1_21_120) {
            return mapping859;
        } else if (protocolId >= ProtocolInfo.v1_21_110_26) {
            return mapping844;
        } else if (protocolId >= ProtocolInfo.v1_21_100) {
            return mapping827;
        } else if (protocolId >= ProtocolInfo.v1_21_93) {
            return mapping819;
        } else if (protocolId >= ProtocolInfo.v1_21_90) {
            return mapping818;
        } else if (protocolId >= ProtocolInfo.v1_21_80) {
            return mapping800;
        } else if (protocolId >= ProtocolInfo.v1_21_70_24) {
            return mapping786;
        } else if (protocolId >= ProtocolInfo.v1_21_60) {
            return mapping776;
        } else if (protocolId >= ProtocolInfo.v1_21_50_26) {
            return mapping766;
        } else if (protocolId >= ProtocolInfo.v1_21_40) {
            return mapping748;
        } else if (protocolId >= ProtocolInfo.v1_21_30) {
            return mapping729;
        } else if (protocolId >= ProtocolInfo.v1_21_20) {
            return mapping712;
        } else if (protocolId >= ProtocolInfo.v1_21_0) {
            return mapping685;
        } else if (protocolId >= ProtocolInfo.v1_20_80) {
            return mapping671;
        } else if (protocolId >= ProtocolInfo.v1_20_70) {
            return mapping662;
        } else if (protocolId >= ProtocolInfo.v1_20_60) {
            return mapping649;
        } else if (protocolId >= ProtocolInfo.v1_20_50) {
            return mapping630;
        } else if (protocolId >= ProtocolInfo.v1_20_30_24) {
            return mapping618;
        } else if (protocolId >= ProtocolInfo.v1_20_10_21) {
            return mapping594;
        } else if (protocolId >= ProtocolInfo.v1_20_0_23) {
            return mapping589;
        }

        throw new RuntimeException("Tried to get unsupported protocol item mapping: " + protocolId);
    }

    public static int getLegacyIdFromLegacyString(String identifier) {
        return legacyString2LegacyInt.getOrDefault(identifier, -1);
    }

    @Data
    public static class MappingEntry {
        private final String legacyName;
        private final int damage;
        private final int protocol;
    }

    public static int getId(int fullId) {
        return (short) (fullId >> 16);
    }

    public static int getData(int fullId) {
        return ((fullId >> 1) & 0x7fff);
    }

    public static int getFullId(int id, int data) {
        return (((short) id) << 16) | ((data & 0x7fff) << 1);
    }

    public static int getNetworkId(int networkFullId) {
        return networkFullId >> 1;
    }

    public static boolean hasData(int id) {
        return (id & 0x1) != 0;
    }

    @Deprecated
    @ToString
    @RequiredArgsConstructor
    static class Entry {
        String name;
        int id;
        Integer oldId;
        Integer oldData;
    }
}
