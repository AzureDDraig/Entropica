package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Null_R Materia (Ethereal &amp; Paradox Fusion #30)</b>
 * (Spellcrafting System Guide Section 6.31 — Dual Radiant {@code #FFFFFF} + Void {@code #41304E} [Leans: Void]).
 */
public final class NullRSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Brilliant Antimatter White (#FFFFFF) primary tint. */
    public static final int NULL_ARCHON_HEX_COLOR = 0xFFFFFF;

    /** Canonical Cosmic Event-Horizon Violet (#41304E) secondary tint. */
    public static final int COSMIC_HORIZON_HEX_COLOR = 0x41304E;

    private static final String TAG_NULL_R_BEAM_LAST_TICK = "SpellNullRBeamLastTick";
    private static final String TAG_NULL_R_BEAM_TICKS = "SpellNullRBeamTicks";
    private static final String TAG_NULL_R_ORB_COLLAPSED = "SpellNullROrbCollapsed";

    @Override
    public EssenceType materiaType() {
        return EssenceType.NULL_R;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Singularity Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "White Hole Singularity Orb";
            case BEAM -> "Event Horizon Tractor Beam";
            case RADIAL_BURST -> "White Hole Antimatter Nova";
            case TOUCH_STRIKE -> "Event Horizon Palm";
            case AURA -> "Event Horizon Bastion";
            case GLYPH -> "White Hole Singularity Trap";
            case CONE -> "Blinding Antimatter Flare Cone";
            case SKYFALL -> "White Hole Singularity Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Antimatter Phase Dash";
            case TRANSMUTE, BREAK -> "Null-Slag Dissolution Transmute";
            case AFFLICTION -> "Antimatter Annihilation Hex";
            default -> "White-Hole Antimatter " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — SINGULARITY ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), NULL_ARCHON_HEX_COLOR, 14, 0.72F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), COSMIC_HORIZON_HEX_COLOR, 16, 0.68F, 0.12F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — WHITE HOLE SINGULARITY ORB (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        projectile.setWallPhasing(true);
        projectile.setNoGravity(true);
        projectile.setMaxFlightTicks(95);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), COSMIC_HORIZON_HEX_COLOR, 18, 0.75F, 0.14F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.02D
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
        collapseWhiteHoleOrbOnce(level, projectile, caster, hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.35D, 0.0D), spell);
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
        // Wall-phasing White Hole Singularity Orbs phase effortlessly through solid cover and collapse on entity hit or max range!
        if (!projectile.isWallPhasing()) {
            collapseWhiteHoleOrbOnce(level, projectile, caster, blockHit.getLocation(), spell);
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
        collapseWhiteHoleOrbOnce(level, projectile, caster, projectile.position(), spell);
    }

    private static void collapseWhiteHoleOrbOnce(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_NULL_R_ORB_COLLAPSED).orElse(false)) {
            return;
        }
        pTag.putBoolean(TAG_NULL_R_ORB_COLLAPSED, true);

        // Section 6.31 White Hole Singularity Orb:
        // Violently collapses inward, pulling all mobs within 5 blocks to the center before detonating in a blinding flash
        // that deals Null_R Materia damage, stuns enemies for 1.5s (30t), and silences mob abilities for 3.0s (60t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.add(0.0D, 0.55D, 0.0D), 5.0D, true, EssenceType.NULL_R, EssenceType.VOID, 18, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(
                level, center, 5.0D, EssenceType.NULL_R, EssenceType.VOID, tier
        );
        SpellLingeringZoneManager.pulseTemporaryLight(level, BlockPos.containing(center), 15, 45);

        AABB box = new AABB(center.x - 5.0D, center.y - 2.5D, center.z - 5.0D,
                center.x + 5.0D, center.y + 3.5D, center.z + 5.0D);
        float collapseDmg = 3.25F + tier * 0.85F;

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = center.subtract(foe.position());
            Vec3 pull = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.12D) : Vec3.ZERO;
            foe.setDeltaMovement(pull.x, 0.28D, pull.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
            SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, foe, caster, collapseDmg, false, true);
        }

        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 1.0F, 1.0F),
                center.x, center.y + 0.6D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.6D, center.z, 28, 1.5D, 0.6D, 1.5D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.38F);
    }

    // =========================================================================
    // FORM MUTATION 2 — EVENT HORIZON TRACTOR BEAM (BEAM)
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
        // Section 6.31 Event Horizon Tractor Beam:
        // Blinding white anti-light beam holding targets suspended in zero-g mid-air and pulling them toward the caster;
        // channeling for 3.0s (60 contact ticks = 15 beam slices) triggers an Annihilation Flash that purges all active
        // potion buffs and blinds nearby hostiles for 4.0s (80t)!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            Vec3 toCaster = caster.position().subtract(hit.position());
            Vec3 horizPull = new Vec3(toCaster.x, 0.0D, toCaster.z);
            Vec3 pull = horizPull.lengthSqr() > 1.0E-4D ? horizPull.normalize().scale(0.46D) : Vec3.ZERO;
            double desiredHoverY = Math.max(caster.getY() + 1.25D, hit.getY());
            double liftY = hit.onGround() ? 0.34D : Math.clamp((desiredHoverY - hit.getY()) * 0.24D, -0.04D, 0.28D);
            hit.setDeltaMovement(pull.x, liftY, pull.z);
            hit.resetFallDistance();
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 35, 0, true, false));

            if (!slot.hasHarmVerb()) {
                float tickDmg = (1.35F + tier * 0.38F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, hit, caster, tickDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_NULL_R_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_NULL_R_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_NULL_R_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_NULL_R_BEAM_TICKS, 0);
                Vec3 flashCenter = hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D);
                SpellWorldVfxManager.broadcastRadialNova(
                        level, hit.position(), 4.5D, EssenceType.NULL_R, EssenceType.VOID, tier
                );
                SpellLingeringZoneManager.pulseTemporaryLight(level, hit.blockPosition().above(), 15, 45);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 1.0F, 1.0F),
                        flashCenter.x, flashCenter.y, flashCenter.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                level.sendParticles(ParticleTypes.END_ROD,
                        flashCenter.x, flashCenter.y, flashCenter.z, 26, 1.4D, 0.6D, 1.4D, 0.09D);
                level.playSound(null, hit.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.45F);

                float flashDmg = (3.40F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.5D), LivingEntity::isAlive)) {
                    if (nearby == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, nearby)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(nearby, caster, wardSafe)) continue;
                    EclipseSpellMutationHandler.purgeBeneficialEffects(nearby);
                    nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, nearby, caster, flashDmg, false, true);
                }
            } else {
                data.putInt(TAG_NULL_R_BEAM_TICKS, contactTicks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — WHITE HOLE ANTIMATTER NOVA (RADIAL BURST)
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
        // Section 6.31 White Hole Antimatter Nova:
        // 360° shockwave (6m+ radius, #FFFFFF / #41304E) that first pulls enemies 4 blocks inward before detonating
        // in a blinding flash; knocks enemies 6 blocks back, inflicts 4.0s (80t) Blindness, strips active potion buffs,
        // and resets mob aggro!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.add(0.0D, 0.85D, 0.0D), r, true, EssenceType.NULL_R, EssenceType.VOID, 12, tier
        );
        SpellLingeringZoneManager.pulseTemporaryLight(level, BlockPos.containing(center).above(), 15, 50);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 toCenter = center.subtract(foe.position());
            Vec3 inward = toCenter.lengthSqr() > 1.0E-4D ? toCenter.normalize().scale(1.02D) : Vec3.ZERO;
            foe.setDeltaMovement(inward.x, 0.24D, inward.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            EclipseSpellMutationHandler.purgeBeneficialEffects(foe);
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 7, level.getGameTime(), spell, slot, EssenceType.NULL_R, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean zWard = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, zone.radius, EssenceType.NULL_R, EssenceType.VOID, zTier
                    );
                    AABB blastBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 3.5D, zone.center.z + zone.radius
                    );
                    float novaDmg = (3.10F + zTier * 0.78F) * Math.max(0.5F, zone.apparatusPotency);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zWard)) continue;

                        Vec3 fromCenter = foe.position().subtract(zone.center);
                        Vec3 away = fromCenter.lengthSqr() > 1.0E-4D ? fromCenter.normalize().scale(1.52D) : new Vec3(0.0D, 0.45D, 0.0D);
                        foe.setDeltaMovement(away.x, 0.44D, away.z);
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        EclipseSpellMutationHandler.purgeBeneficialEffects(foe);
                        if (foe instanceof Mob mob) {
                            mob.setTarget(null);
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(srv, foe, owner, novaDmg, false, true);
                        }
                    }

                    srv.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 1.0F, 1.0F),
                            zone.center.x, zone.center.y + 0.8D, zone.center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    srv.sendParticles(ParticleTypes.END_ROD,
                            zone.center.x, zone.center.y + 0.8D, zone.center.z, 36, 2.2D, 0.5D, 2.2D, 0.10D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.32F);
                    return false;
                }
        ));
    }

    // =========================================================================
    // FORM MUTATION 4 — EVENT HORIZON PALM (TOUCH / STRIKE)
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
        // Section 6.31 Event Horizon Palm:
        // Strikes with antimatter light; deals Null_R Materia damage, ignores 50% of target armor, purges all positive
        // potion buffs, and destabilizes the victim in spacetime so they teleport randomly 3 times over 6s (120t)
        // while blinded for 2.0s (40t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long destabilizedUntil = level.getGameTime() + 121L;

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            EclipseSpellMutationHandler.purgeBeneficialEffects(hit);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, destabilizedUntil);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.50D, 0.0D, 40L);

            if (!slot.hasHarmVerb()) {
                float palmDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, hit, caster, palmDmg, true, true);
            }
            level.sendParticles(ParticleTypes.END_ROD, hit.getX(), hit.getY(0.85D), hit.getZ(), 14, 0.28D, 0.32D, 0.28D, 0.06D);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — EVENT HORIZON BASTION (AURA / SELF)
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
        // Section 6.31 Event Horizon Bastion:
        // Cloaks the caster in a radiant white light corona with violet gravity bands (#FFFFFF / #41304E);
        // grants high-float jumping, zero-g glide, total immunity to Blindness and Darkness effects, emits level-15 light,
        // deflects 50% of incoming hostile arrows, and blinds and repels melee attackers 4 blocks backward!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_NULL_R_BASTION_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        SpellLingeringZoneManager.pulseTemporaryLight(level, caster.blockPosition().above(), 15, 35);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.06D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 6 — WHITE HOLE SINGULARITY TRAP (GLYPH / TRAP)
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
        // Section 6.31 White Hole Singularity Trap:
        // Disarmable Single/Repeating hidden antimatter sigil (1.0s arming delay); when tripped, erupts into a 4m
        // singularity that pulls mobs to the center, suspends them 3 blocks in mid-air for 3.0s (60t), strips all
        // active potion buffs, blinds them for 4.0s (80t), and deals continuous Null_R Materia damage!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.NULL_R, EssenceType.VOID, Math.min(240, maxDuration), tier
        );
        level.playSound(null, surface.above(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.80F, 1.40F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.NULL_R, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.NULL_R, EssenceType.VOID, rem, tier
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

                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 2.4D, 0.0D), zone.radius, true, EssenceType.NULL_R, EssenceType.VOID, 60, tier
                    );
                    SpellLingeringZoneManager.pulseTemporaryLight(srv, BlockPos.containing(zone.center).above(2), 15, 65);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.38F);

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, zone.radius, 60, srv.getGameTime(),
                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sWard = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                AABB box = new AABB(
                                        sZone.center.x - sZone.radius, sZone.center.y - 1.0D, sZone.center.z - sZone.radius,
                                        sZone.center.x + sZone.radius, sZone.center.y + 5.4D, sZone.center.z + sZone.radius
                                );
                                Vec3 hoverTarget = sZone.center.add(0.0D, 3.0D, 0.0D);
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                                    if (v == sOwner || SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sWard)) continue;

                                    Vec3 hover = hoverTarget.subtract(v.position()).scale(0.30D);
                                    v.setDeltaMovement(hover);
                                    v.resetFallDistance();
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                                    EclipseSpellMutationHandler.purgeBeneficialEffects(v);

                                    if (sEl % 20 == 0) {
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                                    sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F
                                            );
                                        }
                                        float singDmg = (1.85F + tier * 0.48F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(sLvl, v, sOwner, singDmg, false, true);
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.END_ROD,
                                            sZone.center.x, sZone.center.y + 2.8D, sZone.center.z, 8, 1.0D, 0.8D, 1.0D, 0.04D);
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
    // FORM MUTATION 7 — BLINDING ANTIMATTER FLARE CONE (CONE / SPRAY)
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
        // Section 6.31 Blinding Antimatter Flare Cone:
        // Wide 60° cone of white-violet antimatter radiation; pushes enemies 6 blocks back, inflicts 4.0s (80t)
        // Blindness, strips positive potion buffs, deals +50% bonus smite damage to undead, and illuminates the
        // blast zone with level-15 light!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.48D, 0.36D, direction.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            EclipseSpellMutationHandler.purgeBeneficialEffects(hit);

            if (!slot.hasHarmVerb()) {
                float coneDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            } else if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                // Additional undead smite surge on top of Harm verb!
                float smiteBonus = (2.75F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, hit, caster, smiteBonus, false, true);
            }
        }

        int lightSteps = Math.min(8, Math.max(2, (int) Math.ceil(coneRange)));
        for (int s = 2; s <= lightSteps; s += 3) {
            BlockPos lp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            SpellLingeringZoneManager.pulseTemporaryLight(level, lp, 15, 65);
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 24, 1.5D, 0.6D, 1.5D, 0.06D);
    }

    // =========================================================================
    // FORM MUTATION 8 — WHITE HOLE SINGULARITY TEMPEST (SKYFALL)
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
        // Section 6.31 White Hole Singularity Tempest:
        // Opens a blinding aerial rift for 10.0s (200t) across a 14m+ radius raining antimatter stars;
        // continuously lifts caught hostiles 3 blocks airborne in zero-g, strips their potion buffs,
        // deals heavy smite damage to undead, and slams them down when the tempest ends!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.NULL_R, EssenceType.VOID, 200, tier
        );
        SpellLingeringZoneManager.pulseTemporaryLight(level, BlockPos.containing(impactCenter).above(2), 15, 205);
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.28F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.NULL_R, apparatusPotency,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    AABB area = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 9.0D, zone.center.z + zone.radius
                    );

                    if (elapsed >= zone.maxDurationTicks) {
                        // Final gravitational collapse slam!
                        float slamDmg = (5.50F + zTier * 1.45F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            v.removeEffect(MobEffects.SLOW_FALLING);
                            v.setDeltaMovement(v.getDeltaMovement().x, -1.65D, v.getDeltaMovement().z);
                            v.hasImpulse = true;
                            SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(srv, v, owner, slamDmg, false, true);
                        }
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.1F, 0.85F);
                        return false;
                    }

                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD,
                                zone.center.x, zone.center.y + 4.5D, zone.center.z,
                                22, zone.radius * 0.55D, 1.4D, zone.radius * 0.55D, 0.08D);
                    }

                    // Smooth zero-g 3-block suspension every 5 ticks + buff strip & undead smite every 20 ticks!
                    if (elapsed % 5 == 0) {
                        double targetY = zone.center.y + 3.0D;
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            double vy = Math.clamp((targetY - v.getY()) * 0.26D, -0.05D, 0.38D);
                            v.setDeltaMovement(v.getDeltaMovement().x * 0.78D, vy, v.getDeltaMovement().z * 0.78D);
                            v.resetFallDistance();
                            v.hasImpulse = true;
                            v.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));
                        }
                    }

                    if (elapsed % 20 == 0) {
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            EclipseSpellMutationHandler.purgeBeneficialEffects(v);
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.38F
                                );
                            }
                            float starDmg = (2.15F + zTier * 0.58F)
                                    * (v.getType().is(EntityTypeTags.UNDEAD) ? 1.45F : 1.0F)
                                    * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(srv, v, owner, starDmg, false, true);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — NULL-ARCHON EIDOLON SUITE (SUMMON)
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
        tag.putString("SpellEidolonMateria", EssenceType.NULL_R.name());
        tag.putInt("SpellEidolonTintRGB", NULL_ARCHON_HEX_COLOR);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.45D, EssenceType.NULL_R, EssenceType.VOID, 36, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.07D);
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
        // Section 6.31 Antimatter Phase Dash:
        // Dashes 16 blocks forward in a streak of white fire, knocking aside mobs and dropping mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.NULL_R, potencyTier);

        Vec3 delta = arrivalPos.subtract(departurePos);
        int steps = Math.max(2, (int) Math.ceil(delta.length()));
        Vec3 stepVec = delta.scale(1.0D / steps);
        Set<UUID> shoved = new HashSet<>();

        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.85D, p.z, 3, 0.18D, 0.28D, 0.18D, 0.03D);
            AABB pathBox = new AABB(p.x - 1.7D, p.y - 0.5D, p.z - 1.7D, p.x + 1.7D, p.y + 2.1D, p.z + 1.7D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, pathBox, LivingEntity::isAlive)) {
                if (foe == caster || !shoved.add(foe.getUUID())) continue;
                if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                if (!(foe instanceof Monster) && !(foe instanceof Mob m && m.getTarget() == caster)) continue;

                Vec3 away = foe.position().subtract(p);
                Vec3 side = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(0.98D) : stepVec.normalize().scale(0.98D);
                foe.setDeltaMovement(side.x, 0.36D, side.z);
                foe.hasImpulse = true;
                SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(
                        level, foe, caster, 2.0F + potencyTier * 0.48F, false, true
                );
            }
        }

        for (Mob mob : level.getEntitiesOfClass(Mob.class, arrivalPosBox(departurePos, arrivalPos, 15.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
        }
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.90F, 1.38F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.31 Null-Slag Dissolution Transmute:
        // Dissolves stone and gravel into raw void slag (Astral Impactite) and glowstone dust!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            level.setBlockAndUpdate(pos, ModBlocks.ASTRAL_IMPACTITE.get().defaultBlockState());
            Block.popResource(level, pos.above(), new ItemStack(Items.GLOWSTONE_DUST, 1 + level.random.nextInt(2)));
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.03D);
            maybeGrantNullRBatchTransmuteDiscount(level, caster, potencyTier);
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
        if (in.is(Items.STONE) || in.is(Items.COBBLESTONE) || in.is(Items.GRAVEL)
                || in.is(Items.DEEPSLATE) || in.is(Items.COBBLED_DEEPSLATE)
                || in.is(Items.ANDESITE) || in.is(Items.DIORITE) || in.is(Items.GRANITE)) {
            itemEntity.setItem(new ItemStack(Items.GLOWSTONE_DUST, in.getCount()));
            ItemEntity slag = new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY(0.2D), itemEntity.getZ(),
                    new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), Math.max(1, (in.getCount() + 1) / 2))
            );
            level.addFreshEntity(slag);
            maybeGrantNullRBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.31 Antimatter Annihilation Hex:
        // Persistent milk-immune reality-eroding curse (cleansed strictly via Restoration magic or caster defeat):
        // permanently melts away 2 armor points every 3s (up to 12 total), permanently disables shield blocking,
        // and suppresses all positive potion buffs!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        CompoundTag data = EntityHelper.getPersistentData(target);
        int ticks = data.getInt("NullRHexMeltSteps").orElse(0) + 1;
        data.putInt("NullRHexMeltSteps", ticks);
        double meltedArmor = Math.min(12.0D, (2.0D + (ticks / 3) * 2.0D) * mult);

        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, meltedArmor, 0.0D, 45L);
        EclipseSpellMutationHandler.purgeBeneficialEffects(target);

        if (target instanceof Player player) {
            for (ItemStack held : List.of(player.getMainHandItem(), player.getOffhandItem())) {
                if (!held.isEmpty() && (held.is(Items.SHIELD) || held.has(DataComponents.BLOCKS_ATTACKS))) {
                    player.getCooldowns().addCooldown(held, 50);
                }
            }
            if (player.isBlocking()) {
                player.stopUsingItem();
            }
        } else if (target.isBlocking()) {
            target.stopUsingItem();
        }

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.45F + potencyTier * 0.50F) * mult;
        SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.03D);
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
            float heal = 3.2F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static AABB arrivalPosBox(Vec3 a, Vec3 b, double inflate) {
        return new AABB(
                Math.min(a.x, b.x) - inflate, Math.min(a.y, b.y) - 4.0D, Math.min(a.z, b.z) - inflate,
                Math.max(a.x, b.x) + inflate, Math.max(a.y, b.y) + 6.0D, Math.max(a.z, b.z) + inflate
        );
    }

    private static void maybeGrantNullRBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellNullRBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellNullRBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellNullRBatchTick", now);
        pData.putInt("SpellNullRBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.NULL_R, refund);
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
