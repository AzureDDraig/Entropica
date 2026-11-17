package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Lightning Materia (Base Type #12)</b>
 * (Spellcrafting System Guide Sections 1.2B, 1.7, 2.2, 5.6 &amp; 6.13).
 *
 * <p>Canonical Advantage: <b>Strong against EARTH (+1) and WATER (+1)</b> |
 * <b>Weak against VOID (-1) and WATER (-1)</b> (Mutually volatile with water bodies;
 * wet targets suffer {@code +100%} Conductive Overcharge bonus damage!).</p>
 */
public final class LightningSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VOLT_SPARK_HEX_COLOR = 0xFFE600;
    public static final int VOLT_PLASMA_CYAN_HEX = 0xB8F4FF;

    private static final String TAG_REPEATING_PULSES_LEFT = "SpellLightningGlyphPulsesLeft";
    private static final String TAG_REPEATING_COOLDOWN_UNTIL = "SpellLightningGlyphCooldownUntil";

    @Override
    public EssenceType materiaType() {
        return EssenceType.LIGHTNING;
    }

    // =========================================================================
    // CANONICAL TITLES & DESCRIPTIONS (GUIDE SECTION 6.13)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Supercharged Forked Chains";
            default -> "Galvanic Voltaic " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Leaping chains jump with near-instant speed and fork into an additional secondary branch at each leap (1 -> 2 -> 4 targets), with jump distance boosted by +50% across wet mobs or water surfaces.";
            default -> "Supercharges spell flow with crackling #FFE600 high-voltage plasma arcs and accelerated electrical velocity.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Plasma Bolt (Ball Lightning)";
            case BEAM -> "Continuous Tesla Arc";
            case RADIAL_BURST -> "Static EMP Nova";
            case TOUCH_STRIKE -> "Overcharge Discharge Strike";
            case AURA -> "Static Shroud (Living Lightning Rod)";
            case GLYPH -> "Tesla Arc Mine";
            case CONE -> "Forked Arc Fan (Tesla Shotgun)";
            case SKYFALL -> "Thunderstorm Skyfall (Bolt Barrage)";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Fires a high-velocity sphere of crackling electricity (#FFE600) with near-instant flight speed; stuns the target for 1.0s and discharges bonus shock sparks to wet or metal-armored mobs within 3 blocks.";
            case BEAM -> "Channels a sustained, blinding electric beam (#FFE600) that delivers rapid shock ticks to stagger movement and conducts rapidly through water pools and wet entities along its line.";
            case RADIAL_BURST -> "Detonates in a brilliant 360-degree electric burst (6-block radius) that shocks all nearby mobs, locks their movement in place for 1.5s, and pulses adjacent redstone components at level 15.";
            case TOUCH_STRIKE -> "Melee strike shocks the target with high-voltage plasma, stunning them for 1.0s, arcing secondary sparks within 3 blocks, and granting the caster a +45% attack speed boost for 15s.";
            case AURA -> "Surrounds the caster in crackling electricity (#FFE600); grants +30% sprint speed, retaliates against melee attackers with a 0.5s shock stun, and safely absorbs vanilla lightning strikes into 100% full Materia fuel.";
            case GLYPH -> "Surface-snapped electric trap (1.0s arming, 50% Sneak-Right-Click disarm refund) that shocks and paralyzes the triggering mob + up to 3 nearby foes within 4 blocks for 2.0s while powering adjacent redstone wire for 4.0s.";
            case CONE -> "Unleashes a wide 60-degree spread of branching electrical arcs; shocks all enemies in the cone with a 1.0s stun, and conducts across water pools or during rain to extend its cone reach by +50%.";
            case SKYFALL -> "Summons a crackling stormcloud overhead that repeatedly strikes enemies across a 12-block zone with violent lightning bolts over 15s, staggering mobs and electrifying water pools.";
            case SUMMON_AGENT -> "Summons an electric gold-yellow (#FFE600) Volt-Spark Agent that slashes with plasma arc-blades and leaves an electrified floor-spark trail behind as it runs.";
            case SUMMON_SENTRY -> "Anchors a high-voltage Tesla Coil Volt-Spark Sentry (#FFE600) that fires rapid homing Ball-Lightning orbs.";
            case SUMMON_DECOY -> "Conjures a crackling Volt-Spark Decoy (#FFE600) that taunts hostiles and live-shocks any entity that attempts to pass through or touch it.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Thunderstep";
            case TRANSMUTE, BREAK -> "Galvanic Electrolysis & Smelting";
            case AFFLICTION -> "Galvanic Overcharge Hex";
            case KINETIC -> "Magnetic Railgun";
            default -> "Voltaic " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Instantly teleports the caster with a thunderclap, leaving a persistent electrified arc line between departure and arrival that shocks entities crossing it.";
            case TRANSMUTE, BREAK -> "Auto-smelts metal ores & items, manages copper oxidation/wax states, fuses sand into fulgurite glass, and remotely recharges placed Materia battery cells & redstone machinery.";
            case AFFLICTION -> "Persistent milk-immune electrical seizure curse (cleansed by Earth/Grounding magic, Restoration, or caster defeat): cuts attack and movement speed by 40%, disables shield blocking, and periodically arcs shock sparks to adjacent allies.";
            case KINETIC -> "Acts as an electromagnetic railgun, flinging metallic-armored entities with extreme velocity or launching the caster horizontally forward.";
            default -> "Infuses the spell effect with high-voltage Lightning Materia (+1 vs Earth & Water, +100% Conductive Overcharge vs wet targets, -1 vs Void).";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — SUPERCHARGED FORKED CHAINS (1 -> 2 -> 4)
    // =========================================================================

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.13 Supercharged Forked Chains:
        // SpellExecutionEngine.chainLeapFrom executes the exact 1 -> 2 -> 4 exponential forks
        // (+50% 18m jump range across wet mobs or water surfaces via hasWaterAlongArc).
        // Each leap applies a crisp electric micro-stagger and electrifies any water pool crossed!
        int tier = spell.getEffectiveTier();
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            applyParalysisStun(toTarget, 10);
        }
        if (toTarget.isInWater()) {
            electrifyConnectedWaterPool(
                    level, caster, toTarget.blockPosition(),
                    2.0F + tier * 0.45F, spell.hasWardFriendlyFireSafeguard()
            );
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                toTarget.getX(), toTarget.getY(0.7D), toTarget.getZ(),
                16, 0.28D, 0.38D, 0.28D, 0.12D);
        level.playSound(null, toTarget.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.45F, 1.75F);
    }

    /**
     * Returns {@code true} if the chain arc between {@code from} and {@code to} passes over or through
     * a water surface block (granting the {@code +50%} jump-range boost in {@code SpellExecutionEngine.chainLeapFrom}).
     */
    public static boolean hasWaterAlongArc(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 span = to.subtract(from);
        double len = span.length();
        if (len < 0.5D) return false;
        int samples = Math.clamp((int) Math.ceil(len / 1.5D), 2, 14);
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            Vec3 pt = from.add(span.scale(t));
            BlockPos bp = BlockPos.containing(pt);
            for (int dy = -2; dy <= 1; dy++) {
                BlockState bs = level.getBlockState(bp.offset(0, dy, 0));
                if (bs.is(Blocks.WATER) || bs.is(Blocks.BUBBLE_COLUMN)
                        || (bs.hasProperty(BlockStateProperties.WATERLOGGED) && bs.getValue(BlockStateProperties.WATERLOGGED))) {
                    return true;
                }
            }
        }
        return false;
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 LIGHTNING FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.13 Plasma Bolt (Ball Lightning): zero gravity drop + near-instant flight speed!
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        double speed = vel.length();
        if (speed > 1.0E-4D) {
            double boosted = Math.min(3.55D, Math.max(2.65D, speed * 1.85D));
            projectile.setDeltaMovement(vel.scale(boosted / speed));
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                projectile.getX(), projectile.getY(), projectile.getZ(),
                10, 0.16D, 0.16D, 0.16D, 0.10D);
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.55F, 1.80F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                projectile.getX(), projectile.getY(), projectile.getZ(),
                4, 0.12D, 0.12D, 0.12D, 0.08D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), VOLT_SPARK_HEX_COLOR, 14, 0.78F, 0.12F),
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
        // Section 6.13 Plasma Bolt (Ball Lightning):
        // Stuns primary target for 1.0s (20t) and discharges bonus shock sparks to any wet or iron/metal-armored mobs within 3 blocks!
        int tier = spell.getEffectiveTier();
        Vec3 impactPos = hitEntity.getBoundingBox().getCenter();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.LIGHTNING, null, 1.20F, tier
        );

        if (hitEntity instanceof LivingEntity primary && primary.isAlive()) {
            boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
            if (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(primary, caster, wardSafe)) {
                applyParalysisStun(primary, 20);
                if (!spell.prefix().enforcesZeroEntityHarm() && !slotExecutesHarmVerb(slot)) {
                    float boltDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * 0.50F * spell.flawProfile().getPotencyMultiplier();
                    SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, primary, caster, boltDmg, false, true);
                }
            }
            arcSecondarySparks(
                    level, caster, primary, 3.0D,
                    (2.8F + tier * 0.65F) * spell.flawProfile().getPotencyMultiplier(),
                    true, wardSafe, tier
            );
        }

        alertHostilesToLightningClap(level, caster, impactPos, spell);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, impactPos.x, impactPos.y, impactPos.z,
                26, 0.35D, 0.35D, 0.35D, 0.14D);
        level.playSound(null, BlockPos.containing(impactPos), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.70F, 1.60F);
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
        BlockPos hitPos = blockHit.getBlockPos();
        Vec3 loc = blockHit.getLocation();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());

        SpellWorldVfxManager.broadcastProjectileImpact(level, loc, normal, EssenceType.LIGHTNING, null, 1.05F, tier);
        tryMachineRecharging(level, hitPos, EssenceType.LIGHTNING, tier);
        electrifyConnectedWaterPool(
                level, caster, hitPos,
                2.5F + tier * 0.60F, spell.hasWardFriendlyFireSafeguard()
        );

        // Also discharge bonus sparks to wet or metal-armored mobs within 3 blocks of block impact!
        if (!spell.prefix().enforcesZeroEntityHarm()) {
            float sparkDmg = (2.4F + tier * 0.55F) * spell.flawProfile().getPotencyMultiplier();
            for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, new AABB(loc, loc).inflate(3.0D), LivingEntity::isAlive)) {
                if (other == caster || other.position().distanceTo(loc) > 3.2D) continue;
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(other, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                if (SpellLingeringZoneManager.isEntityWetOrSoaked(other) || isWearingOrHoldingMetalEquipment(other)) {
                    SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, other, caster, sparkDmg, false, true);
                    applyParalysisStun(other, 14);
                    SpellWorldVfxManager.broadcastChainArc(
                            level, loc, other.getBoundingBox().getCenter(), EssenceType.LIGHTNING, null, tier
                    );
                }
            }
        }

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, loc.x, loc.y + 0.1D, loc.z, 20, 0.28D, 0.25D, 0.28D, 0.12D);
        level.playSound(null, hitPos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.60F, 1.65F);
    }

    // =========================================================================
    // FORM MUTATION 2 — CONTINUOUS TESLA ARC (BEAM)
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
        // Section 6.13 Continuous Tesla Arc (#FFE600):
        // Sustained, blinding electric beam delivering rapid shock ticks that stagger target movement
        // and rapidly conducting through water pools or wet entities along its line!
        int tier = spell.getEffectiveTier();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float arcDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.25F, apparatusPotency) * 0.65F * flawPot;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 muzzle = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D).add(normDir.scale(0.35D));
        Vec3 endPos = blockHit.getType() == HitResult.Type.BLOCK
                ? blockHit.getLocation()
                : muzzle.add(normDir.scale(maxReach));

        SpellWorldVfxManager.broadcastChanneledBeamSlice(
                level, muzzle, endPos, EssenceType.LIGHTNING, null, tier, 8
        );

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            tryMachineRecharging(level, blockHit.getBlockPos(), EssenceType.LIGHTNING, tier);
        }

        Set<UUID> seen = new HashSet<>();
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || !seen.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            // Rapid electric stagger + brief blinding arc glare while channeled!
            applyParalysisStun(hit, 14);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 26, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                Vec3 v = hit.getDeltaMovement();
                hit.setDeltaMovement(v.x * 0.18D, Math.min(0.08D, v.y), v.z * 0.18D);
                hit.hasImpulse = true;
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, hit, caster, arcDmg, false, false);
            }
            if (SpellLingeringZoneManager.isEntityWetOrSoaked(hit)) {
                arcSecondarySparks(level, caster, hit, 5.0D, (2.2F + tier * 0.55F) * flawPot, false, wardSafe, tier);
            }
        }

        // Rapidly conduct through any water pools intersected along the beam line!
        double beamLen = Math.min(maxReach, muzzle.distanceTo(endPos) + 0.5D);
        boolean conductedPool = false;
        for (double d = 1.0D; d <= Math.min(beamLen, 24.0D); d += 1.0D) {
            Vec3 pt = muzzle.add(normDir.scale(d));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y, pt.z, 2, 0.08D, 0.08D, 0.08D, 0.05D);
            if (!conductedPool) {
                BlockPos bp = BlockPos.containing(pt);
                if (level.getBlockState(bp).is(Blocks.WATER) || level.getBlockState(bp.below()).is(Blocks.WATER)) {
                    conductedPool = true;
                    electrifyConnectedWaterPool(level, caster, bp, (2.4F + tier * 0.55F) * flawPot, wardSafe);
                }
            }
        }
        if (level.getGameTime() % 6L == 0L) {
            level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.40F, 1.85F);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — STATIC EMP NOVA (RADIAL BURST)
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
        // Section 6.13 Static EMP Nova:
        // Brilliant 360-degree electric burst (6-block radius) shocking all nearby mobs, locking their movement
        // in place for 1.5s (30t), and delivering a momentary level-15 pulse to adjacent redstone components!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean hasHarm = slotExecutesHarmVerb(slot);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float empDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.60F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.LIGHTNING, null, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == caster || e.position().distanceTo(center) > r + 0.5D) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, wardSafe)) continue;

            // Lock movement in place for 1.5 seconds (30t)!
            applyParalysisStun(e, 30);
            e.addEffect(new MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 30, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, e)) {
                e.setDeltaMovement(0.0D, Math.min(0.0D, e.getDeltaMovement().y), 0.0D);
                e.hasImpulse = true;
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, e, caster, empDmg, false, true);
            }
        }

        // Momentary (1.0s = 20t) level-15 EMP pulse to adjacent redstone components & Materia batteries!
        BlockPos centerPos = BlockPos.containing(center);
        maintainRedstonePowerZone(level, caster, centerPos, 6, 20);
        for (BlockPos p : BlockPos.betweenClosed(centerPos.offset(-4, -1, -4), centerPos.offset(4, 2, 4))) {
            SpellFuelService.chargePlacedMateriaBatteryOrMachine(level, p, EssenceType.LIGHTNING, tier);
        }

        alertHostilesToLightningClap(level, caster, center, spell);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.85D, center.z,
                56, r * 0.45D, 0.55D, r * 0.45D, 0.16D);
        level.playSound(null, centerPos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.90F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 4 — OVERCHARGE DISCHARGE STRIKE (TOUCH / STRIKE)
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
        // Section 6.13 Overcharge Discharge Strike:
        // Shocks target with high-voltage plasma, stuns for 1.0s (20t), arcs secondary sparks to any other foe
        // within 3 blocks, and grants the caster an exact +45% attack speed boost for 15 seconds (300t)!
        int tier = spell.getEffectiveTier();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float strikeDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, apparatusPotency) * flawPot;
        Vec3 slashDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D), slashDir, 2.85D, EssenceType.LIGHTNING, null, tier
        );

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }
            if (hit.isAlive()) {
                applyParalysisStun(hit, 20);
            }
            arcSecondarySparks(
                    level, caster, hit, 3.0D,
                    (2.8F + tier * 0.65F) * flawPot,
                    false, wardSafe, tier
            );
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hit.getX(), hit.getY(0.75D), hit.getZ(),
                    18, 0.30D, 0.40D, 0.30D, 0.14D);
        }

        // Exact +45% attack speed boost for 15.0 seconds (300 ticks)!
        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(caster, level, 0.45D, 300L);
        caster.addEffect(new MobEffectInstance(MobEffects.HASTE, 300, 1, false, true));
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.70F, 1.55F);
    }

    // =========================================================================
    // FORM MUTATION 5 — STATIC SHROUD / LIVING LIGHTNING ROD (AURA / SELF)
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
        // Section 6.13 Static Shroud (Living Lightning Rod):
        // - Surrounds caster in crackling #FFE600 electricity
        // - Grants +30% sprint speed (tickLightningStaticShroud in SpellLingeringZoneManager)
        // - Retaliates against melee attackers with a 0.5s (10t) shock stun (SpellCombatReactionEngine)
        // - Safely absorbs vanilla lightning strikes into 100% full Orbis Cell Materia fuel restoration!
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        long now = level.getGameTime();

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 0.0D, 0.0D, 0.30D, 25L);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.80F, null);
        SpellWorldVfxManager.broadcastWardShell(
                level, caster.position(), Math.max(1.35D, caster.getBbWidth() * 1.35D),
                EssenceType.LIGHTNING, null, Math.min(dur, 120), tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, caster.position().add(0.0D, 0.04D, 0.0D), 2.6D, EssenceType.LIGHTNING, null, 52, tier
        );

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX(), caster.getY(0.8D), caster.getZ(),
                32, 0.48D, 0.65D, 0.48D, 0.14D);
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.65F, 1.65F);
    }

    // =========================================================================
    // FORM MUTATION 6 — TESLA ARC MINE (GLYPH / TRAP)
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
        // Section 4.6 & 6.13 Tesla Arc Mine:
        // - Surface-snapped electric trap (1.0s arming, 10m or Tier 5+ indefinite lifetime, 50% Sneak-Right-Click disarm refund)
        // - Supports Single-Use Detonator (100%) and Repeating Glyphic Sentry (3 pulses @ 60%, 2.0s re-arm)
        // - Shocks & paralyzes triggering mob + up to 3 nearby enemies (4 total) within 4 blocks for 2.0s (40t)
        // - Powers adjacent redstone wire & components at level 15 for 4.0s (80t)!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceBlock = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(glyphCenter.x, surfaceBlock.getY() + 1.02D, glyphCenter.z);
        double padRad = Math.max(2.2D, Math.min(3.5D, radius));

        String subMode = slot.primaryEffect().subMode();
        boolean repeatingSentry = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        CompoundTag casterTag = EntityHelper.getPersistentData(caster);
        String pulseKey = TAG_REPEATING_PULSES_LEFT + "_" + level.getGameTime();
        String cdKey = TAG_REPEATING_COOLDOWN_UNTIL + "_" + level.getGameTime();
        if (repeatingSentry) {
            casterTag.putInt(pulseKey, 3);
            casterTag.putLong(cdKey, 0L);
        }

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, 2.2D, EssenceType.LIGHTNING, null, 52, tier);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), VOLT_SPARK_HEX_COLOR, 32, 0.90F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 5, 0.45D, 0.05D, 0.45D, 0.005D
        );
        level.playSound(null, BlockPos.containing(snappedCenter), SoundEvents.COPPER_PLACE, SoundSource.PLAYERS, 0.80F, 1.20F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, 4.0D, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 22 == 0) {
                        srv.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                zone.center.x, zone.center.y + 0.08D, zone.center.z, 3, 0.40D, 0.05D, 0.40D, 0.03D);
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
                    int stunTicks = Math.max(24, Math.round(40.0F * pulseScale)); // Canonical 2.0s (40t) on Single-Use!
                    boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    float mineDmg = Math.max(3.6F + tier * 1.10F,
                            MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, zone.apparatusPotency) * 0.72F)
                            * flawPot * pulseScale;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 4.0D, EssenceType.LIGHTNING, null, tier);

                    // Sort victims by distance to mine center so the triggering mob + up to 3 closest foes (4 total) are struck!
                    List<LivingEntity> victims = srv.getEntitiesOfClass(
                            LivingEntity.class,
                            new AABB(zone.center.x - 4.0D, zone.center.y - 1.8D, zone.center.z - 4.0D,
                                    zone.center.x + 4.0D, zone.center.y + 3.0D, zone.center.z + 4.0D),
                            e -> e.isAlive()
                                    && e != owner
                                    && e.position().distanceTo(zone.center) <= 4.35D
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    victims.sort(Comparator.comparingDouble(e -> e.position().distanceToSqr(zone.center)));

                    int shocked = 0;
                    for (LivingEntity v : victims) {
                        applyParalysisStun(v, stunTicks);
                        SpellWorldVfxManager.broadcastChainArc(
                                srv, zone.center.add(0.0D, 0.25D, 0.0D), v.getBoundingBox().getCenter(),
                                EssenceType.LIGHTNING, null, tier
                        );
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * pulseScale
                            );
                        }
                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                            SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(srv, v, owner, mineDmg, false, true);
                        }
                        if (++shocked >= 4) break;
                    }

                    // Power adjacent redstone wire & components for 4.0 seconds (80t)!
                    maintainRedstonePowerZone(srv, owner, BlockPos.containing(zone.center), 4, 80);
                    srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, zone.center.x, zone.center.y + 0.55D, zone.center.z,
                            44, 1.25D, 0.55D, 1.25D, 0.16D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.45F);

                    if (repeatingSentry && owner != null) {
                        CompoundTag oTag = EntityHelper.getPersistentData(owner);
                        int rem = oTag.getInt(pulseKey).orElse(1) - 1;
                        if (rem > 0) {
                            oTag.putInt(pulseKey, rem);
                            oTag.putLong(cdKey, now + 40L); // 2.0s re-arm delay
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
    // FORM MUTATION 7 — FORKED ARC FAN / TESLA SHOTGUN (CONE / SPRAY)
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
        // Section 6.13 Forked Arc Fan (Tesla Shotgun):
        // Wide 60-degree spread of branching electrical arcs shocking all enemies in the cone with a 1.0s (20t) stun,
        // and conducting across water pools or during rain to extend its cone reach by +50%!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D);
        double baseRange = Math.max(5.5D, coneRange);
        double cosHalfCone = 0.866D; // 60-degree total cone (+/- 30 deg)
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);

        boolean conductiveExtension = level.isRaining() || caster.isInWaterOrRain();
        if (!conductiveExtension) {
            // Also check if any water pool or wet entity lies inside the base cone!
            for (LivingEntity hit : hitTargets) {
                if (SpellLingeringZoneManager.isEntityWetOrSoaked(hit)) {
                    conductiveExtension = true;
                    break;
                }
            }
        }
        if (!conductiveExtension) {
            for (double d = 1.0D; d <= baseRange; d += 1.25D) {
                BlockPos probe = BlockPos.containing(eye.add(normDir.scale(d)));
                if (level.getBlockState(probe).is(Blocks.WATER) || level.getBlockState(probe.below()).is(Blocks.WATER)) {
                    conductiveExtension = true;
                    break;
                }
            }
        }

        double effectiveRange = conductiveExtension ? baseRange * 1.50D : baseRange;
        SpellWorldVfxManager.broadcastVortexCone(level, eye, normDir, effectiveRange, EssenceType.LIGHTNING, null, tier);

        float shotgunDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.65F * spell.flawProfile().getPotencyMultiplier();

        Set<UUID> shocked = new HashSet<>();
        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || hit == caster || !shocked.add(hit.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            applyParalysisStun(hit, 20);
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, hit, caster, shotgunDmg, false, true);
            }
        }

        // Sweep full 60-degree (and +50% extended) cone so extra targets in extended range receive sub-spell + 1.0s stun!
        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(effectiveRange)).inflate(effectiveRange * 0.65D);
        for (LivingEntity extra : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (extra == caster || !shocked.add(extra.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(extra, caster, wardSafe)) continue;
            Vec3 toExtra = extra.getBoundingBox().getCenter().subtract(eye);
            double dist = toExtra.length();
            if (dist > effectiveRange + 0.5D || dist < 1.0E-4D || toExtra.scale(1.0D / dist).dot(normDir) < cosHalfCone) {
                continue;
            }
            applyParalysisStun(extra, 20);
            SpellExecutionEngine.deliverSubSpellOnEntity(
                    level, caster, extra, normDir, spell, slot, fallbackMateria, apparatusPotency * 0.85F
            );
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm && extra.isAlive()) {
                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, extra, caster, shotgunDmg * 0.85F, false, true);
            }
        }

        // Conduct into water pools inside the cone!
        for (double d = 1.5D; d <= Math.min(effectiveRange, 14.0D); d += 2.0D) {
            BlockPos bp = BlockPos.containing(eye.add(normDir.scale(d)));
            if (level.getBlockState(bp).is(Blocks.WATER) || level.getBlockState(bp.below()).is(Blocks.WATER)) {
                electrifyConnectedWaterPool(level, caster, bp, 2.4F + tier * 0.55F, wardSafe);
                break;
            }
        }

        alertHostilesToLightningClap(level, caster, caster.position(), spell);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                eye.x + normDir.x * 2.5D, eye.y + normDir.y * 2.5D, eye.z + normDir.z * 2.5D,
                40, 0.90D, 0.45D, 0.90D, 0.18D);
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.80F, 1.55F);
    }

    // =========================================================================
    // FORM MUTATION 8 — THUNDERSTORM SKYFALL / BOLT BARRAGE (RAIN / SKYFALL)
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
        // Section 6.13 Thunderstorm Skyfall (Bolt Barrage):
        // Summons a crackling stormcloud overhead that repeatedly strikes random enemies across a 12-block zone
        // with violent lightning bolts over 15 seconds (300t), staggering mobs and electrifying water pools!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        int stormDuration = 300; // Canonical 15.0s (300t)!

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.LIGHTNING, null, stormDuration, tier);
        alertHostilesToLightningClap(level, caster, impactCenter, spell);
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.90F, 1.15F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, stormDuration, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    BlockPos floorPos = BlockPos.containing(zone.center);
                    double ceilingH = probeOverheadClearance(srv, floorPos, 10);

                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD,
                                zone.center.x, zone.center.y + ceilingH - 0.5D, zone.center.z,
                                10, zone.radius * 0.50D, 0.35D, zone.radius * 0.50D, 0.02D);
                        srv.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                zone.center.x, zone.center.y + ceilingH - 0.9D, zone.center.z,
                                14, zone.radius * 0.50D, 0.55D, zone.radius * 0.50D, 0.10D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                        float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                        float boltDmg = Math.max(2.8F + tier * 0.75F,
                                MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.35F, zone.apparatusPotency) * 0.55F * flawPot);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + ceilingH + 0.5D, zone.center.z + zone.radius
                        );
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        List<LivingEntity> candidates = srv.getEntitiesOfClass(LivingEntity.class, area, e -> {
                            if (!e.isAlive() || e == owner) return false;
                            double dx = e.getX() - zone.center.x;
                            double dz = e.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > rSq) return false;
                            return owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe);
                        });

                        Vec3 boltPos;
                        LivingEntity victim = null;
                        if (!candidates.isEmpty()) {
                            victim = candidates.get(srv.random.nextInt(candidates.size()));
                            boltPos = victim.position();
                        } else {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * zone.radius;
                            boltPos = zone.center.add(Math.cos(ang) * dist, 0.0D, Math.sin(ang) * dist);
                        }

                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(srv, EntitySpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.setPos(boltPos.x, boltPos.y, boltPos.z);
                            bolt.setVisualOnly(true);
                            EntityHelper.getPersistentData(bolt).putBoolean("SpellStaticShroudAbsorbed", true);
                            srv.addFreshEntity(bolt);
                        }
                        SpellWorldVfxManager.broadcastChainArc(
                                srv, new Vec3(boltPos.x, zone.center.y + ceilingH - 0.3D, boltPos.z),
                                boltPos.add(0.0D, 0.35D, 0.0D), EssenceType.LIGHTNING, null, tier
                        );

                        if (victim != null) {
                            applyParalysisStun(victim, 20);
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, victim, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.65F
                                );
                            }
                            if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && victim.isAlive()) {
                                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(srv, victim, owner, boltDmg, false, true);
                            }
                            if (SpellLingeringZoneManager.isEntityWetOrSoaked(victim)) {
                                arcSecondarySparks(srv, owner, victim, 4.5D, boltDmg * 0.60F, false, wardSafe, tier);
                            }
                        }

                        // Electrify connected water pools & recharge placed batteries struck by the bolt!
                        BlockPos strikeBp = BlockPos.containing(boltPos);
                        tryMachineRecharging(srv, strikeBp, EssenceType.LIGHTNING, tier);
                        electrifyConnectedWaterPool(srv, owner, strikeBp, 3.2F + tier * 0.65F, wardSafe);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — VOLT-SPARK CONSTRUCT SUITE (SUMMON TRIAD)
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
        tag.putString("SpellEidolonMateria", EssenceType.LIGHTNING.name());
        tag.putInt("SpellEidolonTintRGB", VOLT_SPARK_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(level, spawnPos, 1.35D, EssenceType.LIGHTNING, null, tier);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawnPos.x, spawnPos.y + 0.85D, spawnPos.z,
                32, 0.48D, 0.65D, 0.48D, 0.15D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.65F, 1.55F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — THUNDERSTEP, ELECTROLYSIS, HEX, RAILGUN
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.13 Thunderstep:
        // Instantly teleports the caster with a thunderclap, leaving a persistent electrified arc line
        // between departure and arrival that shocks entities crossing it!
        int tier = Math.max(1, potencyTier);
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.LIGHTNING, tier);
        SpellWorldVfxManager.broadcastChainArc(
                level, departurePos.add(0.0D, 0.9D, 0.0D), arrivalPos.add(0.0D, 0.9D, 0.0D),
                EssenceType.LIGHTNING, null, tier
        );
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, departurePos.x, departurePos.y + 0.9D, departurePos.z,
                26, 0.35D, 0.55D, 0.35D, 0.14D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z,
                26, 0.35D, 0.55D, 0.35D, 0.14D);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.35F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.50F);

        Vec3 span = arrivalPos.subtract(departurePos);
        double segLen = span.length();
        if (segLen < 0.25D) return;
        Vec3 normDir = span.scale(1.0D / segLen);
        Vec3 mid = departurePos.add(arrivalPos).scale(0.5D);
        double halfLen = segLen * 0.5D + 1.5D;

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, mid, halfLen, 80, level.getGameTime(), dummy, dummy.subSpells().getFirst(), EssenceType.LIGHTNING, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        SpellWorldVfxManager.broadcastChainArc(
                                srv, departurePos.add(0.0D, 0.85D, 0.0D), arrivalPos.add(0.0D, 0.85D, 0.0D),
                                EssenceType.LIGHTNING, null, tier
                        );
                        for (double d = 0.0D; d <= segLen; d += 1.5D) {
                            Vec3 pt = departurePos.add(normDir.scale(d));
                            srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y + 0.85D, pt.z,
                                    2, 0.10D, 0.15D, 0.10D, 0.05D);
                        }
                    }
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB lineBox = new AABB(departurePos, arrivalPos).inflate(1.35D);
                        for (LivingEntity crosser : srv.getEntitiesOfClass(LivingEntity.class, lineBox, LivingEntity::isAlive)) {
                            if (crosser == owner || (owner != null && crosser.isAlliedTo(owner))) continue;
                            // Exact point-to-segment distance check so diagonal blinks ONLY shock entities crossing the arc line!
                            double distToArc = distanceToRaySegment(crosser.getBoundingBox().getCenter(),
                                    departurePos.add(0.0D, 0.85D, 0.0D), normDir, segLen);
                            if (distToArc <= 1.20D) {
                                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(
                                        srv, crosser, owner, 2.4F + tier * 0.60F, false, true
                                );
                                applyParalysisStun(crosser, 16);
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 2.2 & 6.13 Galvanic Electrolysis & Smelting + Machine Re-Charging:
        // - Recharges placed Orbis Cells / Calix batteries & redstone machinery
        // - Fuses sand & red sand into fulgurite glass
        // - Manages copper oxidation & wax states (de-wax / de-oxidize by default; oxidize forward when sneaking!)
        // - Auto-smelts metal ores & raw metal blocks!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.25D), Entity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }

        if (tryMachineRecharging(level, pos, EssenceType.LIGHTNING, potencyTier)) {
            return true;
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        // 1. Fuse Sand / Red Sand into Fulgurite Glass!
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            emitGalvanicTransmuteVfx(level, pos);
            return true;
        }

        // 2. Manage Copper Oxidation & Wax States!
        if (caster != null && caster.isShiftKeyDown()) {
            Optional<BlockState> oxidizedNext = WeatheringCopper.getNext(state.getBlock()).map(b -> b.withPropertiesOf(state));
            if (oxidizedNext.isPresent()) {
                level.setBlockAndUpdate(pos, oxidizedNext.get());
                level.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        10, 0.3D, 0.3D, 0.3D, 0.05D);
                emitGalvanicTransmuteVfx(level, pos);
                return true;
            }
        }
        Block unwaxedBlock = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock());
        if (unwaxedBlock != null) {
            level.setBlockAndUpdate(pos, unwaxedBlock.withPropertiesOf(state));
            level.sendParticles(ParticleTypes.WAX_OFF, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    10, 0.3D, 0.3D, 0.3D, 0.05D);
            emitGalvanicTransmuteVfx(level, pos);
            return true;
        }
        Optional<BlockState> deoxidized = WeatheringCopper.getPrevious(state);
        if (deoxidized.isPresent()) {
            level.setBlockAndUpdate(pos, deoxidized.get());
            level.sendParticles(ParticleTypes.SCRAPE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    12, 0.3D, 0.3D, 0.3D, 0.05D);
            emitGalvanicTransmuteVfx(level, pos);
            return true;
        }

        // 3. Raw Metal Storage Blocks -> Smelted Metal Blocks!
        if (state.is(Blocks.RAW_IRON_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.IRON_BLOCK.defaultBlockState());
            emitGalvanicTransmuteVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.RAW_COPPER_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.COPPER_BLOCK.defaultBlockState());
            emitGalvanicTransmuteVfx(level, pos);
            return true;
        }
        if (state.is(Blocks.RAW_GOLD_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.GOLD_BLOCK.defaultBlockState());
            emitGalvanicTransmuteVfx(level, pos);
            return true;
        }

        // 4. Auto-smelt metal & mineral ore blocks in-world!
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness >= 0.0F && hardness <= 2.0F + potencyTier * 5.0F) {
            BlockEntity be = level.getBlockEntity(pos);
            ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
            List<ItemStack> drops = Block.getDrops(state, level, pos, be, caster, tool);
            List<ItemStack> smeltedOutputs = new ArrayList<>();
            boolean smeltedAny = false;
            for (ItemStack d : drops) {
                ItemStack res = NetherSpellMutationHandler.trySmeltStack(level, d);
                if (res != d) {
                    smeltedAny = true;
                }
                smeltedOutputs.add(res);
            }
            if (smeltedAny) {
                level.removeBlock(pos, false);
                for (ItemStack out : smeltedOutputs) {
                    Block.popResource(level, pos, out);
                }
                emitGalvanicTransmuteVfx(level, pos);
                return true;
            }
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;
        int count = in.getCount();

        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.GLASS, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    10, 0.2D, 0.2D, 0.2D, 0.08D);
            return true;
        }
        if (in.is(Items.RAW_IRON_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.IRON_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            return true;
        }
        if (in.is(Items.RAW_COPPER_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.COPPER_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            return true;
        }
        if (in.is(Items.RAW_GOLD_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.GOLD_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            return true;
        }

        ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, in);
        if (smelted != in) {
            itemEntity.setItem(smelted);
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    10, 0.2D, 0.2D, 0.2D, 0.08D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 5.6 & 6.13 Galvanic Overcharge Hex:
        // - Persistent milk-immune electrical seizure curse (cleansed by Earth/Grounding magic, Restoration, or caster defeat)
        // - Permanently cuts mob/player attack and movement speed by an exact 40%
        // - Disables shield blocking
        // - Periodically discharges violent shock sparks that damage adjacent allies of the cursed victim!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.40D, 42L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, 0.40D, 42L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1, true, true));

        if (target instanceof Player player && player.isBlocking()) {
            ItemStack shield = player.getUseItem();
            player.stopUsingItem();
            if (!shield.isEmpty()) {
                player.getCooldowns().addCooldown(shield, 100);
            }
        } else if (target.isUsingItem() && target.getUseItem().is(Items.SHIELD)) {
            target.stopUsingItem();
        }

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float seizureDmg = (1.25F + potencyTier * 0.45F) * mult;
        SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, target, hexCaster, seizureDmg, false, false);

        // Periodically arc violent shock sparks to adjacent allies of the cursed victim!
        boolean targetIsHostileMob = target instanceof Enemy;
        Vec3 targetMid = target.getBoundingBox().getCenter();
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.8D), LivingEntity::isAlive)) {
            if (adj == target || adj == hexCaster) continue;
            boolean sameFactionAsVictim = targetIsHostileMob
                    ? (adj instanceof Enemy)
                    : (adj.isAlliedTo(target) || (!(adj instanceof Enemy) && (adj instanceof Player || target instanceof Player)));
            if (!sameFactionAsVictim) continue;

            SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(
                    level, adj, hexCaster, (1.05F + potencyTier * 0.38F) * mult, false, true
            );
            SpellWorldVfxManager.broadcastChainArc(
                    level, targetMid, adj.getBoundingBox().getCenter(), EssenceType.LIGHTNING, null, Math.max(1, potencyTier)
            );
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, adj.getX(), adj.getY(0.7D), adj.getZ(),
                    8, 0.22D, 0.32D, 0.22D, 0.09D);
        }

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.75D), target.getZ(),
                8, 0.28D, 0.38D, 0.28D, 0.08D);
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
        if (effect == SpellCoreEffect.KINETIC) {
            // Section 6.13 Magnetic Railgun:
            // Launches the caster horizontally forward OR flings metallic-armored entities with extreme velocity!
            if (target == caster) {
                fireMagneticRailgunCasterLaunch(level, caster, direction, potencyTier);
                return;
            }
            if (SpellCombatReactionEngine.isBossEntity(level, target)) {
                return;
            }

            Vec3 horiz = new Vec3(direction.x, 0.0D, direction.z);
            if (horiz.lengthSqr() < 1.0E-4D && caster != null) {
                Vec3 fromCaster = target.position().subtract(caster.position());
                horiz = new Vec3(fromCaster.x, 0.0D, fromCaster.z);
            }
            Vec3 launchDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

            boolean metallic = isWearingOrHoldingMetalEquipment(target);
            double horizVel = metallic ? (2.65D + potencyTier * 0.18D) : (1.15D + potencyTier * 0.08D);
            double vertVel = metallic ? 0.46D : 0.28D;
            SpellExecutionEngine.applySyncedMotion(target, new Vec3(launchDir.x * horizVel, vertVel, launchDir.z * horizVel));

            if (caster != null) {
                SpellWorldVfxManager.broadcastChainArc(
                        level, caster.getBoundingBox().getCenter(), target.getBoundingBox().getCenter(),
                        EssenceType.LIGHTNING, null, Math.max(1, potencyTier)
                );
            }
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.7D), target.getZ(),
                    26, 0.35D, 0.38D, 0.35D, 0.16D);
            level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.75F, 1.50F);
        }
    }

    /**
     * Launches {@code caster} horizontally forward like an electromagnetic railgun slug with brief fall-damage cushion!
     */
    public static void fireMagneticRailgunCasterLaunch(
            ServerLevel level,
            LivingEntity caster,
            Vec3 aimDir,
            int potencyTier
    ) {
        Vec3 horiz = new Vec3(aimDir.x, 0.0D, aimDir.z);
        if (horiz.lengthSqr() < 1.0E-4D) {
            Vec3 look = caster.getLookAngle();
            horiz = new Vec3(look.x, 0.0D, look.z);
        }
        Vec3 launchDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        double speed = 2.25D + Math.clamp(potencyTier, 1, 10) * 0.14D;

        Vec3 origin = caster.position().add(0.0D, 0.85D, 0.0D);
        SpellExecutionEngine.applySyncedMotion(caster, new Vec3(launchDir.x * speed, 0.32D, launchDir.z * speed));
        caster.resetFallDistance();

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, origin, launchDir, EssenceType.LIGHTNING, null, 1.35F, Math.max(1, potencyTier)
        );
        SpellWorldVfxManager.broadcastChainArc(
                level, origin, origin.add(launchDir.scale(4.5D)), EssenceType.LIGHTNING, null, Math.max(1, potencyTier)
        );
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX(), caster.getY(0.7D), caster.getZ(),
                28, 0.35D, 0.35D, 0.35D, 0.16D);
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.75F, 1.65F);

        // Cushion fall distance for 3.0s after horizontal railgun launch!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, caster.position(), 1.5D, 60, level.getGameTime(),
                null, null, EssenceType.LIGHTNING, 1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (owner == null || !owner.isAlive() || elapsed > zone.maxDurationTicks) return false;
                    owner.resetFallDistance();
                    return !owner.onGround() || elapsed < 10;
                }
        ));
    }

    /**
     * Guide Section 2.2 Lightning / Static Lineage Utility Specialization (<i>Machine Re-Charging</i>):
     * Directly charges placed Materia battery cells or pulses redstone machinery from a distance!
     */
    public static boolean tryMachineRecharging(
            ServerLevel level,
            BlockPos pos,
            EssenceType materia,
            int tier
    ) {
        if (SpellFuelService.chargePlacedMateriaBatteryOrMachine(level, pos, materia, tier)) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                    18, 0.32D, 0.32D, 0.32D, 0.10D);
            level.playSound(null, pos, SoundEvents.COPPER_BULB_TURN_ON, SoundSource.BLOCKS, 0.85F, 1.35F);
            return true;
        }
        BlockState bs = level.getBlockState(pos);
        if (bs.is(Blocks.REDSTONE_WIRE) || bs.is(Blocks.REDSTONE_LAMP)
                || bs.is(Blocks.COPPER_BULB) || bs.is(Blocks.EXPOSED_COPPER_BULB)
                || bs.is(Blocks.WEATHERED_COPPER_BULB) || bs.is(Blocks.OXIDIZED_COPPER_BULB)
                || bs.is(Blocks.POWERED_RAIL) || bs.is(Blocks.ACTIVATOR_RAIL)
                || bs.is(Blocks.LIGHTNING_ROD)) {
            if (bs.hasProperty(BlockStateProperties.LIT) && (bs.getBlock().getDescriptionId().contains("copper_bulb"))) {
                level.setBlockAndUpdate(pos, bs.setValue(BlockStateProperties.LIT, !bs.getValue(BlockStateProperties.LIT)));
            } else {
                maintainRedstonePowerZone(level, null, pos, 2, 40);
            }
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    14, 0.28D, 0.28D, 0.28D, 0.08D);
            level.playSound(null, pos, SoundEvents.COPPER_BULB_TURN_ON, SoundSource.BLOCKS, 0.75F, 1.40F);
            return true;
        }
        return false;
    }

    // =========================================================================
    // INTERNAL ELECTRICAL, WATER-CONDUCTION & REDSTONE HELPERS
    // =========================================================================

    private static void applyParalysisStun(LivingEntity target, int durationTicks) {
        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), durationTicks, 0));
    }

    public static boolean isWearingOrHoldingMetalEquipment(LivingEntity entity) {
        if (entity instanceof IronGolem) return true;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.SHIELD)) return true;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String path = id.getPath();
            if (path.contains("iron") || path.contains("chainmail") || path.contains("gold")
                    || path.contains("netherite") || path.contains("copper") || path.contains("bronze")
                    || path.contains("steel") || path.contains("silver") || path.contains("tin")
                    || path.contains("lead") || path.contains("brass") || path.contains("electrum")
                    || path.contains("viscanite") || path.contains("arcanite") || path.contains("trident")) {
                return true;
            }
        }
        return entity.getArmorValue() >= 6;
    }

    private static void arcSecondarySparks(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity origin,
            double radius,
            float damage,
            boolean requireWetOrMetalArmor,
            boolean wardSafeguard,
            int tier
    ) {
        Vec3 originMid = origin.getBoundingBox().getCenter();
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, origin.getBoundingBox().inflate(radius), LivingEntity::isAlive)) {
            if (other == origin || other == caster || origin.distanceTo(other) > radius + 0.45D) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(other, caster, wardSafeguard)) {
                continue;
            }
            if (requireWetOrMetalArmor
                    && !SpellLingeringZoneManager.isEntityWetOrSoaked(other)
                    && !isWearingOrHoldingMetalEquipment(other)) {
                continue;
            }
            SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, other, caster, damage, false, true);
            applyParalysisStun(other, 12);
            SpellWorldVfxManager.broadcastChainArc(
                    level, originMid, other.getBoundingBox().getCenter(), EssenceType.LIGHTNING, null, tier
            );
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, other.getX(), other.getY(0.7D), other.getZ(),
                    12, 0.25D, 0.35D, 0.25D, 0.10D);
        }
    }

    /**
     * Conducts high-voltage electricity through a connected body of water (BFS up to 48 water blocks)
     * and shocks all submerged or wet entities touching that water pool!
     */
    private static void electrifyConnectedWaterPool(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos strikePos,
            float shockDamage,
            boolean wardSafeguard
    ) {
        BlockPos seedWater = null;
        for (BlockPos p : BlockPos.betweenClosed(strikePos.offset(-2, -1, -2), strikePos.offset(2, 1, 2))) {
            if (level.getBlockState(p).is(Blocks.WATER) || level.getBlockState(p).is(Blocks.BUBBLE_COLUMN)) {
                seedWater = p.immutable();
                break;
            }
        }
        if (seedWater == null) return;

        Set<BlockPos> poolBlocks = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(seedWater);
        poolBlocks.add(seedWater);

        while (!queue.isEmpty() && poolBlocks.size() < 48) {
            BlockPos cur = queue.poll();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    cur.getX() + 0.5D, cur.getY() + 0.95D, cur.getZ() + 0.5D,
                    3, 0.35D, 0.08D, 0.35D, 0.08D);
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos nb = cur.relative(dir);
                if (poolBlocks.size() >= 48 || poolBlocks.contains(nb)) continue;
                BlockState nbs = level.getBlockState(nb);
                if (nbs.is(Blocks.WATER) || nbs.is(Blocks.BUBBLE_COLUMN)) {
                    poolBlocks.add(nb.immutable());
                    queue.add(nb.immutable());
                }
            }
        }

        AABB poolBounds = new AABB(strikePos).inflate(8.5D);
        for (LivingEntity submerged : level.getEntitiesOfClass(LivingEntity.class, poolBounds, LivingEntity::isAlive)) {
            if (submerged == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(submerged, caster, wardSafeguard)) {
                continue;
            }
            if (submerged.isInWater() || poolBlocks.contains(submerged.blockPosition())
                    || poolBlocks.contains(submerged.blockPosition().below())) {
                SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(level, submerged, caster, shockDamage, false, true);
                applyParalysisStun(submerged, 20);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, submerged.getX(), submerged.getY(0.7D), submerged.getZ(),
                        14, 0.30D, 0.35D, 0.30D, 0.12D);
            }
        }
    }

    /**
     * Actively maintains level-15 power on adjacent redstone wires, lamps, and powered rails for {@code durationTicks}
     * (e.g., 20t momentary EMP pulse on <i>Static EMP Nova</i>, 80t 4.0s sustain on <i>Tesla Arc Mine</i>),
     * then cleanly restores normal redstone state upon expiry!
     */
    private static void maintainRedstonePowerZone(
            ServerLevel level,
            @Nullable LivingEntity owner,
            BlockPos center,
            int rad,
            int durationTicks
    ) {
        List<BlockPos> affected = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 2, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.REDSTONE_WIRE) || bs.is(Blocks.REDSTONE_LAMP)
                    || bs.is(Blocks.POWERED_RAIL) || bs.is(Blocks.ACTIVATOR_RAIL)) {
                affected.add(p.immutable());
            }
        }
        if (affected.isEmpty()) return;

        applyRedstonePulseStep(level, affected, true);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                owner, Vec3.atCenterOf(center), rad, durationTicks, level.getGameTime(),
                null, null, EssenceType.LIGHTNING, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) {
                        if (elapsed % 4 == 0) {
                            applyRedstonePulseStep(srv, affected, true);
                        }
                        return true;
                    }
                    applyRedstonePulseStep(srv, affected, false);
                    return false;
                }
        ));
    }

    private static void applyRedstonePulseStep(ServerLevel level, List<BlockPos> positions, boolean energized) {
        for (BlockPos p : positions) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.REDSTONE_WIRE) && bs.hasProperty(RedStoneWireBlock.POWER)) {
                if (energized) {
                    if (bs.getValue(RedStoneWireBlock.POWER) < 15) {
                        level.setBlock(p, bs.setValue(RedStoneWireBlock.POWER, 15), Block.UPDATE_CLIENTS);
                        level.updateNeighborsAt(p, bs.getBlock());
                    }
                } else {
                    level.setBlock(p, bs.setValue(RedStoneWireBlock.POWER, 0), Block.UPDATE_ALL);
                    level.updateNeighborsAt(p, bs.getBlock());
                }
            } else if (bs.is(Blocks.REDSTONE_LAMP) && bs.hasProperty(BlockStateProperties.LIT)) {
                boolean hasNaturalSignal = level.hasNeighborSignal(p);
                boolean targetLit = energized || hasNaturalSignal;
                if (bs.getValue(BlockStateProperties.LIT) != targetLit) {
                    level.setBlock(p, bs.setValue(BlockStateProperties.LIT, targetLit), Block.UPDATE_CLIENTS);
                }
            } else if ((bs.is(Blocks.POWERED_RAIL) || bs.is(Blocks.ACTIVATOR_RAIL))
                    && bs.hasProperty(PoweredRailBlock.POWERED)) {
                boolean hasNaturalSignal = level.hasNeighborSignal(p);
                boolean targetPowered = energized || hasNaturalSignal;
                if (bs.getValue(PoweredRailBlock.POWERED) != targetPowered) {
                    level.setBlock(p, bs.setValue(PoweredRailBlock.POWERED, targetPowered), Block.UPDATE_ALL);
                }
            }
        }
    }

    /**
     * Guide Section 1.7 Dynamic Aggro &amp; Sonic/Luminescent Threat Emission:
     * Loud Lightning claps alert hostile mobs within 28 blocks unless suppressed by Umbral/Silence!
     */
    private static void alertHostilesToLightningClap(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 clapOrigin,
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
        AABB alertBox = new AABB(clapOrigin, clapOrigin).inflate(28.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, alertBox, LivingEntity::isAlive)) {
            if (mob instanceof Monster && mob.getTarget() == null && !mob.isAlliedTo(caster)) {
                if (mob.distanceToSqr(clapOrigin) <= 784.0D) {
                    mob.setTarget(caster);
                }
            }
        }
    }

    private static void emitGalvanicTransmuteVfx(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                14, 0.28D, 0.28D, 0.28D, 0.09D);
        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.45F, 1.75F);
    }

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
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.LIGHTNING, refundMfum);
        }
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

    private static double distanceToRaySegment(Vec3 point, Vec3 segStart, Vec3 normDir, double maxLen) {
        Vec3 rel = point.subtract(segStart);
        double proj = Mth.clamp(rel.dot(normDir), 0.0D, maxLen);
        return point.distanceTo(segStart.add(normDir.scale(proj)));
    }
}
