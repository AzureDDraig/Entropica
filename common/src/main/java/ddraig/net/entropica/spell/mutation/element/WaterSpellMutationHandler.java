package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.CastingApparatusItem;
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
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Complete per-part Signature Mutation implementation for <b>Water Materia (Base Type #2)</b>
 * (Spellcrafting System Guide Section 6.3):
 * <ul>
 *     <li><b>Flow (Slow / Chain)</b>: <i>Viscous Fluid Mass</i> (+0.6 Materia Advantage vs burning/Nether/fire/magma targets)
 *         &amp; <i>Riptide Undertow Chains</i> (drenches chained targets and quenches flames).</li>
 *     <li><b>Projectile</b>: <i>Hydro-Cannon Slug</i> — fluid-dense 3D water orb with a slight gravity arc and an exact 3m impact splash
 *         (30% Harm splash damage, knockback, fire dousing, farmland hydration, and heavy soak).</li>
 *     <li><b>Beam</b>: <i>High-Pressure Hydro-Cutter</i> — piercing pressurized water jet dealing 2x horizontal and vertical knockback
 *         to pre-soaked/swimming foes, washing away cobwebs, fire, and surface weeds, and hydrating farmland.</li>
 *     <li><b>Radial Burst</b>: <i>Tidal Nova</i> — uncapped circular tsunami wave flushing non-boss mobs back, solidifying lava into obsidian/cobblestone,
 *         clearing surface flora/weeds, hydrating farmland, and leaving foes dripping wet.</li>
 *     <li><b>Touch / Strike</b>: <i>Crashing Breaker</i> — hydrostatic shockwave stunning the primary non-boss foe for 1.0s, drenching targets,
 *         and dealing +50% combat bonus damage vs burning or true Nether/Fire foes.</li>
 *     <li><b>Aura / Self</b>: <i>Oceanic Shroud</i> — Water Breathing, Dolphin's Grace, Water defending affinity, fire/lava immunity,
 *         and 40% explosion damage dampening.</li>
 *     <li><b>Glyph / Trap</b>: <i>Whirlpool Geyser</i> — 1s arm, 10m/indefinite persistence, 50% disarm refund; circular upward spinning
 *         3s drowning water vortex immobilizing non-boss victims.</li>
 *     <li><b>Cone / Spray</b>: <i>Deluge Torrent</i> — exact 60-degree foaming surge (12m+ reach) sweeping non-boss mobs and incoming physical projectiles back,
 *         soaking entities, extinguishing fires/campfires/candles, and hydrating tilled farmland across the full cone.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Monsoon Cloudburst</i> — 8s–36s torrential downpour scaling from 8m to 16m circular radius across Tiers 1–10,
 *         extinguishing fires, hydrating soil, healing friendly aquatic creatures, and drowning true Nether/Fire mobs.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Aqueous Eidolon</i> — deep azure (#225C7C) Agent (water tendrils), Sentry (arcing soaking orbs + passive fire/soil care),
 *         or Decoy (pops immediately when struck into a 3.5s blinding splash).</li>
 *     <li><b>Blink / Step</b>: <i>Fluid Dissolution</i> — dissolves into a high-speed water droplet (2m departure + 3m arrival splashes, open-water landing).</li>
 *     <li><b>Transmute</b>: <i>Aqueous Elutriation</i> — washes dirt/grass into clay (4 clay balls = 1 clay on dropped dirt), sand into mud,
 *         sponge into wet sponge, concrete powder into concrete, magma into cobblestone, and cleans oxidized copper across any namespace.</li>
 *     <li><b>Affliction</b>: <i>Waterlogged Drowning Hex</i> — persistent anti-milk soaking curse immediately quenching flames and dealing
 *         continuous land drowning damage, 2x Lightning damage, and 3x Frozen freeze rates.</li>
 * </ul>
 */
public final class WaterSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int AQUEOUS_EIDOLON_HEX_COLOR = 0x225C7C;
    private static final int WATER_AZURE_RGB = 0x225C7C;
    private static final int WATER_CERULEAN_RGB = 0x24B6FF;
    private static final int WATER_SEAFOAM_RGB = 0xB8FAFF;

    /** cos(30 deg) — exact 60-degree full cone half-angle cosine. */
    private static final double CONE_HALF_ANGLE_COS = 0.8660254D;
    /** tan(30 deg) — exact 60-degree full cone half-angle tangent. */
    private static final double CONE_HALF_ANGLE_TAN = 0.57735027D;

    @Override
    public EssenceType materiaType() {
        return EssenceType.WATER;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.3)
    // =========================================================================

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Viscous Fluid Mass";
            case CHAIN_LEAP -> "Riptide Undertow Chains";
            default -> null;
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Viscous Fluid Mass: Travels slowly with heavy fluid volume, adding +0.6 Materia Advantage score against burning, Nether, and fire/magma targets.";
            case CHAIN_LEAP -> "Riptide Undertow Chains: Pressurized water streams arc between targets, drenching chained foes and quenching flames.";
            default -> null;
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hydro-Cannon Slug";
            case BEAM -> "High-Pressure Hydro-Cutter";
            case RADIAL_BURST -> "Tidal Nova";
            case TOUCH_STRIKE -> "Crashing Breaker";
            case AURA -> "Oceanic Shroud";
            case GLYPH -> "Whirlpool Geyser";
            case CONE -> "Deluge Torrent";
            case SKYFALL -> "Monsoon Cloudburst";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hydro-Cannon Slug: Heavy, fluid-dense water orb flying with a slight gravity arc; impacts burst in a 3m splash (30% Harm splash damage, knockback, fire dousing, farmland hydration, and heavy soak).";
            case BEAM -> "High-Pressure Hydro-Cutter: Piercing pressurized water jet dealing 2x horizontal and vertical knockback to pre-soaked or swimming foes, washing away cobwebs, fire, and weeds, and hydrating farmland.";
            case RADIAL_BURST -> "Tidal Nova: Expanding circular tsunami wave flushing foes backward, cooling lava into obsidian/cobblestone, clearing surface flora, hydrating soil, and leaving foes soaked.";
            case TOUCH_STRIKE -> "Crashing Breaker: Hydrostatic strike stunning the primary non-boss foe for 1.0s, drenching cleave targets, and dealing +50% combat bonus damage vs burning or Nether/Fire foes.";
            case AURA -> "Oceanic Shroud: Grants underwater breathing, Dolphin's Grace swim speed, fire/lava immunity, and 40% explosion damage dampening (with Water affinity).";
            case GLYPH -> "Whirlpool Geyser: Arms in 1s, lasts 10m (indefinite at Tier 5+); erupts into a circular 3s upward spinning water vortex immobilizing and drowning non-boss victims.";
            case CONE -> "Deluge Torrent: 60° foaming water surge (12m+ reach) sweeping foes and incoming physical projectiles back, soaking targets, extinguishing fires, and hydrating farmland.";
            case SKYFALL -> "Monsoon Cloudburst: 8s–36s torrential downpour (8m–16m circular radius) extinguishing fires, hydrating soil, healing friendly aquatic creatures, and drowning Nether/Fire mobs.";
            case SUMMON_AGENT -> "Aqueous Agent: Deep azure (#225C7C) water-hunter that lashes and drenches targets with pressurized water tendrils.";
            case SUMMON_SENTRY -> "Aqueous Sentry: Anchored deep azure (#225C7C) hydro-spire launching arcing soaking water orbs while passively dousing nearby flames and hydrating farmland.";
            case SUMMON_DECOY -> "Aqueous Decoy: Shimmering deep azure (#225C7C) aquatic mirage that pops immediately when struck into a 3.5s blinding, soaking splash.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Fluid Dissolution";
            case TRANSMUTE, BREAK -> "Aqueous Elutriation";
            case AFFLICTION -> "Waterlogged Drowning Hex";
            case HARM -> "Hydrostatic Surge";
            case WARD -> "Tidal Bulwark";
            case RESTORATION -> "Springwater Cleansing";
            default -> "Aqueous " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Fluid Dissolution: Dissolves you into a high-speed water droplet (splashing 2m on departure and 3m on reform, and able to land atop open water).";
            case TRANSMUTE, BREAK -> "Aqueous Elutriation: Washes dirt/grass into clay (4 clay balls = 1 clay for dropped dirt), sand into mud, sponge into wet sponge, concrete powder into concrete, magma into cobblestone, and cleans oxidized copper.";
            case AFFLICTION -> "Waterlogged Drowning Hex: Persistent anti-milk soaking curse immediately quenching flames and dealing continuous drowning damage on land, 2x Lightning damage, and 3x Frozen freeze rates.";
            case HARM -> "Hydrostatic Surge: Pure Water Materia concussion (+1 vs Nether & Arid, -1 vs Frozen & Nature) soaking struck foes.";
            case WARD -> "Tidal Bulwark: Wraps the Arcane Ward in pressurized ocean currents that quench flames and cushion blast shockwaves.";
            case RESTORATION -> "Springwater Cleansing: Washes away burning and negative ailments while restoring vitality.";
            default -> "Aqueous " + effect.getDisplayName() + ": Infuses the spell effect with pressurized Water Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS
    // =========================================================================

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        // Viscous Fluid Mass: +0.6 Materia Advantage score strictly against burning or true Nether/Fire/Magma targets
        if (flow == SpellFlowModifier.SLOW_HEAVY && (target.isOnFire() || isTrueNetherOrFireEntity(target))) {
            return 0.6F;
        }
        return 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow == SpellFlowModifier.SLOW_HEAVY) {
            // Heavy fluid volume wake droplets & surging bubbles
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_CERULEAN_RGB, 14, 0.24F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    2, 0.16D, 0.16D, 0.16D, 0.015D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), WATER_AZURE_RGB, 16, 0.22F, 0.02F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    2, 0.14D, 0.14D, 0.14D, 0.01D
            );
            if (projectile.tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(),
                        3, 0.16D, 0.14D, 0.16D, 0.025D);
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
        Vec3 from = fromTarget.getEyePosition().subtract(0.0D, 0.25D, 0.0D);
        Vec3 to = toTarget.getEyePosition().subtract(0.0D, 0.25D, 0.0D);
        SpellWorldVfxManager.broadcastChainArc(level, from, to, EssenceType.WATER, null, spell.getEffectiveTier());
        fromTarget.clearFire();
        toTarget.clearFire();
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            SpellLingeringZoneManager.applySoaked(toTarget, 180);
        }
        level.sendParticles(ParticleTypes.SPLASH, to.x, to.y, to.z, 10, 0.25D, 0.25D, 0.25D, 0.08D);
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
        // Hydro-Cannon Slug: smooth calibrated slight gravity arc (enlarged heavy fluid arc when paired with Viscous Fluid Mass)
        boolean heavyFluidMass = spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY;
        projectile.enableSlightFluidArc(heavyFluidMass);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), WATER_CERULEAN_RGB, 12, 0.18F, 0.02F),
                projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.08D, 0.08D, 0.08D, 0.01D
        );
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_SEAFOAM_RGB, 10, 0.16F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    1, 0.09D, 0.09D, 0.09D, 0.01D
            );
            level.sendParticles(ParticleTypes.DRIPPING_WATER, projectile.getX(), projectile.getY(), projectile.getZ(),
                    1, 0.08D, 0.08D, 0.08D, 0.0D);
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
        triggerHydroSplash(level, projectile.position(), new Vec3(0.0D, 1.0D, 0.0D), caster, hitEntity, spell, slot, 3.0D);
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
        triggerHydroSplash(level, blockHit.getLocation(), normal, caster, null, spell, slot, 3.0D);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Hydro-Cannon Slug splashes ONLY on actual entity impact or block collision — never explode in mid-air on max-range timeout!
        level.sendParticles(ParticleTypes.DRIPPING_WATER, projectile.getX(), projectile.getY(), projectile.getZ(),
                5, 0.12D, 0.12D, 0.12D, 0.02D);
    }

    public void triggerHydroSplash(
            ServerLevel level,
            Vec3 pos,
            Vec3 normal,
            @Nullable LivingEntity caster,
            @Nullable Entity directHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            double rad
    ) {
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcastProjectileImpact(level, pos, normal, EssenceType.WATER, null, (float) rad, tier);

        douseAndHydrateCircularArea(level, pos, rad, false, caster);

        // Exact circular/spherical 3.0m splash radius (distSq <= rad * rad = 9.0D, with no +0.35D inflation)
        double radSq = rad * rad;
        float splashDmg = 0.30F * MateriaStageTable.getBaseCombatDamageHp(tier);
        AABB box = new AABB(pos.x - rad, pos.y - rad, pos.z - rad, pos.x + rad, pos.y + rad, pos.z + rad);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (nearby != directHit && nearby.getBoundingBox().getCenter().distanceToSqr(pos) > radSq) {
                continue;
            }
            // Douse burning status on everyone (including allies) in the 3-block splash!
            nearby.clearFire();
            if (nearby == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(nearby, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            // Heavy soaking wetness (11 seconds -> 2x Lightning & 3x Ice!)
            SpellLingeringZoneManager.applySoaked(nearby, 220);

            if (nearby != directHit && slot.hasHarmVerb() && splashDmg >= 1.0F) {
                SpellCombatReactionEngine.hurtWithPureMateria(
                        level,
                        nearby,
                        caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic(),
                        splashDmg
                );
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, nearby)) {
                Vec3 delta = nearby.position().subtract(pos);
                Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                Vec3 pushDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                SpellExecutionEngine.applySyncedMotion(nearby, new Vec3(pushDir.x * 0.52D, 0.24D, pushDir.z * 0.52D));
            }
        }

        level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + 0.35D, pos.z, 34, 0.95D, 0.45D, 0.95D, 0.16D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_CERULEAN_RGB, 18, 0.28F, 0.05F),
                pos.x, pos.y + 0.4D, pos.z, 14, 0.85D, 0.35D, 0.85D, 0.05D
        );
        level.playSound(null, BlockPos.containing(pos), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.9F, 1.08F);
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
        // High-Pressure Hydro-Cutter: cuts through mobs, pushes pre-soaked or swimming enemies with exact 2.0x horizontal + vertical knockback,
        // washes away cobwebs, fire, campfires/candles, and surface weeds, and hydrates farmland!
        boolean continuousPulse = apparatusPotency <= 0.45F;
        double baseKb = continuousPulse ? 0.50D : 0.88D;
        double baseLiftY = Math.max(0.18D, direction.y * baseKb * 0.55D + 0.16D);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            hit.clearFire();
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            // Check already-wet / swimming state BEFORE applying this pulse's soak!
            boolean wasSoakedBeforePulse = SpellLingeringZoneManager.isEntityWetOrSoaked(hit);
            boolean alreadyWetOrSwimming = hit.isInWaterOrRain() || wasSoakedBeforePulse;
            SpellLingeringZoneManager.applySoaked(hit, 200);

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                double kbMult = alreadyWetOrSwimming ? 2.0D : 1.0D;
                double horizKb = baseKb * kbMult;
                double liftY = baseLiftY * kbMult;
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(direction.x * horizKb, liftY, direction.z * horizKb));
            }
            level.sendParticles(ParticleTypes.SPLASH, hit.getX(), hit.getY(0.65D), hit.getZ(),
                    continuousPulse ? 6 : 14, 0.25D, 0.3D, 0.25D, 0.10D);
        }

        Vec3 eye = caster.getEyePosition();
        double effectiveReach = maxReach;
        if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
            effectiveReach = Math.min(maxReach, eye.distanceTo(blockHit.getLocation()) + 0.65D);
        }

        for (double d = 0.5D; d <= effectiveReach; d += 0.75D) {
            BlockPos p = BlockPos.containing(eye.add(direction.scale(d)));
            douseAndHydrateArea(level, p, 1, true, caster);
        }

        if (!continuousPulse || level.getGameTime() % 8L == 0L) {
            level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS,
                    continuousPulse ? 0.45F : 0.85F, 1.35F);
        }
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
        if (hitEntities == null || hitEntities.isEmpty()) {
            return;
        }
        // Crashing Breaker: 1.0s (20t) hydrostatic stun (skipping bosses) and +50% combat bonus damage vs burning or true Nether/Fire foes
        // strictly on hitEntities.getFirst() (i == 0), while secondary cleave bystanders receive soaking + minor push only!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        boolean meleeStrike = isMeleeWeaponStrike(caster);
        boolean combatTouch = slot.hasHarmVerb() || (meleeStrike && isCombatCompatibleTouchSlot(slot));

        float harmBase = slot.hasHarmVerb()
                ? MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier()
                : 0.0F;
        float weaponBase = meleeStrike ? (float) Math.max(0.0D, caster.getAttributeValue(Attributes.ATTACK_DAMAGE)) : 0.0F;
        float bonusDmg = Math.max(2.0F, 0.50F * (harmBase + weaponBase));

        SpellWorldVfxManager.broadcastCrescentSlash(
                level,
                caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D),
                direction,
                2.8D,
                EssenceType.WATER,
                null,
                tier
        );

        LivingEntity primaryRef = hitEntities.getFirst();
        boolean hitAny = false;
        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (hit == null || !hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) {
                hit.clearFire();
                continue;
            }
            hitAny = true;

            if (i == 0) {
                boolean bonusVsFire = combatTouch && (hit.isOnFire() || isTrueNetherOrFireEntity(hit));
                hit.clearFire();
                SpellLingeringZoneManager.applySoaked(hit, 220);

                // 1.0-second (20-tick) hydrostatic stun on the primary non-boss target only
                if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                    hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
                    if (hit instanceof Mob mob) {
                        mob.getNavigation().stop();
                    }
                }

                Vec3 hitCenter = hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D);
                SpellWorldVfxManager.broadcastProjectileImpact(level, hitCenter, direction, EssenceType.WATER, null, 2.4F, tier);
                level.sendParticles(ParticleTypes.SPLASH, hitCenter.x, hitCenter.y, hitCenter.z, 22, 0.45D, 0.4D, 0.45D, 0.14D);

                if (bonusVsFire) {
                    hit.invulnerableTime = 0;
                    SpellCombatReactionEngine.hurtWithPureMateria(
                            level,
                            hit,
                            level.damageSources().indirectMagic(caster, caster),
                            bonusDmg
                    );
                    level.sendParticles(ParticleTypes.CLOUD, hitCenter.x, hitCenter.y, hitCenter.z, 12, 0.35D, 0.35D, 0.35D, 0.06D);
                    level.playSound(null, hit.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.75F, 1.15F);
                }
            } else {
                // Secondary cleave bystanders (i > 0): douse, soak, and minor non-boss push only (no stun, no +50% bonus hit)
                hit.clearFire();
                SpellLingeringZoneManager.applySoaked(hit, 160);
                if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                    Vec3 cur = hit.getDeltaMovement();
                    Vec3 away = primaryRef != null ? hit.position().subtract(primaryRef.position()) : direction;
                    Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                    Vec3 pushDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(direction.x, 0.0D, direction.z);
                    SpellExecutionEngine.applySyncedMotion(
                            hit,
                            new Vec3(cur.x * 0.40D + pushDir.x * 0.32D, Math.max(cur.y, 0.18D), cur.z * 0.40D + pushDir.z * 0.32D)
                    );
                }
                level.sendParticles(ParticleTypes.SPLASH, hit.getX(), hit.getY(0.5D), hit.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.08D);
            }
        }

        if (hitAny) {
            level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.95F, 0.92F);
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
        // Tidal Nova: uncapped expanding circular tsunami wave flushing non-boss mobs backward, cooling source lava -> obsidian
        // & flowing lava / magma blocks -> cobblestone, clearing surface flora/weeds, hydrating farmland, and soaking foes!
        double waveRad = Math.max(5.5D, radius);
        double waveRadSq = waveRad * waveRad;

        AABB box = new AABB(center.x - waveRad, center.y - 2.0D, center.z - waveRad,
                center.x + waveRad, center.y + 2.75D, center.z + waveRad);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;
            if (dx * dx + dz * dz > waveRadSq) continue;
            entity.clearFire();
            if (entity == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            SpellLingeringZoneManager.applySoaked(entity, 240);
            if (!SpellCombatReactionEngine.isBossEntity(level, entity)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : direction;
                SpellExecutionEngine.applySyncedMotion(entity, new Vec3(away.x * 1.45D, 0.38D, away.z * 1.45D));
            }
        }

        int r = (int) Math.ceil(waveRad);
        BlockPos cPos = BlockPos.containing(center);
        boolean cooledLava = false;
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-r, -1, -r), cPos.offset(r, 2, r))) {
            double dx = (p.getX() + 0.5D) - center.x;
            double dz = (p.getZ() + 0.5D) - center.z;
            if (dx * dx + dz * dz > waveRadSq) continue;

            BlockState bs = level.getBlockState(p);
            if (isSurfaceWeed(bs) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                level.destroyBlock(p, true, caster);
            } else if (bs.is(Blocks.LAVA)) {
                BlockState cooled = bs.getFluidState().isSource()
                        ? Blocks.OBSIDIAN.defaultBlockState()
                        : Blocks.COBBLESTONE.defaultBlockState();
                level.setBlockAndUpdate(p, cooled);
                cooledLava = true;
            } else if (bs.is(Blocks.MAGMA_BLOCK) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                level.setBlockAndUpdate(p, Blocks.COBBLESTONE.defaultBlockState());
                cooledLava = true;
            }
        }

        douseAndHydrateCircularArea(level, center, waveRad, false, caster);

        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.55D, center.z, 52, waveRad * 0.45D, 0.35D, waveRad * 0.45D, 0.16D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_SEAFOAM_RGB, 20, 0.30F, 0.05F),
                center.x, center.y + 0.65D, center.z, 24, waveRad * 0.42D, 0.3D, waveRad * 0.42D, 0.06D
        );
        level.playSound(null, cPos, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.0F, 0.88F);
        if (cooledLava) {
            level.playSound(null, cPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.85F, 1.05F);
        }
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
        // Oceanic Shroud: underwater breathing, Dolphin's Grace swimming speed, Water defending affinity,
        // 100% fire/lava immunity, and 40% explosion damage dampening (rendered as a persistent 3D azure shroud)!
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        long now = level.getGameTime();

        caster.clearFire();
        caster.setAirSupply(caster.getMaxAirSupply());
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        data.putLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL, now + dur);

        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, true));

        // Sync persistent 3D Oceanic Shroud barrier state immediately to nearby clients
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);

        level.sendParticles(
                TimedTintableParticleOption.spiral(ModParticles.TINTABLE_BUBBLE.get(), WATER_CERULEAN_RGB, 24, 0.26F,
                        (float) caster.getX(), 0.06F, (float) caster.getZ()),
                caster.getX(), caster.getY(0.5D), caster.getZ(), 18, 0.45D, 0.5D, 0.45D, 0.04D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.75F, 1.25F);
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
        // Whirlpool Geyser (Section 4.6 + Section 6.3):
        // 1.0s (20t) arming delay, 10m persistence (indefinite at Tier 5+), 50% Sneak-Right-Click disarm refund;
        // circular detection & circular 3.0s (60t) upward spinning water vortex immobilizing non-boss victims and drowning!
        final int tier = spell.getEffectiveTier();
        final int armingTicks = 20;
        final int maxWaitTicks = tier >= 5 ? 72_000_000 : 12_000;
        final double triggerRad = Math.max(2.6D, Math.min(4.0D, radius));
        final double triggerRadSq = triggerRad * triggerRad;

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster,
                glyphCenter,
                triggerRad,
                maxWaitTicks,
                level.getGameTime(),
                spell,
                slot,
                fallbackMateria,
                apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed % 12 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_CERULEAN_RGB, 26, 0.22F, 0.04F),
                                zone.center.x, zone.center.y + 0.08D, zone.center.z,
                                5, triggerRad * 0.45D, 0.05D, triggerRad * 0.45D, 0.015D
                        );
                    }

                    // 1.0s (20-tick) arming grace period
                    if (elapsed < armingTicks) {
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean safeguard = zone.spell.hasWardFriendlyFireSafeguard();
                    AABB detectBox = new AABB(
                            zone.center.x - triggerRad, zone.center.y - 1.0D, zone.center.z - triggerRad,
                            zone.center.x + triggerRad, zone.center.y + 2.6D, zone.center.z + triggerRad
                    );
                    List<LivingEntity> entering = srv.getEntitiesOfClass(LivingEntity.class, detectBox, e -> {
                        if (!e.isAlive() || e == owner) return false;
                        double dx = e.getX() - zone.center.x;
                        double dz = e.getZ() - zone.center.z;
                        if (dx * dx + dz * dz > triggerRadSq) return false;
                        return !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, safeguard);
                    });
                    if (entering.isEmpty()) {
                        return true;
                    }

                    // Erupt into a 3-second (60-tick) upward spinning 3D Whirlpool Geyser vortex (+ smooth 16t visual subside)!
                    final int vortexDuration = 60;
                    final double vortexRad = Math.max(2.8D, triggerRad);
                    final double vortexRadSq = vortexRad * vortexRad;
                    SpellWorldVfxManager.broadcastGeyserPlume(srv, zone.center, vortexRad, 4.4D, EssenceType.WATER, null, vortexDuration + 16, tier);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.05F, 0.85F);

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner,
                            zone.center,
                            vortexRad,
                            vortexDuration,
                            srv.getGameTime(),
                            zone.spell,
                            zone.slot,
                            zone.fallbackMateria,
                            zone.apparatusPotency,
                            (wsrv, wzone, welapsed) -> {
                                if (welapsed > wzone.maxDurationTicks) return false;
                                LivingEntity wOwner = wzone.resolveCaster(wsrv);
                                boolean wSafeguard = wzone.spell.hasWardFriendlyFireSafeguard();

                                if (welapsed % 3 == 0) {
                                    wsrv.sendParticles(
                                            TimedTintableParticleOption.spiral(ModParticles.TINTABLE_BUBBLE.get(), WATER_CERULEAN_RGB,
                                                    15, 0.26F, (float) wzone.center.x, 0.14F, (float) wzone.center.z),
                                            wzone.center.x, wzone.center.y + 0.4D, wzone.center.z,
                                            8, vortexRad * 0.4D, 0.9D, vortexRad * 0.4D, 0.05D
                                    );
                                    wsrv.sendParticles(ParticleTypes.SPLASH, wzone.center.x, wzone.center.y + 1.1D, wzone.center.z,
                                            10, 0.65D, 0.95D, 0.65D, 0.08D);
                                }
                                if (welapsed % 20 == 0) {
                                    wsrv.playSound(null, BlockPos.containing(wzone.center), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
                                            SoundSource.PLAYERS, 0.9F, 1.05F);
                                }

                                AABB trapBox = new AABB(
                                        wzone.center.x - vortexRad, wzone.center.y - 1.0D, wzone.center.z - vortexRad,
                                        wzone.center.x + vortexRad, wzone.center.y + 4.2D, wzone.center.z + vortexRad
                                );
                                for (LivingEntity v : wsrv.getEntitiesOfClass(LivingEntity.class, trapBox, LivingEntity::isAlive)) {
                                    if (v == wOwner) continue;
                                    double dx = wzone.center.x - v.getX();
                                    double dz = wzone.center.z - v.getZ();
                                    if (dx * dx + dz * dz > vortexRadSq) continue;
                                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, wOwner, wSafeguard)) {
                                        v.clearFire();
                                        continue;
                                    }
                                    v.clearFire();
                                    SpellLingeringZoneManager.applySoaked(v, 140);

                                    // Hold non-boss victims immobilized & suspended inside the upward spinning vortex!
                                    if (!SpellCombatReactionEngine.isBossEntity(wsrv, v)) {
                                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10, 6, false, false));
                                        v.addEffect(new MobEffectInstance(
                                                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 10, 0, false, false));
                                        if (v instanceof Mob mob) {
                                            mob.getNavigation().stop();
                                        }
                                        double targetHoverY = wzone.center.y + 0.85D;
                                        double vy = Mth.clamp((targetHoverY - v.getY()) * 0.35D, -0.06D, 0.24D);
                                        SpellExecutionEngine.applySyncedMotion(v, new Vec3(dx * 0.38D, vy, dz * 0.38D));
                                        v.setYRot(v.getYRot() + 18.0F);
                                        v.yHeadRot = v.getYRot();
                                    }

                                    // Continuous drowning air drain + periodic drowning ticks
                                    v.setAirSupply(Math.max(-20, v.getAirSupply() - 10));
                                    if (welapsed == 1 || welapsed % 15 == 0) {
                                        v.hurtServer(wsrv, wsrv.damageSources().drown(), 2.0F + tier * 0.65F);
                                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                                wsrv, wOwner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                                wzone.spell, wzone.slot, wzone.fallbackMateria, wzone.apparatusPotency * 0.35F
                                        );
                                    }
                                }
                                return true;
                            }
                    ));
                    return false;
                }
        ).markDisarmableGlyph());

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.7F, 1.3F);
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
        // Deluge Torrent: exact 60-degree foaming surge (12m+ reach) sweeping non-boss mobs and incoming physical projectiles back,
        // soaking entities, extinguishing fire/campfires/candles, and hydrating all tilled farmland across the full cone!
        Vec3 dir = direction.lengthSqr() > 1.0E-6D ? direction.normalize() : caster.getLookAngle().normalize();
        double delugeReach = Math.max(12.0D, coneRange);
        double rangeSq = delugeReach * delugeReach;
        Vec3 origin = caster.getEyePosition();
        double maxConeWidth = delugeReach * CONE_HALF_ANGLE_TAN + 1.25D;
        AABB delugeBox = new AABB(origin, origin.add(dir.scale(delugeReach))).inflate(maxConeWidth);
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, delugeBox, LivingEntity::isAlive)) {
            if (cand == caster) continue;
            Vec3 toBody = cand.getBoundingBox().getCenter().subtract(origin);
            Vec3 toEye = cand.getEyePosition().subtract(origin);
            if (toBody.lengthSqr() > rangeSq && toEye.lengthSqr() > rangeSq) continue;
            boolean inCone = (toBody.lengthSqr() > 1.0E-6D && toBody.normalize().dot(dir) >= CONE_HALF_ANGLE_COS)
                    || (toEye.lengthSqr() > 1.0E-6D && toEye.normalize().dot(dir) >= CONE_HALF_ANGLE_COS);
            if (!inCone) continue;

            cand.clearFire();
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, safeguard)) {
                continue;
            }
            if (!hitTargets.contains(cand)) {
                SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, cand, dir, spell, slot, fallbackMateria, apparatusPotency);
            }
            SpellLingeringZoneManager.applySoaked(cand, 220);
            if (!SpellCombatReactionEngine.isBossEntity(level, cand)) {
                SpellExecutionEngine.applySyncedMotion(
                        cand,
                        new Vec3(dir.x * 1.55D, Math.max(0.32D, dir.y * 0.55D + 0.28D), dir.z * 1.55D)
                );
            }
        }

        // Push ONLY incoming non-allied physical projectiles inside the 60-degree cone
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, delugeBox)) {
            if (!proj.isAlive()) continue;
            if (proj instanceof SpellProjectileEntity || proj instanceof FishingHook) continue;
            if (isGroundedArrow(proj)) continue;

            Entity pOwner = proj.getOwner();
            if (pOwner == caster) continue;
            if (pOwner instanceof LivingEntity livingOwner
                    && (ApexEschatonSingularityHelper.isFriendly(livingOwner, caster)
                            || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(livingOwner, caster, true))) {
                continue;
            }

            Vec3 toProj = proj.position().subtract(origin);
            if (toProj.lengthSqr() <= 1.0E-6D || toProj.lengthSqr() > rangeSq) continue;
            if (toProj.normalize().dot(dir) < CONE_HALF_ANGLE_COS) continue;

            Vec3 pVel = proj.getDeltaMovement();
            if (pVel.lengthSqr() <= 1.0E-6D || pVel.dot(dir) > 0.15D) continue;

            SpellExecutionEngine.applySyncedMotion(
                    proj,
                    new Vec3(dir.x * 1.65D, Math.max(0.28D, dir.y * 1.15D), dir.z * 1.65D)
            );
            level.sendParticles(ParticleTypes.SPLASH, proj.getX(), proj.getY(), proj.getZ(), 4, 0.12D, 0.12D, 0.12D, 0.05D);
        }

        // Uncapped 60-degree cone block scan out to full delugeReach for fire, campfires, candles, and tilled farmland!
        douseAndHydrateAcrossFullCone(level, caster, origin, dir, delugeReach);

        // Uncapped splash & seafoam bubble bursts along the full delugeReach & 60-degree cone spread
        for (double burst = 2.0D; burst <= delugeReach; burst += 3.5D) {
            Vec3 sp = origin.add(dir.scale(burst));
            double spread = burst * CONE_HALF_ANGLE_TAN * 0.42D;
            level.sendParticles(ParticleTypes.SPLASH, sp.x, sp.y, sp.z, 24, spread, 0.35D, spread, 0.16D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_SEAFOAM_RGB, 16, 0.26F, 0.05F),
                    sp.x, sp.y, sp.z, 10, spread * 0.90D, 0.28D, spread * 0.90D, 0.05D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.0F, 0.92F);
    }

    private static void douseAndHydrateAcrossFullCone(
            ServerLevel level,
            LivingEntity caster,
            Vec3 origin,
            Vec3 dir,
            double delugeReach
    ) {
        Vec3 tipEnd = origin.add(dir.scale(delugeReach));
        double endRadius = delugeReach * CONE_HALF_ANGLE_TAN + 0.85D;

        int minX = (int) Math.floor(Math.min(origin.x - 1.0D, tipEnd.x - endRadius));
        int maxX = (int) Math.ceil(Math.max(origin.x + 1.0D, tipEnd.x + endRadius));
        int minY = (int) Math.floor(Math.min(origin.y - 3.0D, tipEnd.y - endRadius - 1.5D));
        int maxY = (int) Math.ceil(Math.max(origin.y + 1.5D, tipEnd.y + endRadius));
        int minZ = (int) Math.floor(Math.min(origin.z - 1.0D, tipEnd.z - endRadius));
        int maxZ = (int) Math.ceil(Math.max(origin.z + 1.0D, tipEnd.z + endRadius));

        double maxRangeSq = (delugeReach + 0.75D) * (delugeReach + 0.75D);
        Vec3 horizDir = new Vec3(dir.x, 0.0D, dir.z);
        boolean hasHorizAim = horizDir.lengthSqr() > 0.12D;
        Vec3 normHorizDir = hasHorizAim ? horizDir.normalize() : Vec3.ZERO;

        for (BlockPos p : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            Vec3 rel = Vec3.atCenterOf(p).subtract(origin);
            double distSq = rel.lengthSqr();
            if (distSq > maxRangeSq) continue;

            if (distSq > 1.5625D) {
                double axial3D = rel.dot(dir);
                boolean in3DCone = false;
                if (axial3D > 0.0D && axial3D <= delugeReach + 0.75D) {
                    double radialSq3D = Math.max(0.0D, distSq - axial3D * axial3D);
                    double allowed3D = axial3D * CONE_HALF_ANGLE_TAN + 0.55D;
                    in3DCone = radialSq3D <= allowed3D * allowed3D;
                }
                // Also match ground/farmland blocks directly beneath a horizontal 60-degree cone sheet (within 2.8m below ray centerline)
                boolean inGroundConeSheet = false;
                if (!in3DCone && hasHorizAim) {
                    double hAxial = rel.x * normHorizDir.x + rel.z * normHorizDir.z;
                    if (hAxial > 0.0D && hAxial <= delugeReach + 0.65D) {
                        double hDistSq = rel.x * rel.x + rel.z * rel.z;
                        double hRadialSq = Math.max(0.0D, hDistSq - hAxial * hAxial);
                        double allowedHoriz = hAxial * CONE_HALF_ANGLE_TAN + 0.55D;
                        double rayCenterY = origin.y + dir.y * (hAxial / horizDir.length());
                        double dyFromRay = (p.getY() + 0.5D) - rayCenterY;
                        inGroundConeSheet = hRadialSq <= allowedHoriz * allowedHoriz && dyFromRay >= -2.85D && dyFromRay <= 1.25D;
                    }
                }
                if (!in3DCone && !inGroundConeSheet) {
                    continue;
                }
            }

            douseAndHydrateSingleBlock(level, p, false, caster);
        }
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
        // Monsoon Cloudburst: scales BOTH circular radius (8.0m at T1 -> 16.0m at T10)
        // AND duration (8.0s / 160t at T1 -> 36.0s / 720t at T10), uncapped dousing/hydration,
        // friendly aquatic creature healing, and continuous drowning on true Nether/Fire mobs!
        final int tier = Math.clamp(spell.getEffectiveTier(), 1, 10);
        final double stormRad = Math.max(radius, 8.0D + (tier - 1) * (8.0D / 9.0D));
        final int totalTicks = tier == 10 ? 720 : 160 + (tier - 1) * 62;

        douseAndHydrateCircularArea(level, impactCenter, stormRad, false, caster);
        // Single uninterrupted full-storm 3D Monsoon Cloudburst canopy, rain sheets, and overhead hydro seal!
        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, stormRad, EssenceType.WATER, null, totalTicks + 14, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 8.5D, 0.0D), stormRad, EssenceType.WATER, null, totalTicks + 14, tier);
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 1.0F, 1.0F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster,
                impactCenter,
                stormRad,
                totalTicks,
                level.getGameTime(),
                spell,
                slot,
                fallbackMateria,
                apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed % 3 == 0) {
                        srv.sendParticles(ParticleTypes.FALLING_WATER,
                                zone.center.x, zone.center.y + 7.5D, zone.center.z,
                                36, zone.radius * 0.56D, 1.2D, zone.radius * 0.56D, 0.0D);
                        srv.sendParticles(ParticleTypes.SPLASH,
                                zone.center.x, zone.center.y + 0.15D, zone.center.z,
                                24, zone.radius * 0.54D, 0.15D, zone.radius * 0.54D, 0.10D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), WATER_CERULEAN_RGB, 24, 0.24F, 0.0F),
                                zone.center.x, zone.center.y + 6.5D, zone.center.z,
                                14, zone.radius * 0.52D, 1.5D, zone.radius * 0.52D, 0.0D
                        );
                    }

                    if (elapsed == 1 || elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        douseAndHydrateCircularArea(srv, zone.center, zone.radius, false, owner);
                        double radSq = zone.radius * zone.radius;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = e.getX() - zone.center.x;
                            double dz = e.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > radSq) continue;

                            e.clearFire();

                            // Heal friendly aquatic pets & non-hostile aquatic creatures (excluding hostile-owned pets & entities targeting allies)
                            if (isFriendlyAquaticPet(e, owner)) {
                                float healAmt = 3.5F + tier * 0.45F;
                                SpellCombatReactionEngine.notifyMagicalHeal(e, healAmt);
                                e.heal(healAmt);
                                e.setAirSupply(e.getMaxAirSupply());
                                srv.sendParticles(ParticleTypes.HEART, e.getX(), e.getY(0.85D), e.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.02D);
                                continue;
                            }

                            if (e == owner || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    e, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }

                            SpellLingeringZoneManager.applySoaked(e, 200);
                            if (isTrueNetherOrFireEntity(e)) {
                                e.hurtServer(srv, srv.damageSources().drown(), 4.0F + tier * 0.9F);
                                srv.sendParticles(ParticleTypes.CLOUD, e.getX(), e.getY(0.7D), e.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.04D);
                            }
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, e, new Vec3(0.0D, -1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                            );
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
        tag.putString("SpellEidolonMateria", EssenceType.WATER.name());
        tag.putInt("SpellEidolonTintRGB", AQUEOUS_EIDOLON_HEX_COLOR);
        // Gentle non-damaging spawn coalescence droplets (no explosive spawn hydro-splash!)
        level.sendParticles(ParticleTypes.SPLASH, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z,
                14, 0.35D, 0.45D, 0.35D, 0.06D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_CERULEAN_RGB, 16, 0.24F, 0.04F),
                spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 8, 0.30D, 0.40D, 0.30D, 0.03D
        );
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Fluid Dissolution: caster dissolves into a high-speed water droplet (2.0m departure splash + 3.0m arrival splash)!
        caster.clearFire();
        boolean wardSafeguard = resolveActiveCasterWardSafeguard(caster);

        // 1. Dissolve at departure spot into collapsing azure droplets (2.0m splash)
        douseAndHydrateCircularArea(level, departurePos, 2.0D, false, caster);
        SpellWorldVfxManager.broadcastArcaneSeal(level, departurePos, 1.6D, EssenceType.WATER, null, 18, potencyTier);
        level.sendParticles(ParticleTypes.SPLASH, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.35D, 0.45D, 0.35D, 0.10D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_AZURE_RGB, 14, 0.26F, 0.05F),
                departurePos.x, departurePos.y + 0.85D, departurePos.z, 12, 0.3D, 0.4D, 0.3D, 0.03D
        );
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.8F, 1.35F);

        // 2. High-speed water droplet streak from departure to arrival
        Vec3 fromMid = departurePos.add(0.0D, 0.85D, 0.0D);
        Vec3 toMid = arrivalPos.add(0.0D, 0.85D, 0.0D);
        if (fromMid.distanceToSqr(toMid) > 0.25D) {
            SpellWorldVfxManager.broadcastChainArc(level, fromMid, toMid, EssenceType.WATER, null, potencyTier);
            Vec3 delta = toMid.subtract(fromMid);
            int samples = Math.min(24, Math.max(6, (int) Math.ceil(delta.length() * 2.0D)));
            for (int i = 0; i <= samples; i++) {
                Vec3 p = fromMid.add(delta.scale((double) i / samples));
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), WATER_CERULEAN_RGB, 12, 0.22F, 0.02F),
                        p.x, p.y, p.z, 2, 0.08D, 0.08D, 0.08D, 0.015D
                );
            }
        }

        // 3. Reform splash at arrival spot (exact 3.0m circular radius, dynamic Ward safeguard & boss knockback guard)
        final double arrivalRad = 3.0D;
        final double arrivalRadSq = arrivalRad * arrivalRad;
        douseAndHydrateCircularArea(level, arrivalPos, arrivalRad, false, caster);
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, arrivalPos.add(0.0D, 0.15D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D), EssenceType.WATER, null, 2.8F, potencyTier
        );
        AABB arrivalBox = new AABB(
                arrivalPos.x - arrivalRad, arrivalPos.y - 1.5D, arrivalPos.z - arrivalRad,
                arrivalPos.x + arrivalRad, arrivalPos.y + 2.6D, arrivalPos.z + arrivalRad
        );
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, arrivalBox, LivingEntity::isAlive)) {
            double dx = nearby.getX() - arrivalPos.x;
            double dz = nearby.getZ() - arrivalPos.z;
            if (dx * dx + dz * dz > arrivalRadSq) continue;
            nearby.clearFire();
            if (nearby == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(nearby, caster, wardSafeguard)) continue;
            SpellLingeringZoneManager.applySoaked(nearby, 180);
            if (!SpellCombatReactionEngine.isBossEntity(level, nearby)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                SpellExecutionEngine.applySyncedMotion(nearby, new Vec3(push.x * 0.55D, 0.22D, push.z * 0.55D));
            }
        }
        level.sendParticles(ParticleTypes.SPLASH, arrivalPos.x, arrivalPos.y + 0.85D, arrivalPos.z, 32, 0.55D, 0.5D, 0.55D, 0.16D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.9F, 1.15F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Aqueous Elutriation: washes dirt/grass into clay, sand into mud, sponge into wet sponge,
        // magma into cobblestone, concrete powder into concrete, and cleans oxidized copper across any namespace!
        if (state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            emitElutriationFeedback(level, pos, false);
            return true;
        }
        if (state.is(BlockTags.SAND)) {
            level.setBlockAndUpdate(pos, Blocks.MUD.defaultBlockState());
            emitElutriationFeedback(level, pos, false);
            return true;
        }
        if (state.is(Blocks.SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.WET_SPONGE.defaultBlockState());
            emitElutriationFeedback(level, pos, false);
            return true;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
            emitElutriationFeedback(level, pos, false);
            return true;
        }

        // Clean unwaxed OR waxed oxidized / weathered / exposed copper family blocks across any namespace!
        BlockState cleanedMetal = cleanOxidizedMetalBlockState(state);
        if (cleanedMetal != null) {
            level.setBlockAndUpdate(pos, cleanedMetal);
            emitElutriationFeedback(level, pos, true);
            return true;
        }

        if (state.getBlock() instanceof ConcretePowderBlock powder) {
            ResourceLocation powderKey = BuiltInRegistries.BLOCK.getKey(powder);
            String path = powderKey.getPath();
            if (path.endsWith("_concrete_powder")) {
                String solidPath = path.substring(0, path.length() - "_powder".length());
                Block solid = resolveBlockAcrossNamespace(powderKey.getNamespace(), solidPath);
                if (solid != null && solid != Blocks.AIR) {
                    level.setBlockAndUpdate(pos, solid.defaultBlockState());
                    emitElutriationFeedback(level, pos, false);
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
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) {
            return false;
        }
        int count = stack.getCount();

        // Dirt / Grass Block / Coarse Dirt / Rooted Dirt -> 4 Clay Balls per block (4 Clay Balls = 1 Clay)
        if (stack.is(Items.DIRT)
                || stack.is(Items.GRASS_BLOCK)
                || stack.is(Items.COARSE_DIRT)
                || stack.is(Items.ROOTED_DIRT)) {
            int totalClayBalls = count * 4;
            itemEntity.setItem(new ItemStack(Items.CLAY_BALL, Math.min(64, totalClayBalls)));
            int rem = totalClayBalls - 64;
            while (rem > 0) {
                int batch = Math.min(64, rem);
                level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                        new ItemStack(Items.CLAY_BALL, batch)));
                rem -= batch;
            }
            level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.06D);
            return true;
        }
        if (stack.is(ItemTags.SAND) || stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.MUD, count));
            level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.06D);
            return true;
        }
        if (stack.is(Items.SPONGE)) {
            itemEntity.setItem(new ItemStack(Items.WET_SPONGE, count));
            level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.06D);
            return true;
        }
        if (stack.is(Items.MAGMA_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.COBBLESTONE, count));
            level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.06D);
            return true;
        }

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String namespace = itemKey.getNamespace();
        String path = itemKey.getPath();

        // Cross-namespace oxidized / weathered / exposed copper item cleaning (same namespace checked first!)
        Item cleanCopperItem = cleanOxidizedMetalItem(stack.getItem(), namespace, path);
        if (cleanCopperItem != null) {
            itemEntity.setItem(new ItemStack(cleanCopperItem, count));
            level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.06D);
            return true;
        }

        if (path.endsWith("_concrete_powder")) {
            String solidPath = path.substring(0, path.length() - "_powder".length());
            Item solidItem = resolveItemAcrossNamespace(namespace, solidPath);
            if (solidItem != null && solidItem != Items.AIR && solidItem != stack.getItem()) {
                itemEntity.setItem(new ItemStack(solidItem, count));
                level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.06D);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Waterlogged Drowning Hex: persistent soaking curse clinging until cleansed via Restoration, fire/heat, or caster defeat;
        // continuously inflicts drowning damage on land, doubles incoming Lightning damage, and triples Frozen freeze rates!
        target.clearFire();
        SpellLingeringZoneManager.applySoaked(target, 50);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), WATER_CERULEAN_RGB, 16, 0.22F, 0.0F),
                target.getX(), target.getY(0.75D), target.getZ(),
                5, 0.28D, 0.35D, 0.28D, 0.0D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BUBBLE.get(), WATER_AZURE_RGB, 14, 0.20F, 0.04F),
                target.getX(), target.getY(0.85D), target.getZ(),
                4, 0.25D, 0.25D, 0.25D, 0.02D
        );

        if (!target.isInWater()) {
            target.setAirSupply(Math.max(-20, target.getAirSupply() - 25));
            float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
            target.hurtServer(level, level.damageSources().drown(), (1.5F + potencyTier * 0.5F) * mult);
            level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.85D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.05D);
        }
        return true;
    }

    // =========================================================================
    // PUBLIC & INTERNAL HELPERS
    // =========================================================================

    /**
     * Strictly identifies genuine Nether, Fire, Magma, Pyre, Soulfire, Cataclysm, or Radiant-Fire creatures
     * (never matching Overworld fire-immune entities such as Wardens, Withers, Ender Dragons, or Fire-Resistance players).
     */
    public static boolean isTrueNetherOrFireEntity(@Nullable LivingEntity e) {
        if (e == null) {
            return false;
        }
        if (e instanceof Blaze
                || e instanceof MagmaCube
                || e instanceof Ghast
                || e instanceof Strider
                || e instanceof Hoglin
                || e instanceof Zoglin
                || e instanceof AbstractPiglin
                || e instanceof ZombifiedPiglin
                || e instanceof WitherSkeleton) {
            return true;
        }

        Set<EssenceType> affinities = SpellCombatReactionEngine.resolveEntityAffinities(e);
        if (affinities.contains(EssenceType.NETHER)
                || affinities.contains(EssenceType.MAGMA)
                || affinities.contains(EssenceType.PYRE)
                || affinities.contains(EssenceType.SOULFIRE)
                || affinities.contains(EssenceType.CATACLYSM)
                || affinities.contains(EssenceType.RADIANT_FIRE)) {
            return true;
        }

        ResourceLocation typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        if (typeKey != null) {
            String path = typeKey.getPath();
            return "brimstone_wyvern".equals(path)
                    || "brimstone_chimera".equals(path)
                    || "soulfire_djinn".equals(path)
                    || "soulfire_phenex".equals(path)
                    || "obsidian_golem".equals(path)
                    || "obsidian_scorpioid".equals(path)
                    || "obsidian_wendigo".equals(path)
                    || "ashen_stalker".equals(path)
                    || "ash_cinder_wisp".equals(path)
                    || "magma_cherufe".equals(path)
                    || "orthrus_magma_beast".equals(path)
                    || "soul_sand_hydra".equals(path);
        }
        return false;
    }

    public static boolean isNetherFireEntity(@Nullable LivingEntity e) {
        return isTrueNetherOrFireEntity(e);
    }

    private static boolean isMeleeWeaponStrike(LivingEntity caster) {
        ItemStack mainHand = caster.getMainHandItem();
        if (mainHand.isEmpty() || mainHand.getItem() instanceof CastingApparatusItem) {
            return false;
        }
        return mainHand.is(ItemTags.SWORDS)
                || mainHand.is(ItemTags.AXES)
                || mainHand.getItem() instanceof TridentItem
                || mainHand.getItem() instanceof MaceItem
                || caster.getAttributeValue(Attributes.ATTACK_DAMAGE) > 1.5D;
    }

    private static boolean isCombatCompatibleTouchSlot(SubSpellSlot slot) {
        SpellCoreEffect primary = slot.primaryEffect().effect();
        return primary != SpellCoreEffect.RESTORATION
                && primary != SpellCoreEffect.DIAGNOSTIC
                && primary != SpellCoreEffect.SONAR
                && primary != SpellCoreEffect.LOGISTICS;
    }

    private static boolean resolveActiveCasterWardSafeguard(@Nullable LivingEntity caster) {
        if (caster == null) {
            return false;
        }
        CompoundTag data = EntityHelper.getPersistentData(caster);
        if (data.contains(SpellLingeringZoneManager.TAG_AIR_BLINK_WARD_SAFE)) {
            return data.getBoolean(SpellLingeringZoneManager.TAG_AIR_BLINK_WARD_SAFE).orElse(false);
        }
        for (ItemStack held : List.of(caster.getMainHandItem(), caster.getOffhandItem())) {
            if (held.isEmpty()) continue;
            CastingApparatusState appState = held.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
            if (appState != null) {
                Optional<SpellAssembly> active = appState.getActiveSpell();
                if (active.isPresent()) {
                    return active.get().hasWardFriendlyFireSafeguard();
                }
            }
            SpellAssembly directSpell = held.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (directSpell != null) {
                return directSpell.hasWardFriendlyFireSafeguard();
            }
        }
        return false;
    }

    private static boolean isGroundedArrow(Projectile proj) {
        return proj instanceof AbstractArrow arrow
                && (arrow.onGround() || arrow.getDeltaMovement().lengthSqr() <= 1.0E-5D);
    }

    private static boolean isFriendlyAquaticPet(LivingEntity entity, @Nullable LivingEntity owner) {
        if (entity instanceof Monster || entity instanceof Enemy) {
            return false;
        }
        if (entity instanceof OwnableEntity ownable) {
            LivingEntity petOwner = ownable.getOwner();
            if (petOwner != null && owner != null && petOwner != owner
                    && !ApexEschatonSingularityHelper.isFriendly(petOwner, owner)
                    && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(petOwner, owner, true)) {
                return false;
            }
        }
        if (owner != null) {
            if (entity instanceof Mob mob) {
                LivingEntity target = mob.getTarget();
                if (target != null
                        && (target == owner
                                || ApexEschatonSingularityHelper.isFriendly(target, owner)
                                || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, owner, true))) {
                    return false;
                }
            }
            if (entity instanceof NeutralMob neutral && owner.getUUID().equals(neutral.getPersistentAngerTarget())) {
                return false;
            }
        }
        return entity instanceof Axolotl
                || entity instanceof Turtle
                || entity instanceof Dolphin
                || entity instanceof Tadpole
                || entity instanceof WaterAnimal;
    }

    private static boolean isSurfaceWeed(BlockState bs) {
        return bs.is(Blocks.SHORT_GRASS)
                || bs.is(Blocks.TALL_GRASS)
                || bs.is(Blocks.FERN)
                || bs.is(Blocks.LARGE_FERN)
                || bs.is(Blocks.DEAD_BUSH)
                || bs.is(Blocks.CRIMSON_ROOTS)
                || bs.is(Blocks.WARPED_ROOTS)
                || bs.is(Blocks.NETHER_SPROUTS)
                || bs.is(BlockTags.FLOWERS)
                || bs.is(Blocks.BROWN_MUSHROOM)
                || bs.is(Blocks.RED_MUSHROOM)
                || bs.is(Blocks.CRIMSON_FUNGUS)
                || bs.is(Blocks.WARPED_FUNGUS)
                || bs.is(Blocks.VINE)
                || bs.is(Blocks.CAVE_VINES)
                || bs.is(Blocks.CAVE_VINES_PLANT)
                || bs.is(Blocks.WEEPING_VINES)
                || bs.is(Blocks.WEEPING_VINES_PLANT)
                || bs.is(Blocks.TWISTING_VINES)
                || bs.is(Blocks.TWISTING_VINES_PLANT)
                || bs.is(Blocks.MOSS_CARPET)
                || bs.is(Blocks.SWEET_BERRY_BUSH);
    }

    @Nullable
    private static BlockState cleanOxidizedMetalBlockState(BlockState state) {
        var prev = WeatheringCopper.getPrevious(state);
        if (prev.isPresent()) {
            BlockState cur = prev.get();
            while (WeatheringCopper.getPrevious(cur).isPresent()) {
                cur = WeatheringCopper.getPrevious(cur).get();
            }
            return cur;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String namespace = key.getNamespace();
        for (String cleanPath : stripOxidationPrefixCandidates(key.getPath())) {
            Block cleanBlock = resolveBlockAcrossNamespace(namespace, cleanPath);
            if (cleanBlock != null && cleanBlock != Blocks.AIR && cleanBlock != state.getBlock()) {
                return cleanBlock.withPropertiesOf(state);
            }
        }
        return null;
    }

    @Nullable
    private static Item cleanOxidizedMetalItem(Item original, String namespace, String path) {
        for (String cleanPath : stripOxidationPrefixCandidates(path)) {
            Item cleanItem = resolveItemAcrossNamespace(namespace, cleanPath);
            if (cleanItem != null && cleanItem != Items.AIR && cleanItem != original) {
                return cleanItem;
            }
        }
        return null;
    }

    @Nullable
    private static Block resolveBlockAcrossNamespace(String namespace, String path) {
        Optional<Block> sameNamespace = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (sameNamespace.isPresent() && sameNamespace.get() != Blocks.AIR) {
            return sameNamespace.get();
        }
        if (!ResourceLocation.DEFAULT_NAMESPACE.equals(namespace)) {
            Optional<Block> fallback = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.withDefaultNamespace(path));
            if (fallback.isPresent() && fallback.get() != Blocks.AIR) {
                return fallback.get();
            }
        }
        return null;
    }

    @Nullable
    private static Item resolveItemAcrossNamespace(String namespace, String path) {
        Optional<Item> sameNamespace = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (sameNamespace.isPresent() && sameNamespace.get() != Items.AIR) {
            return sameNamespace.get();
        }
        if (!ResourceLocation.DEFAULT_NAMESPACE.equals(namespace)) {
            Optional<Item> fallback = BuiltInRegistries.ITEM.getOptional(ResourceLocation.withDefaultNamespace(path));
            if (fallback.isPresent() && fallback.get() != Items.AIR) {
                return fallback.get();
            }
        }
        return null;
    }

    private static List<String> stripOxidationPrefixCandidates(String path) {
        boolean waxed = path.startsWith("waxed_");
        String core = waxed ? path.substring("waxed_".length()) : path;
        String stripped = null;
        if (core.startsWith("oxidized_")) {
            stripped = core.substring("oxidized_".length());
        } else if (core.startsWith("weathered_")) {
            stripped = core.substring("weathered_".length());
        } else if (core.startsWith("exposed_")) {
            stripped = core.substring("exposed_".length());
        } else if (core.startsWith("rusted_")) {
            stripped = core.substring("rusted_".length());
        }
        if (stripped == null || stripped.isEmpty()) {
            return List.of();
        }
        List<String> candidates = new ArrayList<>(2);
        if ("copper".equals(stripped)) {
            candidates.add(waxed ? "waxed_copper_block" : "copper_block");
            candidates.add(waxed ? "waxed_copper" : "copper");
        } else {
            candidates.add(waxed ? ("waxed_" + stripped) : stripped);
        }
        return candidates;
    }

    private static void emitElutriationFeedback(ServerLevel level, BlockPos pos, boolean scrapedMetal) {
        level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                14, 0.35D, 0.25D, 0.35D, 0.08D);
        if (scrapedMetal) {
            level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 0.85F, 1.15F);
        } else {
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.65F, 1.25F);
        }
    }

    private static void douseAndHydrateCircularArea(
            ServerLevel level,
            Vec3 center,
            double rad,
            boolean clearCobwebsAndWeeds,
            @Nullable LivingEntity caster
    ) {
        int r = (int) Math.ceil(rad);
        double radSq = rad * rad;
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-r, -1, -r), cPos.offset(r, 2, r))) {
            double dx = (p.getX() + 0.5D) - center.x;
            double dz = (p.getZ() + 0.5D) - center.z;
            if (dx * dx + dz * dz > radSq) continue;
            douseAndHydrateSingleBlock(level, p, clearCobwebsAndWeeds, caster);
        }
    }

    private static void douseAndHydrateArea(
            ServerLevel level,
            BlockPos center,
            int rad,
            boolean clearCobwebsAndWeeds,
            @Nullable LivingEntity caster
    ) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            douseAndHydrateSingleBlock(level, p, clearCobwebsAndWeeds, caster);
        }
    }

    private static void douseAndHydrateSingleBlock(
            ServerLevel level,
            BlockPos p,
            boolean clearCobwebsAndWeeds,
            @Nullable LivingEntity caster
    ) {
        BlockState bs = level.getBlockState(p);
        if (bs.isAir()) {
            return;
        }
        if (bs.getBlock() instanceof BaseFireBlock) {
            level.removeBlock(p, false);
        } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
            CampfireBlock.dowse(caster, level, p, bs);
            level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
        } else if (bs.getBlock() instanceof AbstractCandleBlock && AbstractCandleBlock.isLit(bs)) {
            AbstractCandleBlock.extinguish(null, bs, level, p);
        } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
            level.setBlockAndUpdate(p, bs.setValue(FarmBlock.MOISTURE, 7));
        } else if (clearCobwebsAndWeeds
                && (bs.is(Blocks.COBWEB) || isSurfaceWeed(bs))
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
            level.destroyBlock(p, true, caster);
        }
    }
}
