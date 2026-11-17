package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Air Materia (Base Type #1)</b>
 * (Spellcrafting System Guide Section 6.2):
 * <ul>
 *     <li><b>Flow (Chains)</b>: <i>Sonic Leap Chains</i> — instant wind-bridge arcs dragging chained targets forcefully together.</li>
 *     <li><b>Projectile</b>: <i>Turbulence Spiral</i> — 3D corkscrew wind-lance deflecting incoming hostile projectiles along flight.</li>
 *     <li><b>Beam</b>: <i>Jetstream Propulsion</i> — wind-tunnel knockback + continuous vertical jetpack lifter when aimed downward.</li>
 *     <li><b>Radial Burst</b>: <i>Gust Shield</i> — 3D spherical wind barrier pushing foes back, halving projectile damage, +30% caster speed.</li>
 *     <li><b>Touch / Strike</b>: <i>Concussive Uppercut</i> — launches struck foes 8 blocks straight into the air for full fall impact.</li>
 *     <li><b>Aura / Self</b>: <i>Slipstream Shield</i> — sprinting charges a 3D wind-cone barrier bowling colliding mobs aside with wind-shear.</li>
 *     <li><b>Glyph / Trap</b>: <i>Geyser Mine (Air Trampoline)</i> — 1s arm, 10m/indefinite persistence, 50% disarm refund; launches 14 blocks skyward.</li>
 *     <li><b>Cone / Spray</b>: <i>Howling Gale</i> — 60° hurricane cone pushing mobs 8 blocks back, extinguishing fires/candles, deflecting projectiles.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Tornado Vortex</i> — 5s 3D indoor-viable funnel pulling mobs into its spinning eye before flinging them outward.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Zephyr Eidolon</i> — golden-buff (#A88654) Agent, Sentry, or Decoy construct.</li>
 *     <li><b>Blink / Step</b>: <i>Vacuum Implosion</i> — leaves an inward-collapsing 3D vacuum sphere at the departure spot sucking foes together.</li>
 *     <li><b>Transmute</b>: <i>Aeration & Dehydration</i> — dries wet sponges, turns mud into clay, blows away cobwebs & leaves.</li>
 *     <li><b>Affliction</b>: <i>Hypoxia Suffocation Hex</i> — persistent anti-milk respiratory curse cutting attack speed by 40% & suffocating on land.</li>
 * </ul>
 */
public final class AirSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ZEPHYR_EIDOLON_HEX_COLOR = 0xA88654;
    private static final int AIR_SLIPSTREAM_GOLD_RGB = 0xCFA86E;
    private static final int AIR_SLIPSTREAM_WHITE_RGB = 0xEEF9FF;

    @Override
    public EssenceType materiaType() {
        return EssenceType.AIR;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.2)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Sonic Leap Chains";
            case FAST_DART -> "Supersonic Slipstream Dart";
            case ORBIT_SATELLITE -> "Cyclone Satellite Ring";
            default -> "Zephyr Gale " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Sonic Leap Chains: Instant vacuum wind-bridges drag chained enemies forcefully toward their midpoint.";
            case FAST_DART -> "Supersonic Slipstream Dart: Compresses wind around the payload for +30% flight speed and razor-straight stability.";
            case ORBIT_SATELLITE -> "Cyclone Satellite Ring: Orbits swirling golden-ivory wind blades that slice and repel encroaching foes.";
            default -> "Zephyr Gale Flow: Accelerates spell transit by +30% along pressurized slipstream currents.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Turbulence Spiral";
            case BEAM -> "Jetstream Propulsion";
            case RADIAL_BURST -> "Gust Shield";
            case TOUCH_STRIKE -> "Concussive Uppercut";
            case AURA -> "Slipstream Shield";
            case GLYPH -> "Geyser Mine (Air Trampoline)";
            case CONE -> "Howling Gale";
            case SKYFALL -> "Tornado Vortex";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Turbulence Spiral: 3D corkscrew wind-lance that deflects incoming hostile arrows and projectiles along its path.";
            case BEAM -> "Jetstream Propulsion: Wind-tunnel beam knocking foes back; aiming downward lifts and hovers you like a vertical jetpack.";
            case RADIAL_BURST -> "Gust Shield: Spherical wind barrier pushing foes away, halving projectile damage, and granting +30% movement speed.";
            case TOUCH_STRIKE -> "Concussive Uppercut: Launches struck foes 8 blocks straight into the air so they suffer full fall impact on landing.";
            case AURA -> "Slipstream Shield: Agile wind veil; sprinting charges a ramming air-barrier that bowls colliding enemies aside.";
            case GLYPH -> "Geyser Mine: Arms in 1s, lasts 10m (indefinite at Tier 5+); launches entities within 3m 14 blocks skyward.";
            case CONE -> "Howling Gale: 60° hurricane blast pushing foes 8m back, extinguishing fires/campfires, and blowing projectiles away.";
            case SKYFALL -> "Tornado Vortex: 5s 3D twisting funnel (works indoors) pulling mobs into its spinning eye before flinging them outward.";
            case SUMMON_AGENT -> "Zephyr Agent: Swift golden-buff (#A88654) wind-blade hunter that juggles targets upward with cyclone slashes.";
            case SUMMON_SENTRY -> "Zephyr Sentry: Anchored golden-buff (#A88654) wind pylon pulsing 6m updraft gusts and rapid spiral wind-bolts.";
            case SUMMON_DECOY -> "Zephyr Decoy: Shimmering golden-buff (#A88654) cyclone effigy taunting foes within 12m before bursting outward.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Vacuum Implosion";
            case TRANSMUTE, BREAK -> "Aeration & Dehydration";
            case AFFLICTION -> "Hypoxia Suffocation Hex";
            case HARM -> "Zephyr Wind-Shear";
            case KINETIC, FLIGHT, GRAPPLE -> "Aerodynamic Updraft";
            case WARD -> "Slipstream Wind-Wall";
            case RESTORATION -> "Second Wind Breath";
            default -> "Zephyr " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Vacuum Implosion: Blinking leaves a vacuum pop at your departure spot that sucks adjacent foes inward.";
            case TRANSMUTE, BREAK -> "Aeration & Dehydration: Instantly dries wet sponges, dehydrates mud into clay, and blows away cobwebs and leaves.";
            case AFFLICTION -> "Hypoxia Suffocation Hex: Anti-milk curse cutting attack speed by 40% and suffocating targets on dry land.";
            case HARM -> "Zephyr Wind-Shear: Pure Air Materia blades (+1 vs Earth, -1 vs Void); triggers Atmospheric Eruption vs Earth-hexed foes.";
            case KINETIC, FLIGHT, GRAPPLE -> "Aerodynamic Updraft: Amplifies vertical lift, aerial glide control, and slipstream displacement velocity.";
            case WARD -> "Slipstream Wind-Wall: Wraps the Arcane Ward in a swirling air cushion that halves incoming projectile damage.";
            case RESTORATION -> "Second Wind Breath: Restores health while immediately refilling the target's full oxygen supply.";
            default -> "Zephyr Attunement: Infuses the incantation with crisp golden-buff atmospheric currents.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS
    // =========================================================================

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Sonic Leap Chains: instant vacuum wind bridge dragging both chained targets forcefully toward their midpoint!
        Vec3 p1 = fromTarget.getEyePosition();
        Vec3 p2 = toTarget.getEyePosition();
        SpellWorldVfxManager.broadcastChainArc(level, p1, p2, EssenceType.AIR, null, spell.getEffectiveTier());

        double dist = p1.distanceTo(p2);
        if (dist > 0.25D) {
            Vec3 stepVec = p2.subtract(p1).normalize();
            TimedTintableParticleOption vortexOpt = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_VORTEX.get(), AIR_SLIPSTREAM_WHITE_RGB, 16, 0.72F, 0.015F
            );
            for (double d = 0.0D; d <= dist; d += 0.60D) {
                Vec3 pt = p1.add(stepVec.scale(d));
                level.sendParticles(vortexOpt, pt.x, pt.y, pt.z, 1, 0.04D, 0.04D, 0.04D, 0.01D);
            }
        }

        Vec3 midpoint = fromTarget.position().add(toTarget.position()).scale(0.5D);
        if (!SpellCombatReactionEngine.isBossEntity(level, fromTarget)) {
            pullEntityToward(fromTarget, midpoint, 0.72D, 0.28D);
        }
        if (!SpellCombatReactionEngine.isBossEntity(level, toTarget)) {
            pullEntityToward(toTarget, midpoint, 0.72D, 0.28D);
        }
        level.playSound(null, BlockPos.containing(midpoint), SoundEvents.BREEZE_IDLE_AIR, SoundSource.PLAYERS, 0.75F, 1.55F);
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 FORMS)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Turbulence Spiral: 60fps 3D corkscrew spear rendered by ARCHETYPE_WIND_SPIRAL + live projectile deflection!
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 0.0025D) {
            Vec3 dir = vel.normalize();
            Vec3 upRef = Math.abs(dir.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = dir.cross(upRef).normalize();
            Vec3 orthoUp = right.cross(dir).normalize();

            double angle = projectile.tickCount * 0.95D;
            Vec3 spiralOffset = right.scale(Math.cos(angle) * 0.28D).add(orthoUp.scale(Math.sin(angle) * 0.28D));
            TimedTintableParticleOption slipOpt = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_VORTEX.get(),
                    (projectile.tickCount & 1) == 0 ? AIR_SLIPSTREAM_GOLD_RGB : AIR_SLIPSTREAM_WHITE_RGB,
                    14,
                    0.68F,
                    0.01F
            );
            level.sendParticles(
                    slipOpt,
                    projectile.getX() + spiralOffset.x,
                    projectile.getY() + spiralOffset.y,
                    projectile.getZ() + spiralOffset.z,
                    1,
                    0.01D, 0.01D, 0.01D, 0.0D
            );
        }

        // Deflect incoming hostile arrows, tridents, fireballs, and bolts along the slipstream!
        AABB slipstream = projectile.getBoundingBox().inflate(2.6D);
        for (Projectile other : level.getEntitiesOfClass(Projectile.class, slipstream)) {
            if (other == projectile || other.getOwner() == projectile.getOwner()) continue;
            Vec3 delta = other.position().subtract(projectile.position());
            Vec3 pushDir = delta.lengthSqr() > 1.0E-4D ? delta.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
            double deflectSpeed = Math.max(1.25D, other.getDeltaMovement().length() * 1.15D);
            SpellExecutionEngine.applySyncedMotion(
                    other,
                    new Vec3(pushDir.x * deflectSpeed, Math.max(0.28D, pushDir.y * deflectSpeed), pushDir.z * deflectSpeed)
            );
            level.sendParticles(ParticleTypes.POOF, other.getX(), other.getY(), other.getZ(), 3, 0.1D, 0.1D, 0.1D, 0.03D);
        }
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
        // Jetstream Propulsion: wind-tunnel knockback on struck foes + vertical jetpack lifter when aimed downward!
        int tier = spell.getEffectiveTier();
        boolean channeledPulse = apparatusPotency <= 0.45F;
        double pushStrength = channeledPulse ? (0.36D + tier * 0.055D) : (0.88D + (tier * 0.14D));
        double pushLiftY = channeledPulse ? 0.18D : 0.32D;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isBossEntity(level, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            SpellExecutionEngine.applySyncedMotion(
                    hit,
                    new Vec3(direction.x * pushStrength, Math.max(pushLiftY, direction.y * pushStrength + pushLiftY * 0.75D), direction.z * pushStrength)
            );
        }

        if (direction.y < -0.50D) {
            double liftY = channeledPulse ? (0.50D + tier * 0.045D) : (0.92D + (tier * 0.08D));
            double strafeScale = channeledPulse ? 0.18D : 0.32D;
            Vec3 cur = caster.getDeltaMovement();
            SpellExecutionEngine.applySyncedMotion(
                    caster,
                    new Vec3(cur.x * 0.85D - direction.x * strafeScale, Math.max(cur.y, liftY), cur.z * 0.85D - direction.z * strafeScale)
            );
            caster.resetFallDistance();
            if (!channeledPulse || level.getGameTime() % 8L == 0L) {
                SpellWorldVfxManager.broadcastArcaneSeal(
                        level, caster.position(), 1.65D, EssenceType.AIR, null, 14, tier
                );
                level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, caster.getX(), caster.getY() + 0.1D, caster.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS,
                channeledPulse ? 0.32F : 0.70F, channeledPulse ? 1.45F : 1.35F);
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
        // Concussive Uppercut: launch struck targets 8 blocks straight into the air (vy = 1.28 -> ~8.15m peak height)!
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isBossEntity(level, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            Vec3 cur = hit.getDeltaMovement();
            SpellExecutionEngine.applySyncedMotion(
                    hit,
                    new Vec3(cur.x * 0.30D + direction.x * 0.16D, 1.28D, cur.z * 0.30D + direction.z * 0.16D)
            );
            SpellWorldVfxManager.broadcastGeyserPlume(
                    level, hit.position(), 1.15D, 8.0D, EssenceType.AIR, null, 44, tier
            );
            level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, hit.getX(), hit.getY() + 0.2D, hit.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (!hitEntities.isEmpty()) {
            level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 1.15F);
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
        // Gust Shield: outward 3D spherical wind barrier pushing mobs back, 50% projectile DR, and exact +30% caster speed!
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center, radius, false, EssenceType.AIR, null, 60, tier
        );

        AABB area = new AABB(center.x - radius, center.y - 2.0D, center.z - radius,
                center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity == caster || !entity.isAlive() || SpellCombatReactionEngine.isBossEntity(level, entity)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 diff = entity.position().subtract(center);
            Vec3 away = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(1.0D, 0.0D, 0.0D)).scale(1.45D);
            SpellExecutionEngine.applySyncedMotion(entity, new Vec3(away.x, 0.45D, away.z));
        }

        // Deflect any mid-air projectiles currently inside the sphere
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, area)) {
            if (proj.getOwner() == caster) continue;
            Vec3 diff = proj.position().subtract(center);
            Vec3 away = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0.0D, 1.0D, 0.0D)).scale(1.5D);
            SpellExecutionEngine.applySyncedMotion(proj, new Vec3(away.x, Math.max(0.35D, away.y), away.z));
        }

        long durationTicks = 120L + (tier * 20L);
        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        casterData.putLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL, level.getGameTime() + durationTicks);
        // Exact +30% movement speed modifier per Section 6.2!
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 0.0D, 0.0D, 0.30D, durationTicks);

        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, center.x, center.y + 0.8D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
        // Slipstream Shield: sprinting builds a charging 3D wind-cone barrier that bowls colliding mobs aside!
        int tier = spell.getEffectiveTier();
        long durationTicks = 160L + (tier * 30L);
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        cData.putLong(SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL, level.getGameTime() + durationTicks);
        cData.putInt("SpellAirSlipstreamTier", tier);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 0.0D, 0.0D, 0.20D, durationTicks);

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        SpellWorldVfxManager.broadcastArcaneSeal(level, caster.position(), Math.max(1.45D, radius * 0.50D), EssenceType.AIR, null, 52, tier);
        SpellParticleHelper.spawnImpactBurst(level, caster.position().add(0.0D, 0.9D, 0.0D), EssenceType.AIR, null, 1.35D, tier);
        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_WHIRL, SoundSource.PLAYERS, 0.85F, 1.25F);
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
        // Geyser Mine (Air Trampoline): 1.0s arming delay, 10m persistence (indefinite at Tier 5+),
        // 50% Sneak-Right-Click disarm refund, and 14-block skyward geyser launch within 3 blocks!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? 72_000_000 : 12_000;

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster,
                glyphCenter,
                3.0D,
                maxWaitTicks,
                level.getGameTime(),
                spell,
                slot,
                fallbackMateria,
                apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    // Section 4.6: 1.0-second (20-tick) arming delay!
                    if (elapsed < 20) {
                        return true;
                    }

                    AABB triggerBox = new AABB(
                            zone.center.x - 3.0D, zone.center.y - 0.85D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 1.85D, zone.center.z + 3.0D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, LivingEntity::isAlive);
                    if (stepped.isEmpty()) {
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, 2.8D, 14.0D, EssenceType.AIR, null, 60, tier
                    );

                    AABB plumeBox = new AABB(
                            zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 3.5D, zone.center.z + 3.0D
                    );
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, plumeBox, LivingEntity::isAlive)) {
                        boolean friendly = victim == owner
                                || (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                        victim, owner, zone.spell.hasWardFriendlyFireSafeguard()));
                        if (!friendly && SpellCombatReactionEngine.isBossEntity(srv, victim)) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                            continue;
                        }

                        // Launch 14 blocks upward (vy = 1.72D reaches ~14.2 blocks peak height in vanilla gravity!)
                        Vec3 v = victim.getDeltaMovement();
                        SpellExecutionEngine.applySyncedMotion(victim, new Vec3(v.x * 0.45D, 1.72D, v.z * 0.45D));
                        if (friendly) {
                            victim.resetFallDistance();
                        } else {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                    }

                    srv.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, zone.center.x, zone.center.y + 0.5D, zone.center.z,
                            2, 0.3D, 0.5D, 0.3D, 0.0D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(),
                            SoundSource.PLAYERS, 1.15F, 0.9F);
                    return false; // Detonated!
                }
        ).markDisarmableGlyph());

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.75F, 1.4F);
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
        // Howling Gale: 60° hurricane cone pushing mobs 8 blocks back, extinguishing fires/campfires/candles,
        // and blowing all physical projectiles away!
        AABB coneBox = caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(coneRange * 0.55D);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (target == caster) continue;
            Vec3 toTarget = target.getEyePosition().subtract(caster.getEyePosition()).normalize();
            if (toTarget.dot(direction) < 0.50D) continue;
            target.clearFire();
            if (SpellCombatReactionEngine.isBossEntity(level, target)) continue;
            if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) {
                SpellExecutionEngine.applySyncedMotion(
                        target,
                        new Vec3(direction.x * 1.85D, Math.max(0.42D, direction.y * 0.65D + 0.35D), direction.z * 1.85D)
                );
            }
        }

        // Blow away all physical projectiles inside the 60-degree cone
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, coneBox)) {
            if (proj.getOwner() == caster) continue;
            Vec3 toProj = proj.position().subtract(caster.getEyePosition()).normalize();
            if (toProj.dot(direction) >= 0.50D) {
                SpellExecutionEngine.applySyncedMotion(
                        proj,
                        new Vec3(direction.x * 1.95D, Math.max(0.38D, direction.y * 1.25D), direction.z * 1.95D)
                );
            }
        }

        // Extinguish fire blocks, soul fire, lit campfires, and lit candles in the cone
        int sampleSteps = Math.min(12, (int) Math.ceil(coneRange));
        for (int s = 1; s <= sampleSteps; s++) {
            BlockPos centerPos = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(centerPos.offset(-2, -1, -2), centerPos.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                    level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                } else if (bs.getBlock() instanceof AbstractCandleBlock && AbstractCandleBlock.isLit(bs)) {
                    AbstractCandleBlock.extinguish(null, bs, level, p);
                }
            }
        }

        // Section 4.7 Air Cone Recoil Impulse: pushes caster backward while blasting enemies away!
        Vec3 cVel = caster.getDeltaMovement();
        SpellExecutionEngine.applySyncedMotion(
                caster,
                new Vec3(cVel.x - direction.x * 0.58D, Math.max(cVel.y, 0.16D), cVel.z - direction.z * 0.58D)
        );

        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL,
                caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D,
                2, 0.5D, 0.35D, 0.5D, 0.0D);
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
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
        // Tornado Vortex: lingering 3D twisting atmospheric funnel (works indoors & underground!)
        // pulling nearby mobs into its spinning eye before violently tossing them outward!
        final int tier = spell.getEffectiveTier();
        final int totalTicks = 180 + (tier * 12);
        final double pullRadius = Math.max(5.5D, radius * 1.25D);
        final double funnelHeight = 7.5D + (tier * 0.45D);

        SpellWorldVfxManager.broadcastTornadoVortex(
                level, impactCenter, pullRadius, funnelHeight, EssenceType.AIR, null, totalTicks + 14, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster,
                impactCenter,
                pullRadius,
                totalTicks,
                level.getGameTime(),
                spell,
                slot,
                fallbackMateria,
                apparatusPotency,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB vortexBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + funnelHeight, zone.center.z + zone.radius
                    );

                    if (elapsed < totalTicks - 5) {
                        // Helical suction phase every 5 ticks
                        if (elapsed % 5 == 0) {
                            for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, vortexBox, LivingEntity::isAlive)) {
                                if (victim == owner) continue;
                                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                        victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                    continue;
                                }
                                Vec3 toEye = zone.center.subtract(victim.position());
                                Vec3 horiz = new Vec3(toEye.x, 0.0D, toEye.z);
                                Vec3 tangent = horiz.lengthSqr() > 1.0E-4D
                                        ? new Vec3(-horiz.z, 0.0D, horiz.x).normalize().scale(0.32D)
                                        : Vec3.ZERO;
                                Vec3 inward = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize().scale(0.52D) : Vec3.ZERO).add(tangent);
                                if (!SpellCombatReactionEngine.isBossEntity(srv, victim)) {
                                    SpellExecutionEngine.applySyncedMotion(victim, new Vec3(inward.x, 0.26D, inward.z));
                                }
                                if (elapsed > 0 && elapsed % 20 == 0) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, victim,
                                            inward.lengthSqr() > 1.0E-4D ? inward.normalize() : new Vec3(0.0D, 1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.25F
                                    );
                                }
                            }
                        }
                        if (elapsed % 20 == 0) {
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BREEZE_WHIRL,
                                    SoundSource.PLAYERS, 0.85F, 0.95F);
                        }
                        return true;
                    }

                    // Final violent outward toss + full sub-spell payoff!
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 1.0D, 0.0D), zone.radius, false, EssenceType.AIR, null, 50, tier
                    );
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, vortexBox, LivingEntity::isAlive)) {
                        if (victim == owner) continue;
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        Vec3 outward = victim.position().subtract(zone.center);
                        Vec3 horiz = new Vec3(outward.x, 0.0D, outward.z);
                        if (horiz.lengthSqr() < 1.0E-4D) horiz = new Vec3(1.0D, 0.0D, 0.0D);
                        Vec3 toss = horiz.normalize().scale(1.75D);
                        if (!SpellCombatReactionEngine.isBossEntity(srv, victim)) {
                            SpellExecutionEngine.applySyncedMotion(victim, new Vec3(toss.x, 1.02D, toss.z));
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                srv, owner, victim, toss.normalize(),
                                zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                        );
                    }
                    srv.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, zone.center.x, zone.center.y + 1.0D, zone.center.z,
                            2, 0.5D, 0.5D, 0.5D, 0.0D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(),
                            SoundSource.PLAYERS, 1.25F, 0.8F);
                    return false;
                }
        ));
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.BREEZE_WHIRL, SoundSource.PLAYERS, 1.0F, 0.9F);
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
        // Zephyr Eidolon (#A88654): role-specific spawn attunement for Agent, Sentry, or Decoy!
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.AIR.name());
        tag.putInt("SpellEidolonTintRGB", ZEPHYR_EIDOLON_HEX_COLOR);
        SpellDeliveryForm chosenForm = slot.formPart().form();
        if (chosenForm == SpellDeliveryForm.SUMMON_AGENT && summonEntity instanceof LivingEntity livingSummon) {
            livingSummon.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 1, false, false));
        }

        double initRad = chosenForm == SpellDeliveryForm.SUMMON_DECOY ? 12.0D
                : (chosenForm == SpellDeliveryForm.SUMMON_SENTRY ? 6.0D : Math.max(4.0D, radius));
        AABB cycloneArea = summonEntity.getBoundingBox().inflate(initRad);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, cycloneArea)) {
            if (nearby == caster || nearby == summonEntity || !nearby.isAlive()) continue;
            if (nearby instanceof Monster && nearby instanceof Mob mob) {
                if (chosenForm == SpellDeliveryForm.SUMMON_DECOY && summonEntity instanceof LivingEntity livingSummon) {
                    mob.setTarget(livingSummon);
                } else if (chosenForm == SpellDeliveryForm.SUMMON_SENTRY
                        && !SpellCombatReactionEngine.isBossEntity(level, nearby)) {
                    SpellExecutionEngine.applySyncedMotion(
                            nearby,
                            new Vec3(nearby.getDeltaMovement().x, 0.78D, nearby.getDeltaMovement().z)
                    );
                }
            }
        }
        SpellParticleHelper.spawnImpactBurst(level, spawnPos.add(0.0D, 1.0D, 0.0D), EssenceType.AIR, null, 1.4D, spell.getEffectiveTier());
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.2 Vacuum Implosion: Blinking leaves a vacuum pop at the departure spot that sucks adjacent foes inward!
        final double pullRad = 4.5D + (potencyTier * 0.35D);

        // Whisper-thin golden-ivory slipstream wind trace from departure to arrival (no generic purple portals or arrival explosion!)
        Vec3 depMid = departurePos.add(0.0D, 0.90D, 0.0D);
        Vec3 arrMid = arrivalPos.add(0.0D, 0.90D, 0.0D);
        double dashLen = depMid.distanceTo(arrMid);
        if (dashLen > 0.5D) {
            Vec3 dashDir = arrMid.subtract(depMid).normalize();
            TimedTintableParticleOption goldWisp = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_WISP.get(), AIR_SLIPSTREAM_GOLD_RGB, 14, 0.55F, 0.015F
            );
            TimedTintableParticleOption ivoryWisp = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_VORTEX.get(), AIR_SLIPSTREAM_WHITE_RGB, 12, 0.62F, 0.015F
            );
            int idx = 0;
            for (double d = 0.6D; d < dashLen - 0.6D; d += 0.95D) {
                Vec3 pt = depMid.add(dashDir.scale(d));
                level.sendParticles((idx++ & 1) == 0 ? ivoryWisp : goldWisp,
                        pt.x, pt.y, pt.z, 1, 0.03D, 0.03D, 0.03D, 0.01D);
            }
        }

        // Centered 3D Logarithmic Vacuum Implosion Sphere + Wind-Rose suction ring right at departurePos!
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, departurePos, pullRad, true, EssenceType.AIR, null, 18, potencyTier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(level, departurePos, pullRad * 0.68D, EssenceType.AIR, null, 16, potencyTier);

        // Immediate tick-0 suction + sustained 10-tick (0.5s) inward collapse culminating in a sonic Vacuum Pop!
        suckAdjacentIntoDepartureVacuum(level, caster, departurePos, pullRad);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.BREEZE_WHIRL, SoundSource.PLAYERS, 0.90F, 1.45F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster,
                departurePos,
                pullRad,
                10,
                level.getGameTime(),
                null,
                null,
                EssenceType.AIR,
                1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed < zone.maxDurationTicks) {
                        if (elapsed > 0 && elapsed % 2 == 0) {
                            suckAdjacentIntoDepartureVacuum(srv, owner, zone.center, zone.radius);
                        }
                        return true;
                    }

                    // Final Vacuum Pop at the implosion epicenter!
                    Vec3 popCenter = zone.center.add(0.0D, 0.85D, 0.0D);
                    srv.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, popCenter.x, popCenter.y, popCenter.z,
                            1, 0.05D, 0.05D, 0.05D, 0.0D);
                    TimedTintableParticleOption popVortex = TimedTintableParticleOption.linear(
                            ModParticles.TINTABLE_VORTEX.get(), AIR_SLIPSTREAM_WHITE_RGB, 14, 0.85F, 0.03F
                    );
                    srv.sendParticles(popVortex, popCenter.x, popCenter.y, popCenter.z, 10, 0.35D, 0.25D, 0.35D, 0.06D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS, 1.0F, 0.85F);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.35F);

                    AABB centerCluster = new AABB(
                            zone.center.x - 2.6D, zone.center.y - 1.2D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.6D, zone.center.z + 2.6D
                    );
                    for (LivingEntity clustered : srv.getEntitiesOfClass(LivingEntity.class, centerCluster, LivingEntity::isAlive)) {
                        if (clustered == owner) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(clustered, owner, true)) continue;
                        if (SpellCombatReactionEngine.isBossEntity(srv, clustered)) continue;
                        Vec3 toCenter = zone.center.subtract(clustered.position());
                        Vec3 h = new Vec3(toCenter.x, 0.0D, toCenter.z);
                        Vec3 snapIn = h.lengthSqr() > 0.04D ? h.normalize().scale(Math.min(0.45D, h.length() * 0.45D)) : Vec3.ZERO;
                        SpellExecutionEngine.applySyncedMotion(clustered, new Vec3(snapIn.x, 0.34D, snapIn.z));
                        clustered.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 24, 1, false, false));
                    }
                    return false;
                }
        ));
    }

    private static void suckAdjacentIntoDepartureVacuum(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 departurePos,
            double pullRad
    ) {
        AABB departureBox = new AABB(
                departurePos.x - pullRad, departurePos.y - 2.0D, departurePos.z - pullRad,
                departurePos.x + pullRad, departurePos.y + 3.0D, departurePos.z + pullRad
        );
        double radSq = pullRad * pullRad;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, departureBox, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            if (SpellCombatReactionEngine.isBossEntity(level, foe)) continue;
            Vec3 diff = departurePos.subtract(foe.position());
            double hDistSq = diff.x * diff.x + diff.z * diff.z;
            if (hDistSq > radSq) continue;
            double hDist = Math.sqrt(hDistSq);
            if (hDist < 0.25D) {
                // Already pinned inside the vacuum eye: hold them tightly at the center without overshooting!
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(0.0D, Math.max(0.06D, foe.getDeltaMovement().y * 0.3D), 0.0D));
            } else {
                double speed = Math.min(0.95D, Math.max(0.32D, hDist * 0.52D));
                double lift = foe.onGround() ? 0.22D : Math.max(0.05D, diff.y * 0.25D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3((diff.x / hDist) * speed, lift, (diff.z / hDist) * speed));
            }
        }

        // Also suck hostile mid-air projectiles & loose items into the collapsing air pocket!
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, departureBox)) {
            if (caster != null && proj.getOwner() == caster) continue;
            Vec3 toVac = departurePos.add(0.0D, 0.9D, 0.0D).subtract(proj.position());
            if (toVac.lengthSqr() > 0.04D && toVac.lengthSqr() <= radSq) {
                SpellExecutionEngine.applySyncedMotion(proj, toVac.normalize().scale(0.85D));
            }
        }
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, departureBox, ItemEntity::isAlive)) {
            Vec3 toVac = departurePos.add(0.0D, 0.35D, 0.0D).subtract(item.position());
            if (toVac.lengthSqr() > 0.09D && toVac.lengthSqr() <= radSq) {
                item.setDeltaMovement(toVac.normalize().scale(Math.min(0.65D, toVac.length() * 0.40D)));
                item.hasImpulse = true;
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Aeration & Dehydration: dries wet sponges, turns mud into clay, dries muddy roots, and blows away cobwebs & leaves!
        if (state.is(Blocks.WET_SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
            spawnWindPuff(level, pos, potencyTier);
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            spawnWindPuff(level, pos, potencyTier);
            return true;
        }
        if (state.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
            level.setBlockAndUpdate(pos, Blocks.MANGROVE_ROOTS.defaultBlockState());
            spawnWindPuff(level, pos, potencyTier);
            return true;
        }
        if (state.is(Blocks.COBWEB) || state.is(BlockTags.LEAVES)) {
            level.destroyBlock(pos, true, caster);
            spawnWindPuff(level, pos, potencyTier);
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
        ItemStack stack = itemEntity.getItem();
        if (stack.is(Items.WET_SPONGE)) {
            itemEntity.setItem(new ItemStack(Items.SPONGE, stack.getCount()));
            spawnWindPuff(level, itemEntity.blockPosition(), potencyTier);
            return true;
        }
        if (stack.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, stack.getCount()));
            spawnWindPuff(level, itemEntity.blockPosition(), potencyTier);
            return true;
        }
        if (stack.is(Items.MUDDY_MANGROVE_ROOTS)) {
            itemEntity.setItem(new ItemStack(Items.MANGROVE_ROOTS, stack.getCount()));
            spawnWindPuff(level, itemEntity.blockPosition(), potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Hypoxia Suffocation Hex: cuts stamina & attack speed by 40% and deals continuous suffocation damage on dry land!
        float stackMult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, 0.40D * stackMult, 35L);
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 35, 1, true, false));

        if (!target.isInWater()) {
            float hypoxiaDamage = Math.max(1.5F, MateriaStageTable.getBaseCombatDamageHp(potencyTier) * 0.12F) * stackMult;
            target.hurtServer(level, level.damageSources().drown(), hypoxiaDamage);
            target.setAirSupply(Math.max(0, target.getAirSupply() - (int) (55 * stackMult)));

            // Atmospheric breath-siphon collar spiraling outward from the victim's head/throat!
            Vec3 throat = new Vec3(target.getX(), target.getEyeY() - 0.12D, target.getZ());
            TimedTintableParticleOption siphonVortex = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_VORTEX.get(), AIR_SLIPSTREAM_WHITE_RGB, 16, 0.65F, 0.015F
            );
            TimedTintableParticleOption siphonGold = TimedTintableParticleOption.linear(
                    ModParticles.TINTABLE_WISP.get(), AIR_SLIPSTREAM_GOLD_RGB, 18, 0.58F, 0.02F
            );
            double basePhase = level.getGameTime() * 0.35D;
            for (int i = 0; i < 6; i++) {
                double ang = basePhase + (Math.PI * 2.0D * i / 6.0D);
                double rx = Math.cos(ang) * 0.48D;
                double rz = Math.sin(ang) * 0.48D;
                level.sendParticles((i & 1) == 0 ? siphonVortex : siphonGold,
                        throat.x + rx, throat.y + 0.05D * (i % 3), throat.z + rz,
                        1, 0.02D, 0.03D, 0.02D, 0.02D);
            }
            SpellWorldVfxManager.broadcastVacuumSphere(
                    level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D),
                    1.20D, true, EssenceType.AIR, null, 12, potencyTier
            );
        }
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
            // Second Wind Breath: immediately refills target oxygen supply + rising golden-ivory breeze halo
            target.setAirSupply(target.getMaxAirSupply());
            SpellWorldVfxManager.broadcastArcaneSeal(level, target.position(), 1.25D, EssenceType.AIR, null, 18, potencyTier);
        } else if (effect == SpellCoreEffect.WARD) {
            // Slipstream Wind-Wall: activates 50% projectile dampening window alongside persistent 3D Swirling Zephyr Ward Shell
            EntityHelper.getPersistentData(target).putLong(
                    SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL,
                    level.getGameTime() + 120L + (potencyTier * 20L)
            );
            SpellCombatReactionEngine.syncEntityBarrierState(level, target, 0.55F, null);
        }
    }

    private static void pullEntityToward(LivingEntity entity, Vec3 dest, double horizontalScale, double liftY) {
        Vec3 diff = dest.subtract(entity.position());
        if (diff.lengthSqr() < 1.0E-4D) return;
        Vec3 pull = diff.normalize().scale(horizontalScale);
        SpellExecutionEngine.applySyncedMotion(entity, new Vec3(pull.x, liftY, pull.z));
    }

    private static void spawnWindPuff(ServerLevel level, BlockPos pos, int tier) {
        Vec3 center = Vec3.atCenterOf(pos);
        SpellWorldVfxManager.broadcastProjectileImpact(level, center, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.AIR, null, 0.85F, tier);
        SpellParticleHelper.spawnImpactBurst(level, center, EssenceType.AIR, null, 0.85D, tier);
        level.playSound(null, pos, SoundEvents.BREEZE_IDLE_GROUND, SoundSource.PLAYERS, 0.65F, 1.35F);
    }
}
