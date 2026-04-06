package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.material.BlockType;
import cn.nukkit.block.material.tags.BlockInternalTags;
import cn.nukkit.block.material.tags.BlockTag;
import cn.nukkit.event.block.ComposterEmptyEvent;
import cn.nukkit.event.block.ComposterFillEvent;
import cn.nukkit.item.*;
import cn.nukkit.item.material.ItemType;
import cn.nukkit.item.material.tags.ItemTag;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Sound;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

public class BlockComposter extends BlockSolidMeta implements ItemID {

    private static final Object2IntOpenHashMap<String> ITEMS = new Object2IntOpenHashMap<>();

    static {
        registerByBlockTag(BlockInternalTags.SAPLING, 30);
        registerByBlockTag(BlockInternalTags.LEAVES, 30);
        registerByBlockTag(BlockInternalTags.FLOWERS, 65);
        register(30, ItemNamespaceId.BEETROOT_SEEDS, ItemNamespaceId.BUSH, ItemNamespaceId.CACTUS_FLOWER, ItemNamespaceId.DRIED_KELP, ItemNamespaceId.FIREFLY_BUSH, ItemNamespaceId.GLOW_BERRIES, ItemNamespaceId.GRASS_BLOCK, ItemNamespaceId.HANGING_ROOTS, ItemNamespaceId.KELP, ItemNamespaceId.LEAF_LITTER, ItemNamespaceId.MANGROVE_PROPAGULE, ItemNamespaceId.MANGROVE_ROOTS, ItemNamespaceId.MELON_SEEDS, ItemNamespaceId.MOSS_CARPET, ItemNamespaceId.PALE_HANGING_MOSS, ItemNamespaceId.PALE_MOSS_CARPET, ItemNamespaceId.PINK_PETALS, ItemNamespaceId.PITCHER_POD, ItemNamespaceId.PUMPKIN_SEEDS, ItemNamespaceId.SEAGRASS, ItemNamespaceId.SHORT_GRASS, ItemNamespaceId.SHORT_DRY_GRASS, ItemNamespaceId.SMALL_DRIPLEAF_BLOCK, ItemNamespaceId.SWEET_BERRIES, ItemNamespaceId.TALL_DRY_GRASS, ItemNamespaceId.TORCHFLOWER_SEEDS, ItemNamespaceId.WHEAT_SEEDS, ItemNamespaceId.WILDFLOWERS);
        register(50, ItemNamespaceId.CACTUS, ItemNamespaceId.DRIED_KELP_BLOCK, ItemNamespaceId.AZALEA_LEAVES_FLOWERED, ItemNamespaceId.GLOW_LICHEN, ItemNamespaceId.MELON_SLICE, ItemNamespaceId.NETHER_SPROUTS, ItemNamespaceId.SUGAR_CANE, ItemNamespaceId.TALL_GRASS, ItemNamespaceId.TWISTING_VINES, ItemNamespaceId.VINE, ItemNamespaceId.WEEPING_VINES);
        register(65, ItemNamespaceId.APPLE, ItemNamespaceId.AZALEA, ItemNamespaceId.BEETROOT, ItemNamespaceId.BIG_DRIPLEAF, ItemNamespaceId.CARROT, ItemNamespaceId.COCOA_BEANS, ItemNamespaceId.FERN, ItemNamespaceId.LARGE_FERN, ItemNamespaceId.WATERLILY, ItemNamespaceId.MELON_BLOCK, ItemNamespaceId.MOSS_BLOCK, ItemNamespaceId.RED_MUSHROOM, ItemNamespaceId.BROWN_MUSHROOM, ItemNamespaceId.MUSHROOM_STEM, ItemNamespaceId.CRIMSON_FUNGUS, ItemNamespaceId.WARPED_FUNGUS, ItemNamespaceId.PALE_MOSS_BLOCK, ItemNamespaceId.POTATO, ItemNamespaceId.PUMPKIN, ItemNamespaceId.CARVED_PUMPKIN, ItemNamespaceId.CRIMSON_ROOTS, ItemNamespaceId.WARPED_ROOTS, ItemNamespaceId.SEA_PICKLE, ItemNamespaceId.SHROOMLIGHT, ItemNamespaceId.SPORE_BLOSSOM, ItemNamespaceId.WHEAT, ItemNamespaceId.WITHER_ROSE);
        register(85, ItemNamespaceId.BAKED_POTATO, ItemNamespaceId.BREAD, ItemNamespaceId.COOKIE, ItemNamespaceId.FLOWERING_AZALEA, ItemNamespaceId.HAY_BLOCK, ItemNamespaceId.BROWN_MUSHROOM_BLOCK, ItemNamespaceId.RED_MUSHROOM_BLOCK, ItemNamespaceId.NETHER_WART_BLOCK, ItemNamespaceId.PITCHER_PLANT, ItemNamespaceId.TORCHFLOWER, ItemNamespaceId.WARPED_WART_BLOCK);
        register(100, ItemNamespaceId.CAKE, ItemNamespaceId.PUMPKIN_PIE);
        register(ItemNamespaceId.GOLDEN_DANDELION, 0); //Override chance registered by flowers tag
    }

    public BlockComposter() {
        this(0);
    }

    public BlockComposter(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return COMPOSTER;
    }

    @Override
    public String getName() {
        return "Composter";
    }

    @Override
    public double getHardness() {
        return 0.6;
    }

    @Override
    public double getResistance() {
        return 0.6;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride() {
        return this.getDamage();
    }

    public boolean incrementLevel() {
        int damage = this.getDamage() + 1;
        this.setDamage(damage);
        this.level.setBlock(this, this, true, true);
        return damage == 8;
    }

    public boolean isFull() {
        return this.getDamage() == 8;
    }

    public boolean isEmpty() {
        return this.getDamage() == 0;
    }

    @Override
    public boolean onActivate(@Nonnull Item item, Player player) {
        if (this.isFull()) {
            empty(item, player);
            return true;
        }

        int chance = getChance(item);
        if (chance <= 0) {
            return false;
        }

        return !this.addItem(item, player, chance);
    }

    public boolean addItem(@NotNull Item item, Player player, int chance) {
        boolean success = ThreadLocalRandom.current().nextInt(100) < chance;
        ComposterFillEvent event = new ComposterFillEvent(this, player, item, chance, success);
        this.level.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        if (player != null &&!player.isCreative()) {
            item.setCount(item.getCount() - 1);
            player.getInventory().setItemInHand(item);
        }

        if (event.isSuccess()) {
            level.addParticleEffect(this.add(0.5, 0.5, 0.5), ParticleEffect.CROP_GROWTH, -1, level.getDimension(), (Player[]) null);
            if (this.incrementLevel()) {
                level.addSound(this.add(0.5, 0.5, 0.5), Sound.BLOCK_COMPOSTER_READY);
            } else {
                level.addSound(this.add(0.5, 0.5, 0.5), Sound.BLOCK_COMPOSTER_FILL_SUCCESS);
            }
        } else {
            level.addSound(this.add(0.5, 0.5, 0.5), Sound.BLOCK_COMPOSTER_FILL);
        }
        return true;
    }

    public Item empty() {
        return empty(null, null);
    }

    public Item empty(Player player) {
        return this.empty(null, player);
    }

    public Item empty(Item item, Player player) {
        if (isEmpty()) {
            return null;
        }
        ComposterEmptyEvent event = new ComposterEmptyEvent(this, player, item, new ItemBoneMeal(), 0);
        this.level.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.setDamage(event.getNewLevel());
            this.level.setBlock(this, this, true, true);
            if (item != null) {
                this.level.dropItem(add(0.5, 0.85, 0.5), event.getDrop());
            }
            this.level.addSound(add(0.5 , 0.5, 0.5), Sound.BLOCK_COMPOSTER_EMPTY);
            return event.getDrop();
        }
        return null;
    }

    public static void register(int value, String... keys) {
        for(String key : keys) {
            register(key, value);
        }
    }

    public static void register(String key, int value) {
        ITEMS.put(key, value);
    }

    public static void registerByItemTag(ItemTag tag, int value) {
        for (ItemType type : tag.getItemTypes()) {
            register(type.getIdentifier(), value);
        }
    }

    public static void registerByBlockTag(BlockTag tag, int value) {
        for (BlockType type : tag.getBlockTypes()) {
            register(type.getIdentifier(), value);
        }
    }

    public static int getChance(Item item) {
        return ITEMS.getOrDefault(item.getNamespaceId(), 0);
    }
}