package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Complete per-part Signature Mutation implementation for <b>Frozen Materia (Base Type #5)</b>
 * (Spellcrafting System Guide Sections 1.2A, 2.2, 2.3, 3.8, 4.1–4.9, 5.1–5.8 &amp; 6.6):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>WATER (+1)</b> | Weak against <b>NETHER (-1)</b> and <b>UNDEAD (-1)</b>
 *     (all movement slows apply exact non-compounded speed reductions via {@code applyTimedMovementSpeedSap} alone).</li>
 *     <li><b>Projectile</b>: <i>Glacial Ice-Shard</i> — aerodynamic zero-drop 3D crystalline frost needle that shatters on impact,
 *     dealing Frozen Materia damage (+1 vs Water, -1 vs Nether/Undead), inflicting exact 60% slowness for 3.0s, and freezing water into packed ice and lava into obsidian.</li>
 *     <li><b>Beam</b>: <i>Absolute-Zero Cryo-Ray</i> — continuous beam of sub-zero frost stacking progressive non-compounded slowness (up to 60%) into
 *     complete freeze-stasis at 3.0s dry (1.0s–1.5s wet; 1.0s boss cap) while freezing water and lava under the beam into walkable ice and obsidian bridges.</li>
 *     <li><b>Radial Burst</b>: <i>Frost Nova Burst</i> — detonates in a circular 6.0-block ring of jagged 3D ice spikes, dealing Frozen Materia damage,
 *     encasing grounded foes inside real temporary {@code Ice} blocks for exact 2.0s (40t; 1.0s boss cap) with clean non-water restoration,
 *     inflicting exact 60% slow on airborne foes, and freezing surface water and lava.</li>
 *     <li><b>Touch / Strike</b>: <i>Flash-Freeze Stasis</i> — converts weapon strike to pure Frozen Materia damage and encases struck primary
 *     and sweeping-cleave targets inside real temporary {@code Ice} blocks for exact 1.5s (30t; 1.0s boss cap) before reverting cleanly.</li>
 *     <li><b>Aura / Self</b>: <i>Glacial Spikes Retaliation</i> — registers defense as Frozen; melee attackers are stabbed by reactive
 *     3D ice spikes, taking retaliatory Frozen Materia damage and suffering an exact 30% attack speed reduction.</li>
 *     <li><b>Glyph / Trap</b>: <i>Glacial Pitfall</i> — turns a 3x3 surface patch into fragile frosted ice that collapses when stepped on into a
 *     temporary non-destructive 3x3x3 pit with Frozen Materia chill damage and exact 50% slow, cleanly restoring original terrain afterward.</li>
 *     <li><b>Cone / Spray</b>: <i>Hoarfrost Flash-Freeze</i> — freezes source and flowing water and lava in a 10-block cone into walkable
 *     packed/blue ice and obsidian causeways, blankets dry ground in permanent natural-melting snow layers, and inflicts frost damage + exact 50% slow.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Colossal Glacial Monolith</i> — summons a tier-scaled 3D pseudoblock glacial spire that plummets after 2.0s,
 *     crushing and stasis-locking across the full tier-scaled circular Skyfall radius before materializing into a permanent layered ice monument.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Glacial Eidolon Suite</i> — cyan/frost (#98DCF2) triad: 3-hit solid-freezing Agent,
 *     exact 60%-slow icicle-dart Sentry, and Decoy-only spherical 4m destruction shatter Ice Effigy.</li>
 *     <li><b>Flow (Slow)</b>: <i>Permafrost Creep</i> — exclusive to Deliberate Slow ({@code SLOW_HEAVY}): permanently freezes water into Blue Ice
 *     and adds +0.4 Materia Advantage score against Water targets.</li>
 *     <li><b>Blink / Step</b>: <i>Frostwalker Stasis</i> — teleports caster safely while freezing a 3x3 water platform underfoot into Packed/Blue Ice
 *     and leaving a 3D Frostwalker Ice Statue decoy at departure that shatters only when destroyed.</li>
 *     <li><b>Transmute &amp; Break</b>: <i>Silk Touch &amp; Permafrost</i> — guaranteed Silk Touch block harvesting on all breakable non-container blocks,
 *     sealing of adjacent water/lava leaks, Lava/Lava Bucket to Obsidian (+ empty Bucket), and full Snow Block / 8-layer Snow / 4-Snowball compaction into Packed Ice.</li>
 *     <li><b>Affliction</b>: <i>Permafrost Stasis Hex</i> — milk-proof cryogenic curse (0 continuous tick damage) inflicting exact 60% slow,
 *     locking sprinting, and increasing blunt/physical/fall/crush shatter damage taken across Levels I–III.</li>
 * </ul>
 */
public final class FrozenSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GLACIAL_EIDOLON_HEX_COLOR = 0x98DCF2;
    /** Marks entities encased inside temporary real {@code Blocks.ICE} so {@code SpellCombatReactionEngine} suppresses vanilla {@code IN_WALL} suffocation. */
    public static final String TAG_FROZEN_ICE_STASIS_UNTIL = "entropica_frozen_ice_stasis_until";

    private static final int DIAMOND_FROST_HEX_COLOR = 0xEBF9FF;
    private static final int PERMAFROST_BLUE_HEX_COLOR = 0x2C82C9;

    /** Persistent NBT keys for tracking consecutive 4-tick Absolute-Zero Cryo-Ray stacks (15 pulses = 3.0s). */
    private static final String TAG_CRYO_RAY_STACKS = "FrozenCryoRayStacks";
    private static final String TAG_CRYO_RAY_LAST_TICK = "FrozenCryoRayLastTick";

    /** Tracks true pre-encasement {@link BlockState}s for active temporary stasis {@code Blocks.ICE} blocks across overlapping casts. */
    private static final Map<GlobalPos, BlockState> ACTIVE_TEMP_STASIS_ICE_ORIGINALS = new ConcurrentHashMap<>();
    private static final Map<GlobalPos, Long> ACTIVE_TEMP_STASIS_ICE_UNTIL = new ConcurrentHashMap<>();

    // Generous 3D VFX durations so players can admire glacial crystal formations in the world
    private static final int VFX_SHARD_SHATTER_TICKS = 56;
    private static final int VFX_NOVA_SPIKES_TICKS = 68;
    private static final int VFX_NOVA_SEAL_TICKS = 64;
    private static final int VFX_FLASH_SLASH_TICKS = 52;
    private static final int VFX_HOARFROST_CONE_TICKS = 56;
    private static final int VFX_PITFALL_COLLAPSE_TICKS = 76;
    private static final int VFX_MONOLITH_TOTAL_TICKS = 110;
    private static final int VFX_SUMMON_PILLAR_TICKS = 60;
    private static final int PITFALL_HOLD_OPEN_TICKS = 100;

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
            default -> null;
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
            case GLYPH -> "Glacial Pitfall";
            case CONE -> "Hoarfrost Flash-Freeze";
            case SKYFALL -> "Colossal Glacial Monolith";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Glacial Ice-Shard: Aerodynamic crystalline frost needle shattering on impact (+1 vs Water, -1 vs Nether/Undead), inflicting an exact 60% slow for 3.0s, and freezing water into Packed Ice and lava into Obsidian.";
            case BEAM -> "Absolute-Zero Cryo-Ray: Sub-zero frost beam stacking progressive non-compounded slowness up to complete freeze-stasis at 3.0s dry (1.0s–1.5s wet; 1.0s boss cap) while freezing water and lava into walkable bridges.";
            case RADIAL_BURST -> "Frost Nova Burst: Circular 6m ring of jagged ice spikes encasing grounded enemies in real temporary Ice blocks for 2.0s (1.0s boss cap), inflicting an exact 60% slow on airborne foes, and freezing surface water and lava.";
            case TOUCH_STRIKE -> "Flash-Freeze Stasis: Purely converts the weapon strike to Frozen Materia damage and encases primary and cleave targets inside real temporary Ice blocks for 1.5s (1.0s boss cap), halting movement and attacks.";
            case AURA -> "Glacial Spikes Retaliation: Registers defense as Frozen; reactive 3D ice spikes stab melee attackers for Frozen Materia damage and cut their attack speed by an exact 30%.";
            case GLYPH -> "Glacial Pitfall: Disarmable 3x3 Frosted Ice floor trap that collapses into a temporary non-destructive 3x3x3 pit with Frozen Materia chill damage and an exact 50% slow before cleanly restoring original terrain.";
            case CONE -> "Hoarfrost Flash-Freeze: Freezes source and flowing water and lava in a 10-block cone into walkable Packed/Blue Ice and Obsidian causeways, leaves permanent natural-melting snow layers, and inflicts frost damage + exact 50% slow.";
            case SKYFALL -> "Colossal Glacial Monolith: Summons a tier-scaled 3D pseudoblock glacial spire that plummets after 2.0s, crushing and stasis-locking across the full tier-scaled Skyfall radius before materializing into a permanent layered ice monument.";
            case SUMMON_AGENT -> "Glacial Eidolon (Agent): Cyan-frost melee companion (#98DCF2) whose chilling strikes freeze enemies solid after 3 consecutive hits.";
            case SUMMON_SENTRY -> "Glacial Eidolon (Sentry): Anchored frost crystal pylon firing rapid zero-drop icicle darts inflicting an exact 60% movement slow.";
            case SUMMON_DECOY -> "Glacial Eidolon (Decoy): Crystalline ice effigy that taunts mobs and shatters into a spherical 4m Frost Nova strictly upon combat destruction, freezing surrounding foes.";
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
            case BLINK -> "Frostwalker Stasis: Teleports caster safely while freezing a 3x3 water platform underfoot into Packed/Blue Ice and leaving a Frostwalker Ice Statue decoy at departure that shatters strictly when destroyed.";
            case TRANSMUTE, BREAK -> "Silk Touch & Permafrost: Unlocks guaranteed Silk Touch block harvesting across all breakable non-container blocks, freezes Lava/Lava Buckets into Obsidian (+ empty Bucket), and compacts full Snow Blocks (1:1), 8-layer Snow (8:1), or 4 Snowballs (4:1) into Packed Ice.";
            case AFFLICTION -> "Permafrost Stasis Hex: Immediate milk-proof cryogenic curse (0 tick damage) inflicting an exact 60% movement slow, locking sprinting, and increasing blunt/physical/fall/crush shatter damage by +50% / +75% / +100% across Levels I–III.";
            default -> null;
        };
    }

    // =========================================================================
    // FLOW MUTATION — PERMAFROST CREEP (SLOW_HEAVY ONLY, +0.4 ADVANTAGE VS WATER)
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
        // Permafrost Creep: creeping wave of heavy frost permanently freezing water (water only, never lava!) into Blue Ice along its path
        if (projectile.tickCount % 2 == 0) {
            BlockPos center = projectile.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -2, -1), center.offset(1, 0, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER) || bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE)) {
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
        // Permafrost Creep: permanently freeze nearby water (water only, never lava) into Blue Ice without dealing an extra hit
        // (+0.4 Materia Advantage score vs Water is applied directly by getFlowBonusAdvantageScore / hurtWithFrozenMateriaAdvantage)
        freezeWaterOnlyToBlueIceInRadius(level, living.blockPosition(), 2);
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
        if (spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY) {
            return;
        }
        freezeWaterOnlyToBlueIceInRadius(level, BlockPos.containing(splitOrigin), 2);
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

        // Freeze nearby water into Packed Ice (or Blue Ice with Permafrost Creep) & lava into Obsidian on impact
        freezeLiquidsInRadius(level, living.blockPosition(), 2, iceBlock, true);

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            // Glacial Ice-Shard: standalone Frozen Materia damage only when no Harm verb is slotted
            if (!hasHarmEffect(spell, slot)) {
                float shardDmg = (2.8F + tier * 0.95F) * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, living, caster, shardDmg, permafrost, false);
            }

            // Exact 60% movement slow for 3.0 seconds (60 ticks) via MOVEMENT_SPEED attribute sap alone (never double-stacked!)
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(living, level, 0.60D, 60L);

            if (living.isOnFire()) {
                living.clearFire();
            }
        }

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

        if (spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }

        // 2. Progressive non-compounded Slowness (up to 60%) stacking into complete freeze-stasis at 3.0s dry (1.0s wet)!
        for (LivingEntity target : struckTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    target, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (target.isOnFire()) {
                target.clearFire();
            }

            if (!hasHarmEffect(spell, slot)) {
                float rayDmg = (1.15F + tier * 0.35F) * potencyMult * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, target, caster, rayDmg, permafrost, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(target);
            long lastHit = data.getLong(TAG_CRYO_RAY_LAST_TICK).orElse(0L);
            int prevStacks = (now - lastHit <= 16L) ? data.getInt(TAG_CRYO_RAY_STACKS).orElse(0) : 0;

            boolean wetAccelerated = SpellLingeringZoneManager.isEntityWetOrSoaked(target)
                    || SpellCombatReactionEngine.hasActiveMutatedHexPublic(target, EssenceType.WATER);
            int stacks = prevStacks + (wetAccelerated ? 3 : 1);
            data.putLong(TAG_CRYO_RAY_LAST_TICK, now);

            // Smooth pre-stasis slowness scaling up to exact 60% via applyTimedMovementSpeedSap alone (no SLOWNESS double-stack!)
            double progressiveSap = Mth.clamp((stacks / 15.0D) * 0.60D, 0.10D, 0.60D);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, progressiveSap, 24L);

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 18, 0.95F, 0.05F),
                    target.getX(), target.getY(0.65D), target.getZ(), 4, 0.25D, 0.35D, 0.25D, 0.02D
            );

            // 15 stacks = 60 ticks (3.0s dry) or 5 pulses = 20 ticks (1.0s wet) -> complete freeze-stasis (bosses cap at 20t)!
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
        double effectiveRad = Math.max(6.0D, blastRadius); // Canonical circular 6.0-block minimum radius!
        boolean permafrost = hasPermafrostCreep(spell, fallbackMateria);
        BlockState novaIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        // 1. Generous 3D jagged ice-spike nova & snowflake floor seal
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

        // 2. Freeze all surface liquids (Water -> Packed/Blue Ice, Lava -> Obsidian) & extinguish fires in the circular 6m+ radius
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

        // 3. Respect Utility zero-harm rules; deal Frozen Materia damage, encase grounded foes in real temporary Ice blocks for exact 2.0s (40t),
        //    and inflict exact 60% slow on airborne foes!
        if (!spell.prefix().enforcesZeroEntityHarm()) {
            AABB area = new AABB(
                    center.x - effectiveRad, center.y - 2.2D, center.z - effectiveRad,
                    center.x + effectiveRad, center.y + 3.2D, center.z + effectiveRad
            );
            boolean hasHarm = hasHarmEffect(spell, slot);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != caster)) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        foe, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                double dx = foe.getX() - center.x;
                double dz = foe.getZ() - center.z;
                double horizDistSq = dx * dx + dz * dz;
                if (horizDistSq > rSq) {
                    continue;
                }
                double dist = Math.sqrt(horizDistSq);

                if (foe.isOnFire()) {
                    foe.clearFire();
                }

                // Linear distance falloff from 100% at epicenter to 33% at outermost perimeter (Section 5.1)
                float distFactor = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, effectiveRad)) * 0.67D, 0.33D, 1.0D);
                if (!hasHarm) {
                    float baseDmg = (4.0F + tier * 1.35F) * distFactor * potencyMult;
                    SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                            level, foe, caster, baseDmg * spell.flawProfile().getPotencyMultiplier(), permafrost, false
                    );
                }
                if (dist > blastRadius) {
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, foe, direction, spell, slot, fallbackMateria, potencyMult * distFactor
                    );
                }

                boolean grounded = isEntityGroundedForFrostNova(level, foe);
                if (grounded) {
                    // Exact 2.0s (40 ticks) real temporary Blocks.ICE encasement (0 skid ticks; bosses cap at 20t)!
                    applyIceStasis(level, caster, foe, 40, 0, spell, slot);
                } else {
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 away = new Vec3(dx, 0.0D, dz);
                        Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize() : caster.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
                        double kb = 0.88D + tier * 0.07D;
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * kb, 0.32D, push.z * kb));
                    }
                    // Airborne foes: exact 60% slow via applyTimedMovementSpeedSap alone (no SLOWNESS double-stack!)
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.60D, 60L);
                }
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
        Vec3 slashOrigin = caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D).add(direction.normalize().scale(0.95D));

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                slashOrigin, direction, EssenceType.FROZEN, EssenceType.STASIS,
                2.55F, VFX_FLASH_SLASH_TICKS, tier
        );

        if (spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }

        // SpellCombatReactionEngine converts the melee weapon strike directly to Frozen Materia damage;
        // encase both primary and sweeping-cleave targets inside real temporary Blocks.ICE for exact 1.5s (30 ticks; bosses cap at 20t)!
        for (LivingEntity target : struckTargets) {
            if (target == null || !target.isAlive() || target == caster) {
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    target, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (target.isOnFire()) {
                target.clearFire();
            }
            applyIceStasis(level, caster, target, 30, 0, spell, slot);
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
        // stabs melee attackers with reactive 3D ice spikes & cuts player and mob attack speed by exact 30%!
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
    // FORM MUTATION 6 — GLACIAL PITFALL (GLYPH)
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

        // Snapshot original 3x3 floor blocks (and later the 3x3x3 pit shaft beneath) for non-destructive restoration,
        // placing fragile hairline-cracked FROSTED_ICE (AGE = 1) as the hollow Glacial Pitfall crust!
        Map<BlockPos, BlockState> originalPitBlocks = new LinkedHashMap<>();
        List<BlockPos> crustPositions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floorPos = surfaceCenter.offset(dx, 0, dz);
                BlockState orig = level.getBlockState(floorPos);
                if (orig.is(Blocks.BEDROCK) || (!orig.isAir() && SpellExecutionEngine.isProtectedBlockOrContainer(level, floorPos, orig))) {
                    continue;
                }
                if (!orig.getFluidState().isEmpty()) {
                    continue;
                }
                float hardness = orig.getDestroySpeed(level, floorPos);
                if (hardness < 0.0F || hardness > (6.0F + tier * 5.0F)) {
                    continue;
                }
                if (!orig.isAir() || level.getBlockState(floorPos.below()).isSolidRender()) {
                    originalPitBlocks.put(floorPos.immutable(), orig);
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
        final int[] pitRestoreAtTick = {-1};

        SpellLingeringZoneManager.ActiveSpellZone pitZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, trapRad, maxTicks, level.getGameTime(), spell, slot, fallbackMateria, potencyMult,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks
                            || (pitRestoreAtTick[0] >= 0 && elapsed >= pitRestoreAtTick[0])) {
                        restoreOriginalPitTerrainAndEjectEntities(srv, surfaceCenter, originalPitBlocks);
                        return false;
                    }

                    // Once all charges have triggered, hold the 3-block-deep pit open until pitRestoreAtTick elapses
                    if (pitRestoreAtTick[0] >= 0) {
                        return true;
                    }

                    // Keep hollow Frosted Ice stabilized at hairline crack stage (AGE = 1) so sunlight never melts it into water!
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

                    // COLLAPSE! Clear the 3x3 Frosted Ice surface and temporary 3x3x3 pit shaft to Blocks.AIR WITHOUT dropping mined items!
                    for (BlockPos cp : crustPositions) {
                        BlockState cur = srv.getBlockState(cp);
                        if (cur.is(Blocks.FROSTED_ICE)) {
                            srv.setBlock(cp, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                        if (!pitExcavated[0]) {
                            for (int depth = 1; depth <= 3; depth++) {
                                BlockPos sub = cp.below(depth);
                                BlockState subState = srv.getBlockState(sub);
                                if (subState.isAir() || subState.is(Blocks.BEDROCK) || !subState.getFluidState().isEmpty()) {
                                    continue;
                                }
                                if (SpellExecutionEngine.isProtectedBlockOrContainer(srv, sub, subState)) {
                                    continue;
                                }
                                float h = subState.getDestroySpeed(srv, sub);
                                if (h >= 0.0F && h <= (5.0F + tier * 5.0F)) {
                                    originalPitBlocks.putIfAbsent(sub.immutable(), subState);
                                    srv.setBlock(sub, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                                }
                            }
                        }
                    }
                    pitExcavated[0] = true;

                    boolean permafrost = zone.spell != null && hasPermafrostCreep(zone.spell, zone.fallbackMateria);
                    boolean allowHarm = zone.spell == null || !zone.spell.prefix().enforcesZeroEntityHarm();
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    float effPot = zone.apparatusPotency * chargePotencyScale;
                    for (LivingEntity v : victims) {
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, effPot
                            );
                        }
                        if (allowHarm) {
                            if (!hasHarmEffect(zone.spell, zone.slot)) {
                                SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                                        srv, v, owner, (3.5F + tier * 1.10F) * effPot * flawPot, permafrost, false
                                );
                            }
                            // Exact 50% movement slow via applyTimedMovementSpeedSap alone (no compounding Slowness III!)
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.50D, 70L);
                        }
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
                        pitRestoreAtTick[0] = elapsed + PITFALL_HOLD_OPEN_TICKS;
                        return true;
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
                .withOnDisarm((srv, z) -> restoreOriginalPitTerrainAndEjectEntities(srv, surfaceCenter, originalPitBlocks));

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

        // Sweep 60-degree horizontal fan across dy = +2..-4: freeze source & flowing Water/Lava into solid bridges,
        // and place permanent Snow layers on dry ground (no temporary timer — melts naturally in warm light/biomes)!
        BlockPos originBlock = caster.blockPosition();
        int rCeil = (int) Math.ceil(reach);
        double rSq = reach * reach;
        int frozenBlocks = 0;

        for (int dx = -rCeil; dx <= rCeil; dx++) {
            for (int dz = -rCeil; dz <= rCeil; dz++) {
                double horizSq = (double) (dx * dx + dz * dz);
                if (horizSq < 0.5D || horizSq > rSq) continue;
                Vec3 horizVec = new Vec3(dx, 0.0D, dz).normalize();
                if (horizVec.dot(flatForward) < 0.82D) continue;

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
                        BlockPos snowPos = p.above();
                        BlockState snowState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1);
                        if (snowState.canSurvive(level, snowPos)) {
                            level.setBlockAndUpdate(snowPos, snowState);
                        }
                        break;
                    }
                }
            }
        }

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            boolean hasHarm = hasHarmEffect(spell, slot);
            for (LivingEntity target : struckTargets) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        target, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                if (target.isOnFire()) {
                    target.clearFire();
                }
                if (!hasHarm) {
                    float coneDmg = (3.2F + tier * 1.05F) * potencyMult * spell.flawProfile().getPotencyMultiplier();
                    SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(level, target, caster, coneDmg, permafrost, false);
                }
                // Exact 50% movement slow via applyTimedMovementSpeedSap alone (no compounding Slowness III!)
                SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.50D, 75L);
            }
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
        double impactRad = Math.max(5.5D, radius); // Full tier-scaled Skyfall zone radius across Tiers 1–10!
        BlockPos groundSurface = findGroundSurfaceNear(level, BlockPos.containing(center));
        BlockPos baseAir = groundSurface.above();
        Vec3 impactCenter = new Vec3(groundSurface.getX() + 0.5D, baseAir.getY(), groundSurface.getZ() + 0.5D);
        double riftClearance = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);

        // Build tier-scaled monolith blueprint & broadcast falling->standing 3D Pseudoblock columns
        // (pseudoblocks stand in place during ticks 40..110; actual permanent layered ice monument materializes when finished!)
        int effectiveMonolithTicks = SpellWorldVfxManager.resolveEffectiveZoneDurationTicks(spell, fallbackMateria, VFX_MONOLITH_TOTAL_TICKS);
        int maxSpireH = Math.clamp((int) Math.floor(riftClearance) - 1, 3, 4 + tier);
        List<int[]> monumentPlan = buildTierScaledGlacialColumns(tier, maxSpireH);
        for (int[] col : monumentPlan) {
            BlockPos colBase = baseAir.offset(col[0], 0, col[1]);
            int colH = col[2];
            BlockState pseudoState = col[3] == 2 ? Blocks.BLUE_ICE.defaultBlockState()
                    : (col[3] == 1 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState());
            Vec3 skyOrigin = new Vec3(colBase.getX() + 0.5D, baseAir.getY() + Math.max(3.5D, riftClearance - colH * 0.45D), colBase.getZ() + 0.5D);
            SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                    level, skyOrigin, colBase, pseudoState, colH,
                    40, Math.max(2, effectiveMonolithTicks - 40), EssenceType.FROZEN, tier
            );
        }

        // Broadcast 110-tick Colossal Glacial Monolith Sky-Rift & Ground Target Seal
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                impactCenter, impactCenter.add(0.0D, riftClearance, 0.0D),
                EssenceType.FROZEN, null, (float) impactRad, VFX_MONOLITH_TOTAL_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                impactCenter.add(0.0D, 0.03D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.FROZEN, EssenceType.STASIS, (float) impactRad, VFX_MONOLITH_TOTAL_TICKS, tier
        );
        level.playSound(null, baseAir, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.90F, 1.42F);

        final boolean[] crashed = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, impactRad, VFX_MONOLITH_TOTAL_TICKS, level.getGameTime(), spell, slot, fallbackMateria, potencyMult,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        // Spell complete! Materialize permanent layered Blue Ice / Packed Ice / Ice monument!
                        materializePlannedGlacialColumns(srv, baseAir, monumentPlan);
                        return false;
                    }

                    // Phase 1 (0..39 ticks = 2.0s warning delay): overhead Snowflake Rift materializes & colossal Pseudoblock Monolith plummets!
                    if (elapsed < 40) {
                        double dropFrac = elapsed / 40.0D;
                        double spireY = zone.center.y + riftClearance * (1.0D - dropFrac * dropFrac);
                        if (elapsed % 3 == 0) {
                            srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                    zone.center.x, spireY, zone.center.z, 10 + tier, 0.95D + tier * 0.12D, 0.70D, 0.95D + tier * 0.12D, 0.04D);
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), PERMAFROST_BLUE_HEX_COLOR, 24, 1.30F, 0.06F),
                                    zone.center.x, spireY, zone.center.z, 6 + tier / 2, 0.70D + tier * 0.10D, 0.65D, 0.70D + tier * 0.10D, 0.03D
                            );
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.RUNE_ISA.get(), DIAMOND_FROST_HEX_COLOR, 22, 1.10F, 0.05F),
                                    zone.center.x, zone.center.y + 0.25D, zone.center.z, 4, impactRad * 0.38D, 0.1D, impactRad * 0.38D, 0.01D
                            );
                        }
                        if (elapsed == 20) {
                            srv.playSound(null, baseAir, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.72F);
                        }
                        return true;
                    }

                    // Phase 2 (Tick 40 = exact 2.0s crash!): crush full tier-scaled impact zone, freeze liquids, and lock pseudoblocks in place!
                    if (!crashed[0]) {
                        crashed[0] = true;
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean permafrost = zone.spell != null && hasPermafrostCreep(zone.spell, zone.fallbackMateria);

                        // Freeze surface water & lava across full impact zone first
                        int rInt = (int) Math.ceil(zone.radius);
                        double rSq = zone.radius * zone.radius;
                        for (BlockPos p : BlockPos.betweenClosed(baseAir.offset(-rInt, -2, -rInt), baseAir.offset(rInt, 1, rInt))) {
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

                        // Push entities clear of the landed pseudoblock monolith footprint
                        for (int[] col : monumentPlan) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, baseAir.offset(col[0], 0, col[1]), col[2], true);
                        }

                        // Crush & stasis-lock entities across the full tier-scaled circular Skyfall zone radius (respecting Utility zero-harm!)
                        boolean allowHarm = zone.spell == null || !zone.spell.prefix().enforcesZeroEntityHarm();
                        AABB crushBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.0D, maxSpireH + 1.5D), zone.center.z + zone.radius
                        );
                        boolean hasHarm = hasHarmEffect(zone.spell, zone.slot);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, crushBox, LivingEntity::isAlive)) {
                            if (foe == owner) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    foe, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }

                            double dx = foe.getX() - zone.center.x;
                            double dz = foe.getZ() - zone.center.z;
                            double distSq = dx * dx + dz * dz;
                            if (distSq > rSq) continue;
                            double dist = Math.sqrt(distSq);
                            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, zone.radius)) * 0.65D, 0.35D, 1.0D);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * falloff * 2.0F
                                );
                            }
                            if (allowHarm) {
                                if (!hasHarm) {
                                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                                    float crushDmg = (12.0F + tier * 5.0F) * falloff * zone.apparatusPotency * flawPot;
                                    SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(srv, foe, owner, crushDmg, permafrost, true);
                                }
                                applyIceStasis(srv, owner, foe, 50, 0, zone.spell, zone.slot);
                            }
                        }

                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                                zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.FROZEN, EssenceType.STASIS, (float) zone.radius, 64, tier
                        );
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                                zone.center.x, zone.center.y + 1.2D, zone.center.z, 60, 1.6D, 1.5D, 1.6D, 0.22D);
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 1.0D, zone.center.z, 42, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.08D);
                        srv.playSound(null, baseAir, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.05F, 1.18F);
                        srv.playSound(null, baseAir, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.30F, 0.72F);
                        srv.playSound(null, baseAir, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.10F, 0.90F);
                        return true;
                    }

                    // Phase 3 (41..109 ticks): maintain pseudoblock obstacle pushout & drift frost mist until real blocks materialize!
                    for (int[] col : monumentPlan) {
                        SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, baseAir.offset(col[0], 0, col[1]), col[2], false);
                    }
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 1.8D, zone.center.z,
                                6 + tier / 2, zone.radius * 0.35D, 1.3D, zone.radius * 0.35D, 0.02D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), GLACIAL_EIDOLON_HEX_COLOR, 28, 1.05F, 0.04F),
                                zone.center.x, zone.center.y + 1.5D, zone.center.z,
                                4 + tier / 2, 0.85D + tier * 0.12D, 1.2D, 0.85D + tier * 0.12D, 0.02D
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
        boolean permafrost = SpellExecutionEngine.hasPrimedFrozenPermafrostCreep(caster)
                || hasActiveSlowFlowOnCaster(caster);
        BlockState padIce = permafrost ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();

        // Freeze a 3x3 platform of WATER ONLY underfoot at both departure & arrival (never lava)!
        freezeWaterPlatform3x3(level, BlockPos.containing(departurePos).below(), padIce);
        freezeWaterPlatform3x3(level, BlockPos.containing(arrivalPos).below(), padIce);

        if (caster.isOnFire()) {
            caster.clearFire();
        }

        // Frostwalker Stasis: spawn a 3D Crystalline Player Ice Statue Decoy at departurePos
        // (shatters into a spherical 4m Frost Nova strictly when attacked/destroyed, dissolving peacefully on expiry!)
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
        // 1. Freeze Lava -> Obsidian, Magma Block -> Basalt, Water -> Packed Ice, full Snow Block / 8-layer Snow -> Packed Ice
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
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.75F, 1.40F);
            return true;
        }
        // Only full Snow Block or 8-layer SnowLayerBlock (LAYERS == 8) compacts 1:1 into Packed Ice!
        if (state.is(Blocks.SNOW_BLOCK)
                || (state.is(Blocks.SNOW) && state.getValue(SnowLayerBlock.LAYERS) == 8)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.85F, 1.35F);
            return true;
        }

        // 2. Guaranteed Silk Touch block harvesting on ALL breakable non-container blocks!
        float maxHardness = 4.0F + potencyTier * 6.0F;
        return harvestBlockWithSilkTouchAndSealLiquids(
                level, caster, pos, state, maxHardness, true, null, null
        );
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

        // 4 Snowballs -> 1 Packed Ice (never 1 Snowball -> 1 Ice!)
        if (stack.is(Items.SNOWBALL)) {
            int blocks = count / 4;
            if (blocks <= 0) {
                return false;
            }
            int remainder = count % 4;
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, blocks));
            if (remainder > 0) {
                level.addFreshEntity(new ItemEntity(
                        level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                        new ItemStack(Items.SNOWBALL, remainder)
                ));
            }
            return true;
        }
        // 1 full Snow Block -> 1 Packed Ice
        if (stack.is(Items.SNOW_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, count));
            return true;
        }
        // 8 Snow layer items -> 1 Packed Ice (never 1 thin snow carpet -> 1 Packed Ice!)
        if (stack.is(Items.SNOW)) {
            int blocks = count / 8;
            if (blocks <= 0) {
                return false;
            }
            int remainder = count % 8;
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, blocks));
            if (remainder > 0) {
                level.addFreshEntity(new ItemEntity(
                        level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                        new ItemStack(Items.SNOW, remainder)
                ));
            }
            return true;
        }
        if (stack.is(Items.POWDER_SNOW_BUCKET) || stack.is(Items.WATER_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, count));
            spawnReturnedEmptyBuckets(level, itemEntity, count);
            return true;
        }
        // Lava Bucket -> Obsidian + 1 empty Bucket per bucket consumed!
        if (stack.is(Items.LAVA_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.OBSIDIAN, count));
            spawnReturnedEmptyBuckets(level, itemEntity, count);
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
        // 0 continuous HP tick damage! Applies exact 60% movement slow via applyTimedMovementSpeedSap alone (no SLOWNESS!)
        // + sprint lock (blunt/physical/fall/crush shatter bonus & milk immunity handled by SpellCombatReactionEngine).
        target.setSprinting(false);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.60D, 45L);

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
    // PUBLIC SHARED HELPERS (REAL TEMPORARY ICE STASIS & SILK TOUCH SEALING)
    // =========================================================================

    /**
     * Harvests {@code pos} with guaranteed <b>Silk Touch</b> across all breakable non-container blocks AND automatically
     * freezes any exposed adjacent water/lava pockets into {@code Packed Ice} ({@code Blue Ice} with <i>Permafrost Creep</i>) /
     * {@code Obsidian} so underwater and sub-lava excavation automatically seals leaks (Sections 2.2, 2.3 &amp; 6.6).
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
        if (isUnbreakableOrProtectedContainer(level, pos, state)) {
            return false;
        }

        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > maxHardness) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack silkTool = createSilkTouchToolForState(level, state);
        if (state.is(Blocks.FROSTED_ICE)) {
            Block.popResource(level, pos, new ItemStack(Items.ICE));
        } else {
            Block.dropResources(state, level, pos, blockEntity, caster, silkTool);
        }
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
     * Convenience overload with {@code skidTicks = 0} (immediate solid lock &amp; temporary {@code Blocks.ICE} encasement).
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
     * Encases {@code target} in real temporary {@code Blocks.ICE} blocks occupying replaceable positions around its
     * bounding box, stamps {@link #TAG_FROZEN_ICE_STASIS_UNTIL} ({@code gameTime + totalTicks}) for {@code IN_WALL}
     * suffocation immunity, completely halts movement/attacks ({@code TAG_FROZEN_STASIS_UNTIL}; capped at 20 ticks on bosses),
     * and cleanly reverts all placed {@code Blocks.ICE} blocks back to their exact original {@link BlockState}
     * (without melting into water) when the stasis timer expires or if broken/cleansed early.
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
        if (spell != null && spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }
        boolean isBoss = SpellCombatReactionEngine.isBossEntity(level, target);
        int effectiveStasisTicks = isBoss ? Math.min(ticks, 20) : Math.max(10, ticks);
        int effectiveSkidTicks = isBoss ? 0 : Math.max(0, skidTicks);
        int totalTicks = effectiveStasisTicks + effectiveSkidTicks;

        long now = level.getGameTime();
        long until = now + totalTicks;
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL,
                Math.max(until, data.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)));
        data.putLong(TAG_FROZEN_ICE_STASIS_UNTIL,
                Math.max(until, data.getLong(TAG_FROZEN_ICE_STASIS_UNTIL).orElse(0L)));

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
        // Use applyTimedMovementSpeedSap alone — never stack MobEffects.SLOWNESS or frost-tick speed modifiers!
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.90D, totalTicks);

        Vec3 hitOrigin = caster != null ? caster.getBoundingBox().getCenter() : null;
        SpellCombatReactionEngine.syncEntityBarrierState(level, target, 0.85F, hitOrigin);

        // Freeze standing water directly beneath the encased target into Packed Ice (water only)
        freezeWaterPlatform3x3(level, target.blockPosition().below(), Blocks.PACKED_ICE.defaultBlockState());

        // Place real temporary Blocks.ICE around target's bounding box and revert cleanly without turning into water!
        scheduleTemporaryStasisIceEncasement(level, caster, target, effectiveSkidTicks, totalTicks, until);

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

    private static boolean hasHarmEffect(@Nullable SpellAssembly spell, @Nullable SubSpellSlot slot) {
        if (slot != null) {
            return slot.hasHarmVerb();
        }
        if (spell != null) {
            for (SubSpellSlot s : spell.subSpells()) {
                if (s.hasHarmVerb()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasActiveSlowFlowOnCaster(LivingEntity caster) {
        return stackHasActiveSlowFlow(caster.getMainHandItem())
                || stackHasActiveSlowFlow(caster.getOffhandItem());
    }

    private static boolean stackHasActiveSlowFlow(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        try {
            CastingApparatusState state = stack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
            if (state == null) {
                return false;
            }
            return state.getActiveSpell()
                    .map(spell -> spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY)
                    .orElse(false);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void scheduleTemporaryStasisIceEncasement(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            int skidTicks,
            int totalTicks,
            long expireTick
    ) {
        final UUID targetId = target.getUUID();
        final List<BlockPos> placedPositions = new ArrayList<>();
        final boolean[] placed = {false};

        if (skidTicks <= 0) {
            placeTemporaryStasisIceAroundTarget(level, target, expireTick, placedPositions);
            placed[0] = true;
            if (placedPositions.isEmpty()) {
                return;
            }
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, target.position(), 1.5D, totalTicks, level.getGameTime(), null, null, EssenceType.FROZEN, 1.0F,
                (srv, zone, elapsed) -> {
                    long curTime = srv.getGameTime();
                    Entity ent = srv.getEntity(targetId);
                    LivingEntity liv = (ent instanceof LivingEntity l && l.isAlive()) ? l : null;
                    boolean stasisClearedEarly = liv == null
                            || EntityHelper.getPersistentData(liv).getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L) <= curTime;

                    if (!placed[0]) {
                        if (stasisClearedEarly) {
                            return false;
                        }
                        if (elapsed < skidTicks) {
                            return true;
                        }
                        placeTemporaryStasisIceAroundTarget(srv, liv, expireTick, placedPositions);
                        placed[0] = true;
                        if (placedPositions.isEmpty()) {
                            return false;
                        }
                    }

                    boolean anyIceBrokenOrMeltedEarly = false;
                    for (BlockPos p : placedPositions) {
                        BlockState curBs = srv.getBlockState(p);
                        if (!curBs.is(Blocks.ICE)) {
                            anyIceBrokenOrMeltedEarly = true;
                            GlobalPos gp = GlobalPos.of(srv.dimension(), p);
                            BlockState orig = ACTIVE_TEMP_STASIS_ICE_ORIGINALS.get(gp);
                            if (orig != null && curBs.is(Blocks.WATER) && !orig.is(Blocks.WATER)) {
                                BlockState restore = orig.canSurvive(srv, p) ? orig : Blocks.AIR.defaultBlockState();
                                srv.setBlock(p, restore, Block.UPDATE_ALL);
                            }
                        }
                    }

                    if (elapsed < zone.maxDurationTicks && !stasisClearedEarly && !anyIceBrokenOrMeltedEarly) {
                        CompoundTag lData = EntityHelper.getPersistentData(liv);
                        lData.putLong(TAG_FROZEN_ICE_STASIS_UNTIL,
                                Math.max(curTime + 2L, lData.getLong(TAG_FROZEN_ICE_STASIS_UNTIL).orElse(0L)));
                        return true;
                    }

                    // Revert temporary Blocks.ICE cleanly back to original BlockState without turning into water!
                    boolean revertedAny = false;
                    for (BlockPos p : placedPositions) {
                        GlobalPos gp = GlobalPos.of(srv.dimension(), p);
                        long lockUntil = ACTIVE_TEMP_STASIS_ICE_UNTIL.getOrDefault(gp, 0L);
                        if (!stasisClearedEarly && !anyIceBrokenOrMeltedEarly && lockUntil > curTime) {
                            continue;
                        }
                        BlockState orig = ACTIVE_TEMP_STASIS_ICE_ORIGINALS.remove(gp);
                        ACTIVE_TEMP_STASIS_ICE_UNTIL.remove(gp);
                        if (orig == null) {
                            continue;
                        }
                        BlockState curBs = srv.getBlockState(p);
                        if (curBs.is(Blocks.ICE) || (curBs.is(Blocks.WATER) && !orig.is(Blocks.WATER))) {
                            BlockState restore = orig.canSurvive(srv, p) ? orig : Blocks.AIR.defaultBlockState();
                            srv.setBlock(p, restore, Block.UPDATE_ALL);
                            revertedAny = true;
                        }
                    }

                    if (anyIceBrokenOrMeltedEarly && liv != null && !stasisClearedEarly) {
                        CompoundTag lData = EntityHelper.getPersistentData(liv);
                        lData.remove(TAG_FROZEN_ICE_STASIS_UNTIL);
                        SpellLingeringZoneManager.clearFrozenStasisAndShatter(srv, liv, lData, null);
                    } else if (liv != null) {
                        CompoundTag lData = EntityHelper.getPersistentData(liv);
                        if (lData.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L) <= curTime) {
                            lData.remove(TAG_FROZEN_ICE_STASIS_UNTIL);
                        }
                    }

                    if (revertedAny && !placedPositions.isEmpty()) {
                        BlockPos soundPos = placedPositions.getFirst();
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                                soundPos.getX() + 0.5D, soundPos.getY() + 0.8D, soundPos.getZ() + 0.5D,
                                16, 0.35D, 0.45D, 0.35D, 0.12D);
                        srv.playSound(null, soundPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.80F, 1.30F);
                    }
                    return false;
                }
        ));
    }

    private static void placeTemporaryStasisIceAroundTarget(
            ServerLevel level,
            LivingEntity target,
            long expireTick,
            List<BlockPos> placedPositions
    ) {
        AABB bb = target.getBoundingBox();
        BlockPos centerPos = target.blockPosition();
        int minX = Math.max(centerPos.getX() - 2, Mth.floor(bb.minX + 0.02D));
        int maxX = Math.min(centerPos.getX() + 2, Mth.floor(bb.maxX - 0.02D));
        int minY = Math.max(centerPos.getY(), Mth.floor(bb.minY + 0.02D));
        int maxY = Math.min(centerPos.getY() + 3, Mth.floor(bb.maxY - 0.02D));
        int minZ = Math.max(centerPos.getZ() - 2, Mth.floor(bb.minZ + 0.02D));
        int maxZ = Math.min(centerPos.getZ() + 2, Mth.floor(bb.maxZ - 0.02D));

        for (BlockPos p : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockPos imm = p.immutable();
            GlobalPos gp = GlobalPos.of(level.dimension(), imm);
            BlockState cur = level.getBlockState(imm);

            if (ACTIVE_TEMP_STASIS_ICE_ORIGINALS.containsKey(gp)) {
                ACTIVE_TEMP_STASIS_ICE_UNTIL.merge(gp, expireTick, Math::max);
                if (!cur.is(Blocks.ICE) && isReplaceableForTemporaryStasisIce(level, imm, cur)) {
                    level.setBlock(imm, Blocks.ICE.defaultBlockState(), Block.UPDATE_ALL);
                }
                placedPositions.add(imm);
            } else if (isReplaceableForTemporaryStasisIce(level, imm, cur)) {
                BlockState snapshot = (cur.getBlock() instanceof BaseFireBlock)
                        ? Blocks.AIR.defaultBlockState()
                        : cur;
                ACTIVE_TEMP_STASIS_ICE_ORIGINALS.put(gp, snapshot);
                ACTIVE_TEMP_STASIS_ICE_UNTIL.put(gp, expireTick);
                level.setBlock(imm, Blocks.ICE.defaultBlockState(), Block.UPDATE_ALL);
                placedPositions.add(imm);
            }
        }
    }

    private static boolean isReplaceableForTemporaryStasisIce(ServerLevel level, BlockPos pos, BlockState cur) {
        if (cur.hasBlockEntity() || level.getBlockEntity(pos) != null) {
            return false;
        }
        if (!cur.getFluidState().isEmpty()) {
            return false;
        }
        if (cur.isAir() || cur.is(Blocks.SNOW)) {
            return true;
        }
        if (cur.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        return cur.canBeReplaced() || cur.is(BlockTags.REPLACEABLE);
    }

    private static boolean isUnbreakableOrProtectedContainer(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (state.hasBlockEntity() || be != null) {
            if (be instanceof Container) {
                return true;
            }
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (!"minecraft".equals(key.getNamespace())) {
                return true;
            }
            return !(state.is(BlockTags.CAMPFIRES)
                    || state.is(BlockTags.BEEHIVES)
                    || state.is(Blocks.ENDER_CHEST)
                    || state.is(Blocks.SCULK_CATALYST)
                    || state.is(Blocks.SCULK_SENSOR)
                    || state.is(Blocks.CALIBRATED_SCULK_SENSOR)
                    || state.is(Blocks.SCULK_SHRIEKER));
        }
        return false;
    }

    private static ItemStack createSilkTouchToolForState(ServerLevel level, BlockState state) {
        ItemStack silkTool;
        if (state.is(Blocks.COBWEB)
                || state.is(Blocks.VINE)
                || state.is(Blocks.GLOW_LICHEN)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.SEAGRASS)) {
            silkTool = new ItemStack(Items.SHEARS);
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            silkTool = new ItemStack(Items.NETHERITE_SHOVEL);
        } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            silkTool = new ItemStack(Items.NETHERITE_AXE);
        } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            silkTool = new ItemStack(Items.NETHERITE_HOE);
        } else {
            silkTool = new ItemStack(Items.NETHERITE_PICKAXE);
        }
        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.SILK_TOUCH)
                .ifPresent(holder -> silkTool.enchant(holder, 1));
        return silkTool;
    }

    private static void spawnReturnedEmptyBuckets(ServerLevel level, ItemEntity origin, int count) {
        int remaining = count;
        while (remaining > 0) {
            int batch = Math.min(16, remaining);
            level.addFreshEntity(new ItemEntity(
                    level, origin.getX(), origin.getY(), origin.getZ(),
                    new ItemStack(Items.BUCKET, batch)
            ));
            remaining -= batch;
        }
    }

    private static void freezeWaterOnlyToBlueIceInRadius(ServerLevel level, BlockPos center, int radius) {
        double rSq = (radius + 0.5D) * (radius + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER) || bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE)) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
            }
        }
    }

    private static void freezeWaterPlatform3x3(ServerLevel level, BlockPos underCenter, BlockState padIce) {
        boolean upgradeToBlue = padIce.is(Blocks.BLUE_ICE);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy >= -1; dy--) {
                    BlockPos p = underCenter.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(p);
                    if (bs.is(Blocks.WATER)) {
                        level.setBlockAndUpdate(p, padIce);
                    } else if (upgradeToBlue && (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE))) {
                        level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                    }
                }
            }
        }
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

    /**
     * Builds a tier-scaled monolith blueprint {@code [dx, dz, height, materialKind]} ({@code 2 = BLUE_ICE, 1 = PACKED_ICE, 0 = ICE})
     * scaling smoothly in both footprint and height across Tiers 1–10:
     * 3x3 spire (Tiers 1–2) -> 5x5 spire (Tiers 3–5) -> 7x7 spire (Tiers 6–8) -> 9x9 cathedral monolith (Tiers 9–10).
     */
    private static List<int[]> buildTierScaledGlacialColumns(int tier, int spireHeight) {
        List<int[]> cols = new ArrayList<>();
        int h = Math.max(3, spireHeight);
        cols.add(new int[]{0, 0, h, 2});

        int cardH = Math.max(2, (int) Math.round(h * 0.65D));
        cols.add(new int[]{1, 0, cardH, 2});
        cols.add(new int[]{-1, 0, cardH, 2});
        cols.add(new int[]{0, 1, cardH, 2});
        cols.add(new int[]{0, -1, cardH, 2});

        int diagH = Math.max(1, (int) Math.round(h * 0.42D));
        int[][] diags = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : diags) {
            cols.add(new int[]{d[0], d[1], diagH, 1});
        }

        if (tier >= 3) {
            int outerCardH = Math.max(1, (int) Math.round(h * 0.36D));
            cols.add(new int[]{2, 0, outerCardH, 1});
            cols.add(new int[]{-2, 0, outerCardH, 1});
            cols.add(new int[]{0, 2, outerCardH, 1});
            cols.add(new int[]{0, -2, outerCardH, 1});
            if (tier >= 5) {
                int knightH = Math.max(1, (int) Math.round(h * 0.25D));
                int[][] knights = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};
                for (int[] k : knights) {
                    cols.add(new int[]{k[0], k[1], knightH, 1});
                }
            }
        }
        if (tier >= 7) {
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
        if (tier >= 9) {
            int buttressH = Math.max(1, (int) Math.round(h * 0.18D));
            cols.add(new int[]{4, 0, buttressH, 0});
            cols.add(new int[]{-4, 0, buttressH, 0});
            cols.add(new int[]{0, 4, buttressH, 0});
            cols.add(new int[]{0, -4, buttressH, 0});
        }
        return cols;
    }

    private static void materializePlannedGlacialColumns(ServerLevel level, BlockPos base, List<int[]> cols) {
        for (int[] col : cols) {
            BlockPos colBase = base.offset(col[0], 0, col[1]);
            int colH = col[2];
            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(level, colBase, colH, true);
            BlockState state = col[3] == 2 ? Blocks.BLUE_ICE.defaultBlockState()
                    : (col[3] == 1 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState());
            for (int y = 0; y < colH; y++) {
                BlockPos p = colBase.above(y);
                if (canPlaceMonumentIceAt(level, p)) {
                    level.setBlockAndUpdate(p, state);
                } else if (y > 1) {
                    break;
                }
            }
        }
        level.playSound(null, base, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.05F, 0.85F);
    }

    private static boolean canPlaceMonumentIceAt(ServerLevel level, BlockPos pos) {
        BlockState cur = level.getBlockState(pos);
        if (!cur.isAir() && SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, cur)) {
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

    private static void restoreOriginalPitTerrainAndEjectEntities(
            ServerLevel level,
            BlockPos surfaceCenter,
            Map<BlockPos, BlockState> originalBlocks
    ) {
        if (originalBlocks.isEmpty()) {
            return;
        }

        // Nudge any entities still inside the 3x3x3 pit shaft safely upward onto the restored surface so nothing suffocates!
        double safeSurfaceY = surfaceCenter.getY() + 1.05D;
        AABB shaftBox = new AABB(
                surfaceCenter.getX() - 1.25D, surfaceCenter.getY() - 3.35D, surfaceCenter.getZ() - 1.25D,
                surfaceCenter.getX() + 2.25D, surfaceCenter.getY() + 1.02D, surfaceCenter.getZ() + 2.25D
        );
        List<Entity> trappedEntities = level.getEntitiesOfClass(Entity.class, shaftBox, Entity::isAlive);
        for (Entity ent : trappedEntities) {
            ent.teleportTo(ent.getX(), Math.max(ent.getY(), safeSurfaceY), ent.getZ());
            ent.resetFallDistance();
            Vec3 vel = ent.getDeltaMovement();
            if (vel.y < 0.0D) {
                ent.setDeltaMovement(vel.x, 0.05D, vel.z);
                ent.hasImpulse = true;
            }
        }

        // Restore all snapshotted shaft & surface BlockStates cleanly without overwriting any player containers/builds placed mid-spell
        for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
            BlockPos p = entry.getKey();
            BlockState orig = entry.getValue();
            BlockState cur = level.getBlockState(p);
            if (!cur.hasBlockEntity() && level.getBlockEntity(p) == null
                    && (cur.isAir() || cur.is(Blocks.FROSTED_ICE) || cur.canBeReplaced())) {
                level.setBlockAndUpdate(p, orig);
            }
        }
    }
}
