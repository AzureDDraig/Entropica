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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * Per-part Signature Mutation implementation for <b>Radiant Fire Materia (Caprine Secret Fusion)</b>
 * (Radiant {@code #FFEA78} + Nether {@code #FF3C00} — Purging Solar Prominence &amp; Steam-Cavitation Detonator).
 */
public final class RadiantFireSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SOLAR_PROMINENCE_GOLD_HEX = 0xFFEA78;
    public static final int BLAZING_VERMILION_HEX = 0xFF3C00;

    public static final String TAG_RADIANT_FIRE_SEAR_UNTIL = "SpellRadiantFireSearUntil";
    private static final String TAG_RF_BEAM_LAST_TICK = "SpellRadiantFireBeamLastTick";
    private static final String TAG_RF_BEAM_TICKS = "SpellRadiantFireBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.RADIANT_FIRE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case FAST_DART -> "Solar Prominence Velocity Lance Flow";
            case CHAIN_LEAP -> "Helios Flare Chain Flow";
            case ORBIT_SATELLITE -> "Solar Corona Prominence Orbit Flow";
            default -> SpellMutationRegistry.getEpithet(EssenceType.RADIANT_FIRE) + " " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case FAST_DART -> "Accelerates spell projectiles on a purging solar-fire jetstream that ignites targets and detonates Steam-Cavitation on drenched foes.";
            case CHAIN_LEAP -> "Arcs blazing solar-prominence filaments between nearby hostiles, revealing invisible enemies and igniting undead.";
            case ORBIT_SATELLITE -> "Conjures orbiting solar-prominence embers that intercept projectiles and scorch encroaching foes.";
            default -> "Infuses spell flow with Caprine Solar Prominence Materia.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Radiant Fire Lance";
            case BEAM -> "Solar Prominence Jetstream";
            case RADIAL_BURST -> "Helios Corona Eruption";
            case TOUCH_STRIKE -> "Caprine Sunfire Cleave";
            case AURA -> "Solar Prominence Mantle";
            case GLYPH -> "Caprine Crucible Pyre Sigil";
            case CONE -> "Prominence Flare Deluge";
            case SKYFALL -> "Solar Prominence Starfall";
            case SUMMON_AGENT -> "Solar-Prominence Seraph";
            case SUMMON_SENTRY -> "Helios Pyre Obelisk";
            case SUMMON_DECOY -> "Incandescent Corona Effigy";
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Launches a high-velocity purging sunfire lance that ignites targets, smites undead, and triggers a 2.5x Steam-Cavitation shockwave against Umbral-Drenched foes.";
            case BEAM -> "Channels a focused solar-pyric beam that blinds enemies, shreds armor, and erupts in a Prominence Flare after 3.0s of sustained contact.";
            case RADIAL_BURST -> "Detonates a 360-degree solar corona shockwave that ignites hostiles, blinds undead, and smelts nearby raw drops.";
            case TOUCH_STRIKE -> "Sweeps a blazing Caprine sunfire blade that shreds 3 armor points, ignites targets, and triggers Steam-Cavitation on drenched foes.";
            case AURA -> "Cloaks the caster in purging sunfire granting Fire Resistance, Blindness/Darkness immunity, and solar ember retaliation.";
            case GLYPH -> "Inscribes a Caprine crucible sigil that erupts in purging solar flames under hostiles while cleansing debuffs from allies.";
            case CONE -> "Unleashes a wide fan of solar-prominence flames that pushes foes back, reveals hidden entities, and ignites undead.";
            case SKYFALL -> "Calls down a 12-second bombardment of blazing solar-prominence meteors across the target zone.";
            case SUMMON_AGENT -> "Summons an autonomous Solar-Prominence Seraph armed with purging sunfire lances.";
            case SUMMON_SENTRY -> "Summons an anchored Helios Pyre Obelisk projecting searing solar-fire bolts.";
            case SUMMON_DECOY -> "Manifests an Incandescent Corona Effigy that taunts hostiles before detonating in a blinding sunfire burst.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Solar Prominence Flash-Step";
            case TRANSMUTE, BREAK -> "Caprine Sunfire Calcination";
            case AFFLICTION -> "Solar Prominence Sear Hex";
            default -> "Solar Prominence " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Teleports in a blinding solar flash, cleansing 1 negative effect on arrival and scorching the departure point.";
            case TRANSMUTE, BREAK -> "Purifies rotten flesh into leather and calcines sand, cobblestone, and raw ores with clean Caprine sunfire.";
            case AFFLICTION -> "Brands the target with a milk-immune Solar Prominence Hex that shreds 3 armor, reveals through walls, and sears continuously.";
            default -> "Infuses the effect with purging Caprine Radiant Fire Materia.";
        };
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), BLAZING_VERMILION_HEX, 14, 0.74F, 0.08F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_PROMINENCE_GOLD_HEX, 14, 0.70F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
        }
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
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
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
        applySunfireBrandAndCavitationCheck(level, living, caster, tier, !slot.hasHarmVerb() ? (3.20F + tier * 0.85F) : (1.50F + tier * 0.45F));
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D),
                projectile.getDeltaMovement(), EssenceType.RADIANT_FIRE, EssenceType.RADIANT, 0.95F, tier
        );
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
        Vec3 hitPos = blockHit.getLocation();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, hitPos, normal, EssenceType.RADIANT_FIRE, EssenceType.NETHER, 0.90F, spell.getEffectiveTier()
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
            hit.igniteForSeconds(5);
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 60L);

            if (!slot.hasHarmVerb()) {
                float tickDmg = (1.50F + tier * 0.42F) * Math.max(0.5F, apparatusPotency);
                hurtWithRadiantFireAdvantage(level, hit, caster, tickDmg);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_RF_BEAM_LAST_TICK).orElse(-100L);
            if (now == last) {
                continue;
            }
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_RF_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_RF_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_RF_BEAM_TICKS, 0);
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 1.0D, 100L);
                float burstDmg = (3.80F + tier * 0.95F) * Math.max(0.5F, apparatusPotency);
                applySunfireBrandAndCavitationCheck(level, hit, caster, tier, burstDmg);
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 3.8D, EssenceType.RADIANT_FIRE, EssenceType.RADIANT, tier);
                level.playSound(null, hit.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.25F);
            } else {
                data.putInt(TAG_RF_BEAM_TICKS, contactTicks);
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

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.RADIANT_FIRE, EssenceType.NETHER, tier);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 4.0D, center.z + r);
        float novaDmg = (3.30F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 away = foe.position().subtract(center);
            Vec3 push = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.35D) : new Vec3(0.0D, 0.45D, 0.0D);
            foe.setDeltaMovement(push.x, 0.45D, push.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            applySunfireBrandAndCavitationCheck(level, foe, caster, tier, !slot.hasHarmVerb() ? novaDmg : (novaDmg * 0.35F));
        }

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            onTransmuteDroppedItem(level, caster, item, tier);
        }
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.8D, center.z, 28, 2.0D, 0.5D, 2.0D, 0.06D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 24, 2.0D, 0.5D, 2.0D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.1F, 1.15F);
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
        Set<UUID> hitIds = new HashSet<>();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.2D,
                EssenceType.RADIANT_FIRE, EssenceType.RADIANT, tier
        );

        float strikeDmg = (3.10F + tier * 0.80F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            hitIds.add(hit.getUUID());
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
            applySunfireBrandAndCavitationCheck(level, hit, caster, tier, !slot.hasHarmVerb() ? strikeDmg : (strikeDmg * 0.35F));
        }

        AABB sweep = caster.getBoundingBox().expandTowards(direction.scale(3.0D)).inflate(1.4D, 0.8D, 1.4D);
        for (LivingEntity extra : level.getEntitiesOfClass(LivingEntity.class, sweep, LivingEntity::isAlive)) {
            if (extra == caster || hitIds.contains(extra.getUUID())) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, extra)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(extra, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(extra, level, 3.0D, 0.0D, 120L);
            applySunfireBrandAndCavitationCheck(level, extra, caster, tier, !slot.hasHarmVerb() ? strikeDmg : (strikeDmg * 0.35F));
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.18F);
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
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, dur, 0, false, true));
        level.sendParticles(ParticleTypes.FLAME, caster.getX(), caster.getY(0.8D), caster.getZ(), 16, 0.45D, 0.5D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 16, 0.45D, 0.5D, 0.45D, 0.04D);
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
                level, glyphCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.RADIANT_FIRE, EssenceType.RADIANT, 240, tier
        );

        SpellLingeringZoneManager.ActiveSpellZone glyph = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, r, maxDuration, level.getGameTime(), spell, slot, EssenceType.RADIANT_FIRE, apparatusPotency,
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
                    List<LivingEntity> entities = srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive);
                    if (entities.isEmpty()) return true;

                    boolean triggered = false;
                    for (LivingEntity e : entities) {
                        if (e == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) continue;
                        triggered = true;
                        break;
                    }
                    if (!triggered) return true;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, Math.max(4.0D, zone.radius), EssenceType.RADIANT_FIRE, EssenceType.NETHER, tier);
                    float trapDmg = (3.40F + tier * 0.85F) * Math.max(0.5F, zone.apparatusPotency);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box.inflate(1.8D), LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        foe.setDeltaMovement(foe.getDeltaMovement().x, 0.68D, foe.getDeltaMovement().z);
                        foe.hasImpulse = true;
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 3.0D, 0.0D, 120L);
                        applySunfireBrandAndCavitationCheck(srv, foe, owner, tier, trapDmg);
                    }
                    triggersLeft[0]--;
                    cooldown[0] = 40;
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
                EssenceType.RADIANT_FIRE, EssenceType.RADIANT, tier
        );
        float coneDmg = (3.00F + tier * 0.78F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.setDeltaMovement(direction.x * 1.15D, 0.32D, direction.z * 1.15D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 100L);
            applySunfireBrandAndCavitationCheck(level, hit, caster, tier, !slot.hasHarmVerb() ? coneDmg : (coneDmg * 0.35F));
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

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.RADIANT_FIRE, EssenceType.NETHER, 240, tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 240, level.getGameTime(), spell, slot, EssenceType.RADIANT_FIRE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.FLAME, zone.center.x, zone.center.y + 4.5D, zone.center.z,
                                16, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.05D);
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 5.0D, zone.center.z,
                                14, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        float tickDmg = (2.40F + tier * 0.65F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                            SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 3.0D, 0.0D, 60L);
                            applySunfireBrandAndCavitationCheck(srv, foe, owner, tier, tickDmg);
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
        tag.putString("SpellEidolonMateria", EssenceType.RADIANT_FIRE.name());
        tag.putInt("SpellEidolonTintRGB", SOLAR_PROMINENCE_GOLD_HEX);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.5D, EssenceType.RADIANT_FIRE, EssenceType.NETHER, 36, spell.getEffectiveTier()
        );
    }

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.RADIANT_FIRE, potencyTier);
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, false, true));
        for (MobEffectInstance inst : List.copyOf(caster.getActiveEffects())) {
            if (!inst.getEffect().value().isBeneficial()) {
                caster.removeEffect(inst.getEffect());
                break;
            }
        }
        level.sendParticles(ParticleTypes.FLAME, departurePos.x, departurePos.y + 0.6D, departurePos.z, 18, 0.4D, 0.4D, 0.4D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 18, 0.4D, 0.4D, 0.4D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        BlockState out = null;
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            out = Blocks.GLASS.defaultBlockState();
        } else if (state.is(Blocks.COBBLESTONE)) {
            out = Blocks.SMOOTH_STONE.defaultBlockState();
        } else if (state.is(Blocks.CLAY)) {
            out = Blocks.TERRACOTTA.defaultBlockState();
        } else if (state.is(Blocks.NETHERRACK)) {
            out = Blocks.NETHER_BRICKS.defaultBlockState();
        } else if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            out = Blocks.GLOWSTONE.defaultBlockState();
        }
        if (out != null) {
            level.setBlockAndUpdate(pos, out);
            level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.03D);
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
        } else if (stack.is(Items.RAW_IRON)) {
            out = new ItemStack(Items.IRON_INGOT, stack.getCount());
        } else if (stack.is(Items.RAW_GOLD)) {
            out = new ItemStack(Items.GOLD_INGOT, stack.getCount());
        } else if (stack.is(Items.RAW_COPPER)) {
            out = new ItemStack(Items.COPPER_INGOT, stack.getCount());
        } else if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            out = new ItemStack(Items.GLASS, stack.getCount());
        }
        if (!out.isEmpty()) {
            itemEntity.setItem(out);
            level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.04D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.removeEffect(MobEffects.INVISIBILITY);
        target.igniteForSeconds(3);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float smiteBonus = target.getType().is(EntityTypeTags.UNDEAD) ? 1.4F : 1.0F;
        float hexDmg = (1.60F + potencyTier * 0.55F) * mult * smiteBonus;
        hurtWithRadiantFireAdvantage(level, target, hexCaster, hexDmg);
        return true;
    }

    static void applySunfireBrandAndCavitationCheck(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            int tier,
            float directDamage
    ) {
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);
        data.putLong(TAG_RADIANT_FIRE_SEAR_UNTIL, now + 100L);
        target.removeEffect(MobEffects.INVISIBILITY);
        target.igniteForSeconds(5);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));

        long umbralDrenchUntil = data.getLong(UmbralWaterSpellMutationHandler.TAG_UMBRAL_WATER_DRENCH_UNTIL).orElse(0L);
        if (umbralDrenchUntil >= now) {
            // Caprine Alchemy Thermal-Void Shock: 2.5x Steam-Cavitation AoE detonation!
            data.remove(UmbralWaterSpellMutationHandler.TAG_UMBRAL_WATER_DRENCH_UNTIL);
            float cavitationDmg = Math.max(4.0F, directDamage * 2.5F);
            SpellWorldVfxManager.broadcastRadialNova(level, target.position(), 4.0D, EssenceType.EQUINOX, EssenceType.RADIANT_FIRE, tier);
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.7D), target.getZ(), 24, 1.2D, 0.6D, 1.2D, 0.08D);
            level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.85F, 1.35F);

            AABB shockBox = target.getBoundingBox().inflate(3.5D, 2.0D, 3.5D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, shockBox, LivingEntity::isAlive)) {
                if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                hurtWithRadiantFireAdvantage(level, foe, caster, foe == target ? cavitationDmg : cavitationDmg * 0.65F);
            }
            return;
        }

        if (directDamage > 0.0F) {
            float smite = target.getType().is(EntityTypeTags.UNDEAD) ? 1.35F : 1.0F;
            hurtWithRadiantFireAdvantage(level, target, caster, directDamage * smite);
        }
    }

    static float hurtWithRadiantFireAdvantage(
            ServerLevel level,
            LivingEntity target,
            @Nullable LivingEntity caster,
            float baseDamage
    ) {
        if (!target.isAlive() || baseDamage <= 0.0F) return 0.0F;
        Set<EssenceType> defenderAffs = SpellCombatReactionEngine.resolveEntityAffinities(target);
        boolean mobToPlayer = (target instanceof Player) && !(caster instanceof Player);
        float mult = VisInteractionManager.getInstance().calculateDamageMultiplier(
                EssenceType.RADIANT_FIRE, defenderAffs, mobToPlayer, 0.0F
        );
        if (mult <= 0.0F) return 0.0F;
        float finalDmg = Math.max(0.5F, baseDamage * mult);
        target.invulnerableTime = 0;
        DamageSource src = caster != null ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic();
        return SpellCombatReactionEngine.hurtWithPureMateria(level, target, src, finalDmg) ? finalDmg : 0.0F;
    }
}
