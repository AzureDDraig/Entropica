package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Vapor Materia (Ethereal &amp; Paradox Fusion #29)</b>
 * (Spellcrafting System Guide Section 6.30 — Water + Nether [Leans: Water]).
 */
public final class VaporSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int STEAM_AUTOMATON_HEX_COLOR = 0xA8E6EA;

    @Override
    public EssenceType materiaType() {
        return EssenceType.VAPOR;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Hydro-Steam Split Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Scalding Steam-Burst Geyser Orb";
            case BEAM -> "Scalding Steam Jetstream";
            case RADIAL_BURST -> "Scalding Pressure Blast";
            case TOUCH_STRIKE -> "Superheated Hydro-Gouge";
            case AURA -> "Mist-Walker Shroud";
            case GLYPH -> "Scalding Geyser Pitfall Trap";
            case CONE -> "Scalding Steam-Blast Deluge";
            case SKYFALL -> "Thermal Condensation Storm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Mist-Slip Blink";
            case TRANSMUTE, BREAK -> "Cloud Condensation Transmute";
            case AFFLICTION -> "Scalding Dissolution Hex";
            default -> "Scalding Vapor " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Hydro-Steam Split Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileImpactOrExpireFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 splitOrigin,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Splits into 3 pressurized steam bursts arcing outward in a wide fan to blind secondary enemies!
        float[] fan = new float[]{-28.0F, 0.0F, 28.0F};
        for (float yaw : fan) {
            SpellProjectileEntity child = projectile.spawnSplitChild(level, splitOrigin, yaw, false, 0.55F, 1.25F);
            child.setNoGravity(false);
        }
        level.sendParticles(ParticleTypes.CLOUD, splitOrigin.x, splitOrigin.y + 0.3D, splitOrigin.z, 16, 0.35D, 0.3D, 0.35D, 0.06D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        spawnSteamGeyserUpdraft(level, caster, projectile.position(), spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        spawnSteamGeyserUpdraft(level, caster, projectile.position(), spell, slot);
    }

    private static void spawnSteamGeyserUpdraft(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell, SubSpellSlot slot) {
        // Scalding Steam-Burst Geyser Orb: 3m boiling vapor cloud blinding 3s (60t), dousing fires, thermal updraft launching 4m airborne!
        AABB box = new AABB(center.x - 3.0D, center.y - 1.5D, center.z - 3.0D, center.x + 3.0D, center.y + 2.5D, center.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applySoaked(foe, 140);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.4D, 0.78D, foe.getDeltaMovement().z * 0.4D);
            foe.hasImpulse = true;
        }
        douseFiresAndMeltIce(level, BlockPos.containing(center), 3, false);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 2.8D, 60, level.getGameTime(), spell, slot, EssenceType.VAPOR, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 0.5D, zone.center.z, 10, 1.1D, 0.6D, 1.1D, 0.05D);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            ent.setDeltaMovement(ent.getDeltaMovement().x, Math.max(ent.getDeltaMovement().y, 0.72D), ent.getDeltaMovement().z);
                            ent.hasImpulse = true;
                        }
                    }
                    return true;
                }
        ));
        level.playSound(null, BlockPos.containing(center), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.9F, 1.15F);
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
        // Scalding Steam Jetstream: blinds, pushes 3m back, melts snow & packed ice -> water, vertical jetpack recoil, 3s Convective Hydro-Burst (6m launch)!
        if (direction.y < -0.45D) {
            caster.setDeltaMovement(caster.getDeltaMovement().x, Math.max(0.58D, caster.getDeltaMovement().y + 0.42D), caster.getDeltaMovement().z);
            caster.hasImpulse = true;
            caster.resetFallDistance();
        }
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            SpellLingeringZoneManager.applySoaked(hit, 140);
            hit.setDeltaMovement(direction.x * 0.80D, 0.25D, direction.z * 0.80D);
            hit.hasImpulse = true;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("VaporBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("VaporBeamStacks").orElse(0) + 1 : 1;
            data.putLong("VaporBeamLastTick", now);
            data.putInt("VaporBeamStacks", stacks);
            if (stacks >= 3) {
                data.putInt("VaporBeamStacks", 0);
                hit.setDeltaMovement(direction.x * 0.45D, 1.02D, direction.z * 0.45D);
                hit.hasImpulse = true;
                level.sendParticles(ParticleTypes.CLOUD, hit.getX(), hit.getY(0.8D), hit.getZ(), 22, 0.45D, 0.5D, 0.45D, 0.08D);
            }
        }
        int steps = Math.min(14, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            douseFiresAndMeltIce(level, BlockPos.containing(caster.getEyePosition().add(direction.scale(s))), 1, true);
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
        // Scalding Pressure Blast: 6m pneumatic steam blast launching surrounding mobs 8m airborne & 6m backward + shatters glass!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.45D);
            foe.setDeltaMovement(away.x, 1.18D, away.z);
            foe.hasImpulse = true;
            SpellLingeringZoneManager.applySoaked(foe, 160);
        }
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-4, -1, -4), cPos.offset(4, 2, 4))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(BlockTags.IMPERMEABLE) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                level.destroyBlock(p, true, caster);
            }
        }
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.9D, center.z, 42, 2.3D, 0.6D, 2.3D, 0.10D);
        level.playSound(null, cPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.1F, 0.85F);
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
        // Superheated Hydro-Gouge: bypasses 40% armor, extinguishes fires, slain enemies dissolve into blinding steam cloud!
        caster.clearFire();
        long until = level.getGameTime() + 100L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.clearFire();
            SpellLingeringZoneManager.applySoaked(hit, 140);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VAPOR_BOIL_DEATH_UNTIL, until);
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
        // Mist-Walker Shroud: invisibility in rain/water, +30% swim & sprint speed!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, true));
        if (caster.isInWaterOrRain()) {
            caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
        }
        level.sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.03D);
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
        // Scalding Geyser Pitfall Trap: violent vertical steam jet for 3s (60t) — launches 10m straight up, blinds 4s, snuffs torches in 4m!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 1.0D, 0.1D, 1.0D, 0.01D);
                    }
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    EclipseSpellMutationHandler.snuffTorchesAndFires(srv, BlockPos.containing(zone.center), 4);
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 3.0D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (gLvl, gZone, gEl) -> {
                                if (gEl > gZone.maxDurationTicks) return false;
                                if (gEl % 5 == 0) {
                                    gLvl.sendParticles(ParticleTypes.CLOUD, gZone.center.x, gZone.center.y + 2.0D, gZone.center.z, 18, 0.6D, 2.2D, 0.6D, 0.08D);
                                }
                                if (gEl == 0 || gEl % 20 == 0) {
                                    LivingEntity gOwner = gZone.resolveCaster(gLvl);
                                    AABB jetBox = new AABB(gZone.center.x - 3.0D, gZone.center.y - 1.0D, gZone.center.z - 3.0D,
                                            gZone.center.x + 3.0D, gZone.center.y + 5.0D, gZone.center.z + 3.0D);
                                    for (LivingEntity v : gLvl.getEntitiesOfClass(LivingEntity.class, jetBox, e -> e.isAlive() && e != gOwner)) {
                                        if (gOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, gOwner, gZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                        v.setDeltaMovement(v.getDeltaMovement().x * 0.2D, 1.38D, v.getDeltaMovement().z * 0.2D);
                                        v.hasImpulse = true;
                                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                                        SpellExecutionEngine.deliverSubSpellOnEntity(gLvl, gOwner, v, new Vec3(0, 1, 0), gZone.spell, gZone.slot, gZone.fallbackMateria, gZone.apparatusPotency * 0.55F);
                                    }
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.1F, 0.9F);
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
        // Scalding Steam-Blast Deluge: 6m pushback, 4s Blindness, snuffs torches/fires, melts snow & packed ice -> water, backward recoil thrust!
        Vec3 recoil = direction.scale(-0.65D);
        caster.setDeltaMovement(caster.getDeltaMovement().add(recoil.x, Math.max(0.15D, recoil.y * 0.5D), recoil.z));
        caster.hasImpulse = true;

        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.35D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            SpellLingeringZoneManager.applySoaked(hit, 160);
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            EclipseSpellMutationHandler.snuffTorchesAndFires(level, stepPos, 2);
            douseFiresAndMeltIce(level, stepPos, 2, true);
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
        // Thermal Condensation Storm: 12s (240t), 14m scalding rain — shreds 1 armor per 2s (up to 6 total) & hydrates all farmland!
        double r = Math.max(10.0D, radius);
        BlockPos cPos = BlockPos.containing(impactCenter);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-8, -2, -8), cPos.offset(8, 2, 8))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 240, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 4.5D, zone.center.z, 20, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.04D);
                        srv.sendParticles(ParticleTypes.RAIN, zone.center.x, zone.center.y + 4.0D, zone.center.z, 26, zone.radius * 0.55D, 1.0D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        int armorShred = Math.min(6, 1 + (elapsed / 40));
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            SpellLingeringZoneManager.applySoaked(v, 80);
                            SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, armorShred, 0.0D, 60L);
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
        tag.putString("SpellEidolonMateria", EssenceType.VAPOR.name());
        tag.putInt("SpellEidolonTintRGB", STEAM_AUTOMATON_HEX_COLOR);
        level.sendParticles(ParticleTypes.CLOUD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Mist-Slip Blink: 16m invisible phase through enemies leaving a dense blinding fog trail!
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
        Vec3 delta = arrivalPos.subtract(departurePos);
        int steps = Math.max(2, (int) Math.ceil(delta.length() * 0.5D));
        Vec3 stepVec = delta.scale(1.0D / steps);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(ParticleTypes.CLOUD, p.x, p.y + 0.9D, p.z, 8, 0.45D, 0.45D, 0.45D, 0.02D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - 1.8D, p.y - 0.5D, p.z - 1.8D, p.x + 1.8D, p.y + 2.2D, p.z + 1.8D))) {
                if (foe == caster || !foe.isAlive() || foe.isAlliedTo(caster)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Cloud Condensation Transmute: condenses air -> water source, dries mud -> clay, melts snow/ice -> water!
        if (state.isAir()) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.WET_SPONGE.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.ICE) || state.is(Blocks.SNOW_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, in.getCount()));
            return true;
        }
        if (in.is(Items.SPONGE)) {
            itemEntity.setItem(new ItemStack(Items.WET_SPONGE, in.getCount()));
            return true;
        }
        if (in.is(Items.BUCKET) && in.getCount() == 1) {
            itemEntity.setItem(new ItemStack(Items.WATER_BUCKET, 1));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Scalding Dissolution Hex: dissolves 2 armor points every 3s up to 10 total, slows 30%, +50% Lightning shock damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        CompoundTag data = EntityHelper.getPersistentData(target);
        int ticksElapsed = data.getInt("VaporHexDissolveSteps").orElse(0) + 1;
        data.putInt("VaporHexDissolveSteps", ticksElapsed);
        double dissolvedArmor = Math.min(10.0D, (2.0D + (ticksElapsed / 3) * 2.0D) * mult);

        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, dissolvedArmor, 0.0D, 40L);
        SpellLingeringZoneManager.applySoaked(target, 40);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, true));
        target.hurtServer(level, level.damageSources().magic(), (1.35F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    private static void douseFiresAndMeltIce(ServerLevel level, BlockPos center, int rad, boolean includePackedIce) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock || bs.is(Blocks.SNOW)) {
                level.removeBlock(p, false);
            } else if (!SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)
                    && (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.SNOW_BLOCK)
                    || (includePackedIce && bs.is(Blocks.PACKED_ICE)))) {
                level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
            }
        }
    }
}
