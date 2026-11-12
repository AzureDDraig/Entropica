package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
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
 * Complete per-part Signature Mutation implementation for <b>Cataclysm Materia (Tier 5 Eschaton Fusion #53)</b>
 * (Spellcrafting System Guide Section 6.55 — Void + Umbral + Nether: "The World Breaker").
 */
public final class CataclysmSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CATACLYSM_STYGIAN_CRIMSON_HEX_COLOR = 0xB83C08;

    @Override
    public EssenceType materiaType() {
        return EssenceType.CATACLYSM;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Singularity Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Catastrophic Hellfire Railgun Slug";
            case BEAM -> "Tectonic Magma Fissure Ray & Seismic Cataclysm";
            case RADIAL_BURST -> "World-Breaker Seismic Nova";
            case TOUCH_STRIKE -> "Tectonic Calamity Sunder";
            case AURA -> "Mantle of the World-Breaker";
            case GLYPH -> "Abyssal Calamity Caldera Sigil";
            case CONE -> "Cataclysmic Magma-Ash Deluge";
            case SKYFALL -> "Cataclysmic World-Breaker Skyfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Gravitational Void-Warp Collapse";
            case TRANSMUTE, BREAK -> "Tectonic Subduction & Magma Vitrification";
            case AFFLICTION -> "Abyssal World-Breaker Singularity Hex";
            default -> "World-Breaker Cataclysm " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Singularity Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.4D), null, 0L);
        level.sendParticles(ParticleTypes.LAVA, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Catastrophic Hellfire Railgun Slug: each penetrated entity triggers a secondary volcanic magma eruption
        // knocking mobs flat, shredding 35% armor toughness, and inflicting unquenchable stygian burns!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 pos = hitEntity.position();
        AABB erupt = new AABB(pos.x - 3.5D, pos.y - 1.5D, pos.z - 3.5D, pos.x + 3.5D, pos.y + 3.0D, pos.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, erupt, LivingEntity::isAlive)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyPercentageArmorShred(foe, level, 0.0D, 0.35D, 160L);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.45D, foe.getDeltaMovement().z * 0.25D);
            foe.hasImpulse = true;
            foe.igniteForSeconds(7);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 140L);
            if (foe != hitEntity) {
                foe.hurtServer(level, level.damageSources().onFire(), 5.0F + spell.getEffectiveTier() * 1.3F);
            }
        }
        level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 0.6D, pos.z, 14, 0.6D, 0.4D, 0.6D, 0.05D);
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
        // Tectonic Magma Fissure Ray & Seismic Cataclysm: -2 armor/s, pulls into core, disintegrates arrows;
        // 5.0s continuous -> 14m KB, -7 armor, purges buffs, 12m molten caldera (70% slow & burns underwater)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB rayBox = new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(maxReach))).inflate(2.5D);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, rayBox, null, 0L);

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 80L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 60L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("CataclysmBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("CataclysmBeamStacks").orElse(0) + 1 : 1;
            data.putLong("CataclysmBeamLastTick", now);
            data.putInt("CataclysmBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("CataclysmBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 7.0D, 0.0D, 220L);
                hit.setDeltaMovement(dir.x * 2.75D, 0.50D, dir.z * 2.75D); // 14m KB!
                hit.hasImpulse = true;
                hit.hurtServer(level, level.damageSources().fellOutOfWorld(), 16.0F + spell.getEffectiveTier() * 2.8F);

                Vec3 calderaCenter = hit.position();
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, calderaCenter, 12.0D, 120, now, spell, slot, fallbackMateria, apparatusPotency,
                        (srv, zone, elapsed) -> {
                            if (elapsed > zone.maxDurationTicks) return false;
                            if (elapsed % 20 == 0) {
                                LivingEntity owner = zone.resolveCaster(srv);
                                AABB cal = new AABB(zone.center.x - 12.0D, zone.center.y - 2.0D, zone.center.z - 12.0D,
                                        zone.center.x + 12.0D, zone.center.y + 4.0D, zone.center.z + 12.0D);
                                for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cal, e -> e.isAlive() && e != owner)) {
                                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4)); // 70% slow
                                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, srv.getGameTime() + 50L);
                                    foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 4.0F);
                                }
                                srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.3D, zone.center.z, 20, 4.0D, 0.3D, 4.0D, 0.04D);
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
        // World-Breaker Seismic Nova: 12m shockwave (ZERO light blocks!), 12m KB flat, -6 armor,
        // disables shields 6.0s (120t), leaves bubbling molten obsidian cracks!
        double r = Math.max(12.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 5.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(2.4D); // 12m KB!
            foe.setDeltaMovement(away.x, 0.35D, away.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(foe, 120);
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 100, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB cracks = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 2.5D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cracks, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.igniteForSeconds(4);
                            foe.hurtServer(srv, srv.damageSources().onFire(), 3.5F);
                        }
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.2D, zone.center.z, 16, 3.5D, 0.2D, 3.5D, 0.03D);
                    }
                    return true;
                }
        ));
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
        // Tectonic Calamity Sunder: 60% bypass, -6 armor, disables shields 6.0s (120t),
        // opens 10m fissure dragging hostiles inward & burning 2.0 hearts/s (4.0 HP)!
        Vec3 fissureOrigin = hitEntities.isEmpty() ? caster.position().add(caster.getLookAngle().scale(2.5D)) : hitEntities.getFirst().position();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 120);
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, fissureOrigin, 10.0D, 100, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB fis = new AABB(zone.center.x - 10.0D, zone.center.y - 2.0D, zone.center.z - 10.0D,
                                zone.center.x + 10.0D, zone.center.y + 3.5D, zone.center.z + 10.0D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, fis, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 in = zone.center.subtract(foe.position()).scale(0.28D);
                            foe.setDeltaMovement(foe.getDeltaMovement().add(in.x, 0.0D, in.z));
                            foe.hasImpulse = true;
                            foe.igniteForSeconds(4);
                            foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 4.0F);
                        }
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.3D, zone.center.z, 16, 3.0D, 0.2D, 3.0D, 0.04D);
                    }
                    return true;
                }
        ));
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
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.LAVA, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Abyssal Calamity Caldera Sigil: 5m trigger -> 8m caldera vacuuming inward, launching 14m straight up,
        // -6 armor, strips buffs, Wither III + Stygian Conflagration 6.0s (120t), then slamming down!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 5.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.15D, zone.center.z, 3, 1.0D, 0.05D, 1.0D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trig = new AABB(zone.center.x - 5.0D, zone.center.y - 1.5D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 2.5D, zone.center.z + 5.0D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard()))).isEmpty()) {
                        return true;
                    }

                    long now = srv.getGameTime();
                    AABB caldera = new AABB(zone.center.x - 8.0D, zone.center.y - 2.0D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, caldera, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 200L);
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.38D);
                        foe.setDeltaMovement(in.x, 1.62D, in.z); // 14m vertical launch!
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));
                        foe.igniteForSeconds(6);
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
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
        // Cataclysmic Magma-Ash Deluge: -6 armor, vaporizes projectiles, incinerates wood, boils water,
        // Tectonic Vertigo Panic (30.0s / 600t AI disruption + 60% slow + blind terror)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(coneRange))).inflate(3.5D), null, 0L);

        BlockPos mid = BlockPos.containing(caster.getEyePosition().add(dir.scale(Math.min(7.0D, coneRange * 0.5D))));
        for (BlockPos p : BlockPos.betweenClosed(mid.offset(-3, -2, -3), mid.offset(3, 2, 3))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            } else if ((bs.is(BlockTags.LOGS) || bs.is(BlockTags.PLANKS)) && level.random.nextFloat() < 0.35F) {
                level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
            }
        }

        long fearUntil = level.getGameTime() + 200L;
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600, 3));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 600);
            CompoundTag d = EntityHelper.getPersistentData(hit);
            d.putLong(SpellLingeringZoneManager.TAG_FEARED_UNTIL, fearUntil);
            d.putDouble(SpellLingeringZoneManager.TAG_FEAR_X, caster.getX());
            d.putDouble(SpellLingeringZoneManager.TAG_FEAR_Z, caster.getZ());
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
        // Cataclysmic World-Breaker Skyfall: 16m zone, 24.0s (480t) + 12.0s (240t) Stygian Magma Slag (720t total)!
        double r = Math.max(16.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 720, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean storm = elapsed <= 480;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + (storm ? 5.0D : 0.3D), zone.center.z,
                                22, zone.radius * 0.55D, storm ? 2.4D : 0.3D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (storm) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 7.0D, 0.0D, 200L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.45D, foe.getDeltaMovement().z * 0.25D);
                                foe.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.55F);
                            } else {
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4));
                                foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 2));
                                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 50L);
                                foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 4.5F);
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
        tag.putString("SpellEidolonMateria", EssenceType.CATACLYSM.name());
        tag.putInt("SpellEidolonTintRGB", CATACLYSM_STYGIAN_CRIMSON_HEX_COLOR);
        level.sendParticles(ParticleTypes.LAVA, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 24, 0.5D, 0.6D, 0.5D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Gravitational Void-Warp Collapse: 22m through walls; departure crushes arrows, pulls inward, stuns 2.5s (50t);
        // arrival grants fire & fall immunity 8.0s (160t) and sheds 100% mob aggro!
        AABB dep = new AABB(departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 4.0D, departurePos.z + 6.0D);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, dep, null, 0L);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 in = departurePos.subtract(foe.position()).scale(0.38D);
            foe.setDeltaMovement(in.x, 0.2D, in.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
        }

        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 160, 0));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 160, 0));
        caster.resetFallDistance();
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 26.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Tectonic Subduction & Magma Vitrification [5.0s Channel]:
        // Stone/Deepslate/Cobble -> Obsidian/Crying Obsidian/Blackstone; smelts raw ores in 12m with +35% bonus yield & nuggets;
        // Sand/Red Sand -> Glass; melts damaged equipment into Void Slag & ingots!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 12, potencyTier, EssenceType.CATACLYSM,
                "Tectonic Subduction & Magma Vitrification", null)) {
            return true;
        }

        Item smeltedOre = ApexEschatonSingularityHelper.resolveSmeltedBlockOreOutput(state.getBlock());
        if (smeltedOre != null) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            int count = (level.random.nextFloat() < 0.35F) ? 2 : 1;
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(smeltedOre, count)));
            Item nugget = smeltedOre == Items.GOLD_INGOT ? Items.GOLD_NUGGET : Items.IRON_NUGGET;
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(nugget, 2)));
            return true;
        }

        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.COBBLESTONE)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.OBSIDIAN.defaultBlockState();
                case 1 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                default -> Blocks.BLACKSTONE.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        Item smelted = ApexEschatonSingularityHelper.resolveSmeltedOreOutput(stack.getItem());
        if (smelted != null) {
            int boosted = Math.min(64, Math.max(stack.getCount() + 1, Math.round(stack.getCount() * 1.35F)));
            itemEntity.setItem(new ItemStack(smelted, boosted));
            Item nugget = smelted == Items.GOLD_INGOT ? Items.GOLD_NUGGET : Items.IRON_NUGGET;
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(nugget, Math.min(64, stack.getCount() * 2))));
            return true;
        }

        Item scrapIngot = ApexEschatonSingularityHelper.resolveDamagedMetalScrapIngot(stack);
        if (scrapIngot != null) {
            itemEntity.setItem(new ItemStack(scrapIngot, 2));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), 1)));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Abyssal World-Breaker Singularity Hex: inward gravitational crush + unquenchable void-nether fire underwater,
        // -70% move speed, blocks jumping, -50% toughness, 50% weapon recoil!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.50D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4, true, true));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
        target.hurtServer(level, level.damageSources().fellOutOfWorld(), (2.5F + potencyTier * 0.65F) * mult);
        level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.7D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }
}
