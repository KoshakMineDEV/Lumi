package cn.nukkit.entity.custom;

import cn.nukkit.registry.Registries;

@Deprecated
public class EntityManager {
    private static final EntityManager ENTITY_MANAGER = new EntityManager();

    public static EntityManager get() {
        return ENTITY_MANAGER;
    }

    /**
     * @deprecated use Registries.ENTITY.registerCustomEntityDefinition() instead
     */
    @Deprecated
    public void registerDefinition(EntityDefinition entityDefinition) {
        Registries.ENTITY.registerCustomEntityDefinition(entityDefinition);
    }
}

