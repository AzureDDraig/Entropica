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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Miasma Materia (Tier 3 Trinity Fusion #45)</b>
 * (Spellcrafting System Guide Section 6.46 — Nature + Undead + Water).
 */
public final class MiasmaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PESTILENT_MIASMA_HEX_COLOR = 0x225C7C;

    @Override
    public EssenceType materiaType() {
        return EssenceType.MIASMA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Virulent Contagion-Chain Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Putrid Swampland Gas-Pod";
            case BEAM -> "Decomposing Quagmire Siphon Ray & Sinkhole";
            case RADIAL_BURST -> "Putrescent Swamp-Blight Nova";
            case TOUCH_STRIKE -> "Septic Quagmire Palm-Gouge";
            case AURA -> "Putrescent Mire-Carapace";
            case GLYPH -> "Pressurized Septic Gas-Geyser";
            case CONE -> "Choking Bog-Gas Fan";
            case SKYFALL -> "Cataclysmic Miasmic Bog-Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Putrid Miasma Phase-Shift";
            case TRANSMUTE, BREAK -> "Virulent Quagmire & Peat Fusion";
            case AFFLICTION -> "Virulent Septic Pestilence Hex";
            default -> "Putrescent Miasma " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Virulent Contagion-Chain Flow) ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Virulent Contagion-Chain Flow: up to 4 targets within 6m (60% initial damage in SpellExecutionEngine),
        // Slowness II & -25% armor toughness for 5.0s (100t)!
        toTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
        SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.25D, 100L);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.03D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        detonateSwampPod(level, caster, hitEntity.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        detonateSwampPod(level, caster, blockHit.getLocation(), spell);
    }

    private static void detonateSwampPod(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // Putrid Swampland Gas-Pod: knocks hostiles airborne, strips 3 armor points, 4.0s (80t) blinding gas cloud!
        AABB box = new AABB(center.x - 3.5D, center.y - 1.5D, center.z - 3.5D, center.x + 3.5D, center.y + 2.5D, center.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.4D, 0.68D, foe.getDeltaMovement().z * 0.4D);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 0.6D, center.z, 28, 1.4D, 0.5D, 1.4D, 0.04D);
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
        // Decomposing Quagmire Siphon Ray & Sinkhole: drains 1 heart/1.5s, drags victim 1.5m/s closer;
        // REQUIRES 5.0s sustained (stacks >= 5) -> Swamp Quagmire Collapse (drags downward, roots 4.0s / 80t + Wither II)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 pull = caster.position().subtract(hit.position()).normalize().scale(0.32D);
            hit.setDeltaMovement(pull.x, 0.05D, pull.z);
            hit.hasImpulse = true;
            if (caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.0F);
                caster.heal(2.0F);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("MiasmaBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("MiasmaBeamStacks").orElse(0) + 1 : 1;
            data.putLong("MiasmaBeamLastTick", now);
            data.putInt("MiasmaBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("MiasmaBeamStacks", 0);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.5D), e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    caught.setDeltaMovement(0.0D, -0.45D, 0.0D);
                    caught.hasImpulse = true;
                    caught.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                    caught.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.13F, 0.36F, 0.48F), hit.getX(), hit.getY(0.8D), hit.getZ(), 1, 0, 0, 0, 0);
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
        // Putrescent Swamp-Blight Nova: 6m radius, strictly ZERO physical light blocks — 7m KB, shreds 3 armor,
        // resets mob aggro, grants caster & allies Septic Immunity (Poison/Wither/Hunger immunity for 10.0s / 200t)!
        double r = Math.max(6.0D, radius);
        long septicUntil = level.getGameTime() + 200L;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_MIASMA_SEPTIC_IMMUNE_UNTIL, septicUntil);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || ent.isAlliedTo(caster) || (ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()))) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_MIASMA_SEPTIC_IMMUNE_UNTIL, septicUntil);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(1.65D);
            ent.setDeltaMovement(away.x, 0.38D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 160L);
            if (ent instanceof Mob mob) {
                mob.setTarget(null);
            }
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.13F, 0.36F, 0.48F), center.x, center.y + 0.9D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 0.9D, center.z, 40, 2.3D, 0.6D, 2.3D, 0.05D);
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
        // Septic Quagmire Palm-Gouge: shreds 3 armor, Poison II 6.0s (120t), splashes 3m bog sludge puddle
        // rooting 2.5s (50t) and causing +25% increased damage from all sources!
        Vec3 puddlePos = hitEntities.isEmpty() ? caster.position().add(direction.scale(2.0D)) : hitEntities.getFirst().position();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, puddlePos, 3.0D, 60, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        AABB box = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 1.8D, zone.center.z + 3.0D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, now + 50L);
                        }
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.15D, zone.center.z, 10, 1.1D, 0.1D, 1.1D, 0.02D);
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
        // Putrescent Mire-Carapace: +7 Armor, +3 Toughness, Poison/Wither/Hunger/Drowning immunity, 35% thorns & 2 armor shred!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_MIASMA_CARAPACE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur + 40, 0, false, false));
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.03D);
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
        // Pressurized Septic Gas-Geyser: launches enemies 8m airborne, clears all active positive buffs,
        // and inflicts +50% fall damage vulnerability for 6.0s (120t)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.7D, 0.05D, 0.7D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D);
                    List<LivingEntity> foes = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (foes.isEmpty()) return true;

                    long fallVulnUntil = srv.getGameTime() + 120L;
                    for (LivingEntity v : foes) {
                        purgeBeneficialBuffs(v);
                        EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_FALL_VULN_50_UNTIL, fallVulnUntil);
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.3D, 1.18D, v.getDeltaMovement().z * 0.3D);
                        v.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.6D, zone.center.z, 36, 1.2D, 0.8D, 1.2D, 0.12D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.75F);
                    return false;
                }
        ));
        return true;
    }

    public static void purgeBeneficialBuffs(LivingEntity target) {
        List<MobEffectInstance> copy = new ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance inst : copy) {
            if (inst.getEffect().value().isBeneficial()) {
                target.removeEffect(inst.getEffect());
            }
        }
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
        // Choking Bog-Gas Fan: knocks back incoming projectiles, drains 2 hunger bars (4 food) from hostile players,
        // Miasmic Asphyxiation (interrupts attacks & 25.0s / 500t AI pathfinding disruption)!
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB projCone = new AABB(caster.getEyePosition(), caster.getEyePosition().add(dirNorm.scale(coneRange))).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, projCone, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            proj.setDeltaMovement(dirNorm.scale(1.2D));
            proj.hasImpulse = true;
        }
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.stopUsingItem();
            if (hit instanceof Player p) {
                p.getFoodData().setFoodLevel(Math.max(0, p.getFoodData().getFoodLevel() - 4));
            }
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
        // Cataclysmic Miasmic Bog-Downpour: Extended 18.0s (360t) downpour + 8.0s (160t) lingering quagmire (520t total)
        // across 12m zone — knocks flat, shreds 5 armor points, 60% slow & continuous Poison!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + (stormActive ? 3.5D : 0.3D), zone.center.z,
                                24, zone.radius * 0.55D, stormActive ? 1.8D : 0.2D, zone.radius * 0.55D, 0.04D);
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow
                            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
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
        tag.putString("SpellEidolonMateria", EssenceType.MIASMA.name());
        tag.putInt("SpellEidolonTintRGB", PESTILENT_MIASMA_HEX_COLOR);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Putrid Miasma Phase-Shift: 14m phase through enemies & thin barriers (fences/bars);
        // passed-through hostiles lose target lock and cough uncontrollably for 3.0s (60t)!
        AABB path = new AABB(departurePos, arrivalPos).inflate(1.5D);
        for (LivingEntity passed : level.getEntitiesOfClass(LivingEntity.class, path, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(passed, caster, true)) continue;
            passed.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
            passed.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            passed.hurtServer(level, level.damageSources().magic(), 3.0F + potencyTier);
            if (passed instanceof Mob mob) {
                mob.setTarget(null);
            }
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, path.inflate(16.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 20, 0.4D, 0.4D, 0.4D, 0.04D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Virulent Quagmire & Peat Fusion: dirt/grass/mud -> fertile Bog Peat (Muddy Mangrove Roots / Podzol),
        // water + glass bottles -> Poison II potions, rotten flesh/spider eyes -> slime balls & bone meal (+25% bonus)!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(BlockTags.DIRT) || state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState() : Blocks.PODZOL.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WATER) && level.getBlockState(pos.above()).isAir()) {
            level.setBlockAndUpdate(pos.above(), Blocks.LILY_PAD.defaultBlockState());
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.SPIDER_EYE)) {
            int boosted = Math.min(64, Math.max(stack.getCount() + 1, Math.round(stack.getCount() * 1.25F)));
            itemEntity.setItem(new ItemStack(Items.SLIME_BALL, boosted));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(Items.BONE_MEAL, Math.max(1, boosted / 2))));
            return true;
        }
        if (stack.is(Items.GLASS_BOTTLE)) {
            itemEntity.setItem(PotionContents.createItemStack(Items.POTION, Potions.STRONG_POISON).copyWithCount(stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Virulent Septic Pestilence Hex: -25% Max Health (SpellCombatReactionEngine), disables regen,
        // -35% toughness, vomits caustic swamp bile every 3.5s (1 heart necrotic + 2m slowing puddle)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        target.hurtServer(level, level.damageSources().magic(), (1.50F + potencyTier * 0.45F) * mult);
        if ((now / 20L) % 4L == 0L) {
            target.hurtServer(level, level.damageSources().magic(), 2.0F * mult);
            for (LivingEntity puddleVictim : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.0D), LivingEntity::isAlive)) {
                LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(puddleVictim, caster, true)) continue;
                puddleVictim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
            }
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.4D), target.getZ(), 12, 0.9D, 0.15D, 0.9D, 0.03D);
        }
        return true;
    }
}
