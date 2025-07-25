package cn.nukkit.block;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.entity.effect.PotionType;
import cn.nukkit.entity.item.EntityPotionSplash;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.event.block.BlockBurnEvent;
import cn.nukkit.event.block.BlockFadeEvent;
import cn.nukkit.event.block.BlockIgniteEvent;
import cn.nukkit.event.entity.EntityCombustByBlockEvent;
import cn.nukkit.event.entity.EntityDamageByBlockEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Utils;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class BlockFire extends BlockFlowable {

    public BlockFire() {
        this(0);
    }

    public BlockFire(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return FIRE;
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    public String getName() {
        return "Fire Block";
    }

    @Override
    public int getLightLevel() {
        return 15;
    }

    @Override
    public boolean isBreakable(Item item) {
        return false;
    }

    @Override
    public boolean canBeReplaced() {
        return true;
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (entity instanceof EntityPotionSplash potion) {
            if (potion.potionId == PotionType.WATER.id()) {
                BlockFadeEvent event = new BlockFadeEvent(this, Block.get(AIR));
                this.level.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    this.level.setBlock(this, event.getNewState(), true);
                }
            }
            return;
        }

        if (!entity.hasEffect(EffectType.FIRE_RESISTANCE) && this.level.getGameRules().getBoolean(GameRule.FIRE_DAMAGE)) {
            entity.attack(new EntityDamageByBlockEvent(this, entity, DamageCause.FIRE, 1));
        }

        EntityCombustByBlockEvent event = new EntityCombustByBlockEvent(this, entity, 8);
        if (entity instanceof EntityArrow) {
            event.setCancelled();
        }

        if (event.call() && entity.isAlive() && entity.noDamageTicks == 0) {
            entity.setOnFire(event.getDuration());
        }
    }

    @Override
    public Item[] getDrops(Item item) {
        return Item.EMPTY_ARRAY;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_RANDOM) {
            if (!this.isBlockTopFacingSurfaceSolid(this.down()) && !this.canNeighborBurn()) {
                this.getLevel().setBlock(this, Block.get(BlockID.AIR), true);
            } else if (this.level.gameRules.getBoolean(GameRule.DO_FIRE_TICK) && !level.isUpdateScheduled(this, this)) {
                level.scheduleUpdate(this, tickRate());
            }

            return Level.BLOCK_UPDATE_NORMAL;
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED && this.level.gameRules.getBoolean(GameRule.DO_FIRE_TICK)) {
            Block down = this.down();
            boolean forever = this.getId() == SOUL_FIRE || down.getId() == NETHERRACK || down.getId() == MAGMA || (down.getId() == BEDROCK && level.getDimension() == Level.DIMENSION_THE_END);

            boolean canBlockSeeSky = this.getLevel().canBlockSeeSky(this) ||
                    this.getLevel().canBlockSeeSky(this.east()) ||
                    this.getLevel().canBlockSeeSky(this.west()) ||
                    this.getLevel().canBlockSeeSky(this.south()) ||
                    this.getLevel().canBlockSeeSky(this.north());

            if (!forever && this.getLevel().isRaining() && canBlockSeeSky) {
                this.getLevel().setBlock(this, Block.get(BlockID.AIR), true);
            }

            if (!this.isBlockTopFacingSurfaceSolid(down) && !this.canNeighborBurn()) {
                this.getLevel().setBlock(this, Block.get(BlockID.AIR), true);
                return 0;
            }

            int meta = this.getDamage();

            if (meta < 15) {
                int newMeta = meta + Utils.random.nextInt(3);
                if (newMeta > 15) {
                    newMeta = 15;
                }
                this.setDamage(newMeta);
                this.getLevel().setBlock(this, this, true);
            }

            this.getLevel().scheduleUpdate(this, this.tickRate() + Utils.random.nextInt(10));

            if (!forever && !this.canNeighborBurn()) {
                if (!this.isBlockTopFacingSurfaceSolid(this.down()) || meta > 3) {
                    this.getLevel().setBlock(this, Block.get(BlockID.AIR), true);
                }
            } else if (!forever && !(this.down().getBurnAbility() > 0) && meta == 15 && Utils.random.nextInt(4) == 0) {
                this.getLevel().setBlock(this, Block.get(BlockID.AIR), true);
            } else {
                int o = 0;

                //TODO: decrease the o if the rainfall values are high

                this.tryToCatchBlockOnFire(this.getSideIfLoaded(BlockFace.EAST), 300 + o, meta);
                this.tryToCatchBlockOnFire(this.getSideIfLoaded(BlockFace.WEST), 300 + o, meta);
                this.tryToCatchBlockOnFire(this.getSideIfLoaded(BlockFace.DOWN), 250 + o, meta);
                this.tryToCatchBlockOnFire(this.getSideIfLoaded(BlockFace.UP), 250 + o, meta);
                this.tryToCatchBlockOnFire(this.getSideIfLoaded(BlockFace.SOUTH), 300 + o, meta);
                this.tryToCatchBlockOnFire(this.getSideIfLoaded(BlockFace.NORTH), 300 + o, meta);

                int dif = 40 + this.getLevel().getServer().getDifficulty().getId() * 7;

                for (int x = (int) (this.x - 1); x <= (int) (this.x + 1); ++x) {
                    for (int z = (int) (this.z - 1); z <= (int) (this.z + 1); ++z) {
                        for (int y = (int) (this.y - 1); y <= (int) (this.y + 4); ++y) {
                            if (x != (int) this.x || y != (int) this.y || z != (int) this.z) {
                                int k = 100;

                                if (y > this.y + 1) {
                                    k += (y - (this.y + 1)) * 100;
                                }

                                FullChunk chunk = this.getLevel().getChunkIfLoaded(x >> 4, z >> 4);
                                if (chunk == null) {
                                    continue;
                                }

                                Block block = this.getLevel().getBlock(chunk, x, y, z, 0, false);
                                int chance = getChanceOfNeighborsEncouragingFire(block);

                                if (chance > 0) {
                                    int t = (chance + dif) / (meta + 30);

                                    //TODO: decrease the t if the rainfall values are high

                                    if (t > 0 && Utils.random.nextInt(k) <= t) {
                                        int damage = meta + (Utils.random.nextInt(5) >> 2);

                                        if (damage > 15) {
                                            damage = 15;
                                        }

                                        BlockIgniteEvent e = new BlockIgniteEvent(block, this, null, BlockIgniteEvent.BlockIgniteCause.SPREAD);
                                        this.level.getServer().getPluginManager().callEvent(e);

                                        if (!e.isCancelled()) {
                                            this.getLevel().setBlock(block, Block.get(FIRE, damage), true);
                                            this.getLevel().scheduleUpdate(block, this.tickRate());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return 0;
    }

    private void tryToCatchBlockOnFire(Block block, int bound, int damage) {
        int burnAbility = block.getBurnAbility();
        if (burnAbility == 0) {
            return;
        }

        if (Utils.random.nextInt(bound) < burnAbility) {

            if (Utils.random.nextInt(damage + 10) < 5) {
                int meta = damage + (Utils.random.nextInt(5) >> 2);

                if (meta > 15) {
                    meta = 15;
                }

                BlockIgniteEvent e = new BlockIgniteEvent(block, this, null, BlockIgniteEvent.BlockIgniteCause.SPREAD);
                this.level.getServer().getPluginManager().callEvent(e);

                if (!e.isCancelled()) {
                    this.getLevel().setBlock(block, Block.get(FIRE, meta), true);
                    this.getLevel().scheduleUpdate(block, this.tickRate());
                }
            } else {
                BlockBurnEvent ev = new BlockBurnEvent(block);
                this.getLevel().getServer().getPluginManager().callEvent(ev);

                if (!ev.isCancelled()) {
                    this.getLevel().setBlock(block, Block.get(BlockID.AIR), true);
                }
            }

            if (block instanceof BlockTNT) {
                ((BlockTNT) block).prime();
            }
        }
    }

    private static int getChanceOfNeighborsEncouragingFire(Block block) {
        if (block.getId() != AIR) {
            return 0;
        } else {
            int chance = 0;
            chance = Math.max(chance, block.getSideIfLoaded(BlockFace.EAST).getBurnChance());
            chance = Math.max(chance, block.getSideIfLoaded(BlockFace.WEST).getBurnChance());
            chance = Math.max(chance, block.getSideIfLoaded(BlockFace.DOWN).getBurnChance());
            chance = Math.max(chance, block.getSideIfLoaded(BlockFace.UP).getBurnChance());
            chance = Math.max(chance, block.getSideIfLoaded(BlockFace.SOUTH).getBurnChance());
            chance = Math.max(chance, block.getSideIfLoaded(BlockFace.NORTH).getBurnChance());
            return chance;
        }
    }

    public boolean canNeighborBurn() {
        for (BlockFace face : BlockFace.values()) {
            if (this.getSide(face).getBurnChance() > 0) {
                return true;
            }
        }

        return false;
    }

    public boolean isBlockTopFacingSurfaceSolid(Block block) {
        if (block != null) {
            if (block.isSolid()) {
                return true;
            } else {
                if (block instanceof BlockStairs && (block.getDamage() & 4) == 4) {
                    return true;
                } else if (block instanceof BlockSlab && (block.getDamage() & 8) == 8) {
                    return true;
                } else {
                    return block instanceof BlockSnowLayer && (block.getDamage() & 7) == 7;
                }
            }
        }

        return false;
    }

    @Override
    public int tickRate() {
        return 30;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.AIR_BLOCK_COLOR;
    }

    @Override
    protected AxisAlignedBB recalculateCollisionBoundingBox() {
        return new SimpleAxisAlignedBB(this.x, this.y, this.z, this.x + 1.0D, this.y + 1.0D, this.z + 1.0D);
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(BlockID.AIR));
    }
}
