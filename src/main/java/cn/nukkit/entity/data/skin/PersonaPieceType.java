package cn.nukkit.entity.data.skin;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Persona (character creator) piece type. Ordinals align with the Bedrock v2168 protocol.
 *
 * <p>Adapted from NukkitPetteriM1Edition and CloudburstMC Protocol.</p>
 */
public enum PersonaPieceType {

    UNKNOWN("unknown", "persona_unknown"),
    SKELETON("skeleton", "persona_skeleton"),
    BODY("body", "persona_body"),
    SKIN("skin", "persona_skin"),
    BOTTOM("bottom", "persona_bottom"),
    FEET("feet", "persona_feet"),
    DRESS("dress", "persona_dress"),
    TOP("top", "persona_top"),
    HIGH_PANTS("high_pants", "persona_high_pants"),
    HANDS("hands", "persona_hand"),
    OUTERWEAR("outerwear", "persona_outerwear"),
    FACIAL_HAIR("facialhair", "persona_facial_hair"),
    MOUTH("mouth", "persona_mouth"),
    EYES("eyes", "persona_eyes"),
    HAIR("hair", "persona_hair"),
    HOOD("hood", "persona_hood"),
    BACK("back", "persona_back"),
    FACE_ACCESSORY("faceaccessory", "persona_face_accessory"),
    HEAD("head", "persona_head"),
    LEGS("legs", "persona_legs"),
    LEFT_LEG("leftleg", "persona_left_leg"),
    RIGHT_LEG("rightleg", "persona_right_leg"),
    ARMS("arms", "persona_arms"),
    LEFT_ARM("leftarm", "persona_left_arm"),
    RIGHT_ARM("rightarm", "persona_right_arm"),
    CAPES("capes", "persona_capes"),
    CLASSIC_SKIN("classicskin", "persona_classic_skin"),
    EMOTE("emote", "persona_emote"),
    COCO("coco", "persona_coco"),
    UNSUPPORTED("unsupported", "unsupported");

    private static final Map<String, PersonaPieceType> BY_NAME = new HashMap<>(values().length * 2, 1);

    static {
        for (PersonaPieceType value : values()) {
            BY_NAME.put(value.serializeName, value);
            BY_NAME.put(value.type, value);
        }
    }

    @Getter
    private final String serializeName;

    @Getter
    private final String type;

    PersonaPieceType(String serializeName, String type) {
        this.serializeName = serializeName;
        this.type = type;
    }

    /**
     * Looks up either the short serialized name or the prefixed persona type.
     */
    public static PersonaPieceType fromName(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        return BY_NAME.getOrDefault(name, UNKNOWN);
    }

    /**
     * Looks up a protocol ordinal and safely falls back for malformed data.
     */
    public static PersonaPieceType fromOrdinal(int ordinal) {
        PersonaPieceType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : UNKNOWN;
    }
}