package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
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

/**
 * Complete per-part Signature Mutation implementation for <b>Eschaton Materia (Tier 6 Singularity Fusion #55)</b>
 * (Spellcrafting System Guide Section 6.57 — Empyrean + Cataclysm + Terminus + Entropic: "The Omega Singularity").
 */
public final class EschatonSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ESCHATON_ACCRETION_VIOLET_HEX_COLOR = 0x7012A5;

    @Override
    public EssenceType materiaType() {
        return EssenceType.ESCHATON;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Event Horizon Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Black-Hole Railgun Dart";
            case BEAM -> "Dimensional Void-Fissure Beam & Omega Collapse";
            case RADIAL_BURST -> "Cosmic Event-Horizon Nova";
            case TOUCH_STRIKE -> "Reality-Cleaving Scythe";
            case AURA -> "Mantle of the Omega Singularity";
            case GLYPH -> "Singularity Anchor Sigil";
            case CONE -> "Accretion Singularity Torrent";
            case SKYFALL -> "Dimensional Inversion Starfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Spacetime Fold Slip";
            case TRANSMUTE, BREAK -> "Dimensional Event Horizon Transmutation & Accretion Compression";
            case AFFLICTION -> "Omega Event Horizon Dissolution Hex";
            default -> "Omega Singularity " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Event Horizon Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.8D), EssenceType.ESCHATON, 48L);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.03D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Leaves trailing mini-singularities that drag nearby enemies into its wake!
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(3.5D), LivingEntity::isAlive)) {
            if (owner != null && (ApexEschatonSingularityHelper.isFriendly(foe, owner)
                    || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true))) {
                continue;
            }
            Vec3 pull = projectile.position().subtract(foe.position()).scale(0.28D);
            foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.04D, pull.z));
            foe.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.08D, 0.08D, 0.08D, 0.03D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (projectile.getPierceCountRemaining() <= 0) {
            collapseOmegaEventHorizon(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (projectile.isWallPhasing()) {
            ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(level, caster, blockHit.getBlockPos(), 1, EssenceType.ESCHATON, 16L);
        } else {
            collapseOmegaEventHorizon(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        collapseOmegaEventHorizon(level, caster, projectile.position(), spell);
    }

    private static void collapseOmegaEventHorizon(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // 10m Omega Event Horizon: violently vacuums mobs inward, vaporizes loose obstacles into fuel,
        // shatters 8 armor points, and compresses matter into raw Materia droplets!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(level, caster, BlockPos.containing(center), 4, EssenceType.ESCHATON, 40L);
        AABB horizon = new AABB(center.x - 10.0D, center.y - 3.0D, center.z - 10.0D, center.x + 10.0D, center.y + 5.5D, center.z + 10.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, horizon, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            Vec3 in = center.subtract(foe.position()).scale(0.48D);
            foe.setDeltaMovement(in.x, 0.25D, in.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 220L);
            foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 10.0F + spell.getEffectiveTier() * 2.0F);
        }
        SpellLingeringZoneManager.spawnTypedEssenceOrb(level, center, EssenceType.ESCHATON, 3);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 48, 3.0D, 0.6D, 3.0D, 0.1D);
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
        // Dimensional Void-Fissure Beam & Omega Collapse: -3 armor/s, erases non-bedrock obstacles into fuel;
        // 5.0s continuous -> 16m KB, -8 armor, purges buffs, opens 14m gravitational tear for 6.0s (120t)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(maxReach))).inflate(2.5D), EssenceType.ESCHATON, 48L);
        if (blockHit != null) {
            ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(level, caster, blockHit.getBlockPos(), 2, EssenceType.ESCHATON, 32L);
        }

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 100L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("EschatonBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("EschatonBeamStacks").orElse(0) + 1 : 1;
            data.putLong("EschatonBeamLastTick", now);
            data.putInt("EschatonBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("EschatonBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
                hit.setDeltaMovement(dir.x * 3.1D, 0.55D, dir.z * 3.1D); // 16m KB!
                hit.hasImpulse = true;
                hit.hurtServer(level, level.damageSources().fellOutOfWorld(), 20.0F + spell.getEffectiveTier() * 3.2F);

                Vec3 tearCenter = hit.position();
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, tearCenter, 14.0D, 120, now, spell, slot, fallbackMateria, apparatusPotency,
                        (srv, zone, elapsed) -> {
                            if (elapsed > zone.maxDurationTicks) return false;
                            if (elapsed % 8 == 0) {
                                LivingEntity owner = zone.resolveCaster(srv);
                                AABB tear = new AABB(zone.center.x - 14.0D, zone.center.y - 3.0D, zone.center.z - 14.0D,
                                        zone.center.x + 14.0D, zone.center.y + 6.0D, zone.center.z + 14.0D);
                                for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, tear, e -> e.isAlive() && e != owner)) {
                                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    Vec3 in = zone.center.subtract(foe.position()).scale(0.32D);
                                    foe.setDeltaMovement(foe.getDeltaMovement().add(in.x, 0.04D, in.z));
                                    foe.hasImpulse = true;
                                }
                                srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.6D, zone.center.z, 24, 4.0D, 0.5D, 4.0D, 0.08D);
                            }
                            return true;
                        }
                ));
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
        // Cosmic Event-Horizon Nova: 16m shockwave (ZERO light blocks!), 14m KB flat, -8 armor,
        // dissolves projectiles into fuel, grants party Mantle of the Omega Singularity for 20.0s (400t)!
        double r = Math.max(16.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 3.0D, center.z - r, center.x + r, center.y + 6.0D, center.z + r);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, box, EssenceType.ESCHATON, 64L);

        long mantleUntil = level.getGameTime() + 400L;
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL, mantleUntil);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 14.0D, 8.0D, 0.0D, 0.0D, 400L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(2.75D); // 14m KB!
            ent.setDeltaMovement(away.x, 0.35D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL, mantleUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 14.0D, 8.0D, 0.0D, 0.0D, 400L);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0D, center.z, 52, 4.0D, 0.6D, 4.0D, 0.1D);
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
        // Reality-Cleaving Scythe: 75% bypass, -8 armor, disables shields 8.0s (160t), severs buffs,
        // locks primary victim in 4.0s (80t) stasis, pulls adjacent enemies directly into slash arc!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        Vec3 arcOrigin = hitEntities.isEmpty() ? caster.position().add(caster.getLookAngle().scale(2.5D)) : hitEntities.getFirst().position();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            ApexEschatonSingularityHelper.disableShield(hit, 160);
            ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
        }

        AABB pullBox = new AABB(arcOrigin.x - 8.0D, arcOrigin.y - 2.0D, arcOrigin.z - 8.0D,
                arcOrigin.x + 8.0D, arcOrigin.y + 4.0D, arcOrigin.z + 8.0D);
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, pullBox, e -> e.isAlive() && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
            Vec3 in = arcOrigin.subtract(adj.position()).scale(0.45D);
            adj.setDeltaMovement(in.x, 0.18D, in.z);
            adj.hasImpulse = true;
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
        int dur = 360 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 14.0D, 8.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY(1.0D), caster.getZ(), 36, 0.6D, 0.7D, 0.6D, 0.06D);
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
        // Singularity Anchor Sigil: 6m trigger -> locks entering mobs for 6.0s (120t) with unbreakable gravity tethers,
        // -8 armor, strips buffs, and pulls all nearby projectiles directly into the anchored targets!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 6.0D, 480, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 1.2D, 0.05D, 1.2D, 0.02D);
                    }
                    if (elapsed < 10) return true;

                    AABB trig = new AABB(zone.center.x - 6.0D, zone.center.y - 1.5D, zone.center.z - 6.0D,
                            zone.center.x + 6.0D, zone.center.y + 3.0D, zone.center.z + 6.0D);
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (victims.isEmpty()) return true;

                    for (LivingEntity foe : victims) {
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 240L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 6));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 120, 0));
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 10.0D, 120, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB pBox = new AABB(sZone.center.x - 10.0D, sZone.center.y - 3.0D, sZone.center.z - 10.0D,
                                        sZone.center.x + 10.0D, sZone.center.y + 6.0D, sZone.center.z + 10.0D);
                                ApexEschatonSingularityHelper.pullProjectilesToward(sLvl, sOwner, pBox, sZone.center.add(0.0D, 1.0D, 0.0D));
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
        // Accretion Singularity Torrent: -8 armor, vaporizes projectiles into fuel, carves safe tunnel through non-bedrock blocks,
        // Omega Spacetime Disorientation (35.0s / 700t AI disruption + 75% Slowness V)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(coneRange))).inflate(4.0D), EssenceType.ESCHATON, 64L);

        BlockPos tunnelStep = BlockPos.containing(caster.getEyePosition().add(dir.scale(3.0D)));
        ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(level, caster, tunnelStep, 2, EssenceType.ESCHATON, 24L);

        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 700, 4)); // 75% slow for 35.0s!
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 700); // 35.0s!
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
        // Dimensional Inversion Starfall: 20m zone, 30.0s (600t) bombardment + 15.0s (300t) Collapsed Reality Slag (900t total)!
        double r = Math.max(20.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 900, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean starfall = elapsed <= 600;
                    if (elapsed == 20) {
                        ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(srv, zone.resolveCaster(srv),
                                BlockPos.containing(zone.center), 4, EssenceType.ESCHATON, 32L);
                    }
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + (starfall ? 5.5D : 0.4D), zone.center.z,
                                32, zone.radius * 0.55D, starfall ? 2.6D : 0.3D, zone.radius * 0.55D, 0.08D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 10.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && (ApexEschatonSingularityHelper.isFriendly(foe, owner)
                                    || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard()))) {
                                continue;
                            }
                            if (starfall) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 220L);
                                if (elapsed % 60 == 0) {
                                    foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 25, 0));
                                } else {
                                    foe.setDeltaMovement(foe.getDeltaMovement().x * 0.2D, -0.55D, foe.getDeltaMovement().z * 0.2D);
                                    foe.hasImpulse = true;
                                }
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.60F);
                            } else {
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5)); // 80% slow
                                Vec3 in = zone.center.subtract(foe.position()).scale(0.28D);
                                foe.setDeltaMovement(foe.getDeltaMovement().add(in.x, 0.0D, in.z));
                                foe.hasImpulse = true;
                                foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 6.0F);
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
        tag.putString("SpellEidolonMateria", EssenceType.ESCHATON.name());
        tag.putInt("SpellEidolonTintRGB", ESCHATON_ACCRETION_VIOLET_HEX_COLOR);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawnPos.x, spawnPos.y + 1.1D, spawnPos.z, 36, 0.6D, 0.7D, 0.6D, 0.08D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Spacetime Fold Slip: 24m enemy position-swap executed in SpellExecutionEngine;
        // arrival grants complete Invisibility, cloaking, and 4.0s (80t) complete damage immunity + sheds 100% aggro!
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120, 0, false, false));
        EntityHelper.getPersistentData(caster)
                .putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, level.getGameTime() + 80L);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 0.65F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Dimensional Event Horizon Transmutation & Accretion Compression [5.0s Channel, 16m, strictly NO air transmutation]:
        // Stone/Deepslate/Netherrack -> Crying Obsidian/Blackstone/Void Slag; erases loose non-bedrock obstacles into fuel;
        // compresses cobble/stone/gravel -> raw mineral blocks; hostile drops -> pure essence orbs;
        // purges curses + elevates up to 2 enchants (+2); 4m standing water + bottle -> 50/50 Stasis Elixir / Essence of Omega Horizon!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 16, potencyTier, EssenceType.ESCHATON,
                "Dimensional Event Horizon Transmutation & Accretion Compression",
                () -> {
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 2, 1.0F, 2);
                    }
                    ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 4, EssenceType.ESCHATON);
                })) {
            return true;
        }

        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.NETHERRACK)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                case 1 -> Blocks.BLACKSTONE.defaultBlockState();
                default -> ModBlocks.ASTRAL_IMPACTITE.get().defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        if ((state.is(Blocks.COBBLESTONE) || state.is(Blocks.DIRT) || state.is(Blocks.GRAVEL) || state.is(Blocks.SAND))
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            level.removeBlock(pos, false);
            if (caster instanceof Player p) {
                SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ESCHATON, 48L);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 2, 1.0F, 2)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if ((stack.is(Items.COBBLESTONE) || stack.is(Items.STONE) || stack.is(Items.GRAVEL)) && stack.getCount() >= 8) {
            int blocks = Math.max(1, stack.getCount() / 8);
            ItemStack condensed = switch (level.random.nextInt(4)) {
                case 0 -> new ItemStack(Items.IRON_BLOCK, blocks);
                case 1 -> new ItemStack(Items.COPPER_BLOCK, blocks);
                case 2 -> new ItemStack(Items.COAL_BLOCK, blocks);
                default -> new ItemStack(Items.AMETHYST_BLOCK, blocks);
            };
            itemEntity.setItem(condensed);
            return true;
        }
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE) || stack.is(Items.GUNPOWDER)
                || stack.is(Items.SPIDER_EYE) || stack.is(Items.ENDER_PEARL) || stack.is(Items.BLAZE_ROD)) {
            ItemStack ess = new ItemStack(ModItems.STRONG_ESSENCE.get(), stack.getCount());
            EssenceItem.setEssenceType(ess, EssenceType.ESCHATON);
            itemEntity.setItem(ess);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Omega Event Horizon Dissolution Hex: true void-collapse damage, -85% move speed,
        // blocks jumping & teleportation, -60% toughness, 60% weapon recoil!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.60D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5, true, true));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
        target.hurtServer(level, level.damageSources().fellOutOfWorld(), (3.5F + potencyTier * 0.85F) * mult);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.85D), target.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.04D);
        return true;
    }
}
