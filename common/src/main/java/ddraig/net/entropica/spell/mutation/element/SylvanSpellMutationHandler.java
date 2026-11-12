package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
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
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
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
 * Complete per-part Signature Mutation implementation for <b>Sylvan Materia (Tier 3 Trinity Fusion #44)</b>
 * (Spellcrafting System Guide Section 6.45 — Nature + Earth + Radiant).
 */
public final class SylvanSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PRIMEVAL_SYLVAN_HEX_COLOR = 0x1A5923;

    @Override
    public EssenceType materiaType() {
        return EssenceType.SYLVAN;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Photosynthetic Sun-Canopy Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Elderwood Sun-Bough Javelin";
            case BEAM -> "Canopy Solar-Root Tether Beam";
            case RADIAL_BURST -> "Primeval Sylvan Canopy Nova";
            case TOUCH_STRIKE -> "Elderwood Canopy-Cleave";
            case AURA -> "Primeval Photosynthetic Aegis";
            case GLYPH -> "Canopy Solar-Mine";
            case CONE -> "Canopy Solar-Flare Breath";
            case SKYFALL -> "Sun-Canopy Orbital Meteor Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Canopy Solar-Glide Step";
            case TRANSMUTE, BREAK -> "Petrified Amber-Bark Fusion";
            case AFFLICTION -> "Petrifying Arbor-Root Entombment Hex";
            default -> "Primeval Sylvan " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Photosynthetic Sun-Canopy Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Elderwood Sun-Bough Javelin: roots primary target 3.0s (60t), shatters 3 armor points,
        // and sprouts an entangled briar cluster slowing nearby hostiles by 50% and damaging them for 4.0s (80t)!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard()))) {
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(victim, level, 3.0D, 0.0D, 140L);
        }
        sproutBriarCluster(level, caster, hitEntity.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        sproutBriarCluster(level, caster, blockHit.getLocation(), spell);
    }

    private static void sproutBriarCluster(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 2.8D, 80, level.getGameTime(), spell, null, EssenceType.SYLVAN, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 2.8D, zone.center.y - 1.0D, zone.center.z - 2.8D,
                                zone.center.x + 2.8D, zone.center.y + 2.0D, zone.center.z + 2.8D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2)); // 50% slow
                            foe.hurtServer(srv, srv.damageSources().cactus(), 2.0F);
                        }
                        srv.sendParticles(ParticleTypes.HAPPY_VILLAGER, zone.center.x, zone.center.y + 0.3D, zone.center.z, 8, 1.0D, 0.2D, 1.0D, 0.02D);
                    }
                    return true;
                }
        ));
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
        // Canopy Solar-Root Tether Beam: drags foes closer, smites Undead, heals friendly allies (1 heart / 2.0 HP);
        // REQUIRES 5.0s sustained (stacks >= 5) -> Primeval Arbor Overgrowth (8m KB + 3.0s / 60t wooden root cages)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 2.0F);
                hit.heal(2.0F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 pull = caster.position().subtract(hit.position()).normalize().scale(0.38D);
            hit.setDeltaMovement(pull.x, 0.08D, pull.z);
            hit.hasImpulse = true;
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.igniteForSeconds(3);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("SylvanBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("SylvanBeamStacks").orElse(0) + 1 : 1;
            data.putLong("SylvanBeamLastTick", now);
            data.putInt("SylvanBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("SylvanBeamStacks", 0);
                Vec3 away = direction.lengthSqr() > 1.0E-4D ? direction.normalize().scale(1.85D) : caster.getLookAngle().scale(1.85D);
                hit.setDeltaMovement(away.x, 0.42D, away.z);
                hit.hasImpulse = true;
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 9.5F + spell.getEffectiveTier() * 2.3F);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
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
        // Primeval Sylvan Canopy Nova: 6m radius, strictly ZERO physical light blocks — 7m KB, roots 3.0s (60t),
        // grows crops/saplings, grants caster & nearby allies Ironwood Barkskin (+6 Armor, +2 Toughness, KB Res for 8.0s / 160t)!
        double r = Math.max(6.0D, radius);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.50D, 0.0D, 160L);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 6.0D, 2.0D, 0.50D, 0.0D, 160L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(1.65D);
            ent.setDeltaMovement(away.x, 0.38D, away.z);
            ent.hasImpulse = true;
            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            if (ent.getType().is(EntityTypeTags.UNDEAD)) {
                ent.igniteForSeconds(5);
            }
        }
        SpringSpellMutationHandler.matureCropsInRadius(level, BlockPos.containing(center), 5);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F), center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y + 0.9D, center.z, 32, 2.2D, 0.6D, 2.2D, 0.05D);
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
        // Elderwood Canopy-Cleave: strips 3 armor, 6m KB, roots 2.5s (50t), crescent foliage arc rooting foes 2.0s & Regen I to allies!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.setDeltaMovement(push.x * 1.45D, 0.35D, push.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
        }
        Vec3 arcPos = caster.position().add(push.scale(2.4D));
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcPos, 2.5D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0));
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                            }
                        }
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
        // Primeval Photosynthetic Aegis: +7 Armor, +3 Toughness, Poison/Wither/Blindness immunity,
        // absorbs up to 12 incoming damage instances converting 50% directly into bonus Materia fuel!
        int dur = 200 + spell.getEffectiveTier() * 40;
        CompoundTag tag = EntityHelper.getPersistentData(caster);
        tag.putLong(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_UNTIL, level.getGameTime() + dur);
        tag.putInt(SpellLingeringZoneManager.TAG_SYLVAN_AEGIS_HITS, 12);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.03D);
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
        // Canopy Solar-Mine: proximity 3m floating leaf-prism mine — blinds 5.0s (100t), launches airborne, strips 3 armor!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.6D, zone.center.z, 3, 0.5D, 0.2D, 0.5D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.5D, zone.center.z + 3.0D);
                    List<LivingEntity> foes = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (foes.isEmpty()) return true;

                    for (LivingEntity v : foes) {
                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, 3.0D, 0.0D, 160L);
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.4D, 0.88D, v.getDeltaMovement().z * 0.4D);
                        v.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F), zone.center.x, zone.center.y + 0.8D, zone.center.z, 1, 0, 0, 0, 0);
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
        // Canopy Solar-Flare Breath: purges poison/wither from allies; ignites enemies in arbor flame,
        // Solar Arbor Blindness (-50% attack speed & 25.0s / 500t AI pathfinding disruption)!
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) {
                hit.removeEffect(MobEffects.POISON);
                hit.removeEffect(MobEffects.WITHER);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(4);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
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
        // Sun-Canopy Orbital Meteor Barrage: Extended 16.0s (320t) barrage striking every 0.4s (8t) + 8.0s (160t) lingering root field (480t total)
        // across 10m zone — launches airborne, shatters 5 armor, smites Undead, 60% slow!
        double r = Math.max(10.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 480, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean barrageActive = elapsed <= 320;
                    if (elapsed % 8 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        if (barrageActive) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = srv.random.nextDouble() * (zone.radius - 1.0D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.5D, Math.sin(ang) * dist);
                            srv.sendParticles(ParticleTypes.END_ROD, strike.x, strike.y + 1.5D, strike.z, 12, 0.8D, 1.0D, 0.8D, 0.06D);
                        }
                        if (elapsed % 16 == 0) {
                            AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                    zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                            for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow
                                if (barrageActive) {
                                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                    foe.setDeltaMovement(foe.getDeltaMovement().x * 0.4D, 0.48D, foe.getDeltaMovement().z * 0.4D);
                                    foe.hasImpulse = true;
                                    SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
                                }
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
        tag.putString("SpellEidolonMateria", EssenceType.SYLVAN.name());
        tag.putInt("SpellEidolonTintRGB", PRIMEVAL_SYLVAN_HEX_COLOR);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Canopy Solar-Glide Step: 16m upward/forward step granting Slow Falling 6.0s (120t) and shedding hostile aggro!
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0));
        caster.fallDistance = 0.0F;
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 18, 0.4D, 0.4D, 0.4D, 0.04D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Petrified Amber-Bark Fusion: logs -> Amber Wood Logs, clay -> Terracotta, accelerates crops across 6m,
        // purifies Netherrack/Soul Sand -> Moss Blocks & Ferns!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didAny = true;
        }
        SpringSpellMutationHandler.matureCropsInRadius(level, pos, 6);
        if (state.is(BlockTags.LOGS) && !state.is(ModBlocks.AMBER_LOG.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AMBER_LOG.get().withPropertiesOf(state));
            return true;
        }
        if (state.is(Blocks.CLAY)) {
            level.setBlockAndUpdate(pos, Blocks.YELLOW_GLAZED_TERRACOTTA.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            level.setBlockAndUpdate(pos, Blocks.MOSS_BLOCK.defaultBlockState());
            if (level.getBlockState(pos.above()).isAir()) {
                level.setBlockAndUpdate(pos.above(), Blocks.FERN.defaultBlockState());
            }
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.LOGS)) {
            itemEntity.setItem(new ItemStack(ModBlocks.AMBER_LOG.get().asItem(), stack.getCount()));
            return true;
        }
        if (stack.is(Items.CLAY)) {
            itemEntity.setItem(new ItemStack(Items.TERRACOTTA, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Petrifying Arbor-Root Entombment Hex: 1.0s (20t) stun every 3.5s, -60% move speed, -40% attack speed,
        // -35% toughness, +50% vs Undead, drops collectable golden acorn healing orbs (1 heart / 2.0 HP)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 3, true, true)); // -60% speed
        if ((now / 20L) % 4L == 0L) {
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, target.position(), caster, 2.0F, true);
        }
        float dmg = (1.55F + potencyTier * 0.50F) * mult * (target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F);
        target.hurtServer(level, level.damageSources().magic(), dmg);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    private static boolean isFriendlyTarget(LivingEntity entity, LivingEntity caster) {
        return entity == caster || entity.isAlliedTo(caster)
                || (entity instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()));
    }
}
