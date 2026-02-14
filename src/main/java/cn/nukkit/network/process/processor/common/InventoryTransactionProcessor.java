package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.block.*;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.passive.EntityVillager;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.inventory.*;
import cn.nukkit.inventory.transaction.*;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.*;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.recipe.impl.MultiRecipe;
import cn.nukkit.registry.Registries;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class InventoryTransactionProcessor extends DataPacketProcessor<InventoryTransactionPacket> {
    public static final InventoryTransactionProcessor INSTANCE = new InventoryTransactionProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull InventoryTransactionPacket packet) {
        var player = handle.player;
        var server = player.getServer();
        var level = player.getLevel();
        var inventory = player.getInventory();
        
        if (!handle.isSpawned() || !handle.isAlive()) {
            log.debug("Player {} sent inventory transaction packet while not spawned or not alive", player.getName());
            return;
        }

        if (player.isSpectator()) {
            player.setNeedSendInventory(true);
            return;
        }

        InventoryTransactionPacket transactionPacket = packet;
        // Nasty hack because the client won't change the right packet in survival when creating netherite stuff,
        // so we are emulating what Mojang should be sending
        if (player.getWindowById(Player.SMITHING_WINDOW_ID) instanceof SmithingInventory smithingInventory
                && transactionPacket.transactionType == InventoryTransactionPacket.TYPE_MISMATCH
                && !smithingInventory.getResult().isNull()) {
            InventoryTransactionPacket fixedPacket = new InventoryTransactionPacket();
            fixedPacket.isRepairItemPart = true;
            fixedPacket.actions = new NetworkInventoryAction[8];

            Item fromIngredient = smithingInventory.getIngredient().clone();
            Item toIngredient = fromIngredient.decrement(1);

            Item fromEquipment = smithingInventory.getEquipment().clone();
            Item toEquipment = fromEquipment.decrement(1);

            Item fromTemplate = smithingInventory.getTemplate().clone();
            Item toTemplate = fromTemplate.decrement(1);

            Item fromResult = Item.get(Item.AIR);
            Item toResult = smithingInventory.getResult().clone();

            NetworkInventoryAction action = new NetworkInventoryAction();
            action.windowId = ContainerIds.UI;
            action.inventorySlot = SmithingInventory.SMITHING_INGREDIENT_UI_SLOT;
            action.oldItem = fromIngredient.clone();
            action.newItem = toIngredient.clone();
            fixedPacket.actions[0] = action;

            action = new NetworkInventoryAction();
            action.windowId = ContainerIds.UI;
            action.inventorySlot = SmithingInventory.SMITHING_EQUIPMENT_UI_SLOT;
            action.oldItem = fromEquipment.clone();
            action.newItem = toEquipment.clone();
            fixedPacket.actions[1] = action;


            action = new NetworkInventoryAction();
            action.windowId = ContainerIds.UI;
            action.inventorySlot = SmithingInventory.SMITHING_TEMPLATE_UI_SLOT;
            action.oldItem = fromTemplate.clone();
            action.newItem = toTemplate.clone();
            fixedPacket.actions[2] = action;

            int emptyPlayerSlot = -1;
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (inventory.getItem(slot).isNull()) {
                    emptyPlayerSlot = slot;
                    break;
                }
            }
            if (emptyPlayerSlot == -1) {
                player.setNeedSendInventory(true);
                return;
            } else {
                action = new NetworkInventoryAction();
                action.windowId = ContainerIds.INVENTORY;
                action.inventorySlot = emptyPlayerSlot; // Cursor
                action.oldItem = Item.get(Item.AIR);
                action.newItem = toResult.clone();
                fixedPacket.actions[3] = action;

                action = new NetworkInventoryAction();
                action.sourceType = NetworkInventoryAction.SOURCE_TODO;
                action.windowId = NetworkInventoryAction.SOURCE_TYPE_ANVIL_RESULT;
                action.inventorySlot = 2; // result
                action.oldItem = toResult.clone();
                action.newItem = fromResult.clone();
                fixedPacket.actions[4] = action;

                action = new NetworkInventoryAction();
                action.sourceType = NetworkInventoryAction.SOURCE_TODO;
                action.windowId = NetworkInventoryAction.SOURCE_TYPE_ANVIL_INPUT;
                action.inventorySlot = 0; // equipment
                action.oldItem = toEquipment.clone();
                action.newItem = fromEquipment.clone();
                fixedPacket.actions[5] = action;

                action = new NetworkInventoryAction();
                action.sourceType = NetworkInventoryAction.SOURCE_TODO;
                action.windowId = NetworkInventoryAction.SOURCE_TYPE_ANVIL_MATERIAL;
                action.inventorySlot = 1; // material
                action.oldItem = toIngredient.clone();
                action.newItem = fromIngredient.clone();
                fixedPacket.actions[6] = action;

                action = new NetworkInventoryAction();
                action.sourceType = NetworkInventoryAction.SOURCE_TODO;
                action.windowId = NetworkInventoryAction.SOURCE_TYPE_ANVIL_MATERIAL;
                action.inventorySlot = 3; // template
                action.oldItem = toTemplate.clone();
                action.newItem = fromTemplate.clone();
                fixedPacket.actions[7] = action;

                transactionPacket = fixedPacket;
            }
        }

        List<InventoryAction> actions = new ArrayList<>();
        for (NetworkInventoryAction networkInventoryAction : transactionPacket.actions) {
            InventoryAction a = networkInventoryAction.createInventoryAction(player);

            if (a == null) {
                log.debug("Unmatched inventory action from " + player.getName() + ": " + networkInventoryAction);
                player.setNeedSendInventory(true);
                return;
            }

            actions.add(a);
        }
        if (actions.size() > 50) {
            player.close("", "Client sent invalid packet");
            return;
        }

        if (transactionPacket.isCraftingPart) {
            if (LoomTransaction.isIn(actions)) {
                if (handle.getLoomTransaction() == null) {
                    handle.setLoomTransaction(new LoomTransaction(player, actions));
                } else {
                    for (InventoryAction action : actions) {
                        handle.getLoomTransaction().addAction(action);
                    }
                }
                if (handle.getLoomTransaction().canExecute()) {
                    if (handle.getLoomTransaction().execute()) {
                        level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BLOCK_LOOM_USE);
                    }
                }
                handle.setLoomTransaction(null);
                return;
            }

            if (handle.getCraftingTransaction() == null) {
                handle.setCraftingTransaction(new CraftingTransaction(player, actions));
            } else {
                for (InventoryAction action : actions) {
                    handle.getCraftingTransaction().addAction(action);
                }
            }

            if (handle.getCraftingTransaction().getPrimaryOutput() != null && handle.getCraftingTransaction().canExecute()) {
                try {
                    handle.getCraftingTransaction().execute();
                } catch (Exception e) {
                    log.warn("Executing crafting transaction failed", e);
                }
                handle.setCraftingTransaction(null);
            }
            return;
        } else if (transactionPacket.isEnchantingPart) {
            if (handle.getEnchantTransaction() == null) {
                handle.setEnchantTransaction(new EnchantTransaction(player, actions));
            } else {
                for (InventoryAction action : actions) {
                    handle.getEnchantTransaction().addAction(action);
                }
            }
            if (handle.getEnchantTransaction().canExecute()) {
                handle.getEnchantTransaction().execute();
                handle.setEnchantTransaction(null);
            }
            return;
        } else if (transactionPacket.isRepairItemPart) {
            Sound sound = null;
            if (SmithingTransaction.isIn(actions)) {
                if (handle.getSmithingTransaction() == null) {
                    handle.setSmithingTransaction(new SmithingTransaction(player, actions));
                } else {
                    for (InventoryAction action : actions) {
                        handle.getSmithingTransaction().addAction(action);
                    }
                }
                if (handle.getSmithingTransaction().canExecute()) {
                    try {
                        if (handle.getSmithingTransaction().execute()) {
                            sound = Sound.SMITHING_TABLE_USE;
                        }
                    } finally {
                        handle.setSmithingTransaction(null);
                    }
                }
            } else if (GrindstoneTransaction.isIn(actions)) {
                if (handle.getGrindstoneTransaction() == null) {
                    handle.setGrindstoneTransaction(new GrindstoneTransaction(player, actions));
                } else {
                    for (InventoryAction action : actions) {
                        handle.getGrindstoneTransaction().addAction(action);
                    }
                }
                if (handle.getGrindstoneTransaction().canExecute()) {
                    if (handle.getGrindstoneTransaction().execute()) {
                        Collection<Player> players = level.getChunkPlayers(player.getChunkX(), player.getChunkZ()).values();
                        players.remove(player);
                        if (!players.isEmpty()) {
                            level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BLOCK_GRINDSTONE_USE);
                        }
                    }
                    handle.setGrindstoneTransaction(null);
                }
            } else {
                if (handle.getRepairItemTransaction() == null) {
                    handle.setRepairItemTransaction(new RepairItemTransaction(player, actions));
                } else {
                    for (InventoryAction action : actions) {
                        handle.getRepairItemTransaction().addAction(action);
                    }
                }
                if (handle.getRepairItemTransaction().canExecute()) {
                    handle.getRepairItemTransaction().execute();
                    handle.setRepairItemTransaction(null);
                }
            }

            if (sound != null) {
                Collection<Player> players = level.getChunkPlayers(player.getChunkX(), player.getChunkZ()).values();
                players.remove(player);
                if (!players.isEmpty()) {
                    level.addSound(player, sound, 1f, 1f, players);
                }
            }
            return;
        } else if (transactionPacket.isTradeItemPart) {
            if (handle.getTradingTransaction() == null) {
                handle.setTradingTransaction(new TradingTransaction(player, actions));
            } else {
                for (InventoryAction action : actions) {
                    handle.getTradingTransaction().addAction(action);
                }
            }
            if (handle.getTradingTransaction().canExecute()) {
                handle.getTradingTransaction().execute();

                for (Inventory inv : handle.getTradingTransaction().getInventories()) {
                    if (inv instanceof TradeInventory tradeInventory) {
                        EntityVillager ent = tradeInventory.getHolder();
                        ent.namedTag.putBoolean("traded", true);
                        for (Tag tag : ent.getRecipes().getAll()) {
                            CompoundTag ta = (CompoundTag) tag;
                            if (ta.getCompound("buyA").getShort("id") == tradeInventory.getItem(0).getId()) {
                                int tradeXP = ta.getInt("traderExp");
                                player.addExperience(ta.getByte("rewardExp"));
                                ent.addExperience(tradeXP);
                                level.addSound(player, Sound.RANDOM_ORB, 0, 3f, player);
                            }
                        }
                    }
                }

                handle.setTradingTransaction(null);
            }
            return;
        } else if (handle.getCraftingTransaction() != null) {
            MultiRecipe multiRecipe = Registries.RECIPE.getMultiRecipe(player, handle.getCraftingTransaction().getPrimaryOutput(), handle.getCraftingTransaction().getInputList());
            if (handle.getCraftingTransaction().checkForCraftingPart(actions) || multiRecipe != null) {
                for (InventoryAction action : actions) {
                    handle.getCraftingTransaction().addAction(action);
                }
                return;
            } else {
                log.debug("Got unexpected normal inventory action with incomplete crafting transaction from " + player.getName() + ", refusing to execute crafting");
                player.removeAllWindows(false);
                player.setNeedSendInventory(true);
                handle.setCraftingTransaction(null);
            }
        } else if (handle.getEnchantTransaction() != null) {
            if (handle.getEnchantTransaction().checkForEnchantPart(actions)) {
                for (InventoryAction action : actions) {
                    handle.getEnchantTransaction().addAction(action);
                }
                return;
            } else {
                log.debug("Got unexpected normal inventory action with incomplete enchanting transaction from " + player.getName() + ", refusing to execute enchant " + transactionPacket.toString());
                player.removeAllWindows(false);
                player.setNeedSendInventory(true);
                handle.setEnchantTransaction(null);
            }
        } else if (handle.getRepairItemTransaction() != null) {
            if (RepairItemTransaction.checkForRepairItemPart(actions)) {
                for (InventoryAction action : actions) {
                    handle.getRepairItemTransaction().addAction(action);
                }
                return;
            } else {
                log.debug("Got unexpected normal inventory action with incomplete repair item transaction from " + player.getName() + ", refusing to execute repair item " + transactionPacket.toString());
                player.removeAllWindows(false);
                player.setNeedSendInventory(true);
                handle.setRepairItemTransaction(null);
            }
        } else if (handle.getSmithingTransaction() != null) {
            if (SmithingTransaction.isIn(actions)) {
                for (InventoryAction action : actions) {
                    handle.getSmithingTransaction().addAction(action);
                }
                return;
            } else {
                log.debug("Got unexpected normal inventory action with incomplete smithing table transaction from {}, refusing to execute use the smithing table {}", player.getName(), transactionPacket.toString());
                player.removeAllWindows(false);
                player.setNeedSendInventory(true);
                handle.setSmithingTransaction(null);
            }
        } else if (handle.getGrindstoneTransaction() != null) {
            if (GrindstoneTransaction.isIn(actions)) {
                for (InventoryAction action : actions) {
                    handle.getGrindstoneTransaction().addAction(action);
                }
                return;
            } else {
                log.debug("Got unexpected normal inventory action with incomplete repair item transaction from " + player.getName() + ", refusing to execute repair item " + transactionPacket.toString());
                player.removeAllWindows(false);
                player.setNeedSendInventory(true);
                handle.setRepairItemTransaction(null);
            }
        }

        switch (transactionPacket.transactionType) {
            case InventoryTransactionPacket.TYPE_NORMAL:
                InventoryTransaction transaction = new InventoryTransaction(player, actions);

                if (!transaction.execute()) {
                    log.debug("Failed to execute inventory transaction from " + player.getName() + " with actions: " + Arrays.toString(transactionPacket.actions));
                    handle.incrementFailedTransactions();
                    if (handle.getFailedTransactions() > 15) { //撤回合成事件时，如果玩家点的太快会到12
                        player.close("", "Too many failed inventory transactions");
                    }
                }

                return;
            case InventoryTransactionPacket.TYPE_MISMATCH:
                if (transactionPacket.actions.length > 0) {
                    log.debug("Expected 0 actions for mismatch, got " + transactionPacket.actions.length + ", " + Arrays.toString(transactionPacket.actions));
                }
                player.setNeedSendInventory(true);
                return;
            case InventoryTransactionPacket.TYPE_USE_ITEM:
                UseItemData useItemData;
                BlockVector3 blockVector;

                useItemData = (UseItemData) transactionPacket.transactionData;
                blockVector = useItemData.blockPos;
                BlockFace face = useItemData.face;

                if (inventory.getHeldItemIndex() != useItemData.hotbarSlot) {
                    inventory.equipItem(useItemData.hotbarSlot);
                }

                Item item;
                switch (useItemData.actionType) {
                    case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK:
                        boolean spamming = !server.getSettings().player().doNotLimitInteractions()
                                && handle.getLastRightClickPos() != null
                                && System.currentTimeMillis() - handle.getLastRightClickTime() < 100.0
                                && blockVector.distanceSquared(handle.getLastRightClickPos()) < 0.00001;

                        handle.setLastRightClickPos(blockVector);
                        handle.setLastRightClickTime(System.currentTimeMillis());

                        // Hack: Fix client spamming right clicks
                        if (spamming && inventory.getItemInHandFast().getBlockId() == BlockID.AIR) {
                            return;
                        }

                        player.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_ACTION, false);

                        if (!(player.distance(blockVector.asVector3()) > (handle.isCreative() ? 13 : 7))) {
                            if (handle.isCreative()) {
                                if (level.useItemOn(blockVector.asVector3(), inventory.getItemInHand(), face, useItemData.clickPos.x, useItemData.clickPos.y, useItemData.clickPos.z, player) != null) {
                                    return;
                                }
                            } else if (inventory.getItemInHand().equals(useItemData.itemInHand)) {
                                Item i = inventory.getItemInHand();
                                Item oldItem = i.clone();
                                if ((i = level.useItemOn(blockVector.asVector3(), i, face, useItemData.clickPos.x, useItemData.clickPos.y, useItemData.clickPos.z, player)) != null) {
                                    if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
                                        if (oldItem.getId() == i.getId() || i.getId() == 0) {
                                            inventory.setItemInHand(i);
                                        } else {
                                            log.debug("Tried to set item " + i.getId() + " but " + player.getName() + " had item " + oldItem.getId() + " in their hand slot");
                                        }
                                        inventory.sendHeldItem(player.getViewers().values());
                                    }
                                    return;
                                }
                            } else {
                                handle.setNeedSendHeldItem(true);
                            }
                        }

                        if (blockVector.distanceSquared(player) > 10000) {
                            return;
                        }

                        Block target = level.getBlock(blockVector.asVector3());
                        Block block = target.getSide(face);

                        level.sendBlocks(new Player[]{player}, new Block[]{target, block}, UpdateBlockPacket.FLAG_NOGRAPHIC);
                        level.sendBlocks(new Player[]{player}, new Block[]{target.getLevelBlockAtLayer(1), block.getLevelBlockAtLayer(1)}, UpdateBlockPacket.FLAG_NOGRAPHIC, 1);

                        if (target instanceof BlockDoor) {
                            BlockDoor door = (BlockDoor) target;

                            Block part;

                            if ((door.getDamage() & 0x08) > 0) {
                                part = target.down();

                                if (part.getId() == target.getId()) {
                                    target = part;
                                    level.sendBlocks(new Player[]{player}, new Block[]{target}, UpdateBlockPacket.FLAG_NOGRAPHIC);
                                    level.sendBlocks(new Player[]{player}, new Block[]{target.getLevelBlockAtLayer(1)}, UpdateBlockPacket.FLAG_NOGRAPHIC, 1);
                                }
                            }
                        }
                        return;
                    case InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK:
                        if (!handle.isSpawned() || !handle.isAlive()) {
                            return;
                        }

                        handle.resetCraftingGridType();

                        Item i = inventory.getItemInHand();

                        Item oldItem = i.clone();

                        if (player.canInteract(blockVector.add(0.5, 0.5, 0.5), handle.isCreative() ? 13 : 7) && (i = level.useBreakOn(blockVector.asVector3(), face, i, player, true)) != null) {
                            if (player.isSurvival() || player.isAdventure()) {
                                player.getFoodData().exhaust(0.005f);
                                if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
                                    if (oldItem.getId() == i.getId() || i.getId() == 0) {
                                        inventory.setItemInHand(i);
                                    } else {
                                        log.debug("Tried to set item " + i.getId() + " but " + player.getName() + " had item " + oldItem.getId() + " in their hand slot");
                                    }
                                    inventory.sendHeldItem(player.getViewers().values());
                                }
                            }
                            return;
                        }

                        inventory.sendContents(player);
                        inventory.sendHeldItem(player);

                        if (blockVector.distanceSquared(player) < 10000) {
                            target = level.getBlock(blockVector.asVector3());
                            level.sendBlocks(new Player[]{player}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);

                            BlockEntity blockEntity = level.getBlockEntity(blockVector.asVector3());
                            if (blockEntity instanceof BlockEntitySpawnable) {
                                ((BlockEntitySpawnable) blockEntity).spawnTo(player);
                            }
                        }

                        return;
                    case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR:
                        Vector3 directionVector = player.getDirectionVector();

                        if (inventory.getHeldItemIndex() != useItemData.hotbarSlot) {
                            inventory.equipItem(useItemData.hotbarSlot);
                        }

                        item = inventory.getItemInHand();

                        if (item instanceof ItemCrossbow) {
                            if (!item.onClickAir(player, directionVector)) {
                                return; // Shoot
                            }
                        }

                        if (!item.equalsFast(useItemData.itemInHand)) {
                            handle.setNeedSendHeldItem(true);
                            return;
                        }

                        PlayerInteractEvent interactEvent = new PlayerInteractEvent(player, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);

                        if (!interactEvent.call()) {
                            handle.setNeedSendHeldItem(true);
                            return;
                        }

                        if (item.onClickAir(player, directionVector)) {
                            if (player.isSurvival() || player.isAdventure()) {
                                if (item.getId() == 0 || inventory.getItemInHandFast().getId() == item.getId()) {
                                    inventory.setItemInHand(item);
                                } else {
                                    log.debug("Tried to set item " + item.getId() + " but " + player.getName() + " had item " + inventory.getItemInHandFast().getId() + " in their hand slot");
                                }
                            }

                            if (!player.isUsingItem()) {
                                player.setUsingItem(item.canRelease());
                                return;
                            }

                            // Used item
                            int ticksUsed = server.getTick() - handle.getStartAction();
                            player.setUsingItem(false);
                            if (!item.onUse(player, ticksUsed)) {
                                inventory.sendContents(player);
                            }
                        }
                    case InventoryTransactionPacket.USE_ITEM_ACTION_SPEAR_STAB:
                            Item spearItem;
                            Item useItemDataItem = useItemData.itemInHand;
                            Item serverItemInHand = player.getInventory().getItemInHand();
                            // Removes Damage Tag that the client adds, but we do not store.
                            if(useItemDataItem.hasCompoundTag() && (!serverItemInHand.hasCompoundTag() || !serverItemInHand.getNamedTag().containsInt("Damage"))) {
                                if(useItemDataItem.getNamedTag().containsInt("Damage")) {
                                    useItemDataItem.getNamedTag().remove("Damage");
                                }
                            }

                            if (player.isCreative()) {
                                spearItem = serverItemInHand;
                            } else if (!player.getInventory().getItemInHand().equals(useItemDataItem)) {
                                player.getServer().getLogger().debug("Item received did not match item in hand.");
                                player.getInventory().sendHeldItem(player);
                                return;
                            } else {
                                spearItem = serverItemInHand;
                            }

                            PlayerInteractEvent event = new PlayerInteractEvent(player, spearItem.clone(), player.getDirectionVector(), face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);
                            player.getServer().getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                player.getInventory().sendSlot(useItemData.hotbarSlot, player);
                                return;
                            }

                            if (!(spearItem instanceof ItemSpear spear)) {
                                return;
                            }

                            spear.onSpearStab(player, player.getMovementSpeed());
                            return;
                    default:
                        break;
                }
                break;
            case InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY:
                UseItemOnEntityData useItemOnEntityData = (UseItemOnEntityData) transactionPacket.transactionData;

                Entity targetEntity = level.getEntity(useItemOnEntityData.entityRuntimeId);
                if (targetEntity == null) {
                    return;
                }

                if (inventory.getHeldItemIndex() != useItemOnEntityData.hotbarSlot) {
                    inventory.equipItem(useItemOnEntityData.hotbarSlot);
                }

                if (!useItemOnEntityData.itemInHand.equalsFast(inventory.getItemInHand())) {
                    inventory.sendHeldItem(player);
                }

                item = inventory.getItemInHand();

                switch (useItemOnEntityData.actionType) {
                    case InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT:
                        if (player.distanceSquared(targetEntity) > 256) { // TODO: Note entity scale
                            log.debug(player.getName() + ": target entity is too far away");
                            return;
                        }

                        handle.setBreakingBlock(null);

                        player.setUsingItem(false);

                        PlayerInteractEntityEvent playerInteractEntityEvent = new PlayerInteractEntityEvent(player, targetEntity, item, useItemOnEntityData.clickPos);
                        if (player.isSpectator()) playerInteractEntityEvent.setCancelled();

                        if (!playerInteractEntityEvent.call()) {
                            break;
                        }

                        if (targetEntity.onInteract(player, item, useItemOnEntityData.clickPos) && (player.isSurvival() || player.isAdventure())) {
                            if (item.isTool()) {
                                if (item.useOn(targetEntity) && item.getDamage() >= item.getMaxDurability()) {
                                    level.addSoundToViewers(player, Sound.RANDOM_BREAK);
                                    level.addParticle(new ItemBreakParticle(player, item));
                                    item = new ItemBlock(Block.get(BlockID.AIR));
                                }
                            } else {
                                if (item.count > 1) {
                                    item.count--;
                                } else {
                                    item = new ItemBlock(Block.get(BlockID.AIR));
                                }
                            }

                            if (item.getId() == 0 || inventory.getItemInHandFast().getId() == item.getId()) {
                                inventory.setItemInHand(item);
                            } else {
                                log.debug("Tried to set item " + item.getId() + " but " + player.getName() + " had item " + inventory.getItemInHandFast().getId() + " in their hand slot");
                            }
                        }
                        break;
                    case InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK:
                        if (targetEntity.getId() == player.getId()) {
                            player.kick(PlayerKickEvent.Reason.INVALID_PVP, "Tried to attack invalid player");
                            return;
                        }

                        if (!player.canInteractEntity(targetEntity, handle.isCreative() ? 8 : 5)) {
                            break;
                        } else if (targetEntity instanceof Player) {
                            if ((((Player) targetEntity).getGamemode() & 0x01) > 0) {
                                break;
                            } else if (!server.getSettings().world().allowPvp()) {
                                break;
                            }
                        }

                        handle.setBreakingBlock(null);

                        player.setUsingItem(false);

                        if (handle.getSleeping() != null) {
                            log.debug(player.getName() + ": USE_ITEM_ON_ENTITY_ACTION_ATTACK while sleeping");
                            return;
                        }

                        if (handle.isInventoryOpen()) {
                            log.debug(player.getName() + ": USE_ITEM_ON_ENTITY_ACTION_ATTACK while viewing inventory");
                            return;
                        }

                        Enchantment[] enchantments = item.getEnchantments();

                        float itemDamage = item.getAttackDamage(player);
                        for (Enchantment enchantment : enchantments) {
                            itemDamage += (float) enchantment.getDamageBonus(targetEntity, player);
                        }

                        Map<EntityDamageEvent.DamageModifier, Float> damage = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
                        damage.put(EntityDamageEvent.DamageModifier.BASE, itemDamage);

                        float knockBack = 0.3f;
                        Enchantment knockBackEnchantment = item.getEnchantment(Enchantment.ID_KNOCKBACK);
                        if (knockBackEnchantment != null) {
                            knockBack += knockBackEnchantment.getLevel() * 0.1f;
                        }

                        EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(player, targetEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage, knockBack, enchantments);
                        entityDamageByEntityEvent.setBreakShield(item.canBreakShield());
                        if (player.isSpectator()) entityDamageByEntityEvent.setCancelled();
                        if ((targetEntity instanceof Player) && !level.getGameRules().getBoolean(GameRule.PVP)) {
                            entityDamageByEntityEvent.setCancelled();
                        }

                        if (!item.onAttack(player, targetEntity)) {
                            inventory.sendContents(player);
                        }

                        if (!targetEntity.attack(entityDamageByEntityEvent)) {
                            if (item.isTool() && !handle.isCreative()) {
                                inventory.sendContents(player);
                            }
                            break;
                        }

                        for (Enchantment enchantment : item.getEnchantments()) {
                            enchantment.doPostAttack(player, targetEntity);
                        }

                        if (item.isTool() && !handle.isCreative()) {
                            if (item.useOn(targetEntity) && item.getDamage() >= item.getMaxDurability()) {
                                level.addSoundToViewers(player, Sound.RANDOM_BREAK);
                                level.addParticle(new ItemBreakParticle(player, item));
                                inventory.setItemInHand(Item.get(0));
                            } else {
                                if (item.getId() == 0 || inventory.getItemInHandFast().getId() == item.getId()) {
                                    inventory.setItemInHand(item);
                                } else {
                                    log.debug("Tried to set item " + item.getId() + " but " + player.getName() + " had item " + inventory.getItemInHandFast().getId() + " in their hand slot");
                                }
                            }
                        }
                        return;
                    default:
                        break;
                }

                break;
            case InventoryTransactionPacket.TYPE_RELEASE_ITEM:
                if (player.isSpectator()) {
                    player.setNeedSendInventory(true);
                    return;
                }
                ReleaseItemData releaseItemData = (ReleaseItemData) transactionPacket.transactionData;

                try {
                    if (releaseItemData.actionType == InventoryTransactionPacket.RELEASE_ITEM_ACTION_RELEASE) {
                        if (player.isUsingItem()) {
                            item = inventory.getItemInHand();
                            int ticksUsed = server.getTick() - handle.getStartAction();
                            if (!item.onRelease(player, ticksUsed)) {
                                inventory.sendContents(player);
                            }
                            player.setUsingItem(false);
                        } else {
                            inventory.sendContents(player);
                        }
                        return;
                    } else {
                        log.debug(player.getName() + ": unknown release item action type: " + releaseItemData.actionType);
                    }
                } finally {
                    player.setUsingItem(false);
                }
                break;
            default:
                inventory.sendContents(player);
                break;
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.INVENTORY_TRANSACTION_PACKET;
    }

    @Override
    public Class<InventoryTransactionPacket> getPacketClass() {
        return InventoryTransactionPacket.class;
    }
}