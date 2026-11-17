package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
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
 * Complete per-part Signature Mutation implementation for <b>Frozen Materia (Base Type #5)</b>
 * (Spellcrafting System Guide Sections 1.2A, 2.2, 2.3, 3.8, 4.1–4.9, 5.1–5.8 &amp; 6.6):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>WATER (+1)</b> | Weak against <b>NETHER (-1)</b> and <b>UNDEAD (-1)</b>.</li>
 *     <li><b>Projectile</b>: <i>Glacial Ice-Shard</i> — aerodynamic zero-drop 3D crystalline frost needle that shatters on impact,
 *     dealing Frozen Materia damage (+1 vs Water, -1 vs Nether/Undead), inflicting exact 60% Slowness for 3.0s, and freezing water into packed ice on hit.</li>
 *     <li><b>Beam</b>: <i>Absolute-Zero Cryo-Ray</i> — continuous beam of sub-zero frost stacking progressive slowness up to
 *     complete 3D Solid Ice Block freeze-stasis at 3.0 seconds while freezing water surfaces under the beam into thick walkable ice bridges.</li>
 *     <li><b>Radial Burst</b>: <i>Frost Nova Burst</i> — detonates in a 6-block ring of jagged 3D ice spikes, dealing Frozen Materia damage,
 *     knocking enemies backward across frozen ground, encasing grounded foes in solid 3D ice blocks for 2.0s, and freezing all surface liquids.</li>
 *     <li><b>Touch / Strike</b>: <i>Flash-Freeze Stasis</i> — converts weapon strike to pure Frozen Materia damage and encases the primary target
 *     inside a temporary solid 3D block of ice for 1.5s (0.75s on 50% sweeping cleave splash targets), completely halting movement and attacks.</li>
 *     <li><b>Aura / Self</b>: <i>Glacial Spikes Retaliation</i> — registers defense as Frozen; melee attackers are stabbed by reactive
 *     3D ice spikes, taking retaliatory Frozen damage and getting their attack speed cut by 30% (both players and hostile mobs).</li>
 *     <li><b>Glyph / Trap</b>: <i>Rime Pitfall</i> — turns ground beneath the trap into fragile hollow frosted ice that collapses
 *     when stepped on, dropping mobs into a 3-block-deep pit below (and restoring the floor cleanly if disarmed).</li>
 *     <li><b>Cone / Spray</b>: <i>Hoarfrost Flash-Freeze</i> — freezes all standing and flowing water and lava in a 10-block 60-degree cone into
 *     solid walkable ice and obsidian causeways while coating dry ground in transient rime frost.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Colossal Glacial Monolith</i> — summons a single gigantic 3D spire of blue ice from an overhead sky/cave rift
 *     that plummets after 2.0 seconds, crushing the impact zone and leaving a permanent decorative blue-ice monument.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Glacial Eidolon Suite</i> — cyan/frost (#98DCF2) triad: 3-hit solid-freezing Agent,
 *     rapid icicle-dart frost crystal Sentry, and 4m Frost-Nova-shattering Ice Effigy Decoy.</li>
 *     <li><b>Flow (Slow)</b>: <i>Permafrost Creep</i> — creeping wave of heavy frost that permanently freezes water into Blue Ice
 *     and adds +0.4 Materia Advantage score against Water targets.</li>
 *     <li><b>Blink / Step</b>: <i>Frostwalker Stasis</i> — teleports caster safely, leaving a 3D crystalline player ice statue decoy at departure.</li>
 *     <li><b>Transmute &amp; Break</b>: <i>Silk Touch &amp; Permafrost</i> — guaranteed Silk Touch block harvesting, automatic sealing of adjacent
 *     water/lava leaks into ice/obsidian, lava to obsidian, and snow compaction into packed ice.</li>
 *     <li><b>Affliction</b>: <i>Permafrost Stasis Hex</i> — deep cryogenic curse slowing movement by 60%, locking sprinting every tick,
 *     and increasing blunt/physical shatter damage taken by +50% until cleansed by fire reactions or Restoration magic (milk-immune).</li>
 * </ul>
 */
public final class FrozenSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GLACIAL_EIDOLON_HEX_COLOR = 0x98DCF2;
    private static final int DIAMOND_FROST_HEX_COLOR = 0xEBF9FF;
    private static final int PERMAFROST_BLUE_HEX_COLOR = 0x2C82C9;

    /** Persistent NBT keys for tracking consecutive 4-tick Absolute-Zero Cryo-Ray stacks (15 pulses = 3.0s). */
    private static final String TAG_CRYO_RAY_STACKS = "FrozenCryoRayStacks";
    private static final String TAG_CRYO_RAY_LAST_TICK = "FrozenCryoRayLastTick";

    // Generous 3D VFX durations so players can admire glacial crystal formations in the world
    private static final int VFX_SHARD_SHATTER_TICKS = 56;
    private static final int VFX_NOVA_SPIKES_TICKS = 68;
    private static final int VFX_NOVA_SEAL_TICKS = 64;
    private static final int VFX_FLASH_SLASH_TICKS = 52;
    private static final int VFX_HOARFROST_CONE_TICKS = 56;
    private static final int VFX_PITFALL_COLLAPSE_TICKS = 76;
    private static final int VFX_MONOLITH_TOTAL_TICKS = 110;
    private static final int VFX_SUMMON_PILLAR_TICKS = 60;

    @Override
    public EssenceType materiaType() {
        return EssenceType.FROZEN;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.6)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Permafrost Creep";
            default -> "Cryo-Frost " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Permafrost Creep: Turns the spell into a creeping wave of heavy frost that permanently freezes water into Blue Ice and adds +0.4 Materia Advantage score against Water targets.";
            default -> "Cryo-Frost Flow: Channels sub-zero crystalline momentum along glacial currents, leaving a chilling wake that slows nearby projectiles.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Glacial Ice-Shard";
            case BEAM -> "Absolute-Zero Cryo-Ray";
            case RADIAL_BURST -> "Frost Nova Burst";
            case TOUCH_STRIKE -> "Flash-Freeze Stasis";
            case AURA -> "Glacial Spikes Retaliation";
            case GLYPH -> "Rime Pitfall";
            case CONE -> "Hoarfrost Flash-Freeze";
            case SKYFALL -> "Colossal Glacial Monolith";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> "Glacial Eidolon Suite";
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Glacial Ice-Shard: Aerodynamic crystalline frost needle shattering on impact (+1 vs Water, -1 vs Nether/Undead), inflicting 60% Slowness for 3s, and freezing water into Packed Ice.";
            case BEAM -> "Absolute-Zero Cryo-Ray: Sub-zero frost beam stacking progressive slowness up to complete 3D ice freeze-stasis at 3.0s while freezing water into thick walkable ice bridges.";
            case RADIAL_BURST -> "Frost Nova Burst: 6m ring of jagged 3D ice spikes knocking foes back across frozen ground, encasing grounded enemies in solid 3D ice blocks for 2.0s, and freezing surface liquids.";
            case TOUCH_STRIKE -> "Flash-Freeze Stasis: Converts weapon strike to pure Frozen Materia damage and encases the target in a solid 3D ice block for 1.5s, halting movement and attacks.";
            case AURA -> "Glacial Spikes Retaliation: Registers defense as Frozen; reactive 3D ice spikes stab melee attackers for Frozen damage and cut their attack speed by 30%.";
            case GLYPH -> "Rime Pitfall: Turns the floor beneath the trap into fragile hollow frosted ice that collapses when stepped on, dropping mobs into a 3-block-deep pit below.";
            case CONE -> "Hoarfrost Flash-Freeze: Freezes all water and lava blocks in a 10-block 60-degree cone into solid walkable ice and obsidian causeways.";
            case SKYFALL -> "Colossal Glacial Monolith: Summons a single gigantic spire of blue ice that plummets after 2.0s, crushing the impact zone and leaving a permanent decorative ice monument.";
            case SUMMON_AGENT -> "Glacial Eidolon (Agent): Cyan-frost melee companion (#98DCF2) whose chilling strikes freeze enemies solid in 3D ice after 3 consecutive hits.";
            case SUMMON_SENTRY -> "Glacial Eidolon (Sentry): Anchored frost crystal pylon firing rapid zero-drop icicle darts at distant foes.";
            case SUMMON_DECOY -> "Glacial Eidolon (Decoy): Crystalline ice effigy that taunts mobs and shatters into a 4m Frost Nova upon destruction, freezing attackers solid.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Frostwalker Stasis";
            case TRANSMUTE, BREAK -> "Silk Touch & Permafrost";
            case AFFLICTION -> "Permafrost Stasis Hex";
            default -> "Glacial " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Frostwalker Stasis: Teleports caster safely while leaving a taunting 3D crystalline player ice statue decoy at the departure point.";
            case TRANSMUTE, BREAK -> "Silk Touch & Permafrost: Unlocks guaranteed Silk Touch block harvesting, seals exposed water/lava leaks into ice/obsidian, freezes lava to obsidian, and compacts snow into packed ice.";
            case AFFLICTION -> "Permafrost Stasis Hex: Persistent cryogenic curse slowing movement by 60%, locking sprinting, and increasing blunt impact shatter damage by +50% (cleansed by fire or Restoration; milk-immune).";
            default -> null;
        };
    }

    // =========================================================================
    // FLOW MUTATION — PERMAFROST CREEP (SLOW_HEAVY, +0.4 ADVANTAGE VS WATER)
    // =========================================================================

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        if (flow != SpellFlowModifier.SLOW_HEAVY) {
            return 0.0F;
        }
        boolean isWaterTarget = target instanceof Drowned
                || target instanceof Guardian
                || target instanceof WaterAnimal
                || SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                || SpellCombatReactionEngine.hasActiveMutatedHexPublic(target, EssenceType.WATER)
                || (target.level() instanceof ServerLevel srv
                    && srv.getGameTime() <= EntityHelper.getPersistentData(target).getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L))
                || (target instanceof SpellEidolonEntity eid && eid.getEffectiveMateria() == EssenceType.WATER);
        return isWaterTarget ? 0.4F : 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.SLOW_HEAVY) {
            return;
        }
        // Permafrost Creep: creeping wave of heavy frost permanently freezing water into Blue Ice along its path!
        if (projectile.tickCount % 2 == 0) {
            BlockPos center = projectile.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -2, -1), center.offset(1, 0, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER)) {
                    level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                } else if (bs.is(Blocks.LAVA)) {
                    level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                } else if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE)) {
                    level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                }
            }
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), PERMAFROST_BLUE_HEX_COLOR, 26, 1.05F, 0.06F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    3, 0.22D, 0.16D, 0.22D, 0.015D
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    3, 0.25D, 0.15D, 0.25D, 0.01D);
        }
    }

    @Override
    public void onProjectileHitEntityFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        if (spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY || !(hit instanceof LivingEntity living)) {
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        // Permafrost Creep: +0.4 Materia Advantage score against Water targets (aquatic mobs, soaked targets, Water Shroud, Aqueous Eidolons)
        boolean isWaterTarget = living instanceof Drowned
                || living instanceof Guardian
                || living instanceof WaterAnimal
                || SpellLingeringZoneManager.isEntityWetOrSoaked(living)
                || SpellCombatReactionEngine.hasActiveMutatedHexPublic(living, EssenceType.WATER)
                || level.getGameTime() <= EntityHelper.getPersistentData(living).getLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL).orElse(0L)
                || (living instanceof SpellEidolonEntity eid && eid.getEffectiveMateria() == EssenceType.WATER);
        if (isWaterTarget) {
            float bonusDmg = Math.max(2.0F, spell.getEffectiveTier() * 1.15F);
            SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, living, caster, bonusDmg, true, true);
        }
        living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 90, living.getTicksFrozen() + 90));
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(living, level, 0.60D, 70L);
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 3));

        BlockPos hitPos = living.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(hitPos.offset(-2, -1, -2), hitPos.offset(2, 0, 2))) {
            if (p.distSqr(hitPos) <= 5.0D && level.getBlockState(p).is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — GLACIAL ICE-SHARD (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        Vec3 pos = projectile.position();
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 20, 0.90F, 0.04F),
                    pos.x, pos.y, pos.z, 2, 0.06D, 0.06D, 0.06D, 0.008D
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 2, 0.05D, 0.05D, 0.05D, 0.005D);
        }
        // Douse fire blocks grazed along flight path
        BlockPos curPos = projectile.blockPosition();
        if (level.getBlockState(curPos).getBlock() instanceof BaseFireBlock) {
            level.removeBlock(curPos, false);
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        if (!(hit instanceof LivingEntity living)) {
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        boolean permafrost = hasPermafrostCreep(spell, projectile.getVisualEssenceType());
        BlockState iceBlock = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        // Glacial Ice-Shard: Frozen Materia damage (+1 vs Water, -1 vs Nether/Undead) when no Harm verb already billed it
        if (!slotHasHarmEffect(slot)) {
            float shardDmg = (2.8F + tier * 0.95F) * spell.flawProfile().getPotencyMultiplier();
            SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, living, caster, shardDmg, permafrost, false);
        }

        // Exact 60% Slowness for 3.0 seconds (60 ticks) via both real MOVEMENT_SPEED attribute sap AND Slowness IV!
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(living, level, 0.60D, 60L);
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));

        // Water (#2) Soaked / Waterlogged Hex synergy: 3.0x Frozen freeze buildup rate!
        boolean tripleFreeze = SpellLingeringZoneManager.isEntityWetOrSoaked(living)
                || SpellCombatReactionEngine.hasActiveMutatedHexPublic(living, EssenceType.WATER);
        int freezeAdd = (tripleFreeze ? 3 : 1) * (65 + tier * 10);
        living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 90, living.getTicksFrozen() + freezeAdd));
        if (living.isOnFire()) {
            living.clearFire();
        }

        // Freeze nearby water into Packed Ice (or Blue Ice with Permafrost Creep) & extinguish surface fire
        freezeLiquidsInRadius(level, living.blockPosition(), 2, iceBlock, true);

        Vec3 impactCenter = living.position().add(0.0D, living.getBbHeight() * 0.55D, 0.0D);
        Vec3 normal = projectile.getDeltaMovement().lengthSqr() > 1.0E-4D
                ? projectile.getDeltaMovement().normalize().scale(-1.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                impactCenter, normal, EssenceType.FROZEN, null,
                1.35F, VFX_SHARD_SHATTER_TICKS, tier
        );
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, iceBlock),
                living.getX(), living.getY(0.6D), living.getZ(), 18, 0.32D, 0.35D, 0.32D, 0.14D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), DIAMOND_FROST_HEX_COLOR, 30, 1.15F, 0.08F),
                living.getX(), living.getY(0.65D), living.getZ(), 12, 0.30D, 0.35D, 0.30D, 0.04D
        );
        level.playSound(null, living.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 1.38F);
        level.playSound(null, living.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.65F, 1.50F);
    }

    @Override
    public void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult hitResult,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        boolean permafrost = hasPermafrostCreep(spell, projectile.getVisualEssenceType());
        BlockState iceBlock = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
        BlockPos hitPos = hitResult.getBlockPos();

        freezeLiquidsInRadius(level, hitPos, 2, iceBlock, true);

        Vec3 hitLoc = hitResult.getLocation();
        Vec3 normal = Vec3.atLowerCornerOf(hitResult.getDirection().getUnitVec3i());
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                hitLoc, normal, EssenceType.FROZEN, null,
                1.25F, VFX_SHARD_SHATTER_TICKS, tier
        );
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, iceBlock),
                hitLoc.x, hitLoc.y + 0.15D, hitLoc.z, 16, 0.35D, 0.25D, 0.35D, 0.12D);
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                hitLoc.x, hitLoc.y + 0.20D, hitLoc.z, 10, 0.30D, 0.20D, 0.30D, 0.04D);
        level.playSound(null, hitPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.85F, 1.42F);
    }

    // =========================================================================
    // FORM MUTATION 2 — ABSOLUTE-ZERO CRYO-RAY (BEAM)
    // =========================================================================

    @Override
    public void onBeamFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            List<LivingEntity> struckTargets,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float potencyMult
    ) {
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean permafrost = hasPermafrostCreep(spell, fallbackMateria);
        BlockState bridgeIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        // 1. Freeze water surfaces under the beam into thick walkable 3-wide x 2-thick ice bridges (and lava -> obsidian),
        // stopping cleanly at genuine solid non-ice walls while projecting seamlessly across water & newly formed ice!
        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 sideHoriz = new Vec3(-normDir.z, 0.0D, normDir.x);
        if (sideHoriz.lengthSqr() > 1.0E-4D) {
            sideHoriz = sideHoriz.normalize();
        } else {
            sideHoriz = new Vec3(1.0D, 0.0D, 0.0D);
        }

        double effectiveBridgeReach = maxReach;
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState hitState = level.getBlockState(blockHit.getBlockPos());
            boolean grazedIceOrFluid = hitState.is(Blocks.WATER) || hitState.is(Blocks.LAVA)
                    || (blockHit.getDirection() == Direction.UP
                        && (hitState.is(Blocks.PACKED_ICE) || hitState.is(Blocks.BLUE_ICE)
                            || hitState.is(Blocks.ICE) || hitState.is(Blocks.FROSTED_ICE)));
            if (!grazedIceOrFluid) {
                effectiveBridgeReach = Math.min(maxReach, eye.distanceTo(blockHit.getLocation()) + 0.5D);
            }
        }

        boolean frozeLiquidThisPulse = false;
        for (double d = 1.25D; d <= effectiveBridgeReach; d += 0.85D) {
            Vec3 sample = eye.add(normDir.scale(d));
            BlockPos rayBlock = BlockPos.containing(sample);
            BlockState rayBs = level.getBlockState(rayBlock);

            // Stop at genuine solid non-ice terrain walls
            if (!rayBs.isAir() && !rayBs.is(Blocks.WATER) && !rayBs.is(Blocks.LAVA)
                    && !rayBs.is(Blocks.PACKED_ICE) && !rayBs.is(Blocks.BLUE_ICE)
                    && !rayBs.is(Blocks.ICE) && !rayBs.is(Blocks.FROSTED_ICE)
                    && !rayBs.getCollisionShape(level, rayBlock).isEmpty()) {
                break;
            }

            // Probe from +1 down to -4 below the ray line so aiming across a lake surface builds a continuous 3-wide x 2-thick bridge!
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
                        // Reinforce into a thick 2-block walkable ice slab!
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
                        break; // Reached solid dry ground in this column
                    }
                }
            }
        }
        if (frozeLiquidThisPulse) {
            level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.45F, 1.45F);
        }

        // 2. Progressive Slowness stacking up to complete 3D Solid Ice Block Stasis at 3.0 seconds (15 pulses of 4 ticks)!
        for (LivingEntity target : struckTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    target, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (target.isOnFire()) {
                target.clearFire();
            }

            if (!slotHasHarmEffect(slot)) {
                float rayDmg = (1.15F + tier * 0.35F) * potencyMult * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, target, caster, rayDmg, permafrost, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(target);
            long lastHit = data.getLong(TAG_CRYO_RAY_LAST_TICK).orElse(0L);
            int prevStacks = (now - lastHit <= 16L) ? data.getInt(TAG_CRYO_RAY_STACKS).orElse(0) : 0;

            boolean wetTriple = SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                    || SpellCombatReactionEngine.hasActiveMutatedHexPublic(target, EssenceType.WATER);
            int stacks = prevStacks + (wetTriple ? 3 : 1);
            data.putLong(TAG_CRYO_RAY_LAST_TICK, now);

            double progressiveSap = Mth.clamp(0.20D + (stacks * 0.05D), 0.20D, 0.88D);
            int slowAmp = Math.min(5, stacks / 3);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, progressiveSap, 24L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 24, slowAmp));
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 50, target.getTicksFrozen() + (wetTriple ? 48 : 18)));

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 18, 0.95F, 0.05F),
                    target.getX(), target.getY(0.65D), target.getZ(), 4, 0.25D, 0.35D, 0.25D, 0.02D
            );

            // 15 consecutive 4-tick action pulses = 60 ticks = exact 3.0 seconds -> complete freeze-stasis!
            if (stacks >= 15) {
                data.putInt(TAG_CRYO_RAY_STACKS, 0);
                applyIceStasis(level, caster, target, 60, 0, spell, slot);
            } else {
                data.putInt(TAG_CRYO_RAY_STACKS, stacks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — FROST NOVA BURST (RADIAL_BURST)
    // =========================================================================

    @Override
    public void onRadialBurstFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 center,
            double blastRadius,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float potencyMult
    ) {
        int tier = spell.getEffectiveTier();
        double effectiveRad = Math.max(6.0D, blastRadius); // Canonical 6-block minimum radius!
        boolean permafrost = hasPermafrostCreep(spell, fallbackMateria);
        BlockState novaIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        // 1. Generous 3D jagged ice-spike nova & snowflake floor seal so the ring can be admired!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, null, (float) effectiveRad, VFX_NOVA_SPIKES_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                center.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, EssenceType.STASIS, (float) (effectiveRad * 0.92D), VFX_NOVA_SEAL_TICKS, tier
        );

        // 2. Freeze all surface liquids (Water -> Packed/Blue Ice, Lava -> Obsidian) & extinguish fires in the 6m+ radius
        BlockPos cPos = BlockPos.containing(center);
        int rInt = (int) Math.ceil(effectiveRad);
        double rSq = effectiveRad * effectiveRad;
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-rInt, -2, -rInt), cPos.offset(rInt, 2, rInt))) {
            double dx = (p.getX() + 0.5D) - center.x;
            double dz = (p.getZ() + 0.5D) - center.z;
            if ((dx * dx + dz * dz) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, novaIce);
            } else if (bs.is(Blocks.LAVA)) {
                level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            }
        }

        // 3. Deal Frozen Materia damage, blast foes backward across the ice, and encase grounded foes in solid 3D Ice Blocks for 2.0s!
        AABB area = new AABB(
                center.x - effectiveRad, center.y - 2.2D, center.z - effectiveRad,
                center.x + effectiveRad, center.y + 3.2D, center.z + effectiveRad
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    foe, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            double dist = Math.sqrt(
                    Math.pow(foe.getX() - center.x, 2) + Math.pow(foe.getZ() - center.z, 2)
            );
            if (dist > effectiveRad + 0.5D) continue;

            if (foe.isOnFire()) {
                foe.clearFire();
            }

            // Linear distance falloff from 100% at epicenter to 33% at outermost perimeter (Section 5.1)
            float distFactor = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, effectiveRad)) * 0.67D, 0.33D, 1.0D);
            if (!slotHasHarmEffect(slot) || dist > blastRadius) {
                float baseDmg = slotHasHarmEffect(slot)
                        ? MateriaStageTable.getBaseCombatDamageHp(tier) * distFactor * potencyMult
                        : (4.0F + tier * 1.35F) * distFactor * potencyMult;
                SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                        level, foe, caster, baseDmg * spell.flawProfile().getPotencyMultiplier(), permafrost, false
                );
                if (dist > blastRadius) {
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, foe, direction, spell, slot, fallbackMateria, potencyMult * distFactor
                    );
                }
            }

            boolean grounded = isEntityGroundedForFrostNova(level, foe);
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 away = new Vec3(foe.getX() - center.x, 0.0D, foe.getZ() - center.z);
                Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize() : caster.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
                double kb = 0.88D + tier * 0.07D;
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * kb, grounded ? 0.22D : 0.32D, push.z * kb));
            }

            if (grounded) {
                // 5-tick skid window lets the knockback visibly blast foes across the ice before locking solid for a full 2.0s (40t)!
                applyIceStasis(level, caster, foe, 40, 5, spell, slot);
            } else {
                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.60D, 60L);
                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
                foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 80, foe.getTicksFrozen() + 80));
            }
        }

        // Ring of shattering crystal & snowflake plumes
        for (int i = 0; i < 16; i++) {
            double ang = (Math.PI * 2.0D * i) / 16.0D;
            double rx = center.x + Math.cos(ang) * (effectiveRad * 0.78D);
            double rz = center.z + Math.sin(ang) * (effectiveRad * 0.78D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, novaIce),
                    rx, center.y + 0.45D, rz, 4, 0.25D, 0.30D, 0.25D, 0.12D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 36, 1.25F, 0.07F),
                    rx, center.y + 0.55D, rz, 3, 0.20D, 0.35D, 0.20D, 0.03D
            );
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.6D, center.z, 36,
                effectiveRad * 0.48D, 0.45D, effectiveRad * 0.48D, 0.06D);
        level.playSound(null, cPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.15F, 0.88F);
        level.playSound(null, cPos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.00F, 1.18F);
    }

    // =========================================================================
    // FORM MUTATION 4 — FLASH-FREEZE STASIS (TOUCH_STRIKE)
    // =========================================================================

    @Override
    public void onTouchStrikeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            List<LivingEntity> struckTargets,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float potencyMult
    ) {
        int tier = spell.getEffectiveTier();
        boolean permafrost = hasPermafrostCreep(spell, fallbackMateria);
        Vec3 slashOrigin = caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D).add(direction.normalize().scale(0.95D));

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                slashOrigin, direction, EssenceType.FROZEN, EssenceType.STASIS,
                2.55F, VFX_FLASH_SLASH_TICKS, tier
        );

        for (int idx = 0; idx < struckTargets.size(); idx++) {
            LivingEntity target = struckTargets.get(idx);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    target, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            boolean isPrimary = (idx == 0);
            float splashScale = isPrimary ? 1.0F : 0.50F; // Section 4.4: 100% primary, 50% sweeping cleave splash!

            if (target.isOnFire()) {
                target.clearFire();
            }
            if (!slotHasHarmEffect(slot)) {
                float strikeDmg = (3.5F + tier * 1.10F) * potencyMult * splashScale * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, target, caster, strikeDmg, permafrost, true);
            }

            // Flash-Freeze Stasis: 1.5s (30 ticks) solid 3D Ice Block on primary target; 0.75s (15 ticks) on 50% cleave targets!
            int stasisTicks = isPrimary ? 30 : 15;
            applyIceStasis(level, caster, target, stasisTicks, 0, spell, slot);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — GLACIAL SPIKES RETALIATION (AURA)
    // =========================================================================

    @Override
    public void onAuraFormCast(
            ServerLevel level,
            LivingEntity caster,
            double auraRadius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float potencyMult
    ) {
        int tier = spell.getEffectiveTier();
        int duration = 200 + (tier * 40);
        long until = level.getGameTime() + duration;

        // Glacial Spikes Retaliation: registers defense as Frozen (+1 vs Water -> 0.50x, -1 vs Nether/Undead -> 1.50x),
        // stabs melee attackers with reactive 3D ice spikes & cuts player and mob attack speed by 30%!
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_FROZEN_SPIKES_UNTIL, until);
        data.putInt("SpellFrozenSpikesTier", tier);
        caster.setTicksFrozen(0);
        if (caster.isOnFire()) {
            caster.clearFire();
        }

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                caster.position().add(0.0D, 0.03D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, null, (float) Math.max(2.1D, auraRadius * 0.70D), VFX_NOVA_SEAL_TICKS, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 36, 1.15F, 0.06F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 18, 0.48D, 0.55D, 0.48D, 0.04D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.9D), caster.getZ(),
                16, 0.45D, 0.50D, 0.45D, 0.03D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.28F);
        level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.75F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 6 — RIME PITFALL (GLYPH)
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
            float potencyMult
    ) {
        int tier = spell.getEffectiveTier();
        double trapRad = Math.max(2.0D, radius);
        BlockPos surfaceCenter = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 anchoredCenter = new Vec3(surfaceCenter.getX() + 0.5D, surfaceCenter.getY() + 1.02D, surfaceCenter.getZ() + 0.5D);

        // Snapshot original 3x3 floor blocks so safe disarm (or untripped expiry) restores terrain cleanly,
        // and place fragile hairline-cracked FROSTED_ICE (AGE = 1) as hollow rime ice!
        Map<BlockPos, BlockState> originalSurface = new HashMap<>();
        List<BlockPos> crustPositions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floorPos = surfaceCenter.offset(dx, 0, dz);
                BlockState orig = level.getBlockState(floorPos);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, floorPos, orig)) continue;
                float hardness = orig.getDestroySpeed(level, floorPos);
                if (hardness < 0.0F || hardness > (6.0F + tier * 5.0F)) continue;
                if (!orig.isAir() || level.getBlockState(floorPos.below()).isSolidRender()) {
                    originalSurface.put(floorPos.immutable(), orig);
                    crustPositions.add(floorPos.immutable());
                    level.setBlockAndUpdate(floorPos, Blocks.FROSTED_ICE.defaultBlockState().setValue(FrostedIceBlock.AGE, 1));
                }
            }
        }

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                anchoredCenter, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, null, (float) trapRad, 56, tier
        );
        level.playSound(null, surfaceCenter, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.90F, 1.30F);

        final boolean repeatingSentry = "repeating".equalsIgnoreCase(slot.formPart().subMode())
                || "sentry".equalsIgnoreCase(slot.formPart().subMode());
        final int maxCharges = repeatingSentry ? 3 : 1;
        final float chargePotencyScale = repeatingSentry ? 0.60F : 1.0F;
        int maxTicks = tier >= 5 ? 72_000 : 12_000; // Section 4.6: 10 minutes (indefinite at Tier 5+)
        final int[] chargesUsed = {0};
        final int[] rearmCooldownUntil = {20}; // Section 4.6: 1.0-second (20-tick) arming delay!
        final boolean[] pitExcavated = {false};
        final boolean silkDropPit = slot.primaryEffect().effect() == SpellCoreEffect.TRANSMUTE
                || slot.primaryEffect().effect() == SpellCoreEffect.BREAK
                || slot.secondaryEffect().map(b -> b.effect() == SpellCoreEffect.TRANSMUTE || b.effect() == SpellCoreEffect.BREAK).orElse(false);

        SpellLingeringZoneManager.ActiveSpellZone pitZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, trapRad, maxTicks, level.getGameTime(), spell, slot, fallbackMateria, potencyMult,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        // Restore intact crust if trap expired without ever collapsing
                        if (!pitExcavated[0]) {
                            restoreOriginalPitSurface(srv, originalSurface);
                        } else {
                            for (BlockPos cp : crustPositions) {
                                if (srv.getBlockState(cp).is(Blocks.FROSTED_ICE)) {
                                    srv.removeBlock(cp, false);
                                }
                            }
                        }
                        return false;
                    }

                    // Keep hollow Frosted Ice stabilized at hairline crack stage (AGE = 1) so sunlight never melts it into flooding water!
                    if (elapsed % 10 == 0 && elapsed >= rearmCooldownUntil[0] - 5) {
                        for (BlockPos cp : crustPositions) {
                            BlockState cur = srv.getBlockState(cp);
                            if (cur.is(Blocks.FROSTED_ICE) && cur.getValue(FrostedIceBlock.AGE) != 1) {
                                srv.setBlock(cp, cur.setValue(FrostedIceBlock.AGE, 1), Block.UPDATE_CLIENTS);
                            } else if (pitExcavated[0] && cur.isAir() && elapsed == rearmCooldownUntil[0]) {
                                srv.setBlockAndUpdate(cp, Blocks.FROSTED_ICE.defaultBlockState().setValue(FrostedIceBlock.AGE, 1));
                            }
                        }
                    }

                    if (elapsed < rearmCooldownUntil[0]) {
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(
                            zone.center.x - 1.65D, zone.center.y - 0.55D, zone.center.z - 1.65D,
                            zone.center.x + 1.65D, zone.center.y + 1.75D, zone.center.z + 1.65D
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner);
                    victims.removeIf(v -> owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                            v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard()));
                    if (victims.isEmpty()) {
                        return true;
                    }

                    // COLLAPSE! Shatter the 3x3 hollow rime ice crust and excavate the 3-block-deep pit beneath!
                    for (BlockPos cp : crustPositions) {
                        BlockState cur = srv.getBlockState(cp);
                        if (cur.is(Blocks.FROSTED_ICE)) {
                            srv.removeBlock(cp, false);
                            BlockState origFloor = originalSurface.get(cp);
                            if (!pitExcavated[0] && origFloor != null && !origFloor.isAir()
                                    && !origFloor.is(Blocks.WATER) && !origFloor.is(Blocks.LAVA)) {
                                dropExcavatedPitBlock(srv, owner, cp, origFloor, silkDropPit);
                            }
                        }
                        if (!pitExcavated[0]) {
                            for (int depth = 1; depth <= 3; depth++) {
                                BlockPos sub = cp.below(depth);
                                BlockState subState = srv.getBlockState(sub);
                                if (subState.isAir() || subState.is(Blocks.WATER) || subState.is(Blocks.LAVA)) continue;
                                if (SpellExecutionEngine.isProtectedBlockOrContainer(srv, sub, subState)) continue;
                                float h = subState.getDestroySpeed(srv, sub);
                                if (h >= 0.0F && h <= (5.0F + tier * 5.0F)) {
                                    dropExcavatedPitBlock(srv, owner, sub, subState, silkDropPit);
                                    srv.removeBlock(sub, false);
                                }
                            }
                        }
                    }
                    pitExcavated[0] = true;

                    boolean permafrost = zone.spell != null && hasPermafrostCreep(zone.spell, zone.fallbackMateria);
                    float effPot = zone.apparatusPotency * chargePotencyScale;
                    for (LivingEntity v : victims) {
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, effPot
                            );
                        }
                        if (zone.slot == null || !slotHasHarmEffect(zone.slot)) {
                            SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                                    srv, v, owner, (4.0F + tier * 1.25F) * effPot, permafrost, false
                            );
                        }
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.60D, 80L);
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3));
                        v.setTicksFrozen(Math.min(v.getTicksRequiredToFreeze() + 80, v.getTicksFrozen() + 80));
                        if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                            SpellExecutionEngine.applySyncedMotion(v, new Vec3(0.0D, -0.72D, 0.0D));
                        }
                    }

                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                            zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.FROZEN, null, (float) Math.max(1.9D, trapRad), VFX_PITFALL_COLLAPSE_TICKS, tier
                    );
                    srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                            zone.center.x, zone.center.y - 0.1D, zone.center.z, 40, 1.15D, 0.45D, 1.15D, 0.16D);
                    srv.sendParticles(ParticleTypes.SNOWFLAKE,
                            zone.center.x, zone.center.y + 0.2D, zone.center.z, 24, 1.10D, 0.50D, 1.10D, 0.05D);
                    srv.playSound(null, surfaceCenter, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.20F, 0.85F);
                    srv.playSound(null, surfaceCenter, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.90F, 1.10F);

                    chargesUsed[0]++;
                    if (chargesUsed[0] >= maxCharges) {
                        return false;
                    }
                    // Repeating Glyphic Sentry: 2.0-second (40-tick) re-arm delay, then re-crusts the pit mouth!
                    rearmCooldownUntil[0] = elapsed + 40;
                    for (BlockPos cp : crustPositions) {
                        if (srv.getBlockState(cp).isAir()) {
                            srv.setBlockAndUpdate(cp, Blocks.FROSTED_ICE.defaultBlockState().setValue(FrostedIceBlock.AGE, 1));
                        }
                    }
                    return true;
                }
        ).markDisarmableGlyph()
                .withOnDisarm((srv, z) -> {
                    if (!pitExcavated[0]) {
                        restoreOriginalPitSurface(srv, originalSurface);
                    } else {
                        for (BlockPos cp : crustPositions) {
                            if (srv.getBlockState(cp).is(Blocks.FROSTED_ICE)) {
                                srv.removeBlock(cp, false);
                            }
                        }
                    }
                });

        SpellLingeringZoneManager.registerZone(level, pitZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — HOARFROST FLASH-FREEZE (CONE)
    // =========================================================================

    @Override
    public void onConeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double coneRange,
            List<LivingEntity> struckTargets,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float potencyMult
    ) {
        int tier = spell.getEffectiveTier();
        double reach = Math.max(10.0D, coneRange); // Canonical 10-block Hoarfrost cone!
        boolean permafrost = hasPermafrostCreep(spell, fallbackMateria);
        BlockState causewayIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        Vec3 coneOrigin = caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 flatForward = new Vec3(normDir.x, 0.0D, normDir.z);
        if (flatForward.lengthSqr() > 1.0E-4D) {
            flatForward = flatForward.normalize();
        } else {
            flatForward = caster.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        }

        // Generous 56-tick 3D swirling hoarfrost fan cone
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                coneOrigin, normDir, EssenceType.FROZEN, null,
                (float) reach, VFX_HOARFROST_CONE_TICKS, tier
        );

        // Sweep full 60-degree horizontal fan (half-angle = 30 deg -> cos(30) = 0.866, with generous edge feathering 0.82)
        // across dy = +2..-4 so aiming across pools from banks or ledges freezes gap-free walkable causeways!
        BlockPos originBlock = caster.blockPosition();
        int rCeil = (int) Math.ceil(reach);
        double rSq = reach * reach;
        List<BlockPos> transientRimeSnow = new ArrayList<>();
        int frozenBlocks = 0;

        for (int dx = -rCeil; dx <= rCeil; dx++) {
            for (int dz = -rCeil; dz <= rCeil; dz++) {
                double horizSq = (double) (dx * dx + dz * dz);
                if (horizSq < 0.5D || horizSq > rSq) continue;
                Vec3 horizVec = new Vec3(dx, 0.0D, dz).normalize();
                if (horizVec.dot(flatForward) < 0.82D) continue; // Authentic 60-degree fan!

                for (int dy = 2; dy >= -4; dy--) {
                    BlockPos p = originBlock.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof BaseFireBlock) {
                        level.removeBlock(p, false);
                        continue;
                    }
                    if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                        level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                        continue;
                    }
                    if (bs.is(Blocks.WATER)) {
                        level.setBlockAndUpdate(p, causewayIce);
                        BlockPos below = p.below();
                        if (level.getBlockState(below).is(Blocks.WATER)) {
                            level.setBlockAndUpdate(below, causewayIce);
                        }
                        frozenBlocks++;
                        break;
                    } else if (bs.is(Blocks.LAVA)) {
                        level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                        frozenBlocks++;
                        break;
                    } else if (permafrost && (bs.is(Blocks.ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.FROSTED_ICE))) {
                        level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                        frozenBlocks++;
                        break;
                    } else if (bs.isSolidRender() && level.getBlockState(p.above()).isAir()) {
                        // Section 4.7 Environmental Coating: dust dry ground in transient hoarfrost rime snow!
                        BlockPos snowPos = p.above();
                        BlockState snowState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1);
                        if (transientRimeSnow.size() < 48 && snowState.canSurvive(level, snowPos) && ( (dx + dz) & 1 ) == 0) {
                            level.setBlockAndUpdate(snowPos, snowState);
                            transientRimeSnow.add(snowPos.immutable());
                        }
                        break;
                    }
                }
            }
        }

        // Sublimate transient rime snow layers cleanly after 7 seconds so player builds stay tidy
        if (!transientRimeSnow.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, caster.position(), 1.0D, 140, level.getGameTime(), null, null, EssenceType.FROZEN, 1.0F,
                    (srv, z, el) -> {
                        if (el < z.maxDurationTicks) return true;
                        for (BlockPos sp : transientRimeSnow) {
                            BlockState cur = srv.getBlockState(sp);
                            if (cur.is(Blocks.SNOW) && cur.getValue(SnowLayerBlock.LAYERS) == 1) {
                                srv.removeBlock(sp, false);
                            }
                        }
                        return false;
                    }
            ));
        }

        for (LivingEntity target : struckTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    target, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (target.isOnFire()) {
                target.clearFire();
            }
            if (!slotHasHarmEffect(slot)) {
                float coneDmg = (3.2F + tier * 1.05F) * potencyMult * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, target, caster, coneDmg, permafrost, false);
            }
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.60D, 75L);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 75, 3));
            boolean wetTriple = SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                    || SpellCombatReactionEngine.hasActiveMutatedHexPublic(target, EssenceType.WATER);
            int addFreeze = (wetTriple ? 3 : 1) * 75;
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 90, target.getTicksFrozen() + addFreeze));
        }

        Vec3 midCone = coneOrigin.add(normDir.scale(reach * 0.50D));
        level.sendParticles(ParticleTypes.SNOWFLAKE, midCone.x, midCone.y, midCone.z,
                32, reach * 0.28D, 0.55D, reach * 0.28D, 0.06D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), DIAMOND_FROST_HEX_COLOR, 34, 1.15F, 0.06F),
                midCone.x, midCone.y, midCone.z, 18, reach * 0.25D, 0.45D, reach * 0.25D, 0.04D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.95F, 0.88F);
        if (frozenBlocks > 0) {
            level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.90F, 1.28F);
        }
    }

    // =========================================================================
    // FORM MUTATION 8 — COLOSSAL GLACIAL MONOLITH (SKYFALL)
    // =========================================================================

    @Override
    public boolean onSkyfallFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 center,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float potencyMult
    ) {
        int tier = spell.getEffectiveTier();
        double impactRad = Math.max(4.5D, radius);
        BlockPos groundSurface = findGroundSurfaceNear(level, BlockPos.containing(center));
        Vec3 impactCenter = new Vec3(center.x, groundSurface.getY() + 1.0D, center.z);
        double riftClearance = probeOverheadClearance(level, BlockPos.containing(impactCenter), 12);

        // Broadcast 110-tick Colossal Glacial Monolith Sky-Rift & Ground Target Seal
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                impactCenter, new Vec3(0.0D, -1.0D, 0.0D),
                EssenceType.FROZEN, null, (float) impactRad, VFX_MONOLITH_TOTAL_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                impactCenter.add(0.0D, 0.03D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, EssenceType.STASIS, (float) impactRad, VFX_MONOLITH_TOTAL_TICKS, tier
        );
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.90F, 1.42F);

        final boolean[] crashed = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, impactRad, VFX_MONOLITH_TOTAL_TICKS, level.getGameTime(), spell, slot, fallbackMateria, potencyMult,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    // Phase 1 (0..39 ticks = 2.0s warning delay): overhead Snowflake Rift materializes & colossal Blue-Ice Monolith plummets!
                    if (elapsed < 40) {
                        double dropFrac = elapsed / 40.0D;
                        double spireY = zone.center.y + riftClearance * (1.0D - dropFrac * dropFrac);
                        if (elapsed % 3 == 0) {
                            srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                    zone.center.x, spireY, zone.center.z, 10, 0.95D, 0.70D, 0.95D, 0.04D);
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), PERMAFROST_BLUE_HEX_COLOR, 24, 1.30F, 0.06F),
                                    zone.center.x, spireY, zone.center.z, 6, 0.70D, 0.65D, 0.70D, 0.03D
                            );
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.RUNE_ISA.get(), DIAMOND_FROST_HEX_COLOR, 22, 1.10F, 0.05F),
                                    zone.center.x, zone.center.y + 0.25D, zone.center.z, 3, impactRad * 0.38D, 0.1D, impactRad * 0.38D, 0.01D
                            );
                        }
                        if (elapsed == 20) {
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.72F);
                        }
                        return true;
                    }

                    // Phase 2 (Tick 40 = exact 2.0s crash!): crush impact zone, freeze liquids, and raise permanent Blue-Ice Monument!
                    if (!crashed[0]) {
                        crashed[0] = true;
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean permafrost = zone.spell != null && hasPermafrostCreep(zone.spell, zone.fallbackMateria);

                        // Freeze surface water & lava across impact zone first
                        BlockPos base = BlockPos.containing(zone.center);
                        int rInt = (int) Math.ceil(zone.radius);
                        double rSq = zone.radius * zone.radius;
                        for (BlockPos p : BlockPos.betweenClosed(base.offset(-rInt, -2, -rInt), base.offset(rInt, 1, rInt))) {
                            double dx = (p.getX() + 0.5D) - zone.center.x;
                            double dz = (p.getZ() + 0.5D) - zone.center.z;
                            double dSq = dx * dx + dz * dz;
                            if (dSq > rSq) continue;
                            BlockState bs = srv.getBlockState(p);
                            if (bs.is(Blocks.WATER)) {
                                srv.setBlockAndUpdate(p, (permafrost || dSq <= 9.0D)
                                        ? Blocks.BLUE_ICE.defaultBlockState()
                                        : Blocks.PACKED_ICE.defaultBlockState());
                            } else if (bs.is(Blocks.LAVA)) {
                                srv.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                            } else if (bs.getBlock() instanceof BaseFireBlock) {
                                srv.removeBlock(p, false);
                            }
                        }

                        // Crush entities in the impact zone (linear falloff 100% -> 35%) and nudge center entities clear of the physical spire
                        AABB crushBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, crushBox, LivingEntity::isAlive)) {
                            // Nudge any entity standing inside the 1x1 central spire column slightly outward so no one suffocates in Blue Ice!
                            double cdx = foe.getX() - (base.getX() + 0.5D);
                            double cdz = foe.getZ() - (base.getZ() + 0.5D);
                            if ((cdx * cdx + cdz * cdz) < 1.35D) {
                                Vec3 nudge = (cdx * cdx + cdz * cdz) > 1.0E-4D
                                        ? new Vec3(cdx, 0.0D, cdz).normalize().scale(1.65D)
                                        : new Vec3(1.65D, 0.0D, 0.0D);
                                foe.teleportTo(base.getX() + 0.5D + nudge.x, foe.getY() + 0.1D, base.getZ() + 0.5D + nudge.z);
                            }

                            if (foe == owner) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    foe, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }

                            double dist = Math.sqrt(Math.pow(foe.getX() - zone.center.x, 2) + Math.pow(foe.getZ() - zone.center.z, 2));
                            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, zone.radius)) * 0.65D, 0.35D, 1.0D);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * falloff
                                );
                            }
                            float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                            float crushDmg = (12.0F + tier * 5.0F) * falloff * zone.apparatusPotency * flawPot;
                            SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(srv, foe, owner, crushDmg, permafrost, true);

                            if (dist <= zone.radius * 0.65D && zone.spell != null && zone.slot != null) {
                                applyIceStasis(srv, owner, foe, 50, 0, zone.spell, zone.slot);
                            } else {
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 80L);
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3));
                                foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 90, foe.getTicksFrozen() + 90));
                                if (!SpellCombatReactionEngine.isBossEntity(srv, foe)) {
                                    Vec3 push = new Vec3(foe.getX() - zone.center.x, 0.0D, foe.getZ() - zone.center.z);
                                    Vec3 pNorm = push.lengthSqr() > 1.0E-4D ? push.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(pNorm.x * 0.85D, 0.30D, pNorm.z * 0.85D));
                                }
                            }
                        }

                        // Sculpt permanent decorative Blue-Ice & Packed-Ice Monolith Spire (clamped safely to cave ceiling!)
                        int maxSpireH = Math.clamp((int) Math.floor(riftClearance) - 1, 3, Math.min(9, 4 + tier / 2));
                        sculptGlacialMonument(srv, base, maxSpireH);

                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                                zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.FROZEN, EssenceType.STASIS, (float) zone.radius, 64, tier
                        );
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                                zone.center.x, zone.center.y + 1.2D, zone.center.z, 60, 1.6D, 1.5D, 1.6D, 0.22D);
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 1.0D, zone.center.z, 42, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.08D);
                        srv.playSound(null, base, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.05F, 1.18F);
                        srv.playSound(null, base, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.30F, 0.72F);
                        srv.playSound(null, base, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.10F, 0.90F);
                        return true;
                    }

                    // Phase 3 (41..110 ticks): lingering diamond-frost mist drifting off the newly risen Blue-Ice Monument!
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 1.8D, zone.center.z,
                                6, zone.radius * 0.35D, 1.3D, zone.radius * 0.35D, 0.02D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 28, 1.05F, 0.04F),
                                zone.center.x, zone.center.y + 1.5D, zone.center.z,
                                4, 0.85D, 1.2D, 0.85D, 0.02D
                        );
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — GLACIAL EIDOLON SUITE (AGENT / SENTRY / DECOY)
    // =========================================================================

    @Override
    public void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double summonRadius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SUMMON_PILLAR,
                spawnPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, null, (float) Math.max(1.85D, summonRadius * 0.65D), VFX_SUMMON_PILLAR_TICKS, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 36, 1.20F, 0.07F),
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 22, 0.45D, 0.65D, 0.45D, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_ISA.get(), DIAMOND_FROST_HEX_COLOR, 34, 1.15F, 0.05F),
                spawnPos.x, spawnPos.y + 1.10D, spawnPos.z, 8, 0.40D, 0.50D, 0.40D, 0.02D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z,
                16, 0.45D, 0.55D, 0.45D, 0.03D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.32F);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.80F, 1.38F);
    }

    // =========================================================================
    // CORE EFFECT SYNERGIES — BLINK, TRANSMUTE / BREAK & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        boolean permafrost = SpellExecutionEngine.hasPrimedFrozenPermafrostCreep(caster);
        BlockState padIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        // Freeze water underfoot at both departure & arrival so blinking onto water forms an instant ice pad!
        freezeLiquidsInRadius(level, BlockPos.containing(departurePos).below(), 1, padIce, true);
        freezeLiquidsInRadius(level, BlockPos.containing(arrivalPos).below(), 1, padIce, true);

        if (caster.isOnFire()) {
            caster.clearFire();
        }

        // Frostwalker Stasis: spawn an authentic 3D Crystalline Player Ice Statue Decoy at departurePos!
        // Mirrors caster armor/held gear inside a 4-slab translucent crystalline ice prism, taunts monsters for 6.0s,
        // and shatters into a 4-block Frost Nova upon destruction or expiry!
        float statueHp = 16.0F + potencyTier * 4.0F;
        SpellEidolonEntity.spawnShortLivedDecoy(
                level,
                caster,
                departurePos,
                EssenceType.FROZEN,
                statueHp,
                120,
                "§bFrostwalker Ice Statue",
                null
        );

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                departurePos.add(0.0D, 0.03D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, EssenceType.STASIS, 1.65F, 56, potencyTier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_ISA.get(), DIAMOND_FROST_HEX_COLOR, 32, 1.15F, 0.05F),
                departurePos.x, departurePos.y + 1.0D, departurePos.z, 8, 0.35D, 0.55D, 0.35D, 0.02D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 28, 1.05F, 0.06F),
                arrivalPos.x, arrivalPos.y + 0.95D, arrivalPos.z, 14, 0.35D, 0.55D, 0.35D, 0.03D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z,
                12, 0.35D, 0.50D, 0.35D, 0.03D);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.90F, 1.35F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.48F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Silk Touch & Permafrost (Sections 2.3, 5.5 & 6.6):
        // 1. Freeze Lava -> Obsidian, Magma Block -> Basalt, Water -> Packed Ice, Snow -> Packed Ice, Ice/Packed Ice -> Blue Ice
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.85F, 1.25F);
            return true;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.BASALT.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.75F, 1.30F);
            return true;
        }
        if (state.getBlock() instanceof BaseFireBlock) {
            level.removeBlock(pos, false);
            return true;
        }
        if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.70F, 1.35F);
            return true;
        }
        if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.85F, 1.35F);
            return true;
        }
        if (state.is(Blocks.PACKED_ICE) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
            level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.45F);
            return true;
        }
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.75F, 1.40F);
            return true;
        }

        // 2. Guaranteed Silk Touch block extraction on Silk-Touch-sensitive / crystalline / ore blocks!
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (isSilkTouchValuableBlock(state)) {
            float maxHardness = 4.0F + potencyTier * 6.0F;
            return harvestBlockWithSilkTouchAndSealLiquids(
                    level, caster, pos, state, maxHardness, true, null, null
            );
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack stack = itemEntity.getItem();
        int count = stack.getCount();
        if (stack.is(Items.SNOWBALL)) {
            int blocks = count / 4;
            if (blocks > 0) {
                itemEntity.setItem(new ItemStack(Items.PACKED_ICE, blocks));
                return true;
            }
            itemEntity.setItem(new ItemStack(Items.ICE, count));
            return true;
        }
        if (stack.is(Items.SNOW_BLOCK) || stack.is(Items.SNOW)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, count));
            return true;
        }
        if (stack.is(Items.POWDER_SNOW_BUCKET) || stack.is(Items.WATER_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, count));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                    new ItemStack(Items.BUCKET, count)));
            return true;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.OBSIDIAN, count));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                    new ItemStack(Items.BUCKET, count)));
            return true;
        }
        if (stack.is(Items.ICE)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, count));
            return true;
        }
        if (stack.is(Items.PACKED_ICE)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, count));
            return true;
        }
        if (stack.is(Items.MAGMA_CREAM)) {
            itemEntity.setItem(new ItemStack(Items.SLIME_BALL, count));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Permafrost Stasis Hex (Section 6.6):
        // Permanently slows movement by 60% (real attribute sap + Slowness IV), locks sprinting (also enforced every tick),
        // +50% physical/blunt shatter damage amplification, cryogenic Frozen Materia DoT;
        // immune to milk buckets, cleansed by fire reactions or Restoration magic!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.setSprinting(false);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.60D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 3, true, false));
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 50);

        SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                level, target, null, (1.25F + potencyTier * 0.50F) * mult, false, true
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 28, 1.05F, 0.06F),
                target.getX(), target.getY(0.65D), target.getZ(), 5, 0.28D, 0.38D, 0.28D, 0.02D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_ISA.get(), DIAMOND_FROST_HEX_COLOR, 26, 0.95F, 0.04F),
                target.getX(), target.getY(0.90D), target.getZ(), 2, 0.22D, 0.25D, 0.22D, 0.01D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.75D), target.getZ(),
                6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    // =========================================================================
    // PUBLIC SHARED HELPERS (ENTITY-TRACKED 3D ICE STASIS & SILK TOUCH SEALING)
    // =========================================================================

    /**
     * Harvests {@code pos} with guaranteed <b>Silk Touch</b> AND automatically freezes any exposed adjacent
     * water/lava pockets into {@code Packed Ice} ({@code Blue Ice} with <i>Permafrost Creep</i>) / {@code Obsidian}
     * so underwater and sub-lava excavation automatically seals leaks (Sections 2.2, 2.3 &amp; 6.6).
     */
    public static boolean harvestBlockWithSilkTouchAndSealLiquids(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            float maxHardness,
            boolean mutatedIceQuality,
            @Nullable SpellAssembly spell,
            @Nullable EssenceType fallbackMateria
    ) {
        boolean permafrost = spell != null && hasPermafrostCreep(spell, fallbackMateria);
        BlockState sealIce = permafrost
                ? Blocks.BLUE_ICE.defaultBlockState()
                : (mutatedIceQuality ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState());

        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, sealIce);
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.75F, 1.35F);
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.80F, 1.25F);
            return true;
        }
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > maxHardness) {
            return false;
        }

        ItemStack silkTool = createSilkTouchPickaxe(level);
        Block.dropResources(state, level, pos, level.getBlockEntity(pos), caster, silkTool);
        level.removeBlock(pos, false);

        // Guide Sections 2.2 & 2.3: automatically seal any exposed adjacent water/lava pockets!
        boolean sealedFluid = false;
        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.relative(dir);
            BlockState adjState = level.getBlockState(adj);
            if (adjState.is(Blocks.WATER)) {
                level.setBlockAndUpdate(adj, sealIce);
                sealedFluid = true;
            } else if (adjState.is(Blocks.LAVA)) {
                level.setBlockAndUpdate(adj, Blocks.OBSIDIAN.defaultBlockState());
                sealedFluid = true;
            }
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), DIAMOND_FROST_HEX_COLOR, 28, 1.05F, 0.08F),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.30D, 0.30D, 0.30D, 0.03D
        );
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.80F, 1.38F);
        if (sealedFluid) {
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.75F, 1.32F);
        }
        return true;
    }

    /**
     * Convenience overload with {@code skidTicks = 0} (immediate solid lock).
     */
    public static void applyIceStasis(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            int ticks,
            @Nullable SpellAssembly spell,
            @Nullable SubSpellSlot slot
    ) {
        applyIceStasis(level, caster, target, ticks, 0, spell, slot);
    }

    /**
     * Encases {@code target} inside an entity-tracked 3D Solid Crystalline Ice Block
     * ({@code primary = FROZEN, secondary = STASIS} via {@link SpellCombatReactionEngine#syncEntityBarrierState}),
     * completely halting movement, rotation, bow/item usage, and outgoing attacks ({@code TAG_FROZEN_STASIS_UNTIL})
     * without placing suffocating opaque blocks inside the entity's hitbox.
     *
     * @param skidTicks optional brief knockback-skid window (e.g. 5 ticks on <i>Frost Nova Burst</i>) before velocity locks to zero.
     */
    public static void applyIceStasis(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            int ticks,
            int skidTicks,
            @Nullable SpellAssembly spell,
            @Nullable SubSpellSlot slot
    ) {
        if (!target.isAlive()) {
            return;
        }
        boolean isBoss = SpellCombatReactionEngine.isBossEntity(level, target);
        int effectiveStasisTicks = isBoss ? Math.min(ticks, 20) : Math.max(10, ticks);
        int effectiveSkidTicks = isBoss ? 0 : Math.max(0, skidTicks);
        int totalTicks = effectiveStasisTicks + effectiveSkidTicks;

        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL,
                Math.max(now + totalTicks, data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)));
        if (effectiveSkidTicks > 0) {
            data.putLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_SKID_UNTIL, now + effectiveSkidTicks);
        } else {
            data.remove(SpellLingeringZoneManager.TAG_FROZEN_STASIS_SKID_UNTIL);
            target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
            target.hasImpulse = true;
        }
        data.putBoolean(SpellLingeringZoneManager.TAG_FROZEN_STASIS_SHATTER_PENDING, true);
        data.putFloat(SpellLingeringZoneManager.TAG_FROZEN_STASIS_YAW, target.getYRot());
        data.putFloat(SpellLingeringZoneManager.TAG_FROZEN_STASIS_PITCH, target.getXRot());

        target.setSprinting(false);
        if (target.isUsingItem()) {
            target.stopUsingItem();
        }
        if (target.isOnFire()) {
            target.clearFire();
        }
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setJumping(false);
            mob.setAggressive(false);
            if (mob instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
            }
        }

        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), totalTicks, 0));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, totalTicks, 6));
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.95D, totalTicks);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + totalTicks);

        // Sync entity-tracked 3D Translucent Solid Ice Block Encasement to all nearby clients!
        Vec3 hitOrigin = caster != null ? caster.getBoundingBox().getCenter() : null;
        SpellCombatReactionEngine.syncEntityBarrierState(level, target, 0.85F, hitOrigin);

        // Freeze any water directly beneath the encased target so they stand on solid Packed Ice
        freezeLiquidsInRadius(level, target.blockPosition().below(), 1, Blocks.PACKED_ICE.defaultBlockState(), false);

        double sx = Math.max(0.30D, target.getBbWidth() * 0.45D);
        double sy = Math.max(0.40D, target.getBbHeight() * 0.45D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 32, 1.15F, 0.05F),
                target.getX(), target.getY(0.5D), target.getZ(), 14, sx, sy, sx, 0.03D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                target.getX(), target.getY(0.55D), target.getZ(), 12, sx, sy, sx, 0.03D);
        level.playSound(null, target.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.95F, 1.38F);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.70F, 1.52F);
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    public static boolean hasPermafrostCreep(SpellAssembly spell, @Nullable EssenceType fallbackMateria) {
        return spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && spell.flowPart().mutationEnabled()
                && spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria) == EssenceType.FROZEN;
    }

    private static boolean slotHasHarmEffect(SubSpellSlot slot) {
        return slot.hasHarmVerb();
    }

    private static ItemStack createSilkTouchPickaxe(ServerLevel level) {
        ItemStack silkTool = new ItemStack(Items.NETHERITE_PICKAXE);
        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.SILK_TOUCH)
                .ifPresent(holder -> silkTool.enchant(holder, 1));
        return silkTool;
    }

    private static boolean isSilkTouchValuableBlock(BlockState state) {
        Block b = state.getBlock();
        return b instanceof TransparentBlock
                || b instanceof StainedGlassBlock
                || b instanceof StainedGlassPaneBlock
                || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.TINTED_GLASS)
                || state.is(Blocks.SEA_LANTERN)
                || state.is(Blocks.GLOWSTONE)
                || state.is(Blocks.AMETHYST_CLUSTER)
                || state.is(Blocks.LARGE_AMETHYST_BUD)
                || state.is(Blocks.MEDIUM_AMETHYST_BUD)
                || state.is(Blocks.SMALL_AMETHYST_BUD)
                || state.is(Blocks.BUDDING_AMETHYST)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.CRIMSON_NYLIUM)
                || state.is(Blocks.WARPED_NYLIUM)
                || state.is(Blocks.SCULK)
                || state.is(Blocks.SCULK_VEIN)
                || state.is(Blocks.SCULK_CATALYST)
                || state.is(Blocks.SCULK_SENSOR)
                || state.is(Blocks.BOOKSHELF)
                || state.is(Blocks.ENDER_CHEST)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.CORALS)
                || state.is(BlockTags.CORAL_BLOCKS)
                || state.is(BlockTags.WALL_CORALS)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.ICE);
    }

    private static void freezeLiquidsInRadius(
            ServerLevel level,
            BlockPos center,
            int radius,
            BlockState iceState,
            boolean douseFires
    ) {
        double rSq = (radius + 0.5D) * (radius + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, iceState);
            } else if (bs.is(Blocks.LAVA)) {
                level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
            } else if (douseFires && bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (douseFires && bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            }
        }
    }

    private static boolean isEntityGroundedForFrostNova(ServerLevel level, LivingEntity entity) {
        if (entity.onGround()) {
            return true;
        }
        BlockPos below = BlockPos.containing(entity.getX(), entity.getY() - 0.55D, entity.getZ());
        BlockState bs = level.getBlockState(below);
        return !bs.isAir() && (!bs.getCollisionShape(level, below).isEmpty() || bs.is(Blocks.WATER) || bs.is(Blocks.LAVA));
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        BlockPos cursor = start;
        for (int dy = 1; dy >= -4; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty())
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return cursor.below();
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

    private static void sculptGlacialMonument(ServerLevel level, BlockPos base, int spireHeight) {
        // Central tapering Blue-Ice monolith column
        for (int y = 0; y < spireHeight; y++) {
            BlockPos p = base.above(y);
            if (canPlaceMonumentIceAt(level, p)) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
            } else if (y > 1) {
                break;
            }
        }
        // 4 cardinal crystalline buttresses (2–3 blocks tall) + 4 lower diagonal rime spurs
        int buttressH = Math.max(2, spireHeight / 2);
        for (Direction horiz : Direction.Plane.HORIZONTAL) {
            for (int y = 0; y < buttressH; y++) {
                BlockPos bp = base.relative(horiz).above(y);
                if (canPlaceMonumentIceAt(level, bp)) {
                    level.setBlockAndUpdate(bp, y == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                }
            }
        }
        int[][] diags = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : diags) {
            BlockPos dp = base.offset(d[0], 0, d[1]);
            if (canPlaceMonumentIceAt(level, dp)) {
                level.setBlockAndUpdate(dp, Blocks.PACKED_ICE.defaultBlockState());
            }
        }
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

    private static void restoreOriginalPitSurface(ServerLevel level, Map<BlockPos, BlockState> originalSurface) {
        for (Map.Entry<BlockPos, BlockState> entry : originalSurface.entrySet()) {
            BlockPos p = entry.getKey();
            if (level.getBlockState(p).is(Blocks.FROSTED_ICE)) {
                level.setBlockAndUpdate(p, entry.getValue());
            }
        }
    }

    private static void dropExcavatedPitBlock(
            ServerLevel level,
            @Nullable LivingEntity owner,
            BlockPos pos,
            BlockState state,
            boolean silkTouch
    ) {
        ItemStack tool = silkTouch ? createSilkTouchPickaxe(level) : new ItemStack(Items.DIAMOND_PICKAXE);
        Block.dropResources(state, level, pos, level.getBlockEntity(pos), owner, tool);
    }
}
