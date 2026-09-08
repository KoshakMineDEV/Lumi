package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.MemoryTypes;


public class LiftController implements Controller {
    @Override
    public boolean control(EntityIntelligent entity) {
        //add lift force
        if (entity.getMemoryStorage().get(MemoryTypes.ENABLE_LIFT_FORCE))
            entity.motionY += entity.getGravity();
        return true;
    }
}
