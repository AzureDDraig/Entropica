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
        EntityAttributeRegistry.register(ModEntityTypes.SPORE_DRIFTER, ddraig.net.entropica.entity.SporeDrifterEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.RIME_BACK_OVIS, ddraig.net.entropica.entity.ovis.RimeBackOvisEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.OVERGROWTH_OVIS, ddraig.net.entropica.entity.ovis.OvergrowthOvisEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.RIME_SHEPHERD, ddraig.net.entropica.entity.RimeShepherdEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.BLOOM_CRAWLER, ddraig.net.entropica.entity.BloomCrawlerEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.AURORAFOWL, ddraig.net.entropica.entity.aurorafowl.AurorafowlEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.GEYSER_WIGGLE_WORM, ddraig.net.entropica.entity.geyser_wiggle_worm.GeyserWiggleWormEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.CRYO_STALKER, ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.PATINA_OVIS, ddraig.net.entropica.entity.ovis.PatinaGalvanicOvisEntity::createAttributes);

        // Caprine Mage Fix
        EntityAttributeRegistry.register(ModEntityTypes.GEMINI_GOAT_MAGE, ddraig.net.entropica.entity.gemini_goat_mage.GeminiGoatMageEntity::createAttributes);

        // Fauna Quintet
        EntityAttributeRegistry.register(ModEntityTypes.VOID_SEA_SERPENT, ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.GLACIAL_HYDRA, ddraig.net.entropica.entity.glacial_hydra.GlacialHydraEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.LUMINOTH, ddraig.net.entropica.entity.luminoth.LuminothEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.AMBER_WEEPING_STAG, ddraig.net.entropica.entity.amber_weeping_stag.AmberWeepingStagEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.STORM_KITE, ddraig.net.entropica.entity.storm_kite.StormKiteEntity::createAttributes);
    }
}