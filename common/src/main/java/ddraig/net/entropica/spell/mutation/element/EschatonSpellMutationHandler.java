package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModBlocks;
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
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for
 * <b>Eschaton Materia (#55 — Empyrean + Cataclysm + Terminus + Entropic, Tier 6 Singularity Fusion:
 * "The Omega Singularity")</b> (Spellcrafting System Guide Section 6.57).
 *
 * <p><b>Palette</b>: Event Horizon Void Black ({@code #0B0813}), Accretion Disk Searing Violet ({@code #7012A5}),
 * and Event Edge Chrono-Silver ({@code #D4C2FC}); text format code {@code "§5"}.
 *
 * <p><b>Tier 6 Singularity Scaling &amp; World Impact</b>: Wall-phasing delivery, 75% armor bypass,
 * 8 armor shredding, extended 30.0s + 15.0s Skyfall duration, 90.0s summon lifecycles
 * (140 HP Leviathan / 150 HP Spire / 20.0s 20m Micro-Nova Effigy), 20-block effect radii,
 * and Dimensional Event Horizon Vaporization (non-protected non-bedrock matter dissolved into pure Materia fuel).
 *
 * <p><b>Elemental Affinity</b>: Strong against {@code ALL LOWER-TIER ELEMENTS (+1)} |
 * Weak against {@code APOTHEOSIS (-1)} (Forced Single Weakness).
 */
public final class EschatonSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ESCHATON_VOID_BLACK_HEX_COLOR = 0x0B0813;
    public static final int ESCHATON_ACCRETION_VIOLET_HEX_COLOR = 0x7012A5;
    public static final int ESCHATON_CHRONO_SILVER_HEX_COLOR = 0xD4C2FC;

    public static final int[] ESCHATON_PALETTE = new int[]{
            ESCHATON_VOID_BLACK_HEX_COLOR,
            ESCHATON_ACCRETION_VIOLET_HEX_COLOR,
            ESCHATON_CHRONO_SILVER_HEX_COLOR
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.ESCHATON;
    }

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Event Horizon Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Manifests 4 persistent spinning dark-matter singularity cores (#0B0813 / #7012A5 / #D4C2FC) "
                        + "circling the primary spell or caster; phases through solid walls, intercepts and vaporizes "
                        + "100% of incoming projectiles directly into pure Materia fuel, and automatically discharges "
                        + "seeking mini-black holes at nearby hostiles (-50% Armor Toughness and true void damage)."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Black-Hole Railgun Dart";
            case BEAM -> "Dimensional Void-Fissure Beam & Omega Collapse";
            case RADIAL_BURST -> "Cosmic Event-Horizon Nova";
            case TOUCH_STRIKE -> "Reality-Cleaving Scythe";
            case AURA -> "Mantle of the Omega Singularity";
            case GLYPH -> "Singularity Anchor Sigil";
            case CONE -> "Accretion Singularity Torrent";
            case SKYFALL -> "Dimensional Inversion Starfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Launches an ultra-dense wall-phasing void dart forged from collapsed dark matter, violet "
                            + "accretion fire, and chrono-silver distortion halos (#0B0813 / #7012A5 / #D4C2FC); "
                            + "drills instantly through solid terrain and up to 12 targets (75% armor bypass), "
                            + "leaves trailing mini-singularities dragging enemies into its wake, and collapses on "
                            + "terminal impact into a 10-block Omega Event Horizon (vacuums mobs inward, vaporizes "
                            + "loose obstacles into fuel, -8 armor, and compresses matter into raw Materia droplets).";
            case BEAM ->
                    "Channels a colossal wall-phasing continuous ray of light-devouring void darkness, searing "
                            + "violet accretion jets, and chrono-silver lightning; melts 3 armor/sec, cleanly erases "
                            + "non-bedrock obstacles into Materia fuel, and after 5.0 continuous seconds triggers "
                            + "Omega Event Horizon Implosion (16m knockback, -8 armor, buff purge, and a 14-block "
                            + "gravitational tear pulling entities inward for 6.0s).";
            case RADIAL_BURST ->
                    "Wall-penetrating 16-block concussive shockwave of expanding black-hole event horizons, "
                            + "violet gravity rings, and chrono-silver shockwaves (zero light blocks); knocks "
                            + "hostiles 14m back flat, shatters 8 armor points, dissolves incoming projectiles "
                            + "into fuel, and grants party Mantle of the Omega Singularity (+14 Armor, +8 Toughness, "
                            + "debuff/fall/void immunity, and 8m gravitational vacuum aura for 20.0s).";
            case TOUCH_STRIKE ->
                    "Sweeping two-handed scythe strike woven of pure event horizons, swirling violet accretion "
                            + "fire, and silver spacetime cracks; cleaves cleanly through shields and solid walls "
                            + "(75% armor bypass, -8 armor, 8.0s shield disable), severs positive buffs, locks the "
                            + "primary victim in 4.0s event-horizon stasis, and pulls adjacent enemies into the slash arc.";
            case AURA ->
                    "Envelopes the caster in a dense event-horizon void field, swirling violet accretion disks, "
                            + "and chrono-silver distortion halos (mobile or Sneak-anchored); snuffs torches and "
                            + "lights within 10m, grants +14 Armor, +8 Toughness, complete fire/lava/drowning/freeze/"
                            + "wither/void immunity, -90% detection, 6m projectile-to-fuel disintegration, and an "
                            + "8m gravitational singularity aura (3.0 hearts/sec true damage).";
            case GLYPH ->
                    "Void-singularity mandala glyph on any floor, wall, or ceiling (disarmable, Single or Repeating); "
                            + "hostile approach within 6m locks entering mobs in place with unbreakable gravity "
                            + "tethers for 6.0s (-8 armor, strips positive buffs, and gravitationally pulls all "
                            + "nearby projectiles directly into the anchored targets).";
            case CONE ->
                    "Wall-penetrating cone of pressurized dark matter, violet relativistic plasma, and razor "
                            + "chrono-silver spacetime shards; shatters 8 armor points, vaporizes incoming "
                            + "projectiles into fuel, carves a safe tunnel through non-bedrock blocks, and inflicts "
                            + "Omega Spacetime Disorientation (35.0s AI disruption, -75% move speed, and helpless spinning).";
            case SKYFALL ->
                    "30.0s Dimensional Inversion Starfall across a 20-block+ inverted cosmic sky fissure dropping "
                            + "8 3D compressed obsidian & dark-matter singularities that permanently materialize on "
                            + "completion; lifts caught mobs into zero-g stasis while pulverizing them flat (-8 armor, "
                            + "vaporizes loose blocks into fuel), leaving a 15.0s Collapsed Reality Slag field "
                            + "(-80% speed, inward drag, and continuous crushing true damage).";
            case SUMMON_AGENT ->
                    "Summons a 140 HP Event-Horizon Leviathan (90s refreshing lifecycle) — colossal wall-phasing "
                            + "abyssal leviathan gliding through terrain, dragging nearby mobs and loose matter "
                            + "inward, delivering sweeping singularity cleaves (75% armor bypass, -5 armor/strike, "
                            + "and pulling distant enemies into its crushing jaws).";
            case SUMMON_SENTRY ->
                    "Summons a 150 HP Accretion Disk Singularity Spire (90s refreshing lifecycle) — bombards "
                            + "hostiles up to 24m with wall-penetrating singularity lances (-4 armor/hit) while "
                            + "pulsing a 20m Gravitational Accretion Field (drags hostiles & projectiles inward, -1.5 armor/s).";
            case SUMMON_DECOY ->
                    "Summons a Collapsing Micro-Nova Effigy (20s duration) — taunts hostiles across 20m, reflects "
                            + "50% incoming damage, and detonates in an apocalyptic 20m reality tear (vaporizes "
                            + "loose obstacles into fuel, massive true damage, -8 armor, and inward singularity pull).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Spacetime Fold Slip";
            case TRANSMUTE, BREAK -> "Dimensional Event Horizon Transmutation & Accretion Compression";
            case AFFLICTION -> "Omega Event Horizon Dissolution Hex";
            case WARD -> "Omega Event-Horizon Bastion";
            default -> "Omega Singularity " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Instantly and silently swaps positions with the furthest enemy within 24 blocks through solid "
                            + "walls (encasing the displaced enemy in a 4.0s event-horizon stasis bubble with all "
                            + "buffs purged); arrival grants complete invisibility, cloaking, 4.0s complete damage "
                            + "immunity, and 100% hostile mob aggro shed.";
            case TRANSMUTE, BREAK ->
                    "5.0s continuous channel across 16 blocks (air cannot be transmuted); erases loose non-bedrock "
                            + "obstacles into Materia fuel; transmutes stone/deepslate/netherrack into Crying Obsidian, "
                            + "Blackstone, or Void Slag; compresses cobble/stone/gravel into condensed mineral blocks; "
                            + "converts mob drops into pure essence; purges curses + elevates up to 2 enchants (+2); "
                            + "4m standing water + glass bottle yields 50/50 Gravitational Stasis Elixir or Essence of the Omega Horizon.";
            case AFFLICTION ->
                    "Milk-immune apocalyptic cosmic collapse curse (30.0s boss cap + 10.0s immunity, 6m conditional "
                            + "death contagion); inflicts true void-collapse damage, -85% move speed, blocks jumping "
                            + "and teleportation, -60% Armor Toughness, and 60% weapon self-recoil; Cosmic Reality "
                            + "Tear accelerates monster & void horror spawns by +200% across 36 blocks.";
            default ->
                    "Infuses " + effect.getDisplayName() + " with Tier 6 Singularity Eschaton Omega Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW SYNERGY — EVENT HORIZON ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        // Vaporize 100% of incoming physical & magical projectiles directly into pure Eschaton Materia fuel!
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, owner, projectile.getBoundingBox().inflate(2.8D), EssenceType.ESCHATON, 48L
        );

        // 4 persistent spinning dark-matter singularity cores (#0B0813 / #7012A5 / #D4C2FC)
        double baseAng = projectile.tickCount * 0.46D;
        for (int i = 0; i < 4; i++) {
            double ang = baseAng + (Math.PI * 0.5D * i);
            int col = ESCHATON_PALETTE[i % ESCHATON_PALETTE.length];
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 12, 0.32F, 0.03F),
                    projectile.getX() + Math.cos(ang) * 0.52D,
                    projectile.getY() + Math.sin(ang * 0.5D) * 0.25D,
                    projectile.getZ() + Math.sin(ang) * 0.52D,
                    1, 0.02D, 0.02D, 0.02D, 0.005D
            );
        }

        // Automatically discharge seeking mini-black holes at nearby hostiles (-50% Toughness & true void damage)!
        if (projectile.tickCount % 10 == 0) {
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(7.5D),
                    e -> e.isAlive() && e != owner && e instanceof Monster
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, true)))) {
                SpellWorldVfxManager.broadcastChainArc(
                        level, projectile.position(), foe.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                        EssenceType.ESCHATON, EssenceType.VOID, 9
                );
                SpellCombatReactionEngine.applyPercentageArmorShred(foe, level, 0.0D, 0.50D, 140L);
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, foe, owner, 4.40F, false, false, true);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, foe.getX(), foe.getY(0.75D), foe.getZ(),
                        8, 0.22D, 0.22D, 0.22D, 0.03D);
                break;
            }
        }
    }

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        Vec3 from = fromTarget.getEyePosition().subtract(0.0D, 0.2D, 0.0D);
        Vec3 to = toTarget.getEyePosition().subtract(0.0D, 0.2D, 0.0D);
        SpellWorldVfxManager.broadcastChainArc(level, from, to, EssenceType.ESCHATON, EssenceType.VOID, spell.getEffectiveTier());
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, new AABB(from, to).inflate(2.5D), EssenceType.ESCHATON, 48L);
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.50D, 140L);
            SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(
                    level, toTarget, caster, 4.60F + spell.getEffectiveTier() * 0.65F, false, false, true
            );
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Ultra-high-velocity zero-gravity wall-phasing Black-Hole Railgun Dart!
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D) {
            projectile.setDeltaMovement(vel.normalize().scale(Math.max(2.35D, vel.length() * 1.45D)));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.55F, 1.55F);
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.70F, 1.68F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Trailing line of mini-singularities that drag nearby enemies into its wake & dissolve solid terrain along flight!
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(4.0D), LivingEntity::isAlive)) {
            if (foe == owner) continue;
            if (owner != null && (ApexEschatonSingularityHelper.isFriendly(foe, owner)
                    || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true))) {
                continue;
            }
            Vec3 pull = projectile.position().subtract(foe.position()).scale(0.32D);
            foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.05D, pull.z));
            foe.hasImpulse = true;
        }

        if (projectile.isWallPhasing() && projectile.tickCount % 2 == 0) {
            BlockPos flightPos = BlockPos.containing(projectile.position());
            if (!level.getBlockState(flightPos).isAir()) {
                ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(
                        level, owner, flightPos, 1, EssenceType.ESCHATON, 16L
                );
            }
        }

        int col = ESCHATON_PALETTE[projectile.tickCount % ESCHATON_PALETTE.length];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 14, 0.36F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D
        );
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(),
                3, 0.08D, 0.08D, 0.08D, 0.03D);
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
        // Section 6.57 Black-Hole Railgun Dart:
        // Pierces solid terrain and up to 12 targets (75% armor bypass); terminal impact collapses into 10m Omega Event Horizon!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        int tier = spell.getEffectiveTier();
        if (hitEntity instanceof LivingEntity direct && direct.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(direct, caster, safeguard))) {
            SpellCombatReactionEngine.applyTimedAttributeShred(direct, level, 4.0D, 0.0D, 140L);
            if (!slot.hasHarmVerb()) {
                float dartDmg = (7.2F + tier * 1.75F) * Math.max(0.5F, spell.flawProfile().getPotencyMultiplier());
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, direct, caster, dartDmg, false, true, false);
            }
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            collapseOmegaEventHorizon(level, caster, hitEntity.position(), spell);
        }
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
        if (projectile.isWallPhasing()) {
            ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(
                    level, caster, blockHit.getBlockPos(), 1, EssenceType.ESCHATON, 16L
            );
        } else {
            collapseOmegaEventHorizon(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        collapseOmegaEventHorizon(level, caster, projectile.position(), spell);
    }

    private static void collapseOmegaEventHorizon(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        // Section 6.57 10-block Omega Event Horizon:
        // Violently vacuums all mobs inward, vaporizes loose obstacles into pure fuel,
        // shatters 8 armor points, deals massive true void collapse damage, and compresses matter into raw Materia droplets!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center.add(0.0D, 0.30D, 0.0D), 10.0D, EssenceType.ESCHATON, EssenceType.VOID, tier);
        SpellWorldVfxManager.broadcastVacuumSphere(level, center.add(0.0D, 0.80D, 0.0D), 10.0D, true, EssenceType.ESCHATON, EssenceType.UMBRAL, 46, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), 10.0D, EssenceType.ESCHATON, EssenceType.VOID, 80, tier);

        ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(
                level, caster, BlockPos.containing(center), 4, EssenceType.ESCHATON, 40L
        );

        AABB horizon = new AABB(
                center.x - 10.0D, center.y - 3.0D, center.z - 10.0D,
                center.x + 10.0D, center.y + 5.5D, center.z + 10.0D
        );
        float collapseDmg = 11.5F + tier * 2.35F;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, horizon, LivingEntity::isAlive)) {
            if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;

            Vec3 in = center.subtract(foe.position()).scale(0.55D);
            foe.setDeltaMovement(in.x, 0.25D, in.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 220L);
            SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, foe, caster, collapseDmg, false, false, true);
        }

        SpellLingeringZoneManager.spawnTypedEssenceOrb(level, center, EssenceType.ESCHATON, 3);
        emitEschatonBurstParticles(level, center.add(0.0D, 0.8D, 0.0D), 48, 3.0D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.15F, 0.62F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.68F);
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
        onBeamFormCast(level, caster, direction, maxReach, hitEntities, blockHit, spell, slot, fallbackMateria, apparatusPotency, 0);
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
            float apparatusPotency,
            int heldTicks
    ) {
        // Section 6.57 Dimensional Void-Fissure Beam & Omega Collapse:
        // Melts 3 armor/sec, cleanly erases non-bedrock obstacles in its path into pure Materia fuel;
        // Sustaining for 5.0 continuous seconds (100t, tracked on the CASTER) triggers Omega Event Horizon Implosion:
        // blasts all surviving hostiles 16 blocks back, shatters 8 armor points, purges all positive mob buffs,
        // and opens a 14-block gravitational tear pulling all entities inward for 6.0 seconds (120t)!
        Vec3 eye = caster.getEyePosition();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        boolean continuousPulse = heldTicks > 0 || apparatusPotency <= 0.45F;
        float tickFrac = continuousPulse ? 0.25F : 1.0F;

        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, caster, new AABB(eye, eye.add(dir.scale(maxReach))).inflate(2.6D), EssenceType.ESCHATON, 48L
        );

        // Cleanly erase non-protected non-bedrock obstacles along the void-fissure ray into Materia fuel
        for (int step = 3; step <= (int) Math.min(20.0D, maxReach); step += 4) {
            BlockPos stepPos = BlockPos.containing(eye.add(dir.scale(step)));
            if (stepPos.getY() >= caster.getBlockY()) {
                ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(level, caster, stepPos, 1, EssenceType.ESCHATON, 24L);
            }
        }
        if (blockHit != null) {
            ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(
                    level, caster, blockHit.getBlockPos(), 2, EssenceType.ESCHATON, 32L
            );
        }

        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        long lastCast = casterData.getLong("EschatonCasterBeamLastTick").orElse(-100L);
        int channelTicks;
        if (lastCast == now) {
            channelTicks = casterData.getInt("EschatonCasterBeamTicks").orElse(0);
        } else {
            int step = heldTicks > 0
                    ? ((lastCast >= 0L && now - lastCast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - lastCast)) : Math.min(heldTicks, 4))
                    : 0;
            channelTicks = heldTicks > 0
                    ? ((now - lastCast <= 28L) ? casterData.getInt("EschatonCasterBeamTicks").orElse(0) + step : step)
                    : 0;
            casterData.putLong("EschatonCasterBeamLastTick", now);
            casterData.putInt("EschatonCasterBeamTicks", channelTicks);
        }

        LivingEntity primaryFoe = null;
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            if (primaryFoe == null) primaryFoe = hit;

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            if (hData.getLong("EschatonBeamLastTick").orElse(-100L) == now) continue;
            hData.putLong("EschatonBeamLastTick", now);
            long foeLast = hData.getLong("EschatonFoeLastTick").orElse(-100L);
            int foeStep = heldTicks > 0
                    ? ((foeLast >= 0L && now - foeLast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - foeLast)) : Math.min(heldTicks, 4))
                    : 0;
            int foeTicks = heldTicks > 0
                    ? ((now - foeLast <= 28L) ? hData.getInt("EschatonFoeTicks").orElse(0) + foeStep : foeStep)
                    : 0;
            hData.putLong("EschatonFoeLastTick", now);
            hData.putInt("EschatonFoeTicks", foeTicks);

            int meltedArmor = Math.min(15, Math.max(3, (foeTicks / 20) * 3));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, meltedArmor, 0.0D, 100L);

            if (!slot.hasHarmVerb()) {
                float beamDmg = (6.2F + tier * 1.50F) * tickFrac;
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, hit, caster, beamDmg, false, true, false);
            }
        }

        // 5.0 continuous seconds (100 ticks) -> Omega Event Horizon Implosion!
        if (heldTicks >= 100 && channelTicks >= 100 && (primaryFoe != null || blockHit != null)) {
            casterData.putInt("EschatonCasterBeamTicks", 0);
            Vec3 epicenter = primaryFoe != null
                    ? primaryFoe.position()
                    : (blockHit != null ? blockHit.getLocation() : eye.add(dir.scale(Math.min(14.0D, maxReach))));

            SpellWorldVfxManager.broadcastRadialNova(level, epicenter.add(0.0D, 0.3D, 0.0D), 16.0D, EssenceType.ESCHATON, EssenceType.VOID, tier);
            SpellWorldVfxManager.broadcastVacuumSphere(level, epicenter.add(0.0D, 0.9D, 0.0D), 14.0D, true, EssenceType.ESCHATON, EssenceType.UMBRAL, 120, tier);
            SpellWorldVfxManager.broadcastArcaneSeal(level, epicenter.add(0.0D, 0.04D, 0.0D), 14.0D, EssenceType.ESCHATON, EssenceType.VOID, 120, tier);

            AABB blastBox = new AABB(
                    epicenter.x - 16.0D, epicenter.y - 3.5D, epicenter.z - 16.0D,
                    epicenter.x + 16.0D, epicenter.y + 6.5D, epicenter.z + 16.0D
            );
            float implosionDmg = 20.5F + tier * 3.30F;

            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;

                ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 240L);
                if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                    Vec3 diff = foe.position().subtract(caster.position());
                    Vec3 push = (diff.horizontalDistanceSqr() > 1.0E-4D
                            ? new Vec3(diff.x, 0.0D, diff.z).normalize()
                            : new Vec3(dir.x, 0.0D, dir.z)).scale(3.15D);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x, 0.48D, push.z)); // 16m knockback!
                }
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, foe, caster, implosionDmg, false, true, false);
            }

            // 14-block Gravitational Tear pulling all entities inward for 6.0 seconds (120 ticks)!
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, epicenter, 14.0D, 120, now, spell, slot, EssenceType.ESCHATON, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        if (elapsed % 8 == 0) {
                            LivingEntity owner = zone.resolveCaster(srv);
                            AABB tear = new AABB(
                                    zone.center.x - 14.0D, zone.center.y - 3.0D, zone.center.z - 14.0D,
                                    zone.center.x + 14.0D, zone.center.y + 6.0D, zone.center.z + 14.0D
                            );
                            ApexEschatonSingularityHelper.pullProjectilesToward(srv, owner, tear, zone.center.add(0.0D, 1.0D, 0.0D));
                            for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, tear, e -> e.isAlive() && e != owner)) {
                                if (owner != null && (ApexEschatonSingularityHelper.isFriendly(foe, owner)
                                        || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard()))) {
                                    continue;
                                }
                                Vec3 in = zone.center.subtract(foe.position()).scale(0.34D);
                                foe.setDeltaMovement(foe.getDeltaMovement().add(in.x, 0.04D, in.z));
                                foe.hasImpulse = true;
                            }
                            emitEschatonBurstParticles(srv, zone.center.add(0.0D, 0.8D, 0.0D), 22, 4.2D);
                        }
                        return true;
                    }
            ));

            emitEschatonBurstParticles(level, epicenter.add(0.0D, 0.9D, 0.0D), 60, 4.8D);
            level.playSound(null, BlockPos.containing(epicenter), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.35F, 0.58F);
            level.playSound(null, BlockPos.containing(epicenter), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.90F, 0.72F);
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
        // Section 6.57 Cosmic Event-Horizon Nova:
        // Wall-penetrating 16-block concussive shockwave (rendered purely via shaders/bloom — ZERO light blocks!),
        // knocks all hostiles 14 blocks back flat, shatters 8 armor points, deals massive Tier 6 Eschaton Materia damage,
        // dissolves incoming projectiles into fuel, and grants caster + allies Mantle of the Omega Singularity
        // (+14 Armor, +8 Toughness, debuff/fall/void immunity, and 8m gravitational vacuum aura for 20.0s / 400t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(16.0D, radius);
        long now = level.getGameTime();
        long mantleUntil = now + 400L;

        SpellWorldVfxManager.broadcastRadialNova(level, center.add(0.0D, 0.30D, 0.0D), r, EssenceType.ESCHATON, EssenceType.VOID, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.ESCHATON, EssenceType.UMBRAL, 110, tier);

        AABB box = new AABB(
                center.x - r, center.y - 3.5D, center.z - r,
                center.x + r, center.y + 6.0D, center.z + r
        );
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, box, EssenceType.ESCHATON, 64L);

        float novaDmg = (13.5F + tier * 2.65F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                grantMantleOfTheOmegaSingularity(level, ent, mantleUntil, 400L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
            if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                Vec3 diff = ent.position().subtract(center);
                Vec3 away = (diff.horizontalDistanceSqr() > 1.0E-4D
                        ? new Vec3(diff.x, 0.0D, diff.z).normalize()
                        : new Vec3(1.0D, 0.0D, 0.0D)).scale(2.80D);
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(away.x, 0.24D, away.z)); // 14m flat knockback!
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, ent, caster, novaDmg, false, true, false);
            }
        }
        grantMantleOfTheOmegaSingularity(level, caster, mantleUntil, 400L);

        emitEschatonBurstParticles(level, center.add(0.0D, 1.0D, 0.0D), 60, r * 0.34D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.25F, 0.60F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.85F, 0.75F);
    }

    private static void grantMantleOfTheOmegaSingularity(ServerLevel level, LivingEntity ally, long mantleUntil, long durTicks) {
        EntityHelper.getPersistentData(ally).putLong(SpellLingeringZoneManager.TAG_ESCHATON_SHROUD_UNTIL, mantleUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 14.0D, 8.0D, 0.0D, 0.0D, durTicks);
        ally.clearFire();
        ally.setTicksFrozen(0);
        ally.fallDistance = 0.0F;
        ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) durTicks, 0, true, false));
        ally.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, (int) durTicks, 0, true, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, ally, 0.94F, null);
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
        // Section 6.57 Reality-Cleaving Scythe:
        // Sweeping two-handed scythe strike cleaving cleanly through shields and solid walls,
        // bypassing 75% armor rating, shattering 8 armor points, disabling shields 8.0s (160t),
        // severing all positive mob buffs, locking the primary victim in 4.0s (80t) event-horizon stasis,
        // and pulling all nearby adjacent enemies within 8m directly into the slash arc!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().add(dir.scale(1.5D)), dir, 5.8D, EssenceType.ESCHATON, EssenceType.VOID, tier
        );
        Vec3 arcOrigin = !hitEntities.isEmpty() ? hitEntities.getFirst().position() : caster.position().add(dir.scale(2.6D));

        float scytheDmg = (11.5F + tier * 2.40F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;

            ApexEschatonSingularityHelper.disableShield(hit, 160);
            ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, hit, caster, scytheDmg, true, true, false);
            }
            if (hit.isAlive()) {
                hit.setDeltaMovement(Vec3.ZERO);
                hit.hasImpulse = true;
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0));
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                SpellWorldVfxManager.broadcastVacuumSphere(
                        level, hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D),
                        Math.max(1.8D, hit.getBbWidth() + 1.1D), true, EssenceType.ESCHATON, EssenceType.VOID, 80, tier
                );
            }
        }

        AABB pullBox = new AABB(
                arcOrigin.x - 8.0D, arcOrigin.y - 2.5D, arcOrigin.z - 8.0D,
                arcOrigin.x + 8.0D, arcOrigin.y + 4.5D, arcOrigin.z + 8.0D
        );
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, pullBox,
                e -> e.isAlive() && e != caster && !hitEntities.contains(e) && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
            Vec3 in = arcOrigin.subtract(adj.position()).scale(0.48D);
            adj.setDeltaMovement(in.x, 0.18D, in.z);
            adj.hasImpulse = true;
        }

        emitEschatonBurstParticles(level, arcOrigin.add(0.0D, 0.85D, 0.0D), 36, 1.9D);
        level.playSound(null, BlockPos.containing(arcOrigin), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.75F, 1.32F);
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
        // Section 6.57 Mantle of the Omega Singularity:
        // Snuffs torches & light sources within 10 blocks, grants +14 Armor, +8 Toughness,
        // complete fire/lava/drowning/freeze/wither/void/debuff immunity, -90% mob detection,
        // 6m physical & magical projectile disintegration into Materia fuel, and an 8m gravitational
        // singularity aura dragging nearby hostiles inward while dealing 3.0 hearts/sec (6.0F) of true damage!
        int tier = spell.getEffectiveTier();
        int dur = 360 + tier * 40;
        long now = level.getGameTime();
        double auraRad = Math.max(8.0D, radius);

        snuffLightSourcesWithinTenBlocks(level, caster.blockPosition());

        if (caster.isShiftKeyDown()) {
            Vec3 anchor = caster.position();
            SpellWorldVfxManager.broadcastWardShell(level, anchor.add(0.0D, 1.1D, 0.0D), auraRad, EssenceType.ESCHATON, EssenceType.VOID, dur, tier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, anchor, auraRad, dur, now, spell, slot, EssenceType.ESCHATON, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        long zNow = srv.getGameTime();
                        BlockPos cPos = BlockPos.containing(zone.center);
                        if (elapsed % 12 == 0) {
                            snuffLightSourcesWithinTenBlocks(srv, cPos);
                        }
                        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                                srv, owner, new AABB(zone.center, zone.center).inflate(6.0D), EssenceType.ESCHATON, 64L
                        );

                        AABB box = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                grantMantleOfTheOmegaSingularity(srv, ent, zNow + 16L, 25L);
                            } else if (elapsed % 20 == 0 && ent instanceof Monster
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, true))) {
                                Vec3 in = zone.center.subtract(ent.position()).scale(0.35D);
                                ent.setDeltaMovement(ent.getDeltaMovement().add(in.x, 0.05D, in.z));
                                ent.hasImpulse = true;
                                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(srv, ent, owner, 6.0F, false, false, true);
                            }
                        }
                        return true;
                    }
            ));
        } else {
            grantMantleOfTheOmegaSingularity(level, caster, now + dur, dur);
        }

        emitEschatonBurstParticles(level, caster.position().add(0.0D, 1.0D, 0.0D), 36, 0.65D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.90F, 0.68F);
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
        // Section 6.57 Singularity Anchor Sigil:
        // Disarmable Single/Repeating void-singularity mandala glyph on any floor, wall, or ceiling;
        // When hostiles approach within 6 blocks, locks entering mobs in place with unbreakable gravity tethers
        // for 6.0 seconds (120t), anchoring them to the spot, shattering 8 armor points, stripping all active
        // positive buffs, and gravitationally pulling all nearby projectiles directly into the anchored targets!
        final int tier = spell.getEffectiveTier();
        final int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        String subMode = slot.primaryEffect().subMode();
        final boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] cooldown = {0};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, Math.max(6.0D, radius), maxWaitTicks, level.getGameTime(), spell, slot, EssenceType.ESCHATON, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        int col = ESCHATON_PALETTE[(elapsed / 10) % ESCHATON_PALETTE.length];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 20, 0.36F, 0.03F),
                                zone.center.x, zone.center.y + 0.14D, zone.center.z, 6, 2.0D, 0.08D, 2.0D, 0.01D
                        );
                    }
                    if (elapsed < 16) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(
                            zone.center.x - 6.0D, zone.center.y - 2.0D, zone.center.z - 6.0D,
                            zone.center.x + 6.0D, zone.center.y + 3.5D, zone.center.z + 6.0D
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive()
                            && e != owner
                            && (owner == null || !ApexEschatonSingularityHelper.isFriendly(e, owner))
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (victims.isEmpty()) return true;

                    long now = srv.getGameTime();
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 6.5D, EssenceType.ESCHATON, EssenceType.VOID, 120, tier
                    );
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 0.75D, 0.0D), 6.5D, true, EssenceType.ESCHATON, EssenceType.UMBRAL, 36, tier
                    );

                    Map<UUID, Vec3> anchoredSpots = new HashMap<>();
                    for (LivingEntity foe : victims) {
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 240L);
                        foe.setDeltaMovement(Vec3.ZERO);
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 6));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 120, 0));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 120L);
                        anchoredSpots.put(foe.getUUID(), foe.position());

                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, EssenceType.ESCHATON, zone.apparatusPotency
                        );
                    }

                    // 6.0-second (120-tick) Unbreakable Gravity Tethers & Projectile-to-Anchored-Victim Magnet!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 12.0D, 120, now, zone.spell, zone.slot, EssenceType.ESCHATON, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                LivingEntity primaryAnchored = null;

                                for (Map.Entry<UUID, Vec3> entry : anchoredSpots.entrySet()) {
                                    Entity ent = sLvl.getEntity(entry.getKey());
                                    if (ent instanceof LivingEntity anchored && anchored.isAlive()) {
                                        if (primaryAnchored == null) primaryAnchored = anchored;
                                        Vec3 lockPos = entry.getValue();
                                        if (anchored.position().distanceToSqr(lockPos) > 0.25D) {
                                            anchored.teleportTo(lockPos.x, lockPos.y, lockPos.z);
                                        }
                                        anchored.setDeltaMovement(Vec3.ZERO);
                                        anchored.hasImpulse = true;
                                        if (sEl % 12 == 0) {
                                            SpellWorldVfxManager.broadcastChainArc(
                                                    sLvl, sZone.center.add(0.0D, 0.15D, 0.0D),
                                                    anchored.position().add(0.0D, anchored.getBbHeight() * 0.5D, 0.0D),
                                                    EssenceType.ESCHATON, EssenceType.VOID, tier
                                            );
                                        }
                                    }
                                }

                                AABB pBox = new AABB(
                                        sZone.center.x - 12.0D, sZone.center.y - 3.5D, sZone.center.z - 12.0D,
                                        sZone.center.x + 12.0D, sZone.center.y + 6.5D, sZone.center.z + 12.0D
                                );
                                Vec3 magnetTarget = primaryAnchored != null
                                        ? primaryAnchored.getEyePosition().subtract(0.0D, 0.2D, 0.0D)
                                        : sZone.center.add(0.0D, 1.0D, 0.0D);
                                ApexEschatonSingularityHelper.pullProjectilesToward(sLvl, sOwner, pBox, magnetTarget);
                                return true;
                            }
                    ));

                    emitEschatonBurstParticles(srv, zone.center.add(0.0D, 0.8D, 0.0D), 38, 2.5D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.15F, 0.65F);

                    if (repeating) {
                        cooldown[0] = 150;
                        return true;
                    }
                    return false;
                }
        ).markDisarmableGlyph().withOnDisarm((srv, z) ->
                emitEschatonBurstParticles(srv, z.center.add(0.0D, 0.3D, 0.0D), 14, 0.45D)));

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 0.75F, 0.72F);
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
        // Section 6.57 Accretion Singularity Torrent:
        // Wall-penetrating cone; shatters 8 armor points, vaporizes all incoming enemy projectiles into fuel,
        // carves a safe tunnel through non-bedrock blocks, and inflicts Omega Spacetime Disorientation
        // (disrupting mob AI pathfinding AND slowing movement by 75% for 35.0s / 700t as mobs spin helplessly in place)!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(14.0D, coneRange);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 origin = caster.getEyePosition();

        SpellWorldVfxManager.broadcastVortexCone(
                level, origin.subtract(0.0D, 0.2D, 0.0D), dir, reach, EssenceType.ESCHATON, EssenceType.VOID, tier
        );

        AABB coneBox = caster.getBoundingBox().expandTowards(dir.scale(reach)).inflate(reach * 0.55D);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, coneBox, EssenceType.ESCHATON, 64L);

        // Carve a clean safe tunnel above caster foot level through non-protected non-bedrock blocks into fuel
        int floorY = caster.getBlockY();
        int carved = 0;
        for (int step = 2; step <= (int) Math.min(12.0D, reach); step += 2) {
            BlockPos stepCenter = BlockPos.containing(origin.add(dir.scale(step)));
            for (BlockPos p : BlockPos.betweenClosed(stepCenter.offset(-1, -1, -1), stepCenter.offset(1, 2, 1))) {
                if (carved >= 72) break;
                if (p.getY() < floorY) continue;
                BlockState bs = level.getBlockState(p);
                if (bs.isAir() || !bs.getFluidState().isEmpty()) continue;
                if (bs.getDestroySpeed(level, p) < 0.0F) continue;
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.CRYING_OBSIDIAN) || bs.is(Blocks.OBSIDIAN) || bs.is(Blocks.BLACKSTONE)) continue;
                level.removeBlock(p, false);
                carved++;
            }
        }
        if (carved > 0 && caster instanceof Player player) {
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.ESCHATON, carved * 24L);
        }

        float torrentDmg = (8.2F + tier * 1.85F) * Math.max(0.5F, apparatusPotency);
        List<UUID> disorientedIds = new ArrayList<>();

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.75D, 700L); // Exact -75% move speed for 35.0s!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 700, 4));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 700); // 35.0s AI pathfinding disruption!

            if (hit instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.setTarget(null);
                mob.setAggressive(false);
                disorientedIds.add(mob.getUUID());
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(level, hit, caster, torrentDmg, false, true, false);
            }
        }

        // Helpless in-place spacetime spinning for the 35.0s (700t) disorientation window!
        if (!disorientedIds.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, caster.position().add(dir.scale(4.0D)), reach, 700, level.getGameTime(), spell, slot, EssenceType.ESCHATON, apparatusPotency,
                    (srv, z, el) -> {
                        if (el > z.maxDurationTicks) return false;
                        if (el % 4 == 0) {
                            boolean anyAlive = false;
                            for (UUID id : disorientedIds) {
                                Entity ent = srv.getEntity(id);
                                if (ent instanceof Mob mob && mob.isAlive()) {
                                    anyAlive = true;
                                    float nextYaw = mob.getYRot() + 44.0F;
                                    mob.setYRot(nextYaw);
                                    mob.setYHeadRot(nextYaw);
                                    mob.yBodyRot = nextYaw;
                                    mob.getNavigation().stop();
                                }
                            }
                            if (!anyAlive) return false;
                        }
                        return true;
                    }
            ));
        }

        for (int s = 2; s <= 12; s += 4) {
            emitEschatonBurstParticles(level, origin.add(dir.scale(s)), 16, s * 0.22D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.85F, 0.78F);
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
        // Section 6.57 Dimensional Inversion Starfall [30.0s Bombardment + 15.0s Collapsed Reality Slag = 900t, 20m AOE]:
        // Drops 8 colossal 3D Skyfall Pseudoblock compressed obsidian & dark-matter singularities that permanently
        // materialize into real blocks upon spell completion; lifts caught mobs into zero-g stasis while pulverizing
        // them flat (-8 armor, vaporizes loose blocks into Materia fuel), and leaves a lingering 15.0s zone of
        // Collapsed Reality Slag (-80% speed, drags hostiles toward the center, and crushes them with true damage)!
        final int tier = spell.getEffectiveTier();
        final double r = Math.max(20.0D, radius);
        final BlockPos centerPos = BlockPos.containing(impactCenter);
        final double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.ESCHATON, EssenceType.VOID, 900, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.04D, 0.0D), r, EssenceType.ESCHATON, EssenceType.UMBRAL, 900, tier);
        level.playSound(null, centerPos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.15F, 0.62F);

        final int singularityCount = 8;
        final BlockPos[] singularityTargets = new BlockPos[singularityCount];
        singularityTargets[0] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, centerPos);
        for (int i = 1; i < singularityCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (singularityCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            singularityTargets[i] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, raw);
        }

        final boolean[] landed = new boolean[singularityCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 900, level.getGameTime(), spell, slot, EssenceType.ESCHATON, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < singularityCount; mIdx++) {
                                BlockState pseudoState = resolveSingularityMonolithBlockState(mIdx);
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, singularityTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean starfall = elapsed < 600; // 30.0s active bombardment, then 15.0s Collapsed Reality Slag!
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (elapsed == 18 || elapsed == 240) {
                        ApexEschatonSingularityHelper.dissolveNonProtectedBlocksIntoFuel(
                                srv, zone.resolveCaster(srv), BlockPos.containing(zone.center), 4, EssenceType.ESCHATON, 32L
                        );
                    }

                    if (starfall) {
                        for (int mIdx = 0; mIdx < singularityCount; mIdx++) {
                            int impactTick = 24 + mIdx * 68;
                            BlockPos strikePos = singularityTargets[mIdx];
                            BlockState pseudoState = resolveSingularityMonolithBlockState(mIdx);
                            int colHeight = (mIdx % 2 == 0) ? 2 : 1;

                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(4, zone.maxDurationTicks - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        pseudoState,
                                        colHeight,
                                        14,
                                        holdTicks,
                                        EssenceType.ESCHATON,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.ESCHATON, EssenceType.VOID, 3.05F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 28, 1.3D, 0.5D, 1.3D, 0.10D
                                );
                                srv.playSound(null, strikePos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.62F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 lanceImpact = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, lanceImpact, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.ESCHATON, EssenceType.VOID, 1.85F, zTier
                            );
                        }
                    }

                    for (int mIdx = 0; mIdx < singularityCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, singularityTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        emitEschatonBurstParticles(
                                srv,
                                zone.center.add(0.0D, starfall ? 5.5D : 0.4D, 0.0D),
                                28,
                                zone.radius * 0.52D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float starfallDmg = (7.4F + zTier * 1.70F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 4.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 14.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && (ApexEschatonSingularityHelper.isFriendly(foe, owner)
                                    || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe))) {
                                continue;
                            }
                            if (starfall) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 220L);
                                if (elapsed % 60 == 0) {
                                    // Zero-g stasis lift before the next monolithic void-shard slam!
                                    foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 24, 1));
                                } else {
                                    // Pulverize caught mobs flat!
                                    foe.removeEffect(MobEffects.LEVITATION);
                                    foe.setDeltaMovement(foe.getDeltaMovement().x * 0.2D, -0.78D, foe.getDeltaMovement().z * 0.2D);
                                    foe.hasImpulse = true;
                                }
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(srv, foe, owner, starfallDmg, false, true, false);
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.ESCHATON, zone.apparatusPotency * 0.60F
                                    );
                                }
                            } else {
                                // 15.0s Collapsed Reality Slag: exact -80% slow, inward drag, and continuous crushing TRUE damage!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.80D, 35L);
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4));
                                Vec3 in = zone.center.subtract(foe.position()).scale(0.32D);
                                foe.setDeltaMovement(foe.getDeltaMovement().add(in.x, 0.0D, in.z));
                                foe.hasImpulse = true;
                                SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(
                                        srv, foe, owner, 6.5F + zTier * 1.15F, false, false, true
                                );
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    private static BlockState resolveSingularityMonolithBlockState(int index) {
        return switch (index & 3) {
            case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
            case 1 -> ModBlocks.ASTRAL_IMPACTITE.get().defaultBlockState();
            case 2 -> Blocks.OBSIDIAN.defaultBlockState();
            default -> ModBlocks.CHISELED_ECLIPSE_BASALT.get().defaultBlockState();
        };
    }

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
        tag.putString("SpellEidolonMateria", EssenceType.ESCHATON.name());
        tag.putInt("SpellEidolonTintRGB", ESCHATON_ACCRETION_VIOLET_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(2.0D, radius), EssenceType.ESCHATON, EssenceType.VOID, spell.getEffectiveTier()
        );
        emitEschatonBurstParticles(level, spawnPos.add(0.0D, 1.1D, 0.0D), 38, 0.65D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.57 Spacetime Fold Slip [100% SILENT]:
        // 24m wall-phasing enemy position-swap (+ 4.0s stasis bubble & buff purge) is executed in SpellExecutionEngine;
        // Arrival grants complete invisibility, cloaking, 4.0s (80t) complete damage immunity, and 100% mob aggro shed!
        long now = level.getGameTime();
        caster.removeEffect(MobEffects.GLOWING);
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120, 0, false, false, true));

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL, now + 120L);
        data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, now + 120L);
        data.putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, now + 80L);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.96F, arrivalPos);

        ApexEschatonSingularityHelper.shedMobAggro(level, caster, departurePos, 32.0D);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 32.0D);
        emitEschatonBurstParticles(level, arrivalPos.add(0.0D, 0.9D, 0.0D), 22, 0.45D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.57 Dimensional Event Horizon Transmutation & Accretion Compression
        // [5.0s Channel, 16m radius, strictly NO air transmutation]:
        // - Stone/Deepslate/Netherrack -> Crying Obsidian, Blackstone, or Void Slag (Astral Impactite)
        // - Cleanly erases loose non-bedrock obstacles directly into pure Eschaton Materia fuel
        // - Purges all curses + elevates up to 2 enchantments (+2) on worn & dropped equipment
        // - Standing water within 4m + empty glass bottles -> 50/50 Gravitational Stasis Elixir or Essence of the Omega Horizon!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 16, potencyTier, EssenceType.ESCHATON,
                "Dimensional Event Horizon Transmutation & Accretion Compression",
                () -> {
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 2, 1.0F, 2);
                    }
                    ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 4, EssenceType.ESCHATON);
                })) {
            return true;
        }

        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.TUFF) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE) || state.is(Blocks.NETHERRACK)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                case 1 -> Blocks.BLACKSTONE.defaultBlockState();
                default -> ModBlocks.ASTRAL_IMPACTITE.get().defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }

        if ((state.is(Blocks.COBBLESTONE) || state.is(BlockTags.DIRT) || state.is(Blocks.GRAVEL)
                || state.is(BlockTags.SAND) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)
                || state.is(Blocks.BASALT))
                && state.getDestroySpeed(level, pos) >= 0.0F
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            level.removeBlock(pos, false);
            if (caster instanceof Player p) {
                SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ESCHATON, 48L);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 2, 1.0F, 2)) {
            itemEntity.setItem(stack.copy());
            return true;
        }

        if ((stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE)
                || stack.is(Items.STONE) || stack.is(Items.DEEPSLATE) || stack.is(Items.GRAVEL))
                && stack.getCount() >= 8) {
            int blocks = Math.max(1, stack.getCount() / 8);
            ItemStack condensed = switch (level.random.nextInt(5)) {
                case 0 -> new ItemStack(Items.RAW_IRON_BLOCK, blocks);
                case 1 -> new ItemStack(Items.RAW_COPPER_BLOCK, blocks);
                case 2 -> new ItemStack(Items.RAW_GOLD_BLOCK, blocks);
                case 3 -> new ItemStack(Items.COAL_BLOCK, blocks);
                default -> new ItemStack(Items.AMETHYST_BLOCK, blocks);
            };
            itemEntity.setItem(condensed);
            return true;
        }

        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE) || stack.is(Items.GUNPOWDER)
                || stack.is(Items.SPIDER_EYE) || stack.is(Items.STRING) || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.BLAZE_ROD) || stack.is(Items.GHAST_TEAR) || stack.is(Items.PHANTOM_MEMBRANE)
                || stack.is(Items.MAGMA_CREAM)) {
            int orbCount = Math.min(5, Math.max(1, stack.getCount() / 4));
            SpellLingeringZoneManager.spawnTypedEssenceOrb(level, itemEntity.position(), EssenceType.ESCHATON, orbCount);
            ItemStack ess = new ItemStack(ModItems.STRONG_ESSENCE.get(), stack.getCount());
            EssenceItem.setEssenceType(ess, EssenceType.ESCHATON);
            itemEntity.setItem(ess);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.57 Omega Event Horizon Dissolution Hex:
        // Continuously crushes the victim with true void-collapse damage, slows movement by 85%,
        // blocks jumping and teleportation, shreds 60% of Armor Toughness, and causes 60% weapon self-recoil!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();

        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.60D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.95D, 0.85D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5, true, true));

        CompoundTag d = EntityHelper.getPersistentData(target);
        d.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 45L);

        // Block teleportation (Enderman/Shulker/pearl/chorus escape) by snapping sudden >3.5m hops back into the horizon!
        long prevTick = d.getLong("SpellEschatonHexPosTick").orElse(0L);
        if (prevTick > 0L && (now - prevTick) <= 30L) {
            double px = d.getDouble("SpellEschatonHexPosX").orElse(target.getX());
            double py = d.getDouble("SpellEschatonHexPosY").orElse(target.getY());
            double pz = d.getDouble("SpellEschatonHexPosZ").orElse(target.getZ());
            double dx = target.getX() - px;
            double dy = target.getY() - py;
            double dz = target.getZ() - pz;
            if ((dx * dx + dy * dy + dz * dz) > 12.25D) {
                target.teleportTo(px, py, pz);
                target.setDeltaMovement(Vec3.ZERO);
                target.hasImpulse = true;
            }
        }
        d.putLong("SpellEschatonHexPosTick", now);
        d.putDouble("SpellEschatonHexPosX", target.getX());
        d.putDouble("SpellEschatonHexPosY", target.getY());
        d.putDouble("SpellEschatonHexPosZ", target.getZ());

        SpellCombatReactionEngine.hurtWithEschatonMateriaAdvantage(
                level, target, null, (3.8F + potencyTier * 0.90F) * mult, false, false, true
        );
        emitEschatonBurstParticles(level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), 9, 0.32D);
        return true;
    }

    // =========================================================================
    // HELPERS — 10-BLOCK LIGHT SNUFFING & PALETTE PARTICLES
    // =========================================================================

    private static void snuffLightSourcesWithinTenBlocks(ServerLevel level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-10, -3, -10), center.offset(10, 4, 10))) {
            if (p.distSqr(center) > 100.0D) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof TorchBlock || bs.getBlock() instanceof LanternBlock) {
                level.destroyBlock(p, true);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof CandleBlock && bs.getValue(CandleBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CandleBlock.LIT, false));
            }
        }
    }

    private static void emitEschatonBurstParticles(ServerLevel level, Vec3 center, int totalCount, double spread) {
        int perColor = Math.max(1, totalCount / ESCHATON_PALETTE.length);
        for (int col : ESCHATON_PALETTE) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 22, 0.52F, 0.04F),
                    center.x, center.y, center.z, perColor, spread, spread * 0.55D, spread, 0.03D
            );
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z,
                Math.max(4, totalCount / 3), spread, spread * 0.45D, spread, 0.05D);
    }
}
