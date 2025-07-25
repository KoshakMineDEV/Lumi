package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockMobSpawner;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawner;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityZombie;
import cn.nukkit.entity.passive.EntityChicken;
import cn.nukkit.entity.passive.EntityCow;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class ItemSpawnEgg extends Item {

    public ItemSpawnEgg() {
        this(0, 1);
    }

    public ItemSpawnEgg(Integer meta) {
        this(meta, 1);
    }

    public ItemSpawnEgg(Integer meta, int count) {
        super(SPAWN_EGG, meta, count, "Spawn Entity Egg");
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        if (player.isAdventure()) {
            return false;
        }

        if (target instanceof BlockMobSpawner) {
            BlockEntity blockEntity = level.getBlockEntity(target);
            if (blockEntity instanceof BlockEntitySpawner) {
                if (((BlockEntitySpawner) blockEntity).getSpawnEntityType() != this.getDamage()) {
                    ((BlockEntitySpawner) blockEntity).setSpawnEntityType(this.getDamage());

                    if (!player.isCreative()) {
                        player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                    }
                }
            } else {
                if (blockEntity != null) {
                    blockEntity.close();
                }

                CompoundTag nbt = new CompoundTag()
                        .putString("id", BlockEntity.MOB_SPAWNER)
                        .putInt("EntityId", this.getDamage())
                        .putInt("x", (int) target.x)
                        .putInt("y", (int) target.y)
                        .putInt("z", (int) target.z);
                BlockEntity.createBlockEntity(BlockEntity.MOB_SPAWNER, level.getChunk(target.getChunkX(), target.getChunkZ()), nbt);

                if (!player.isCreative()) {
                    player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                }
            }

            return true;
        }

        FullChunk chunk = level.getChunk((int) block.getX() >> 4, (int) block.getZ() >> 4);

        if (chunk == null) {
            return false;
        }

        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", block.getX() + 0.5))
                        .add(new DoubleTag("", target.getBoundingBox() == null ? block.getY() : target.getBoundingBox().getMaxY() + 0.0001f))
                        .add(new DoubleTag("", block.getZ() + 0.5)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", ThreadLocalRandom.current().nextFloat() * 360))
                        .add(new FloatTag("", 0)));

        if (this.hasCustomName()) {
            nbt.putString("CustomName", this.getCustomName());
        }

        CreatureSpawnEvent ev = new CreatureSpawnEvent(this.meta, block, nbt, CreatureSpawnEvent.SpawnReason.SPAWN_EGG, player);
        level.getServer().getPluginManager().callEvent(ev);

        if (ev.isCancelled()) {
            return false;
        }

        Entity entity = Entity.createEntity(this.meta, chunk, nbt);

        if (entity != null) {
            if (!player.isCreative()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }

            entity.spawnToAll();

            if (Utils.rand(1, 20) == 1 &&
                    (entity instanceof EntityCow ||
                            entity instanceof EntityChicken ||
                            entity instanceof EntityPig ||
                            entity instanceof EntitySheep ||
                            entity instanceof EntityZombie)) {

                ((BaseEntity) entity).setBaby(true);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        int meta = this.getDamage();
        if (meta < 138) {
            return true;
        }
        return switch (meta) {
            case 138, 139 -> protocolId >= ProtocolInfo.v1_20_0_23;
            case 142 -> protocolId >= ProtocolInfo.v1_20_80;
            case 140, 144 -> protocolId >= ProtocolInfo.v1_21_0;
            case 141, 143, 145, 146 -> protocolId >= ProtocolInfo.v1_21_50;
            default -> true;
        };
    }
}