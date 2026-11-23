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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
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
 * Per-part Signature Mutation implementation for <b>Aureole Materia (Ethereal &amp; Paradox Fusion)</b>
 * (Radiant {@code #FFF59D} + Air {@code #81D4FA} — Shimmering Solar Atmospheric Crown &amp; Halo Sanctification).
 */
public final class AureoleSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int AUREOLE_HALO_GOLD_HEX = 0xFFF59D;
    public static final int AUREOLE_SKY_CYAN_HEX = 0x81D4FA;

    private static final String TAG_AUREOLE_BEAM_LAST_TICK = "SpellAureoleBeamLastTick";
    private static final String TAG_AUREOLE_BEAM_TICKS = "SpellAureoleBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.AUREOLE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case ORBIT_SATELLITE -> "Luminous Halo Orbit Flow";
            case CHAIN_LEAP -> "Refractive Halo Arc Flow";
            case FAST_DART -> "Zenith Crown Slipstream Flow";
            default -> SpellMutationRegistry.getEpithet(EssenceType.AUREOLE) + " " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case ORBIT_SATELLITE -> "Conjures shimmering golden-azure halo rings around the caster that deflect arrows and illuminate foes.";
            case CHAIN_LEAP -> "Refracts spells along golden atmospheric arcs between enemies, revealing invisible targets.";
            case FAST_DART -> "Rides high-altitude solar drafts for crisp, gravity-defying flight.";
            default -> "Infuses spell flow with Luminous Halo Aureole Materia.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Luminous Halo Chakram";
            case BEAM -> "Aureole Zenith Ray";
            case RADIAL_BURST -> "Solar Halo Sanctification Nova";
            case TOUCH_STRIKE -> "Crowned Halo Arc-Slash";
            case AURA -> "Aureole Nimbus Crown";
            case GLYPH -> "Sanctified Halo Ward-Sigil";
            case CONE -> "Refractive Halo Gale";
            case SKYFALL -> "Zenith Aureole Star-Rain";
            case SUMMON_AGENT -> "Aureole Halo-Vanguard";
            case SUMMON_SENTRY -> "Celestial Crown Spire";
            case SUMMON_DECOY -> "Luminous Nimbus Effigy";
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hurls a spinning halo chakram of sunlit air that lifts targets slightly, reveals stealth, and smites undead.";
            case BEAM -> "Projects a shimmering zenith ray that blinds enemies, shreds armor, and erupts in a Halo Crown Burst at 3.0s.";
            case RADIAL_BURST -> "Expands a 360-degree halo ring that repels hostiles, deflects projectiles, and cleanses nearby allies.";
            case TOUCH_STRIKE -> "Sweeps a luminous halo arc that knocks foes back, shreds 2 armor points, and grants Slow Falling.";
            case AURA -> "Crowns the caster in a luminous nimbus granting Slow Falling, Blindness/Darkness immunity, and arrow deflection.";
            case GLYPH -> "Inscribes a sanctified halo ward that heals allies stepping onto it or blasts encroaching hostiles skyward.";
            case CONE -> "Unleashes a refractive sunlit gale that pushes enemies backward, deflects arrows, and marks foes with Glowing.";
            case SKYFALL -> "Calls down a 10-second rain of golden-azure halo rings that smite hostiles and soothe allies.";
            case SUMMON_AGENT -> "Summons an autonomous Aureole Halo-Vanguard delivering sanctified wind-light strikes.";
            case SUMMON_SENTRY -> "Summons an anchored Celestial Crown Spire firing luminous halo bolts.";
            case SUMMON_DECOY -> "Manifests a Luminous Nimbus Effigy that draws aggro before bursting in a blinding halo wave.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Halo Slipstream Step";
            case TRANSMUTE, BREAK -> "Aureole Atmospheric Vitrification";
            case AFFLICTION -> "Blinding Halo Judgement Hex";
            default -> "Luminous Halo " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Steps gracefully along an upper-atmosphere halo current, granting Slow Falling and Speed on arrival.";
            case TRANSMUTE, BREAK -> "Vitrifies sand into tinted/clear glass, purifies rotten flesh into leather, and brightens dark stone into quartz/calcite.";
            case AFFLICTION -> "Crowns the victim with a milk-immune Blinding Halo Hex that reveals through walls, blinds, and shreds 2 armor points.";
            default -> "Infuses the effect with shimmering Aureole Materia.";
        };
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), AUREOLE_HALO_GOLD_HEX, 14, 0.72F, 0.05F),
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
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
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
        living.removeEffect(MobEffects.INVISIBILITY);
        living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));
        if (living.getType().is(EntityTypeTags.UNDEAD)) {
            living.igniteForSeconds(4);
        }
        if (!slot.hasHarmVerb()) {
            hurtWithAureoleAdvantage(level, living, caster, 2.85F + tier * 0.75F);
        }
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D),
                projectile.getDeltaMovement(), EssenceType.AUREOLE, EssenceType.AIR, 0.88F, tier
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

            hit.removeEffect(MobEffects.INVISIBILITY);
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 60L);
            if (!slot.hasHarmVerb()) {
                hurtWithAureoleAdvantage(level, hit, caster, (1.35F + tier * 0.38F) * Math.max(0.5F, apparatusPotency));
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_AUREOLE_BEAM_LAST_TICK).orElse(-100L);
            if (now == last) continue;
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_AUREOLE_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_AUREOLE_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_AUREOLE_BEAM_TICKS, 0);
                hit.setDeltaMovement(direction.x * 1.35D, 0.48D, direction.z * 1.35D);
                hit.hasImpulse = true;
                hurtWithAureoleAdvantage(level, hit, caster, (3.40F + tier * 0.85F) * Math.max(0.5F, apparatusPotency));
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 3.4D, EssenceType.AUREOLE, EssenceType.RADIANT, tier);
            } else {
                data.putInt(TAG_AUREOLE_BEAM_TICKS, contactTicks);
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

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.AUREOLE, EssenceType.AIR, tier);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 4.0D, center.z + r);
        float burstDmg = (3.00F + tier * 0.78F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, e)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(e);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, wardSafe)) continue;
            Vec3 away = e.position().subtract(center);
            Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.5D) : new Vec3(0.0D, 0.55D, 0.0D);
            e.setDeltaMovement(push.x, 0.55D, push.z);
            e.hasImpulse = true;
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            if (!slot.hasHarmVerb()) {
                hurtWithAureoleAdvantage(level, e, caster, burstDmg);
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
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.2D,
                EssenceType.AUREOLE, EssenceType.RADIANT, tier
        );
        float strikeDmg = (2.85F + tier * 0.74F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.setDeltaMovement(direction.x * 1.2D, 0.4D, direction.z * 1.2D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 100L);
            if (!slot.hasHarmVerb()) {
                hurtWithAureoleAdvantage(level, hit, caster, strikeDmg);
            }
        }
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true));
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
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.1D), caster.getZ(), 16, 0.4D, 0.3D, 0.4D, 0.04D);
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
                level, glyphCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.AUREOLE, EssenceType.AIR, 240, tier
        );

        SpellLingeringZoneManager.ActiveSpellZone glyph = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, r, maxDuration, level.getGameTime(), spell, slot, EssenceType.AUREOLE, apparatusPotency,
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
                    boolean hostileHit = false;
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        hostileHit = true;
                        foe.setDeltaMovement(foe.getDeltaMovement().x, 0.78D, foe.getDeltaMovement().z);
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                        hurtWithAureoleAdvantage(srv, foe, owner, (3.10F + tier * 0.78F) * Math.max(0.5F, zone.apparatusPotency));
                    }
                    if (!hostileHit) return true;
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, Math.max(4.0D, zone.radius), EssenceType.AUREOLE, EssenceType.RADIANT, tier);
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
                EssenceType.AUREOLE, EssenceType.AIR, tier
        );
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(1.5D))) {
            if (proj.getOwner() != caster) {
                proj.setDeltaMovement(direction.scale(1.4D));
                proj.hasImpulse = true;
            }
        }
        float coneDmg = (2.80F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.setDeltaMovement(direction.x * 1.25D, 0.36D, direction.z * 1.25D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            if (!slot.hasHarmVerb()) {
                hurtWithAureoleAdvantage(level, hit, caster, coneDmg);
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

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.AUREOLE, EssenceType.AIR, 200, tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.AUREOLE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 5.0D, zone.center.z,
                                16, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        float tickDmg = (2.15F + tier * 0.58F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (e == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 1.0F);
                                e.heal(1.0F);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) continue;
                            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));
                            hurtWithAureoleAdvantage(srv, e, owner, tickDmg);
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
        tag.putString("SpellEidolonMateria", EssenceType.AUREOLE.name());
        tag.putInt("SpellEidolonTintRGB", AUREOLE_HALO_GOLD_HEX);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.45D, EssenceType.AUREOLE, EssenceType.AIR, 36, spell.getEffectiveTier()
        );
    }

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.AUREOLE, potencyTier);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, true));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        BlockState out = null;
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            out = Blocks.GLASS.defaultBlockState();
        } else if (state.is(Blocks.BLACKSTONE) || state.is(Blocks.DEEPSLATE)) {
            out = Blocks.CALCITE.defaultBlockState();
        } else if (state.is(Blocks.COBBLESTONE)) {
            out = Blocks.QUARTZ_BLOCK.defaultBlockState();
        }
        if (out != null) {
            level.setBlockAndUpdate(pos, out);
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        ItemStack out = ItemStack.EMPTY;
        if (stack.is(Items.ROTTEN_FLESH)) {
            out = new ItemStack(Items.LEATHER, stack.getCount());
        } else if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            out = new ItemStack(Items.GLASS, stack.getCount());
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
        target.removeEffect(MobEffects.INVISIBILITY);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 2.0D * mult, 0.0D, 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.40F + potencyTier * 0.48F) * mult;
        hurtWithAureoleAdvantage(level, target, hexCaster, hexDmg);
        return true;
    }

    private static float hurtWithAureoleAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) return 0.0F;
        Set<EssenceType> defenderAffs = SpellCombatReactionEngine.resolveEntityAffinities(target);
        boolean mobToPlayer = (target instanceof Player) && !(caster instanceof Player);
        float mult = VisInteractionManager.getInstance().calculateDamageMultiplier(
                EssenceType.AUREOLE, defenderAffs, mobToPlayer, 0.0F
        );
        if (mult <= 0.0F) return 0.0F;
        float finalDmg = Math.max(0.5F, baseDamage * mult);
        target.invulnerableTime = 0;
        DamageSource src = caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic();
        return SpellCombatReactionEngine.hurtWithPureMateria(level, target, src, finalDmg) ? finalDmg : 0.0F;
    }
}
