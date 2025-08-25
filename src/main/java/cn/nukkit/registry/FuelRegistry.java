package cn.nukkit.registry;

import cn.nukkit.block.material.BlockType;
import cn.nukkit.block.material.tags.BlockInternalTags;
import cn.nukkit.block.material.tags.BlockTag;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.material.ItemType;
import cn.nukkit.item.material.tags.ItemTag;
import cn.nukkit.item.material.tags.ItemTags;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

public class FuelRegistry implements IRegistry<String, Integer, Integer> {

    private static final Object2ObjectOpenHashMap<String, Integer> ID_TO_BURNING_TIME = new Object2ObjectOpenHashMap<>();
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        register(ItemNamespaceId.LAVA_BUCKET, 20000);
        register(ItemNamespaceId.COAL_BLOCK, 16000);
        register(ItemNamespaceId.DRIED_KELP_BLOCK, 4000);
        register(ItemNamespaceId.BLAZE_ROD, 2400);
        register(ItemNamespaceId.COAL, 1600);
        register(ItemNamespaceId.CHARCOAL, 1600);
        register(ItemNamespaceId.BAMBOO_MOSAIC, 300);
        register(ItemNamespaceId.BAMBOO_MOSAIC_SLAB, 300);
        register(ItemNamespaceId.BAMBOO_MOSAIC_STAIRS, 300);
        register(ItemNamespaceId.CHISELED_BOOKSHELF, 300);
        register(ItemNamespaceId.BAMBOO_BLOCK, 300);
        register(ItemNamespaceId.STRIPPED_BAMBOO_BLOCK, 300);
        register(ItemNamespaceId.MANGROVE_ROOTS, 300);
        register(ItemNamespaceId.CRAFTING_TABLE, 300);
        register(ItemNamespaceId.CARTOGRAPHY_TABLE, 300);
        register(ItemNamespaceId.FLETCHING_TABLE, 300);
        register(ItemNamespaceId.SMITHING_TABLE, 300);
        register(ItemNamespaceId.LOOM, 300);
        register(ItemNamespaceId.BOOKSHELF, 300);
        register(ItemNamespaceId.LECTERN, 300);
        register(ItemNamespaceId.COMPOSTER, 300);
        register(ItemNamespaceId.CHEST, 300);
        register(ItemNamespaceId.TRAPPED_CHEST, 300);
        register(ItemNamespaceId.BARREL, 300);
        register(ItemNamespaceId.DAYLIGHT_DETECTOR, 300);
        register(ItemNamespaceId.JUKEBOX, 300);
        register(ItemNamespaceId.NOTEBLOCK, 300);
        register(ItemNamespaceId.BANNER, 300);
        register(ItemNamespaceId.CROSSBOW, 200);
        register(ItemNamespaceId.BOW, 200);
        register(ItemNamespaceId.FISHING_ROD, 300);
        register(ItemNamespaceId.WOODEN_PICKAXE, 200);
        register(ItemNamespaceId.WOODEN_SHOVEL, 200);
        register(ItemNamespaceId.WOODEN_HOE, 200);
        register(ItemNamespaceId.WOODEN_AXE, 200);
        register(ItemNamespaceId.WOODEN_SWORD, 200);
        register(ItemNamespaceId.BOWL, 200);
        register(ItemNamespaceId.STICK, 100);
        register(ItemNamespaceId.DEADBUSH, 100);
        register(ItemNamespaceId.AZALEA, 100);
        register(ItemNamespaceId.FLOWERING_AZALEA, 100);
        register(ItemNamespaceId.LEAF_LITTER, 100);
        register(ItemNamespaceId.SHORT_DRY_GRASS, 100);
        register(ItemNamespaceId.TALL_DRY_GRASS, 100);
        register(ItemNamespaceId.BAMBOO, 50);
        register(ItemNamespaceId.SCAFFOLDING, 50);
        registerByItemTag(ItemTags.BOAT, 1200);
        registerByItemTag(ItemTags.LOGS_THAT_BURN, 300);
        registerByItemTag(ItemTags.PLANKS, 300);
        registerByItemTag(ItemTags.WOODEN_SLABS, 300);
        registerByItemTag(ItemTags.SIGN, 200);
        registerByBlockTag(BlockInternalTags.WOODEN_STAIRS, 300);
        registerByBlockTag(BlockInternalTags.WOODEN_PRESSURE_PLATE, 300);
        registerByBlockTag(BlockInternalTags.WOODEN_BUTTON, 300);
        registerByBlockTag(BlockInternalTags.WOODEN_TRAPDOOR, 300);
        registerByBlockTag(BlockInternalTags.WOODEN_FENCE, 300);
        registerByBlockTag(BlockInternalTags.WOODEN_FENCE_GATE, 300);
        registerByBlockTag(BlockInternalTags.WOODEN_DOOR, 200);
        registerByBlockTag(BlockInternalTags.SAPLING, 100);
    }

    @Override
    public void register(String key, Integer value) {
        ID_TO_BURNING_TIME.put(key, value);
    }

    public void registerByItemTag(ItemTag tag, Integer value) {
        for (ItemType type : tag.getItemTypes()) {
            register(type.getIdentifier(), value);
        }
    }

    public void registerByBlockTag(BlockTag tag, Integer value) {
        for (BlockType type : tag.getBlockTypes()) {
            register(type.getIdentifier(), value);
        }
    }

    @Override
    public Integer get(String key) {
        return ID_TO_BURNING_TIME.get(key);
    }

    public boolean hasBurningTime(String key) {
        return ID_TO_BURNING_TIME.containsKey(key);
    }

    @Override
    public void trim() {
        ID_TO_BURNING_TIME.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        ID_TO_BURNING_TIME.clear();
        init();
    }
}
