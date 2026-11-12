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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
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
 * Complete per-part Signature Mutation implementation for <b>Storm Materia (Tier 1 Fusion #14)</b>
 * (Spellcrafting System Guide Section 6.15).
 */
public final class StormSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TEMPEST_ZEPHYR_HEX_COLOR = 0x429BA3;

    @Override
    public EssenceType materiaType() {
        return EssenceType.STORM;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Squall-Leap Chains";
            default -> "Tempest Squall " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Squall Tempest Slug";
            case BEAM -> "Gale-Cutter Torrent Beam";
            case RADIAL_BURST -> "Tempest Nova Burst";
            case TOUCH_STRIKE -> "Squall Cleave";
            case AURA -> "Squall-Rider Shroud";
            case GLYPH -> "Atmospheric Downburst Trap";
            case CONE -> "Deluge Squall Cone";
            case SKYFALL -> "Tempest Monsoon Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Squall-Step";
            case TRANSMUTE, BREAK -> "Aero-Hydraulic Erosion";
            case AFFLICTION -> "Waterlogged Hypoxia Hex";
            case KINETIC -> "Hurricane Force Push";
            default -> "Tempest " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Squall-Leap Chains: pulls chained victims toward each other with vacuum currents and drenches them!
        Vec3 mid = fromTarget.position().add(toTarget.position()).scale(0.5D);
        Vec3 pullA = mid.subtract(fromTarget.position()).normalize().scale(0.60D);
        Vec3 pullB = mid.subtract(toTarget.position()).normalize().scale(0.60D);
        fromTarget.setDeltaMovement(pullA.x, 0.22D, pullA.z);
        toTarget.setDeltaMovement(pullB.x, 0.22D, pullB.z);
        fromTarget.hasImpulse = true;
        toTarget.hasImpulse = true;
        SpellLingeringZoneManager.applySoaked(fromTarget, 160);
        SpellLingeringZoneManager.applySoaked(toTarget, 160);
        level.sendParticles(ParticleTypes.SPLASH, mid.x, mid.y + 0.8D, mid.z, 12, 0.4D, 0.3D, 0.4D, 0.08D);
        level.sendParticles(ParticleTypes.GUST, mid.x, mid.y + 0.8D, mid.z, 2, 0.25D, 0.2D, 0.25D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.02D);
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Squall Tempest Slug: 5-block knockback, soaks target, extinguishes surface fires!
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            Vec3 push = projectile.getDeltaMovement().normalize();
            living.setDeltaMovement(push.x * 1.25D, 0.38D, push.z * 1.25D);
            living.hasImpulse = true;
            living.clearFire();
            SpellLingeringZoneManager.applySoaked(living, 180);
        }
        extinguishFiresAround(level, BlockPos.containing(projectile.position()), 2);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        extinguishFiresAround(level, blockHit.getBlockPos(), 2);
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 14, 0.35D, 0.35D, 0.35D, 0.08D);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Gale-Cutter Torrent Beam: shoves enemies back, drenches, extinguishes fire, recoil propulsion when aimed down!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.05D, Math.max(0.25D, direction.y * 0.45D), direction.z * 1.05D);
            hit.hasImpulse = true;
            hit.clearFire();
            SpellLingeringZoneManager.applySoaked(hit, 160);
        }
        boolean groundRecoil = direction.y < -0.35D
                || (direction.y < -0.15D && blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                    && blockHit.getLocation().distanceToSqr(caster.position()) <= 20.25D);
        if (groundRecoil) {
            caster.setDeltaMovement(caster.getDeltaMovement().x - direction.x * 0.45D, 0.72D, caster.getDeltaMovement().z - direction.z * 0.45D);
            caster.hasImpulse = true;
            caster.resetFallDistance();
        }
        int steps = Math.min(16, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            extinguishFiresAround(level, BlockPos.containing(caster.getEyePosition().add(direction.scale(s))), 1);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Tempest Nova Burst: 6m radius blasting mobs 8 blocks backward, deflecting projectiles, soaking entities, extinguishing fires
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            SpellLingeringZoneManager.applySoaked(entity, 180);
            entity.clearFire();
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = entity.position().subtract(center).normalize().scale(1.75D);
            entity.setDeltaMovement(away.x, 0.48D, away.z);
            entity.hasImpulse = true;
        }
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
            if (proj.getOwner() == caster) continue;
            Vec3 away = proj.position().subtract(center).normalize().scale(1.45D);
            proj.setDeltaMovement(away);
            proj.hasImpulse = true;
        }
        extinguishFiresAround(level, BlockPos.containing(center), 6);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.9F);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Squall Cleave: hurls victim 6m back & 4m airborne, drenches, deflects incoming arrows during the swing!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.85D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.clearFire();
            SpellLingeringZoneManager.applySoaked(hit, 180);
        }
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, caster.getBoundingBox().inflate(3.5D))) {
            if (proj.getOwner() == caster) continue;
            proj.setDeltaMovement(direction.scale(1.4D));
            proj.hasImpulse = true;
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Squall-Rider Shroud: continuous glider drift + slow-fall, speed doubled in rain/thunder!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.GUST, caster.getX(), caster.getY(0.8D), caster.getZ(), 8, 0.45D, 0.45D, 0.45D, 0.04D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Atmospheric Downburst Trap: pins mobs in a 4m radius flat to the ground for 3s (60t), disabling movement & jumping!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 4.0D, zone.center.y - 1.0D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        SpellLingeringZoneManager.applySoaked(v, 160);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 1.5D, zone.center.z, 36, 2.0D, 0.5D, 2.0D, 0.12D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.7F);

                    // Sustain active 3-second (60t) crushing atmospheric downburst field pinning mobs flat!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (dsrv, dzone, delapsed) -> {
                                if (delapsed > dzone.maxDurationTicks) return false;
                                if (delapsed % 5 == 0) {
                                    dsrv.sendParticles(ParticleTypes.SPLASH, dzone.center.x, dzone.center.y + 1.8D, dzone.center.z,
                                            16, 2.0D, 0.4D, 2.0D, 0.08D);
                                }
                                LivingEntity dOwner = dzone.resolveCaster(dsrv);
                                long lockUntil = dsrv.getGameTime() + 15L;
                                for (LivingEntity pinned : dsrv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != dOwner)) {
                                    if (dOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(pinned, dOwner, dzone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    SpellLingeringZoneManager.applySoaked(pinned, 60);
                                    pinned.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 6, false, false));
                                    pinned.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 20, 0, false, false));
                                    EntityHelper.getPersistentData(pinned).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, lockUntil);
                                    pinned.setDeltaMovement(0.0D, Math.min(-0.45D, pinned.getDeltaMovement().y), 0.0D);
                                    pinned.hasImpulse = true;
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
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Deluge Squall Cone: pushes enemies 8m back, soaks, extinguishes fires, washes away cobwebs, hydrates farmland!
        for (LivingEntity hit : hitTargets) {
            SpellLingeringZoneManager.applySoaked(hit, 180);
            hit.clearFire();
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.70D, 0.40D, direction.z * 1.70D);
            hit.hasImpulse = true;
        }
        int steps = Math.min(10, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-2, -1, -2), stepPos.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.is(Blocks.COBWEB)) {
                    level.destroyBlock(p, true, caster);
                } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                    level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Tempest Monsoon Downpour: 16m hurricane downpour for 8s (160t) drenching, hydrating, pushing hostiles to rim, grounding flyers!
        double r = Math.max(8.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 160, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 4 == 0) {
                        srv.sendParticles(ParticleTypes.RAIN, zone.center.x, zone.center.y + 5.0D, zone.center.z, 35, zone.radius * 0.55D, 1.5D, zone.radius * 0.55D, 0.15D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(8, (int) Math.ceil(zone.radius));
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -1, -br), cPos.offset(br, 1, br))) {
                            BlockState bs = srv.getBlockState(p);
                            if (bs.getBlock() instanceof BaseFireBlock) {
                                srv.removeBlock(p, false);
                            } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                                srv.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                            }
                        }
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 7.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            SpellLingeringZoneManager.applySoaked(v, 180);
                            v.clearFire();
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 toRim = v.position().subtract(zone.center).normalize().scale(0.75D);
                            double downY = !v.onGround() ? -0.85D : 0.18D; // Grounds flying mobs!
                            v.setDeltaMovement(toRim.x, downY, toRim.z);
                            v.hasImpulse = true;
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, toRim, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.STORM.name());
        tag.putInt("SpellEidolonTintRGB", TEMPEST_ZEPHYR_HEX_COLOR);
        level.sendParticles(ParticleTypes.SPLASH, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.08D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Squall-Step: vacuum splash at departure + +40% movement speed on arrival!
        AABB depBox = new AABB(departurePos.x - 3.5D, departurePos.y - 1.5D, departurePos.z - 3.5D,
                departurePos.x + 3.5D, departurePos.y + 2.5D, departurePos.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox)) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            Vec3 in = departurePos.subtract(foe.position()).normalize().scale(0.8D);
            foe.setDeltaMovement(in.x, 0.25D, in.z);
            foe.hasImpulse = true;
            SpellLingeringZoneManager.applySoaked(foe, 140);
        }
        level.sendParticles(ParticleTypes.SPLASH, departurePos.x, departurePos.y + 0.8D, departurePos.z, 22, 0.5D, 0.4D, 0.5D, 0.08D);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100 + potencyTier * 20, 1));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Aero-Hydraulic Erosion: Dirt -> Mud -> Clay -> Gravel -> Sand, Lava -> Obsidian!
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.MUD.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CLAY)) {
            level.setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.GRAVEL)) {
            level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.DIRT) || in.is(Items.COARSE_DIRT)) {
            itemEntity.setItem(new ItemStack(Items.MUD, in.getCount()));
            return true;
        }
        if (in.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, in.getCount()));
            return true;
        }
        if (in.is(Items.CLAY)) {
            itemEntity.setItem(new ItemStack(Items.GRAVEL, in.getCount()));
            return true;
        }
        if (in.is(Items.GRAVEL)) {
            itemEntity.setItem(new ItemStack(Items.SAND, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Waterlogged Hypoxia Hex: drowning damage on land, -40% attack speed, 2x Lightning damage (in interceptWardDamage)
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellLingeringZoneManager.applySoaked(target, 45);
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 35, 1, true, true));
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, 0.40D * mult, 35L);
        target.hurtServer(level, level.damageSources().drown(), (1.5F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getY(0.85D), target.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.03D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.KINETIC && target != caster && !SpellCombatReactionEngine.isBossEntity(level, target)) {
            // Hurricane Force Push: 14-block gale push, 2x knockback against wet or swimming targets!
            double mult = SpellLingeringZoneManager.isEntityWetOrSoaked(target) ? 2.0D : 1.0D;
            double push = 1.85D * mult;
            target.setDeltaMovement(direction.x * push, Math.max(0.38D, direction.y * 0.6D + 0.30D), direction.z * push);
            target.hasImpulse = true;
            level.sendParticles(ParticleTypes.GUST, target.getX(), target.getY(0.6D), target.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.04D);
        }
    }

    private static void extinguishFiresAround(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            if (level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }
    }
}
