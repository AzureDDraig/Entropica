package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Glacial Materia (Tier 1 Elemental Fusion #15: Water + Frozen)</b>
 * (Spellcrafting System Guide Sections 1.2, 2.2, 4.1–4.9, 5.1–5.7 &amp; 6.16).
 *
 * <p>Canonical Advantage: <b>Strong against NETHER (+1), ARID (+1), and NATURE (+1)</b> |
 * <b>Weak against NATURE (-1), NETHER (-1), and UNDEAD (-1)</b>
 * (Dominance: {@code glacial_water_dom} -&gt; Strong against Arid, Weak against Nature &amp; Undead; Nether neutralized,
 * computed automatically by {@code VisInteractionManager};
 * Tier 1 Elemental Fusion: {@code 10%} base physical armor bypass &amp; {@code 1} base armor shred point).</p>
 */
public final class GlacialSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GLACIAL_BRUISER_HEX_COLOR = 0x98DCF2;
    public static final int PERMAFROST_BLUE_HEX_COLOR = 0x2C82C9;
    public static final int DIAMOND_CRYO_HEX_COLOR = 0xE0FFFF;

    private static final String TAG_GLACIAL_BEAM_LAST_TICK = "SpellGlacialTorrentLastTick";
    private static final String TAG_GLACIAL_BEAM_STACKS = "SpellGlacialTorrentStacks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.GLACIAL;
    }

    // =========================================================================
    // CANONICAL TITLES & DESCRIPTIONS (GUIDE SECTION 6.16)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Permafrost Creep";
            default -> "Permafrost " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY ->
                    "Permafrost Creep: The spell travels slowly as a creeping wave of sub-zero cryo-mass, adding +0.4 Materia Advantage score and permanently freezing water beneath its wake into solid blue ice.";
            default ->
                    "Propels the spell along pale cryo-blue (#98DCF2) glacial currents with a chilling wake that slows nearby enemy projectiles.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Glacial Ice-Lance";
            case BEAM -> "Absolute-Zero Cryo-Torrent Beam";
            case RADIAL_BURST -> "Hoarfrost Blizzard Shockwave";
            case TOUCH_STRIKE -> "Brittle Frost Strike";
            case AURA -> "Permafrost Cryo-Shroud";
            case GLYPH -> "Powder Snow Pitfall Trap";
            case CONE -> "Blizzard Shrapnel Fan";
            case SKYFALL -> "Glacial Monolith Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Fires a heavy, aerodynamic spear of blue ice (#98DCF2); pierces through up to 2 mobs, inflicts 60% Slowness for 3 seconds, and flash-freezes hit water blocks into packed ice.";
            case BEAM ->
                    "Channels a sustained stream of sub-zero liquid frost and ice slurry (#98DCF2); stacks progressive Slowness up to a 2-second solid freeze-stasis if sustained for 3 seconds, while freezing water surfaces into packed ice bridges and cooling lava into obsidian.";
            case RADIAL_BURST ->
                    "Releases an expanding cryogenic fog burst in a 6-block radius that inflicts Blindness and 80% Slowness on nearby hostiles for 4 seconds while snuffing out all open fires.";
            case TOUCH_STRIKE ->
                    "Strikes the victim with extreme sub-zero impact, reducing their armor toughness by 30% for 5 seconds and inflicting Slowness IV.";
            case AURA ->
                    "Surrounds caster in a freezing mist (#98DCF2); grants Frost Walker ice-stepping, total immunity to frostbite and powder snow, and inflicts 40% Slowness and a 0.5s freeze stutter on any mob that strikes the caster in melee.";
            case GLYPH ->
                    "Turns a 3x3 floor area into powdered snow that enemies sink helplessly into when stepped on, plunging them downward and causing rapid freezing and frostbite (disarmable via Sneak-Right-Click).";
            case CONE ->
                    "Blasts a fan of razor-sharp icicles in a 60-degree cone that shreds enemy armor toughness by 30% and knocks mobs flat for 1.5 seconds.";
            case SKYFALL ->
                    "Calls down a colossal blue ice spire after a 2-second telegraph; crushes the impact epicenter, detonates a frost shockwave encasing grounded mobs in ice for 2 seconds, and leaves permanent solid ice and blue ice blocks as a lasting geological monument.";
            case SUMMON_AGENT ->
                    "Summons a pale cryo-blue (#98DCF2) Glacial-Bruiser Agent with frost vapor particles whose chilling melee strikes freeze enemies solid after 3 consecutive hits.";
            case SUMMON_SENTRY ->
                    "Anchors a Cryo-Pylon Sentry (#98DCF2) radiating chilling frost vapor and firing rapid zero-drop icicle darts with heavy 60% Slowness.";
            case SUMMON_DECOY ->
                    "Conjures a Crystalline Freeze-Nova Effigy (#98DCF2) that taunts mobs and shatters into a 4-block freeze-nova upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Ice Statue Blink";
            case TRANSMUTE, BREAK -> "Cryo-Solidification & Silk Touch";
            case AFFLICTION -> "Absolute-Zero Cryo-Hex";
            case RESTORATION -> "Cryo-Cocoon Mending";
            default -> "Glacial " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports the caster forward safely, leaving a lifelike frozen ice statue decoy at the departure point that taunts hostiles and shatters into a frost pop when struck.";
            case TRANSMUTE, BREAK ->
                    "Turns water into packed/blue ice, cools lava to obsidian, and grants guaranteed Silk Touch harvesting on all frozen/brittle blocks.";
            case AFFLICTION ->
                    "Persistent milk-immune cryogenic curse (thawed via Nether/Fire reactions, Restoration magic, or caster defeat): permanently inflicts deep Freezing, cuts movement speed by 50%, shreds 40% armor toughness, and freezes the target solid in ice for 2s on any sprint or jump attempt.";
            case RESTORATION ->
                    "Encases the target in an invulnerable ice cocoon that prevents all incoming damage for 2 seconds while rapidly regenerating health points.";
            default ->
                    "Infuses the spell effect with Tier 1 Fusion Glacial Materia (Strong vs Arid, Weak vs Nature & Undead, 10% armor bypass).";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — PERMAFROST CREEP (SLOW FLOW MODIFIER)
    // =========================================================================

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        // Section 6.16 Permafrost Creep: adds +0.4 Materia Advantage score!
        return flow == SpellFlowModifier.SLOW_HEAVY ? 0.4F : 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.SLOW_HEAVY) return;

        // Permafrost Creep: creeping wave of sub-zero cryo-mass permanently freezing water beneath its wake into solid Blue Ice!
        BlockPos bp = projectile.blockPosition();
        boolean frozeWake = false;
        for (int dy = -1; dy >= -2; dy--) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx * dx + dz * dz > 1) continue;
                    BlockPos p = bp.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(p);
                    if (bs.is(Blocks.WATER) || bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE)) {
                        level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                        frozeWake = true;
                    } else if (bs.is(Blocks.LAVA)) {
                        level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                        frozeWake = true;
                    }
                }
            }
        }
        if (frozeWake || projectile.tickCount % 3 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), PERMAFROST_BLUE_HEX_COLOR, 22, 1.05F, 0.05F),
                    projectile.getX(), projectile.getY() - 0.10D, projectile.getZ(), 2, 0.16D, 0.10D, 0.16D, 0.015D
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.18D, 0.10D, 0.18D, 0.01D);
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — GLACIAL ICE-LANCE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        level.playSound(null, projectile.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.78F, 1.42F);
        level.playSound(null, projectile.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.55F, 1.32F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_BRUISER_HEX_COLOR, 18, 0.90F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        }
        // Skimming low over water flash-freezes surface water directly beneath into Packed Ice!
        BlockPos below = projectile.blockPosition().below();
        if (level.getBlockState(below).is(Blocks.WATER) && level.getBlockState(below.above()).isAir()) {
            level.setBlockAndUpdate(below, Blocks.PACKED_ICE.defaultBlockState());
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.16 Glacial Ice-Lance:
        // Heavy aerodynamic spear of blue ice (#98DCF2); pierces through up to 2 mobs (via SpellMutationRegistry),
        // inflicts 60% Slowness for 3 seconds (60t), and flash-freezes hit water blocks into packed ice!
        int tier = spell.getEffectiveTier();
        boolean permafrost = hasGlacialPermafrostCreep(spell, projectile.getVisualEssenceType());
        BlockState iceState = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        if (hitEntity instanceof LivingEntity living
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.clearFire();
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float lanceDmg = (3.4F + tier * 0.95F) * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(level, living, caster, lanceDmg, permafrost, false, true);
            }
            // Exact 60% Slowness for 3.0 seconds (60 ticks) + deep frostbite!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(living, level, 0.60D, 60L);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
            boolean wetBonus = SpellLingeringZoneManager.isEntityWetOrSoaked(living);
            int addFreeze = wetBonus ? 180 : 75;
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 75, living.getTicksFrozen() + addFreeze));
        }

        freezeWaterAndSnuffFires(level, BlockPos.containing(projectile.position()), 2, iceState, false);
        Vec3 hitPos = hitEntity.getBoundingBox().getCenter();
        Vec3 dir = projectile.getDeltaMovement().lengthSqr() > 1.0E-4D ? projectile.getDeltaMovement().normalize() : new Vec3(0.0D, 1.0D, 0.0D);
        SpellWorldVfxManager.broadcastProjectileImpact(level, hitPos, dir, EssenceType.GLACIAL, EssenceType.FROZEN, 1.45F, tier);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                hitPos.x, hitPos.y, hitPos.z, 16, 0.28D, 0.28D, 0.28D, 0.14D);
        level.playSound(null, BlockPos.containing(hitPos), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.85F, 1.32F);
    }

    @Override
    public void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        boolean permafrost = hasGlacialPermafrostCreep(spell, projectile.getVisualEssenceType());
        BlockState iceState = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
        BlockPos hitPos = blockHit.getBlockPos();

        freezeWaterAndSnuffFires(level, hitPos, 2, iceState, false);
        freezeWaterAndSnuffFires(level, hitPos.relative(blockHit.getDirection()), 2, iceState, false);

        Vec3 loc = blockHit.getLocation();
        Vec3 nrm = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        SpellWorldVfxManager.broadcastProjectileImpact(level, loc, nrm, EssenceType.GLACIAL, EssenceType.FROZEN, 1.55F, tier);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                loc.x, loc.y + 0.1D, loc.z, 18, 0.35D, 0.25D, 0.35D, 0.12D);
        level.playSound(null, hitPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.82F, 1.28F);
    }

    // =========================================================================
    // FORM MUTATION 2 — ABSOLUTE-ZERO CRYO-TORRENT BEAM (BEAM)
    // =========================================================================

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
        // Section 6.16 Absolute-Zero Cryo-Torrent Beam:
        // Channels a sustained stream of sub-zero liquid frost and ice slurry (#98DCF2);
        // stacks progressive Slowness up to a 2-second (40t) solid freeze-stasis if sustained for 3 seconds (15 x 4t = 60t),
        // while freezing water surfaces into walkable packed ice bridges and cooling lava into obsidian!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean permafrost = hasGlacialPermafrostCreep(spell, fallbackMateria);
        BlockState bridgeIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 sideHoriz = new Vec3(-normDir.z, 0.0D, normDir.x);
        sideHoriz = sideHoriz.lengthSqr() > 1.0E-4D ? sideHoriz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);

        double effectiveBridgeReach = maxReach;
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState hitState = level.getBlockState(blockHit.getBlockPos());
            boolean grazedFluidOrIce = hitState.is(Blocks.WATER) || hitState.is(Blocks.LAVA)
                    || (blockHit.getDirection() == Direction.UP
                        && (hitState.is(Blocks.PACKED_ICE) || hitState.is(Blocks.BLUE_ICE)
                            || hitState.is(Blocks.ICE) || hitState.is(Blocks.FROSTED_ICE)));
            if (!grazedFluidOrIce) {
                effectiveBridgeReach = Math.min(maxReach, eye.distanceTo(blockHit.getLocation()) + 0.5D);
            }
        }

        boolean frozeLiquidThisPulse = false;
        for (double d = 1.25D; d <= effectiveBridgeReach; d += 0.85D) {
            Vec3 sample = eye.add(normDir.scale(d));
            BlockPos rayBlock = BlockPos.containing(sample);
            BlockState rayBs = level.getBlockState(rayBlock);
            if (!rayBs.isAir() && !rayBs.is(Blocks.WATER) && !rayBs.is(Blocks.LAVA)
                    && !rayBs.is(Blocks.PACKED_ICE) && !rayBs.is(Blocks.BLUE_ICE)
                    && !rayBs.is(Blocks.ICE) && !rayBs.is(Blocks.FROSTED_ICE)
                    && !rayBs.getCollisionShape(level, rayBlock).isEmpty()) {
                break;
            }
            for (int w = -1; w <= 1; w++) {
                Vec3 lateral = sample.add(sideHoriz.scale(w * 0.95D));
                BlockPos colTop = BlockPos.containing(lateral);
                for (int dy = 1; dy >= -4; dy--) {
                    BlockPos check = colTop.offset(0, dy, 0);
                    BlockState bs = level.getBlockState(check);
                    if (bs.getBlock() instanceof BaseFireBlock) {
                        level.removeBlock(check, false);
                        continue;
                    }
                    if (bs.is(Blocks.WATER)) {
                        level.setBlockAndUpdate(check, bridgeIce);
                        BlockPos under = check.below();
                        if (level.getBlockState(under).is(Blocks.WATER)) {
                            level.setBlockAndUpdate(under, bridgeIce);
                        }
                        frozeLiquidThisPulse = true;
                        break;
                    } else if (bs.is(Blocks.LAVA)) {
                        level.setBlockAndUpdate(check, Blocks.OBSIDIAN.defaultBlockState());
                        frozeLiquidThisPulse = true;
                        break;
                    } else if (permafrost && (bs.is(Blocks.ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.FROSTED_ICE))) {
                        level.setBlockAndUpdate(check, Blocks.BLUE_ICE.defaultBlockState());
                        break;
                    } else if (!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty()) {
                        break;
                    }
                }
            }
        }
        if (frozeLiquidThisPulse) {
            level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.45F, 1.42F);
        }

        // Progressive Slowness stacking over 15 consecutive 4-tick pulses (= exact 3.0s) -> 2.0s (40t) solid freeze-stasis!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            hit.clearFire();
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float rayDmg = (1.25F + tier * 0.38F) * Math.max(0.35F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(level, hit, caster, rayDmg, permafrost, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_GLACIAL_BEAM_LAST_TICK).orElse(0L);
            int prevStacks = (now - last <= 16L) ? data.getInt(TAG_GLACIAL_BEAM_STACKS).orElse(0) : 0;
            boolean wetFast = SpellLingeringZoneManager.isEntityWetOrSoaked(hit)
                    || SpellCombatReactionEngine.hasActiveMutatedHexPublic(hit, EssenceType.WATER)
                    || SpellCombatReactionEngine.hasActiveMutatedHexPublic(hit, EssenceType.STORM);
            int stacks = prevStacks + (wetFast ? 3 : 1);
            data.putLong(TAG_GLACIAL_BEAM_LAST_TICK, now);

            double progressiveSap = Mth.clamp(0.20D + (stacks * 0.048D), 0.20D, 0.88D);
            int slowAmp = Math.min(5, stacks / 3);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, progressiveSap, 26L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 26, slowAmp));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 55, hit.getTicksFrozen() + (wetFast ? 48 : 20)));

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_BRUISER_HEX_COLOR, 18, 0.95F, 0.05F),
                    hit.getX(), hit.getY(0.65D), hit.getZ(), 4, 0.25D, 0.35D, 0.25D, 0.02D
            );

            if (stacks >= 15) {
                data.putInt(TAG_GLACIAL_BEAM_STACKS, 0);
                // Encase in 3D Solid Ice Block Stasis for exact 2.0 seconds (40 ticks)!
                FrozenSpellMutationHandler.applyIceStasis(level, caster, hit, 40, 0, spell, slot);
            } else {
                data.putInt(TAG_GLACIAL_BEAM_STACKS, stacks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — HOARFROST BLIZZARD SHOCKWAVE (RADIAL BURST)
    // =========================================================================

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
        // Section 6.16 Hoarfrost Blizzard Shockwave:
        // Expanding cryogenic fog burst in a 6-block radius that inflicts Blindness and 80% Slowness
        // on nearby hostiles for 4 seconds (80t) while snuffing out all open fires!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean permafrost = hasGlacialPermafrostCreep(spell, fallbackMateria);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float novaDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.58F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.GLACIAL, EssenceType.FROZEN, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;
            if (dx * dx + dz * dz > (r + 0.5D) * (r + 0.5D)) continue;
            entity.clearFire();
            if (entity == caster || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, wardSafe)) {
                continue;
            }

            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(level, entity, caster, novaDmg, permafrost, false, true);
            }
            // Exact Blindness + 80% Slowness for 4.0 seconds (80 ticks)!
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(entity, level, 0.80D, 80L);
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 4));
            entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() + 70, entity.getTicksFrozen() + 90));
        }

        BlockState surfaceIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
        freezeWaterAndSnuffFires(level, BlockPos.containing(center), (int) Math.ceil(r), surfaceIce, false);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), GLACIAL_BRUISER_HEX_COLOR, 36, 1.35F, 0.14F),
                center.x, center.y + 0.85D, center.z, 38, r * 0.42D, 0.65D, r * 0.42D, 0.05D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.8D, center.z, 48, r * 0.45D, 0.75D, r * 0.45D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.90F, 0.78F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.85F, 1.15F);
    }

    // =========================================================================
    // FORM MUTATION 4 — BRITTLE FROST STRIKE (TOUCH / STRIKE)
    // =========================================================================

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
        // Section 6.16 Brittle Frost Strike:
        // Strikes the victim with extreme sub-zero impact, reducing their armor toughness by 30% for 5 seconds (100t)
        // and inflicting Slowness IV!
        int tier = spell.getEffectiveTier();
        boolean permafrost = hasGlacialPermafrostCreep(spell, fallbackMateria);
        boolean cleaveSplash = apparatusPotency < 0.75F;
        int debuffTicks = cleaveSplash ? 50 : 100;
        double toughnessShredPct = cleaveSplash ? 0.15D : 0.30D;
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        if (!cleaveSplash) {
            SpellWorldVfxManager.broadcastCrescentSlash(
                    level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D),
                    dir, 2.15D, EssenceType.GLACIAL, EssenceType.FROZEN, tier
            );
        }

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            hit.clearFire();
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float strikeDmg = (3.2F + tier * 0.90F) * apparatusPotency * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(level, hit, caster, strikeDmg, permafrost, true, true);
            }
            // -30% Armor Toughness for 5.0s (100t) + 2 armor-shred vulnerability points + Slowness IV!
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, toughnessShredPct, debuffTicks);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, debuffTicks);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.60D, debuffTicks);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, debuffTicks, 3));
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 70, hit.getTicksFrozen() + 75));

            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                    hit.getX(), hit.getY(0.65D), hit.getZ(), 14, 0.28D, 0.30D, 0.28D, 0.12D);
            level.sendParticles(ParticleTypes.SNOWFLAKE, hit.getX(), hit.getY(0.70D), hit.getZ(),
                    10, 0.28D, 0.30D, 0.28D, 0.04D);
            level.playSound(null, hit.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.85F, 1.35F);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — PERMAFROST CRYO-SHROUD (AURA / SELF & ANCHORED)
    // =========================================================================

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
        // Section 6.16 Permafrost Cryo-Shroud:
        // Surrounds caster in a freezing mist (#98DCF2); grants Frost Walker ice-stepping,
        // total immunity to frostbite and powder snow (enforced every tick in SpellLingeringZoneManager),
        // and inflicts 40% Slowness + 0.5s freeze stutter on any mob that strikes the caster in melee!
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        long now = level.getGameTime();
        boolean permafrost = hasGlacialPermafrostCreep(spell, fallbackMateria);

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL, now + dur);
        data.putBoolean("SpellGlacialShroudPermafrost", permafrost);
        data.putInt("SpellWardTier", tier);

        caster.setTicksFrozen(0);
        caster.isInPowderSnow = false;
        caster.clearFire();

        BlockPos below = caster.blockPosition().below();
        BlockState walkIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.FROSTED_ICE.defaultBlockState();
        for (BlockPos p : BlockPos.betweenClosed(below.offset(-2, 0, -2), below.offset(2, 0, 2))) {
            if (p.distSqr(below) <= 6.5D && level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                level.setBlockAndUpdate(p, walkIce);
            }
        }

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.78F, null);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, caster.position().add(0.0D, 0.04D, 0.0D), 2.6D, EssenceType.GLACIAL, EssenceType.WATER, 52, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), GLACIAL_BRUISER_HEX_COLOR, 28, 1.05F, 0.08F),
                caster.getX(), caster.getY(0.65D), caster.getZ(), 16, 0.45D, 0.45D, 0.45D, 0.03D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.8D), caster.getZ(),
                24, 0.45D, 0.5D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.32F);
    }

    // =========================================================================
    // FORM MUTATION 6 — POWDER SNOW PITFALL TRAP (GLYPH / TRAP)
    // =========================================================================

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
        // Section 4.6 & 6.16 Powder Snow Pitfall Trap:
        // - Surface-snapped disarmable trap (1.0s arming, Single or Repeating mode, 50% Sneak-Right-Click disarm refund)
        // - Turns a 3x3 floor area (2 blocks deep) into powdered snow that enemies sink helplessly into when stepped on,
        //   plunging them downward and causing rapid freezing and frostbite, then cleanly restores original floor blocks!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceFloor = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(surfaceFloor.getX() + 0.5D, surfaceFloor.getY() + 1.02D, surfaceFloor.getZ() + 0.5D);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxCharges = repeating ? 3 : 1;
        int[] chargesUsed = {0};
        int[] rearmAtElapsed = {20};
        Map<BlockPos, BlockState> activeSavedFloor = new HashMap<>();

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, 2.2D, EssenceType.GLACIAL, EssenceType.FROZEN, 56, tier);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_ISA.get(), GLACIAL_BRUISER_HEX_COLOR, 36, 0.95F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 5, 0.45D, 0.05D, 0.45D, 0.005D
        );
        level.playSound(null, surfaceFloor.above(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.78F, 1.38F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, 2.4D, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        restoreSavedPitfallBlocks(srv, activeSavedFloor);
                        return false;
                    }
                    if (elapsed % 20 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 0.08D, zone.center.z, 4, 0.55D, 0.05D, 0.55D, 0.01D);
                    }
                    if (elapsed < rearmAtElapsed[0]) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 1.75D, zone.center.y - 0.8D, zone.center.z - 1.75D,
                            zone.center.x + 1.75D, zone.center.y + 2.2D, zone.center.z + 1.75D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (stepped.isEmpty()) return true;

                    float chargeScale = repeating ? 0.60F : 1.0F;
                    boolean permafrost = zone.spell != null && hasGlacialPermafrostCreep(zone.spell, zone.fallbackMateria);

                    // Convert 3x3x2 floor beneath into Powder Snow so victims plunge helplessly downward!
                    Map<BlockPos, BlockState> batchSaved = new HashMap<>();
                    for (int dy = 0; dy >= -1; dy--) {
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                BlockPos p = surfaceFloor.offset(dx, dy, dz).immutable();
                                BlockState bs = srv.getBlockState(p);
                                if (bs.is(Blocks.POWDER_SNOW)) continue;
                                if (!bs.isAir()
                                        && bs.getDestroySpeed(srv, p) >= 0.0F
                                        && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) {
                                    batchSaved.put(p, bs);
                                    activeSavedFloor.putIfAbsent(p, bs);
                                    srv.setBlockAndUpdate(p, Blocks.POWDER_SNOW.defaultBlockState());
                                }
                            }
                        }
                    }

                    for (LivingEntity v : stepped) {
                        v.clearFire();
                        v.setTicksFrozen(v.getTicksRequiredToFreeze() + 100);
                        v.isInPowderSnow = true;
                        if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                            SpellExecutionEngine.applySyncedMotion(v, new Vec3(0.0D, -0.58D, 0.0D));
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * chargeScale
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(
                                    srv, v, owner, (3.2F + tier * 0.90F) * chargeScale, permafrost, false, true
                            );
                        }
                    }

                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, 2.6D, EssenceType.GLACIAL, EssenceType.FROZEN, 64, tier);
                    srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.3D, zone.center.z,
                            30, 1.1D, 0.35D, 1.1D, 0.05D);
                    srv.playSound(null, surfaceFloor, SoundEvents.POWDER_SNOW_FALL, SoundSource.PLAYERS, 1.15F, 0.85F);

                    // Sustain rapid freezing & frostbite inside the 3x3 Powder Snow pit for 6.0s (120t), then restore floor!
                    int pitDuration = 120;
                    AABB pitBox = new AABB(
                            surfaceFloor.getX() - 1.2D, surfaceFloor.getY() - 1.5D, surfaceFloor.getZ() - 1.2D,
                            surfaceFloor.getX() + 2.2D, surfaceFloor.getY() + 1.8D, surfaceFloor.getZ() + 2.2D
                    );
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.2D, pitDuration, srv.getGameTime(), null, null, EssenceType.GLACIAL, chargeScale,
                            (psrv, pzone, pelapsed) -> {
                                LivingEntity pOwner = pzone.resolveCaster(psrv);
                                if (pelapsed < pzone.maxDurationTicks) {
                                    if (pelapsed % 15 == 0) {
                                        for (LivingEntity sunk : psrv.getEntitiesOfClass(LivingEntity.class, pitBox, LivingEntity::isAlive)) {
                                            if (sunk == pOwner || (pOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(sunk, pOwner, wardSafe))) {
                                                continue;
                                            }
                                            sunk.setTicksFrozen(sunk.getTicksRequiredToFreeze() + 90);
                                            sunk.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 3));
                                            sunk.hurtServer(psrv, psrv.damageSources().freeze(), (1.6F + tier * 0.45F) * chargeScale);
                                        }
                                    }
                                    return true;
                                }
                                // Gently lift trapped entities above the restored floor so nobody suffocates in solid blocks!
                                for (LivingEntity sunk : psrv.getEntitiesOfClass(LivingEntity.class, pitBox, LivingEntity::isAlive)) {
                                    if (sunk.getY() < surfaceFloor.getY() + 1.02D) {
                                        sunk.teleportTo(sunk.getX(), surfaceFloor.getY() + 1.05D, sunk.getZ());
                                    }
                                }
                                restoreSavedPitfallBlocks(psrv, batchSaved);
                                batchSaved.keySet().forEach(activeSavedFloor::remove);
                                return false;
                            }
                    ));

                    chargesUsed[0]++;
                    if (chargesUsed[0] >= maxCharges) {
                        return false;
                    }
                    rearmAtElapsed[0] = elapsed + pitDuration + 30;
                    return true;
                }
        ).markDisarmableGlyph()
                .withOnDisarm((srv, z) -> restoreSavedPitfallBlocks(srv, activeSavedFloor)));
        return true;
    }

    private static void restoreSavedPitfallBlocks(ServerLevel level, Map<BlockPos, BlockState> saved) {
        saved.forEach((pos, orig) -> {
            if (level.getBlockState(pos).is(Blocks.POWDER_SNOW)) {
                level.setBlockAndUpdate(pos, orig);
            }
        });
        saved.clear();
    }

    // =========================================================================
    // FORM MUTATION 7 — BLIZZARD SHRAPNEL FAN (CONE / SPRAY)
    // =========================================================================

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
        // Section 6.16 Blizzard Shrapnel Fan:
        // Blasts a fan of razor-sharp icicles in a 60-degree cone that shreds enemy armor toughness by 30%
        // and knocks mobs flat for 1.5 seconds (30t)!
        int tier = spell.getEffectiveTier();
        boolean permafrost = hasGlacialPermafrostCreep(spell, fallbackMateria);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double reach = Math.max(7.0D, coneRange);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D),
                dir, EssenceType.GLACIAL, EssenceType.FROZEN, (float) reach, 26, tier
        );

        // Freeze exposed water & snuff fires inside the 60-degree cone
        BlockState coneIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
        Vec3 origin = caster.position();
        int rSteps = (int) Math.ceil(reach);
        for (BlockPos p : BlockPos.betweenClosed(
                BlockPos.containing(origin).offset(-rSteps, -1, -rSteps),
                BlockPos.containing(origin).offset(rSteps, 2, rSteps))) {
            Vec3 toBlock = Vec3.atCenterOf(p).subtract(caster.getEyePosition());
            double d = toBlock.length();
            if (d < 0.8D || d > reach) continue;
            if (toBlock.scale(1.0D / d).dot(dir) < 0.866D) continue; // cos(30 deg) = 60-degree cone!
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, coneIce);
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }

        float shrapnelDmg = (3.0F + tier * 0.85F) * Math.max(0.40F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            hit.clearFire();
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(level, hit, caster, shrapnelDmg, permafrost, false, true);
            }
            // Shred Armor Toughness by 30% for 5.0s (100t) + knock mobs flat onto the ground for 1.5s (30t)!
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 100L);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 100L);
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 34L);
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(dir.x * 0.72D, -0.65D, dir.z * 0.72D));
            }
            FrozenSpellMutationHandler.applyIceStasis(level, caster, hit, 30, 4, spell, slot);
        }

        level.sendParticles(ParticleTypes.SNOWFLAKE,
                caster.getX() + dir.x * 2.6D, caster.getEyeY() - 0.1D + dir.y * 2.6D, caster.getZ() + dir.z * 2.6D,
                34, 0.85D, 0.45D, 0.85D, 0.12D);
        level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 1.38F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.75F, 1.28F);
    }

    // =========================================================================
    // FORM MUTATION 8 — GLACIAL MONOLITH CATACLYSM (RAIN / SKYFALL)
    // =========================================================================

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
        // Section 6.16 Glacial Monolith Cataclysm:
        // Calls down a colossal blue ice spire after a 2-second (40t) telegraph; crushes the impact epicenter,
        // detonates a frost shockwave encasing grounded mobs in ice for 2 seconds (40t), and leaves permanent
        // solid ice and blue ice blocks in the world as a lasting geological monument (100% cave-viable)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        BlockPos groundSurface = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        BlockPos baseAir = groundSurface.above();
        Vec3 snappedImpact = new Vec3(groundSurface.getX() + 0.5D, baseAir.getY(), groundSurface.getZ() + 0.5D);
        double clearance = ddraig.net.entropica.spell.SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);
        int totalCataclysmTicks = 110;
        int spireHeight = Math.clamp((int) Math.floor(clearance) - 1, 3, 5 + tier);

        // Build tier-scaled monolith blueprint & broadcast falling->standing 3D Pseudoblock columns
        // (pseudoblocks stand in place during ticks 40..110; actual world blocks materialize when the spell finishes!)
        List<int[]> monumentPlan = buildTierScaledGlacialCataclysmColumns(tier, spireHeight);
        for (int[] col : monumentPlan) {
            BlockPos colBase = baseAir.offset(col[0], 0, col[1]);
            int colH = col[2];
            BlockState pseudoState = col[3] == 2 ? Blocks.BLUE_ICE.defaultBlockState()
                    : (col[3] == 1 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState());
            Vec3 skyOrigin = new Vec3(colBase.getX() + 0.5D, baseAir.getY() + Math.max(3.5D, clearance - colH * 0.45D), colBase.getZ() + 0.5D);
            SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                    level, skyOrigin, colBase, pseudoState, colH,
                    40, totalCataclysmTicks - 40, EssenceType.GLACIAL, tier
            );
        }

        // Broadcast 110-tick 3D Descending Colossal Blue-Ice Monolith + Sky-Rift + Impact Shockwave!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                snappedImpact, snappedImpact.add(0.0D, clearance, 0.0D),
                EssenceType.GLACIAL, EssenceType.FROZEN, (float) r, totalCataclysmTicks, tier
        );
        level.playSound(null, baseAir, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.15F, 0.65F);

        final boolean[] crashed = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedImpact, r, totalCataclysmTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        // Spell complete! Materialize permanent solid Ice, Packed Ice & Blue Ice blocks where pseudoblocks stood!
                        materializePlannedGlacialCataclysmColumns(srv, baseAir, monumentPlan);
                        return false;
                    }

                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            double dropFrac = elapsed / 40.0D;
                            double riftY = zone.center.y + Math.max(1.0D, clearance * (1.0D - dropFrac * 0.85D));
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), PERMAFROST_BLUE_HEX_COLOR, 22, 1.25F, 0.06F),
                                    zone.center.x, riftY, zone.center.z, 8 + tier, 1.0D + tier * 0.14D, 0.45D, 1.0D + tier * 0.14D, 0.04D
                            );
                            srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                    zone.center.x, zone.center.y + 0.15D, zone.center.z, 10 + tier, zone.radius * 0.42D, 0.15D, zone.radius * 0.42D, 0.03D);
                        }
                        return true;
                    }

                    if (!crashed[0]) {
                        crashed[0] = true;
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean permafrost = zone.spell != null && hasGlacialPermafrostCreep(zone.spell, zone.fallbackMateria);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                        // Freeze surrounding liquids & snuff fires across the cataclysm impact crater
                        freezeWaterAndSnuffFires(srv, baseAir, (int) Math.ceil(zone.radius),
                                permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState(), true);

                        // Push entities safely outside the landed pseudoblock monolith footprint
                        for (int[] col : monumentPlan) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, baseAir.offset(col[0], 0, col[1]), col[2], true);
                        }

                        double epicenterSq = Math.pow(Math.max(3.0D, zone.radius * 0.35D), 2);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.0D, spireHeight + 1.5D), zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            double hDistSq = dx * dx + dz * dz;
                            if (hDistSq > (zone.radius + 0.6D) * (zone.radius + 0.6D)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) {
                                continue;
                            }

                            v.clearFire();
                            boolean inEpicenter = hDistSq <= epicenterSq;
                            float potencyScale = zone.apparatusPotency * (inEpicenter ? 1.35F : 1.05F);

                            // Grounded mobs across the frost shockwave are encased solid in 3D ice for 2.0 seconds (40 ticks)!
                            if (isEntityGrounded(srv, v)) {
                                FrozenSpellMutationHandler.applyIceStasis(srv, owner, v, 40, 0, zone.spell, zone.slot);
                            } else {
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.60D, 60L);
                                v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
                            }

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, potencyScale
                                );
                            }
                            if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                float crushDmg = (5.5F + tier * 1.45F) * potencyScale;
                                SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(srv, v, owner, crushDmg, permafrost, false, true);
                            }
                        }

                        SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.GLACIAL, EssenceType.FROZEN, tier);
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                                zone.center.x, zone.center.y + 1.2D, zone.center.z, 46, 1.4D, 1.2D, 1.4D, 0.16D);
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 0.9D, zone.center.z, 42, zone.radius * 0.42D, 0.8D, zone.radius * 0.42D, 0.07D);
                        srv.playSound(null, baseAir, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.05F, 0.78F);
                        srv.playSound(null, baseAir, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.25F, 0.72F);
                        return true;
                    }

                    // Ticks 41..109: maintain pseudoblock obstacle collision until actual world ice blocks materialize at completion!
                    for (int[] col : monumentPlan) {
                        SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, baseAir.offset(col[0], 0, col[1]), col[2], false);
                    }
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 1.5D, zone.center.z,
                                6 + tier / 2, zone.radius * 0.35D, 1.2D, zone.radius * 0.35D, 0.02D);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — GLACIAL-BRUISER EIDOLON SUITE (AGENT / SENTRY / DECOY)
    // =========================================================================

    @Override
    public void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.GLACIAL.name());
        tag.putInt("SpellEidolonTintRGB", GLACIAL_BRUISER_HEX_COLOR);

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.04D, 0.0D), 2.2D, EssenceType.GLACIAL, EssenceType.FROZEN, 52, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), GLACIAL_BRUISER_HEX_COLOR, 26, 1.05F, 0.07F),
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 14, 0.45D, 0.55D, 0.45D, 0.03D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.85D, spawnPos.z,
                24, 0.45D, 0.55D, 0.45D, 0.04D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.28F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT SYNERGIES (BLINK, TRANSMUTE/BREAK, HEX, RESTORATION)
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.16 Ice Statue Blink:
        // Teleports the caster forward safely, leaving a lifelike frozen ice statue decoy at the departure point
        // that taunts hostiles and shatters into a frost pop when struck!
        boolean permafrost = SpellExecutionEngine.hasPrimedGlacialPermafrostCreep(caster);
        BlockState padIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        // Freeze water underfoot at both departure & arrival so blinking onto water forms a solid ice pad!
        freezeWaterAndSnuffFires(level, BlockPos.containing(departurePos).below(), 1, padIce, false);
        freezeWaterAndSnuffFires(level, BlockPos.containing(arrivalPos).below(), 1, padIce, false);
        caster.clearFire();

        // Spawn lifelike 3D Frozen Ice Statue Decoy mirroring the caster's armor & held gear that shatters immediately when struck!
        SpellEidolonEntity statue = SpellEidolonEntity.spawnShortLivedDecoy(
                level,
                caster,
                departurePos,
                EssenceType.GLACIAL,
                14.0F + potencyTier * 3.0F,
                120,
                "§bFrozen Ice Statue",
                null
        );
        EntityHelper.getPersistentData(statue).putBoolean("SpellGlacialStatuePopOnStrike", true);

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, departurePos.add(0.0D, 0.03D, 0.0D), 1.85D, EssenceType.GLACIAL, EssenceType.FROZEN, 52, potencyTier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_BRUISER_HEX_COLOR, 26, 1.05F, 0.06F),
                arrivalPos.x, arrivalPos.y + 0.95D, arrivalPos.z, 14, 0.35D, 0.55D, 0.35D, 0.03D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, arrivalPos.x, arrivalPos.y + 0.90D, arrivalPos.z,
                14, 0.35D, 0.50D, 0.35D, 0.03D);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.90F, 1.35F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.78F, 1.48F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.16 Cryo-Solidification & Silk Touch:
        // Turns water into packed/blue ice, cools lava to obsidian, and grants guaranteed Silk Touch harvesting
        // on all frozen/brittle blocks!
        if (state.is(Blocks.WATER)) {
            boolean source = state.hasProperty(LiquidBlock.LEVEL) && state.getValue(LiquidBlock.LEVEL) == 0;
            level.setBlockAndUpdate(pos, (source ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState());
            emitCryoTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.80F, 1.25F);
            emitCryoTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.BASALT.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.75F, 1.30F);
            emitCryoTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }
        if (state.getBlock() instanceof BaseFireBlock) {
            level.removeBlock(pos, false);
            return true;
        }
        if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
            return true;
        }
        if (state.getBlock() instanceof AbstractCandleBlock && state.getValue(AbstractCandleBlock.LIT)) {
            AbstractCandleBlock.extinguish(null, state, level, pos);
            return true;
        }
        if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            emitCryoTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }
        if (state.is(Blocks.PACKED_ICE) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
            level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            emitCryoTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (isFrozenOrBrittleSilkBlock(state)) {
            float maxHardness = 4.0F + potencyTier * 6.0F;
            boolean harvested = FrozenSpellMutationHandler.harvestBlockWithSilkTouchAndSealLiquids(
                    level, caster, pos, state, maxHardness, true, null, null
            );
            if (harvested) {
                grantBatchCryoDiscountIfEligible(level, caster, potencyTier);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        int count = in.getCount();
        if (in.is(Items.SNOWBALL)) {
            int packed = count / 4;
            itemEntity.setItem(packed > 0 ? new ItemStack(Items.PACKED_ICE, packed) : new ItemStack(Items.ICE, count));
            return true;
        }
        if (in.is(Items.SNOW_BLOCK) || in.is(Items.SNOW) || in.is(Items.ICE)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, count));
            return true;
        }
        if (in.is(Items.PACKED_ICE)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, count));
            return true;
        }
        if (in.is(Items.WATER_BUCKET) || in.is(Items.POWDER_SNOW_BUCKET)) {
            itemEntity.setItem(new ItemStack(in.is(Items.WATER_BUCKET) ? Items.BLUE_ICE : Items.PACKED_ICE, count));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                    new ItemStack(Items.BUCKET, count)));
            return true;
        }
        if (in.is(Items.LAVA_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.OBSIDIAN, count));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                    new ItemStack(Items.BUCKET, count)));
            return true;
        }
        if (in.is(Items.MAGMA_CREAM)) {
            itemEntity.setItem(new ItemStack(Items.SLIME_BALL, count));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.16 Absolute-Zero Cryo-Hex:
        // Persistent milk-immune cryogenic curse until thawed via Nether/Fire reactions, Restoration magic, or caster defeat:
        // - Permanently inflicts deep Freezing
        // - Permanently cuts movement speed by 50% (also enforced every 5 ticks in SpellCombatReactionEngine)
        // - Permanently shreds 40% Armor Toughness
        // - Any sprint or jump attempt freezes the target solid in 3D ice for 2.0s (enforced every tick in SpellLingeringZoneManager)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.setSprinting(false);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 55);

        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.85D, 0.50D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2, true, false));
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 45L);

        SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(
                level, target, null, (1.45F + potencyTier * 0.52F) * mult, false, false, true
        );

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_BRUISER_HEX_COLOR, 26, 1.05F, 0.06F),
                target.getX(), target.getY(0.65D), target.getZ(), 5, 0.28D, 0.38D, 0.28D, 0.02D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_ISA.get(), DIAMOND_CRYO_HEX_COLOR, 24, 0.95F, 0.04F),
                target.getX(), target.getY(0.90D), target.getZ(), 2, 0.22D, 0.25D, 0.22D, 0.01D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.8D), target.getZ(),
                6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellCoreEffect effect,
            int potencyTier,
            SpellAssembly spell
    ) {
        if (effect != SpellCoreEffect.RESTORATION) {
            return;
        }
        // Guard against encasing hostile Undead when combat Restoration inverts into Undead smite damage!
        boolean ownedSummon = target instanceof SpellEidolonEntity eid && caster != null && eid.isOwnedBy(caster);
        boolean alliedTarget = target == caster || ownedSummon || (caster != null && target.isAlliedTo(caster));
        if (!spell.prefix().enforcesZeroEntityHarm() && !alliedTarget && target.getType().is(EntityTypeTags.UNDEAD)) {
            return;
        }

        // Section 6.16 Cryo-Cocoon Mending:
        // Encases the target in an invulnerable 3D ice cocoon that prevents 100% of incoming damage for 2.0 seconds (40t)
        // while rapidly regenerating health points!
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL, now + 40L);
        data.putInt("SpellCryoCocoonTier", potencyTier);
        data.putInt("SpellWardTier", potencyTier);

        target.clearFire();
        target.setTicksFrozen(0);
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 3, false, true));
        SpellCombatReactionEngine.syncEntityBarrierState(level, target, 0.90F, null);

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, target.position().add(0.0D, 0.03D, 0.0D), 1.95D, EssenceType.GLACIAL, EssenceType.FROZEN, 44, potencyTier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_BRUISER_HEX_COLOR, 26, 1.05F, 0.05F),
                target.getX(), target.getY(0.85D), target.getZ(), 18, 0.40D, 0.55D, 0.40D, 0.03D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.9D), target.getZ(),
                20, 0.40D, 0.60D, 0.40D, 0.04D);
        level.playSound(null, target.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.90F, 1.35F);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.48F);
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    public static boolean hasGlacialPermafrostCreep(SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        if (spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY || !spell.flowPart().mutationEnabled()) {
            return false;
        }
        EssenceType flowMat = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        return flowMat == EssenceType.GLACIAL || flowMat == EssenceType.FROZEN;
    }

    private static void emitCryoTransmuteFx(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, int tier) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_BRUISER_HEX_COLOR, 22, 0.95F, 0.05F),
                pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.02D
        );
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.72F, 1.38F);
        grantBatchCryoDiscountIfEligible(level, caster, tier);
    }

    private static void grantBatchCryoDiscountIfEligible(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellGlacialBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellGlacialBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellGlacialBatchTick", now);
        pData.putInt("SpellGlacialBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.GLACIAL, refund);
        }
    }

    private static boolean isFrozenOrBrittleSilkBlock(BlockState state) {
        Block b = state.getBlock();
        return b instanceof TransparentBlock
                || b instanceof StainedGlassBlock
                || b instanceof StainedGlassPaneBlock
                || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.TINTED_GLASS)
                || state.is(Blocks.GLOWSTONE)
                || state.is(Blocks.SEA_LANTERN)
                || state.is(Blocks.AMETHYST_CLUSTER)
                || state.is(Blocks.LARGE_AMETHYST_BUD)
                || state.is(Blocks.MEDIUM_AMETHYST_BUD)
                || state.is(Blocks.SMALL_AMETHYST_BUD)
                || state.is(Blocks.BUDDING_AMETHYST)
                || state.is(Blocks.POINTED_DRIPSTONE)
                || state.is(Blocks.CALCITE)
                || state.is(Blocks.TURTLE_EGG)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.SCULK)
                || state.is(Blocks.SCULK_VEIN)
                || state.is(Blocks.SCULK_CATALYST)
                || state.is(Blocks.SCULK_SENSOR)
                || state.is(Blocks.BOOKSHELF)
                || state.is(Blocks.ENDER_CHEST)
                || state.is(BlockTags.ICE)
                || state.is(BlockTags.IMPERMEABLE)
                || state.is(BlockTags.CORALS)
                || state.is(BlockTags.CORAL_BLOCKS)
                || state.is(BlockTags.WALL_CORALS)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES);
    }

    private static void freezeWaterAndSnuffFires(
            ServerLevel level,
            BlockPos center,
            int r,
            BlockState iceState,
            boolean coolLavaToObsidian
    ) {
        double rSq = (r + 0.5D) * (r + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, iceState);
            } else if (coolLavaToObsidian && bs.is(Blocks.LAVA)) {
                level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof AbstractCandleBlock && bs.getValue(AbstractCandleBlock.LIT)) {
                AbstractCandleBlock.extinguish(null, bs, level, p);
            }
        }
    }

    private static boolean isEntityGrounded(ServerLevel level, LivingEntity entity) {
        if (entity.onGround()) return true;
        BlockPos below = BlockPos.containing(entity.getX(), entity.getY() - 0.55D, entity.getZ());
        BlockState bs = level.getBlockState(below);
        return !bs.isAir() && (!bs.getCollisionShape(level, below).isEmpty() || bs.is(Blocks.WATER) || bs.is(Blocks.LAVA));
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 1; dy >= -4; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty())
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return start.below();
    }

    private static double probeOverheadClearance(ServerLevel level, BlockPos groundAir, int maxScan) {
        for (int dy = 3; dy <= maxScan; dy++) {
            BlockPos p = groundAir.above(dy);
            BlockState bs = level.getBlockState(p);
            if (!bs.isAir() && !bs.getCollisionShape(level, p).isEmpty()) {
                return Math.max(3.5D, dy - 0.35D);
            }
        }
        return maxScan;
    }

    /**
     * Builds a tier-scaled Glacial Cataclysm monolith blueprint {@code [dx, dz, height, materialKind]}
     * ({@code 2 = BLUE_ICE, 1 = PACKED_ICE, 0 = ICE}), scaling from 3x3 at Tier 1–3 up to a colossal 7x7 spire at Tier 8–10!
     */
    private static List<int[]> buildTierScaledGlacialCataclysmColumns(int tier, int spireHeight) {
        List<int[]> cols = new ArrayList<>();
        int h = Math.max(3, spireHeight);
        cols.add(new int[]{0, 0, h, 2});

        int cardH = Math.max(2, (int) Math.round(h * 0.65D));
        cols.add(new int[]{1, 0, cardH, 2});
        cols.add(new int[]{-1, 0, cardH, 2});
        cols.add(new int[]{0, 1, cardH, 2});
        cols.add(new int[]{0, -1, cardH, 2});

        int diagH = Math.max(1, (int) Math.round(h * 0.44D));
        int[][] diags = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : diags) {
            cols.add(new int[]{d[0], d[1], diagH, 1});
        }

        if (tier >= 4) {
            int outerCardH = Math.max(2, (int) Math.round(h * 0.38D));
            cols.add(new int[]{2, 0, outerCardH, 1});
            cols.add(new int[]{-2, 0, outerCardH, 1});
            cols.add(new int[]{0, 2, outerCardH, 1});
            cols.add(new int[]{0, -2, outerCardH, 1});
            if (tier >= 6) {
                int knightH = Math.max(1, (int) Math.round(h * 0.25D));
                int[][] knights = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};
                for (int[] k : knights) {
                    cols.add(new int[]{k[0], k[1], knightH, 1});
                }
            }
        }
        if (tier >= 8) {
            int spurH = Math.max(1, (int) Math.round(h * 0.26D));
            cols.add(new int[]{3, 0, spurH, 0});
            cols.add(new int[]{-3, 0, spurH, 0});
            cols.add(new int[]{0, 3, spurH, 0});
            cols.add(new int[]{0, -3, spurH, 0});
            cols.add(new int[]{2, 2, Math.max(1, spurH - 1), 0});
            cols.add(new int[]{-2, 2, Math.max(1, spurH - 1), 0});
            cols.add(new int[]{2, -2, Math.max(1, spurH - 1), 0});
            cols.add(new int[]{-2, -2, Math.max(1, spurH - 1), 0});
        }
        return cols;
    }

    private static void materializePlannedGlacialCataclysmColumns(ServerLevel level, BlockPos base, List<int[]> cols) {
        for (int[] col : cols) {
            BlockPos colBase = base.offset(col[0], 0, col[1]);
            int colH = col[2];
            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(level, colBase, colH, true);
            BlockState state = col[3] == 2 ? Blocks.BLUE_ICE.defaultBlockState()
                    : (col[3] == 1 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState());
            for (int y = 0; y < colH; y++) {
                BlockPos p = colBase.above(y);
                if (canPlaceMonumentIceAt(level, p)) {
                    level.setBlockAndUpdate(p, (col[3] == 2 && y == colH - 1) ? Blocks.PACKED_ICE.defaultBlockState() : state);
                } else if (y > 1) {
                    break;
                }
            }
        }
        level.playSound(null, base, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.05F, 0.82F);
    }

    private static boolean canPlaceMonumentIceAt(ServerLevel level, BlockPos pos) {
        BlockState cur = level.getBlockState(pos);
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, cur)) {
            return false;
        }
        return cur.isAir()
                || cur.canBeReplaced()
                || cur.is(Blocks.WATER)
                || cur.is(Blocks.SNOW)
                || cur.is(Blocks.PACKED_ICE)
                || cur.is(Blocks.ICE)
                || cur.is(Blocks.FROSTED_ICE);
    }
}
