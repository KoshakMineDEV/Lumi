package cn.nukkit.registry;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.impl.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockEntityRegistry implements IRegistry<String, Class<? extends BlockEntity>, Class<? extends BlockEntity>> {

    private static final BiMap<String, Class<? extends BlockEntity>> KNOWN_BLOCK_ENTITIES = HashBiMap.create(30);
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        register(BlockEntityID.FURNACE, BlockEntityFurnace.class);
        register(BlockEntityID.BLAST_FURNACE, BlockEntityBlastFurnace.class);
        register(BlockEntityID.SMOKER, BlockEntitySmoker.class);
        register(BlockEntityID.CHEST, BlockEntityChest.class);
        register(BlockEntityID.SIGN, BlockEntitySign.class);
        register(BlockEntityID.ENCHANT_TABLE, BlockEntityEnchantTable.class);
        register(BlockEntityID.SKULL, BlockEntitySkull.class);
        register(BlockEntityID.FLOWER_POT, BlockEntityFlowerPot.class);
        register(BlockEntityID.BREWING_STAND, BlockEntityBrewingStand.class);
        register(BlockEntityID.ITEM_FRAME, BlockEntityItemFrame.class);
        register(BlockEntityID.GLOW_ITEM_FRAME, BlockEntityItemFrameGlow.class);
        register(BlockEntityID.CAULDRON, BlockEntityCauldron.class);
        register(BlockEntityID.ENDER_CHEST, BlockEntityEnderChest.class);
        register(BlockEntityID.BEACON, BlockEntityBeacon.class);
        register(BlockEntityID.PISTON_ARM, BlockEntityPistonArm.class);
        register(BlockEntityID.COMPARATOR, BlockEntityComparator.class);
        register(BlockEntityID.HOPPER, BlockEntityHopper.class);
        register(BlockEntityID.BED, BlockEntityBed.class);
        register(BlockEntityID.JUKEBOX, BlockEntityJukebox.class);
        register(BlockEntityID.SHULKER_BOX, BlockEntityShulkerBox.class);
        register(BlockEntityID.BANNER, BlockEntityBanner.class);
        register(BlockEntityID.DROPPER, BlockEntityDropper.class);
        register(BlockEntityID.DISPENSER, BlockEntityDispenser.class);
        register(BlockEntityID.MOB_SPAWNER, BlockEntitySpawner.class);
        register(BlockEntityID.MUSIC, BlockEntityMusic.class);
        register(BlockEntityID.LECTERN, BlockEntityLectern.class);
        register(BlockEntityID.BEEHIVE, BlockEntityBeehive.class);
        register(BlockEntityID.CAMPFIRE, BlockEntityCampfire.class);
        register(BlockEntityID.BELL, BlockEntityBell.class);
        register(BlockEntityID.BARREL, BlockEntityBarrel.class);
        register(BlockEntityID.MOVING_BLOCK, BlockEntityMovingBlock.class);
        register(BlockEntityID.END_GATEWAY, BlockEntityEndGateway.class);
        register(BlockEntityID.DECORATED_POT, BlockEntityDecoratedPot.class);
        register(BlockEntityID.TARGET, BlockEntityTarget.class);
        register(BlockEntityID.BRUSHABLE_BLOCK, BlockEntityBrushableBlock.class);
        register(BlockEntityID.CONDUIT, BlockEntityConduit.class);
        register(BlockEntityID.CHISELED_BOOKSHELF, BlockEntityChiseledBookshelf.class);
        register(BlockEntityID.HANGING_SIGN, BlockEntityHangingSign.class);
        register(BlockEntityID.SCULK_SENSOR, BlockEntitySculkSensor.class);
        register(BlockEntityID.COMMAND_BLOCK, BlockEntityCommandBlock.class);

        // Persistent container, not on vanilla
        register(BlockEntityID.PERSISTENT_CONTAINER, PersistentDataContainerBlockEntity.class);
    }

    @Override
    public void register(String key, Class<? extends BlockEntity> value) {
        if (value == null) {
            throw new RegisterException("Tried to register null as BlockEntity with identifier:  " + key);
        }

        KNOWN_BLOCK_ENTITIES.put(key, value);
    }

    @Override
    public Class<? extends BlockEntity> get(String key) {
        return KNOWN_BLOCK_ENTITIES.get(key);
    }

    public String getSaveId(Class<? extends BlockEntity> blockEntity) {
        return KNOWN_BLOCK_ENTITIES.inverse().get(blockEntity);
    }

    public Map<String, Class<? extends BlockEntity>> getKnownBlockEntities() {
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
