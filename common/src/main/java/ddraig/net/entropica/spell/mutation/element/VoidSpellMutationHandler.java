package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Complete, zero-shortcut Signature Mutation implementation for <b>Void Materia (Base Type #10)</b>
 * (Spellcrafting System Guide Sections 1.2B, 2.2, 2.3, 3.4, 4.1–4.9, 5.1–5.7, and 6.11).
 *
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against {@code AIR (+1)} | Weak against {@code EARTH (-1)}.</li>
 *     <li><b>Flow (Void-Warp)</b>: <i>Void-Warp Phasing Flow</i> — Chained, bifurcated, or sequenced sub-spells
 *         phase through solid terrain and walls via micro-wormholes regardless of physical line-of-sight obstructions.</li>
 *     <li><b>Projectile</b>: <i>Singularity Rift Orb</i> — Slow-drifting abyssal orb ({@code #41304E}) capable of phasing
 *         through thin obstacles; collapses upon impacting an entity, thick terrain, or reaching max range to pull adjacent
 *         mobs inward within 4 blocks and deal Void Materia damage.</li>
 *     <li><b>Beam</b>: <i>Gravitational Tractor Beam</i> — Sustained stone-piercing violet ray ({@code #41304E}) that pulls
 *         struck entities steadily toward the caster (or holds them weightlessly suspended in mid-air while channeled)
 *         while dealing continuous Void Materia damage.</li>
 *     <li><b>Radial Burst</b>: <i>Singularity Implosion</i> — 360-degree 6-block+ inward gravitational collapse dragging all
 *         surrounding mobs directly toward the epicenter and dealing Void Materia damage.</li>
 *     <li><b>Touch / Strike</b>: <i>Spatial Destabilization Strike</i> — Armor-piercing Void Materia melee strike (stacking
 *         held weapon Materia Advantage) that destabilizes the victim in space for 10s using Void Materia toxicity warps.</li>
 *     <li><b>Aura / Self</b>: <i>Zero-G Levitation Aura</i> — Registers defense as Void ({@code 0.50x} Air, {@code 1.50x} Earth),
 *         granting personal weightlessness, high-float jumps, smooth slow-falling glide, and cosmic violet particles
 *         (mobile or Sneak-Cast anchored field).</li>
 *     <li><b>Glyph / Trap</b>: <i>Null-Zone Silencing Mine</i> — Surface-snapped cosmic mine (1.0s arming, 10m / Tier 5+ indefinite,
 *         50% Sneak-Right-Click disarm refund, Single &amp; Repeating Sentry) releasing a 6-block distortion pulse that disables
 *         mob abilities and silences player spellcasting for 5 seconds.</li>
 *     <li><b>Cone / Spray</b>: <i>Null-Matter Dispel Spray</i> — 60-degree dark void wave stripping positive potion buffs
 *         and temporary wards while dealing Void Materia damage.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Singularity Vortex Rift</i> — Cave-ceiling-adaptive 6-second dark cosmic sky rift across
 *         a 12-block+ zone lifting mobs upward toward the vortex and dealing Void Materia damage before dropping them for
 *         vertical fall damage when the rift expires.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Void-Stalker Eidolon Suite</i> ({@code #41304E}) — Short-range phasing armor-piercing
 *         claw Agent, inward-gravity-pulsing Void Dart Sentry, and celestial silhouette Decoy collapsing into a micro-singularity.</li>
 *     <li><b>Blink / Step</b>: <i>Wormhole Transit</i> — Teleports the caster and leaves a persistent 5-second two-way 3D
 *         wormhole portal pair that allies and the caster can walk through in both directions.</li>
 *     <li><b>Transmute / Break</b>: <i>Abyssal Disintegration</i> &amp; <i>Direct-to-Inventory / Omni-Sponge</i> — Dissolves
 *         organic blocks/items into raw void slag (with up to 30% batch fuel refund), teleports mined/harvested drops directly
 *         into the player's inventory, and sponges liquid source blocks into held buckets.</li>
 *     <li><b>Affliction</b>: <i>Spacetime Anchoring Curse</i> — Milk-immune dimensional tether yanking the victim back to
 *         cursed coordinates every 10–20s, blocking teleports/Chorus Fruit, increasing incoming Void damage by +50%, and
 *         triggering <i>Spacetime Rift Beacon</i> (2x Void-Stalker &amp; Enderman perimeter spawns) on cursed players.</li>
 *     <li><b>Kinetic / Gravity</b>: <i>Gravity Well</i> — Stationary, non-destructive 3D black hole pulling surrounding
 *         items and mobs inward with strong gravitational suction.</li>
 * </ul>
 */
public final class VoidSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Section 6.11 deep cosmic violet hex color (#41304E). */
    public static final int VOID_STALKER_HEX_COLOR = 0x41304E;
    /** Luminous event-horizon violet accent (#C77DFF). */
    public static final int VOID_ACCRETION_ACCENT_HEX = 0xC77DFF;

    private static final String TAG_SINGULARITY_COLLAPSED = "SpellVoidSingularityCollapsed";
    private static final String TAG_WORMHOLE_COOLDOWN_UNTIL = "SpellWormholeCooldownUntil";
    private static final String TAG_REPEATING_PULSES_LEFT = "SpellVoidNullMinePulses";
    private static final String TAG_REPEATING_COOLDOWN_UNTIL = "SpellVoidNullMineCooldown";

    private static final int VFX_TRACTOR_BEAM_TICKS = 10;
    private static final int VFX_IMPLOSION_NOVA_TICKS = 48;
    private static final int VFX_NULL_MINE_BLAST_TICKS = 52;
    private static final int VFX_DISPEL_CONE_TICKS = 52;
    private static final int VFX_VORTEX_RIFT_TICKS = 130;
    private static final int VFX_WORMHOLE_PORTAL_TICKS = 104;

    @Override
    public EssenceType materiaType() {
        return EssenceType.VOID;
    }

    // =========================================================================
    // TITLES & DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case VOID_WARP, CHAIN_LEAP, DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE -> "Void-Warp Phasing Flow";
            default -> "Singularity Rift " + flow.getDisplayName();
        };
    }

    @Override
    @Nullable
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "Chained, bifurcated, or sequenced sub-spells phase through solid terrain and walls via micro-wormholes, "
                + "jumping directly to target entities regardless of physical line-of-sight obstructions.";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Singularity Rift Orb";
            case BEAM -> "Gravitational Tractor Beam";
            case RADIAL_BURST -> "Singularity Implosion";
            case TOUCH_STRIKE -> "Spatial Destabilization Strike";
            case AURA -> "Zero-G Levitation Aura";
            case GLYPH -> "Null-Zone Silencing Mine";
            case CONE -> "Null-Matter Dispel Spray";
            case SKYFALL -> "Singularity Vortex Rift";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    @Nullable
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Fires a slow-drifting abyssal orb (#41304E) that phases through thin obstacles and collapses "
                    + "on impact or max range, pulling mobs within 4m inward with Void Materia damage (+1 vs Air, -1 vs Earth).";
            case BEAM -> "Channels a stone-piercing violet ray (#41304E) that pulls struck foes steadily toward you or suspends "
                    + "them weightlessly in mid-air while dealing continuous Void Materia damage.";
            case RADIAL_BURST -> "Detonates in a 360° inward gravitational collapse (6m+ radius), dragging all surrounding mobs "
                    + "directly into the epicenter and dealing Void Materia damage.";
            case TOUCH_STRIKE -> "Infuses your melee strike with armor-piercing Void Materia damage (stacking held weapon advantage) "
                    + "and destabilizes struck victims in space so they teleport randomly for 10 seconds.";
            case AURA -> "Registers defense as Void (0.5x Air, 1.5x Earth) and confers personal weightlessness with high-float "
                    + "jumps, smooth slow-falling glide, and cosmic violet particles (mobile or Sneak-Cast anchored).";
            case GLYPH -> "Inscribes a cosmic mine (1s arm, 10m / Tier 5+ indefinite, 50% disarm refund) that erupts in a 6m "
                    + "distortion pulse disabling mob abilities and silencing player spellcasting for 5 seconds.";
            case CONE -> "Blasts a 60° wave of dark null-matter particles that strips positive potion buffs and temporary wards "
                    + "from struck enemies while dealing Void Materia damage.";
            case SKYFALL -> "Opens a cave-adaptive cosmic sky rift for 6s across a 12m+ zone, lifting mobs upward with Void Materia "
                    + "damage before dropping them for vertical fall damage when the rift snaps shut.";
            case SUMMON_AGENT -> "Summons a #41304E Void-Stalker Agent that short-range phases through obstacles to strike hostiles "
                    + "with armor-piercing void claws.";
            case SUMMON_SENTRY -> "Summons an anchored #41304E Void Gravity Pylon pulsing inward gravitational suction across 10m "
                    + "while firing wall-phasing void darts.";
            case SUMMON_DECOY -> "Summons a celestial #41304E Void Singularity Silhouette that taunts hostiles and collapses into "
                    + "a 5m micro-singularity implosion upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Wormhole Transit";
            case TRANSMUTE, BREAK -> "Abyssal Disintegration";
            case AFFLICTION -> "Spacetime Anchoring Curse";
            case KINETIC, GRAVITY -> "Gravity Well";
            default -> "Abyssal " + effect.getDisplayName();
        };
    }

    @Override
    @Nullable
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Teleports you and leaves a persistent 5-second two-way 3D wormhole portal pair that allies and you "
                    + "can walk through in either direction.";
            case TRANSMUTE, BREAK -> "Dissolves organic blocks and dropped items into raw void slag (up to 30% batch fuel refund), "
                    + "while teleporting mined drops to inventory and sponging fluids into buckets.";
            case AFFLICTION -> "Milk-immune spacetime curse that rubber-band yanks victims back every 10–20s, blocks teleports/Chorus Fruit, "
                    + "boosts incoming Void damage by +50%, and lures 2x Void-Stalkers & Endermen around cursed players.";
            case KINETIC, GRAVITY -> "Conjures a stationary, non-destructive 3D black hole that pulls surrounding items and mobs "
                    + "inward with strong gravitational suction.";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATION: VOID-WARP PHASING FLOW
    // =========================================================================

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity from,
            LivingEntity to,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.11 Void-Warp Phasing Flow: chained sub-spells jump through solid walls via micro-wormholes!
        int tier = spell.getEffectiveTier();
        Vec3 start = from.getBoundingBox().getCenter();
        Vec3 end = to.getBoundingBox().getCenter();
        SpellWorldVfxManager.broadcastBlinkRift(level, start, end, EssenceType.VOID, tier);
        SpellWorldVfxManager.broadcastVacuumSphere(level, end.subtract(0.0D, 0.65D, 0.0D), 1.35D, true, EssenceType.VOID, null, 18, tier);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, end.x, end.y, end.z, 14, 0.25D, 0.30D, 0.25D, 0.06D);
        level.playSound(null, to.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.68F, 1.32F);

        if (to.isBlocking() && !spell.prefix().enforcesZeroEntityHarm()) {
            if (to instanceof Player bp && !bp.getUseItem().isEmpty()) {
                bp.getCooldowns().addCooldown(bp.getUseItem(), 60);
            }
            to.stopUsingItem();
        }
    }

    @Override
    public void onFlowMultiCastOrigin(
            ServerLevel level,
            LivingEntity caster,
            SpellFlowModifier flow,
            int branchCount,
            SpellAssembly spell
    ) {
        Vec3 hand = caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D).add(caster.getLookAngle().scale(0.65D));
        SpellWorldVfxManager.broadcastVacuumSphere(level, hand.subtract(0.0D, 0.65D, 0.0D), 1.35D, true, EssenceType.VOID, null, 18, spell.getEffectiveTier());
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, hand.x, hand.y, hand.z, 18, 0.25D, 0.25D, 0.25D, 0.06D);
        level.playSound(null, caster.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.72F, 1.28F);
    }

    // =========================================================================
    // FORM MUTATION 1 — SINGULARITY RIFT ORB (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.11 Singularity Rift Orb (#41304E):
        // Slow-drifting, zero-drop abyssal orb capable of phasing through thin obstacles (panes, bars, fences, gates, leaves, thin partitions),
        // collapsing upon reaching max range, striking thick terrain, or impacting an entity!
        projectile.setNoGravity(true);
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.58D));
        level.playSound(null, caster.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.55F, 1.35F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                projectile.getX(), projectile.getY(0.5D), projectile.getZ(),
                3, 0.14D, 0.14D, 0.14D, 0.025D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_STALKER_HEX_COLOR, 18, 0.88F, 0.08F),
                    projectile.getX(), projectile.getY(0.5D), projectile.getZ(),
                    2, 0.12D, 0.12D, 0.12D, 0.01D
            );
        }
        // Subtle in-flight singularity drag on nearby loose items!
        if (projectile.tickCount % 3 == 0) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, projectile.getBoundingBox().inflate(2.2D), Entity::isAlive)) {
                Vec3 pull = projectile.position().subtract(item.position());
                if (pull.lengthSqr() > 0.09D) {
                    item.setDeltaMovement(item.getDeltaMovement().scale(0.5D).add(pull.normalize().scale(0.22D)));
                    item.hasImpulse = true;
                }
            }
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
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_SINGULARITY_COLLAPSED).orElse(false)) return;
        pTag.putBoolean(TAG_SINGULARITY_COLLAPSED, true);
        Vec3 impactCenter = hitEntity.getBoundingBox().getCenter();
        collapseSingularityOrb(level, impactCenter, caster, hitEntity, spell, slot);
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
        if (projectile.isWallPhasing()) {
            return; // Full Void-Warp Phasing Flow continues through solid terrain until entity hit or max range!
        }
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_SINGULARITY_COLLAPSED).orElse(false)) return;
        pTag.putBoolean(TAG_SINGULARITY_COLLAPSED, true);
        collapseSingularityOrb(level, hitResult.getLocation(), caster, null, spell, slot);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_SINGULARITY_COLLAPSED).orElse(false)) return;
        pTag.putBoolean(TAG_SINGULARITY_COLLAPSED, true);
        collapseSingularityOrb(level, projectile.position(), caster, null, spell, slot);
    }

    /**
     * Collapses a <i>Singularity Rift Orb</i> across a 4.0-block radius:
     * broadcasts a 3D collapsing black-hole sphere, pulls adjacent mobs &amp; loose items inward,
     * and deals Void Materia damage (+1 vs Air, -1 vs Earth).
     */
    public static void collapseSingularityOrb(
            ServerLevel level,
            Vec3 center,
            @Nullable LivingEntity caster,
            @Nullable Entity directHitEntity,
            SpellAssembly spell,
            @Nullable SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        double collapseRadius = 4.0D;
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float collapseDmg = Math.max(2.8F + tier * 0.85F,
                MateriaStageTable.getBaseCombatDamageHp(tier) * 0.62F) * flawPot;

        // 3D collapsing Singularity Event Horizon + inward accretion ribbons!
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.subtract(0.0D, 0.75D, 0.0D), collapseRadius, true, EssenceType.VOID, null, 36, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.VOID, null, (float) collapseRadius, 38, tier
        );

        AABB box = new AABB(
                center.x - collapseRadius, center.y - 2.6D, center.z - collapseRadius,
                center.x + collapseRadius, center.y + 3.2D, center.z + collapseRadius
        );
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, Entity::isAlive)) {
            Vec3 toCore = center.subtract(item.position());
            if (toCore.lengthSqr() > 0.09D) {
                item.setDeltaMovement(toCore.normalize().scale(0.60D));
                item.hasImpulse = true;
            }
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == caster) continue;
            double dist = e.position().distanceTo(center);
            if (dist > collapseRadius + 0.45D) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    e, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            float falloff = (float) Mth.clamp(1.0D - (dist / collapseRadius) * 0.45D, 0.55D, 1.0D);
            if (!SpellCombatReactionEngine.isBossEntity(level, e)) {
                Vec3 toCenter = center.subtract(e.position());
                double len = toCenter.length();
                if (len > 0.25D) {
                    Vec3 inward = toCenter.scale(1.0D / len).scale(Math.min(1.20D, 0.42D + len * 0.24D));
                    SpellExecutionEngine.applySyncedMotion(e, new Vec3(inward.x, 0.30D, inward.z));
                }
            } else if (!spell.prefix().enforcesZeroEntityHarm()) {
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, e, caster, (1.8F + tier * 0.65F) * falloff, false, 0.25F, true
                );
            }

            // Deal Void Materia collapse damage to all pulled adjacent mobs (and to directHitEntity if slot lacked HARM)!
            if (!spell.prefix().enforcesZeroEntityHarm() && !(hasHarm && e == directHitEntity)) {
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, e, caster, collapseDmg * falloff, false, 0.20F, true
                );
            }
        }

        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.35D, center.z,
                34, 0.65D, 0.55D, 0.65D, 0.14D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_ACCRETION_ACCENT_HEX, 30, 1.15F, 0.12F),
                center.x, center.y + 0.35D, center.z, 14, 0.75D, 0.55D, 0.75D, 0.03D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.90F, 0.65F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.55F, 1.25F);
    }

    /**
     * Determines whether {@code state} at {@code pos} is a thin or permeable obstacle that
     * <i>Singularity Rift Orb</i> and <i>Void-Warp Relay</i> phase cleanly through without premature detonation!
     */
    public static boolean isThinVoidPassableObstacle(
            Level level,
            BlockPos pos,
            BlockState state,
            Vec3 travelDir
    ) {
        if (state.isAir() || state.getCollisionShape(level, pos).isEmpty()) {
            return true;
        }
        Block block = state.getBlock();
        if (block instanceof IronBarsBlock
                || block instanceof StainedGlassPaneBlock
                || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.GLASS)
                || state.is(Blocks.TINTED_GLASS)
                || state.is(BlockTags.IMPERMEABLE)
                || block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof WallBlock
                || block instanceof LeavesBlock
                || state.is(BlockTags.LEAVES)
                || block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof ChainBlock
                || block instanceof WebBlock
                || block instanceof ScaffoldingBlock
                || block instanceof BambooStalkBlock
                || block instanceof SugarCaneBlock
                || block instanceof BannerBlock
                || block instanceof WallBannerBlock
                || block instanceof SignBlock
                || block instanceof WallSignBlock) {
            return true;
        }
        // Also phase through a 1-block-thin partition wall if open space exists immediately on the far side
        // AND travel is primarily horizontal (so floor shots still collapse cleanly on floors!).
        if (Math.abs(travelDir.y) < 0.60D
                && state.getDestroySpeed(level, pos) >= 0.0F
                && !state.is(Blocks.BEDROCK)
                && !state.is(Blocks.OBSIDIAN)) {
            Direction stepDir = Direction.getApproximateNearest(travelDir.x, travelDir.y, travelDir.z);
            BlockPos behind = pos.relative(stepDir);
            return level.getBlockState(behind).getCollisionShape(level, behind).isEmpty();
        }
        return false;
    }

    // =========================================================================
    // FORM MUTATION 2 — GRAVITATIONAL TRACTOR BEAM (BEAM)
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
        // Section 4.2 & 6.11 Gravitational Tractor Beam (#41304E):
        // Sustained stone-piercing violet ray that pulls struck entities steadily toward the caster
        // (or suspends them weightlessly in mid-air ~2.85m in front of the caster while channeled),
        // dealing continuous Void Materia damage (+1 vs Air, -1 vs Earth)!
        int tier = spell.getEffectiveTier();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float baseBeamDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.25F, apparatusPotency) * flawPot;

        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 muzzle = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D).add(normDir.scale(0.35D));
        Vec3 beamEnd = muzzle.add(normDir.scale(maxReach)); // Void beams pierce solid stone (Section 4.2)!

        // Flexible 45° tractor lock so suspended / reeled-in targets don't slip out of the beam on small aim shifts!
        List<LivingEntity> targets = new ArrayList<>(hitEntities);
        if (targets.isEmpty()) {
            AABB corridor = new AABB(muzzle, beamEnd).inflate(1.30D);
            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, corridor, LivingEntity::isAlive)) {
                if (cand == caster) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                Vec3 toCand = cand.getBoundingBox().getCenter().subtract(muzzle);
                double len = toCand.length();
                if (len <= maxReach + 0.5D && len > 1.0E-4D && toCand.scale(1.0D / len).dot(normDir) >= 0.707D
                        && distanceToRaySegment(cand.getBoundingBox().getCenter(), muzzle, normDir, maxReach) <= 1.25D) {
                    targets.add(cand);
                }
            }
        }

        Vec3 visualEnd = !targets.isEmpty()
                ? targets.getFirst().getBoundingBox().getCenter()
                : (blockHit.getType() == HitResult.Type.BLOCK && !spell.flowPart().isVoidWarpAuthorized(spell.materiaSpecific(), fallbackMateria)
                    ? beamEnd : beamEnd);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_BEAM,
                muzzle, visualEnd, EssenceType.VOID, null,
                0.30F + tier * 0.02F, VFX_TRACTOR_BEAM_TICKS, tier
        );

        // Also reel loose ItemEntities along the tractor beam straight to the caster!
        AABB itemCorridor = new AABB(muzzle, beamEnd).inflate(1.25D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, itemCorridor, Entity::isAlive)) {
            if (distanceToRaySegment(item.position(), muzzle, normDir, maxReach) <= 1.35D) {
                Vec3 toCaster = caster.position().add(0.0D, 0.65D, 0.0D).subtract(item.position());
                if (toCaster.lengthSqr() > 0.25D) {
                    item.setDeltaMovement(toCaster.normalize().scale(0.68D));
                    item.hasImpulse = true;
                }
            }
        }

        // Equilibrium suspension anchor 2.85m along caster's aim ray (allows lifting & holding foes suspended in mid-air!)
        Vec3 suspensionAnchor = caster.getEyePosition().add(normDir.scale(2.85D)).subtract(0.0D, 0.55D, 0.0D);

        Set<UUID> seen = new HashSet<>();
        boolean tractoredAny = false;
        for (LivingEntity hit : targets) {
            if (!hit.isAlive() || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            tractoredAny = true;

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                double distFromCaster = hit.position().distanceTo(caster.position());
                Vec3 toAnchor = suspensionAnchor.subtract(hit.position());
                double anchorDist = toAnchor.length();
                if (distFromCaster > 3.6D && anchorDist > 0.65D) {
                    // Steadily reel distant target along the tractor ray toward the suspension point!
                    Vec3 pull = toAnchor.scale(1.0D / anchorDist).scale(Math.min(1.05D, 0.45D + anchorDist * 0.12D));
                    SpellExecutionEngine.applySyncedMotion(hit, new Vec3(pull.x, Math.max(0.22D, pull.y), pull.z));
                } else {
                    // Hold target weightlessly suspended in mid-air right in front of the beam!
                    Vec3 holdVel = toAnchor.scale(0.36D);
                    SpellExecutionEngine.applySyncedMotion(hit, new Vec3(
                            Mth.clamp(holdVel.x, -0.45D, 0.45D),
                            Mth.clamp(holdVel.y + 0.045D, -0.25D, 0.42D),
                            Mth.clamp(holdVel.z, -0.45D, 0.45D)
                    ));
                }
                hit.resetFallDistance();
                if (hit instanceof Mob mob) {
                    mob.getNavigation().stop();
                }
            } else if (!spell.prefix().enforcesZeroEntityHarm()) {
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, hit, caster, Math.max(1.2F, baseBeamDmg * 0.35F), false, 0.25F, false
                );
            }

            // Stream cosmic #41304E / #C77DFF tractor particles from target toward caster
            Vec3 fromVictim = hit.getBoundingBox().getCenter();
            for (int i = 1; i <= 3; i++) {
                Vec3 pt = fromVictim.lerp(muzzle, i * 0.26D);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_ACCRETION_ACCENT_HEX, 18, 0.85F, 0.06F),
                        pt.x, pt.y, pt.z, 1, 0.05D, 0.05D, 0.05D, 0.01D
                );
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, fromVictim.x, fromVictim.y, fromVictim.z,
                    4, 0.20D, 0.25D, 0.20D, 0.04D);

            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, hit, caster, Math.max(1.8F, baseBeamDmg * 0.72F), false, 0.20F, false
                );
            }
        }
        if (tractoredAny && level.getGameTime() % 8L == 0L) {
            level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.50F, 1.55F);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SINGULARITY IMPLOSION (RADIAL BURST)
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
        // Section 6.11 Singularity Implosion:
        // 360-degree inward gravitational collapse (6-block+ radius) dragging all surrounding mobs
        // directly toward the epicenter and dealing Void Materia damage (+1 vs Air, -1 vs Earth)!
        double effRadius = Math.max(6.0D, radius);
        int tier = spell.getEffectiveTier();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float baseImplosionDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.40F, apparatusPotency) * 0.80F * flawPot;

        // 3D collapsing Black-Hole Event Horizon Sphere + inward spiraling suction ribbons!
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center, effRadius, true, EssenceType.VOID, null, VFX_IMPLOSION_NOVA_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center.add(0.0D, 0.06D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.VOID, null, (float) effRadius, VFX_IMPLOSION_NOVA_TICKS, tier
        );

        AABB box = new AABB(
                center.x - effRadius, center.y - 2.6D, center.z - effRadius,
                center.x + effRadius, center.y + 3.4D, center.z + effRadius
        );
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, Entity::isAlive)) {
            Vec3 toCenter = center.subtract(item.position());
            if (toCenter.lengthSqr() > 0.09D) {
                item.setDeltaMovement(toCenter.normalize().scale(0.72D));
                item.hasImpulse = true;
            }
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == caster) continue;
            double dist = e.position().distanceTo(center);
            if (dist > effRadius + 0.50D) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, effRadius)) * 0.55D, 0.45D, 1.0D);
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, e, caster, Math.max(3.0F, baseImplosionDmg * falloff), false, 0.25F, true
                );
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, e)) {
                Vec3 toCenter = center.subtract(e.position());
                double len = toCenter.length();
                if (len > 0.25D) {
                    // Calibrated distance-proportional inward pull so mobs snap right into the epicenter without overshooting!
                    double speed = Math.min(1.55D, 0.45D + len * 0.22D);
                    Vec3 inward = toCenter.scale(speed / len);
                    SpellExecutionEngine.applySyncedMotion(e, new Vec3(inward.x, 0.36D, inward.z));
                }
            } else if (!spell.prefix().enforcesZeroEntityHarm()) {
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, e, caster, (2.4F + tier * 0.75F) * falloff, false, 0.30F, true
                );
            }
        }

        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.85D, center.z,
                52, effRadius * 0.38D, 0.65D, effRadius * 0.38D, 0.16D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_STALKER_HEX_COLOR, 36, 1.25F, 0.14F),
                center.x, center.y + 0.75D, center.z, 22, effRadius * 0.32D, 0.50D, effRadius * 0.32D, 0.03D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.05F, 0.60F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.75F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 4 — SPATIAL DESTABILIZATION STRIKE (TOUCH / STRIKE)
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
        // Section 6.11 Spatial Destabilization Strike:
        // Infuses melee strike with armor-piercing Void Materia damage (stacking held weapon Materia Advantage)
        // and destabilizes the victim in space for 10.0s (200t), reusing Void Materia toxicity displacement!
        int tier = spell.getEffectiveTier();
        long until = level.getGameTime() + 200L;
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float strikeDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, apparatusPotency) * flawPot;
        Vec3 slashDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D), slashDir, 2.85D, EssenceType.VOID, null, tier
        );

        var voidTearHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.VOID_TEAR.get());
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, hit, caster, strikeDmg, true, 0.35F, true
                );
            }

            if (! hit.isAlive()) continue;
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, until);
            hit.addEffect(new MobEffectInstance(voidTearHolder, 200, 0));

            // Immediate initial Void toxicity phase-displacement on strike (unless anchored or boss)!
            if (!SpellCombatReactionEngine.isBlinkAnchored(hit, level.getGameTime())
                    && !SpellCombatReactionEngine.isBossEntity(level, hit)) {
                Vec3 before = hit.position();
                double d0 = hit.getX() + (level.random.nextDouble() - 0.5D) * 16.0D;
                double d1 = hit.getY() + (double) (level.random.nextInt(16) - 8);
                double d2 = hit.getZ() + (level.random.nextDouble() - 0.5D) * 16.0D;
                if (hit.randomTeleport(d0, d1, d2, true)) {
                    hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
                    SpellWorldVfxManager.broadcastBlinkRift(level, before, hit.position(), EssenceType.VOID, tier);
                }
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, hit.getX(), hit.getY(0.75D), hit.getZ(),
                    18, 0.32D, 0.42D, 0.32D, 0.08D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, 1.15F);
    }

    // =========================================================================
    // FORM MUTATION 5 — ZERO-G LEVITATION AURA (AURA / SELF)
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
        // Section 6.11 Zero-G Levitation Aura:
        // Registers defense as Void (+1 vs Air -> 0.50x, -1 vs Earth -> 1.50x) and confers personal weightlessness,
        // high-float jumps, smooth slow-falling glide, and cosmic #41304E violet particles!
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        long now = level.getGameTime();

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_VOID_ZERO_G_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        caster.resetFallDistance();
        caster.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.WEIGHTLESSNESS.get()), dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 2, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.75F, null);
        SpellWorldVfxManager.broadcastWardShell(
                level, caster.position(), Math.max(1.35D, caster.getBbWidth() * 1.35D),
                EssenceType.VOID, null, Math.min(dur, 120), tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                caster.position().add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.VOID, null, 2.8F, 52, tier
        );
        level.sendParticles(ParticleTypes.PORTAL, caster.getX(), caster.getY(0.8D), caster.getZ(),
                28, 0.45D, 0.65D, 0.45D, 0.16D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_STALKER_HEX_COLOR, 34, 1.05F, 0.10F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 14, 0.55D, 0.55D, 0.55D, 0.02D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.65F, 1.45F);
    }

    // =========================================================================
    // FORM MUTATION 6 — NULL-ZONE SILENCING MINE (GLYPH / TRAP)
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
        // Section 4.6 & 6.11 Null-Zone Silencing Mine:
        // - Surface-snapped cosmic trap (1.0s arming delay, 10m or Tier 5+ indefinite lifetime, 50% Sneak-Right-Click disarm refund)
        // - Supports both Single-Use Detonator (100%) and Repeating Glyphic Sentry (3 pulses @ 60%, 2.0s re-arm)
        // - Releases a 6-block pulse of cosmic distortion disabling mob abilities and silencing player spellcasting for 5.0s (100t)!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceBlock = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(glyphCenter.x, surfaceBlock.getY() + 1.02D, glyphCenter.z);
        double padRad = Math.max(2.6D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeatingSentry = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        CompoundTag casterTag = EntityHelper.getPersistentData(caster);
        String pulseKey = TAG_REPEATING_PULSES_LEFT + "_" + level.getGameTime();
        String cdKey = TAG_REPEATING_COOLDOWN_UNTIL + "_" + level.getGameTime();
        if (repeatingSentry) {
            casterTag.putInt(pulseKey, 3);
            casterTag.putLong(cdKey, 0L);
        }

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                snappedCenter, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.VOID, null, 2.3F, 48, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_PERTHRO.get(), VOID_STALKER_HEX_COLOR, 32, 0.90F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 5, 0.45D, 0.05D, 0.45D, 0.005D
        );
        level.playSound(null, BlockPos.containing(snappedCenter), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 0.55F, 1.35F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, padRad, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 25 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.RUNE_PERTHRO.get(), VOID_STALKER_HEX_COLOR, 26, 0.82F, 0.03F),
                                zone.center.x, zone.center.y + 0.06D, zone.center.z, 2, 0.55D, 0.03D, 0.55D, 0.002D
                        );
                    }
                    if (elapsed < 20) return true; // 1.0s arming delay (Section 4.6)

                    LivingEntity owner = zone.resolveCaster(srv);
                    long now = srv.getGameTime();
                    if (repeatingSentry && owner != null) {
                        long cdUntil = EntityHelper.getPersistentData(owner).getLong(cdKey).orElse(0L);
                        if (now < cdUntil) return true;
                    }

                    AABB triggerBox = new AABB(
                            zone.center.x - 2.3D, zone.center.y - 1.0D, zone.center.z - 2.3D,
                            zone.center.x + 2.3D, zone.center.y + 2.3D, zone.center.z + 2.3D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            e, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard()))
                    );
                    if (stepped.isEmpty()) return true;

                    float pulseScale = repeatingSentry ? 0.60F : 1.0F;
                    int silenceTicks = Math.max(60, Math.round(100.0F * pulseScale)); // 5.0s (100t) on Single-Use Detonator!
                    double silenceRad = 6.0D;
                    boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    float mineDmg = Math.max(3.6F + tier * 1.15F,
                            MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, zone.apparatusPotency) * 0.75F)
                            * flawPot * pulseScale;

                    // 3D 6-block Null-Zone Cosmic Distortion Pulse!
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center, silenceRad, false, EssenceType.VOID, null, VFX_NULL_MINE_BLAST_TICKS, tier
                    );
                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                            zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.VOID, null, (float) silenceRad, VFX_NULL_MINE_BLAST_TICKS, tier
                    );
                    srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.85D, zone.center.z,
                            46, 1.8D, 0.65D, 1.8D, 0.14D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 0.72F);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.90F, 0.85F);

                    AABB silenceBox = new AABB(
                            zone.center.x - silenceRad, zone.center.y - 2.2D, zone.center.z - silenceRad,
                            zone.center.x + silenceRad, zone.center.y + 3.6D, zone.center.z + silenceRad
                    );
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, silenceBox, LivingEntity::isAlive)) {
                        if (v == owner || v.position().distanceTo(zone.center) > silenceRad + 0.5D) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }

                        SpellLingeringZoneManager.applyArcaneSilence(v, silenceTicks);
                        CompoundTag vTag = EntityHelper.getPersistentData(v);
                        vTag.putDouble(SpellLingeringZoneManager.TAG_SILENCED_ANCHOR_X, v.getX());
                        vTag.putDouble(SpellLingeringZoneManager.TAG_SILENCED_ANCHOR_Y, v.getY());
                        vTag.putDouble(SpellLingeringZoneManager.TAG_SILENCED_ANCHOR_Z, v.getZ());
                        if (v.isUsingItem()) {
                            v.stopUsingItem();
                        }
                        if (v instanceof Mob mob) {
                            mob.setAggressive(false);
                        } else if (v instanceof Player silencedPlayer) {
                            silencedPlayer.displayClientMessage(
                                    Component.literal("§5[Null-Zone] §7Cosmic distortion has silenced your spellcasting for §f"
                                            + String.format("%.1fs", silenceTicks / 20.0F) + "§7!"),
                                    true
                            );
                        }

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * pulseScale
                            );
                        }
                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                            SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                                    srv, v, owner, mineDmg, false, 0.20F, true
                            );
                        }
                    }

                    if (repeatingSentry && owner != null) {
                        CompoundTag oTag = EntityHelper.getPersistentData(owner);
                        int rem = oTag.getInt(pulseKey).orElse(1) - 1;
                        if (rem > 0) {
                            oTag.putInt(pulseKey, rem);
                            oTag.putLong(cdKey, now + 40L); // 2.0s re-arm delay (Section 4.6)
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

    // =========================================================================
    // FORM MUTATION 7 — NULL-MATTER DISPEL SPRAY (CONE / SPRAY)
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
        // Section 6.11 Null-Matter Dispel Spray:
        // Blasts a 60-degree wave of dark void particles that strips positive potion buffs (and temporary wards)
        // from struck enemies and deals Void Materia damage (+1 vs Air, -1 vs Earth)!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double effRange = Math.max(8.0D, coneRange);
        Vec3 origin = caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                origin, normDir, EssenceType.VOID, null,
                (float) effRange, VFX_DISPEL_CONE_TICKS, tier
        );

        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float baseConeDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.40F, apparatusPotency) * 0.76F * flawPot;

        Set<UUID> seen = new HashSet<>();
        List<LivingEntity> candidates = new ArrayList<>(hitTargets);
        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(effRange)).inflate(effRange * 0.55D);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (cand == caster) continue;
            Vec3 to = cand.getBoundingBox().getCenter().subtract(origin);
            double dist = to.length();
            if (dist <= effRange + 0.4D && dist > 1.0E-4D && to.scale(1.0D / dist).dot(normDir) >= 0.50D) {
                candidates.add(cand);
            }
        }

        int totalStripped = 0;
        for (LivingEntity hit : candidates) {
            if (!hit.isAlive() || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            List<MobEffectInstance> copy = new ArrayList<>(hit.getActiveEffects());
            for (MobEffectInstance inst : copy) {
                if (inst.getEffect().value().isBeneficial()) {
                    hit.removeEffect(inst.getEffect());
                    totalStripped++;
                }
            }
            if (hit.getAbsorptionAmount() > 0.0F) {
                hit.setAbsorptionAmount(0.0F);
                totalStripped++;
            }

            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                // Bonus null-matter feedback per stripped beneficial buff!
                float dispelBonus = Math.min(6.0F, totalStripped * 0.75F);
                SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                        level, hit, caster, Math.max(2.6F, baseConeDmg + dispelBonus), false, 0.20F, true
                );
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, hit.getX(), hit.getY(0.85D), hit.getZ(),
                    10, 0.28D, 0.35D, 0.28D, 0.06D);
        }

        Vec3 midCone = origin.add(normDir.scale(effRange * 0.45D));
        level.sendParticles(ParticleTypes.PORTAL, midCone.x, midCone.y, midCone.z,
                34, 0.95D, 0.55D, 0.95D, 0.20D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_STALKER_HEX_COLOR, 32, 1.18F, 0.14F),
                midCone.x, midCone.y, midCone.z, 16, 0.85D, 0.45D, 0.85D, 0.03D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.85F, 0.90F);
        level.playSound(null, caster.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.75F, 0.75F);
    }

    // =========================================================================
    // FORM MUTATION 8 — SINGULARITY VORTEX RIFT (RAIN / SKYFALL)
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
        // Section 4.8 & 6.11 Singularity Vortex Rift:
        // Opens a cave-ceiling-adaptive dark cosmic rift in the sky for 6.0 seconds (120t) across a 12-block+ zone
        // that exerts an upward gravitational pull lifting mobs toward the vortex while dealing Void Materia damage,
        // before snapping shut and dropping them for full vertical fall damage when the rift expires!
        int tier = spell.getEffectiveTier();
        double riftRad = Math.max(6.0D, radius); // >= 6.0m radius = 12.0m+ diameter zone!
        BlockPos surfacePos = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 anchoredCenter = new Vec3(impactCenter.x, surfacePos.getY() + 1.0D, impactCenter.z);
        double clearance = probeOverheadClearance(level, BlockPos.containing(anchoredCenter), 10);
        double vortexHeight = Math.max(3.6D, clearance - 0.65D);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                anchoredCenter, new Vec3(0.0D, -1.0D, 0.0D),
                EssenceType.VOID, null, (float) riftRad, VFX_VORTEX_RIFT_TICKS, tier
        );
        level.playSound(null, BlockPos.containing(anchoredCenter), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.95F, 0.72F);
        level.playSound(null, BlockPos.containing(anchoredCenter), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, 0.62F);

        Map<UUID, Double> peakLiftAboveFloor = new HashMap<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, riftRad, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    Vec3 skyEye = zone.center.add(0.0D, vortexHeight, 0.0D);
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB area = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + vortexHeight + 2.5D, zone.center.z + zone.radius
                    );

                    // Final expiration tick (t = 120): snap the cosmic rift shut and drop all lifted mobs for real fall damage!
                    if (elapsed >= zone.maxDurationTicks) {
                        SpellWorldVfxManager.broadcastVacuumSphere(
                                srv, skyEye.subtract(0.0D, 0.8D, 0.0D), Math.min(5.0D, zone.radius * 0.75D),
                                true, EssenceType.VOID, null, 28, tier
                        );
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                                SoundSource.PLAYERS, 1.0F, 0.75F);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }
                            if (SpellCombatReactionEngine.isBossEntity(srv, v)) continue;
                            v.removeEffect(MobEffects.SLOW_FALLING);
                            v.removeEffect(MobEffects.LEVITATION);
                            double lifted = Math.max(v.getY() - zone.center.y, peakLiftAboveFloor.getOrDefault(v.getUUID(), 0.0D));
                            if (lifted > 1.25D || !v.onGround()) {
                                // Prime fallDistance so even indoor low-ceiling vortex drops deal genuine vertical fall crunch!
                                v.fallDistance = Math.max(v.fallDistance, (float) Math.max(5.0D, lifted + 3.0D));
                                Vec3 cur = v.getDeltaMovement();
                                SpellExecutionEngine.applySyncedMotion(v, new Vec3(cur.x * 0.25D, -0.42D, cur.z * 0.25D));
                            }
                        }
                        return false;
                    }

                    if (elapsed % 4 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, skyEye.x, skyEye.y, skyEye.z,
                                20, zone.radius * 0.35D, 0.45D, zone.radius * 0.35D, 0.14D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_STALKER_HEX_COLOR, 26, 1.15F, 0.10F),
                                skyEye.x, skyEye.y - 0.35D, skyEye.z,
                                8, zone.radius * 0.32D, 0.40D, zone.radius * 0.32D, 0.02D
                        );
                    }

                    // Continuous upward gravitational lift every 4 ticks during active vortex!
                    if (elapsed % 4 == 0) {
                        boolean damageTick = (elapsed > 0 && elapsed % 20 == 0);
                        boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                        float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                        float tickDmg = Math.max(2.0F + tier * 0.55F,
                                MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.35F, zone.apparatusPotency) * 0.30F) * flawPot;

                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            double horizDist = Math.sqrt(dx * dx + dz * dz);
                            if (horizDist > zone.radius + 0.5D) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }

                            if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                Vec3 toEye = skyEye.subtract(0.0D, 0.65D, 0.0D).subtract(v.position());
                                double distToEye = toEye.length();
                                Vec3 unit = distToEye > 1.0E-4D ? toEye.scale(1.0D / distToEye) : new Vec3(0.0D, 1.0D, 0.0D);
                                double upVel = v.getY() < (skyEye.y - 1.15D)
                                        ? Math.max(0.32D, unit.y * 0.48D)
                                        : Mth.clamp((skyEye.y - 0.85D - v.getY()) * 0.35D, -0.08D, 0.18D);
                                SpellExecutionEngine.applySyncedMotion(v, new Vec3(unit.x * 0.36D, upVel, unit.z * 0.36D));
                                double curLift = v.getY() - zone.center.y;
                                if (curLift > peakLiftAboveFloor.getOrDefault(v.getUUID(), 0.0D)) {
                                    peakLiftAboveFloor.put(v.getUUID(), curLift);
                                }
                            }

                            if (damageTick) {
                                float falloff = (float) Mth.clamp(1.0D - (horizDist / Math.max(1.0D, zone.radius)) * 0.50D, 0.50D, 1.0D);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.32F * falloff
                                    );
                                }
                                if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                                    SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                                            srv, v, owner, tickDmg * falloff, false, 0.15F, true
                                    );
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9–11 — VOID-STALKER EIDOLON SUITE (SUMMON)
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
        tag.putString("SpellEidolonMateria", EssenceType.VOID.name());
        tag.putInt("SpellEidolonTintRGB", VOID_STALKER_HEX_COLOR);

        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.25D, radius * 0.45D), EssenceType.VOID, null, tier
        );
        collapseSingularityOrb(level, spawnPos, caster, summonEntity, spell, slot);
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.11 Wormhole Transit:
        // Teleports the caster and leaves a persistent 5.0-second (100t) true two-way 3D wormhole portal pair
        // (departure <-> arrival) that allies, pets, summons, and the caster can walk through in either direction!
        long initNow = level.getGameTime();
        EntityHelper.getPersistentData(caster).putLong(TAG_WORMHOLE_COOLDOWN_UNTIL, initNow + 22L);

        // Persistent 5.2s 3D twin diamond wormhole portals + cosmic ground thresholds at both ends!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_BLINK_RIFT,
                departurePos, arrivalPos, EssenceType.VOID, null,
                1.35F, VFX_WORMHOLE_PORTAL_TICKS, potencyTier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                departurePos.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.VOID, null, 1.55F, VFX_WORMHOLE_PORTAL_TICKS, potencyTier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                arrivalPos.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.VOID, null, 1.55F, VFX_WORMHOLE_PORTAL_TICKS, potencyTier
        );
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 0.75F, 1.25F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, 1.15F);

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 1.55D, 100, initNow, dummy, dummy.subSpells().getFirst(), EssenceType.VOID, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 4 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                departurePos.x, departurePos.y + 1.0D, departurePos.z, 6, 0.28D, 0.58D, 0.28D, 0.04D);
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                arrivalPos.x, arrivalPos.y + 1.0D, arrivalPos.z, 6, 0.28D, 0.58D, 0.28D, 0.04D);
                    }
                    long now = srv.getGameTime();
                    LivingEntity owner = zone.resolveCaster(srv);

                    transitWormholeGate(srv, owner, departurePos, arrivalPos, now, potencyTier);
                    transitWormholeGate(srv, owner, arrivalPos, departurePos, now, potencyTier);
                    return true;
                }
        ));
    }

    private static void transitWormholeGate(
            ServerLevel srv,
            @Nullable LivingEntity owner,
            Vec3 entryPos,
            Vec3 exitPos,
            long now,
            int tier
    ) {
        AABB gateBox = new AABB(
                entryPos.x - 1.25D, entryPos.y - 0.5D, entryPos.z - 1.25D,
                entryPos.x + 1.25D, entryPos.y + 2.2D, entryPos.z + 1.25D
        );
        for (LivingEntity traveler : srv.getEntitiesOfClass(LivingEntity.class, gateBox, LivingEntity::isAlive)) {
            if (!isFriendlyWormholeTraveler(traveler, owner)) continue;
            if (SpellCombatReactionEngine.isBlinkAnchored(traveler, now)) continue;
            CompoundTag tData = EntityHelper.getPersistentData(traveler);
            if (now < tData.getLong(TAG_WORMHOLE_COOLDOWN_UNTIL).orElse(0L)) continue;

            tData.putLong(TAG_WORMHOLE_COOLDOWN_UNTIL, now + 22L);
            Vec3 keepVel = traveler.getDeltaMovement();
            traveler.teleportTo(exitPos.x, exitPos.y, exitPos.z);
            traveler.resetFallDistance();
            SpellExecutionEngine.applySyncedMotion(traveler, new Vec3(keepVel.x * 0.85D, Math.max(0.05D, keepVel.y), keepVel.z * 0.85D));

            SpellWorldVfxManager.broadcastBlinkRift(srv, entryPos, exitPos, EssenceType.VOID, tier);
            srv.sendParticles(ParticleTypes.REVERSE_PORTAL, exitPos.x, exitPos.y + 1.0D, exitPos.z,
                    18, 0.32D, 0.55D, 0.32D, 0.08D);
            srv.playSound(null, BlockPos.containing(exitPos), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.80F, 1.25F);
        }
    }

    private static boolean isFriendlyWormholeTraveler(LivingEntity traveler, @Nullable LivingEntity owner) {
        if (owner == null || traveler == owner) return true;
        if (traveler.isAlliedTo(owner)) return true;
        if (traveler instanceof TamableAnimal tam && tam.isOwnedBy(owner)) return true;
        if (traveler instanceof SpellEidolonEntity eid && eid.isFriendlyTo(owner)) return true;
        return traveler instanceof Player tp && owner instanceof Player op && !op.canHarmPlayer(tp);
    }

    // =========================================================================
    // ABYSSAL DISINTEGRATION & VOID-LINEAGE DIRECT-TO-INVENTORY / OMNI-SPONGE
    // =========================================================================

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 5.5 & 6.11 Abyssal Disintegration:
        // Dissolves organic blocks and loose organic items into raw void slag (Crying Obsidian / End Stone),
        // strictly protecting chests and block-entity containers!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.25D), Entity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        // 1. Dense organic trunks, turf, soil, gourds, hay, wool & giant mushrooms -> Crying Obsidian raw void slag!
        if (state.is(BlockTags.LOGS)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.WOOL)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MANGROVE_ROOTS)
                || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || state.is(Blocks.MELON)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.HAY_BLOCK)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.BAMBOO_BLOCK)
                || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(Blocks.RED_MUSHROOM_BLOCK)
                || state.is(Blocks.MUSHROOM_STEM)) {
            level.setBlockAndUpdate(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState());
            emitAbyssalSlagVfx(level, pos);
            return true;
        }

        // 2. Crafted organic lumber (planks, wooden stairs/slabs/fences) -> solid Obsidian void-glass slag!
        if (state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_FENCES)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            emitAbyssalSlagVfx(level, pos);
            return true;
        }

        // 3. Delicate flora (leaves, flowers, saplings, crops, grass, vines) -> dissolves into Crying Obsidian slag!
        if (state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.MOSS_CARPET)) {
            level.removeBlock(pos, false);
            ItemStack slag = new ItemStack(Items.CRYING_OBSIDIAN, 1);
            if (caster instanceof Player p && p.getInventory().add(slag.copy())) {
                // Direct-to-Inventory Void specialization!
            } else {
                Block.popResource(level, pos, slag);
            }
            emitAbyssalSlagVfx(level, pos);
            return true;
        }

        // 4. Mundane stone & cobblestone -> End-warped stone!
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.DEEPSLATE)) {
            level.setBlockAndUpdate(pos, Blocks.END_STONE.defaultBlockState());
            emitAbyssalSlagVfx(level, pos);
            return true;
        }

        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || stack.is(Items.CRYING_OBSIDIAN) || stack.is(Items.END_STONE)) {
            return false;
        }
        int count = stack.getCount();

        boolean isOrganic = stack.is(ItemTags.LOGS)
                || stack.is(ItemTags.DIRT)
                || stack.is(ItemTags.LEAVES)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.FLOWERS)
                || stack.is(ItemTags.SAPLINGS)
                || stack.is(ItemTags.WOOL)
                || stack.is(ItemTags.MEAT)
                || stack.is(ItemTags.FISHES)
                || stack.is(ModItems.RAW_AURORAFOWL.get())
                || stack.is(ModItems.COOKED_AURORAFOWL.get())
                || stack.has(DataComponents.FOOD)
                || stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.BONE)
                || stack.is(Items.BONE_MEAL)
                || stack.is(Items.LEATHER)
                || stack.is(Items.WHEAT)
                || stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.SUGAR_CANE)
                || stack.is(Items.KELP);

        if (isOrganic) {
            itemEntity.setItem(new ItemStack(Items.CRYING_OBSIDIAN, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    10, 0.22D, 0.22D, 0.22D, 0.06D);
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.55F, 1.35F);
            return true;
        }
        if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.END_STONE, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    8, 0.20D, 0.20D, 0.20D, 0.05D);
            return true;
        }
        return false;
    }

    /**
     * Guide Section 2.2 Void-Lineage Utility Specialization (<i>Direct-to-Inventory &amp; Omni-Sponge</i>):
     * Sponges up liquid source blocks into held empty buckets (and clears surrounding liquid flood pockets),
     * and teleports all mined block drops directly into the player's inventory!
     */
    public static boolean harvestBlockWithDirectToInventoryAndOmniSponge(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            float maxHardness,
            int potencyTier,
            boolean effectMutated,
            @Nullable MateriaPartMutationHandler effHandler
    ) {
        // 1. Omni-Sponge: sponge liquid source blocks at or immediately bordering the excavated pocket into held buckets!
        boolean spongedFluid = spongeFluidsIntoPlayerBuckets(level, caster, pos);
        BlockState refreshed = level.getBlockState(pos);
        if (refreshed.isAir()) {
            return spongedFluid;
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, refreshed)) {
            return spongedFluid;
        }

        // If Abyssal Disintegration mutation is toggled ON and matches an organic transmutation target, apply it first!
        if (effectMutated && effHandler != null && effHandler.onTransmuteBlock(level, caster, pos, refreshed, potencyTier)) {
            spongeAdjacentFloodPockets(level, caster, pos);
            return true;
        }

        float hardness = refreshed.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > maxHardness) {
            return spongedFluid;
        }

        BlockEntity be = level.getBlockEntity(pos);
        ItemStack tool = caster != null ? caster.getMainHandItem() : new ItemStack(Items.DIAMOND_PICKAXE);
        List<ItemStack> drops = Block.getDrops(refreshed, level, pos, be, caster,
                tool.isEmpty() ? new ItemStack(Items.DIAMOND_PICKAXE) : tool);
        level.levelEvent(2001, pos, Block.getId(refreshed));
        level.removeBlock(pos, false);

        if (caster instanceof Player player) {
            for (ItemStack drop : drops) {
                if (drop.isEmpty()) continue;
                ItemStack copy = drop.copy();
                if (!player.getInventory().add(copy) && !copy.isEmpty()) {
                    ItemEntity overflow = new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), copy);
                    overflow.setNoPickUpDelay();
                    level.addFreshEntity(overflow);
                }
            }
            collectFreshDropsDirectToInventory(level, player, pos, 1.75D);
        } else {
            for (ItemStack drop : drops) {
                Block.popResource(level, pos, drop);
            }
        }

        spongeAdjacentFloodPockets(level, caster, pos);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                10, 0.25D, 0.25D, 0.25D, 0.05D);
        return true;
    }

    public static void collectFreshDropsDirectToInventory(
            ServerLevel level,
            Player player,
            BlockPos origin,
            double radius
    ) {
        AABB box = new AABB(origin).inflate(radius);
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, box, e -> e.isAlive() && e.getAge() <= 5)) {
            ItemStack copy = ie.getItem().copy();
            if (player.getInventory().add(copy)) {
                if (copy.isEmpty()) {
                    ie.discard();
                } else {
                    ie.setItem(copy);
                    ie.setNoPickUpDelay();
                    ie.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
                }
            }
        }
        for (ExperienceOrb xp : level.getEntitiesOfClass(ExperienceOrb.class, box, Entity::isAlive)) {
            xp.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
        }
    }

    private static boolean spongeFluidsIntoPlayerBuckets(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos
    ) {
        BlockState bs = level.getBlockState(pos);
        boolean isWaterSource = bs.getFluidState().isSource() && bs.getFluidState().is(Fluids.WATER);
        boolean isLavaSource = bs.getFluidState().isSource() && bs.getFluidState().is(Fluids.LAVA);
        if (!isWaterSource && !isLavaSource) {
            return false;
        }

        if (bs.getBlock() instanceof BucketPickup pickup) {
            ItemStack filled = pickup.pickupBlock(caster instanceof Player p ? p : null, level, pos, bs);
            if (!filled.isEmpty() && caster instanceof Player player) {
                tryConsumeEmptyBucketAndGiveFilled(player, filled);
            }
        } else {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (caster instanceof Player player) {
                tryConsumeEmptyBucketAndGiveFilled(player, new ItemStack(isLavaSource ? Items.LAVA_BUCKET : Items.WATER_BUCKET));
            }
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                10, 0.25D, 0.25D, 0.25D, 0.05D);
        level.playSound(null, pos, isLavaSource ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL,
                SoundSource.PLAYERS, 0.70F, 1.20F);
        return true;
    }

    private static void spongeAdjacentFloodPockets(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos center
    ) {
        for (Direction dir : Direction.values()) {
            BlockPos adj = center.relative(dir);
            BlockState adjState = level.getBlockState(adj);
            if (adjState.getFluidState().isSource()
                    && (adjState.is(Blocks.WATER) || adjState.is(Blocks.LAVA))) {
                spongeFluidsIntoPlayerBuckets(level, caster, adj);
            } else if (adjState.hasProperty(BlockStateProperties.WATERLOGGED)
                    && adjState.getValue(BlockStateProperties.WATERLOGGED)) {
                level.setBlockAndUpdate(adj, adjState.setValue(BlockStateProperties.WATERLOGGED, false));
                if (caster instanceof Player player) {
                    tryConsumeEmptyBucketAndGiveFilled(player, new ItemStack(Items.WATER_BUCKET));
                }
            }
        }
    }

    private static void tryConsumeEmptyBucketAndGiveFilled(Player player, ItemStack filledBucket) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack slotStack = inv.getItem(i);
            if (slotStack.is(Items.BUCKET)) {
                slotStack.shrink(1);
                if (!inv.add(filledBucket.copy())) {
                    player.drop(filledBucket.copy(), false);
                }
                return;
            }
        }
    }

    private static void emitAbyssalSlagVfx(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                12, 0.28D, 0.28D, 0.28D, 0.07D);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRYING_OBSIDIAN.defaultBlockState()),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                10, 0.25D, 0.25D, 0.25D, 0.06D
        );
        level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.60F, 0.90F);
    }

    // =========================================================================
    // SPACETIME ANCHORING CURSE (AFFLICTION) & GRAVITY WELL (KINETIC / GRAVITY)
    // =========================================================================

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 5.6 & 6.11 Spacetime Anchoring Curse:
        // - Persistent milk-immune dimensional warping curse clinging until cleansed via Restoration/Earth magic or caster defeat
        // - Periodically rubber-band yanks the victim back to cursed coordinates every 10–20s (300t Stack I, 250t Stack II, 200t Stack III)
        // - Prevents teleportation and Chorus Fruit / Ender Pearl escapes
        // - Increases incoming Void damage by +50%, and triggers Spacetime Rift Beacon (2x Void-Stalker & Enderman spawns) on players!
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        int stack = SpellCombatReactionEngine.getHexStackLevel(target);
        long rubberbandInterval = Math.max(200L, 300L - (stack - 1) * 50L); // 15s (I) -> 12.5s (II) -> 10s (III)!

        if (!data.contains(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X)) {
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X, target.getX());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y, target.getY());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z, target.getZ());
            data.putLong("SpellVoidLastRubberbandTick", now);
        } else {
            double ax = data.getDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X).orElse(target.getX());
            double ay = data.getDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y).orElse(target.getY());
            double az = data.getDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z).orElse(target.getZ());
            long lastSnap = data.getLong("SpellVoidLastRubberbandTick").orElse(0L);
            if (now - lastSnap >= rubberbandInterval && target.distanceToSqr(ax, ay, az) > 2.25D) {
                Vec3 stretchedFrom = target.position();
                data.putLong("SpellVoidLastRubberbandTick", now);
                data.putLong(SpellLingeringZoneManager.TAG_VOID_RUBBERBAND_BYPASS_TICK, now);
                data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_X, ax);
                data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Y, ay);
                data.putDouble(SpellLingeringZoneManager.TAG_VOID_LAST_POS_Z, az);

                target.teleportTo(ax, ay, az);
                target.setDeltaMovement(Vec3.ZERO);
                target.resetFallDistance();

                SpellWorldVfxManager.broadcastBlinkRift(
                        level, stretchedFrom, new Vec3(ax, ay, az), EssenceType.VOID, Math.max(1, potencyTier)
                );
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, ax, ay + 0.85D, az,
                        24, 0.35D, 0.50D, 0.35D, 0.10D);
                level.playSound(null, BlockPos.containing(ax, ay, az), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.90F, 0.65F);
                if (target instanceof Player cursedPlayer) {
                    cursedPlayer.displayClientMessage(
                            Component.literal("§5[Spacetime Anchor] §7Rubber-band tether yanked you back to your cursed coordinates!"),
                            true
                    );
                }
            }
        }

        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        // Note: hurtWithVoidMateriaAdvantage includes the +50% Spacetime Anchoring Curse multiplier!
        SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                level, target, hexCaster, (1.10F + potencyTier * 0.42F) * mult, false, 0.15F, false
        );
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.75D), target.getZ(),
                6, 0.25D, 0.35D, 0.25D, 0.05D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_STALKER_HEX_COLOR, 24, 0.88F, 0.08F),
                target.getX(), target.getY(0.80D), target.getZ(), 2, 0.25D, 0.30D, 0.25D, 0.01D
        );
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
        if (effect == SpellCoreEffect.KINETIC || effect == SpellCoreEffect.GRAVITY) {
            spawnGravityWell(level, caster, target.position().add(0.0D, 0.35D, 0.0D), potencyTier, spell);
        }
    }

    /**
     * Guide Section 6.11 <i>Gravity Well</i>:
     * Conjures a stationary, non-destructive 3D black hole that pulls surrounding loose items
     * and non-boss mobs inward with strong gravitational suction!
     */
    public static void spawnGravityWell(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 wellCenter,
            int potencyTier,
            @Nullable SpellAssembly spell
    ) {
        int durationTicks = 100 + Math.clamp(potencyTier, 1, 10) * 10; // 5.5s (T1) up to 10.0s (T10)!
        double pullRadius = 6.5D;
        boolean wardSafe = spell == null || spell.hasWardFriendlyFireSafeguard();
        SpellAssembly zoneSpell = spell != null ? spell : SpellAssembly.createMagicMissileStarter(potencyTier);

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, wellCenter.subtract(0.0D, 0.65D, 0.0D), 3.2D, true, EssenceType.VOID, null, 38, potencyTier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                wellCenter.subtract(0.0D, 0.30D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.VOID, null, 4.2F, durationTicks, potencyTier
        );
        level.playSound(null, BlockPos.containing(wellCenter), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.85F, 0.72F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, wellCenter, pullRadius, durationTicks, level.getGameTime(),
                zoneSpell, zoneSpell.subSpells().getFirst(), EssenceType.VOID, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    // Re-pulse the 3D collapsing black-hole sphere every 28 ticks so the Gravity Well stays vividly visible!
                    if (elapsed > 0 && elapsed % 28 == 0 && (zone.maxDurationTicks - elapsed) >= 16) {
                        SpellWorldVfxManager.broadcastVacuumSphere(
                                srv, zone.center.subtract(0.0D, 0.65D, 0.0D), 3.2D, true,
                                EssenceType.VOID, null, 32, potencyTier
                        );
                    }
                    if (elapsed % 3 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                zone.center.x, zone.center.y + 0.35D, zone.center.z,
                                12, 0.32D, 0.32D, 0.32D, 0.07D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), VOID_STALKER_HEX_COLOR, 22, 1.05F, 0.10F),
                                zone.center.x, zone.center.y + 0.35D, zone.center.z,
                                4, 0.35D, 0.35D, 0.35D, 0.02D
                        );
                    }

                    if (elapsed % 2 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB pullBox = new AABB(
                                zone.center.x - pullRadius, zone.center.y - 3.0D, zone.center.z - pullRadius,
                                zone.center.x + pullRadius, zone.center.y + 4.0D, zone.center.z + pullRadius
                        );
                        for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, pullBox, Entity::isAlive)) {
                            Vec3 toWell = zone.center.subtract(item.position());
                            double dSq = toWell.lengthSqr();
                            if (dSq > 0.12D && dSq <= pullRadius * pullRadius) {
                                item.setDeltaMovement(toWell.normalize().scale(0.58D));
                                item.hasImpulse = true;
                            } else if (dSq <= 0.12D) {
                                item.setDeltaMovement(item.getDeltaMovement().scale(0.20D));
                            }
                        }
                        for (LivingEntity mob : srv.getEntitiesOfClass(LivingEntity.class, pullBox, LivingEntity::isAlive)) {
                            if (mob == owner || SpellCombatReactionEngine.isBossEntity(srv, mob)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(mob, owner, wardSafe)) {
                                continue;
                            }
                            Vec3 toWell = zone.center.subtract(mob.position());
                            double dist = toWell.length();
                            if (dist > 0.35D && dist <= pullRadius) {
                                Vec3 step = toWell.scale(1.0D / dist).scale(Math.min(0.62D, 0.25D + dist * 0.065D));
                                SpellExecutionEngine.applySyncedMotion(mob, new Vec3(step.x, Mth.clamp(step.y + 0.08D, -0.25D, 0.32D), step.z));
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    // =========================================================================
    // INTERNAL GEOMETRY & FUEL HELPERS
    // =========================================================================

    private static boolean slotExecutesHarmVerb(@Nullable SubSpellSlot slot) {
        if (slot == null) return false;
        if (slot.primaryEffect().effect() == SpellCoreEffect.HARM) return true;
        return slot.secondaryEffect().map(s -> s.effect() == SpellCoreEffect.HARM).orElse(false);
    }

    private static void refundSubLinearBatchFuelDiscount(@Nullable LivingEntity caster, int tier, int stackCount) {
        if (!(caster instanceof Player player) || stackCount <= 1) return;
        float discountFrac = Math.min(0.30F, 0.04F * (stackCount - 1));
        long refundMfum = Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * discountFrac);
        if (refundMfum > 0L) {
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.VOID, refundMfum);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        BlockPos probe = start;
        if (!level.getBlockState(probe).getCollisionShape(level, probe).isEmpty()) {
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

    private static double distanceToRaySegment(Vec3 point, Vec3 segStart, Vec3 normDir, double maxLen) {
        Vec3 rel = point.subtract(segStart);
        double proj = Mth.clamp(rel.dot(normDir), 0.0D, maxLen);
        return point.distanceTo(segStart.add(normDir.scale(proj)));
    }
}
