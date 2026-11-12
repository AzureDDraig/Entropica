package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Astral Materia (Ethereal &amp; Paradox Fusion #27)</b>
 * (Spellcrafting System Guide Section 6.28 — Void + Lightning [Leans: Void]).
 */
public final class AstralSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ASTRAL_CONSTRUCT_HEX_COLOR = 0xA08B27;

    @Override
    public EssenceType materiaType() {
        return EssenceType.ASTRAL;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Supercharged Void-Warp Chains Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Celestial Comet Missile";
            case BEAM -> "Pulsar Tractor Ray";
            case RADIAL_BURST -> "Supernova Gravitational Wave";
            case TOUCH_STRIKE -> "Cosmic Overcharge Strike";
            case AURA -> "Cosmic Starlight Shroud";
            case GLYPH -> "Cosmic Vortex Singularity Mine";
            case CONE -> "Pulsar Arc Spray";
            case SKYFALL -> "Celestial Meteor-Storm Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Astral Comet Dash";
            case TRANSMUTE, BREAK -> "Cosmic Disintegration Transmute";
            case AFFLICTION -> "Gravitational Singularity Tether Hex";
            default -> "Astral Cosmic " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Supercharged Void-Warp Chains Flow) ---

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Wall-phasing branched cosmic lightning: 1.0s (20t) shock stun & pulls nearby mobs inward!
        toTarget.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, toTarget.getBoundingBox().inflate(3.5D))) {
            if (near == toTarget || near == caster || !near.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 inward = toTarget.position().subtract(near.position()).normalize().scale(0.45D);
            near.setDeltaMovement(near.getDeltaMovement().add(inward.x, 0.14D, inward.z));
            near.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, toTarget.getX(), toTarget.getY(0.9D), toTarget.getZ(), 14, 0.3D, 0.35D, 0.3D, 0.1D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, toTarget.getX(), toTarget.getY(0.7D), toTarget.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.05D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.04D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(false);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.FIREWORK, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.02D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.05D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        detonateStarfallComet(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        detonateStarfallComet(level, caster, projectile.position(), spell);
    }

    private static void detonateStarfallComet(ServerLevel level, @Nullable LivingEntity caster, Vec3 pos, SpellAssembly spell) {
        // Celestial Comet Missile: starfall blast launching struck enemies 5 blocks airborne & powering redstone for 3s (60t)!
        AABB box = new AABB(pos.x - 3.0D, pos.y - 1.5D, pos.z - 3.0D, pos.x + 3.0D, pos.y + 2.5D, pos.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.4D, 0.90D, foe.getDeltaMovement().z * 0.4D);
            foe.hasImpulse = true;
        }
        SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, BlockPos.containing(pos), 3, 60);
        level.sendParticles(ParticleTypes.FIREWORK, pos.x, pos.y + 0.4D, pos.z, 24, 0.8D, 0.5D, 0.8D, 0.12D);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.85F);
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
        // Pulsar Tractor Ray: lifts enemies mid-air & pulls toward caster; 3s triggers Stellar Supernova Flash (2s stun + 4s redstone)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 pull = caster.position().subtract(hit.position()).normalize().scale(0.45D);
            hit.setDeltaMovement(pull.x, 0.30D, pull.z);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("AstralBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("AstralBeamStacks").orElse(0) + 1 : 1;
            data.putLong("AstralBeamLastTick", now);
            data.putInt("AstralBeamStacks", stacks);
            if (stacks >= 3) {
                data.putInt("AstralBeamStacks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, hit.blockPosition(), 4, 80);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.63F, 0.55F, 0.95F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
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
        // Supernova Gravitational Wave: 6m plasma wave launching hostiles 8 blocks back & 4 blocks airborne + 3s silence!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.85D);
            foe.setDeltaMovement(away.x, 0.80D, away.z);
            foe.hasImpulse = true;
            SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
        }
        level.sendParticles(ParticleTypes.FIREWORK, center.x, center.y + 0.9D, center.z, 40, 2.2D, 0.6D, 2.2D, 0.14D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 26, 2.0D, 0.5D, 2.0D, 0.08D);
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
        // Cosmic Overcharge Strike: 1.0s stun, arcs sparks to 2 nearby enemies (3m), 4-block zero-g launch (3s), caster +40% attack speed 10s!
        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(caster, level, 0.40D, 200L);
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
            hit.setDeltaMovement(direction.x * 0.35D, 0.78D, direction.z * 0.35D);
            hit.hasImpulse = true;

            int arced = 0;
            for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.0D))) {
                if (near == hit || near == caster || !near.isAlive()) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                near.hurtServer(level, level.damageSources().lightningBolt(), 3.0F + spell.getEffectiveTier() * 0.85F);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, near.getX(), near.getY(0.8D), near.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.08D);
                if (++arced >= 2) break;
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
        // Cosmic Starlight Shroud: high-float jump, zero-g slow-fall, +30% sprint speed, melee shock + knockback, Lightning/Void fuel absorption!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, dur, 1, false, true));
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.06D);
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
        // Cosmic Vortex Singularity Mine: 4m vortex pulling mobs to center, suspending 3m airborne for 3s (60t) while shocking + 5s (100t) redstone pulse!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.2D, zone.center.z, 5, 1.1D, 0.1D, 1.1D, 0.02D);
                    }
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    SpellLingeringZoneManager.pulseTemporaryRedstonePower(srv, BlockPos.containing(zone.center), 4, 100);
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (vLvl, vZone, vEl) -> {
                                if (vEl > vZone.maxDurationTicks) return false;
                                LivingEntity vOwner = vZone.resolveCaster(vLvl);
                                AABB vortexBox = new AABB(vZone.center.x - 4.0D, vZone.center.y - 1.0D, vZone.center.z - 4.0D,
                                        vZone.center.x + 4.0D, vZone.center.y + 5.0D, vZone.center.z + 4.0D);
                                for (LivingEntity v : vLvl.getEntitiesOfClass(LivingEntity.class, vortexBox, e -> e.isAlive() && e != vOwner)) {
                                    if (vOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, vOwner, vZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    Vec3 hoverTarget = vZone.center.add(0.0D, 3.0D, 0.0D);
                                    Vec3 pull = hoverTarget.subtract(v.position()).scale(0.28D);
                                    v.setDeltaMovement(pull);
                                    v.hasImpulse = true;
                                    if (vEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(vLvl, vOwner, v, new Vec3(0, 1, 0), vZone.spell, vZone.slot, vZone.fallbackMateria, vZone.apparatusPotency * 0.45F);
                                    }
                                }
                                if (vEl % 5 == 0) {
                                    vLvl.sendParticles(ParticleTypes.ELECTRIC_SPARK, vZone.center.x, vZone.center.y + 2.5D, vZone.center.z, 12, 1.2D, 1.0D, 1.2D, 0.08D);
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
        // Pulsar Arc Spray: 1.0s shock stun, 4m knockback, strips positive potion buffs, powers redstone crossed for 3s!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            hit.setDeltaMovement(direction.x * 1.05D, 0.30D, direction.z * 1.05D);
            hit.hasImpulse = true;
            EclipseSpellMutationHandler.purgeBeneficialEffects(hit);
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, BlockPos.containing(caster.position().add(direction.scale(s))), 2, 60);
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
        // Celestial Meteor-Storm Cataclysm: 10s (200t) cosmic rift — 0.5s stuns, zero-g float lift, heavy downward slam when rift expires!
        double r = Math.max(10.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                    if (elapsed >= zone.maxDurationTicks) {
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.removeEffect(MobEffects.SLOW_FALLING);
                            v.setDeltaMovement(v.getDeltaMovement().x, -1.65D, v.getDeltaMovement().z);
                            v.hasImpulse = true;
                            v.hurtServer(srv, srv.damageSources().magic(), 6.0F + zone.spell.getEffectiveTier() * 1.5F);
                        }
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.75F);
                        return false;
                    }
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.FIREWORK, zone.center.x, zone.center.y + 5.0D, zone.center.z, 20, zone.radius * 0.55D, 1.5D, zone.radius * 0.55D, 0.08D);
                    }
                    if (elapsed % 20 == 0) {
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0));
                            v.setDeltaMovement(v.getDeltaMovement().x, 0.38D, v.getDeltaMovement().z);
                            v.hasImpulse = true;
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.ASTRAL.name());
        tag.putInt("SpellEidolonTintRGB", ASTRAL_CONSTRUCT_HEX_COLOR);
        level.sendParticles(ParticleTypes.FIREWORK, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.08D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Astral Comet Dash: 18m starfire streak knocking aside mobs and granting Slow Falling!
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
        Vec3 delta = arrivalPos.subtract(departurePos);
        int steps = Math.max(2, (int) Math.ceil(delta.length()));
        Vec3 stepVec = delta.scale(1.0D / steps);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(ParticleTypes.FIREWORK, p.x, p.y + 0.8D, p.z, 3, 0.15D, 0.2D, 0.15D, 0.03D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - 1.6D, p.y - 0.5D, p.z - 1.6D, p.x + 1.6D, p.y + 2.0D, p.z + 1.6D))) {
                if (foe == caster || !foe.isAlive() || foe.isAlliedTo(caster) || !(foe instanceof Monster)) continue;
                Vec3 side = foe.position().subtract(p).normalize().scale(0.95D);
                foe.setDeltaMovement(side.x, 0.38D, side.z);
                foe.hasImpulse = true;
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Cosmic Disintegration Transmute: dissolves cobble/stone/dirt into raw void slag / Astral Impactite & redstone dust!
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.STONE)) {
            level.setBlockAndUpdate(pos, ModBlocks.ASTRAL_IMPACTITE.get().defaultBlockState());
            Block.popResource(level, pos.above(), new ItemStack(Items.REDSTONE, 1 + level.random.nextInt(2)));
            return true;
        }
        if (state.is(BlockTags.DIRT)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(Items.REDSTONE, 1 + level.random.nextInt(2)));
            if (level.random.nextFloat() < 0.35F) {
                Block.popResource(level, pos, new ItemStack(ModItems.STARLIGHT_FLAKES.get(), 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.COBBLESTONE) || in.is(Items.DIRT) || in.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.REDSTONE, in.getCount()));
            if (level.random.nextFloat() < 0.50F) {
                ItemEntity slag = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(0.2D), itemEntity.getZ(),
                        new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), Math.max(1, in.getCount() / 2)));
                level.addFreshEntity(slag);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Gravitational Singularity Tether Hex: 70% reduced speed (Slowness IV), disables jumping & sprinting, pulls adjacent mobs into victim!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3, true, true));
        target.setSprinting(false);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 35L);
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0D))) {
            if (adj == target || !adj.isAlive() || adj instanceof net.minecraft.world.entity.player.Player) continue;
            Vec3 pull = target.position().subtract(adj.position()).normalize().scale(0.42D);
            adj.setDeltaMovement(adj.getDeltaMovement().add(pull.x, 0.10D, pull.z));
            adj.hasImpulse = true;
        }
        target.hurtServer(level, level.damageSources().magic(), (1.4F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.04D);
        return true;
    }
}
