package cn.nukkit.blockentity;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockEntityHolder;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.persistence.PersistentDataContainer;
import cn.nukkit.level.persistence.impl.PersistentDataContainerBlockWrapper;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.ChunkException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Constructor;

/**
 * @author MagicDroidX
 */
public abstract class BlockEntity extends Position implements BlockEntityID {

    public static long count = 1;
    public FullChunk chunk;
    public String name;
    public long id;

    public boolean movable;

    public boolean closed = false;
    public CompoundTag namedTag;
    protected Server server;

    private PersistentDataContainer persistentContainer;

    public BlockEntity(FullChunk chunk, CompoundTag nbt) {
        if (chunk == null || chunk.getProvider() == null) {
            throw new ChunkException("Invalid garbage chunk given to BlockEntity");
        }

        this.server = chunk.getProvider().getLevel().getServer();
        this.chunk = chunk;
        this.setLevel(chunk.getProvider().getLevel());
        this.namedTag = nbt;
        this.name = "";
        this.id = BlockEntity.count++;
        this.x = this.namedTag.getInt("x");
        this.y = this.namedTag.getInt("y");
        this.z = this.namedTag.getInt("z");
        this.movable = this.namedTag.getBoolean("isMovable", true);

        this.initBlockEntity();

        this.chunk.addBlockEntity(this);
        this.getLevel().addBlockEntity(this);
    }

    public static BlockEntity createBlockEntity(String type, Position pos, CompoundTag nbt, Object... args) {
        return createBlockEntity(type, pos.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4), nbt, args);
    }

    public static BlockEntity createBlockEntity(String type, FullChunk chunk, CompoundTag nbt, Object... args) {
        BlockEntity blockEntity = null;

        if (Registries.BLOCK_ENTITY.isRegistered(type)) {
            Class<? extends BlockEntity> clazz = Registries.BLOCK_ENTITY.get(type);

            if (clazz == null) {
                return null;
            }

            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (blockEntity != null) {
                    break;
                }

                if (constructor.getParameterCount() != (args == null ? 2 : args.length + 2)) {
                    continue;
                }

                try {
                    if (args == null || args.length == 0) {
                        blockEntity = (BlockEntity) constructor.newInstance(chunk, nbt);
                    } else {
                        Object[] objects = new Object[args.length + 2];

                        objects[0] = chunk;
                        objects[1] = nbt;
                        System.arraycopy(args, 0, objects, 2, args.length);
                        blockEntity = (BlockEntity) constructor.newInstance(objects);

                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            Server.getInstance().getLogger().warning("Tried to create block entity that doesn't exists: " + type);
        }

        return blockEntity;
    }

    public static CompoundTag getDefaultCompound(Vector3 pos, String id) {
        return new CompoundTag()
                .putString("id", id)
                .putInt("x", pos.getFloorX())
                .putInt("y", pos.getFloorY())
                .putInt("z", pos.getFloorZ());
    }

    protected void initBlockEntity() {
    }

    public long getId() {
        return id;
    }

    public void saveNBT() {
        this.namedTag.putString("id", Registries.BLOCK_ENTITY.getSaveId(this.getClass()));
        this.namedTag.putInt("x", (int) this.getX());
        this.namedTag.putInt("y", (int) this.getY());
        this.namedTag.putInt("z", (int) this.getZ());
        this.namedTag.putBoolean("isMovable", this.movable);
    }

    public CompoundTag getCleanedNBT() {
        this.saveNBT();
        CompoundTag tag = this.namedTag.clone();
        tag.remove("x").remove("y").remove("z").remove("id");
        if (!tag.getTags().isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public Block getBlock() {
        return this.getLevelBlock();
    }

    public boolean isBlockEntityValid() {
        if (this.getLevelBlock() instanceof BlockEntityHolder<?> holder) {
            return holder.getBlockEntityType().equals(Registries.BLOCK_ENTITY.getSaveId(this.getClass()));
        }
        return false;
    }

    public boolean onUpdate() {
        return false;
    }

    public final void scheduleUpdate() {
        if (this.level.isBeingConverted) {
            return;
        }
        this.level.scheduleBlockEntityUpdate(this);
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
            if (this.chunk != null) {
                this.chunk.removeBlockEntity(this);
            }
            if (this.level != null) {
                this.level.removeBlockEntity(this);
            }
            this.level = null;
        }
    }

    public void onBreak() {

    }

    public void setDirty() {
        chunk.setChanged();
    }

    public String getName() {
        return name;
    }

    public boolean isMovable() {
        return movable;
    }

    public PersistentDataContainer getPersistentDataContainer() {
        if (this.persistentContainer == null) {
            this.persistentContainer = new PersistentDataContainerBlockWrapper(this);
        }
        return this.persistentContainer;
    }

    public boolean hasPersistentDataContainer() {
        return !this.getPersistentDataContainer().isEmpty();
    }

    public void onReplacedWith(BlockEntity blockEntity) {
        blockEntity.getPersistentDataContainer().setStorage(this.getPersistentDataContainer().getStorage().clone());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockEntity && this.getClass().equals(obj.getClass()) && super.equals(obj);
    }

    public boolean canSaveToStorage() {
        return !this.closed;
    }

    protected int getBlockIdIfLoaded(int bx, int by, int bz) {
        if (!this.level.isYInRange(by)) {
            return 0;
        }

        int cx = bx >> 4;
        int cz = bz >> 4;
        FullChunk fullChunk = this.chunk;
        if (fullChunk == null || cx != fullChunk.getX() || cz != fullChunk.getZ()) {
            fullChunk = level.getChunkIfLoaded(cx, cz);
            if (fullChunk == null) {
                return -1;
            }
        }
        return fullChunk.getBlockId(bx & 0x0f, by, bz & 0x0f);
    }
}