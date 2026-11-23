package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
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
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Complete per-part Signature Mutation implementation for <b>Taiga Materia (Tier 1 Fusion #19 — Nature + Frozen)</b>
 * (Spellcrafting System Guide Section 6.20):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Dual Nature + Frozen harmonic matrix — Strong vs <b>WATER (+1)</b> &amp; <b>NATURE (+1)</b>
 *     (Dominance {@code taiga_nature_dom}: Strong vs Water &amp; Earth) | Weak vs <b>NETHER (-1)</b>, <b>UMBRAL (-1)</b>, &amp; <b>UNDEAD (-1)</b>.</li>
 *     <li><b>Flow (Split)</b>: <i>Permafrost Fork Split</i> — splits into two sub-zero resin shards upon impact or reaching maximum range.</li>
 *     <li><b>Projectile</b>: <i>Hardened Frost-Resin Spike</i> — sub-zero coniferous resin lance ({@code #1A5923} / {@code #98DCF2})
 *     piercing up to 2 enemies and pinning the second victim to the ground in sticky freezing sap for 3.0s.</li>
 *     <li><b>Beam</b>: <i>Glacial Amber Leeching Beam</i> — siphons enemy movement speed &amp; armor toughness directly into the caster
 *     while laying down a trail of frozen resin on the ground.</li>
 *     <li><b>Radial Burst</b>: <i>Boreal Blizzard Nova</i> — 6m+ blast of whirling frosted pine needles &amp; ice shards dealing Taiga Materia damage,
 *     knockback, Slowness III &amp; Freezing for 4.0s, and blanketing the ground in snow layers &amp; frost-rimed sweet berry bushes.</li>
 *     <li><b>Touch / Strike</b>: <i>Heat-Siphon Conifer Touch</i> — drains target warmth with Taiga Materia damage &amp; shivering Freezing,
 *     granting the caster +30% movement speed and temporary Frost Walker for 10.0s.</li>
 *     <li><b>Aura / Self</b>: <i>Winter Solstice Shroud</i> — sub-zero blizzard mist deflecting 50% of incoming arrows, snuffing fire instantly,
 *     and regenerating health when standing in snow, ice, podzol, or cold/taiga biomes (mobile or sneak-anchored).</li>
 *     <li><b>Glyph / Trap</b>: <i>Frosted Conifer Briar Trap</i> — disarmable Single/Repeating boreal ground rune erupting into frozen thorny
 *     spruce roots that trap victims for 4.0s, inflict Freezing &amp; Slowness IV, and leave sweet berry bushes &amp; powder snow.</li>
 *     <li><b>Cone / Spray</b>: <i>Frozen Sap Splatter Cone</i> — shotgun-like spread of sticky sub-zero pine sap reducing movement speed by 60%
 *     and making victims take +50% shatter damage from physical/blunt strikes.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Falling Glacial Timber Cataclysm</i> — tier-scaled 6.0s barrage of petrified frozen spruce logs &amp; heavy icicles
 *     falling as 3D Pseudoblock columns (acting as obstacle barricades mid-spell) before solidifying into real world barricade blocks upon completion.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Boreal Hunter Eidolon Suite</i> — {@code #1A5923} / {@code #98DCF2} Boreal Stalker Agent,
 *     +25%-attack-speed / cold-resist Frost-Howler Spire Sentry, and blinding Phantom Snow Stalker Decoy.</li>
 *     <li><b>Blink / Step</b>: <i>Boreal Pine-Phasing Blink</i> — 14m+ pine-needle blizzard teleport leaving a frosted decoy bush at departure.</li>
 *     <li><b>Transmute &amp; Break</b>: <i>Boreal Timber &amp; Podzol Transmute</i> — converts oak/birch/jungle wood &amp; leaves into spruce,
 *     grass/dirt into podzol, and standing water into packed ice (+ Silk Touch block harvesting).</li>
 *     <li><b>Affliction</b>: <i>Petrifying Frost-Sap Hex</i> — milk-immune resin curse inflicting Freezing &amp; Slowness III, preventing sprinting,
 *     and increasing incoming physical/shatter damage by +50% until cleansed by Fire/Restoration magic or caster defeat.</li>
 *     <li><b>Restoration</b>: <i>Evergreen Resilience Restoration</i> — Regeneration II, cures burning &amp; Hunger, and grants 30s complete cold/freeze immunity.</li>
 * </ul>
 */
public final class TaigaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int EVERGREEN_PINE_HEX_COLOR = 0x1A5923;
    public static final int FROST_CYAN_HEX_COLOR = 0x98DCF2;
    public static final int FROZEN_AMBER_SAP_HEX_COLOR = 0xDC9E3C;

    private static final String TAG_RESIN_TRAIL_LAST_POS = "SpellTaigaResinTrailLastPos";
    private static final String TAG_RESIN_TRAIL_LAST_TICK = "SpellTaigaResinTrailLastTick";

    private record PlannedTimberColumn(BlockPos basePos, BlockState blockState, int height, int dropTick) {
    }

    @Override
    public EssenceType materiaType() {
        return EssenceType.TAIGA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, OCTUPLE_DOME -> "Permafrost Fork Split";
            default -> "Permafrost Fork Split (" + flow.getDisplayName() + ")";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hardened Frost-Resin Spike";
            case BEAM -> "Glacial Amber Leeching Beam";
            case RADIAL_BURST -> "Boreal Blizzard Nova";
            case TOUCH_STRIKE -> "Heat-Siphon Conifer Touch";
            case AURA -> "Winter Solstice Shroud";
            case GLYPH -> "Frosted Conifer Briar Trap";
            case CONE -> "Frozen Sap Splatter Cone";
            case SKYFALL -> "Falling Glacial Timber Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Boreal Pine-Phasing Blink";
            case TRANSMUTE, BREAK -> "Boreal Timber & Podzol Transmute";
            case AFFLICTION -> "Petrifying Frost-Sap Hex";
            case RESTORATION -> "Evergreen Resilience Restoration";
            default -> "Boreal Frost-Resin " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — PERMAFROST FORK SPLIT
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), FROST_CYAN_HEX_COLOR, 18, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
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
        // Section 6.20 Permafrost Fork Split: splits into two sub-zero resin shards upon impact or reaching maximum range!
        projectile.spawnSplitChild(level, splitOrigin, -20.0F, false, 0.72F, 1.45F);
        projectile.spawnSplitChild(level, splitOrigin, 20.0F, false, 0.72F, 1.45F);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 24, 1.00F, 0.06F),
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 10, 0.22D, 0.22D, 0.22D, 0.05D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), FROST_CYAN_HEX_COLOR, 24, 1.05F, 0.06F),
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 10, 0.24D, 0.24D, 0.24D, 0.05D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 10, 0.24D, 0.24D, 0.24D, 0.04D);
        level.playSound(null, BlockPos.containing(splitOrigin), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.75F, 1.42F);
    }

    // =========================================================================
    // FORM MUTATION 1 — HARDENED FROST-RESIN SPIKE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Aerodynamic zero-drop coniferous resin lance (#1A5923 / #98DCF2)
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D && vel.length() < 1.85D) {
            projectile.setDeltaMovement(vel.normalize().scale(1.90D));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.75F, 1.35F);
        level.playSound(null, caster.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.65F, 1.22F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 20, 0.88F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.01D);
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
        // Section 6.20 Hardened Frost-Resin Spike: pierces through up to 2 enemies (bonusPierceTargets == 2),
        // pinning the SECOND victim (getHitEntityCount() == 2) to the ground in sticky freezing sap for 3.0 seconds (60t)!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        if (!slot.hasHarmVerb()) {
            float spikeDmg = (2.8F + tier * 0.85F)
                    * spell.flawProfile().getPotencyMultiplier();
            SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(level, living, caster, spikeDmg, false, true);
        }

        long now = level.getGameTime();
        CompoundTag vTag = EntityHelper.getPersistentData(living);
        vTag.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, Math.max(now + 100L, vTag.getLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL).orElse(0L)));
        if (living.canFreeze()) {
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 50, living.getTicksFrozen() + 65));
        }

        int hitOrdinal = projectile.getHitEntityCount();
        if (hitOrdinal == 2) {
            // Pin the SECOND victim to the ground in sticky freezing sap for exact 3.0 seconds (60 ticks)!
            vTag.putLong(SpellLingeringZoneManager.TAG_TAIGA_SAP_PIN_UNTIL, now + 60L);
            vTag.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 60L);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            SpellExecutionEngine.applySyncedMotion(living, new Vec3(0.0D, Math.min(0.0D, living.getDeltaMovement().y), 0.0D));

            SpellWorldVfxManager.broadcastArcaneSeal(
                    level, living.position().add(0.0D, 0.04D, 0.0D), 1.45D, EssenceType.TAIGA, EssenceType.FROZEN, 60, tier
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), FROZEN_AMBER_SAP_HEX_COLOR, 32, 1.15F, 0.06F),
                    living.getX(), living.getY(0.35D), living.getZ(), 16, 0.38D, 0.25D, 0.38D, 0.04D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), FROST_CYAN_HEX_COLOR, 30, 1.10F, 0.06F),
                    living.getX(), living.getY(0.45D), living.getZ(), 14, 0.38D, 0.30D, 0.38D, 0.04D
            );
            level.playSound(null, living.blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.95F, 0.78F);
            level.playSound(null, living.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.80F, 1.25F);
        } else {
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(living, level, 0.40D, 50L);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
        }
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
        BlockPos hitPos = hitResult.getBlockPos();
        if (level.getBlockState(hitPos).is(Blocks.WATER)) {
            level.setBlockAndUpdate(hitPos, Blocks.PACKED_ICE.defaultBlockState());
        } else {
            BlockPos adj = hitPos.relative(hitResult.getDirection());
            if (level.getBlockState(adj).is(Blocks.WATER)) {
                level.setBlockAndUpdate(adj, Blocks.PACKED_ICE.defaultBlockState());
            } else if (level.getBlockState(adj).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, adj)) {
                level.setBlockAndUpdate(adj, Blocks.SNOW.defaultBlockState());
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 2 — GLACIAL AMBER LEECHING BEAM (BEAM)
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
        // Section 6.20 Glacial Amber Leeching Beam:
        // Channels a sticky freezing beam that siphons enemy movement speed and armor toughness directly into the caster,
        // leaving a trail of frozen resin on the ground!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        int siphonedCount = 0;

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float tickDmg = (1.15F + tier * 0.32F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(level, hit, caster, tickDmg, false, true);
            }

            // Drain 35% movement speed & 30% armor toughness from enemy!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.35D, 70L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 80L);
            if (hit.canFreeze()) {
                hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 30, hit.getTicksFrozen() + 18));
            }
            siphonedCount++;
        }

        if (siphonedCount > 0) {
            // Siphon drained speed (+35%) & Armor Toughness (+2.5 + 0.5*tier) directly into the caster!
            SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.35D, 80L);
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(
                    caster, level, 0.0D, 2.5D + tier * 0.5D, 0.0D, 0.0D, 80L
            );
        }

        // Lay down frozen resin nodes along the ground under the beam (throttled per-caster so channeling doesn't spam duplicate zones!)
        double actualLen = blockHit != null ? caster.getEyePosition().distanceTo(blockHit.getLocation()) : maxReach;
        int steps = Math.clamp((int) Math.ceil(actualLen), 2, 24);
        List<Vec3> resinNodes = new ArrayList<>();
        Vec3 eye = caster.getEyePosition();

        for (int s = 2; s <= steps; s += 2) {
            Vec3 sample = eye.add(direction.scale(s));
            BlockPos ground = findGroundSurfaceNear(level, BlockPos.containing(sample));
            BlockPos above = ground.above();
            if (level.getBlockState(ground).is(Blocks.WATER)) {
                level.setBlockAndUpdate(ground, Blocks.PACKED_ICE.defaultBlockState());
            } else if (level.getBlockState(above).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, above)
                    && level.random.nextFloat() < 0.45F) {
                level.setBlockAndUpdate(above, Blocks.SNOW.defaultBlockState());
            }
            resinNodes.add(new Vec3(ground.getX() + 0.5D, above.getY(), ground.getZ() + 0.5D));
        }

        long now = level.getGameTime();
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        Vec3 tipGround = !resinNodes.isEmpty() ? resinNodes.getLast() : eye.add(direction.scale(Math.min(8.0D, actualLen)));
        long tipPacked = BlockPos.containing(tipGround).asLong();
        long prevTick = cData.getLong(TAG_RESIN_TRAIL_LAST_TICK).orElse(0L);
        long prevPacked = cData.getLong(TAG_RESIN_TRAIL_LAST_POS).orElse(Long.MIN_VALUE);

        if (!resinNodes.isEmpty() && (now - prevTick >= 16L || tipPacked != prevPacked)) {
            cData.putLong(TAG_RESIN_TRAIL_LAST_TICK, now);
            cData.putLong(TAG_RESIN_TRAIL_LAST_POS, tipPacked);

            Vec3 mid = resinNodes.get(resinNodes.size() / 2);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, mid, Math.max(3.0D, actualLen * 0.55D), 80, now, spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        long cur = srv.getGameTime();
                        for (Vec3 node : resinNodes) {
                            if (elapsed % 8 == 0) {
                                srv.sendParticles(
                                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), FROZEN_AMBER_SAP_HEX_COLOR, 20, 0.92F, 0.04F),
                                        node.x, node.y + 0.12D, node.z, 2, 0.35D, 0.06D, 0.35D, 0.01D
                                );
                                srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                        node.x, node.y + 0.16D, node.z, 1, 0.35D, 0.08D, 0.35D, 0.01D);
                            }
                            AABB seg = new AABB(node.x - 1.25D, node.y - 1.0D, node.z - 1.25D,
                                    node.x + 1.25D, node.y + 1.8D, node.z + 1.25D);
                            for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, seg, e -> e.isAlive() && e != owner)) {
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                        foe, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                    continue;
                                }
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.45D, 25L);
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, false));
                                if (foe.canFreeze()) {
                                    foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 25, foe.getTicksFrozen() + 10));
                                }
                                CompoundTag fTag = EntityHelper.getPersistentData(foe);
                                fTag.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, cur + 20L);
                                fTag.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, Math.max(cur + 40L, fTag.getLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL).orElse(0L)));
                            }
                        }
                        return true;
                    }
            ));
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — BOREAL BLIZZARD NOVA (RADIAL BURST)
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
        // Section 6.20 Boreal Blizzard Nova:
        // Explosive 360-degree blast of whirling frosted pine needles and ice shards (6-block+ radius)
        // dealing Taiga Materia damage, knocking enemies back, inflicting Slowness III & Freezing for 4.0s (80t),
        // and blanketing the ground in snow layers and frost-rimed sweet berry bushes!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        freezeWaterAndSnuffFires(level, BlockPos.containing(center), (int) Math.ceil(r));

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            double dx = foe.getX() - center.x;
            double dz = foe.getZ() - center.z;
            if (dx * dx + dz * dz > (r + 0.6D) * (r + 0.6D)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float novaDmg = (3.4F + tier * 0.95F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }

            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 away = new Vec3(dx, 0.0D, dz);
                Vec3 norm = away.lengthSqr() > 1.0E-4D ? away.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(norm.x * 1.15D, 0.34D, norm.z * 1.15D));
            }
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2)); // Slowness III for 4.0s
            if (foe.canFreeze()) {
                foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 80); // Freezing for 4.0s
            }
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, level.getGameTime() + 100L);
        }

        // Blanket the ground in snow layers and frost-rimed sweet berry bushes!
        BlockPos cPos = BlockPos.containing(center);
        int br = Math.min(8, (int) Math.ceil(r));
        double rSq = (r + 0.4D) * (r + 0.4D);
        List<BlockPos> temporaryBushes = new ArrayList<>();
        BlockState matureBerryBush = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 2);

        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -2, -br), cPos.offset(br, 2, br))) {
            double dx = (p.getX() + 0.5D) - center.x;
            double dz = (p.getZ() + 0.5D) - center.z;
            if (dx * dx + dz * dz > rSq) continue;
            if (!level.getBlockState(p).isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))) continue;

            if (level.random.nextFloat() < 0.14F && matureBerryBush.canSurvive(level, p)) {
                level.setBlockAndUpdate(p, matureBerryBush);
                temporaryBushes.add(p.immutable());
            } else if (level.random.nextFloat() < 0.48F && Blocks.SNOW.defaultBlockState().canSurvive(level, p)) {
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }

        if (!temporaryBushes.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, r, 160, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) {
                            if (elapsed % 16 == 0) {
                                for (BlockPos bp : temporaryBushes) {
                                    if (srv.getBlockState(bp).is(Blocks.SWEET_BERRY_BUSH)) {
                                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                                bp.getX() + 0.5D, bp.getY() + 0.6D, bp.getZ() + 0.5D, 2, 0.25D, 0.25D, 0.25D, 0.01D);
                                    }
                                }
                            }
                            return true;
                        }
                        for (BlockPos bp : temporaryBushes) {
                            if (srv.getBlockState(bp).is(Blocks.SWEET_BERRY_BUSH)) {
                                srv.removeBlock(bp, false);
                            }
                        }
                        return false;
                    }
            ));
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 30, 1.10F, 0.07F),
                center.x, center.y + 0.8D, center.z, 34, r * 0.45D, 0.65D, r * 0.45D, 0.08D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                center.x, center.y + 0.9D, center.z, 36, r * 0.45D, 0.70D, r * 0.45D, 0.08D);
        level.playSound(null, cPos, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.82F);
        level.playSound(null, cPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.95F, 1.15F);
    }

    // =========================================================================
    // FORM MUTATION 4 — HEAT-SIPHON CONIFER TOUCH (TOUCH / STRIKE)
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
        // Section 6.20 Heat-Siphon Conifer Touch:
        // Drains target warmth with Taiga Materia damage + shivering Freezing,
        // and grants the caster +30% movement speed and temporary Frost Walker for 10 seconds (200t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        boolean drainedWarmth = false;

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.position().add(0.0D, caster.getBbHeight() * 0.58D, 0.0D),
                direction, 3.4D, EssenceType.TAIGA, EssenceType.FROZEN, tier
        );

        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            float cleaveScale = (i == 0) ? 1.0F : 0.50F;
            if (!slot.hasHarmVerb()) {
                float touchDmg = (3.6F + tier * 1.05F) * cleaveScale * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(level, hit, caster, touchDmg, true, true);
            }

            if (hit.canFreeze()) {
                hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 100);
            }
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, level.getGameTime() + 100L);
            drainedWarmth = true;
        }

        if (drainedWarmth) {
            SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.30D, 200L);
            EntityHelper.getPersistentData(caster)
                    .putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, level.getGameTime() + 200L);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), FROST_CYAN_HEX_COLOR, 26, 1.05F, 0.06F),
                    caster.getX(), caster.getY(0.65D), caster.getZ(), 14, 0.35D, 0.40D, 0.35D, 0.04D
            );
            level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.85F, 1.25F);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — WINTER SOLSTICE SHROUD (AURA / SELF)
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
        // Section 6.20 Winter Solstice Shroud:
        // Sub-zero blizzard mist that deflects 50% of incoming arrows, snuffs out fire instantly,
        // and regenerates health when standing in snow, ice, podzol, or cold/taiga biomes!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40; // 14s at Tier 1 up to 32s at Tier 10!
        long until = level.getGameTime() + dur;

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL, until);
        data.putInt("SpellTaigaShroudTier", tier);
        data.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
        caster.clearFire();
        caster.setTicksFrozen(0);

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 28, 1.00F, 0.06F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 18, 0.45D, 0.50D, 0.45D, 0.04D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                caster.getX(), caster.getY(0.90D), caster.getZ(), 22, 0.48D, 0.55D, 0.48D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.18F);
    }

    // =========================================================================
    // FORM MUTATION 6 — FROSTED CONIFER BRIAR TRAP (GLYPH / TRAP)
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
        // Section 6.20 Frosted Conifer Briar Trap:
        // Disarmable (Sneak + Right-Click for 50% refund) boreal ground rune (1s arming delay) that erupts into
        // frozen thorny spruce roots when stepped on, trapping the victim for 4.0s (80t), inflicting Freezing & Slowness IV,
        // and leaving behind a cluster of frost-rimed sweet berry bushes and powder snow!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.TAIGA, EssenceType.FROZEN, maxDuration, tier
        );
        level.playSound(null, surface.above(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 0.85F, 0.88F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    // Canonical 1.0s (20t) Glyph arming delay
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, zone.radius * 0.42D, 0.05D, zone.radius * 0.42D, 0.01D);
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (victims.isEmpty()) return true;

                    long now = srv.getGameTime();
                    for (LivingEntity v : victims) {
                        // Trap for 4.0 seconds (80 ticks) + Freezing + Slowness IV (amplifier 3)!
                        CompoundTag vTag = EntityHelper.getPersistentData(v);
                        vTag.putLong(SpellLingeringZoneManager.TAG_TAIGA_SAP_PIN_UNTIL, now + 80L);
                        vTag.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 80L);
                        vTag.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, now + 120L);
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3)); // Slowness IV
                        v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                        if (v.canFreeze()) {
                            v.setTicksFrozen(v.getTicksRequiredToFreeze() + 90);
                        }
                        SpellExecutionEngine.applySyncedMotion(v, Vec3.ZERO);

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float briarDmg = (3.4F + tier * 0.90F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(srv, v, owner, briarDmg, false, true);
                        }
                    }

                    // Erupt cluster of frost-rimed Sweet Berry Bushes & Powder Snow!
                    List<BlockPos> placedCluster = new ArrayList<>();
                    BlockPos basePos = BlockPos.containing(zone.center);
                    BlockState rimedBush = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 2);
                    for (BlockPos p : BlockPos.betweenClosed(basePos.offset(-2, -1, -2), basePos.offset(2, 1, 2))) {
                        if (p.distSqr(basePos) > 5.0D) continue;
                        if (!srv.getBlockState(p).isAir() || !srv.getBlockState(p.below()).isSolidRender()) continue;
                        if (SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, srv.getBlockState(p))) continue;

                        BlockState toPlace = (srv.random.nextBoolean() && rimedBush.canSurvive(srv, p))
                                ? rimedBush
                                : Blocks.POWDER_SNOW.defaultBlockState();
                        srv.setBlockAndUpdate(p, toPlace);
                        placedCluster.add(p.immutable());
                    }

                    if (!placedCluster.isEmpty()) {
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, zone.center, 2.5D, 140, now, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                                (hsrv, hzone, helapsed) -> {
                                    if (helapsed < hzone.maxDurationTicks) return true;
                                    for (BlockPos hp : placedCluster) {
                                        BlockState cur = hsrv.getBlockState(hp);
                                        if (cur.is(Blocks.SWEET_BERRY_BUSH) || cur.is(Blocks.POWDER_SNOW)) {
                                            hsrv.removeBlock(hp, false);
                                        }
                                    }
                                    return false;
                                }
                        ));
                    }

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.TAIGA, EssenceType.FROZEN, tier);
                    srv.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 28, 1.05F, 0.06F),
                            zone.center.x, zone.center.y + 0.5D, zone.center.z, 22, 1.1D, 0.45D, 1.1D, 0.06D
                    );
                    srv.sendParticles(ParticleTypes.SNOWFLAKE,
                            zone.center.x, zone.center.y + 0.5D, zone.center.z, 22, 1.1D, 0.45D, 1.1D, 0.06D);
                    srv.playSound(null, basePos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, 0.80F);
                    srv.playSound(null, basePos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 1.15F);

                    triggersLeft[0]--;
                    rearmCooldown[0] = 35;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — FROZEN SAP SPLATTER CONE (CONE / SPRAY)
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
        // Section 6.20 Frozen Sap Splatter Cone:
        // Sprays a shotgun-like spread of sticky sub-zero pine sap that glues enemies to the floor,
        // reducing movement speed by exact 60% and making them vulnerable (+50% shatter damage) to physical/blunt strikes!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D),
                direction, coneRange, EssenceType.TAIGA, EssenceType.FROZEN, tier
        );

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                float sapDmg = (2.9F + tier * 0.80F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(level, hit, caster, sapDmg, false, true);
            }

            // Exact 60% movement speed reduction + grounded glue + +50% shatter vulnerability!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.60D, 100L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
            if (hit.canFreeze()) {
                hit.setTicksFrozen(Math.min(hit.getTicksRequiredToFreeze() + 50, hit.getTicksFrozen() + 65));
            }
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 100L);
            data.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, now + 140L);
            Vec3 v = hit.getDeltaMovement();
            SpellExecutionEngine.applySyncedMotion(hit, new Vec3(v.x * 0.25D, Math.min(0.0D, v.y), v.z * 0.25D));
        }

        // Lingering 4.5s (90t) sticky frozen sap slick across the forward cone footprint
        Vec3 slickCenter = caster.position().add(direction.scale(Math.max(2.5D, coneRange * 0.50D)));
        double slickR = Math.max(3.5D, coneRange * 0.52D);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, slickCenter, slickR, 90, now, spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), FROZEN_AMBER_SAP_HEX_COLOR, 22, 0.95F, 0.04F),
                                zone.center.x, zone.center.y + 0.18D, zone.center.z, 8, zone.radius * 0.45D, 0.08D, zone.radius * 0.45D, 0.01D
                        );
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, zone.center.y + 0.25D, zone.center.z, 8, zone.radius * 0.45D, 0.12D, zone.radius * 0.45D, 0.01D);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    long cur = srv.getGameTime();
                    AABB slickBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.0D, zone.center.z + zone.radius
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, slickBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                foe, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 25L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, false));
                        CompoundTag fd = EntityHelper.getPersistentData(foe);
                        fd.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, cur + 20L);
                        fd.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL,
                                Math.max(fd.getLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL).orElse(0L), cur + 80L));
                    }
                    return true;
                }
        ));

        level.playSound(null, caster.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 1.0F, 0.78F);
        level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.80F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 8 — FALLING GLACIAL TIMBER CATACLYSM (RAIN / SKYFALL)
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
        // Section 6.20 Falling Glacial Timber Cataclysm:
        // Summons a 6.0-second (120t) tier-scaled barrage of petrified frozen spruce logs and heavy icicles
        // crashing down from a cave-safe Boreal Sky-Rift.
        // During the 6-second barrage, falling & landed trunks/icicles are rendered as 3D Skyfall Pseudoblocks
        // (and push entities clear as obstacle barricades without placing real world blocks mid-spell);
        // on the exact tick the Skyfall completes (tick 120), the actual SPRUCE_LOG, PACKED_ICE & BLUE_ICE
        // barricade blocks materialize seamlessly in the world!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.8D, radius);
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        BlockPos baseAir = surface.above();
        Vec3 snappedCenter = new Vec3(surface.getX() + 0.5D, baseAir.getY(), surface.getZ() + 0.5D);
        double riftHeight = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);
        int totalTicks = 120; // Canonical 6.0 seconds
        int fallTicks = 18;   // Crisp 0.9s gravitational drop per wave

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, snappedCenter, r, EssenceType.TAIGA, EssenceType.FROZEN, totalTicks, tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snappedCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.TAIGA, EssenceType.FROZEN, totalTicks, tier
        );
        level.playSound(null, baseAir, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.15F, 0.68F);

        List<PlannedTimberColumn> plannedColumns = new ArrayList<>();
        Set<Long> reservedFootprints = new HashSet<>();
        int columnsPerWave = 2 + (tier + 1) / 2; // 3 per wave (18 total) at Tier 1 up to 7 per wave (42 total!) at Tier 10
        int maxColCap = Math.clamp((int) Math.floor(riftHeight) - 1, 2, 2 + (tier + 1) / 2);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, r, totalTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        // Skyfall complete! Solidify all standing 3D Pseudoblock trunks & icicle monoliths into real world barricade blocks!
                        for (PlannedTimberColumn col : plannedColumns) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, col.basePos(), col.height(), true);
                            for (int y = 0; y < col.height(); y++) {
                                BlockPos p = col.basePos().above(y);
                                if (canPlaceTimberBarricadeAt(srv, p)) {
                                    srv.setBlockAndUpdate(p, col.blockState());
                                } else if (y > 0) {
                                    break;
                                }
                            }
                        }
                        srv.playSound(null, baseAir, SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.10F, 0.72F);
                        srv.playSound(null, baseAir, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.00F, 0.85F);
                        return false;
                    }

                    // Ambient boreal blizzard particles inside the tier-scaled storm cylinder
                    if (elapsed % 5 == 0) {
                        double snowY = zone.center.y + Math.min(riftHeight * 0.70D, 12.0D);
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                zone.center.x, snowY, zone.center.z, 12 + tier * 2, zone.radius * 0.48D, 1.2D, zone.radius * 0.48D, 0.06D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 26, 0.95F, 0.06F),
                                zone.center.x, snowY * 0.85D, zone.center.z, 8 + tier, zone.radius * 0.45D, 1.0D, zone.radius * 0.45D, 0.05D
                        );
                    }

                    // Every 20 ticks (0, 20, 40, 60, 80, 100): launch a new wave of falling 3D Pseudoblock Spruce Logs & Heavy Icicles!
                    if (elapsed % 20 == 0 && elapsed <= totalTicks - fallTicks) {
                        for (int i = 0; i < columnsPerWave; i++) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double minRing = Math.min(1.5D, zone.radius * 0.22D);
                            double dist = minRing + Math.sqrt(srv.random.nextDouble()) * Math.max(0.8D, zone.radius * 0.88D - minRing);
                            BlockPos cand = BlockPos.containing(zone.center.x + Math.cos(ang) * dist, zone.center.y, zone.center.z + Math.sin(ang) * dist);
                            BlockPos gSurf = findGroundSurfaceNear(srv, cand);
                            BlockPos colBase = gSurf.above();
                            long xzKey = BlockPos.asLong(colBase.getX(), 0, colBase.getZ());
                            if (reservedFootprints.contains(xzKey) || !canPlaceTimberBarricadeAt(srv, colBase)) {
                                continue;
                            }
                            reservedFootprints.add(xzKey);

                            boolean icicle = (i % 3 == 2);
                            BlockState colState = icicle
                                    ? ((tier >= 6 && srv.random.nextBoolean()) ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState())
                                    : Blocks.SPRUCE_LOG.defaultBlockState();
                            int colH = Math.clamp(2 + srv.random.nextInt(Math.max(1, maxColCap - 1)), 2, maxColCap);
                            double localClearance = SpellDeliveryForm.probeCaveSafeSkyfallHeight(srv, colBase, tier);
                            colH = Math.min(colH, Math.max(1, (int) Math.floor(localClearance) - 1));

                            Vec3 skyOrigin = new Vec3(
                                    colBase.getX() + 0.5D,
                                    colBase.getY() + Math.max(2.5D, localClearance - colH * 0.4D),
                                    colBase.getZ() + 0.5D
                            );
                            int holdTicks = Math.max(2, zone.maxDurationTicks - elapsed - fallTicks);
                            SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                    srv, skyOrigin, colBase, colState, colH, fallTicks, holdTicks, EssenceType.TAIGA, tier
                            );
                            plannedColumns.add(new PlannedTimberColumn(colBase.immutable(), colState, colH, elapsed));
                        }
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    // Check pseudoblocks that JUST slammed into the ground on this tick + maintain obstacle pushout on landed pseudoblocks!
                    boolean waveJustCrashed = false;
                    for (PlannedTimberColumn col : plannedColumns) {
                        int landTick = col.dropTick() + fallTicks;
                        if (elapsed == landTick) {
                            waveJustCrashed = true;
                            Vec3 hitPos = new Vec3(col.basePos().getX() + 0.5D, col.basePos().getY() + 0.1D, col.basePos().getZ() + 0.5D);
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, col.basePos(), col.height(), true);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, hitPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.TAIGA, EssenceType.FROZEN, 1.25F, tier
                            );
                            srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, col.blockState()),
                                    hitPos.x, hitPos.y + 0.5D, hitPos.z, 14, 0.45D, 0.45D, 0.45D, 0.12D);

                            // Heavy direct-impact crush around each crashing timber/icicle column!
                            AABB localCrush = new AABB(col.basePos()).inflate(2.35D, 2.5D, 2.35D);
                            float directDmg = (3.8F + tier * 1.15F) * Math.max(0.5F, zone.apparatusPotency);
                            for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, localCrush, e -> e.isAlive() && e != owner)) {
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                                SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(srv, foe, owner, directDmg, false, true);
                                if (foe.canFreeze()) {
                                    foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 50, foe.getTicksFrozen() + 50));
                                }
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                            }
                        } else if (elapsed > landTick) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, col.basePos(), col.height(), false);
                        }
                    }

                    // Storm-wide barrage impact wave every 20 ticks starting at first touchdown (tick 18)
                    if (waveJustCrashed) {
                        freezeWaterAndSnuffFires(srv, baseAir, Math.min(14, (int) Math.ceil(zone.radius)));
                        AABB stormBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.0D, riftHeight * 0.65D), zone.center.z + zone.radius
                        );
                        float barrageDmg = (2.4F + tier * 0.72F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, stormBox, e -> e.isAlive() && e != owner)) {
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > (zone.radius + 0.6D) * (zone.radius + 0.6D)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            if (v.canFreeze()) {
                                v.setTicksFrozen(Math.min(v.getTicksRequiredToFreeze() + 45, v.getTicksFrozen() + 45));
                            }
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                            EntityHelper.getPersistentData(v).putLong(
                                    SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, srv.getGameTime() + 80L);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.50F
                                );
                            }
                            if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(srv, v, owner, barrageDmg, false, true);
                            }
                        }
                        srv.playSound(null, baseAir, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 0.90F, 0.74F);
                        srv.playSound(null, baseAir, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.80F, 1.08F);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — BOREAL HUNTER EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        tag.putString("SpellEidolonMateria", EssenceType.TAIGA.name());
        tag.putInt("SpellEidolonTintRGB", EVERGREEN_PINE_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.TAIGA, EssenceType.FROZEN, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 32, 1.10F, 0.06F),
                spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.04D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.24F);
    }

    // =========================================================================
    // CORE EFFECT SYNERGIES — BLINK, TRANSMUTE / BREAK, AFFLICTION & RESTORATION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.20 Boreal Pine-Phasing Blink:
        // Disintegrate into a swirling blizzard of pine needles & teleport 14+ blocks away,
        // leaving a frosted decoy bush at the point of departure that lures & chills nearby hostiles!
        BlockPos depGround = findGroundSurfaceNear(level, BlockPos.containing(departurePos));
        BlockPos bushPos = depGround.above().immutable();
        List<BlockPos> placedDecoyBlocks = new ArrayList<>();

        BlockState frostedBush = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 2);
        if (level.getBlockState(bushPos).isAir() && frostedBush.canSurvive(level, bushPos)) {
            level.setBlockAndUpdate(bushPos, frostedBush);
            placedDecoyBlocks.add(bushPos);
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos snowRim = bushPos.relative(d);
            if (level.getBlockState(snowRim).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, snowRim)) {
                level.setBlockAndUpdate(snowRim, Blocks.SNOW.defaultBlockState());
                placedDecoyBlocks.add(snowRim.immutable());
            }
        }

        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.TAIGA, potencyTier);
        for (Vec3 pt : List.of(departurePos, arrivalPos)) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 28, 1.05F, 0.07F),
                    pt.x, pt.y + 0.95D, pt.z, 22, 0.42D, 0.55D, 0.42D, 0.06D
            );
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    pt.x, pt.y + 0.95D, pt.z, 24, 0.42D, 0.55D, 0.42D, 0.06D);
        }
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.28F);

        AABB lureBox = new AABB(
                departurePos.x - 5.5D, departurePos.y - 2.0D, departurePos.z - 5.5D,
                departurePos.x + 5.5D, departurePos.y + 2.8D, departurePos.z + 5.5D
        );
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 5.5D, 80, level.getGameTime(), dummy, dummy.subSpells().getFirst(), EssenceType.TAIGA, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        for (BlockPos bp : placedDecoyBlocks) {
                            BlockState cur = srv.getBlockState(bp);
                            if (cur.is(Blocks.SWEET_BERRY_BUSH) || cur.is(Blocks.SNOW)) {
                                srv.removeBlock(bp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE,
                                bushPos.getX() + 0.5D, bushPos.getY() + 0.65D, bushPos.getZ() + 0.5D, 4, 0.3D, 0.3D, 0.3D, 0.02D);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, lureBox, LivingEntity::isAlive)) {
                        if (foe == owner || (owner != null && foe.isAlliedTo(owner))) continue;
                        if (foe instanceof Mob mob && mob instanceof Monster && elapsed % 10 == 0) {
                            mob.setTarget(null);
                            mob.getNavigation().moveTo(bushPos.getX() + 0.5D, bushPos.getY(), bushPos.getZ() + 0.5D, 1.15D);
                        }
                        if (foe.position().distanceToSqr(departurePos) <= 4.5D) {
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.45D, 35L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 2));
                            if (foe.canFreeze()) {
                                foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 35, foe.getTicksFrozen() + 14));
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.20 Boreal Timber & Podzol Transmute:
        // Converts oak, birch, and jungle wood and leaves into spruce variants, grass into podzol, and standing water into packed ice!
        BlockState out = resolveBorealBlockTransmute(state);
        if (out == null) {
            return false;
        }
        level.setBlockAndUpdate(pos, out);
        level.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
                4, 0.25D, 0.25D, 0.25D, 0.02D);
        maybeGrantTaigaBatchTransmuteDiscount(level, caster, potencyTier);
        return true;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack in = itemEntity.getItem();
        ItemStack out = resolveBorealItemTransmute(in);
        if (out.isEmpty()) {
            return false;
        }
        itemEntity.setItem(out);
        level.sendParticles(ParticleTypes.SNOWFLAKE, itemEntity.getX(), itemEntity.getY() + 0.25D, itemEntity.getZ(),
                5, 0.2D, 0.2D, 0.2D, 0.02D);
        maybeGrantTaigaBatchTransmuteDiscount(level, caster, potencyTier);
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.20 Petrifying Frost-Sap Hex:
        // Persistent sub-zero resin curse (milk-immune, cleansed via Fire/Restoration magic or caster defeat):
        // permanently inflicts Freezing & Slowness III, prevents sprinting, and increases physical/shatter damage by +50%!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.setSprinting(false);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 2, true, true)); // Slowness III
        if (target.canFreeze()) {
            target.setTicksFrozen(target.getTicksRequiredToFreeze() + 50);
        }

        float sapHexDmg = (1.25F + potencyTier * 0.42F) * mult;
        SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(level, target, null, sapHexDmg, false, true);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_DRIP.get(), FROZEN_AMBER_SAP_HEX_COLOR, 20, 0.90F, 0.04F),
                target.getX(), target.getY(0.65D), target.getZ(), 4, 0.25D, 0.35D, 0.25D, 0.02D
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                target.getX(), target.getY(0.80D), target.getZ(), 5, 0.28D, 0.35D, 0.28D, 0.02D);
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
        if (effect == SpellCoreEffect.RESTORATION) {
            // Section 6.20 Evergreen Resilience Restoration:
            // Grants Regeneration II, cures active burning and hunger debuffs,
            // and provides 30 seconds (600t) of complete immunity to cold and freezing damage!
            CompoundTag tData = EntityHelper.getPersistentData(target);
            tData.remove(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL);
            tData.putLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL, level.getGameTime() + 600L);

            target.clearFire();
            target.setTicksFrozen(0);
            target.isInPowderSnow = false;
            target.removeEffect(MobEffects.HUNGER);
            target.removeEffect(MobEffects.SLOWNESS);

            int regenDur = 120 + potencyTier * 20;
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDur, 1)); // Regeneration II

            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), EVERGREEN_PINE_HEX_COLOR, 28, 1.05F, 0.06F),
                    target.getX(), target.getY(0.85D), target.getZ(), 14, 0.38D, 0.45D, 0.38D, 0.04D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), FROST_CYAN_HEX_COLOR, 28, 1.05F, 0.06F),
                    target.getX(), target.getY(0.90D), target.getZ(), 14, 0.38D, 0.45D, 0.38D, 0.04D
            );
            level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.32F);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    @Nullable
    private static BlockState resolveBorealBlockTransmute(BlockState state) {
        // Oak, Birch, and Jungle wood, logs, stripped variants, planks, saplings & leaves -> Spruce variants!
        if (state.is(Blocks.OAK_LOG) || state.is(Blocks.BIRCH_LOG) || state.is(Blocks.JUNGLE_LOG)) {
            return copyAxisIfPresent(state, Blocks.SPRUCE_LOG.defaultBlockState());
        }
        if (state.is(Blocks.STRIPPED_OAK_LOG) || state.is(Blocks.STRIPPED_BIRCH_LOG) || state.is(Blocks.STRIPPED_JUNGLE_LOG)) {
            return copyAxisIfPresent(state, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        }
        if (state.is(Blocks.OAK_WOOD) || state.is(Blocks.BIRCH_WOOD) || state.is(Blocks.JUNGLE_WOOD)) {
            return copyAxisIfPresent(state, Blocks.SPRUCE_WOOD.defaultBlockState());
        }
        if (state.is(Blocks.STRIPPED_OAK_WOOD) || state.is(Blocks.STRIPPED_BIRCH_WOOD) || state.is(Blocks.STRIPPED_JUNGLE_WOOD)) {
            return copyAxisIfPresent(state, Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());
        }
        if (state.is(Blocks.OAK_PLANKS) || state.is(Blocks.BIRCH_PLANKS) || state.is(Blocks.JUNGLE_PLANKS)) {
            return Blocks.SPRUCE_PLANKS.defaultBlockState();
        }
        if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.JUNGLE_LEAVES)
                || (state.is(BlockTags.LEAVES) && !state.is(Blocks.SPRUCE_LEAVES))) {
            return Blocks.SPRUCE_LEAVES.defaultBlockState();
        }
        if (state.is(Blocks.OAK_SAPLING) || state.is(Blocks.BIRCH_SAPLING) || state.is(Blocks.JUNGLE_SAPLING)
                || (state.is(BlockTags.SAPLINGS) && !state.is(Blocks.SPRUCE_SAPLING))) {
            return Blocks.SPRUCE_SAPLING.defaultBlockState();
        }
        // Grass / Dirt -> Podzol
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MYCELIUM)) {
            return Blocks.PODZOL.defaultBlockState();
        }
        // Standing Water -> Packed Ice
        if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }
        return null;
    }

    private static BlockState copyAxisIfPresent(BlockState from, BlockState to) {
        if (from.hasProperty(BlockStateProperties.AXIS) && to.hasProperty(BlockStateProperties.AXIS)) {
            return to.setValue(BlockStateProperties.AXIS, from.getValue(BlockStateProperties.AXIS));
        }
        return to;
    }

    private static ItemStack resolveBorealItemTransmute(ItemStack in) {
        int c = in.getCount();
        if (in.is(Items.OAK_LOG) || in.is(Items.BIRCH_LOG) || in.is(Items.JUNGLE_LOG)) return new ItemStack(Items.SPRUCE_LOG, c);
        if (in.is(Items.STRIPPED_OAK_LOG) || in.is(Items.STRIPPED_BIRCH_LOG) || in.is(Items.STRIPPED_JUNGLE_LOG)) return new ItemStack(Items.STRIPPED_SPRUCE_LOG, c);
        if (in.is(Items.OAK_WOOD) || in.is(Items.BIRCH_WOOD) || in.is(Items.JUNGLE_WOOD)) return new ItemStack(Items.SPRUCE_WOOD, c);
        if (in.is(Items.STRIPPED_OAK_WOOD) || in.is(Items.STRIPPED_BIRCH_WOOD) || in.is(Items.STRIPPED_JUNGLE_WOOD)) return new ItemStack(Items.STRIPPED_SPRUCE_WOOD, c);
        if (in.is(Items.OAK_PLANKS) || in.is(Items.BIRCH_PLANKS) || in.is(Items.JUNGLE_PLANKS)) return new ItemStack(Items.SPRUCE_PLANKS, c);
        if (in.is(Items.OAK_LEAVES) || in.is(Items.BIRCH_LEAVES) || in.is(Items.JUNGLE_LEAVES)) return new ItemStack(Items.SPRUCE_LEAVES, c);
        if (in.is(Items.OAK_SAPLING) || in.is(Items.BIRCH_SAPLING) || in.is(Items.JUNGLE_SAPLING)) return new ItemStack(Items.SPRUCE_SAPLING, c);
        if (in.is(Items.GRASS_BLOCK) || in.is(Items.DIRT) || in.is(Items.COARSE_DIRT) || in.is(Items.MYCELIUM)) return new ItemStack(Items.PODZOL, c);
        if (in.is(Items.WATER_BUCKET)) return new ItemStack(Items.PACKED_ICE, c);
        if (in.is(Items.ICE)) return new ItemStack(Items.PACKED_ICE, c);
        return ItemStack.EMPTY;
    }

    private static void maybeGrantTaigaBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellTaigaBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellTaigaBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellTaigaBatchTick", now);
        pData.putInt("SpellTaigaBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.TAIGA, refund);
        }
    }

    private static boolean canPlaceTimberBarricadeAt(ServerLevel level, BlockPos pos) {
        BlockState cur = level.getBlockState(pos);
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, cur)) {
            return false;
        }
        return cur.isAir()
                || cur.canBeReplaced()
                || cur.is(Blocks.SNOW)
                || cur.is(Blocks.POWDER_SNOW)
                || cur.is(Blocks.SWEET_BERRY_BUSH);
    }

    private static void freezeWaterAndSnuffFires(ServerLevel level, BlockPos center, int r) {
        double rSq = (r + 0.5D) * (r + 0.5D);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            if (p.distSqr(center) > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER) && bs.getFluidState().isSource()) {
                level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof AbstractCandleBlock && bs.getValue(AbstractCandleBlock.LIT)) {
                AbstractCandleBlock.extinguish(null, bs, level, p);
            }
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -5; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && (!bs.getCollisionShape(level, check).isEmpty() || bs.is(Blocks.WATER)))
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return start.below();
    }
}
