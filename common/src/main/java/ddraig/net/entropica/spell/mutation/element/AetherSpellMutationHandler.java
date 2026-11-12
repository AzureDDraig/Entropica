package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
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
 * Complete per-part Signature Mutation implementation for <b>Aether Materia (Tier 4 Apex Fusion #48)</b>
 * (Spellcrafting System Guide Section 6.50 — Air + Earth + Water + Nether catalyzed by Chimera).
 */
public final class AetherSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int AETHER_CHALK_GOLD_HEX_COLOR = 0xA88654;

    @Override
    public EssenceType materiaType() {
        return EssenceType.AETHER;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Quintessential Quad-Harmonic Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Apex Celestial Meteorite";
            case BEAM -> "Quintessential Firmament Ray & Celestial Supernova";
            case RADIAL_BURST -> "Quintessential Firmament Resonance Nova";
            case TOUCH_STRIKE -> "Quintessential Firmament Sunder";
            case AURA -> "Transcendent Firmament Quintessence Shroud";
            case GLYPH -> "Quintessential Firmament Inversion Vortex Sigil";
            case CONE -> "Quintessential Firmament Deluge";
            case SKYFALL -> "Cataclysmic Firmament Skyfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Firmament Ascension Step";
            case TRANSMUTE, BREAK -> "Quintessential Firmament Genesis";
            case AFFLICTION -> "Apex Gravitational Collapse Hex";
            default -> "Quintessential Firmament " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Quintessential Quad-Harmonic Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.2D), null, 0L);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (projectile.getPierceCountRemaining() <= 0) {
            detonateCelestialMeteorite(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isWallPhasing()) {
            detonateCelestialMeteorite(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        detonateCelestialMeteorite(level, caster, projectile.position(), spell);
    }

    private static void detonateCelestialMeteorite(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // 7m multi-elemental detonation + 4 autonomous homing spikes shattering 5 armor & disabling shields 5.0s (100t)!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        AABB blast = new AABB(center.x - 7.0D, center.y - 2.5D, center.z - 7.0D, center.x + 7.0D, center.y + 4.5D, center.z + 7.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, blast, LivingEntity::isAlive)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 160L);
            ApexEschatonSingularityHelper.disableShield(foe, 100);
            foe.hurtServer(level, level.damageSources().magic(), 5.0F + spell.getEffectiveTier() * 1.4F);
        }

        List<LivingEntity> spikeTargets = level.getEntitiesOfClass(LivingEntity.class, blast.inflate(5.0D),
                e -> e.isAlive() && (e instanceof Monster || (caster != null && e == caster.getLastHurtMob()))
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, safeguard)));
        int spikes = Math.min(4, spikeTargets.size());
        for (int i = 0; i < spikes; i++) {
            LivingEntity t = spikeTargets.get(i);
            SpellCombatReactionEngine.applyTimedAttributeShred(t, level, 5.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(t, 100);
            t.hurtServer(level, level.damageSources().magic(), 6.0F + spell.getEffectiveTier() * 1.5F);
            level.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY(0.8D), t.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.1D);
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.5D, center.z, 1, 0, 0, 0, 0);
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
        // Quintessential Firmament Ray & Celestial Supernova: -2 armor/s, vaporizes projectiles, zero-g suspension;
        // 5.0s continuous -> Cataclysmic Firmament Supernova (12m KB, -6 armor, purges buffs, Levitation + Stun 5.0s)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(maxReach))).inflate(2.0D), null, 0L);

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 80L);
            hit.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("AetherBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("AetherBeamStacks").orElse(0) + 1 : 1;
            data.putLong("AetherBeamLastTick", now);
            data.putInt("AetherBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("AetherBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
                hit.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 1));
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                hit.setDeltaMovement(dir.x * 2.4D, 0.55D, dir.z * 2.4D); // 12m knockback!
                hit.hasImpulse = true;
                hit.hurtServer(level, level.damageSources().magic(), 14.0F + spell.getEffectiveTier() * 2.5F);
                level.sendParticles(ParticleTypes.EXPLOSION, hit.getX(), hit.getY(0.9D), hit.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
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
        // Quintessential Firmament Resonance Nova: 10m shockwave, ZERO light blocks, launches hostiles 10m airborne
        // into 4.0s (80t) zero-g suspension, shatters 5 armor, grants Transcendent Firmament Aegis for 12.0s (240t)!
        double r = Math.max(10.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 5.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 160L);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.4D, 1.28D, foe.getDeltaMovement().z * 0.4D); // 10m vertical launch!
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 0));
        }

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL, level.getGameTime() + 240L);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, 240L);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, 240);
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
        // Quintessential Firmament Sunder: 50% armor bypass, -5 armor, disables shields 5.0s (100t),
        // launches target + adjacent mobs within 6m 10m airborne into 4.0s zero-g float + vacuum fissure!
        Vec3 fissureOrigin = hitEntities.isEmpty() ? caster.position().add(caster.getLookAngle().scale(2.5D)) : hitEntities.getFirst().position();
        AABB cleave = new AABB(fissureOrigin.x - 6.0D, fissureOrigin.y - 2.0D, fissureOrigin.z - 6.0D,
                fissureOrigin.x + 6.0D, fissureOrigin.y + 3.5D, fissureOrigin.z + 6.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, cleave, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 160L);
            ApexEschatonSingularityHelper.disableShield(foe, 100);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, 1.28D, foe.getDeltaMovement().z * 0.3D);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 0));
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, fissureOrigin, 5.0D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB vac = new AABB(zone.center.x - 5.0D, zone.center.y - 2.0D, zone.center.z - 5.0D,
                                zone.center.x + 5.0D, zone.center.y + 4.0D, zone.center.z + 5.0D);
                        for (LivingEntity p : srv.getEntitiesOfClass(LivingEntity.class, vac, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(p, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 pull = zone.center.subtract(p.position()).scale(0.22D);
                            p.setDeltaMovement(p.getDeltaMovement().add(pull.x, 0.0D, pull.z));
                            p.hasImpulse = true;
                        }
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 0.4D, zone.center.z, 6, 1.2D, 0.2D, 1.2D, 0.02D);
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
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AETHER_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, dur);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 28, 0.5D, 0.6D, 0.5D, 0.04D);
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
        // Quintessential Firmament Inversion Vortex Sigil: 4m trigger -> 8m zone pulling inward, launching 12m straight up,
        // -5 armor, strips buffs, Levitation + Weakness III 5.0s (100t), then violently slamming down!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 360, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.9D, 0.05D, 0.9D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trig = new AABB(zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D);
                    List<LivingEntity> triggered = srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggered.isEmpty()) return true;

                    AABB pillar = new AABB(zone.center.x - 8.0D, zone.center.y - 2.0D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D);
                    List<LivingEntity> caught = srv.getEntitiesOfClass(LivingEntity.class, pillar, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    for (LivingEntity foe : caught) {
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 180L);
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.35D);
                        foe.setDeltaMovement(in.x, 1.48D, in.z); // 12m vertical launch!
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
                        foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 8.0D, 100, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 100) return true;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB slamBox = new AABB(sZone.center.x - 8.0D, sZone.center.y - 2.0D, sZone.center.z - 8.0D,
                                        sZone.center.x + 8.0D, sZone.center.y + 18.0D, sZone.center.z + 8.0D);
                                for (LivingEntity foe : sLvl.getEntitiesOfClass(LivingEntity.class, slamBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    foe.removeEffect(MobEffects.LEVITATION);
                                    foe.setDeltaMovement(0.0D, -2.2D, 0.0D);
                                    foe.hasImpulse = true;
                                    foe.hurtServer(sLvl, sLvl.damageSources().fall(), 10.0F + sZone.spell.getEffectiveTier() * 2.0F);
                                }
                                return false;
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
        // Quintessential Firmament Deluge: -5 armor, vaporizes projectiles, 8m launch, Firmament Spatial Vertigo (25s AI + 50% slow)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(coneRange))).inflate(3.0D), null, 0L);

        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.setDeltaMovement(dir.x * 0.65D, 1.12D, dir.z * 0.65D); // 8m airborne!
            hit.hasImpulse = true;
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500); // 25.0s!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 500, 2)); // ~50% speed reduction!
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
        // Cataclysmic Firmament Skyfall: 14m zone, 20.0s (400t) bombardment + 10.0s (200t) Inverted Firmament Pressure (600t total)!
        double r = Math.max(14.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean bombardment = elapsed <= 400;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + (bombardment ? 4.5D : 0.6D), zone.center.z,
                                24, zone.radius * 0.55D, bombardment ? 2.2D : 0.4D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (bombardment) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 180L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.45D, foe.getDeltaMovement().z * 0.25D);
                                foe.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.50F);
                            } else {
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4)); // 70% slow
                                foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 25, 0));
                                foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 3.0F); // 1.5 hearts/s true crushing!
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
        tag.putString("SpellEidolonMateria", EssenceType.AETHER.name());
        tag.putInt("SpellEidolonTintRGB", AETHER_CHALK_GOLD_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 28, 0.5D, 0.6D, 0.5D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Firmament Ascension Step: 20m through walls; departure 6m anti-gravity vacuum (3.0s / 60t);
        // arrival 8m KB, -3 armor, 8.0s (160t) 3D flight + fall immunity, sheds 100% mob aggro!
        AABB depBox = new AABB(departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 4.0D, departurePos.z + 6.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 pull = departurePos.subtract(foe.position()).scale(0.35D);
            foe.setDeltaMovement(pull.x, 0.35D, pull.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
        }

        AABB arrBox = new AABB(arrivalPos.x - 6.0D, arrivalPos.y - 2.0D, arrivalPos.z - 6.0D,
                arrivalPos.x + 6.0D, arrivalPos.y + 4.0D, arrivalPos.z + 6.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, arrBox, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 away = foe.position().subtract(arrivalPos).normalize().scale(1.85D); // 8m KB!
            foe.setDeltaMovement(away.x, 0.42D, away.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 160);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 24.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 0.9F, 1.3F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Quintessential Firmament Genesis [5.0s Channel]:
        // Stone/Deepslate/Netherrack/End Stone -> Calcite/Quartz/Amethyst/Gilded Blackstone;
        // cleanses curses + 35% +1 enchant tier; mob drops -> Aether Crystals/Distillate; empty air -> Purified Aetheric Spring Water!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.AETHER, "Quintessential Firmament Genesis",
                () -> {
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 1, 0.35F, 1);
                    }
                    if (!level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                        level.setBlockAndUpdate(pos.above(), Blocks.WATER.defaultBlockState());
                    }
                })) {
            return true;
        }

        if (state.isAir()) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.NETHERRACK) || state.is(Blocks.END_STONE)) {
            BlockState out = switch (level.random.nextInt(4)) {
                case 0 -> Blocks.CALCITE.defaultBlockState();
                case 1 -> Blocks.QUARTZ_BLOCK.defaultBlockState();
                case 2 -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                default -> Blocks.GILDED_BLACKSTONE.defaultBlockState();
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
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 1, 0.35F, 1)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE) || stack.is(Items.GUNPOWDER)
                || stack.is(Items.SPIDER_EYE) || stack.is(Items.ENDER_PEARL) || stack.is(Items.BLAZE_ROD)
                || stack.is(Items.GHAST_TEAR) || stack.is(Items.PHANTOM_MEMBRANE) || stack.is(Items.STRING)) {
            ItemStack aetherEssence = new ItemStack(ModItems.STRONG_ESSENCE.get(), stack.getCount());
            EssenceItem.setEssenceType(aetherEssence, EssenceType.AETHER);
            itemEntity.setItem(aetherEssence);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Apex Gravitational Collapse Hex: 80% crawl, blocks jumping, -50% toughness, 50% weapon recoil,
        // and 1.5 hearts (3.0 HP) true crushing damage every 3.0s!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.50D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4, true, true)); // ~80% crawl
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
        if ((now / 20L) % 3L == 0L) {
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), 3.0F * mult);
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.4D), target.getZ(), 8, 0.3D, 0.1D, 0.3D, 0.05D);
        }
        return true;
    }
}
