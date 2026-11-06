package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.EnumBlockProperty;
import cn.nukkit.block.customblock.properties.IntBlockProperty;
import cn.nukkit.block.data.BlockColor;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.enums.CauldronLiquid;
import cn.nukkit.block.properties.enums.PotionType;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntityCauldron;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.event.entity.EntityCombustByBlockEvent;
import cn.nukkit.event.entity.EntityDamageByBlockEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerBucketEmptyEvent;
import cn.nukkit.event.player.PlayerBucketFillEvent;
import cn.nukkit.item.*;
import cn.nukkit.item.material.tags.ItemTags;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.LavaParticle;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.protocol.LevelEventPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author CreeperFace (Nukkit Project)
 */
public class BlockCauldron extends BlockSolidMeta implements BlockEntityHolder<BlockEntityCauldron>, BlockPropertiesHelper {

    private static final IntBlockProperty FILL_LEVEL = new IntBlockProperty("fill_level", false, 6);
    private static final EnumBlockProperty<CauldronLiquid> CAULDRON_LIQUID = new EnumBlockProperty<>("cauldron_liquid", false, CauldronLiquid.class);

    private static final BlockProperties PROPERTIES = new BlockProperties(FILL_LEVEL, CAULDRON_LIQUID);

    public BlockCauldron() {
        super(0);
    }

    public BlockCauldron(int meta) {
        super(meta);
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    @NotNull
    public String getBlockEntityType() {
        return BlockEntity.CAULDRON;
    }

    @Override
    @NotNull
    public Class<? extends BlockEntityCauldron> getBlockEntityClass() {
        return BlockEntityCauldron.class;
    }

    @Override
    public String getName() {
        return getCauldronLiquid() == CauldronLiquid.LAVA ? "Lava Cauldron" : "Cauldron Block";
    }

    @Override
    public int getId() {
        return Block.CAULDRON_BLOCK;
    }

    @Override
    public double getResistance() {
        return 10;
    }

    @Override
    public double getHardness() {
        return 2;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    public boolean isFull() {
        return getFillLevel() >= 6;
    }

    public boolean isEmpty() {
        return getFillLevel() == 0;
    }

    public int getFillLevel() {
        return this.getIntValue(FILL_LEVEL);
    }

    public void setFillLevel(int fillLevel) {
        fillLevel = MathHelper.clamp(fillLevel, 0, 6);
        this.setPropertyValue(FILL_LEVEL, fillLevel);
    }

    public CauldronLiquid getCauldronLiquid() {
        return this.getPropertyValue(CAULDRON_LIQUID);
    }

    public void setCauldronLiquid(CauldronLiquid liquid) {
        this.setPropertyValue(CAULDRON_LIQUID, liquid);
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (getCauldronLiquid() == CauldronLiquid.LAVA && !isEmpty()) {
            return onLavaActivate(item, player);
        }

        BlockEntityCauldron cauldron = getBlockEntity();
        if (cauldron == null) {
            return false;
        }

        String itemIdentifier = item.getNamespaceId();
        PotionType potionType = switch (itemIdentifier) {
            case ItemNamespaceId.POTION -> PotionType.NORMAL;
            case ItemNamespaceId.SPLASH_POTION -> PotionType.SPLASH;
            case ItemNamespaceId.LINGERING_POTION -> PotionType.LINGERING;
            default -> null;
        };
        if (potionType != null) {
            pourPotion(item, potionType, player, cauldron);
        } else if (itemIdentifier.equals(ItemNamespaceId.GLASS_BOTTLE)) {
            if (!isEmpty() && getCauldronLiquid() == CauldronLiquid.WATER) {
                fillBottle(item, player, cauldron);
            }
        } else if (item instanceof ItemBucket bucket) {
            if (itemIdentifier.equals(ItemNamespaceId.BUCKET)) {
                if (cauldron.isCustomColor() && !cauldron.hasPotion() && getFillLevel() > 0) {
                    PlayerBucketFillEvent ev = new PlayerBucketFillEvent(player, this, null, item, Item.get(ItemNamespaceId.WATER_BUCKET, null, 1, bucket.getCompoundTag()));
                    if (ev.call()) {
                        replaceBucket(bucket, player, ev.getItem());
                        this.setFillLevel(0);
                        this.level.setBlock(this, this, true);
                        cauldron.clearCustomColor();
                        this.getLevel().addLevelEvent(this.add(0.5, 0.375 + getFillLevel() * 0.125, 0.5), LevelEventPacket.EVENT_CAULDRON_TAKE_WATER);
                    }
                    return true;
                }
                if (isFull() && !cauldron.isCustomColor() && !cauldron.hasPotion()) {
                    PlayerBucketFillEvent ev = new PlayerBucketFillEvent(player, this, null, item, Item.get(getFilledBucketIdentifier(), null, 1, bucket.getCompoundTag()));
                    if (ev.call()) {
                        replaceBucket(bucket, player, ev.getItem());
                        this.setFillLevel(0);
                        this.level.setBlock(this, this, true);
                        cauldron.clearCustomColor();
                        this.getLevel().addLevelEvent(this.add(0.5, 0.375 + getFillLevel() * 0.125, 0.5), LevelEventPacket.EVENT_CAULDRON_TAKE_WATER);
                    }
                } else {
                    return false;
                }
            } else if ((itemIdentifier.equals(ItemNamespaceId.WATER_BUCKET) || itemIdentifier.equals(ItemNamespaceId.LAVA_BUCKET) || itemIdentifier.equals(ItemNamespaceId.POWDER_SNOW_BUCKET)) && !(isFull() && !cauldron.isCustomColor() && !cauldron.hasPotion() && itemIdentifier.equals(ItemNamespaceId.WATER_BUCKET))) {
                PlayerBucketEmptyEvent ev = new PlayerBucketEmptyEvent(player, this, null, item, Item.get(ItemNamespaceId.BUCKET, null, 1, bucket.getCompoundTag()));
                if (ev.call()) {
                    if (player.isSurvival() || player.isAdventure()) {
                        replaceBucket(bucket, player, ev.getItem());
                    }
                    if (cauldron.hasPotion()) {
                        clearWithFizz(cauldron);
                    } else if (itemIdentifier.equals(ItemNamespaceId.WATER_BUCKET)) {
                        this.setFillLevel(6);
                        cauldron.clearCustomColor();
                        this.level.setBlock(this, this, true);
                        this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.CAULDRON_FILLWATER);
                    } else if (itemIdentifier.equals(ItemNamespaceId.POWDER_SNOW_BUCKET)) {
                        this.setFillLevel(6);
                        setCauldronLiquid(CauldronLiquid.POWDER_SNOW);
                        cauldron.clearCustomColor();
                        this.level.setBlock(this, this, true);
                    } else if (isEmpty()) {
                        setCauldronLiquid(CauldronLiquid.LAVA);
                        this.setFillLevel(6);
                        this.level.setBlock(this, this, true);
                        cauldron.clearCustomColor();
                        cauldron.setType(PotionType.LAVA);
                        this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.BUCKET_EMPTY_LAVA);
                        for (int i = 0; i < 2; i++) {
                            this.getLevel().addParticle(new LavaParticle(this.add(0.5 + Math.random() * 0.4 - 0.2, 1.0, 0.5 + Math.random() * 0.4 - 0.2)));
                        }
                    } else {
                        clearWithFizz(cauldron);
                    }
                }
            }
        } else if (!isEmpty() && !cauldron.hasPotion()) {
            if (item instanceof ItemDye) {
                if (player.isSurvival() || player.isAdventure()) {
                    item.setCount(item.getCount() - 1);
                    player.getInventory().setItemInHand(item);
                }

                BlockColor color = ((ItemDye) item).getDyeColor().getBlockColor();
                if (!cauldron.isCustomColor()) {
                    cauldron.setCustomColor(color);
                } else {
                    BlockColor current = cauldron.getCustomColor();
                    BlockColor mixed = new BlockColor(
                            (int) Math.round(Math.sqrt(color.getRed() * current.getRed()) * 0.965),
                            (int) Math.round(Math.sqrt(color.getGreen() * current.getGreen()) * 0.965),
                            (int) Math.round(Math.sqrt(color.getBlue() * current.getBlue()) * 0.965)
                    );
                    cauldron.setCustomColor(mixed);
                }
                this.level.addSound(this.add(0.5, 0.5, 0.5), Sound.CAULDRON_ADDDYE);
            } else if (isLeatherArmor(item)) {
                if (cauldron.isCustomColor()) {
                    CompoundTag compoundTag = item.hasCompoundTag() ? item.getNamedTag() : new CompoundTag();
                    compoundTag.putInt("customColor", cauldron.getCustomColor().getRGB());
                    item.setCompoundTag(compoundTag);
                    player.getInventory().setItemInHand(item);
                    setFillLevel(getFillLevel() - 1);
                    this.level.setBlock(this, this, true, true);
                    this.level.addSound(add(0.5, 0.5, 0.5), Sound.CAULDRON_DYEARMOR);
                } else if (item.hasCompoundTag() && item.getNamedTag().exist("customColor")) {
                    CompoundTag compoundTag = item.getNamedTag();
                    compoundTag.remove("customColor");
                    item.setCompoundTag(compoundTag);
                    player.getInventory().setItemInHand(item);

                    setFillLevel(getFillLevel() - 1);
                    this.level.setBlock(this, this, true, true);
                    this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.CAULDRON_TAKEWATER);
                }
            } else if (item.getNamespaceId().equals(ItemNamespaceId.BANNER) && item instanceof ItemBanner banner && banner.hasPattern()) {
                banner.removePattern(banner.getPatternsSize() - 1);
                boolean consumeBanner = player.isSurvival() || player.isAdventure();
                if (consumeBanner && item.getCount() < item.getMaxStackSize()) {
                    player.getInventory().setItemInHand(banner);
                } else {
                    if (consumeBanner) {
                        item.setCount(item.getCount() - 1);
                        player.getInventory().setItemInHand(item);
                    }

                    if (player.getInventory().canAddItem(banner)) {
                        player.getInventory().addItem(banner);
                    } else {
                        player.getLevel().dropItem(player.add(0, 1.3, 0), banner, player.getDirectionVector().multiply(0.4));
                    }
                }
                setFillLevel(NukkitMath.clamp(getFillLevel() - 2, 0, 6));
                this.level.setBlock(this, this, true, true);
                this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.CAULDRON_TAKEWATER);
            }
        }

        this.level.updateComparatorOutputLevel(this);
        return true;
    }

    private void pourPotion(Item item, PotionType type, Player player, BlockEntityCauldron cauldron) {
        if (!isEmpty()) {
            if (getCauldronLiquid() != CauldronLiquid.WATER) {
                return;
            }
            int storedPotionId = cauldron.hasPotion() ? cauldron.getPotionId() : 0;
            PotionType storedType = cauldron.hasPotion() ? cauldron.getType() : PotionType.NORMAL;
            if (storedPotionId != item.getDamage() || storedType != type) {
                clearWithFizz(cauldron);
                consumePotion(item, player);
                return;
            }
            if (isFull()) {
                return;
            }
        } else {
            setCauldronLiquid(CauldronLiquid.WATER);
            cauldron.clearCustomColor();
        }

        cauldron.setType(type);
        // Ordinary water remains water, including when added from a bottle.
        cauldron.setPotionId(item.getDamage() == 0 && type == PotionType.NORMAL ? -1 : item.getDamage());
        setFillLevel(getFillLevel() + 2);
        this.level.setBlock(this, this, true);
        consumePotion(item, player);
        this.level.addLevelEvent(this.add(0.5, 0.375 + getFillLevel() * 0.125, 0.5), LevelEventPacket.EVENT_CAULDRON_FILL_POTION);
    }

    private void fillBottle(Item item, Player player, BlockEntityCauldron cauldron) {
        int meta = cauldron.hasPotion() ? cauldron.getPotionId() : 0;
        String potionIdentifier = switch (cauldron.hasPotion() ? cauldron.getType() : PotionType.NORMAL) {
            case SPLASH -> ItemNamespaceId.SPLASH_POTION;
            case LINGERING -> ItemNamespaceId.LINGERING_POTION;
            default -> ItemNamespaceId.POTION;
        };
        Item potion = Item.get(potionIdentifier, meta);

        setFillLevel(getFillLevel() - 2);
        if (isEmpty()) {
            cauldron.setType(PotionType.EMPTY);
            cauldron.setPotionId(-1);
            cauldron.clearCustomColor();
        }
        this.level.setBlock(this, this, true);

        boolean consumeBottle = player.isSurvival() || player.isAdventure();
        if (consumeBottle && item.getCount() == 1) {
            player.getInventory().setItemInHand(potion);
        } else if (item.getCount() > 1) {
            if (consumeBottle) {
                item.setCount(item.getCount() - 1);
                player.getInventory().setItemInHand(item);
            }
            if (player.getInventory().canAddItem(potion)) {
                player.getInventory().addItem(potion);
            } else {
                player.getLevel().dropItem(player.add(0, 1.3, 0), potion, player.getDirectionVector().multiply(0.4));
            }
        }
        this.level.addLevelEvent(this.add(0.5, 0.375 + getFillLevel() * 0.125, 0.5), LevelEventPacket.EVENT_CAULDRON_TAKE_POTION);
    }

    private boolean isLeatherArmor(Item item) {
        return item.hasItemTag(ItemTags.LEATHER_TIER)
                || item.getNamespaceId().equals(ItemNamespaceId.LEATHER_HORSE_ARMOR)
                || item.getNamespaceId().equals(ItemNamespaceId.WOLF_ARMOR);
    }

    private String getFilledBucketIdentifier() {
        return switch (getCauldronLiquid()) {
            case POWDER_SNOW -> ItemNamespaceId.POWDER_SNOW_BUCKET;
            case LAVA -> ItemNamespaceId.LAVA_BUCKET;
            default -> ItemNamespaceId.WATER_BUCKET;
        };
    }

    public boolean onLavaActivate(@NotNull Item item, Player player) {
        BlockEntity be = this.level.getBlockEntity(this);

        if (!(be instanceof BlockEntityCauldron cauldron)) {
            return false;
        }

        String itemIdentifier = item.getNamespaceId();
        switch (itemIdentifier) {
            case ItemNamespaceId.BUCKET:
            case ItemNamespaceId.WATER_BUCKET:
            case ItemNamespaceId.LAVA_BUCKET:
                ItemBucket bucket = (ItemBucket) item;
                if (itemIdentifier.equals(ItemNamespaceId.BUCKET)) {
                    if (!isFull() || cauldron.isCustomColor() || cauldron.hasPotion()) {
                        break;
                    }

                    PlayerBucketFillEvent ev = new PlayerBucketFillEvent(player, this, null, item, Item.get(getFilledBucketIdentifier(), null, 1, bucket.getCompoundTag()));
                    if (ev.call()) {
                        replaceBucket(bucket, player, ev.getItem());
                        this.setFillLevel(0);
                        this.level.setBlock(this, new BlockCauldron(), true);
                        cauldron.clearCustomColor();
                        this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.BUCKET_FILL_LAVA);
                    }
                } else if (itemIdentifier.equals(ItemNamespaceId.WATER_BUCKET) || itemIdentifier.equals(ItemNamespaceId.LAVA_BUCKET)) {
                    if (isFull() && !cauldron.isCustomColor() && !cauldron.hasPotion() && itemIdentifier.equals(ItemNamespaceId.LAVA_BUCKET)) {
                        break;
                    }

                    PlayerBucketEmptyEvent ev = new PlayerBucketEmptyEvent(player, this, null, item, Item.get(getFilledBucketIdentifier(), null, 1, bucket.getCompoundTag()));
                    if (ev.call()) {
                        replaceBucket(bucket, player, ev.getItem());

                        if (cauldron.hasPotion()) {
                            clearWithFizz(cauldron);
                        } else if (itemIdentifier.equals(ItemNamespaceId.LAVA_BUCKET)) {
                            this.setFillLevel(6);
                            cauldron.clearCustomColor();
                            this.level.setBlock(this, this, true);
                            this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.BUCKET_EMPTY_LAVA);
                            for (int i = 0; i < 2; i++) {
                                this.getLevel().addParticle(new LavaParticle(this.add(0.5 + Math.random() * 0.4 - 0.2, 1.0, 0.5 + Math.random() * 0.4 - 0.2)));
                            }
                        } else {
                            if (isEmpty()) {
                                BlockCauldron blockCauldron = new BlockCauldron();
                                blockCauldron.setFillLevel(6);
                                this.level.setBlock(this, blockCauldron, true, true);
                                cauldron.clearCustomColor();
                                this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.CAULDRON_FILLWATER);
                            } else {
                                clearWithFizz(cauldron);
                            }
                        }
                    }
                }
                break;
            case ItemNamespaceId.POTION:
            case ItemNamespaceId.SPLASH_POTION:
            case ItemNamespaceId.LINGERING_POTION:
                if (!isEmpty()) {
                    clearWithFizz(cauldron);
                    consumePotion(item, player);
                }
                break;
            case ItemNamespaceId.GLASS_BOTTLE:
                if (!isEmpty() && cauldron.hasPotion()) {
                    return super.onActivate(item, player);
                }
            default:
                return true;
        }

        this.level.updateComparatorOutputLevel(this);
        return true;
    }

    protected void replaceBucket(Item oldBucket, Player player, Item newBucket) {
        if (player.isSurvival() || player.isAdventure()) {
            if (oldBucket.getCount() == 1) {
                player.getInventory().setItemInHand(newBucket);
            } else {
                oldBucket.setCount(oldBucket.getCount() - 1);
                if (player.getInventory().canAddItem(newBucket)) {
                    player.getInventory().addItem(newBucket);
                } else {
                    player.getLevel().dropItem(player.add(0, 1.3, 0), newBucket, player.getDirectionVector().multiply(0.4));
                }
            }
        }
    }

    private void consumePotion(Item item, Player player) {
        if (player.isSurvival() || player.isAdventure()) {
            if (item.getCount() == 1) {
                player.getInventory().setItemInHand(Item.get(ItemNamespaceId.GLASS_BOTTLE));
            } else if (item.getCount() > 1) {
                item.setCount(item.getCount() - 1);
                player.getInventory().setItemInHand(item);

                Item bottle = Item.get(ItemNamespaceId.GLASS_BOTTLE);
                if (player.getInventory().canAddItem(bottle)) {
                    player.getInventory().addItem(bottle);
                } else {
                    player.getLevel().dropItem(player.add(0, 1.3, 0), bottle, player.getDirectionVector().multiply(0.4));
                }
            }
        }
    }

    public void clearWithFizz(BlockEntityCauldron cauldron) {
        this.setFillLevel(0);
        cauldron.setType(PotionType.EMPTY);
        cauldron.setPotionId(-1);
        cauldron.clearCustomColor();
        this.level.setBlock(this, new BlockCauldron(), true);
        this.level.addSound(this.add(0.5, 0, 0.5), Sound.RANDOM_FIZZ);
        for (int i = 0; i < 8; ++i) {
            this.getLevel().addParticle(new SmokeParticle(add(Math.random(), 1.2, Math.random())));
        }
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            Block blockAbove = this.up();

            if (canFillByDripstone(blockAbove)) {
                if (blockAbove instanceof BlockWater && blockAbove.getDamage() == 0) {
                    this.setFillLevel(3);
                    this.level.addSound(this.add(0.5, 1, 0.5), Sound.CAULDRON_FILLWATER);
                } else if (blockAbove instanceof BlockLava && blockAbove.getDamage() == 0) {
                    this.setCauldronLiquid(CauldronLiquid.LAVA);
                    this.setFillLevel(3);
                    this.level.addSound(this.add(0.5, 1, 0.5), Sound.BUCKET_EMPTY_LAVA);
                }
            }
        }

        return super.onUpdate(type);
    }

    private boolean canFillByDripstone(Block blockAbove) {
        if (this.getCauldronLiquid() != CauldronLiquid.WATER || this.isFull()
                || !(blockAbove instanceof BlockWater || blockAbove instanceof BlockLava)) {
            return false;
        }
        BlockEntityCauldron cauldron = getBlockEntity();
        return cauldron != null && !cauldron.hasPotion();
    }

    @Override
    public boolean place(@NotNull Item item, @NotNull Block block, @NotNull Block target, @NotNull BlockFace face, double fx, double fy, double fz, Player player) {
        CompoundTag nbt = new CompoundTag()
                .putShort("PotionId", -1)
                .putByte("SplashPotion", 0);

        if (item.hasCustomBlockData()) {
            Map<String, Tag> customData = item.getCustomBlockData().getTags();
            for (Map.Entry<String, Tag> tag : customData.entrySet()) {
                nbt.put(tag.getKey(), tag.getValue());
            }
        }

        return BlockEntityHolder.setBlockAndCreateEntity(this, true, true, nbt) != null;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride() {
        return getFillLevel();
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public boolean isSolid(BlockFace side) {
        return false;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public int getLightLevel() {
        return getCauldronLiquid() == CauldronLiquid.LAVA ? 15 : 0;
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    protected AxisAlignedBB recalculateCollisionBoundingBox() {
        return shrink(0.3, 0.3, 0.3);
    }

    @Override
    public void onEntityCollide(Entity entity) {
        EntityCombustByBlockEvent ev = new EntityCombustByBlockEvent(this, entity, 15);
        if (ev.call()) {
            if (getCauldronLiquid() == CauldronLiquid.LAVA && entity.isAlive() && entity.noDamageTicks == 0) {
                entity.setOnFire(ev.getDuration());
                if (!entity.hasEffect(EffectType.FIRE_RESISTANCE)) {
                    entity.attack(new EntityDamageByBlockEvent(this, entity, EntityDamageEvent.DamageCause.LAVA, 4));
                }
            } else if (entity.isAlive() && entity.isOnFire()) {
                entity.setOnFire(0);
            }
        }
    }
}
