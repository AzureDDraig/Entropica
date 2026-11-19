package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Vapor Materia (Ethereal &amp; Paradox Fusion #29)</b>
 * (Spellcrafting System Guide Section 6.30 — Dual Water {@code #E0F7FA} + Nether {@code #30A9B0} [Leans: Water]).
 */
public final class VaporSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Scalding Steam White (#E0F7FA) primary tint. */
    public static final int SCALDING_STEAM_HEX_COLOR = 0xE0F7FA;

    /** Canonical Boiling Aquatic Cyan (#30A9B0) secondary tint. */
    public static final int AQUATIC_CYAN_HEX_COLOR = 0x30A9B0;

    private static final String TAG_VAPOR_BEAM_LAST_TICK = "SpellVaporBeamLastTick";
    private static final String TAG_VAPOR_BEAM_TICKS = "SpellVaporBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.VAPOR;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Hydro-Steam Split Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Scalding Steam-Burst Geyser Orb";
            case BEAM -> "Scalding Steam Jetstream";
            case RADIAL_BURST -> "Scalding Pressure Blast";
            case TOUCH_STRIKE -> "Superheated Hydro-Gouge";
            case AURA -> "Mist-Walker Shroud";
            case GLYPH -> "Scalding Geyser Pitfall Trap";
            case CONE -> "Scalding Steam-Blast Deluge";
            case SKYFALL -> "Thermal Condensation Storm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Mist-Slip Blink";
            case TRANSMUTE, BREAK -> "Cloud Condensation Transmute";
            case AFFLICTION -> "Scalding Dissolution Hex";
            default -> "Scalding Vapor " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — HYDRO-STEAM SPLIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
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
        // Section 6.30 Hydro-Steam Split Flow:
        // Splits into 3 pressurized steam bursts upon hitting a target or apex range,
        // arcing outward in a wide fan to blind secondary enemies!
        float[] fan = new float[]{-28.0F, 0.0F, 28.0F};
        for (float yaw : fan) {
            SpellProjectileEntity child = projectile.spawnSplitChild(level, splitOrigin, yaw, false, 0.58F, 1.32F);
            child.enableSlightFluidArc(false);
        }
        level.sendParticles(ParticleTypes.CLOUD, splitOrigin.x, splitOrigin.y + 0.3D, splitOrigin.z, 18, 0.35D, 0.3D, 0.35D, 0.06D);
        level.playSound(null, BlockPos.containing(splitOrigin), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.85F, 1.28F);
    }

    // =========================================================================
    // FORM MUTATION 1 — SCALDING STEAM-BURST GEYSER ORB (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
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
        spawnSteamGeyserUpdraft(level, caster, hitEntity.position(), spell, slot);
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
        spawnSteamGeyserUpdraft(level, caster, blockHit.getLocation(), spell, slot);
    }

    private static void spawnSteamGeyserUpdraft(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.30 Scalding Steam-Burst Geyser Orb:
        // Bursts into a 3m cloud of boiling vapor dealing Vapor Materia damage, blinding enemies for 3.0s (60t),
        // dousing surface fires, and leaving a thermal convective updraft that launches entities 4m airborne!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float scaldDmg = (slot != null && slot.hasHarmVerb())
                ? (1.75F + tier * 0.46F)
                : (3.20F + tier * 0.84F);

        SpellWorldVfxManager.broadcastGeyserPlume(level, center, 2.2D, 4.2D, EssenceType.VAPOR, EssenceType.WATER, 60, tier);
        AABB box = new AABB(center.x - 3.0D, center.y - 1.5D, center.z - 3.0D, center.x + 3.0D, center.y + 2.6D, center.z + 3.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applySoaked(foe, 140);
            SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(level, foe, caster, scaldDmg, false, true);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.40D, 0.80D, foe.getDeltaMovement().z * 0.40D);
            foe.hasImpulse = true;
        }
        douseFiresAndMeltIce(level, BlockPos.containing(center), 3, false);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 2.8D, 60, level.getGameTime(), spell, slot, EssenceType.VAPOR, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 0.5D, zone.center.z, 10, 1.1D, 0.6D, 1.1D, 0.05D);
                        AABB updraftBox = new AABB(
                                zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                                zone.center.x + 2.6D, zone.center.y + 3.2D, zone.center.z + 2.6D
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, updraftBox, LivingEntity::isAlive)) {
                            ent.setDeltaMovement(ent.getDeltaMovement().x, Math.max(ent.getDeltaMovement().y, 0.78D), ent.getDeltaMovement().z);
                            ent.hasImpulse = true;
                        }
                    }
                    return true;
                }
        ));
        level.playSound(null, BlockPos.containing(center), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.95F, 1.15F);
    }

    // =========================================================================
    // FORM MUTATION 2 — SCALDING STEAM JETSTREAM (BEAM)
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
        // Section 6.30 Scalding Steam Jetstream:
        // Blinds enemies, pushes targets 3 blocks back, instantly melts snow & packed ice into water,
        // provides vertical jetpack recoil thrust when aimed downward, and channeling for 3.0s (60 contact ticks = 15 pulses)
        // triggers a Convective Hydro-Burst that launches caught targets 6 blocks airborne!
        int tier = spell.getEffectiveTier();
        if (direction.y < -0.45D) {
            caster.setDeltaMovement(
                    caster.getDeltaMovement().x,
                    Math.max(0.58D, caster.getDeltaMovement().y + 0.42D),
                    caster.getDeltaMovement().z
            );
            caster.hasImpulse = true;
            caster.resetFallDistance();
        }

        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            SpellLingeringZoneManager.applySoaked(hit, 140);
            hit.setDeltaMovement(direction.x * 0.82D, 0.25D, direction.z * 0.82D);
            hit.hasImpulse = true;

            if (!slot.hasHarmVerb()) {
                float jetDmg = (1.15F + tier * 0.34F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(level, hit, caster, jetDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_VAPOR_BEAM_LAST_TICK).orElse(-100L);
            int accumTicks = (now - last <= 12L) ? data.getInt(TAG_VAPOR_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_VAPOR_BEAM_LAST_TICK, now);

            if (accumTicks >= 60) {
                data.putInt(TAG_VAPOR_BEAM_TICKS, 0);
                hit.removeEffect(MobEffects.SLOW_FALLING);
                hit.setDeltaMovement(direction.x * 0.45D, 1.04D, direction.z * 0.45D);
                hit.hasImpulse = true;
                SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(
                        level, hit, caster, (3.20F + tier * 0.85F) * Math.max(0.5F, apparatusPotency), false, true
                );
                SpellWorldVfxManager.broadcastGeyserPlume(level, hit.position(), 1.8D, 6.0D, EssenceType.VAPOR, EssenceType.WATER, 40, tier);
                level.sendParticles(ParticleTypes.CLOUD, hit.getX(), hit.getY(0.8D), hit.getZ(), 22, 0.45D, 0.5D, 0.45D, 0.08D);
                level.playSound(null, hit.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.90F);
            } else {
                data.putInt(TAG_VAPOR_BEAM_TICKS, accumTicks);
            }
        }

        int steps = Math.min(14, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            douseFiresAndMeltIce(level, BlockPos.containing(caster.getEyePosition().add(direction.scale(s))), 1, true);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SCALDING PRESSURE BLAST (RADIAL BURST)
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
        // Section 6.30 Scalding Pressure Blast:
        // 6m+ pneumatic blast of compressed steam (#E0F7FA / #30A9B0); launches surrounding mobs
        // 8 blocks airborne and 6 blocks backward while dealing fall damage and shattering glass!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.2D, center.z + r);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            foe.removeEffect(MobEffects.SLOW_FALLING);
            Vec3 delta = foe.position().subtract(center);
            Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
            Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize().scale(1.48D) : direction.scale(1.48D);
            foe.setDeltaMovement(away.x, 1.22D, away.z);
            foe.hasImpulse = true;
            SpellLingeringZoneManager.applySoaked(foe, 160);

            if (!slot.hasHarmVerb()) {
                float blastDmg = (3.35F + tier * 0.86F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(level, foe, caster, blastDmg, false, true);
            }
        }

        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-4, -1, -4), cPos.offset(4, 2, 4))) {
            BlockState bs = level.getBlockState(p);
            if (isShatterableGlass(bs) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                level.destroyBlock(p, true, caster);
            }
        }
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.9D, center.z, 42, 2.3D, 0.6D, 2.3D, 0.10D);
        level.playSound(null, cPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.1F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 4 — SUPERHEATED HYDRO-GOUGE (TOUCH / STRIKE)
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
        // Section 6.30 Superheated Hydro-Gouge:
        // Deals Vapor Materia damage, bypasses 40% of target armor, extinguishes fires,
        // and causes slain enemies to dissolve into a steam cloud that blinds adjacent hostiles!
        int tier = spell.getEffectiveTier();
        caster.clearFire();
        long until = level.getGameTime() + 100L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VAPOR_BOIL_DEATH_UNTIL, until);
            hit.clearFire();
            SpellLingeringZoneManager.applySoaked(hit, 140);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.40D, 0.0D, 40L);

            if (!slot.hasHarmVerb()) {
                float gougeDmg = (2.85F + tier * 0.75F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(level, hit, caster, gougeDmg, true, true);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — MIST-WALKER SHROUD (AURA / SELF)
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
        // Section 6.30 Mist-Walker Shroud:
        // Cloaks caster in thick fog (#E0F7FA / #30A9B0); grants invisibility while moving through rain or water,
        // and boosts swim and sprint speed by +30%!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_VAPOR_MIST_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.clearFire();
        SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.30D, dur);
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, true));
        if (caster.isInWaterOrRain()) {
            caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
        }
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);

        level.sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.03D);
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.85F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 6 — SCALDING GEYSER PITFALL TRAP (GLYPH / TRAP)
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
        // Section 6.30 Scalding Geyser Pitfall Trap:
        // Disarmable Single/Repeating condensation rune (1.0s arming delay); when tripped, erupts into a violent
        // vertical jet of scalding steam for 3.0s (60t), launching mobs 10 blocks straight up into the air
        // (dealing heavy fall damage on descent), blinding enemies for 4.0s (80t), dealing Vapor Materia damage,
        // and snuffing out torches in a 4-block radius!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.VAPOR, EssenceType.WATER, Math.min(240, maxDuration), tier
        );
        level.playSound(null, surface.above(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.80F, 1.25F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.VAPOR, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.VAPOR, EssenceType.WATER, rem, tier
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

                    EclipseSpellMutationHandler.snuffTorchesAndFires(srv, BlockPos.containing(zone.center), 4);
                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, 2.4D, 10.0D, EssenceType.VAPOR, EssenceType.WATER, 60, tier
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.1F, 0.88F);

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 3.2D, 60, srv.getGameTime(),
                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (gLvl, gZone, gEl) -> {
                                if (gEl > gZone.maxDurationTicks) return false;
                                if (gEl % 5 == 0) {
                                    gLvl.sendParticles(ParticleTypes.CLOUD, gZone.center.x, gZone.center.y + 2.2D, gZone.center.z, 16, 0.6D, 2.2D, 0.6D, 0.08D);
                                }
                                if (gEl == 0 || gEl % 20 == 0) {
                                    LivingEntity gOwner = gZone.resolveCaster(gLvl);
                                    boolean gWard = gZone.spell != null && gZone.spell.hasWardFriendlyFireSafeguard();
                                    AABB jetBox = new AABB(
                                            gZone.center.x - 3.2D, gZone.center.y - 1.0D, gZone.center.z - 3.2D,
                                            gZone.center.x + 3.2D, gZone.center.y + 5.5D, gZone.center.z + 3.2D
                                    );
                                    for (LivingEntity v : gLvl.getEntitiesOfClass(LivingEntity.class, jetBox, LivingEntity::isAlive)) {
                                        if (v == gOwner || SpellExecutionEngine.isFriendlyAllyForOasis(gOwner, v)) continue;
                                        if (gOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, gOwner, gWard)) continue;

                                        v.removeEffect(MobEffects.SLOW_FALLING);
                                        v.setDeltaMovement(v.getDeltaMovement().x * 0.20D, 1.44D, v.getDeltaMovement().z * 0.20D);
                                        v.hasImpulse = true;
                                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                                        SpellLingeringZoneManager.applySoaked(v, 140);

                                        if (gZone.spell != null && gZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    gLvl, gOwner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                                    gZone.spell, gZone.slot, gZone.fallbackMateria, gZone.apparatusPotency * 0.55F
                                            );
                                        }
                                        float jetDmg = (2.15F + tier * 0.55F) * Math.max(0.5F, gZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(gLvl, v, gOwner, jetDmg, false, true);
                                    }
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
    // FORM MUTATION 7 — SCALDING STEAM-BLAST DELUGE (CONE / SPRAY)
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
        // Section 6.30 Scalding Steam-Blast Deluge:
        // Wide 60° cone of superheated boiling steam and water droplets; pushes enemies 6 blocks back,
        // blinds targets for 4.0s (80t), snuffs out torches and fires, melts snow and packed ice into water,
        // and provides backward recoil thrust to the caster!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        Vec3 recoil = direction.scale(-0.68D);
        caster.setDeltaMovement(caster.getDeltaMovement().add(recoil.x, Math.max(0.16D, recoil.y * 0.5D), recoil.z));
        caster.hasImpulse = true;

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.48D, 0.36D, direction.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            SpellLingeringZoneManager.applySoaked(hit, 160);

            if (!slot.hasHarmVerb()) {
                float delugeDmg = (2.75F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
            }
        }

        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            EclipseSpellMutationHandler.snuffTorchesAndFires(level, stepPos, 2);
            douseFiresAndMeltIce(level, stepPos, 2, true);
        }
    }

    // =========================================================================
    // FORM MUTATION 8 — THERMAL CONDENSATION STORM (SKYFALL)
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
        // Section 6.30 Thermal Condensation Storm:
        // Calls down scalding rain for 12.0s (240t) across a 14m+ radius; continuously deals Vapor Materia damage,
        // shreds 1 armor point per 2.0s of exposure (up to 6 total), and hydrates all farmland in the radius!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.VAPOR, EssenceType.WATER, 240, tier
        );
        hydrateFarmlandInRadius(level, BlockPos.containing(impactCenter), (int) Math.ceil(r));
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.PLAYERS, 1.0F, 0.90F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 240, level.getGameTime(), spell, slot, EssenceType.VAPOR, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD,
                                zone.center.x, zone.center.y + 4.5D, zone.center.z,
                                18, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.04D);
                        srv.sendParticles(ParticleTypes.RAIN,
                                zone.center.x, zone.center.y + 4.0D, zone.center.z,
                                24, zone.radius * 0.55D, 1.0D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 40 == 0 && elapsed > 0) {
                        hydrateFarmlandInRadius(srv, BlockPos.containing(zone.center), (int) Math.ceil(zone.radius));
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int armorShred = Math.min(6, 1 + (elapsed / 40));
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            SpellLingeringZoneManager.applySoaked(v, 80);
                            SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, armorShred, 0.0D, 60L);
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                );
                            }
                            float rainDmg = (2.10F + tier * 0.54F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(srv, v, owner, rainDmg, false, true);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — STEAM-AUTOMATON SUITE (SUMMON)
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
        tag.putString("SpellEidolonMateria", EssenceType.VAPOR.name());
        tag.putInt("SpellEidolonTintRGB", SCALDING_STEAM_HEX_COLOR);
        level.sendParticles(ParticleTypes.CLOUD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Section 6.30 Mist-Slip Blink:
        // Teleports 16 blocks invisibly through enemies, leaving a dense fog trail behind!
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
        Vec3 delta = arrivalPos.subtract(departurePos);
        int steps = Math.max(2, (int) Math.ceil(delta.length() * 0.55D));
        Vec3 stepVec = delta.scale(1.0D / steps);

        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(ParticleTypes.CLOUD, p.x, p.y + 0.9D, p.z, 9, 0.45D, 0.45D, 0.45D, 0.02D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SCALDING_STEAM_HEX_COLOR, 36, 0.95F, 0.04F),
                    p.x, p.y + 0.8D, p.z, 3, 0.35D, 0.35D, 0.35D, 0.01D
            );
            AABB fogBox = new AABB(p.x - 1.8D, p.y - 0.5D, p.z - 1.8D, p.x + 1.8D, p.y + 2.2D, p.z + 1.8D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, fogBox)) {
                if (foe == caster || !foe.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.30 Cloud Condensation Transmute:
        // Condenses air blocks into water source blocks and dries mud into clay!
        if (!state.isAir() && SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.isAir()) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            maybeGrantVaporBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            maybeGrantVaporBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.WET_SPONGE.defaultBlockState());
            maybeGrantVaporBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.ICE) || state.is(Blocks.SNOW_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            maybeGrantVaporBatchTransmuteDiscount(level, caster, potencyTier);
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
        if (in.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, in.getCount()));
            maybeGrantVaporBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.SPONGE)) {
            itemEntity.setItem(new ItemStack(Items.WET_SPONGE, in.getCount()));
            maybeGrantVaporBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.BUCKET) && in.getCount() == 1) {
            itemEntity.setItem(new ItemStack(Items.WATER_BUCKET, 1));
            maybeGrantVaporBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.30 Scalding Dissolution Hex:
        // Persistent milk-immune superheated moisture curse (cleansed via Restoration/Cryo magic or caster defeat):
        // dissolves 2 armor points every 3.0s (up to 10 points shredded total), slows movement speed by 30%,
        // and increases vulnerability to electrical shock damage by +50%!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        CompoundTag data = EntityHelper.getPersistentData(target);
        int ticksElapsed = data.getInt("VaporHexDissolveSteps").orElse(0) + 1;
        data.putInt("VaporHexDissolveSteps", ticksElapsed);
        double dissolvedArmor = Math.min(10.0D, (2.0D + (ticksElapsed / 3) * 2.0D) * mult);

        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, dissolvedArmor, 0.0D, 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.30D, 45L);
        SpellLingeringZoneManager.applySoaked(target, 45);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.40F + potencyTier * 0.46F) * mult;
        SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.8D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D);
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
            target.clearFire();
            float heal = 3.1F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static boolean isShatterableGlass(BlockState bs) {
        return bs.is(BlockTags.IMPERMEABLE)
                || bs.is(Blocks.GLASS_PANE)
                || bs.getBlock() instanceof StainedGlassPaneBlock;
    }

    private static void hydrateFarmlandInRadius(ServerLevel level, BlockPos center, int rad) {
        int clamped = Math.min(18, Math.max(4, rad));
        int rSq = clamped * clamped;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-clamped, -2, -clamped), center.offset(clamped, 2, clamped))) {
            int dx = p.getX() - center.getX();
            int dz = p.getZ() - center.getZ();
            if (dx * dx + dz * dz > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlock(p, bs.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
            }
        }
    }

    private static void douseFiresAndMeltIce(ServerLevel level, BlockPos center, int rad, boolean includePackedIce) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock || bs.is(Blocks.SNOW)) {
                level.removeBlock(p, false);
            } else if (!SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)
                    && (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.SNOW_BLOCK)
                    || (includePackedIce && bs.is(Blocks.PACKED_ICE)))) {
                level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
            }
        }
    }

    private static void maybeGrantVaporBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellVaporBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellVaporBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellVaporBatchTick", now);
        pData.putInt("SpellVaporBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.VAPOR, refund);
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
