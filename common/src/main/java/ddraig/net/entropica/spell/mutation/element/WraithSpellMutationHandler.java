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

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Wraith Materia (Tier 1 Fusion #37)</b>
 * (Spellcrafting System Guide Section 6.38 — Void + Undead [Leans: Void]).
 */
public final class WraithSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int WRAITH_STALKER_HEX_COLOR = 0x41304E;

    @Override
    public EssenceType materiaType() {
        return EssenceType.WRAITH;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Incorporeal Soul-Shield Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Spectral Wraith-Skull Missile";
            case BEAM -> "Incorporeal Soul-Siphon Tether Beam";
            case RADIAL_BURST -> "Incorporeal Ghost-Storm Shockwave";
            case TOUCH_STRIKE -> "Spectral Phasing Palm-Gouge";
            case AURA -> "Specter Ectoplasm Armor";
            case GLYPH -> "Incorporeal Grave-Grip Pitfall Glyph";
            case CONE -> "Banshee Wail Ectoplasm Deluge";
            case SKYFALL -> "Spectral Rift Skull-Vortex Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Grave-Slip Blink";
            case TRANSMUTE, BREAK -> "Ghostly Ash Transmute";
            case AFFLICTION -> "Incorporeal Soul-Hollowing Hex";
            default -> "Incorporeal Wraith " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Incorporeal Soul-Shield Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SOUL, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Spectral Wraith-Skull Missile: wall-phasing ectoplasm skull on non-Utility combat/tactical spells!
        if (spell.prefix() != ddraig.net.entropica.spell.SpellPrefix.UTILITY) {
            projectile.setWallPhasing(true);
        }
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Inflicts Wither & Slowness II for 3.0s (60t) and leeches 2 hearts (4.0 HP) back to caster!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard()))) {
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            if (caster != null && caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 4.0F);
                caster.heal(4.0F);
                level.sendParticles(ParticleTypes.SOUL, caster.getX(), caster.getY(0.9D), caster.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.03D);
            }
            level.sendParticles(ParticleTypes.SCULK_SOUL, victim.getX(), victim.getY(0.8D), victim.getZ(), 12, 0.3D, 0.35D, 0.3D, 0.04D);
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
        // Incorporeal Soul-Siphon Tether Beam: phases through walls, leeches 1.5 hearts/s (3.0 HP), -50% speed (Slowness III);
        // 3.0s continuous channeling triggers Soul Severance Detonation (massive burst + 1.5s / 30t stun)!
        List<LivingEntity> phasedTargets = new ArrayList<>(hitEntities);
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(1.25D);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, corridor, e -> e.isAlive() && e != caster)) {
            if (phasedTargets.contains(cand)) continue;
            Vec3 toCand = cand.getBoundingBox().getCenter().subtract(eye);
            double proj = toCand.dot(dirNorm);
            if (proj >= 0.0D && proj <= maxReach) {
                Vec3 perp = toCand.subtract(dirNorm.scale(proj));
                if (perp.lengthSqr() <= 1.44D) {
                    phasedTargets.add(cand);
                    SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, cand, dirNorm, spell, slot, fallbackMateria, apparatusPotency * 0.85F);
                }
            }
        }

        long now = level.getGameTime();
        for (LivingEntity hit : phasedTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2)); // ~-50% speed
            if (caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 3.0F);
                caster.heal(3.0F);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("WraithBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("WraithBeamStacks").orElse(0) + 1 : 1;
            data.putLong("WraithBeamLastTick", now);
            data.putInt("WraithBeamStacks", stacks);

            if (stacks >= 3) {
                data.putInt("WraithBeamStacks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 7.5F + spell.getEffectiveTier() * 2.0F);
                level.sendParticles(ParticleTypes.SCULK_SOUL, hit.getX(), hit.getY(1.0D), hit.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.08D);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.25F, 0.19F, 0.31F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.75F, 1.45F);
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
        // Incorporeal Ghost-Storm Shockwave: 6m radius — renders enemies intangible for 3.0s (60t, unable to deal damage/cast),
        // strips 3 armor points, and heals allied summons!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!ent.isAlive() || ent == caster) continue;
            if (ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid())) {
                eid.heal(8.0F);
                level.sendParticles(ParticleTypes.SOUL, eid.getX(), eid.getY(0.9D), eid.getZ(), 8, 0.3D, 0.35D, 0.3D, 0.03D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellLingeringZoneManager.applyIntangiblePhase(ent, 60, false);
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 160L);
            Vec3 away = ent.position().subtract(center).normalize().scale(0.95D);
            ent.setDeltaMovement(away.x, 0.28D, away.z);
            ent.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.SCULK_SOUL, center.x, center.y + 0.8D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.75F);
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
        // Spectral Phasing Palm-Gouge: bypasses shields & armor toughness, steals 2 full hearts (4.0 HP),
        // strips 3 armor points, and inflicts Incorporeal Stagger (1.5s / 30t stun)!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (hit.isBlocking()) {
                hit.stopUsingItem();
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 4.0D, 120L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            if (caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 4.0F);
                caster.heal(4.0F);
            }
            level.sendParticles(ParticleTypes.SCULK_SOUL, hit.getX(), hit.getY(0.9D), hit.getZ(), 16, 0.3D, 0.4D, 0.3D, 0.05D);
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
        // Specter Ectoplasm Armor: +8 Armor, 100% KB immunity, 25% magic damage reflection, suffocation/wither immunity, 100% sculk stealth!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 8.0D, 0.0D, 1.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.SCULK_SOUL, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Incorporeal Grave-Grip Pitfall Glyph: drags caught mobs into intangible phase-stasis for 4.0s (80t)
        // unable to move, attack, or cast abilities while taking life-siphoning Wraith damage!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL, zone.center.x, zone.center.y + 0.1D, zone.center.z, 3, 0.7D, 0.05D, 0.7D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D);
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (victims.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 3.2D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB gripBox = new AABB(sZone.center.x - 3.2D, sZone.center.y - 1.5D, sZone.center.z - 3.2D,
                                        sZone.center.x + 3.2D, sZone.center.y + 2.5D, sZone.center.z + 3.2D);
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, gripBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    SpellLingeringZoneManager.applyIntangiblePhase(v, 25, false);
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    Vec3 pull = sZone.center.subtract(v.position()).scale(0.25D);
                                    v.setDeltaMovement(pull.x, Math.min(0.0D, v.getDeltaMovement().y - 0.12D), pull.z);
                                    v.hasImpulse = true;
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.50F);
                                        if (sOwner != null && sOwner.isAlive()) {
                                            SpellCombatReactionEngine.notifyMagicalHeal(sOwner, 1.5F);
                                            sOwner.heal(1.5F);
                                        }
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.SCULK_SOUL, sZone.center.x, sZone.center.y + 0.25D, sZone.center.z, 10, 1.1D, 0.25D, 1.1D, 0.03D);
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.1F, 0.7F);
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
        // Banshee Wail Ectoplasm Deluge: 6m KB, bypasses physical shields, Blindness + Wither II 3.5s (70t),
        // strips 3 armor points, and silences mob abilities 3.0s (60t)!
        Vec3 pushDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (hit.isBlocking()) {
                hit.stopUsingItem();
            }
            hit.setDeltaMovement(pushDir.x * 1.45D, 0.35D, pushDir.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 70, 1));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            SpellLingeringZoneManager.applyArcaneSilence(hit, 60);
        }
        Vec3 mid = caster.getEyePosition().add(pushDir.scale(coneRange * 0.5D));
        level.sendParticles(ParticleTypes.SCULK_SOUL, mid.x, mid.y, mid.z, 26, 1.4D, 0.5D, 1.4D, 0.05D);
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.75F, 1.55F);
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
        // Spectral Rift Skull-Vortex Cataclysm: 12m radius, swirling spectral skulls spinning at varying heights;
        // targets caught in spiraling paths suffer heavy Wraith Materia damage, Wither II, and Slowness III!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 160, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 4 == 0) {
                        for (int s = 0; s < 4; s++) {
                            double ang = (elapsed * 0.22D) + (Math.PI * 0.5D * s);
                            double orbitR = 2.5D + ((s * 2.8D + (elapsed % 24) * 0.25D) % (zone.radius - 2.0D));
                            double h = 0.6D + (s * 1.35D);
                            double sx = zone.center.x + Math.cos(ang) * orbitR;
                            double sy = zone.center.y + h;
                            double sz = zone.center.z + Math.sin(ang) * orbitR;
                            srv.sendParticles(ParticleTypes.SCULK_SOUL, sx, sy, sz, 3, 0.15D, 0.15D, 0.15D, 0.02D);
                            srv.sendParticles(ParticleTypes.REVERSE_PORTAL, sx, sy, sz, 3, 0.12D, 0.12D, 0.12D, 0.02D);
                        }
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.5D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
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
        tag.putString("SpellEidolonMateria", EssenceType.WRAITH.name());
        tag.putInt("SpellEidolonTintRGB", WRAITH_STALKER_HEX_COLOR);
        level.sendParticles(ParticleTypes.SCULK_SOUL, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.4D, 0.55D, 0.4D, 0.04D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Grave-Slip Blink: silent 12m shadow-floor step, drops mob aggro, grants 2.0s (40t) Speed & Intangibility!
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(16.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1));
        SpellLingeringZoneManager.applyIntangiblePhase(caster, 40, true);
        level.sendParticles(ParticleTypes.SCULK_SOUL, departurePos.x, departurePos.y + 0.4D, departurePos.z, 14, 0.3D, 0.2D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, arrivalPos.x, arrivalPos.y + 0.5D, arrivalPos.z, 14, 0.3D, 0.3D, 0.3D, 0.03D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Ghostly Ash Transmute: dirt/grass -> soul soil, foliage/leaves -> cobwebs, cobblestone -> mossy cobblestone, bone blocks -> soul sand!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(BlockTags.DIRT) && !state.is(Blocks.SOUL_SOIL)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SOIL.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.LEAVES) || state.is(BlockTags.SMALL_FLOWERS) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)) {
            level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.BONE_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SAND.defaultBlockState());
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.DIRT)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_SOIL, stack.getCount()));
            return true;
        }
        if (stack.is(ItemTags.LEAVES) || stack.is(ItemTags.SMALL_FLOWERS)) {
            itemEntity.setItem(new ItemStack(Items.COBWEB, stack.getCount()));
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_COBBLESTONE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.BONE_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_SAND, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Incorporeal Soul-Hollowing Hex: -50% damage dealt, -40% move speed, disables natural regen,
        // and ticks continuous life-drain damage transferred directly to nearby caster!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, Math.min(0.80D, 0.50D * mult), 0.0D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2, true, true));
        float drain = (1.50F + potencyTier * 0.50F) * mult;
        target.hurtServer(level, level.damageSources().magic(), drain);

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        if (caster != null && caster.isAlive() && caster.distanceToSqr(target) <= 576.0D) {
            SpellCombatReactionEngine.notifyMagicalHeal(caster, drain * 0.80F);
            caster.heal(drain * 0.80F);
            level.sendParticles(ParticleTypes.SOUL, caster.getX(), caster.getY(0.9D), caster.getZ(), 3, 0.2D, 0.25D, 0.2D, 0.02D);
        }
        level.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }
}
