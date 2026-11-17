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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Nature Materia (Base Type #3)</b>
 * (Spellcrafting System Guide Section 6.4):
 * <ul>
 *     <li><b>Projectile</b>: <i>Thorned Briar Seed</i> — spiked botanical seed that burrows into the target on impact,
 *     dealing puncture damage and erupting 3D thorny vines that root them to the spot for 2.0s (40t).</li>
 *     <li><b>Beam</b>: <i>Verdant Growth Ray</i> — continuous living emerald beam wrapped in coiling ivy &amp; blossoms
 *     that accelerates crop/sapling growth, rapidly knits ally wounds, and tangles enemies in progressive dragging ivy creepers.</li>
 *     <li><b>Radial Burst</b>: <i>Bramble Bloom Explosion</i> — rapid 6-block ring of jagged 3D wooden spikes &amp; thorny briars,
 *     knocking foes backward, rooting them for 1.5s (30t), and leaving temporary damaging ground briars.</li>
 *     <li><b>Touch / Strike</b>: <i>Poison Ivy Slash</i> — thorny crescent cleave coating targets in virulent botanical sap
 *     that inflicts Poison II for 8s (160t) and lowers target Armor Toughness.</li>
 *     <li><b>Aura / Self</b>: <i>Sylvan Mantle</i> — wildflowers &amp; grass bloom underfoot as you walk, grants exact +30% speed
 *     in forests/foliage, prevents passive animals from fleeing, and confers passive health regeneration under direct sunlight.</li>
 *     <li><b>Glyph / Trap</b>: <i>Overgrowth Snare</i> — 1.0s arm, 10m (or Tier 5+ indefinite) disarmable root trap; violently
 *     erupts dense 3D wooden roots from the floor, trapping all mobs in a 4-block radius inside an entangled dome cage for 4.0s
 *     with continuous strangling crush damage.</li>
 *     <li><b>Cone / Spray</b>: <i>Toxic Pollen Cone</i> — dense billowing cloud of golden-yellow botanical pollen inflicting
 *     Nausea and Poison II on all targets caught in front of the caster.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Spore Tempest</i> — overhead mycelial canopy calling down dense toxic fungal spores,
 *     carpeting the ground in temporary poison moss patches that damage and poison hostiles.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Sylvan Eidolon</i> — lush forest green (#1A5923) triad: Agent lunges &amp; roots foes
 *     with briar tendrils; Sentry shoots Thorn Darts from anchored wooden roots; Decoy is a floral effigy emitting sweet golden
 *     pollen to taunt hostiles before bursting into spores.</li>
 *     <li><b>Flow (Chains)</b>: <i>Entangling Briar Chains</i> — 3D thorny creepers wrapping each chained target and physically
 *     tethering consecutive victims within 5.0 blocks.</li>
 *     <li><b>Blink / Step</b>: <i>Tree-Stepping</i> — teleports cleanly between living trees and leaf canopies, leaving a
 *     blooming flower bush at the departure point.</li>
 *     <li><b>Transmute</b>: <i>Botanical Synthesis</i> — transmutes stone into mossy stone, dirt into rich podzol, dead bushes
 *     into saplings, and cobblestone into living logs.</li>
 *     <li><b>Affliction</b>: <i>Silencing Spore Hex</i> — persistent anti-milk fungal curse (cleansed by Restoration/Fire magic
 *     or caster defeat) permanently silencing spellcasting &amp; mob special abilities while shredding 20% Armor Toughness.</li>
 * </ul>
 */
public final class NatureSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SYLVAN_EIDOLON_HEX_COLOR = 0x1A5923;
    private static final String TAG_IVY_DRAG_STACKS = "SpellNatureIvyDragStacks";
    private static final String TAG_IVY_DRAG_UNTIL = "SpellNatureIvyDragUntil";

    @Override
    public EssenceType materiaType() {
        return EssenceType.NATURE;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.4)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Entangling Briar Chains";
            default -> "Verdant Briar " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Entangling Briar Chains: Leaping chains manifest as thorny creepers that wrap around each chained target, physically tethering consecutive victims within 5 blocks.";
            default -> "Verdant Briar Flow: Guides spell delivery along living emerald vine currents.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Thorned Briar Seed";
            case BEAM -> "Verdant Growth Ray";
            case RADIAL_BURST -> "Bramble Bloom Explosion";
            case TOUCH_STRIKE -> "Poison Ivy Slash";
            case AURA -> "Sylvan Mantle";
            case GLYPH -> "Overgrowth Snare";
            case CONE -> "Toxic Pollen Cone";
            case SKYFALL -> "Spore Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Thorned Briar Seed: Spiked botanical seed that burrows into the target on impact, dealing puncture damage and erupting thorny vines that root them for 2 seconds.";
            case BEAM -> "Verdant Growth Ray: Continuous living emerald beam that accelerates crop/sapling growth, rapidly knits ally wounds, and tangles enemies in speed-draining ivy creepers.";
            case RADIAL_BURST -> "Bramble Bloom Explosion: Detonates in a 6-block ring of jagged wooden spikes and thorny briars, knocking foes back, rooting them for 1.5s, and leaving ground briars.";
            case TOUCH_STRIKE -> "Poison Ivy Slash: Weapon swing coats the target in virulent botanical sap that inflicts Poison II for 8 seconds and lowers target Armor Toughness.";
            case AURA -> "Sylvan Mantle: Flowers and grass bloom underfoot as you walk; grants +30% speed in forests/foliage, calms passive wildlife, and confers sunlight health regeneration.";
            case GLYPH -> "Overgrowth Snare: Dense wooden roots violently erupt from the floor, trapping all mobs in a 4-block radius inside an entangled cage for 4s with continuous crush damage.";
            case CONE -> "Toxic Pollen Cone: Sprays a dense cloud of golden-yellow botanical pollen that inflicts Nausea and Poison II on all targets caught in front of the caster.";
            case SKYFALL -> "Spore Tempest: Calls down dense toxic fungal spores from an overhead mycelial canopy, carpeting the ground in temporary poison moss patches that damage hostiles.";
            case SUMMON_AGENT -> "Sylvan Eidolon: Forest green (#1A5923) woodland hunter that lunges across mid-range gaps and lashes/roots foes with thorny briar tendrils.";
            case SUMMON_SENTRY -> "Sylvan Thorn Sentry: Ground-anchored forest green (#1A5923) root spire that fires high-speed rooting Thorn Darts.";
            case SUMMON_DECOY -> "Sylvan Pollen Effigy: Woven forest green (#1A5923) floral effigy emitting sweet golden pollen to taunt hostiles before bursting into toxic spores.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Tree-Stepping";
            case TRANSMUTE, BREAK -> "Botanical Synthesis";
            case AFFLICTION -> "Silencing Spore Hex";
            case HARVEST -> "Verdant Overgrowth Harvest";
            case RESTORATION -> "Photosynthetic Regrowth";
            default -> "Sylvan " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Tree-Stepping: Teleports instantly between living trees and leaf canopies while phasing through foliage, leaving a blooming flower bush at the departure point.";
            case TRANSMUTE, BREAK -> "Botanical Synthesis: Transmutes stone into mossy stone, dirt into rich podzol, dead bushes into saplings, and cobblestone into living logs.";
            case AFFLICTION -> "Silencing Spore Hex: Persistent anti-milk fungal curse (cleansed by Restoration/Fire magic or caster defeat) silencing spellcasting & mob abilities while shredding 20% Armor Toughness.";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — ENTANGLING BRIAR CHAINS (SECTION 6.4)
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
        int tier = spell.getEffectiveTier();
        Vec3 midpoint = fromTarget.position().add(toTarget.position()).scale(0.5D);
        applyBriarRoot(level, fromTarget, 35);
        applyBriarRoot(level, toTarget, 35);

        // Broadcast immediate 3D thorny creeper vine bridge between chained targets
        SpellWorldVfxManager.broadcastChainArc(
                level,
                fromTarget.position().add(0.0D, fromTarget.getBbHeight() * 0.55D, 0.0D),
                toTarget.position().add(0.0D, toTarget.getBbHeight() * 0.55D, 0.0D),
                EssenceType.NATURE, null, tier
        );
        spawnEmeraldLeafBurst(level, midpoint.add(0.0D, 0.75D, 0.0D), 14, 0.55D);
        level.playSound(null, BlockPos.containing(midpoint), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.85F, 0.90F);

        final UUID idA = fromTarget.getUUID();
        final UUID idB = toTarget.getUUID();
        int tetherDuration = 120; // 6.0s generous tether duration so the 5-block thorny creeper chain can be admired in combat!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, midpoint, 6.5D, tetherDuration, level.getGameTime(), spell, slot, EssenceType.NATURE, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    Entity eA = srv.getEntity(idA);
                    Entity eB = srv.getEntity(idB);
                    if (!(eA instanceof LivingEntity lA) || !(eB instanceof LivingEntity lB) || !lA.isAlive() || !lB.isAlive()) {
                        return false;
                    }

                    Vec3 chestA = lA.position().add(0.0D, lA.getBbHeight() * 0.55D, 0.0D);
                    Vec3 chestB = lB.position().add(0.0D, lB.getBbHeight() * 0.55D, 0.0D);
                    if (elapsed % 14 == 0) {
                        // Refresh live 3D thorny creeper tether tracking both moving targets!
                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_CHAIN_ARC, chestA, chestB,
                                EssenceType.NATURE, null, 0.14F, 18, tier
                        );
                    }
                    if (elapsed % 6 == 0) {
                        Vec3 span = chestB.subtract(chestA);
                        for (double f = 0.2D; f <= 0.8D; f += 0.3D) {
                            Vec3 pt = chestA.add(span.scale(f));
                            srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 22, 1.0F, 0.08F),
                                    pt.x, pt.y, pt.z, 1, 0.06D, 0.06D, 0.06D, 0.01D);
                        }
                    }

                    double dist = lA.distanceTo(lB);
                    if (dist > 5.0D) {
                        Vec3 mid = lA.position().add(lB.position()).scale(0.5D);
                        double pullScale = Math.min(0.85D, 0.38D + (dist - 5.0D) * 0.18D);
                        if (!SpellCombatReactionEngine.isBossEntity(srv, lA)) {
                            Vec3 pA = mid.subtract(lA.position()).normalize().scale(pullScale);
                            SpellExecutionEngine.applySyncedMotion(lA, new Vec3(pA.x, 0.14D, pA.z));
                        }
                        if (!SpellCombatReactionEngine.isBossEntity(srv, lB)) {
                            Vec3 pB = mid.subtract(lB.position()).normalize().scale(pullScale);
                            SpellExecutionEngine.applySyncedMotion(lB, new Vec3(pB.x, 0.14D, pB.z));
                        }
                        if (elapsed % 10 == 0) {
                            srv.playSound(null, BlockPos.containing(mid), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.55F, 0.85F);
                        }
                    }
                    return true;
                }
        ));
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (SECTION 6.4)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x2CE668, 24, 1.0F, 0.10F),
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    2, 0.07D, 0.07D, 0.07D, 0.01D);
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
        // Thorned Briar Seed: burrows into target dealing puncture damage & erupting thorny vines that root for 2.0s (40t)!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        int tier = spell.getEffectiveTier();
        float punctureDmg = Math.max(2.5F + tier * 0.75F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.35F);
        SpellCombatReactionEngine.hurtWithPureMateria(
                level,
                living,
                caster != null ? level.damageSources().indirectMagic(projectile, caster) : level.damageSources().magic(),
                punctureDmg
        );
        applyBriarRoot(level, living, 40);

        // 46-tick 3D thorny vine snare eruption wrapping the rooted target!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                living.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.NATURE, null, 1.25F, 46, tier
        );
        spawnEmeraldLeafBurst(level, living.position().add(0.0D, 0.55D, 0.0D), 16, 0.40D);
        level.playSound(null, living.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.85F, 0.85F);
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
        // Ground-impact Thorned Briar Seed burrows into soil and erupts a 1.8m thorny vine snare!
        int tier = spell.getEffectiveTier();
        Vec3 pos = blockHit.getLocation();
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                pos, Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i()),
                EssenceType.NATURE, null, 1.15F, 44, tier
        );
        spawnEmeraldLeafBurst(level, pos.add(0.0D, 0.25D, 0.0D), 14, 0.45D);
        level.playSound(null, blockHit.getBlockPos(), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.75F, 0.90F);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(1.85D))) {
            if (!foe.isAlive() || foe == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            applyBriarRoot(level, foe, 40);
        }
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
        // Verdant Growth Ray: accelerates crop/sapling growth, rapidly knits ally wounds, and tangles foes in progressive dragging ivy creepers!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        float allyHeal = (2.5F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (isFriendlyOrWoodlandAlly(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, allyHeal);
                hit.heal(allyHeal);
                level.sendParticles(ParticleTypes.HEART, hit.getX(), hit.getY(1.05D), hit.getZ(), 2, 0.25D, 0.25D, 0.25D, 0.02D);
                spawnEmeraldLeafBurst(level, hit.position().add(0.0D, 0.7D, 0.0D), 6, 0.30D);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                CompoundTag hTag = EntityHelper.getPersistentData(hit);
                int prevStacks = (hTag.getLong(TAG_IVY_DRAG_UNTIL).orElse(0L) > now)
                        ? hTag.getInt(TAG_IVY_DRAG_STACKS).orElse(0) : 0;
                int stacks = Math.min(5, prevStacks + 1);
                hTag.putInt(TAG_IVY_DRAG_STACKS, stacks);
                hTag.putLong(TAG_IVY_DRAG_UNTIL, now + 50L);

                // Progressive speed-draining ivy creepers (Slowness II -> VI + ground drag)!
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, Math.min(5, stacks), false, true));
                if (stacks >= 3) {
                    hit.addEffect(new MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 35, 0, false, false));
                }
                Vec3 curVel = hit.getDeltaMovement();
                double dragFactor = Math.max(0.12D, 0.55D - stacks * 0.09D);
                hit.setDeltaMovement(curVel.x * dragFactor, Math.min(0.02D, curVel.y), curVel.z * dragFactor);
                hit.hasImpulse = true;

                if (now % 10L == 0L) {
                    SpellWorldVfxManager.broadcast(
                            level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                            hit.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.NATURE, null, 0.95F, 22, tier
                    );
                }
            }
        }

        // Accelerate crop, sapling, berry & flora growth along the Verdant Growth Ray
        double effectiveStop = blockHit.getLocation().distanceTo(caster.getEyePosition());
        int steps = Math.min(24, (int) Math.ceil(Math.min(maxReach, effectiveStop + 0.5D)));
        int grownCount = 0;
        for (int s = 1; s <= steps && grownCount < 6; s++) {
            BlockPos p = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos bp : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                if (level.random.nextFloat() > 0.42F) continue;
                BlockState bs = level.getBlockState(bp);
                if (bs.getBlock() instanceof BonemealableBlock growable
                        && growable.isValidBonemealTarget(level, bp, bs)) {
                    BlockPos imm = bp.immutable();
                    if (growable.isBonemealSuccess(level, level.random, imm, bs)) {
                        growable.performBonemeal(level, level.random, imm, bs);
                    }
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.RUNE_BERKANO.get(), 0xB4FF78, 26, 1.05F, 0.05F),
                            imm.getX() + 0.5D, imm.getY() + 0.65D, imm.getZ() + 0.5D, 2, 0.2D, 0.2D, 0.2D, 0.01D);
                    if (++grownCount >= 6) break;
                }
            }
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
        // Poison Ivy Slash: coats targets in virulent botanical sap inflicting Poison II for 8s (160t) and lowering Armor Toughness!
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.0D,
                EssenceType.NATURE, null, tier
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.VINE_STEP, SoundSource.PLAYERS, 0.90F, 1.20F);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
            // Lower target Armor Toughness (both flat -4.0 and -20% Toughness for the full 8 seconds!)
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 0.0D, 4.0D + tier * 0.5D, 160L);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.20D, 160L);

            // Undead are immune to vanilla Poison, so virulent botanical sap corrodes undead tissue directly!
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                SpellCombatReactionEngine.hurtWithPureMateria(
                        level, hit, level.damageSources().indirectMagic(caster, caster),
                        (3.0F + tier * 0.85F) * Math.max(0.5F, apparatusPotency)
                );
            }

            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_ACID.get(), 0x8AFF3B, 36, 1.05F, 0.06F),
                    hit.getX(), hit.getY(0.65D), hit.getZ(), 14, 0.32D, 0.38D, 0.32D, 0.04D);
            spawnEmeraldLeafBurst(level, hit.position().add(0.0D, 0.6D, 0.0D), 10, 0.35D);
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
        // Bramble Bloom Explosion: 6-block ring of jagged wooden spikes & thorny briars, knocking back, rooting 1.5s (30t), leaving ground briars!
        int tier = spell.getEffectiveTier();
        double effectiveRadius = Math.max(6.0D, radius);
        AABB box = new AABB(
                center.x - effectiveRadius, center.y - 2.0D, center.z - effectiveRadius,
                center.x + effectiveRadius, center.y + 2.8D, center.z + effectiveRadius
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (entity.position().distanceToSqr(center) > effectiveRadius * effectiveRadius) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, entity)) {
                Vec3 delta = entity.position().subtract(center);
                Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                Vec3 away = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D)).scale(1.15D);
                SpellExecutionEngine.applySyncedMotion(entity, new Vec3(away.x, 0.36D, away.z));
            }
            applyBriarRoot(level, entity, 30);
            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                    entity.position().add(0.0D, 0.08D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.NATURE, null, 1.05F, 34, tier
            );
        }

        int briarDuration = 160; // 8.0s generous ground briar patch duration
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, center, effectiveRadius * 0.92D, EssenceType.NATURE, null, briarDuration, tier
        );

        List<BlockPos> placedBriars = new ArrayList<>();
        BlockPos cPos = BlockPos.containing(center);
        int bRad = Math.min(5, (int) Math.floor(effectiveRadius * 0.85D));
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-bRad, -1, -bRad), cPos.offset(bRad, 1, bRad))) {
            if (p.distSqr(cPos) > (double) (bRad * bRad)) continue;
            if (level.getBlockState(p).isAir()
                    && level.getBlockState(p.below()).isSolidRender()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))
                    && level.random.nextFloat() < 0.38F) {
                level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
                placedBriars.add(p.immutable());
            }
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, effectiveRadius * 0.90D, briarDuration, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        for (BlockPos bp : placedBriars) {
                            if (srv.getBlockState(bp).is(Blocks.MOSS_CARPET)) {
                                srv.removeBlock(bp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB patchBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 1.2D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 1.6D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, patchBox, e -> e.isAlive() && e != owner)) {
                            if (foe.position().distanceToSqr(zone.center) > zone.radius * zone.radius) continue;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1));
                            SpellCombatReactionEngine.hurtWithPureMateria(
                                    srv, foe,
                                    owner != null ? srv.damageSources().indirectMagic(owner, owner) : srv.damageSources().sweetBerryBush(),
                                    1.5F + tier * 0.35F
                            );
                        }
                    }
                    return true;
                }
        ));
        spawnEmeraldLeafBurst(level, center.add(0.0D, 0.45D, 0.0D), 28, effectiveRadius * 0.45D);
        level.playSound(null, cPos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.80F);
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
        // Sylvan Mantle: 3D twining leafy-vine & blossom garland + +30% foliage speed + wildflower trail + wildlife calm + sunlight regen!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 50;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_NATURE_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.65F, null);
        spawnEmeraldLeafBurst(level, caster.position().add(0.0D, 0.9D, 0.0D), 22, 0.55D);
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFDC47, 36, 1.05F, 0.05F),
                caster.getX(), caster.getY(1.0D), caster.getZ(), 14, 0.45D, 0.5D, 0.45D, 0.02D);
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.85F, 1.15F);
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
        // Overgrowth Snare: canonical 1.0s (20t) arming delay, 10m (12,000t) or Tier 5+ indefinite persistence, 50% Sneak-Right-Click disarm!
        // Violently erupts a 4-block 3D wooden root dome cage trapping all mobs inside for 4.0s (80t) with continuous strangling crush damage!
        int tier = spell.getEffectiveTier();
        double cageRadius = Math.max(4.0D, radius);
        int glyphLifetime = tier >= 5 ? (Integer.MAX_VALUE / 4) : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(level, glyphCenter, cageRadius * 0.72D, EssenceType.NATURE, null, 220, tier);
        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 0.75F, 0.95F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, cageRadius, glyphLifetime, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed > 0 && elapsed % 180 == 0) {
                        SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, cageRadius * 0.72D, EssenceType.NATURE, null, 200, tier);
                    }
                    // Canonical 1.0s (20-tick) arming delay before proximity trigger activates
                    if (elapsed < 20) return true;
                    if (elapsed % 4 != 0) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - cageRadius * 0.78D, zone.center.y - 1.0D, zone.center.z - cageRadius * 0.78D,
                            zone.center.x + cageRadius * 0.78D, zone.center.y + 2.4D, zone.center.z + cageRadius * 0.78D
                    );
                    List<LivingEntity> intruders = srv.getEntitiesOfClass(LivingEntity.class, triggerBox,
                            e -> e.isAlive() && e != owner && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)
                                    && !isFriendlyOrWoodlandAlly(e, owner));
                    if (intruders.isEmpty()) return true;

                    // Erupt the 4-block 3D Entangled Wooden Root Dome Cage for 4.0s (80t)!
                    SpellWorldVfxManager.broadcastWardShell(srv, zone.center, cageRadius, EssenceType.NATURE, null, 88, tier);
                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, cageRadius, EssenceType.NATURE, null, 92, tier);
                    spawnEmeraldLeafBurst(srv, zone.center.add(0.0D, 1.0D, 0.0D), 32, cageRadius * 0.55D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.15F, 0.65F);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.VINE_PLACE, SoundSource.PLAYERS, 1.0F, 0.75F);

                    AABB cageBox = new AABB(
                            zone.center.x - cageRadius, zone.center.y - 1.2D, zone.center.z - cageRadius,
                            zone.center.x + cageRadius, zone.center.y + 3.4D, zone.center.z + cageRadius
                    );
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, cageRadius, 80, srv.getGameTime(), zone.spell, zone.slot, EssenceType.NATURE, zone.apparatusPotency,
                            (csrv, czone, celapsed) -> {
                                if (celapsed > czone.maxDurationTicks) return false;
                                LivingEntity cOwner = czone.resolveCaster(csrv);
                                boolean cWard = czone.spell != null && czone.spell.hasWardFriendlyFireSafeguard();
                                for (LivingEntity victim : csrv.getEntitiesOfClass(LivingEntity.class, cageBox,
                                        e -> e.isAlive() && e != cOwner && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, cOwner, cWard)
                                                && !isFriendlyOrWoodlandAlly(e, cOwner))) {
                                    double d = victim.position().distanceTo(czone.center);
                                    if (d > cageRadius + 0.4D) continue;

                                    // Clamp trapped victims inside the 4-block wooden root cage bars!
                                    if (d > cageRadius * 0.78D && !SpellCombatReactionEngine.isBossEntity(csrv, victim)) {
                                        Vec3 inward = czone.center.subtract(victim.position()).normalize().scale(0.52D);
                                        SpellExecutionEngine.applySyncedMotion(victim, new Vec3(inward.x, 0.05D, inward.z));
                                    }
                                    applyBriarRoot(csrv, victim, 22);

                                    if (celapsed % 16 == 0) {
                                        float crushDmg = (2.2F + tier * 0.65F) * Math.max(0.5F, czone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithPureMateria(
                                                csrv, victim,
                                                cOwner != null ? csrv.damageSources().indirectMagic(cOwner, cOwner) : csrv.damageSources().inWall(),
                                                crushDmg
                                        );
                                        if (czone.spell != null && czone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    csrv, cOwner, victim, Vec3.ZERO,
                                                    czone.spell, czone.slot, czone.fallbackMateria, czone.apparatusPotency * 0.35F
                                            );
                                        }
                                        spawnEmeraldLeafBurst(csrv, victim.position().add(0.0D, 0.7D, 0.0D), 6, 0.30D);
                                    }
                                }
                                return true;
                            }
                    ));
                    return false;
                }
        ).markDisarmableGlyph());
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
        // Toxic Pollen Cone: dense billowing cloud of yellow botanical pollen inflicting Nausea and Poison II on all targets in front!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(8.0D, coneRange);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), normDir, reach,
                EssenceType.NATURE, null, tier
        );

        Set<UUID> afflicted = new HashSet<>();
        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            afflicted.add(hit.getUUID());
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
        }

        // Guarantee full-cone pollen coverage even when paired with utility/non-Harm effects
        Vec3 eye = caster.getEyePosition();
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(reach))) {
            if (cand == caster || !cand.isAlive() || afflicted.contains(cand.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())
                    || isFriendlyOrWoodlandAlly(cand, caster)) {
                continue;
            }
            Vec3 toCand = cand.getBoundingBox().getCenter().subtract(eye);
            double dist = toCand.length();
            if (dist <= reach && dist > 0.1D && toCand.scale(1.0D / dist).dot(normDir) >= 0.48D) {
                cand.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0));
                cand.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
            }
        }

        // Dense billowing golden-yellow pollen clouds + 4.5s lingering golden spore haze
        for (double d = 1.5D; d <= reach * 0.85D; d += 2.0D) {
            Vec3 cloudPt = eye.add(normDir.scale(d));
            double spread = 0.25D + d * 0.22D;
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFDC38, 48, 1.15F, 0.05F),
                    cloudPt.x, cloudPt.y, cloudPt.z, 12, spread, spread * 0.65D, spread, 0.025D);
        }
        Vec3 hazeCenter = eye.add(normDir.scale(reach * 0.48D));
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, hazeCenter, reach * 0.42D, 90, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xFFDC38, 36, 1.10F, 0.05F),
                                zone.center.x, zone.center.y, zone.center.z,
                                10, zone.radius * 0.55D, 0.75D, zone.radius * 0.55D, 0.015D);
                    }
                    return true;
                }
        ));
        level.playSound(null, caster.blockPosition(), SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 0.90F, 0.90F);
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
        // Spore Tempest: calls down dense toxic fungal spores (6s+ base, extended for rich visual admiration),
        // carpeting the ground in temporary poison moss patches that damage and poison hostiles!
        // Note: SpellLingeringZoneManager.registerZone automatically broadcasts our 3D mycelial SkyfallPillar for maxDurationTicks + 14!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        int tempestDuration = 140 + (tier - 1) * 20;

        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter, r, EssenceType.NATURE, null, tempestDuration, tier);

        List<BlockPos> sporeMoss = new ArrayList<>();
        BlockPos cPos = BlockPos.containing(impactCenter);
        int mr = Math.min(7, (int) Math.ceil(r));
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-mr, -1, -mr), cPos.offset(mr, 1, mr))) {
            if (p.distSqr(cPos) > (double) (mr * mr)) continue;
            if (level.getBlockState(p).isAir()
                    && level.getBlockState(p.below()).isSolidRender()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))
                    && level.random.nextFloat() < 0.36F) {
                level.setBlockAndUpdate(p, Blocks.MOSS_CARPET.defaultBlockState());
                sporeMoss.add(p.immutable());
            }
        }
        level.playSound(null, cPos, SoundEvents.SPORE_BLOSSOM_PLACE, SoundSource.PLAYERS, 1.0F, 0.75F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, tempestDuration, level.getGameTime(), spell, slot, EssenceType.NATURE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        for (BlockPos mp : sporeMoss) {
                            if (srv.getBlockState(mp).is(Blocks.MOSS_CARPET)) {
                                srv.removeBlock(mp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xB8FF52, 42, 1.15F, 0.05F),
                                zone.center.x, zone.center.y + 4.8D, zone.center.z,
                                16, zone.radius * 0.52D, 1.6D, zone.radius * 0.52D, 0.025D);
                        srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                zone.center.x, zone.center.y + 4.2D, zone.center.z,
                                10, zone.radius * 0.50D, 1.4D, zone.radius * 0.50D, 0.02D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (victim.position().distanceToSqr(zone.center) > zone.radius * zone.radius + 4.0D) continue;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, wardSafe)
                                    || isFriendlyOrWoodlandAlly(victim, owner)) {
                                continue;
                            }
                            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                            float mossDmg = (1.8F + tier * 0.45F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithPureMateria(
                                    srv, victim,
                                    owner != null ? srv.damageSources().indirectMagic(owner, owner) : srv.damageSources().magic(),
                                    mossDmg
                            );
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, victim, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.30F
                                );
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
        tag.putString("SpellEidolonMateria", EssenceType.NATURE.name());
        tag.putInt("SpellEidolonTintRGB", SYLVAN_EIDOLON_HEX_COLOR);
        spawnEmeraldLeafBurst(level, spawnPos.add(0.0D, 0.85D, 0.0D), 24, 0.65D);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, summonEntity.getBoundingBox().inflate(Math.max(3.5D, radius)))) {
            if (nearby != caster && nearby != summonEntity && nearby.isAlive()
                    && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(nearby, caster, spell.hasWardFriendlyFireSafeguard())
                    && !isFriendlyOrWoodlandAlly(nearby, caster)) {
                applyBriarRoot(level, nearby, 35);
            }
        }
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS (SECTION 6.4)
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Tree-Stepping: leaves a blooming flower bush (Flowering Azalea) at departure + lush emerald vine transit!
        BlockPos dep = BlockPos.containing(departurePos);
        BlockPos placePos = null;
        for (BlockPos cand : List.of(dep, dep.below(), dep.above())) {
            BlockState at = level.getBlockState(cand);
            BlockState under = level.getBlockState(cand.below());
            if ((at.isAir() || at.canBeReplaced())
                    && !under.getCollisionShape(level, cand.below()).isEmpty()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, cand, at)) {
                placePos = cand.immutable();
                break;
            }
        }

        if (placePos != null) {
            final BlockPos azaleaPos = placePos;
            level.setBlockAndUpdate(azaleaPos, Blocks.FLOWERING_AZALEA.defaultBlockState());
            // Keep the blooming flower bush in the world for a generous 30s (600t) before dissolving into blossom petals!
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, Vec3.atCenterOf(azaleaPos), 1.0D, 600, level.getGameTime(),
                    null, null, EssenceType.NATURE, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed >= zone.maxDurationTicks) {
                            if (srv.getBlockState(azaleaPos).is(Blocks.FLOWERING_AZALEA)) {
                                srv.removeBlock(azaleaPos, false);
                                srv.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0xFF94C2, 34, 1.05F, 0.08F),
                                        azaleaPos.getX() + 0.5D, azaleaPos.getY() + 0.5D, azaleaPos.getZ() + 0.5D,
                                        12, 0.3D, 0.3D, 0.3D, 0.03D);
                            }
                            return false;
                        }
                        return srv.getBlockState(azaleaPos).is(Blocks.FLOWERING_AZALEA);
                    }
            ));
        }

        // 3D living creeper vine trail & blossom bursts linking departure tree and arrival canopy
        SpellWorldVfxManager.broadcastChainArc(
                level, departurePos.add(0.0D, 0.85D, 0.0D), arrivalPos.add(0.0D, 0.85D, 0.0D),
                EssenceType.NATURE, null, potencyTier
        );
        spawnEmeraldLeafBurst(level, departurePos.add(0.0D, 0.8D, 0.0D), 22, 0.45D);
        spawnEmeraldLeafBurst(level, arrivalPos.add(0.0D, 0.8D, 0.0D), 22, 0.45D);
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0xFF94C2, 38, 1.10F, 0.08F),
                departurePos.x, departurePos.y + 0.75D, departurePos.z, 14, 0.35D, 0.4D, 0.35D, 0.04D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AZALEA_PLACE, SoundSource.PLAYERS, 0.90F, 1.15F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Botanical Synthesis: stone -> mossy stone, dirt -> rich podzol, dead bushes -> saplings, cobblestone -> living logs!
        BlockState result = null;
        if (state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            result = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        } else if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.INFESTED_STONE_BRICKS)) {
            result = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        } else if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
            result = Blocks.PODZOL.defaultBlockState();
        } else if (state.is(Blocks.DEAD_BUSH)) {
            Block[] saplings = new Block[]{
                    Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING,
                    Blocks.JUNGLE_SAPLING, Blocks.CHERRY_SAPLING, Blocks.DARK_OAK_SAPLING
            };
            Block chosen = saplings[level.random.nextInt(saplings.length)];
            BlockState cand = chosen.defaultBlockState();
            if (!cand.canSurvive(level, pos) && level.getBlockState(pos.below()).is(Blocks.SAND)) {
                level.setBlockAndUpdate(pos.below(), Blocks.PODZOL.defaultBlockState());
            }
            result = cand;
        } else if (state.is(Blocks.COBBLESTONE)) {
            result = Blocks.OAK_LOG.defaultBlockState();
        }

        if (result != null) {
            level.setBlockAndUpdate(pos, result);
            spawnEmeraldLeafBurst(level, Vec3.atCenterOf(pos), 10, 0.35D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.RUNE_BERKANO.get(), 0xB4FF78, 28, 1.05F, 0.05F),
                    pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D, 3, 0.25D, 0.25D, 0.25D, 0.01D);
            level.playSound(null, pos, SoundEvents.MOSS_PLACE, SoundSource.PLAYERS, 0.75F, 1.10F);
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
        ItemStack stack = itemEntity.getItem();
        int count = stack.getCount();
        ItemStack transmuted = null;
        if (stack.is(Items.STONE) || stack.is(Items.ANDESITE) || stack.is(Items.DIORITE) || stack.is(Items.GRANITE)) {
            transmuted = new ItemStack(Items.MOSSY_COBBLESTONE, count);
        } else if (stack.is(Items.STONE_BRICKS)) {
            transmuted = new ItemStack(Items.MOSSY_STONE_BRICKS, count);
        } else if (stack.is(Items.DIRT) || stack.is(Items.COARSE_DIRT) || stack.is(Items.ROOTED_DIRT)) {
            transmuted = new ItemStack(Items.PODZOL, count);
        } else if (stack.is(Items.DEAD_BUSH)) {
            transmuted = new ItemStack(Items.OAK_SAPLING, count);
        } else if (stack.is(Items.COBBLESTONE)) {
            transmuted = new ItemStack(Items.OAK_LOG, count);
        }

        if (transmuted != null) {
            itemEntity.setItem(transmuted);
            spawnEmeraldLeafBurst(level, itemEntity.position().add(0.0D, 0.25D, 0.0D), 8, 0.25D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Silencing Spore Hex: persistent anti-milk fungal curse (cleansed by Restoration/Fire magic or caster defeat);
        // permanently silences enemy spellcasting & mob special abilities while shredding 20% Armor Toughness!
        float stackMult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong("SpellSilencedUntil", level.getGameTime() + 45L);
        SpellCombatReactionEngine.applyPercentageArmorShred(
                target, level, 0.0D, Math.min(0.85D, 0.20D * stackMult), 45L
        );
        if (target instanceof Mob mob) {
            mob.setAggressive(false);
        }

        float sporeDot = (0.95F + potencyTier * 0.45F) * stackMult;
        SpellCombatReactionEngine.hurtWithPureMateria(level, target, level.damageSources().magic(), sporeDot);

        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xB8FF52, 32, 1.0F, 0.05F),
                target.getX(), target.getY(0.85D), target.getZ(), 6, 0.28D, 0.35D, 0.28D, 0.015D);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                target.getX(), target.getY(0.95D), target.getZ(), 4, 0.25D, 0.30D, 0.25D, 0.015D);
        return true;
    }

    // =========================================================================
    // BOTANICAL HELPERS
    // =========================================================================

    private static void applyBriarRoot(ServerLevel level, LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6, false, true));
        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0, false, false));
        EntityHelper.getPersistentData(target).putLong(
                SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + ticks);
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hasImpulse = true;
    }

    private static boolean isFriendlyOrWoodlandAlly(LivingEntity entity, @Nullable LivingEntity caster) {
        if (entity instanceof Monster) return false;
        if (caster != null) {
            if (entity == caster || entity.isAlliedTo(caster)) return true;
            if (entity instanceof SpellEidolonEntity eidolon && eidolon.isOwnedBy(caster)) return true;
            if (entity instanceof OwnableEntity ownable && ownable.getOwner() == caster) return true;
        }
        return entity instanceof Animal || entity instanceof AbstractVillager || entity instanceof AbstractGolem;
    }

    private static void spawnEmeraldLeafBurst(ServerLevel level, Vec3 pos, int count, double spread) {
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x2CE668, 34, 1.05F, 0.09F),
                pos.x, pos.y, pos.z, count, spread, spread * 0.65D, spread, 0.035D);
    }
}
