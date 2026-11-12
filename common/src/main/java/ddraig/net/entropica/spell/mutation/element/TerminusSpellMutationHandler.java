package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Terminus Materia (Tier 5 Eschaton Fusion #54)</b>
 * (Spellcrafting System Guide Section 6.56 — Air + Frozen + Water: "The Absolute Zero").
 */
public final class TerminusSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TERMINUS_FROST_CYAN_HEX_COLOR = 0x98DCF2;

    @Override
    public EssenceType materiaType() {
        return EssenceType.TERMINUS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Cryogenic Polar Vortex Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Absolute-Zero Permafrost Lance";
            case BEAM -> "Sustained Cryogenic Vortex Ray & Zero-Point Shockwave";
            case RADIAL_BURST -> "Absolute-Zero Cryo-Nova";
            case TOUCH_STRIKE -> "Absolute-Zero Glacier Cleave";
            case AURA -> "Zero-Point Kinetic Stasis Shield";
            case GLYPH -> "Permafrost Glacial Abyss Sigil";
            case CONE -> "Cryogenic Blizzard Deluge";
            case SKYFALL -> "Continuous Cryo-Stasis Hailstorm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Glacial Slipstream Flash-Slide";
            case TRANSMUTE, BREAK -> "Zero-Point Crystallization & Permafrost Synthesis";
            case AFFLICTION -> "Absolute Zero Molecular Cessation Hex";
            default -> "Absolute-Zero Terminus " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Cryogenic Polar Vortex Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.4D), null, 0L);
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.08D, 0.08D, 0.08D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (projectile.getPierceCountRemaining() <= 0) {
            detonateZeroPointGlacialStasis(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isWallPhasing()) {
            detonateZeroPointGlacialStasis(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        detonateZeroPointGlacialStasis(level, caster, projectile.position(), spell);
    }

    private static void detonateZeroPointGlacialStasis(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // 8m Zero-Point Glacial Stasis Field: freezes hostiles 3.5s (70t), -6 armor, flash-freezes water -> Blue Ice!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-4, -2, -4), cPos.offset(4, 1, 4))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
            }
        }
        AABB box = new AABB(center.x - 8.0D, center.y - 2.5D, center.z - 8.0D, center.x + 8.0D, center.y + 4.5D, center.z + 8.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 70, 0));
            foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 80);
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.6D, center.z, 36, 2.2D, 0.5D, 2.2D, 0.05D);
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
        // Sustained Cryogenic Vortex Ray & Zero-Point Shockwave: -2 armor/s, grounds flyers, douses fires;
        // 5.0s continuous -> 12m KB, -7 armor, purges buffs, flash-freezes 5.0s (100t)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 80L);
            hit.clearFire();
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 60, hit.getTicksFrozen() + 45));
            if (!hit.onGround()) {
                hit.setDeltaMovement(hit.getDeltaMovement().x, -0.85D, hit.getDeltaMovement().z);
                hit.hasImpulse = true;
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("TerminusBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("TerminusBeamStacks").orElse(0) + 1 : 1;
            data.putLong("TerminusBeamLastTick", now);
            data.putInt("TerminusBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("TerminusBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 7.0D, 0.0D, 220L);
                hit.setDeltaMovement(dir.x * 2.4D, 0.42D, dir.z * 2.4D); // 12m KB!
                hit.hasImpulse = true;
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 120);
                hit.hurtServer(level, level.damageSources().freeze(), 16.0F + spell.getEffectiveTier() * 2.8F);
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
        // Absolute-Zero Cryo-Nova: 12m shockwave (ZERO light blocks!), 10m KB flat, -6 armor,
        // freezes water->Packed Ice & lava->Obsidian, freezes mobs 4.0s (80t), grants Glacial Mantle of Terminus 16.0s (320t)!
        double r = Math.max(12.0D, radius);
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-8, -2, -8), cPos.offset(8, 2, 8))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
            } else if (bs.is(Blocks.LAVA) && bs.getFluidState().isSource()) {
                level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }

        long mantleUntil = level.getGameTime() + 320L;
        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 5.0D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL, mantleUntil);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 12.0D, 6.0D, 0.0D, 0.0D, 320L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(2.05D); // 10m KB!
            ent.setDeltaMovement(away.x, 0.35D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 6.0D, 0.0D, 180L);
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
            ent.setTicksFrozen(ent.getTicksRequiredToFreeze() + 100);
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL, mantleUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, 320L);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 1.0D, center.z, 40, 3.0D, 0.5D, 3.0D, 0.06D);
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
        // Absolute-Zero Glacier Cleave: 60% bypass, -6 armor, disables shields 6.0s (120t),
        // encases victim 4.0s (80t) + 8m atmospheric frost-nova freezing surrounding hostiles 2.5s (50t)!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 120);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 100);

            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(8.0D),
                    e -> e.isAlive() && e != hit && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
                adj.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                adj.setTicksFrozen(adj.getTicksRequiredToFreeze() + 60);
            }
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
        int dur = 280 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(1.0D), caster.getZ(), 28, 0.5D, 0.6D, 0.5D, 0.04D);
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
        // Permafrost Glacial Abyss Sigil: 5m trigger -> 8m cryogenic vortex vacuuming inward, launching 12m up,
        // flash-freezing at apex for crash damage, -6 armor, strips buffs, encases survivors 4.0s (80t)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 5.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 1.0D, 0.05D, 1.0D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trig = new AABB(zone.center.x - 5.0D, zone.center.y - 1.5D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 2.5D, zone.center.z + 5.0D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard()))).isEmpty()) {
                        return true;
                    }

                    AABB vortex = new AABB(zone.center.x - 8.0D, zone.center.y - 2.0D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, vortex, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 200L);
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.35D);
                        foe.setDeltaMovement(in.x, 1.48D, in.z);
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
                        foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 100);
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_FALL_VULN_50_UNTIL, srv.getGameTime() + 120L);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
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
        // Cryogenic Blizzard Deluge: -6 armor, deflects projectiles, freezes water -> Packed Ice,
        // Cryogenic Molecular Hypothermia (30.0s / 600t AI disruption + 60% Slowness IV)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.deflectHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(coneRange))).inflate(3.5D));

        BlockPos mid = BlockPos.containing(caster.getEyePosition().add(dir.scale(Math.min(7.0D, coneRange * 0.5D))));
        for (BlockPos p : BlockPos.betweenClosed(mid.offset(-3, -2, -3), mid.offset(3, 1, 3))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
            }
        }

        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600, 3));
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 100);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 600);
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
        // Continuous Cryo-Stasis Hailstorm: 16m zone, 24.0s (480t) + 12.0s (240t) Absolute Zero Permafrost (720t total)!
        double r = Math.max(16.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 720, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean hail = elapsed <= 480;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + (hail ? 5.0D : 0.3D), zone.center.z,
                                28, zone.radius * 0.55D, hail ? 2.4D : 0.3D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner)) {
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 6.0D, 0.0D, 0.0D, 0.0D, 35L);
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, now + 40L);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                            ent.setTicksFrozen(ent.getTicksRequiredToFreeze() + 60);
                            if (hail) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 7.0D, 0.0D, 200L);
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.50F);
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
        tag.putString("SpellEidolonMateria", EssenceType.TERMINUS.name());
        tag.putInt("SpellEidolonTintRGB", TERMINUS_FROST_CYAN_HEX_COLOR);
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 28, 0.5D, 0.6D, 0.5D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Glacial Slipstream Flash-Slide: 22m through walls; departure flash-freeze roots pursuers 3.0s (60t);
        // arrival grants Frost Walker, fall immunity, +40% speed on ice for 10.0s (200t) & sheds 100% aggro!
        AABB dep = new AABB(departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 3.5D, departurePos.z + 6.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
            foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 80);
        }

        long now = level.getGameTime();
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, now + 200L);
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL, now + 200L);
        caster.resetFallDistance();
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 26.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Zero-Point Crystallization & Permafrost Synthesis [5.0s Channel, strictly NO air transmutation]:
        // Water -> Blue Ice; Lava -> Obsidian; Stone/Deepslate -> Packed Ice / Blue Ice;
        // 3m standing water + empty glass bottle -> Glacial Frost Elixirs!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 12, potencyTier, EssenceType.TERMINUS,
                "Zero-Point Crystallization & Permafrost Synthesis",
                () -> ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 3, EssenceType.TERMINUS))) {
            return true;
        }

        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA) && state.getFluidState().isSource()) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Absolute Zero Molecular Cessation Hex: true frostbite damage, -80% move speed, -60% attack speed,
        // -50% toughness, disables jumping!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.50D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 35, 2, true, true));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 60);
        target.hurtServer(level, level.damageSources().fellOutOfWorld(), (2.5F + potencyTier * 0.65F) * mult);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.85D), target.getZ(), 6, 0.3D, 0.35D, 0.3D, 0.02D);
        return true;
    }
}
