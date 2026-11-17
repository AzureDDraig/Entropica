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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Storm Materia (Tier 1 Elemental Fusion #14: Air + Water)</b>
 * (Spellcrafting System Guide Sections 1.2, 2.2, 4.1–4.9, 5.1–5.6 &amp; 6.15).
 *
 * <p>Canonical Advantage: <b>Strong against EARTH (+1) and GLACIAL (+1)</b> |
 * <b>Weak against NATURE (-1)</b>
 * (Dominance: {@code storm_air_dom} computed automatically by {@code VisInteractionManager};
 * Tier 1 Elemental Fusion: {@code 10%} base physical armor bypass &amp; {@code 1} base armor shred point).</p>
 */
public final class StormSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TEMPEST_ZEPHYR_HEX_COLOR = 0x429BA3;
    public static final int TEMPEST_SEAFOAM_HEX_COLOR = 0xD8FAFF;

    private static final String TAG_REPEATING_PULSES_LEFT = "SpellStormGlyphPulsesLeft";
    private static final String TAG_REPEATING_COOLDOWN_UNTIL = "SpellStormGlyphCooldownUntil";

    @Override
    public EssenceType materiaType() {
        return EssenceType.STORM;
    }

    // =========================================================================
    // CANONICAL TITLES & DESCRIPTIONS (GUIDE SECTION 6.15)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Squall-Leap Chains";
            default -> "Tempest Squall " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP ->
                    "Leaping chains jump along roaring wind-and-water arcs; each leap pulls chained victims toward each other with vacuum currents and drenches them, with leap distance increased by +50% during rain.";
            default ->
                    "Accelerates spell flow with roaring #429BA3 gale-force squall currents and driving rain droplets.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Squall Tempest Slug";
            case BEAM -> "Gale-Cutter Torrent Beam";
            case RADIAL_BURST -> "Tempest Nova Burst";
            case TOUCH_STRIKE -> "Squall Cleave";
            case AURA -> "Squall-Rider Shroud";
            case GLYPH -> "Atmospheric Downburst Trap";
            case CONE -> "Deluge Squall Cone";
            case SKYFALL -> "Tempest Monsoon Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Fires a turbulent 3D vortex of high-speed gale-winds and driving rain droplets (#429BA3); knocks struck entities 5 blocks backward, soaks targets, and extinguishes surface fires.";
            case BEAM ->
                    "Channels a sustained high-pressure torrent of howling wind and cutting water droplets (#429BA3); steadily shoves enemies back, drenches targets, extinguishes fire, and provides recoil propulsion if aimed at the ground.";
            case RADIAL_BURST ->
                    "Detonates in an explosive 360-degree hurricane squall (6-block radius) that blasts mobs 8 blocks backward, deflects incoming projectiles, soaks all nearby entities, and instantly extinguishes all fires in the radius.";
            case TOUCH_STRIKE ->
                    "Melee strike hits with gale-force wind and driving rain; hurls the victim 6 blocks back and 4 blocks airborne, drenches them with soaking wetness, and deflects any incoming arrows during the swing.";
            case AURA ->
                    "Enables continuous glider flight drift and slow-fall, with flight speed doubled (2.0x) if the world is currently experiencing rain or thunder.";
            case GLYPH ->
                    "Surface-snapped barometric trap (1.0s arming, 50% Sneak-Right-Click disarm refund) that releases a crushing downburst of wind and rain pinning mobs in a 4-block radius flat to the ground for 3s, disabling movement and jumping.";
            case CONE ->
                    "Sprays a wide 60-degree cone of torrential rain and gale-force winds; pushes enemies 8 blocks backward, soaks all mobs, extinguishes fires, washes away cobwebs, and hydrates all farmland in the cone.";
            case SKYFALL ->
                    "Summons a cave-ready 16-block torrential hurricane downpour for 8s; drenches the zone, extinguishes all fires, hydrates farmland, pushes hostile mobs toward the edges, and knocks down flying mobs.";
            case SUMMON_AGENT ->
                    "Summons a tempest-teal (#429BA3) Tempest-Zephyr Agent that dashes swiftly between enemies delivering drenching squall strikes with knockback.";
            case SUMMON_SENTRY ->
                    "Anchors a Tempest-Squall Spire (#429BA3) that extinguishes nearby fires, hydrates farmland, and fires pressurized water/wind slugs with heavy pushback.";
            case SUMMON_DECOY ->
                    "Conjures a spinning Tempest Nova Effigy (#429BA3) that taunts mobs and detonates into an explosive 6-block Tempest Nova Burst upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Squall-Step";
            case TRANSMUTE, BREAK -> "Aero-Hydraulic Erosion";
            case AFFLICTION -> "Waterlogged Hypoxia Hex";
            case KINETIC -> "Hurricane Force Push";
            default -> "Tempest " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Leaves a vacuum splash at departure that pulls nearby foes inward and drenches them, while granting an exact +40% movement speed boost on arrival.";
            case TRANSMUTE, BREAK ->
                    "Washes Dirt into Mud, Mud into Clay, Clay into Gravel, Gravel into Sand, and cools Lava into Obsidian.";
            case AFFLICTION ->
                    "Persistent milk-immune atmospheric deluge curse (cleansed via Restoration magic, Fire/Heat reactions, or caster defeat): continuously inflicts drowning damage on land, permanently cuts attack speed by 40%, and doubles incoming Lightning damage.";
            case KINETIC ->
                    "Unleashes a 14-block gale-force push with 2x knockback against wet or swimming targets.";
            default ->
                    "Infuses the spell effect with Tier 1 Fusion Storm Materia (+1 vs Earth & Glacial, -1 vs Nature, 10% armor bypass).";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — SQUALL-LEAP CHAINS
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
        // Section 6.15 Squall-Leap Chains:
        // Leaping chains jump along roaring wind-and-water arcs (+50% 18m jump distance during rain in SpellExecutionEngine);
        // each leap pulls chained victims toward each other with vacuum currents and drenches them!
        Vec3 mid = fromTarget.position().add(toTarget.position()).scale(0.5D);
        fromTarget.clearFire();
        toTarget.clearFire();
        SpellLingeringZoneManager.applySoaked(fromTarget, 180);
        SpellLingeringZoneManager.applySoaked(toTarget, 180);

        if (!SpellCombatReactionEngine.isBossEntity(level, fromTarget)) {
            Vec3 deltaA = mid.subtract(fromTarget.position());
            Vec3 pullA = deltaA.lengthSqr() > 1.0E-4D ? deltaA.normalize().scale(0.68D) : Vec3.ZERO;
            SpellExecutionEngine.applySyncedMotion(fromTarget, new Vec3(pullA.x, 0.24D, pullA.z));
        }
        if (!SpellCombatReactionEngine.isBossEntity(level, toTarget)) {
            Vec3 deltaB = mid.subtract(toTarget.position());
            Vec3 pullB = deltaB.lengthSqr() > 1.0E-4D ? deltaB.normalize().scale(0.68D) : Vec3.ZERO;
            SpellExecutionEngine.applySyncedMotion(toTarget, new Vec3(pullB.x, 0.24D, pullB.z));
        }

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, mid.add(0.0D, 0.7D, 0.0D), 2.2D, true, EssenceType.STORM, EssenceType.WATER, 16, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.SPLASH, mid.x, mid.y + 0.8D, mid.z, 16, 0.45D, 0.35D, 0.45D, 0.10D);
        level.sendParticles(ParticleTypes.GUST, mid.x, mid.y + 0.8D, mid.z, 2, 0.25D, 0.20D, 0.25D, 0.02D);
        level.playSound(null, BlockPos.containing(mid), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.55F, 1.30F);
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 STORM FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.15 Squall Tempest Slug: turbulent vortex of high-speed gale-winds and driving rain droplets (#429BA3)!
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        double speed = vel.length();
        if (speed > 1.0E-4D) {
            double boosted = Math.max(2.15D, speed * 1.32D);
            projectile.setDeltaMovement(vel.scale(boosted / speed));
        }
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(),
                8, 0.14D, 0.14D, 0.14D, 0.08D);
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.65F, 1.28F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(),
                3, 0.12D, 0.12D, 0.12D, 0.04D);
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(),
                1, 0.08D, 0.08D, 0.08D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), TEMPEST_ZEPHYR_HEX_COLOR, 14, 0.72F, 0.10F),
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
        // Section 6.15 Squall Tempest Slug:
        // Knocks struck entities 5 blocks backward with concussive squall force, soaks targets, and extinguishes surface fires!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float slugDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * 0.48F * spell.flawProfile().getPotencyMultiplier();
        Vec3 impactPos = hitEntity.getBoundingBox().getCenter();

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.STORM, EssenceType.AIR, 1.35F, tier
        );

        if (hitEntity instanceof LivingEntity living && living.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, wardSafe))) {
            living.clearFire();
            EntityHelper.getPersistentData(living).remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            SpellLingeringZoneManager.applySoaked(living, 180);

            if (!SpellCombatReactionEngine.isBossEntity(level, living)) {
                Vec3 vel = projectile.getDeltaMovement();
                Vec3 horiz = new Vec3(vel.x, 0.0D, vel.z);
                Vec3 pushDir = horiz.lengthSqr() > 1.0E-4D
                        ? horiz.normalize()
                        : (caster != null ? caster.getLookAngle().normalize() : new Vec3(1.0D, 0.0D, 0.0D));
                // Horizontal impulse 1.38D + 0.36D lift -> exact ~5.1 blocks backward knockback!
                SpellExecutionEngine.applySyncedMotion(living, new Vec3(pushDir.x * 1.38D, 0.36D, pushDir.z * 1.38D));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(level, living, caster, slugDmg, false, true);
            }
        }

        extinguishFiresAround(level, BlockPos.containing(impactPos), 3);
        level.sendParticles(ParticleTypes.SPLASH, impactPos.x, impactPos.y, impactPos.z, 22, 0.42D, 0.35D, 0.42D, 0.10D);
        level.sendParticles(ParticleTypes.GUST, impactPos.x, impactPos.y, impactPos.z, 2, 0.25D, 0.20D, 0.25D, 0.02D);
        level.playSound(null, BlockPos.containing(impactPos), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.78F, 1.18F);
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
        Vec3 loc = blockHit.getLocation();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());

        SpellWorldVfxManager.broadcastProjectileImpact(level, loc, normal, EssenceType.STORM, EssenceType.AIR, 1.25F, tier);
        extinguishFiresAround(level, blockHit.getBlockPos(), 3);
        level.sendParticles(ParticleTypes.SPLASH, loc.x, loc.y + 0.1D, loc.z, 20, 0.40D, 0.30D, 0.40D, 0.10D);
        level.playSound(null, blockHit.getBlockPos(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.70F, 1.22F);
    }

    // =========================================================================
    // FORM MUTATION 2 — GALE-CUTTER TORRENT BEAM (BEAM)
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
        // Section 6.15 Gale-Cutter Torrent Beam (#429BA3):
        // Sustained high-pressure torrent of howling wind and cutting water droplets;
        // steadily shoves enemies back, drenches targets, extinguishes fire, and provides recoil propulsion if aimed at the ground!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float beamDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.25F, apparatusPotency) * 0.64F * spell.flawProfile().getPotencyMultiplier();

        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 muzzle = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D).add(normDir.scale(0.38D));
        Vec3 endPos = blockHit.getType() == HitResult.Type.BLOCK
                ? blockHit.getLocation()
                : muzzle.add(normDir.scale(maxReach));

        SpellWorldVfxManager.broadcastChanneledBeamSlice(
                level, muzzle, endPos, EssenceType.STORM, EssenceType.AIR, tier, 8
        );

        Set<UUID> seen = new HashSet<>();
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || !seen.add(hit.getUUID())) continue;
            hit.clearFire();
            SpellLingeringZoneManager.applySoaked(hit, 180);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(
                        hit, new Vec3(normDir.x * 1.08D, Math.max(0.26D, normDir.y * 0.45D), normDir.z * 1.08D)
                );
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(level, hit, caster, beamDmg, false, false);
            }
        }

        // Recoil propulsion when aimed downward toward the ground!
        boolean groundRecoil = normDir.y < -0.35D
                || (normDir.y < -0.15D
                && blockHit.getType() == HitResult.Type.BLOCK
                && blockHit.getLocation().distanceToSqr(caster.position()) <= 25.0D);
        if (groundRecoil) {
            Vec3 cur = caster.getDeltaMovement();
            double rx = Math.clamp(cur.x - normDir.x * 0.48D, -1.65D, 1.65D);
            double ry = Math.max(0.68D, Math.min(1.15D, cur.y - normDir.y * 0.78D));
            double rz = Math.clamp(cur.z - normDir.z * 0.48D, -1.65D, 1.65D);
            SpellExecutionEngine.applySyncedMotion(caster, new Vec3(rx, ry, rz));
            caster.resetFallDistance();
        }

        double spanLen = Math.min(maxReach, muzzle.distanceTo(endPos) + 0.5D);
        int steps = Math.min(20, (int) Math.ceil(spanLen));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(muzzle.add(normDir.scale(s)));
            extinguishFiresAround(level, stepPos, 1);
            if ((s & 1) == 0) {
                level.sendParticles(ParticleTypes.SPLASH,
                        stepPos.getX() + 0.5D, stepPos.getY() + 0.5D, stepPos.getZ() + 0.5D,
                        2, 0.15D, 0.15D, 0.15D, 0.04D);
            }
        }
        if (level.getGameTime() % 6L == 0L) {
            level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.48F, 1.38F);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — TEMPEST NOVA BURST (RADIAL BURST)
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
        // Section 6.15 Tempest Nova Burst:
        // Explosive 360-degree hurricane squall (6-block radius) that blasts mobs 8 blocks backward,
        // deflects incoming projectiles, soaks all nearby entities, and instantly extinguishes all fires!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float novaDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.60F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.STORM, EssenceType.AIR, tier);
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.add(0.0D, 0.8D, 0.0D), r, false, EssenceType.STORM, EssenceType.WATER, 22, tier
        );

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.2D, center.z + r);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (entity == caster || entity.position().distanceTo(center) > r + 0.5D) continue;
            entity.clearFire();
            EntityHelper.getPersistentData(entity).remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            SpellLingeringZoneManager.applySoaked(entity, 200);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, wardSafe)) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, entity)) {
                Vec3 away = entity.position().subtract(center);
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                // Horizontal 1.88D + 0.46D lift -> exact ~8.1 blocks backward blast!
                SpellExecutionEngine.applySyncedMotion(entity, new Vec3(push.x * 1.88D, 0.46D, push.z * 1.88D));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(level, entity, caster, novaDmg, false, true);
            }
        }

        // Deflect incoming projectiles across the 6-block hurricane radius!
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
            if (proj.getOwner() == caster) continue;
            Vec3 away = proj.position().subtract(center);
            Vec3 defl = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.65D) : proj.getDeltaMovement().scale(-1.2D);
            proj.setDeltaMovement(defl);
            proj.hasImpulse = true;
        }

        extinguishFiresAround(level, BlockPos.containing(center), (int) Math.ceil(r));
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.75D, center.z, 54, r * 0.45D, 0.55D, r * 0.45D, 0.16D);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, center.x, center.y + 0.6D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.86F);
    }

    // =========================================================================
    // FORM MUTATION 4 — SQUALL CLEAVE (TOUCH / STRIKE)
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
        // Section 6.15 Squall Cleave:
        // Hurls victim 6 blocks back and 4 blocks airborne, drenches them with soaking wetness,
        // and deflects any incoming arrows during the swing!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float strikeDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 horiz = new Vec3(normDir.x, 0.0D, normDir.z);
        Vec3 pushDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : normDir;

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D),
                normDir, 3.1D, EssenceType.STORM, EssenceType.AIR, tier
        );

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            hit.clearFire();
            SpellLingeringZoneManager.applySoaked(hit, 180);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }
            if (hit.isAlive() && !SpellCombatReactionEngine.isBossEntity(level, hit)) {
                // vy = 0.88D reaches ~4.1 blocks airborne; horiz 1.52D hurls victim ~6.2 blocks back!
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(pushDir.x * 1.52D, 0.88D, pushDir.z * 1.52D));
            }
            level.sendParticles(ParticleTypes.SPLASH, hit.getX(), hit.getY(0.75D), hit.getZ(),
                    18, 0.32D, 0.35D, 0.32D, 0.10D);
        }

        // Deflect incoming arrows & projectiles immediately AND across the active 12-tick swing arc!
        deflectProjectilesDuringSwing(level, caster, normDir);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, caster.position(), 3.6D, 12, level.getGameTime(), null, null, EssenceType.STORM, 1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (owner == null || !owner.isAlive() || elapsed > zone.maxDurationTicks) return false;
                    deflectProjectilesDuringSwing(srv, owner, owner.getLookAngle().normalize());
                    return true;
                }
        ));
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.15F);
    }

    private static void deflectProjectilesDuringSwing(ServerLevel level, LivingEntity caster, Vec3 dir) {
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, caster.getBoundingBox().inflate(3.6D))) {
            if (proj.getOwner() == caster || proj.onGround()) continue;
            proj.setDeltaMovement(dir.scale(Math.max(1.55D, proj.getDeltaMovement().length() * 1.15D)));
            proj.setOwner(caster);
            proj.hasImpulse = true;
            level.sendParticles(ParticleTypes.GUST, proj.getX(), proj.getY(), proj.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.02D);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — SQUALL-RIDER SHROUD (AURA / SELF)
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
        // Section 6.15 Squall-Rider Shroud:
        // Enables continuous glider flight drift and slow-fall (ticked every tick in SpellLingeringZoneManager),
        // with flight speed doubled (2.0x) if the world is currently experiencing rain or thunder!
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        long now = level.getGameTime();
        boolean stormy = level.isRaining() || level.isThundering();

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_STORM_SHROUD_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        caster.clearFire();
        caster.resetFallDistance();
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(
                caster, level, 0.0D, 0.0D, 0.0D, stormy ? 0.40D : 0.20D, 25L
        );
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.78F, null);

        SpellWorldVfxManager.broadcastWardShell(
                level, caster.position(), Math.max(1.35D, caster.getBbWidth() * 1.35D),
                EssenceType.STORM, EssenceType.AIR, Math.min(dur, 120), tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, caster.position().add(0.0D, 0.04D, 0.0D), 2.8D, EssenceType.STORM, EssenceType.AIR, 52, tier
        );
        level.sendParticles(ParticleTypes.GUST, caster.getX(), caster.getY(0.8D), caster.getZ(),
                8, 0.45D, 0.45D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, caster.getX(), caster.getY(0.8D), caster.getZ(),
                24, 0.45D, 0.50D, 0.45D, 0.08D);
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.75F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 6 — ATMOSPHERIC DOWNBURST TRAP (GLYPH / TRAP)
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
        // Section 4.6 & 6.15 Atmospheric Downburst Trap:
        // - Surface-snapped barometric trap (1.0s arming, 10m or Tier 5+ indefinite lifetime, 50% Sneak-Right-Click disarm refund)
        // - Supports Single-Use Detonator (100%) and Repeating Glyphic Sentry (3 pulses @ 60%, re-arms after 3s downburst)
        // - Releases a crushing downburst of wind and rain that pins mobs in a 4-block radius flat to the ground
        //   for 3.0 seconds (60t), disabling movement and jumping!
        int tier = spell.getEffectiveTier();
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceBlock = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(glyphCenter.x, surfaceBlock.getY() + 1.02D, glyphCenter.z);
        double triggerRad = Math.max(2.4D, Math.min(4.0D, radius));

        String subMode = slot.primaryEffect().subMode();
        boolean repeatingSentry = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        CompoundTag casterTag = EntityHelper.getPersistentData(caster);
        String pulseKey = TAG_REPEATING_PULSES_LEFT + "_" + level.getGameTime();
        String cdKey = TAG_REPEATING_COOLDOWN_UNTIL + "_" + level.getGameTime();
        if (repeatingSentry) {
            casterTag.putInt(pulseKey, 3);
            casterTag.putLong(cdKey, 0L);
        }

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, 2.5D, EssenceType.STORM, EssenceType.AIR, 52, tier);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_HAGALAZ.get(), TEMPEST_ZEPHYR_HEX_COLOR, 32, 0.90F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 5, 0.48D, 0.05D, 0.48D, 0.005D
        );
        level.playSound(null, BlockPos.containing(snappedCenter), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.75F, 1.35F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, 4.0D, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        srv.sendParticles(ParticleTypes.SPLASH,
                                zone.center.x, zone.center.y + 0.08D, zone.center.z, 4, 0.45D, 0.05D, 0.45D, 0.03D);
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
                            zone.center.x - triggerRad, zone.center.y - 1.0D, zone.center.z - triggerRad,
                            zone.center.x + triggerRad, zone.center.y + 2.4D, zone.center.z + triggerRad
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (stepped.isEmpty()) return true;

                    float pulseScale = repeatingSentry ? 0.60F : 1.0F;
                    int downburstTicks = Math.max(36, Math.round(60.0F * pulseScale)); // Canonical 3.0s (60t) on Single-Use!
                    boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    float downburstDmg = Math.max(3.6F + tier * 1.10F,
                            MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.45F, zone.apparatusPotency) * 0.68F)
                            * flawPot * pulseScale;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 4.0D, EssenceType.STORM, EssenceType.AIR, tier);
                    extinguishFiresAround(srv, BlockPos.containing(zone.center), 4);

                    AABB pinBox = new AABB(
                            zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 3.2D, zone.center.z + 4.0D
                    );
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, pinBox, e -> e.isAlive() && e != owner)) {
                        if (v.position().distanceTo(zone.center) > 4.35D) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;
                        v.clearFire();
                        SpellLingeringZoneManager.applySoaked(v, 180);
                        pinEntityFlatToGround(srv, v, srv.getGameTime() + downburstTicks);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * pulseScale
                            );
                        }
                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                            SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(srv, v, owner, downburstDmg, false, true);
                        }
                    }
                    srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 1.6D, zone.center.z,
                            42, 2.0D, 0.55D, 2.0D, 0.14D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.68F);

                    // Sustain the crushing 4-block Atmospheric Downburst field for the full 3.0s (60t)!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, downburstTicks, now, null, null, EssenceType.STORM, pulseScale,
                            (dsrv, dzone, delapsed) -> {
                                if (delapsed > dzone.maxDurationTicks) return false;
                                if (delapsed % 5 == 0) {
                                    dsrv.sendParticles(ParticleTypes.RAIN, dzone.center.x, dzone.center.y + 2.4D, dzone.center.z,
                                            24, 2.0D, 0.5D, 2.0D, 0.12D);
                                    dsrv.sendParticles(ParticleTypes.SPLASH, dzone.center.x, dzone.center.y + 0.15D, dzone.center.z,
                                            16, 2.0D, 0.15D, 2.0D, 0.08D);
                                }
                                LivingEntity dOwner = dzone.resolveCaster(dsrv);
                                long lockUntil = dsrv.getGameTime() + 14L;
                                for (LivingEntity pinned : dsrv.getEntitiesOfClass(LivingEntity.class, pinBox, e -> e.isAlive() && e != dOwner)) {
                                    if (pinned.position().distanceTo(dzone.center) > 4.35D) continue;
                                    if (dOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(pinned, dOwner, wardSafe)) {
                                        continue;
                                    }
                                    SpellLingeringZoneManager.applySoaked(pinned, 60);
                                    pinEntityFlatToGround(dsrv, pinned, lockUntil);
                                }
                                return true;
                            }
                    ));

                    if (repeatingSentry && owner != null) {
                        CompoundTag oTag = EntityHelper.getPersistentData(owner);
                        int rem = oTag.getInt(pulseKey).orElse(1) - 1;
                        if (rem > 0) {
                            oTag.putInt(pulseKey, rem);
                            oTag.putLong(cdKey, now + downburstTicks + 20L);
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

    private static void pinEntityFlatToGround(ServerLevel level, LivingEntity target, long lockUntilTick) {
        if (SpellCombatReactionEngine.isBossEntity(level, target)) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 18, 6, false, false));
        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 18, 0, false, false));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, lockUntilTick);
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setJumping(false);
        }
        SpellExecutionEngine.applySyncedMotion(target, new Vec3(0.0D, Math.min(-0.55D, target.getDeltaMovement().y), 0.0D));
    }

    // =========================================================================
    // FORM MUTATION 7 — DELUGE SQUALL CONE (CONE / SPRAY)
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
        // Section 6.15 Deluge Squall Cone:
        // Wide 60-degree cone of torrential rain and gale-force winds;
        // pushes enemies 8 blocks backward, soaks all mobs, extinguishes fires,
        // washes away cobwebs, and hydrates all farmland in the cone!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition().subtract(0.0D, 0.20D, 0.0D);
        double reach = Math.max(7.5D, coneRange);
        double cosHalfCone = 0.866D; // Exact 60-degree cone (+/- 30 degrees)
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean hasHarm = slotExecutesHarmVerb(slot);
        float delugeDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.40F, apparatusPotency) * 0.64F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastVortexCone(
                level, eye, normDir, reach, EssenceType.STORM, EssenceType.AIR, tier
        );

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

        Vec3 horiz = new Vec3(normDir.x, 0.0D, normDir.z);
        Vec3 pushDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : normDir;
        for (LivingEntity hit : candidates) {
            if (!hit.isAlive() || hit == caster || !struckIds.add(hit.getUUID())) continue;
            hit.clearFire();
            SpellLingeringZoneManager.applySoaked(hit, 180);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                // Horizontal 1.86D + 0.42D lift -> exact ~8.0 blocks gale push!
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(pushDir.x * 1.86D, 0.42D, pushDir.z * 1.86D));
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !hasHarm) {
                SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
            }
        }

        // Sweep the true 60-degree cone volume: extinguish fires, wash away cobwebs, and hydrate all farmland!
        int boxR = Math.min(11, (int) Math.ceil(reach));
        BlockPos originBlock = BlockPos.containing(eye);
        for (BlockPos p : BlockPos.betweenClosed(
                originBlock.offset(-boxR, -3, -boxR),
                originBlock.offset(boxR, 3, boxR))) {
            Vec3 cellCenter = Vec3.atCenterOf(p);
            Vec3 rel = cellCenter.subtract(eye);
            double dist = rel.length();
            if (dist < 0.6D || dist > reach + 0.6D) continue;
            if (rel.scale(1.0D / dist).dot(normDir) < 0.82D) continue;

            BlockState bs = level.getBlockState(p);
            if (bs.isAir()) continue;
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.is(Blocks.COBWEB)) {
                level.destroyBlock(p, true, caster);
            } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
            }
        }

        level.sendParticles(ParticleTypes.SPLASH,
                eye.x + normDir.x * 2.6D, eye.y + normDir.y * 2.6D, eye.z + normDir.z * 2.6D,
                32, 0.90D, 0.45D, 0.90D, 0.12D);
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.92F, 1.05F);
    }

    // =========================================================================
    // FORM MUTATION 8 — TEMPEST MONSOON DOWNPOUR (RAIN / SKYFALL)
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
        // Section 4.8 & 6.15 Tempest Monsoon Downpour:
        // Cave-ceiling-adaptive 16-block torrential hurricane downpour (radius >= 8.0m -> 16m diameter)
        // for 8.0 seconds (160t); drenches the zone, extinguishes all fires, hydrates farmland,
        // pushes hostile mobs toward the edges with swirling winds, and knocks down flying mobs!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos groundPos = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 anchoredGround = new Vec3(impactCenter.x, groundPos.getY() + 1.0D, impactCenter.z);
        double canopyClearance = probeOverheadClearance(level, groundPos.above(), 10);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, anchoredGround, r, EssenceType.STORM, EssenceType.AIR, 160, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredGround, r, 160, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 4 == 0) {
                        srv.sendParticles(ParticleTypes.RAIN,
                                zone.center.x, zone.center.y + Math.min(5.0D, canopyClearance * 0.72D), zone.center.z,
                                36, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.15D);
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        boolean hasHarm = slotExecutesHarmVerb(zone.slot);
                        float monsoonDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                                * Math.max(0.35F, zone.apparatusPotency) * 0.36F
                                * (zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F);

                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(9, (int) Math.ceil(zone.radius));
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -2, -br), cPos.offset(br, 2, br))) {
                            if (p.distSqr(cPos) > rSq) continue;
                            BlockState bs = srv.getBlockState(p);
                            if (bs.getBlock() instanceof BaseFireBlock) {
                                srv.removeBlock(p, false);
                            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                                srv.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                            } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                                srv.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                            }
                        }

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(7.5D, canopyClearance), zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            if ((dx * dx + dz * dz) > rSq) continue;

                            v.clearFire();
                            SpellLingeringZoneManager.applySoaked(v, 200);
                            if (v == owner || (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe))) {
                                continue;
                            }

                            if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                // Swirling hurricane winds push hostiles outward + tangent toward the rim AND knock down flyers!
                                Vec3 rad = new Vec3(dx, 0.0D, dz);
                                Vec3 outUnit = rad.lengthSqr() > 1.0E-4D ? rad.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                                Vec3 tang = new Vec3(-outUnit.z, 0.0D, outUnit.x);
                                Vec3 rimSwirl = outUnit.scale(0.68D).add(tang.scale(0.34D));
                                boolean airborne = !v.onGround();
                                if (airborne) {
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 22L);
                                }
                                double downY = airborne ? -0.95D : 0.16D;
                                SpellExecutionEngine.applySyncedMotion(v, new Vec3(rimSwirl.x, downY, rimSwirl.z));
                            }
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.36F
                                );
                            }
                            if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm() && !hasHarm && v.isAlive()) {
                                SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(srv, v, owner, monsoonDmg, false, true);
                            }
                        }
                        srv.playSound(null, cPos, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.65F, 1.05F);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — TEMPEST-ZEPHYR EIDOLON SUITE (#429BA3)
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
        tag.putString("SpellEidolonMateria", EssenceType.STORM.name());
        tag.putInt("SpellEidolonTintRGB", TEMPEST_ZEPHYR_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.35D, radius), EssenceType.STORM, EssenceType.AIR, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.SPLASH, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.08D);
        level.sendParticles(ParticleTypes.GUST, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 3, 0.35D, 0.35D, 0.35D, 0.02D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.78F, 1.25F);
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
        // Section 6.15 Squall-Step:
        // Leaves a vacuum splash at departure and grants an exact +40% movement speed boost on arrival!
        caster.clearFire();
        extinguishFiresAround(level, BlockPos.containing(departurePos), 3);

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, departurePos.add(0.0D, 0.85D, 0.0D), 3.6D, true, EssenceType.STORM, EssenceType.WATER, 22, potencyTier
        );
        AABB depBox = new AABB(
                departurePos.x - 3.6D, departurePos.y - 1.5D, departurePos.z - 3.6D,
                departurePos.x + 3.6D, departurePos.y + 2.5D, departurePos.z + 3.6D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, LivingEntity::isAlive)) {
            if (foe == caster || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.clearFire();
            SpellLingeringZoneManager.applySoaked(foe, 160);
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 toDep = departurePos.subtract(foe.position());
                Vec3 pull = toDep.lengthSqr() > 1.0E-4D ? toDep.normalize().scale(0.85D) : Vec3.ZERO;
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(pull.x, 0.28D, pull.z));
            }
        }
        level.sendParticles(ParticleTypes.SPLASH, departurePos.x, departurePos.y + 0.8D, departurePos.z,
                28, 0.55D, 0.45D, 0.55D, 0.10D);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.78F, 1.20F);

        // Exact +40% movement speed boost on arrival!
        int boostTicks = 100 + potencyTier * 20;
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 0.0D, 0.0D, 0.40D, boostTicks);
        level.sendParticles(ParticleTypes.GUST, arrivalPos.x, arrivalPos.y + 0.6D, arrivalPos.z,
                3, 0.35D, 0.30D, 0.35D, 0.02D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.75F, 1.32F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.15 Aero-Hydraulic Erosion:
        // Washes Dirt -> Mud -> Clay -> Gravel -> Sand, and cools Lava into Obsidian (strictly container-safe)!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        if (state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.FARMLAND)) {
            level.setBlockAndUpdate(pos, Blocks.MUD.defaultBlockState());
            emitHydraulicErosionVfx(level, pos, false);
            return true;
        }
        if (state.is(Blocks.MUD) || state.is(Blocks.PACKED_MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            emitHydraulicErosionVfx(level, pos, false);
            return true;
        }
        if (state.is(Blocks.CLAY) || state.is(Blocks.TERRACOTTA)) {
            level.setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
            emitHydraulicErosionVfx(level, pos, false);
            return true;
        }
        if (state.is(Blocks.GRAVEL) || state.is(Blocks.SANDSTONE) || state.is(Blocks.CHISELED_SANDSTONE)
                || state.is(Blocks.SMOOTH_SANDSTONE)) {
            level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
            emitHydraulicErosionVfx(level, pos, false);
            return true;
        }
        if (state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.SMOOTH_RED_SANDSTONE)) {
            level.setBlockAndUpdate(pos, Blocks.RED_SAND.defaultBlockState());
            emitHydraulicErosionVfx(level, pos, false);
            return true;
        }
        if (state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            emitHydraulicErosionVfx(level, pos, true);
            return true;
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

        if (in.is(Items.DIRT) || in.is(Items.GRASS_BLOCK) || in.is(Items.COARSE_DIRT)
                || in.is(Items.ROOTED_DIRT) || in.is(Items.PODZOL) || in.is(Items.MYCELIUM)) {
            itemEntity.setItem(new ItemStack(Items.MUD, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHydraulicErosionVfx(level, itemEntity.blockPosition(), false);
            return true;
        }
        if (in.is(Items.MUD) || in.is(Items.PACKED_MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHydraulicErosionVfx(level, itemEntity.blockPosition(), false);
            return true;
        }
        if (in.is(Items.CLAY) || in.is(Items.TERRACOTTA)) {
            itemEntity.setItem(new ItemStack(Items.GRAVEL, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHydraulicErosionVfx(level, itemEntity.blockPosition(), false);
            return true;
        }
        if (in.is(Items.GRAVEL) || in.is(Items.SANDSTONE)) {
            itemEntity.setItem(new ItemStack(Items.SAND, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHydraulicErosionVfx(level, itemEntity.blockPosition(), false);
            return true;
        }
        if (in.is(Items.LAVA_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.OBSIDIAN, count));
            Block.popResource(level, itemEntity.blockPosition(), new ItemStack(Items.BUCKET, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHydraulicErosionVfx(level, itemEntity.blockPosition(), true);
            return true;
        }
        if (in.is(Items.SPONGE)) {
            itemEntity.setItem(new ItemStack(Items.WET_SPONGE, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHydraulicErosionVfx(level, itemEntity.blockPosition(), false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.15 Waterlogged Hypoxia Hex:
        // - Persistent milk-immune atmospheric deluge curse (cleansed via Restoration magic, Fire/Heat reactions, or caster defeat)
        // - Continuously inflicts drowning damage on land
        // - Permanently cuts attack speed by an exact 40% (on both players via attribute & mobs via 33t swing gate)
        // - Doubles incoming Lightning damage (enforced in SpellCombatReactionEngine)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.clearFire();
        SpellLingeringZoneManager.applySoaked(target, 50);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);

        if (!target.isInWater()) {
            target.setAirSupply(Math.max(-20, target.getAirSupply() - 30));
        }
        target.hurtServer(level, level.damageSources().drown(), (1.6F + potencyTier * 0.55F) * mult);

        level.sendParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getY(0.85D), target.getZ(),
                8, 0.25D, 0.30D, 0.25D, 0.03D);
        level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.85D), target.getZ(),
                6, 0.25D, 0.30D, 0.25D, 0.05D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_HAGALAZ.get(), TEMPEST_ZEPHYR_HEX_COLOR, 24, 0.90F, 0.04F),
                target.getX(), target.getY(0.95D), target.getZ(), 2, 0.22D, 0.25D, 0.22D, 0.01D
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
        if (effect != SpellCoreEffect.KINETIC) {
            return;
        }
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D
                ? direction.normalize()
                : (caster != null ? caster.getLookAngle().normalize() : new Vec3(0.0D, 0.0D, 1.0D));
        if (target == caster) {
            double selfMult = SpellLingeringZoneManager.isEntityWetOrSoaked(caster) ? 1.6D : 1.15D;
            SpellExecutionEngine.applySyncedMotion(caster, new Vec3(
                    normDir.x * 1.65D * selfMult, Math.max(0.45D, normDir.y * 0.9D + 0.28D), normDir.z * 1.65D * selfMult
            ));
            caster.resetFallDistance();
            level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.15F);
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                target, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        applyHurricaneForcePushToEntity(level, caster, target, normDir, potencyTier);
    }

    /**
     * Called from {@code SpellExecutionEngine.executeSingleEffectOnBlock} when a Storm-mutated {@code KINETIC}
     * spell hits terrain, blasting nearby mobs 14 blocks (2x knockback if wet or swimming)!
     */
    public static void unleashHurricaneForcePushAt(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin,
            Vec3 lookDir,
            int potencyTier,
            SpellAssembly spell
    ) {
        double radius = 4.0D;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        SpellWorldVfxManager.broadcastRadialNova(level, origin.add(0.0D, 0.08D, 0.0D), radius, EssenceType.STORM, EssenceType.AIR, potencyTier);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, origin.x, origin.y + 0.5D, origin.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, BlockPos.containing(origin), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 0.92F);

        AABB box = new AABB(
                origin.x - radius, origin.y - 1.5D, origin.z - radius,
                origin.x + radius, origin.y + 3.2D, origin.z + radius
        );
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (living == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, wardSafe)) {
                continue;
            }
            Vec3 away = living.position().subtract(origin);
            Vec3 dir = away.lengthSqr() > 1.0E-4D ? away.normalize() : lookDir.normalize();
            applyHurricaneForcePushToEntity(level, caster, living, dir, potencyTier);
        }
    }

    private static void applyHurricaneForcePushToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 normDir,
            int potencyTier
    ) {
        if (SpellCombatReactionEngine.isBossEntity(level, target)) {
            SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(
                    level, target, caster, 4.8F + potencyTier * 2.1F, false, true
            );
            return;
        }
        // Section 6.15 Hurricane Force Push:
        // 14-block gale-force push (horiz 2.95D + 0.48D lift), with 2.0x knockback against wet or swimming targets!
        double wetMult = SpellLingeringZoneManager.isEntityWetOrSoaked(target) ? 2.0D : 1.0D;
        Vec3 horiz = new Vec3(normDir.x, 0.0D, normDir.z);
        Vec3 pushDir = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : normDir;
        double pushSpeed = 2.95D * wetMult;
        double liftY = Math.min(0.82D, 0.48D * (wetMult > 1.0D ? 1.35D : 1.0D));

        SpellExecutionEngine.applySyncedMotion(target, new Vec3(pushDir.x * pushSpeed, liftY, pushDir.z * pushSpeed));
        SpellWorldVfxManager.broadcastVortexCone(
                level, target.position().add(0.0D, 0.7D, 0.0D), pushDir, 4.5D * wetMult, EssenceType.STORM, EssenceType.AIR, potencyTier
        );
        level.sendParticles(ParticleTypes.GUST, target.getX(), target.getY(0.6D), target.getZ(),
                5, 0.30D, 0.25D, 0.30D, 0.04D);
        level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.6D), target.getZ(),
                18, 0.35D, 0.30D, 0.35D, 0.10D);
        level.playSound(null, target.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.90F, 0.95F);
    }

    // =========================================================================
    // INTERNAL SHARED STORM HELPERS
    // =========================================================================

    private static void extinguishFiresAround(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            }
        }
    }

    private static void emitHydraulicErosionVfx(ServerLevel level, BlockPos pos, boolean cooledLava) {
        level.sendParticles(ParticleTypes.SPLASH,
                pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                14, 0.28D, 0.28D, 0.28D, 0.08D);
        if (cooledLava) {
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + 0.5D, pos.getY() + 0.70D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.03D);
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.75F, 1.10F);
        } else {
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.55F, 1.30F);
        }
    }

    private static void refundSubLinearBatchFuelDiscount(@Nullable LivingEntity caster, int tier, int stackCount) {
        if (!(caster instanceof Player player) || stackCount <= 1) return;
        float discountFrac = Math.min(0.30F, 0.04F * (stackCount - 1));
        long refundMfum = Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * discountFrac);
        if (refundMfum > 0L) {
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.STORM, refundMfum);
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
