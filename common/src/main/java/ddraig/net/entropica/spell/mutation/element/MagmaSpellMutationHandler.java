package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellTriggerType;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Magma Materia (Tier 1 Elemental Fusion #13: Earth + Nether)</b>
 * (Spellcrafting System Guide Sections 1.2, 2.2, 4.1–4.9, 5.1–5.6 &amp; 6.14).
 *
 * <p>Canonical Advantage: <b>Strong against FROZEN (+1) and NATURE (+1)</b> |
 * <b>Weak against AIR (-1) and WATER (-1)</b>
 * (Dominance: {@code magma_earth_dom} computed automatically by {@code VisInteractionManager};
 * Tier 1 Elemental Fusion: {@code 10%} base physical armor bypass &amp; {@code 1} base armor shred point).</p>
 */
public final class MagmaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int MAGMA_BRUTE_HEX_COLOR = 0xB83C08;
    public static final int MAGMA_INCANDESCENT_GOLD_HEX = 0xFFD15C;
    public static final int MAGMA_OBSIDIAN_CRUST_HEX = 0x2A1712;

    private static final String TAG_REPEATING_PULSES_LEFT = "SpellMagmaGlyphPulsesLeft";
    private static final String TAG_REPEATING_COOLDOWN_UNTIL = "SpellMagmaGlyphCooldownUntil";

    @Override
    public EssenceType materiaType() {
        return EssenceType.MAGMA;
    }

    // =========================================================================
    // CANONICAL TITLES & DESCRIPTIONS (GUIDE SECTION 6.14)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, OCTUPLE_DOME -> "Pyroclastic Split Flow";
            default -> "Pyroclastic " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, OCTUPLE_DOME ->
                    "When bifurcating or splitting into multiple projectiles, each split point erupts in a volcanic pop that sets adjacent enemies ablaze and auto-smelts nearby loose items.";
            default ->
                    "Infuses spell flow with pressurized #B83C08 volcanic magma and molten slag cinders.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Molten Magma Boulder";
            case BEAM -> "Pyroclastic Lavastream";
            case RADIAL_BURST -> "Caldera Shockwave Nova";
            case TOUCH_STRIKE -> "Volcanic Slag Cleave";
            case AURA -> "Igneous Armor Mantle";
            case GLYPH -> "Volcanic Vent Geyser Trap";
            case CONE -> "Pyroclastic Surge";
            case SKYFALL -> "Volcanic Meteorite Bombardment";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Lobs a heavy 3D fractured magma boulder (#B83C08) with a slight gravity arc; on impact, bursts into molten lava splatter, sets entities ablaze for 6s, and leaves a temporary 1-block magma crust on the ground.";
            case BEAM ->
                    "Channels a sustained torrent of liquid magma (#B83C08) that ignites enemies in unquenchable fire, melts ice or snow into water or steam, bores a 1x1 tunnel through stone and dirt, and auto-smelts mined raw ores into ingots.";
            case RADIAL_BURST ->
                    "Detonates in an explosive 360-degree volcanic eruption (6-block radius) that knocks mobs backward, sets enemies ablaze, and turns floor blocks in the radius into contained lava and magma blocks at random.";
            case TOUCH_STRIKE ->
                    "Melee strike coats the target in molten slag, setting them ablaze for 6s, reducing target Armor Toughness by 30% for 4s, and automatically smelting drops if the target is slain.";
            case AURA ->
                    "Coats the caster in thick obsidian/pumice plates granting +4 Armor, 50% knockback resistance, and 100% fire/lava immunity (-10% move speed), while radiating heat in a 4-block radius that melts snow/ice and vaporizes water into steam.";
            case GLYPH ->
                    "Surface-snapped volcanic vent trap (1.0s arming, 50% Sneak-Right-Click disarm refund) that erupts in a vertical column of molten lava and steam for 3s, launching the triggering mob 8 blocks upward, igniting them, and turning a 3x3 floor into contained lava.";
            case CONE ->
                    "Sprays a wide 60-degree cone of molten lava splatter and burning pumice; pushes enemies backward, ignites struck mobs in fire, turns water sources into obsidian and flowing water into cobblestone, and instantly melts snow and ice.";
            case SKYFALL ->
                    "Calls down a cave-ready barrage of exploding molten magma boulders over 6s across a 12-block area; repeatedly staggers mobs with heavy impact impulses, sets the zone ablaze, vaporizes water pools, and auto-smelts dropped raw items.";
            case SUMMON_AGENT ->
                    "Summons a molten volcanic red-orange (#B83C08) Magma-Brute Agent with lava drip particles that delivers ground-shaking magma punches and leaves burning magma footprints.";
            case SUMMON_SENTRY ->
                    "Anchors a Volcanic Magma Mortar (#B83C08) that lobs arcing molten magma boulders at range.";
            case SUMMON_DECOY ->
                    "Conjures a glowing Caldera Nova Effigy (#B83C08) that taunts mobs, burns melee attackers, and detonates into an explosive 6-block Caldera Shockwave Nova upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Caldera Step";
            case TRANSMUTE, BREAK -> "Igneous Forging";
            case AFFLICTION -> "Molten Slag Encasement Hex";
            case KINETIC -> "Volcanic Eruption Blast";
            default -> "Volcanic " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports the caster leaving a temporary pool of molten lava at departure, and arrives with an explosive volcanic fire shockwave.";
            case TRANSMUTE, BREAK ->
                    "Auto-smelts raw ores into ingots, turns water sources into solid obsidian and flowing water into magma blocks, and melts stone blocks into magma.";
            case AFFLICTION ->
                    "Persistent milk-immune volcanic slag curse (cleansed via Water/Restoration magic or caster defeat): permanently sets the target ablaze in unquenchable fire even underwater, reduces Armor Toughness by 40%, and cuts movement speed by 30%.";
            case KINETIC ->
                    "Unleashes a massive concussive upward volcanic blast, launching mobs 12 blocks airborne.";
            default ->
                    "Infuses the spell effect with Tier 1 Fusion Magma Materia (+1 vs Frozen & Nature, -1 vs Air & Water, 10% armor bypass).";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — PYROCLASTIC SPLIT FLOW
    // =========================================================================

    @Override
    public void onFlowMultiCastOrigin(
            ServerLevel level,
            LivingEntity caster,
            SpellFlowModifier flow,
            int branchCount,
            SpellAssembly spell
    ) {
        // Section 6.14 Pyroclastic Split Flow:
        // When bifurcating or splitting into multiple projectiles, each split point erupts in a volcanic pop
        // that sets adjacent enemies ablaze!
        if (branchCount >= 2
                || flow == SpellFlowModifier.DOUBLE_BIFURCATE
                || flow == SpellFlowModifier.TRIPLE_TRIFURCATE
                || flow == SpellFlowModifier.OCTUPLE_DOME) {
            Vec3 muzzle = caster.position().add(0.0D, caster.getBbHeight() * 0.62D, 0.0D)
                    .add(caster.getLookAngle().normalize().scale(0.55D));
            eruptVolcanicSplitPop(level, caster, muzzle, spell);
        }
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
        SpellFlowModifier flow = spell.flowPart().flow();
        if (flow.getCarrierPayloadCount() >= 2
                || flow == SpellFlowModifier.DOUBLE_BIFURCATE
                || flow == SpellFlowModifier.TRIPLE_TRIFURCATE
                || flow == SpellFlowModifier.OCTUPLE_DOME
                || spell.flowPart().trigger() == SpellTriggerType.ON_TIMER) {
            eruptVolcanicSplitPop(level, caster, splitOrigin, spell);
        }
    }

    private static void eruptVolcanicSplitPop(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafeguard = spell.hasWardFriendlyFireSafeguard();
        float popDmg = (2.2F + tier * 0.55F) * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.MAGMA, EssenceType.EARTH, 1.25F, tier
        );
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z, 16, 0.45D, 0.35D, 0.45D, 0.08D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MAGMA_BRUTE_HEX_COLOR, 22, 0.95F, 0.06F),
                center.x, center.y, center.z, 10, 0.40D, 0.30D, 0.40D, 0.04D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.78F, 0.82F);

        AABB box = new AABB(
                center.x - 2.8D, center.y - 1.5D, center.z - 2.8D,
                center.x + 2.8D, center.y + 2.2D, center.z + 2.8D
        );
        NetherSpellMutationHandler.autoSmeltDroppedItems(level, box);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || foe.position().distanceTo(center) > 3.0D) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafeguard)) {
                continue;
            }
            foe.igniteForSeconds(5);
            if (!spell.prefix().enforcesZeroEntityHarm()) {
                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(level, foe, caster, popDmg, false, true);
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 MAGMA FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.14 Molten Magma Boulder: lobbed projectile with slight gravity arc!
        projectile.setNoGravity(false);
        Vec3 v = projectile.getDeltaMovement();
        projectile.setDeltaMovement(v.x * 0.94D, v.y + 0.13D, v.z * 0.94D);
        level.sendParticles(ParticleTypes.LAVA, projectile.getX(), projectile.getY(), projectile.getZ(),
                8, 0.14D, 0.14D, 0.14D, 0.05D);
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.70F, 0.76F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.DRIPPING_LAVA, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.10D, 0.10D, 0.10D, 0.01D);
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.08D, 0.08D, 0.08D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), MAGMA_BRUTE_HEX_COLOR, 16, 0.88F, 0.08F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    1, 0.08D, 0.08D, 0.08D, 0.01D
            );
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
        BlockPos groundCrust = findGroundSurfaceNear(level, BlockPos.containing(hitEntity.position()));
        splatterMoltenBoulder(
                level, caster, hitEntity.getBoundingBox().getCenter(), new Vec3(0.0D, 1.0D, 0.0D),
                groundCrust, spell, slot
        );
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
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        splatterMoltenBoulder(
                level, caster, blockHit.getLocation(), normal,
                blockHit.getBlockPos(), spell, slot
        );
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        BlockPos groundCrust = findGroundSurfaceNear(level, BlockPos.containing(projectile.position()));
        splatterMoltenBoulder(
                level, caster, projectile.position(), new Vec3(0.0D, 1.0D, 0.0D),
                groundCrust, spell, slot
        );
    }

    private static void splatterMoltenBoulder(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            Vec3 normal,
            BlockPos crustPos,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafeguard = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float splashDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * 0.48F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center, normal, EssenceType.MAGMA, EssenceType.EARTH, 1.45F, tier
        );
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.2D, center.z,
                18, 0.48D, 0.36D, 0.48D, 0.08D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                center.x, center.y + 0.15D, center.z, 14, 0.42D, 0.30D, 0.42D, 0.12D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.65F, 0.88F);
        alertHostilesToVolcanicBlast(level, caster, center, spell);

        AABB box = new AABB(
                center.x - 2.6D, center.y - 1.5D, center.z - 2.6D,
                center.x + 2.6D, center.y + 2.2D, center.z + 2.6D
        );
        NetherSpellMutationHandler.autoSmeltDroppedItems(level, box);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (victim == caster || victim.position().distanceTo(center) > 2.85D) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, wardSafeguard)) {
                continue;
            }
            victim.igniteForSeconds(6);
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(level, victim, caster, splashDmg, false, true);
            }
        }

        // Leave a temporary 1-block magma crust on the hit ground (restoring cleanly after 80 ticks = 4.0s)!
        BlockState prev = level.getBlockState(crustPos);
        if ((prev.isSolidRender() || prev.is(Blocks.WATER))
                && !prev.is(Blocks.MAGMA_BLOCK)
                && !prev.is(Blocks.OBSIDIAN)
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, crustPos, prev)) {
            BlockPos targetPos = crustPos.immutable();
            level.setBlockAndUpdate(targetPos, Blocks.MAGMA_BLOCK.defaultBlockState());
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, Vec3.atCenterOf(targetPos), 1.4D, 80, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) {
                            return true;
                        }
                        if (srv.getBlockState(targetPos).is(Blocks.MAGMA_BLOCK)) {
                            srv.setBlockAndUpdate(targetPos, prev);
                        }
                        return false;
                    }
            ));
        }
    }

    // =========================================================================
    // FORM MUTATION 2 — PYROCLASTIC LAVASTREAM (BEAM)
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
        // Section 6.14 Pyroclastic Lavastream (#B83C08):
        // - Sustained torrent of liquid magma igniting enemies in unquenchable fire
        // - Melts ice or snow blocks along its path into water or steam
        // - Bores a 1x1 hole through stone and dirt
        // - Auto-smelts any mined raw ores into ingots!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float beamDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.25F, apparatusPotency) * 0.65F * flawPot;

        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition();
        Vec3 muzzle = eye.subtract(0.0D, 0.18D, 0.0D).add(normDir.scale(0.38D));

        // 1. Ignite struck entities in unquenchable fire & apply Magma Materia damage if HARM is not already slotted
        Set<UUID> seen = new HashSet<>();
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.igniteForSeconds(6);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, now + 80L);
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(level, hit, caster, beamDmg, false, false);
            }
        }

        // 2. Bore a 1x1 tunnel through stone, dirt, and ores along the ray, melting ice/snow & auto-smelting mined raw ores!
        double maxBoreDist = Math.min(8.0D + tier * 2.5D, maxReach);
        ItemStack pickaxeContext = new ItemStack(Items.NETHERITE_PICKAXE);
        Set<BlockPos> visitedBlocks = new HashSet<>();
        double furthestVisualDist = blockHit.getType() == HitResult.Type.BLOCK
                ? eye.distanceTo(blockHit.getLocation())
                : maxReach;

        int blocksBoredThisPulse = 0;
        int maxBorePerPulse = 4 + tier; // Smooth progressive boring while channeling without lag spikes!
        for (double dist = 0.85D; dist <= maxBoreDist; dist += 0.65D) {
            BlockPos p = BlockPos.containing(eye.add(normDir.scale(dist)));
            if (!visitedBlocks.add(p.immutable())) continue;

            BlockState bs = level.getBlockState(p);
            if (bs.isAir()) continue;
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                furthestVisualDist = Math.min(furthestVisualDist, dist);
                break;
            }

            // Melt snow into steam & ice into water (or steam in ultrawarm dimensions)!
            if (bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(p, false);
                level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D,
                        5, 0.22D, 0.22D, 0.22D, 0.025D);
                furthestVisualDist = Math.max(furthestVisualDist, dist);
                continue;
            }
            if (bs.is(BlockTags.ICE) || bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE)
                    || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.BLUE_ICE)) {
                if (level.dimensionType().ultraWarm()) {
                    level.removeBlock(p, false);
                } else {
                    level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                }
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D,
                        5, 0.22D, 0.22D, 0.22D, 0.025D);
                furthestVisualDist = Math.max(furthestVisualDist, dist);
                continue;
            }

            // Vaporize surface water blocks directly in the 1x1 lavastream path into steam!
            if (bs.is(Blocks.WATER)) {
                level.removeBlock(p, false);
                level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.55D, p.getZ() + 0.5D,
                        4, 0.20D, 0.20D, 0.20D, 0.02D);
                furthestVisualDist = Math.max(furthestVisualDist, dist);
                continue;
            }

            float hardness = bs.getDestroySpeed(level, p);
            if (hardness < 0.0F) {
                furthestVisualDist = Math.min(furthestVisualDist, dist);
                break; // Unbreakable (e.g. Bedrock) stops the lavastream!
            }

            if (isLavastreamBoreable(bs, hardness, tier) && blocksBoredThisPulse < maxBorePerPulse) {
                List<ItemStack> drops = Block.getDrops(bs, level, p, null, caster, pickaxeContext);
                level.removeBlock(p, false);
                for (ItemStack d : drops) {
                    if (!d.isEmpty()) {
                        Block.popResource(level, p, NetherSpellMutationHandler.trySmeltStack(level, d));
                    }
                }
                level.sendParticles(ParticleTypes.LAVA, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D,
                        5, 0.20D, 0.20D, 0.20D, 0.04D);
                level.playSound(null, p, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.55F, 0.78F);
                blocksBoredThisPulse++;
                furthestVisualDist = Math.max(furthestVisualDist, dist);
            } else {
                furthestVisualDist = Math.min(furthestVisualDist, dist);
                break;
            }
        }

        Vec3 endPos = muzzle.add(normDir.scale(Math.min(maxReach, Math.max(1.5D, furthestVisualDist))));
        SpellWorldVfxManager.broadcastChanneledBeamSlice(
                level, muzzle, endPos, EssenceType.MAGMA, EssenceType.EARTH, tier, 8
        );
        NetherSpellMutationHandler.autoSmeltDroppedItems(level, new AABB(muzzle, endPos).inflate(1.25D));

        if (now % 6L == 0L) {
            level.playSound(null, caster.blockPosition(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.70F, 0.85F);
        }
    }

    private static boolean isLavastreamBoreable(BlockState bs, float hardness, int tier) {
        if (hardness < 0.0F || hardness > 4.5F + tier * 2.0F) {
            return false;
        }
        return bs.is(BlockTags.BASE_STONE_OVERWORLD)
                || bs.is(BlockTags.BASE_STONE_NETHER)
                || bs.is(BlockTags.DIRT)
                || bs.is(BlockTags.SAND)
                || bs.is(BlockTags.TERRACOTTA)
                || bs.is(BlockTags.COAL_ORES)
                || bs.is(BlockTags.IRON_ORES)
                || bs.is(BlockTags.COPPER_ORES)
                || bs.is(BlockTags.GOLD_ORES)
                || bs.is(BlockTags.REDSTONE_ORES)
                || bs.is(BlockTags.LAPIS_ORES)
                || bs.is(BlockTags.DIAMOND_ORES)
                || bs.is(BlockTags.EMERALD_ORES)
                || bs.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || bs.is(BlockTags.MINEABLE_WITH_SHOVEL)
                || hardness <= 3.0F + tier * 1.5F;
    }

    // =========================================================================
    // FORM MUTATION 3 — CALDERA SHOCKWAVE NOVA (RADIAL BURST)
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
        // Section 6.14 Caldera Shockwave Nova:
        // Explosive 360-degree volcanic eruption (6-block radius) knocking mobs backward,
        // setting enemies ablaze, and turning floor blocks in the radius into lava/magma blocks at random!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float novaDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.62F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.MAGMA, EssenceType.EARTH, tier);
        SpellWorldVfxManager.broadcastGeyserPlume(level, center, 1.8D, 3.8D, EssenceType.MAGMA, EssenceType.NETHER, 36, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        NetherSpellMutationHandler.autoSmeltDroppedItems(level, box);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || foe.position().distanceTo(center) > r + 0.5D) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            foe.igniteForSeconds(6);
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 away = foe.position().subtract(center);
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.35D, 0.45D, push.z * 1.35D));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
        }

        // Turn random floor blocks in the 6-block radius into contained molten Lava (flush in solid floor)
        // or Magma Block crusts on exposed ledges, restoring 100% cleanly after 100 ticks (5.0s)!
        Map<BlockPos, BlockState> moltenFloor = new LinkedHashMap<>();
        BlockPos cPos = findGroundSurfaceNear(level, BlockPos.containing(center));
        int scanR = Math.min(6, (int) Math.ceil(r));
        double maxDistSq = (r + 0.25D) * (r + 0.25D);
        for (int dx = -scanR; dx <= scanR; dx++) {
            for (int dz = -scanR; dz <= scanR; dz++) {
                if ((dx * dx + dz * dz) > maxDistSq) continue;
                if (level.random.nextFloat() >= 0.28F) continue;
                BlockPos floorCell = findGroundSurfaceNear(level, cPos.offset(dx, 1, dz));
                BlockState bs = level.getBlockState(floorCell);
                if (bs.isSolidRender()
                        && level.getBlockState(floorCell.above()).isAir()
                        && !bs.is(Blocks.MAGMA_BLOCK)
                        && !bs.is(Blocks.LAVA)
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, floorCell, bs)) {
                    moltenFloor.put(floorCell.immutable(), bs);
                }
            }
        }
        if (!moltenFloor.isEmpty()) {
            Set<BlockPos> cellSet = moltenFloor.keySet();
            for (BlockPos imm : cellSet) {
                boolean contained = isContainedFloorLavaCell(level, imm, cellSet);
                level.setBlockAndUpdate(imm, (contained ? Blocks.LAVA : Blocks.MAGMA_BLOCK).defaultBlockState());
            }
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, r, 100, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) {
                            return true;
                        }
                        moltenFloor.forEach((pos, orig) -> {
                            BlockState cur = srv.getBlockState(pos);
                            if (cur.is(Blocks.MAGMA_BLOCK) || cur.is(Blocks.LAVA)
                                    || cur.is(Blocks.OBSIDIAN) || cur.is(Blocks.COBBLESTONE)) {
                                srv.setBlockAndUpdate(pos, orig);
                            }
                        });
                        return false;
                    }
            ));
        }

        alertHostilesToVolcanicBlast(level, caster, center, spell);
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.55D, center.z, 38, 2.2D, 0.55D, 2.2D, 0.10D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 0.74F);
    }

    // =========================================================================
    // FORM MUTATION 4 — VOLCANIC SLAG CLEAVE (TOUCH / STRIKE)
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
        // Section 6.14 Volcanic Slag Cleave:
        // Coats target in molten slag, setting them ablaze for 6 seconds,
        // reducing target Armor Toughness by 30% for 4 seconds (80t),
        // and automatically smelting drops if the target is slain!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float strikeDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
        Vec3 slashDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D),
                slashDir, 2.95D, EssenceType.MAGMA, EssenceType.EARTH, tier
        );

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() && hit != caster) {
                NetherSpellMutationHandler.autoSmeltDroppedItems(level, hit.getBoundingBox().inflate(2.5D));
                continue;
            }
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            // Stamp auto-smelt BEFORE any lethal damage so MobDropHandler cooks/smelts drops on instant kills!
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, now + 100L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            hit.igniteForSeconds(6);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 80L);

            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }
            NetherSpellMutationHandler.autoSmeltDroppedItems(level, hit.getBoundingBox().inflate(2.5D));
            level.sendParticles(ParticleTypes.LAVA, hit.getX(), hit.getY(0.75D), hit.getZ(),
                    14, 0.30D, 0.35D, 0.30D, 0.06D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.78F, 0.75F);
    }

    // =========================================================================
    // FORM MUTATION 5 — IGNEOUS ARMOR MANTLE (AURA / SELF)
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
        // Section 6.14 Igneous Armor Mantle:
        // Coats caster in thick obsidian/pumice plates granting +4 Armor, 50% knockback resistance,
        // 100% fire/lava/hot-floor immunity, and a minor -10% movement speed penalty, while radiating
        // intense heat in a 4-block radius that instantly melts snow/ice and vaporizes adjacent water into steam!
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        long now = level.getGameTime();

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        caster.clearFire();
        caster.setTicksFrozen(0);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.50D, -0.10D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.85F, null);

        SpellWorldVfxManager.broadcastWardShell(
                level, caster.position(), Math.max(1.38D, caster.getBbWidth() * 1.40D),
                EssenceType.MAGMA, EssenceType.EARTH, Math.min(dur, 120), tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, caster.position().add(0.0D, 0.04D, 0.0D), 4.0D, EssenceType.MAGMA, EssenceType.EARTH, 52, tier
        );
        level.sendParticles(ParticleTypes.LAVA, caster.getX(), caster.getY(0.8D), caster.getZ(),
                24, 0.45D, 0.55D, 0.45D, 0.06D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.90F, 0.72F);
        level.playSound(null, caster.blockPosition(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.75F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 6 — VOLCANIC VENT GEYSER TRAP (GLYPH / TRAP)
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
        // Section 4.6 & 6.14 Volcanic Vent Geyser Trap:
        // - Surface-snapped volcanic vent trap (1.0s arming, 10m or Tier 5+ indefinite lifetime, 50% Sneak-Right-Click disarm refund)
        // - Supports Single-Use Detonator (100%) and Repeating Glyphic Sentry (3 pulses @ 60%, re-arms after geyser finishes)
        // - When tripped: erupts in a vertical column of molten lava & steam for 3.0s (60t), launches triggering mobs
        //   8 blocks upward, ignites them in fire, and turns a 3x3 ground area into contained lava/magma blocks!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceBlock = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(glyphCenter.x, surfaceBlock.getY() + 1.02D, glyphCenter.z);
        double padRad = Math.max(2.0D, Math.min(3.2D, radius));

        String subMode = slot.primaryEffect().subMode();
        boolean repeatingSentry = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        CompoundTag casterTag = EntityHelper.getPersistentData(caster);
        String pulseKey = TAG_REPEATING_PULSES_LEFT + "_" + level.getGameTime();
        String cdKey = TAG_REPEATING_COOLDOWN_UNTIL + "_" + level.getGameTime();
        if (repeatingSentry) {
            casterTag.putInt(pulseKey, 3);
            casterTag.putLong(cdKey, 0L);
        }

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, 2.2D, EssenceType.MAGMA, EssenceType.EARTH, 52, tier);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_KENAZ.get(), MAGMA_BRUTE_HEX_COLOR, 32, 0.90F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 5, 0.45D, 0.05D, 0.45D, 0.005D
        );
        level.playSound(null, BlockPos.containing(snappedCenter), SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.85F, 0.78F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, 2.5D, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_LAVA,
                                zone.center.x, zone.center.y + 0.08D, zone.center.z, 3, 0.35D, 0.05D, 0.35D, 0.01D);
                    }
                    if (elapsed < 20) return true; // 1.0s arming delay (Section 4.6)

                    LivingEntity owner = zone.resolveCaster(srv);
                    long now = srv.getGameTime();
                    if (repeatingSentry && owner != null) {
                        long cdUntil = EntityHelper.getPersistentData(owner).getLong(cdKey).orElse(0L);
                        if (now < cdUntil) return true;
                    }

                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - padRad, zone.center.y - 1.0D, zone.center.z - padRad,
                            zone.center.x + padRad, zone.center.y + 2.2D, zone.center.z + padRad
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (stepped.isEmpty()) return true;

                    float pulseScale = repeatingSentry ? 0.60F : 1.0F;
                    boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    float initialGeyserDmg = Math.max(3.8F + tier * 1.15F,
                            MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, zone.apparatusPotency) * 0.72F)
                            * flawPot * pulseScale;

                    // 1. Convert 3x3 ground area into contained molten Lava (flush in solid basin) / Magma Block rim!
                    Map<BlockPos, BlockState> savedFloor = new LinkedHashMap<>();
                    BlockPos floorCenter = findGroundSurfaceNear(srv, BlockPos.containing(zone.center));
                    for (BlockPos p : BlockPos.betweenClosed(floorCenter.offset(-1, 0, -1), floorCenter.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if (bs.isSolidRender()
                                && srv.getBlockState(p.above()).isAir()
                                && !bs.is(Blocks.MAGMA_BLOCK)
                                && !bs.is(Blocks.LAVA)
                                && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) {
                            savedFloor.put(p.immutable(), bs);
                        }
                    }
                    if (!savedFloor.isEmpty()) {
                        Set<BlockPos> basinSet = savedFloor.keySet();
                        for (BlockPos imm : basinSet) {
                            boolean contained = isContainedFloorLavaCell(srv, imm, basinSet);
                            srv.setBlockAndUpdate(imm, (contained ? Blocks.LAVA : Blocks.MAGMA_BLOCK).defaultBlockState());
                        }
                    }

                    // 2. Broadcast 3-second (60t) 8-block vertical molten lava & steam geyser column!
                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, 1.85D, 8.2D, EssenceType.MAGMA, EssenceType.NETHER, 60, tier
                    );
                    SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, 3.0D, EssenceType.MAGMA, EssenceType.EARTH, tier
                    );

                    // 3. Launch triggering mobs 8 blocks upward (vy = 1.21 -> ~8.3m apex in 0.08g physics) & ignite!
                    for (LivingEntity v : stepped) {
                        v.igniteForSeconds(6);
                        if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                            Vec3 cur = v.getDeltaMovement();
                            SpellExecutionEngine.applySyncedMotion(v, new Vec3(cur.x * 0.30D, 1.21D, cur.z * 0.30D));
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * pulseScale
                            );
                        }
                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                            SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(srv, v, owner, initialGeyserDmg, false, true);
                        }
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.90F, 0.82F);

                    // 4. Sustain the vertical column of molten lava and steam for 3.0 seconds (60 ticks), then restore the 3x3 floor!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.2D, 60, now, null, null, EssenceType.MAGMA, pulseScale,
                            (vsrv, vzone, velapsed) -> {
                                if (velapsed >= vzone.maxDurationTicks) {
                                    savedFloor.forEach((pos, orig) -> {
                                        BlockState cur = vsrv.getBlockState(pos);
                                        if (cur.is(Blocks.MAGMA_BLOCK) || cur.is(Blocks.LAVA)
                                                || cur.is(Blocks.OBSIDIAN) || cur.is(Blocks.COBBLESTONE)) {
                                            vsrv.setBlockAndUpdate(pos, orig);
                                        }
                                    });
                                    return false;
                                }
                                if (velapsed % 5 == 0) {
                                    vsrv.sendParticles(ParticleTypes.LAVA, vzone.center.x, vzone.center.y + 2.2D, vzone.center.z,
                                            10, 0.40D, 2.2D, 0.40D, 0.07D);
                                    vsrv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, vzone.center.x, vzone.center.y + 2.8D, vzone.center.z,
                                            8, 0.45D, 2.0D, 0.45D, 0.04D);
                                    vsrv.sendParticles(ParticleTypes.CLOUD, vzone.center.x, vzone.center.y + 3.4D, vzone.center.z,
                                            6, 0.48D, 1.8D, 0.48D, 0.05D);
                                }
                                if (velapsed > 0 && velapsed % 12 == 0) {
                                    LivingEntity gOwner = vzone.resolveCaster(vsrv);
                                    AABB column = new AABB(
                                            vzone.center.x - 1.85D, vzone.center.y - 1.0D, vzone.center.z - 1.85D,
                                            vzone.center.x + 1.85D, vzone.center.y + 8.5D, vzone.center.z + 1.85D
                                    );
                                    for (LivingEntity inCol : vsrv.getEntitiesOfClass(LivingEntity.class, column, e -> e.isAlive() && e != gOwner)) {
                                        if (gOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(inCol, gOwner, wardSafe)) {
                                            continue;
                                        }
                                        inCol.igniteForSeconds(5);
                                        if (zone.spell == null || !zone.spell.prefix().enforcesZeroEntityHarm()) {
                                            SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(
                                                    vsrv, inCol, gOwner, (2.2F + tier * 0.60F) * pulseScale, false, false
                                            );
                                        }
                                        if (!SpellCombatReactionEngine.isBossEntity(vsrv, inCol) && inCol.getDeltaMovement().y < 0.82D) {
                                            Vec3 cv = inCol.getDeltaMovement();
                                            SpellExecutionEngine.applySyncedMotion(inCol, new Vec3(cv.x * 0.35D, 0.92D, curZ(cv) * 0.35D));
                                        }
                                    }
                                }
                                return true;
                            }
                    ));

                    if (repeatingSentry && owner != null) {
                        CompoundTag oTag = EntityHelper.getPersistentData(owner);
                        int rem = oTag.getInt(pulseKey).orElse(1) - 1;
                        if (rem > 0) {
                            oTag.putInt(pulseKey, rem);
                            oTag.putLong(cdKey, now + 80L); // Re-arm cleanly right after the 3.0s (60t) geyser + 1.0s settle!
                            return true;
                        }
                        oTag.remove(pulseKey);
                        oTag.remove(cdKey);
                    }
                    return false;
                }
        ).markDisarmableGlyph());
        return true;
    }

    private static double curZ(Vec3 v) {
        return v.z;
    }

    // =========================================================================
    // FORM MUTATION 7 — PYROCLASTIC SURGE (CONE / SPRAY)
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
        // Section 6.14 Pyroclastic Surge:
        // Wide 60-degree cone of molten lava splatter and burning pumice;
        // pushes enemies backward, ignites struck mobs in fire, turns water source blocks into obsidian
        // (and flowing water into cobblestone), and instantly melts snow and ice!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D);
        double reach = Math.max(6.0D, coneRange);
        double cosHalfCone = 0.866D; // Exact 60-degree total spread (+/- 30 degrees)
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float surgeDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.40F, apparatusPotency) * 0.65F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastVortexCone(
                level, eye, normDir, reach, EssenceType.MAGMA, EssenceType.EARTH, tier
        );

        // 1. Push enemies backward & ignite across the full 60-degree cone
        Set<UUID> struckIds = new HashSet<>();
        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(reach)).inflate(reach * 0.55D);
        List<LivingEntity> candidates = new ArrayList<>(hitTargets);
        for (LivingEntity extra : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (extra == caster) continue;
            Vec3 to = extra.getBoundingBox().getCenter().subtract(eye);
            double d = to.length();
            if (d <= reach + 0.6D && d > 0.05D && to.scale(1.0D / d).dot(normDir) >= cosHalfCone) {
                candidates.add(extra);
            }
        }

        for (LivingEntity hit : candidates) {
            if (!hit.isAlive() || hit == caster || !struckIds.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.igniteForSeconds(6);
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normDir.x * 1.22D, 0.36D, normDir.z * 1.22D));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(level, hit, caster, surgeDmg, false, true);
            }
        }

        // 2. Sweep the true 60-degree 3D cone volume: water sources -> Obsidian, flowing water -> Cobblestone, snow/ice -> melted!
        int boxR = Math.min(10, (int) Math.ceil(reach));
        BlockPos originBlock = BlockPos.containing(eye);
        boolean quenchedWater = false;
        for (BlockPos p : BlockPos.betweenClosed(
                originBlock.offset(-boxR, -3, -boxR),
                originBlock.offset(boxR, 3, boxR))) {
            Vec3 cellCenter = Vec3.atCenterOf(p);
            Vec3 rel = cellCenter.subtract(eye);
            double dist = rel.length();
            if (dist < 0.6D || dist > reach + 0.5D) continue;
            if (rel.scale(1.0D / dist).dot(normDir) < 0.82D) continue; // Slightly generous horizontal/floor margin inside 60 deg cone

            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;

            if (bs.is(Blocks.WATER)) {
                boolean isSource = bs.getFluidState().isSource();
                level.setBlockAndUpdate(p, (isSource ? Blocks.OBSIDIAN : Blocks.COBBLESTONE).defaultBlockState());
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        cellCenter.x, cellCenter.y + 0.35D, cellCenter.z, 3, 0.18D, 0.20D, 0.18D, 0.02D);
                quenchedWater = true;
            } else if (bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(p, false);
            } else if (bs.is(BlockTags.ICE) || bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE)
                    || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.BLUE_ICE)) {
                if (level.dimensionType().ultraWarm()) {
                    level.removeBlock(p, false);
                } else {
                    level.setBlockAndUpdate(p, Blocks.COBBLESTONE.defaultBlockState()); // Melted & immediately quenched by lava splatter!
                }
                quenchedWater = true;
            }
        }

        NetherSpellMutationHandler.autoSmeltDroppedItems(level, coneBox);
        if (quenchedWater) {
            level.playSound(null, caster.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.75F, 1.10F);
        }
        level.sendParticles(ParticleTypes.LAVA,
                eye.x + normDir.x * 2.4D, eye.y + normDir.y * 2.4D, eye.z + normDir.z * 2.4D,
                28, 0.85D, 0.45D, 0.85D, 0.08D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.85F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 8 — VOLCANIC METEORITE BOMBARDMENT (RAIN / SKYFALL)
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
        // Section 4.8 & 6.14 Volcanic Meteorite Bombardment:
        // Cave-ceiling-adaptive barrage of exploding molten magma boulders over 6.0 seconds (120t)
        // across a 12-block area (radius >= 6.0m -> 12m diameter); repeatedly staggers mobs with heavy
        // impact impulses, sets the zone ablaze, vaporizes water pools, and auto-smelts dropped raw items!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        BlockPos groundPos = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 anchoredGround = new Vec3(impactCenter.x, groundPos.getY() + 1.0D, impactCenter.z);
        double riftClearance = probeOverheadClearance(level, groundPos.above(), 10);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, anchoredGround, r, EssenceType.MAGMA, EssenceType.EARTH, 120, tier
        );
        List<BlockPos> temporaryFires = new ArrayList<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredGround, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (BlockPos fp : temporaryFires) {
                            if (srv.getBlockState(fp).is(Blocks.FIRE)) {
                                srv.removeBlock(fp, false);
                            }
                        }
                        return false;
                    }

                    // Visual falling magma boulders crashing inside the cave/sky clearance every 6 ticks
                    if (elapsed % 6 == 0) {
                        double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                        double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.90D);
                        Vec3 strikeSpot = zone.center.add(Math.cos(ang) * dist, 0.05D, Math.sin(ang) * dist);
                        SpellWorldVfxManager.broadcastProjectileImpact(
                                srv, strikeSpot, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.MAGMA, EssenceType.EARTH, 1.35F, tier
                        );
                        srv.sendParticles(ParticleTypes.LAVA,
                                zone.center.x, zone.center.y + Math.min(5.5D, riftClearance * 0.72D), zone.center.z,
                                12, zone.radius * 0.50D, 0.85D, zone.radius * 0.50D, 0.08D);
                        BlockPos fireCand = BlockPos.containing(strikeSpot);
                        if (temporaryFires.size() < 28
                                && srv.getBlockState(fireCand).isAir()
                                && srv.getBlockState(fireCand.below()).isSolidRender()
                                && srv.random.nextFloat() < 0.45F) {
                            srv.setBlockAndUpdate(fireCand, Blocks.FIRE.defaultBlockState());
                            temporaryFires.add(fireCand.immutable());
                        }
                    }

                    // Heavy bombardment wave every 15 ticks (8 waves over 6.0s)
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                        float waveDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                                * Math.max(0.35F, zone.apparatusPotency) * 0.42F
                                * (zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F);

                        // Vaporize water pools & melt snow/ice across the 12-block bombardment zone!
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(8, (int) Math.ceil(zone.radius));
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -2, -br), cPos.offset(br, 2, br))) {
                            if (p.distSqr(cPos) > rSq) continue;
                            BlockState bs = srv.getBlockState(p);
                            if (bs.is(Blocks.WATER)) {
                                srv.removeBlock(p, false);
                                srv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                        p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.02D);
                            } else if (bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)
                                    || bs.is(BlockTags.ICE)) {
                                srv.removeBlock(p, false);
                            }
                        }

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(4.5D, riftClearance), zone.center.z + zone.radius
                        );
                        NetherSpellMutationHandler.autoSmeltDroppedItems(srv, area);

                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            if ((dx * dx + dz * dz) > rSq) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) {
                                continue;
                            }
                            v.igniteForSeconds(6);
                            EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, now + 50L);

                            // Stagger mobs with heavy concussive meteorite impact impulse!
                            if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                double jx = (srv.random.nextDouble() - 0.5D) * 0.48D;
                                double jz = (srv.random.nextDouble() - 0.5D) * 0.48D;
                                double jy = v.onGround() ? 0.34D : -0.45D;
                                SpellExecutionEngine.applySyncedMotion(v, new Vec3(jx, jy, jz));
                            }
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.42F
                                );
                            }
                            if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                                SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(srv, v, owner, waveDmg, false, true);
                            }
                        }
                        srv.playSound(null, cPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.72F, 0.78F);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — MAGMA-BRUTE EIDOLON SUITE (#B83C08)
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
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.MAGMA.name());
        tag.putInt("SpellEidolonTintRGB", MAGMA_BRUTE_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.35D, radius), EssenceType.MAGMA, EssenceType.EARTH, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.LAVA, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.08D);
        level.sendParticles(ParticleTypes.DRIPPING_LAVA, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 14, 0.40D, 0.50D, 0.40D, 0.02D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.90F, 0.72F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS (BLINK, TRANSMUTE, AFFLICTION, KINETIC)
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.14 Caldera Step:
        // Teleports caster leaving a pool of molten lava at departure (restoring cleanly after 80t),
        // and arrives with an explosive volcanic fire shockwave!
        BlockPos depFloor = findGroundSurfaceNear(level, BlockPos.containing(departurePos));
        Map<BlockPos, BlockState> savedPool = new LinkedHashMap<>();
        for (BlockPos p : BlockPos.betweenClosed(depFloor.offset(-1, 0, -1), depFloor.offset(1, 0, 1))) {
            if (p.distManhattan(depFloor) > 1) continue; // 5-cell volcanic cross pool at departure!
            BlockState prev = level.getBlockState(p);
            if (prev.isSolidRender()
                    && level.getBlockState(p.above()).isAir()
                    && !prev.is(Blocks.MAGMA_BLOCK)
                    && !prev.is(Blocks.LAVA)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, prev)) {
                savedPool.put(p.immutable(), prev);
            }
        }
        if (!savedPool.isEmpty()) {
            Set<BlockPos> poolSet = savedPool.keySet();
            for (BlockPos imm : poolSet) {
                boolean contained = isContainedFloorLavaCell(level, imm, poolSet);
                level.setBlockAndUpdate(imm, (contained ? Blocks.LAVA : Blocks.MAGMA_BLOCK).defaultBlockState());
            }
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, departurePos, 1.8D, 80, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) {
                            return true;
                        }
                        savedPool.forEach((pos, orig) -> {
                            BlockState cur = srv.getBlockState(pos);
                            if (cur.is(Blocks.MAGMA_BLOCK) || cur.is(Blocks.LAVA)
                                    || cur.is(Blocks.OBSIDIAN) || cur.is(Blocks.COBBLESTONE)) {
                                srv.setBlockAndUpdate(pos, orig);
                            }
                        });
                        return false;
                    }
            ));
        }
        SpellWorldVfxManager.broadcastGeyserPlume(
                level, departurePos, 1.25D, 2.8D, EssenceType.MAGMA, EssenceType.NETHER, 28, potencyTier
        );

        // Arrival explosive volcanic fire shockwave!
        SpellWorldVfxManager.broadcastRadialNova(
                level, arrivalPos.add(0.0D, 0.08D, 0.0D), 4.0D, EssenceType.MAGMA, EssenceType.EARTH, potencyTier
        );
        level.sendParticles(ParticleTypes.LAVA, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z,
                30, 0.85D, 0.50D, 0.85D, 0.09D);

        float shockDmg = 3.4F + potencyTier * 1.20F;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(4.0D), LivingEntity::isAlive)) {
            if (foe == caster || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.igniteForSeconds(6);
            SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(level, foe, caster, shockDmg, false, true);
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 away = foe.position().subtract(arrivalPos);
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x * 1.20D, 0.42D, push.z * 1.20D));
            }
        }
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.88F, 0.82F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.14 Igneous Forging:
        // - Container-safe (enforced before and inside handler)
        // - Auto-smelts raw ores into ingots (using Netherite Pickaxe tool context so stone/ores yield true drops!)
        // - Turns water sources into solid Obsidian and flowing water into Magma Blocks
        // - Melts stone blocks into glowing Magma Blocks!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        // 1. Water -> Solid Obsidian (source) or Magma Block (flowing)
        if (state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN)) {
            boolean isSource = state.is(Blocks.BUBBLE_COLUMN)
                    || state.getFluidState().isSource()
                    || (state.hasProperty(LiquidBlock.LEVEL) && state.getValue(LiquidBlock.LEVEL) == 0);
            level.setBlockAndUpdate(pos, (isSource ? Blocks.OBSIDIAN : Blocks.MAGMA_BLOCK).defaultBlockState());
            emitIgneousForgeVfx(level, pos, true);
            return true;
        }

        // 2. Ice & Snow -> Melt / flash-quench into Magma Block or Water
        if (state.is(BlockTags.ICE) || state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE)) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            emitIgneousForgeVfx(level, pos, true);
            return true;
        }

        // 3. Melt stone-family blocks directly into glowing Magma Blocks!
        if (state.is(Blocks.STONE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.SMOOTH_STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.BLACKSTONE)
                || state.is(Blocks.NETHERRACK)) {
            level.setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
            emitIgneousForgeVfx(level, pos, false);
            return true;
        }

        // 4. Sand -> Glass, Clay/Mud -> Terracotta
        if (state.is(BlockTags.SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            emitIgneousForgeVfx(level, pos, false);
            return true;
        }
        if (state.is(Blocks.CLAY) || state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.TERRACOTTA.defaultBlockState());
            emitIgneousForgeVfx(level, pos, false);
            return true;
        }

        // 5. Auto-smelt raw ores in-world using full Netherite Pickaxe tool context!
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness >= 0.0F && hardness <= 4.5F + potencyTier * 5.0F) {
            List<ItemStack> drops = Block.getDrops(state, level, pos, null, caster, new ItemStack(Items.NETHERITE_PICKAXE));
            if (!drops.isEmpty()) {
                List<ItemStack> forgedDrops = new ArrayList<>(drops.size());
                boolean smeltedAny = false;
                for (ItemStack d : drops) {
                    ItemStack res = NetherSpellMutationHandler.trySmeltStack(level, d);
                    if (res != d) {
                        smeltedAny = true;
                    }
                    forgedDrops.add(res);
                }
                if (smeltedAny) {
                    level.removeBlock(pos, false);
                    for (ItemStack out : forgedDrops) {
                        Block.popResource(level, pos, out);
                    }
                    emitIgneousForgeVfx(level, pos, false);
                    return true;
                }
            }
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
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;
        int count = in.getCount();

        // Water bucket -> Solid Obsidian + Empty Bucket!
        if (in.is(Items.WATER_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.OBSIDIAN, count));
            Block.popResource(level, itemEntity.blockPosition(), new ItemStack(Items.BUCKET, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitIgneousForgeVfx(level, itemEntity.blockPosition(), true);
            return true;
        }

        // Stone / Cobblestone / Deepslate / Basalt / Netherrack stacks -> Magma Block stacks!
        if (in.is(Items.STONE)
                || in.is(Items.COBBLESTONE)
                || in.is(Items.DEEPSLATE)
                || in.is(Items.COBBLED_DEEPSLATE)
                || in.is(Items.BASALT)
                || in.is(Items.BLACKSTONE)
                || in.is(Items.NETHERRACK)) {
            itemEntity.setItem(new ItemStack(Items.MAGMA_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitIgneousForgeVfx(level, itemEntity.blockPosition(), false);
            return true;
        }

        // Raw ores, sand, clay, raw meats -> Smelted ingots / glass / cooked food with up to 30% bulk fuel discount!
        ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, in);
        if (smelted != in) {
            itemEntity.setItem(smelted);
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitIgneousForgeVfx(level, itemEntity.blockPosition(), false);
            return true;
        }

        // Section 5.5 Invalid Organic Fallback -> Cinder Ash Flakes!
        if (in.is(ItemTags.LEAVES) || in.is(ItemTags.FLOWERS) || in.is(ItemTags.SAPLINGS)
                || in.is(Items.STICK) || in.is(Items.PAPER) || in.is(Items.WHEAT_SEEDS)) {
            itemEntity.setItem(new ItemStack(ModItems.CINDER_ASH_FLAKES.get(), count));
            level.sendParticles(ParticleTypes.ASH, itemEntity.getX(), itemEntity.getY(0.25D), itemEntity.getZ(),
                    12, 0.2D, 0.2D, 0.2D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.14 Molten Slag Encasement Hex:
        // - Persistent milk-immune volcanic slag curse (cleansed via Water/Frozen/Restoration magic or caster defeat)
        // - Natural health regeneration locked (SpellCombatReactionEngine.isRegenLockingHex)
        // - Permanently sets target ablaze in unquenchable fire (even underwater!)
        // - Reduces Armor Toughness by 40%
        // - Cuts movement speed by an exact 30% on both players and monsters!
        long now = level.getGameTime();
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);

        target.igniteForSeconds(4);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.75D, 0.30D * mult), 45L);

        SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(
                level, target, hexCaster, (1.70F + potencyTier * 0.60F) * mult, false, true
        );

        level.sendParticles(ParticleTypes.DRIPPING_LAVA, target.getX(), target.getY(0.8D), target.getZ(),
                7, 0.28D, 0.38D, 0.28D, 0.02D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_KENAZ.get(), MAGMA_BRUTE_HEX_COLOR, 24, 0.90F, 0.04F),
                target.getX(), target.getY(0.95D), target.getZ(), 2, 0.22D, 0.25D, 0.22D, 0.01D
        );
        if (target.isInWaterOrRain()) {
            level.sendParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getY(0.6D), target.getZ(),
                    6, 0.25D, 0.30D, 0.25D, 0.03D);
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.85D), target.getZ(),
                    4, 0.22D, 0.25D, 0.22D, 0.02D);
        }
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
        if (effect != SpellCoreEffect.KINETIC) {
            return;
        }
        if (target == caster) {
            // Self-targeted Volcanic Eruption Blast: launches caster 12 blocks upward OR blasts nearby enemies!
            SpellWorldVfxManager.broadcastGeyserPlume(
                    level, caster.position(), 1.65D, 6.5D, EssenceType.MAGMA, EssenceType.NETHER, 32, potencyTier
            );
            SpellExecutionEngine.applySyncedMotion(caster, new Vec3(
                    caster.getDeltaMovement().x * 0.5D, 1.50D, caster.getDeltaMovement().z * 0.5D
            ));
            level.sendParticles(ParticleTypes.LAVA, caster.getX(), caster.getY(0.2D), caster.getZ(),
                    18, 0.45D, 0.35D, 0.45D, 0.08D);
            level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.85F, 0.85F);
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                target, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        launchEntityWithVolcanicEruption(level, caster, target, potencyTier);
    }

    /**
     * Called from {@code SpellExecutionEngine.executeSingleEffectOnBlock} when a Magma-mutated {@code KINETIC}
     * spell strikes terrain, unleashing an upward volcanic blast that launches nearby mobs 12 blocks airborne!
     */
    public static void eruptVolcanicKineticBlastAt(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin,
            int potencyTier,
            SpellAssembly spell
    ) {
        double blastRadius = 3.8D;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastGeyserPlume(
                level, origin, 2.0D, 8.5D, EssenceType.MAGMA, EssenceType.NETHER, 36, potencyTier
        );
        SpellWorldVfxManager.broadcastRadialNova(
                level, origin.add(0.0D, 0.08D, 0.0D), blastRadius, EssenceType.MAGMA, EssenceType.EARTH, potencyTier
        );
        level.sendParticles(ParticleTypes.LAVA, origin.x, origin.y + 0.5D, origin.z,
                26, 1.1D, 0.5D, 1.1D, 0.09D);
        level.playSound(null, BlockPos.containing(origin), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.90F, 0.80F);

        AABB box = new AABB(
                origin.x - blastRadius, origin.y - 1.5D, origin.z - blastRadius,
                origin.x + blastRadius, origin.y + 3.5D, origin.z + blastRadius
        );
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (living.position().distanceTo(origin) > blastRadius + 0.5D) continue;
            if (living == caster) {
                // Rocket-launch caster if aimed right beneath their own feet!
                if (living.position().distanceToSqr(origin) <= 9.0D) {
                    SpellExecutionEngine.applySyncedMotion(living, new Vec3(
                            living.getDeltaMovement().x * 0.5D, 1.50D, living.getDeltaMovement().z * 0.5D
                    ));
                }
                continue;
            }
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, wardSafe)) {
                continue;
            }
            launchEntityWithVolcanicEruption(level, caster, living, potencyTier);
        }
    }

    private static void launchEntityWithVolcanicEruption(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            int potencyTier
    ) {
        if (SpellCombatReactionEngine.isBossEntity(level, target)) {
            SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(
                    level, target, caster, 5.0F + potencyTier * 2.2F, false, true
            );
            return;
        }
        // vy = 1.50D reaches ~12.15 blocks vertical apex in Minecraft 0.08g / 0.98-drag physics!
        Vec3 cur = target.getDeltaMovement();
        SpellExecutionEngine.applySyncedMotion(target, new Vec3(cur.x * 0.30D, 1.50D, cur.z * 0.30D));
        target.igniteForSeconds(5);
        SpellWorldVfxManager.broadcastGeyserPlume(
                level, target.position(), 1.35D, 6.5D, EssenceType.MAGMA, EssenceType.NETHER, 28, potencyTier
        );
        level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.25D), target.getZ(),
                18, 0.38D, 0.35D, 0.38D, 0.08D);
        level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.80F, 0.84F);
    }

    // =========================================================================
    // INTERNAL SHARED VOLCANIC HELPERS
    // =========================================================================

    private static boolean isContainedFloorLavaCell(ServerLevel level, BlockPos cell, Set<BlockPos> basinSet) {
        if (!level.getBlockState(cell.below()).isSolidRender()) {
            return false;
        }
        for (Direction horiz : Direction.Plane.HORIZONTAL) {
            BlockPos adj = cell.relative(horiz);
            if (!basinSet.contains(adj) && !level.getBlockState(adj).isSolidRender()) {
                return false;
            }
        }
        return true;
    }

    private static void emitIgneousForgeVfx(ServerLevel level, BlockPos pos, boolean steamQuench) {
        level.sendParticles(ParticleTypes.LAVA,
                pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                10, 0.24D, 0.24D, 0.24D, 0.05D);
        if (steamQuench) {
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.28D, 0.25D, 0.03D);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.70F, 1.15F);
        } else {
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.65F, 0.88F);
        }
    }

    private static void refundSubLinearBatchFuelDiscount(@Nullable LivingEntity caster, int tier, int stackCount) {
        if (!(caster instanceof Player player) || stackCount <= 1) return;
        float discountFrac = Math.min(0.30F, 0.04F * (stackCount - 1));
        long refundMfum = Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * discountFrac);
        if (refundMfum > 0L) {
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.MAGMA, refundMfum);
        }
    }

    private static void alertHostilesToVolcanicBlast(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin,
            SpellAssembly spell
    ) {
        if (caster == null) return;
        for (SubSpellSlot s : spell.subSpells()) {
            EssenceType pMat = s.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), null);
            EssenceType sMat = s.secondaryEffect().map(sec -> sec.resolveEffectiveMateria(spell.materiaSpecific(), null)).orElse(null);
            if (pMat == EssenceType.UMBRAL || pMat == EssenceType.SILENCE
                    || sMat == EssenceType.UMBRAL || sMat == EssenceType.SILENCE) {
                return;
            }
        }
        AABB alertBox = new AABB(origin, origin).inflate(24.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, alertBox, LivingEntity::isAlive)) {
            if (mob instanceof Monster && mob.getTarget() == null && !mob.isAlliedTo(caster)) {
                if (mob.distanceToSqr(origin) <= 576.0D) {
                    mob.setTarget(caster);
                }
            }
        }
    }

    private static boolean slotExecutesHarmVerb(@Nullable SubSpellSlot slot) {
        if (slot == null) return false;
        if (slot.primaryEffect().effect() == SpellCoreEffect.HARM) return true;
        return slot.secondaryEffect().map(s -> s.effect() == SpellCoreEffect.HARM).orElse(false);
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        if (!level.getBlockState(start).getCollisionShape(level, start).isEmpty()) {
            for (int up = 1; up <= 4; up++) {
                BlockPos above = start.above(up);
                if (level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
                    return above.below();
                }
            }
            return start;
        }
        for (int down = 1; down <= 8; down++) {
            BlockPos below = start.below(down);
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return below;
            }
        }
        return start.below();
    }

    private static double probeOverheadClearance(ServerLevel level, BlockPos groundFloor, int maxProbe) {
        for (int dy = 4; dy <= maxProbe; dy++) {
            BlockPos check = groundFloor.above(dy);
            if (!level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                return Math.max(3.6D, dy - 0.55D);
            }
        }
        return maxProbe;
    }
}
