package cn.nukkit.registry;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.impl.*;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class BlockEntityRegistry implements IRegistry<String, BiFunction<FullChunk, CompoundTag, BlockEntity>, BiFunction<FullChunk, CompoundTag, BlockEntity>> {

    private static final BiMap<String, BiFunction<FullChunk, CompoundTag, BlockEntity>> KNOWN_BLOCK_ENTITIES = HashBiMap.create(30);
    private static final Map<Class<? extends BlockEntity>, String> BLOCK_ENTITY_SAVE_ID = new HashMap<>();
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        register(BlockEntityID.FURNACE, BlockEntityFurnace::new, BlockEntityFurnace.class);
        register(BlockEntityID.BLAST_FURNACE, BlockEntityBlastFurnace::new, BlockEntityBlastFurnace.class);
        register(BlockEntityID.SMOKER, BlockEntitySmoker::new, BlockEntitySmoker.class);
        register(BlockEntityID.CHEST, BlockEntityChest::new, BlockEntityChest.class);
        register(BlockEntityID.SIGN, BlockEntitySign::new, BlockEntitySign.class);
        register(BlockEntityID.ENCHANT_TABLE, BlockEntityEnchantTable::new, BlockEntityEnchantTable.class);
        register(BlockEntityID.SKULL, BlockEntitySkull::new, BlockEntitySkull.class);
        register(BlockEntityID.FLOWER_POT, BlockEntityFlowerPot::new, BlockEntityFlowerPot.class);
        register(BlockEntityID.BREWING_STAND, BlockEntityBrewingStand::new, BlockEntityBrewingStand.class);
        register(BlockEntityID.ITEM_FRAME, BlockEntityItemFrame::new, BlockEntityItemFrame.class);
        register(BlockEntityID.GLOW_ITEM_FRAME, BlockEntityItemFrameGlow::new, BlockEntityItemFrameGlow.class);
        register(BlockEntityID.CAULDRON, BlockEntityCauldron::new, BlockEntityCauldron.class);
        register(BlockEntityID.ENDER_CHEST, BlockEntityEnderChest::new, BlockEntityEnderChest.class);
        register(BlockEntityID.BEACON, BlockEntityBeacon::new, BlockEntityBeacon.class);
        register(BlockEntityID.PISTON_ARM, BlockEntityPistonArm::new, BlockEntityPistonArm.class);
        register(BlockEntityID.COMPARATOR, BlockEntityComparator::new, BlockEntityComparator.class);
        register(BlockEntityID.HOPPER, BlockEntityHopper::new, BlockEntityHopper.class);
        register(BlockEntityID.BED, BlockEntityBed::new, BlockEntityBed.class);
        register(BlockEntityID.JUKEBOX, BlockEntityJukebox::new, BlockEntityJukebox.class);
        register(BlockEntityID.SHELF, BlockEntityShelf::new, BlockEntityShelf.class);
        register(BlockEntityID.SHULKER_BOX, BlockEntityShulkerBox::new, BlockEntityShulkerBox.class);
        register(BlockEntityID.BANNER, BlockEntityBanner::new, BlockEntityBanner.class);
        register(BlockEntityID.DROPPER, BlockEntityDropper::new, BlockEntityDropper.class);
        register(BlockEntityID.DISPENSER, BlockEntityDispenser::new, BlockEntityDispenser.class);
        register(BlockEntityID.MOB_SPAWNER, BlockEntitySpawner::new, BlockEntitySpawner.class);
        register(BlockEntityID.MUSIC, BlockEntityMusic::new, BlockEntityMusic.class);
        register(BlockEntityID.LECTERN, BlockEntityLectern::new, BlockEntityLectern.class);
        register(BlockEntityID.BEEHIVE, BlockEntityBeehive::new, BlockEntityBeehive.class);
        register(BlockEntityID.CAMPFIRE, BlockEntityCampfire::new, BlockEntityCampfire.class);
        register(BlockEntityID.BELL, BlockEntityBell::new, BlockEntityBell.class);
        register(BlockEntityID.BARREL, BlockEntityBarrel::new, BlockEntityBarrel.class);
        register(BlockEntityID.MOVING_BLOCK, BlockEntityMovingBlock::new, BlockEntityMovingBlock.class);
        register(BlockEntityID.END_GATEWAY, BlockEntityEndGateway::new, BlockEntityEndGateway.class);
        register(BlockEntityID.DECORATED_POT, BlockEntityDecoratedPot::new, BlockEntityDecoratedPot.class);
        register(BlockEntityID.TARGET, BlockEntityTarget::new, BlockEntityTarget.class);
        register(BlockEntityID.BRUSHABLE_BLOCK, BlockEntityBrushableBlock::new, BlockEntityBrushableBlock.class);
        register(BlockEntityID.CONDUIT, BlockEntityConduit::new, BlockEntityConduit.class);
        register(BlockEntityID.COPPER_GOLEM_STATUE, BlockEntityCopperGolemStatue::new, BlockEntityCopperGolemStatue.class);
        register(BlockEntityID.CHISELED_BOOKSHELF, BlockEntityChiseledBookshelf::new, BlockEntityChiseledBookshelf.class);
        register(BlockEntityID.HANGING_SIGN, BlockEntityHangingSign::new, BlockEntityHangingSign.class);
        register(BlockEntityID.SCULK_SENSOR, BlockEntitySculkSensor::new, BlockEntitySculkSensor.class);
        register(BlockEntityID.COMMAND_BLOCK, BlockEntityCommandBlock::new, BlockEntityCommandBlock.class);
        register(BlockEntityID.VAULT, BlockEntityVault::new, BlockEntityVault.class);
        register(BlockEntityID.TRIAL_SPAWNER, BlockEntityTrialSpawner::new, BlockEntityTrialSpawner.class);

        // Persistent container, not on vanilla
        register(BlockEntityID.PERSISTENT_CONTAINER, PersistentDataContainerBlockEntity::new, PersistentDataContainerBlockEntity.class);
    }

    @Override
    @ApiStatus.Internal
    public void register(String key, BiFunction<FullChunk, CompoundTag, BlockEntity> value) {
        throw new RuntimeException("Cannot register BlockEntityRegistry");
    }

    public void register(String key, BiFunction<FullChunk, CompoundTag, BlockEntity> value, Class<? extends BlockEntity> type) {
        if (value == null) {
            throw new RegisterException("Tried to register null as BlockEntity with identifier:  " + key);
        }

        BLOCK_ENTITY_SAVE_ID.put(type, key);
        KNOWN_BLOCK_ENTITIES.put(key, value);
    }

    @Override
    public BiFunction<FullChunk, CompoundTag, BlockEntity> get(String key) {
        return KNOWN_BLOCK_ENTITIES.get(key);
    }

    public String getSaveId(Class<? extends BlockEntity> blockEntity) {
        return BLOCK_ENTITY_SAVE_ID.get(blockEntity);
    }

    public Map<String, BiFunction<FullChunk, CompoundTag, BlockEntity>> getKnownBlockEntities() {
        return Collections.unmodifiableMap(KNOWN_BLOCK_ENTITIES);
    }

    public boolean isRegistered(String key) {
        return KNOWN_BLOCK_ENTITIES.containsKey(key);
    }

    @Override
    public void trim() {
    }

    @Override
    public void reload() {
        isLoad.set(false);
        KNOWN_BLOCK_ENTITIES.clear();
        init();
    }
}
