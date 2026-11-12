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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Spore Materia (Tier 1 Fusion #18)</b>
 * (Spellcrafting System Guide Section 6.19).
 */
public final class SporeSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SYMBIOTIC_FUNGAL_HEX_COLOR = 0x5E7A3A;

    @Override
    public EssenceType materiaType() {
        return EssenceType.SPORE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case ORBIT_SATELLITE -> "Drifting Spore-Pollen Orbit Flow";
            default -> "Mycelial Spore " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Aerodynamic Fungal Puffball";
            case BEAM -> "Mycelial Inoculation Stream";
            case RADIAL_BURST -> "Fungal Bloom Nova";
            case TOUCH_STRIKE -> "Mycelial Infestation Strike";
            case AURA -> "Mycelial Spore-Cloud Mantle";
            case GLYPH -> "Silencing Mycelium Web";
            case CONE -> "Billowing Windborne Spore Blast";
            case SKYFALL -> "Choking Fungal Fog Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Hyphal Network Blink";
            case TRANSMUTE, BREAK -> "Nether Spore Transmutation";
            case AFFLICTION -> "Hallucinatory Mycelial Hex";
            case RESTORATION -> "Spore Carapace Restoration";
            default -> "Mycelial " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        // Drifting Spore-Pollen Orbit Flow: poisons adjacent hostiles along its drifting orbital path!
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.15D, 0.15D, 0.15D, 0.02D);
        Entity owner = projectile.getOwner();
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(1.5D))) {
            if (foe == owner || !foe.isAlive() || (owner != null && foe.isAlliedTo(owner))) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Aerodynamic Fungal Puffball: zero-gravity buoyant drift
        projectile.setNoGravity(true);
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.75D));
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.MYCELIUM, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.12D, 0.12D, 0.12D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        burstPuffballCloud(level, caster, projectile.position(), spell.hasWardFriendlyFireSafeguard());
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        burstPuffballCloud(level, caster, projectile.position(), spell.hasWardFriendlyFireSafeguard());
    }

    private static void burstPuffballCloud(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, boolean wardSafeguard) {
        // 3m fungal spore cloud inflicting Poison II + Nausea for 3s (60t) and sprouting small edible mushrooms!
        AABB box = new AABB(center.x - 3.0D, center.y - 1.5D, center.z - 3.0D, center.x + 3.0D, center.y + 2.0D, center.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafeguard)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
        }
        sproutMushrooms(level, BlockPos.containing(center), 2, false);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 0.5D, center.z, 28, 1.2D, 0.6D, 1.2D, 0.05D);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Mycelial Inoculation Stream: Poison + Nausea, converts dirt/grass -> Mycelium, accelerates small mushrooms into Huge Mushrooms!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
        }
        int steps = Math.min(16, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-1, -1, -1), bp.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.DIRT) || bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.COARSE_DIRT)) {
                    level.setBlockAndUpdate(p, Blocks.MYCELIUM.defaultBlockState());
                } else if (bs.getBlock() instanceof MushroomBlock mushroom) {
                    BlockPos soilPos = p.below();
                    BlockState soil = level.getBlockState(soilPos);
                    if (soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.COARSE_DIRT)) {
                        level.setBlockAndUpdate(soilPos, Blocks.MYCELIUM.defaultBlockState());
                    }
                    BlockPos imm = p.immutable();
                    for (int attempt = 0; attempt < 3; attempt++) {
                        BlockState cur = level.getBlockState(imm);
                        if (cur.getBlock() instanceof MushroomBlock curShroom && curShroom.isValidBonemealTarget(level, imm, cur)) {
                            curShroom.performBonemeal(level, level.random, imm, cur);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Fungal Bloom Nova: wind knockback, Poison II + Nausea for 4s (80t), carpets ground in mushrooms & mycelium!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.05D);
            foe.setDeltaMovement(away.x, 0.32D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
        }
        sproutMushrooms(level, BlockPos.containing(center), 4, true);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 0.8D, center.z, 40, 2.2D, 0.7D, 2.2D, 0.06D);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Mycelial Infestation Strike: Poison II + Nausea 5s (100t), -40% speed, bursts into infectious spore pop on death!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_SPORE_INFESTED_UNTIL, now + 120L);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Mycelial Spore-Cloud Mantle
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SPORE_MANTLE_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Silencing Mycelium Web: immobilizes & silences abilities/spellcasting for 4s (80t)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.5D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 3.5D, zone.center.y - 1.0D, zone.center.z - 3.5D,
                            zone.center.x + 3.5D, zone.center.y + 2.5D, zone.center.z + 3.5D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                        v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                        SpellLingeringZoneManager.applyArcaneSilence(v, 80);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.5D, zone.center.z, 30, 1.5D, 0.4D, 1.5D, 0.05D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Billowing Windborne Spore Blast: Poison II + Nausea 5s (100t), carpets mushrooms, accelerates crop growth!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            sproutMushrooms(level, stepPos, 1, false);
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-2, -1, -2), stepPos.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BonemealableBlock grow && grow.isValidBonemealTarget(level, p, bs) && level.random.nextFloat() < 0.35F) {
                    grow.performBonemeal(level, level.random, p.immutable(), bs);
                }
            }
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Choking Fungal Fog Downpour: 15s (300t) fog blinding & slowing non-arthropods by 50%, decaying organic drops into bone meal compost!
        double r = Math.max(7.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 3.0D, zone.center.z,
                                26, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.5D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (!v.getType().is(EntityTypeTags.ARTHROPOD)) {
                                v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                                v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
                            }
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.30F);
                        }
                        // Decay organic dropped items into bone meal compost!
                        if (elapsed % 40 == 0) {
                            for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, area)) {
                                ItemStack st = item.getItem();
                                if (isCompostableOrganicItem(st)) {
                                    item.setItem(new ItemStack(Items.BONE_MEAL, st.getCount()));
                                }
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
        tag.putString("SpellEidolonMateria", EssenceType.SPORE.name());
        tag.putInt("SpellEidolonTintRGB", SYMBIOTIC_FUNGAL_HEX_COLOR);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Hyphal Network Blink: silent underground fungal network transit sprouting mushrooms!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 80L);
        sproutMushrooms(level, BlockPos.containing(departurePos), 1, false);
        sproutMushrooms(level, BlockPos.containing(arrivalPos), 1, false);
        level.sendParticles(ParticleTypes.MYCELIUM, arrivalPos.x, arrivalPos.y + 0.5D, arrivalPos.z, 18, 0.4D, 0.3D, 0.4D, 0.03D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Nether Spore Transmutation: Overworld flora & mushrooms <-> Crimson & Warped fungal vegetation!
        if (state.is(Blocks.RED_MUSHROOM)) {
            level.setBlockAndUpdate(pos, Blocks.CRIMSON_FUNGUS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CRIMSON_FUNGUS)) {
            level.setBlockAndUpdate(pos, Blocks.RED_MUSHROOM.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.BROWN_MUSHROOM)) {
            level.setBlockAndUpdate(pos, Blocks.WARPED_FUNGUS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WARPED_FUNGUS)) {
            level.setBlockAndUpdate(pos, Blocks.BROWN_MUSHROOM.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.RED_MUSHROOM_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.NETHER_WART_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.NETHER_WART_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.BROWN_MUSHROOM_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.WARPED_WART_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WARPED_WART_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.MUSHROOM_STEM)) {
            level.setBlockAndUpdate(pos, (level.random.nextBoolean() ? Blocks.CRIMSON_STEM : Blocks.WARPED_STEM).defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CRIMSON_STEM) || state.is(Blocks.WARPED_STEM)) {
            level.setBlockAndUpdate(pos, Blocks.MUSHROOM_STEM.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.MYCELIUM) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)) {
            level.setBlockAndUpdate(pos, (level.random.nextBoolean() ? Blocks.CRIMSON_NYLIUM : Blocks.WARPED_NYLIUM).defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM)) {
            level.setBlockAndUpdate(pos, Blocks.MYCELIUM.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(BlockTags.SMALL_FLOWERS)) {
            Block netherFlora = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.CRIMSON_ROOTS;
                case 1 -> Blocks.WARPED_ROOTS;
                default -> Blocks.NETHER_SPROUTS;
            };
            level.setBlockAndUpdate(pos, netherFlora.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CRIMSON_ROOTS) || state.is(Blocks.WARPED_ROOTS) || state.is(Blocks.NETHER_SPROUTS)) {
            level.setBlockAndUpdate(pos, (level.random.nextBoolean() ? Blocks.SHORT_GRASS : Blocks.FERN).defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.RED_MUSHROOM)) {
            itemEntity.setItem(new ItemStack(Items.CRIMSON_FUNGUS, in.getCount()));
            return true;
        }
        if (in.is(Items.CRIMSON_FUNGUS)) {
            itemEntity.setItem(new ItemStack(Items.RED_MUSHROOM, in.getCount()));
            return true;
        }
        if (in.is(Items.BROWN_MUSHROOM)) {
            itemEntity.setItem(new ItemStack(Items.WARPED_FUNGUS, in.getCount()));
            return true;
        }
        if (in.is(Items.WARPED_FUNGUS)) {
            itemEntity.setItem(new ItemStack(Items.BROWN_MUSHROOM, in.getCount()));
            return true;
        }
        if (in.is(Items.RED_MUSHROOM_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.NETHER_WART_BLOCK, in.getCount()));
            return true;
        }
        if (in.is(Items.NETHER_WART_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.RED_MUSHROOM_BLOCK, in.getCount()));
            return true;
        }
        if (in.is(Items.BROWN_MUSHROOM_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.WARPED_WART_BLOCK, in.getCount()));
            return true;
        }
        if (in.is(Items.WARPED_WART_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.BROWN_MUSHROOM_BLOCK, in.getCount()));
            return true;
        }
        if (in.is(Items.MUSHROOM_STEM)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.CRIMSON_STEM : Items.WARPED_STEM, in.getCount()));
            return true;
        }
        if (in.is(Items.CRIMSON_STEM) || in.is(Items.WARPED_STEM)) {
            itemEntity.setItem(new ItemStack(Items.MUSHROOM_STEM, in.getCount()));
            return true;
        }
        if (in.is(Items.MYCELIUM) || in.is(Items.PODZOL)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.CRIMSON_NYLIUM : Items.WARPED_NYLIUM, in.getCount()));
            return true;
        }
        if (in.is(Items.CRIMSON_NYLIUM) || in.is(Items.WARPED_NYLIUM)) {
            itemEntity.setItem(new ItemStack(Items.MYCELIUM, in.getCount()));
            return true;
        }
        if (in.is(Items.SHORT_GRASS) || in.is(Items.FERN) || in.is(ItemTags.SMALL_FLOWERS)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.CRIMSON_ROOTS : Items.WARPED_ROOTS, in.getCount()));
            return true;
        }
        if (in.is(Items.CRIMSON_ROOTS) || in.is(Items.WARPED_ROOTS) || in.is(Items.NETHER_SPROUTS)) {
            itemEntity.setItem(new ItemStack(Items.SHORT_GRASS, in.getCount()));
            return true;
        }
        if (in.is(Items.VINE)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.WEEPING_VINES : Items.TWISTING_VINES, in.getCount()));
            return true;
        }
        if (in.is(Items.WEEPING_VINES) || in.is(Items.TWISTING_VINES)) {
            itemEntity.setItem(new ItemStack(Items.VINE, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Hallucinatory Mycelial Hex: confuses allegiance to attack nearest entity (ally or foe), permanent Nausea, poison coughs!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, true, true));

        if (target instanceof Mob mob) {
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(10.0D),
                    e -> e != target && e.isAlive());
            if (!nearby.isEmpty()) {
                nearby.sort(Comparator.comparingDouble(target::distanceToSqr));
                mob.setTarget(nearby.get(0));
            }
        }

        // Periodically cough toxic poison spores onto adjacent entities
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D))) {
            if (adj != target && adj.isAlive()) {
                adj.addEffect(new MobEffectInstance(MobEffects.POISON, 45, 0));
            }
        }
        target.hurtServer(level, level.damageSources().magic(), (1.2F + potencyTier * 0.40F) * mult);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.85D), target.getZ(), 8, 0.35D, 0.35D, 0.35D, 0.03D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.RESTORATION) {
            // Spore Carapace Restoration: hardened chitinous fungal shell granting Absorption hearts & Resistance!
            float abs = 6.0F + potencyTier * 2.0F;
            target.setAbsorptionAmount(Math.max(target.getAbsorptionAmount(), abs));
            target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 0));
            level.playSound(null, target.blockPosition(), SoundEvents.ARMOR_EQUIP_TURTLE.value(), SoundSource.PLAYERS, 0.85F, 1.1F);
        }
    }

    private static boolean isCompostableOrganicItem(ItemStack stack) {
        if (stack.isEmpty() || stack.is(Items.BONE_MEAL)) return false;
        return stack.is(ItemTags.LEAVES) || stack.is(ItemTags.SAPLINGS) || stack.is(ItemTags.SMALL_FLOWERS)
                || stack.is(Items.ROTTEN_FLESH) || stack.has(DataComponents.FOOD);
    }

    private static void sproutMushrooms(ServerLevel level, BlockPos center, int r, boolean convertTurf) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockState bs = level.getBlockState(p);
            if (convertTurf && (bs.is(Blocks.DIRT) || bs.is(Blocks.GRASS_BLOCK)) && level.random.nextFloat() < 0.35F) {
                level.setBlockAndUpdate(p, Blocks.MYCELIUM.defaultBlockState());
            } else if (bs.isAir() && level.getBlockState(p.below()).isSolidRender() && level.random.nextFloat() < 0.28F) {
                Block shroom = level.random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
                if (shroom.defaultBlockState().canSurvive(level, p)) {
                    level.setBlockAndUpdate(p, shroom.defaultBlockState());
                }
            }
        }
    }
}
