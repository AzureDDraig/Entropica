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
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Overgrowth Materia (Tier 1 Elemental Fusion #16: Nature + Earth)</b>
 * (Spellcrafting System Guide Sections 1.2, 2.2, 4.1–4.9, 5.1–5.7 &amp; 6.17).
 *
 * <p>Canonical Advantage: <b>Strong against WATER (+1) and EARTH (+1)</b> |
 * <b>Weak against NETHER (-1), FROZEN (-1), UMBRAL (-1), and AIR (-1)</b>
 * (Dominance: {@code overgrowth_earth_dom} -&gt; Strong against Void, Weak against Nether, Frozen, Umbral &amp; Air,
 * computed automatically by {@code VisInteractionManager};
 * Tier 1 Elemental Fusion: {@code 10%} base physical armor bypass &amp; {@code 1} base armor shred point).</p>
 */
public final class OvergrowthSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SYLVAN_GOLIATH_HEX_COLOR = 0x1A5923;
    public static final int VERDANT_BLOOM_HEX_COLOR = 0x2CE668;
    public static final int EARTHEN_ROOT_HEX_COLOR = 0x773214;

    @Override
    public EssenceType materiaType() {
        return EssenceType.OVERGROWTH;
    }

    // =========================================================================
    // CANONICAL TITLES & DESCRIPTIONS (GUIDE SECTION 6.17)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Entangling Briar Chains";
            default -> "Verdant Overgrowth " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP ->
                    "Leaping chains jump along thick thorny wooden vines; each leap tethers consecutive victims to each other within 5 blocks, dragging them together and rooting them for 1.5 seconds.";
            default ->
                    "Channels the spell along dense jungle-verdant (#1A5923) briar vines and fertile earthen currents.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Verdant Briar-Spike Seed";
            case BEAM -> "Verdant Growth Tether Ray";
            case RADIAL_BURST -> "Bramble Rampart Bloom";
            case TOUCH_STRIKE -> "Botanical Ground Slam";
            case AURA -> "Earth-Root Bastion";
            case GLYPH -> "Strangler Briar Cage Mine";
            case CONE -> "Bramble Flurry & Phyto-Wave";
            case SKYFALL -> "Phyto-Canopy Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Fires an explosive seed pod wrapped in earthen thorns (#1A5923); on hit, erupts into a dense bramble cage that roots the target in place for 2.5 seconds and spreads moss and tall grass onto nearby floor blocks.";
            case BEAM ->
                    "Channels a sustained emerald beam wrapped in root tendrils (#1A5923); drags struck mobs in clinging roots (cutting speed by 60%), while rapidly accelerating the growth of all crops, saplings, and flora passed over by the ray.";
            case RADIAL_BURST ->
                    "Detonates in an explosive 360-degree eruption of stone spires and thorny brambles (6-block radius); knocks enemies back, roots grounded hostiles in briars for 2.5 seconds, and carpets floor blocks in fertile moss and wildflowers.";
            case TOUCH_STRIKE ->
                    "Strikes with crushing vegetative force, knocking the victim flat onto the floor and causing dense floral roots to erupt beneath them.";
            case AURA ->
                    "Standing still for 2 seconds roots the caster into the earth, granting +8 Armor and regenerating 1 HP per second until movement resumes.";
            case GLYPH ->
                    "When tripped, dense living roots and stone spires violently erupt in a 4-block radius; traps entering mobs inside an entangled wooden root cage for 4 seconds, immobilizing them with continuous strangling damage (disarmable via Sneak-Right-Click).";
            case CONE ->
                    "Sprays a wide 60-degree cone of sharp wooden thorns and fertile earth; knocks mobs back, roots enemies for 1.5 seconds, carpets ground in lush moss, and instantly harvests and replants all mature crops in the cone.";
            case SKYFALL ->
                    "Summons a 16-block verdant cloud for 8 seconds that supercharges crop growth, instantly matures saplings into trees, and heals friendly summoned constructs.";
            case SUMMON_AGENT ->
                    "Summons a dense jungle-verdant (#1A5923) Sylvan-Goliath Agent whose heavy ground-smashing punches root enemies in briars.";
            case SUMMON_SENTRY ->
                    "Anchors a Living Root Totem (#1A5923) firing piercing thorn darts while pulsing crop growth across 6 blocks.";
            case SUMMON_DECOY ->
                    "Conjures an Overgrowth Floral Effigy (#1A5923) that taunts mobs and detonates into a 4-block root snare upon destruction.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Tree-Stepping";
            case TRANSMUTE, BREAK -> "Botanical Synthesis";
            case AFFLICTION -> "Strangling Phyto-Bramble Hex";
            case RESTORATION -> "Photosynthesis Mending";
            default -> "Overgrowth " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports between trees and foliage (24m+ range & tree phasing), leaving an entangling flower bush at departure.";
            case TRANSMUTE, BREAK ->
                    "Turns stone to mossy stone, dirt to podzol and moss, and matures crops.";
            case AFFLICTION ->
                    "Persistent milk-immune parasitic curse (burned away via Fire, cleansed via Restoration magic, or caster defeat): permanently silences spellcasting and mob special abilities, shreds 35% armor toughness, and continuously sprouts grasping brambles beneath their feet when moving.";
            case RESTORATION ->
                    "Grants regenerative sap healing that doubles under direct sunlight.";
            default ->
                    "Infuses the spell effect with Tier 1 Fusion Overgrowth Materia (+1 vs Water & Earth, -1 vs Nether, Frozen, Umbral & Air, 10% armor bypass).";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — ENTANGLING BRIAR CHAINS (CHAIN_LEAP)
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
        // Section 6.17 Entangling Briar Chains:
        // Leaping chains jump along thick thorny wooden vines; each leap tethers consecutive victims
        // to each other within 5 blocks, dragging them together and rooting them for 1.5 seconds (30t)!
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcastChainArc(
                level,
                fromTarget.getBoundingBox().getCenter(),
                toTarget.getBoundingBox().getCenter(),
                EssenceType.OVERGROWTH,
                EssenceType.EARTH,
                tier
        );

        double initialDist = fromTarget.distanceTo(toTarget);
        int initialSkid = initialDist > 5.0D ? 6 : 0;
        if (initialDist > 5.0D) {
            pullPairTowardMidpoint(level, fromTarget, toTarget, 0.72D);
        }
        applyBriarStoneRoot(level, fromTarget, 30, initialSkid);
        applyBriarStoneRoot(level, toTarget, 30, initialSkid);

        Vec3 midpoint = fromTarget.position().add(toTarget.position()).scale(0.5D);
        final UUID idA = fromTarget.getUUID();
        final UUID idB = toTarget.getUUID();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, midpoint, 6.0D, 100, level.getGameTime(), spell, slot, EssenceType.OVERGROWTH, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    Entity eA = srv.getEntity(idA);
                    Entity eB = srv.getEntity(idB);
                    if (!(eA instanceof LivingEntity lA) || !(eB instanceof LivingEntity lB) || !lA.isAlive() || !lB.isAlive()) {
                        return false;
                    }
                    if (elapsed % 10 == 0) {
                        SpellWorldVfxManager.broadcastChainArc(
                                srv, lA.getBoundingBox().getCenter(), lB.getBoundingBox().getCenter(),
                                EssenceType.OVERGROWTH, EssenceType.EARTH, tier
                        );
                    }
                    if (lA.distanceTo(lB) > 5.0D) {
                        pullPairTowardMidpoint(srv, lA, lB, 0.58D);
                    }
                    return true;
                }
        ));
    }

    private static void pullPairTowardMidpoint(ServerLevel level, LivingEntity a, LivingEntity b, double speed) {
        Vec3 mid = a.position().add(b.position()).scale(0.5D);
        if (!SpellCombatReactionEngine.isBossEntity(level, a)) {
            Vec3 dA = mid.subtract(a.position());
            Vec3 hA = new Vec3(dA.x, 0.0D, dA.z);
            if (hA.lengthSqr() > 1.0E-4D) {
                Vec3 pA = hA.normalize().scale(speed);
                SpellExecutionEngine.applySyncedMotion(a, new Vec3(pA.x, 0.14D, pA.z));
            }
        }
        if (!SpellCombatReactionEngine.isBossEntity(level, b)) {
            Vec3 dB = mid.subtract(b.position());
            Vec3 hB = new Vec3(dB.x, 0.0D, dB.z);
            if (hB.lengthSqr() > 1.0E-4D) {
                Vec3 pB = hB.normalize().scale(speed);
                SpellExecutionEngine.applySyncedMotion(b, new Vec3(pB.x, 0.14D, pB.z));
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — VERDANT BRIAR-SPIKE SEED (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        level.playSound(null, projectile.blockPosition(), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.75F, 1.22F);
        level.playSound(null, projectile.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.55F, 1.28F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 18, 0.90F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.07D, 0.07D, 0.07D, 0.01D
            );
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
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
        // Section 6.17 Verdant Briar-Spike Seed:
        // Explosive seed pod wrapped in earthen thorns (#1A5923); on hit, erupts into a dense 3D bramble cage
        // that roots the target in place for 2.5 seconds (50t) and spreads moss and tall grass onto nearby floor blocks!
        int tier = spell.getEffectiveTier();
        if (hitEntity instanceof LivingEntity living
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float podDmg = (3.4F + tier * 0.95F) * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(level, living, caster, podDmg, false, true);
            }
            applyBriarStoneRoot(level, living, 50, 0);
            double cageRad = Math.max(1.35D, living.getBbWidth() * 1.45D);
            SpellWorldVfxManager.broadcastWardShell(level, living.position(), cageRad, EssenceType.OVERGROWTH, EssenceType.EARTH, 54, tier);
        }

        Vec3 hitPos = hitEntity.getBoundingBox().getCenter();
        SpellWorldVfxManager.broadcastProjectileImpact(level, hitPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.OVERGROWTH, EssenceType.EARTH, 1.45F, tier);
        spreadMossAndGrass(level, BlockPos.containing(projectile.position()), 2, true);
        level.playSound(null, BlockPos.containing(hitPos), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.90F, 0.85F);
        level.playSound(null, BlockPos.containing(hitPos), SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.85F, 1.05F);
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
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, loc, Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i()),
                EssenceType.OVERGROWTH, EssenceType.EARTH, 1.55F, tier
        );
        SpellWorldVfxManager.broadcastWardShell(level, loc, 1.75D, EssenceType.OVERGROWTH, EssenceType.EARTH, 46, tier);

        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(loc, loc).inflate(1.85D), LivingEntity::isAlive)) {
            if (foe == caster || (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe))) {
                continue;
            }
            applyBriarStoneRoot(level, foe, 50, 0);
        }
        spreadMossAndGrass(level, blockHit.getBlockPos(), 2, true);
        level.playSound(null, blockHit.getBlockPos(), SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.85F, 0.95F);
    }

    // =========================================================================
    // FORM MUTATION 2 — VERDANT GROWTH TETHER RAY (BEAM)
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
        // Section 6.17 Verdant Growth Tether Ray:
        // Channels a sustained emerald beam wrapped in root tendrils (#1A5923); drags struck mobs in clinging roots
        // (cutting speed by exact 60%), while rapidly accelerating the growth of all crops, saplings, and flora passed over by the ray!
        int tier = spell.getEffectiveTier();
        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        double effectiveReach = maxReach;
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            effectiveReach = Math.min(maxReach, eye.distanceTo(blockHit.getLocation()) + 0.65D);
        }

        for (double d = 1.0D; d <= effectiveReach; d += 0.90D) {
            BlockPos bp = BlockPos.containing(eye.add(normDir.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-1, -1, -1), bp.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                    level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                } else if (bs.getBlock() instanceof SaplingBlock sapling) {
                    sapling.advanceTree(level, p.immutable(), bs, level.random);
                } else if (bs.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, p, bs)) {
                    growable.performBonemeal(level, level.random, p.immutable(), bs);
                }
            }
        }

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float rayDmg = (1.25F + tier * 0.38F) * Math.max(0.35F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(level, hit, caster, rayDmg, false, true);
            }
            // Exact -60% movement speed + clinging root drag toward the caster!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.60D, 40L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3));
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                Vec3 toCaster = caster.position().subtract(hit.position());
                Vec3 horiz = new Vec3(toCaster.x, 0.0D, toCaster.z);
                if (horiz.lengthSqr() > 2.25D) {
                    Vec3 drag = horiz.normalize().scale(0.38D);
                    Vec3 cur = hit.getDeltaMovement();
                    SpellExecutionEngine.applySyncedMotion(hit, new Vec3(cur.x * 0.4D + drag.x, cur.y, cur.z * 0.4D + drag.z));
                }
            }
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 18, 0.90F, 0.05F),
                    hit.getX(), hit.getY(0.55D), hit.getZ(), 3, 0.25D, 0.30D, 0.25D, 0.02D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — BRAMBLE RAMPART BLOOM (RADIAL BURST)
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
        // Section 6.17 Bramble Rampart Bloom:
        // Detonates in an explosive 360-degree eruption of stone spires and thorny brambles (6-block radius);
        // knocks enemies back, roots grounded hostiles in briars for 2.5 seconds (50t),
        // and carpets floor blocks in fertile moss and wildflowers!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float novaDmg = MateriaStageTable.getBaseCombatDamageHp(tier)
                * Math.max(0.45F, apparatusPotency) * 0.60F * spell.flawProfile().getPotencyMultiplier();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.OVERGROWTH, EssenceType.EARTH, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = foe.getX() - center.x;
            double dz = foe.getZ() - center.z;
            if (foe == caster || dx * dx + dz * dz > (r + 0.5D) * (r + 0.5D)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            boolean wasGrounded = isEntityGrounded(level, foe);
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(away.x * 1.05D, 0.30D, away.z * 1.05D));
            }
            if (wasGrounded) {
                // 6 skid ticks let the outward knockback travel cleanly before briars lock them fast for 2.5s (50t)!
                applyBriarStoneRoot(level, foe, 50, 6);
            }
        }

        spreadMossAndWildflowers(level, BlockPos.containing(center), (int) Math.min(6, Math.ceil(r)));

        // Raise 8 temporary 2-block Mossy Stone & Mangrove-Root Rampart Spires around the perimeter for 5.0s (100t)!
        List<BlockPos> raisedSpires = new ArrayList<>();
        BlockPos cBase = BlockPos.containing(center);
        double spireRingRad = Math.min(4.5D, r * 0.78D);
        for (int i = 0; i < 8; i++) {
            double ang = (Math.PI * 2.0D * i) / 8.0D;
            BlockPos col = cBase.offset((int) Math.round(Math.cos(ang) * spireRingRad), 0, (int) Math.round(Math.sin(ang) * spireRingRad));
            BlockPos ground = findGroundSurfaceNear(level, col);
            BlockPos spireBase = ground.above().immutable();
            if ((level.getBlockState(spireBase).isAir() || level.getBlockState(spireBase).is(Blocks.MOSS_CARPET))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, spireBase, level.getBlockState(spireBase))) {
                Block baseBlock = (i % 2 == 0) ? Blocks.MOSSY_COBBLESTONE : Blocks.MANGROVE_ROOTS;
                level.setBlockAndUpdate(spireBase, baseBlock.defaultBlockState());
                raisedSpires.add(spireBase);
                BlockPos topPos = spireBase.above().immutable();
                if (level.getBlockState(topPos).isAir()) {
                    level.setBlockAndUpdate(topPos, Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState());
                    raisedSpires.add(topPos);
                }
            }
        }
        if (!raisedSpires.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, r, 100, level.getGameTime(), null, null, EssenceType.OVERGROWTH, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (BlockPos sp : raisedSpires) {
                            BlockState cur = srv.getBlockState(sp);
                            if (cur.is(Blocks.MOSSY_COBBLESTONE)
                                    || cur.is(Blocks.MANGROVE_ROOTS)
                                    || cur.is(Blocks.MOSSY_COBBLESTONE_WALL)) {
                                srv.removeBlock(sp, false);
                            }
                        }
                        return false;
                    }
            ));
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 34, 1.15F, 0.09F),
                center.x, center.y + 0.85D, center.z, 34, r * 0.42D, 0.55D, r * 0.42D, 0.04D
        );
        level.playSound(null, cBase, SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 1.0F, 0.85F);
        level.playSound(null, cBase, SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.90F, 0.78F);
    }

    // =========================================================================
    // FORM MUTATION 4 — BOTANICAL GROUND SLAM (TOUCH / STRIKE)
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
        // Section 6.17 Botanical Ground Slam:
        // Strikes with crushing vegetative force, knocking the victim flat onto the floor
        // and causing dense floral roots to erupt beneath them!
        int tier = spell.getEffectiveTier();
        boolean cleaveSplash = apparatusPotency < 0.75F;
        int rootTicks = cleaveSplash ? 25 : 45;
        int flatTicks = cleaveSplash ? 14 : 28;
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        if (!cleaveSplash) {
            SpellWorldVfxManager.broadcastCrescentSlash(
                    level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D),
                    dir, 2.2D, EssenceType.OVERGROWTH, EssenceType.EARTH, tier
            );
        }

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float slamDmg = (3.4F + tier * 0.92F) * apparatusPotency * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(level, hit, caster, slamDmg, true, true);
            }
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), flatTicks, 0));
            applyBriarStoneRoot(level, hit, rootTicks, 0);
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(0.0D, -0.80D, 0.0D));
            }

            SpellWorldVfxManager.broadcastArcaneSeal(
                    level, hit.position().add(0.0D, 0.04D, 0.0D), 1.75D, EssenceType.OVERGROWTH, EssenceType.EARTH, 46, tier
            );
            SpellWorldVfxManager.broadcastWardShell(
                    level, hit.position(), Math.max(1.25D, hit.getBbWidth() * 1.35D), EssenceType.OVERGROWTH, EssenceType.EARTH, 42, tier
            );
            spreadMossAndWildflowers(level, hit.blockPosition(), 1);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                    hit.getX(), hit.getY(0.2D), hit.getZ(), 14, 0.32D, 0.18D, 0.32D, 0.12D);
            level.playSound(null, hit.blockPosition(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.85F, 0.76F);
            level.playSound(null, hit.blockPosition(), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.85F, 0.90F);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — EARTH-ROOT BASTION (AURA / SELF & ANCHORED)
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
        // Section 6.17 Earth-Root Bastion:
        // Standing still for 2 seconds (40t) roots the caster into the earth, granting +8 Armor
        // and regenerating 1 HP per second until movement resumes!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long now = level.getGameTime();

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BASTION_UNTIL, now + dur);
        data.putDouble(SpellLingeringZoneManager.TAG_OVERGROWTH_LAST_X, caster.getX());
        data.putDouble(SpellLingeringZoneManager.TAG_OVERGROWTH_LAST_Z, caster.getZ());
        data.putInt("SpellWardTier", tier);

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.78F, null);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, caster.position().add(0.0D, 0.04D, 0.0D), 2.5D, EssenceType.OVERGROWTH, EssenceType.EARTH, 52, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 28, 1.0F, 0.06F),
                caster.getX(), caster.getY(0.65D), caster.getZ(), 18, 0.45D, 0.45D, 0.45D, 0.03D
        );
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY(0.8D), caster.getZ(),
                18, 0.45D, 0.5D, 0.45D, 0.05D);
        level.playSound(null, caster.blockPosition(), SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.90F, 0.88F);
    }

    // =========================================================================
    // FORM MUTATION 6 — STRANGLER BRIAR CAGE MINE (GLYPH / TRAP)
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
        // Section 4.6 & 6.17 Strangler Briar Cage Mine:
        // - Surface-snapped disarmable trap (1.0s arming, Single or Repeating mode, 50% Sneak-Right-Click disarm refund)
        // - When tripped, dense living roots and stone spires violently erupt in a 4-block radius;
        //   traps entering mobs inside an entangled wooden root cage for 4 seconds (80t),
        //   immobilizing them with continuous strangling damage!
        int tier = spell.getEffectiveTier();
        double cageRad = Math.max(4.0D, radius);
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceFloor = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(surfaceFloor.getX() + 0.5D, surfaceFloor.getY() + 1.02D, surfaceFloor.getZ() + 0.5D);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxCharges = repeating ? 3 : 1;
        int[] chargesUsed = {0};
        int[] rearmAtElapsed = {20};
        List<BlockPos> activeCageBars = new ArrayList<>();

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, 2.8D, EssenceType.OVERGROWTH, EssenceType.EARTH, 60, tier);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_BERKANO.get(), VERDANT_BLOOM_HEX_COLOR, 36, 0.95F, 0.04F),
                snappedCenter.x, snappedCenter.y + 0.08D, snappedCenter.z, 5, 0.50D, 0.05D, 0.50D, 0.005D
        );
        level.playSound(null, surfaceFloor.above(), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.80F, 0.95F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, cageRad, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        clearTemporaryCageBars(srv, activeCageBars);
                        return false;
                    }
                    if (elapsed % 24 == 0) {
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                zone.center.x, zone.center.y + 0.10D, zone.center.z, 4, 0.8D, 0.08D, 0.8D, 0.01D);
                    }
                    if (elapsed < rearmAtElapsed[0]) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - cageRad * 0.82D, zone.center.y - 1.0D, zone.center.z - cageRad * 0.82D,
                            zone.center.x + cageRad * 0.82D, zone.center.y + 2.4D, zone.center.z + cageRad * 0.82D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (stepped.isEmpty()) return true;

                    float chargeScale = repeating ? 0.60F : 1.0F;

                    // Erupt 3D 12-root Entangled Wooden Root Dome Cage + 8 physical root/stone pillars around the 4m perimeter!
                    SpellWorldVfxManager.broadcastWardShell(srv, zone.center, cageRad, EssenceType.OVERGROWTH, EssenceType.EARTH, 86, tier);
                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, cageRad, EssenceType.OVERGROWTH, EssenceType.EARTH, 88, tier);

                    List<BlockPos> batchBars = new ArrayList<>();
                    BlockPos cageOrigin = BlockPos.containing(zone.center);
                    for (int i = 0; i < 8; i++) {
                        double ang = (Math.PI * 2.0D * i) / 8.0D;
                        BlockPos col = cageOrigin.offset((int) Math.round(Math.cos(ang) * (cageRad - 0.6D)), 0, (int) Math.round(Math.sin(ang) * (cageRad - 0.6D)));
                        BlockPos floor = findGroundSurfaceNear(srv, col);
                        BlockPos barBase = floor.above().immutable();
                        if (srv.getBlockState(barBase).isAir()
                                && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, barBase, srv.getBlockState(barBase))) {
                            Block barType = (i % 2 == 0) ? Blocks.MANGROVE_ROOTS : Blocks.MOSSY_COBBLESTONE_WALL;
                            srv.setBlockAndUpdate(barBase, barType.defaultBlockState());
                            batchBars.add(barBase);
                            activeCageBars.add(barBase);
                            BlockPos upper = barBase.above().immutable();
                            if (srv.getBlockState(upper).isAir()) {
                                srv.setBlockAndUpdate(upper, Blocks.OAK_FENCE.defaultBlockState());
                                batchBars.add(upper);
                                activeCageBars.add(upper);
                            }
                        }
                    }

                    srv.playSound(null, cageOrigin, SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.15F, 0.68F);
                    srv.playSound(null, cageOrigin, SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 1.0F, 0.78F);

                    // Trap & strangle entering mobs inside the 4m cage for exact 4.0 seconds (80 ticks)!
                    int cageTicks = 80;
                    AABB cageBox = new AABB(
                            zone.center.x - cageRad, zone.center.y - 1.2D, zone.center.z - cageRad,
                            zone.center.x + cageRad, zone.center.y + 3.2D, zone.center.z + cageRad
                    );
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, cageRad, cageTicks, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * chargeScale,
                            (csrv, czone, celapsed) -> {
                                if (celapsed >= czone.maxDurationTicks) {
                                    clearTemporaryCageBars(csrv, batchBars);
                                    activeCageBars.removeAll(batchBars);
                                    return false;
                                }
                                LivingEntity cOwner = czone.resolveCaster(csrv);
                                for (LivingEntity v : csrv.getEntitiesOfClass(LivingEntity.class, cageBox, e -> e.isAlive() && e != cOwner)) {
                                    if (v.position().distanceTo(czone.center) > cageRad + 0.5D) continue;
                                    if (cOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, cOwner, wardSafe)) {
                                        continue;
                                    }
                                    if (v.position().distanceTo(czone.center) > cageRad * 0.76D && !SpellCombatReactionEngine.isBossEntity(csrv, v)) {
                                        Vec3 inward = czone.center.subtract(v.position()).normalize().scale(0.50D);
                                        SpellExecutionEngine.applySyncedMotion(v, new Vec3(inward.x, 0.05D, inward.z));
                                    }
                                    applyBriarStoneRoot(csrv, v, 24, 0);
                                    if (celapsed % 16 == 0) {
                                        float strangleDmg = (2.3F + tier * 0.65F) * chargeScale;
                                        SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(csrv, v, cOwner, strangleDmg, false, true);
                                        if (czone.spell != null && czone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    csrv, cOwner, v, Vec3.ZERO,
                                                    czone.spell, czone.slot, czone.fallbackMateria, czone.apparatusPotency * 0.35F
                                            );
                                        }
                                        csrv.sendParticles(
                                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 20, 0.90F, 0.06F),
                                                v.getX(), v.getY(0.65D), v.getZ(), 5, 0.28D, 0.30D, 0.28D, 0.02D
                                        );
                                    }
                                }
                                return true;
                            }
                    ));

                    chargesUsed[0]++;
                    if (chargesUsed[0] >= maxCharges) {
                        return false;
                    }
                    rearmAtElapsed[0] = elapsed + cageTicks + 30;
                    return true;
                }
        ).markDisarmableGlyph()
                .withOnDisarm((srv, z) -> clearTemporaryCageBars(srv, activeCageBars)));
        return true;
    }

    private static void clearTemporaryCageBars(ServerLevel level, List<BlockPos> bars) {
        for (BlockPos bp : bars) {
            BlockState cur = level.getBlockState(bp);
            if (cur.is(Blocks.MANGROVE_ROOTS) || cur.is(Blocks.MOSSY_COBBLESTONE_WALL) || cur.is(Blocks.OAK_FENCE)) {
                level.removeBlock(bp, false);
            }
        }
        bars.clear();
    }

    // =========================================================================
    // FORM MUTATION 7 — BRAMBLE FLURRY & PHYTO-WAVE (CONE / SPRAY)
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
        // Section 6.17 Bramble Flurry & Phyto-Wave:
        // Sprays a wide 60-degree cone of sharp wooden thorns and fertile earth; knocks mobs back,
        // roots enemies for 1.5 seconds (30t), carpets ground in lush moss, and instantly harvests
        // and replants all mature crops in the cone!
        int tier = spell.getEffectiveTier();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double reach = Math.max(7.5D, coneRange);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D),
                dir, EssenceType.OVERGROWTH, EssenceType.EARTH, (float) reach, 26, tier
        );

        float coneDmg = (3.0F + tier * 0.85F) * Math.max(0.40F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(dir.x * 0.95D, 0.28D, dir.z * 0.95D));
            }
            // 5 skid ticks let the thorn wave knock mobs back cleanly before rooting them fast for 1.5s (30t)!
            applyBriarStoneRoot(level, hit, 30, 5);
        }

        // Sweep the true 60-degree cone: harvest & replant all mature crops and carpet ground in lush moss!
        Vec3 eye = caster.getEyePosition();
        int rSteps = (int) Math.ceil(reach);
        BlockPos origin = caster.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-rSteps, -2, -rSteps), origin.offset(rSteps, 2, rSteps))) {
            Vec3 toBlock = Vec3.atCenterOf(p).subtract(eye);
            double d = toBlock.length();
            if (d < 0.8D || d > reach) continue;
            if (toBlock.scale(1.0D / d).dot(dir) < 0.866D) continue; // cos(30 deg) = 60-degree cone!

            BlockState bs = level.getBlockState(p);
            if (harvestAndReplantMatureCrop(level, caster, p, bs)) {
                continue;
            }
            if (bs.isAir()
                    && level.getBlockState(p.below()).isSolidRender()
                    && !(level.getBlockState(p.below()).getBlock() instanceof FarmBlock)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p.below(), level.getBlockState(p.below()))
                    && level.random.nextFloat() < 0.28F
                    && Blocks.MOSS_CARPET.defaultBlockState().canSurvive(level, p)) {
                level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
            }
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 28, 1.05F, 0.08F),
                caster.getX() + dir.x * 2.5D, caster.getEyeY() - 0.1D + dir.y * 2.5D, caster.getZ() + dir.z * 2.5D,
                26, 0.85D, 0.45D, 0.85D, 0.05D
        );
        level.playSound(null, origin, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.90F, 1.15F);
        level.playSound(null, origin, SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.85F, 1.05F);
    }

    private static boolean harvestAndReplantMatureCrop(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState bs) {
        if (bs.getBlock() instanceof CropBlock crop && crop.isMaxAge(bs)) {
            Block.dropResources(bs, level, pos, null, caster, ItemStack.EMPTY);
            level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.02D);
            return true;
        }
        if (bs.getBlock() instanceof NetherWartBlock && bs.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE) {
            Block.dropResources(bs, level, pos, null, caster, ItemStack.EMPTY);
            level.setBlock(pos, bs.setValue(NetherWartBlock.AGE, 0), Block.UPDATE_ALL);
            return true;
        }
        if (bs.getBlock() instanceof CocoaBlock && bs.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE) {
            Block.dropResources(bs, level, pos, null, caster, ItemStack.EMPTY);
            level.setBlock(pos, bs.setValue(CocoaBlock.AGE, 0), Block.UPDATE_ALL);
            return true;
        }
        if (bs.getBlock() instanceof SweetBerryBushBlock && bs.getValue(SweetBerryBushBlock.AGE) >= 2) {
            int berries = 1 + level.random.nextInt(2) + (bs.getValue(SweetBerryBushBlock.AGE) == 3 ? 1 : 0);
            Block.popResource(level, pos, new ItemStack(Items.SWEET_BERRIES, berries));
            level.setBlock(pos, bs.setValue(SweetBerryBushBlock.AGE, 1), Block.UPDATE_CLIENTS);
            return true;
        }
        return false;
    }

    // =========================================================================
    // FORM MUTATION 8 — PHYTO-CANOPY DOWNPOUR (RAIN / SKYFALL)
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
        // Section 6.17 Phyto-Canopy Downpour:
        // Summons a 16-block verdant cloud for 8 seconds (160t) that supercharges crop growth,
        // instantly matures saplings into trees, and heals friendly summoned constructs (100% cave-viable)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos groundFloor = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        Vec3 snappedCenter = new Vec3(impactCenter.x, groundFloor.getY() + 1.02D, impactCenter.z);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                snappedCenter, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.OVERGROWTH, EssenceType.EARTH, (float) r, 160, tier
        );
        level.playSound(null, groundFloor.above(), SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.PLAYERS, 1.0F, 0.90F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, r, 160, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), VERDANT_BLOOM_HEX_COLOR, 30, 1.05F, 0.06F),
                                zone.center.x, zone.center.y + 4.2D, zone.center.z, 12, zone.radius * 0.45D, 1.0D, zone.radius * 0.45D, 0.03D
                        );
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(12, (int) Math.ceil(zone.radius));
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);

                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -2, -br), cPos.offset(br, 2, br))) {
                            if (p.distSqr(cPos) > rSq) continue;
                            BlockState bs = srv.getBlockState(p);
                            if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                                srv.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                            } else if (bs.getBlock() instanceof SaplingBlock) {
                                // Instantly mature saplings straight into full trees!
                                for (int attempt = 0; attempt < 4; attempt++) {
                                    BlockState cur = srv.getBlockState(p);
                                    if (!(cur.getBlock() instanceof SaplingBlock sap)) break;
                                    sap.advanceTree(srv, p.immutable(), cur, srv.random);
                                }
                            } else if (bs.getBlock() instanceof BonemealableBlock grow
                                    && grow.isValidBonemealTarget(srv, p, bs)
                                    && srv.random.nextFloat() < 0.60F) {
                                grow.performBonemeal(srv, srv.random, p.immutable(), bs);
                            }
                        }

                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = e.getX() - zone.center.x;
                            double dz = e.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > rSq) continue;

                            boolean friendlyConstruct = (e instanceof SpellEidolonEntity eidolon && (owner == null || eidolon.isFriendlyTo(owner)))
                                    || e instanceof AbstractGolem
                                    || (e instanceof OwnableEntity pet && owner != null && pet.getOwner() == owner);
                            if (friendlyConstruct) {
                                float constructHeal = 5.0F + tier * 1.25F;
                                SpellCombatReactionEngine.notifyMagicalHeal(e, constructHeal);
                                e.heal(constructHeal);
                                srv.sendParticles(ParticleTypes.HEART, e.getX(), e.getY(0.95D), e.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                            } else if (owner != null && (e == owner || e.isAlliedTo(owner))) {
                                float allyHeal = 2.5F + tier * 0.50F;
                                SpellCombatReactionEngine.notifyMagicalHeal(e, allyHeal);
                                e.heal(allyHeal);
                            } else if (zone.spell != null && zone.slot != null
                                    && !zone.spell.prefix().enforcesZeroEntityHarm()
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, e, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                );
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — SYLVAN-GOLIATH EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.OVERGROWTH.name());
        tag.putInt("SpellEidolonTintRGB", SYLVAN_GOLIATH_HEX_COLOR);

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.04D, 0.0D), 2.2D, EssenceType.OVERGROWTH, EssenceType.EARTH, 52, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 28, 1.05F, 0.07F),
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.03D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.95F, 0.92F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT SYNERGIES (BLINK, TRANSMUTE/BREAK, HEX, RESTORATION)
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.17 Tree-Stepping:
        // Teleports between trees and foliage (24m+ range & tree trunk phasing handled in SpellExecutionEngine),
        // leaving an entangling flower bush at departure that snares pursuers!
        BlockPos dep = BlockPos.containing(departurePos);
        BlockPos bushPos = null;
        for (BlockPos cand : List.of(dep, dep.below(), dep.above())) {
            BlockState at = level.getBlockState(cand);
            BlockState under = level.getBlockState(cand.below());
            if ((at.isAir() || at.is(Blocks.MOSS_CARPET))
                    && !under.getCollisionShape(level, cand.below()).isEmpty()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, cand, at)) {
                bushPos = cand.immutable();
                break;
            }
        }

        final BlockPos finalBush = bushPos;
        if (finalBush != null) {
            level.setBlockAndUpdate(finalBush, Blocks.FLOWERING_AZALEA.defaultBlockState());
        }

        AABB snareBox = new AABB(
                departurePos.x - 2.8D, departurePos.y - 1.2D, departurePos.z - 2.8D,
                departurePos.x + 2.8D, departurePos.y + 2.2D, departurePos.z + 2.8D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, snareBox, LivingEntity::isAlive)) {
            if (foe != caster && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) {
                applyBriarStoneRoot(level, foe, 50, 0);
            }
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 2.8D, 100, level.getGameTime(), null, null, EssenceType.OVERGROWTH, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (finalBush != null && srv.getBlockState(finalBush).is(Blocks.FLOWERING_AZALEA)) {
                            srv.removeBlock(finalBush, false);
                        }
                        return false;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, snareBox, LivingEntity::isAlive)) {
                        if (foe != owner && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true))) {
                            applyBriarStoneRoot(srv, foe, 24, 0);
                        }
                    }
                    return true;
                }
        ));

        SpellWorldVfxManager.broadcastChainArc(
                level, departurePos.add(0.0D, 0.85D, 0.0D), arrivalPos.add(0.0D, 0.85D, 0.0D),
                EssenceType.OVERGROWTH, EssenceType.EARTH, potencyTier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, departurePos.add(0.0D, 0.03D, 0.0D), 2.1D, EssenceType.OVERGROWTH, EssenceType.EARTH, 60, potencyTier
        );
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, departurePos.x, departurePos.y + 0.8D, departurePos.z,
                18, 0.45D, 0.45D, 0.45D, 0.03D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AZALEA_PLACE, SoundSource.PLAYERS, 0.90F, 1.12F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.17 Botanical Synthesis:
        // Turns stone to mossy stone, dirt to podzol and moss, and matures crops!
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }

        BlockState result = null;
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            result = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        } else if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.CRACKED_STONE_BRICKS) || state.is(Blocks.INFESTED_STONE_BRICKS)) {
            result = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        } else if (state.is(Blocks.COBBLESTONE_WALL)) {
            result = Blocks.MOSSY_COBBLESTONE_WALL.withPropertiesOf(state);
        } else if (state.is(Blocks.STONE_BRICK_WALL)) {
            result = Blocks.MOSSY_STONE_BRICK_WALL.withPropertiesOf(state);
        } else if (state.is(Blocks.COBBLESTONE_SLAB)) {
            result = Blocks.MOSSY_COBBLESTONE_SLAB.withPropertiesOf(state);
        } else if (state.is(Blocks.STONE_BRICK_SLAB)) {
            result = Blocks.MOSSY_STONE_BRICK_SLAB.withPropertiesOf(state);
        } else if (state.is(Blocks.COBBLESTONE_STAIRS)) {
            result = Blocks.MOSSY_COBBLESTONE_STAIRS.withPropertiesOf(state);
        } else if (state.is(Blocks.STONE_BRICK_STAIRS)) {
            result = Blocks.MOSSY_STONE_BRICK_STAIRS.withPropertiesOf(state);
        } else if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MUD)) {
            result = (level.random.nextBoolean() ? Blocks.MOSS_BLOCK : Blocks.PODZOL).defaultBlockState();
        } else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            result = Blocks.DIRT.defaultBlockState();
        } else if (state.is(Blocks.DEAD_BUSH)) {
            result = Blocks.OAK_SAPLING.defaultBlockState();
        }

        if (result != null) {
            level.setBlockAndUpdate(pos, result);
            emitBotanicalTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }

        if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
            level.setBlock(pos, crop.getStateForAge(crop.getMaxAge()), Block.UPDATE_ALL);
            emitBotanicalTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }
        if (state.getBlock() instanceof SaplingBlock sapling) {
            for (int i = 0; i < 3; i++) {
                BlockState cur = level.getBlockState(pos);
                if (!(cur.getBlock() instanceof SaplingBlock sap)) break;
                sap.advanceTree(level, pos.immutable(), cur, level.random);
            }
            emitBotanicalTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }
        if (state.getBlock() instanceof BonemealableBlock grow && grow.isValidBonemealTarget(level, pos, state)) {
            grow.performBonemeal(level, level.random, pos, state);
            emitBotanicalTransmuteFx(level, caster, pos, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        int count = in.getCount();
        if (in.is(Items.COBBLESTONE) || in.is(Items.STONE) || in.is(Items.ANDESITE) || in.is(Items.DIORITE) || in.is(Items.GRANITE)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_COBBLESTONE, count));
            return true;
        }
        if (in.is(Items.STONE_BRICKS) || in.is(Items.CRACKED_STONE_BRICKS)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_STONE_BRICKS, count));
            return true;
        }
        if (in.is(Items.DIRT) || in.is(Items.COARSE_DIRT) || in.is(Items.ROOTED_DIRT)) {
            itemEntity.setItem(new ItemStack(in.is(Items.COARSE_DIRT) ? Items.PODZOL : Items.MOSS_BLOCK, count));
            return true;
        }
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.DIRT, count));
            return true;
        }
        if (in.is(Items.DEAD_BUSH)) {
            itemEntity.setItem(new ItemStack(Items.OAK_SAPLING, count));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.17 Strangling Phyto-Bramble Hex:
        // Persistent milk-immune parasitic curse until burned away via Fire, cleansed via Restoration, or caster defeat:
        // - Permanently silences spellcasting & mob special abilities (also enforced every 5t in SpellCombatReactionEngine)
        // - Permanently shreds 35% Armor Toughness
        // - Continuously sprouts grasping brambles beneath feet when moving (tracked every 4t in SpellLingeringZoneManager)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellLingeringZoneManager.applyArcaneSilence(target, 45);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.35D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.5D * mult, 0.0D, 45L);

        SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(
                level, target, null, (1.25F + potencyTier * 0.45F) * mult, false, true
        );

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), SYLVAN_GOLIATH_HEX_COLOR, 26, 0.95F, 0.05F),
                target.getX(), target.getY(0.65D), target.getZ(), 5, 0.28D, 0.35D, 0.28D, 0.02D
        );
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY(0.8D), target.getZ(),
                4, 0.25D, 0.3D, 0.25D, 0.02D);
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
        boolean ownedSummon = target instanceof SpellEidolonEntity eid && caster != null && eid.isOwnedBy(caster);
        boolean alliedTarget = target == caster || ownedSummon || (caster != null && target.isAlliedTo(caster));
        if (!spell.prefix().enforcesZeroEntityHarm() && !alliedTarget && target.getType().is(EntityTypeTags.UNDEAD)) {
            return;
        }

        // Section 6.17 Photosynthesis Mending:
        // Grants regenerative sap healing that DOUBLES (2.0x) under direct sunlight!
        boolean inDirectSun = level.dimensionType().hasSkyLight()
                && level.canSeeSky(target.blockPosition().above())
                && level.isBrightOutside()
                && !level.isRaining();

        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_PHOTOSYNTHESIS_UNTIL, level.getGameTime() + 160L);
        data.putInt("SpellPhotosynthesisTier", potencyTier);

        if (inDirectSun) {
            float sunBurstHeal = 3.5F + potencyTier * 1.15F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, sunBurstHeal);
            target.heal(sunBurstHeal);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 2, false, true));
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, true));
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), inDirectSun ? 0xFFD83D : VERDANT_BLOOM_HEX_COLOR, 28, 1.0F, 0.06F),
                target.getX(), target.getY(0.85D), target.getZ(), inDirectSun ? 20 : 12, 0.40D, 0.50D, 0.40D, 0.03D
        );
        level.playSound(null, target.blockPosition(), SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.PLAYERS, 0.85F, inDirectSun ? 1.32F : 1.08F);
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    public static void applyBriarStoneRoot(ServerLevel level, LivingEntity target, int rootTicks, int skidTicks) {
        if (!target.isAlive()) return;
        boolean boss = SpellCombatReactionEngine.isBossEntity(level, target);
        int effRoot = boss ? Math.min(20, rootTicks) : Math.max(10, rootTicks);
        int effSkid = boss ? 0 : Math.max(0, skidTicks);
        int total = effRoot + effSkid;

        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BRIAR_ROOT_UNTIL,
                Math.max(now + total, data.getLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BRIAR_ROOT_UNTIL).orElse(0L)));
        if (effSkid > 0) {
            data.putLong(SpellLingeringZoneManager.TAG_OVERGROWTH_BRIAR_SKID_UNTIL, now + effSkid);
        } else {
            data.remove(SpellLingeringZoneManager.TAG_OVERGROWTH_BRIAR_SKID_UNTIL);
            target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
            target.hasImpulse = true;
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL,
                    Math.max(now + total, data.getLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL).orElse(0L)));
        }

        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, total, 6, false, true));
        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), total, 0, false, false));
    }

    private static void emitBotanicalTransmuteFx(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, int tier) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), VERDANT_BLOOM_HEX_COLOR, 24, 0.95F, 0.05F),
                pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.02D
        );
        level.playSound(null, pos, SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.72F, 1.08F);
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            long prevTick = pData.getLong("SpellOvergrowthBatchTick").orElse(-20L);
            int count = (prevTick == now) ? pData.getInt("SpellOvergrowthBatchCount").orElse(0) + 1 : 1;
            pData.putLong("SpellOvergrowthBatchTick", now);
            pData.putInt("SpellOvergrowthBatchCount", count);
            if (count == 2) {
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.OVERGROWTH, refund);
            }
        }
    }

    private static void spreadMossAndGrass(ServerLevel level, BlockPos center, int r, boolean emphasizeTallGrass) {
        double rSq = (r + 0.5D) * (r + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            if (p.distSqr(center) > rSq) continue;
            BlockPos underPos = p.below();
            BlockState below = level.getBlockState(underPos);
            if ((below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.STONE))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, underPos, below)
                    && level.random.nextFloat() < 0.22F) {
                level.setBlockAndUpdate(underPos, Blocks.MOSS_BLOCK.defaultBlockState());
                below = Blocks.MOSS_BLOCK.defaultBlockState();
            }
            if (level.getBlockState(p).isAir()
                    && below.isSolidRender()
                    && !(below.getBlock() instanceof FarmBlock)
                    && level.random.nextFloat() < 0.42F) {
                if (emphasizeTallGrass
                        && level.random.nextFloat() < 0.45F
                        && level.getBlockState(p.above()).isAir()
                        && Blocks.TALL_GRASS.defaultBlockState().canSurvive(level, p)) {
                    DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), p, Block.UPDATE_ALL);
                } else {
                    Block cover = level.random.nextBoolean() ? Blocks.MOSS_CARPET : Blocks.SHORT_GRASS;
                    if (cover.defaultBlockState().canSurvive(level, p)) {
                        level.setBlockAndUpdate(p, cover.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void spreadMossAndWildflowers(ServerLevel level, BlockPos center, int r) {
        double rSq = (r + 0.5D) * (r + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            if (p.distSqr(center) > rSq) continue;
            BlockPos underPos = p.below();
            BlockState below = level.getBlockState(underPos);
            if ((below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.STONE))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, underPos, below)
                    && level.random.nextFloat() < 0.20F) {
                level.setBlockAndUpdate(underPos, Blocks.MOSS_BLOCK.defaultBlockState());
                below = Blocks.MOSS_BLOCK.defaultBlockState();
            }
            if (level.getBlockState(p).isAir()
                    && below.isSolidRender()
                    && !(below.getBlock() instanceof FarmBlock)
                    && level.random.nextFloat() < 0.36F) {
                int pick = level.random.nextInt(7);
                if (pick == 6 && level.getBlockState(p.above()).isAir() && Blocks.TALL_GRASS.defaultBlockState().canSurvive(level, p)) {
                    DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), p, Block.UPDATE_ALL);
                    continue;
                }
                Block flora = switch (pick) {
                    case 0 -> Blocks.SHORT_GRASS;
                    case 1 -> Blocks.DANDELION;
                    case 2 -> Blocks.POPPY;
                    case 3 -> Blocks.AZURE_BLUET;
                    case 4 -> Blocks.CORNFLOWER;
                    default -> Blocks.MOSS_CARPET;
                };
                if (flora.defaultBlockState().canSurvive(level, p)) {
                    level.setBlockAndUpdate(p, flora.defaultBlockState());
                }
            }
        }
    }

    private static boolean isEntityGrounded(ServerLevel level, LivingEntity entity) {
        if (entity.onGround()) return true;
        BlockPos below = BlockPos.containing(entity.getX(), entity.getY() - 0.55D, entity.getZ());
        BlockState bs = level.getBlockState(below);
        return !bs.isAir() && !bs.getCollisionShape(level, below).isEmpty();
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
}
