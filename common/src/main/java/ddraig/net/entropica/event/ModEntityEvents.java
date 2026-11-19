package ddraig.net.entropica.event;

import ddraig.net.entropica.entity.ashen_stalker.AshenStalkerEntity;
import ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.entity.mammot.FrostTuskMammotEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxAfterimageEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class ModEntityEvents {

    /**
     * Placement type allowing glacial fauna (Rime-Back Ovis, Glacial Frost-Core, Frost-Tusk Mammot)
     * to spawn naturally on Blue Ice, Packed Ice, regular Ice, Snow Blocks, stone, and grass.
     */
    public static final SpawnPlacementType GLACIAL_SURFACE_PLACEMENT = new SpawnPlacementType() {
        @Override
        public boolean isSpawnPositionOk(LevelReader level, BlockPos pos, EntityType<?> entityType) {
            if (entityType == null || !level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (!GlacialFrostCoreEntity.isGlacialSpawnSurface(level, belowPos, belowState)) {
                return false;
            }
            return isClearForGlacialSpawn(level.getBlockState(pos))
                    && isClearForGlacialSpawn(level.getBlockState(pos.above()));
        }

        @Override
        public BlockPos adjustSpawnPosition(LevelReader level, BlockPos pos) {
            BlockPos below = pos.below();
            return level.getBlockState(below).isPathfindable(PathComputationType.LAND) ? below : pos;
        }

        private boolean isClearForGlacialSpawn(BlockState state) {
            if (state.isAir() || state.is(Blocks.SNOW)) {
                return true;
            }
            return !state.blocksMotion() && state.getFluidState().isEmpty();
        }
    };

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
        EntityAttributeRegistry.register(ModEntityTypes.SPELL_EIDOLON, ddraig.net.entropica.entity.SpellEidolonEntity::createAttributes);

        // Biome 1 (2.1 Blue-Glacier Shelf) Fauna 19 & 20
        EntityAttributeRegistry.register(ModEntityTypes.GLACIAL_FROST_CORE, GlacialFrostCoreEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.FROST_TUSK_MAMMOT, FrostTuskMammotEntity::createAttributes);

        // Natural Spawn Placements for Blue-Glacier Shelf Fauna (Blue Ice / Packed Ice / Ice / Snow / Stone)
        SpawnPlacementsRegistry.register(
                ModEntityTypes.RIME_BACK_OVIS,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FrostTuskMammotEntity::checkBlueGlacierSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.GLACIAL_FROST_CORE,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GlacialFrostCoreEntity::checkBlueGlacierSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.FROST_TUSK_MAMMOT,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FrostTuskMammotEntity::checkBlueGlacierSpawnRules
        );
    }
}
