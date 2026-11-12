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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Nature Materia (Base Type #3)</b>
 * (Spellcrafting System Guide Section 6.4).
 */
public final class NatureSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SYLVAN_EIDOLON_HEX_COLOR = 0x1A5923;

    @Override
    public EssenceType materiaType() {
        return EssenceType.NATURE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Entangling Briar Chains";
            default -> "Verdant Briar " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Thorned Briar Seed";
            case BEAM -> "Verdant Growth Ray";
            case RADIAL_BURST -> "Bramble Bloom Explosion";
            case TOUCH_STRIKE -> "Poison Ivy Slash";
            case AURA -> "Sylvan Mantle";
            case GLYPH -> "Overgrowth Snare";
            case CONE -> "Toxic Pollen Cone";
            case SKYFALL -> "Spore Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Tree-Stepping";
            case TRANSMUTE, BREAK -> "Botanical Synthesis";
            case AFFLICTION -> "Silencing Spore Hex";
            case HARVEST -> "Verdant Overgrowth Harvest";
            case RESTORATION -> "Photosynthetic Regrowth";
            default -> "Sylvan " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Entangling Briar Chains: thorny creepers physically tether consecutive victims within 5 blocks for 5 seconds (100t)!
        Vec3 midpoint = fromTarget.position().add(toTarget.position()).scale(0.5D);
        applyBriarRoot(fromTarget, 35);
        applyBriarRoot(toTarget, 35);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, midpoint.x, midpoint.y + 0.8D, midpoint.z, 10, 0.5D, 0.3D, 0.5D, 0.05D);

        final java.util.UUID idA = fromTarget.getUUID();
        final java.util.UUID idB = toTarget.getUUID();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, midpoint, 6.0D, 100, level.getGameTime(), spell, slot, EssenceType.NATURE, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    Entity eA = srv.getEntity(idA);
                    Entity eB = srv.getEntity(idB);
                    if (!(eA instanceof LivingEntity lA) || !(eB instanceof LivingEntity lB) || !lA.isAlive() || !lB.isAlive()) {
                        return false;
                    }
                    double dist = lA.distanceTo(lB);
                    if (elapsed % 5 == 0) {
                        Vec3 step = lB.getEyePosition().subtract(lA.getEyePosition());
                        for (double f = 0.2D; f <= 0.8D; f += 0.3D) {
                            Vec3 pt = lA.getEyePosition().add(step.scale(f));
                            srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pt.x, pt.y, pt.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
                        }
                    }
                    if (dist > 5.0D) {
                        Vec3 mid = lA.position().add(lB.position()).scale(0.5D);
                        Vec3 pA = mid.subtract(lA.position()).normalize().scale(0.55D);
                        Vec3 pB = mid.subtract(lB.position()).normalize().scale(0.55D);
                        lA.setDeltaMovement(pA.x, 0.12D, pA.z);
                        lB.setDeltaMovement(pB.x, 0.12D, pB.z);
                        lA.hasImpulse = true;
                        lB.hasImpulse = true;
                    }
                    return true;
                }
        ));
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Thorned Briar Seed: roots target to the spot for 2 seconds (40t)
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            applyBriarRoot(living, 40);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, living.getX(), living.getY(0.5D), living.getZ(), 12, 0.35D, 0.4D, 0.35D, 0.05D);
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Verdant Growth Ray: accelerates crop/sapling growth, knits ally wounds, tangles enemies in ivy creepers
        for (LivingEntity hit : hitEntities) {
            if (hit.isAlliedTo(caster)) {
                hit.heal(3.0F + spell.getEffectiveTier());
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            }
        }
        int steps = Math.min(18, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos p = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos bp : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(bp);
                if (bs.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, bp, bs)) {
                    growable.performBonemeal(level, level.random, bp.immutable(), bs);
                }
            }
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Poison Ivy Slash: Poison II for 8s (160t) and shreds physical Armor Toughness!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 1.0D, 3.0D, 160L);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Bramble Bloom Explosion: 6-block ring knocking foes back, rooting for 1.5s (30t), leaving 6s damaging briar patches!
        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = entity.position().subtract(center).normalize().scale(0.95D);
            entity.setDeltaMovement(away.x, 0.3D, away.z);
            entity.hasImpulse = true;
            applyBriarRoot(entity, 30);
        }
        List<BlockPos> placedBriars = new java.util.ArrayList<>();
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-3, -1, -3), cPos.offset(3, 0, 3))) {
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.40F) {
                level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
                placedBriars.add(p.immutable());
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, Math.max(3.5D, radius * 0.75D), 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (BlockPos bp : placedBriars) {
                            if (srv.getBlockState(bp).is(Blocks.MOSS_CARPET)) {
                                srv.removeBlock(bp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB patchBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 1.5D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, patchBox, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1));
                            foe.hurtServer(srv, srv.damageSources().sweetBerryBush(), 1.5F);
                        }
                    }
                    return true;
                }
        ));
        level.playSound(null, cPos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Sylvan Mantle
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY(0.8D), caster.getZ(), 18, 0.5D, 0.5D, 0.5D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Overgrowth Snare: 4-block root cage for 4 seconds (80t) with continuous strangling crush damage
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

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (csrv, czone, celapsed) -> {
                                LivingEntity cOwner = czone.resolveCaster(csrv);
                                for (LivingEntity victim : csrv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != cOwner)) {
                                    applyBriarRoot(victim, 25);
                                    if (celapsed % 20 == 0) {
                                        victim.hurtServer(csrv, csrv.damageSources().sweetBerryBush(), 2.0F + czone.spell.getEffectiveTier() * 0.6F);
                                        SpellExecutionEngine.deliverSubSpellOnEntity(csrv, cOwner, victim, Vec3.ZERO, czone.spell, czone.slot, czone.fallbackMateria, czone.apparatusPotency * 0.35F);
                                    }
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 1.0F, 0.8F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Toxic Pollen Cone: Nausea + Poison II
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D, 35, 0.9D, 0.5D, 0.9D, 0.08D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Spore Tempest: 6s (120t) toxic fungal spores carpeting temporary poison moss & poisoning hostiles
        double r = Math.max(5.0D, radius);
        List<BlockPos> sporeMoss = new java.util.ArrayList<>();
        BlockPos cPos = BlockPos.containing(impactCenter);
        int mr = Math.min(5, (int) Math.ceil(r));
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-mr, -1, -mr), cPos.offset(mr, 0, mr))) {
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.30F) {
                level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
                sporeMoss.add(p.immutable());
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (BlockPos mp : sporeMoss) {
                            if (srv.getBlockState(mp).is(Blocks.MOSS_CARPET)) {
                                srv.removeBlock(mp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 4.0D, zone.center.z,
                                20, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.0D, zone.center.z + zone.radius);
                        for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, victim, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.3F);
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
        tag.putString("SpellEidolonMateria", EssenceType.NATURE.name());
        tag.putInt("SpellEidolonTintRGB", SYLVAN_EIDOLON_HEX_COLOR);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, summonEntity.getBoundingBox().inflate(Math.max(3.5D, radius)))) {
            if (nearby != caster && nearby != summonEntity && nearby.isAlive()) {
                applyBriarRoot(nearby, 35);
            }
        }
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Tree-Stepping: leaves a blooming flower bush at the departure point
        BlockPos dep = BlockPos.containing(departurePos);
        if (level.getBlockState(dep).isAir() && level.getBlockState(dep.below()).isSolidRender()) {
            level.setBlockAndUpdate(dep, Blocks.FLOWERING_AZALEA.defaultBlockState());
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.4D, 0.5D, 0.4D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Botanical Synthesis: stone -> mossy cobblestone, dirt -> podzol, dead bush -> sapling, cobblestone -> oak log
        if (state.is(Blocks.STONE)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STONE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.PODZOL.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DEAD_BUSH)) {
            level.setBlockAndUpdate(pos, Blocks.OAK_SAPLING.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.OAK_LOG.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            net.minecraft.world.entity.item.ItemEntity itemEntity,
            int potencyTier
    ) {
        var stack = itemEntity.getItem();
        if (stack.is(net.minecraft.world.item.Items.DEAD_BUSH)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SAPLING, stack.getCount()));
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.COBBLESTONE)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MOSSY_COBBLESTONE, stack.getCount()));
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.STONE_BRICKS)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MOSSY_STONE_BRICKS, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Silencing Spore Hex: silences spellcasting/abilities, shreds physical Armor Toughness, deals spore DoT
        float stackMult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong("SpellSilencedUntil", level.getGameTime() + 30L);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 1.0D * stackMult, 3.0D * stackMult, 35L);
        if (target instanceof Mob mob) {
            mob.setAggressive(false);
        }
        target.hurtServer(level, level.damageSources().magic(), (1.0F + potencyTier * 0.45F) * stackMult);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    private static void applyBriarRoot(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6, false, true));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0, false, false));
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hasImpulse = true;
    }
}
