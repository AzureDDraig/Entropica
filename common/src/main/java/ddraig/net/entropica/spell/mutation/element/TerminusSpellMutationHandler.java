package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for
 * <b>Terminus Materia (#54 — Air + Frozen + Water, Tier 5 Eschaton Fusion: "The Absolute Zero")</b>
 * (Spellcrafting System Guide Section 6.56).
 *
 * <p><b>Palette</b>: Air Golden-Buff ({@code #A88654}), Frozen Cyan/Frost ({@code #98DCF2}),
 * and Water Deep Azure ({@code #225C7C}); text format code {@code "§b"}.
 *
 * <p><b>Tier 5 Eschaton Scaling</b>: Wall-phasing delivery, 60% armor bypass, 6–7 armor shredding,
 * 24.0s + 12.0s Skyfall duration, 60.0s summon lifecycles (110 HP Elemental / 120 HP Spire), and 16-block effect radii.
 *
 * <p><b>Elemental Affinity</b>: Strong against {@code NETHER (+1)}, {@code ARID (+1)}, {@code EARTH (+1)},
 * {@code WATER (+1)}, {@code MAGMA (+1)}, {@code PYRE (+1)}, {@code BLIGHT (+1)}, {@code SOULFIRE (+1)},
 * and {@code MIASMA (+1)} | Weak against {@code UNDEAD (-1)} (Forced Single Weakness).
 */
public final class TerminusSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TERMINUS_AIR_GOLDEN_BUFF_HEX_COLOR = 0xA88654;
    public static final int TERMINUS_FROST_CYAN_HEX_COLOR = 0x98DCF2;
    public static final int TERMINUS_WATER_AZURE_HEX_COLOR = 0x225C7C;

    public static final int[] TERMINUS_PALETTE = new int[]{
            TERMINUS_AIR_GOLDEN_BUFF_HEX_COLOR,
            TERMINUS_FROST_CYAN_HEX_COLOR,
            TERMINUS_WATER_AZURE_HEX_COLOR
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.TERMINUS;
    }

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Cryogenic Polar Vortex Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Manifests 4 persistent orbiting cryo-blades and ice-shields (#A88654 / #98DCF2 / #225C7C) "
                        + "circling the primary spell or caster; phases through solid walls, intercepts and shatters "
                        + "100% of incoming enemy projectiles, and automatically discharges seeking ice shards at "
                        + "nearby hostiles (-40% Armor Toughness and -40% attack speed)."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Absolute-Zero Permafrost Lance";
            case BEAM -> "Sustained Cryogenic Vortex Ray & Zero-Point Shockwave";
            case RADIAL_BURST -> "Absolute-Zero Cryo-Nova";
            case TOUCH_STRIKE -> "Absolute-Zero Glacier Cleave";
            case AURA -> "Zero-Point Kinetic Stasis Shield";
            case GLYPH -> "Permafrost Glacial Abyss Sigil";
            case CONE -> "Cryogenic Blizzard Deluge";
            case SKYFALL -> "Continuous Cryo-Stasis Hailstorm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a massive wall-phasing cryogenic lance forged from sub-zero vacuum gale currents, "
                            + "crystalline rime, and dense glacial water (#A88654 / #98DCF2 / #225C7C); pierces "
                            + "solid walls and up to 8 targets (60% armor bypass), detonating on terminal impact "
                            + "into an 8-block Zero-Point Glacial Stasis Field (3.5s solid stasis, -6 armor, and "
                            + "flash-freezing ground into slippery Blue Ice).";
            case BEAM ->
                    "Channels a massive wall-phasing continuous ray of sub-zero howling winds, pressurized azure "
                            + "water, and razor rime shards; melts 2 armor/sec, grounds aerial hostiles, douses "
                            + "fires, and encases targets in escalating frostbite; 5.0s continuous channeling "
                            + "triggers Zero-Point Atmospheric Collapse (12m knockback, -7 armor, buff purge, "
                            + "and 5.0s solid blue-ice encasement).";
            case RADIAL_BURST ->
                    "Wall-penetrating 12-block concussive shockwave of sub-zero atmospheric shockwaves, jagged "
                            + "permafrost glaciers, and howling vacuum gales (zero light blocks); knocks hostiles "
                            + "10m back flat, shatters 6 armor, freezes surface water into Packed Ice and lava "
                            + "into Obsidian, encases mobs solid in ice for 4.0s, and grants Glacial Mantle of "
                            + "Terminus (+12 Armor, +6 Toughness, freeze/drowning/fire immunity, +40% ice speed, 16.0s).";
            case TOUCH_STRIKE ->
                    "Luminous wall-cleaving two-handed blade woven of razor permafrost, compressed gale currents, "
                            + "and dense glacial water; bypasses 60% armor, shatters 6 armor points, disables "
                            + "shields 6.0s, encases the victim solid in blue ice for 4.0s, and releases an 8m "
                            + "atmospheric frost-nova freezing surrounding hostiles for 2.5s.";
            case AURA ->
                    "Envelopes the caster in howling cryogenic winds, floating ice mirrors, and azure water "
                            + "currents (mobile or Sneak-anchored); snuffs fires & torches within 6m, grants "
                            + "+12 Armor, +6 Toughness, fire/lava/drowning/freeze/wither immunity, -85% mob "
                            + "detection, 5m arrow/trident ice-hail shattering, 50% damage absorption, and 2.0s "
                            + "solid ice encasement + -60% attack speed on melee attackers.";
            case GLYPH ->
                    "Cryogenic mandala glyph on any floor, wall, or ceiling (disarmable, Single or Repeating); "
                            + "hostile approach within 5m detonates an 8m cryogenic vortex vacuuming mobs inward, "
                            + "launching them 12m into the air on a gale geyser, flash-freezing them at the apex "
                            + "for catastrophic crash impact damage (-6 armor, buff purge, and 4.0s blue-ice encasement).";
            case CONE ->
                    "Wall-penetrating cone of pressurized sub-zero gale winds, razor hail, and freezing water; "
                            + "shatters 6 armor points, deflects all incoming projectiles, freezes standing water "
                            + "into Packed Ice, and inflicts Cryogenic Molecular Hypothermia (30.0s AI disruption "
                            + "and -60% movement speed as mobs shudder in frozen paralysis).";
            case SKYFALL ->
                    "24.0s Continuous Cryo-Stasis Hailstorm across a 16-block+ zone dropping 8 3D glacial "
                            + "permafrost monoliths that permanently materialize upon completion; locks caught "
                            + "hostiles frozen in suspended animation, shatters 7 armor points, chips health, and "
                            + "leaves a 12.0s Absolute Zero Permafrost field (freezes entering mobs solid; grants "
                            + "allies +6 Armor and Frost Walker).";
            case SUMMON_AGENT ->
                    "Summons a 110 HP Titanic Glacial Elemental (60s refreshing lifecycle) — colossal wall-phasing "
                            + "frost elemental delivering sweeping glacial slams (60% armor bypass, heavy Terminus "
                            + "damage, -4 armor/strike, and 3.0s solid ice encasement).";
            case SUMMON_SENTRY ->
                    "Summons a 120 HP Anchored Permafrost Elemental Spire (60s refreshing lifecycle) — bombards "
                            + "hostiles up to 20m with wall-penetrating cryogenic lances (-3 armor/hit) while "
                            + "pulsing a 16m Polar Freeze Field (-50% hostile speed and continuous water freezing).";
            case SUMMON_DECOY ->
                    "Summons an Unstable Cryogenic Elemental Effigy (16s duration) — taunts hostiles across 16m, "
                            + "reflects 45% melee damage, and detonates in a 14m absolute-zero burst (massive true "
                            + "damage, -6 armor, and 5.0s solid ice encasement).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Glacial Slipstream Flash-Slide";
            case TRANSMUTE, BREAK -> "Zero-Point Crystallization & Permafrost Synthesis";
            case AFFLICTION -> "Absolute Zero Molecular Cessation Hex";
            case WARD -> "Zero-Point Glacial Bastion";
            default -> "Absolute-Zero Terminus " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Surges forward up to 22 blocks through solid walls on a frictionless ice-slipstream; "
                            + "departure leaves a 6m flash-freeze frost nova rooting pursuers in ice for 3.0s; "
                            + "arrival grants 10.0s Frost Walker, fall damage immunity, +40% movement speed on "
                            + "ice, and 100% hostile mob aggro shed.";
            case TRANSMUTE, BREAK ->
                    "5.0s continuous channel across 12 blocks (air cannot be transmuted); freezes standing water "
                            + "and rainfall into Blue Ice, lava sources into Obsidian, and stone/deepslate into "
                            + "Permafrost and Packed Ice; converts standing water within 3m into Glacial Frost "
                            + "Elixirs (+6 Toughness, Freeze & Fire immunity, +30% ice attack speed) with glass bottles.";
            case AFFLICTION ->
                    "Milk-immune apocalyptic cryogenic stasis curse (30.0s boss cap + 10.0s immunity); inflicts "
                            + "true frostbite damage, -80% move speed, -60% attack speed, -50% Armor Toughness, "
                            + "and disables jumping; Player Hex Vector grants nearby monsters +40% crushing "
                            + "damage and Freezing on melee hits.";
            default ->
                    "Infuses " + effect.getDisplayName() + " with Tier 5 Eschaton Terminus Absolute-Zero Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW SYNERGY — CRYOGENIC POLAR VORTEX ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, owner, projectile.getBoundingBox().inflate(2.6D), null, 0L
        );

        // 4 persistent orbiting cryo-blades & ice-shields circling the spell carrier (#A88654 / #98DCF2 / #225C7C)
        double baseAng = projectile.tickCount * 0.42D;
        for (int i = 0; i < 4; i++) {
            double ang = baseAng + (Math.PI * 0.5D * i);
            int col = TERMINUS_PALETTE[i % TERMINUS_PALETTE.length];
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 12, 0.30F, 0.03F),
                    projectile.getX() + Math.cos(ang) * 0.48D,
                    projectile.getY() + Math.sin(ang * 0.5D) * 0.24D,
                    projectile.getZ() + Math.sin(ang) * 0.48D,
                    1, 0.02D, 0.02D, 0.02D, 0.005D
            );
        }

        // Automatically discharge seeking ice shards at nearby hostiles (-40% Toughness & -40% attack speed)!
        if (projectile.tickCount % 10 == 0) {
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(6.0D),
                    e -> e.isAlive() && e != owner && e instanceof Monster
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, true)))) {
                SpellWorldVfxManager.broadcastChainArc(
                        level, projectile.position(), foe.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                        EssenceType.TERMINUS, EssenceType.FROZEN, 8
                );
                SpellCombatReactionEngine.applyPercentageArmorShred(foe, level, 0.0D, 0.40D, 120L);
                SpellCombatReactionEngine.applyTimedAttackDebuff(foe, level, 0.0D, 0.40D, 80L);
                foe.clearFire();
                foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 80, foe.getTicksFrozen() + 60));
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, foe, owner, 3.60F, false, true, false);
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
        SpellWorldVfxManager.broadcastChainArc(level, from, to, EssenceType.TERMINUS, EssenceType.FROZEN, spell.getEffectiveTier());
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, new AABB(from, to).inflate(2.2D), null, 0L);
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.40D, 120L);
            SpellCombatReactionEngine.applyTimedAttackDebuff(toTarget, level, 0.0D, 0.40D, 80L);
            toTarget.clearFire();
            toTarget.setTicksFrozen(Math.min(toTarget.getTicksRequiredToFreeze() + 80, toTarget.getTicksFrozen() + 60));
            SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(
                    level, toTarget, caster, 3.80F + spell.getEffectiveTier() * 0.55F, false, true, false
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
        // High-velocity zero-gravity wall-phasing Permafrost Lance!
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D) {
            projectile.setDeltaMovement(vel.normalize().scale(Math.max(2.10D, vel.length() * 1.35D)));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.85F, 1.55F);
        level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.65F, 1.65F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        int col = TERMINUS_PALETTE[(projectile.tickCount / 2) % TERMINUS_PALETTE.length];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 12, 0.34F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.06D, 0.06D, 0.06D, 0.02D);
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
        // Section 6.56 Absolute-Zero Permafrost Lance:
        // Pierces solid walls and up to 8 targets, bypassing 60% of armor rating;
        // Upon terminal impact, detonates into an 8-block Zero-Point Glacial Stasis Field!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        int tier = spell.getEffectiveTier();
        if (hitEntity instanceof LivingEntity direct && direct.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(direct, caster, safeguard))) {
            direct.clearFire();
            direct.setTicksFrozen(Math.min(direct.getTicksRequiredToFreeze() + 90, direct.getTicksFrozen() + 70));
            if (!slot.hasHarmVerb()) {
                float lanceDmg = (5.8F + tier * 1.45F) * Math.max(0.5F, spell.flawProfile().getPotencyMultiplier());
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, direct, caster, lanceDmg, false, true, false);
            }
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            detonateZeroPointGlacialStasis(level, caster, hitEntity.position(), spell, slot);
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
        if (!projectile.isWallPhasing()) {
            detonateZeroPointGlacialStasis(level, caster, blockHit.getLocation(), spell, slot);
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
        detonateZeroPointGlacialStasis(level, caster, projectile.position(), spell, slot);
    }

    private static void detonateZeroPointGlacialStasis(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell,
            @Nullable SubSpellSlot slot
    ) {
        // Section 6.56 8-block Zero-Point Glacial Stasis Field:
        // Freezes all caught hostiles in mid-animation stasis for 3.5s (70t), shatters 6 armor points,
        // and flash-freezes the ground & water into slippery Blue Ice!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        SpellWorldVfxManager.broadcastRadialNova(level, center.add(0.0D, 0.25D, 0.0D), 8.0D, EssenceType.TERMINUS, EssenceType.FROZEN, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.TERMINUS, EssenceType.WATER, 70, tier);

        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-6, -2, -6), cPos.offset(6, 1, 6))) {
            if (p.distSqr(cPos) > 36.0D) continue;
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
            } else if ((bs.is(BlockTags.DIRT) || bs.is(BlockTags.BASE_STONE_OVERWORLD) || bs.is(BlockTags.SAND))
                    && level.getBlockState(p.above()).isAir()
                    && level.random.nextFloat() < 0.45F) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }

        AABB box = new AABB(
                center.x - 8.0D, center.y - 2.5D, center.z - 8.0D,
                center.x + 8.0D, center.y + 4.5D, center.z + 8.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
            FrozenSpellMutationHandler.applyIceStasis(level, caster, foe, 70, 0, spell, slot);
        }

        emitTerminusBurstParticles(level, center.add(0.0D, 0.6D, 0.0D), 38, 2.4D);
        level.playSound(null, cPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.05F, 0.72F);
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
        // Section 6.56 Sustained Cryogenic Vortex Ray & Zero-Point Shockwave:
        // Melts 2 armor/sec, grounds aerial hostiles, douses fires, & encases targets in escalating frostbite;
        // Sustaining for 5.0 continuous seconds (100t, tracked on the CASTER) triggers Zero-Point Atmospheric Collapse:
        // blasts hostiles 12 blocks back, shatters 7 armor points, purges all positive mob buffs,
        // and flash-freezes all caught entities solid in blue ice for 5.0 seconds (100t)!
        Vec3 eye = caster.getEyePosition();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        boolean continuousPulse = heldTicks > 0 || apparatusPotency <= 0.45F;
        float tickFrac = continuousPulse ? 0.25F : 1.0F;

        // Douse fires along the ray trajectory
        for (int step = 2; step <= (int) Math.min(20.0D, maxReach); step += 3) {
            BlockPos stepPos = BlockPos.containing(eye.add(dir.scale(step)));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-1, -1, -1), stepPos.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof BaseFireBlock && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                    level.removeBlock(p, false);
                }
            }
        }

        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        long lastCast = casterData.getLong("TerminusCasterBeamLastTick").orElse(-100L);
        int channelTicks;
        if (lastCast == now) {
            channelTicks = casterData.getInt("TerminusCasterBeamTicks").orElse(0);
        } else {
            int step = heldTicks > 0
                    ? ((lastCast >= 0L && now - lastCast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - lastCast)) : Math.min(heldTicks, 4))
                    : 0;
            channelTicks = heldTicks > 0
                    ? ((now - lastCast <= 28L) ? casterData.getInt("TerminusCasterBeamTicks").orElse(0) + step : step)
                    : 0;
            casterData.putLong("TerminusCasterBeamLastTick", now);
            casterData.putInt("TerminusCasterBeamTicks", channelTicks);
        }

        LivingEntity primaryFoe = null;
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            if (primaryFoe == null) primaryFoe = hit;

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            if (hData.getLong("TerminusBeamLastTick").orElse(-100L) == now) continue;
            hData.putLong("TerminusBeamLastTick", now);

            hit.clearFire();
            if (!hit.onGround() && !SpellCombatReactionEngine.isBossEntity(level, hit)) {
                hit.setDeltaMovement(hit.getDeltaMovement().x * 0.4D, -0.88D, hit.getDeltaMovement().z * 0.4D);
                hit.hasImpulse = true;
            }

            long foeLast = hData.getLong("TerminusFoeLastTick").orElse(-100L);
            int foeStep = heldTicks > 0
                    ? ((foeLast >= 0L && now - foeLast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - foeLast)) : Math.min(heldTicks, 4))
                    : 0;
            int foeTicks = heldTicks > 0
                    ? ((now - foeLast <= 28L) ? hData.getInt("TerminusFoeTicks").orElse(0) + foeStep : foeStep)
                    : 0;
            hData.putLong("TerminusFoeLastTick", now);
            hData.putInt("TerminusFoeTicks", foeTicks);

            int meltedArmor = Math.min(10, Math.max(2, (foeTicks / 20) * 2));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, meltedArmor, 0.0D, 100L);
            hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 100, hit.getTicksFrozen() + (continuousPulse ? 22 : 55)));
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, Math.min(0.75D, 0.25D + (foeTicks / 100.0D) * 0.50D), 35L);

            if (!slot.hasHarmVerb()) {
                float beamDmg = (4.8F + tier * 1.20F) * tickFrac;
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, hit, caster, beamDmg, false, true, false);
            }
        }

        // 5.0 continuous seconds (100 ticks) -> Zero-Point Atmospheric Collapse!
        if (heldTicks >= 100 && channelTicks >= 100 && (primaryFoe != null || blockHit != null)) {
            casterData.putInt("TerminusCasterBeamTicks", 0);
            Vec3 epicenter = primaryFoe != null
                    ? primaryFoe.position()
                    : (blockHit != null ? blockHit.getLocation() : eye.add(dir.scale(Math.min(12.0D, maxReach))));

            SpellWorldVfxManager.broadcastRadialNova(level, epicenter.add(0.0D, 0.3D, 0.0D), 12.0D, EssenceType.TERMINUS, EssenceType.FROZEN, tier);
            SpellWorldVfxManager.broadcastArcaneSeal(level, epicenter.add(0.0D, 0.04D, 0.0D), 12.0D, EssenceType.TERMINUS, EssenceType.AIR, 100, tier);

            AABB collapseBox = new AABB(
                    epicenter.x - 12.0D, epicenter.y - 3.0D, epicenter.z - 12.0D,
                    epicenter.x + 12.0D, epicenter.y + 6.0D, epicenter.z + 12.0D
            );
            float collapseDmg = 16.5F + tier * 2.85F;

            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, collapseBox, LivingEntity::isAlive)) {
                if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;

                ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 7.0D, 0.0D, 220L);
                if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                    Vec3 diff = foe.position().subtract(caster.position());
                    Vec3 push = (diff.lengthSqr() > 1.0E-4D ? new Vec3(diff.x, 0.0D, diff.z).normalize() : new Vec3(dir.x, 0.0D, dir.z)).scale(2.45D);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x, 0.42D, push.z)); // 12m knockback!
                }
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, foe, caster, collapseDmg, false, true, false);
                if (foe.isAlive()) {
                    FrozenSpellMutationHandler.applyIceStasis(level, caster, foe, 100, 6, spell, slot);
                }
            }

            emitTerminusBurstParticles(level, epicenter.add(0.0D, 0.8D, 0.0D), 52, 4.2D);
            level.playSound(null, BlockPos.containing(epicenter), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.25F, 0.62F);
            level.playSound(null, BlockPos.containing(epicenter), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.15F, 0.72F);
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
        // Section 6.56 Absolute-Zero Cryo-Nova:
        // Wall-penetrating 12-block concussive shockwave (rendered purely via shaders/bloom — ZERO light blocks!),
        // knocks hostiles 10m back flat, shatters 6 armor points, deals massive Tier 5 Terminus Materia damage,
        // freezes surface water -> Packed Ice & lava -> Obsidian, encases surviving mobs solid in ice for 4.0s (80t),
        // and grants caster + allies Glacial Mantle of Terminus (+12 Armor, +6 Toughness, freeze/drowning/fire immunity,
        // +40% movement speed on ice for 16.0s / 320t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);
        long now = level.getGameTime();
        long mantleUntil = now + 320L;

        SpellWorldVfxManager.broadcastRadialNova(level, center.add(0.0D, 0.28D, 0.0D), r, EssenceType.TERMINUS, EssenceType.FROZEN, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.TERMINUS, EssenceType.WATER, 100, tier);

        int blockRad = Math.min(12, (int) Math.ceil(r));
        BlockPos cPos = BlockPos.containing(center);
        double blockRadSq = blockRad * blockRad;
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-blockRad, -2, -blockRad), cPos.offset(blockRad, 2, blockRad))) {
            if (p.distSqr(cPos) > blockRadSq) continue;
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
            } else if (bs.is(Blocks.LAVA) && bs.getFluidState().isSource()) {
                level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }

        float novaDmg = (10.5F + tier * 2.15F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(
                center.x - r, center.y - 3.0D, center.z - r,
                center.x + r, center.y + 5.5D, center.z + r
        );
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                grantGlacialMantleOfTerminus(level, ent, mantleUntil);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 6.0D, 0.0D, 180L);
            if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                Vec3 diff = ent.position().subtract(center);
                Vec3 away = (diff.horizontalDistanceSqr() > 1.0E-4D ? new Vec3(diff.x, 0.0D, diff.z).normalize() : new Vec3(1.0D, 0.0D, 0.0D)).scale(2.08D);
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(away.x, 0.22D, away.z)); // 10m flat knockback!
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, ent, caster, novaDmg, false, true, false);
            }
            if (ent.isAlive()) {
                FrozenSpellMutationHandler.applyIceStasis(level, caster, ent, 80, 6, spell, slot);
            }
        }
        grantGlacialMantleOfTerminus(level, caster, mantleUntil);

        emitTerminusBurstParticles(level, center.add(0.0D, 0.9D, 0.0D), 54, r * 0.35D);
        level.playSound(null, cPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.25F, 0.65F);
        level.playSound(null, cPos, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.10F, 0.78F);
    }

    private static void grantGlacialMantleOfTerminus(ServerLevel level, LivingEntity ally, long mantleUntil) {
        EntityHelper.getPersistentData(ally).putLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL, mantleUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 12.0D, 6.0D, 0.0D, 0.0D, 320L);
        ally.clearFire();
        ally.setTicksFrozen(0);
        ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 320, 0, true, false));
        ally.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 320, 0, true, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, ally, 0.85F, null);
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
        // Section 6.56 Absolute-Zero Glacier Cleave:
        // Deals devastating Tier 5 Terminus Materia damage, bypasses 60% of armor rating, shatters 6 armor points,
        // disables shields for 6.0s (120t), encases the victim solid in blue ice for 4.0s (80t),
        // and releases an 8-block atmospheric frost-nova that freezes all surrounding hostiles for 2.5s (50t)!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().add(dir.scale(1.4D)), dir, 5.2D, EssenceType.TERMINUS, EssenceType.FROZEN, tier
        );
        Vec3 novaOrigin = !hitEntities.isEmpty() ? hitEntities.getFirst().position() : caster.position().add(dir.scale(2.5D));
        SpellWorldVfxManager.broadcastRadialNova(level, novaOrigin.add(0.0D, 0.25D, 0.0D), 8.0D, EssenceType.TERMINUS, EssenceType.FROZEN, tier);

        float cleaveDmg = (9.2F + tier * 2.05F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 120);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, hit, caster, cleaveDmg, true, true, false);
            }
            if (hit.isAlive()) {
                FrozenSpellMutationHandler.applyIceStasis(level, caster, hit, 80, 0, spell, slot);
            }
        }

        // 8-block atmospheric frost-nova freezing surrounding hostiles for 2.5 seconds (50t)!
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, new AABB(novaOrigin, novaOrigin).inflate(8.0D),
                e -> e.isAlive() && e != caster && !hitEntities.contains(e) && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(adj, level, 4.0D, 0.0D, 120L);
            FrozenSpellMutationHandler.applyIceStasis(level, caster, adj, 50, 0, spell, slot);
        }

        emitTerminusBurstParticles(level, novaOrigin.add(0.0D, 0.8D, 0.0D), 32, 1.8D);
        level.playSound(null, BlockPos.containing(novaOrigin), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.15F, 0.68F);
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
        // Section 6.56 Zero-Point Kinetic Stasis Shield:
        // Snuffs fires & torches within 6m, +12 Armor, +6 Toughness, complete fire/lava/drowning/freeze/wither immunity,
        // -85% hostile mob detection, 5m arrow/trident ice-hail shattering, 50% damage absorption,
        // and 2.0s solid ice encasement + -60% attack speed on melee attackers!
        int tier = spell.getEffectiveTier();
        int dur = 280 + tier * 40;
        long now = level.getGameTime();
        double auraRad = Math.max(6.0D, radius);

        snuffFiresAndTorchesWithinSixBlocks(level, caster.blockPosition());

        if (caster.isShiftKeyDown()) {
            Vec3 anchor = caster.position();
            SpellWorldVfxManager.broadcastWardShell(level, anchor.add(0.0D, 1.1D, 0.0D), auraRad, EssenceType.TERMINUS, EssenceType.FROZEN, dur, tier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, anchor, auraRad, dur, now, spell, slot, EssenceType.TERMINUS, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        long zNow = srv.getGameTime();
                        BlockPos cPos = BlockPos.containing(zone.center);
                        if (elapsed % 12 == 0) {
                            snuffFiresAndTorchesWithinSixBlocks(srv, cPos);
                        }
                        for (AbstractArrow arrow : srv.getEntitiesOfClass(AbstractArrow.class, new AABB(zone.center, zone.center).inflate(5.0D))) {
                            if (owner != null && (arrow.getOwner() == owner
                                    || (arrow.getOwner() instanceof LivingEntity ao && ApexEschatonSingularityHelper.isFriendly(ao, owner)))) {
                                continue;
                            }
                            srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                                    arrow.getX(), arrow.getY(), arrow.getZ(), 10, 0.14D, 0.14D, 0.14D, 0.05D);
                            srv.sendParticles(ParticleTypes.SNOWFLAKE, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.03D);
                            arrow.discard();
                        }

                        AABB box = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL, zNow + 16L);
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 12.0D, 6.0D, 0.0D, 0.0D, 25L);
                                ent.clearFire();
                                ent.setTicksFrozen(0);
                                ent.removeEffect(MobEffects.WITHER);
                                ent.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 30, 0, true, false));
                                ent.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false));
                            }
                        }
                        return true;
                    }
            ));
        } else {
            EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TERMINUS_SHROUD_UNTIL, now + dur);
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, dur);
            caster.clearFire();
            caster.setTicksFrozen(0);
            caster.removeEffect(MobEffects.WITHER);
            caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
            SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.88F, null);
        }

        emitTerminusBurstParticles(level, caster.position().add(0.0D, 1.0D, 0.0D), 30, 0.60D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.55F);
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
        // Section 6.56 Permafrost Glacial Abyss Sigil:
        // Disarmable Single/Repeating cryogenic mandala glyph on any floor, wall, or ceiling;
        // When hostiles approach within 5 blocks, detonates an 8m cryogenic vortex vacuuming mobs into its epicenter,
        // launches them 12 blocks into the air on a howling gale geyser, flash-freezes them solid at the apex
        // so they crash down for catastrophic impact damage, shatters 6 armor points, strips all positive buffs,
        // and encases survivors in blue ice for 4.0 seconds (80t)!
        final int tier = spell.getEffectiveTier();
        final int maxWaitTicks = tier >= 5 ? 72_000_000 : 12_000;
        String subMode = slot.primaryEffect().subMode();
        final boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] cooldown = {0};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, Math.max(5.0D, radius), maxWaitTicks, level.getGameTime(), spell, slot, EssenceType.TERMINUS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 12 == 0) {
                        int col = TERMINUS_PALETTE[(elapsed / 12) % TERMINUS_PALETTE.length];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 20, 0.36F, 0.03F),
                                zone.center.x, zone.center.y + 0.14D, zone.center.z, 6, 1.8D, 0.08D, 1.8D, 0.01D
                        );
                    }
                    if (elapsed < 16) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(
                            zone.center.x - 5.0D, zone.center.y - 2.0D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 3.5D, zone.center.z + 5.0D
                    );
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive()
                            && e != owner
                            && (owner == null || !ApexEschatonSingularityHelper.isFriendly(e, owner))
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    e, owner, zone.spell.hasWardFriendlyFireSafeguard()))).isEmpty()) {
                        return true;
                    }

                    long now = srv.getGameTime();
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 0.6D, 0.0D), 8.0D, true, EssenceType.TERMINUS, EssenceType.AIR, 36, tier
                    );
                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, 2.4D, 12.0D, EssenceType.TERMINUS, EssenceType.FROZEN, 36, tier
                    );
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.TERMINUS, EssenceType.WATER, 120, tier
                    );

                    AABB vortex = new AABB(
                            zone.center.x - 8.0D, zone.center.y - 2.5D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D
                    );
                    List<UUID> launchedTargets = new ArrayList<>();

                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, vortex, e -> e.isAlive() && e != owner)) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }

                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 200L);
                        foe.clearFire();
                        foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 100);
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_FALL_VULN_50_UNTIL, now + 120L);

                        Vec3 in = zone.center.subtract(foe.position()).scale(0.38D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(in.x, 1.48D, in.z)); // 12m howling gale geyser launch!
                        launchedTargets.add(foe.getUUID());

                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                zone.spell, zone.slot, EssenceType.TERMINUS, zone.apparatusPotency
                        );
                    }

                    // Apex Flash-Freeze & Catastrophic Crash Sequence (4.0s / 80t solid blue-ice encasement on survivors)!
                    if (!launchedTargets.isEmpty()) {
                        float crashDmg = (12.5F + tier * 2.40F) * Math.max(0.5F, zone.apparatusPotency);
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, zone.center, 8.0D, 32, now, zone.spell, zone.slot, EssenceType.TERMINUS, zone.apparatusPotency,
                                (sLvl, sZone, sEl) -> {
                                    if (sEl == 18) {
                                        LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                        for (UUID id : launchedTargets) {
                                            Entity ent = sLvl.getEntity(id);
                                            if (ent instanceof LivingEntity victim && victim.isAlive()) {
                                                // Flash-freeze solid at the 12m apex, then send crashing straight down!
                                                FrozenSpellMutationHandler.applyIceStasis(sLvl, sOwner, victim, 20, 8, sZone.spell, sZone.slot);
                                                Vec3 toCenter = sZone.center.subtract(victim.position()).scale(0.22D);
                                                SpellExecutionEngine.applySyncedMotion(victim, new Vec3(toCenter.x, -1.85D, toCenter.z));
                                                emitTerminusBurstParticles(sLvl, victim.position().add(0.0D, 0.5D, 0.0D), 12, 0.45D);
                                            }
                                        }
                                    } else if (sEl == 27) {
                                        LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                        SpellWorldVfxManager.broadcastRadialNova(
                                                sLvl, sZone.center.add(0.0D, 0.2D, 0.0D), 8.0D, EssenceType.TERMINUS, EssenceType.FROZEN, tier
                                        );
                                        for (UUID id : launchedTargets) {
                                            Entity ent = sLvl.getEntity(id);
                                            if (ent instanceof LivingEntity victim && victim.isAlive()) {
                                                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(
                                                        sLvl, victim, sOwner, crashDmg, false, true, false
                                                );
                                                if (victim.isAlive()) {
                                                    FrozenSpellMutationHandler.applyIceStasis(sLvl, sOwner, victim, 80, 0, sZone.spell, sZone.slot);
                                                }
                                                emitTerminusBurstParticles(sLvl, victim.position().add(0.0D, 0.3D, 0.0D), 16, 0.65D);
                                            }
                                        }
                                        sLvl.playSound(null, BlockPos.containing(sZone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.15F, 0.65F);
                                        return false;
                                    }
                                    return sEl < 32;
                                }
                        ));
                    }

                    emitTerminusBurstParticles(srv, zone.center.add(0.0D, 0.8D, 0.0D), 38, 2.5D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.15F, 0.72F);

                    if (repeating) {
                        cooldown[0] = 140;
                        return true;
                    }
                    return false;
                }
        ).markDisarmableGlyph().withOnDisarm((srv, z) ->
                emitTerminusBurstParticles(srv, z.center.add(0.0D, 0.3D, 0.0D), 14, 0.45D)));

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 0.80F, 1.45F);
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
        // Section 6.56 Cryogenic Blizzard Deluge:
        // Wall-penetrating cone; shatters 6 armor points, deflects all incoming enemy projectiles,
        // freezes standing water into Packed Ice, and inflicts Cryogenic Molecular Hypothermia
        // (disrupting mob AI pathfinding AND slowing movement by 60% for 30.0s / 600t as mobs shudder in frozen paralysis)!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(12.0D, coneRange);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 origin = caster.getEyePosition();

        SpellWorldVfxManager.broadcastVortexCone(
                level, origin.subtract(0.0D, 0.2D, 0.0D), dir, reach, EssenceType.TERMINUS, EssenceType.FROZEN, tier
        );

        AABB coneBox = caster.getBoundingBox().expandTowards(dir.scale(reach)).inflate(reach * 0.55D);
        ApexEschatonSingularityHelper.deflectHostileProjectiles(level, caster, coneBox);

        for (int step = 2; step <= (int) Math.min(14.0D, reach); step += 3) {
            BlockPos stepCenter = BlockPos.containing(origin.add(dir.scale(step)));
            int rad = Math.max(1, step / 4);
            for (BlockPos p : BlockPos.betweenClosed(stepCenter.offset(-rad, -2, -rad), stepCenter.offset(rad, 2, rad))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.WATER)) {
                    level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                }
            }
        }

        float delugeDmg = (6.4F + tier * 1.50F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
            hit.clearFire();
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600, 3)); // Slowness IV = exact -60% movement speed for 30.0s!
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 200);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 600);         // 30.0s AI pathfinding disruption!

            if (hit instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.setAggressive(false);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, hit, caster, delugeDmg, false, true, false);
            }
        }

        for (int s = 2; s <= 10; s += 4) {
            emitTerminusBurstParticles(level, origin.add(dir.scale(s)), 14, s * 0.22D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
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
        // Section 6.56 Continuous Cryo-Stasis Hailstorm [24.0s Hailstorm + 12.0s Absolute Zero Permafrost, 16m AOE]:
        // Drops 8 3D Skyfall Pseudoblock glacial monoliths (Blue Ice, Packed Ice, Runic Blue Ice, Zephyr Ice) that
        // permanently materialize into real blocks upon completion; locks caught hostiles frozen in suspended animation
        // for the 24.0s storm, continuously shatters 7 armor points, continually chips away at mob health, and leaves
        // a 12.0s zone of Absolute Zero Permafrost (freezes entering mobs solid; grants allies +6 Armor & Frost Walker)!
        final int tier = spell.getEffectiveTier();
        final double r = Math.max(16.0D, radius);
        final BlockPos centerPos = BlockPos.containing(impactCenter);
        final double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.TERMINUS, EssenceType.FROZEN, 720, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.04D, 0.0D), r, EssenceType.TERMINUS, EssenceType.WATER, 720, tier);
        level.playSound(null, centerPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.25F, 0.65F);

        final int monolithCount = 8;
        final BlockPos[] monolithTargets = new BlockPos[monolithCount];
        monolithTargets[0] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, centerPos);
        for (int i = 1; i < monolithCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (monolithCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            monolithTargets[i] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, raw);
        }

        final boolean[] landed = new boolean[monolithCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 720, level.getGameTime(), spell, slot, EssenceType.TERMINUS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < monolithCount; mIdx++) {
                                BlockState pseudoState = resolvePermafrostMonolithBlockState(mIdx);
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, monolithTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean hailstorm = elapsed < 480; // 24.0s active hailstorm, then 12.0s Absolute Zero Permafrost!
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (hailstorm) {
                        for (int mIdx = 0; mIdx < monolithCount; mIdx++) {
                            int impactTick = 22 + mIdx * 56;
                            BlockPos strikePos = monolithTargets[mIdx];
                            BlockState pseudoState = resolvePermafrostMonolithBlockState(mIdx);
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
                                        EssenceType.TERMINUS,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.TERMINUS, EssenceType.FROZEN, 2.75F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 26, 1.2D, 0.5D, 1.2D, 0.08D
                                );
                                srv.playSound(null, strikePos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 0.72F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 hailImpact = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, hailImpact, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.TERMINUS, EssenceType.FROZEN, 1.65F, zTier
                            );
                        }
                    }

                    for (int mIdx = 0; mIdx < monolithCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, monolithTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + (hailstorm ? 5.0D : 0.35D), zone.center.z,
                                28, zone.radius * 0.55D, hailstorm ? 2.5D : 0.35D, zone.radius * 0.55D, 0.05D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        float chipDmg = (5.4F + zTier * 1.25F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 6.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 12.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = ent.getX() - zone.center.x;
                            double dz = ent.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > rSq) continue;

                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 6.0D, 0.0D, 0.0D, 0.0D, 35L);
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, now + 40L);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) continue;

                            FrozenSpellMutationHandler.applyIceStasis(srv, owner, ent, hailstorm ? 30 : 45, 0, zone.spell, zone.slot);
                            if (hailstorm) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 7.0D, 0.0D, 200L);
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(srv, ent, owner, chipDmg, false, true, false);
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.TERMINUS, zone.apparatusPotency * 0.55F
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
        tag.putString("SpellEidolonMateria", EssenceType.TERMINUS.name());
        tag.putInt("SpellEidolonTintRGB", TERMINUS_FROST_CYAN_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius), EssenceType.TERMINUS, EssenceType.FROZEN, spell.getEffectiveTier()
        );
        emitTerminusBurstParticles(level, spawnPos.add(0.0D, 1.0D, 0.0D), 32, 0.55D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 0.95F, 1.45F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.56 Glacial Slipstream Flash-Slide:
        // Surges forward up to 22 blocks through solid walls on a frictionless ice-slipstream;
        // Departure leaves a 6m flash-freeze frost nova rooting pursuing hostiles in ice for 3.0s (60t);
        // Arrival grants Frost Walker, complete fall damage immunity, and +40% movement speed on ice for 10.0s (200t)
        // while shedding 100% of hostile mob aggro!
        SpellWorldVfxManager.broadcastRadialNova(level, departurePos.add(0.0D, 0.25D, 0.0D), 6.0D, EssenceType.TERMINUS, EssenceType.FROZEN, potencyTier);
        SpellWorldVfxManager.broadcastChainArc(
                level, departurePos.add(0.0D, 0.9D, 0.0D), arrivalPos.add(0.0D, 0.9D, 0.0D),
                EssenceType.TERMINUS, EssenceType.AIR, potencyTier
        );

        AABB dep = new AABB(
                departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 3.5D, departurePos.z + 6.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            FrozenSpellMutationHandler.applyIceStasis(level, caster, foe, 60, 0, null, null);
        }

        long now = level.getGameTime();
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        cData.putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, now + 200L);
        cData.putLong(SpellLingeringZoneManager.TAG_TERMINUS_FLASH_SLIDE_FALL_IMMUNE_UNTIL, now + 200L);
        cData.putLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL, Math.max(cData.getLong(SpellLingeringZoneManager.TAG_TERMINUS_MANTLE_UNTIL).orElse(0L), now + 200L));
        caster.resetFallDistance();
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.75F, null);

        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        emitTerminusBurstParticles(level, departurePos.add(0.0D, 0.8D, 0.0D), 22, 1.2D);
        emitTerminusBurstParticles(level, arrivalPos.add(0.0D, 0.8D, 0.0D), 22, 0.7D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.95F, 1.38F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.65F, 1.45F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.56 Zero-Point Crystallization & Permafrost Synthesis [5.0s Channel, strictly NO air transmutation]:
        // Transmutes standing water and rainfall into Blue Ice, lava sources into Obsidian,
        // stone/deepslate into Permafrost and Packed Ice, and converts standing water within 3 blocks
        // into Glacial Frost Elixirs when bottled using empty glass bottles!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 12, potencyTier, EssenceType.TERMINUS,
                "Zero-Point Crystallization & Permafrost Synthesis",
                () -> {
                    ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 3, EssenceType.TERMINUS);
                    if (caster != null) {
                        ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, caster.blockPosition(), 3, EssenceType.TERMINUS);
                    }
                })) {
            return true;
        }

        if (state.is(Blocks.WATER) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
            level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.PACKED_ICE)) {
            level.setBlockAndUpdate(pos, ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA) && state.getFluidState().isSource()) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        // Condense active rainfall onto exposed surfaces into solid Blue Ice!
        if (level.isRainingAt(pos.above())
                && level.getBlockState(pos.above()).isAir()
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos.above(), level.getBlockState(pos.above()))) {
            level.setBlockAndUpdate(pos.above(), Blocks.BLUE_ICE.defaultBlockState());
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            BlockState permafrost = switch (level.random.nextInt(3)) {
                case 0 -> ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState();
                case 1 -> ModBlocks.BLUE_FROZEN_MOSS.get().defaultBlockState();
                default -> Blocks.PACKED_ICE.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, permafrost);
            return true;
        }
        if (state.is(BlockTags.DIRT)) {
            level.setBlockAndUpdate(pos, ModBlocks.BLUE_FROZEN_MOSS.get().defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.56 Absolute Zero Molecular Cessation Hex:
        // Continuously inflicts true frostbite damage, slows movement by exact 80%, cuts attack speed by exact 60%,
        // shreds 50% of Armor Toughness, and disables all jumping!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);

        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.92D, 0.80D * mult), 35L);   // Exact -80% move speed!
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.85D, 0.60D * mult), 35L); // Exact -60% attack speed!
        data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
        target.clearFire();
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + 70));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCasterPublic(level, target);
        float frostbiteDmg = (2.65F + potencyTier * 0.68F) * mult;
        SpellCombatReactionEngine.hurtWithTerminusMateriaAdvantage(level, target, hexCaster, frostbiteDmg, false, false, true);
        emitTerminusBurstParticles(level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), 6, 0.28D);
        return true;
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private static BlockState resolvePermafrostMonolithBlockState(int index) {
        return switch (index & 3) {
            case 0 -> Blocks.BLUE_ICE.defaultBlockState();
            case 1 -> Blocks.PACKED_ICE.defaultBlockState();
            case 2 -> ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState();
            default -> ModBlocks.ZEPHYR_ICE.get().defaultBlockState();
        };
    }

    private static void snuffFiresAndTorchesWithinSixBlocks(ServerLevel level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 4, 6))) {
            if (center.distSqr(p) > 36.0D) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof TorchBlock
                    || bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH)
                    || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                level.destroyBlock(p, true);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof CandleBlock && bs.getValue(CandleBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CandleBlock.LIT, false));
            }
        }
    }

    private static void emitTerminusBurstParticles(ServerLevel level, Vec3 center, int count, double spread) {
        int perColor = Math.max(2, count / 3);
        for (int col : TERMINUS_PALETTE) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 18, 0.42F, 0.04F),
                    center.x, center.y, center.z, perColor, spread, spread * 0.45D, spread, 0.03D
            );
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, Math.max(4, count / 2),
                spread * 0.85D, spread * 0.45D, spread * 0.85D, 0.04D);
    }
}
