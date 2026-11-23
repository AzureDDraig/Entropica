package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.combat.VisInteractionManager;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Per-part Signature Mutation implementation for <b>Equinox Materia (Caprine Apex Secret Catalyst)</b>
 * (Radiant Fire {@code #FFEA78/#FF5000} &harr; Umbral Water {@code #211A21/#225C7C} — Lossless Phase-Inversion &amp; Steam-Cavitation).
 */
public final class EquinoxSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int EQUINOX_SOLAR_HEX = 0xFFEA78;
    public static final int EQUINOX_PYRIC_HEX = 0xFF5000;
    public static final int EQUINOX_ABYSS_HEX = 0x225C7C;

    private static final String TAG_EQ_BEAM_LAST_TICK = "SpellEquinoxBeamLastTick";
    private static final String TAG_EQ_BEAM_TICKS = "SpellEquinoxBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.EQUINOX;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case ORBIT_SATELLITE -> "Equinox Phase-Inversion Orbit Flow";
            case DOUBLE_BIFURCATE -> "Gemini Twin-Polarity Split Flow";
            case CHAIN_LEAP -> "Equinox Harmonic Inversion Chain Flow";
            default -> SpellMutationRegistry.getEpithet(EssenceType.EQUINOX) + " " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case ORBIT_SATELLITE -> "Conjures twin counter-rotating Radiant Fire and Umbral Water satellites that trigger Steam-Cavitation on contact.";
            case DOUBLE_BIFURCATE -> "Splits spell delivery into paired Radiant Fire and Umbral Water polarities for instant phase-inversion combos.";
            case CHAIN_LEAP -> "Alternates solar-prominence ignition and abyssal cavitation drench across each chained leap.";
            default -> "Stabilizes spell flow through the Caprine Equinox Matrix.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Equinox Phase-Inversion Lance-Orb";
            case BEAM -> "Gemini Duality Convergence Beam";
            case RADIAL_BURST -> "Equinox Steam-Cavitation Nova";
            case TOUCH_STRIKE -> "Gemini Scepter Duality Cleave";
            case AURA -> "Mantle of Caprine Equilibrium";
            case GLYPH -> "Equinox Crucible Mandala";
            case CONE -> "Twin-Polarity Steam-Cavitation Deluge";
            case SKYFALL -> "Gemini Equinox Tempest";
            case SUMMON_AGENT -> "Gemini Equinox Archon";
            case SUMMON_SENTRY -> "Phase-Inversion Crucible Spire";
            case SUMMON_DECOY -> "Twin-Polarity Mirage Effigy";
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Fires a harmonized Radiant-Fire and Umbral-Water core that pulls foes inward before detonating in a Steam-Cavitation shockwave.";
            case BEAM -> "Channels a twin helix of solar prominence and abyssal waters, shredding armor and triggering a Steam-Cavitation Collapse at 3.0s.";
            case RADIAL_BURST -> "Pulls surrounding hostiles inward with Umbral undertow before blasting them back with purging Radiant steam while cleansing allies.";
            case TOUCH_STRIKE -> "Delivers a dual-polarity Caprine strike that shreds 4 armor points, detonates Steam-Cavitation, and bolsters caster Absorption.";
            case AURA -> "Grants balanced Fire Resistance, Water Breathing, Blindness/Darkness immunity, and harmonic thermal-aquatic protection.";
            case GLYPH -> "Inscribes an Equinox Crucible Mandala that restores and cleanses allies or detonates a Steam-Cavitation vortex beneath hostiles.";
            case CONE -> "Projects a billowing wave of superheated solar fire and pressurized umbral water that shreds armor and dazes foes.";
            case SKYFALL -> "Rains alternating Radiant Fire lances and Umbral Water cavitation spheres across the battlefield for 14 seconds.";
            case SUMMON_AGENT -> "Summons an autonomous Gemini Equinox Archon wielding dual-polarity phase-inversion strikes.";
            case SUMMON_SENTRY -> "Summons an anchored Phase-Inversion Crucible Spire firing alternating sunfire and cavitation volleys.";
            case SUMMON_DECOY -> "Manifests a Twin-Polarity Mirage Effigy that taunts hostiles before erupting in an Equinox Steam-Cavitation blast.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Equinox Phase-Inversion Step";
            case TRANSMUTE, BREAK -> "Equinox Lossless Phase-Inversion";
            case AFFLICTION -> "Equinox Thermal-Void Paradox Hex";
            default -> "Duality Equinox " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Inverts spatial polarity: leaves a pulling Umbral vortex at departure and arrives in a cleansing Radiant flash.";
            case TRANSMUTE, BREAK -> "Executes lossless 1:1 Caprine phase-inversion across light/dark and thermal/aquatic blocks and items.";
            case AFFLICTION -> "Afflicts targets with a milk-immune dual-polarity Equinox Hex oscillating between solar sear and abyssal cavitation.";
            default -> "Harmonizes the effect through the Caprine Equinox Catalyst.";
        };
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        int color = (projectile.tickCount & 1) == 0 ? EQUINOX_SOLAR_HEX : EQUINOX_ABYSS_HEX;
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), color, 15, 0.74F, 0.05F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
    }

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
        } else {
            level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (living == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, living)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        Vec3 center = living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
        detonateEquinoxCavitation(level, center, caster, spell.hasWardFriendlyFireSafeguard(), tier,
                !slot.hasHarmVerb() ? (3.80F + tier * 0.95F) : (1.85F + tier * 0.50F), 3.4D);
    }

    @Override
    public void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        detonateEquinoxCavitation(level, blockHit.getLocation(), caster, spell.hasWardFriendlyFireSafeguard(),
                spell.getEffectiveTier(), 2.80F + spell.getEffectiveTier() * 0.70F, 3.0D);
    }

    @Override
    public void onBeamFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            List<LivingEntity> hitEntities,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 1.5F);
                hit.heal(1.5F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 60L);

            if (!slot.hasHarmVerb()) {
                hurtWithEquinoxAdvantage(level, hit, caster, (1.65F + tier * 0.45F) * Math.max(0.5F, apparatusPotency));
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_EQ_BEAM_LAST_TICK).orElse(-100L);
            if (now == last) continue;
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_EQ_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_EQ_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_EQ_BEAM_TICKS, 0);
                detonateEquinoxCavitation(level, hit.position().add(0.0D, 0.5D, 0.0D), caster, wardSafe, tier,
                        (4.20F + tier * 1.05F) * Math.max(0.5F, apparatusPotency), 4.0D);
            } else {
                data.putInt(TAG_EQ_BEAM_TICKS, contactTicks);
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 center,
            double radius,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.5D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        AABB allyBox = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 4.0D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, allyBox, LivingEntity::isAlive)) {
            if (e == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, e)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(e);
                float heal = 4.0F + tier * 0.6F;
                SpellCombatReactionEngine.notifyMagicalHeal(e, heal);
                e.heal(heal);
            }
        }

        detonateEquinoxCavitation(level, center, caster, wardSafe, tier,
                (!slot.hasHarmVerb() ? (3.85F + tier * 0.95F) : (1.75F + tier * 0.45F)) * Math.max(0.5F, apparatusPotency), r);
    }

    @Override
    public void onTouchStrikeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            List<LivingEntity> hitEntities,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.5D,
                EssenceType.EQUINOX, EssenceType.RADIANT_FIRE, tier
        );
        float strikeDmg = (3.50F + tier * 0.90F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 4.0D, 1.0D, 140L);
            hit.igniteForSeconds(4);
            SpellLingeringZoneManager.applySoaked(hit, 100);
            hurtWithEquinoxAdvantage(level, hit, caster, !slot.hasHarmVerb() ? strikeDmg : (strikeDmg * 0.40F));
        }
        caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1, false, true));
    }

    @Override
    public void onAuraFormCast(
            ServerLevel level,
            LivingEntity caster,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        int dur = 240 + spell.getEffectiveTier() * 40;
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, dur, 0, false, true));
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 18, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 glyphCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        int tier = spell.getEffectiveTier();
        double r = Math.max(4.0D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] triggersLeft = {repeating ? 3 : 1};
        final int[] cooldown = {0};
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, glyphCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.EQUINOX, EssenceType.RADIANT_FIRE, 240, tier
        );

        SpellLingeringZoneManager.ActiveSpellZone glyph = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, r, maxDuration, level.getGameTime(), spell, slot, EssenceType.EQUINOX, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed < 20) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB box = new AABB(zone.center.x - 2.8D, zone.center.y - 1.2D, zone.center.z - 2.8D,
                            zone.center.x + 2.8D, zone.center.y + 2.4D, zone.center.z + 2.8D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity e : stepped) {
                        if ((e == owner && elapsed >= 30 && e.getHealth() < e.getMaxHealth())
                                || (e != owner && SpellExecutionEngine.isFriendlyAllyForOasis(owner, e) && e.getHealth() < e.getMaxHealth())) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(e);
                            SpellCombatReactionEngine.notifyMagicalHeal(e, 8.0F);
                            e.heal(8.0F);
                            triggersLeft[0]--;
                            cooldown[0] = 45;
                            return triggersLeft[0] > 0;
                        }
                    }

                    boolean hostileFound = false;
                    for (LivingEntity e : stepped) {
                        if (e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))) {
                            hostileFound = true;
                            break;
                        }
                    }
                    if (!hostileFound) return true;

                    detonateEquinoxCavitation(srv, zone.center, owner, wardSafe, tier,
                            (3.90F + tier * 0.95F) * Math.max(0.5F, zone.apparatusPotency), Math.max(4.2D, zone.radius));
                    triggersLeft[0]--;
                    cooldown[0] = 45;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyph);
        return true;
    }

    @Override
    public void onConeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double coneRange,
            List<LivingEntity> hitTargets,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.EQUINOX, EssenceType.UMBRAL_WATER, tier
        );
        float coneDmg = (3.40F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.setDeltaMovement(direction.x * 1.22D, 0.35D, direction.z * 1.22D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 4.0D, 0.0D, 120L);
            hurtWithEquinoxAdvantage(level, hit, caster, !slot.hasHarmVerb() ? coneDmg : (coneDmg * 0.40F));
        }
    }

    @Override
    public boolean onSkyfallFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 impactCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.EQUINOX, EssenceType.RADIANT_FIRE, 280, tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 280, level.getGameTime(), spell, slot, EssenceType.EQUINOX, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.FLAME, zone.center.x, zone.center.y + 4.8D, zone.center.z,
                                12, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.05D);
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 2.0D, zone.center.z,
                                16, zone.radius * 0.5D, 0.8D, zone.radius * 0.5D, 0.06D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius);
                        float tickDmg = (2.80F + tier * 0.72F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (e == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 1.5F);
                                e.heal(1.5F);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) continue;
                            SpellCombatReactionEngine.applyTimedAttributeShred(e, srv, 4.0D, 0.0D, 60L);
                            hurtWithEquinoxAdvantage(srv, e, owner, tickDmg);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.EQUINOX.name());
        tag.putInt("SpellEidolonTintRGB", EQUINOX_SOLAR_HEX);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.6D, EssenceType.EQUINOX, EssenceType.UMBRAL_WATER, 40, spell.getEffectiveTier()
        );
    }

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.EQUINOX, potencyTier);
        SpellWorldVfxManager.broadcastVacuumSphere(level, departurePos.add(0.0D, 0.5D, 0.0D), 3.0D, true,
                EssenceType.UMBRAL_WATER, EssenceType.EQUINOX, 16, potencyTier);
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, true));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        // Caprine Phase-Inversion Transmutation: 1:1 lossless opposite-polarity inversion!
        BlockState inverted = null;
        if (state.is(Blocks.OBSIDIAN)) {
            inverted = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        } else if (state.is(Blocks.CRYING_OBSIDIAN)) {
            inverted = Blocks.OBSIDIAN.defaultBlockState();
        } else if (state.is(Blocks.MAGMA_BLOCK)) {
            inverted = Blocks.PRISMARINE.defaultBlockState();
        } else if (state.is(Blocks.PRISMARINE) || state.is(Blocks.DARK_PRISMARINE)) {
            inverted = Blocks.MAGMA_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.BLACKSTONE)) {
            inverted = Blocks.QUARTZ_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.QUARTZ_BLOCK)) {
            inverted = Blocks.BLACKSTONE.defaultBlockState();
        } else if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            inverted = Blocks.GLOWSTONE.defaultBlockState();
        } else if (state.is(Blocks.GLOWSTONE)) {
            inverted = Blocks.SOUL_SOIL.defaultBlockState();
        } else if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) {
            inverted = Blocks.CALCITE.defaultBlockState();
        }
        if (inverted != null) {
            level.setBlockAndUpdate(pos, inverted);
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        ItemStack out = ItemStack.EMPTY;
        if (stack.is(Items.OBSIDIAN)) {
            out = new ItemStack(Items.CRYING_OBSIDIAN, stack.getCount());
        } else if (stack.is(Items.CRYING_OBSIDIAN)) {
            out = new ItemStack(Items.OBSIDIAN, stack.getCount());
        } else if (stack.is(Items.BLAZE_POWDER)) {
            out = new ItemStack(Items.PRISMARINE_CRYSTALS, stack.getCount());
        } else if (stack.is(Items.PRISMARINE_CRYSTALS)) {
            out = new ItemStack(Items.BLAZE_POWDER, stack.getCount());
        } else if (stack.is(Items.ROTTEN_FLESH)) {
            out = new ItemStack(Items.GOLDEN_CARROT, Math.max(1, stack.getCount() / 2));
        }
        if (!out.isEmpty()) {
            itemEntity.setItem(out);
            level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.04D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.removeEffect(MobEffects.INVISIBILITY);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D * mult, 1.0D, 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.85F + potencyTier * 0.62F) * mult;
        hurtWithEquinoxAdvantage(level, target, hexCaster, hexDmg);
        return true;
    }

    private static void detonateEquinoxCavitation(
            ServerLevel level,
            Vec3 center,
            @Nullable LivingEntity caster,
            boolean wardSafe,
            int tier,
            float baseDamage,
            double radius
    ) {
        SpellWorldVfxManager.broadcastVacuumSphere(level, center.add(0.0D, 0.45D, 0.0D), radius * 0.85D, true,
                EssenceType.UMBRAL_WATER, EssenceType.EQUINOX, 14, tier);
        SpellWorldVfxManager.broadcastRadialNova(level, center, radius, EssenceType.EQUINOX, EssenceType.RADIANT_FIRE, tier);

        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius,
                center.x + radius, center.y + 3.5D, center.z + radius);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 70, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 120L);
            hurtWithEquinoxAdvantage(level, foe, caster, baseDamage);
        }
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.6D, center.z, 22, 1.2D, 0.5D, 1.2D, 0.07D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.7D, center.z, 20, 1.2D, 0.5D, 1.2D, 0.07D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.80F, 1.38F);
    }

    private static float hurtWithEquinoxAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) return 0.0F;
        Set<EssenceType> defenderAffs = SpellCombatReactionEngine.resolveEntityAffinities(target);
        boolean mobToPlayer = (target instanceof Player) && !(caster instanceof Player);
        float mult = VisInteractionManager.getInstance().calculateDamageMultiplier(
                EssenceType.EQUINOX, defenderAffs, mobToPlayer, 0.0F
        );
        if (mult <= 0.0F) return 0.0F;
        float finalDmg = Math.max(0.5F, baseDamage * mult);
        target.invulnerableTime = 0;
        DamageSource src = caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic();
        return SpellCombatReactionEngine.hurtWithPureMateria(level, target, src, finalDmg) ? finalDmg : 0.0F;
    }
}
