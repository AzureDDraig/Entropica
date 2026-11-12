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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Overgrowth Materia (Tier 1 Fusion #16)</b>
 * (Spellcrafting System Guide Section 6.17).
 */
public final class OvergrowthSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SYLVAN_GOLIATH_HEX_COLOR = 0x1A5923;

    @Override
    public EssenceType materiaType() {
        return EssenceType.OVERGROWTH;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Entangling Briar Chains";
            default -> "Verdant Overgrowth " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Verdant Briar-Spike Seed";
            case BEAM -> "Verdant Growth Tether Ray";
            case RADIAL_BURST -> "Bramble Rampart Bloom";
            case TOUCH_STRIKE -> "Botanical Ground Slam";
            case AURA -> "Earth-Root Bastion";
            case GLYPH -> "Strangler Briar Cage Mine";
            case CONE -> "Bramble Flurry & Phyto-Wave";
            case SKYFALL -> "Phyto-Canopy Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Tree-Stepping";
            case TRANSMUTE, BREAK -> "Botanical Synthesis";
            case AFFLICTION -> "Strangling Phyto-Bramble Hex";
            case RESTORATION -> "Photosynthesis Mending";
            default -> "Overgrowth " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Entangling Briar Chains: tethers consecutive victims within 5m, dragging them together and rooting for 1.5s (30t)!
        applyRoot(fromTarget, 30);
        applyRoot(toTarget, 30);
        Vec3 midpoint = fromTarget.position().add(toTarget.position()).scale(0.5D);
        final UUID idA = fromTarget.getUUID();
        final UUID idB = toTarget.getUUID();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, midpoint, 6.0D, 100, level.getGameTime(), spell, slot, EssenceType.OVERGROWTH, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    Entity eA = srv.getEntity(idA);
                    Entity eB = srv.getEntity(idB);
                    if (!(eA instanceof LivingEntity lA) || !(eB instanceof LivingEntity lB) || !lA.isAlive() || !lB.isAlive()) {
                        return false;
                    }
                    if (lA.distanceTo(lB) > 5.0D) {
                        Vec3 mid = lA.position().add(lB.position()).scale(0.5D);
                        Vec3 pA = mid.subtract(lA.position()).normalize().scale(0.60D);
                        Vec3 pB = mid.subtract(lB.position()).normalize().scale(0.60D);
                        lA.setDeltaMovement(pA.x, 0.14D, pA.z);
                        lB.setDeltaMovement(pB.x, 0.14D, pB.z);
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
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Verdant Briar-Spike Seed: roots target for 2.5s (50t) and spreads moss & grass onto nearby floor blocks!
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            applyRoot(living, 50);
        }
        spreadMossAndWildflowers(level, BlockPos.containing(projectile.position()), 2);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        spreadMossAndWildflowers(level, blockHit.getBlockPos(), 2);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Verdant Growth Tether Ray: drags struck mobs in clinging roots (-60% speed) + rapidly accelerates crops/saplings/flora!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            Vec3 drag = caster.position().subtract(hit.position()).normalize().scale(0.35D);
            hit.setDeltaMovement(hit.getDeltaMovement().add(drag.x, 0.0D, drag.z));
            hit.hasImpulse = true;
        }
        int steps = Math.min(18, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-1, -1, -1), bp.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, p, bs)) {
                    growable.performBonemeal(level, level.random, p.immutable(), bs);
                }
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Bramble Rampart Bloom: 6m knockback, roots grounded hostiles for 2.5s (50t), raises temporary stone spires, carpets floor in moss & wildflowers!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(0.95D);
            foe.setDeltaMovement(away.x, 0.30D, away.z);
            foe.hasImpulse = true;
            if (foe.onGround()) {
                applyRoot(foe, 50);
            }
        }
        spreadMossAndWildflowers(level, BlockPos.containing(center), 4);

        // Raise temporary 2-block mossy stone spires around the rampart perimeter for 5s (100t)!
        List<BlockPos> raisedSpires = new ArrayList<>();
        BlockPos cBase = BlockPos.containing(center);
        for (int i = 0; i < 6; i++) {
            double ang = (Math.PI * 2.0D * i) / 6.0D;
            BlockPos spireBase = cBase.offset((int) Math.round(Math.cos(ang) * 4.0D), 0, (int) Math.round(Math.sin(ang) * 4.0D)).immutable();
            if (level.getBlockState(spireBase.below()).isSolidRender() && level.getBlockState(spireBase).isAir()) {
                level.setBlockAndUpdate(spireBase, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                raisedSpires.add(spireBase);
                if (level.getBlockState(spireBase.above()).isAir()) {
                    level.setBlockAndUpdate(spireBase.above(), Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState());
                    raisedSpires.add(spireBase.above().immutable());
                }
            }
        }
        if (!raisedSpires.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, r, 100, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (BlockPos sp : raisedSpires) {
                            BlockState cur = srv.getBlockState(sp);
                            if (cur.is(Blocks.MOSSY_COBBLESTONE) || cur.is(Blocks.MOSSY_COBBLESTONE_WALL)) {
                                srv.removeBlock(sp, false);
                            }
                        }
                        return false;
                    }
            ));
        }

        level.playSound(null, BlockPos.containing(center), SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Botanical Ground Slam: knocks victim flat onto floor + erupts dense floral roots beneath them!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
            applyRoot(hit, 40);
            hit.setDeltaMovement(0.0D, -0.75D, 0.0D);
            hit.hasImpulse = true;
            spreadMossAndWildflowers(level, hit.blockPosition(), 1);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Earth-Root Bastion: standing still for 2s roots caster into earth (+8 Armor & 1 HP/s regen)
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY(0.8D), caster.getZ(), 20, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Strangler Briar Cage Mine: 4m wooden root cage for 4s (80t) immobilizing mobs with strangling damage!
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

                    // Erect temporary physical wooden root & mossy stone cage pillars around the 4m snare perimeter!
                    List<BlockPos> cageBars = new ArrayList<>();
                    BlockPos cageOrigin = BlockPos.containing(zone.center);
                    for (int i = 0; i < 8; i++) {
                        double ang = (Math.PI * 2.0D * i) / 8.0D;
                        BlockPos barBase = cageOrigin.offset((int) Math.round(Math.cos(ang) * 3.0D), 0, (int) Math.round(Math.sin(ang) * 3.0D)).immutable();
                        if (srv.getBlockState(barBase.below()).isSolidRender() && srv.getBlockState(barBase).isAir()) {
                            Block barType = (i % 2 == 0) ? Blocks.MANGROVE_ROOTS : Blocks.MOSSY_COBBLESTONE_WALL;
                            srv.setBlockAndUpdate(barBase, barType.defaultBlockState());
                            cageBars.add(barBase);
                            if (srv.getBlockState(barBase.above()).isAir()) {
                                srv.setBlockAndUpdate(barBase.above(), Blocks.OAK_FENCE.defaultBlockState());
                                cageBars.add(barBase.above().immutable());
                            }
                        }
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (csrv, czone, celapsed) -> {
                                if (celapsed >= czone.maxDurationTicks) {
                                    for (BlockPos bp : cageBars) {
                                        BlockState cur = csrv.getBlockState(bp);
                                        if (cur.is(Blocks.MANGROVE_ROOTS) || cur.is(Blocks.MOSSY_COBBLESTONE_WALL) || cur.is(Blocks.OAK_FENCE)) {
                                            csrv.removeBlock(bp, false);
                                        }
                                    }
                                    return false;
                                }
                                LivingEntity cOwner = czone.resolveCaster(csrv);
                                for (LivingEntity v : csrv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != cOwner)) {
                                    if (cOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, cOwner, czone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    applyRoot(v, 25);
                                    if (celapsed % 20 == 0) {
                                        v.hurtServer(csrv, csrv.damageSources().sweetBerryBush(), 2.2F + czone.spell.getEffectiveTier() * 0.6F);
                                        SpellExecutionEngine.deliverSubSpellOnEntity(csrv, cOwner, v, Vec3.ZERO, czone.spell, czone.slot, czone.fallbackMateria, czone.apparatusPotency * 0.35F);
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
        // Bramble Flurry & Phyto-Wave: knocks back, roots 1.5s (30t), carpets moss, harvests & replants mature crops!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 0.9D, 0.25D, direction.z * 0.9D);
            hit.hasImpulse = true;
            applyRoot(hit, 30);
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-2, -1, -2), stepPos.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof CropBlock crop && crop.isMaxAge(bs)) {
                    Block.dropResources(bs, level, p, null, caster, ItemStack.EMPTY);
                    level.setBlock(p, crop.getStateForAge(0), Block.UPDATE_ALL);
                } else if (bs.isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.20F) {
                    level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
                }
            }
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Phyto-Canopy Downpour: 16m verdant cloud for 8s (160t) supercharging crops, maturing saplings into trees, healing friendly constructs!
        double r = Math.max(8.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 160, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.HAPPY_VILLAGER, zone.center.x, zone.center.y + 4.5D, zone.center.z, 18, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(8, (int) Math.ceil(zone.radius));
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -1, -br), cPos.offset(br, 1, br))) {
                            BlockState bs = srv.getBlockState(p);
                            if (bs.getBlock() instanceof SaplingBlock sapling) {
                                sapling.advanceTree(srv, p.immutable(), bs, srv.random);
                                BlockState after = srv.getBlockState(p);
                                if (after.getBlock() instanceof SaplingBlock sapling2) {
                                    sapling2.advanceTree(srv, p.immutable(), after, srv.random);
                                }
                            } else if (bs.getBlock() instanceof BonemealableBlock grow && grow.isValidBonemealTarget(srv, p, bs) && srv.random.nextFloat() < 0.45F) {
                                grow.performBonemeal(srv, srv.random, p.immutable(), bs);
                            }
                        }

                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (e instanceof SpellEidolonEntity eidolon && (owner == null || eidolon.isFriendlyTo(owner))) {
                                eidolon.heal(4.0F + zone.spell.getEffectiveTier());
                            } else if (owner != null && (e == owner || e.isAlliedTo(owner))) {
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 2.0F);
                                e.heal(2.0F);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, e, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.OVERGROWTH.name());
        tag.putInt("SpellEidolonTintRGB", SYLVAN_GOLIATH_HEX_COLOR);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Tree-Stepping: leaves a 3s (60t) entangling flower bush at departure that snares pursuers!
        BlockPos dep = BlockPos.containing(departurePos).immutable();
        boolean placedBush = false;
        if (level.getBlockState(dep).isAir() && level.getBlockState(dep.below()).isSolidRender()) {
            level.setBlockAndUpdate(dep, Blocks.FLOWERING_AZALEA.defaultBlockState());
            placedBush = true;
        }
        final boolean fPlaced = placedBush;
        AABB snare = new AABB(departurePos.x - 2.5D, departurePos.y - 1.0D, departurePos.z - 2.5D,
                departurePos.x + 2.5D, departurePos.y + 2.0D, departurePos.z + 2.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, snare)) {
            if (foe != caster && !foe.isAlliedTo(caster)) {
                applyRoot(foe, 45);
            }
        }
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 2.5D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.OVERGROWTH, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (fPlaced && srv.getBlockState(dep).is(Blocks.FLOWERING_AZALEA)) {
                            srv.removeBlock(dep, false);
                        }
                        return false;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, snare)) {
                        if (foe != owner && foe.isAlive() && (owner == null || !foe.isAlliedTo(owner))) {
                            applyRoot(foe, 25);
                        }
                    }
                    return true;
                }
        ));
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.4D, 0.5D, 0.4D, 0.04D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Botanical Synthesis: stone -> mossy stone, dirt -> podzol/moss, matures crops!
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STONE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) {
            level.setBlockAndUpdate(pos, (level.random.nextBoolean() ? Blocks.MOSS_BLOCK : Blocks.PODZOL).defaultBlockState());
            return true;
        }
        if (state.getBlock() instanceof BonemealableBlock grow && grow.isValidBonemealTarget(level, pos, state)) {
            grow.performBonemeal(level, level.random, pos, state);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.COBBLESTONE) || in.is(Items.STONE)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_COBBLESTONE, in.getCount()));
            return true;
        }
        if (in.is(Items.DIRT)) {
            itemEntity.setItem(new ItemStack(Items.MOSS_BLOCK, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Strangling Phyto-Bramble Hex: silences abilities, shreds 35% toughness, sprouts grasping brambles when moving!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellLingeringZoneManager.applyArcaneSilence(target, 35);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.35D * mult, 35L);

        if (target.getDeltaMovement().horizontalDistanceSqr() > 0.002D) {
            BlockPos foot = target.blockPosition();
            if (level.getBlockState(foot).isAir() && level.getBlockState(foot.below()).isSolidRender()) {
                level.setBlockAndUpdate(foot, Blocks.MOSS_CARPET.defaultBlockState());
            }
            target.hurtServer(level, level.damageSources().sweetBerryBush(), (1.8F + potencyTier * 0.5F) * mult);
        } else {
            target.hurtServer(level, level.damageSources().magic(), (1.0F + potencyTier * 0.4F) * mult);
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.7D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.RESTORATION) {
            // Photosynthesis Mending: regenerative sap healing that DOUBLES under direct sunlight!
            boolean inSun = level.canSeeSky(target.blockPosition()) && level.isBrightOutside();
            if (inSun) {
                float bonusHeal = 4.0F + potencyTier * 1.2F;
                SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
                target.heal(bonusHeal);
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 2));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0));
            }
        }
    }

    private static void applyRoot(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6, false, true));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0, false, false));
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hasImpulse = true;
    }

    private static void spreadMossAndWildflowers(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockState below = level.getBlockState(p.below());
            if ((below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.STONE)) && level.random.nextFloat() < 0.15F) {
                level.setBlockAndUpdate(p.below(), Blocks.MOSS_BLOCK.defaultBlockState());
                below = Blocks.MOSS_BLOCK.defaultBlockState();
            }
            if (level.getBlockState(p).isAir() && below.isSolidRender() && level.random.nextFloat() < 0.35F) {
                int pick = level.random.nextInt(5);
                if (pick == 4 && level.getBlockState(p.above()).isAir() && Blocks.TALL_GRASS.defaultBlockState().canSurvive(level, p)) {
                    DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), p, Block.UPDATE_ALL);
                    continue;
                }
                Block flora = switch (pick) {
                    case 0 -> Blocks.SHORT_GRASS;
                    case 1 -> Blocks.DANDELION;
                    case 2 -> Blocks.POPPY;
                    default -> Blocks.MOSS_CARPET;
                };
                if (flora.defaultBlockState().canSurvive(level, p)) {
                    level.setBlockAndUpdate(p, flora.defaultBlockState());
                }
            }
        }
    }
}
