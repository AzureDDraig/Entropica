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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Abyss Materia (Ethereal &amp; Paradox Fusion #33)</b>
 * (Spellcrafting System Guide Section 6.34 — Umbral + Water [Leans: Umbral]).
 */
public final class AbyssSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int KRAKEN_HUSK_HEX_COLOR = 0x213B4E;

    @Override
    public EssenceType materiaType() {
        return EssenceType.ABYSS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Abyssal Maelstrom Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Abyssal Depth Charge Orb";
            case BEAM -> "Hydrostatic Trench Torrent Beam";
            case RADIAL_BURST -> "Benthic Inversion Shockwave";
            case TOUCH_STRIKE -> "Abyssal Drowning Cleave";
            case AURA -> "Hadopelagic Trench Shroud";
            case GLYPH -> "Benthic Maelstrom Pitfall";
            case CONE -> "Abyssal Ink-Blast Deluge";
            case SKYFALL -> "Crushing Trench Monolith";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Benthic Trench Slip";
            case TRANSMUTE, BREAK -> "Hadopelagic Erosion & Darkening Transmute";
            case AFFLICTION -> "Crushing Benthic Drowning Hex";
            default -> "Hadopelagic Trench " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Abyssal Maelstrom Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.BUBBLE_POP, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        implodeDepthChargeOrb(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        implodeDepthChargeOrb(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        implodeDepthChargeOrb(level, caster, projectile.position(), spell);
    }

    private static void implodeDepthChargeOrb(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // Abyssal Depth Charge Orb: 3m hydrostatic vortex dragging caught mobs inward, 3.0s (60t) Blindness + Slowness II,
        // drenching them (Soaked), and extinguishing surface fires!
        AABB box = new AABB(center.x - 3.0D, center.y - 2.0D, center.z - 3.0D, center.x + 3.0D, center.y + 3.0D, center.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 drag = center.subtract(foe.position()).normalize().scale(0.85D);
            foe.setDeltaMovement(drag.x, 0.18D, drag.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            SpellLingeringZoneManager.applySoaked(foe, 140);
        }
        EclipseSpellMutationHandler.snuffTorchesAndFires(level, BlockPos.containing(center), 3);
        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 0.6D, center.z, 18, 0.9D, 0.4D, 0.9D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.6D, center.z, 26, 1.0D, 0.5D, 1.0D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.70F);
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
        // Hydrostatic Trench Torrent Beam: slows caught mobs 60% (Slowness III) + silences abilities;
        // 3.0s continuous channeling triggers Benthic Implosion dragging targets together & stripping 3 armor points!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2));
            SpellLingeringZoneManager.applyArcaneSilence(hit, 45);
            SpellLingeringZoneManager.applySoaked(hit, 120);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("AbyssBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("AbyssBeamStacks").orElse(0) + 1 : 1;
            data.putLong("AbyssBeamLastTick", now);
            data.putInt("AbyssBeamStacks", stacks);
            if (stacks >= 3) {
                data.putInt("AbyssBeamStacks", 0);
                Vec3 center = hit.position();
                AABB implosion = new AABB(center.x - 4.0D, center.y - 2.0D, center.z - 4.0D, center.x + 4.0D, center.y + 3.0D, center.z + 4.0D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, implosion)) {
                    if (foe == caster || !foe.isAlive()) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    Vec3 pull = center.subtract(foe.position()).normalize().scale(0.95D);
                    foe.setDeltaMovement(pull.x, 0.22D, pull.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                }
                level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 0.8D, center.z, 20, 1.0D, 0.5D, 1.0D, 0.04D);
                level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.8D, center.z, 28, 1.1D, 0.5D, 1.1D, 0.09D);
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
        // Benthic Inversion Shockwave: silent 6m ink eruption — 6m knockback, 4.0s (80t) Blindness + Slowness III,
        // wipes mob aggro, drenches targets (2.0x incoming Lightning damage), and extinguishes open fires!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.45D);
            foe.setDeltaMovement(away.x, 0.36D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            SpellLingeringZoneManager.applySoaked(foe, 160);
            foe.setLastHurtByMob(null);
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        EclipseSpellMutationHandler.snuffTorchesAndFires(level, BlockPos.containing(center), (int) Math.min(6, Math.ceil(r)));
        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 0.7D, center.z, 32, 2.2D, 0.5D, 2.2D, 0.05D);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.7D, center.z, 36, 2.2D, 0.5D, 2.2D, 0.09D);
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
        // Abyssal Drowning Cleave: 5m knockback, inflicts drowning damage on land for 4.0s (80t), & drops mob aggro!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.25D, 0.32D, direction.z * 1.25D);
            hit.hasImpulse = true;
            SpellLingeringZoneManager.applyAbyssLandDrowning(hit, 80);
            SpellLingeringZoneManager.applySoaked(hit, 140);
            hit.setLastHurtByMob(null);
            if (hit instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            level.sendParticles(ParticleTypes.SQUID_INK, hit.getX(), hit.getY(0.8D), hit.getZ(), 12, 0.3D, 0.35D, 0.3D, 0.03D);
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
        // Hadopelagic Trench Shroud: infinite Water Breathing, +60% swim speed, Sculk/Warden immunity,
        // 60% mob aggro cut, and blinds & drenches melee attackers!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL, until);
        data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, until);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 1, false, true));
        caster.clearFire();
        level.sendParticles(ParticleTypes.SQUID_INK, caster.getX(), caster.getY(0.8D), caster.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.03D);
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
        // Benthic Maelstrom Pitfall: 4m whirlpool vortex sucking mobs downward and pinning them at center for 3.5s (70t)
        // with continuous drowning, Abyss damage, & Blindness!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, 1.0D, 0.08D, 1.0D, 0.01D);
                    }
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 70, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB box = new AABB(sZone.center.x - 4.0D, sZone.center.y - 1.5D, sZone.center.z - 4.0D,
                                        sZone.center.x + 4.0D, sZone.center.y + 3.0D, sZone.center.z + 4.0D);
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    Vec3 vortex = sZone.center.subtract(v.position()).scale(0.30D);
                                    v.setDeltaMovement(vortex.x, Math.min(v.getDeltaMovement().y, -0.25D), vortex.z);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    SpellLingeringZoneManager.applyAbyssLandDrowning(v, 40);
                                    SpellLingeringZoneManager.applySoaked(v, 100);
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F);
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.SPLASH, sZone.center.x, sZone.center.y + 0.25D, sZone.center.z, 14, 1.3D, 0.2D, 1.3D, 0.06D);
                                    sLvl.sendParticles(ParticleTypes.SQUID_INK, sZone.center.x, sZone.center.y + 0.25D, sZone.center.z, 8, 1.2D, 0.2D, 1.2D, 0.02D);
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
        // Abyssal Ink-Blast Deluge: 6m knockback, 4.0s (80t) Blindness + Slowness II, wipes mob aggro,
        // drenches targets, and extinguishes fires & wall torches!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.34D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
            SpellLingeringZoneManager.applySoaked(hit, 160);
            hit.setLastHurtByMob(null);
            if (hit instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        EclipseSpellMutationHandler.snuffTorchesAndFires(level, BlockPos.containing(mid), 4);
        level.sendParticles(ParticleTypes.SQUID_INK, mid.x, mid.y, mid.z, 24, 1.5D, 0.5D, 1.5D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, mid.x, mid.y, mid.z, 28, 1.5D, 0.5D, 1.5D, 0.08D);
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
        // Crushing Trench Monolith: 2.0s (40t) telegraph -> colossal abyssal black-ice monolith impact:
        // heavy Abyss damage, black tidal wave knocking mobs 10m back, and temporary pool of water (restored cleanly after 4.0s)!
        double r = Math.max(8.0D, radius);
        Map<BlockPos, BlockState> savedBlocks = new HashMap<>();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
                            if (srv.getBlockState(entry.getKey()).is(Blocks.WATER)) {
                                srv.setBlockAndUpdate(entry.getKey(), entry.getValue());
                            }
                        }
                        return false;
                    }
                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 6.5D - elapsed * 0.14D, zone.center.z, 12, 0.8D, 0.8D, 0.8D, 0.02D);
                            srv.sendParticles(ParticleTypes.DRIPPING_WATER, zone.center.x, zone.center.y + 6.0D - elapsed * 0.12D, zone.center.z, 10, 0.9D, 0.5D, 0.9D, 0.02D);
                        }
                        return true;
                    }
                    if (elapsed == 40) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.5D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 tidal = foe.position().subtract(zone.center).normalize().scale(2.15D);
                            foe.setDeltaMovement(tidal.x, 0.52D, tidal.z);
                            foe.hasImpulse = true;
                            SpellLingeringZoneManager.applySoaked(foe, 180);
                            SpellLingeringZoneManager.applyAbyssLandDrowning(foe, 80);
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.35F);
                        }
                        BlockPos cPos = BlockPos.containing(zone.center);
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-1, 0, -1), cPos.offset(1, 0, 1))) {
                            BlockState cur = srv.getBlockState(p);
                            if (cur.isAir() && srv.getBlockState(p.below()).isSolidRender()
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, cur)) {
                                savedBlocks.put(p.immutable(), cur);
                                srv.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                            }
                        }
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 1.0D, zone.center.z, 40, 2.2D, 0.8D, 2.2D, 0.07D);
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 1.0D, zone.center.z, 50, 2.5D, 0.8D, 2.5D, 0.12D);
                        srv.playSound(null, cPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.3F, 0.55F);
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.ABYSS.name());
        tag.putInt("SpellEidolonTintRGB", KRAKEN_HUSK_HEX_COLOR);
        level.sendParticles(ParticleTypes.SQUID_INK, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.45D, 0.5D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, spawnPos.x, spawnPos.y + 0.6D, spawnPos.z, 24, 0.5D, 0.4D, 0.5D, 0.08D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Benthic Trench Slip: 14m silent zero-particle slip, dropping all active mob aggro completely!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 60L);
        caster.clearFire();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(18.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            if (mob.getLastHurtByMob() == caster) {
                mob.setLastHurtByMob(null);
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Hadopelagic Erosion & Darkening Transmute: sand -> clay, gravel -> cobblestone, lava -> crying obsidian,
        // glowstone/sea lantern -> dark prismarine, stone bricks -> cracked stone bricks!
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.GRAVEL) || state.is(Blocks.SMOOTH_STONE) || state.is(Blocks.STONE)) {
            level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.GLOWSTONE) || state.is(Blocks.SEA_LANTERN) || state.is(Blocks.SHROOMLIGHT)) {
            level.setBlockAndUpdate(pos, Blocks.DARK_PRISMARINE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STONE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DEEPSLATE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, in.getCount()));
            return true;
        }
        if (in.is(Items.GRAVEL) || in.is(Items.SMOOTH_STONE) || in.is(Items.STONE)) {
            itemEntity.setItem(new ItemStack(Items.COBBLESTONE, in.getCount()));
            return true;
        }
        if (in.is(Items.GLOWSTONE) || in.is(Items.SEA_LANTERN) || in.is(Items.SHROOMLIGHT)) {
            itemEntity.setItem(new ItemStack(Items.DARK_PRISMARINE, in.getCount()));
            return true;
        }
        if (in.is(Items.STONE_BRICKS)) {
            itemEntity.setItem(new ItemStack(Items.CRACKED_STONE_BRICKS, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Crushing Benthic Drowning Hex: continuous drowning on land, -40% movement speed (Slowness II),
        // silences mob abilities, and drenches for 2.0x incoming Lightning damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellLingeringZoneManager.applyAbyssLandDrowning(target, 45);
        SpellLingeringZoneManager.applyArcaneSilence(target, 40);
        SpellLingeringZoneManager.applySoaked(target, 60);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(4, Math.max(1, Math.round(1.5F * mult))), true, true));
        target.hurtServer(level, level.damageSources().magic(), (1.40F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.8D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }
}
