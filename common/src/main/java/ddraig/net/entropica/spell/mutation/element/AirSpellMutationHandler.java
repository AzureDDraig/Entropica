package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
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
 *     <li><b>Flow (Chains)</b>: <i>Sonic Leap Chains</i> — near-zero transit wind bridges dragging chained targets together.</li>
 *     <li><b>Projectile</b>: <i>Turbulence Spiral</i> — corkscrew spiral path deflecting incoming hostile projectiles along flight.</li>
 *     <li><b>Beam</b>: <i>Jetstream Propulsion</i> — wind-tunnel knockback + vertical jetpack lifter when aimed downward.</li>
 *     <li><b>Radial Burst</b>: <i>Gust Shield</i> — spherical wind pushback, 50% incoming projectile damage reduction, +30% caster speed.</li>
 *     <li><b>Touch / Strike</b>: <i>Concussive Uppercut</i> — launches struck foes ~8 blocks straight into the air.</li>
 *     <li><b>Aura / Self</b>: <i>Slipstream Shield</i> — sprinting wind-barrier bowling colliding mobs aside.</li>
 *     <li><b>Glyph / Trap</b>: <i>Geyser Mine (Air Trampoline)</i> — proximity mine launching entities within 3 blocks ~14 blocks upward.</li>
 *     <li><b>Cone / Spray</b>: <i>Howling Gale</i> — 60° hurricane cone pushing mobs 8 blocks back, extinguishing fires, blowing away projectiles.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Tornado Vortex</i> — twisting vortex pulling mobs into its spinning eye before tossing them outward.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Zephyr Eidolon</i> — golden-buff (#A88654) cyclone summon launching hostiles & taunting attacks.</li>
 *     <li><b>Blink / Step</b>: <i>Vacuum Implosion</i> — leaves a departure vacuum pop sucking adjacent foes inward.</li>
 *     <li><b>Transmute</b>: <i>Aeration & Dehydration</i> — dries wet sponges, turns mud into clay, blows away cobwebs & leaves.</li>
 *     <li><b>Affliction</b>: <i>Hypoxia Suffocation Hex</i> — persistent anti-milk respiratory curse cutting stamina/attack speed by 40% & suffocating on land.</li>
 * </ul>
 */
public final class AirSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ZEPHYR_EIDOLON_HEX_COLOR = 0xA88654;

    @Override
    public EssenceType materiaType() {
        return EssenceType.AIR;
    }

    // --- Canonical Signature Titles ---

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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
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
        // Sonic Leap Chains: instant vacuum wind bridge dragging both chained targets toward their midpoint!
        Vec3 p1 = fromTarget.getEyePosition();
        Vec3 p2 = toTarget.getEyePosition();
        double dist = p1.distanceTo(p2);
        Vec3 stepVec = p2.subtract(p1).normalize();
        for (double d = 0.0D; d <= dist; d += 0.55D) {
            Vec3 pt = p1.add(stepVec.scale(d));
            level.sendParticles(ParticleTypes.CLOUD, pt.x, pt.y, pt.z, 1, 0.03D, 0.03D, 0.03D, 0.01D);
        }

        Vec3 midpoint = fromTarget.position().add(toTarget.position()).scale(0.5D);
        pullEntityToward(fromTarget, midpoint, 0.55D, 0.22D);
        pullEntityToward(toTarget, midpoint, 0.55D, 0.22D);
        level.playSound(null, BlockPos.containing(midpoint), SoundEvents.BREEZE_IDLE_AIR, SoundSource.PLAYERS, 0.65F, 1.55F);
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 FORMS)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Turbulence Spiral: tight corkscrew spiral + aerodynamic deflection of incoming hostile projectiles!
        Vec3 vel = projectile.getDeltaMovement();
        double speed = vel.length();
        if (speed > 0.05D) {
            Vec3 dir = vel.normalize();
            Vec3 upRef = Math.abs(dir.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = dir.cross(upRef).normalize();
            Vec3 orthoUp = right.cross(dir).normalize();

            double angle = projectile.tickCount * 0.95D;
            double spiralAmp = 0.09D;
            Vec3 spiralOffset = right.scale(Math.cos(angle) * spiralAmp).add(orthoUp.scale(Math.sin(angle) * spiralAmp));
            // Oscillate physical projectile trajectory in a tight zero-drift corkscrew around its forward axis
            Vec3 tangent = right.scale(-Math.sin(angle) * spiralAmp).add(orthoUp.scale(Math.cos(angle) * spiralAmp));
            projectile.setPos(projectile.getX() + tangent.x, projectile.getY() + tangent.y, projectile.getZ() + tangent.z);

            level.sendParticles(ParticleTypes.CLOUD,
                    projectile.getX() + spiralOffset.x * 3.0D,
                    projectile.getY() + spiralOffset.y * 3.0D,
                    projectile.getZ() + spiralOffset.z * 3.0D,
                    1, 0.01D, 0.01D, 0.01D, 0.0D);
        }

        // Deflect incoming hostile projectiles along the slipstream
        AABB slipstream = projectile.getBoundingBox().inflate(2.2D);
        for (Projectile other : level.getEntitiesOfClass(Projectile.class, slipstream)) {
            if (other == projectile || other.getOwner() == projectile.getOwner()) continue;
            Vec3 push = other.position().subtract(projectile.position()).normalize().scale(1.1D);
            other.setDeltaMovement(push.x, Math.max(0.25D, push.y), push.z);
            other.hasImpulse = true;
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
        // Jetstream Propulsion: wind-tunnel pushback on struck foes + vertical jetpack lifter when aimed downward!
        double pushStrength = 0.75D + (spell.getEffectiveTier() * 0.12D);
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            hit.setDeltaMovement(direction.x * pushStrength, Math.max(0.28D, direction.y * pushStrength + 0.2D), direction.z * pushStrength);
            hit.hasImpulse = true;
        }

        if (direction.y < -0.55D) {
            double liftY = 0.88D + (spell.getEffectiveTier() * 0.08D);
            Vec3 cur = caster.getDeltaMovement();
            caster.setDeltaMovement(cur.x * 0.85D - direction.x * 0.3D, Math.max(cur.y, liftY), cur.z * 0.85D - direction.z * 0.3D);
            caster.hasImpulse = true;
            caster.resetFallDistance();
            level.sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY(), caster.getZ(), 10, 0.3D, 0.15D, 0.3D, 0.08D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.65F, 1.35F);
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
        // Concussive Uppercut: launch struck targets ~8 blocks straight into the air (vy = 1.18 -> ~8m peak height)!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            Vec3 cur = hit.getDeltaMovement();
            hit.setDeltaMovement(cur.x * 0.35D + direction.x * 0.18D, 1.18D, cur.z * 0.35D + direction.z * 0.18D);
            hit.hasImpulse = true;
            level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, hit.getX(), hit.getY() + 0.2D, hit.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (!hitEntities.isEmpty()) {
            level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.9F, 1.15F);
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
        // Gust Shield: push surrounding mobs back, halve incoming projectile damage for 6s, and grant +30% movement speed!
        AABB area = new AABB(center.x - radius, center.y - 2.0D, center.z - radius,
                center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = entity.position().subtract(center).normalize().scale(1.25D);
            entity.setDeltaMovement(away.x, 0.42D, away.z);
            entity.hasImpulse = true;
        }

        // Deflect any mid-air projectiles currently inside the bubble
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, area)) {
            if (proj.getOwner() == caster) continue;
            Vec3 away = proj.position().subtract(center).normalize().scale(1.3D);
            proj.setDeltaMovement(away.x, Math.max(0.3D, away.y), away.z);
            proj.hasImpulse = true;
        }

        int durationTicks = 120 + (spell.getEffectiveTier() * 20);
        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        casterData.putLong(SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL, level.getGameTime() + durationTicks);
        // Speed II (amplifier 1 = +40%, closest clean vanilla tier to +30% movement speed)
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, durationTicks, 1, false, true));

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
        // Slipstream Shield: sprinting builds a charging air barrier that bowls colliding mobs aside!
        int durationTicks = 160 + (spell.getEffectiveTier() * 30);
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_AIR_SLIPSTREAM_UNTIL,
                level.getGameTime() + durationTicks
        );
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, durationTicks, 0, false, true));
        level.sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY() + 0.8D, caster.getZ(), 18, 0.45D, 0.5D, 0.45D, 0.06D);
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
        // Geyser Mine (Air Trampoline): proximity glyph waits until stepped on, then launches entities within 3 blocks ~14 blocks upward!
        int maxWaitTicks = 400 + (spell.getEffectiveTier() * 60);
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
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 0.1D, zone.center.z,
                                4, 0.8D, 0.05D, 0.8D, 0.01D);
                    }
                    // Arm after 6 ticks so the caster placing it at close range can also step onto it intentionally or let foes trip it
                    if (elapsed < 6) return true;

                    AABB triggerBox = new AABB(zone.center.x - 1.8D, zone.center.y - 0.8D, zone.center.z - 1.8D,
                            zone.center.x + 1.8D, zone.center.y + 1.8D, zone.center.z + 1.8D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, LivingEntity::isAlive);
                    if (stepped.isEmpty()) {
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB plumeBox = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 3.5D, zone.center.z + 3.0D);
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, plumeBox, LivingEntity::isAlive)) {
                        // Launch 14 blocks upward (vy = 1.58D reaches ~14 blocks peak height in vanilla gravity!)
                        Vec3 v = victim.getDeltaMovement();
                        victim.setDeltaMovement(v.x * 0.5D, 1.58D, v.z * 0.5D);
                        victim.hasImpulse = true;
                        if (victim == owner) {
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
                            SoundSource.PLAYERS, 1.1F, 0.9F);
                    return false; // Detonated!
                }
        ));
        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 1.4F);
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
        // Howling Gale: 60° hurricane cone pushing mobs 8 blocks back, extinguishing fires, and blowing away projectiles!
        for (LivingEntity target : hitTargets) {
            target.clearFire();
            if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) {
                target.setDeltaMovement(direction.x * 1.65D, Math.max(0.38D, direction.y * 0.6D + 0.32D), direction.z * 1.65D);
                target.hasImpulse = true;
            }
        }

        AABB coneBox = caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(coneRange * 0.55D);
        // Blow away all physical projectiles inside the 60-degree cone (cos 30° = 0.866; 60° half-angle wide cone >= 0.50)
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, coneBox)) {
            if (proj.getOwner() == caster) continue;
            Vec3 toProj = proj.position().subtract(caster.getEyePosition()).normalize();
            if (toProj.dot(direction) >= 0.50D) {
                proj.setDeltaMovement(direction.x * 1.8D, Math.max(0.35D, direction.y * 1.2D), direction.z * 1.8D);
                proj.hasImpulse = true;
            }
        }

        // Extinguish fire blocks and campfires in the cone
        int sampleSteps = Math.min(10, (int) Math.ceil(coneRange));
        for (int s = 1; s <= sampleSteps; s++) {
            BlockPos centerPos = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(centerPos.offset(-2, -1, -2), centerPos.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                    level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                }
            }
        }

        // Section 4.7 Air Cone Recoil Impulse: pushes caster backward while blasting enemies away!
        caster.push(-direction.x * 0.55D, 0.14D, -direction.z * 0.55D);
        caster.hasImpulse = true;

        level.sendParticles(ParticleTypes.CLOUD,
                caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D,
                22, 0.8D, 0.5D, 0.8D, 0.18D);
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
        // Tornado Vortex: 5s (100t) twisting atmospheric vortex pulling nearby mobs into its spinning eye before violently tossing them outward!
        final int totalTicks = 100;
        final double pullRadius = Math.max(5.5D, radius * 1.25D);

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
                            zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                    );

                    // Swirl funnel visuals every 4 ticks
                    if (elapsed % 4 == 0) {
                        for (int h = 0; h <= 5; h++) {
                            double swirlR = 0.4D + (h * 0.45D);
                            double ang = (elapsed * 0.45D) + h;
                            srv.sendParticles(ParticleTypes.CLOUD,
                                    zone.center.x + Math.cos(ang) * swirlR,
                                    zone.center.y + h * 0.9D,
                                    zone.center.z + Math.sin(ang) * swirlR,
                                    2, 0.1D, 0.1D, 0.1D, 0.02D);
                        }
                    }

                    if (elapsed < totalTicks - 5) {
                        // Suction phase: pull mobs into the spinning eye every 10 ticks
                        if (elapsed % 10 == 0) {
                            for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, vortexBox, LivingEntity::isAlive)) {
                                if (victim == owner) continue;
                                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                    continue;
                                }
                                Vec3 toEye = zone.center.subtract(victim.position());
                                Vec3 tangent = new Vec3(-toEye.z, 0.0D, toEye.x).normalize().scale(0.28D);
                                Vec3 inward = toEye.normalize().scale(0.45D).add(tangent);
                                victim.setDeltaMovement(inward.x, 0.24D, inward.z);
                                victim.hasImpulse = true;
                                if (elapsed % 20 == 0) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, victim, inward.normalize(),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                    );
                                }
                            }
                        }
                        return true;
                    }

                    // Final violent outward toss!
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, vortexBox, LivingEntity::isAlive)) {
                        if (victim == owner) continue;
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        Vec3 outward = victim.position().subtract(zone.center);
                        Vec3 horiz = new Vec3(outward.x, 0.0D, outward.z);
                        if (horiz.lengthSqr() < 1.0E-4D) horiz = new Vec3(1.0D, 0.0D, 0.0D);
                        Vec3 toss = horiz.normalize().scale(1.55D);
                        victim.setDeltaMovement(toss.x, 0.95D, toss.z);
                        victim.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                srv, owner, victim, toss.normalize(),
                                zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                        );
                    }
                    srv.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, zone.center.x, zone.center.y + 1.0D, zone.center.z,
                            2, 0.5D, 0.5D, 0.5D, 0.0D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(),
                            SoundSource.PLAYERS, 1.2F, 0.8F);
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
            livingSummon.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 1, false, false));
        }

        AABB cycloneArea = summonEntity.getBoundingBox().inflate(Math.max(4.0D, radius));
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, cycloneArea)) {
            if (nearby == caster || nearby == summonEntity || !nearby.isAlive()) continue;
            if (nearby instanceof Monster && nearby instanceof Mob mob) {
                if (chosenForm == SpellDeliveryForm.SUMMON_DECOY && summonEntity instanceof LivingEntity livingSummon) {
                    mob.setTarget(livingSummon);
                }
                if (chosenForm == SpellDeliveryForm.SUMMON_SENTRY) {
                    nearby.setDeltaMovement(nearby.getDeltaMovement().x, 0.78D, nearby.getDeltaMovement().z);
                    nearby.hasImpulse = true;
                }
            }
        }
        level.sendParticles(ParticleTypes.CLOUD, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 20, 0.5D, 0.7D, 0.5D, 0.08D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Vacuum Implosion: blinking leaves a vacuum pop at the departure spot sucking adjacent foes inward!
        double pullRad = 4.5D + (potencyTier * 0.35D);
        AABB departureBox = new AABB(
                departurePos.x - pullRad, departurePos.y - 2.0D, departurePos.z - pullRad,
                departurePos.x + pullRad, departurePos.y + 3.0D, departurePos.z + pullRad
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, departureBox)) {
            if (foe == caster || !foe.isAlive() || foe.isAlliedTo(caster)) continue;
            pullEntityToward(foe, departurePos, 0.85D, 0.30D);
        }
        level.sendParticles(ParticleTypes.CLOUD, departurePos.x, departurePos.y + 0.8D, departurePos.z,
                20, 0.6D, 0.4D, 0.6D, 0.05D);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS, 0.9F, 0.75F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Aeration & Dehydration: dries wet sponges, turns mud into clay, and blows away cobwebs and leaves!
        if (state.is(Blocks.WET_SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
            spawnWindPuff(level, pos);
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            spawnWindPuff(level, pos);
            return true;
        }
        if (state.is(Blocks.COBWEB) || state.is(BlockTags.LEAVES)) {
            level.destroyBlock(pos, true, caster);
            spawnWindPuff(level, pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            net.minecraft.world.entity.item.ItemEntity itemEntity,
            int potencyTier
    ) {
        var stack = itemEntity.getItem();
        if (stack.is(net.minecraft.world.item.Items.WET_SPONGE)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPONGE, stack.getCount()));
            spawnWindPuff(level, itemEntity.blockPosition());
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.MUD)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CLAY, stack.getCount()));
            spawnWindPuff(level, itemEntity.blockPosition());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Hypoxia Suffocation Hex: cuts stamina & attack speed by ~40% (Mining Fatigue II + Weakness I)
        // and deals continuous suffocating tick damage while on land!
        float stackMult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 35, 1, true, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 35, 0, true, false));

        if (!target.isInWater()) {
            float hypoxiaDamage = (1.0F + (potencyTier * 0.5F)) * stackMult;
            target.hurtServer(level, level.damageSources().drown(), hypoxiaDamage);
            target.setAirSupply(Math.max(0, target.getAirSupply() - (int) (45 * stackMult)));
            level.sendParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getY(0.85D), target.getZ(),
                    6, 0.2D, 0.2D, 0.2D, 0.03D);
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
            // Second Wind Breath: immediately refills target oxygen supply
            target.setAirSupply(target.getMaxAirSupply());
        } else if (effect == SpellCoreEffect.WARD) {
            // Slipstream Wind-Wall: activates projectile dampening window alongside Arcane Ward
            EntityHelper.getPersistentData(target).putLong(
                    SpellLingeringZoneManager.TAG_AIR_GUST_SHIELD_UNTIL,
                    level.getGameTime() + 120L + (potencyTier * 20L)
            );
        }
    }

    private static void pullEntityToward(LivingEntity entity, Vec3 dest, double horizontalScale, double liftY) {
        Vec3 diff = dest.subtract(entity.position());
        if (diff.lengthSqr() < 1.0E-4D) return;
        Vec3 pull = diff.normalize().scale(horizontalScale);
        entity.setDeltaMovement(pull.x, liftY, pull.z);
        entity.hasImpulse = true;
    }

    private static void spawnWindPuff(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.CLOUD,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                8, 0.25D, 0.25D, 0.25D, 0.04D);
    }
}
