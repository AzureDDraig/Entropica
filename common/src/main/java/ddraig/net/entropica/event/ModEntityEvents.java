package ddraig.net.entropica.event;

import ddraig.net.entropica.block.biome2.AuroralLanternBlock;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.ashen_stalker.AshenStalkerEntity;
import ddraig.net.entropica.entity.aurora_jelly.AuroraJellyEntity;
import ddraig.net.entropica.entity.barrow_crypt_glow_owl.BarrowCryptGlowOwlEntity;
import ddraig.net.entropica.entity.barrow_hound.BarrowHoundEntity;
import ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity;
import ddraig.net.entropica.entity.cryo_static_hedgehog.CryoStaticHedgehogEntity;
import ddraig.net.entropica.entity.crystalline_qilin.CrystallineQilinEntity;
import ddraig.net.entropica.entity.dust_core_golem.DustCoreGolemEntity;
import ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity;
import ddraig.net.entropica.entity.geyser_wiggle_worm.GeyserWiggleWormEntity;
import ddraig.net.entropica.entity.glacial_hydra.GlacialHydraEntity;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.entity.mammot.FrostTuskMammotEntity;
import ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataSpriteEntity;
import ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderEntity;
import ddraig.net.entropica.entity.mycelial_spore_weaver.MycelialSporeWeaverEntity;
import ddraig.net.entropica.entity.peat_mud_kelpie.PeatMudKelpieEntity;
import ddraig.net.entropica.entity.sandstone_golem.SandstoneGolemEntity;
import ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelEntity;
import ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity;
import ddraig.net.entropica.entity.silt_sphinx.SiltSphinxEntity;
import ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusEntity;
import ddraig.net.entropica.entity.storm_kite.StormKiteEntity;
import ddraig.net.entropica.entity.vapor_bellows_crab.VaporBellowsCrabEntity;
import ddraig.net.entropica.entity.zephyr_simurgh.ZephyrSimurghEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxAfterimageEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxEntity;
import ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity;
import ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentEntity;
import ddraig.net.entropica.item.biome2.CamouflageCloakItem;
import ddraig.net.entropica.item.biome3.CryoInsulatedArmorItem;
import ddraig.net.entropica.item.biome4.AegisShieldingCharmItem;
import ddraig.net.entropica.item.biome5.SilentStridersItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModParticles;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.ExplosionEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathComputationType;

import java.util.ArrayList;
import java.util.List;

public class ModEntityEvents {

    /**
     * Returns true if the block beneath a cold-coast or Auroral Skerries creature is a valid
     * natural surface (Blue Ice, Packed Ice, Ice, Glimmer-Ice, Gravel, Coarse Gravel, Snow, Stone, Grass).
     */
    public static boolean isColdOrSkerriesSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.GRAVEL)
                || belowState.is(Blocks.SAND)
                || belowState.is(Blocks.SANDSTONE)
                || belowState.is(Blocks.RED_SAND)
                || belowState.is(Blocks.RED_SANDSTONE)
                || belowState.is(Blocks.STONE)
                || belowState.is(Blocks.ANDESITE)
                || belowState.is(Blocks.GRANITE)
                || belowState.is(Blocks.DEEPSLATE)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.POLISHED_BASALT)
                || belowState.is(Blocks.MAGMA_BLOCK)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.SNOW)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(Blocks.DIRT)
                || belowState.is(Blocks.COARSE_DIRT)
                || belowState.is(Blocks.MYCELIUM)
                || belowState.is(Blocks.MOSS_BLOCK)
                || belowState.is(Blocks.MUD)
                || belowState.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || belowState.is(Blocks.CLAY)
                || belowState.is(Blocks.OBSIDIAN)
                || belowState.is(Blocks.CRYING_OBSIDIAN)
                || belowState.is(BlockTags.TERRACOTTA)
                || "amber_resin_block".equals(path)
                || "fulgurite_ice".equals(path)
                || "zephyr_ice".equals(path)
                || "frozen_spore_moss".equals(path)
                || "spore_glazed_ice".equals(path)
                || "glass_sand".equals(path)
                || "glass_sandstone".equals(path)
                || "mirage_ice".equals(path)
                || "fulgurite_block".equals(path)
                || "steaming_ice".equals(path)
                || "steaming_spring_stone".equals(path)
                || "steam_vent".equals(path)
                || "coarse_gravel".equals(path)
                || "necrotic_soil".equals(path)
                || "barrow_stone_bricks".equals(path)
                || "cracked_barrow_bricks".equals(path)
                || "mossy_barrow_bricks".equals(path)
                || "barrow_moss".equals(path)
                || "void_sand".equals(path)
                || "void_sand_turf".equals(path)
                || "void_stone".equals(path)
                || "obsidian_bricks".equals(path)
                || "void_crystal_block".equals(path)
                || "radiant_snow_block".equals(path)
                || "radiant_glaze_ice".equals(path)
                || "vitae_spring_stone".equals(path)
                || "qilin_light_refractor".equals(path)
                || "grey_sand".equals(path)
                || "dust_crusted_ice".equals(path)
                || "sanguine_ice".equals(path)
                || "red_clay_sand".equals(path)
                || "starlight_ice".equals(path)
                || "astral_impactite".equals(path)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || (ModBlocks.COARSE_GRAVEL.isPresent() && belowState.is(ModBlocks.COARSE_GRAVEL.get()))
                || (ModBlocks.GLIMMER_ICE.isPresent() && belowState.is(ModBlocks.GLIMMER_ICE.get()))
                || (ModBlocks.AURORAL_FEATHER_MOSS.isPresent() && belowState.is(ModBlocks.AURORAL_FEATHER_MOSS.get()))
                || (ModBlocks.AEGIS_QUARTZ_BLOCK.isPresent() && belowState.is(ModBlocks.AEGIS_QUARTZ_BLOCK.get()))
                || (ModBlocks.BASALT_COLUMN.isPresent() && belowState.is(ModBlocks.BASALT_COLUMN.get()))
                || (ModBlocks.BLUE_FROZEN_MOSS.isPresent() && belowState.is(ModBlocks.BLUE_FROZEN_MOSS.get()))
                || (ModBlocks.BLUE_FROZEN_MOSS_CARPET.isPresent() && belowState.is(ModBlocks.BLUE_FROZEN_MOSS_CARPET.get()))
                || (ModBlocks.SHADOWED_ECLIPSE_MOSS.isPresent() && belowState.is(ModBlocks.SHADOWED_ECLIPSE_MOSS.get()))
                || GlacialFrostCoreEntity.isGlacialSpawnSurface(level, belowPos, belowState);
    }

    private static boolean isThinColdMossOrSnowLayer(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return state.is(Blocks.SNOW)
                || "cryo_static_shrub".equals(path)
                || "vapor_weaver_lichen".equals(path)
                || "mist_veil_fern".equals(path)
                || "grave_moss".equals(path)
                || "crypt_grave_root".equals(path)
                || "rime_cap_mushroom".equals(path)
                || "barrow_moss_carpet".equals(path)
                || "null_fringe_tussock".equals(path)
                || "golden_lichen".equals(path)
                || "zephyr_glass_grass".equals(path)
                || "frozen_spore_moss_carpet".equals(path)
                || "rime_star_tussock".equals(path)
                || (ModBlocks.AURORAL_FEATHER_MOSS.isPresent() && state.is(ModBlocks.AURORAL_FEATHER_MOSS.get()))
                || (ModBlocks.BLUE_FROZEN_MOSS_CARPET.isPresent() && state.is(ModBlocks.BLUE_FROZEN_MOSS_CARPET.get()))
                || (ModBlocks.SHADOWED_ECLIPSE_MOSS.isPresent() && state.is(ModBlocks.SHADOWED_ECLIPSE_MOSS.get()));
    }

    /**
     * Expanded spawn rule check for Blue-Glacier Shelf and Auroral Skerries surface fauna
     * (including Frost-Tusk Mammot spawning on gravel, coarse_gravel, and glimmer_ice).
     */
    public static boolean checkBlueGlacierSpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinColdMossOrSnowLayer(below)
                && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        return isColdOrSkerriesSpawnSurface(level, belowPos, below);
    }

    /**
     * Placement type allowing glacial & skerries fauna (Rime-Back Ovis, Glacial Frost-Core, Frost-Tusk Mammot)
     * to spawn naturally on Blue Ice, Packed Ice, regular Ice, Glimmer-Ice, Gravel, Coarse Gravel, Snow Blocks, stone, and grass.
     */
    public static final SpawnPlacementType GLACIAL_SURFACE_PLACEMENT = new SpawnPlacementType() {
        @Override
        public boolean isSpawnPositionOk(LevelReader level, BlockPos pos, EntityType<?> entityType) {
            if (entityType == null || !level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (isThinColdMossOrSnowLayer(belowState)
                    && level.getBlockState(belowPos.below()).blocksMotion()) {
                belowPos = belowPos.below();
                belowState = level.getBlockState(belowPos);
            }
            if (!isColdOrSkerriesSpawnSurface(level, belowPos, belowState)) {
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
            if (state.isAir()
                    || isThinColdMossOrSnowLayer(state)
                    || (ModBlocks.MIST_VEIL_FERN.isPresent() && state.is(ModBlocks.MIST_VEIL_FERN.get()))) {
                return true;
            }
            return !state.blocksMotion() && state.getFluidState().isEmpty();
        }
    };

    public static final SpawnPlacementType BARROW_OWL_SURFACE_PLACEMENT = new SpawnPlacementType() {
        @Override
        public boolean isSpawnPositionOk(LevelReader level, BlockPos pos, EntityType<?> entityType) {
            if (GLACIAL_SURFACE_PLACEMENT.isSpawnPositionOk(level, pos, entityType)) {
                return true;
            }
            if (entityType == null || !level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }
            BlockState belowState = level.getBlockState(pos.below());
            String belowPath = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
            if (belowState.is(BlockTags.LOGS)
                    || belowState.is(BlockTags.STONE_BRICKS)
                    || belowState.is(BlockTags.LEAVES)
                    || "glazed_amber_leaves".equals(belowPath)) {
                return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
            }
            return false;
        }

        @Override
        public BlockPos adjustSpawnPosition(LevelReader level, BlockPos pos) {
            return GLACIAL_SURFACE_PLACEMENT.adjustSpawnPosition(level, pos);
        }
    };

    public static boolean checkLuminothSpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinColdMossOrSnowLayer(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        String path = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
        return isColdOrSkerriesSpawnSurface(level, belowPos, below)
                || below.is(BlockTags.LEAVES)
                || below.is(BlockTags.LOGS)
                || "glazed_amber_leaves".equals(path);
    }

    private static boolean isSuppressibleMonsterSpawn(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL
                || reason == EntitySpawnReason.CHUNK_GENERATION
                || reason == EntitySpawnReason.REINFORCEMENT
                || reason == EntitySpawnReason.PATROL;
    }

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

        // Biome 2 (2.2 Auroral Skerries) Fauna 4
        EntityAttributeRegistry.register(ModEntityTypes.AURORA_JELLY, AuroraJellyEntity::createAttributes);

        // Biome 6 (2.6 Steaming Skerries) Fauna 22
        EntityAttributeRegistry.register(ModEntityTypes.VAPOR_BELLOWS_CRAB, VaporBellowsCrabEntity::createAttributes);

        // Biome 7 (2.7 Static Ice-Plains) Fauna 21
        EntityAttributeRegistry.register(ModEntityTypes.CRYO_STATIC_HEDGEHOG, CryoStaticHedgehogEntity::createAttributes);

        // Biome 8 (2.8 Barrow-Mound Tundra) Fauna 25 & Fauna 9
        EntityAttributeRegistry.register(ModEntityTypes.BARROW_HOUND, BarrowHoundEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.BARROW_CRYPT_GLOW_OWL, BarrowCryptGlowOwlEntity::createAttributes);

        // Biome 9 (2.9 Null-Fringe Bay) Fauna 42
        EntityAttributeRegistry.register(ModEntityTypes.VOID_RIFT_SHADE, VoidRiftShadeEntity::createAttributes);

        // Biome 10 (2.10 Radiant Glaze-Plains) Crystalline Qilin (Glow Guardian)
        EntityAttributeRegistry.register(ModEntityTypes.CRYSTALLINE_QILIN, CrystallineQilinEntity::createAttributes);

        // Biome 12 (2.12 Spore-Glazed Pack) Fauna 23 — Mycelial Spore-Weaver
        EntityAttributeRegistry.register(ModEntityTypes.MYCELIAL_SPORE_WEAVER, MycelialSporeWeaverEntity::createAttributes);

        // Biome 13 (2.13 Mirage Glacier) Fauna 13 — Silica Glass-Scarab
        EntityAttributeRegistry.register(ModEntityTypes.SILICA_GLASS_SCARAB, SilicaGlassScarabEntity::createAttributes);

        // Biome 14 (2.14 Oasis Floes) Fauna 33 — Mud-Lurker Salamander
        EntityAttributeRegistry.register(ModEntityTypes.MUD_LURKER_SALAMANDER, MudLurkerSalamanderEntity::createAttributes);

        // Biome 15 (2.15 Dust-Crusted Bergs) Fauna 14 — Dust-Core Golem
        EntityAttributeRegistry.register(ModEntityTypes.DUST_CORE_GOLEM, DustCoreGolemEntity::createAttributes);

        // Biome 16 (2.16 Crimson Tide-Skerries) — Sanguine Sea-Angel
        EntityAttributeRegistry.register(ModEntityTypes.SANGUINE_SEA_ANGEL, SanguineSeaAngelEntity::createAttributes);

        // Biome 17 (2.17 Astral Skerries) — Flying Starlight Nautilus
        EntityAttributeRegistry.register(ModEntityTypes.STARLIGHT_NAUTILUS, StarlightNautilusEntity::createAttributes);

        // Fauna Bestiary Pass: Attributes
        EntityAttributeRegistry.register(ModEntityTypes.SANDSTONE_GOLEM, SandstoneGolemEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.PEAT_MUD_KELPIE, PeatMudKelpieEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.SILT_SPHINX, SiltSphinxEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.ZEPHYR_SIMURGH, ZephyrSimurghEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.MIRAGE_FATA_SPRITE, MirageFataSpriteEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.MIRAGE_FATA_AFTERIMAGE, ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataAfterimageEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.MAGMA_CHERUFE, ddraig.net.entropica.entity.magma_cherufe.MagmaCherufeEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.SOULFIRE_PHENEX, ddraig.net.entropica.entity.soulfire_phenex.SoulfirePhenexEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.VOID_ARACHNE, ddraig.net.entropica.entity.void_arachne.VoidArachneEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.ASH_BARROW_GNOME, ddraig.net.entropica.entity.ash_barrow_gnome.AshBarrowGnomeEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.ORTHRUS_MAGMA_BEAST, ddraig.net.entropica.entity.orthrus_magma_beast.OrthrusMagmaBeastEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.ASH_CINDER_WISP, ddraig.net.entropica.entity.ash_cinder_wisp.AshCinderWispEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.SOUL_SAND_HYDRA, ddraig.net.entropica.entity.soul_sand_hydra.SoulSandHydraEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.VOID_SHADOW_GARGOYLE, ddraig.net.entropica.entity.void_shadow_gargoyle.VoidShadowGargoyleEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.BARROW_WIGHT_GOLEM, ddraig.net.entropica.entity.barrow_wight_golem.BarrowWightGolemEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.OBSIDIAN_SCORPIOID, ddraig.net.entropica.entity.obsidian_scorpioid.ObsidianScorpioidEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.BRIMSTONE_WYVERN, ddraig.net.entropica.entity.brimstone_wyvern.BrimstoneWyvernEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.OBSIDIAN_GOLEM, ddraig.net.entropica.entity.obsidian_golem.ObsidianGolemEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.SOULFIRE_DJINN, ddraig.net.entropica.entity.soulfire_djinn.SoulfireDjinnEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.BRIMSTONE_CHIMERA, ddraig.net.entropica.entity.brimstone_chimera.BrimstoneChimeraEntity::createAttributes);
        EntityAttributeRegistry.register(ModEntityTypes.OBSIDIAN_WENDIGO, ddraig.net.entropica.entity.obsidian_wendigo.ObsidianWendigoEntity::createAttributes);

        // Natural Spawn Placements for Blue-Glacier Shelf, Auroral Skerries, Steaming Skerries, Static Ice-Plains & Barrow-Mound Tundra Fauna
        SpawnPlacementsRegistry.register(
                ModEntityTypes.RIME_BACK_OVIS,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntityEvents::checkBlueGlacierSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.PATINA_OVIS,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntityEvents::checkBlueGlacierSpawnRules
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
                ModEntityEvents::checkBlueGlacierSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.AURORA_JELLY,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AuroraJellyEntity::checkAuroraJellySpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.CRYO_STALKER,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CryoStalkerEntity::checkCryoStalkerSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.VAPOR_BELLOWS_CRAB,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VaporBellowsCrabEntity::checkVaporBellowsCrabSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.GEYSER_WIGGLE_WORM,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GeyserWiggleWormEntity::checkGeyserWiggleWormSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.CRYO_STATIC_HEDGEHOG,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CryoStaticHedgehogEntity::checkCryoStaticHedgehogSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.RIME_SHEPHERD,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntityEvents::checkBlueGlacierSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.BARROW_HOUND,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BarrowHoundEntity::checkBarrowHoundSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.BARROW_CRYPT_GLOW_OWL,
                BARROW_OWL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING,
                BarrowCryptGlowOwlEntity::checkBarrowOwlSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.VOID_RIFT_SHADE,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidRiftShadeEntity::checkVoidRiftShadeSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.VOID_SEA_SERPENT,
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidSeaSerpentEntity::checkVoidSeaSerpentSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.CRYSTALLINE_QILIN,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CrystallineQilinEntity::checkCrystallineQilinSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.AMBER_WEEPING_STAG,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.amber_weeping_stag.AmberWeepingStagEntity::checkAmberWeepingStagSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.AURORAFOWL,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntityEvents::checkBlueGlacierSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.LUMINOTH,
                BARROW_OWL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING,
                ModEntityEvents::checkLuminothSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SANDSTONE_GOLEM,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SandstoneGolemEntity::checkSandstoneGolemSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.PEAT_MUD_KELPIE,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PeatMudKelpieEntity::checkPeatMudKelpieSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SILT_SPHINX,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SiltSphinxEntity::checkSiltSphinxSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.ZEPHYR_SIMURGH,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ZephyrSimurghEntity::checkZephyrSimurghSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.STORM_KITE,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                StormKiteEntity::checkStormKiteSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.GLACIAL_HYDRA,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GlacialHydraEntity::checkGlacialHydraSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.MIRAGE_FATA_SPRITE,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MirageFataSpriteEntity::checkMirageFataSpriteSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.MAGMA_CHERUFE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.magma_cherufe.MagmaCherufeEntity::checkMagmaCherufeSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SOULFIRE_PHENEX,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.soulfire_phenex.SoulfirePhenexEntity::checkSoulfirePhenexSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.VOID_ARACHNE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.void_arachne.VoidArachneEntity::checkVoidArachneSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.ASH_BARROW_GNOME,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.ash_barrow_gnome.AshBarrowGnomeEntity::checkAshBarrowGnomeSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.ORTHRUS_MAGMA_BEAST,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.orthrus_magma_beast.OrthrusMagmaBeastEntity::checkOrthrusMagmaBeastSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.ASH_CINDER_WISP,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.ash_cinder_wisp.AshCinderWispEntity::checkAshCinderWispSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SOUL_SAND_HYDRA,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.soul_sand_hydra.SoulSandHydraEntity::checkSoulSandHydraSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.VOID_SHADOW_GARGOYLE,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.void_shadow_gargoyle.VoidShadowGargoyleEntity::checkVoidShadowGargoyleSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.BARROW_WIGHT_GOLEM,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.barrow_wight_golem.BarrowWightGolemEntity::checkBarrowWightGolemSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.OBSIDIAN_SCORPIOID,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.obsidian_scorpioid.ObsidianScorpioidEntity::checkObsidianScorpioidSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.BRIMSTONE_WYVERN,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.brimstone_wyvern.BrimstoneWyvernEntity::checkBrimstoneWyvernSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.OBSIDIAN_GOLEM,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.obsidian_golem.ObsidianGolemEntity::checkObsidianGolemSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SOULFIRE_DJINN,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.soulfire_djinn.SoulfireDjinnEntity::checkSoulfireDjinnSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.BRIMSTONE_CHIMERA,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.brimstone_chimera.BrimstoneChimeraEntity::checkBrimstoneChimeraSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.OBSIDIAN_WENDIGO,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.obsidian_wendigo.ObsidianWendigoEntity::checkObsidianWendigoSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.MYCELIAL_SPORE_WEAVER,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MycelialSporeWeaverEntity::checkMycelialSporeWeaverSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.OVERGROWTH_OVIS,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntityEvents::checkBlueGlacierSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SPORE_DRIFTER,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ddraig.net.entropica.entity.SporeDrifterEntity::checkSporeDrifterSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SILICA_GLASS_SCARAB,
                GLACIAL_SURFACE_PLACEMENT,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SilicaGlassScarabEntity::checkSilicaGlassScarabSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.MUD_LURKER_SALAMANDER,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MudLurkerSalamanderEntity::checkMudLurkerSalamanderSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.DUST_CORE_GOLEM,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                DustCoreGolemEntity::checkDustCoreGolemSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.SANGUINE_SEA_ANGEL,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SanguineSeaAngelEntity::checkSanguineSeaAngelSpawnRules
        );
        SpawnPlacementsRegistry.register(
                ModEntityTypes.STARLIGHT_NAUTILUS,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                StarlightNautilusEntity::checkStarlightNautilusSpawnRules
        );

        // Auroral Lantern 16-block Hostile Mob Spawn Suppression Ward
        EntityEvent.LIVING_CHECK_SPAWN.register((entity, world, x, y, z, spawnReason, spawner) -> {
            if (entity instanceof Monster
                    && !(entity instanceof SpellEidolonEntity)
                    && isSuppressibleMonsterSpawn(spawnReason)) {
                ServerLevel serverLevel = null;
                if (world instanceof ServerLevel sl) {
                    serverLevel = sl;
                } else if (world instanceof ServerLevelAccessor sla) {
                    serverLevel = sla.getLevel();
                }
                if (serverLevel != null) {
                    BlockPos spawnPos = BlockPos.containing(x, y, z);
                    if (AuroralLanternBlock.isWithinWardRadius(serverLevel, spawnPos, 16.0D)) {
                        return EventResult.interruptFalse();
                    }
                }
            }
            return EventResult.pass();
        });

        EntityEvent.ADD.register((entity, level) -> {
            if (!level.isClientSide()
                    && level instanceof ServerLevel serverLevel
                    && entity instanceof Monster monster
                    && !(entity instanceof SpellEidolonEntity)
                    && monster.tickCount == 0
                    && !monster.isPersistenceRequired()
                    && AuroralLanternBlock.isWithinWardRadius(serverLevel, monster.blockPosition(), 16.0D)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // Camouflage Cloak, Cryo-Insulated Armor & Stalking Cryo-Stalker Visibility / Aggro Range Enforcement
        TickEvent.PLAYER_POST.register(player -> {
            if (player == null || player.level().isClientSide() || !(player.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if ((serverLevel.getGameTime() + player.getId()) % 10L != 0L) {
                return;
            }
            boolean hasCloak = CamouflageCloakItem.hasCamouflageCloak(player);
            boolean hasFullCryo = CryoInsulatedArmorItem.hasFullCryoInsulation(player);
            boolean hasSilentStriders = SilentStridersItem.hasSilentStriders(player);
            boolean hasEclipseShroud = player.hasEffect(MobEffects.SPEED) && player.hasEffect(MobEffects.NIGHT_VISION);
            if (!hasCloak && !hasFullCryo && !hasSilentStriders && !hasEclipseShroud) {
                return;
            }
            for (Mob mob : serverLevel.getEntitiesOfClass(
                    Mob.class,
                    player.getBoundingBox().inflate(32.0D),
                    m -> m.isAlive() && m.getTarget() == player && m.getLastHurtByMob() != player
            )) {
                double visibilityMult = 1.0D;
                if (hasCloak) {
                    visibilityMult *= 0.5D * (player.isCrouching() ? 0.65D : 1.0D);
                }
                if (hasSilentStriders) {
                    visibilityMult *= 0.65D;
                }
                if (hasEclipseShroud) {
                    visibilityMult *= 0.50D;
                }
                if (mob instanceof CryoStalkerEntity stalker) {
                    if (hasFullCryo) {
                        // Full Cryo-Insulated suit masks body warmth from Cryo-Stalker thermal tracking
                        visibilityMult *= 0.70D;
                    } else if (stalker.isStalking()) {
                        // Camouflaged ambush stalker retains heightened thermal scent on uninsulated prey
                        visibilityMult = Math.min(1.0D, visibilityMult * 1.25D);
                    }
                }
                if (visibilityMult < 0.999D) {
                    double baseFollow = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
                    double effectiveRange = baseFollow * visibilityMult;
                    if (mob.distanceToSqr(player) > effectiveRange * effectiveRange) {
                        mob.setTarget(null);
                        mob.getNavigation().stop();
                    }
                }
            }
        });

        // Biome 4: Aegis Shielding Charm Fatal-Blow Save
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (!entity.level().isClientSide()
                    && entity instanceof Player player
                    && entity.level() instanceof ServerLevel serverLevel
                    && !source.is(DamageTypes.GENERIC_KILL)
                    && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                if (AegisShieldingCharmItem.tryConsumeCharm(player)) {
                    player.setHealth(4.0F);

                    List<Holder<MobEffect>> harmfulEffects = new ArrayList<>();
                    for (MobEffectInstance inst : player.getActiveEffects()) {
                        if (inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                            harmfulEffects.add(inst.getEffect());
                        }
                    }
                    for (Holder<MobEffect> effect : harmfulEffects) {
                        player.removeEffect(effect);
                    }

                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, true, true));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1, false, true, true));

                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.85F, 1.25F);
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            player.getX(), player.getY() + 1.0D, player.getZ(),
                            32, 0.5D, 0.7D, 0.5D, 0.25D);
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });
    }
}
