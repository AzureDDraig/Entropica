package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
 * Complete per-part Signature Mutation implementation for <b>Celestial Materia (Tier 4 Apex Fusion #50)</b>
 * (Spellcrafting System Guide Section 6.52 — Radiant + Nature + Vitae + Air catalyzed by Chimera).
 */
public final class CelestialSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CELESTIAL_STARLIGHT_GOLD_HEX_COLOR = 0xFFEA78;

    @Override
    public EssenceType materiaType() {
        return EssenceType.CELESTIAL;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Holy White-Hole Star-Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Holy Solar Singularity Dart";
            case BEAM -> "Holy Pranic Fusion Beam & Cataclysmic Supernova";
            case RADIAL_BURST -> "Holy Supernova Dawn-Shockwave";
            case TOUCH_STRIKE -> "Holy Supernova Cleave / White-Hole Touch";
            case AURA -> "Holy Celestial Supernova Shroud";
            case GLYPH -> "Holy White-Hole Sigil";
            case CONE -> "Holy Dawn-Deluge Cone";
            case SKYFALL -> "Cataclysmic Supernova Starfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Celestial Supernova Ascendance Step";
            case TRANSMUTE, BREAK -> "Celestial Genesis & Consecration Transmutation";
            case AFFLICTION -> "Divine Genesis Brand / Holy Supernova Overload Hex";
            default -> "Celestial Supernova " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Holy White-Hole Star-Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.2D), null, 0L);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (hitEntity instanceof LivingEntity le) {
            ApexEschatonSingularityHelper.disableShield(le, 80);
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            collapseHolyWhiteHole(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isWallPhasing()) {
            collapseHolyWhiteHole(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        collapseHolyWhiteHole(level, caster, projectile.position(), spell);
    }

    private static void collapseHolyWhiteHole(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // 6m Holy White-Hole Singularity: pulls inward, -5 armor, Blindness 8.0s (160t), siphons health to caster & allies!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        float siphoned = 0.0F;
        AABB well = new AABB(center.x - 6.0D, center.y - 2.0D, center.z - 6.0D, center.x + 6.0D, center.y + 4.0D, center.z + 6.0D);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, well, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, safeguard)) continue;
            Vec3 in = center.subtract(ent.position()).scale(0.38D);
            ent.setDeltaMovement(in.x, 0.2D, in.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 160L);
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
            siphoned += 3.5F;
        }
        if (caster != null && siphoned > 0.0F) {
            float healEach = Math.min(12.0F, siphoned);
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(8.0D), e -> ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                SpellCombatReactionEngine.notifyMagicalHeal(ally, healEach);
                ally.heal(healEach);
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.6D, center.z, 28, 1.5D, 0.4D, 1.5D, 0.06D);
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
        // Holy Pranic Fusion Beam & Cataclysmic Supernova: -2 armor/s, blinds, heals allies, refills fuel;
        // 5.0s continuous -> -6 armor, Blindness 12.0s (240t), 10m KB, blooms restorative Celestial Lilies!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 4.0F);
                hit.heal(4.0F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 80L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            if (caster instanceof Player p) {
                SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.CELESTIAL, 32L);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("CelestialBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("CelestialBeamStacks").orElse(0) + 1 : 1;
            data.putLong("CelestialBeamLastTick", now);
            data.putInt("CelestialBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("CelestialBeamStacks", 0);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 220L);
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0)); // 12.0s!
                hit.setDeltaMovement(dir.x * 2.1D, 0.45D, dir.z * 2.1D); // 10m KB!
                hit.hasImpulse = true;
                float smite = ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(hit) ? 2.0F : 1.0F;
                hit.hurtServer(level, level.damageSources().magic(), (14.0F + spell.getEffectiveTier() * 2.5F) * smite);

                BlockPos center = hit.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                    if (level.getBlockState(p).isAir() && Blocks.LILY_OF_THE_VALLEY.defaultBlockState().canSurvive(level, p) && level.random.nextFloat() < 0.35F) {
                        level.setBlockAndUpdate(p, Blocks.LILY_OF_THE_VALLEY.defaultBlockState());
                    }
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(8.0D), e -> ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 6.0F);
                    ally.heal(6.0F);
                }
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
        // Holy Supernova Dawn-Shockwave: 10m burst (ZERO light blocks!), 8m KB, vaporizes hostile projectiles,
        // -5 armor, cloaks party in Celestial Ascendance Shroud for 15.0s (300t)!
        double r = Math.max(10.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 4.5D, center.z + r);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, box, null, 0L);

        long shroudUntil = level.getGameTime() + 300L;
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL, shroudUntil);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 10.0D, 5.0D, 0.0D, 0.0D, 300L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(1.85D); // 8m KB!
            ent.setDeltaMovement(away.x, 0.42D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 160L);
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL, shroudUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, 300L);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0D, center.z, 36, 2.5D, 0.5D, 2.5D, 0.06D);
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
        // Holy Supernova Cleave / White-Hole Touch: 50% bypass, -5 armor, disables shields 5.0s,
        // leeches 35% as health/fuel to party, 6m KB, Holy Blindness + Glowing Brand!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 160L);
            ApexEschatonSingularityHelper.disableShield(hit, 100);
            hit.setDeltaMovement(push.x * 1.45D, 0.35D, push.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
            for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(8.0D), e -> ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                SpellCombatReactionEngine.notifyMagicalHeal(ally, 4.0F);
                ally.heal(4.0F);
            }
            if (caster instanceof Player p) {
                SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.CELESTIAL, 40L);
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
        // Holy Celestial Supernova Shroud (strictly NO physical light blocks!)
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 28, 0.5D, 0.6D, 0.5D, 0.05D);
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
        // Holy White-Hole Sigil: Ally step -> 10 hearts (20 HP), Regen III, cleanses all debuffs;
        // Hostile step within 4m -> 10m vortex dragging in, launching 12m airborne, -5 armor, Blindness 10s (200t), then slam!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 360, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.9D, 0.05D, 0.9D, 0.01D);
                    }
                    if (elapsed < 12) return true;

                    AABB trig = new AABB(zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D);
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trig, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    for (LivingEntity st : steppers) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(st, owner)) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            SpellCombatReactionEngine.notifyMagicalHeal(st, 20.0F); // 10 hearts!
                            st.heal(20.0F);
                            st.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2)); // Regen III
                            return false;
                        }
                    }

                    AABB vortex = new AABB(zone.center.x - 10.0D, zone.center.y - 2.0D, zone.center.z - 10.0D,
                            zone.center.x + 10.0D, zone.center.y + 5.0D, zone.center.z + 10.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, vortex, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.35D);
                        foe.setDeltaMovement(in.x, 1.48D, in.z);
                        foe.hasImpulse = true;
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 180L);
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0)); // 10.0s!
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
        // Holy Dawn-Deluge Cone: allies cleansed + Regen III 12.0s (240t);
        // hostiles -5 armor + Divine Stupefaction (25.0s / 500t AI disruption + Blindness + traitor panic)!
        for (LivingEntity hit : hitTargets) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 2));
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            SpellLingeringZoneManager.applyMirageTraitorFrenzy(hit, 300);
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
        // Cataclysmic Supernova Starfall: 14m zone, 20.0s (400t) + 10.0s (200t) Consecrated White-Hole Singularity Zone (600t)!
        double r = Math.max(14.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean bombardment = elapsed <= 400;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + (bombardment ? 4.5D : 0.4D), zone.center.z,
                                24, zone.radius * 0.55D, bombardment ? 2.2D : 0.3D, zone.radius * 0.55D, 0.06D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner)) {
                                if (!bombardment) {
                                    SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                    ent.heal(4.0F);
                                    if (ent instanceof Player p) {
                                        p.getFoodData().eat(1, 0.4F);
                                        SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.CELESTIAL, 32L);
                                    }
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (bombardment) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 6.0D, 0.0D, 180L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.25D, -0.45D, ent.getDeltaMovement().z * 0.25D);
                                ent.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.50F);
                            } else {
                                Vec3 in = zone.center.subtract(ent.position()).scale(0.25D);
                                ent.setDeltaMovement(ent.getDeltaMovement().add(in.x, 0.0D, in.z));
                                ent.hasImpulse = true;
                                ent.igniteForSeconds(3);
                                ent.hurtServer(srv, srv.damageSources().magic(), ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent) ? 7.0F : 3.5F);
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
        tag.putString("SpellEidolonMateria", EssenceType.CELESTIAL.name());
        tag.putInt("SpellEidolonTintRGB", CELESTIAL_STARLIGHT_GOLD_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 28, 0.5D, 0.6D, 0.5D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Celestial Supernova Ascendance Step: 20m through walls; sheds 100% aggro, 6.0s (120t) 3D flight + Slow Falling,
        // leaves Holy Dawn-Decoy blinding pursuers for 6.0s (120t)!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.CELESTIAL,
                24.0F, 120, "Holy Dawn-Decoy", null
        );
        AABB dep = new AABB(departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 3.5D, departurePos.z + 6.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 120);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0));
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 24.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.35F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Celestial Genesis & Consecration Transmutation [5.0s Channel]:
        // Cures Zombie Villagers with max trade discounts; Netherrack/Soul Sand/Sculk -> Calcite/White Terracotta/Flowering Azalea Moss;
        // Rotten Flesh/Spider Eyes -> Golden Carrots/Glistering Melons; purges Curses from held weapons & armor!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.CELESTIAL,
                "Celestial Genesis & Consecration Transmutation",
                () -> {
                    ApexEschatonSingularityHelper.cureZombieVillagersWithMaxDiscounts(level, caster, pos, 8.0D);
                    if (caster != null) {
                        for (EquipmentSlot eq : EquipmentSlot.values()) {
                            PenumbraSpellMutationHandler.purgeCursesFromStack(caster.getItemBySlot(eq));
                        }
                    }
                })) {
            return true;
        }

        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.SCULK)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> Blocks.CALCITE.defaultBlockState();
                case 1 -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
                default -> Blocks.MOSS_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (pick == 2 && level.getBlockState(pos.above()).isAir() && level.random.nextBoolean()) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (PenumbraSpellMutationHandler.purgeCursesFromStack(stack)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.GOLDEN_CARROT, stack.getCount()));
            return true;
        }
        if (stack.is(Items.SPIDER_EYE) || stack.is(Items.FERMENTED_SPIDER_EYE)) {
            itemEntity.setItem(new ItemStack(Items.GLISTERING_MELON_SLICE, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Divine Genesis Brand / Holy Supernova Overload Hex: true holy burn (2x vs Undead/Demonic/Abyssal),
        // disables healing & positive buffs, 50% fumble miss (in SpellCombatReactionEngine), -40% toughness!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        ApexEschatonSingularityHelper.purgeBeneficialBuffs(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0, true, true));
        float base = (2.0F + potencyTier * 0.55F) * mult;
        if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)) {
            base *= 2.0F;
        }
        target.hurtServer(level, level.damageSources().magic(), base);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 6, 0.3D, 0.35D, 0.3D, 0.03D);
        return true;
    }
}
