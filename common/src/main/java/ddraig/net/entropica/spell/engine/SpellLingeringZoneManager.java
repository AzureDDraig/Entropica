package ddraig.net.entropica.spell.engine;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages active in-world lingering spell zones (proximity Glyph Mines, multi-second Skyfall
 * vortices/tempests, vine tethers, temporary ice/briar/moss constructs) and active elemental
 * Shroud Auras & lingering spatial debuffs on entities.
 */
public final class SpellLingeringZoneManager {

    // Base #1 Air
    public static final String TAG_AIR_SLIPSTREAM_UNTIL = "SpellAirSlipstreamUntil";
    public static final String TAG_AIR_SPRINT_CHARGE = "SpellAirSprintCharge";
    public static final String TAG_AIR_GUST_SHIELD_UNTIL = "SpellAirGustShieldUntil";

    // Base #2 Water
    public static final String TAG_SOAKED_UNTIL = "SpellWaterSoakedUntil";
    public static final String TAG_WATER_SHROUD_UNTIL = "SpellWaterOceanicShroudUntil";

    // Base #3 Nature
    public static final String TAG_NATURE_MANTLE_UNTIL = "SpellNatureSylvanMantleUntil";

    // Base #4 Earth
    public static final String TAG_EARTH_DEFENSE_UNTIL = "SpellEarthDefenseUntil";

    // Base #5 Frozen
    public static final String TAG_FROZEN_SPIKES_UNTIL = "SpellFrozenSpikesUntil";

    // Base #6 Nether
    public static final String TAG_NETHER_SHROUD_UNTIL = "SpellNetherImmolationUntil";
    public static final String TAG_UNQUENCHABLE_FIRE_UNTIL = "SpellNetherUnquenchableUntil";
    public static final String TAG_NETHER_AUTO_SMELT_UNTIL = "SpellNetherAutoSmeltUntil";

    // Base #7 Radiant
    public static final String TAG_RADIANT_SHROUD_UNTIL = "SpellRadiantPacifyUntil";
    public static final String TAG_RADIANT_PURIFY_DROPS_UNTIL = "SpellRadiantPurifyDropsUntil";

    // Base #8 Umbral
    public static final String TAG_UMBRAL_SHROUD_UNTIL = "SpellUmbralStealthUntil";
    public static final String TAG_UMBRAL_CHAIN_GROUP = "SpellUmbralChainGroup";
    public static final String TAG_UMBRAL_CHAIN_UNTIL = "SpellUmbralChainUntil";

    // Base #9 Undead
    public static final String TAG_UNDEAD_SHROUD_UNTIL = "SpellUndeadNecroticShroudUntil";
    public static final String TAG_FEARED_UNTIL = "SpellUndeadFearedUntil";
    public static final String TAG_FEAR_X = "SpellUndeadFearOriginX";
    public static final String TAG_FEAR_Z = "SpellUndeadFearOriginZ";

    // Base #10 Void
    public static final String TAG_VOID_DESTABILIZED_UNTIL = "SpellVoidDestabilizedUntil";
    public static final String TAG_VOID_ANCHOR_X = "SpellVoidAnchorX";
    public static final String TAG_VOID_ANCHOR_Y = "SpellVoidAnchorY";
    public static final String TAG_VOID_ANCHOR_Z = "SpellVoidAnchorZ";

    // Base #11 Arid
    public static final String TAG_ARID_VEIL_UNTIL = "SpellAridSandstormVeilUntil";
    public static final String TAG_ARID_MUMMIFY_DROPS_UNTIL = "SpellAridMummifyDropsUntil";

    // Base #12 Lightning
    public static final String TAG_LIGHTNING_SHROUD_UNTIL = "SpellLightningStaticShroudUntil";

    // Elemental Fusions (#13–#20)
    public static final String TAG_MAGMA_MANTLE_UNTIL = "SpellMagmaIgneousMantleUntil";
    public static final String TAG_STORM_SHROUD_UNTIL = "SpellStormSquallShroudUntil";
    public static final String TAG_GLACIAL_SHROUD_UNTIL = "SpellGlacialCryoShroudUntil";
    public static final String TAG_OVERGROWTH_BASTION_UNTIL = "SpellOvergrowthBastionUntil";
    public static final String TAG_OVERGROWTH_STILL_TICKS = "SpellOvergrowthStillTicks";
    public static final String TAG_OVERGROWTH_LAST_X = "SpellOvergrowthLastX";
    public static final String TAG_OVERGROWTH_LAST_Z = "SpellOvergrowthLastZ";
    public static final String TAG_DUST_VEIL_UNTIL = "SpellDustChalkVeilUntil";
    public static final String TAG_SPORE_MANTLE_UNTIL = "SpellSporeCloudMantleUntil";
    public static final String TAG_SPORE_ORBIT_UNTIL = "SpellSporeOrbitUntil";
    public static final String TAG_SPORE_INFESTED_UNTIL = "SpellSporeInfestedUntil";
    public static final String TAG_TAIGA_SHROUD_UNTIL = "SpellTaigaSolsticeShroudUntil";
    public static final String TAG_TAIGA_FROST_WALKER_UNTIL = "SpellTaigaFrostWalkerUntil";
    public static final String TAG_TAIGA_COLD_IMMUNE_UNTIL = "SpellTaigaColdImmuneUntil";
    public static final String TAG_TAIGA_BRITTLE_UNTIL = "SpellTaigaBrittleMarkUntil";
    public static final String TAG_OASIS_CARAPACE_UNTIL = "SpellOasisThornCarapaceUntil";

    // Vital Fusions (#21–#24) & Shared Tactical Estados
    public static final String TAG_VITAE_SHROUD_UNTIL = "SpellVitaeOverdriveShroudUntil";
    public static final String TAG_VITAE_OVERCHARGE_UNTIL = "SpellVitaeOverchargeMarkUntil";
    public static final String TAG_VITAE_DEATH_BLOOM_UNTIL = "SpellVitaeDeathBloomUntil";
    public static final String TAG_VITAE_DISORIENTED_UNTIL = "SpellVitaeDisorientedUntil";
    public static final String TAG_BLOOD_CARAPACE_UNTIL = "SpellBloodCrimsonCarapaceUntil";
    public static final String TAG_BLOOD_HEMORRHAGE_UNTIL = "SpellBloodHemorrhageUntil";
    public static final String TAG_BLOOD_FEAST_UNTIL = "SpellBloodFeastMarkUntil";
    public static final String TAG_AURA_SHROUD_UNTIL = "SpellAuraChakraShroudUntil";
    public static final String TAG_AMBER_MANTLE_UNTIL = "SpellAmberBastionMantleUntil";
    public static final String TAG_AMBER_FOSSILIZE_UNTIL = "SpellAmberFossilizeMarkUntil";
    public static final String TAG_AMBER_FRAGILE_STATUE_UNTIL = "SpellAmberFragileStatueUntil";
    public static final String TAG_NO_JUMP_UNTIL = "SpellNoJumpPinnedUntil";
    public static final String TAG_PACIFIED_UNTIL = "SpellPacifiedUntil";
    public static final String TAG_CRYO_COCOON_UNTIL = "SpellGlacialCryoCocoonUntil";
    public static final String TAG_SILENT_VIBRATION_UNTIL = "SpellSilentVibrationUntil";
    public static final String TAG_SILENCED_ANCHOR_X = "SpellSilencedAnchorX";
    public static final String TAG_SILENCED_ANCHOR_Y = "SpellSilencedAnchorY";
    public static final String TAG_SILENCED_ANCHOR_Z = "SpellSilencedAnchorZ";

    // Ethereal & Paradox Fusions (#25–#36)
    public static final String TAG_ECLIPSE_BASTION_UNTIL = "SpellEclipseBastionUntil";
    public static final String TAG_ECLIPSE_CORONA_BURN_UNTIL = "SpellEclipseCoronaBurnUntil";
    public static final String TAG_BLIGHT_CARAPACE_UNTIL = "SpellBlightCarapaceUntil";
    public static final String TAG_BLIGHT_CHAIN_MARK_UNTIL = "SpellBlightChainDeathMarkUntil";
    public static final String TAG_BLIGHT_CORPSE_SNARE_UNTIL = "SpellBlightCorpseSnareUntil";
    public static final String TAG_ASTRAL_SHROUD_UNTIL = "SpellAstralStarlightShroudUntil";
    public static final String TAG_SOULFIRE_SHROUD_UNTIL = "SpellSoulfireShroudUntil";
    public static final String TAG_SOULFIRE_BURN_UNTIL = "SpellSoulfireBurnUntil";
    public static final String TAG_SOULFIRE_CHAIN_BURST_UNTIL = "SpellSoulfireChainBurstUntil";
    public static final String TAG_SOULFIRE_SOULSAND_MARK_UNTIL = "SpellSoulfireSoulSandMarkUntil";
    public static final String TAG_VAPOR_MIST_SHROUD_UNTIL = "SpellVaporMistShroudUntil";
    public static final String TAG_VAPOR_BOIL_DEATH_UNTIL = "SpellVaporBoilDeathUntil";
    public static final String TAG_NULL_R_ORBIT_UNTIL = "SpellNullROrbitUntil";
    public static final String TAG_NULL_R_BASTION_UNTIL = "SpellNullRBastionUntil";
    public static final String TAG_NULL_U_ORBIT_UNTIL = "SpellNullUOrbitUntil";
    public static final String TAG_NULL_U_VEIL_UNTIL = "SpellNullUVeilUntil";
    public static final String TAG_DAWN_ORBIT_UNTIL = "SpellDawnOrbitUntil";
    public static final String TAG_DAWN_SHROUD_UNTIL = "SpellDawnSolarShroudUntil";
    public static final String TAG_ABYSS_ORBIT_UNTIL = "SpellAbyssOrbitUntil";
    public static final String TAG_ABYSS_SHROUD_UNTIL = "SpellAbyssTrenchShroudUntil";
    public static final String TAG_ABYSS_LAND_DROWNING_UNTIL = "SpellAbyssLandDrowningUntil";
    public static final String TAG_AEGIS_ORBIT_UNTIL = "SpellAegisOrbitUntil";
    public static final String TAG_AEGIS_MANTLE_UNTIL = "SpellAegisCitadelMantleUntil";
    public static final String TAG_AEGIS_TETHER_GUARD_UUID = "SpellAegisTetherGuardUUID";
    public static final String TAG_AEGIS_TETHER_UNTIL = "SpellAegisTetherUntil";
    public static final String TAG_AURORA_ORBIT_UNTIL = "SpellAuroraOrbitUntil";
    public static final String TAG_AURORA_MANTLE_UNTIL = "SpellAuroraVeilMantleUntil";
    public static final String TAG_AURORA_BRITTLE_UNTIL = "SpellAuroraBrittleUntil";
    public static final String TAG_MIRAGE_ORBIT_UNTIL = "SpellMirageOrbitUntil";
    public static final String TAG_MIRAGE_SHROUD_UNTIL = "SpellMirageGlamourShroudUntil";
    public static final String TAG_MIRAGE_TRAITOR_UNTIL = "SpellMirageTraitorUntil";
    public static final String TAG_MIRAGE_PARANOIA_CASTER_UUID = "SpellMirageParanoiaCasterUUID";
    public static final String TAG_MIRAGE_PARANOIA_UNTIL = "SpellMirageParanoiaUntil";
    public static final String TAG_MIRAGE_REDIRECT_UUID = "SpellMirageRedirectTargetUUID";
    public static final String TAG_MIRAGE_REDIRECT_UNTIL = "SpellMirageRedirectUntil";

    // Ethereal (#37–#38), Tier 3 Trinity (#39–#47) & Chimera Catalyst (Sec 6.38–6.49)
    public static final String TAG_AI_PATH_DISRUPTED_UNTIL = "SpellAiPathDisruptedUntil";
    public static final String TAG_INTANGIBLE_OFFENSIVE_UNTIL = "SpellWraithIntangibleOffensiveUntil";
    public static final String TAG_INTANGIBLE_CASTER_UNTIL = "SpellWraithIntangibleCasterUntil";
    public static final String TAG_VULNERABLE_25_UNTIL = "SpellVulnerable25Until";
    public static final String TAG_FALL_VULN_50_UNTIL = "SpellFallVuln50Until";

    public static final String TAG_WRAITH_ORBIT_UNTIL = "SpellWraithOrbitUntil";
    public static final String TAG_WRAITH_SHROUD_UNTIL = "SpellWraithSpecterArmorUntil";
    public static final String TAG_BARROW_ORBIT_UNTIL = "SpellBarrowOrbitUntil";
    public static final String TAG_BARROW_SHROUD_UNTIL = "SpellBarrowMiasmaShroudUntil";
    public static final String TAG_PYRE_ORBIT_UNTIL = "SpellPyreOrbitUntil";
    public static final String TAG_PYRE_SHROUD_UNTIL = "SpellPyreHearthShroudUntil";
    public static final String TAG_PYRE_MELEE_BOOST_UNTIL = "SpellPyreMeleeBoostUntil";
    public static final String TAG_PENUMBRA_ORBIT_UNTIL = "SpellPenumbraOrbitUntil";
    public static final String TAG_PENUMBRA_MANTLE_UNTIL = "SpellPenumbraCoronaMantleUntil";
    public static final String TAG_PENUMBRA_SHIMMER_UNTIL = "SpellPenumbraShimmerUntil";
    public static final String TAG_PENUMBRA_SHIMMER_DODGE = "SpellPenumbraShimmerDodgePct";
    public static final String TAG_PENUMBRA_DAZZLE_UNTIL = "SpellPenumbraDazzleUntil";
    public static final String TAG_RIME_ORBIT_UNTIL = "SpellRimeOrbitUntil";
    public static final String TAG_RIME_MANTLE_UNTIL = "SpellRimeHoarfrostMantleUntil";
    public static final String TAG_RIME_FIRE_RES_UNTIL = "SpellRimeHoarfrostFireResUntil";
    public static final String TAG_SPRING_ORBIT_UNTIL = "SpellSpringOrbitUntil";
    public static final String TAG_SPRING_MANTLE_UNTIL = "SpellSpringFountainheadMantleUntil";
    public static final String TAG_SPRING_UNDEAD_STEALTH_UNTIL = "SpellSpringUndeadStealthUntil";
    public static final String TAG_STATIC_ORBIT_UNTIL = "SpellStaticOrbitUntil";
    public static final String TAG_STATIC_SHROUD_UNTIL = "SpellStaticFaradayShroudUntil";
    public static final String TAG_STATIC_SENTRY_IMMUNE_UNTIL = "SpellStaticSentryImmuneUntil";
    public static final String TAG_STATIC_SPRINT_SHOCK_UNTIL = "SpellStaticSprintShockUntil";
    public static final String TAG_SYLVAN_ORBIT_UNTIL = "SpellSylvanOrbitUntil";
    public static final String TAG_SYLVAN_AEGIS_UNTIL = "SpellSylvanPhotosyntheticAegisUntil";
    public static final String TAG_SYLVAN_AEGIS_HITS = "SpellSylvanPhotosyntheticAegisHits";
    public static final String TAG_MIASMA_CARAPACE_UNTIL = "SpellMiasmaMireCarapaceUntil";
    public static final String TAG_MIASMA_SEPTIC_IMMUNE_UNTIL = "SpellMiasmaSepticImmuneUntil";
    public static final String TAG_GENESIS_ORBIT_UNTIL = "SpellGenesisOrbitUntil";
    public static final String TAG_GENESIS_SHROUD_UNTIL = "SpellGenesisAmnioticShroudUntil";
    public static final String TAG_OBLIVION_SHROUD_UNTIL = "SpellOblivionVoidShroudUntil";
    public static final String TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL = "SpellOblivionExtinctionArmorUntil";
    public static final String TAG_CHIMERA_ORBIT_UNTIL = "SpellChimeraOrbitUntil";
    public static final String TAG_CHIMERA_ORBIT_ELEMENT = "SpellChimeraOrbitElement";
    public static final String TAG_CHIMERA_SHROUD_UNTIL = "SpellChimeraFluxShroudUntil";
    public static final String TAG_CHIMERA_PRIMAL_ROAR_UNTIL = "SpellChimeraPrimalRoarUntil";

    // Tier 4 Apex (#48–#50), Prismatic Catalyst (#51), Tier 5 Eschaton (#52–#54) & Tier 6 Singularity (#55–#57)
    public static final String TAG_ARCANE_3D_FLIGHT_UNTIL = "SpellArcane3DFlightUntil";
    public static final String TAG_GRANTED_MAYFLY = "SpellArcaneGrantedMayfly";
    public static final String TAG_ARCANE_WINGS_ACTIVE = "SpellArcaneWingsActive";
    public static final String TAG_ARCANE_WING_PRIMARY = "SpellArcaneWingPrimaryOrd";
    public static final String TAG_ARCANE_WING_SECONDARY = "SpellArcaneWingSecondaryOrd";
    public static final String TAG_ARCANE_WING_TIER = "SpellArcaneWingTier";
    public static final String TAG_COMPLETE_INVULNERABILITY_UNTIL = "SpellCompleteInvulnerabilityUntil";
    public static final String TAG_VOID_IMMUNE_UNTIL = "SpellVoidDamageImmuneUntil";

    public static final String TAG_AETHER_ORBIT_UNTIL = "SpellAetherOrbitUntil";
    public static final String TAG_AETHER_SHROUD_UNTIL = "SpellAetherQuintessenceShroudUntil";
    public static final String TAG_AETHER_AEGIS_UNTIL = "SpellAetherFirmamentAegisUntil";

    public static final String TAG_ENTROPIC_SHROUD_UNTIL = "SpellEntropicEventHorizonMantleUntil";
    public static final String TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL = "SpellEntropicLifestealShroudUntil";

    public static final String TAG_CELESTIAL_ORBIT_UNTIL = "SpellCelestialOrbitUntil";
    public static final String TAG_CELESTIAL_SHROUD_UNTIL = "SpellCelestialSupernovaShroudUntil";
    public static final String TAG_CELESTIAL_ASCENDANCE_UNTIL = "SpellCelestialAscendanceShroudUntil";

    public static final String TAG_PRISMATIC_ORBIT_UNTIL = "SpellPrismaticOrbitUntil";
    public static final String TAG_PRISMATIC_SHROUD_UNTIL = "SpellPrismaticDispersionAuraUntil";
    public static final String TAG_PRISMATIC_DEFLECT_SENTRY_UNTIL = "SpellPrismaticSentryReflectUntil";

    public static final String TAG_EMPYREAN_ORBIT_UNTIL = "SpellEmpyreanOrbitUntil";
    public static final String TAG_EMPYREAN_SHROUD_UNTIL = "SpellEmpyreanCanopyMantleUntil";
    public static final String TAG_EMPYREAN_AEGIS_UNTIL = "SpellEmpyreanInfiniteLifeAegisUntil";

    public static final String TAG_CATACLYSM_ORBIT_UNTIL = "SpellCataclysmOrbitUntil";
    public static final String TAG_CATACLYSM_SHROUD_UNTIL = "SpellCataclysmWorldBreakerMantleUntil";

    public static final String TAG_TERMINUS_ORBIT_UNTIL = "SpellTerminusOrbitUntil";
    public static final String TAG_TERMINUS_SHROUD_UNTIL = "SpellTerminusStasisShieldUntil";
    public static final String TAG_TERMINUS_MANTLE_UNTIL = "SpellTerminusGlacialMantleUntil";
    public static final String TAG_TERMINUS_ELIXIR_UNTIL = "SpellTerminusFrostElixirUntil";

    public static final String TAG_ESCHATON_ORBIT_UNTIL = "SpellEschatonOrbitUntil";
    public static final String TAG_ESCHATON_SHROUD_UNTIL = "SpellEschatonOmegaMantleUntil";
    public static final String TAG_ESCHATON_STASIS_ELIXIR_UNTIL = "SpellEschatonStasisElixirUntil";

    public static final String TAG_APOTHEOSIS_ORBIT_UNTIL = "SpellApotheosisOrbitUntil";
    public static final String TAG_APOTHEOSIS_SHROUD_UNTIL = "SpellApotheosisAlphaMantleUntil";

    public static final String TAG_ENTROPICA_ORBIT_UNTIL = "SpellEntropicaOrbitUntil";
    public static final String TAG_ENTROPICA_SHROUD_UNTIL = "SpellEntropicaMantleUntil";

    @FunctionalInterface
    public interface ZoneTickBehavior {
        /**
         * @return {@code true} to keep the zone alive, or {@code false} to expire/detonate immediately.
         */
        boolean tick(ServerLevel level, ActiveSpellZone zone, int elapsedTicks);
    }

    public static final class ActiveSpellZone {
        public final UUID casterId;
        public final Vec3 center;
        public final double radius;
        public final int maxDurationTicks;
        public final long createdGameTime;
        public final SpellAssembly spell;
        public final SubSpellSlot slot;
        @Nullable
        public final EssenceType fallbackMateria;
        public final float apparatusPotency;
        public final ZoneTickBehavior behavior;

        public ActiveSpellZone(
                @Nullable LivingEntity caster,
                Vec3 center,
                double radius,
                int maxDurationTicks,
                long createdGameTime,
                SpellAssembly spell,
                SubSpellSlot slot,
                @Nullable EssenceType fallbackMateria,
                float apparatusPotency,
                ZoneTickBehavior behavior
        ) {
            this.casterId = caster != null ? caster.getUUID() : null;
            this.center = center;
            this.radius = radius;
            this.maxDurationTicks = maxDurationTicks;
            this.createdGameTime = createdGameTime;
            this.spell = spell;
            this.slot = slot;
            this.fallbackMateria = fallbackMateria;
            this.apparatusPotency = apparatusPotency;
            this.behavior = behavior;
        }

        @Nullable
        public LivingEntity resolveCaster(ServerLevel level) {
            if (this.casterId == null) return null;
            Entity e = level.getEntity(this.casterId);
            return e instanceof LivingEntity l ? l : null;
        }
    }

    private static final Map<ResourceKey<Level>, List<ActiveSpellZone>> ZONES_BY_DIMENSION = new HashMap<>();

    private SpellLingeringZoneManager() {
    }

    public static void registerZone(ServerLevel level, ActiveSpellZone zone) {
        ActiveSpellZone toRegister = zone;
        if (zone.spell != null
                && zone.spell.flowPart().mutationEnabled()
                && zone.spell.flowPart().resolveEffectiveMateria(zone.spell.materiaSpecific(), zone.fallbackMateria) == EssenceType.AMBER) {
            // Section 6.25 Amber Resinite Viscous Linger Flow: +50% duration on lingering zones/traps + escalating 10%/s up to 50% slow!
            int extendedDuration = Math.max(zone.maxDurationTicks + 10, (int) Math.ceil(zone.maxDurationTicks * 1.5D));
            ZoneTickBehavior inner = zone.behavior;
            ZoneTickBehavior wrapped = (srv, z, elapsed) -> {
                if (elapsed <= z.maxDurationTicks && elapsed % 20 == 0) {
                    LivingEntity owner = z.resolveCaster(srv);
                    int amp = Math.min(2, elapsed / 40); // escalates from Slowness I (~15%) up to Slowness III (~45-50%)
                    AABB amberBox = new AABB(z.center.x - z.radius, z.center.y - 1.5D, z.center.z - z.radius,
                            z.center.x + z.radius, z.center.y + 2.5D, z.center.z + z.radius);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, amberBox, e -> e.isAlive() && e != owner)) {
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, z.spell.hasWardFriendlyFireSafeguard())) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, amp, true, true));
                    }
                }
                return inner.tick(srv, z, elapsed);
            };
            toRegister = new ActiveSpellZone(
                    zone.resolveCaster(level),
                    zone.center,
                    zone.radius,
                    extendedDuration,
                    zone.createdGameTime,
                    zone.spell,
                    zone.slot,
                    zone.fallbackMateria,
                    zone.apparatusPotency,
                    wrapped
            );
        }
        ZONES_BY_DIMENSION.computeIfAbsent(level.dimension(), k -> new ArrayList<>()).add(toRegister);
        if (toRegister.spell != null && toRegister.radius >= 1.5D) {
            EssenceType zoneMat = toRegister.fallbackMateria;
            if (toRegister.slot != null) {
                EssenceType fm = toRegister.slot.formPart().resolveEffectiveMateria(toRegister.spell.materiaSpecific(), toRegister.fallbackMateria);
                zoneMat = fm != EssenceType.REGULAR ? fm : toRegister.slot.primaryEffect().resolveEffectiveMateria(toRegister.spell.materiaSpecific(), toRegister.fallbackMateria);
            }
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastArcaneSeal(
                    level,
                    toRegister.center,
                    toRegister.radius,
                    zoneMat,
                    null,
                    toRegister.maxDurationTicks,
                    toRegister.spell.getEffectiveTier()
            );
        }
    }

    public static int getActiveZoneCount(ResourceKey<Level> dimension) {
        List<ActiveSpellZone> list = ZONES_BY_DIMENSION.get(dimension);
        return list == null ? 0 : list.size();
    }

    public static boolean isEntityWetOrSoaked(LivingEntity entity) {
        if (entity.isInWaterOrRain()) return true;
        long now = entity.level().getGameTime();
        return now <= EntityHelper.getPersistentData(entity).getLong(TAG_SOAKED_UNTIL).orElse(0L);
    }

    public static void applySoaked(LivingEntity entity, int durationTicks) {
        long until = entity.level().getGameTime() + Math.max(20, durationTicks);
        CompoundTag data = EntityHelper.getPersistentData(entity);
        data.putLong(TAG_SOAKED_UNTIL, Math.max(until, data.getLong(TAG_SOAKED_UNTIL).orElse(0L)));
        data.remove(TAG_UNQUENCHABLE_FIRE_UNTIL);
        entity.clearFire();
    }

    public static void applyFearFlee(LivingEntity target, Vec3 fearOrigin, int durationTicks) {
        if (target.level().isClientSide()) return;
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(TAG_FEARED_UNTIL, target.level().getGameTime() + Math.max(20, durationTicks));
        data.putDouble(TAG_FEAR_X, fearOrigin.x);
        data.putDouble(TAG_FEAR_Z, fearOrigin.z);
        if (target instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    /**
     * Sustains genuine AI pacification across ticks so hostile/neutral mobs drop aggro and do not immediately re-acquire targets.
     */
    public static void applyPacified(LivingEntity target, int durationTicks) {
        if (target.level().isClientSide()) return;
        long until = target.level().getGameTime() + Math.max(10, durationTicks);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(TAG_PACIFIED_UNTIL, Math.max(until, data.getLong(TAG_PACIFIED_UNTIL).orElse(0L)));
        target.setLastHurtByMob(null);
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.setAggressive(false);
        }
        if (target instanceof NeutralMob neutral) {
            neutral.stopBeingAngry();
        }
    }

    /**
     * Applies Arcane Silence to players (blocking apparatus spellcasting) and monsters (suppressing Creeper swell,
     * Enderman/Shulker teleports, Evoker/Witch/Blaze/Ghast special casts, and Warden sonic booms).
     */
    public static void applyArcaneSilence(LivingEntity target, int durationTicks) {
        if (target.level().isClientSide()) return;
        long until = target.level().getGameTime() + Math.max(10, durationTicks);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong("SpellSilencedUntil", Math.max(until, data.getLong("SpellSilencedUntil").orElse(0L)));
        data.putDouble(TAG_SILENCED_ANCHOR_X, target.getX());
        data.putDouble(TAG_SILENCED_ANCHOR_Y, target.getY());
        data.putDouble(TAG_SILENCED_ANCHOR_Z, target.getZ());
        if (target instanceof Creeper creeper) {
            creeper.setSwellDir(-1);
        }
    }

    /**
     * Scrambles hostile monster AI into a Paranoid / Traitor frenzy (#36 Mirage), forcing them to drop aggro on
     * the caster and actively pursue & strike nearby monsters!
     */
    public static void applyMirageTraitorFrenzy(LivingEntity target, int durationTicks) {
        if (target.level().isClientSide()) return;
        long until = target.level().getGameTime() + Math.max(20, durationTicks);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(TAG_MIRAGE_TRAITOR_UNTIL, Math.max(until, data.getLong(TAG_MIRAGE_TRAITOR_UNTIL).orElse(0L)));
        if (target instanceof Mob mob && target.level() instanceof ServerLevel srv) {
            mob.setTarget(null);
            for (Monster other : srv.getEntitiesOfClass(Monster.class, mob.getBoundingBox().inflate(12.0D))) {
                if (other != mob && other.isAlive()) {
                    mob.setTarget(other);
                    mob.setLastHurtByMob(other);
                    mob.setAggressive(true);
                    break;
                }
            }
        }
    }

    /**
     * Inflicts Benthic Land-Drowning (#33 Abyss), draining oxygen bubbles even on dry land!
     */
    public static void applyAbyssLandDrowning(LivingEntity target, int durationTicks) {
        if (target.level().isClientSide()) return;
        long until = target.level().getGameTime() + Math.max(20, durationTicks);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(TAG_ABYSS_LAND_DROWNING_UNTIL, Math.max(until, data.getLong(TAG_ABYSS_LAND_DROWNING_UNTIL).orElse(0L)));
    }

    /**
     * Disrupts hostile mob AI pathfinding for {@code durationTicks} (e.g. 25.0s / 500t on Tier 3 Trinity Cones
     * and Penumbra Contrast-Collapse Hex), forcing afflicted mobs to stumble off-course and lose navigation paths.
     */
    public static void applyAiPathDisruption(LivingEntity target, int durationTicks) {
        if (target.level().isClientSide()) return;
        long until = target.level().getGameTime() + Math.max(20, durationTicks);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(TAG_AI_PATH_DISRUPTED_UNTIL, Math.max(until, data.getLong(TAG_AI_PATH_DISRUPTED_UNTIL).orElse(0L)));
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
        }
    }

    /**
     * Places {@code target} into spectral intangible phase-state (#37 Wraith):
     * either beneficial caster phase-step ({@code casterBeneficial == true}, phasing harmlessly through attacks)
     * or offensive ghost-stasis ({@code casterBeneficial == false}, unable to deal damage or use abilities).
     */
    public static void applyIntangiblePhase(LivingEntity target, int durationTicks, boolean casterBeneficial) {
        if (target.level().isClientSide()) return;
        long until = target.level().getGameTime() + Math.max(10, durationTicks);
        CompoundTag data = EntityHelper.getPersistentData(target);
        if (casterBeneficial) {
            data.putLong(TAG_INTANGIBLE_CASTER_UNTIL, Math.max(until, data.getLong(TAG_INTANGIBLE_CASTER_UNTIL).orElse(0L)));
        } else {
            data.putLong(TAG_INTANGIBLE_OFFENSIVE_UNTIL, Math.max(until, data.getLong(TAG_INTANGIBLE_OFFENSIVE_UNTIL).orElse(0L)));
            applyArcaneSilence(target, durationTicks);
        }
    }

    /**
     * Spawns a collectable 1-heart restorative orb (#42 Spring Healing Dew-Orb / #44 Sylvan Golden Acorn Orb)
     * that any friendly player or summon can step across within 6.0 seconds to absorb {@code healHp} health.
     */
    public static void spawnCollectableHealingOrb(
            ServerLevel level,
            Vec3 pos,
            @Nullable LivingEntity owner,
            float healHp,
            boolean goldenAcorn
    ) {
        registerZone(level, new ActiveSpellZone(
                owner, pos, 1.75D, 120, level.getGameTime(), null, null,
                goldenAcorn ? EssenceType.SYLVAN : EssenceType.SPRING, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(goldenAcorn ? ParticleTypes.WAX_ON : ParticleTypes.DRIPPING_WATER,
                                zone.center.x, zone.center.y + 0.35D, zone.center.z, 3, 0.15D, 0.15D, 0.15D, 0.02D);
                        srv.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                zone.center.x, zone.center.y + 0.35D, zone.center.z, 2, 0.18D, 0.18D, 0.18D, 0.02D);
                    }
                    LivingEntity caster = zone.resolveCaster(srv);
                    AABB pickupBox = new AABB(zone.center.x - 1.65D, zone.center.y - 0.8D, zone.center.z - 1.65D,
                            zone.center.x + 1.65D, zone.center.y + 1.8D, zone.center.z + 1.65D);
                    for (LivingEntity cand : srv.getEntitiesOfClass(LivingEntity.class, pickupBox)) {
                        if (!cand.isAlive() || cand instanceof Monster) continue;
                        if (caster != null && cand != caster && !cand.isAlliedTo(caster)
                                && !(cand instanceof net.minecraft.world.entity.player.Player)
                                && !(cand instanceof SpellEidolonEntity eid && eid.isFriendlyTo(caster))) {
                            continue;
                        }
                        if (cand.getHealth() >= cand.getMaxHealth() - 0.05F && elapsed < 20) continue;
                        SpellCombatReactionEngine.notifyMagicalHeal(cand, healHp);
                        cand.heal(healHp);
                        srv.sendParticles(ParticleTypes.HEART, cand.getX(), cand.getY(0.9D), cand.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.03D);
                        srv.playSound(null, cand.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.55F, 1.45F);
                        return false;
                    }
                    return true;
                }
        ));
    }

    /**
     * Temporarily powers redstone lamps and redstone circuitry within {@code radius} (#27 Astral)
     * without permanently altering or griefing player builds!
     */
    public static void pulseTemporaryRedstonePower(ServerLevel level, BlockPos center, int radius, int durationTicks) {
        int r = Math.clamp(radius, 1, 8);
        List<BlockPos> litLamps = new ArrayList<>();
        List<BlockPos> tempTorches = new ArrayList<>();

        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.REDSTONE_LAMP) && bs.hasProperty(net.minecraft.world.level.block.RedstoneLampBlock.LIT)
                    && !bs.getValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT, true));
                litLamps.add(p.immutable());
            } else if ((bs.is(Blocks.REDSTONE_WIRE) || bs.is(Blocks.REPEATER) || bs.is(Blocks.COMPARATOR)
                    || bs.is(Blocks.PISTON) || bs.is(Blocks.STICKY_PISTON) || bs.is(Blocks.NOTE_BLOCK) || bs.is(Blocks.BELL))
                    && tempTorches.size() < 4) {
                BlockPos above = p.above();
                if (level.getBlockState(above).isAir() && Blocks.REDSTONE_TORCH.defaultBlockState().canSurvive(level, above)) {
                    level.setBlockAndUpdate(above, Blocks.REDSTONE_TORCH.defaultBlockState());
                    tempTorches.add(above.immutable());
                }
            }
        }

        if (litLamps.isEmpty() && tempTorches.isEmpty()) {
            return;
        }

        int ticks = Math.max(10, durationTicks);
        registerZone(level, new ActiveSpellZone(
                null, Vec3.atCenterOf(center), r, ticks, level.getGameTime(), null, null, EssenceType.ASTRAL, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) {
                        return true;
                    }
                    for (BlockPos torchPos : tempTorches) {
                        if (srv.getBlockState(torchPos).is(Blocks.REDSTONE_TORCH)) {
                            srv.removeBlock(torchPos, false);
                        }
                    }
                    for (BlockPos lampPos : litLamps) {
                        BlockState cur = srv.getBlockState(lampPos);
                        if (cur.is(Blocks.REDSTONE_LAMP) && cur.hasProperty(net.minecraft.world.level.block.RedstoneLampBlock.LIT)
                                && !srv.hasNeighborSignal(lampPos)) {
                            srv.setBlockAndUpdate(lampPos, cur.setValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT, false));
                        }
                    }
                    return false;
                }
        ));
    }

    public static void tickServerLevel(ServerLevel level) {
        long now = level.getGameTime();

        // 1. Tick active lingering Glyph Mines, Skyfall Tempests, Tethers, and Temporary Constructs
        List<ActiveSpellZone> zones = ZONES_BY_DIMENSION.get(level.dimension());
        if (zones != null && !zones.isEmpty()) {
            Iterator<ActiveSpellZone> it = zones.iterator();
            while (it.hasNext()) {
                ActiveSpellZone zone = it.next();
                int elapsed = (int) (now - zone.createdGameTime);
                if (elapsed > zone.maxDurationTicks) {
                    // Give behaviors one final cleanup call on expiry when elapsed > maxDurationTicks
                    zone.behavior.tick(level, zone, elapsed);
                    it.remove();
                    continue;
                }
                boolean keepAlive = zone.behavior.tick(level, zone, elapsed);
                if (!keepAlive) {
                    it.remove();
                } else if (zone.spell != null && zone.radius >= 1.5D && elapsed % 12 == 0) {
                    EssenceType zoneMat = zone.fallbackMateria;
                    if (zone.slot != null) {
                        EssenceType fm = zone.slot.formPart().resolveEffectiveMateria(zone.spell.materiaSpecific(), zone.fallbackMateria);
                        zoneMat = fm != EssenceType.REGULAR ? fm : zone.slot.primaryEffect().resolveEffectiveMateria(zone.spell.materiaSpecific(), zone.fallbackMateria);
                    }
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnZonePerimeterParticles(
                            level, zone.center, zone.radius, zoneMat, elapsed
                    );
                }
            }
        }

        // 2. Tick active Elemental Shroud Auras, Fear Panic, Unquenchable Fire & Spatial Anchors every 4 ticks
        if (now % 4L != 0L) {
            return;
        }

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            CompoundTag tag = EntityHelper.getPersistentData(living);

            // Air (#1) Slipstream Shield with sprint-charge bowling buildup
            if (tag.getLong(TAG_AIR_SLIPSTREAM_UNTIL).orElse(0L) > now) {
                if (living.isSprinting()) {
                    int charge = Math.min(10, tag.getInt(TAG_AIR_SPRINT_CHARGE).orElse(0) + 1);
                    tag.putInt(TAG_AIR_SPRINT_CHARGE, charge);
                    tickAirSlipstreamSprintCollision(level, living, charge);
                } else {
                    tag.putInt(TAG_AIR_SPRINT_CHARGE, 0);
                }
            }

            // Water (#2) Oceanic Shroud: instant auto-extinguishing of fire (including unquenchable fire)
            if (tag.getLong(TAG_WATER_SHROUD_UNTIL).orElse(0L) > now) {
                tag.remove(TAG_UNQUENCHABLE_FIRE_UNTIL);
                if (living.isOnFire()) {
                    living.clearFire();
                }
            }

            // Nether (#6) Unquenchable Netherfire: burns continuously even in rain or underwater!
            if (tag.getLong(TAG_UNQUENCHABLE_FIRE_UNTIL).orElse(0L) > now) {
                living.igniteForSeconds(2);
                if (living.isInWaterOrRain()) {
                    level.sendParticles(ParticleTypes.FLAME, living.getX(), living.getY(0.6D), living.getZ(),
                            3, 0.2D, 0.25D, 0.2D, 0.02D);
                }
            }

            // Nature (#3) Sylvan Mantle: bloom flowers/grass underfoot, +30% speed near foliage, sunlight regen, wildlife calm
            if (tag.getLong(TAG_NATURE_MANTLE_UNTIL).orElse(0L) > now && now % 20L == 0L) {
                tickNatureSylvanMantle(level, living);
            }

            // Nether (#6) Immolation Shroud: lava buoyancy & melt nearby ice/snow
            if (tag.getLong(TAG_NETHER_SHROUD_UNTIL).orElse(0L) > now) {
                tickNetherImmolationShroud(level, living, now);
            }

            // Radiant (#7) Pacification Shroud: pacify neutral mobs & Endermen
            if (tag.getLong(TAG_RADIANT_SHROUD_UNTIL).orElse(0L) > now && now % 12L == 0L) {
                tickRadiantPacificationShroud(level, living);
            }

            // Umbral (#8) Shroud of Silence & Stealth: obscure caster, blind adjacent foes, clear Warden anger & distant aggro
            if (tag.getLong(TAG_UMBRAL_SHROUD_UNTIL).orElse(0L) > now && now % 12L == 0L) {
                tickUmbralStealthShroud(level, living);
            }

            // Undead (#9) Necrotic Shroud: befriend vanilla undead & drain 1 HP/s from living hostiles within 3m
            if (tag.getLong(TAG_UNDEAD_SHROUD_UNTIL).orElse(0L) > now && now % 20L == 0L) {
                tickUndeadNecroticShroud(level, living);
            }

            // Undead (#9) Screaming Skull Swarm Fear Panic: force PathfinderMobs to flee away from fear origin!
            if (tag.getLong(TAG_FEARED_UNTIL).orElse(0L) > now && living instanceof PathfinderMob pfMob) {
                pfMob.setTarget(null);
                double fx = tag.getDouble(TAG_FEAR_X).orElse(pfMob.getX());
                double fz = tag.getDouble(TAG_FEAR_Z).orElse(pfMob.getZ());
                Vec3 fleeDir = new Vec3(pfMob.getX() - fx, 0.0D, pfMob.getZ() - fz);
                if (fleeDir.lengthSqr() < 1.0E-4D) {
                    fleeDir = new Vec3(1.0D, 0.0D, 0.0D);
                }
                Vec3 dest = pfMob.position().add(fleeDir.normalize().scale(7.0D));
                pfMob.getNavigation().moveTo(dest.x, pfMob.getY(), dest.z, 1.35D);
                level.sendParticles(ParticleTypes.SOUL, pfMob.getX(), pfMob.getY(0.9D), pfMob.getZ(),
                        1, 0.15D, 0.15D, 0.15D, 0.01D);
            }

            // Void (#10) Spacetime Anchoring Curse anti-teleport clamp (blocks Enderman/Chorus/Pearl/Shulker teleports!)
            if (SpellCombatReactionEngine.isBlinkAnchored(living, now)) {
                double ax = tag.getDouble(TAG_VOID_ANCHOR_X).orElse(living.getX());
                double ay = tag.getDouble(TAG_VOID_ANCHOR_Y).orElse(living.getY());
                double az = tag.getDouble(TAG_VOID_ANCHOR_Z).orElse(living.getZ());
                if (living.position().distanceToSqr(ax, ay, az) > 36.0D) {
                    living.teleportTo(ax, ay, az);
                    living.setDeltaMovement(Vec3.ZERO);
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, ax, ay + 0.8D, az,
                            18, 0.3D, 0.45D, 0.3D, 0.08D);
                    level.playSound(null, BlockPos.containing(ax, ay, az), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.55F);
                }
            }

            // Void (#10) Spatial Destabilization Strike: random teleport bursts every 40 ticks (unless anchored!)
            if (tag.getLong(TAG_VOID_DESTABILIZED_UNTIL).orElse(0L) > now
                    && !SpellCombatReactionEngine.isBlinkAnchored(living, now)
                    && now % 40L == 0L) {
                double rx = living.getX() + (level.random.nextDouble() - 0.5D) * 12.0D;
                double ry = living.getY() + (level.random.nextInt(9) - 4);
                double rz = living.getZ() + (level.random.nextDouble() - 0.5D) * 12.0D;
                living.randomTeleport(rx, ry, rz, true);
                level.sendParticles(ParticleTypes.PORTAL, living.getX(), living.getY(0.7D), living.getZ(),
                        12, 0.3D, 0.4D, 0.3D, 0.1D);
            }

            // Arid (#11) Sandstorm Veil: blow hostile arrows off-target & form sandstone crust over water
            if (tag.getLong(TAG_ARID_VEIL_UNTIL).orElse(0L) > now) {
                tickAridSandstormVeil(level, living, now);
            }

            // Pinned / Sticky No-Jump Clamp (#14 Storm Downburst, #24 Amber Resin & Stasis Hex)
            if (tag.getLong(TAG_NO_JUMP_UNTIL).orElse(0L) > now) {
                Vec3 v = living.getDeltaMovement();
                if (v.y > 0.04D) {
                    living.setDeltaMovement(v.x * 0.5D, 0.0D, v.z * 0.5D);
                    living.hasImpulse = true;
                }
            }

            // Magma (#13) Igneous Armor Mantle: 4m heat aura melting ice/snow & vaporizing shallow water
            if (tag.getLong(TAG_MAGMA_MANTLE_UNTIL).orElse(0L) > now) {
                living.clearFire();
                if (now % 20L == 0L) {
                    tickMagmaIgneousMantle(level, living);
                }
            }

            // Storm (#14) Squall-Rider Shroud: continuous glider flight drift & slow-fall, with 2.0x flight drift speed in rain/thunder!
            if (tag.getLong(TAG_STORM_SHROUD_UNTIL).orElse(0L) > now) {
                boolean stormy = level.isRaining() || level.isThundering();
                if (!living.onGround()) {
                    Vec3 v = living.getDeltaMovement();
                    Vec3 look = living.getLookAngle();
                    double glidePush = stormy ? 0.085D : 0.0425D; // 2.0x glider flight drift speed during rain or thunder!
                    double maxHoriz = stormy ? 1.45D : 0.725D;
                    double nx = v.x + look.x * glidePush;
                    double nz = v.z + look.z * glidePush;
                    double hLen = Math.hypot(nx, nz);
                    if (hLen > maxHoriz) {
                        nx = (nx / hLen) * maxHoriz;
                        nz = (nz / hLen) * maxHoriz;
                    }
                    double ny = v.y < -0.09D ? -0.09D : v.y;
                    living.setDeltaMovement(nx, ny, nz);
                    living.resetFallDistance();
                    living.hasImpulse = true;
                }
                int spdAmp = level.isRaining() ? (level.isThundering() ? 3 : 2) : 0;
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, spdAmp, true, false));
            }

            // Glacial (#15) Permafrost Cryo-Shroud & Taiga (#19) Frost Walker / Cold Immunity
            boolean glacialShroud = tag.getLong(TAG_GLACIAL_SHROUD_UNTIL).orElse(0L) > now;
            boolean taigaFrostWalk = tag.getLong(TAG_TAIGA_FROST_WALKER_UNTIL).orElse(0L) > now;
            if (glacialShroud || taigaFrostWalk) {
                living.setTicksFrozen(0);
                living.isInPowderSnow = false;
                // Total Powder Snow Immunity: prevent sinking into powder snow blocks underfoot!
                BlockPos feetPos = living.blockPosition();
                if (glacialShroud && (level.getBlockState(feetPos).is(Blocks.POWDER_SNOW)
                        || level.getBlockState(feetPos.below()).is(Blocks.POWDER_SNOW))) {
                    Vec3 v = living.getDeltaMovement();
                    if (v.y < 0.0D) {
                        living.setDeltaMovement(v.x, 0.04D, v.z);
                        living.resetFallDistance();
                        living.hasImpulse = true;
                    }
                }
                BlockPos below = feetPos.below();
                for (BlockPos p : BlockPos.betweenClosed(below.offset(-2, 0, -2), below.offset(2, 0, 2))) {
                    if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                        level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
                    }
                }
            }
            if (tag.getLong(TAG_TAIGA_COLD_IMMUNE_UNTIL).orElse(0L) > now) {
                living.setTicksFrozen(0);
                living.isInPowderSnow = false;
            }

            // Glacial (#15) Cryo-Cocoon Mending: 2s (40t) invincible ice shell with rapid regeneration!
            if (tag.getLong(TAG_CRYO_COCOON_UNTIL).orElse(0L) > now) {
                living.clearFire();
                living.setTicksFrozen(0);
                living.setDeltaMovement(Vec3.ZERO);
                living.hasImpulse = true;
                SpellCombatReactionEngine.notifyMagicalHeal(living, 1.5F);
                living.heal(1.5F);
                level.sendParticles(ParticleTypes.SNOWFLAKE, living.getX(), living.getY(0.85D), living.getZ(),
                        8, 0.45D, 0.55D, 0.45D, 0.02D);
            }

            // True Sustained AI Pacification (#20 Oasis Blessing Touch / Greening Mist / Mirage Decoy, #18 Spore Decoy)
            if (tag.getLong(TAG_PACIFIED_UNTIL).orElse(0L) > now) {
                living.setLastHurtByMob(null);
                if (living instanceof Mob mob) {
                    mob.setTarget(null);
                    mob.setAggressive(false);
                }
                if (living instanceof NeutralMob neutral) {
                    neutral.stopBeingAngry();
                }
            }

            // Mob Special-Ability Silencing (#23 Aura Resonance Cascade, Disruption Glyph, Severance Hex & Vacuum Decoy)
            if (tag.getLong("SpellSilencedUntil").orElse(0L) > now && living instanceof Mob silencedMob) {
                if (silencedMob instanceof Creeper creeper) {
                    creeper.setSwellDir(-1);
                } else if (silencedMob instanceof EnderMan || silencedMob instanceof Shulker) {
                    double sx = tag.getDouble(TAG_SILENCED_ANCHOR_X).orElse(silencedMob.getX());
                    double sy = tag.getDouble(TAG_SILENCED_ANCHOR_Y).orElse(silencedMob.getY());
                    double sz = tag.getDouble(TAG_SILENCED_ANCHOR_Z).orElse(silencedMob.getZ());
                    if (silencedMob.position().distanceToSqr(sx, sy, sz) > 16.0D) {
                        silencedMob.teleportTo(sx, sy, sz);
                    } else {
                        tag.putDouble(TAG_SILENCED_ANCHOR_X, silencedMob.getX());
                        tag.putDouble(TAG_SILENCED_ANCHOR_Y, silencedMob.getY());
                        tag.putDouble(TAG_SILENCED_ANCHOR_Z, silencedMob.getZ());
                    }
                } else if (silencedMob instanceof SpellcasterIllager || silencedMob instanceof Witch
                        || silencedMob instanceof Blaze || silencedMob instanceof Ghast) {
                    silencedMob.setAggressive(false);
                } else if (silencedMob instanceof Warden warden) {
                    warden.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, 60L);
                }
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, silencedMob.getX(), silencedMob.getY(0.95D), silencedMob.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.01D);
            }

            // Acoustic Zero-Vibration Stealth (#17 Dust Chalk-Step & #23 Aura Pranic Slipstream)
            if (tag.getLong(TAG_SILENT_VIBRATION_UNTIL).orElse(0L) > now) {
                for (Warden warden : level.getEntitiesOfClass(Warden.class, living.getBoundingBox().inflate(18.0D))) {
                    warden.clearAnger(living);
                    if (warden.getTarget() == living && warden.distanceToSqr(living) > 9.0D) {
                        warden.setTarget(null);
                    }
                }
            }

            // Overgrowth (#16) Earth-Root Bastion: standing still 2s grants +8 Armor & 1 HP/s regen until moving
            if (tag.getLong(TAG_OVERGROWTH_BASTION_UNTIL).orElse(0L) > now) {
                tickOvergrowthEarthRootBastion(level, living, tag, now);
            }

            // Dust (#17) Obscuring Chalk Veil: extinguish fire, deflect arrows, cut mob detection 60%, snuff torches in 3m
            if (tag.getLong(TAG_DUST_VEIL_UNTIL).orElse(0L) > now) {
                tickDustChalkVeil(level, living, now);
            }

            // Spore (#18) Mycelial Spore-Cloud Mantle: Poison/Nausea immunity, +30% speed on Mycelium, mushroom trail
            if (tag.getLong(TAG_SPORE_MANTLE_UNTIL).orElse(0L) > now) {
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.NAUSEA);
                BlockPos below = living.blockPosition().below();
                if (level.getBlockState(below).is(Blocks.MYCELIUM)) {
                    living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 1, true, false));
                }
                if (now % 20L == 0L && level.random.nextFloat() < 0.25F) {
                    BlockPos feet = living.blockPosition();
                    if (level.getBlockState(feet).isAir() && (level.getBlockState(below).is(Blocks.MYCELIUM) || level.getBlockState(below).is(Blocks.PODZOL))) {
                        level.setBlockAndUpdate(feet, level.random.nextBoolean() ? Blocks.RED_MUSHROOM.defaultBlockState() : Blocks.BROWN_MUSHROOM.defaultBlockState());
                    }
                }
            }

            // Taiga (#19) Winter Solstice Shroud: snuff fire & regenerate on snow/ice/cold biomes
            if (tag.getLong(TAG_TAIGA_SHROUD_UNTIL).orElse(0L) > now) {
                living.clearFire();
                living.setTicksFrozen(0);
                if (now % 20L == 0L) {
                    BlockPos feet = living.blockPosition();
                    BlockState under = level.getBlockState(feet.below());
                    boolean coldSurface = under.is(Blocks.SNOW_BLOCK) || under.is(Blocks.ICE) || under.is(Blocks.PACKED_ICE)
                            || under.is(Blocks.BLUE_ICE) || level.getBlockState(feet).is(Blocks.SNOW)
                            || level.getBiome(feet).value().getBaseTemperature() < 0.25F;
                    if (coldSurface) {
                        SpellCombatReactionEngine.notifyMagicalHeal(living, 1.0F);
                        living.heal(1.0F);
                    }
                }
            }

            // Vitae (#21) Cellular Overdrive Shroud: +20% sprint speed, 2x natural regen, -50% exhaustion
            if (tag.getLong(TAG_VITAE_SHROUD_UNTIL).orElse(0L) > now) {
                if (living.isSprinting()) {
                    living.addEffect(new MobEffectInstance(MobEffects.SPEED, 15, 0, true, false));
                }
                if (now % 40L == 0L) {
                    if (living instanceof net.minecraft.world.entity.player.Player player) {
                        if (player.getFoodData().getFoodLevel() >= 18 && player.getHealth() < player.getMaxHealth()) {
                            SpellCombatReactionEngine.notifyMagicalHeal(player, 1.0F);
                            player.heal(1.0F);
                        }
                        if (player.getFoodData().getSaturationLevel() < 5.0F) {
                            player.getFoodData().eat(0, 0.25F);
                        }
                    } else if (living.getHealth() < living.getMaxHealth()) {
                        SpellCombatReactionEngine.notifyMagicalHeal(living, 1.0F);
                        living.heal(1.0F);
                    }
                }
            }

            // Blood (#22) Coagulated Crimson Carapace: slow saturation drain while active
            if (tag.getLong(TAG_BLOOD_CARAPACE_UNTIL).orElse(0L) > now && now % 80L == 0L) {
                if (living instanceof net.minecraft.world.entity.player.Player player && !player.isCreative()) {
                    player.causeFoodExhaustion(2.0F);
                }
            }

            // Blood (#22) Hemorrhage: extra laceration bleed damage whenever moving rapidly!
            if (tag.getLong(TAG_BLOOD_HEMORRHAGE_UNTIL).orElse(0L) > now && now % 12L == 0L) {
                if (living.getDeltaMovement().horizontalDistanceSqr() > 0.006D) {
                    living.hurtServer(level, level.damageSources().magic(), 1.5F);
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, living.getX(), living.getY(0.6D), living.getZ(),
                            2, 0.2D, 0.2D, 0.2D, 0.02D);
                }
            }

            // Aura (#23) Chakra Respiration Shroud: 100% drowning immunity & passive saturation every 4s
            if (tag.getLong(TAG_AURA_SHROUD_UNTIL).orElse(0L) > now) {
                if (living.getAirSupply() < living.getMaxAirSupply()) {
                    living.setAirSupply(living.getMaxAirSupply());
                }
                if (now % 80L == 0L && living instanceof net.minecraft.world.entity.player.Player player) {
                    player.getFoodData().eat(1, 0.5F);
                }
            }

            // Amber (#24) & Aegis (#34) Bastion Mantles: prevent/mend armor durability wear while active
            if ((tag.getLong(TAG_AMBER_MANTLE_UNTIL).orElse(0L) > now
                    || tag.getLong(TAG_AEGIS_MANTLE_UNTIL).orElse(0L) > now) && now % 20L == 0L) {
                for (net.minecraft.world.entity.EquipmentSlot eq : new net.minecraft.world.entity.EquipmentSlot[]{
                        net.minecraft.world.entity.EquipmentSlot.HEAD,
                        net.minecraft.world.entity.EquipmentSlot.CHEST,
                        net.minecraft.world.entity.EquipmentSlot.LEGS,
                        net.minecraft.world.entity.EquipmentSlot.FEET}) {
                    net.minecraft.world.item.ItemStack piece = living.getItemBySlot(eq);
                    if (!piece.isEmpty() && piece.isDamaged()) {
                        piece.setDamageValue(Math.max(0, piece.getDamageValue() - 1));
                    }
                }
            }

            // Eclipse (#25) Unquenchable Coronal Burn & Solar Corona Bastion
            if (tag.getLong(TAG_ECLIPSE_CORONA_BURN_UNTIL).orElse(0L) > now) {
                living.igniteForSeconds(2);
                if (now % 16L == 0L) {
                    level.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY(0.75D), living.getZ(), 2, 0.2D, 0.25D, 0.2D, 0.02D);
                    level.sendParticles(ParticleTypes.SMOKE, living.getX(), living.getY(0.75D), living.getZ(), 3, 0.2D, 0.25D, 0.2D, 0.01D);
                }
            }
            if (tag.getLong(TAG_ECLIPSE_BASTION_UNTIL).orElse(0L) > now) {
                living.clearFire();
                living.removeEffect(MobEffects.BLINDNESS);
                living.removeEffect(MobEffects.DARKNESS);
                living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                if (now % 16L == 0L) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(3.0D))) {
                        if (foe == living || !foe.isAlive() || !(foe instanceof Monster)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, false));
                    }
                }
            }

            // Blight (#26) Cadaverous Briar-Carapace: Poison/Wither/Hunger immunity, undead neutrality unless provoked, & crop/flower wilting underfoot
            if (tag.getLong(TAG_BLIGHT_CARAPACE_UNTIL).orElse(0L) > now) {
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.removeEffect(MobEffects.HUNGER);
                if (now % 16L == 0L) {
                    for (Mob undead : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D),
                            m -> m.isAlive() && m.getType().is(EntityTypeTags.UNDEAD))) {
                        if (undead.getTarget() == living && undead.getLastHurtByMob() != living) {
                            undead.setTarget(null);
                            undead.setAggressive(false);
                        }
                    }
                }
                if (now % 20L == 0L) {
                    BlockPos feet = living.blockPosition();
                    BlockState atFeet = level.getBlockState(feet);
                    if ((atFeet.is(BlockTags.SMALL_FLOWERS) || atFeet.is(Blocks.SHORT_GRASS) || atFeet.is(Blocks.FERN))
                            && !atFeet.is(Blocks.WITHER_ROSE)) {
                        level.setBlockAndUpdate(feet, Blocks.DEAD_BUSH.defaultBlockState());
                    }
                }
            }

            // Astral (#27) Cosmic Starlight Shroud: high-float jump, zero-g glide, +30% sprint speed, & ambient redstone pulse
            if (tag.getLong(TAG_ASTRAL_SHROUD_UNTIL).orElse(0L) > now) {
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 1, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 25, 1, true, false));
                if (!living.onGround() && living.getDeltaMovement().y < -0.075D) {
                    Vec3 v = living.getDeltaMovement();
                    living.setDeltaMovement(v.x, -0.075D, v.z);
                    living.resetFallDistance();
                    living.hasImpulse = true;
                }
                if (now % 20L == 0L) {
                    pulseTemporaryRedstonePower(level, living.blockPosition(), 3, 25);
                    level.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY(0.9D), living.getZ(), 3, 0.35D, 0.45D, 0.35D, 0.02D);
                }
            }

            // Soulfire (#28) Unquenchable Soul-Burn & Phantasmal Soulfire Shroud
            if (tag.getLong(TAG_SOULFIRE_BURN_UNTIL).orElse(0L) > now) {
                living.igniteForSeconds(2);
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 0, true, false));
                if (now % 12L == 0L) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, living.getX(), living.getY(0.7D), living.getZ(), 3, 0.2D, 0.25D, 0.2D, 0.02D);
                }
            }
            if (tag.getLong(TAG_SOULFIRE_SHROUD_UNTIL).orElse(0L) > now) {
                living.clearFire();
                if (living.isInLava()) {
                    Vec3 v = living.getDeltaMovement();
                    living.setDeltaMovement(v.x * 1.15D, Math.max(v.y, 0.08D), v.z * 1.15D);
                    living.hasImpulse = true;
                }
                BlockPos feetPos = living.blockPosition();
                BlockPos below = feetPos.below();
                if (level.getBlockState(feetPos).is(Blocks.SOUL_SAND) || level.getBlockState(feetPos).is(Blocks.SOUL_SOIL)
                        || level.getBlockState(below).is(Blocks.SOUL_SAND) || level.getBlockState(below).is(Blocks.SOUL_SOIL)) {
                    living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 1, true, false));
                }
                if (now % 16L == 0L) {
                    for (Mob netherMob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D),
                            m -> m.isAlive() && (m instanceof AbstractPiglin || m instanceof Blaze
                                    || m instanceof WitherSkeleton || m instanceof ZombifiedPiglin || m instanceof MagmaCube))) {
                        if (netherMob.getTarget() == living && netherMob.getLastHurtByMob() != living) {
                            netherMob.setTarget(null);
                            netherMob.setAggressive(false);
                        }
                    }
                }
                if (now % 20L == 0L) {
                    BlockPos c = living.blockPosition();
                    for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 1, 3))) {
                        BlockState bs = level.getBlockState(p);
                        if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE)) {
                            level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                        } else if (bs.is(Blocks.SNOW)) {
                            level.removeBlock(p, false);
                        }
                    }
                }
            }

            // Vapor (#29) Mist-Walker Shroud: rain/water invisibility, +30% swim/sprint speed, fire dousing, 40% arrow deflection, 50% aggro range cut
            if (tag.getLong(TAG_VAPOR_MIST_SHROUD_UNTIL).orElse(0L) > now) {
                living.clearFire();
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 0, true, false));
                if (living.isInWaterOrRain()) {
                    living.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false));
                    living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 30, 0, true, false));
                }
                level.sendParticles(ParticleTypes.CLOUD, living.getX(), living.getY(0.65D), living.getZ(), 3, 0.4D, 0.4D, 0.4D, 0.01D);
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.6D))) {
                    if (proj.getOwner() != living && proj instanceof AbstractArrow && level.random.nextFloat() < 0.40F) {
                        Vec3 d = proj.getDeltaMovement().add((level.random.nextDouble() - 0.5D) * 0.65D, 0.22D, (level.random.nextDouble() - 0.5D) * 0.65D);
                        proj.setDeltaMovement(d);
                        proj.hasImpulse = true;
                    }
                }
                if (now % 16L == 0L) {
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D))) {
                        if (mob.getTarget() == living && mob.distanceToSqr(living) > 49.0D) {
                            mob.setTarget(null);
                        }
                    }
                }
            }

            // Null_R (#30) Singularity Orbit & Event Horizon Bastion: disintegrate projectiles, high-float zero-g, Blind/Darkness immunity, & Level-15 light!
            boolean nullROrbit = tag.getLong(TAG_NULL_R_ORBIT_UNTIL).orElse(0L) > now;
            boolean nullRBastion = tag.getLong(TAG_NULL_R_BASTION_UNTIL).orElse(0L) > now;
            if (nullROrbit || nullRBastion) {
                double pRad = nullRBastion ? 3.5D : 2.5D;
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(pRad))) {
                    if (proj.getOwner() != living && (!nullRBastion || nullROrbit || level.random.nextFloat() < 0.50F)) {
                        level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 8, 0.15D, 0.15D, 0.15D, 0.05D);
                        proj.discard();
                    }
                }
                if (nullRBastion) {
                    living.removeEffect(MobEffects.BLINDNESS);
                    living.removeEffect(MobEffects.DARKNESS);
                    living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 25, 1, true, false));
                    living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 25, 0, true, false));
                    living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                    if (now % 16L == 0L) {
                        pulseTemporaryLight(level, living.blockPosition().above(), 15, 22);
                    }
                }
            }

            // Null_U (#31) Abyssal Singularity Orbit & Zero-Acoustic Veil
            if (tag.getLong(TAG_NULL_U_ORBIT_UNTIL).orElse(0L) > now) {
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.5D))) {
                    if (proj.getOwner() != living) {
                        level.sendParticles(ParticleTypes.SMOKE, proj.getX(), proj.getY(), proj.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.02D);
                        proj.discard();
                    }
                }
            }
            if (tag.getLong(TAG_NULL_U_VEIL_UNTIL).orElse(0L) > now) {
                living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 25, 1, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 25, 0, true, false));
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.8D))) {
                    if (proj.getOwner() != living && proj instanceof AbstractArrow && level.random.nextFloat() < 0.50F) {
                        level.sendParticles(ParticleTypes.SMOKE, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
                        proj.discard();
                    }
                }
                for (Warden warden : level.getEntitiesOfClass(Warden.class, living.getBoundingBox().inflate(20.0D))) {
                    warden.clearAnger(living);
                    if (warden.getTarget() == living && warden.distanceToSqr(living) > 9.0D) {
                        warden.setTarget(null);
                    }
                }
                if (now % 16L == 0L) {
                    BlockPos c = living.blockPosition();
                    for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 2, 3))) {
                        BlockState bs = level.getBlockState(p);
                        if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH) || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                            level.destroyBlock(p, true);
                        } else if (bs.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                            level.removeBlock(p, false);
                        }
                    }
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(18.0D))) {
                        if (mob.getTarget() == living && mob.distanceToSqr(living) > 16.0D) {
                            mob.setTarget(null); // 75% detection range cut!
                        }
                    }
                }
            }

            // Dawn (#32) Convective Horizon Orbit & Dawn-Bringer Solar Shroud
            if (tag.getLong(TAG_DAWN_ORBIT_UNTIL).orElse(0L) > now) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 25, 0, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 0, true, false));
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.5D))) {
                    if (proj.getOwner() != living && proj instanceof AbstractArrow) {
                        proj.setDeltaMovement(proj.getDeltaMovement().scale(-0.7D).add(0.0D, 0.35D, 0.0D));
                        proj.hasImpulse = true;
                    }
                }
            }
            if (tag.getLong(TAG_DAWN_SHROUD_UNTIL).orElse(0L) > now) {
                living.removeEffect(MobEffects.BLINDNESS);
                living.removeEffect(MobEffects.DARKNESS);
                living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 25, 1, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 25, 0, true, false));
                if (living.isSprinting()) {
                    living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 1, true, false));
                }
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.8D))) {
                    if (proj.getOwner() != living && proj instanceof AbstractArrow && level.random.nextFloat() < 0.40F) {
                        Vec3 v = proj.getDeltaMovement();
                        proj.setDeltaMovement(v.x * 0.25D, 0.85D, v.z * 0.25D);
                        proj.hasImpulse = true;
                    }
                }
                if (now % 40L == 0L && level.isBrightOutside() && level.canSeeSky(living.blockPosition())) {
                    SpellCombatReactionEngine.notifyMagicalHeal(living, 1.0F);
                    living.heal(1.0F);
                    level.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY(1.0D), living.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
                }
            }

            // Abyss (#33) Maelstrom Orbit, Hadopelagic Trench Shroud, & Dry-Land Drowning
            if (tag.getLong(TAG_ABYSS_ORBIT_UNTIL).orElse(0L) > now) {
                living.clearFire();
            }
            if (tag.getLong(TAG_ABYSS_SHROUD_UNTIL).orElse(0L) > now) {
                living.clearFire();
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 30, 1, true, false));
                for (Warden warden : level.getEntitiesOfClass(Warden.class, living.getBoundingBox().inflate(18.0D))) {
                    warden.clearAnger(living);
                    if (warden.getTarget() == living && warden.distanceToSqr(living) > 9.0D) {
                        warden.setTarget(null);
                    }
                }
                if (living.isInWater()) {
                    living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                    Vec3 v = living.getDeltaMovement();
                    if (v.horizontalDistanceSqr() > 0.002D && v.horizontalDistanceSqr() < 0.85D) {
                        living.setDeltaMovement(v.x * 1.18D, v.y, v.z * 1.18D);
                        living.hasImpulse = true;
                    }
                }
                if (now % 16L == 0L && (living.isInWater() || level.getMaxLocalRawBrightness(living.blockPosition()) <= 7)) {
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D))) {
                        if (mob.getTarget() == living && mob.distanceToSqr(living) > 49.0D) {
                            mob.setTarget(null);
                        }
                    }
                }
            }
            if (tag.getLong(TAG_ABYSS_LAND_DROWNING_UNTIL).orElse(0L) > now) {
                int nextAir = Math.max(-20, living.getAirSupply() - 24);
                living.setAirSupply(nextAir);
                if (nextAir <= -20 && now % 20L == 0L) {
                    living.setAirSupply(0);
                    living.hurtServer(level, level.damageSources().drown(), 2.0F);
                    level.sendParticles(ParticleTypes.BUBBLE_POP, living.getX(), living.getY(0.85D), living.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.03D);
                }
            }

            // Aegis (#34) Gilded Bastion Orbit: +4 Armor & 50% projectile interception
            if (tag.getLong(TAG_AEGIS_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 4.0D, 0.0D, 0.0D, 0.0D, 15L);
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.5D))) {
                    if (proj.getOwner() != living && level.random.nextFloat() < 0.50F) {
                        level.sendParticles(ParticleTypes.CRIT, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.04D);
                        proj.discard();
                    }
                }
            }

            // Aurora (#35) Glacial Aurora Spiral Orbit & Borealis Prismatic Veil Mantle
            if (tag.getLong(TAG_AURORA_ORBIT_UNTIL).orElse(0L) > now) {
                living.clearFire();
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, living.getBoundingBox().inflate(5.5D))) {
                    Vec3 pull = living.position().add(0.0D, 0.5D, 0.0D).subtract(item.position());
                    if (pull.lengthSqr() > 0.25D) {
                        item.setDeltaMovement(item.getDeltaMovement().scale(0.6D).add(pull.normalize().scale(0.22D)));
                        item.hasImpulse = true;
                    }
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(3.2D))) {
                    if (foe != living && foe.isAlive() && foe instanceof Monster) {
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1, true, false));
                        foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 40, foe.getTicksFrozen() + 8));
                    }
                }
            }
            if (tag.getLong(TAG_AURORA_MANTLE_UNTIL).orElse(0L) > now) {
                living.setTicksFrozen(0);
                living.isInPowderSnow = false;
                living.removeEffect(MobEffects.BLINDNESS);
                living.removeEffect(MobEffects.DARKNESS);
                living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                if (now % 16L == 0L) {
                    pulseTemporaryLight(level, living.blockPosition().above(), 12, 22);
                }
                BlockPos feetPos = living.blockPosition();
                if (level.getBlockState(feetPos).is(Blocks.POWDER_SNOW) || level.getBlockState(feetPos.below()).is(Blocks.POWDER_SNOW)) {
                    Vec3 v = living.getDeltaMovement();
                    if (v.y < 0.0D) {
                        living.setDeltaMovement(v.x, 0.04D, v.z);
                        living.resetFallDistance();
                        living.hasImpulse = true;
                    }
                }
                BlockPos below = feetPos.below();
                for (BlockPos p : BlockPos.betweenClosed(below.offset(-2, 0, -2), below.offset(2, 0, 2))) {
                    if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                        level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
                    }
                }
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.6D))) {
                    if (proj.getOwner() != living && proj instanceof AbstractArrow && level.random.nextFloat() < 0.40F) {
                        proj.setDeltaMovement(proj.getDeltaMovement().scale(-0.85D).add(0.0D, 0.2D, 0.0D));
                        proj.hasImpulse = true;
                    }
                }
            }

            // Mirage (#36) Mirror Mirage Spiral Orbit, Glamour Shroud, & Traitor Paranoia Frenzy
            if (tag.getLong(TAG_MIRAGE_ORBIT_UNTIL).orElse(0L) > now) {
                if (now % 16L == 0L) {
                    pulseTemporaryLight(level, living.blockPosition().above(), 12, 22);
                }
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.8D))) {
                    if (proj.getOwner() != living && level.random.nextFloat() < 0.25F) {
                        Vec3 back = proj.getOwner() != null
                                ? proj.getOwner().getEyePosition().subtract(proj.position()).normalize().scale(Math.max(1.1D, proj.getDeltaMovement().length()))
                                : proj.getDeltaMovement().scale(-1.0D);
                        proj.setDeltaMovement(back);
                        proj.setOwner(living);
                        proj.hasImpulse = true;
                        level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.04D);
                    }
                }
            }
            if (tag.getLong(TAG_MIRAGE_SHROUD_UNTIL).orElse(0L) > now) {
                BlockPos below = living.blockPosition().below();
                if (level.getBlockState(below).is(Blocks.WATER)) {
                    level.setBlockAndUpdate(below, Blocks.SANDSTONE.defaultBlockState());
                }
                if (now % 80L == 0L) {
                    double angle = level.random.nextDouble() * Math.PI * 2.0D;
                    Vec3 spawnPos = living.position().add(Math.cos(angle) * 2.5D, 0.0D, Math.sin(angle) * 2.5D);
                    Vec3 wander = new Vec3(-Math.sin(angle) * 0.18D, 0.0D, Math.cos(angle) * 0.18D);
                    SpellEidolonEntity.spawnShortLivedDecoy(level, living, spawnPos, EssenceType.MIRAGE, 6.0F, 55, "Phantom Double", wander);
                }
                if (now % 16L == 0L) {
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D))) {
                        if (mob.getTarget() == living && mob.distanceToSqr(living) > 49.0D) {
                            mob.setTarget(null);
                        }
                    }
                }
            }
            if (tag.getLong(TAG_MIRAGE_PARANOIA_UNTIL).orElse(0L) > now) {
                String pCasterUuid = tag.getString(TAG_MIRAGE_PARANOIA_CASTER_UUID).orElse("");
                if (living instanceof Mob pMob && pMob.getTarget() != null
                        && pMob.getTarget().getUUID().toString().equals(pCasterUuid)) {
                    pMob.setTarget(null);
                    pMob.setAggressive(false);
                }
                if (now % 12L == 0L) {
                    living.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
                if (now % 20L == 0L) {
                    living.hurtServer(level, level.damageSources().magic(), 1.8F);
                    level.sendParticles(ParticleTypes.ENCHANT, living.getX(), living.getY(0.95D), living.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.05D);
                }
            }
            if (tag.getLong(TAG_MIRAGE_TRAITOR_UNTIL).orElse(0L) > now && living instanceof Mob traitorMob) {
                if (now % 12L == 0L) {
                    traitorMob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
                Monster bestVictim = null;
                double bestDist = Double.MAX_VALUE;
                for (Monster peer : level.getEntitiesOfClass(Monster.class, traitorMob.getBoundingBox().inflate(12.0D))) {
                    if (peer == traitorMob || !peer.isAlive()) continue;
                    double d = traitorMob.distanceToSqr(peer);
                    if (d < bestDist) {
                        bestDist = d;
                        bestVictim = peer;
                    }
                }
                if (bestVictim != null) {
                    traitorMob.setTarget(bestVictim);
                    traitorMob.setLastHurtByMob(bestVictim);
                    traitorMob.setAggressive(true);
                    traitorMob.getNavigation().moveTo(bestVictim, 1.25D);
                    if (bestDist <= 6.5D && now % 20L == 0L) {
                        traitorMob.doHurtTarget(level, bestVictim);
                        level.sendParticles(ParticleTypes.CRIT, bestVictim.getX(), bestVictim.getY(0.7D), bestVictim.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                } else if (traitorMob.getTarget() instanceof net.minecraft.world.entity.player.Player) {
                    traitorMob.setTarget(null);
                }
            }

            // Shared AI Pathfinding Disruption (25.0s / 500t Trinity Cones + Penumbra Contrast-Collapse Hex)
            if (tag.getLong(TAG_AI_PATH_DISRUPTED_UNTIL).orElse(0L) > now && living instanceof Mob disruptedMob) {
                if (now % 12L == 0L) {
                    disruptedMob.getNavigation().stop();
                    double ox = disruptedMob.getX() + (level.random.nextDouble() - 0.5D) * 5.5D;
                    double oz = disruptedMob.getZ() + (level.random.nextDouble() - 0.5D) * 5.5D;
                    disruptedMob.getNavigation().moveTo(ox, disruptedMob.getY(), oz, 0.85D);
                }
            }

            // #37 Wraith Specter Ectoplasm Armor & Incorporeal Soul-Shield Orbit
            if (tag.getLong(TAG_WRAITH_SHROUD_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 8.0D, 0.0D, 1.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.WITHER);
                tag.putLong(TAG_SILENT_VIBRATION_UNTIL, Math.max(now + 12L, tag.getLong(TAG_SILENT_VIBRATION_UNTIL).orElse(0L)));
                if (now % 12L == 0L) {
                    for (Warden warden : level.getEntitiesOfClass(Warden.class, living.getBoundingBox().inflate(18.0D))) {
                        warden.clearAnger(living);
                        if (warden.getTarget() == living) warden.setTarget(null);
                    }
                }
            } else if (tag.getLong(TAG_WRAITH_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 4.0D, 0.0D, 0.0D, 0.0D, 12L);
            }

            // #38 Barrow Catacomb Miasma Shroud (-40% mob detection, 4m Wither+Slowness, Poison/Wither immune, Night Vision)
            if (tag.getLong(TAG_BARROW_SHROUD_UNTIL).orElse(0L) > now) {
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false));
                if (now % 16L == 0L) {
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D))) {
                        if (mob.getTarget() == living && mob.distanceToSqr(living) > 58.0D) {
                            mob.setTarget(null);
                        }
                    }
                }
                if (now % 20L == 0L) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(4.0D))) {
                        if (foe == living || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                        if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 0));
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 0));
                        }
                    }
                }
            }

            // #39 Pyre Hearthstone Shroud (+8 Armor, +2 Toughness, -10% speed, fire/lava/freeze immune, melt ice/snow) & Orbit
            if (tag.getLong(TAG_PYRE_SHROUD_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 8.0D, 2.0D, 0.0D, -0.10D, 12L);
                living.clearFire();
                living.setTicksFrozen(0);
                if (now % 16L == 0L) {
                    BlockPos c = living.blockPosition();
                    for (BlockPos p : BlockPos.betweenClosed(c.offset(-2, -1, -2), c.offset(2, 1, 2))) {
                        BlockState bs = level.getBlockState(p);
                        if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE)) {
                            level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                        } else if (bs.is(Blocks.SNOW) || bs.is(Blocks.POWDER_SNOW)) {
                            level.removeBlock(p, false);
                        }
                    }
                }
            } else if (tag.getLong(TAG_PYRE_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 3.0D, 0.0D, 0.0D, 0.0D, 12L);
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(2.8D))) {
                    if (arrow.getOwner() == living) continue;
                    level.sendParticles(ParticleTypes.ASH, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.02D);
                    arrow.discard();
                }
            }

            // #40 Penumbra Corona Mantle & Binary Orbit Flow
            if (tag.getLong(TAG_PENUMBRA_MANTLE_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 6.0D, 2.0D, 0.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.BLINDNESS);
                living.removeEffect(MobEffects.DARKNESS);
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.8D))) {
                    if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                    if (level.random.nextFloat() < 0.25F) {
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL, proj.getX(), proj.getY(), proj.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.04D);
                        proj.discard();
                    }
                }
            } else if (tag.getLong(TAG_PENUMBRA_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 5.0D, 2.0D, 0.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.BLINDNESS);
                living.removeEffect(MobEffects.DARKNESS);
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(2.8D))) {
                    if (arrow.getOwner() == living) continue;
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.03D);
                    arrow.discard();
                }
            }

            // #41 Rime Hoarfrost Glacial Aegis Mantle & Crystalline Hoarfrost Spiral Flow
            if (tag.getLong(TAG_RIME_MANTLE_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 7.0D, 3.0D, 0.0D, 0.0D, 12L);
                living.clearFire();
                living.setTicksFrozen(0);
                living.removeEffect(MobEffects.SLOWNESS);
                BlockPos below = living.blockPosition().below();
                for (BlockPos p : BlockPos.betweenClosed(below.offset(-1, 0, -1), below.offset(1, 0, 1))) {
                    if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                        level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            } else if (tag.getLong(TAG_RIME_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 5.0D, 2.0D, 0.0D, 0.0D, 12L);
                living.setTicksFrozen(0);
                living.removeEffect(MobEffects.SLOWNESS);
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(2.8D))) {
                    if (arrow.getOwner() == living) continue;
                    level.sendParticles(ParticleTypes.SNOWFLAKE, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.03D);
                    arrow.discard();
                }
            }

            // #42 Spring Vernal Fountainhead Mantle, Blossom-Aquifer Spiral Flow & Petal-Warp Undead Stealth
            if (tag.getLong(TAG_SPRING_MANTLE_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 6.0D, 2.0D, 0.0D, 0.0D, 12L);
                living.clearFire();
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, true, false));
                if (now % 60L == 0L) {
                    SpellCombatReactionEngine.notifyMagicalHeal(living, 2.0F);
                    living.heal(2.0F);
                }
                if (now % 80L == 0L) {
                    for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(5.0D))) {
                        if (ally == living || !ally.isAlive() || ally instanceof Monster) continue;
                        if (ally.isAlliedTo(living)
                                || ally instanceof net.minecraft.world.entity.player.Player
                                || (ally instanceof SpellEidolonEntity eid && eid.isFriendlyTo(living))) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, 2.0F);
                            ally.heal(2.0F);
                            for (MobEffectInstance eff : new ArrayList<>(ally.getActiveEffects())) {
                                if (!eff.getEffect().value().isBeneficial()) ally.removeEffect(eff.getEffect());
                            }
                        }
                    }
                }
            } else if (tag.getLong(TAG_SPRING_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 5.0D, 2.0D, 0.0D, 0.0D, 12L);
                living.clearFire();
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.8D))) {
                    if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                    level.sendParticles(ParticleTypes.CHERRY_LEAVES, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
                    proj.discard();
                    SpellCombatReactionEngine.notifyMagicalHeal(living, 1.0F);
                    living.heal(1.0F);
                }
            }
            if (tag.getLong(TAG_SPRING_UNDEAD_STEALTH_UNTIL).orElse(0L) > now) {
                for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(18.0D))) {
                    if (mob.getType().is(EntityTypeTags.UNDEAD) && mob.getTarget() == living) {
                        mob.setTarget(null);
                        mob.setAggressive(false);
                    }
                }
            }

            // #43 Static Dielectric Faraday Shroud, Galvanic Tesla Orbit & Sprint-Shock Ionized Field
            if (tag.getLong(TAG_STATIC_SHROUD_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 7.0D, 3.0D, 0.0D, 0.25D, 12L);
                SpellCombatReactionEngine.applyTimedAttackSpeedBuff(living, level, 0.25D, 12L);
                living.removeEffect(MobEffects.SLOWNESS);
                living.resetFallDistance();
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(3.0D))) {
                    if (arrow.getOwner() == living) continue;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.06D);
                    arrow.discard();
                }
            } else if (tag.getLong(TAG_STATIC_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 5.0D, 2.0D, 0.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.SLOWNESS);
                living.resetFallDistance();
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(2.8D))) {
                    if (arrow.getOwner() == living) continue;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.05D);
                    arrow.discard();
                }
            } else if (tag.getLong(TAG_STATIC_SENTRY_IMMUNE_UNTIL).orElse(0L) > now) {
                living.resetFallDistance();
            }
            if (tag.getLong(TAG_STATIC_SPRINT_SHOCK_UNTIL).orElse(0L) > now
                    && (living.isSprinting() || living.getDeltaMovement().horizontalDistanceSqr() > 0.035D)
                    && now % 12L == 0L) {
                living.hurtServer(level, level.damageSources().lightningBolt(), 2.0F);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, living.getX(), living.getY(0.5D), living.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.06D);
            }

            // #44 Sylvan Primeval Photosynthetic Aegis & Sun-Canopy Orbit Flow
            if (tag.getLong(TAG_SYLVAN_AEGIS_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 7.0D, 3.0D, 0.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.removeEffect(MobEffects.BLINDNESS);
            } else if (tag.getLong(TAG_SYLVAN_ORBIT_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 5.0D, 2.0D, 0.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                if (now % 20L == 0L
                        && living instanceof net.minecraft.world.entity.player.Player sylvanPlayer
                        && level.isBrightOutside()
                        && level.canSeeSky(living.blockPosition())) {
                    ddraig.net.entropica.spell.fuel.SpellFuelService.restorePlayerMateriaFuel(sylvanPlayer, EssenceType.SYLVAN, 32L);
                }
            }

            // #45 Miasma Putrescent Mire-Carapace & Septic Immunity
            if (tag.getLong(TAG_MIASMA_CARAPACE_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 7.0D, 3.0D, 0.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.removeEffect(MobEffects.HUNGER);
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
                BlockPos below = living.blockPosition().below();
                BlockState bState = level.getBlockState(below);
                if ((bState.is(Blocks.WATER) || bState.is(Blocks.MUD)) && level.getBlockState(below.above()).isAir()) {
                    BlockPos savedPos = below.immutable();
                    BlockState orig = bState;
                    level.setBlockAndUpdate(savedPos, Blocks.MOSS_BLOCK.defaultBlockState());
                    registerZone(level, new ActiveSpellZone(
                            living, Vec3.atCenterOf(savedPos), 1.0D, 100, now, null, null, EssenceType.MIASMA, 1.0F,
                            (srv, z, el) -> {
                                if (el >= z.maxDurationTicks) {
                                    if (srv.getBlockState(savedPos).is(Blocks.MOSS_BLOCK)) srv.setBlockAndUpdate(savedPos, orig);
                                    return false;
                                }
                                return true;
                            }
                    ));
                }
            }
            if (tag.getLong(TAG_MIASMA_SEPTIC_IMMUNE_UNTIL).orElse(0L) > now) {
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.removeEffect(MobEffects.HUNGER);
            }

            // #46 Genesis Amniotic Genesis Shroud & Dawn-Tide Lotus Orbit Flow
            if (tag.getLong(TAG_GENESIS_SHROUD_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 7.0D, 3.0D, 0.0D, 0.0D, 12L);
                living.removeEffect(MobEffects.BLINDNESS);
                living.removeEffect(MobEffects.DARKNESS);
                living.removeEffect(MobEffects.WITHER);
                living.removeEffect(MobEffects.POISON);
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 1, true, false));
            }
            if (tag.getLong(TAG_GENESIS_ORBIT_UNTIL).orElse(0L) > now) {
                for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(2.8D))) {
                    if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                    level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
                    proj.discard();
                }
                if (now % 20L == 0L) {
                    for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(5.0D))) {
                        if (!ally.isAlive() || ally instanceof Monster) continue;
                        if (ally == living || ally.isAlliedTo(living)
                                || ally instanceof net.minecraft.world.entity.player.Player
                                || (ally instanceof SpellEidolonEntity eid && eid.isFriendlyTo(living))) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, 2.0F);
                            ally.heal(2.0F);
                            ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 35, 0, true, false));
                        }
                    }
                }
            }

            // #47 Oblivion Void-Shroud & Obsidian Extinction Armor
            if (tag.getLong(TAG_OBLIVION_SHROUD_UNTIL).orElse(0L) > now) {
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(3.0D))) {
                    if (arrow.getOwner() == living) continue;
                    level.sendParticles(ParticleTypes.ASH, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.03D);
                    arrow.discard();
                }
                if (now % 16L == 0L) {
                    BlockPos c = living.blockPosition();
                    for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 2, 3))) {
                        BlockState bs = level.getBlockState(p);
                        if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH) || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                            level.destroyBlock(p, true);
                        }
                    }
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D))) {
                        if (mob.getTarget() == living && mob.distanceToSqr(living) > 25.0D) {
                            mob.setTarget(null);
                        }
                    }
                }
                if (now % 20L == 0L) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(3.5D))) {
                        if (foe == living || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                        if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                            foe.hurtServer(level, level.damageSources().wither(), 2.0F);
                        }
                    }
                }
            }
            if (tag.getLong(TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL).orElse(0L) > now) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 6.0D, 3.0D, 0.0D, 0.0D, 12L);
                living.clearFire();
                living.removeEffect(MobEffects.WITHER);
            }

            // Sec 6.49 Chimera Unstable Hybrid Flux Shroud & Quad-Elemental Orbit
            if (tag.getLong(TAG_CHIMERA_SHROUD_UNTIL).orElse(0L) > now) {
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(3.0D))) {
                    if (arrow.getOwner() == living) continue;
                    level.sendParticles(ParticleTypes.ENCHANTED_HIT, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.06D);
                    arrow.discard();
                }
                if (now % 16L == 0L) {
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(16.0D))) {
                        if (mob.getTarget() == living && mob.distanceToSqr(living) > 25.0D) {
                            mob.setTarget(null);
                        }
                    }
                }
                if (now % 20L == 0L) {
                    for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(3.5D))) {
                        if (foe == living || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                        if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                            foe.hurtServer(level, level.damageSources().magic(), 2.0F);
                        }
                    }
                }
            }
            if (tag.getLong(TAG_CHIMERA_ORBIT_UNTIL).orElse(0L) > now) {
                for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(2.8D))) {
                    if (arrow.getOwner() == living) continue;
                    Entity shooter = arrow.getOwner();
                    arrow.discard();
                    if (shooter instanceof LivingEntity foeShooter && foeShooter.isAlive()) {
                        String rolled = tag.getString(TAG_CHIMERA_ORBIT_ELEMENT).orElse("NETHER");
                        switch (rolled) {
                            case "AIR" -> {
                                Vec3 away = foeShooter.position().subtract(living.position()).normalize().scale(1.25D);
                                foeShooter.setDeltaMovement(away.x, 0.42D, away.z);
                                foeShooter.hasImpulse = true;
                            }
                            case "EARTH" -> {
                                foeShooter.hurtServer(level, level.damageSources().magic(), 3.0F);
                                SpellCombatReactionEngine.applyTimedAttributeShred(foeShooter, level, 2.0D, 0.0D, 80L);
                            }
                            case "WATER" -> {
                                applySoaked(foeShooter, 120);
                                foeShooter.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                            }
                            default -> foeShooter.igniteForSeconds(4);
                        }
                    }
                }
            }

            // Arcane 3D Flight, Materia Wings & Void Immunity Lifecycle
            long flightUntil = tag.getLong(TAG_ARCANE_3D_FLIGHT_UNTIL).orElse(0L);
            if (flightUntil > now) {
                living.resetFallDistance();
                if (living instanceof net.minecraft.server.level.ServerPlayer sp && !sp.getAbilities().mayfly) {
                    sp.getAbilities().mayfly = true;
                    tag.putBoolean(TAG_GRANTED_MAYFLY, true);
                    sp.onUpdateAbilities();
                }
                int pOrd = tag.getInt(TAG_ARCANE_WING_PRIMARY).orElse(EssenceType.AIR.ordinal());
                int sOrd = tag.getInt(TAG_ARCANE_WING_SECONDARY).orElse(-1);
                int wTier = tag.getInt(TAG_ARCANE_WING_TIER).orElse(3);
                EssenceType wPrimary = ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(pOrd);
                EssenceType wSecondary = sOrd >= 0 ? ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(sOrd) : null;

                if (now % 20L == 0L) {
                    ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.broadcastWings(
                            level, living, (int) Math.min(Integer.MAX_VALUE, flightUntil - now), wPrimary, wSecondary, wTier
                    );
                }
                if (now % 8L == 0L && !living.onGround()) {
                    Vec3 backPos = living.position()
                            .add(0.0D, living.getBbHeight() * 0.68D, 0.0D)
                            .subtract(living.getLookAngle().multiply(0.32D, 0.0D, 0.32D));
                    ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                            level, backPos, wPrimary, wSecondary, 0.42D, Math.max(1, wTier / 2)
                    );
                }
            } else if (tag.getBoolean(TAG_GRANTED_MAYFLY).orElse(false) || tag.getBoolean(TAG_ARCANE_WINGS_ACTIVE).orElse(false)) {
                boolean revokedMayfly = tag.getBoolean(TAG_GRANTED_MAYFLY).orElse(false);
                tag.remove(TAG_GRANTED_MAYFLY);
                tag.remove(TAG_ARCANE_WINGS_ACTIVE);
                int pOrd = tag.getInt(TAG_ARCANE_WING_PRIMARY).orElse(EssenceType.AIR.ordinal());
                int sOrd = tag.getInt(TAG_ARCANE_WING_SECONDARY).orElse(-1);
                int wTier = tag.getInt(TAG_ARCANE_WING_TIER).orElse(3);
                tag.remove(TAG_ARCANE_WING_PRIMARY);
                tag.remove(TAG_ARCANE_WING_SECONDARY);
                tag.remove(TAG_ARCANE_WING_TIER);

                if (revokedMayfly && living instanceof net.minecraft.server.level.ServerPlayer sp && !sp.isCreative() && !sp.isSpectator()) {
                    sp.getAbilities().mayfly = false;
                    sp.getAbilities().flying = false;
                    sp.onUpdateAbilities();
                }
                if (!living.onGround()) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, true));
                }
                ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.broadcastWings(
                        level,
                        living,
                        0,
                        ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(pOrd),
                        sOrd >= 0 ? ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(sOrd) : null,
                        wTier
                );
            }
            if (tag.getLong(TAG_VOID_IMMUNE_UNTIL).orElse(0L) > now && living.getY() < level.getMinY() - 2) {
                Vec3 dv = living.getDeltaMovement();
                living.setDeltaMovement(dv.x, Math.max(dv.y, 0.65D), dv.z);
                living.resetFallDistance();
                living.hasImpulse = true;
            }

            // #48–#57 Apex, Prismatic, Eschaton & Singularity Shrouds, Mantles, Elixirs & Orbits
            tickApexEschatonSingularityStates(level, living, tag, now);
        }
    }

    private static void tickAirSlipstreamSprintCollision(ServerLevel level, LivingEntity caster, int sprintCharge) {
        level.sendParticles(ParticleTypes.CLOUD,
                caster.getX(), caster.getY() + 0.3D, caster.getZ(),
                sprintCharge >= 6 ? 6 : 2, 0.25D, 0.15D, 0.25D, 0.02D);

        // Full bowling-ball ramming charge unlocks after ~1.2s (6x 4-tick intervals) of continuous sprinting
        if (sprintCharge < 6) {
            return;
        }

        AABB hitBox = caster.getBoundingBox().inflate(1.65D, 0.6D, 1.65D);
        Vec3 forward = caster.getDeltaMovement();
        if (forward.horizontalDistanceSqr() < 1.0E-4D) {
            forward = caster.getLookAngle();
        }
        Vec3 normForward = new Vec3(forward.x, 0.0D, forward.z).normalize();

        boolean knockedAny = false;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox)) {
            if (target == caster || !target.isAlive() || target.isAlliedTo(caster)) continue;
            Vec3 side = target.position().subtract(caster.position());
            Vec3 fling = new Vec3(side.x, 0.0D, side.z).normalize().scale(1.25D).add(normForward.scale(0.75D));
            target.setDeltaMovement(fling.x, 0.55D, fling.z);
            target.hasImpulse = true;
            knockedAny = true;
        }
        if (knockedAny) {
            level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.7F, 1.25F);
        }
    }

    private static void tickNatureSylvanMantle(ServerLevel level, LivingEntity caster) {
        BlockPos feet = caster.blockPosition();
        BlockPos below = feet.below();
        if (level.getBlockState(below).is(Blocks.GRASS_BLOCK) && level.getBlockState(feet).isAir() && level.random.nextFloat() < 0.35F) {
            BlockState bloom = level.random.nextBoolean()
                    ? Blocks.SHORT_GRASS.defaultBlockState()
                    : (level.random.nextBoolean() ? Blocks.DANDELION.defaultBlockState() : Blocks.POPPY.defaultBlockState());
            level.setBlockAndUpdate(feet, bloom);
        }

        boolean nearFoliage = false;
        for (BlockPos p : BlockPos.betweenClosed(feet.offset(-2, -1, -2), feet.offset(2, 2, 2))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(BlockTags.LEAVES) || bs.is(BlockTags.LOGS) || bs.is(Blocks.GRASS_BLOCK)) {
                nearFoliage = true;
                break;
            }
        }
        if (nearFoliage) {
            caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 35, 1, true, false));
        }
        if (level.isBrightOutside() && level.canSeeSky(feet)) {
            SpellCombatReactionEngine.notifyMagicalHeal(caster, 0.5F);
            caster.heal(0.5F);
        }

        // Calm nearby wildlife (Animals & Neutral woodland fauna)
        for (Animal animal : level.getEntitiesOfClass(Animal.class, caster.getBoundingBox().inflate(8.0D))) {
            if (animal.getLastHurtByMob() == caster) {
                animal.setLastHurtByMob(null);
            }
            if (animal.getTarget() == caster) {
                animal.setTarget(null);
            }
            if (animal instanceof NeutralMob neutral && neutral.isAngryAt(caster, level)) {
                neutral.stopBeingAngry();
            }
        }
    }

    private static void tickNetherImmolationShroud(ServerLevel level, LivingEntity caster, long now) {
        caster.clearFire();
        if (caster.isInLava()) {
            Vec3 v = caster.getDeltaMovement();
            caster.setDeltaMovement(v.x * 1.15D, Math.max(v.y, 0.08D), v.z * 1.15D);
            caster.hasImpulse = true;
        }
        if (now % 20L == 0L) {
            BlockPos center = caster.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE)) {
                    level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                } else if (bs.is(Blocks.SNOW)) {
                    level.removeBlock(p, false);
                }
            }
        }
    }

    private static void tickRadiantPacificationShroud(ServerLevel level, LivingEntity caster) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(10.0D))) {
            if (mob instanceof NeutralMob neutral && (mob.getTarget() == caster || neutral.isAngryAt(caster, level))) {
                neutral.stopBeingAngry();
                mob.setTarget(null);
            } else if (mob instanceof EnderMan && mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
    }

    private static void tickUmbralStealthShroud(ServerLevel level, LivingEntity caster) {
        level.sendParticles(ParticleTypes.SMOKE, caster.getX(), caster.getY(0.6D), caster.getZ(),
                4, 0.35D, 0.45D, 0.35D, 0.01D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(16.0D))) {
            // Dampen sculk vibrations & clear Warden anger toward shrouded caster
            if (mob instanceof Warden warden) {
                warden.clearAnger(caster);
                if (warden.getTarget() == caster && mob.distanceToSqr(caster) > 9.0D) {
                    warden.setTarget(null);
                }
            }
            double distSq = mob.distanceToSqr(caster);
            if (distSq <= 9.0D && mob instanceof Monster) {
                mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false));
            } else if (distSq > 36.0D && mob.getTarget() == caster) {
                // 60% detection range reduction
                mob.setTarget(null);
            }
        }
    }

    private static void tickUndeadNecroticShroud(ServerLevel level, LivingEntity caster) {
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(12.0D))) {
            if (nearby == caster || !nearby.isAlive()) continue;
            boolean isUndead = nearby.getType().is(EntityTypeTags.UNDEAD);
            if (isUndead && nearby instanceof Mob mob && mob.getTarget() == caster) {
                mob.setTarget(null);
            } else if (!isUndead && nearby instanceof Monster && nearby.distanceToSqr(caster) <= 9.0D) {
                nearby.hurtServer(level, level.damageSources().magic(), 1.0F);
                level.sendParticles(ParticleTypes.SOUL, nearby.getX(), nearby.getY(0.6D), nearby.getZ(),
                        2, 0.15D, 0.2D, 0.15D, 0.02D);
            }
        }
    }

    private static void tickAridSandstormVeil(ServerLevel level, LivingEntity caster, long now) {
        // Blow incoming hostile arrows off target
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, caster.getBoundingBox().inflate(2.8D))) {
            if (proj.getOwner() == caster) continue;
            if (proj instanceof AbstractArrow) {
                Vec3 deflect = proj.getDeltaMovement().add((level.random.nextDouble() - 0.5D) * 0.7D, 0.25D, (level.random.nextDouble() - 0.5D) * 0.7D);
                proj.setDeltaMovement(deflect);
                proj.hasImpulse = true;
            }
        }

        // Form dry sandstone crust underfoot when walking over water surfaces
        BlockPos below = caster.blockPosition().below();
        if (level.getBlockState(below).is(Blocks.WATER)) {
            level.setBlockAndUpdate(below, Blocks.SANDSTONE.defaultBlockState());
        }

        if (now % 16L == 0L) {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(14.0D))) {
                if (mob.getTarget() == caster && mob.distanceToSqr(caster) > 49.0D) {
                    mob.setTarget(null);
                }
            }
        }
    }

    private static void tickMagmaIgneousMantle(ServerLevel level, LivingEntity caster) {
        BlockPos center = caster.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, 2, 4))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE)) {
                level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
            } else if (bs.is(Blocks.SNOW) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(p, false);
            } else if (bs.is(Blocks.WATER) && p.distManhattan(center) <= 3 && level.getFluidState(p).getAmount() < 8) {
                level.removeBlock(p, false);
                level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.6D, p.getZ() + 0.5D, 2, 0.2D, 0.2D, 0.2D, 0.01D);
            }
        }
    }

    private static void tickOvergrowthEarthRootBastion(ServerLevel level, LivingEntity caster, CompoundTag tag, long now) {
        double lx = tag.getDouble(TAG_OVERGROWTH_LAST_X).orElse(caster.getX());
        double lz = tag.getDouble(TAG_OVERGROWTH_LAST_Z).orElse(caster.getZ());
        double movedSq = (caster.getX() - lx) * (caster.getX() - lx) + (caster.getZ() - lz) * (caster.getZ() - lz);
        tag.putDouble(TAG_OVERGROWTH_LAST_X, caster.getX());
        tag.putDouble(TAG_OVERGROWTH_LAST_Z, caster.getZ());

        if (movedSq <= 0.0025D && caster.onGround()) {
            int still = Math.min(40, tag.getInt(TAG_OVERGROWTH_STILL_TICKS).orElse(0) + 4);
            tag.putInt(TAG_OVERGROWTH_STILL_TICKS, still);
            if (still >= 40) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 8.0D, 0.0D, 0.5D, 0.0D, 12L);
                if (now % 20L == 0L) {
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, 1.0F);
                    caster.heal(1.0F);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY(0.5D), caster.getZ(),
                            4, 0.3D, 0.25D, 0.3D, 0.02D);
                }
            }
        } else {
            tag.putInt(TAG_OVERGROWTH_STILL_TICKS, 0);
        }
    }

    private static void tickDustChalkVeil(ServerLevel level, LivingEntity caster, long now) {
        if (caster.isOnFire()) {
            caster.clearFire();
        }
        level.sendParticles(ParticleTypes.ASH, caster.getX(), caster.getY(0.8D), caster.getZ(),
                4, 0.4D, 0.45D, 0.4D, 0.01D);

        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, caster.getBoundingBox().inflate(2.8D))) {
            if (proj.getOwner() == caster) continue;
            if (proj instanceof AbstractArrow) {
                Vec3 deflect = proj.getDeltaMovement().add((level.random.nextDouble() - 0.5D) * 0.75D, 0.2D, (level.random.nextDouble() - 0.5D) * 0.75D);
                proj.setDeltaMovement(deflect);
                proj.hasImpulse = true;
            }
        }

        if (now % 16L == 0L) {
            BlockPos c = caster.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 2, 3))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH) || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                    level.destroyBlock(p, true);
                } else if (bs.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                    level.removeBlock(p, false);
                }
            }
            for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(16.0D))) {
                if (mob.getTarget() == caster && mob.distanceToSqr(caster) > 36.0D) {
                    mob.setTarget(null);
                }
            }
        }
    }

    public static void grantArcane3DFlight(LivingEntity target, int durationTicks) {
        CompoundTag tag = EntityHelper.getPersistentData(target);
        long now = target.level().getGameTime();
        EssenceType inferred = null;
        int inferredTier = tag.getInt(TAG_ARCANE_WING_TIER).orElse(7);
        if (tag.getLong(TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L) > now) {
            inferred = EssenceType.ENTROPICA;
            inferredTier = 10;
        } else if (tag.getLong(TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L) > now) {
            inferred = EssenceType.APOTHEOSIS;
            inferredTier = 10;
        } else if (tag.getLong(TAG_EMPYREAN_SHROUD_UNTIL).orElse(0L) > now) {
            inferred = EssenceType.EMPYREAN;
            inferredTier = 9;
        } else if (tag.getLong(TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L) > now) {
            inferred = EssenceType.CELESTIAL;
            inferredTier = 8;
        } else if (tag.getLong(TAG_AETHER_SHROUD_UNTIL).orElse(0L) > now || tag.getLong(TAG_AETHER_AEGIS_UNTIL).orElse(0L) > now) {
            inferred = EssenceType.AETHER;
            inferredTier = 8;
        } else if (tag.contains(TAG_ARCANE_WING_PRIMARY)) {
            inferred = ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(
                    tag.getInt(TAG_ARCANE_WING_PRIMARY).orElse(EssenceType.AIR.ordinal())
            );
        }
        EssenceType existingSec = tag.getInt(TAG_ARCANE_WING_SECONDARY).orElse(-1) >= 0
                ? ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(tag.getInt(TAG_ARCANE_WING_SECONDARY).orElse(0))
                : null;
        grantArcane3DFlight(target, durationTicks, inferred != null ? inferred : EssenceType.AIR, existingSec, inferredTier, false);
    }

    public static void grantArcane3DFlight(
            LivingEntity target,
            int durationTicks,
            @Nullable EssenceType primaryMateria,
            @Nullable EssenceType secondaryMateria,
            int potencyTier,
            boolean activateFlyingImmediately
    ) {
        long now = target.level().getGameTime();
        CompoundTag tag = EntityHelper.getPersistentData(target);
        int appliedDuration = Math.max(20, durationTicks);
        long newUntil = Math.max(tag.getLong(TAG_ARCANE_3D_FLIGHT_UNTIL).orElse(0L), now + appliedDuration);
        int remaining = (int) Math.min(Integer.MAX_VALUE, newUntil - now);

        EssenceType pMat = primaryMateria != null ? primaryMateria : EssenceType.REGULAR;
        EssenceType sMat = (secondaryMateria != null && secondaryMateria != pMat && secondaryMateria != EssenceType.REGULAR)
                ? secondaryMateria
                : null;
        int clampedTier = Math.clamp(potencyTier, 1, 10);

        tag.putLong(TAG_ARCANE_3D_FLIGHT_UNTIL, newUntil);
        tag.putBoolean(TAG_ARCANE_WINGS_ACTIVE, true);
        tag.putInt(TAG_ARCANE_WING_PRIMARY, pMat.ordinal());
        tag.putInt(TAG_ARCANE_WING_SECONDARY, sMat != null ? sMat.ordinal() : -1);
        tag.putInt(TAG_ARCANE_WING_TIER, clampedTier);
        target.resetFallDistance();

        if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
            boolean dirty = false;
            if (!sp.getAbilities().mayfly) {
                sp.getAbilities().mayfly = true;
                tag.putBoolean(TAG_GRANTED_MAYFLY, true);
                dirty = true;
            }
            if (activateFlyingImmediately && !sp.isSpectator() && !sp.getAbilities().flying) {
                sp.getAbilities().flying = true;
                dirty = true;
            }
            if (dirty) {
                sp.onUpdateAbilities();
            }
        }

        if (target.level() instanceof ServerLevel srv) {
            ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.broadcastWings(srv, target, remaining, pMat, sMat, clampedTier);
        }
    }

    public static void grantPartyAbsorption(ServerLevel level, @Nullable LivingEntity caster, float amount, float maxCap, double radius) {
        if (amount <= 0.0F || caster == null) return;
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius), LivingEntity::isAlive)) {
            if (ent == caster
                    || ent.isAlliedTo(caster)
                    || (caster instanceof net.minecraft.world.entity.player.Player && ent instanceof net.minecraft.world.entity.player.Player)
                    || (ent instanceof SpellEidolonEntity eid && eid.isFriendlyTo(caster))) {
                if (ent instanceof Monster && ent != caster) continue;
                ent.setAbsorptionAmount(Math.min(maxCap, ent.getAbsorptionAmount() + amount));
            }
        }
    }

    public static void spawnTypedEssenceOrb(ServerLevel level, Vec3 pos, EssenceType type, int tier) {
        net.minecraft.world.item.Item base = tier >= 3 ? ddraig.net.entropica.registry.ModItems.STRONG_ESSENCE.get()
                : (tier == 2 ? ddraig.net.entropica.registry.ModItems.AVERAGE_ESSENCE.get()
                : ddraig.net.entropica.registry.ModItems.WEAK_ESSENCE.get());
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(base, 1);
        ddraig.net.entropica.item.EssenceItem.setEssenceType(stack, type);
        ItemEntity drop = new ItemEntity(level, pos.x, pos.y + 0.35D, pos.z, stack);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }

    private static void snuffTorchesInRadius(ServerLevel level, BlockPos center, int radius, boolean douseFires) {
        int r = Math.min(8, Math.max(1, radius));
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH) || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                level.destroyBlock(p, true);
            } else if (douseFires && bs.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }
    }

    private static void dampenMobDetection(ServerLevel level, LivingEntity shrouded, double maxDetectDistSq) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, shrouded.getBoundingBox().inflate(20.0D))) {
            if (mob.getTarget() == shrouded && mob.distanceToSqr(shrouded) > maxDetectDistSq) {
                mob.setTarget(null);
            }
            if (mob instanceof Warden warden && mob.distanceToSqr(shrouded) > maxDetectDistSq) {
                warden.clearAnger(shrouded);
            }
        }
    }

    private static void tickApexEschatonSingularityStates(ServerLevel level, LivingEntity living, CompoundTag tag, long now) {
        // #48 Aether Transcendent Firmament Quintessence Shroud, Aegis & Orbit
        if (tag.getLong(TAG_AETHER_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 10.0D, 5.0D, 0.0D, 0.0D, 12L);
            living.resetFallDistance();
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            grantArcane3DFlight(living, 12);
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(4.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.ASH, proj.getX(), proj.getY(), proj.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.02D);
                proj.discard();
            }
            if (now % 16L == 0L) dampenMobDetection(level, living, 16.0D);
            if (now % 20L == 0L) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(6.0D))) {
                    if (foe == living || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                    if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                        foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 25, 0, true, true));
                    }
                }
            }
        } else if (tag.getLong(TAG_AETHER_AEGIS_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 10.0D, 5.0D, 0.0D, 0.0D, 12L);
            living.resetFallDistance();
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            grantArcane3DFlight(living, 12);
        }
        if (tag.getLong(TAG_AETHER_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
                proj.discard();
            }
        }

        // #49 Entropic Abyssal Event Horizon Mantle & Apex Entropic Shroud
        if (tag.getLong(TAG_ENTROPIC_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 10.0D, 5.0D, 0.0D, 0.0D, 12L);
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.POISON);
            living.removeEffect(MobEffects.BLINDNESS);
            living.removeEffect(MobEffects.DARKNESS);
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(4.0D))) {
                if (arrow.getOwner() == living) continue;
                level.sendParticles(ParticleTypes.ASH, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.1D, 0.1D, 0.1D, 0.02D);
                arrow.discard();
            }
            if (now % 16L == 0L) {
                snuffTorchesInRadius(level, living.blockPosition(), 6, false);
                dampenMobDetection(level, living, 16.0D);
            }
            if (now % 20L == 0L) {
                float drained = 0.0F;
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(6.0D))) {
                    if (foe == living || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                    if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                        if (foe.hurtServer(level, level.damageSources().wither(), 3.0F)) {
                            drained += 3.0F;
                        }
                    }
                }
                if (drained > 0.0F) {
                    SpellCombatReactionEngine.notifyMagicalHeal(living, drained);
                    living.heal(drained);
                }
            }
        } else if (tag.getLong(TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 10.0D, 5.0D, 0.0D, 0.0D, 12L);
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.POISON);
            living.removeEffect(MobEffects.BLINDNESS);
        }

        // #50 Celestial Holy Supernova Shroud, Ascendance Shroud & White-Hole Star Orbit
        if (tag.getLong(TAG_CELESTIAL_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 10.0D, 5.0D, 0.0D, 0.0D, 12L);
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.POISON);
            living.removeEffect(MobEffects.BLINDNESS);
            living.removeEffect(MobEffects.DARKNESS);
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(4.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 6, 0.12D, 0.12D, 0.12D, 0.03D);
                proj.discard();
                SpellCombatReactionEngine.notifyMagicalHeal(living, 2.0F);
                living.heal(2.0F);
            }
            if (now % 16L == 0L) dampenMobDetection(level, living, 16.0D);
        } else if (tag.getLong(TAG_CELESTIAL_ASCENDANCE_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 10.0D, 5.0D, 0.0D, 0.0D, 12L);
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.POISON);
            living.removeEffect(MobEffects.BLINDNESS);
        }
        if (tag.getLong(TAG_CELESTIAL_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
                proj.discard();
                SpellCombatReactionEngine.notifyMagicalHeal(living, 1.5F);
                living.heal(1.5F);
            }
            if (now % 60L == 0L) {
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(4.0D), LivingEntity::isAlive)) {
                    if (ally instanceof Monster && ally != living) continue;
                    if (ally == living || ally.isAlliedTo(living)
                            || ally instanceof net.minecraft.world.entity.player.Player
                            || (ally instanceof SpellEidolonEntity eid && eid.isFriendlyTo(living))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, 2.0F);
                        ally.heal(2.0F);
                    }
                }
            }
        }

        // #51 Prismatic Chromatic Dispersion Aura & Kaleidoscope Orbit
        if (tag.getLong(TAG_PRISMATIC_SHROUD_UNTIL).orElse(0L) > now) {
            living.clearFire();
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));
            if (now % 40L == 0L) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(5.0D), LivingEntity::isAlive)) {
                    if (foe == living || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                    boolean undeadOrBlight = foe.getType().is(EntityTypeTags.UNDEAD)
                            || EntityHelper.getPersistentData(foe).getLong(TAG_BLIGHT_CHAIN_MARK_UNTIL).orElse(0L) > now;
                    if (undeadOrBlight) {
                        foe.hurtServer(level, level.damageSources().magic(), 4.0F);
                        foe.igniteForSeconds(3);
                        level.sendParticles(ParticleTypes.END_ROD, foe.getX(), foe.getY(0.6D), foe.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.04D);
                    }
                }
            }
        }
        if (tag.getLong(TAG_PRISMATIC_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.0D));
                proj.setOwner(living);
                proj.hasImpulse = true;
            }
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(4.0D), LivingEntity::isAlive)) {
                if (ally instanceof Monster && ally != living) continue;
                if (ally == living || ally.isAlliedTo(living)
                        || ally instanceof net.minecraft.world.entity.player.Player
                        || (ally instanceof SpellEidolonEntity eid && eid.isFriendlyTo(living))) {
                    SpellCombatReactionEngine.applyTimedAttackSpeedBuff(ally, level, 0.20D, 12L);
                }
            }
        }

        // #52 Empyrean Living Titan Bark & Canopy Mantle, Aegis of the Infinite Life & Root-Web Orbit
        if (tag.getLong(TAG_EMPYREAN_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 12.0D, 6.0D, 0.0D, 0.0D, 12L);
            living.removeEffect(MobEffects.POISON);
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.HUNGER);
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(5.0D))) {
                if (arrow.getOwner() == living) continue;
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.02D);
                arrow.discard();
            }
            if (now % 16L == 0L) dampenMobDetection(level, living, 9.0D);
            if (now % 20L == 0L) {
                float siphoned = 0.0F;
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(6.0D), LivingEntity::isAlive)) {
                    if (foe == living || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                    if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                        if (foe.hurtServer(level, level.damageSources().magic(), 4.0F)) {
                            siphoned += 4.0F;
                        }
                    }
                }
                if (siphoned > 0.0F) {
                    grantPartyAbsorption(level, living, siphoned * 0.5F, 36.0F, 12.0D);
                }
            }
        } else if (tag.getLong(TAG_EMPYREAN_AEGIS_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 12.0D, 6.0D, 0.0D, 0.0D, 12L);
            living.removeEffect(MobEffects.POISON);
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.HUNGER);
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            if (now % 20L == 0L) {
                grantPartyAbsorption(level, living, 2.0F, 36.0F, 12.0D);
            }
        }
        if (tag.getLong(TAG_EMPYREAN_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.2D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
                proj.discard();
                grantPartyAbsorption(level, living, 2.0F, 32.0F, 10.0D);
            }
        }

        // #53 Cataclysm Mantle of the World-Breaker & Singularity Orbit
        if (tag.getLong(TAG_CATACLYSM_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 12.0D, 6.0D, 0.0D, 0.0D, 12L);
            living.clearFire();
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.BLINDNESS);
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(5.0D))) {
                if (arrow.getOwner() == living) continue;
                level.sendParticles(ParticleTypes.LAVA, arrow.getX(), arrow.getY(), arrow.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
                arrow.discard();
            }
            if (now % 16L == 0L) {
                snuffTorchesInRadius(level, living.blockPosition(), 6, false);
                dampenMobDetection(level, living, 9.0D);
            }
            if (now % 20L == 0L) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(6.0D), LivingEntity::isAlive)) {
                    if (foe == living || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                    if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                        Vec3 pull = living.position().subtract(foe.position()).normalize().scale(0.32D);
                        foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.05D, pull.z));
                        foe.hasImpulse = true;
                        foe.hurtServer(level, level.damageSources().magic(), 4.0F);
                        EntityHelper.getPersistentData(foe).putLong(TAG_UNQUENCHABLE_FIRE_UNTIL, now + 60L);
                        foe.igniteForSeconds(3);
                    }
                }
            }
        }
        if (tag.getLong(TAG_CATACLYSM_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.2D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.LAVA, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
                proj.discard();
            }
        }

        // #54 Terminus Zero-Point Kinetic Stasis Shield, Glacial Mantle, Frost Elixir & Polar Vortex Orbit
        if (tag.getLong(TAG_TERMINUS_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 12.0D, 6.0D, 0.0D, 0.0D, 12L);
            living.clearFire();
            living.setTicksFrozen(0);
            living.isInPowderSnow = false;
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            living.removeEffect(MobEffects.WITHER);
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, living.getBoundingBox().inflate(5.0D))) {
                if (arrow.getOwner() == living) continue;
                level.sendParticles(ParticleTypes.SNOWFLAKE, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.1D, 0.1D, 0.1D, 0.03D);
                arrow.discard();
            }
            if (now % 16L == 0L) {
                snuffTorchesInRadius(level, living.blockPosition(), 6, true);
                dampenMobDetection(level, living, 9.0D);
            }
        } else if (tag.getLong(TAG_TERMINUS_MANTLE_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 12.0D, 6.0D, 0.0D, 0.0D, 12L);
            living.clearFire();
            living.setTicksFrozen(0);
            living.isInPowderSnow = false;
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            BlockState under = level.getBlockState(living.blockPosition().below());
            if (under.is(Blocks.ICE) || under.is(Blocks.PACKED_ICE) || under.is(Blocks.BLUE_ICE) || under.is(Blocks.FROSTED_ICE)) {
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 1, true, false));
            }
        }
        if (tag.getLong(TAG_TERMINUS_ELIXIR_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 0.0D, 6.0D, 0.0D, 0.0D, 12L);
            living.clearFire();
            living.setTicksFrozen(0);
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            BlockState under = level.getBlockState(living.blockPosition().below());
            if (under.is(Blocks.ICE) || under.is(Blocks.PACKED_ICE) || under.is(Blocks.BLUE_ICE) || under.is(Blocks.FROSTED_ICE)) {
                SpellCombatReactionEngine.applyTimedAttackSpeedBuff(living, level, 0.30D, 12L);
            }
        }
        if (tag.getLong(TAG_TERMINUS_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.2D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.SNOWFLAKE, proj.getX(), proj.getY(), proj.getZ(), 7, 0.1D, 0.1D, 0.1D, 0.03D);
                proj.discard();
            }
        }

        // #55 Eschaton Mantle of the Omega Singularity, Stasis Elixir & Event Horizon Orbit
        if (tag.getLong(TAG_ESCHATON_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 14.0D, 8.0D, 0.0D, 0.0D, 12L);
            living.clearFire();
            living.setTicksFrozen(0);
            living.resetFallDistance();
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            living.removeEffect(MobEffects.WITHER);
            tag.putLong(TAG_VOID_IMMUNE_UNTIL, Math.max(tag.getLong(TAG_VOID_IMMUNE_UNTIL).orElse(0L), now + 16L));
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(6.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, proj.getX(), proj.getY(), proj.getZ(), 8, 0.1D, 0.1D, 0.1D, 0.04D);
                proj.discard();
                if (living instanceof net.minecraft.world.entity.player.Player p) {
                    ddraig.net.entropica.spell.fuel.SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ESCHATON, 64L);
                }
            }
            if (now % 16L == 0L) {
                snuffTorchesInRadius(level, living.blockPosition(), 8, false);
                dampenMobDetection(level, living, 6.25D);
            }
            if (now % 20L == 0L) {
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(8.0D), LivingEntity::isAlive)) {
                    if (foe == living || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, living, true)) continue;
                    if (foe instanceof Monster || foe == living.getLastHurtMob()) {
                        Vec3 pull = living.position().subtract(foe.position()).normalize().scale(0.42D);
                        foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.06D, pull.z));
                        foe.hasImpulse = true;
                        foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 6.0F);
                    }
                }
            }
        }
        if (tag.getLong(TAG_ESCHATON_STASIS_ELIXIR_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 0.0D, 10.0D, 0.0D, 0.0D, 12L);
            tag.putLong(TAG_COMPLETE_INVULNERABILITY_UNTIL, Math.max(tag.getLong(TAG_COMPLETE_INVULNERABILITY_UNTIL).orElse(0L), now + 12L));
            grantArcane3DFlight(living, 12);
        }
        if (tag.getLong(TAG_ESCHATON_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.5D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, proj.getX(), proj.getY(), proj.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.04D);
                proj.discard();
                if (living instanceof net.minecraft.world.entity.player.Player p) {
                    ddraig.net.entropica.spell.fuel.SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ESCHATON, 48L);
                }
            }
        }

        // #56 Apotheosis Mantle of the Alpha Singularity & White-Hole Star-Orbit (emits Level-15 light!)
        if (tag.getLong(TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 14.0D, 8.0D, 0.0D, 0.0D, 12L);
            living.clearFire();
            living.setTicksFrozen(0);
            living.resetFallDistance();
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.BLINDNESS);
            living.removeEffect(MobEffects.DARKNESS);
            tag.putLong(TAG_VOID_IMMUNE_UNTIL, Math.max(tag.getLong(TAG_VOID_IMMUNE_UNTIL).orElse(0L), now + 16L));
            grantArcane3DFlight(living, 12);
            if (now % 12L == 0L) {
                pulseTemporaryLight(level, living.blockPosition().above(), 15, 16);
                dampenMobDetection(level, living, 6.25D);
            }
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(6.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                Vec3 pPos = proj.position();
                proj.discard();
                level.sendParticles(ParticleTypes.END_ROD, pPos.x, pPos.y, pPos.z, 8, 0.12D, 0.12D, 0.12D, 0.04D);
                if (level.random.nextFloat() < 0.35F) {
                    spawnTypedEssenceOrb(level, pPos, EssenceType.RADIANT, 1);
                }
            }
            if (now % 20L == 0L) {
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(8.0D), LivingEntity::isAlive)) {
                    if (ent == living || ent.isAlliedTo(living)
                            || (ent instanceof net.minecraft.world.entity.player.Player && !(ent instanceof Monster))
                            || (ent instanceof SpellEidolonEntity eid && eid.isFriendlyTo(living))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F);
                        ent.heal(6.0F);
                        ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, 10, 0, true, false));
                    } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, living, true)
                            && (ent.getType().is(EntityTypeTags.UNDEAD) || ent instanceof EnderMan || ent instanceof Blaze || ent instanceof WitherSkeleton || ent instanceof Monster)) {
                        ent.hurtServer(level, level.damageSources().magic(), 6.0F);
                        ent.igniteForSeconds(3);
                    }
                }
            }
        }
        if (tag.getLong(TAG_APOTHEOSIS_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.5D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                Vec3 pPos = proj.position();
                proj.discard();
                SpellCombatReactionEngine.notifyMagicalHeal(living, 2.0F);
                living.heal(2.0F);
                if (level.random.nextFloat() < 0.25F) {
                    spawnTypedEssenceOrb(level, pPos, EssenceType.RADIANT, 1);
                }
            }
        }

        // #57 Entropica Mantle of Entropica & Cosmic Parallax Orbit (strictly NO dynamic light blocks!)
        if (tag.getLong(TAG_ENTROPICA_SHROUD_UNTIL).orElse(0L) > now) {
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(living, level, 14.0D, 8.0D, 0.0D, 0.0D, 12L);
            living.clearFire();
            living.setTicksFrozen(0);
            living.resetFallDistance();
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.BLINDNESS);
            living.removeEffect(MobEffects.DARKNESS);
            tag.putLong(TAG_VOID_IMMUNE_UNTIL, Math.max(tag.getLong(TAG_VOID_IMMUNE_UNTIL).orElse(0L), now + 16L));
            grantArcane3DFlight(living, 12);
            if (now % 16L == 0L) dampenMobDetection(level, living, 6.25D);
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(6.0D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
                proj.discard();
                if (living instanceof net.minecraft.world.entity.player.Player p) {
                    ddraig.net.entropica.spell.fuel.SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ENTROPICA, 80L);
                }
            }
            if (now % 20L == 0L) {
                for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(8.0D), LivingEntity::isAlive)) {
                    if (ent == living || ent.isAlliedTo(living)
                            || (ent instanceof net.minecraft.world.entity.player.Player && !(ent instanceof Monster))
                            || (ent instanceof SpellEidolonEntity eid && eid.isFriendlyTo(living))) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F);
                        ent.heal(6.0F);
                    } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, living, true)
                            && (ent instanceof Monster || ent == living.getLastHurtMob())) {
                        ent.hurtServer(level, level.damageSources().fellOutOfWorld(), 6.0F);
                    }
                }
            }
        }
        if (tag.getLong(TAG_ENTROPICA_ORBIT_UNTIL).orElse(0L) > now) {
            for (Projectile proj : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(3.5D))) {
                if (proj.getOwner() == living || proj instanceof ddraig.net.entropica.entity.projectile.SpellProjectileEntity) continue;
                proj.discard();
                if (living instanceof net.minecraft.world.entity.player.Player p) {
                    ddraig.net.entropica.spell.fuel.SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ENTROPICA, 64L);
                }
            }
        }
    }

    /**
     * Places a temporary self-clearing {@link Blocks#LIGHT} block at {@code pos} if it is currently air,
     * restoring clean air when {@code durationTicks} elapses.
     */
    public static void pulseTemporaryLight(ServerLevel level, BlockPos pos, int lightLevel, int durationTicks) {
        int clamped = Math.max(1, Math.min(15, lightLevel));
        if (!level.getBlockState(pos).isAir()) {
            return;
        }
        BlockPos immutable = pos.immutable();
        level.setBlockAndUpdate(immutable, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, clamped));
        registerZone(level, new ActiveSpellZone(
                null, Vec3.atCenterOf(immutable), 1.0D, durationTicks, level.getGameTime(),
                null, null, null, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) {
                        return true;
                    }
                    if (srv.getBlockState(immutable).is(Blocks.LIGHT)) {
                        srv.setBlockAndUpdate(immutable, Blocks.AIR.defaultBlockState());
                    }
                    return false;
                }
        ));
    }
}
