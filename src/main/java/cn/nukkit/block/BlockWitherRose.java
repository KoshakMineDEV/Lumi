package cn.nukkit.block;

import cn.nukkit.Difficulty;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.event.entity.EntityEffectUpdateEvent;
import cn.nukkit.math.AxisAlignedBB;

public class BlockWitherRose extends BlockFlower {

    public BlockWitherRose() {
        this(0);
    }

    public BlockWitherRose(int meta) {
        super(0);
    }

    @Override
    public int getId() {
        return WITHER_ROSE;
    }

    @Override
    public boolean canBeActivated() {
        return false;
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (level.getServer().getDifficulty() != Difficulty.PEACEFUL && entity instanceof EntityLiving living) {
            if (!living.invulnerable && !living.hasEffect(EffectType.WITHER)
                    && (!(living instanceof Player) || !((Player) living).isCreative() && !((Player) living).isSpectator())) {
                Effect effect = Effect.get(EffectType.WITHER);
                effect.setDuration(50); // No damage is given if less due to how the effect is ticked
                living.addEffect(effect, EntityEffectUpdateEvent.Cause.WITHER_ROSE);
            }
        }
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    public String getName() {
        return "Wither Rose";
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        return this;
    }

    @Override
    public double getMinX() {
        return this.x + 0.2;
    }

    @Override
    public double getMinZ() {
        return this.z + 0.2;
    }

    @Override
    public double getMaxX() {
        return this.x + 0.8;
    }

    @Override
    public double getMaxY() {
        return this.y + 0.8;
    }

    @Override
    public double getMaxZ() {
        return this.z + 0.8;
    }
}