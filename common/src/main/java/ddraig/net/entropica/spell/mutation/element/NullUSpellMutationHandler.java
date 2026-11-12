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
 * Complete per-part Signature Mutation implementation for <b>Null_U Materia (Ethereal &amp; Paradox Fusion #31)</b>
 * (Spellcrafting System Guide Section 6.32 — Void + Umbral [Leans: Void]).
 */
public final class NullUSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VOID_STALKER_OBLIVION_HEX_COLOR = 0x291D33;

    @Override
    public EssenceType materiaType() {
        return EssenceType.NULL_U;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Abyssal Singularity Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Black Hole Singularity Orb";
            case BEAM -> "Void-Umbral Disintegration Stream";
            case RADIAL_BURST -> "Abyssal Gravity Shockwave";
            case TOUCH_STRIKE -> "Spacetime Oblivion Palm";
            case AURA -> "Abyssal Zero-Acoustic Veil";
            case GLYPH -> "Shadow-Snare Singularity Mine";
            case CONE -> "Void-Umbral Disintegration Wave";
            case SKYFALL -> "Black Hole Oblivion Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Abyssal Event Horizon Blink";
            case TRANSMUTE, BREAK -> "Abyssal Dark Matter Transmute";
            case AFFLICTION -> "Sensory Blackout Hex";
            default -> "Abyssal Singularity " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Abyssal Singularity Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setWallPhasing(true);
        projectile.setMaxFlightTicks(95);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.03D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        collapseBlackHoleOrb(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        collapseBlackHoleOrb(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        collapseBlackHoleOrb(level, caster, projectile.position(), spell);
    }

    private static void collapseBlackHoleOrb(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // Black Hole Singularity Orb: 4-block black hole dragging caught mobs inward, 3.0s (60t) Blindness, & wiping mob aggro!
        AABB box = new AABB(center.x - 4.0D, center.y - 2.0D, center.z - 4.0D, center.x + 4.0D, center.y + 3.0D, center.z + 4.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 pull = center.subtract(foe.position()).normalize().scale(1.0D);
            foe.setDeltaMovement(pull.x, 0.22D, pull.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            foe.setLastHurtByMob(null);
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 0.6D, center.z, 18, 1.1D, 0.5D, 1.1D, 0.04D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.6D, center.z, 24, 1.2D, 0.5D, 1.2D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WARDEN_TENDRIL_CLICKS, SoundSource.PLAYERS, 0.85F, 0.65F);
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
        // Void-Umbral Disintegration Stream: silences abilities, melts armor (-1/s up to -4), pulls toward caster,
        // and 3s continuous channeling triggers Event Horizon Implosion (4m inward drag + 4s Blindness)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellLingeringZoneManager.applyArcaneSilence(hit, 45);
            Vec3 pull = caster.position().subtract(hit.position()).normalize().scale(0.45D);
            hit.setDeltaMovement(pull.x, 0.18D, pull.z);
            hit.hasImpulse = true;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("NullUBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("NullUBeamStacks").orElse(0) + 1 : 1;
            data.putLong("NullUBeamLastTick", now);
            data.putInt("NullUBeamStacks", stacks);

            double armorMelt = Math.min(4.0D, stacks);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorMelt, 0.0D, 80L);

            if (stacks >= 3) {
                data.putInt("NullUBeamStacks", 0);
                Vec3 implosionCenter = hit.position();
                AABB implosionBox = new AABB(implosionCenter.x - 4.0D, implosionCenter.y - 2.0D, implosionCenter.z - 4.0D,
                        implosionCenter.x + 4.0D, implosionCenter.y + 3.0D, implosionCenter.z + 4.0D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, implosionBox)) {
                    if (nearby == caster || !nearby.isAlive()) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(nearby, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    Vec3 drag = implosionCenter.subtract(nearby.position()).normalize().scale(1.1D);
                    nearby.setDeltaMovement(drag.x, 0.25D, drag.z);
                    nearby.hasImpulse = true;
                    nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                }
                level.sendParticles(ParticleTypes.SQUID_INK, implosionCenter.x, implosionCenter.y + 0.8D, implosionCenter.z, 22, 1.1D, 0.5D, 1.1D, 0.05D);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, implosionCenter.x, implosionCenter.y + 0.8D, implosionCenter.z, 28, 1.2D, 0.5D, 1.2D, 0.08D);
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
        // Abyssal Gravity Shockwave: silent 6m shockwave pulling hostiles inward then blasting backward,
        // 4.0s (80t) Blindness, wipes mob aggro, and renders caster Invisible & silent for 4.0s (80t)!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, box,
                foe -> foe != caster && foe.isAlive() && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard()));

        for (LivingEntity foe : victims) {
            Vec3 inward = center.subtract(foe.position()).normalize().scale(0.95D);
            foe.setDeltaMovement(inward.x, 0.20D, inward.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.setLastHurtByMob(null);
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }

        // Delayed secondary blast hurling pulled enemies backward 6 ticks later!
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
                        foe.setDeltaMovement(away.x, 0.38D, away.z);
                        foe.hasImpulse = true;
                    }
                    srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 0.8D, zone.center.z, 28, 2.0D, 0.4D, 2.0D, 0.06D);
                    return false;
                }
        ));

        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, false));
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 80L);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.7D, center.z, 32, 2.0D, 0.5D, 2.0D, 0.08D);
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
        // Spacetime Oblivion Palm: bypasses 50% physical armor, roots shadow 2.5s (50t), random warps over 6s (120t)
        // while blinded, and siphons 25% of damage into caster health!
        long now = level.getGameTime();
        boolean hitAny = false;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hitAny = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 50L);
            data.putLong(SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, now + 120L);
            level.sendParticles(ParticleTypes.SQUID_INK, hit.getX(), hit.getY(0.8D), hit.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.04D);
        }
        if (hitAny) {
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.25F, apparatusPotency);
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
        // Abyssal Zero-Acoustic Veil: zero-g glide, complete Sculk/Warden immunity, 70% mob aggro cut,
        // 50% arrow deflection, and melee attacker 3s blind + silence!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL, until);
        data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, until);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        level.sendParticles(ParticleTypes.SQUID_INK, caster.getX(), caster.getY(0.9D), caster.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.03D);
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
        // Shadow-Snare Singularity Mine: 4m black hole trap dragging mobs to center, pinning flat 3.0s (60t),
        // 4.0s (80t) Blindness, wiping mob aggro, and continuous Null_U damage!
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
                            owner, zone.center, 4.0D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB box = new AABB(sZone.center.x - 4.0D, sZone.center.y - 1.5D, sZone.center.z - 4.0D,
                                        sZone.center.x + 4.0D, sZone.center.y + 3.0D, sZone.center.z + 4.0D);
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    Vec3 drag = sZone.center.subtract(v.position()).scale(0.32D);
                                    v.setDeltaMovement(drag.x, Math.min(v.getDeltaMovement().y, -0.25D), drag.z);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 5));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    v.setLastHurtByMob(null);
                                    if (v instanceof Mob mob) {
                                        mob.setTarget(null);
                                    }
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F);
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.REVERSE_PORTAL, sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 12, 1.2D, 0.2D, 1.2D, 0.05D);
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
        // Void-Umbral Disintegration Wave: 6m pushback, 4.0s Blindness, wipes mob aggro, melts 3 armor points for 6s (120t),
        // and extinguishes torches & open fires!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.34D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
            hit.setLastHurtByMob(null);
            if (hit instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        EclipseSpellMutationHandler.snuffTorchesAndFires(level, BlockPos.containing(mid), 4);
        level.sendParticles(ParticleTypes.SQUID_INK, mid.x, mid.y, mid.z, 22, 1.5D, 0.6D, 1.5D, 0.04D);
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
        // Black Hole Oblivion Tempest: 10s (200t), 14m dark singularity rain — pulls toward center, lifts 3m airborne in zero-g,
        // blinds hostiles, and slams them violently into the ground when the rift expires!
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
                            v.hurtServer(srv, srv.damageSources().magic(), 6.0F + zone.spell.getEffectiveTier() * 1.45F);
                        }
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 0.8D, zone.center.z, 32, zone.radius * 0.45D, 0.5D, zone.radius * 0.45D, 0.07D);
                        return false;
                    }
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 4.5D, zone.center.z, 18, zone.radius * 0.50D, 1.3D, zone.radius * 0.50D, 0.04D);
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 4.0D, zone.center.z, 16, zone.radius * 0.45D, 1.2D, zone.radius * 0.45D, 0.06D);
                    }
                    if (elapsed % 20 == 0) {
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 pullAndLift = zone.center.add(0.0D, 3.0D, 0.0D).subtract(v.position()).scale(0.26D);
                            v.setDeltaMovement(pullAndLift.x, Math.max(0.32D, pullAndLift.y), pullAndLift.z);
                            v.hasImpulse = true;
                            v.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
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
        tag.putString("SpellEidolonMateria", EssenceType.NULL_U.name());
        tag.putInt("SpellEidolonTintRGB", VOID_STALKER_OBLIVION_HEX_COLOR);
        level.sendParticles(ParticleTypes.SQUID_INK, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 18, 0.4D, 0.5D, 0.4D, 0.04D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.4D, 0.5D, 0.4D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Abyssal Event Horizon Blink: 16m silent teleport leaving a micro-singularity at departure pulling pursuers inward & blinding them!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 60L);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 3.8D, 40, level.getGameTime(), null, null, EssenceType.NULL_U, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.8D, zone.center.z, 10, 0.7D, 0.4D, 0.7D, 0.05D);
                        AABB box = new AABB(zone.center.x - 3.8D, zone.center.y - 1.5D, zone.center.z - 3.8D,
                                zone.center.x + 3.8D, zone.center.y + 2.5D, zone.center.z + 3.8D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && foe.isAlliedTo(owner)) continue;
                            Vec3 pull = zone.center.subtract(foe.position()).normalize().scale(0.55D);
                            foe.setDeltaMovement(pull.x, 0.12D, pull.z);
                            foe.hasImpulse = true;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                            if (foe instanceof Mob mob && mob.getTarget() == owner) {
                                mob.setTarget(null);
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Abyssal Dark Matter Transmute: dissolves cobble, stone, deepslate, and ores into blackstone & raw dark matter slag!
        String blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean isOre = blockKey.endsWith("_ore") || state.is(Blocks.ANCIENT_DEBRIS);
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE) || isOre) {
            BlockState targetState = isOre
                    ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : Blocks.BLACKSTONE.defaultBlockState();
            level.setBlockAndUpdate(pos, targetState);
            if (isOre) {
                Block.popResource(level, pos.above(), new ItemStack(Items.BLACKSTONE, 1 + level.random.nextInt(2)));
            }
            level.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.02D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;
        String itemKey = BuiltInRegistries.ITEM.getKey(in.getItem()).getPath();
        boolean isOre = itemKey.endsWith("_ore") || itemKey.startsWith("raw_");
        if (in.is(Items.STONE) || in.is(Items.COBBLESTONE) || in.is(Items.DEEPSLATE) || in.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.BLACKSTONE, in.getCount()));
            return true;
        }
        if (isOre) {
            itemEntity.setItem(new ItemStack(Items.CRYING_OBSIDIAN, Math.max(1, in.getCount())));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sensory Blackout Hex: total Blindness + Silence, -40% movement speed, prevents health regen;
        // cleansed by Restoration/Radiant light magic, eating mushroom soup/stew, or caster defeat!
        if (target instanceof Player stewPlayer && stewPlayer.isUsingItem()) {
            ItemStack useItem = stewPlayer.getUseItem();
            if (useItem.is(Items.MUSHROOM_STEW) || useItem.is(Items.SUSPICIOUS_STEW)
                    || useItem.is(Items.RABBIT_STEW) || useItem.is(Items.BEETROOT_SOUP)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(stewPlayer);
                level.sendParticles(ParticleTypes.END_ROD, stewPlayer.getX(), stewPlayer.getY(1.0D), stewPlayer.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.04D);
                return true;
            }
        }
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(4, Math.max(1, Math.round(1.5F * mult))), true, true));
        SpellLingeringZoneManager.applyArcaneSilence(target, 40);
        if (target instanceof Mob mob) {
            mob.setTarget(null);
        }
        target.hurtServer(level, level.damageSources().magic(), (1.35F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }
}
