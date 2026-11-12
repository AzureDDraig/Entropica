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
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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
 * Complete per-part Signature Mutation implementation for <b>Null_R Materia (Ethereal &amp; Paradox Fusion #30)</b>
 * (Spellcrafting System Guide Section 6.31 — Radiant + Void [Leans: Void]).
 */
public final class NullRSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int NULL_ARCHON_HEX_COLOR = 0xEFE8FA;

    @Override
    public EssenceType materiaType() {
        return EssenceType.NULL_R;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Singularity Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "White Hole Singularity Orb";
            case BEAM -> "Event Horizon Tractor Beam";
            case RADIAL_BURST -> "White Hole Antimatter Nova";
            case TOUCH_STRIKE -> "Event Horizon Palm";
            case AURA -> "Event Horizon Bastion";
            case GLYPH -> "White Hole Singularity Trap";
            case CONE -> "Blinding Antimatter Flare Cone";
            case SKYFALL -> "White Hole Singularity Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Antimatter Phase Dash";
            case TRANSMUTE, BREAK -> "Null-Slag Dissolution Transmute";
            case AFFLICTION -> "Antimatter Annihilation Hex";
            default -> "White-Hole Antimatter " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Singularity Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setWallPhasing(true);
        projectile.setMaxFlightTicks(95);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.03D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        collapseWhiteHoleOrb(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        collapseWhiteHoleOrb(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        collapseWhiteHoleOrb(level, caster, projectile.position(), spell);
    }

    private static void collapseWhiteHoleOrb(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // White Hole Singularity Orb: pulls mobs within 5m inward, blinding flash, 1.5s stun (30t), 3s silence (60t)!
        AABB box = new AABB(center.x - 5.0D, center.y - 2.0D, center.z - 5.0D, center.x + 5.0D, center.y + 3.0D, center.z + 5.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 pull = center.subtract(foe.position()).normalize().scale(1.05D);
            foe.setDeltaMovement(pull.x, 0.25D, pull.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.95F, 0.97F, 1.0F), center.x, center.y + 0.6D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.6D, center.z, 26, 1.5D, 0.6D, 1.5D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.35F);
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
        // Event Horizon Tractor Beam: zero-g mid-air suspension + pull toward caster; 3s triggers Annihilation Flash (buff purge + 4s blind)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 pull = caster.position().subtract(hit.position()).normalize().scale(0.45D);
            hit.setDeltaMovement(pull.x, 0.28D, pull.z);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("NullRBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("NullRBeamStacks").orElse(0) + 1 : 1;
            data.putLong("NullRBeamLastTick", now);
            data.putInt("NullRBeamStacks", stacks);
            if (stacks >= 3) {
                data.putInt("NullRBeamStacks", 0);
                EclipseSpellMutationHandler.purgeBeneficialEffects(hit);
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.95F, 0.97F, 1.0F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
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
        // White Hole Antimatter Nova: 6m shockwave pulling 4m inward first, then blasting 6m back, 4s Blindness, strips buffs, resets mob aggro!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 inward = center.subtract(foe.position()).normalize().scale(0.95D);
            foe.setDeltaMovement(inward.x, 0.22D, inward.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            EclipseSpellMutationHandler.purgeBeneficialEffects(foe);
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 7, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB blastBox = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 3.0D, zone.center.z + zone.radius);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blastBox)) {
                        if (foe == owner || !foe.isAlive()) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        Vec3 away = foe.position().subtract(zone.center).normalize().scale(1.45D);
                        foe.setDeltaMovement(away.x, 0.42D, away.z);
                        foe.hasImpulse = true;
                    }
                    srv.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.95F, 0.97F, 1.0F), zone.center.x, zone.center.y + 0.8D, zone.center.z, 1, 0, 0, 0, 0);
                    srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.8D, zone.center.z, 36, 2.2D, 0.5D, 2.2D, 0.10D);
                    return false;
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
        // Event Horizon Palm: ignores 50% armor, purges positive buffs, random teleports over 6s (120t) + 2s Blindness!
        long destabilizedUntil = level.getGameTime() + 120L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            EclipseSpellMutationHandler.purgeBeneficialEffects(hit);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, destabilizedUntil);
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
        // Event Horizon Bastion: high-float jump, zero-g glide, Blindness/Darkness immunity, 50% arrow deflection, melee blind + 4m repel!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.06D);
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
        // White Hole Singularity Trap: 4m singularity pulling mobs to center, suspending 3m airborne 3s (60t), stripping buffs, 4s Blindness!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 1.1D, 0.1D, 1.1D, 0.02D);
                    }
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB box = new AABB(sZone.center.x - 4.0D, sZone.center.y - 1.0D, sZone.center.z - 4.0D,
                                        sZone.center.x + 4.0D, sZone.center.y + 5.0D, sZone.center.z + 4.0D);
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    Vec3 hover = sZone.center.add(0.0D, 3.0D, 0.0D).subtract(v.position()).scale(0.28D);
                                    v.setDeltaMovement(hover);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                                    EclipseSpellMutationHandler.purgeBeneficialEffects(v);
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, new Vec3(0, 1, 0), sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F);
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
        // Blinding Antimatter Flare Cone: 6m pushback, 4s Blindness, strips positive buffs, +50% bonus smite damage to undead!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.35D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            EclipseSpellMutationHandler.purgeBeneficialEffects(hit);
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.hurtServer(level, level.damageSources().magic(), 4.0F + spell.getEffectiveTier() * 1.25F);
            }
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        SpellLingeringZoneManager.pulseTemporaryLight(level, BlockPos.containing(mid), 15, 60);
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 24, 1.5D, 0.6D, 1.5D, 0.06D);
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
        // White Hole Singularity Tempest: 10s (200t), 14m antimatter star rain — lifts 3m zero-g, strips buffs, smites undead, slams at end!
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
                            v.setDeltaMovement(v.getDeltaMovement().x, -1.55D, v.getDeltaMovement().z);
                            v.hasImpulse = true;
                            v.hurtServer(srv, srv.damageSources().magic(), 5.5F + zone.spell.getEffectiveTier() * 1.4F);
                        }
                        return false;
                    }
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 4.5D, zone.center.z, 22, zone.radius * 0.55D, 1.4D, zone.radius * 0.55D, 0.08D);
                    }
                    if (elapsed % 20 == 0) {
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            EclipseSpellMutationHandler.purgeBeneficialEffects(v);
                            v.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0));
                            v.setDeltaMovement(v.getDeltaMovement().x, 0.36D, v.getDeltaMovement().z);
                            v.hasImpulse = true;
                            if (v.getType().is(EntityTypeTags.UNDEAD)) {
                                v.hurtServer(srv, srv.damageSources().magic(), 3.0F + zone.spell.getEffectiveTier() * 0.8F);
                            }
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
        tag.putString("SpellEidolonMateria", EssenceType.NULL_R.name());
        tag.putInt("SpellEidolonTintRGB", NULL_ARCHON_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.07D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Antimatter Phase Dash: 16m white-fire streak knocking aside mobs & dropping mob aggro!
        Vec3 delta = arrivalPos.subtract(departurePos);
        int steps = Math.max(2, (int) Math.ceil(delta.length()));
        Vec3 stepVec = delta.scale(1.0D / steps);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.8D, p.z, 4, 0.2D, 0.3D, 0.2D, 0.03D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - 1.6D, p.y - 0.5D, p.z - 1.6D, p.x + 1.6D, p.y + 2.0D, p.z + 1.6D))) {
                if (foe == caster || !foe.isAlive() || foe.isAlliedTo(caster) || !(foe instanceof Monster)) continue;
                Vec3 side = foe.position().subtract(p).normalize().scale(0.90D);
                foe.setDeltaMovement(side.x, 0.32D, side.z);
                foe.hasImpulse = true;
            }
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(14.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Null-Slag Dissolution Transmute: dissolves stone, cobble, and gravel into raw void slag (crying obsidian) & glowstone dust!
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.GRAVEL) || state.is(Blocks.DEEPSLATE)) {
            level.setBlockAndUpdate(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState());
            Block.popResource(level, pos.above(), new ItemStack(Items.GLOWSTONE_DUST, 1 + level.random.nextInt(2)));
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.STONE) || in.is(Items.COBBLESTONE) || in.is(Items.GRAVEL)) {
            itemEntity.setItem(new ItemStack(Items.GLOWSTONE_DUST, in.getCount()));
            if (level.random.nextFloat() < 0.50F) {
                ItemEntity slag = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(0.2D), itemEntity.getZ(),
                        new ItemStack(Items.CRYING_OBSIDIAN, Math.max(1, in.getCount() / 2)));
                level.addFreshEntity(slag);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Antimatter Annihilation Hex: melts 2 armor points every 3s up to 12 total, disables shield blocking, suppresses positive buffs!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        CompoundTag data = EntityHelper.getPersistentData(target);
        int ticks = data.getInt("NullRHexMeltSteps").orElse(0) + 1;
        data.putInt("NullRHexMeltSteps", ticks);
        double meltedArmor = Math.min(12.0D, (2.0D + (ticks / 3) * 2.0D) * mult);

        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, meltedArmor, 0.0D, 40L);
        EclipseSpellMutationHandler.purgeBeneficialEffects(target);
        if (target instanceof Player player) {
            for (ItemStack held : List.of(player.getMainHandItem(), player.getOffhandItem())) {
                if (!held.isEmpty() && (held.is(Items.SHIELD) || held.has(DataComponents.BLOCKS_ATTACKS))) {
                    player.getCooldowns().addCooldown(held, 50);
                }
            }
            if (player.isBlocking()) {
                player.stopUsingItem();
            }
        }
        target.hurtServer(level, level.damageSources().magic(), (1.45F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.03D);
        return true;
    }
}
