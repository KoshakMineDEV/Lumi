package cn.nukkit.utils;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.data.skin.PersonaPiece;
import cn.nukkit.entity.data.skin.PersonaPieceTint;
import cn.nukkit.entity.data.skin.PersonaPieceType;
import cn.nukkit.entity.data.skin.Skin;
import cn.nukkit.entity.data.skin.SkinAnimation;
import cn.nukkit.item.*;
import cn.nukkit.item.RuntimeItemMapping.LegacyEntry;
import cn.nukkit.item.RuntimeItemMapping.RuntimeEntry;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.EntityLink;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.TextProcessingEventOrigin;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.*;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.*;

import static org.cloudburstmc.protocol.common.util.Preconditions.checkArgument;

/**
 * BinaryStream
 *
 * @author MagicDroidX
 * Nukkit Project
 */
public class BinaryStream {

    public int offset;
    private byte[] buffer;
    protected int count;

    private static final int MAX_ARRAY_SIZE = 2147483639;

    public BinaryStream() {
        this.buffer = new byte[32];
        this.offset = 0;
        this.count = 0;
    }

    public BinaryStream(int initialCapacity) {
        this.buffer = new byte[initialCapacity];
        this.offset = 0;
        this.count = 0;
    }

    public BinaryStream(byte[] buffer) {
        this(buffer, 0);
    }

    public BinaryStream(byte[] buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
        this.count = buffer.length;
    }

    public void reuse() {
        this.offset = 0;
        this.count = 0;
    }

    public BinaryStream reset() {
        this.offset = 0;
        this.count = 0;
        return this;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
        this.count = buffer == null ? -1 : buffer.length;
    }

    public void setBuffer(byte[] buffer, int offset) {
        this.setBuffer(buffer);
        this.setOffset(offset);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getBuffer() {
        return Arrays.copyOf(buffer, count);
    }

    public byte[] getBufferUnsafe() {
        return buffer;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public byte[] get() {
        return this.get(this.count - this.offset);
    }

    public byte[] get(int len) {
        if (len < 0) {
            this.offset = this.count - 1;
            return new byte[0];
        }
        len = Math.min(len, this.count - this.offset);
        this.offset += len;
        return Arrays.copyOfRange(this.buffer, this.offset - len, this.offset);
    }

    public void skip(int len) {
        if (len <= 0) {
            return;
        }
        this.offset += Math.min(len, this.count - this.offset);
    }

    public void put(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }

        this.ensureCapacity(this.count + bytes.length);

        System.arraycopy(bytes, 0, this.buffer, this.count, bytes.length);
        this.count += bytes.length;
    }

    public long getLong() {
        return Binary.readLong(this.get(8));
    }

    public void putLong(long l) {
        this.put(Binary.writeLong(l));
    }

    public int getInt() {
        return Binary.readInt(this.get(4));
    }

    public void putInt(int i) {
        this.put(Binary.writeInt(i));
    }

    public long getLLong() {
        return Binary.readLLong(this.get(8));
    }

    public void putLLong(long l) {
        this.put(Binary.writeLLong(l));
    }

    public int getLInt() {
        return Binary.readLInt(this.get(4));
    }

    public void putLInt(int i) {
        this.put(Binary.writeLInt(i));
    }

    public int getShort() {
        return Binary.readShort(this.get(2));
    }

    public void putShort(int s) {
        this.put(Binary.writeShort(s));
    }

    public int getLShort() {
        return Binary.readLShort(this.get(2));
    }

    public void putLShort(int s) {
        this.put(Binary.writeLShort(s));
    }

    public float getFloat() {
        return getFloat(-1);
    }

    public float getFloat(int accuracy) {
        return Binary.readFloat(this.get(4), accuracy);
    }

    public void putFloat(float v) {
        this.put(Binary.writeFloat(v));
    }

    public float getLFloat() {
        return getLFloat(-1);
    }

    public float getLFloat(int accuracy) {
        return Binary.readLFloat(this.get(4), accuracy);
    }

    public void putLFloat(float v) {
        this.put(Binary.writeLFloat(v));
    }

    public int getTriad() {
        return Binary.readTriad(this.get(3));
    }

    public void putTriad(int triad) {
        this.put(Binary.writeTriad(triad));
    }

    public int getLTriad() {
        return Binary.readLTriad(this.get(3));
    }

    public void putLTriad(int triad) {
        this.put(Binary.writeLTriad(triad));
    }

    public boolean getBoolean() {
        return this.getByte() == 0x01;
    }

    public void putBoolean(boolean bool) {
        this.putByte((byte) (bool ? 1 : 0));
    }

    public byte getSingedByte() {
        return this.buffer[this.offset++];
    }

    public int getByte() {
        return this.buffer[this.offset++] & 0xff;
    }

    public void putByte(byte b) {
        this.put(new byte[]{b});
    }

    public void putByte(int b) {
        putByte((byte) b);
    }

    /**
     * Reads a list of Attributes from the stream.
     *
     * @return Attribute[]
     */
    public Attribute[] getAttributeList() throws Exception {
        List<Attribute> list = new ArrayList<>();
        long count = this.getUnsignedVarInt();

        for (int i = 0; i < count; ++i) {
            String name = this.getString();
            Attribute attr = Attribute.getAttributeByName(name);
            if (attr != null) {
                attr.setMinValue(this.getLFloat());
                attr.setValue(this.getLFloat());
                attr.setMaxValue(this.getLFloat());
                list.add(attr);
            } else {
                throw new Exception("Unknown attribute type \"" + name + '"');
            }
        }

        return list.toArray(new Attribute[0]);
    }

    /**
     * Writes a list of Attributes to the packet buffer using the standard format.
     */
    public void putAttributeList(Attribute[] attributes) {
        this.putUnsignedVarInt(attributes.length);
        for (Attribute attribute : attributes) {
            this.putString(attribute.getName());
            this.putLFloat(attribute.getMinValue());
            this.putLFloat(attribute.getValue());
            this.putLFloat(attribute.getMaxValue());
        }
    }

    public void putUUID(UUID uuid) {
        this.put(Binary.writeUUID(uuid));
    }

    public UUID getUUID() {
        return Binary.readUUID(this.get(16));
    }

    public void putSkin(Skin skin) {
        Server.mvw("BinaryStream#putSkin(Skin)");
        this.putSkin(ProtocolInfo.CURRENT_PROTOCOL, skin);
    }

    private static byte[] steveSkinDecoded;

    public void putSkin(int protocol, Skin skin) {
        this.putString(skin.getSkinId());

        this.putString(skin.getPlayFabId());
        this.putString(skin.getSkinResourcePatch());
        this.putImage(skin.getSkinData());

        List<SkinAnimation> animations = skin.getAnimations();
        if (protocol >= ProtocolInfo.v1_26_40) {
            this.putUnsignedVarInt(animations.size());
        } else {
            this.putLInt(animations.size());
        }
        for (SkinAnimation animation : animations) {
            this.putImage(animation.image);
            if (protocol >= ProtocolInfo.v1_26_40) {
                this.putUnsignedVarInt(animation.type);
                this.putLFloat(animation.frames);
                this.putUnsignedVarInt(animation.expression);
            } else {
                this.putLInt(animation.type);
                this.putLFloat(animation.frames);
                this.putLInt(animation.expression);
            }
        }

        this.putImage(skin.getCapeData());
        this.putString(skin.getGeometryData());
        this.putString(skin.getGeometryDataEngineVersion());
        this.putString(skin.getAnimationData());
        this.putString(skin.getCapeId());
        this.putString(skin.getFullSkinId());
        boolean v2168 = protocol >= ProtocolInfo.v1_26_40;
        if (v2168) {
            this.putByte((byte) ("wide".equalsIgnoreCase(skin.getArmSize()) ? 1 : 0));
            this.putLInt(skinColorToInt(skin.getSkinColor()));
        } else {
            this.putString(skin.getArmSize());
            this.putString(skin.getSkinColor());
        }

        List<PersonaPiece> pieces = skin.getPersonaPieces();
        if (v2168) {
            this.putUnsignedVarInt(pieces.size());
        } else {
            this.putLInt(pieces.size());
        }
        for (PersonaPiece piece : pieces) {
            this.putString(piece.id);
            if (v2168) {
                this.putLInt(piece.type.ordinal());
                this.putUUID(piece.packId);
            } else {
                this.putString(piece.type.getSerializeName());
                this.putString(piece.packId.toString());
            }
            this.putBoolean(piece.isDefault);
            this.putString(piece.productId);
        }

        List<PersonaPieceTint> tints = skin.getTintColors();
        if (v2168) {
            this.putUnsignedVarInt(tints.size());
        } else {
            this.putLInt(tints.size());
        }
        for (PersonaPieceTint tint : tints) {
            this.putString(tint.pieceType.getSerializeName());
            if (v2168) {
                for (int i = 0; i < 4; i++) {
                    this.putLInt(i < tint.colors.size() ? skinColorToInt(tint.colors.get(i)) : 0);
                }
            } else {
                List<String> colors = tint.colors;
                this.putLInt(colors.size());
                for (String color : colors) {
                    this.putString(color);
                }
            }
        }

        this.putBoolean(skin.isPremium());
        this.putBoolean(skin.isPersona());
        this.putBoolean(skin.isCapeOnClassic());
        this.putBoolean(skin.isPrimaryUser());
        this.putBoolean(skin.isOverridingPlayerAppearance());
        if (v2168) {
            this.putString(Boolean.toString(skin.isTrusted()));
            this.putString(skin.getProfileHash());
        }
    }

    public void putImage(SerializedImage image) {
        this.putLInt(image.width);
        this.putLInt(image.height);
        this.putByteArray(image.data);
    }

    // v2168 skin field-type conversion helpers
    private static int skinColorToInt(String color) {
        // Accept hexadecimal RGB/RGBA colors and small numeric values; malformed values become zero.
        if (color == null || color.isEmpty()) {
            return 0;
        }
        String hex = color.startsWith("#") ? color.substring(1) : color;
        try {
            int rgba;
            if (hex.length() <= 4) {
                // Preserve small numeric color values used by some persona payloads.
                return Integer.parseInt(color.startsWith("#") ? hex : color);
            }
            if (hex.length() == 6) {
                rgba = (int) (Long.parseLong(hex, 16) | 0xFF000000L);
            } else {
                rgba = (int) Long.parseLong(hex, 16);
            }
            return rgba;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String intToSkinColor(int argb) {
        return String.format("#%08X", argb);
    }

    public SerializedImage getImage() {
        int width = this.getLInt();
        int height = this.getLInt();
        byte[] data = this.getByteArray();
        return new SerializedImage(width, height, data);
    }

    public SerializedImage getImage(int maxSize) {
        int width = this.getLInt();
        int height = this.getLInt();
        byte[] data = this.getByteArray(maxSize);
        return new SerializedImage(width, height, data);
    }

    public Skin getSkin() {
        Server.mvw("BinaryStream#getSkin()");
        return getSkin(ProtocolInfo.CURRENT_PROTOCOL);
    }

    public Skin getSkin(int protocol) { // Can be used only with protocol >= 388
        Skin skin = new Skin();
        skin.setSkinId(this.getString());
        skin.setPlayFabId(this.getString());
        skin.setSkinResourcePatch(this.getString());
        skin.setSkinData(this.getImage(Skin.SKIN_PERSONA_SIZE));

        int animationCount = protocol >= ProtocolInfo.v1_26_40 ? (int) this.getUnsignedVarInt() : this.getLInt();
        for (int i = 0; i < Math.min(animationCount, 128); i++) {
            SerializedImage image = this.getImage(Skin.SKIN_128_128_SIZE);
            int type;
            float frames;
            int expression;
            if (protocol >= ProtocolInfo.v1_26_40) {
                type = (int) this.getUnsignedVarInt();
                frames = this.getLFloat();
                expression = (int) this.getUnsignedVarInt();
            } else {
                type = this.getLInt();
                frames = this.getLFloat();
                expression = this.getLInt();
            }
            skin.getAnimations().add(new SkinAnimation(image, type, frames, expression));
        }

        skin.setCapeData(this.getImage(Skin.SINGLE_SKIN_SIZE));
        skin.setGeometryData(this.getString());
        skin.setGeometryDataEngineVersion(this.getString());
        skin.setAnimationData(this.getString());
        skin.setCapeId(this.getString());
        skin.setFullSkinId(this.getString());
        boolean v2168 = protocol >= ProtocolInfo.v1_26_40;
        if (v2168) {
            skin.setArmSize(this.getByte() != 0 ? "wide" : "slim");
            skin.setSkinColor(intToSkinColor(this.getLInt()));
        } else {
            skin.setArmSize(this.getString());
            skin.setSkinColor(this.getString());
        }

        int piecesLength = v2168 ? (int) this.getUnsignedVarInt() : this.getLInt();
        for (int i = 0; i < Math.min(piecesLength, 128); i++) {
            String pieceId = this.getString();
            String pieceType;
            String packId;
            if (v2168) {
                pieceType = PersonaPieceType.fromOrdinal(this.getLInt()).getSerializeName();
                packId = this.getUUID().toString();
            } else {
                pieceType = this.getString();
                packId = this.getString();
            }
            boolean isDefault = this.getBoolean();
            String productId = this.getString();
            skin.getPersonaPieces().add(new PersonaPiece(pieceId, pieceType, packId, isDefault, productId));
        }

        int tintsLength = v2168 ? (int) this.getUnsignedVarInt() : this.getLInt();
        for (int i = 0; i < Math.min(tintsLength, 128); i++) {
            String pieceType = this.getString();
            List<String> colors = new ArrayList<>();
            if (v2168) {
                for (int i2 = 0; i2 < 4; i2++) {
                    colors.add(intToSkinColor(this.getLInt()));
                }
            } else {
                int colorsLength = this.getLInt();
                for (int i2 = 0; i2 < Math.min(colorsLength, 128); i2++) {
                    colors.add(this.getString());
                }
            }
            skin.getTintColors().add(new PersonaPieceTint(pieceType, colors));
        }

        skin.setPremium(this.getBoolean());
        skin.setPersona(this.getBoolean());
        skin.setCapeOnClassic(this.getBoolean());
        skin.setPrimaryUser(this.getBoolean());
        skin.setOverridingPlayerAppearance(this.getBoolean());
        if (v2168) {
            skin.setTrusted(Boolean.parseBoolean(this.getString()));
            skin.setProfileHash(this.getString());
        }
        return skin;
    }

    private static final String MV_ORIGIN_NBT = "mv_origin_nbt";
    private static final String MV_ORIGIN_ID = "mv_origin_id";
    private static final String MV_ORIGIN_NAMESPACE = "mv_origin_namespace";
    private static final String MV_ORIGIN_META = "mv_origin_meta";

    public Item getSlot() {
        Server.mvw("BinaryStream#getSlot()");
        return this.getSlot(ProtocolInfo.CURRENT_PROTOCOL);
    }

    public Item getSlot(int protocolId) {
        if (protocolId >= ProtocolInfo.v1_26_40) {
            return this.getNetworkItemStackDescriptor(protocolId, false);
        }
        int runtimeId = this.getVarInt();
        if (runtimeId == 0) {
            return Item.get(Item.AIR, 0, 0);
        }

        int cnt = this.getLShort();
        int damage = (int) this.getUnsignedVarInt();

        RuntimeItemMapping mapping = RuntimeItems.getMapping(protocolId);


        Integer id = null;
        String stringId = null;
        try {
            LegacyEntry legacyEntry = mapping.fromRuntime(runtimeId);
            id = legacyEntry.getLegacyId();
            if (legacyEntry.isHasDamage()) {
                damage = legacyEntry.getDamage();
            }
        } catch (IllegalArgumentException e) {

        }

        if (id == null || !Utils.hasItemOrBlock(id)) {
            stringId = mapping.getNamespacedIdByNetworkId(runtimeId);
            if (stringId == null) {
                throw new IllegalArgumentException("Unknown item: runtimeID=" + runtimeId + " protocol=" + protocolId);
            }
            stringId = stringId + ":" + damage;
            id = null;
        }

        int stackNetId = 0;
        if (this.getBoolean()) { // hasNetId
            stackNetId = this.getVarInt();
        }

        this.getVarInt(); // blockRuntimeId

        //TODO 在1.21.30会得到错误数据

        BinaryStream stream = new BinaryStream(this.getByteArray());

        byte[] nbt = new byte[0];
        String[] canPlace;
        String[] canBreak;

        try {
            int nbtSize = (short) stream.getLShort();

            CompoundTag compoundTag = null;
            if (nbtSize > 0) {
                compoundTag = readItemUserDataNbt(stream);
            } else if (nbtSize == -1) {
                int tagCount = stream.getByte();
                if (tagCount != 1) throw new IllegalArgumentException("Expected 1 tag but got " + tagCount);
                compoundTag = readItemUserDataNbt(stream);
            }

            if (compoundTag != null && !compoundTag.getAllTags().isEmpty()) {
                if (compoundTag.contains("Damage")) {
                    if (stringId != null || id > 255) {
                        damage = compoundTag.getInt("Damage");
                    }
                    compoundTag.remove("Damage");
                }
                if (compoundTag.contains("__DamageConflict__")) {
                    compoundTag.put("Damage", compoundTag.removeAndGet("__DamageConflict__"));
                }
                if (compoundTag.containsList("ench")) {
                    int enchCount = compoundTag.getList("ench", CompoundTag.class).getAll().size();
                    if (enchCount > Enchantment.getEnchantments().length * 1.5) {
                        throw new RuntimeException("Too many enchantment: " + enchCount);
                    }
                }
                if (!compoundTag.isEmpty()) {
                    nbt = NBTIO.write(compoundTag, ByteOrder.LITTLE_ENDIAN);
                }
            }

            int canPlaceCount = stream.getLInt();
            if (canPlaceCount > 4096) {
                throw new RuntimeException("Too many CanPlaceOn blocks: " + canPlaceCount);
            }

            canPlace = new String[canPlaceCount];
            for (int i = 0; i < canPlace.length; i++) {
                canPlace[i] = getItemUserDataString(stream);
            }

            int canBreakCount = stream.getLInt();
            if (canBreakCount > 4096) {
                throw new RuntimeException("Too many CanDestroy blocks: " + canBreakCount);
            }

            canBreak = new String[canBreakCount];
            for (int i = 0; i < canBreak.length; i++) {
                canBreak[i] = getItemUserDataString(stream);
            }

            if (id != null) {
                if (id == ItemID.SHIELD) {
                    stream.getLLong();
                }

                if (compoundTag != null && compoundTag.contains(MV_ORIGIN_ID) && compoundTag.contains(MV_ORIGIN_META)) {
                    int originID = compoundTag.getInt(MV_ORIGIN_ID);
                    int originMeta = compoundTag.getInt(MV_ORIGIN_META);
                    Item item;
                    if (originID == ItemID.STRING_IDENTIFIED_ITEM && compoundTag.contains(MV_ORIGIN_NAMESPACE)) {
                        item = Item.fromString(compoundTag.getString(MV_ORIGIN_NAMESPACE));
                        item.setDamage(originMeta);
                        item.setCount(cnt);
                    } else {
                        item = Item.get(originID, originMeta, cnt);
                    }
                    if (compoundTag.contains(MV_ORIGIN_NBT)) {
                        item.setNamedTag(compoundTag.getCompound(MV_ORIGIN_NBT));
                    }
                    return item;
                }
            } else if (compoundTag != null && compoundTag.contains(MV_ORIGIN_ID) && compoundTag.contains(MV_ORIGIN_META)) {
                int originID = compoundTag.getInt(MV_ORIGIN_ID);
                int originMeta = compoundTag.getInt(MV_ORIGIN_META);
                Item item;
                if (originID == ItemID.STRING_IDENTIFIED_ITEM && compoundTag.contains(MV_ORIGIN_NAMESPACE)) {
                    item = Item.fromString(compoundTag.getString(MV_ORIGIN_NAMESPACE));
                    item.setDamage(originMeta);
                    item.setCount(cnt);
                } else {
                    item = Item.get(originID, originMeta, cnt);
                }
                if (compoundTag.contains(MV_ORIGIN_NBT)) {
                    item.setNamedTag(compoundTag.getCompound(MV_ORIGIN_NBT));
                }
                return item;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read item user data", e);
        }

        Item item;
        if (id != null) {
            item = Item.get(id, damage, cnt, nbt);
        } else {
            item = Item.fromString(stringId);
            item.setDamage(damage);
            item.setCount(cnt);
            item.setCompoundTag(nbt);
        }

        if (canBreak.length > 0 || canPlace.length > 0) {
            CompoundTag namedTag = item.getNamedTag();
            if (namedTag == null) {
                namedTag = new CompoundTag();
            }

            if (canBreak.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanDestroy");
                for (String blockName : canBreak) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.put("CanDestroy", listTag);
            }

            if (canPlace.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanPlaceOn");
                for (String blockName : canPlace) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.put("CanPlaceOn", listTag);
            }

            item.setNamedTag(namedTag);
        }

        return item;
    }

    public void putSlot(Item item) {
        Server.mvw("BinaryStream#putSlot(Item)");
        this.putSlot(ProtocolInfo.CURRENT_PROTOCOL, item);
    }

    public void putSlot(int protocolId, Item item) {
        this.putSlot(protocolId, item, false);
    }

    public void putSlot(int protocolId, Item item, boolean instanceItem) {
        if (protocolId >= ProtocolInfo.v1_26_40) {
            this.putNetworkItemStackDescriptor(protocolId, item, instanceItem);
            return;
        }
        if (item == null || item.getId() == Item.AIR) {
            this.putByte((byte) 0);
            return;
        }

        boolean isBlock = item instanceof ItemBlock;
        Item mappingItem = isBlock ? GlobalBlockPalette.getDowngradedItemBlock(protocolId, item.getBlockId()) : null;

        RuntimeItemMapping mapping = RuntimeItems.getMapping(protocolId);
        boolean isErrorItem = false;
        boolean isStringItem = item instanceof StringItem;
        try {
            if (isStringItem && mapping.getNetworkIdByNamespaceId(item.getNamespaceId()).isEmpty()) {
                throw new IllegalArgumentException("Unknown StringItem : NamespaceId=" + item.getNamespaceId() + " protocol=" + protocolId);
            } else {
                mapping.toRuntime(item.getId(), item.getDamage());
            }
        } catch (Exception e) {
            Server.getInstance().getLogger().debug("Unknown Item", e);
            isErrorItem = true;
        }

        if (mappingItem != null || !item.isSupportedOn(protocolId) || isErrorItem) {
            Item originItem = item;
            if(mappingItem == null) {
                String downgradedIdentifier = RuntimeItems.downgradeIdentifier(item.getIdentifier(), protocolId);
                if (downgradedIdentifier != null) {
                    String[] parts = downgradedIdentifier.split("-");
                    String identifier = parts[0];
                    int damage = parts.length > 1 ? Integer.parseInt(parts[1]) : originItem.getDamage();
                    item = Item.get(identifier, damage, originItem.getCount());
                } else {
                    item = Item.get(Item.INFO_UPDATE, 0, originItem.getCount());
                }
            } else {
                item = mappingItem;
            }

            item.setCount(originItem.getCount());
            CompoundTag compoundTag = originItem.getNamedTag();
            if (compoundTag != null) {
                item.setNamedTag(new CompoundTag().putCompound(MV_ORIGIN_NBT, compoundTag));
            }
            item.setCustomName(originItem.getName());
            item.setNamedTag(item.getNamedTag().putInt(MV_ORIGIN_ID, originItem.getId()).putInt(MV_ORIGIN_META, originItem.getDamage()));
            if (isStringItem) {
                item.setNamedTag(item.getNamedTag().putString(MV_ORIGIN_NAMESPACE, originItem.getNamespaceId(protocolId)));
            }

            isErrorItem = false;
            isStringItem = item instanceof StringItem;

            try {
                if (isStringItem && mapping.getNetworkIdByNamespaceId(item.getNamespaceId()).isEmpty()) {
                    throw new IllegalArgumentException("Unknown StringItem : NamespaceId=" + item.getNamespaceId() + " protocol=" + protocolId);
                } else {
                    mapping.toRuntime(item.getId(), item.getDamage());
                }
            } catch (Exception e) {
                item = Item.get(Item.INFO_UPDATE, 0, originItem.getCount());
                isErrorItem = true;
            }
        }

        int id = item.getId();
        int meta = item.getDamage();
        boolean isDurable = item instanceof ItemDurable;

        int runtimeId;
        int damage = 0;
        if (isStringItem && !isErrorItem) {
            runtimeId = mapping.getNetworkId(item);
            damage = item.getDamage();
        } else {
            RuntimeEntry runtimeEntry = mapping.toRuntime(id, meta);
            runtimeId = runtimeEntry.getRuntimeId();
            damage = isBlock || isDurable || runtimeEntry.isHasDamage() ? 0 : meta;
        }

        this.putVarInt(runtimeId);
        this.putLShort(item.getCount());
        this.putUnsignedVarInt(damage);

        if (!instanceItem) {
            this.putBoolean(true);
            this.putVarInt(1); // Item is present
        }

        Block block = isBlock ? item.getBlockUnsafe() : null;
        int blockRuntimeId = block == null ? 0 : GlobalBlockPalette.getOrCreateRuntimeId(protocolId, block.getId(), block.getDamage());
        this.putVarInt(blockRuntimeId);

        try {
            BinaryStream stream = new BinaryStream();
            if (!instanceItem && (isDurable || block != null && block.getDamage() > 0)) {
                byte[] nbt = item.getCompoundTag();
                CompoundTag tag;
                if (nbt == null || nbt.length == 0) {
                    tag = new CompoundTag();
                } else {
                    tag = NBTIO.read(nbt, ByteOrder.LITTLE_ENDIAN);
                }
                if (tag.contains("Damage")) {
                    tag.put("__DamageConflict__", tag.removeAndGet("Damage"));
                }
                tag.putInt("Damage", meta);
                stream.putLShort(-1);
                stream.putByte((byte) 1); // Hardcoded in current version
                stream.put(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN));
            } else if (item.hasCompoundTag()) {
                stream.putLShort(-1);
                stream.putByte((byte) 1); // Hardcoded in current version
                stream.put(item.getCompoundTag());
            } else {
                stream.putLShort(0);
            }

            List<String> canPlaceOn = extractStringList(item, "CanPlaceOn");
            stream.putLInt(canPlaceOn.size());
            for (String string : canPlaceOn) {
                byte[] b = string.getBytes(StandardCharsets.UTF_8);
                stream.putLShort(string.length());
                stream.put(b);
            }

            List<String> canDestroy = extractStringList(item, "CanDestroy");
            stream.putLInt(canDestroy.size());
            for (String string : canDestroy) {
                byte[] b = string.getBytes(StandardCharsets.UTF_8);
                stream.putLShort(string.length());
                stream.put(b);
            }

            if (id == ItemID.SHIELD) {
                stream.putLLong(0);
            }

            this.putByteArray(stream.getBuffer());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write item user data", e);
        }
    }

    private Item readItemInstanceDescriptorV2168() {
        int descriptorType = (int) this.getUnsignedVarInt();
        this.getByte(); // duplicated descriptor type
        String identifier = null;
        int damage = 0;
        if (descriptorType != 0) {
            identifier = this.getString();
            damage = this.getVarInt();
        }
        int count = this.getLShort();
        this.getUnsignedVarInt(); // blockRuntimeId
        byte[] userData = this.getByteArray();
        if (identifier == null) {
            return Item.AIR_ITEM.clone();
        }
        Item item = Item.fromString(identifier);
        if (item == null) {
            return Item.AIR_ITEM.clone();
        }
        item.setDamage(damage);
        item.setCount(count);
        if (userData.length > 0) {
            BinaryStream stream = new BinaryStream(userData);
            try {
                int nbtSize = (short) stream.getLShort();
                CompoundTag tag = null;
                if (nbtSize > 0) {
                    tag = readItemUserDataNbt(stream);
                } else if (nbtSize == -1) {
                    int tagCount = stream.getByte();
                    if (tagCount != 1) throw new IllegalArgumentException("Expected 1 tag but got " + tagCount);
                    tag = readItemUserDataNbt(stream);
                }
                if (tag != null && !tag.isEmpty()) {
                    item.setCompoundTag(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN));
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read item instance user data", e);
            }
        }
        return item;
    }
    public Item getRecipeIngredient(int protocolId) {
        int runtimeId = this.getVarInt();
        if (runtimeId == 0) {
            return Item.get(0, 0, 0);
        }

        int damage = this.getVarInt();
        if (damage == 0x7fff) {
            damage = -1;
        }

        RuntimeItemMapping mapping = RuntimeItems.getMapping(protocolId);
        LegacyEntry legacyEntry = mapping.fromRuntime(runtimeId);
        int id = legacyEntry.getLegacyId();
        if (legacyEntry.isHasDamage()) {
            damage = legacyEntry.getDamage();
        }

        int count = this.getVarInt();
        return Item.get(id, damage, count);
    }

    private static List<String> extractStringList(Item item, String tagName) {
        CompoundTag namedTag = item.getNamedTag();
        if (namedTag == null) {
            return Collections.emptyList();
        }

        ListTag<StringTag> listTag = namedTag.getList(tagName, StringTag.class);
        if (listTag == null) {
            return Collections.emptyList();
        }

        int size = listTag.size();
        List<String> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            StringTag stringTag = listTag.get(i);
            if (stringTag != null) {
                values.add(stringTag.data);
            }
        }

        return values;
    }

    private static CompoundTag readItemUserDataNbt(BinaryStream stream) throws IOException {
        int remaining = stream.getCount() - stream.getOffset();
        ByteArrayInputStream input = new ByteArrayInputStream(
                stream.getBufferUnsafe(), stream.getOffset(), remaining);
        CompoundTag tag = NBTIO.read(input, ByteOrder.LITTLE_ENDIAN);
        stream.skip(remaining - input.available());
        return tag;
    }

    private static String getItemUserDataString(BinaryStream stream) {
        int length = stream.getLShort();
        return new String(stream.get(length), StandardCharsets.UTF_8);
    }

    public void putNetworkItemStackDescriptor(int protocolId, Item item) {
        this.putNetworkItemStackDescriptor(protocolId, item, false);
    }

    public void putNetworkItemStackDescriptor(int protocolId, Item item, boolean instanceItem) {
        if (protocolId < ProtocolInfo.v1_26_20_26) {
            this.putSlot(protocolId, item, instanceItem);
            return;
        }


        if (item == null) {
            item = Item.AIR_ITEM.clone();
        }

        RuntimeItemMapping mapping = RuntimeItems.getMapping(protocolId);
        boolean isErrorItem = false;
        boolean isStringItem = item instanceof StringItem;
        if (item.getId() != Item.AIR) {
            try {
                if (isStringItem && mapping.getNetworkIdByNamespaceId(item.getNamespaceId()).isEmpty()) {
                    throw new IllegalArgumentException("Unknown StringItem : NamespaceId=" + item.getNamespaceId() + " protocol=" + protocolId);
                } else {
                    mapping.toRuntime(item.getId(), item.getDamage());
                }
            } catch (Exception e) {
                Server.getInstance().getLogger().debug("Unknown Item", e);
                isErrorItem = true;
            }
        }

        if (item.getId() != Item.AIR && (!item.isSupportedOn(protocolId) || isErrorItem)) {
            Item originItem = item;
            String downgradedIdentifier = RuntimeItems.downgradeIdentifier(item.getIdentifier(), protocolId);
            if(downgradedIdentifier != null) {
                String[] parts = downgradedIdentifier.split("-");
                String identifier = parts[0];
                int damage = parts.length > 1 ? Integer.parseInt(parts[1]) : originItem.getDamage();
                item = Item.get(identifier, damage, originItem.getCount());
            } else {
                item = Item.get(Item.INFO_UPDATE, 0, originItem.getCount());
            }

            item.setCount(originItem.getCount());
            CompoundTag compoundTag = originItem.getNamedTag();
            if (compoundTag != null) {
                item.setNamedTag(new CompoundTag().putCompound(MV_ORIGIN_NBT, compoundTag));
            }
            item.setCustomName(originItem.getName());
            item.setNamedTag(item.getNamedTag().putInt(MV_ORIGIN_ID, originItem.getId()).putInt(MV_ORIGIN_META, originItem.getDamage()));
            if (isStringItem) {
                item.setNamedTag(item.getNamedTag().putString(MV_ORIGIN_NAMESPACE, originItem.getNamespaceId(protocolId)));
            }

            isErrorItem = false;
            isStringItem = item instanceof StringItem;

            try {
                if (isStringItem && mapping.getNetworkIdByNamespaceId(item.getNamespaceId()).isEmpty()) {
                    throw new IllegalArgumentException("Unknown StringItem : NamespaceId=" + item.getNamespaceId() + " protocol=" + protocolId);
                } else {
                    mapping.toRuntime(item.getId(), item.getDamage());
                }
            } catch (Exception e) {
                item = Item.get(Item.INFO_UPDATE, 0, originItem.getCount());
                isErrorItem = true;
            }

        }

        int id = item.getId();
        int meta = item.getDamage();
        boolean isBlock = item instanceof ItemBlock;
        boolean isDurable = item instanceof ItemDurable;

        int runtimeId = 0;
        int damage = 0;
        if (id != Item.AIR) {
            if (isStringItem && !isErrorItem) {
                runtimeId = mapping.getNetworkId(item);
                damage = item.getDamage();
            } else {
                RuntimeEntry runtimeEntry = mapping.toRuntime(id, meta);
                runtimeId = runtimeEntry.getRuntimeId();
                damage = isBlock || isDurable || runtimeEntry.isHasDamage() ? 0 : meta;
            }
        }

        if (instanceItem) {
            this.putVarInt(runtimeId);
        } else {
            this.putLShort(runtimeId);
        }
        this.putLShort(item.getCount());
        this.putUnsignedVarInt(damage);

        if (!instanceItem) {
            boolean hasNetId = id != Item.AIR;
            this.putBoolean(hasNetId);
            if (hasNetId) {
                if (protocolId < ProtocolInfo.v1_26_40) {
                    this.putUnsignedVarInt(0); // ItemStackNetId variant
                }
                this.putVarInt(1); // Lumi does not expose stack net IDs on Item
            }
        }

        Block block = isBlock && id != Item.AIR ? item.getBlockUnsafe() : null;
        int blockRuntimeId = block == null ? 0 : GlobalBlockPalette.getOrCreateRuntimeId(protocolId, block.getId(), block.getDamage());
        if (instanceItem) {
            this.putVarInt(blockRuntimeId);
        } else {
            this.putUnsignedVarInt(blockRuntimeId);
        }

        if (id == Item.AIR) {
            this.putUnsignedVarInt(0); // No user data
            return;
        }

        try {
            BinaryStream stream = new BinaryStream();
            if (!instanceItem && (isDurable || block != null && block.getDamage() > 0)) {
                byte[] nbt = item.getCompoundTag();
                CompoundTag tag;
                if (nbt == null || nbt.length == 0) {
                    tag = new CompoundTag();
                } else {
                    tag = NBTIO.read(nbt, ByteOrder.LITTLE_ENDIAN);
                }
                if (tag.contains("Damage")) {
                    tag.put("__DamageConflict__", tag.removeAndGet("Damage"));
                }
                tag.putInt("Damage", meta);
                stream.putLShort(-1);
                stream.putByte((byte) 1); // Hardcoded in current version
                stream.put(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN));
            } else if (item.hasCompoundTag()) {
                stream.putLShort(-1);
                stream.putByte((byte) 1); // Hardcoded in current version
                stream.put(item.getCompoundTag());
            } else {
                stream.putLShort(0);
            }

            List<String> canPlaceOn = extractStringList(item, "CanPlaceOn");
            stream.putLInt(canPlaceOn.size());
            for (String string : canPlaceOn) {
                byte[] b = string.getBytes(StandardCharsets.UTF_8);
                stream.putLShort(string.length());
                stream.put(b);
            }

            List<String> canDestroy = extractStringList(item, "CanDestroy");
            stream.putLInt(canDestroy.size());
            for (String string : canDestroy) {
                byte[] b = string.getBytes(StandardCharsets.UTF_8);
                stream.putLShort(string.length());
                stream.put(b);
            }

            if (id == ItemID.SHIELD) {
                stream.putLLong(0);
            }

            this.putByteArray(stream.getBuffer());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write item user data", e);
        }
    }

    public Item getNetworkItemStackDescriptor(int protocolId) {
        return this.getNetworkItemStackDescriptor(protocolId, false);
    }

    public Item getNetworkItemStackDescriptor(int protocolId, boolean instanceItem) {
        if (protocolId < ProtocolInfo.v1_26_20_26) {
            return this.getSlot(protocolId);
        }

        Integer id = null;
        String stringId = null;
        int runtimeId = instanceItem ? this.getVarInt() : (short) this.getLShort();
        int count = this.getLShort();
        int damage = (int) this.getUnsignedVarInt();

        RuntimeItemMapping mapping = RuntimeItems.getMapping(protocolId);
        LegacyEntry legacyEntry = null;

        if (runtimeId != 0) {
            try {
                stringId = mapping.getNamespacedIdByNetworkId(runtimeId);
                legacyEntry = mapping.fromRuntime(runtimeId);
                id = legacyEntry.getLegacyId();
                if (legacyEntry.isHasDamage()) {
                    damage = legacyEntry.getDamage();
                }
            } catch (IllegalArgumentException ignore) {}

            if (id == null || !Utils.hasItemOrBlock(id)) {
                if (stringId == null) {
                    stringId = mapping.getNamespacedIdByNetworkId(runtimeId);
                }
                if (stringId == null) {
                    throw new IllegalArgumentException("Unknown item: runtimeID=" + runtimeId + " protocol=" + protocolId);
                }
                stringId = stringId + ":" + damage;
                id = null;
            }
        } else {
            id = 0;
        }

        int stackNetId = 0;
        if (!instanceItem && this.getBoolean()) { // hasNetId
            if (protocolId < ProtocolInfo.v1_26_40) {
                this.getUnsignedVarInt(); // netIdVariant
            }
            stackNetId = this.getVarInt();
        }

        int blockRuntimeId = instanceItem ? this.getVarInt() : (int) this.getUnsignedVarInt();

        byte[] nbt = new byte[0];
        String[] canPlace = null;
        String[] canBreak = null;

        BinaryStream stream = new BinaryStream(this.getByteArray());

        if (stream.getCount() != 0) {
            try {
                int nbtSize = (short) stream.getLShort();

                CompoundTag compoundTag = null;
                if (nbtSize > 0) {
                    compoundTag = readItemUserDataNbt(stream);
                } else if (nbtSize == -1) {
                    int tagCount = stream.getByte();
                    if (tagCount != 1) throw new IllegalArgumentException("Expected 1 tag but got " + tagCount);
                    compoundTag = readItemUserDataNbt(stream);
                }

                if (compoundTag != null && !compoundTag.getAllTags().isEmpty()) {
                    if (compoundTag.contains("Damage")) {
                        damage = compoundTag.getInt("Damage");
                        compoundTag.remove("Damage");
                    }
                    if (compoundTag.contains("__DamageConflict__")) {
                        compoundTag.put("Damage", compoundTag.removeAndGet("__DamageConflict__"));
                    }
                    if (!compoundTag.isEmpty()) {
                        nbt = NBTIO.write(compoundTag, ByteOrder.LITTLE_ENDIAN);
                    }
                }

                int canPlaceCount = stream.getLInt();
                if (canPlaceCount > 4096) {
                    throw new RuntimeException("Too many CanPlaceOn blocks: " + canPlaceCount);
                }

                canPlace = new String[canPlaceCount];
                for (int i = 0; i < canPlace.length; i++) {
                    canPlace[i] = getItemUserDataString(stream);
                }

                int canBreakCount = stream.getLInt();
                if (canBreakCount > 4096) {
                    throw new RuntimeException("Too many CanDestroy blocks: " + canBreakCount);
                }

                canBreak = new String[canBreakCount];
                for (int i = 0; i < canBreak.length; i++) {
                    canBreak[i] = getItemUserDataString(stream);
                }

                if (id != null && id == ItemID.SHIELD) {
                    stream.getLLong();
                }

                if (compoundTag != null && compoundTag.contains(MV_ORIGIN_ID) && compoundTag.contains(MV_ORIGIN_META)) {
                    int originID = compoundTag.getInt(MV_ORIGIN_ID);
                    int originMeta = compoundTag.getInt(MV_ORIGIN_META);
                    Item item;
                    if (originID == ItemID.STRING_IDENTIFIED_ITEM && compoundTag.contains(MV_ORIGIN_NAMESPACE)) {
                        item = Item.fromString(compoundTag.getString(MV_ORIGIN_NAMESPACE));
                        item.setDamage(originMeta);
                        item.setCount(count);
                    } else {
                        item = Item.get(originID, originMeta, count);
                    }
                    if (compoundTag.contains(MV_ORIGIN_NBT)) {
                        item.setNamedTag(compoundTag.getCompound(MV_ORIGIN_NBT));
                    }
                    return item;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read item user data", e);
            }
        }

        if (runtimeId == 0) {
            return Item.AIR_ITEM.clone();
        }

        Item item;
        if (id != null) {
            item = Item.get(id, damage, count, nbt);
        } else {
            item = Item.fromString(stringId);
            item.setDamage(damage);
            item.setCount(count);
            item.setCompoundTag(nbt);
        }

        if ((canBreak != null && canBreak.length > 0) || (canPlace != null && canPlace.length > 0)) {
            CompoundTag namedTag = item.getNamedTag();
            if (namedTag == null) {
                namedTag = new CompoundTag();
            }

            if (canBreak != null && canBreak.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanDestroy");
                for (String blockName : canBreak) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.putList(listTag);
            }

            if (canPlace != null && canPlace.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanPlaceOn");
                for (String blockName : canPlace) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.putList(listTag);
            }

            item.setNamedTag(namedTag);
        }

        return item;
    }

    public byte[] getByteArray() {
        return this.get((int) this.getUnsignedVarInt());
    }

    public byte[] getByteArray(int maxLength) {
        int length = (int) this.getUnsignedVarInt();
        checkArgument(this.isReadable(length),
                "Tried to read %s bytes but only has %s readable", length, this.readableBytes());
        checkArgument(maxLength <= 0 || length <= maxLength, "Tried to read %s bytes but maximum is %s", length, maxLength);
        return this.get(length);
    }

    public void putByteArray(byte[] b) {
        this.putUnsignedVarInt(b.length);
        this.put(b);
    }

    public String getString() {
        return new String(this.getByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Reads a length-prefixed UTF-8 string and rejects values exceeding the code-point limit.
     * A byte-level guard rejects oversized payloads before allocating them.
     */
    public String getString(int maxChars) {
        byte[] raw = this.getByteArray(maxChars <= 0 ? 0 : maxChars * 4);
        String value = new String(raw, StandardCharsets.UTF_8);
        int length = Character.codePointCount(value, 0, value.length());
        if (maxChars > 0 && length > maxChars) {
            throw new IllegalArgumentException("String length " + length + " exceeds maximum " + maxChars);
        }
        return value;
    }

    public void putString(String string) {
        byte[] b = string.getBytes(StandardCharsets.UTF_8);
        this.putByteArray(b);
    }

    public long getUnsignedVarInt() {
        return VarInt.readUnsignedVarInt(this);
    }

    public void putUnsignedVarInt(long v) {
        VarInt.writeUnsignedVarInt(this, v);
    }

    public int getVarInt() {
        return VarInt.readVarInt(this);
    }

    public void putVarInt(int v) {
        VarInt.writeVarInt(this, v);
    }

    public long getVarLong() {
        return VarInt.readVarLong(this);
    }

    public void putVarLong(long v) {
        VarInt.writeVarLong(this, v);
    }

    public long getUnsignedVarLong() {
        return VarInt.readUnsignedVarLong(this);
    }

    public void putUnsignedVarLong(long v) {
        VarInt.writeUnsignedVarLong(this, v);
    }

    public BlockVector3 getBlockVector3() {
        return new BlockVector3(this.getVarInt(), (int) this.getUnsignedVarInt(), this.getVarInt());
    }

    public BlockVector3 getBlockVector3(int protocol) {
        return new BlockVector3(this.getVarInt(), protocol >= ProtocolInfo.v1_26_10 ? this.getVarInt() : (int) this.getUnsignedVarInt(), this.getVarInt());
    }

    public BlockVector3 getSignedBlockPosition() {
        return new BlockVector3(getVarInt(), getVarInt(), getVarInt());
    }

    public void putSignedBlockPosition(BlockVector3 v) {
        putVarInt(v.x);
        putVarInt(v.y);
        putVarInt(v.z);
    }

    public void putBlockVector3(BlockVector3 v) {
        this.putBlockVector3(v.x, v.y, v.z);
    }

    public void putBlockVector3(int protocol, BlockVector3 v) {
        this.putBlockVector3(protocol, v.x, v.y, v.z);
    }

    public void putBlockVector3(int x, int y, int z) {
        this.putVarInt(x);
        this.putUnsignedVarInt(y);
        this.putVarInt(z);
    }

    public void putBlockVector3(int protocol, int x, int y, int z) {
        this.putVarInt(x);
        if (protocol >= ProtocolInfo.v1_26_10) {
            this.putVarInt(y);
        } else {
            this.putUnsignedVarInt(y);
        }
        this.putVarInt(z);
    }


    public Vector3f getVector3f() {
        return new Vector3f(this.getLFloat(), this.getLFloat(), this.getLFloat());
    }

    public void putVector3f(Vector3f v) {
        this.putVector3f(v.x, v.y, v.z);
    }

    public void putVector3f(float x, float y, float z) {
        this.putLFloat(x);
        this.putLFloat(y);
        this.putLFloat(z);
    }

    public Vector2f getVector2f() {
        return new Vector2f(this.getLFloat(), this.getLFloat());
    }

    public void putVector2f(Vector2f v) {
        this.putVector2f(v.x, v.y);
    }

    public void putVector2f(float x, float y) {
        this.putLFloat(x);
        this.putLFloat(y);
    }

    public double getRotationByte() {
        return this.getByte() * (360d / 256d);
    }

    public void putRotationByte(double rotation) {
        this.putByte((byte) (rotation / (360d / 256d)));
    }

    public void putGameRules(GameRules gameRules, boolean startGame) {
        Server.mvw("BinaryStream#putGameRules(GameRules, boolean)");
        this.putGameRules(ProtocolInfo.CURRENT_PROTOCOL, gameRules, startGame);
    }

    public void putGameRules(int protocol, GameRules gameRules, boolean startGame) {
        Map<GameRule, GameRules.Value> allGameRules = gameRules.getGameRules();
        Map<GameRule, GameRules.Value> rulesToSend = new HashMap<>();
        allGameRules.forEach((gameRule, value) -> {
            if (protocol > value.getMinProtocol()) {
                rulesToSend.put(gameRule, value);
            }
        });
        this.putUnsignedVarInt(rulesToSend.size());
        rulesToSend.forEach((gameRule, value) -> {
            putString(gameRule.getName().toLowerCase(Locale.ROOT));
            value.write(protocol, this, startGame);
        });
    }

    public void putGameRulesMap(int protocol, Map<GameRule, GameRules.Value> allGameRules, boolean startGame) {
        Map<GameRule, GameRules.Value> rulesToSend = new HashMap<>();
        allGameRules.forEach((gameRule, value) -> {
            if (protocol > value.getMinProtocol()) {
                rulesToSend.put(gameRule, value);
            }
        });
        this.putUnsignedVarInt(rulesToSend.size());
        rulesToSend.forEach((gameRule, value) -> {
            putString(gameRule.getName().toLowerCase(Locale.ROOT));
            value.write(protocol, this, startGame);
        });
    }

    /**
     * Reads and returns an EntityUniqueID
     *
     * @return int
     */
    public long getEntityUniqueId() {
        return this.getVarLong();
    }

    /**
     * Writes an EntityUniqueID
     */
    public void putEntityUniqueId(long eid) {
        this.putVarLong(eid);
    }

    /**
     * Reads and returns an EntityRuntimeID
     */
    public long getEntityRuntimeId() {
        return this.getUnsignedVarLong();
    }

    /**
     * Writes an EntityUniqueID
     */
    public void putEntityRuntimeId(long eid) {
        this.putUnsignedVarLong(eid);
    }

    public BlockFace getBlockFace() {
        return BlockFace.fromIndex(this.getVarInt());
    }

    public void putBlockFace(BlockFace face) {
        this.putVarInt(face.getIndex());
    }

    public void putEntityLink(int protocol, EntityLink link) {
        this.putEntityUniqueId(link.fromEntityUniquieId);
        this.putEntityUniqueId(link.toEntityUniquieId);
        this.putByte(link.type);
        this.putBoolean(link.immediate);
        this.putBoolean(link.riderInitiated);
        if (protocol >= ProtocolInfo.v1_21_20) {
            this.putLFloat(link.vehicleAngularVelocity);
        }
    }

    public EntityLink getEntityLink() {
        return new EntityLink(
                getEntityUniqueId(),
                getEntityUniqueId(),
                (byte) getByte(),
                getBoolean(),
                getBoolean(), //1.16+
                getLFloat() //1.21.20+
        );
    }

    public void putColor(Color color) {
        this.putLInt(color.getRGB());
    }

    public Color getColor() {
        return new Color(this.getLInt());
    }

    public void writeScriptDebugShapeType(ScriptDebugShapeType type) {
        this.putByte((byte) type.ordinal());
    }

    public ScriptDebugShapeType getScriptDebugShapeType() {
        return ScriptDebugShapeType.values()[getByte()];
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getArray(Class<T> clazz, Function<BinaryStream, T> function) {
        ArrayDeque<T> deque = new ArrayDeque<>();
        int count = (int) getUnsignedVarInt();
        for (int i = 0; i < count; i++) {
            deque.add(function.apply(this));
        }
        return deque.toArray((T[]) Array.newInstance(clazz, 0));
    }

    public <T> void getArray(Collection<T> array, Function<BinaryStream, T> function) {
        getArray(array, BinaryStream::getUnsignedVarInt, function);
    }

    public <T> void getArray(Collection<T> array, ToLongFunction<BinaryStream> lengthReader, Function<BinaryStream, T> function) {
        long length = lengthReader.applyAsLong(this);
        for (int i = 0; i < length; i++) {
            array.add(function.apply(this));
        }
    }

    public <T> void putArray(Collection<T> collection, Consumer<T> writer) {
        if (collection == null) {
            putUnsignedVarInt(0);
            return;
        }
        putUnsignedVarInt(collection.size());
        collection.forEach(writer);
    }

    public <T> void putArray(T[] collection, Consumer<T> writer) {
        if (collection == null) {
            putUnsignedVarInt(0);
            return;
        }
        putUnsignedVarInt(collection.length);
        for (T t : collection) {
            writer.accept(t);
        }
    }

    public <T> void putArray(@NotNull Collection<T> array, BiConsumer<BinaryStream, T> biConsumer) {
        this.putUnsignedVarInt(array.size());
        for (T val : array) {
            biConsumer.accept(this, val);
        }
    }

    public <O> O getOptional(O emptyValue, Function<BinaryStream, O> function) {
        if (this.getBoolean()) {
            return function.apply(this);
        }
        return emptyValue;
    }

    public <T> void putOptional(@NotNull Predicate<T> isPresent, T object, Consumer<T> consumer) {
        Preconditions.checkNotNull(consumer, "read consumer");

        boolean exists = isPresent.test(object);
        this.putBoolean(exists);
        if (exists) {
            consumer.accept(object);
        }
    }

    public <T> void putOptional(@NotNull Predicate<T> isPresent, T object, BiConsumer<BinaryStream, T> consumer) {
        Preconditions.checkNotNull(consumer, "read consumer");

        boolean exists = isPresent.test(object);
        this.putBoolean(exists);
        if (exists) {
            consumer.accept(this, object);
        }
    }

    public <T> void putOptionalNull(T object, Consumer<T> consumer) {
        this.putOptional(Objects::nonNull, object, consumer);
    }

    public <T> void putOptionalNull(T object, BiConsumer<BinaryStream, T> consumer) {
        this.putOptional(Objects::nonNull, object, consumer);
    }

    public void writeFullContainerName(FullContainerName fullContainerName) {
        this.putByte((byte) fullContainerName.getContainer().getId());
        this.putOptionalNull(fullContainerName.getDynamicId(), this::putLInt);
    }

    public boolean isReadable(int length) {
        return this.count - this.offset >= length;
    }

    public int readableBytes() {
        return this.count - this.offset;
    }

    public boolean feof() {
        return this.offset < 0 || this.offset >= this.buffer.length;
    }

    @SneakyThrows(IOException.class)
    public CompoundTag getTag() {
        ByteArrayInputStream is = new ByteArrayInputStream(buffer, offset, buffer.length);
        int initial = is.available();
        try {
            return NBTIO.read(is);
        } finally {
            offset += initial - is.available();
        }
    }

    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buffer.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buffer.length;
        int newCapacity = oldCapacity << 1;

        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }

        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        this.buffer = Arrays.copyOf(buffer, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public <T> void putNbtTag(T tag) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (NBTOutputStream writer = NbtUtils.createNetworkWriter(stream)) {
            writer.writeTag(tag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.put(stream.toByteArray());
    }

    public ItemStackRequest readItemStackRequest() {
        return readItemStackRequest(ProtocolInfo.CURRENT_PROTOCOL);
    }

    public ItemStackRequest readItemStackRequest(int protocol) {
        int requestId = getVarInt();
        boolean v2168 = protocol >= ProtocolInfo.v1_26_40;
        ItemStackRequestAction[] actions = getArray(ItemStackRequestAction.class, (s) -> {
            int typeId = v2168 ? (int) s.getUnsignedVarInt() : s.getByte();
            ItemStackRequestActionType itemStackRequestActionType = ItemStackRequestActionType.fromId(typeId, protocol);
            if (itemStackRequestActionType == null) {
                throw new UnsupportedOperationException("Unhandled stack request action type id " + typeId + " for protocol " + protocol);
            }
            if (v2168) {
                s.getByte(); // duplicated action type
            }
            return readRequestActionData(protocol, itemStackRequestActionType);
        });
        String[] filteredStrings = getArray(String.class, BinaryStream::getString);

        int originVal = getLInt();
        TextProcessingEventOrigin origin = originVal == -1 ? null : TextProcessingEventOrigin.fromId(originVal);  // new for v552
        return new ItemStackRequest(requestId, actions, filteredStrings, origin);
    }

    protected ItemStackRequestAction readRequestActionData(int protocol, ItemStackRequestActionType type) {
        boolean hasNumberOfCrafts = protocol >= ProtocolInfo.v1_21_20;
        return switch (type) {
            case CRAFT_REPAIR_AND_DISENCHANT -> {
                int recipeId = protocol >= ProtocolInfo.v1_26_40 ? getLInt() : (int) getUnsignedVarInt();
                if (protocol >= ProtocolInfo.v1_26_40) getByte(); // numberOfRequestedCrafts
                yield new CraftGrindstoneAction(recipeId, getVarInt());
            }
            case CRAFT_LOOM -> {
                String pattern = getString();
                if (protocol >= ProtocolInfo.v1_26_40) getByte(); // numberOfRequestedCrafts
                yield new CraftLoomAction(pattern);
            }
            case CRAFT_RECIPE_AUTO -> {
                int recipeId = (int) getUnsignedVarInt();
                int numberOfRequestedCrafts = hasNumberOfCrafts ? (getByte() & 0xFF) : 0;
                // No separate timesCrafted byte since v2168 (aligned with CB's reader since its 26.50
                // commit removed the duplicated count byte): timesCrafted mirrors numberOfRequestedCrafts
                int timesCrafted = protocol < ProtocolInfo.v1_26_40 ? (getByte() & 0xFF) : numberOfRequestedCrafts;
                if (protocol >= ProtocolInfo.v1_26_40) {
                    int ingredientCount = (int) getUnsignedVarInt();
                    for (int i = 0; i < ingredientCount; i++) skipIngredientDescriptorV2168();
                }
                yield new AutoCraftRecipeAction(recipeId, timesCrafted, Collections.emptyList());
            }
            case CRAFT_RESULTS_DEPRECATED -> new CraftResultsDeprecatedAction(
                    getArray(Item.class, s -> protocol >= ProtocolInfo.v1_26_40
                            ? s.readItemInstanceDescriptorV2168()
                            : s.getSlot(protocol)),
                    getByte()
            );
            case MINE_BLOCK -> new MineBlockAction(getVarInt(), getVarInt(),
                    protocol >= ProtocolInfo.v1_26_40 ? getLInt() : getVarInt());
            case CRAFT_RECIPE_OPTIONAL -> new CraftRecipeOptionalAction((int) getUnsignedVarInt(), getLInt());
            case TAKE -> new TakeAction(
                    getByte(),
                    readStackRequestSlotInfo(protocol),
                    readStackRequestSlotInfo(protocol)
            );
            case PLACE -> new PlaceAction(
                    getByte(),
                    readStackRequestSlotInfo(protocol),
                    readStackRequestSlotInfo(protocol)
            );
            case SWAP -> new SwapAction(
                    readStackRequestSlotInfo(protocol),
                    readStackRequestSlotInfo(protocol)
            );
            case DROP -> new DropAction(
                    getByte(),
                    readStackRequestSlotInfo(protocol),
                    getBoolean()
            );
            case DESTROY -> new DestroyAction(
                    getByte(),
                    readStackRequestSlotInfo(protocol)
            );
            case CONSUME -> new ConsumeAction(
                    getByte(),
                    readStackRequestSlotInfo(protocol)
            );
            case CREATE -> new CreateAction(
                    getByte()
            );
            case LAB_TABLE_COMBINE -> new LabTableCombineAction();
            case BEACON_PAYMENT -> new BeaconPaymentAction(
                    getVarInt(),
                    getVarInt()
            );
            case CRAFT_RECIPE -> {
                int recipeId = (int) getUnsignedVarInt();
                if (protocol >= ProtocolInfo.v1_26_40) getByte();
                yield new CraftRecipeAction(recipeId);
            }
            case CRAFT_CREATIVE -> {
                int creativeId = (int) getUnsignedVarInt();
                if (protocol >= ProtocolInfo.v1_26_40) getByte();
                yield new CraftCreativeAction(creativeId);
            }
            case CRAFT_NON_IMPLEMENTED_DEPRECATED -> new CraftNonImplementedAction();
            default -> throw new UnsupportedOperationException("Unhandled stack request action type: " + type);
        };
    }

    private ItemStackRequestSlotData readStackRequestSlotInfo(int protocol) {
        return new ItemStackRequestSlotData(
                ContainerSlotType.fromId(getByte()),
                getByte(),
                protocol >= ProtocolInfo.v1_26_40 ? getLInt() : getVarInt()
        );
    }

    private void skipIngredientDescriptorV2168() {
        int descriptorType = (int) getUnsignedVarInt();
        getByte(); // duplicated descriptor type
        switch (descriptorType) {
            case 0 -> { }
            case 1, 3 -> {
                getString();
                if (descriptorType == 1) getVarInt();
            }
            case 2 -> {
                getString();
                getLShort();
            }
            default -> throw new UnsupportedOperationException("Unhandled v2168 ingredient descriptor " + descriptorType);
        }
        getLShort(); // count
    }
}