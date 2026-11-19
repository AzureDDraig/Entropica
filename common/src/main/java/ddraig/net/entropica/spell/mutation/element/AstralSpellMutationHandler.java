package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Astral Materia (Ethereal &amp; Paradox Fusion #27)</b>
 * (Spellcrafting System Guide Section 6.28 — Dual Void {@code #41304E} + Lightning {@code #FFE600} [Leans: Void]).
 */
public final class AstralSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Cosmic Violet (#41304E) primary tint. */
    public static final int COSMIC_VIOLET_HEX_COLOR = 0x41304E;

    /** Canonical Electric Gold-Yellow (#FFE600) pulsar accent tint. */
    public static final int PULSAR_GOLD_HEX_COLOR = 0xFFE600;

    private static final String TAG_ASTRAL_BEAM_LAST_TICK = "SpellAstralBeamLastTick";
    private static final String TAG_ASTRAL_BEAM_TICKS = "SpellAstralBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.ASTRAL;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Supercharged Void-Warp Chains Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Celestial Comet Missile";
            case BEAM -> "Pulsar Tractor Ray";
            case RADIAL_BURST -> "Supernova Gravitational Wave";
            case TOUCH_STRIKE -> "Cosmic Overcharge Strike";
            case AURA -> "Cosmic Starlight Shroud";
            case GLYPH -> "Cosmic Vortex Singularity Mine";
            case CONE -> "Pulsar Arc Spray";
            case SKYFALL -> "Celestial Meteor-Storm Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Astral Comet Dash";
            case TRANSMUTE, BREAK -> "Cosmic Disintegration Transmute";
            case AFFLICTION -> "Gravitational Singularity Tether Hex";
            default -> "Astral Cosmic " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — SUPERCHARGED VOID-WARP CHAINS FLOW
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
        // Section 6.28 Supercharged Void-Warp Chains Flow:
        // Phases through solid terrain & walls via micro-wormholes, forks 1 -> 2 -> 4 targets,
        // inflicts a 1.0s (20t) shock stun per leap, and pulls nearby mobs inward!
        int tier = spell.getEffectiveTier();
        toTarget.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
        SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(
                level, toTarget, caster, 1.35F + tier * 0.40F, false, true
        );

        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, toTarget.getBoundingBox().inflate(3.5D))) {
            if (near == toTarget || near == caster || !near.isAlive()) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, near)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, wardSafe)) continue;
            Vec3 delta = toTarget.position().subtract(near.position());
            Vec3 inward = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(0.48D) : Vec3.ZERO;
            near.setDeltaMovement(near.getDeltaMovement().add(inward.x, 0.16D, inward.z));
            near.hasImpulse = true;
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), PULSAR_GOLD_HEX_COLOR, 16, 0.92F, 0.06F),
                toTarget.getX(), toTarget.getY(0.85D), toTarget.getZ(), 10, 0.28D, 0.32D, 0.28D, 0.04D
        );
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, toTarget.getX(), toTarget.getY(0.7D), toTarget.getZ(), 8, 0.25D, 0.30D, 0.25D, 0.05D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), COSMIC_VIOLET_HEX_COLOR, 16, 0.80F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D
        );
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 1 — CELESTIAL COMET MISSILE (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.28 Celestial Comet Missile: calibrated arcing star-matter comet (#41304E / #FFE600)
        // that detonates into a starfall crater on impact (preserving wall-phasing if VOID_WARP flow is explicitly active)!
        projectile.enableSlightFluidArc(false);
        boolean explicitVoidWarpFlow = spell.flowPart().flow() == SpellFlowModifier.VOID_WARP
                || (spell.flowPart().mutationEnabled() && spell.flowPart().materiaType() == EssenceType.VOID);
        if (!explicitVoidWarpFlow) {
            projectile.setWallPhasing(false);
        }
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), COSMIC_VIOLET_HEX_COLOR, 18, 0.85F, 0.05F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.02D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), PULSAR_GOLD_HEX_COLOR, 14, 0.82F, 0.06F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.03D
        );
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
        detonateStarfallComet(level, caster, hitEntity.position(), spell, slot);
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
        detonateStarfallComet(level, caster, blockHit.getLocation(), spell, slot);
    }

    private static void detonateStarfallComet(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 pos,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.28 Celestial Comet Missile:
        // Creates an explosive starfall crater blast on impact, dealing Astral Materia damage,
        // launching struck enemies 5 blocks airborne (vy = 0.92D), and powering redstone wire for 3s (60t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float starfallDmg = (slot != null && slot.hasHarmVerb())
                ? (1.75F + tier * 0.48F)
                : (3.25F + tier * 0.85F);

        SpellWorldVfxManager.broadcastRadialNova(level, pos, 3.0D, EssenceType.ASTRAL, EssenceType.LIGHTNING, tier);
        AABB box = new AABB(pos.x - 3.0D, pos.y - 1.5D, pos.z - 3.0D, pos.x + 3.0D, pos.y + 2.6D, pos.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, foe, caster, starfallDmg, false, true);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.40D, 0.92D, foe.getDeltaMovement().z * 0.40D);
            foe.hasImpulse = true;
        }

        SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, BlockPos.containing(pos), 3, 60);
        level.sendParticles(ParticleTypes.FIREWORK, pos.x, pos.y + 0.4D, pos.z, 22, 0.75D, 0.45D, 0.75D, 0.10D);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 2 — PULSAR TRACTOR RAY (BEAM)
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
        // Section 6.28 Pulsar Tractor Ray:
        // Lifts enemies into mid-air and pulls them toward the caster while shocking them;
        // channeling for 3.0s (60 contact ticks = 15 pulses) triggers a Stellar Supernova Flash
        // that stuns targets for 2.0s (40t) and powers redstone components for 4.0s (80t)!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            Vec3 toCaster = caster.position().subtract(hit.position());
            Vec3 pull = toCaster.lengthSqr() > 2.25D ? toCaster.normalize().scale(0.45D) : Vec3.ZERO;
            hit.setDeltaMovement(pull.x, 0.28D, pull.z);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));

            if (!slot.hasHarmVerb()) {
                float shockDmg = (1.15F + tier * 0.34F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, hit, caster, shockDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_ASTRAL_BEAM_LAST_TICK).orElse(-100L);
            int accumTicks = (now - last <= 12L) ? data.getInt(TAG_ASTRAL_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_ASTRAL_BEAM_LAST_TICK, now);

            if (accumTicks >= 60) {
                data.putInt(TAG_ASTRAL_BEAM_TICKS, 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(
                        level, hit, caster, (3.25F + tier * 0.85F) * Math.max(0.5F, apparatusPotency), false, true
                );
                SpellLingeringZoneManager.pulseTemporaryRedstonePower(level, hit.blockPosition(), 4, 80);
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 3.6D, EssenceType.ASTRAL, EssenceType.LIGHTNING, tier);
                level.playSound(null, hit.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.35F);
            } else {
                data.putInt(TAG_ASTRAL_BEAM_TICKS, accumTicks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SUPERNOVA GRAVITATIONAL WAVE (RADIAL BURST)
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
        // Section 6.28 Supernova Gravitational Wave:
        // 360-degree blast of cosmic plasma (6m+ radius, #41304E / #FFE600); launches all nearby hostiles
        // 8 blocks backward and 4 blocks airborne while disabling their abilities for 3.0s (60t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.2D, center.z + r);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = foe.position().subtract(center);
            Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
            Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize().scale(1.88D) : direction.scale(1.88D);
            foe.setDeltaMovement(away.x, 0.80D, away.z);
            foe.hasImpulse = true;
            SpellLingeringZoneManager.applyArcaneSilence(foe, 60);

            if (!slot.hasHarmVerb()) {
                float waveDmg = (3.40F + tier * 0.88F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, foe, caster, waveDmg, false, true);
            }
        }

        level.sendParticles(ParticleTypes.FIREWORK, center.x, center.y + 0.9D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.14D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 24, 2.0D, 0.5D, 2.0D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.72F);
    }

    // =========================================================================
    // FORM MUTATION 4 — COSMIC OVERCHARGE STRIKE (TOUCH / STRIKE)
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
        // Section 6.28 Cosmic Overcharge Strike:
        // Deals Astral Materia damage, stuns target for 1.0s (20t), arcs sparks to up to 2 nearby enemies
        // within 3m, launches target 4 blocks airborne into zero-g float for 3.0s (60t), and grants caster
        // +40% attack speed for 10.0s (200t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(caster, level, 0.40D, 200L);

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
            hit.setDeltaMovement(direction.x * 0.35D, 0.80D, direction.z * 0.35D);
            hit.hasImpulse = true;

            if (!slot.hasHarmVerb()) {
                float strikeDmg = (2.85F + tier * 0.75F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }

            Vec3 hitMid = hit.position().add(0.0D, hit.getBbHeight() * 0.55D, 0.0D);
            int arced = 0;
            for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.0D))) {
                if (near == hit || near == caster || !near.isAlive()) continue;
                if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, near)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, wardSafe)) continue;

                Vec3 nearMid = near.position().add(0.0D, near.getBbHeight() * 0.55D, 0.0D);
                SpellWorldVfxManager.broadcastChainArc(level, hitMid, nearMid, EssenceType.ASTRAL, EssenceType.LIGHTNING, tier);
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(
                        level, near, caster, (2.60F + tier * 0.75F) * Math.max(0.5F, apparatusPotency), true, true
                );
                near.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 14, 0));
                if (++arced >= 2) break;
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — COSMIC STARLIGHT SHROUD (AURA / SELF)
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
        // Section 6.28 Cosmic Starlight Shroud:
        // Crackling #41304E / #FFE600 shroud granting high-float jumping, zero-g glide slow-fall,
        // +30% sprint speed, 0.5s melee shock stun + 3-block knockback, and Lightning/Void damage-to-fuel absorption!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ASTRAL_SHROUD_UNTIL, until);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.30D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), PULSAR_GOLD_HEX_COLOR, 22, 0.95F, 0.06F),
                caster.getX(), caster.getY(1.0D), caster.getZ(), 14, 0.42D, 0.52D, 0.42D, 0.04D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 6 — COSMIC VORTEX SINGULARITY MINE (GLYPH / TRAP)
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
        // Section 6.28 Cosmic Vortex Singularity Mine:
        // Disarmable Single/Repeating starlight rune (1.0s arming delay) erupting into a 4m gravitational vortex:
        // pulls mobs to center, suspends them 3m in mid-air for 3.0s (60t) while shocking them with Astral Materia
        // damage, and emits a level-15 redstone pulse for 5.0s (100t)!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double mineRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), mineRad, EssenceType.ASTRAL, EssenceType.LIGHTNING, Math.min(240, maxDuration), tier
        );
        level.playSound(null, surface.above(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 0.85F, 1.15F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, mineRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.ASTRAL, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.ASTRAL, EssenceType.LIGHTNING, rem, tier
                        );
                    }
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 2.8D, zone.center.y - 1.0D, zone.center.z - 2.8D,
                            zone.center.x + 2.8D, zone.center.y + 2.2D, zone.center.z + 2.8D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (triggers.isEmpty()) return true;

                    // Emit level-15 redstone pulse for 5.0s (100t) & spawn 3.0s (60t) 3m-high gravitational suspension vortex!
                    SpellLingeringZoneManager.pulseTemporaryRedstonePower(srv, BlockPos.containing(zone.center), 4, 100);
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 2.2D, 0.0D), zone.radius, true, EssenceType.ASTRAL, EssenceType.LIGHTNING, 60, tier
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.78F);

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, zone.radius, 60, srv.getGameTime(),
                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (vLvl, vZone, vEl) -> {
                                if (vEl > vZone.maxDurationTicks) return false;
                                LivingEntity vOwner = vZone.resolveCaster(vLvl);
                                boolean vWard = vZone.spell != null && vZone.spell.hasWardFriendlyFireSafeguard();
                                AABB vortexBox = new AABB(
                                        vZone.center.x - vZone.radius, vZone.center.y - 1.0D, vZone.center.z - vZone.radius,
                                        vZone.center.x + vZone.radius, vZone.center.y + 5.2D, vZone.center.z + vZone.radius
                                );
                                Vec3 hoverTarget = vZone.center.add(0.0D, 3.0D, 0.0D);
                                for (LivingEntity v : vLvl.getEntitiesOfClass(LivingEntity.class, vortexBox, LivingEntity::isAlive)) {
                                    if (v == vOwner || SpellExecutionEngine.isFriendlyAllyForOasis(vOwner, v)) continue;
                                    if (vOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, vOwner, vWard)) continue;

                                    Vec3 pull = hoverTarget.subtract(v.position()).scale(0.30D);
                                    v.setDeltaMovement(pull);
                                    v.resetFallDistance();
                                    v.hasImpulse = true;

                                    if (vEl % 20 == 0) {
                                        if (vZone.spell != null && vZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    vLvl, vOwner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                                    vZone.spell, vZone.slot, vZone.fallbackMateria, vZone.apparatusPotency * 0.45F
                                            );
                                        }
                                        float shockDmg = (1.65F + tier * 0.45F) * Math.max(0.5F, vZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(vLvl, v, vOwner, shockDmg, false, true);
                                    }
                                }
                                if (vEl % 5 == 0) {
                                    vLvl.sendParticles(
                                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), PULSAR_GOLD_HEX_COLOR, 14, 0.90F, 0.06F),
                                            vZone.center.x, vZone.center.y + 2.8D, vZone.center.z, 8, 1.1D, 0.9D, 1.1D, 0.05D
                                    );
                                }
                                return true;
                            }
                    ));

                    triggersLeft[0]--;
                    rearmCooldown[0] = 65;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — PULSAR ARC SPRAY (CONE / SPRAY)
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
        // Section 6.28 Pulsar Arc Spray (Cosmic Tesla Shotgun):
        // Wide 60° spread of branching cosmic lightning & void distortion; shocks enemies with a 1.0s (20t) stun,
        // knocks targets 4 blocks backward, strips positive potion buffs, and powers redstone crossed for 3.0s (60t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            hit.setDeltaMovement(direction.x * 1.08D, 0.32D, direction.z * 1.08D);
            hit.hasImpulse = true;
            EclipseSpellMutationHandler.purgeBeneficialEffects(hit);

            if (!slot.hasHarmVerb()) {
                float arcDmg = (2.75F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, hit, caster, arcDmg, false, true);
            }
        }

        int steps = Math.min(9, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            SpellLingeringZoneManager.pulseTemporaryRedstonePower(
                    level, BlockPos.containing(caster.position().add(direction.scale(s))), 2, 60
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 8 — CELESTIAL METEOR-STORM CATACLYSM (SKYFALL)
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
        // Section 6.28 Celestial Meteor-Storm Cataclysm:
        // Crackling cosmic starlight rift for 10.0s (200t, 14m+ radius) raining falling star comets and
        // cosmic lightning bolts; repeatedly stuns hostiles for 0.5s (10t) per strike, lifts enemies into
        // zero-g float, and smashes them down for heavy Astral Materia impact damage when the rift expires!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.ASTRAL, EssenceType.LIGHTNING, 200, tier
        );
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.1F, 0.70F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.ASTRAL, apparatusPotency,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB area = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 9.0D, zone.center.z + zone.radius
                    );

                    if (elapsed >= zone.maxDurationTicks) {
                        SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius * 0.85D, EssenceType.ASTRAL, EssenceType.LIGHTNING, tier);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            v.removeEffect(MobEffects.SLOW_FALLING);
                            v.setDeltaMovement(v.getDeltaMovement().x, -1.68D, v.getDeltaMovement().z);
                            v.hasImpulse = true;
                            float slamDmg = (6.25F + tier * 1.55F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(srv, v, owner, slamDmg, false, true);
                        }
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.75F);
                        return false;
                    }

                    if (elapsed % 8 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), PULSAR_GOLD_HEX_COLOR, 18, 0.95F, 0.06F),
                                zone.center.x, zone.center.y + 4.5D, zone.center.z, 12, zone.radius * 0.55D, 1.4D, zone.radius * 0.55D, 0.06D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
                            v.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0));
                            v.setDeltaMovement(v.getDeltaMovement().x, 0.40D, v.getDeltaMovement().z);
                            v.hasImpulse = true;

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                );
                            }
                            if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                float meteorDmg = (2.15F + tier * 0.55F) * Math.max(0.5F, zone.apparatusPotency);
                                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(srv, v, owner, meteorDmg, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — ASTRAL-CONSTRUCT EIDOLON SUITE (SUMMON)
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
        tag.putString("SpellEidolonMateria", EssenceType.ASTRAL.name());
        tag.putInt("SpellEidolonTintRGB", COSMIC_VIOLET_HEX_COLOR);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), PULSAR_GOLD_HEX_COLOR, 22, 0.95F, 0.06F),
                spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 18, 0.45D, 0.55D, 0.45D, 0.06D
        );
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.28 Astral Comet Dash:
        // Launches the caster forward 18 blocks in a streak of starfire, knocking aside mobs and granting slow-fall!
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.ASTRAL, potencyTier);

        Vec3 delta = arrivalPos.subtract(departurePos);
        int steps = Math.max(2, (int) Math.ceil(delta.length()));
        Vec3 stepVec = delta.scale(1.0D / steps);
        java.util.Set<java.util.UUID> shoved = new java.util.HashSet<>();

        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), PULSAR_GOLD_HEX_COLOR, 16, 0.85F, 0.05F),
                    p.x, p.y + 0.85D, p.z, 2, 0.14D, 0.18D, 0.14D, 0.02D
            );
            AABB pathBox = new AABB(p.x - 1.7D, p.y - 0.5D, p.z - 1.7D, p.x + 1.7D, p.y + 2.1D, p.z + 1.7D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, pathBox)) {
                if (foe == caster || !foe.isAlive() || !shoved.add(foe.getUUID())) continue;
                if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                if (!(foe instanceof Monster) && !(foe instanceof Mob m && m.getTarget() == caster)) continue;

                Vec3 away = foe.position().subtract(p);
                Vec3 side = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.02D) : stepVec.normalize().scale(1.02D);
                foe.setDeltaMovement(side.x, 0.40D, side.z);
                foe.hasImpulse = true;
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(
                        level, foe, caster, 2.0F + potencyTier * 0.50F, false, true
                );
            }
        }
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.30F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.28 Cosmic Disintegration Transmute:
        // Dissolves cobble and dirt blocks into raw void slag (Astral Impactite) and redstone dust!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.GRAVEL)) {
            level.setBlockAndUpdate(pos, ModBlocks.ASTRAL_IMPACTITE.get().defaultBlockState());
            Block.popResource(level, pos.above(), new ItemStack(Items.REDSTONE, 1 + level.random.nextInt(2)));
            maybeGrantAstralBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.DIRT)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(Items.REDSTONE, 1 + level.random.nextInt(2)));
            Block.popResource(level, pos, new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), 1));
            if (level.random.nextFloat() < 0.35F) {
                Block.popResource(level, pos, new ItemStack(ModItems.STARLIGHT_FLAKES.get(), 1));
            }
            maybeGrantAstralBatchTransmuteDiscount(level, caster, potencyTier);
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
        if (in.is(Items.COBBLESTONE) || in.is(Items.DIRT) || in.is(Items.COBBLED_DEEPSLATE)
                || in.is(Items.STONE) || in.is(Items.DEEPSLATE) || in.is(Items.GRAVEL)) {
            itemEntity.setItem(new ItemStack(Items.REDSTONE, in.getCount()));
            ItemEntity slag = new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY(0.2D), itemEntity.getZ(),
                    new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), Math.max(1, (in.getCount() + 1) / 2))
            );
            level.addFreshEntity(slag);
            maybeGrantAstralBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.28 Gravitational Singularity Tether Hex:
        // Persistent milk-immune cosmic anchor curse (cleansed strictly via Restoration magic or caster defeat):
        // permanently forces 70% reduced movement speed, completely disables jumping & sprinting,
        // and periodically pulls adjacent mobs into collision with the victim!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.70D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 3, true, true));
        target.setSprinting(false);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.5D))) {
            if (adj == target || adj == hexCaster || !adj.isAlive()) continue;
            if (adj instanceof Player || SpellExecutionEngine.isFriendlyAllyForOasis(hexCaster, adj)) continue;
            if (hexCaster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, hexCaster, true)) continue;

            Vec3 delta = target.position().subtract(adj.position());
            double distSq = delta.lengthSqr();
            Vec3 pull = distSq > 1.0E-4D ? delta.normalize().scale(0.48D) : Vec3.ZERO;
            adj.setDeltaMovement(adj.getDeltaMovement().add(pull.x, 0.12D, pull.z));
            adj.hasImpulse = true;

            if (distSq <= 2.56D) {
                // Collision crunch when pulled mobs slam into the singularity-anchored victim!
                float slamDmg = (1.10F + potencyTier * 0.35F) * mult;
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, adj, hexCaster, slamDmg, false, true);
                SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, target, hexCaster, slamDmg * 0.50F, false, true);
            }
        }

        float hexDmg = (1.45F + potencyTier * 0.50F) * mult;
        SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.30D, 0.25D, 0.04D);
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
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            float heal = 3.1F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void maybeGrantAstralBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellAstralBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellAstralBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellAstralBatchTick", now);
        pData.putInt("SpellAstralBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.ASTRAL, refund);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -5; dy--) {
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
