package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.combat.VisInteractionManager;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Per-part Signature Mutation implementation for <b>Vesper Materia (Ethereal &amp; Paradox Fusion)</b>
 * (Umbral {@code #5C6BC0} + Air {@code #2A3B4C} — Whispering Eventide Twilight Gale &amp; Nocturne Silence).
 */
public final class VesperSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VESPER_INDIGO_HEX = 0x5C6BC0;
    public static final int VESPER_DUSK_SLATE_HEX = 0x2A3B4C;

    private static final String TAG_VESPER_BEAM_LAST_TICK = "SpellVesperBeamLastTick";
    private static final String TAG_VESPER_BEAM_TICKS = "SpellVesperBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.VESPER;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Whispering Dusk Chain Flow";
            case ORBIT_SATELLITE -> "Eventide Vesper Orbit Flow";
            case FAST_DART -> "Silent Zephyr-Shadow Flow";
            default -> SpellMutationRegistry.getEpithet(EssenceType.VESPER) + " " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Threads silent twilight gusts between nearby targets, dampening sound and dropping mob aggro.";
            case ORBIT_SATELLITE -> "Sustains whispering dusk satellites that cloak the caster's footsteps and slow encroaching foes.";
            case FAST_DART -> "Delivers silent, wind-assisted dusk bolts with zero acoustic signature.";
            default -> "Infuses spell flow with Eventide Twilight Vesper Materia.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Eventide Dusk Wind-Blade";
            case BEAM -> "Vesper Twilight Gale Tether";
            case RADIAL_BURST -> "Nocturne Whisper Shockwave";
            case TOUCH_STRIKE -> "Eventide Zephyr-Scythe";
            case AURA -> "Cloak of the Evening Star";
            case GLYPH -> "Vesper Hush-Wind Trap";
            case CONE -> "Twilight Gale Shear";
            case SKYFALL -> "Vespera Comet Dusk-Tempest";
            case SUMMON_AGENT -> "Eventide Vesper-Wraith";
            case SUMMON_SENTRY -> "Nocturne Gale Obelisk";
            case SUMMON_DECOY -> "Dusk-Whisper Silhouette";
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Slices silently through the dusk air, dealing Vesper Materia damage, slowing foes, and dropping mob target lock.";
            case BEAM -> "Channels a whispering twilight gale that weakens enemy attacks and triggers a Nocturne Hush Pulse at 3.0s.";
            case RADIAL_BURST -> "Releases a silent 360-degree dusk gust that blinds enemies, wipes mob aggro, and grants brief Invisibility.";
            case TOUCH_STRIKE -> "Delivers a silent twilight wind-scythe strike that shreds 2 armor points, weakens foes, and muffles vibrations.";
            case AURA -> "Wraps the caster in Eventide dusk winds granting Slow Falling, Night Vision, and Sculk vibration immunity.";
            case GLYPH -> "Inscribes a silent dusk trap that pacifies and blinds hostiles while launching them off-balance.";
            case CONE -> "Projects a silent twilight wind-shear cone that knocks enemies backward, blinds them, and clears mob aggro.";
            case SKYFALL -> "Summons a 10-second Vespera comet dusk-tempest of swirling indigo wind-blades across the target zone.";
            case SUMMON_AGENT -> "Summons an autonomous Eventide Vesper-Wraith striking silently from dusk currents.";
            case SUMMON_SENTRY -> "Summons an anchored Nocturne Gale Obelisk projecting hushing twilight wind bolts.";
            case SUMMON_DECOY -> "Manifests a Dusk-Whisper Silhouette that lures hostiles before vanishing in a blinding twilight gust.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Eventide Whisper-Step";
            case TRANSMUTE, BREAK -> "Eventide Dusk Weathering";
            case AFFLICTION -> "Vesper Nocturne Silence Hex";
            default -> "Eventide Twilight " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Teleports on a completely silent twilight breeze, shedding mob aggro and granting brief Invisibility.";
            case TRANSMUTE, BREAK -> "Weathers bright stone and wood into darkened twilight slate and dark oak variants.";
            case AFFLICTION -> "Wraps the victim in a milk-immune Nocturne Silence Hex inflicting Blindness, Weakness, Slowness, and 2 armor shred.";
            default -> "Infuses the effect with whispering Vesper Materia.";
        };
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), VESPER_INDIGO_HEX, 15, 0.68F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D
        );
    }

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        projectile.setNoGravity(true);
        projectile.setSilent(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), VESPER_DUSK_SLATE_HEX, 14, 0.65F, 0.03F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
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
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (living == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, living)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        int tier = spell.getEffectiveTier();
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1));
        living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
        if (living instanceof Mob mob) {
            mob.setTarget(null);
        }
        if (!slot.hasHarmVerb()) {
            hurtWithVesperAdvantage(level, living, caster, 2.85F + tier * 0.75F);
        }
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D),
                projectile.getDeltaMovement(), EssenceType.VESPER, EssenceType.UMBRAL, 0.85F, tier
        );
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
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 60L);
            if (!slot.hasHarmVerb()) {
                hurtWithVesperAdvantage(level, hit, caster, (1.35F + tier * 0.38F) * Math.max(0.5F, apparatusPotency));
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_VESPER_BEAM_LAST_TICK).orElse(-100L);
            if (now == last) continue;
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_VESPER_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_VESPER_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_VESPER_BEAM_TICKS, 0);
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                if (hit instanceof Mob mob) {
                    mob.setTarget(null);
                }
                hurtWithVesperAdvantage(level, hit, caster, (3.35F + tier * 0.84F) * Math.max(0.5F, apparatusPotency));
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 3.4D, EssenceType.VESPER, EssenceType.UMBRAL, tier);
            } else {
                data.putInt(TAG_VESPER_BEAM_TICKS, contactTicks);
            }
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
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.VESPER, EssenceType.AIR, tier);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 4.0D, center.z + r);
        float burstDmg = (2.95F + tier * 0.76F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            Vec3 away = foe.position().subtract(center);
            Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.35D) : new Vec3(0.0D, 0.45D, 0.0D);
            foe.setDeltaMovement(push.x, 0.45D, push.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }
            if (!slot.hasHarmVerb()) {
                hurtWithVesperAdvantage(level, foe, caster, burstDmg);
            }
        }
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, false));
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
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 80L);
        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.2D,
                EssenceType.VESPER, EssenceType.UMBRAL, tier
        );
        float strikeDmg = (2.85F + tier * 0.74F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 100L);
            if (!slot.hasHarmVerb()) {
                hurtWithVesperAdvantage(level, hit, caster, strikeDmg);
            }
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
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, dur, 0, false, true));
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
        int tier = spell.getEffectiveTier();
        double r = Math.max(3.5D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] triggersLeft = {repeating ? 3 : 1};
        final int[] cooldown = {0};
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, glyphCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.VESPER, EssenceType.UMBRAL, 240, tier
        );

        SpellLingeringZoneManager.ActiveSpellZone glyph = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, r, maxDuration, level.getGameTime(), spell, slot, EssenceType.VESPER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed < 20) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB box = new AABB(zone.center.x - 2.6D, zone.center.y - 1.2D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.4D, zone.center.z + 2.6D);
                    boolean hitAny = false;
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        hitAny = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                        if (foe instanceof Mob mob) {
                            mob.setTarget(null);
                        }
                        hurtWithVesperAdvantage(srv, foe, owner, (3.05F + tier * 0.76F) * Math.max(0.5F, zone.apparatusPotency));
                    }
                    if (!hitAny) return true;
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, Math.max(4.0D, zone.radius), EssenceType.VESPER, EssenceType.AIR, tier);
                    triggersLeft[0]--;
                    cooldown[0] = 45;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyph);
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
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.VESPER, EssenceType.AIR, tier
        );
        float coneDmg = (2.80F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.setDeltaMovement(direction.x * 1.25D, 0.34D, direction.z * 1.25D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            if (hit instanceof Mob mob) {
                mob.setTarget(null);
            }
            if (!slot.hasHarmVerb()) {
                hurtWithVesperAdvantage(level, hit, caster, coneDmg);
            }
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
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.VESPER, EssenceType.UMBRAL, 200, tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.VESPER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 3.0D, zone.center.z,
                                12, zone.radius * 0.48D, 0.9D, zone.radius * 0.48D, 0.03D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        float tickDmg = (2.15F + tier * 0.58F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                            hurtWithVesperAdvantage(srv, foe, owner, tickDmg);
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
        tag.putString("SpellEidolonMateria", EssenceType.VESPER.name());
        tag.putInt("SpellEidolonTintRGB", VESPER_INDIGO_HEX);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.45D, EssenceType.VESPER, EssenceType.UMBRAL, 36, spell.getEffectiveTier()
        );
    }

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 100L);
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.VESPER, potencyTier);
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, true));
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, departurePos).inflate(12.0D), m -> m.getTarget() == caster)) {
            mob.setTarget(null);
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        BlockState out = null;
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) {
            out = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        } else if (state.is(Blocks.SANDSTONE) || state.is(Blocks.SMOOTH_SANDSTONE)) {
            out = Blocks.BLACKSTONE.defaultBlockState();
        } else if (state.is(Blocks.BIRCH_LOG) || state.is(Blocks.OAK_LOG)) {
            out = Blocks.DARK_OAK_LOG.defaultBlockState();
        } else if (state.is(Blocks.BIRCH_PLANKS) || state.is(Blocks.OAK_PLANKS)) {
            out = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        }
        if (out != null) {
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        ItemStack out = ItemStack.EMPTY;
        if (stack.is(Items.COBBLESTONE) || stack.is(Items.STONE)) {
            out = new ItemStack(Items.COBBLED_DEEPSLATE, stack.getCount());
        } else if (stack.is(Items.BIRCH_LOG) || stack.is(Items.OAK_LOG)) {
            out = new ItemStack(Items.DARK_OAK_LOG, stack.getCount());
        }
        if (!out.isEmpty()) {
            itemEntity.setItem(out);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.0D * mult, 0.0D, 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.40F + potencyTier * 0.48F) * mult;
        hurtWithVesperAdvantage(level, target, hexCaster, hexDmg);
        return true;
    }

    private static float hurtWithVesperAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) return 0.0F;
        Set<EssenceType> defenderAffs = SpellCombatReactionEngine.resolveEntityAffinities(target);
        boolean mobToPlayer = (target instanceof Player) && !(caster instanceof Player);
        float mult = VisInteractionManager.getInstance().calculateDamageMultiplier(
                EssenceType.VESPER, defenderAffs, mobToPlayer, 0.0F
        );
        if (mult <= 0.0F) return 0.0F;
        float finalDmg = Math.max(0.5F, baseDamage * mult);
        target.invulnerableTime = 0;
        DamageSource src = caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic();
        return SpellCombatReactionEngine.hurtWithPureMateria(level, target, src, finalDmg) ? finalDmg : 0.0F;
    }
}
