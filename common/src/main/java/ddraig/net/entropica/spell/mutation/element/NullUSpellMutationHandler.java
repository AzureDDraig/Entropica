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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
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
 * Complete per-part Signature Mutation implementation for <b>Null_U Materia (Ethereal &amp; Paradox Fusion #31)</b>
 * (Spellcrafting System Guide Section 6.32 — Dual Void {@code #41304E} + Umbral {@code #110B17} [Leans: Void]).
 */
public final class NullUSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Abyssal Pitch Black (#110B17) primary tint. */
    public static final int VOID_STALKER_OBLIVION_HEX_COLOR = 0x110B17;

    /** Canonical Cosmic Event-Horizon Violet (#41304E) secondary tint. */
    public static final int COSMIC_HORIZON_HEX_COLOR = 0x41304E;

    private static final String TAG_NULL_U_BEAM_LAST_TICK = "SpellNullUBeamLastTick";
    private static final String TAG_NULL_U_BEAM_TICKS = "SpellNullUBeamTicks";
    private static final String TAG_NULL_U_ORB_COLLAPSED = "SpellNullUOrbCollapsed";

    @Override
    public EssenceType materiaType() {
        return EssenceType.NULL_U;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Abyssal Singularity Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Black Hole Singularity Orb";
            case BEAM -> "Void-Umbral Disintegration Stream";
            case RADIAL_BURST -> "Abyssal Gravity Shockwave";
            case TOUCH_STRIKE -> "Spacetime Oblivion Palm";
            case AURA -> "Abyssal Zero-Acoustic Veil";
            case GLYPH -> "Shadow-Snare Singularity Mine";
            case CONE -> "Void-Umbral Disintegration Wave";
            case SKYFALL -> "Black Hole Oblivion Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Abyssal Event Horizon Blink";
            case TRANSMUTE, BREAK -> "Abyssal Dark Matter Transmute";
            case AFFLICTION -> "Sensory Blackout Hex";
            default -> "Abyssal Singularity " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — ABYSSAL SINGULARITY ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), VOID_STALKER_OBLIVION_HEX_COLOR, 16, 0.76F, 0.14F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), COSMIC_HORIZON_HEX_COLOR, 16, 0.68F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — BLACK HOLE SINGULARITY ORB (PROJECTILE)
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
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), COSMIC_HORIZON_HEX_COLOR, 18, 0.74F, 0.14F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.07D, 0.07D, 0.07D, 0.02D
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
        collapseBlackHoleOrbOnce(level, projectile, caster, hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.35D, 0.0D), spell);
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
        // Wall-phasing Black Hole Singularity Orbs phase cleanly through solid obstacles and collapse on entity hit or apex range!
        if (!projectile.isWallPhasing()) {
            collapseBlackHoleOrbOnce(level, projectile, caster, blockHit.getLocation(), spell);
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
        collapseBlackHoleOrbOnce(level, projectile, caster, projectile.position(), spell);
    }

    private static void collapseBlackHoleOrbOnce(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_NULL_U_ORB_COLLAPSED).orElse(false)) {
            return;
        }
        pTag.putBoolean(TAG_NULL_U_ORB_COLLAPSED, true);

        // Section 6.32 Black Hole Singularity Orb:
        // Silently collapses into a 4-block black hole that drags caught mobs inward, deals Null_U Materia damage,
        // inflicts Blindness for 3.0s (60t), and wipes mob aggro!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.add(0.0D, 0.55D, 0.0D), 4.0D, true, EssenceType.NULL_U, EssenceType.UMBRAL, 20, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(
                level, center, 4.0D, EssenceType.NULL_U, EssenceType.UMBRAL, tier
        );

        AABB box = new AABB(center.x - 4.0D, center.y - 2.5D, center.z - 4.0D,
                center.x + 4.0D, center.y + 3.5D, center.z + 4.0D);
        float collapseDmg = 3.25F + tier * 0.85F;

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = center.subtract(foe.position());
            Vec3 pull = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.08D) : Vec3.ZERO;
            foe.setDeltaMovement(pull.x, 0.24D, pull.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            wipeMobAggro(foe);
            SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(level, foe, caster, collapseDmg, false, true);
        }

        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 0.6D, center.z, 20, 1.1D, 0.5D, 1.1D, 0.04D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.6D, center.z, 24, 1.2D, 0.5D, 1.2D, 0.08D);
    }

    // =========================================================================
    // FORM MUTATION 2 — VOID-UMBRAL DISINTEGRATION STREAM (BEAM)
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
        // Section 6.32 Void-Umbral Disintegration Stream:
        // Sustained pitch-black disintegrating ray (#110B17 / #41304E); silences enemy abilities, steadily melts
        // armor (-1 armor per second channeled, up to -4), pulls caught targets toward the caster, and triggers an
        // Event Horizon Implosion after 3.0s (60 contact ticks = 15 beam slices) that pulls nearby enemies inward
        // and blinds them!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            SpellLingeringZoneManager.applyArcaneSilence(hit, 45);
            Vec3 toCaster = caster.position().subtract(hit.position());
            Vec3 horizPull = new Vec3(toCaster.x, 0.0D, toCaster.z);
            Vec3 pull = horizPull.lengthSqr() > 1.0E-4D ? horizPull.normalize().scale(0.46D) : Vec3.ZERO;
            hit.setDeltaMovement(pull.x, 0.18D, pull.z);
            hit.hasImpulse = true;

            if (!slot.hasHarmVerb()) {
                float tickDmg = (1.35F + tier * 0.38F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(level, hit, caster, tickDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_NULL_U_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_NULL_U_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_NULL_U_BEAM_LAST_TICK, now);

            double armorMelt = Math.min(4.0D, 1.0D + (contactTicks / 20));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorMelt, 0.0D, 80L);

            if (contactTicks >= 60) {
                data.putInt(TAG_NULL_U_BEAM_TICKS, 0);
                Vec3 implosionCenter = hit.position();
                SpellWorldVfxManager.broadcastVacuumSphere(
                        level, implosionCenter.add(0.0D, 0.85D, 0.0D), 4.5D, true, EssenceType.NULL_U, EssenceType.UMBRAL, 20, tier
                );
                SpellWorldVfxManager.broadcastRadialNova(
                        level, implosionCenter, 4.5D, EssenceType.NULL_U, EssenceType.UMBRAL, tier
                );

                float implosionDmg = (3.40F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
                AABB implosionBox = new AABB(
                        implosionCenter.x - 4.5D, implosionCenter.y - 2.0D, implosionCenter.z - 4.5D,
                        implosionCenter.x + 4.5D, implosionCenter.y + 3.2D, implosionCenter.z + 4.5D
                );
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, implosionBox, LivingEntity::isAlive)) {
                    if (nearby == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, nearby)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(nearby, caster, wardSafe)) continue;

                    Vec3 toImplosion = implosionCenter.subtract(nearby.position());
                    Vec3 drag = toImplosion.lengthSqr() > 1.0E-4D ? toImplosion.normalize().scale(1.12D) : Vec3.ZERO;
                    nearby.setDeltaMovement(drag.x, 0.26D, drag.z);
                    nearby.hasImpulse = true;
                    nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(level, nearby, caster, implosionDmg, false, true);
                }
                level.sendParticles(ParticleTypes.SQUID_INK,
                        implosionCenter.x, implosionCenter.y + 0.8D, implosionCenter.z, 24, 1.1D, 0.5D, 1.1D, 0.05D);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        implosionCenter.x, implosionCenter.y + 0.8D, implosionCenter.z, 28, 1.2D, 0.5D, 1.2D, 0.08D);
            } else {
                data.putInt(TAG_NULL_U_BEAM_TICKS, contactTicks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — ABYSSAL GRAVITY SHOCKWAVE (RADIAL BURST)
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
        // Section 6.32 Abyssal Gravity Shockwave:
        // Completely silent 360° dark gravity shockwave (6m+ radius, #110B17 / #41304E); pulls surrounding hostiles
        // inward before blasting them backward, deals Null_U Materia damage, inflicts Blindness for 4.0s (80t),
        // wipes mob aggro, and renders the caster invisible and silent for 4.0s (80t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVacuumSphere(
                level, center.add(0.0D, 0.85D, 0.0D), r, true, EssenceType.NULL_U, EssenceType.UMBRAL, 12, tier
        );

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 toCenter = center.subtract(foe.position());
            Vec3 inward = toCenter.lengthSqr() > 1.0E-4D ? toCenter.normalize().scale(1.00D) : Vec3.ZERO;
            foe.setDeltaMovement(inward.x, 0.22D, inward.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            wipeMobAggro(foe);
        }

        // Delayed secondary blast hurling pulled enemies backward 6 ticks later (completely silent)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 7, level.getGameTime(), spell, slot, EssenceType.NULL_U, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean zWard = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, zone.radius, EssenceType.NULL_U, EssenceType.UMBRAL, zTier
                    );
                    AABB blastBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 3.5D, zone.center.z + zone.radius
                    );
                    float waveDmg = (3.10F + zTier * 0.78F) * Math.max(0.5F, zone.apparatusPotency);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zWard)) continue;

                        Vec3 fromCenter = foe.position().subtract(zone.center);
                        Vec3 away = fromCenter.lengthSqr() > 1.0E-4D ? fromCenter.normalize().scale(1.48D) : new Vec3(0.0D, 0.40D, 0.0D);
                        foe.setDeltaMovement(away.x, 0.40D, away.z);
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        wipeMobAggro(foe);
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(srv, foe, owner, waveDmg, false, true);
                        }
                    }
                    srv.sendParticles(ParticleTypes.SQUID_INK,
                            zone.center.x, zone.center.y + 0.8D, zone.center.z, 28, 2.0D, 0.4D, 2.0D, 0.06D);
                    return false;
                }
        ));

        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, false));
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 80L);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.7D, center.z, 32, 2.0D, 0.5D, 2.0D, 0.08D);
    }

    // =========================================================================
    // FORM MUTATION 4 — SPACETIME OBLIVION PALM (TOUCH / STRIKE)
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
        // Section 6.32 Spacetime Oblivion Palm (Hybrid Gouge):
        // Deals Null_U Materia damage, bypasses 50% target armor, roots the victim's shadow for 2.5s (50t),
        // causes them to randomly warp 3 times over 6s (121t) while blinded, and siphons 25% of damage dealt
        // directly into the caster's health pool!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float totalDealt = 0.0F;

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            UmbralSpellMutationHandler.pinEntityShadowToFloor(level, hit, 50, tier);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, now + 121L);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.50D, 0.0D, 40L);

            if (!slot.hasHarmVerb()) {
                float palmDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
                totalDealt += SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(level, hit, caster, palmDmg, true, true);
            }
            level.sendParticles(ParticleTypes.SQUID_INK, hit.getX(), hit.getY(0.8D), hit.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.04D);
        }

        if (!slot.hasHarmVerb() && totalDealt > 0.0F && caster.isAlive()) {
            float heal = totalDealt * 0.25F;
            SpellCombatReactionEngine.notifyMagicalHeal(caster, heal);
            caster.heal(heal);
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — ABYSSAL ZERO-ACOUSTIC VEIL (AURA / SELF)
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
        // Section 6.32 Abyssal Zero-Acoustic Veil:
        // Envelops caster in a silent abyssal shroud (#110B17 / #41304E); zero acoustic vibrations (100% Sculk Sensor
        // & Warden immunity), cuts hostile mob detection range by 70%, grants high-float jumps and zero-g glide,
        // deflects 50% of incoming arrows into the void, and blinds and silences melee attackers for 3.0s (60t)!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL, until);
        data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, until);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(ParticleTypes.SQUID_INK, caster.getX(), caster.getY(0.9D), caster.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.03D);
    }

    // =========================================================================
    // FORM MUTATION 6 — SHADOW-SNARE SINGULARITY MINE (GLYPH / TRAP)
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
        // Section 6.32 Shadow-Snare Singularity Mine:
        // Disarmable Single/Repeating hidden floor glyph (1.0s arming delay); when tripped, erupts into a 4m black hole
        // trap that drags mobs into the center, pins them flat to the ground for 3.0s (60t), blinds them for 4.0s (80t),
        // wipes active mob aggro, and deals continuous Null_U Materia damage!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.NULL_U, EssenceType.UMBRAL, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.NULL_U, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.NULL_U, EssenceType.UMBRAL, rem, tier
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
                            srv, zone.center.add(0.0D, 0.75D, 0.0D), zone.radius, true, EssenceType.NULL_U, EssenceType.UMBRAL, 60, tier
                    );

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, zone.radius, 60, srv.getGameTime(),
                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sWard = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                AABB box = new AABB(
                                        sZone.center.x - sZone.radius, sZone.center.y - 1.5D, sZone.center.z - sZone.radius,
                                        sZone.center.x + sZone.radius, sZone.center.y + 3.2D, sZone.center.z + sZone.radius
                                );
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                                    if (v == sOwner || SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sWard)) continue;

                                    Vec3 toCenter = sZone.center.subtract(v.position());
                                    if (toCenter.horizontalDistanceSqr() > 0.36D) {
                                        Vec3 drag = toCenter.scale(0.32D);
                                        v.setDeltaMovement(drag.x, Math.min(v.getDeltaMovement().y, -0.28D), drag.z);
                                        v.hasImpulse = true;
                                    } else {
                                        UmbralSpellMutationHandler.pinEntityShadowToFloor(sLvl, v, 25, tier);
                                    }
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 6));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    wipeMobAggro(v);

                                    if (sEl % 20 == 0) {
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO,
                                                    sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F
                                            );
                                        }
                                        float snareDmg = (1.85F + tier * 0.48F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(sLvl, v, sOwner, snareDmg, false, true);
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                            sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 12, 1.2D, 0.2D, 1.2D, 0.05D);
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
    // FORM MUTATION 7 — VOID-UMBRAL DISINTEGRATION WAVE (CONE / SPRAY)
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
        // Section 6.32 Void-Umbral Disintegration Wave:
        // Wide 60° cone of pitch-black antimatter needles and light-devouring void mist; pushes enemies 6 blocks back,
        // inflicts 4.0s (80t) Blindness, wipes mob aggro, melts 3 armor points for 6.0s (120t), and extinguishes
        // torches and open fires!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.NULL_U, EssenceType.UMBRAL, tier
        );

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.48D, 0.35D, direction.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
            wipeMobAggro(hit);

            if (!slot.hasHarmVerb()) {
                float waveDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(level, hit, caster, waveDmg, false, true);
            }
        }

        int steps = Math.min(8, Math.max(2, (int) Math.ceil(coneRange)));
        for (int s = 1; s <= steps; s += 2) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            EclipseSpellMutationHandler.snuffTorchesAndFires(level, stepPos, 2);
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        level.sendParticles(ParticleTypes.SQUID_INK, mid.x, mid.y, mid.z, 22, 1.5D, 0.6D, 1.5D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 8 — BLACK HOLE OBLIVION TEMPEST (SKYFALL)
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
        // Section 6.32 Black Hole Oblivion Tempest:
        // Opens an aerial rift for 10.0s (200t) across a 14m+ radius raining dark singularity micro-orbs;
        // continuously pulls enemies toward the center, lifts them 3 blocks airborne in zero-g, blinds hostiles,
        // and slams them down for heavy fall/impact damage when the rift expires!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.NULL_U, EssenceType.UMBRAL, 200, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.NULL_U, apparatusPotency,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    AABB area = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 9.0D, zone.center.z + zone.radius
                    );

                    if (elapsed >= zone.maxDurationTicks) {
                        // Final crushing gravitational slam!
                        float slamDmg = (6.00F + zTier * 1.50F) * Math.max(0.5F, zone.apparatusPotency);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            v.removeEffect(MobEffects.SLOW_FALLING);
                            v.setDeltaMovement(v.getDeltaMovement().x, -1.65D, v.getDeltaMovement().z);
                            v.hasImpulse = true;
                            SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(srv, v, owner, slamDmg, false, true);
                        }
                        srv.sendParticles(ParticleTypes.SQUID_INK,
                                zone.center.x, zone.center.y + 0.8D, zone.center.z,
                                32, zone.radius * 0.45D, 0.5D, zone.radius * 0.45D, 0.07D);
                        return false;
                    }

                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SQUID_INK,
                                zone.center.x, zone.center.y + 4.5D, zone.center.z,
                                18, zone.radius * 0.50D, 1.3D, zone.radius * 0.50D, 0.04D);
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL,
                                zone.center.x, zone.center.y + 4.0D, zone.center.z,
                                16, zone.radius * 0.45D, 1.2D, zone.radius * 0.45D, 0.06D);

                        Vec3 hoverAnchor = zone.center.add(0.0D, 3.0D, 0.0D);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            Vec3 delta = hoverAnchor.subtract(v.position());
                            Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                            Vec3 pull = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize().scale(0.28D) : Vec3.ZERO;
                            double vy = Math.clamp(delta.y * 0.26D, -0.05D, 0.38D);
                            v.setDeltaMovement(pull.x, vy, pull.z);
                            v.resetFallDistance();
                            v.hasImpulse = true;
                            v.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));
                        }
                    }

                    if (elapsed % 20 == 0) {
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                );
                            }
                            float orbDmg = (2.10F + zTier * 0.56F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(srv, v, owner, orbDmg, false, true);
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — VOID-STALKER OBLIVION SUITE (SUMMON)
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
        tag.putString("SpellEidolonMateria", EssenceType.NULL_U.name());
        tag.putInt("SpellEidolonTintRGB", VOID_STALKER_OBLIVION_HEX_COLOR);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.45D, EssenceType.NULL_U, EssenceType.UMBRAL, 36, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.SQUID_INK, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 18, 0.4D, 0.5D, 0.4D, 0.04D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.4D, 0.5D, 0.4D, 0.06D);
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
        // Section 6.32 Abyssal Event Horizon Blink:
        // Teleports up to 16 blocks silently leaving a micro-singularity at departure that pulls pursuers inward
        // and blinds them!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 60L);
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.NULL_U, potencyTier);
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, departurePos.add(0.0D, 0.85D, 0.0D), 3.8D, true, EssenceType.NULL_U, EssenceType.UMBRAL, 50, potencyTier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 3.8D, 50, level.getGameTime(), null, null, EssenceType.NULL_U, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SQUID_INK, zone.center.x, zone.center.y + 0.8D, zone.center.z, 6, 0.6D, 0.3D, 0.6D, 0.03D);
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.8D, zone.center.z, 8, 0.7D, 0.4D, 0.7D, 0.05D);
                        AABB box = new AABB(
                                zone.center.x - 3.8D, zone.center.y - 1.5D, zone.center.z - 3.8D,
                                zone.center.x + 3.8D, zone.center.y + 2.5D, zone.center.z + 3.8D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;

                            Vec3 toWell = zone.center.subtract(foe.position());
                            Vec3 pull = toWell.lengthSqr() > 1.0E-4D ? toWell.normalize().scale(0.55D) : Vec3.ZERO;
                            foe.setDeltaMovement(pull.x, 0.14D, pull.z);
                            foe.hasImpulse = true;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                            wipeMobAggro(foe);
                            if (elapsed % 20 == 0) {
                                SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(
                                        srv, foe, owner, 1.55F + potencyTier * 0.40F, false, true
                                );
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
        // Section 6.32 Abyssal Dark Matter Transmute:
        // Dissolves cobble, stone, and ores into raw dark matter slag (Astral Impactite) and blackstone!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        String blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean isOre = blockKey.endsWith("_ore") || state.is(Blocks.ANCIENT_DEBRIS);
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) || isOre) {
            BlockState targetState = isOre
                    ? ModBlocks.ASTRAL_IMPACTITE.get().defaultBlockState()
                    : Blocks.BLACKSTONE.defaultBlockState();
            level.setBlockAndUpdate(pos, targetState);
            ItemStack bonusDrop = isOre
                    ? new ItemStack(Items.BLACKSTONE, 1 + level.random.nextInt(2))
                    : new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), 1);
            Block.popResource(level, pos.above(), bonusDrop);
            level.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.02D);
            maybeGrantNullUBatchTransmuteDiscount(level, caster, potencyTier);
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
        if (in.isEmpty()) return false;
        String itemKey = BuiltInRegistries.ITEM.getKey(in.getItem()).getPath();
        boolean isOre = itemKey.endsWith("_ore") || itemKey.startsWith("raw_");
        if (in.is(Items.STONE) || in.is(Items.COBBLESTONE) || in.is(Items.DEEPSLATE)
                || in.is(Items.COBBLED_DEEPSLATE) || in.is(Items.ANDESITE)
                || in.is(Items.DIORITE) || in.is(Items.GRANITE)) {
            itemEntity.setItem(new ItemStack(Items.BLACKSTONE, in.getCount()));
            ItemEntity slag = new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY(0.2D), itemEntity.getZ(),
                    new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), Math.max(1, (in.getCount() + 1) / 2))
            );
            level.addFreshEntity(slag);
            maybeGrantNullUBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (isOre) {
            itemEntity.setItem(new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), Math.max(1, in.getCount())));
            ItemEntity blackstone = new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY(0.2D), itemEntity.getZ(),
                    new ItemStack(Items.BLACKSTONE, Math.max(1, in.getCount()))
            );
            level.addFreshEntity(blackstone);
            maybeGrantNullUBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.32 Sensory Blackout Hex:
        // Persistent milk-immune abyssal darkness curse (cleansed via Restoration/Radiant light magic, eating mushroom
        // soup/stew, or caster defeat): permanently inflicts total Blindness & Deafness/Silence (clearing mob aggro and
        // suppressing spellcasting), cuts target movement speed by 40%, and prevents health regeneration!
        if (target instanceof Player stewPlayer && stewPlayer.isUsingItem()) {
            ItemStack useItem = stewPlayer.getUseItem();
            if (useItem.is(Items.MUSHROOM_STEW) || useItem.is(Items.SUSPICIOUS_STEW)
                    || useItem.is(Items.RABBIT_STEW) || useItem.is(Items.BEETROOT_SOUP)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(stewPlayer);
                level.sendParticles(ParticleTypes.END_ROD, stewPlayer.getX(), stewPlayer.getY(1.0D), stewPlayer.getZ(),
                        12, 0.3D, 0.4D, 0.3D, 0.04D);
                return true;
            }
        }

        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.40D, 45L);
        SpellLingeringZoneManager.applyArcaneSilence(target, 45);
        wipeMobAggro(target);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.35F + potencyTier * 0.46F) * mult;
        SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
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
            float heal = 3.1F + potencyTier * 0.92F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void wipeMobAggro(LivingEntity entity) {
        entity.setLastHurtByMob(null);
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.setAggressive(false);
        }
    }

    private static void maybeGrantNullUBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellNullUBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellNullUBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellNullUBatchTick", now);
        pData.putInt("SpellNullUBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.NULL_U, refund);
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
