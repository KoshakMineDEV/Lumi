package cn.nukkit.entity.mob;

import cn.nukkit.block.Block;
import cn.nukkit.entity.*;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.item.Item;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;

import java.util.Set;

public class EntityWither extends EntityCreature {

    public static final int NETWORK_ID = 52;

    public EntityWither(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 3.5f;
    }

    @Override
    public boolean isUndead() {
        return true;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(witherMaxHealth());
        super.initEntity();
        this.fireProof = true;
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.NETHER_STAR, 0, 1)};
    }

    @Override
    protected DataPacket createAddEntityPacket() {
        AddEntityPacket addEntity = new AddEntityPacket();
        addEntity.type = NETWORK_ID;
        addEntity.entityUniqueId = this.getId();
        addEntity.entityRuntimeId = this.getId();
        addEntity.yaw = (float) this.yaw;
        addEntity.headYaw = (float) this.yaw;
        addEntity.pitch = (float) this.pitch;
        addEntity.x = (float) this.x;
        addEntity.y = (float) this.y;
        addEntity.z = (float) this.z;
        addEntity.speedX = (float) this.motionX;
        addEntity.speedY = (float) this.motionY;
        addEntity.speedZ = (float) this.motionZ;
        addEntity.metadata = this.dataProperties.clone();
        addEntity.attributes = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(witherMaxHealth()).setValue(witherMaxHealth())};
        return addEntity;
    }

    private int witherMaxHealth() {
        return switch (this.getServer().getDifficulty()) {
            case NORMAL -> 450;
            case HARD -> 600;
            default -> 300;
        };
    }

    @Override
    public String getName() {
        String name = this.getNameTag();
        return !name.isEmpty() ? name : "Wither";
    }

    @Override
    public boolean canBeAffected(EffectType type) {
        return type == EffectType.INSTANT_DAMAGE || type == EffectType.INSTANT_HEALTH;
    }

    /**
     * try to spawn wither entity
     */
    public static boolean trySpawnWither(Block block) {
        Block check = block;
        BlockFace skullFace = null;
        if (block.getLevel().getGameRules().getBoolean(GameRule.DO_MOB_SPAWNING)) {
            for (BlockFace face : Set.of(BlockFace.NORTH, BlockFace.EAST)) {
                boolean[] skulls = new boolean[5];
                for (int i = -2; i <= 2; i++) {
                    skulls[i + 2] = block.getSide(face, i).getId() == Block.WITHER_SKELETON_SKULL;
                }
                int inrow = 0;
                for (int i = 0; i < skulls.length; i++) {
                    if (skulls[i]) {
                        inrow++;
                        if (inrow == 2) check = block.getSide(face, i - 2);
                    } else if (inrow < 3) {
                        inrow = 0;
                    }
                }
                if (inrow >= 3) {
                    skullFace = face;
                }
            }
            if (skullFace == null) return false;
            if (check.getId() == Block.WITHER_SKELETON_SKULL) {
                faces:
                for (BlockFace blockFace : BlockFace.values()) {
                    for (int i = 1; i <= 2; i++) {
                        if (!(check.getSide(blockFace, i).getId() == Block.SOUL_SAND)) {
                            continue faces;
                        }
                    }
                    faces1:
                    for (BlockFace face : Set.of(BlockFace.UP, BlockFace.NORTH, BlockFace.EAST)) {
                        for (int i = -1; i <= 1; i++) {
                            if (!(check.getSide(blockFace).getSide(face, i).getId() == Block.SOUL_SAND)) {
                                continue faces1;
                            }
                        }

                        for (int i = 0; i <= 2; i++) {
                            Block location = check.getSide(blockFace, i);
                            location.level.breakBlock(location);
                        }
                        for (int i = -1; i <= 1; i++) {
                            Block location = check.getSide(blockFace).getSide(face, i);
                            location.level.breakBlock(location);
                            location.level.breakBlock(location.getSide(blockFace.getOpposite()));
                        }
                        Block pos = check.getSide(blockFace, 2);
                        CompoundTag nbt = new CompoundTag()
                                .putList("Pos", new ListTag<DoubleTag>()
                                        .add(new DoubleTag(pos.x + 0.5))
                                        .add(new DoubleTag(pos.y))
                                        .add(new DoubleTag(pos.z + 0.5)))
                                .putList("Motion", new ListTag<>()
                                        .add(new DoubleTag(0))
                                        .add(new DoubleTag(0))
                                        .add(new DoubleTag(0)))
                                .putList("Rotation", new ListTag<FloatTag>()
                                        .add(new FloatTag(0f))
                                        .add(new FloatTag(0f)));

                        Entity wither = Entity.createEntity("Wither", check.level.getChunk(check.getChunkX(), check.getChunkZ()), nbt);
                        if (wither != null) {
                            wither.spawnToAll();
                            wither.getLevel().addSoundToViewers(wither, Sound.MOB_WITHER_SPAWN);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
