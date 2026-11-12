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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Spring Materia (Tier 3 Trinity Fusion #42)</b>
 * (Spellcrafting System Guide Section 6.43 — Vitae + Nature + Water).
 */
public final class SpringSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VERNAL_SPRING_HEX_COLOR = 0xFF6B9D;

    @Override
    public EssenceType materiaType() {
        return EssenceType.SPRING;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Vernal Blossom-Aquifer Spiral Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Vernal Blossom Aquifer-Orb";
            case BEAM -> "Photosynthetic Life-Tether";
            case RADIAL_BURST -> "Vernal Rebirth Blossom-Nova";
            case TOUCH_STRIKE -> "Vernal Blossom-Cleave";
            case AURA -> "Vernal Fountainhead Mantle";
            case GLYPH -> "Bubbling Vernal Aquifer Well Glyph";
            case CONE -> "Vernal Rejuvenation Torrent Cone";
            case SKYFALL -> "Vernal Miracle Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Petal-Warp Blink";
            case TRANSMUTE, BREAK -> "Vernal Purification & Greening Transmutation";
            case AFFLICTION -> "Uncontrolled Vernal Overgrowth Hex";
            default -> "Vernal Spring " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Vernal Blossom-Aquifer Spiral Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Vernal Blossom Aquifer-Orb: heals allies 3 hearts (6.0 HP) + Regen II 4.0s (80t);
        // roots Undead 3.0s (60t) and knocks hostiles 5m back!
        if (!(hitEntity instanceof LivingEntity target) || !target.isAlive()) return;
        if (caster != null && isFriendlyTarget(target, caster)) {
            SpellCombatReactionEngine.notifyMagicalHeal(target, 6.0F);
            target.heal(6.0F);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1));
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY(1.0D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) return;
        if (target.getType().is(EntityTypeTags.UNDEAD)) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
        } else {
            Vec3 kb = projectile.getDeltaMovement().normalize().scale(1.25D);
            target.setDeltaMovement(kb.x, 0.32D, kb.z);
            target.hasImpulse = true;
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
        // Photosynthetic Life-Tether: on hostiles -> siphons 3 HP/s & Slowness III; on allies -> cures debuffs & +30% attack speed;
        // REQUIRES 5.0s sustained (stacks >= 5) -> Vernal Bloom Supercharge (8m KB + +4 Absorption Hearts to allies)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 3.0F);
                hit.heal(3.0F);
                SpellCombatReactionEngine.applyTimedAttackSpeedBuff(hit, level, 0.30D, 80L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));
            if (caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 3.0F);
                caster.heal(3.0F);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("SpringBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("SpringBeamStacks").orElse(0) + 1 : 1;
            data.putLong("SpringBeamLastTick", now);
            data.putInt("SpringBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("SpringBeamStacks", 0);
                Vec3 away = direction.lengthSqr() > 1.0E-4D ? direction.normalize().scale(1.85D) : caster.getLookAngle().scale(1.85D);
                hit.setDeltaMovement(away.x, 0.42D, away.z);
                hit.hasImpulse = true;
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 9.0F + spell.getEffectiveTier() * 2.2F);
                caster.setAbsorptionAmount(Math.max(caster.getAbsorptionAmount(), 8.0F));
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(8.0D), e -> isFriendlyTarget(e, caster))) {
                    ally.setAbsorptionAmount(Math.max(ally.getAbsorptionAmount(), 8.0F));
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.42F, 0.62F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
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
        // Vernal Rebirth Blossom-Nova: 6m radius, strictly ZERO physical light blocks — 7m KB, matures crops/saplings,
        // grants caster & nearby allies Vernal Vigor (+20% move speed, +3 Armor, Regeneration II for 8.0s / 160t)!
        double r = Math.max(6.0D, radius);
        grantVernalVigor(caster, level);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                grantVernalVigor(ent, level);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(1.65D);
            ent.setDeltaMovement(away.x, 0.38D, away.z);
            ent.hasImpulse = true;
        }
        matureCropsInRadius(level, BlockPos.containing(center), 5);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.42F, 0.62F), center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, center.x, center.y + 1.0D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.05D);
    }

    private static void grantVernalVigor(LivingEntity ally, ServerLevel level) {
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 3.0D, 0.0D, 0.0D, 0.20D, 160L);
        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
    }

    public static void matureCropsInRadius(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BonemealableBlock growable && !(bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.MOSS_BLOCK))) {
                if (growable.isValidBonemealTarget(level, p, bs)) {
                    growable.performBonemeal(level, level.random, p.immutable(), bs);
                }
            }
        }
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
        // Vernal Blossom-Cleave: Touch of Spring on allies (purges debuffs + 3 absorption hearts);
        // on hostiles -> strips 3 armor on Undead, 5m KB, crescent water arc rooting foes 2.5s (50t) & healing allies 2 hearts!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.setAbsorptionAmount(Math.max(hit.getAbsorptionAmount(), 6.0F));
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            }
            hit.setDeltaMovement(push.x * 1.25D, 0.32D, push.z * 1.25D);
            hit.hasImpulse = true;
        }
        Vec3 arcPos = caster.position().add(push.scale(2.4D));
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcPos, 2.5D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                ent.heal(4.0F);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                                ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
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
        // Vernal Fountainhead Mantle: +6 Armor, +2 Toughness, Water Breathing, Drowning/Poison/Wither/Fire immunity, 5m restorative aura!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SPRING_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.0D, 0.0D, dur);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur + 40, 0, false, false));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, false));
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.03D);
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
        // Bubbling Vernal Aquifer Well Glyph: Dual-purpose active font —
        // Ally step -> Restorative Spring (cleanses debuffs, heals 5 hearts / 10 HP, Regen II 6.0s);
        // Hostile step -> Geyser Chasm (4.0s / 80t airborne launch, strips 3 armor, drowns Undead)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 0.7D, 0.1D, 0.7D, 0.02D);
                        srv.sendParticles(ParticleTypes.CHERRY_LEAVES, zone.center.x, zone.center.y + 0.25D, zone.center.z, 2, 0.6D, 0.1D, 0.6D, 0.01D);
                    }
                    if (elapsed < 12) return true;

                    AABB trigger = new AABB(zone.center.x - 2.2D, zone.center.y - 1.0D, zone.center.z - 2.2D,
                            zone.center.x + 2.2D, zone.center.y + 2.0D, zone.center.z + 2.2D);
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trigger, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    for (LivingEntity st : steppers) {
                        if (owner != null && isFriendlyTarget(st, owner)) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            SpellCombatReactionEngine.notifyMagicalHeal(st, 10.0F);
                            st.heal(10.0F);
                            st.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
                            srv.sendParticles(ParticleTypes.HEART, st.getX(), st.getY(1.0D), st.getZ(), 8, 0.35D, 0.4D, 0.35D, 0.03D);
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.25F);
                            return false;
                        }
                    }

                    List<LivingEntity> foes = steppers.stream()
                            .filter(e -> owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard()))
                            .toList();
                    if (foes.isEmpty()) return true;

                    for (LivingEntity foe : foes) {
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 3.0D, 0.0D, 160L);
                        foe.setDeltaMovement(0.0D, 0.95D, 0.0D);
                        foe.hasImpulse = true;
                        if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                            SpellLingeringZoneManager.applyAbyssLandDrowning(foe, 80);
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.6D, zone.center.z, 36, 1.2D, 0.8D, 1.2D, 0.12D);
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
        // Vernal Rejuvenation Torrent Cone: drenches allies (1.5 hearts / 3 HP heal & extinguishes fire);
        // on hostiles -> 3m KB, -50% attack speed, & 25.0s (500t) AI pathfinding disruption!
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) {
                hit.clearFire();
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 3.0F);
                hit.heal(3.0F);
                continue;
            }
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
        // Vernal Miracle Downpour: Extended 18.0s (360t) storm + 8.0s (160t) verdant sanctuary (520t total) across 12m radius —
        // pins hostiles downward & strips 5 armor; heals allies 1 heart/s, satisfies hunger, Regen II + Resistance I, matures crops!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.CHERRY_LEAVES, zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.4D), zone.center.z,
                                20, zone.radius * 0.55D, stormActive ? 1.8D : 0.2D, zone.radius * 0.55D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        matureCropsInRadius(srv, BlockPos.containing(zone.center), 6);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 2.0F);
                                ent.heal(2.0F);
                                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1));
                                ent.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0));
                                if (ent instanceof Player p) {
                                    p.getFoodData().eat(1, 0.3F);
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3));
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 5.0D, 0.0D, 160L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.3D, -0.32D, ent.getDeltaMovement().z * 0.3D);
                                ent.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
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
        tag.putString("SpellEidolonMateria", EssenceType.SPRING.name());
        tag.putInt("SpellEidolonTintRGB", VERNAL_SPRING_HEX_COLOR);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Petal-Warp Blink: 14m step shedding all hostile aggro and rendering caster undetectable to Undead for 4.0s (80t)!
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SPRING_UNDEAD_STEALTH_UNTIL, level.getGameTime() + 80L);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.35D, 0.45D, 0.35D, 0.03D);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 20, 0.35D, 0.45D, 0.35D, 0.03D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Vernal Purification & Greening Transmutation: cures Zombie Villagers, Netherrack/SoulSand/Sculk -> Grass/Moss/Farmland,
        // hydrates Farmland across 6m, Rotten Flesh -> Leather/Fresh Beef, matures crops!
        boolean didAny = cureNearbyZombieVillagers(level, pos);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didAny = true;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-6, -1, -6), pos.offset(6, 1, 6))) {
            BlockState fs = level.getBlockState(p);
            if (fs.is(Blocks.FARMLAND) && fs.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlockAndUpdate(p, fs.setValue(FarmBlock.MOISTURE, 7));
                didAny = true;
            }
        }
        matureCropsInRadius(level, pos, 3);
        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.SCULK)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> Blocks.GRASS_BLOCK.defaultBlockState();
                case 1 -> Blocks.MOSS_BLOCK.defaultBlockState();
                default -> Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        return didAny;
    }

    public static boolean cureNearbyZombieVillagers(ServerLevel level, BlockPos pos) {
        boolean cured = false;
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, new AABB(pos).inflate(3.5D), LivingEntity::isAlive)) {
            Villager villager = EntityType.VILLAGER.create(level, EntitySpawnReason.CONVERSION);
            if (villager != null) {
                villager.setPos(zv.getX(), zv.getY(), zv.getZ());
                villager.setYRot(zv.getYRot());
                villager.setXRot(zv.getXRot());
                villager.setVillagerData(zv.getVillagerData());
                if (zv.isBaby()) villager.setBaby(true);
                level.addFreshEntity(villager);
                zv.discard();
                level.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 1.0F);
                cured = true;
            }
        }
        return cured;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.LEATHER : Items.BEEF, stack.getCount()));
            return true;
        }
        if (stack.is(Items.NETHERRACK) || stack.is(Items.SOUL_SAND) || stack.is(Items.SCULK)) {
            itemEntity.setItem(new ItemStack(Items.MOSS_BLOCK, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Uncontrolled Vernal Overgrowth Hex: 1.0s (20t) stun every 4.0s, -50% move speed, -40% attack speed,
        // -35% toughness, extra damage vs Undead, drops collectable healing dew-orbs (1 heart / 2.0 HP)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2, true, true));
        if ((now / 20L) % 4L == 0L) {
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, target.position(), caster, 2.0F, false);
        }
        float dmg = (1.50F + potencyTier * 0.50F) * mult * (target.getType().is(EntityTypeTags.UNDEAD) ? 1.50F : 1.0F);
        target.hurtServer(level, level.damageSources().magic(), dmg);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    private static boolean isFriendlyTarget(LivingEntity entity, LivingEntity caster) {
        return entity == caster || entity.isAlliedTo(caster)
                || (entity instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()));
    }
}
