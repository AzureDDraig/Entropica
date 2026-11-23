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
import net.minecraft.world.entity.Mob;
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
 * Per-part Signature Mutation implementation for <b>Umbral Water Materia (Caprine Secret Fusion)</b>
 * (Umbral {@code #211A21} + Water {@code #14508C} — Dissolving Abyssal Cavitation &amp; Thermal-Void Primer).
 */
public final class UmbralWaterSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int UMBRAL_SHADOW_VIOLET_HEX = 0x211A21;
    public static final int ABYSSAL_CAVITATION_AZURE_HEX = 0x14508C;

    public static final String TAG_UMBRAL_WATER_DRENCH_UNTIL = "SpellUmbralWaterDrenchUntil";
    private static final String TAG_UW_ORB_IMPLODED = "SpellUmbralWaterOrbImploded";
    private static final String TAG_UW_BEAM_LAST_TICK = "SpellUmbralWaterBeamLastTick";
    private static final String TAG_UW_BEAM_TICKS = "SpellUmbralWaterBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.UMBRAL_WATER;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Pressurized Cavitation Undertow Flow";
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE -> "Bifurcating Abyssal Riptide Flow";
            case ORBIT_SATELLITE -> "Umbral Waters Cavitation Orbit Flow";
            default -> SpellMutationRegistry.getEpithet(EssenceType.UMBRAL_WATER) + " " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Increases hydrostatic mass (+0.4 Materia Advantage vs soaked targets) and drenches targets in Abyssal Drench.";
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE -> "Splits spell currents into silent dark-water eddies that converge on targets.";
            case ORBIT_SATELLITE -> "Sustains orbiting dark-water cavitation spheres that douse fire and slow melee attackers.";
            default -> "Infuses spell flow with Caprine Umbral Waters Materia.";
        };
    }

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        return flow == SpellFlowModifier.SLOW_HEAVY ? 0.4F : 0.0F;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Umbral Water Vortex Orb";
            case BEAM -> "Abyssal Cavitation Torrent";
            case RADIAL_BURST -> "Umbral Undertow Shockwave";
            case TOUCH_STRIKE -> "Cavitation Undertow Cleave";
            case AURA -> "Umbral Waters Shroud";
            case GLYPH -> "Abyssal Cavitation Whirlpool Sigil";
            case CONE -> "Umbral Tide Cavitation Spray";
            case SKYFALL -> "Abyssal Cavitation Monsoon";
            case SUMMON_AGENT -> "Cavitation Trench-Stalker";
            case SUMMON_SENTRY -> "Umbral Undertow Spire";
            case SUMMON_DECOY -> "Submerged Shadow Effigy";
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Launches an Umbral Water Vortex that draws nearby enemies inward, inflicts Abyssal Drench, and primes targets for 2.5x Steam-Cavitation.";
            case BEAM -> "Channels a pressurized dark-water stream that slows targets by 60%, strips armor, and triggers a Cavitation Collapse at 3.0s.";
            case RADIAL_BURST -> "Erupts in a silent, inward-pulling dark tide that drenches hostiles, blinds foes, and clears mob aggro.";
            case TOUCH_STRIKE -> "Sweeps a dissolving shadow-water blade that shreds 3 armor points, drenches foes, and cloaks the caster briefly.";
            case AURA -> "Envelops the caster in silent Umbral Waters granting Water Breathing, swim speed, and Sculk vibration suppression.";
            case GLYPH -> "Inscribes a hidden whirlpool sigil that pulls entering hostiles inward and drenches them in Abyssal Waters.";
            case CONE -> "Sprays a 60-degree pressurized cavitation wave that extinguishes flames, blinds enemies, and applies Abyssal Drench.";
            case SKYFALL -> "Calls down a 12-second dark cavitation monsoon that continuously pulls and drenches hostiles across the impact zone.";
            case SUMMON_AGENT -> "Summons an autonomous Cavitation Trench-Stalker that drags foes inward and applies Abyssal Drench.";
            case SUMMON_SENTRY -> "Summons an anchored Umbral Undertow Spire projecting pulling cavitation vortices.";
            case SUMMON_DECOY -> "Manifests a Submerged Shadow Effigy that taunts hostiles before imploding in a blinding dark-water vortex.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Umbral Undertow Slip";
            case TRANSMUTE, BREAK -> "Caprine Abyssal Dissolution";
            case AFFLICTION -> "Abyssal Cavitation Dissolution Hex";
            default -> "Abyssal Cavitation " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Silently slips through dark water currents, shedding hostile mob aggro and suppressing acoustic vibrations.";
            case TRANSMUTE, BREAK -> "Dissolves stone into deepslate, cools lava into crying obsidian, and saturates sand into clay.";
            case AFFLICTION -> "Submerges the victim in a milk-immune Abyssal Cavitation Hex that blinds, slows, shreds 3 armor, and primes Thermal-Void Shock.";
            default -> "Infuses the effect with dissolving Caprine Umbral Water Materia.";
        };
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), ABYSSAL_CAVITATION_AZURE_HEX, 15, 0.72F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), UMBRAL_SHADOW_VIOLET_HEX, 15, 0.68F, 0.03F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
        }
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.02D);
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
        implodeVortexOnce(level, projectile, caster, hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.4D, 0.0D), spell);
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
        implodeVortexOnce(level, projectile, caster, blockHit.getLocation(), spell);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        implodeVortexOnce(level, projectile, caster, projectile.position(), spell);
    }

    private static void implodeVortexOnce(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_UW_ORB_IMPLODED).orElse(false)) return;
        pTag.putBoolean(TAG_UW_ORB_IMPLODED, true);

        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.add(0.0D, 0.5D, 0.0D), 3.4D, true, EssenceType.UMBRAL_WATER, EssenceType.WATER, 18, tier
        );
        AABB box = new AABB(center.x - 3.5D, center.y - 2.0D, center.z - 3.5D, center.x + 3.5D, center.y + 3.0D, center.z + 3.5D);
        float vortexDmg = 3.15F + tier * 0.82F;

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 pull = center.subtract(foe.position());
            Vec3 vel = pull.lengthSqr() > 1.0E-4D ? pull.normalize().scale(0.95D) : Vec3.ZERO;
            foe.setDeltaMovement(vel.x, 0.18D, vel.z);
            foe.hasImpulse = true;
            applyAbyssalDrenchAndCavitationCheck(level, foe, caster, tier, vortexDmg);
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.95F, 0.72F);
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
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.clearFire();
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 60L);
            applyAbyssalDrenchAndCavitationCheck(level, hit, caster, tier, !slot.hasHarmVerb()
                    ? (1.45F + tier * 0.40F) * Math.max(0.5F, apparatusPotency) : 0.0F);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_UW_BEAM_LAST_TICK).orElse(-100L);
            if (now == last) continue;
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_UW_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_UW_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_UW_BEAM_TICKS, 0);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 1.0D, 120L);
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
                float collapseDmg = (3.60F + tier * 0.90F) * Math.max(0.5F, apparatusPotency);
                hurtWithUmbralWaterAdvantage(level, hit, caster, collapseDmg);
                SpellWorldVfxManager.broadcastVacuumSphere(level, hit.position().add(0.0D, 0.6D, 0.0D), 3.2D, true,
                        EssenceType.UMBRAL_WATER, EssenceType.UMBRAL, 16, tier);
            } else {
                data.putInt(TAG_UW_BEAM_TICKS, contactTicks);
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
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.UMBRAL_WATER, EssenceType.WATER, tier);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 4.0D, center.z + r);
        float burstDmg = (3.20F + tier * 0.82F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 pull = center.subtract(foe.position());
            Vec3 vel = pull.lengthSqr() > 1.0E-4D ? pull.normalize().scale(0.85D) : Vec3.ZERO;
            foe.setDeltaMovement(vel.x, 0.22D, vel.z);
            foe.hasImpulse = true;
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }
            applyAbyssalDrenchAndCavitationCheck(level, foe, caster, tier, !slot.hasHarmVerb() ? burstDmg : (burstDmg * 0.35F));
        }
        caster.clearFire();
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
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.2D,
                EssenceType.UMBRAL_WATER, EssenceType.UMBRAL, tier
        );
        float strikeDmg = (3.05F + tier * 0.78F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
            if (hit instanceof Mob mob) {
                mob.setTarget(null);
            }
            applyAbyssalDrenchAndCavitationCheck(level, hit, caster, tier, !slot.hasHarmVerb() ? strikeDmg : (strikeDmg * 0.35F));
        }
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 50, 0, false, false));
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
        int dur = 200 + spell.getEffectiveTier() * 40;
        caster.clearFire();
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, true));
        level.sendParticles(ParticleTypes.SQUID_INK, caster.getX(), caster.getY(0.8D), caster.getZ(), 14, 0.45D, 0.5D, 0.45D, 0.03D);
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
        double r = Math.max(3.5D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] triggersLeft = {repeating ? 3 : 1};
        final int[] cooldown = {0};
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, glyphCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.UMBRAL_WATER, EssenceType.WATER, 240, tier
        );

        SpellLingeringZoneManager.ActiveSpellZone glyph = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, r, maxDuration, level.getGameTime(), spell, slot, EssenceType.UMBRAL_WATER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed < 20) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB box = new AABB(zone.center.x - 2.6D, zone.center.y - 1.2D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.4D, zone.center.z + 2.6D);
                    boolean triggered = false;
                    for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                        if (e == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) continue;
                        triggered = true;
                        break;
                    }
                    if (!triggered) return true;

                    SpellWorldVfxManager.broadcastVacuumSphere(srv, zone.center.add(0.0D, 0.5D, 0.0D), Math.max(3.8D, zone.radius),
                            true, EssenceType.UMBRAL_WATER, EssenceType.WATER, 20, tier);
                    float trapDmg = (3.30F + tier * 0.82F) * Math.max(0.5F, zone.apparatusPotency);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box.inflate(1.8D), LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        Vec3 pull = zone.center.subtract(foe.position());
                        Vec3 vel = pull.lengthSqr() > 1.0E-4D ? pull.normalize().scale(0.9D) : Vec3.ZERO;
                        foe.setDeltaMovement(vel.x, 0.15D, vel.z);
                        foe.hasImpulse = true;
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 3.0D, 0.0D, 120L);
                        applyAbyssalDrenchAndCavitationCheck(srv, foe, owner, tier, trapDmg);
                    }
                    triggersLeft[0]--;
                    cooldown[0] = 40;
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
                EssenceType.UMBRAL_WATER, EssenceType.WATER, tier
        );
        float coneDmg = (2.95F + tier * 0.76F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.setDeltaMovement(direction.x * 1.12D, 0.26D, direction.z * 1.12D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 100L);
            applyAbyssalDrenchAndCavitationCheck(level, hit, caster, tier, !slot.hasHarmVerb() ? coneDmg : (coneDmg * 0.35F));
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
        double r = Math.max(12.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.UMBRAL_WATER, EssenceType.WATER, 240, tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 240, level.getGameTime(), spell, slot, EssenceType.UMBRAL_WATER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 3.5D, zone.center.z,
                                14, zone.radius * 0.48D, 1.0D, zone.radius * 0.48D, 0.04D);
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 1.0D, zone.center.z,
                                20, zone.radius * 0.5D, 0.5D, zone.radius * 0.5D, 0.06D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        float tickDmg = (2.35F + tier * 0.62F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                            Vec3 pull = zone.center.subtract(foe.position());
                            if (pull.lengthSqr() > 1.0E-4D) {
                                Vec3 drag = pull.normalize().scale(0.45D);
                                foe.setDeltaMovement(foe.getDeltaMovement().add(drag.x, 0.0D, drag.z));
                                foe.hasImpulse = true;
                            }
                            applyAbyssalDrenchAndCavitationCheck(srv, foe, owner, tier, tickDmg);
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
        tag.putString("SpellEidolonMateria", EssenceType.UMBRAL_WATER.name());
        tag.putInt("SpellEidolonTintRGB", ABYSSAL_CAVITATION_AZURE_HEX);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.5D, EssenceType.UMBRAL_WATER, EssenceType.WATER, 36, spell.getEffectiveTier()
        );
    }

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 80L);
        caster.clearFire();
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.UMBRAL_WATER, potencyTier);
        AABB box = new AABB(departurePos, departurePos).inflate(12.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, m -> m.getTarget() == caster)) {
            mob.setTarget(null);
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        BlockState out = null;
        if (state.is(Blocks.LAVA)) {
            out = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        } else if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) {
            out = Blocks.DEEPSLATE.defaultBlockState();
        } else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            out = Blocks.CLAY.defaultBlockState();
        } else if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) {
            out = Blocks.MUD.defaultBlockState();
        } else if (state.is(Blocks.GLOWSTONE)) {
            out = Blocks.DARK_PRISMARINE.defaultBlockState();
        }
        if (out != null) {
            level.setBlockAndUpdate(pos, out);
            level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D, 10, 0.25D, 0.25D, 0.25D, 0.04D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        ItemStack out = ItemStack.EMPTY;
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            out = new ItemStack(Items.CLAY_BALL, Math.min(64, stack.getCount() * 2));
        } else if (stack.is(Items.COBBLESTONE) || stack.is(Items.STONE)) {
            out = new ItemStack(Items.DEEPSLATE, stack.getCount());
        } else if (stack.is(Items.TORCH)) {
            out = new ItemStack(Items.SOUL_TORCH, stack.getCount());
        }
        if (!out.isEmpty()) {
            itemEntity.setItem(out);
            level.sendParticles(ParticleTypes.SQUID_INK, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.clearFire();
        EntityHelper.getPersistentData(target).putLong(TAG_UMBRAL_WATER_DRENCH_UNTIL, level.getGameTime() + 60L);
        SpellLingeringZoneManager.applySoaked(target, 60);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.55F + potencyTier * 0.52F) * mult;
        hurtWithUmbralWaterAdvantage(level, target, hexCaster, hexDmg);
        return true;
    }

    static void applyAbyssalDrenchAndCavitationCheck(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            int tier,
            float directDamage
    ) {
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);
        target.clearFire();
        data.putLong(TAG_UMBRAL_WATER_DRENCH_UNTIL, now + 160L);
        SpellLingeringZoneManager.applySoaked(target, 160);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));

        long sunfireSearUntil = data.getLong(RadiantFireSpellMutationHandler.TAG_RADIANT_FIRE_SEAR_UNTIL).orElse(0L);
        if (sunfireSearUntil >= now) {
            // Inverse Caprine Thermal-Void Shock: quenching Radiant Fire with Umbral Waters triggers 2.5x Steam-Cavitation!
            data.remove(RadiantFireSpellMutationHandler.TAG_RADIANT_FIRE_SEAR_UNTIL);
            float cavitationDmg = Math.max(4.0F, Math.max(2.5F, directDamage) * 2.5F);
            SpellWorldVfxManager.broadcastRadialNova(level, target.position(), 4.0D, EssenceType.EQUINOX, EssenceType.UMBRAL_WATER, tier);
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.7D), target.getZ(), 24, 1.2D, 0.6D, 1.2D, 0.08D);
            hurtWithUmbralWaterAdvantage(level, target, caster, cavitationDmg);
            return;
        }

        if (directDamage > 0.0F) {
            hurtWithUmbralWaterAdvantage(level, target, caster, directDamage);
        }
    }

    static float hurtWithUmbralWaterAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) return 0.0F;
        Set<EssenceType> defenderAffs = SpellCombatReactionEngine.resolveEntityAffinities(target);
        boolean mobToPlayer = (target instanceof Player) && !(caster instanceof Player);
        float mult = VisInteractionManager.getInstance().calculateDamageMultiplier(
                EssenceType.UMBRAL_WATER, defenderAffs, mobToPlayer, 0.0F
        );
        if (mult <= 0.0F) return 0.0F;
        float finalDmg = Math.max(0.5F, baseDamage * mult);
        target.invulnerableTime = 0;
        DamageSource src = caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic();
        return SpellCombatReactionEngine.hurtWithPureMateria(level, target, src, finalDmg) ? finalDmg : 0.0F;
    }
}
