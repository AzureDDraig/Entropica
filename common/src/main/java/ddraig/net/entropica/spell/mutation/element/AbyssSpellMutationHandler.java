package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Abyss Materia (Ethereal &amp; Paradox Fusion #33)</b>
 * (Spellcrafting System Guide Section 6.34 — Dual Umbral {@code #211A21} + Water {@code #225C7C} [Leans: Umbral]).
 */
public final class AbyssSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Pitch-Black Oceanic Ink (#211A21) primary tint. */
    public static final int KRAKEN_HUSK_HEX_COLOR = 0x211A21;

    /** Canonical Deep Trench Navy (#225C7C) secondary tint. */
    public static final int DEEP_TRENCH_NAVY_HEX_COLOR = 0x225C7C;

    private static final String TAG_ABYSS_BEAM_LAST_TICK = "SpellAbyssBeamLastTick";
    private static final String TAG_ABYSS_BEAM_TICKS = "SpellAbyssBeamTicks";
    private static final String TAG_ABYSS_ORB_IMPLODED = "SpellAbyssOrbImploded";

    @Override
    public EssenceType materiaType() {
        return EssenceType.ABYSS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Abyssal Maelstrom Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Abyssal Depth Charge Orb";
            case BEAM -> "Hydrostatic Trench Torrent Beam";
            case RADIAL_BURST -> "Benthic Inversion Shockwave";
            case TOUCH_STRIKE -> "Abyssal Drowning Cleave";
            case AURA -> "Hadopelagic Trench Shroud";
            case GLYPH -> "Benthic Maelstrom Pitfall";
            case CONE -> "Abyssal Ink-Blast Deluge";
            case SKYFALL -> "Crushing Trench Monolith";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Benthic Trench Slip";
            case TRANSMUTE, BREAK -> "Hadopelagic Erosion & Darkening Transmute";
            case AFFLICTION -> "Crushing Benthic Drowning Hex";
            default -> "Hadopelagic Trench " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — ABYSSAL MAELSTROM ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), KRAKEN_HUSK_HEX_COLOR, 16, 0.76F, -0.05F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), DEEP_TRENCH_NAVY_HEX_COLOR, 16, 0.68F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — ABYSSAL DEPTH CHARGE ORB (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
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
        implodeDepthChargeOrbOnce(level, projectile, caster, hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.35D, 0.0D), spell);
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
        implodeDepthChargeOrbOnce(level, projectile, caster, blockHit.getLocation(), spell);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        implodeDepthChargeOrbOnce(level, projectile, caster, projectile.position(), spell);
    }

    private static void implodeDepthChargeOrbOnce(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_ABYSS_ORB_IMPLODED).orElse(false)) {
            return;
        }
        pTag.putBoolean(TAG_ABYSS_ORB_IMPLODED, true);

        // Section 6.34 Abyssal Depth Charge Orb:
        // Dense sphere of pressurized pitch-black oceanic water (#211A21 / #225C7C); on impact, implodes with
        // crushing hydrostatic trench pressure, dragging caught mobs into a 3-block vortex, dealing Abyss Materia
        // damage, inflicting 3.0s (60t) Blindness & Slowness II, and extinguishing surface fires!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.add(0.0D, 0.55D, 0.0D), 3.0D, true, EssenceType.ABYSS, EssenceType.WATER, 18, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(
                level, center, 3.0D, EssenceType.ABYSS, EssenceType.WATER, tier
        );

        AABB box = new AABB(center.x - 3.0D, center.y - 2.0D, center.z - 3.0D,
                center.x + 3.0D, center.y + 3.0D, center.z + 3.0D);
        float implosionDmg = 3.15F + tier * 0.82F;

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = center.subtract(foe.position());
            Vec3 drag = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(0.92D) : Vec3.ZERO;
            foe.setDeltaMovement(drag.x, 0.18D, drag.z);
            foe.hasImpulse = true;
            foe.clearFire();
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            SpellLingeringZoneManager.applySoaked(foe, 140);
            SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, foe, caster, implosionDmg, false, true);
        }

        extinguishOpenFiresOnly(level, BlockPos.containing(center), 3);
        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 0.6D, center.z, 18, 0.9D, 0.4D, 0.9D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.6D, center.z, 26, 1.0D, 0.5D, 1.0D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.68F);
    }

    // =========================================================================
    // FORM MUTATION 2 — HYDROSTATIC TRENCH TORRENT BEAM (BEAM)
    // =========================================================================

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
        // Section 6.34 Hydrostatic Trench Torrent Beam:
        // Continuous pressurized stream of ink-black oceanic water (#211A21 / #225C7C); applies crushing hydrostatic
        // pressure that slows caught mobs by 60% and silences their abilities; channeling continuously for 3.0s
        // (60 contact ticks = 15 beam slices) triggers a Benthic Implosion that drags caught targets together and
        // strips 3 physical armor points!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.clearFire();
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.60D, 45L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2));
            SpellLingeringZoneManager.applyArcaneSilence(hit, 45);
            SpellLingeringZoneManager.applySoaked(hit, 120);

            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.35F + tier * 0.38F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_ABYSS_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_ABYSS_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_ABYSS_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_ABYSS_BEAM_TICKS, 0);
                Vec3 implosionCenter = hit.position();
                SpellWorldVfxManager.broadcastVacuumSphere(
                        level, implosionCenter.add(0.0D, 0.8D, 0.0D), 4.0D, true, EssenceType.ABYSS, EssenceType.WATER, 20, tier
                );
                SpellWorldVfxManager.broadcastRadialNova(
                        level, implosionCenter, 4.0D, EssenceType.ABYSS, EssenceType.WATER, tier
                );

                float implosionDmg = (3.40F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
                AABB implosion = new AABB(
                        implosionCenter.x - 4.0D, implosionCenter.y - 2.0D, implosionCenter.z - 4.0D,
                        implosionCenter.x + 4.0D, implosionCenter.y + 3.0D, implosionCenter.z + 4.0D
                );
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, implosion, LivingEntity::isAlive)) {
                    if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

                    Vec3 delta = implosionCenter.subtract(foe.position());
                    Vec3 pull = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.05D) : Vec3.ZERO;
                    foe.setDeltaMovement(pull.x, 0.22D, pull.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
                    SpellLingeringZoneManager.applySoaked(foe, 160);
                    SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, foe, caster, implosionDmg, false, true);
                }

                level.sendParticles(ParticleTypes.SQUID_INK,
                        implosionCenter.x, implosionCenter.y + 0.8D, implosionCenter.z, 22, 1.0D, 0.5D, 1.0D, 0.04D);
                level.sendParticles(ParticleTypes.SPLASH,
                        implosionCenter.x, implosionCenter.y + 0.8D, implosionCenter.z, 28, 1.1D, 0.5D, 1.1D, 0.09D);
                level.playSound(null, hit.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.05F, 0.62F);
            } else {
                data.putInt(TAG_ABYSS_BEAM_TICKS, contactTicks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — BENTHIC INVERSION SHOCKWAVE (RADIAL BURST)
    // =========================================================================

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
        // Section 6.34 Benthic Inversion Shockwave:
        // Silent 360° eruption of crushing abyssal water & blinding black ink (6m+ radius, #211A21 / #225C7C);
        // knocks hostiles 6 blocks backward, inflicts 4.0s (80t) Blindness & Slowness III, wipes mob aggro,
        // drenches caught targets (doubling incoming Lightning damage), and extinguishes all open fires!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.ABYSS, EssenceType.WATER, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.2D, center.z + r);
        float waveDmg = (3.10F + tier * 0.78F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = foe.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.48D) : new Vec3(0.0D, 0.36D, 0.0D);
            foe.setDeltaMovement(away.x, 0.36D, away.z);
            foe.hasImpulse = true;
            foe.clearFire();
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            SpellLingeringZoneManager.applySoaked(foe, 180);
            wipeMobAggro(foe);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, foe, caster, waveDmg, false, true);
            }
        }

        extinguishOpenFiresOnly(level, BlockPos.containing(center), Math.min(7, (int) Math.ceil(r)));
        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 0.7D, center.z, 32, 2.2D, 0.5D, 2.2D, 0.05D);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.7D, center.z, 36, 2.2D, 0.5D, 2.2D, 0.09D);
    }

    // =========================================================================
    // FORM MUTATION 4 — ABYSSAL DROWNING CLEAVE (TOUCH / STRIKE)
    // =========================================================================

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
        // Section 6.34 Abyssal Drowning Cleave:
        // Sweeping horizontal cleave of dark water (#211A21 / #225C7C); deals Abyss Materia damage (stacking held
        // weapon Materia), knocks enemies back 5 blocks, inflicts drowning damage on land for 4.0s (80t), and drops
        // active mob aggro so struck enemies lose target lock!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.0D,
                EssenceType.ABYSS, EssenceType.WATER, tier
        );

        float cleaveDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.clearFire();
            hit.setDeltaMovement(direction.x * 1.28D, 0.34D, direction.z * 1.28D);
            hit.hasImpulse = true;
            SpellLingeringZoneManager.applyAbyssLandDrowning(hit, 80);
            SpellLingeringZoneManager.applySoaked(hit, 140);
            wipeMobAggro(hit);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, hit, caster, cleaveDmg, true, true);
            }
            level.sendParticles(ParticleTypes.SQUID_INK, hit.getX(), hit.getY(0.8D), hit.getZ(), 12, 0.3D, 0.35D, 0.3D, 0.03D);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — HADOPELAGIC TRENCH SHROUD (AURA / SELF)
    // =========================================================================

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
        // Section 6.34 Hadopelagic Trench Shroud:
        // Envelops caster in a dense cloak of silent, pitch-black deep-sea water & ink (#211A21 / #225C7C);
        // grants infinite Water Breathing, swim speed boosted by +60%, zero acoustic vibrations (100% Sculk Sensor
        // & Warden immunity), cuts mob detection range by 60%, and blinds & drenches melee attackers!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL, until);
        data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, until);
        caster.clearFire();
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 1, false, true));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(ParticleTypes.SQUID_INK, caster.getX(), caster.getY(0.8D), caster.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.03D);
    }

    // =========================================================================
    // FORM MUTATION 6 — BENTHIC MAELSTROM PITFALL (GLYPH / TRAP)
    // =========================================================================

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
        // Section 6.34 Benthic Maelstrom Pitfall:
        // Disarmable Single/Repeating hidden dark water glyph (1.0s arming delay); when tripped, opens a swirling
        // 4m whirlpool vortex that sucks entering mobs downward, pinning them helpless at the center for 3.5s (70t)
        // with continuous drowning and crushing Abyss Materia damage while blinding them with black ink!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double vortexRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), vortexRad, EssenceType.ABYSS, EssenceType.WATER, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, vortexRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.ABYSS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.ABYSS, EssenceType.WATER, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SQUID_INK,
                                zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, 1.0D, 0.08D, 1.0D, 0.01D);
                    }
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.2D, zone.center.z + 2.6D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (triggers.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 0.65D, 0.0D), zone.radius, true, EssenceType.ABYSS, EssenceType.WATER, 70, tier
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.05F, 0.60F);

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, zone.radius, 70, srv.getGameTime(),
                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sWard = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int zTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                AABB box = new AABB(
                                        sZone.center.x - sZone.radius, sZone.center.y - 1.5D, sZone.center.z - sZone.radius,
                                        sZone.center.x + sZone.radius, sZone.center.y + 3.0D, sZone.center.z + sZone.radius
                                );
                                long now = sLvl.getGameTime();

                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                                    if (v == sOwner || SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sWard)) continue;

                                    Vec3 toCenter = sZone.center.subtract(v.position());
                                    Vec3 horiz = new Vec3(toCenter.x, 0.0D, toCenter.z);
                                    Vec3 swirl = horiz.lengthSqr() > 1.0E-4D
                                            ? new Vec3(-horiz.z, 0.0D, horiz.x).normalize().scale(0.18D)
                                            : Vec3.ZERO;
                                    Vec3 suction = horiz.scale(0.32D).add(swirl);
                                    v.setDeltaMovement(suction.x, Math.min(v.getDeltaMovement().y, -0.28D), suction.z);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 6));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    SpellLingeringZoneManager.applyAbyssLandDrowning(v, 45);
                                    SpellLingeringZoneManager.applySoaked(v, 120);

                                    if (sEl % 20 == 0) {
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO,
                                                    sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F
                                            );
                                        }
                                        float crushDmg = (1.90F + zTier * 0.50F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(sLvl, v, sOwner, crushDmg, false, true);
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.SPLASH,
                                            sZone.center.x, sZone.center.y + 0.25D, sZone.center.z, 14, 1.3D, 0.2D, 1.3D, 0.06D);
                                    sLvl.sendParticles(ParticleTypes.SQUID_INK,
                                            sZone.center.x, sZone.center.y + 0.25D, sZone.center.z, 8, 1.2D, 0.2D, 1.2D, 0.02D);
                                }
                                return true;
                            }
                    ));

                    triggersLeft[0]--;
                    rearmCooldown[0] = 80;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — ABYSSAL INK-BLAST DELUGE (CONE / SPRAY)
    // =========================================================================

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
        // Section 6.34 Abyssal Ink-Blast Deluge:
        // Wide 60° cone of pressurized black ocean water & blinding cephalopod ink (#211A21 / #225C7C);
        // knocks enemies 6 blocks back, inflicts 4.0s (80t) Blindness & Slowness II, wipes mob aggro across the cone,
        // drenches struck targets, and instantly extinguishes all fires and wall torches!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.ABYSS, EssenceType.WATER, tier
        );

        float delugeDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.clearFire();
            hit.setDeltaMovement(direction.x * 1.48D, 0.35D, direction.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            SpellLingeringZoneManager.applySoaked(hit, 160);
            wipeMobAggro(hit);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
            }
        }

        int steps = Math.min(8, Math.max(2, (int) Math.ceil(coneRange)));
        for (int s = 1; s <= steps; s += 2) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            EclipseSpellMutationHandler.snuffTorchesAndFires(level, stepPos, 2);
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        level.sendParticles(ParticleTypes.SQUID_INK, mid.x, mid.y, mid.z, 24, 1.5D, 0.5D, 1.5D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, mid.x, mid.y, mid.z, 28, 1.5D, 0.5D, 1.5D, 0.08D);
    }

    // =========================================================================
    // FORM MUTATION 8 — CRUSHING TRENCH MONOLITH (SKYFALL)
    // =========================================================================

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
        // Section 6.34 Crushing Trench Monolith:
        // Calls down a colossal pillar of compacted abyssal black ice and deep-sea rock after a 2.0s (40t) telegraph;
        // crushes the impact epicenter with heavy Abyss Materia damage, detonates a black tidal wave that knocks mobs
        // 10 blocks back, and leaves a temporary pool of black water!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.ABYSS, EssenceType.WATER, 120, tier
        );

        // Falling 3D pseudoblock monolith of compacted abyssal black ice & deep-sea dark prismarine!
        int monoHeight = Math.clamp(3 + tier / 3, 3, 6);
        int baseDuration = 120;
        int effectiveDuration = SpellWorldVfxManager.resolveEffectiveZoneDurationTicks(spell, EssenceType.ABYSS, baseDuration);
        int[][] cross = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        BlockPos[] colBases = new BlockPos[cross.length];
        BlockState[] colStates = new BlockState[cross.length];
        int[] colHeights = new int[cross.length];
        for (int i = 0; i < cross.length; i++) {
            int dx = cross[i][0];
            int dz = cross[i][1];
            colBases[i] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, centerPos.offset(dx, 0, dz));
            colStates[i] = (dx == 0 && dz == 0)
                    ? Blocks.BLUE_ICE.defaultBlockState()
                    : Blocks.DARK_PRISMARINE.defaultBlockState();
            colHeights[i] = (dx == 0 && dz == 0) ? monoHeight : Math.max(2, monoHeight - 1);
            SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                    level,
                    new Vec3(colBases[i].getX() + 0.5D, colBases[i].getY() + skyH, colBases[i].getZ() + 0.5D),
                    colBases[i],
                    colStates[i],
                    colHeights[i],
                    40,
                    Math.max(4, effectiveDuration - 40),
                    EssenceType.ABYSS,
                    tier
            );
        }

        final boolean[] materialized = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, baseDuration, level.getGameTime(), spell, slot, EssenceType.ABYSS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int i = 0; i < cross.length; i++) {
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, colBases[i], colStates[i], colHeights[i]);
                            }
                        }
                        return false;
                    }
                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            srv.sendParticles(ParticleTypes.SQUID_INK,
                                    zone.center.x, zone.center.y + 6.5D - elapsed * 0.14D, zone.center.z,
                                    12, 0.8D, 0.8D, 0.8D, 0.02D);
                            srv.sendParticles(ParticleTypes.DRIPPING_WATER,
                                    zone.center.x, zone.center.y + 6.0D - elapsed * 0.12D, zone.center.z,
                                    10, 0.9D, 0.5D, 0.9D, 0.02D);
                        }
                        return true;
                    }
                    for (int i = 0; i < cross.length; i++) {
                        SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, colBases[i], colHeights[i], elapsed == 40);
                    }
                    if (elapsed == 40) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                        SpellWorldVfxManager.broadcastRadialNova(
                                srv, zone.center, zone.radius, EssenceType.ABYSS, EssenceType.WATER, zTier
                        );

                        AABB box = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.5D, zone.center.z + zone.radius
                        );
                        float monolithDmg = (6.80F + zTier * 1.65F) * Math.max(0.5F, zone.apparatusPotency);

                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            Vec3 delta = foe.position().subtract(zone.center);
                            boolean inEpicenter = delta.horizontalDistanceSqr() <= 12.25D;
                            Vec3 tidal = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(2.20D) : new Vec3(0.0D, 0.55D, 0.0D);
                            foe.setDeltaMovement(tidal.x, 0.54D, tidal.z);
                            foe.hasImpulse = true;
                            foe.clearFire();
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                            SpellLingeringZoneManager.applySoaked(foe, 200);
                            SpellLingeringZoneManager.applyAbyssLandDrowning(foe, 80);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * (inEpicenter ? 1.35F : 0.85F)
                                );
                            }
                            float impactDmg = monolithDmg * (inEpicenter ? 1.25F : 0.75F);
                            SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(srv, foe, owner, impactDmg, false, true);
                        }

                        BlockPos cPos = BlockPos.containing(zone.center);
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 1.0D, zone.center.z,
                                42, 2.2D, 0.8D, 2.2D, 0.07D);
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 1.0D, zone.center.z,
                                52, 2.5D, 0.8D, 2.5D, 0.12D);
                        srv.playSound(null, cPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.3F, 0.55F);
                    } else if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 0.25D, zone.center.z,
                                8, 1.2D, 0.15D, 1.2D, 0.02D);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — KRAKEN-HUSK CONSTRUCT SUITE (SUMMON)
    // =========================================================================

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
        tag.putString("SpellEidolonMateria", EssenceType.ABYSS.name());
        tag.putInt("SpellEidolonTintRGB", KRAKEN_HUSK_HEX_COLOR);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.45D, EssenceType.ABYSS, EssenceType.WATER, 36, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.SQUID_INK, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.45D, 0.5D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, spawnPos.x, spawnPos.y + 0.6D, spawnPos.z, 24, 0.5D, 0.4D, 0.5D, 0.08D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.34 Benthic Trench Slip:
        // Silently teleports up to 14 blocks into liquid or shadow with zero acoustic sound or particles,
        // dropping all active mob aggro completely!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 60L);
        caster.clearFire();
        caster.setLastHurtByMob(null);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(24.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            if (mob.getLastHurtByMob() == caster) {
                mob.setLastHurtByMob(null);
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.34 Hadopelagic Erosion & Darkening Transmute:
        // Washes sand into clay, compacts gravel into cobblestone, cools lava into crying obsidian/obsidian,
        // turns glowstone/sea lanterns into unlit dark prismarine, and erodes smooth stone into cracked variants!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        BlockState eroded = null;
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            eroded = Blocks.CLAY.defaultBlockState();
        } else if (state.is(Blocks.GRAVEL)) {
            eroded = Blocks.COBBLESTONE.defaultBlockState();
        } else if (state.is(Blocks.LAVA)) {
            eroded = level.getFluidState(pos).isSource()
                    ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : Blocks.OBSIDIAN.defaultBlockState();
        } else if (state.is(Blocks.GLOWSTONE) || state.is(Blocks.SEA_LANTERN) || state.is(Blocks.SHROOMLIGHT)) {
            eroded = Blocks.DARK_PRISMARINE.defaultBlockState();
        } else if (state.is(Blocks.SMOOTH_STONE) || state.is(Blocks.STONE) || state.is(Blocks.STONE_BRICKS)) {
            eroded = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        } else if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.POLISHED_DEEPSLATE) || state.is(Blocks.DEEPSLATE_BRICKS)) {
            eroded = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        } else if (state.is(Blocks.NETHER_BRICKS)) {
            eroded = Blocks.CRACKED_NETHER_BRICKS.defaultBlockState();
        } else if (state.is(Blocks.POLISHED_BLACKSTONE_BRICKS)) {
            eroded = Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }

        if (eroded != null) {
            level.setBlockAndUpdate(pos, eroded);
            level.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                    6, 0.25D, 0.25D, 0.25D, 0.02D);
            maybeGrantAbyssBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;

        ItemStack out = ItemStack.EMPTY;
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            out = new ItemStack(Items.CLAY, in.getCount());
        } else if (in.is(Items.GRAVEL)) {
            out = new ItemStack(Items.COBBLESTONE, in.getCount());
        } else if (in.is(Items.GLOWSTONE) || in.is(Items.SEA_LANTERN) || in.is(Items.SHROOMLIGHT)) {
            out = new ItemStack(Items.DARK_PRISMARINE, in.getCount());
        } else if (in.is(Items.SMOOTH_STONE) || in.is(Items.STONE) || in.is(Items.STONE_BRICKS)) {
            out = new ItemStack(Items.CRACKED_STONE_BRICKS, in.getCount());
        } else if (in.is(Items.DEEPSLATE) || in.is(Items.POLISHED_DEEPSLATE) || in.is(Items.DEEPSLATE_BRICKS)) {
            out = new ItemStack(Items.CRACKED_DEEPSLATE_BRICKS, in.getCount());
        } else if (in.is(Items.LAVA_BUCKET)) {
            out = new ItemStack(Items.CRYING_OBSIDIAN, in.getCount());
            ItemEntity emptyBucket = new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY(0.2D), itemEntity.getZ(),
                    new ItemStack(Items.BUCKET, in.getCount())
            );
            level.addFreshEntity(emptyBucket);
        }

        if (!out.isEmpty()) {
            itemEntity.setItem(out);
            level.sendParticles(ParticleTypes.SQUID_INK, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    6, 0.2D, 0.2D, 0.2D, 0.02D);
            maybeGrantAbyssBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.34 Crushing Benthic Drowning Hex:
        // Persistent milk-immune deep-sea hydrostatic curse (cleansed via Restoration/Vitae magic, Radiant light,
        // or caster defeat): permanently inflicts continuous drowning damage on land, cuts movement speed by 40%,
        // silences mob abilities, and doubles all incoming Lightning damage (plus Hadopelagic Helplessness 2.0x
        // aquatic/abyssal monster damage & +50% swim speed on afflicted players)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellLingeringZoneManager.applyAbyssLandDrowning(target, 45);
        SpellLingeringZoneManager.applyArcaneSilence(target, 45);
        SpellLingeringZoneManager.applySoaked(target, 60);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.40D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(4, Math.max(1, Math.round(1.5F * mult))), true, true));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.40F + potencyTier * 0.46F) * mult;
        SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.8D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellCoreEffect effect,
            int potencyTier,
            SpellAssembly spell
    ) {
        if (effect == SpellCoreEffect.RESTORATION) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            float heal = 3.1F + potencyTier * 0.92F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void wipeMobAggro(LivingEntity entity) {
        entity.setLastHurtByMob(null);
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.setAggressive(false);
        }
    }

    private static void extinguishOpenFiresOnly(ServerLevel level, BlockPos center, int radius) {
        int r = Math.min(8, Math.max(1, radius));
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 3, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            }
        }
    }

    private static void maybeGrantAbyssBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellAbyssBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellAbyssBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellAbyssBatchTick", now);
        pData.putInt("SpellAbyssBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.ABYSS, refund);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -5; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty())
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return start.below();
    }
}
