package ddraig.net.entropica.event;

import ddraig.net.entropica.entity.ashen_stalker.AshenStalkerEntity;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxAfterimageEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;

public class ModEntityEvents {
    public static void register() {
        EntityAttributeRegistry.register(ModEntityTypes.GROT, GrotEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.VEIL_FOX, VeilFoxEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.VEIL_FOX_AFTERIMAGE, VeilFoxAfterimageEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.ASHEN_STALKER, AshenStalkerEntity::createAttributes);
    }
}