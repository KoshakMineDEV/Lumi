package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.customblock.CustomBlock;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.RuntimeItemMapping.RuntimeEntry;
import cn.nukkit.item.customitem.CustomItem;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.material.ItemType;
import cn.nukkit.item.material.ItemTypes;
import cn.nukkit.item.material.tags.ItemTag;
import cn.nukkit.item.material.tags.ItemTags;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
@Log4j2
public class Item implements Cloneable, BlockID, ItemID, ProtocolInfo {

    public static final Item AIR_ITEM = new ItemBlock(Block.get(BlockID.AIR), null, 0);

    public static final Item[] EMPTY_ARRAY = new Item[0];

    /**
     * Groups:
     * <ol>
     *     <li>namespace (optional)</li>
     *     <li>item name (choice)</li>
     *     <li>damage (optional, for item name)</li>
     *     <li>numeric id (choice)</li>
     *     <li>damage (optional, for numeric id)</li>
     * </ol>
     */
    private static final Pattern ITEM_STRING_PATTERN = Pattern.compile(
            //       1:namespace    2:name           3:damage   4:num-id    5:damage
            "^(?:(?:([a-z_]\\w*):)?([a-z._]\\w*)(?::(-?\\d+))?|(-?\\d+)(?::(-?\\d+))?)$"
    );

    public static final String UNKNOWN_STR = "Unknown";

    protected final int id;
    protected ItemType type;
    protected Block block = null;
    protected int meta;
    protected boolean hasMeta = true;
    private byte[] tags = new byte[0];
    private CompoundTag cachedNBT = null;
    public int count;
    protected String name;

    public Item(int id) {
        this(id, 0, 1, UNKNOWN_STR);
    }

    public Item(int id, Integer meta) {
        this(id, meta, 1, UNKNOWN_STR);
    }

    public Item(int id, Integer meta, int count) {
        this(id, meta, count, UNKNOWN_STR);
    }

    public Item(int id, Integer meta, int count, String name) {
        this.id = id;
        if (meta != null && meta >= 0) {
            this.meta = meta & 0xffff;
        } else {
            this.hasMeta = false;
        }
        this.count = count;
        this.name = name;
    }

    public static Item get(int id) {
        return get(id, 0);
    }

    public static Item get(int id, Integer meta) {
        return get(id, meta, 1);
    }

    public static Item get(int id, Integer meta, int count) {
        return get(id, meta, count, new byte[0]);
    }

    public static Item get(int id, Integer meta, int count, byte[] tags) {
        try {
            Class<?> clazz;
            if (id < 255 - Block.MAX_BLOCK_ID) {
                var customBlockItem = Block.get(255 - id).toItem();
                customBlockItem.setCount(count);
                customBlockItem.setDamage(meta);
                customBlockItem.setCompoundTag(tags);
                return customBlockItem;
            } else if (id < 0) {
                int blockId = 255 - id;
                clazz = Registries.BLOCK.getClass(blockId);
            } else {
                clazz = Registries.ITEM_LEGACY.get(id);
            }

            Item item;
            if (clazz == null) {
                item = new Item(id, meta, count);
            } else if (id < 256 && id != 166) {
                if (meta >= 0) {
                    item = new ItemBlock(Block.get(id, meta), meta, count);
                } else {
                    item = new ItemBlock(Block.get(id), meta, count);
                }
            } else {
                item = (Item) clazz.getConstructor(Integer.class, int.class).newInstance(meta, count);
            }

            if (tags.length != 0) {
                item.setCompoundTag(tags);
            }

            return item;
        } catch (Exception e) {
            return new Item(id, meta, count).setCompoundTag(tags);
        }
    }

    public static Item get(String id) {
        return get(id, null);
    }

    public static Item get(String id, Integer meta) {
        return get(id, meta, 1);
    }

    public static Item get(String id, Integer meta, int count) {
        return get(id, meta, count, new byte[0]);
    }

    public static Item get(String id, Integer meta, int count, byte[] tags) {
        id = id.toLowerCase();
        if (!id.contains(":")) id = "minecraft:" + id;

        Item item = Registries.ITEM.get(id);
        if (meta != null) {
            item.setDamage(meta);
        }
        item.setCount(count);

        if (tags.length != 0) {
            item.setCompoundTag(tags);
        }

        return item;
    }

    /**
     * Deprecated. Use Item.get() instead
     */
    @Deprecated
    public static Item fromString(String str) {
        String normalized = str.trim().replace(' ', '_').toLowerCase(Locale.ROOT);
        Matcher matcher = ITEM_STRING_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return AIR_ITEM.clone();
        }

        String name = matcher.group(2);
        OptionalInt meta = OptionalInt.empty();
        String metaGroup;
        if (name != null) {
            metaGroup = matcher.group(3);
        } else {
            metaGroup = matcher.group(5);
        }
        if (metaGroup != null) {
            meta = OptionalInt.of(Short.parseShort(metaGroup));
        }

        String numericIdGroup = matcher.group(4);
        if (name != null) {
            String namespaceGroup = matcher.group(1);
            String namespacedId;
            if (namespaceGroup != null) {
                namespacedId = namespaceGroup + ":" + name;
            } else {
                namespacedId = "minecraft:" + name;
            }
            if ("minecraft:air".equals(namespacedId)) {
                return Item.AIR_ITEM.clone();
            }

            Supplier<Item> constructor = Registries.ITEM.getSupplier(namespacedId);
            if (constructor != null) {
                try {
                    Item item = constructor.get().clone();
                    if (meta.isPresent()) {
                        int metaValue = meta.getAsInt();
                        if (metaValue != 0) {
                            item.setDamage(metaValue);
                        }
                    }
                    // Avoid the upcoming changes to the original item object
                    return item;
                } catch (Exception e) {
                    log.warn("Could not create a new instance of {} using the namespaced id {}", constructor, namespacedId, e);
                }
            } else {
                return RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL).getItemByNamespaceId(namespacedId, 1);
            }

            //common item
            int id = RuntimeItems.getLegacyIdFromLegacyString(namespacedId);
            if (id > 0) {
                return get(id, meta.orElse(0));
            } else if (namespaceGroup != null && !namespaceGroup.equals("minecraft:")) {
                return Item.AIR_ITEM.clone();
            }
        } else if (numericIdGroup != null) {
            int id = Integer.parseInt(numericIdGroup);
            return get(id, meta.orElse(0));
        }

        if (name == null) {
            return Item.AIR_ITEM.clone();
        }

        int id = 0;
        try {
            id = BlockID.class.getField(name.toUpperCase(Locale.ROOT)).getInt(null);
            if (id > 255) {
                id = 255 - id;
            }
        } catch (Exception ignore) {
            try {
                id = ItemID.class.getField(name.toUpperCase(Locale.ROOT)).getInt(null);
            } catch (Exception ignore1) {
            }
        }
        return get(id, meta.orElse(0));
    }

    @Deprecated
    public static Item[] fromStringMultiple(String str) {
        String[] b = str.split(",");
        Item[] items = new Item[b.length - 1];
        for (int i = 0; i < b.length; i++) {
            items[i] = fromString(b[i]);
        }
        return items;
    }

    @Deprecated
    public static OK<?> registerCustomItem(@NotNull List<Class<? extends CustomItem>> itemClassList) {
        try {
            Registries.ITEM.registerCustom(itemClassList);
            return OK.TRUE;
        } catch (Exception e) {
            return new OK<>(false, e);
        }
    }

    @Deprecated
    public static OK<?> registerCustomItem(@NotNull Class<? extends CustomItem> clazz) {
        try {
            Registries.ITEM.registerCustom(clazz);
            return OK.TRUE;
        } catch (Exception e) {
            return new OK<>(false, e);
        }
    }

    @Deprecated
    public static OK<?> registerCustomItem(@NotNull Class<? extends CustomItem> clazz, boolean addCreativeItem) {
        try {
            Registries.ITEM.registerCustom(clazz, addCreativeItem);
            return OK.TRUE;
        } catch (Exception e) {
            return new OK<>(false, e);
        }
    }

    private String idConvertToName() {
        if (this.name == null) {
            var path = this.getNamespaceId().split(":")[1];
            StringBuilder result = new StringBuilder();
            String[] parts = path.split("_");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
                }
            }
            this.name = result.toString().trim().intern();
        }
        return this.name;
    }

    public static Item fromJson(Map<String, Object> data) {
        return fromJson(data, false);
    }

    public static Item fromJson(Map<String, Object> data, boolean ignoreUnsupported) {
        String nbt = (String) data.get("nbt_b64");
        byte[] nbtBytes;
        if (nbt != null) {
            nbtBytes = Base64.getDecoder().decode(nbt);
        } else { // Support old format for backwards compatibility
            nbt = (String) data.getOrDefault("nbt_hex", null);
            if (nbt == null) {
                nbtBytes = new byte[0];
            } else {
                nbtBytes = Utils.parseHexBinary(nbt);
            }
        }

        Object id1 = data.get("id");
        if (ignoreUnsupported && !Utils.hasItemOrBlock(id1)) {
            return null;
        }
        Item item = fromString(id1 + ":" + data.getOrDefault("damage", 0));
        item.setCount(Utils.toInt(data.getOrDefault("count", 1)));
        item.setCompoundTag(nbtBytes);
        return item;
    }

    public boolean hasMeta() {
        return hasMeta;
    }

    public boolean canBeActivated() {
        return false;
    }

    public Item setCompoundTag(CompoundTag tag) {
        this.setNamedTag(tag);
        return this;
    }

    public Item setCompoundTag(byte[] tags) {
        this.tags = tags == null ? new byte[0] : tags;
        this.cachedNBT = null;
        return this;
    }

    public byte[] getCompoundTag() {
        return tags;
    }

    public boolean hasCompoundTag() {
        return this.tags != null && this.tags.length > 0;
    }

    public boolean hasCustomBlockData() {
        if (!this.hasCompoundTag()) {
            return false;
        }

        CompoundTag tag = this.getNamedTag();
        return tag.contains("BlockEntityTag") && tag.get("BlockEntityTag") instanceof CompoundTag;

    }

    public Item clearCustomBlockData() {
        if (!this.hasCompoundTag()) {
            return this;
        }
        CompoundTag tag = this.getNamedTag();

        if (tag.contains("BlockEntityTag") && tag.get("BlockEntityTag") instanceof CompoundTag) {
            tag.remove("BlockEntityTag");
            this.setNamedTag(tag);
        }

        return this;
    }

    public Item setCustomBlockData(CompoundTag compoundTag) {
        CompoundTag tags = compoundTag.copy();
        tags.setName("BlockEntityTag");

        CompoundTag tag;
        if (!this.hasCompoundTag()) {
            tag = new CompoundTag();
        } else {
            tag = this.getNamedTag();
        }

        tag.putCompound("BlockEntityTag", tags);
        this.setNamedTag(tag);

        return this;
    }

    public CompoundTag getCustomBlockData() {
        if (!this.hasCompoundTag()) {
            return null;
        }

        CompoundTag tag = this.getNamedTag();

        if (tag.contains("BlockEntityTag")) {
            Tag bet = tag.get("BlockEntityTag");
            if (bet instanceof CompoundTag) {
                return (CompoundTag) bet;
            }
        }

        return null;
    }

    public boolean applyEnchantments() {
        return true;
    }

    public boolean hasEnchantments() {
        if (!this.hasCompoundTag()) {
            return false;
        }

        CompoundTag tag = this.getNamedTag();

        if (tag.contains("ench")) {
            Tag enchTag = tag.get("ench");
            return enchTag instanceof ListTag;
        } else if (tag.contains("custom_ench")) {
            Tag enchTag = tag.get("custom_ench");
            return enchTag instanceof ListTag;
        }

        return false;
    }

    public boolean hasEnchantment(int id) {
        return this.getEnchantmentLevel(id) > 0;
    }

    public boolean hasEnchantment(short id) {
        return this.getEnchantment(id) != null;
    }

    public boolean hasCustomEnchantment(String id) {
        return this.getCustomEnchantmentLevel(id) > 0;
    }

    public boolean hasCustomEnchantment(@NotNull Identifier id) {
        return hasCustomEnchantment(id.toString());
    }

    public Enchantment getEnchantment(int id) {
        return getEnchantment((short) (id & 0xffff));
    }

    public Enchantment getEnchantment(short id) {
        if (!this.hasEnchantments()) {
            return null;
        }

        for (CompoundTag entry : this.getNamedTag().getList("ench", CompoundTag.class).getAll()) {
            if (entry.getShort("id") == id) {
                Enchantment enchantment = Enchantment.get(entry.getShort("id"));
                if (enchantment != null) {
                    enchantment.setLevel(entry.getShort("lvl"), false);
                    return enchantment;
                }
            }
        }

        return null;
    }

    public Enchantment getEnchantment(String id) {
        if (!this.hasEnchantments()) {
            return null;
        }

        if (this.hasCustomEnchantment(id)) {
            return this.getCustomEnchantment(id);
        }

        for (CompoundTag entry : this.getNamedTag().getList("ench", CompoundTag.class).getAll()) {
            Enchantment byId = Enchantment.get(id);
            if (entry.getShort("id") == byId.getId()) {
                Enchantment enchantment = Enchantment.get(entry.getShort("id"));
                if (enchantment != null) {
                    enchantment.setLevel(entry.getShort("lvl"), false);
                    return enchantment;
                }
            }
        }

        return null;
    }

    public Enchantment getCustomEnchantment(String id) {
        if (!this.hasEnchantments()) {
            return null;
        }

        for (CompoundTag entry : this.getNamedTag().getList("custom_ench", CompoundTag.class).getAll()) {
            if (entry.getString("id").equals(id)) {
                Enchantment enchantment = Enchantment.get(entry.getString("id"));
                if (enchantment != null) {
                    enchantment.setLevel(entry.getShort("lvl"), false);
                    return enchantment;
                }
            }
        }

        return null;
    }

    public Enchantment getCustomEnchantment(@NotNull Identifier id) {
        return getCustomEnchantment(id.toString());
    }

    public int getEnchantmentLevel(int id) {
        if (!this.hasEnchantments()) {
            return 0;
        }

        for (CompoundTag entry : this.getNamedTag().getList("ench", CompoundTag.class).getAll()) {
            if (entry.getShort("id") == id) {
                return entry.getShort("lvl");
            }
        }

        return 0;
    }

    public int getCustomEnchantmentLevel(String id) {
        if (!this.hasEnchantments()) {
            return 0;
        }
        for (CompoundTag entry : this.getNamedTag().getList("custom_ench", CompoundTag.class).getAll()) {
            if (entry.getString("id").equals(id)) {
                return entry.getShort("lvl");
            }
        }
        return 0;
    }

    public int getCustomEnchantmentLevel(@NotNull Identifier id) {
        return getCustomEnchantmentLevel(id.toString());
    }

    public Enchantment[] getEnchantments() {
        if (!this.hasEnchantments()) {
            return Enchantment.EMPTY_ARRAY;
        }
        List<Enchantment> enchantments = new ArrayList<>();

        ListTag<CompoundTag> enchants = this.getNamedTag().getList("ench", CompoundTag.class);
        for (CompoundTag entry : enchants.getAll()) {
            Enchantment e = Enchantment.get(entry.getShort("id"));
            if (e != null) {
                e.setLevel(entry.getShort("lvl"), false);
                enchantments.add(e);
            }
        }

        ListTag<CompoundTag> customEnchants = this.getNamedTag().getList("custom_ench", CompoundTag.class);
        for (CompoundTag entry : customEnchants.getAll()) {
            Enchantment e = Enchantment.get(entry.getString("id"));
            if (e != null) {
                e.setLevel(entry.getShort("lvl"), false);
                enchantments.add(e);
            }
        }

        return enchantments.toArray(Enchantment.EMPTY_ARRAY);
    }

    public void addEnchantment(Enchantment... enchantments) {
        CompoundTag tag = !this.hasCompoundTag() ? new CompoundTag() : this.getNamedTag();

        ListTag<CompoundTag> enchants;
        if (!tag.contains("ench")) {
            enchants = new ListTag<>();
            tag.putList("ench", enchants);
        } else {
            enchants = tag.getList("ench", CompoundTag.class);
        }

        ListTag<CompoundTag> customEnchants;
        if (!tag.contains("custom_ench")) {
            customEnchants = new ListTag<>();
            tag.putList("custom_ench", customEnchants);
        } else {
            customEnchants = tag.getList("custom_ench", CompoundTag.class);
        }

        for (Enchantment enchantment : enchantments) {
            boolean found = false;
            if (enchantment.getId() != Enchantment.CUSTOM_ENCHANTMENT_ID) {
                for (int k = 0; k < enchants.size(); k++) {
                    CompoundTag entry = enchants.get(k);
                    if (entry.getShort("id") == enchantment.getId()) {
                        enchants.add(k, new CompoundTag()
                                .putShort("id", enchantment.getId())
                                .putShort("lvl", enchantment.getLevel())
                        );
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    enchants.add(new CompoundTag()
                            .putShort("id", enchantment.getId())
                            .putShort("lvl", enchantment.getLevel())
                    );
                }
            } else {
                for (int k = 0; k < customEnchants.size(); k++) {
                    CompoundTag entry = customEnchants.get(k);
                    if (entry.getString("id").equals(enchantment.getIdentifier().toString())) {
                        customEnchants.add(k, new CompoundTag()
                                .putString("id", enchantment.getIdentifier().toString())
                                .putShort("lvl", enchantment.getLevel())
                        );
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    customEnchants.add(new CompoundTag()
                            .putString("id", enchantment.getIdentifier().toString())
                            .putShort("lvl", enchantment.getLevel())
                    );
                }
            }
        }

        this.setNamedTag(tag);

        var customEnchantmentDisplay = Server.getInstance().getCustomEnchantmentDisplay();
        if (customEnchantmentDisplay != null) {
            customEnchantmentDisplay.apply(this, customEnchants.getAll().stream()
                    .map(enchant -> Enchantment.get(
                            enchant.getString("id")).setLevel(
                            enchant.getShort("lvl")))
                    .toList());
        }
    }

    public boolean hasCustomName() {
        if (!this.hasCompoundTag()) {
            return false;
        }

        CompoundTag tag = this.getNamedTag();
        if (tag.contains("display")) {
            Tag tag1 = tag.get("display");
            return tag1 instanceof CompoundTag && ((CompoundTag) tag1).contains("Name") && ((CompoundTag) tag1).get("Name") instanceof StringTag;
        }

        return false;
    }

    public String getCustomName() {
        if (!this.hasCompoundTag()) {
            return "";
        }

        CompoundTag tag = this.getNamedTag();
        if (tag.contains("display")) {
            Tag tag1 = tag.get("display");
            if (tag1 instanceof CompoundTag && ((CompoundTag) tag1).contains("Name") && ((CompoundTag) tag1).get("Name") instanceof StringTag) {
                return ((CompoundTag) tag1).getString("Name");
            }
        }

        return "";
    }

    public Item setCustomName(String name) {
        if (name == null || name.isEmpty()) {
            this.clearCustomName();
            return this;
        }

        if (name.length() > 100) {
            name = name.substring(0, 100);
        }

        CompoundTag tag;
        if (!this.hasCompoundTag()) {
            tag = new CompoundTag();
        } else {
            tag = this.getNamedTag();
        }
        if (tag.contains("display") && tag.get("display") instanceof CompoundTag) {
            tag.getCompound("display")
                    .putString("Name", name);
        } else {
            tag.putCompound("display", new CompoundTag("display")
                    .putString("Name", name)
            );
        }
        this.setNamedTag(tag);
        return this;
    }

    public Item clearCustomName() {
        if (!this.hasCompoundTag()) {
            return this;
        }

        CompoundTag tag = this.getNamedTag();

        if (tag.contains("display") && tag.get("display") instanceof CompoundTag) {
            tag.getCompound("display").remove("Name");
            if (tag.getCompound("display").isEmpty()) {
                tag.remove("display");
            }

            this.setNamedTag(tag);
        }

        return this;
    }

    public String[] getLore() {
        Tag tag = this.getNamedTagEntry("display");
        ArrayList<String> lines = new ArrayList<>();

        if (tag instanceof CompoundTag) {
            CompoundTag nbt = (CompoundTag) tag;
            ListTag<StringTag> lore = nbt.getList("Lore", StringTag.class);

            if (lore.size() > 0) {
                for (StringTag stringTag : lore.getAll()) {
                    lines.add(stringTag.data);
                }
            }
        }

        return lines.toArray(new String[0]);
    }

    public Item setLore(String... lines) {
        CompoundTag tag;
        if (!this.hasCompoundTag()) {
            tag = new CompoundTag();
        } else {
            tag = this.getNamedTag();
        }
        ListTag<StringTag> lore = new ListTag<>("Lore");

        for (String line : lines) {
            lore.add(new StringTag("", line));
        }

        if (!tag.contains("display")) {
            tag.putCompound("display", new CompoundTag("display").putList(lore));
        } else {
            tag.getCompound("display").putList(lore);
        }

        this.setNamedTag(tag);
        return this;
    }

    public Tag getNamedTagEntry(String name) {
        CompoundTag tag = this.getNamedTag();
        if (tag != null) {
            return tag.contains(name) ? tag.get(name) : null;
        }

        return null;
    }

    public CompoundTag getNamedTag() {
        if (!this.hasCompoundTag()) {
            return null;
        }

        if (this.cachedNBT == null) {
            this.cachedNBT = parseCompoundTag(this.tags);
        }

        this.cachedNBT.setName("");

        return this.cachedNBT;
    }

    public CompoundTag getOrCreateNamedTag() {
        if (!this.hasCompoundTag()) {
            return new CompoundTag();
        }
        return this.getNamedTag();
    }

    public Item setNamedTag(CompoundTag tag) {
        if (tag.isEmpty()) {
            return this.clearNamedTag();
        }
        tag.setName(null);

        this.cachedNBT = tag;
        this.tags = writeCompoundTag(tag);

        return this;
    }

    public Item clearNamedTag() {
        return this.setCompoundTag(new byte[0]);
    }

    public static CompoundTag parseCompoundTag(byte[] tag) {
        try {
            return NBTIO.read(tag, ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] writeCompoundTag(CompoundTag tag) {
        try {
            tag.setName("");
            return NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isNull() {
        return this.count <= 0 || this.id == AIR;
    }

    final public String getName() {
        return this.hasCustomName() ? this.getCustomName() : this.name;
    }

    @NotNull
    final public String getDisplayName() {
        return this.hasCustomName() ? this.getCustomName() : this.name == null ? StringItem.createItemName(getNamespaceId()) : name;
    }

    final public boolean canBePlaced() {
        return ((this.block != null) && this.block.canBePlaced());
    }

    @NotNull
    public Block getBlock() {
        if (this.block != null) {
            return this.block.clone();
        } else {
            return Block.get(BlockID.AIR);
        }
    }

    public Block getBlockUnsafe() {
        return this.block;
    }

    public int getBlockId() {
        return block == null ? 0 : block.getId();
    }

    public int getId() {
        return id;
    }

    /**
     * Gets the type of the item.
     *
     * @return ItemType
     */
    public ItemType getItemType() {
        if (this.type != null) {
            return this.type;
        }

        if (this instanceof StringItem stringItem) {
            this.type = ItemTypes.get(stringItem.getNamespaceId());
        } else if (this.block instanceof CustomBlock customBlock) {
            this.type = ItemTypes.get(customBlock.getIdentifier());
        } else {
            var mappings = RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL);
            var entry = mappings.toRuntime(this.getId(), this.getDamage());
            this.type = ItemTypes.get(entry.getIdentifier());
        }

        // Throw an exception if for some reason the type cannot be determined.
        if (this.type == null) {
            throw new IllegalStateException("Failed to initialize item type " + this.getName() + ": " + this.getId() + ":" + this.getDamage());
        }

        return this.type;
    }

    /**
     * Gets all item tags.
     *
     * @return Set<ItemTag>
     */
    public Set<ItemTag> getItemTags() {
        return ItemTags.getTagsSet(this.getIdentifier());
    }

    /**
     * Checks whether the item has a tag.
     *
     * @param itemTag ItemTag to check
     * @return true if there is, otherwise false
     */
    public boolean hasItemTag(ItemTag itemTag) {
        return this.getItemTags().contains(itemTag);
    }

    /**
     * Gets the item string identifier from the type.
     *
     * @return String identifier
     */
    public String getIdentifier() {
        return this.getItemType().getIdentifier();
    }

    public int getDamage() {
        return meta == 0xffff ? 0 : meta;
    }

    public void setDamage(Integer meta) {
        if (meta != null) {
            this.meta = meta & 0xffff;
        } else {
            this.hasMeta = false;
        }
    }

    public int getMaxStackSize() {
        return 64;
    }

    final public Integer getFuelTime() {
        return Registries.FUEL.get(getNamespaceId());
    }

    public boolean useOn(Entity entity) {
        return false;
    }

    public boolean useOn(Block block) {
        return false;
    }

    public boolean isTool() {
        return false;
    }

    public int getMaxDurability() {
        return -1;
    }

    public int getTier() {
        return 0;
    }

    public boolean isPickaxe() {
        return false;
    }

    public boolean isAxe() {
        return false;
    }

    public boolean isSword() {
        return false;
    }

    public boolean isMace() {
        return false;
    }

    public boolean isShovel() {
        return false;
    }

    public boolean isSpear() {
        return false;
    }

    public boolean isHoe() {
        return false;
    }

    public boolean isShears() {
        return false;
    }

    public boolean isArmor() {
        return false;
    }

    public boolean isHelmet() {
        return false;
    }

    public boolean canBePutInHelmetSlot() {
        return false;
    }

    public boolean isChestplate() {
        return false;
    }

    public boolean isLeggings() {
        return false;
    }

    public boolean isBoots() {
        return false;
    }

    public int getEnchantAbility() {
        return 0;
    }

    public int getAttackDamage() {
        return 1;
    }

    public int getAttackDamage(Entity entity) {
        return this.getAttackDamage();
    }

    public int getArmorPoints() {
        return 0;
    }

    public int getToughness() {
        return 0;
    }

    public boolean isUnbreakable() {
        if (!(this instanceof ItemDurable)) {
            return false;
        }

        Tag tag = this.getNamedTagEntry("Unbreakable");
        return tag instanceof ByteTag byteTag && byteTag.data > 0;
    }

    public Item setUnbreakable(boolean value) {
        if (!(this instanceof ItemDurable)) {
            return this;
        }

        CompoundTag tag = this.getOrCreateNamedTag();
        this.setNamedTag(tag.putByte("Unbreakable", value ? 1 : 0));
        return this;
    }

    public Item setUnbreakable() {
        return this.setUnbreakable(true);
    }

    public boolean canBreakShield() {
        return false;
    }

    public boolean onUse(Player player, int ticksUsed) {
        return false;
    }

    public boolean onRelease(Player player, int ticksUsed) {
        return false;
    }

    public boolean onAttack(Player player, Entity entity) {
        return false;
    }

    @Override
    final public String toString() {
        return "Item " + this.name + " (" + (this instanceof StringItem ? this.getNamespaceId() : this.id) + ':' + (!this.hasMeta ? "?" : this.meta) + ")x" + this.count + (this.hasCompoundTag() ? " tags:0x" + Binary.bytesToHexString(this.getCompoundTag()) : "");
    }

    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        return false;
    }

    /**
     * Called when a player uses the item on air, for example throwing a projectile.
     * Returns whether the item was changed, for example count decrease or durability change.
     *
     * @param player          player
     * @param directionVector direction
     * @return item changed
     */
    public boolean onClickAir(Player player, Vector3 directionVector) {
        return false;
    }

    public boolean canRelease() {
        return false;
    }

    /**
     * Returns a new item instance with count decreased by amount or air if new count is less or equal to 0
     */
    public final Item decrement(int amount) {
        return increment(-amount);
    }

    /**
     * Returns a new item instance with count increased by amount or air if new count is less or equal to 0
     */
    public final Item increment(int amount) {
        if (count + amount <= 0) {
            return get(0);
        }
        Item cloned = clone();
        cloned.count += amount;
        return cloned;
    }

    @Override
    public final boolean equals(Object item) {
        return item instanceof Item && this.equals((Item) item, true);
    }

    public final boolean equals(Item item, boolean checkDamage) {
        return equals(item, checkDamage, true);
    }

    public final boolean equals(Item item, boolean checkDamage, boolean checkCompound) {
        if (this.id == STRING_IDENTIFIED_ITEM && item.id == STRING_IDENTIFIED_ITEM) {
            if (!this.getNamespaceId(ProtocolInfo.CURRENT_PROTOCOL).equals(item.getNamespaceId(ProtocolInfo.CURRENT_PROTOCOL))) {
                return false;
            }
        } else if (this.id != item.id) {
            return false;
        }
        if (!checkDamage || this.meta == item.meta) {
            if (checkCompound) {
                if (Arrays.equals(this.getCompoundTag(), item.getCompoundTag())) {
                    return true;
                } else if (this.hasCompoundTag() && item.hasCompoundTag()) {
                    return this.getNamedTag().equals(item.getNamedTag());
                }
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the specified item stack has the same ID, damage, NBT and count as this item stack.
     *
     * @param other item
     * @return equal
     */
    public final boolean equalsExact(Item other) {
        return this.equals(other, true, true) && this.count == other.count;
    }

    public final boolean equalsFast(Item other) {
        if (this.id == STRING_IDENTIFIED_ITEM && other.id == STRING_IDENTIFIED_ITEM) {
            if (!this.getNamespaceId().equals(other.getNamespaceId())) {
                return false;
            }
        }
        return other != null && other.id == this.id && other.meta == this.meta;
    }

    public final boolean deepEquals(Item item) {
        return equals(item, true);
    }

    public final boolean deepEquals(Item item, boolean checkDamage) {
        return equals(item, checkDamage, true);
    }

    public final boolean deepEquals(Item item, boolean checkDamage, boolean checkCompound) {
        return equals(item, checkDamage, checkCompound);
    }

    public int getRepairCost() {
        if (this.hasCompoundTag()) {
            CompoundTag tag = this.getNamedTag();
            if (tag.contains("RepairCost")) {
                Tag repairCost = tag.get("RepairCost");
                if (repairCost instanceof IntTag) {
                    return ((IntTag) repairCost).data;
                }
            }
        }
        return 0;
    }

    public Item setRepairCost(int cost) {
        if (cost <= 0 && this.hasCompoundTag()) {
            return this.setNamedTag(this.getNamedTag().remove("RepairCost"));
        }

        CompoundTag tag;
        if (!this.hasCompoundTag()) {
            tag = new CompoundTag();
        } else {
            tag = this.getNamedTag();
        }
        return this.setNamedTag(tag.putInt("RepairCost", cost));
    }

    @Override
    public Item clone() {
        try {
            Item item = (Item) super.clone();
            item.tags = this.tags.clone();
            item.cachedNBT = null;
            return item;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Deprecated
    public final RuntimeEntry getRuntimeEntry() {
        Server.mvw("Item#getRuntimeEntry()");
        return this.getRuntimeEntry(ProtocolInfo.CURRENT_PROTOCOL);
    }

    @Deprecated
    public final RuntimeEntry getRuntimeEntry(int protocolId) {
        return RuntimeItems.getMapping(protocolId).toRuntime(this.getId(), this.getDamage());
    }

    public final int getNetworkId() {
        Server.mvw("Item#getNetworkId()");
        return this.getNetworkId(ProtocolInfo.CURRENT_PROTOCOL);
    }

    public final int getNetworkId(int protocolId) {
        return RuntimeItems.getMapping(protocolId).getNetworkId(this);
    }

    public String getNamespaceId() {
        return this.getNamespaceId(ProtocolInfo.CURRENT_PROTOCOL);
    }

    public String getNamespaceId(int protocolId) {
        if (this.getId() == 0) {
            return "minecraft:air";
        }
        RuntimeItemMapping runtimeMapping = RuntimeItems.getMapping(protocolId);
        return runtimeMapping.getNamespacedIdByNetworkId(this.getNetworkId(protocolId));
    }

    /**
     * 返回物品是否支持指定版本
     * <p>
     * Returns whether the item supports the specified version
     *
     * @param protocolId 协议版本 protocol version
     * @return 是否支持 whether supported
     */
    public boolean isSupportedOn(int protocolId) {
        int itemId = this.getId();

        if (itemId >= 0 && itemId <= 255) {
            return true;
        }

        return RuntimeItems.getMapping(protocolId).isRegistered(itemId, this.getDamage());
    }

    /**
     * 设置物品锁定在玩家的物品栏的模式
     *
     * @param mode lock mode
     */
    public void setItemLockMode(ItemLockMode mode) {
        CompoundTag tag = getOrCreateNamedTag();
        if (mode == ItemLockMode.NONE) {
            tag.remove("minecraft:item_lock");
        } else {
            tag.putByte("minecraft:item_lock", mode.ordinal());
        }
        this.setCompoundTag(tag);
    }

    /**
     * 获取物品锁定在玩家的物品栏的模式
     * <p>
     * Get items locked mode in the player's item inventory
     *
     * @return lock mode
     */
    public ItemLockMode getItemLockMode() {
        CompoundTag tag = getOrCreateNamedTag();
        if (tag.contains("minecraft:item_lock")) {
            return ItemLockMode.values()[tag.getByte("minecraft:item_lock")];
        }
        return ItemLockMode.NONE;
    }

    public enum ItemLockMode {
        NONE,//only used in server
        LOCK_IN_SLOT,
        LOCK_IN_INVENTORY
    }
}