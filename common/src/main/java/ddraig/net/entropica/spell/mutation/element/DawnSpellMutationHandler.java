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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Dawn Materia (Ethereal &amp; Paradox Fusion #32)</b>
 * (Spellcrafting System Guide Section 6.33 — Dual Radiant {@code #FFEA78} + Air {@code #A88654} [Leans: Radiant]).
 */
public final class DawnSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Blazing Sunrise Gold (#FFEA78) primary tint. */
    public static final int DAWN_WARDEN_HEX_COLOR = 0xFFEA78;

    /** Canonical Atmospheric Buff Air Gold (#A88654) secondary tint. */
    public static final int HORIZON_AIR_HEX_COLOR = 0xA88654;

    private static final String TAG_DAWN_BEAM_LAST_TICK = "SpellDawnBeamLastTick";
    private static final String TAG_DAWN_BEAM_TICKS = "SpellDawnBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.DAWN;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Convective Horizon Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Solar Horizon Javelin";
            case BEAM -> "Pranic Solar Lance";
            case RADIAL_BURST -> "Sunrise Horizon Nova";
            case TOUCH_STRIKE -> "Aero-Solar Cleave";
            case AURA -> "Dawn-Bringer Solar Shroud";
            case GLYPH -> "Pranic Sunrise Well";
            case CONE -> "Solar Flare Breath";
            case SKYFALL -> "Sunrise Daybreak Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Solar Zephyr Blink";
            case TRANSMUTE, BREAK -> "Daybreak Awakening Transmute";
            case AFFLICTION -> "Solar Blind-Sear Hex";
            default -> "Sunrise Horizon " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — CONVECTIVE HORIZON ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), DAWN_WARDEN_HEX_COLOR, 14, 0.72F, 0.12F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), HORIZON_AIR_HEX_COLOR, 14, 0.62F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.05D, 0.06D, 0.01D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — SOLAR HORIZON JAVELIN (PROJECTILE)
    // =========================================================================

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
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), HORIZON_AIR_HEX_COLOR, 14, 0.65F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
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
        // Section 6.33 Solar Horizon Javelin:
        // Aerodynamic daylight javelin (#FFEA78 / #A88654) that pierces through up to 3 targets along an atmospheric
        // slipstream, dragging struck mobs backward with aerodynamic push, revealing invisible targets with Glowing
        // for 5.0s (100t), and igniting undead in solar fire!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (living == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, living)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        Vec3 vel = projectile.getDeltaMovement();
        Vec3 push = vel.lengthSqr() > 1.0E-4D ? vel.normalize().scale(0.98D) : new Vec3(0.0D, 0.28D, 0.0D);
        living.setDeltaMovement(push.x, 0.28D, push.z);
        living.hasImpulse = true;
        living.removeEffect(MobEffects.INVISIBILITY);
        living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D),
                push, EssenceType.DAWN, EssenceType.AIR, 0.90F, tier
        );

        if (living.getType().is(EntityTypeTags.UNDEAD)) {
            living.igniteForSeconds(5);
            float smiteBonus = (slot.hasHarmVerb() ? 2.80F : 5.10F) + tier * 0.95F;
            SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, living, caster, smiteBonus, false, true);
        } else if (!slot.hasHarmVerb()) {
            SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, living, caster, 2.75F + tier * 0.72F, false, true);
        }
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
                level, hitPos, normal, EssenceType.DAWN, EssenceType.AIR, 0.85F, spell.getEffectiveTier()
        );
    }

    // =========================================================================
    // FORM MUTATION 2 — PRANIC SOLAR LANCE (BEAM)
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
        // Section 6.33 Pranic Solar Lance:
        // Sustained lance of concentrated sunrise light (#FFEA78 / #A88654); blinds and staggers hostiles held in the
        // beam while siphoning 20% of damage dealt into caster Materia fuel; continuous channeling for 3.0s
        // (60 contact ticks = 15 beam slices at 4t per slice) triggers a Sunrise Flash that knocks mobs back 6 blocks!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.removeEffect(MobEffects.INVISIBILITY);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.igniteForSeconds(4);
            }

            if (!slot.hasHarmVerb()) {
                float lanceDmg = (1.30F + tier * 0.36F) * Math.max(0.5F, apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, hit, caster, lanceDmg, false, true);
                if (dealt > 0.0F && caster instanceof Player player && player.isAlive()) {
                    SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.DAWN, tier, dealt, 0.20F);
                }
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_DAWN_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_DAWN_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_DAWN_BEAM_LAST_TICK, now);

            if (contactTicks >= 60) {
                data.putInt(TAG_DAWN_BEAM_TICKS, 0);
                hit.setDeltaMovement(direction.x * 1.52D, 0.42D, direction.z * 1.52D);
                hit.hasImpulse = true;
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));

                float flashDmg = (3.35F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
                float flashDealt = SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, hit, caster, flashDmg, false, true);
                if (flashDealt > 0.0F && caster instanceof Player player && player.isAlive()) {
                    SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.DAWN, tier, flashDealt, 0.20F);
                }

                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 3.6D, EssenceType.DAWN, EssenceType.AIR, tier);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                        hit.getX(), hit.getY(0.9D), hit.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                level.playSound(null, hit.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.32F);
            } else {
                data.putInt(TAG_DAWN_BEAM_TICKS, contactTicks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SUNRISE HORIZON NOVA (RADIAL BURST)
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
        // Section 6.33 Sunrise Horizon Nova:
        // Brilliant 360° shockwave of golden sunrise light & howling gale winds (6m+ radius, #FFEA78 / #A88654);
        // blasts hostiles 8 blocks backward and 3 blocks airborne, inflicts 4.0s (80t) Blindness & Glowing,
        // smites undead with double damage, and clears lingering gas clouds & cobwebs WITHOUT placing dynamic light blocks!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.DAWN, EssenceType.AIR, tier);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 4.0D, center.z + r);
        float baseNovaDmg = MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = foe.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.72D, 0.0D);
            foe.setDeltaMovement(away.x, 0.72D, away.z);
            foe.hasImpulse = true;
            foe.removeEffect(MobEffects.INVISIBILITY);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));

            boolean undead = foe.getType().is(EntityTypeTags.UNDEAD);
            if (undead) {
                foe.igniteForSeconds(6);
            }
            if (slot.hasHarmVerb()) {
                if (undead) {
                    // Add +100% Dawn Materia smite on top of Core Harm so undead take true double (2.0x) damage!
                    SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(
                            level, foe, caster, Math.max(3.5F, baseNovaDmg), false, true
                    );
                }
            } else {
                float nonHarmDmg = (3.10F + tier * 0.78F) * Math.max(0.5F, apparatusPotency) * (undead ? 2.0F : 1.0F);
                SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, foe, caster, nonHarmDmg, false, true);
            }
        }

        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, box)) {
            cloud.discard();
        }
        BlockPos cPos = BlockPos.containing(center);
        int blockRad = Math.min(7, (int) Math.ceil(r));
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-blockRad, -1, -blockRad), cPos.offset(blockRad, 3, blockRad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.COBWEB) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                level.destroyBlock(p, true);
            }
        }

        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 32, 2.2D, 0.6D, 2.2D, 0.10D);
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, center.x, center.y + 0.8D, center.z, 4, 1.5D, 0.3D, 1.5D, 0.02D);
        level.playSound(null, cPos, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.12F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 4 — AERO-SOLAR CLEAVE (TOUCH / STRIKE)
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
        // Section 6.33 Aero-Solar Cleave:
        // Broad crescent of sunrise wind (#FFEA78 / #A88654); deals Dawn Materia damage (stacking held weapon Materia),
        // hits all enemies in a 3m frontal sweep, deflects incoming arrows during the swing, knocks enemies back 4m,
        // and grants the caster +30% sprint and attack speed for 5.0s (100t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Set<UUID> alreadyHit = new HashSet<>();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 3.2D,
                EssenceType.DAWN, EssenceType.AIR, tier
        );

        float cleaveDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            alreadyHit.add(hit.getUUID());
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            applyAeroSolarCleaveHit(level, caster, hit, direction, !slot.hasHarmVerb() ? cleaveDmg : 0.0F);
        }

        AABB sweepBox = caster.getBoundingBox().expandTowards(direction.scale(3.0D)).inflate(1.5D, 0.85D, 1.5D);
        for (LivingEntity extra : level.getEntitiesOfClass(LivingEntity.class, sweepBox, LivingEntity::isAlive)) {
            if (extra == caster || alreadyHit.contains(extra.getUUID())) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, extra)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(extra, caster, wardSafe)) continue;

            alreadyHit.add(extra.getUUID());
            SpellExecutionEngine.deliverSubSpellOnEntity(
                    level, caster, extra, direction, spell, slot, fallbackMateria, apparatusPotency
            );
            applyAeroSolarCleaveHit(level, caster, extra, direction, !slot.hasHarmVerb() ? cleaveDmg : 0.0F);
        }

        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, sweepBox.inflate(0.9D))) {
            Entity owner = proj.getOwner();
            if (owner == caster || (owner instanceof LivingEntity lOwner && SpellExecutionEngine.isFriendlyAllyForOasis(caster, lOwner))) {
                continue;
            }
            double spd = Math.max(1.25D, proj.getDeltaMovement().length());
            proj.setDeltaMovement(direction.x * spd, Math.max(0.25D, direction.y * spd), direction.z * spd);
            proj.setOwner(caster);
            proj.hasImpulse = true;
        }

        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, true));
        SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.30D, 100L);
        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(caster, level, 0.30D, 100L);

        Vec3 arcPos = caster.getEyePosition().add(direction.scale(1.8D));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, arcPos.x, arcPos.y - 0.3D, arcPos.z, 3, 0.5D, 0.2D, 0.5D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, arcPos.x, arcPos.y - 0.2D, arcPos.z, 14, 0.8D, 0.3D, 0.8D, 0.06D);
        level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.22F);
    }

    private static void applyAeroSolarCleaveHit(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            float nonHarmDamage
    ) {
        target.setDeltaMovement(direction.x * 1.18D, 0.36D, direction.z * 1.18D);
        target.hasImpulse = true;
        target.removeEffect(MobEffects.INVISIBILITY);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
        if (target.getType().is(EntityTypeTags.UNDEAD)) {
            target.igniteForSeconds(5);
        }
        if (nonHarmDamage > 0.0F) {
            SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, target, caster, nonHarmDamage, true, true);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — DAWN-BRINGER SOLAR SHROUD (AURA / SELF)
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
        // Section 6.33 Dawn-Bringer Solar Shroud:
        // Envelops caster in blazing sunrise radiance & swirling morning air (#FFEA78 / #A88654); grants high-float
        // jumps, slow-falling glide, total immunity to Blindness and Darkness effects, deflects 40% of incoming
        // hostile arrows into the sky, and retaliates against melee attackers with a 3m gale pushback + 2s Blindness!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_DAWN_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    // =========================================================================
    // FORM MUTATION 6 — PRANIC SUNRISE WELL (GLYPH / TRAP)
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
        // Section 6.33 Pranic Sunrise Well:
        // Disarmable Single/Repeating dual-purpose ground glyph (1.0s arming delay); restores 4 hearts (8.0 HP) and
        // clears negative debuffs for friendly players/allies who step on it, OR detonates into a concussive gale
        // blast that repels hostiles 8 blocks backward!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double wellRad = Math.max(3.5D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), wellRad, EssenceType.DAWN, EssenceType.AIR, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, wellRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.DAWN, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.DAWN, EssenceType.AIR, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD,
                                zone.center.x, zone.center.y + 0.16D, zone.center.z, 4, 0.85D, 0.1D, 0.85D, 0.02D);
                    }
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB stepBox = new AABB(
                            zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.2D, zone.center.z + 2.6D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, stepBox, LivingEntity::isAlive);
                    if (stepped.isEmpty()) return true;

                    // 1. Friendly player / pet / ally step -> restore 4 hearts (8.0 HP) & cleanse negative debuffs!
                    for (LivingEntity e : stepped) {
                        boolean isFriendly = (e == owner && elapsed >= 30 && (e.getHealth() < e.getMaxHealth() || hasAnyNegativeDebuff(e)))
                                || (e != owner && SpellExecutionEngine.isFriendlyAllyForOasis(owner, e));
                        if (isFriendly) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(e);
                            SpellCombatReactionEngine.notifyMagicalHeal(e, 8.0F);
                            e.heal(8.0F);
                            srv.sendParticles(ParticleTypes.HEART, e.getX(), e.getY(1.0D), e.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.03D);
                            srv.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY(0.8D), e.getZ(), 16, 0.4D, 0.4D, 0.4D, 0.05D);
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.45F);
                            triggersLeft[0]--;
                            rearmCooldown[0] = 50;
                            return triggersLeft[0] > 0;
                        }
                    }

                    // 2. Hostile step -> concussive gale blast repelling hostiles 8 blocks backward!
                    boolean hasHostileTrigger = false;
                    for (LivingEntity e : stepped) {
                        if (e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))) {
                            hasHostileTrigger = true;
                            break;
                        }
                    }
                    if (!hasHostileTrigger) return true;

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, Math.max(4.5D, zone.radius), EssenceType.DAWN, EssenceType.AIR, tier);
                    AABB blastBox = new AABB(
                            zone.center.x - 4.5D, zone.center.y - 1.5D, zone.center.z - 4.5D,
                            zone.center.x + 4.5D, zone.center.y + 3.5D, zone.center.z + 4.5D
                    );
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    float galeDmg = (2.95F + zTier * 0.75F) * Math.max(0.5F, zone.apparatusPotency);

                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                        Vec3 delta = foe.position().subtract(zone.center);
                        Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.52D, 0.0D);
                        foe.setDeltaMovement(away.x, 0.52D, away.z);
                        foe.hasImpulse = true;
                        foe.removeEffect(MobEffects.INVISIBILITY);
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                        if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                            foe.igniteForSeconds(5);
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, away.normalize(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(srv, foe, owner, galeDmg, false, true);
                        }
                    }

                    srv.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, zone.center.x, zone.center.y + 0.6D, zone.center.z, 3, 0.8D, 0.3D, 0.8D, 0.02D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.1F, 1.3F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 55;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — SOLAR FLARE BREATH (CONE / SPRAY)
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
        // Section 6.33 Solar Flare Breath:
        // Wide 60° fan of dawn light & superheated air (#FFEA78 / #A88654); pushes enemies 5 blocks back,
        // sets undead ablaze in solar fire for 6.0s, deals Dawn Materia damage, and illuminates stealth entities
        // with Glowing for 6.0s (120t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.DAWN, EssenceType.AIR, tier
        );

        float breathDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.28D, 0.34D, direction.z * 1.28D);
            hit.hasImpulse = true;
            hit.removeEffect(MobEffects.INVISIBILITY);
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.igniteForSeconds(6);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, hit, caster, breathDmg, false, true);
            }
        }

        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class,
                new AABB(mid.x - 4.0D, mid.y - 2.0D, mid.z - 4.0D, mid.x + 4.0D, mid.y + 2.0D, mid.z + 4.0D))) {
            cloud.discard();
        }
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 22, 1.4D, 0.5D, 1.4D, 0.07D);
        level.sendParticles(ParticleTypes.CLOUD, mid.x, mid.y, mid.z, 14, 1.3D, 0.5D, 1.3D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 8 — SUNRISE DAYBREAK TEMPEST (SKYFALL)
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
        // Section 6.33 Sunrise Daybreak Tempest:
        // Opens a celestial sunrise rift in the sky for 10.0s (200t) across a 14m+ radius raining lances of
        // concentrated morning sunlight & howling updrafts; repeatedly launches hostiles 4 blocks airborne with
        // convective gusts, smites undead with continuous holy damage, strips active darkness/blindness effects,
        // and heals allies inside the storm for 1.0 HP per second!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.DAWN, EssenceType.AIR, 200, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.DAWN, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD,
                                zone.center.x, zone.center.y + 5.0D, zone.center.z,
                                18, zone.radius * 0.52D, 1.3D, zone.radius * 0.52D, 0.06D);
                        srv.sendParticles(ParticleTypes.CLOUD,
                                zone.center.x, zone.center.y + 1.0D, zone.center.z,
                                10, zone.radius * 0.48D, 0.4D, zone.radius * 0.48D, 0.03D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius
                        );
                        float stormDmg = (2.10F + zTier * 0.56F) * Math.max(0.5F, zone.apparatusPotency);

                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (e == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) {
                                e.removeEffect(MobEffects.DARKNESS);
                                e.removeEffect(MobEffects.BLINDNESS);
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 1.0F);
                                e.heal(1.0F);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) {
                                continue;
                            }

                            e.setDeltaMovement(e.getDeltaMovement().x, 0.82D, e.getDeltaMovement().z);
                            e.hasImpulse = true;
                            e.removeEffect(MobEffects.INVISIBILITY);
                            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));

                            boolean undead = e.getType().is(EntityTypeTags.UNDEAD);
                            if (undead) {
                                e.igniteForSeconds(4);
                            }
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, e, new Vec3(0.0D, 1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                );
                            }
                            if (undead || zone.slot == null || !zone.slot.hasHarmVerb()) {
                                float tickDmg = undead ? stormDmg * 1.45F : stormDmg;
                                SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(srv, e, owner, tickDmg, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — DAWN-WARDEN EIDOLON SUITE (SUMMON)
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
        tag.putString("SpellEidolonMateria", EssenceType.DAWN.name());
        tag.putInt("SpellEidolonTintRGB", DAWN_WARDEN_HEX_COLOR);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.45D, EssenceType.DAWN, EssenceType.AIR, 36, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.05D);
        level.sendParticles(ParticleTypes.CLOUD, spawnPos.x, spawnPos.y + 0.5D, spawnPos.z, 10, 0.4D, 0.2D, 0.4D, 0.03D);
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
        // Section 6.33 Solar Zephyr Blink:
        // Silently teleports up to 14 blocks away on an atmospheric thermal draft, cleansing 1 active negative debuff
        // upon arrival and granting +30% sprint speed for 5.0s (100t)!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 60L);
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.DAWN, potencyTier);

        Holder<MobEffect> negToRemove = null;
        for (MobEffectInstance inst : caster.getActiveEffects()) {
            if (!inst.getEffect().value().isBeneficial()) {
                negToRemove = inst.getEffect();
                break;
            }
        }
        if (negToRemove != null) {
            caster.removeEffect(negToRemove);
        }

        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, true));
        SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.30D, 100L);
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 16, 0.35D, 0.45D, 0.35D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.33 Daybreak Awakening Transmute:
        // Un-rots zombie villagers back to human form, converts dark blocks (blackstone, deepslate, dark oak) into
        // sunlit sandstone, terracotta, or birch variants, and instantly purifies rotting flesh into clean leather!
        boolean didAny = false;
        AABB area = new AABB(pos).inflate(2.5D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, area)) {
            var villager = zv.convertTo(EntityType.VILLAGER, ConversionParams.single(zv, false, false), v -> {});
            if (villager != null) {
                level.sendParticles(ParticleTypes.END_ROD, villager.getX(), villager.getY(0.9D), villager.getZ(),
                        22, 0.35D, 0.5D, 0.35D, 0.08D);
                level.playSound(null, villager.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 0.9F, 1.35F);
                maybeGrantDawnBatchTransmuteDiscount(level, caster, potencyTier);
                didAny = true;
            }
        }

        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        BlockState sunlit = null;
        if (state.is(Blocks.BLACKSTONE) || state.is(Blocks.GILDED_BLACKSTONE)) {
            sunlit = Blocks.SANDSTONE.defaultBlockState();
        } else if (state.is(Blocks.POLISHED_BLACKSTONE) || state.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
                || state.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)) {
            sunlit = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        } else if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.POLISHED_DEEPSLATE) || state.is(Blocks.DEEPSLATE_BRICKS)) {
            sunlit = Blocks.TERRACOTTA.defaultBlockState();
        } else if (state.is(Blocks.DARK_OAK_LOG)) {
            sunlit = Blocks.BIRCH_LOG.defaultBlockState();
        } else if (state.is(Blocks.STRIPPED_DARK_OAK_LOG)) {
            sunlit = Blocks.STRIPPED_BIRCH_LOG.defaultBlockState();
        } else if (state.is(Blocks.DARK_OAK_WOOD)) {
            sunlit = Blocks.BIRCH_WOOD.defaultBlockState();
        } else if (state.is(Blocks.DARK_OAK_PLANKS)) {
            sunlit = Blocks.BIRCH_PLANKS.defaultBlockState();
        } else if (state.is(Blocks.DARK_OAK_LEAVES)) {
            sunlit = Blocks.BIRCH_LEAVES.defaultBlockState();
        }

        if (sunlit != null) {
            level.setBlockAndUpdate(pos, sunlit);
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.03D);
            maybeGrantDawnBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        ItemStack out = ItemStack.EMPTY;
        if (stack.is(Items.ROTTEN_FLESH)) {
            out = new ItemStack(Items.LEATHER, stack.getCount());
        } else if (stack.is(Items.BLACKSTONE) || stack.is(Items.GILDED_BLACKSTONE)) {
            out = new ItemStack(Items.SANDSTONE, stack.getCount());
        } else if (stack.is(Items.POLISHED_BLACKSTONE) || stack.is(Items.POLISHED_BLACKSTONE_BRICKS)) {
            out = new ItemStack(Items.SMOOTH_SANDSTONE, stack.getCount());
        } else if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE) || stack.is(Items.POLISHED_DEEPSLATE)) {
            out = new ItemStack(Items.TERRACOTTA, stack.getCount());
        } else if (stack.is(Items.DARK_OAK_LOG)) {
            out = new ItemStack(Items.BIRCH_LOG, stack.getCount());
        } else if (stack.is(Items.STRIPPED_DARK_OAK_LOG)) {
            out = new ItemStack(Items.STRIPPED_BIRCH_LOG, stack.getCount());
        } else if (stack.is(Items.DARK_OAK_PLANKS)) {
            out = new ItemStack(Items.BIRCH_PLANKS, stack.getCount());
        } else if (stack.is(Items.DARK_OAK_LEAVES)) {
            out = new ItemStack(Items.BIRCH_LEAVES, stack.getCount());
        }

        if (!out.isEmpty()) {
            itemEntity.setItem(out);
            level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    8, 0.2D, 0.2D, 0.2D, 0.04D);
            maybeGrantDawnBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.33 Solar Blind-Sear Hex:
        // Persistent milk-immune sunrise curse (cleansed via Restoration/Vitae magic, Umbral shadow reactions, or
        // caster defeat): permanently inflicts total Blindness & Glowing (revealing target through walls), melts away
        // 3 physical armor points, and smites undead with continuous solar fire ticks!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.removeEffect(MobEffects.INVISIBILITY);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 45L);

        boolean undead = target.getType().is(EntityTypeTags.UNDEAD);
        if (undead) {
            target.igniteForSeconds(3);
        }

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = ((undead ? 2.15F : 1.35F) + potencyTier * (undead ? 0.70F : 0.45F)) * mult;
        SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.03D);
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

    private static boolean hasAnyNegativeDebuff(LivingEntity entity) {
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (!inst.getEffect().value().isBeneficial()) {
                return true;
            }
        }
        return false;
    }

    private static void maybeGrantDawnBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellDawnBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellDawnBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellDawnBatchTick", now);
        pData.putInt("SpellDawnBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.DAWN, refund);
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
