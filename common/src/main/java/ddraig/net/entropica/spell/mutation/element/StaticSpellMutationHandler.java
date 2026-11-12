package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
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
import net.minecraft.core.particles.ColorParticleOption;
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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Static Materia (Tier 3 Trinity Fusion #43)</b>
 * (Spellcrafting System Guide Section 6.44 — Lightning + Air + Arid).
 */
public final class StaticSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GALVANIC_STATIC_HEX_COLOR = 0xFFE600;

    @Override
    public EssenceType materiaType() {
        return EssenceType.STATIC;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Galvanic Tesla Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Dielectric Ball-Lightning Mortar";
            case BEAM -> "Dielectric Breakdown Arc-Stream Beam";
            case RADIAL_BURST -> "Ground-Discharge EMP Shockwave";
            case TOUCH_STRIKE -> "Dielectric Palm Overload";
            case AURA -> "Dielectric Faraday Shroud";
            case GLYPH -> "Galvanic Arc-Snare Sigil";
            case CONE -> "High-Voltage Static Arc-Fan Deluge";
            case SKYFALL -> "Galvanic Tesla Cataclysmic Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Ionized Slipstream Step";
            case TRANSMUTE, BREAK -> "Dielectric Electrolysis & Vitrification Transmute";
            case AFFLICTION -> "Galvanic Nerve-Spasm Hex";
            default -> "Galvanic Static " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Galvanic Tesla Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.03D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Dielectric Ball-Lightning Mortar: continuously zaps enemies within 3m as it flies!
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.1D, 0.1D, 0.1D, 0.04D);
        if (projectile.tickCount % 5 == 0) {
            Entity owner = projectile.getOwner();
            LivingEntity caster = owner instanceof LivingEntity l ? l : null;
            for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(3.0D), e -> e.isAlive() && e != owner)) {
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, true)) continue;
                near.hurtServer(level, level.damageSources().lightningBolt(), 2.0F);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, near.getX(), near.getY(0.8D), near.getZ(), 6, 0.2D, 0.25D, 0.2D, 0.04D);
            }
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        detonateEmBurst(level, caster, hitEntity.position(), spell.hasWardFriendlyFireSafeguard());
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        detonateEmBurst(level, caster, blockHit.getLocation(), spell.hasWardFriendlyFireSafeguard());
    }

    private static void detonateEmBurst(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, boolean safeguard) {
        // 5m electromagnetic burst knocking enemies 5m backward & stripping 3 armor points!
        AABB box = new AABB(center.x - 5.0D, center.y - 2.0D, center.z - 5.0D, center.x + 5.0D, center.y + 3.0D, center.z + 5.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.25D);
            foe.setDeltaMovement(away.x, 0.35D, away.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.6D, center.z, 30, 1.8D, 0.6D, 1.8D, 0.08D);
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
        // Dielectric Breakdown Arc-Stream Beam: secondary shocks, -1 armor, magnetizes metallic items into caster's inventory;
        // REQUIRES 5.0s sustained (stacks >= 5) -> Catastrophic Dielectric Flashover (colossal EM blast + 3.0s / 60t paralyze)!
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(2.5D);
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, corridor)) {
            if (caster instanceof Player player && player.getInventory().add(ie.getItem().copy())) {
                ie.discard();
            } else {
                ie.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
            }
        }

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 1.0D, 0.0D, 80L);
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.5D), e -> e.isAlive() && e != hit && e != caster)) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                adj.hurtServer(level, level.damageSources().lightningBolt(), 2.5F);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("StaticBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("StaticBeamStacks").orElse(0) + 1 : 1;
            data.putLong("StaticBeamLastTick", now);
            data.putInt("StaticBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("StaticBeamStacks", 0);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.5D), e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
                    caught.hurtServer(level, level.damageSources().lightningBolt(), 10.5F + spell.getEffectiveTier() * 2.5F);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.90F, 0.1F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.95F, 1.3F);
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
        // Ground-Discharge EMP Shockwave: 6m radius, strictly ZERO physical light blocks — disables creepers,
        // clears all hostile mob aggro, and paralyzes caught hostiles in galvanic stasis for 2.5s (50t)!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (foe instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
                SpellLingeringZoneManager.applyArcaneSilence(creeper, 120);
            }
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.90F, 0.1F), center.x, center.y + 0.8D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.4D, center.z, 44, 2.4D, 0.3D, 2.4D, 0.08D);
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
        // Dielectric Palm Overload: discharges creeper fuses safely & blasts victim 8m backward!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (hit instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
                SpellLingeringZoneManager.applyArcaneSilence(creeper, 100);
            }
            hit.setDeltaMovement(push.x * 1.85D, 0.40D, push.z * 1.85D);
            hit.hasImpulse = true;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hit.getX(), hit.getY(0.9D), hit.getZ(), 20, 0.35D, 0.4D, 0.35D, 0.08D);
        }
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
        // Dielectric Faraday Shroud: +7 Armor, +3 Toughness, +25% sprint & attack speed, Lightning/Fall/Slowness/Thorns immunity!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.25D, dur);
        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(caster, level, 0.25D, dur);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.06D);
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
        // Galvanic Arc-Snare Sigil: tethers up to 5 caught hostiles across an 8m radius, preventing them from moving apart
        // while shocking linked targets & stripping 3 armor points!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.5D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.8D, 0.05D, 0.8D, 0.02D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard()))).isEmpty()) {
                        return true;
                    }

                    AABB snareSearch = new AABB(zone.center.x - 8.0D, zone.center.y - 2.0D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 3.5D, zone.center.z + 8.0D);
                    List<UUID> linkedIds = new ArrayList<>();
                    for (LivingEntity cand : srv.getEntitiesOfClass(LivingEntity.class, snareSearch, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        SpellCombatReactionEngine.applyTimedAttributeShred(cand, srv, 3.0D, 0.0D, 160L);
                        linkedIds.add(cand.getUUID());
                        if (linkedIds.size() >= 5) break;
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 8.0D, 100, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                for (UUID id : linkedIds) {
                                    Entity e = sLvl.getEntity(id);
                                    if (!(e instanceof LivingEntity v) || !v.isAlive()) continue;
                                    if (v.position().distanceToSqr(sZone.center) > 9.0D) {
                                        Vec3 pull = sZone.center.subtract(v.position()).scale(0.35D);
                                        v.setDeltaMovement(pull.x, 0.05D, pull.z);
                                        v.hasImpulse = true;
                                    }
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.50F);
                                        sLvl.sendParticles(ParticleTypes.ELECTRIC_SPARK, v.getX(), v.getY(0.8D), v.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.04D);
                                    }
                                }
                                return true;
                            }
                    ));
                    return false;
                }
        ));
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
        // High-Voltage Static Arc-Fan Deluge: melts arrows/light projectiles, 3m KB,
        // Electromagnetic Neurological Overload (-50% attack speed & 25.0s / 500t AI pathfinding disruption)!
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB projCone = new AABB(caster.getEyePosition(), caster.getEyePosition().add(dirNorm.scale(coneRange))).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, projCone, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.03D);
            proj.discard();
        }
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(dirNorm.x * 0.85D, 0.22D, dirNorm.z * 0.85D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.50D, 100L);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
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
        // Galvanic Tesla Cataclysmic Tempest: Extended 18.0s (360t) storm + 8.0s (160t) ionized ground field (520t total)
        // across 12m radius — pins downward, shatters 5 armor, chain-paralyzes 3.0s (60t), disables creepers, 60% slow & sprint shock!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, zone.center.x, zone.center.y + (stormActive ? 3.5D : 0.3D), zone.center.z,
                                26, zone.radius * 0.55D, stormActive ? 2.0D : 0.2D, zone.radius * 0.55D, 0.08D);
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_STATIC_SPRINT_SHOCK_UNTIL, now + 35L);
                            if (foe instanceof Creeper creeper) {
                                creeper.setSwellDir(-1);
                            }
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.35D, foe.getDeltaMovement().z * 0.3D);
                                foe.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.STATIC.name());
        tag.putInt("SpellEidolonTintRGB", GALVANIC_STATIC_HEX_COLOR);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Ionized Slipstream Step: silent 14m step leaving sparking dielectric dust and shedding all hostile mob aggro!
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.35D, 0.45D, 0.35D, 0.05D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 20, 0.35D, 0.45D, 0.35D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Dielectric Electrolysis & Vitrification Transmute: raw iron/copper -> ingots (+25% bonus yield),
        // sand -> glass, cobblestone/gravel -> deepslate, standing water -> dry air!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.GRAVEL)) {
            level.setBlockAndUpdate(pos, Blocks.DEEPSLATE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.03D);
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.RAW_IRON) || stack.is(Items.RAW_COPPER)) {
            boolean iron = stack.is(Items.RAW_IRON);
            float totalIngots = stack.getCount() * 1.25F;
            int fullIngots = Math.min(64, (int) totalIngots);
            int extraNuggets = Math.round((totalIngots - fullIngots) * 9.0F);
            itemEntity.setItem(new ItemStack(iron ? Items.IRON_INGOT : Items.COPPER_INGOT, Math.max(1, fullIngots)));
            if (extraNuggets > 0 && iron) {
                level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                        new ItemStack(Items.IRON_NUGGET, extraNuggets)));
            }
            return true;
        }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.GLASS, stack.getCount()));
            return true;
        }
        if (stack.is(Items.COBBLESTONE) || stack.is(Items.GRAVEL)) {
            itemEntity.setItem(new ItemStack(Items.DEEPSLATE, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Galvanic Nerve-Spasm Hex: involuntary stagger/interrupt every 3.0s, -50% move speed, -40% attack speed,
        // -35% toughness, continuous electric shocks, and 4m secondary arc shocks!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2, true, true));
        if ((now / 20L) % 3L == 0L) {
            target.stopUsingItem();
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
        }
        float shock = (1.55F + potencyTier * 0.50F) * mult;
        target.hurtServer(level, level.damageSources().lightningBolt(), shock);

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0D), e -> e.isAlive() && e != target && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, true)) continue;
            adj.hurtServer(level, level.damageSources().lightningBolt(), shock * 0.50F);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, adj.getX(), adj.getY(0.8D), adj.getZ(), 5, 0.2D, 0.25D, 0.2D, 0.04D);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.85D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.04D);
        return true;
    }
}
