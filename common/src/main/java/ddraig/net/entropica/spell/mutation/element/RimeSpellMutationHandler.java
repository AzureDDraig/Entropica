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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
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
 * Complete per-part Signature Mutation implementation for <b>Rime Materia (Tier 3 Trinity Fusion #41)</b>
 * (Spellcrafting System Guide Section 6.42 — Air + Water + Frozen).
 */
public final class RimeSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int HOARFROST_RIME_HEX_COLOR = 0x6395B1;

    @Override
    public EssenceType materiaType() {
        return EssenceType.RIME;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Crystalline Hoarfrost Spiral Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hoarfrost Rime-Spike Javelin";
            case BEAM -> "Glacial Absolute-Zero Jetstream Beam";
            case RADIAL_BURST -> "Flash-Freeze Rime Frost-Nova";
            case TOUCH_STRIKE -> "Permafrost Rime-Cleave";
            case AURA -> "Hoarfrost Glacial Aegis Mantle";
            case GLYPH -> "Flash-Freeze Rime Pitfall Glyph";
            case CONE -> "Sub-Zero Blizzarding Rime Cone";
            case SKYFALL -> "Glacial Rime Cataclysmic Blizzard";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Permafrost Glacial Slide";
            case TRANSMUTE, BREAK -> "Glacial Cryo-Crystallization Transmute";
            case AFFLICTION -> "Sub-Zero Cryo-Numbness Hex";
            default -> "Hoarfrost Rime " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Crystalline Hoarfrost Spiral Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.02D);
        level.sendParticles(ParticleTypes.WHITE_ASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Hoarfrost Rime-Spike Javelin: pierces 3 foes, encasing in brittle rime-ice (freezing + 3.0s / 60t root);
        // shatters on 3rd target into crystalline frost shrapnel slowing adjacent mobs by 50% for 4.0s (80t)!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard()))) {
            victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 80);
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
        }
        if (projectile.getPierceRemaining() <= 1) {
            shatterFrostShrapnel(level, caster, hitEntity.position(), spell.hasWardFriendlyFireSafeguard());
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        shatterFrostShrapnel(level, caster, blockHit.getLocation(), spell.hasWardFriendlyFireSafeguard());
    }

    private static void shatterFrostShrapnel(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, boolean safeguard) {
        AABB box = new AABB(center.x - 3.5D, center.y - 1.5D, center.z - 3.5D, center.x + 3.5D, center.y + 2.5D, center.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2)); // 50% slow
            foe.setTicksFrozen(foe.getTicksFrozen() + 60);
        }
        BlockPos cp = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cp.offset(-2, -1, -2), cp.offset(2, 1, 2))) {
            if (level.getBlockState(p).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, p)) {
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.5D, center.z, 26, 1.1D, 0.5D, 1.1D, 0.06D);
        level.playSound(null, cp, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.3F);
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
        // Glacial Absolute-Zero Jetstream Beam: rapid freezing + stacking Slowness up to IV;
        // REQUIRES 5.0s sustained channeling (stacks >= 5) to trigger Deep Freeze Shatter (2.5s / 50t solid freeze + massive burst)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("RimeBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("RimeBeamStacks").orElse(0) + 1 : 1;
            data.putLong("RimeBeamLastTick", now);
            data.putInt("RimeBeamStacks", stacks);

            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 60);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(3, stacks)));

            if (stacks >= 5) {
                data.putInt("RimeBeamStacks", 0);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.5D), e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                    caught.hurtServer(level, level.damageSources().freeze(), 9.5F + spell.getEffectiveTier() * 2.4F);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.86F, 0.94F, 1.0F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.1F, 0.75F);
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
        // Flash-Freeze Rime Frost-Nova: 6m radius, strictly ZERO physical light blocks — 6m KB, flash-freezes hostiles 3.0s (60t),
        // coats ground in snow, grants caster & nearby allies Hoarfrost Carapace (+4 Armor & 50% fire res for 8.0s / 160t)!
        double r = Math.max(6.0D, radius);
        long carapaceUntil = level.getGameTime() + 160L;
        grantHoarfrostCarapace(caster, level, carapaceUntil);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || ent.isAlliedTo(caster) || (ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()))) {
                grantHoarfrostCarapace(ent, level, carapaceUntil);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(1.45D);
            ent.setDeltaMovement(away.x, 0.35D, away.z);
            ent.hasImpulse = true;
            ent.setTicksFrozen(ent.getTicksRequiredToFreeze() + 80);
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
        }
        BlockPos cp = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cp.offset(-4, -1, -4), cp.offset(4, 1, 4))) {
            if (level.getBlockState(p).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, p)) {
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.86F, 0.94F, 1.0F), center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.9D, center.z, 44, 2.3D, 0.6D, 2.3D, 0.06D);
    }

    private static void grantHoarfrostCarapace(LivingEntity ally, ServerLevel level, long until) {
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 4.0D, 0.0D, 0.0D, 0.0D, 160L);
        EntityHelper.getPersistentData(ally).putLong(SpellLingeringZoneManager.TAG_RIME_FIRE_RES_UNTIL, until);
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
        // Permafrost Rime-Cleave: shatters 3 armor, flash-freezes primary target 2.5s (50t), 5m KB, leaves needle-ice rooting 2.0s (40t)!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 80);
            hit.setDeltaMovement(push.x * 1.25D, 0.32D, push.z * 1.25D);
            hit.hasImpulse = true;
        }
        Vec3 trailPos = caster.position().add(push.scale(2.4D));
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, trailPos, 2.5D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                        }
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.15D, zone.center.z, 8, 1.0D, 0.1D, 1.0D, 0.02D);
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
        // Hoarfrost Glacial Aegis Mantle: +7 Armor, +3 Toughness, Freeze/PowderSnow/Slowness/Fire immunity, Packed Ice underfoot!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Flash-Freeze Rime Pitfall Glyph: 4m rime-ice chasm for 4.0s (80t), rooting 3.5s (70t), stripping 3 armor, blocking jump!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.5D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.8D, 0.05D, 0.8D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D);
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (victims.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB chasm = new AABB(sZone.center.x - 4.0D, sZone.center.y - 1.5D, sZone.center.z - 4.0D,
                                        sZone.center.x + 4.0D, sZone.center.y + 2.5D, sZone.center.z + 4.0D);
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, chasm, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 6));
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 35, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
                                    v.setTicksFrozen(v.getTicksRequiredToFreeze() + 60);
                                    if (sEl == 0) {
                                        SpellCombatReactionEngine.applyTimedAttributeShred(v, sLvl, 3.0D, 0.0D, 160L);
                                    }
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.55F);
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.SNOWFLAKE, sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 16, 1.5D, 0.3D, 1.5D, 0.04D);
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.85F);
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
        // Sub-Zero Blizzarding Rime Cone: extinguishes fires/torches, freezes water into packed ice, 3m KB,
        // Hypothermic Numbness (Slowness IV, -50% attack speed, & 25.0s / 500t AI pathfinding disruption)!
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.clearFire();
            hit.setDeltaMovement(dirNorm.x * 0.85D, 0.22D, dirNorm.z * 0.85D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3));
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.50D, 100L);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
        }
        Vec3 eye = caster.getEyePosition();
        for (double d = 1.5D; d <= coneRange; d += 1.6D) {
            BlockPos step = BlockPos.containing(eye.add(dirNorm.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -2, -1), step.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER) && level.getFluidState(p).isSource()) {
                    level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH)) {
                    level.destroyBlock(p, true);
                }
            }
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
        // Glacial Rime Cataclysmic Blizzard: Extended 18.0s (360t) storm + 8.0s (160t) lingering permafrost (520t total)
        // across 12m radius — slams downward, shatters 5 armor, 6.0s (120t) freezing stasis, 60% slow!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.3D), zone.center.z,
                                28, zone.radius * 0.55D, stormActive ? 2.0D : 0.2D, zone.radius * 0.55D, 0.06D);
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 120);
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
        tag.putString("SpellEidolonMateria", EssenceType.RIME.name());
        tag.putInt("SpellEidolonTintRGB", HOARFROST_RIME_HEX_COLOR);
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Permafrost Glacial Slide: 18m slide leaving packed/blue ice trail, knocking aside mobs in trajectory,
        // granting Frost Walker & +30% move speed for 6.0s (120t)!
        Vec3 diff = arrivalPos.subtract(departurePos);
        int steps = Math.max(4, (int) diff.length());
        Map<BlockPos, BlockState> trail = new HashMap<>();
        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(diff.scale((double) i / steps));
            BlockPos below = BlockPos.containing(p).below();
            BlockState bs = level.getBlockState(below);
            if ((bs.is(BlockTags.DIRT) || bs.is(Blocks.STONE) || bs.is(Blocks.WATER))
                    && !trail.containsKey(below)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, below, bs)) {
                trail.put(below.immutable(), bs);
                level.setBlockAndUpdate(below, (i % 3 == 0 ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState());
            }
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(1.6D), e -> e.isAlive() && e != caster)) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                Vec3 side = foe.position().subtract(p).normalize().scale(1.1D);
                foe.setDeltaMovement(side.x, 0.28D, side.z);
                foe.hasImpulse = true;
            }
        }
        if (!trail.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, arrivalPos, 2.0D, 120, level.getGameTime(), null, null, EssenceType.RIME, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (Map.Entry<BlockPos, BlockState> e : trail.entrySet()) {
                            BlockState cur = srv.getBlockState(e.getKey());
                            if (cur.is(Blocks.PACKED_ICE) || cur.is(Blocks.BLUE_ICE)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
            ));
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, level.getGameTime() + 120L);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 1));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Glacial Cryo-Crystallization Transmute: water -> Packed/Blue Ice, lava -> Obsidian, dirt/grass -> Coarse Dirt/Packed Ice,
        // mud -> Clay, sand -> Calcite/Quartz, air condensation -> Ice!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.QUARTZ_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.WATER_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, stack.getCount()));
            return true;
        }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.QUARTZ_BLOCK, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sub-Zero Cryo-Numbness Hex: suppresses weapon & armor enchantments (SpellCombatReactionEngine),
        // strips 30% armor toughness, 0.5s (10t) stun every 3.0s, continuous sub-zero freezing damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.30D * mult), 45L);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 60);
        if ((now / 20L) % 3L == 0L) {
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
        }
        target.hurtServer(level, level.damageSources().freeze(), (1.55F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.85D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }
}
