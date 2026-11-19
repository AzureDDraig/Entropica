package ddraig.net.entropica.entity.projectile;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellPrefix;
import ddraig.net.entropica.spell.SpellTriggerType;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Modular ballistic spell projectile carrying a specific {@link SubSpellSlot} from a {@link SpellAssembly}.
 *
 * <p>Respects per-part {@code mutationEnabled} toggles independently:
 * Flow mutations govern flight guidance, Form mutations govern wall-phasing and target piercing,
 * and Primary / Secondary Core Effect mutations govern impact payloads and elemental reactions.</p>
 */
public class SpellProjectileEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Integer> DATA_COLOR_TINT =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACCENT_COLOR =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ESSENCE_ORDINAL =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SECONDARY_ORDINAL =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_ARCHETYPE =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_POTENCY_TIER =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VISUAL_FLAGS =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.BYTE);

    public static final byte VFLAG_MUTATED = 1;
    public static final byte VFLAG_ORBITAL = 1 << 1;
    public static final byte VFLAG_SPLIT_CHILD = 1 << 2;
    public static final byte VFLAG_WALL_PHASING = 1 << 3;
    public static final byte VFLAG_HOMING = 1 << 4;
    public static final byte VFLAG_HEAVY_FLUID = 1 << 5;

    private final java.util.ArrayDeque<Vec3> clientTrailPoints = new java.util.ArrayDeque<>(32);

    private SpellAssembly spell = SpellAssembly.createMagicMissileStarter(1);
    private int slotIndex = 0;
    @Nullable
    private EssenceType fallbackMateria = EssenceType.REGULAR;
    private float apparatusPotencyMult = 1.0F;
    private int remainingPierces = 0;
    private int remainingBounces = 0;
    private boolean wallPhasing = false;
    private boolean homingGuidance = false;
    private boolean splitChild = false;
    private boolean flowSplitTriggered = false;
    private boolean sporeOrbitalActive = false;
    private boolean slightFluidArc = false;
    private double customFluidGravity = 0.0115D;
    private float orbitPhaseRadians = 0.0F;
    @Nullable
    private EssenceType sentryBoltMateria = null;
    private int maxFlightTicks = 60;
    private boolean carrierPayload = false;
    private float apparatusRangeMult = 1.0F;
    private boolean originInitialized = false;
    private double originX = 0.0D;
    private double originY = 0.0D;
    private double originZ = 0.0D;
    private double maxTravelDistance = 16.0D;
    private double cruiseSpeed = 0.0D;
    @Nullable
    private UUID assignedTargetUuid = null;
    private boolean anchoredForDelayedBloom = false;
    @Nullable
    private net.minecraft.core.BlockPos anchoredBlockPos = null;
    private net.minecraft.core.Direction anchoredFace = net.minecraft.core.Direction.UP;
    private int delayedFuseTicks = 0;
    private boolean dynamicFeeCharged = false;
    private final Set<UUID> alreadyHitEntities = new HashSet<>();

    public SpellProjectileEntity(EntityType<? extends SpellProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public SpellProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.SPELL_PROJECTILE.get(), shooter, level, new ItemStack(ModItems.SPELL_GEM.get()));
        this.setNoGravity(true);
    }

    public void configurePayload(
            SpellAssembly spell,
            int slotIndex,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotencyMult
    ) {
        this.spell = spell;
        this.slotIndex = Math.clamp(slotIndex, 0, spell.subSpells().size() - 1);
        this.fallbackMateria = fallbackMateria;
        this.apparatusPotencyMult = apparatusPotencyMult;

        SubSpellSlot slot = spell.subSpells().get(this.slotIndex);
        EssenceType formMateria = slot.formPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        SpellMutationProfile formMutation = slot.formPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);

        SpellMutationProfile flowMutation = spell.flowPart().resolveMutation(spell.materiaSpecific(), fallbackMateria);
        EssenceType flowMateria = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);

        this.remainingPierces = formMutation.mutated() ? formMutation.bonusPierceTargets() : 0;
        this.remainingBounces = (formMutation.mutated()
                && formMateria == EssenceType.PRISMATIC
                && slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                && !this.splitChild) ? 6 : 0;
        boolean voidFlowPhase = spell.flowPart().isVoidWarpAuthorized(spell.materiaSpecific(), fallbackMateria);
        this.wallPhasing = ((formMutation.mutated() && formMutation.wallPhasing()) || voidFlowPhase)
                && spell.prefix() != SpellPrefix.UTILITY;
        this.homingGuidance = (flowMutation.mutated() && (flowMateria == EssenceType.ASTRAL
                || flowMateria == EssenceType.ECLIPSE
                || flowMateria == EssenceType.CELESTIAL
                || flowMateria == EssenceType.WRAITH
                || flowMateria == EssenceType.APOTHEOSIS
                || flowMateria == EssenceType.ENTROPICA
                || (flowMateria == EssenceType.VOID
                    && (spell.flowPart().flow() == SpellFlowModifier.DOUBLE_BIFURCATE
                        || spell.flowPart().flow() == SpellFlowModifier.TRIPLE_TRIFURCATE))))
                || (formMutation.mutated() && (formMateria == EssenceType.ECLIPSE
                || formMateria == EssenceType.SOULFIRE
                || formMateria == EssenceType.WRAITH
                || formMateria == EssenceType.APOTHEOSIS));

        if (!this.splitChild && spell.flowPart().flow() == SpellFlowModifier.ORBIT_SATELLITE) {
            this.sporeOrbitalActive = true;
            this.orbitPhaseRadians = (float) ((this.getId() % 8) * (Math.PI * 0.25D));
        }
        recalculateTravelDistanceAndLifetime();

        EssenceType primaryEffType = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        EssenceType tintType = formMateria != EssenceType.REGULAR ? formMateria : primaryEffType;
        EssenceType secondaryType = slot.secondaryEffect()
                .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                .orElse(primaryEffType != tintType ? primaryEffType : null);

        boolean fastLance = spell.flowPart().flow() == SpellFlowModifier.FAST_DART
                || (formMutation.mutated() && formMutation.velocityMultiplier() >= 1.35F);
        boolean prismaticDisc = formMutation.mutated() && formMateria == EssenceType.PRISMATIC;
        boolean hasHarmEffect = slot.primaryEffect().effect() == ddraig.net.entropica.spell.SpellCoreEffect.HARM
                || slot.secondaryEffect().map(s -> s.effect() == ddraig.net.entropica.spell.SpellCoreEffect.HARM).orElse(false);
        byte archetype = (!prismaticDisc && hasHarmEffect && (tintType == EssenceType.AIR || primaryEffType == EssenceType.AIR))
                ? ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_WIND_SLASH
                : ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(tintType, fastLance, prismaticDisc);

        long gt = this.level() != null ? this.level().getGameTime() : 0L;
        this.entityData.set(DATA_COLOR_TINT, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantPrimaryRgb(tintType, gt));
        this.entityData.set(DATA_ACCENT_COLOR, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantAccentRgb(tintType, secondaryType, gt));
        this.entityData.set(DATA_ESSENCE_ORDINAL, tintType.ordinal());
        this.entityData.set(DATA_SECONDARY_ORDINAL, secondaryType != null ? secondaryType.ordinal() : -1);
        this.entityData.set(DATA_ARCHETYPE, archetype);
        this.entityData.set(DATA_POTENCY_TIER, (byte) Math.clamp(spell.getEffectiveTier(), 1, 10));
        if (slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                && slot.formPart().mutationEnabled()
                && (formMateria == EssenceType.RADIANT
                    || formMateria == EssenceType.DAWN
                    || formMateria == EssenceType.AURORA
                    || formMateria == EssenceType.NULL_R
                    || formMateria == EssenceType.NULL_U
                    || formMateria == EssenceType.WRAITH
                    || (formMateria == EssenceType.FROZEN && spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY))) {
            this.setNoGravity(true);
        } else if (spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && spell.flowPart().mutationEnabled()
                && (flowMateria == EssenceType.FROZEN || flowMateria == EssenceType.GLACIAL)) {
            enableSlightFluidArc(true);
        } else if (slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                && slot.formPart().mutationEnabled()
                && formMateria == EssenceType.GLACIAL) {
            enableSlightFluidArc(false);
        }
        syncVisualFlags(formMutation.mutated() || flowMutation.mutated() || slot.primaryEffect().mutationEnabled());
    }

    private void syncVisualFlags(boolean anyMutated) {
        byte flags = 0;
        if (anyMutated) flags |= VFLAG_MUTATED;
        if (this.sporeOrbitalActive) flags |= VFLAG_ORBITAL;
        if (this.splitChild) flags |= VFLAG_SPLIT_CHILD;
        if (this.wallPhasing) flags |= VFLAG_WALL_PHASING;
        if (this.homingGuidance || (anyMutated && getVisualEssenceType() == EssenceType.RADIANT)) flags |= VFLAG_HOMING;
        EssenceType effFlowMateria = this.spell.flowPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        if (this.spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                && (getVisualEssenceType() == EssenceType.WATER
                || effFlowMateria == EssenceType.WATER
                || (this.spell.flowPart().mutationEnabled()
                && (effFlowMateria == EssenceType.EARTH
                || effFlowMateria == EssenceType.FROZEN
                || effFlowMateria == EssenceType.GLACIAL
                || effFlowMateria == EssenceType.UNDEAD)))) {
            flags |= VFLAG_HEAVY_FLUID;
        }
        this.entityData.set(DATA_VISUAL_FLAGS, flags);
    }

    /**
     * Enables a smooth, calibrated slight gravity arc for pressurized fluid orbs
     * (Section 6.3 <i>Hydro-Cannon Slug</i>, <i>Viscous Fluid Mass</i>, and <i>Aqueous Sentry</i> soaking orbs).
     * Uses a gentle parabolic drop instead of vanilla's steep {@code 0.03} mortar drop, plus a subtle launch loft
     * so aimed shots arc gracefully onto crosshair targets at medium range.
     */
    public void enableSlightFluidArc(boolean heavySlowMass) {
        if (this.sporeOrbitalActive || this.spell.flowPart().trigger() == SpellTriggerType.ON_PROXIMITY) {
            return;
        }
        this.slightFluidArc = true;
        this.customFluidGravity = heavySlowMass ? 0.0068D : 0.0115D;
        this.setNoGravity(false);
        Vec3 curVel = this.getDeltaMovement();
        if (curVel.lengthSqr() > 1.0E-4D && curVel.normalize().y > -0.78D) {
            double loft = heavySlowMass ? 0.052D : 0.068D;
            this.setDeltaMovement(curVel.x, curVel.y + loft, curVel.z);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return this.slightFluidArc ? this.customFluidGravity : super.getDefaultGravity();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOR_TINT, 0x62E8FF);
        builder.define(DATA_ACCENT_COLOR, 0xDF78FF);
        builder.define(DATA_ESSENCE_ORDINAL, EssenceType.REGULAR.ordinal());
        builder.define(DATA_SECONDARY_ORDINAL, -1);
        builder.define(DATA_ARCHETYPE, ddraig.net.entropica.spell.vfx.SpellParticleHelper.ARCHETYPE_ARCANE_BOLT);
        builder.define(DATA_POTENCY_TIER, (byte) 1);
        builder.define(DATA_VISUAL_FLAGS, (byte) 0);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SPELL_GEM.get();
    }

    public int getColorTint() {
        return this.entityData.get(DATA_COLOR_TINT);
    }

    public int getAccentColor() {
        return this.entityData.get(DATA_ACCENT_COLOR);
    }

    public EssenceType getVisualEssenceType() {
        return ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(this.entityData.get(DATA_ESSENCE_ORDINAL));
    }

    @Nullable
    public EssenceType getVisualSecondaryEssenceType() {
        int ord = this.entityData.get(DATA_SECONDARY_ORDINAL);
        return ord >= 0 ? ddraig.net.entropica.spell.vfx.SpellParticleHelper.fromOrdinal(ord) : null;
    }

    public byte getVisualArchetype() {
        return this.entityData.get(DATA_ARCHETYPE);
    }

    public int getVisualPotencyTier() {
        return Math.clamp(this.entityData.get(DATA_POTENCY_TIER), 1, 10);
    }

    public byte getVisualFlags() {
        return this.entityData.get(DATA_VISUAL_FLAGS);
    }

    public java.util.ArrayDeque<Vec3> getClientTrailPoints() {
        return this.clientTrailPoints;
    }

    public void setMaxFlightTicks(int ticks) {
        this.maxFlightTicks = Math.max(5, ticks);
    }

    public void setApparatusRangeMult(float apparatusRangeMult) {
        this.apparatusRangeMult = Math.max(0.25F, apparatusRangeMult);
        recalculateTravelDistanceAndLifetime();
    }

    private void recalculateTravelDistanceAndLifetime() {
        SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
        SpellMutationProfile formMutation = slot.formPart().resolveMutation(this.spell.materiaSpecific(), this.fallbackMateria);
        float formRangeMult = formMutation.mutated() ? formMutation.rangeMultiplier() : 1.0F;
        double baseMeters = this.spell.flowPart().travelDistanceMeters();
        if (this.splitChild) {
            this.maxTravelDistance = Math.max(4.0D, Math.min(24.0D, Math.max(8.0D, baseMeters * 0.60D)) * this.apparatusRangeMult * formRangeMult);
            this.maxFlightTicks = Math.max(22, (int) Math.ceil(this.maxTravelDistance / 0.85D) + 12);
            return;
        }
        this.maxTravelDistance = Math.max(0.5D, baseMeters
                * this.apparatusRangeMult
                * formRangeMult
                * this.spell.flawProfile().getRangeAndRadiusMultiplier());
        double minExpectedSpeed = (this.spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                || this.spell.flowPart().trigger() == SpellTriggerType.ON_PROXIMITY)
                ? 0.55D
                : 0.95D;
        int travelTicks = (int) Math.ceil(this.maxTravelDistance / minExpectedSpeed) + 25;
        this.maxFlightTicks = Math.max(18, travelTicks);
        if (this.sporeOrbitalActive) {
            this.maxFlightTicks += 45;
        }
        if (this.delayedFuseTicks > 0) {
            this.maxFlightTicks = Math.max(this.maxFlightTicks, this.delayedFuseTicks + 10);
        }
    }

    public void setWallPhasing(boolean wallPhasing) {
        this.wallPhasing = wallPhasing;
    }

    public boolean isWallPhasing() {
        return this.wallPhasing;
    }

    public void setHomingGuidance(boolean homingGuidance) {
        this.homingGuidance = homingGuidance;
    }

    public void setOrbitPhaseRadians(float phaseRadians) {
        this.orbitPhaseRadians = phaseRadians;
    }

    /**
     * Marks this projectile as a Flow Carrier Payload that executes all active Multi-Cast sub-spells
     * ({@code Single 1x} through {@code Octuple 8x}) at its point of contact.
     */
    public void configureCarrierPayload(@Nullable UUID assignedTarget, float apparatusRangeMult) {
        this.carrierPayload = true;
        this.apparatusRangeMult = apparatusRangeMult;
        this.assignedTargetUuid = assignedTarget;
        if (assignedTarget != null) {
            this.homingGuidance = true;
        }
        SpellFlowModifier flow = this.spell.flowPart().flow();
        SpellTriggerType trigger = this.spell.flowPart().trigger();
        SubSpellSlot leadSlot = this.spell.subSpells().getFirst();
        EssenceType leadFormMat = leadSlot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        boolean waterHydroProjectile = leadSlot.formPart().form() == SpellDeliveryForm.PROJECTILE
                && leadSlot.formPart().mutationEnabled()
                && leadFormMat == EssenceType.WATER;
        boolean radiantDawnStarProjectile = leadSlot.formPart().form() == SpellDeliveryForm.PROJECTILE
                && leadSlot.formPart().mutationEnabled()
                && (leadFormMat == EssenceType.RADIANT || leadFormMat == EssenceType.DAWN || leadFormMat == EssenceType.AURORA
                    || leadFormMat == EssenceType.NULL_R || leadFormMat == EssenceType.NULL_U);
        if (flow == SpellFlowModifier.FAST_DART
                || radiantDawnStarProjectile
                || (flow == SpellFlowModifier.SLOW_HEAVY && !waterHydroProjectile)
                || trigger == SpellTriggerType.ON_PROXIMITY) {
            this.setNoGravity(true);
        }
        if (trigger == SpellTriggerType.ON_TIMER) {
            float sec = this.spell.flowPart().delaySeconds() >= 0.5F ? this.spell.flowPart().delaySeconds() : 1.5F;
            this.delayedFuseTicks = Math.clamp(Math.round(sec * 20.0F), 10, 80);
        }
        recalculateTravelDistanceAndLifetime();
    }

    private boolean consumeCarrierDynamicTriggerFee(ServerLevel level, @Nullable LivingEntity caster) {
        if (this.dynamicFeeCharged) {
            return true;
        }
        this.dynamicFeeCharged = true;
        return SpellExecutionEngine.consumeDynamicTriggerFeeForPayload(level, caster, this.spell, this.fallbackMateria);
    }

    private void triggerCarrierContactAndDiscard(
            ServerLevel serverLevel,
            @Nullable LivingEntity caster,
            Vec3 contactPos,
            @Nullable Entity directHit,
            @Nullable net.minecraft.core.BlockPos hitBlockPos,
            net.minecraft.core.Direction hitFace,
            Vec3 travelDir
    ) {
        triggerCarrierContactAndDiscard(
                serverLevel, caster, contactPos, directHit, hitBlockPos, hitFace, travelDir, this.apparatusPotencyMult
        );
    }

    private void triggerCarrierContactAndDiscard(
            ServerLevel serverLevel,
            @Nullable LivingEntity caster,
            Vec3 contactPos,
            @Nullable Entity directHit,
            @Nullable net.minecraft.core.BlockPos hitBlockPos,
            net.minecraft.core.Direction hitFace,
            Vec3 travelDir,
            float potencyOverride
    ) {
        if (!consumeCarrierDynamicTriggerFee(serverLevel, caster)) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, contactPos.x, contactPos.y, contactPos.z, 8, 0.2D, 0.2D, 0.2D, 0.03D);
            this.discard();
            return;
        }
        SubSpellSlot firstSlot = this.spell.subSpells().getFirst();
        SpellExecutionEngine.executeContactMultiCast(
                serverLevel,
                caster,
                this,
                contactPos,
                directHit,
                hitBlockPos,
                hitFace,
                travelDir,
                this.spell,
                this.fallbackMateria,
                potencyOverride,
                this.apparatusRangeMult
        );
        tryTriggerFlowSplit(serverLevel, caster, contactPos, firstSlot);
        this.discard();
    }

    public void addBonusPierces(int extraPierces) {
        this.remainingPierces += Math.max(0, extraPierces);
    }

    public int getPierceRemaining() {
        return this.remainingPierces;
    }

    public int getPierceCountRemaining() {
        return this.remainingPierces;
    }

    /**
     * Returns the 1-based ordinal of how many distinct entities this projectile has struck so far
     * ({@code 1} on first target, {@code 2} on second target, etc.).
     */
    public int getHitEntityCount() {
        return this.alreadyHitEntities.size();
    }

    public boolean isSplitChild() {
        return this.splitChild;
    }

    public boolean isSilentStealthProjectile() {
        if (this.sentryBoltMateria == EssenceType.UMBRAL || this.sentryBoltMateria == EssenceType.NULL_U) {
            return true;
        }
        if (this.getVisualEssenceType() == EssenceType.UMBRAL || this.getVisualEssenceType() == EssenceType.NULL_U) {
            return true;
        }
        return SpellExecutionEngine.isSilentStealthSpellPublic(this.spell, this.fallbackMateria);
    }

    public void setSplitChild(boolean splitChild) {
        this.splitChild = splitChild;
        if (splitChild) {
            this.sporeOrbitalActive = false;
        }
        byte flags = this.entityData.get(DATA_VISUAL_FLAGS);
        if (splitChild) {
            flags = (byte) ((flags | VFLAG_SPLIT_CHILD) & ~VFLAG_ORBITAL);
        } else {
            flags = (byte) (flags & ~VFLAG_SPLIT_CHILD);
        }
        this.entityData.set(DATA_VISUAL_FLAGS, flags);
    }

    /**
     * Releases up to {@code maxCount} active orbiting {@link SpellProjectileEntity} satellites owned by {@code caster}
     * outward directly toward {@code target} (used by Dawn melee strikes and Aegis / Null_R / Null_U melee retaliation).
     */
    public static void releaseOrbitingProjectilesToward(ServerLevel level, LivingEntity caster, LivingEntity target, int maxCount) {
        if (!target.isAlive() || caster == target) {
            return;
        }
        int released = 0;
        for (SpellProjectileEntity proj : level.getEntitiesOfClass(SpellProjectileEntity.class, caster.getBoundingBox().inflate(5.0D))) {
            if (!proj.isAlive() || proj.getOwner() != caster || !proj.sporeOrbitalActive) continue;
            proj.sporeOrbitalActive = false;
            proj.homingGuidance = true;
            proj.originX = proj.getX();
            proj.originY = proj.getY();
            proj.originZ = proj.getZ();
            proj.originInitialized = true;
            proj.cruiseSpeed = 1.45D;
            proj.maxFlightTicks = Math.max(proj.tickCount + (int) Math.ceil(proj.maxTravelDistance / 0.95D) + 25, proj.maxFlightTicks);
            Vec3 aim = target.getEyePosition().subtract(proj.position());
            if (aim.lengthSqr() > 1.0E-4D) {
                Vec3 dir = aim.normalize();
                proj.shoot(dir.x, dir.y, dir.z, 1.45F, 0.0F);
            }
            if (++released >= maxCount) {
                break;
            }
        }
    }

    public void setSentryBoltMateria(@Nullable EssenceType sentryBoltMateria) {
        this.sentryBoltMateria = sentryBoltMateria;
        if (sentryBoltMateria != null) {
            long gt = this.level() != null ? this.level().getGameTime() : 0L;
            this.entityData.set(DATA_ESSENCE_ORDINAL, sentryBoltMateria.ordinal());
            this.entityData.set(DATA_COLOR_TINT, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantPrimaryRgb(sentryBoltMateria, gt));
            this.entityData.set(DATA_ACCENT_COLOR, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantAccentRgb(sentryBoltMateria, null, gt));
            this.entityData.set(DATA_ARCHETYPE, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(
                    sentryBoltMateria, sentryBoltMateria == EssenceType.NULL_R || sentryBoltMateria == EssenceType.NULL_U, false));
            if (sentryBoltMateria == EssenceType.FROZEN
                    || sentryBoltMateria == EssenceType.GLACIAL
                    || sentryBoltMateria == EssenceType.OVERGROWTH
                    || sentryBoltMateria == EssenceType.NULL_R
                    || sentryBoltMateria == EssenceType.NULL_U
                    || sentryBoltMateria == EssenceType.DAWN
                    || sentryBoltMateria == EssenceType.AURORA) {
                this.setNoGravity(true);
            }
        }
    }

    /**
     * Convenience overload that rotates this projectile's current travel direction by {@code yawOffsetDegrees}
     * with a slight upward arc so secondary blossoms/shards scatter cleanly without immediately hitting the floor.
     */
    public SpellProjectileEntity spawnSplitChild(
            ServerLevel level,
            Vec3 origin,
            float yawOffsetDegrees,
            boolean homing,
            float potencyFraction,
            float speed
    ) {
        LivingEntity caster = this.getOwner() instanceof LivingEntity l ? l : null;
        Vec3 baseDir = this.getDeltaMovement().lengthSqr() > 0.001D
                ? this.getDeltaMovement().normalize()
                : (caster != null ? caster.getLookAngle().normalize() : new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 rotated = baseDir.yRot((float) Math.toRadians(yawOffsetDegrees));
        Vec3 dir = new Vec3(rotated.x, Math.max(rotated.y, 0.08D), rotated.z).normalize();
        return spawnSplitChild(level, caster, origin, dir, speed, potencyFraction, homing);
    }

    /**
     * Spawns a secondary split shard/blossom/mitosis orb from this projectile's current position,
     * marked {@code splitChild = true} so it cannot recursively split infinitely.
     */
    public SpellProjectileEntity spawnSplitChild(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin,
            Vec3 direction,
            float speed,
            float potencyFraction,
            boolean homing
    ) {
        SpellProjectileEntity child = caster != null
                ? new SpellProjectileEntity(level, caster)
                : new SpellProjectileEntity(ModEntityTypes.SPELL_PROJECTILE.get(), level);
        child.setPos(origin.x + direction.x * 0.35D, origin.y + direction.y * 0.35D, origin.z + direction.z * 0.35D);
        child.configurePayload(this.spell, this.slotIndex, this.fallbackMateria, this.apparatusPotencyMult * potencyFraction);
        child.setSplitChild(true);
        child.setMaxFlightTicks(28);
        if (homing) {
            child.setHomingGuidance(true);
        }
        child.alreadyHitEntities.addAll(this.alreadyHitEntities);
        child.shoot(direction.x, direction.y, direction.z, speed, 0.0F);
        if (this.slightFluidArc) {
            child.enableSlightFluidArc(false);
        }
        level.addFreshEntity(child);
        return child;
    }

    @Override
    public void tick() {
        if (this.wallPhasing) {
            this.noPhysics = false;
        }
        super.tick();

        // Pressurized Water & Storm Materia orbs/slugs glide effortlessly underwater without suffering vanilla 0.80 water drag!
        if (this.isInWater()
                && !this.sporeOrbitalActive
                && !this.anchoredForDelayedBloom
                && (this.getVisualEssenceType() == EssenceType.WATER
                || this.sentryBoltMateria == EssenceType.WATER
                || this.getVisualEssenceType() == EssenceType.STORM
                || this.sentryBoltMateria == EssenceType.STORM)) {
            Vec3 wVel = this.getDeltaMovement();
            double len = wVel.length();
            double cap = this.cruiseSpeed > 0.05D ? this.cruiseSpeed * 1.04D : 2.15D;
            if (len > 1.0E-4D && len < cap) {
                double boosted = Math.min(cap, len * (0.99D / 0.80D));
                this.setDeltaMovement(wVel.scale(boosted / len));
            }
        }

        if (this.level().isClientSide()) {
            Vec3 curPos = this.position();
            if (this.clientTrailPoints.isEmpty() || this.clientTrailPoints.peekFirst().distanceToSqr(curPos) > 0.0025D) {
                this.clientTrailPoints.addFirst(curPos);
                while (this.clientTrailPoints.size() > 28) {
                    this.clientTrailPoints.removeLast();
                }
            }
            byte vFlags = this.getVisualFlags();
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnClientProjectileTrail(
                    this.level(),
                    curPos,
                    this.getDeltaMovement(),
                    this.getVisualEssenceType(),
                    this.getVisualSecondaryEssenceType(),
                    this.tickCount,
                    this.getVisualPotencyTier(),
                    (vFlags & VFLAG_ORBITAL) != 0,
                    (vFlags & VFLAG_SPLIT_CHILD) != 0
            );
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            LivingEntity caster = this.getOwner() instanceof LivingEntity l ? l : null;

            if (!this.originInitialized) {
                this.originX = this.getX();
                this.originY = this.getY();
                this.originZ = this.getZ();
                this.originInitialized = true;
            }
            if (this.cruiseSpeed <= 0.001D && !this.sporeOrbitalActive && !this.anchoredForDelayedBloom) {
                this.cruiseSpeed = this.getDeltaMovement().length();
            }
            if (this.isNoGravity() && !this.sporeOrbitalActive && !this.anchoredForDelayedBloom && !this.isInWater() && this.cruiseSpeed > 0.05D) {
                Vec3 vel = this.getDeltaMovement();
                double curLen = vel.length();
                if (curLen > 1.0E-4D && curLen < this.cruiseSpeed * 0.995D) {
                    this.setDeltaMovement(vel.scale(this.cruiseSpeed / curLen));
                }
            }

            // Section 3.5 Anchored Timed Delay Fuse: hold position on struck block until fuse pops, then unleash Multi-Cast!
            if (this.anchoredForDelayedBloom) {
                this.setDeltaMovement(Vec3.ZERO);
                if (this.tickCount % 4 == 0 && !isSilentStealthProjectile()) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.1D, this.getZ(),
                            3, 0.15D, 0.15D, 0.15D, 0.02D);
                }
                if (this.tickCount >= this.delayedFuseTicks) {
                    if (!isSilentStealthProjectile()) {
                        serverLevel.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_CLUSTER_BREAK,
                                net.minecraft.sounds.SoundSource.PLAYERS, 0.9F, 1.3F);
                    }
                    triggerCarrierContactAndDiscard(
                            serverLevel, caster, this.position(), null,
                            this.anchoredBlockPos != null ? this.anchoredBlockPos : this.blockPosition(),
                            this.anchoredFace, new Vec3(0.0D, 1.0D, 0.0D)
                    );
                }
                return;
            }

            if (this.sporeOrbitalActive) {
                tickSporeOrbitalSpiral(serverLevel);
            }

            // Section 3.5 Mid-Air Timed Delay Fuse: split into Fork/Dome children or detonate in flight when fuse reaches zero!
            if (this.carrierPayload && !this.sporeOrbitalActive && this.delayedFuseTicks > 0 && this.tickCount >= this.delayedFuseTicks) {
                Vec3 dir = this.getDeltaMovement().lengthSqr() > 1.0E-4D ? this.getDeltaMovement().normalize() : new Vec3(0.0D, 1.0D, 0.0D);
                if (!isSilentStealthProjectile()) {
                    serverLevel.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_CLUSTER_BREAK,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.9F, 1.3F);
                }
                SpellFlowModifier flow = this.spell.flowPart().flow();
                if (!this.splitChild && (flow == SpellFlowModifier.DOUBLE_BIFURCATE
                        || flow == SpellFlowModifier.TRIPLE_TRIFURCATE
                        || flow == SpellFlowModifier.OCTUPLE_DOME)) {
                    SpellExecutionEngine.burstTimedMidAirFlowSplit(
                            serverLevel, caster, this.position(), dir,
                            this.spell, this.fallbackMateria, this.apparatusPotencyMult, this.apparatusRangeMult
                    );
                    this.discard();
                    return;
                }
                triggerCarrierContactAndDiscard(
                        serverLevel, caster, this.position(), null, this.blockPosition(), net.minecraft.core.Direction.UP, dir
                );
                return;
            }

            // Section 3.5 Proximity Trigger: 3-block spherical hostile detection bubble!
            if (this.carrierPayload
                    && !this.sporeOrbitalActive
                    && this.spell.flowPart().trigger() == SpellTriggerType.ON_PROXIMITY
                    && this.tickCount % 2 == 0) {
                List<LivingEntity> nearbyHostiles = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox().inflate(3.0D),
                        e -> e.isAlive()
                                && e != caster
                                && (e instanceof Monster || (caster != null && e == caster.getLastHurtMob()))
                                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                        e, caster, this.spell.hasWardFriendlyFireSafeguard()))
                );
                if (!nearbyHostiles.isEmpty()) {
                    LivingEntity proxHit = nearbyHostiles.getFirst();
                    Vec3 toFoe = proxHit.position().add(0.0D, proxHit.getBbHeight() * 0.5D, 0.0D).subtract(this.position());
                    Vec3 dir = toFoe.lengthSqr() > 1.0E-4D ? toFoe.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
                    if (!isSilentStealthProjectile()) {
                        serverLevel.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                                net.minecraft.sounds.SoundSource.PLAYERS, 0.95F, 1.45F);
                    }
                    triggerCarrierContactAndDiscard(
                            serverLevel, caster, this.position(), proxHit, this.blockPosition(), net.minecraft.core.Direction.UP, dir
                    );
                    return;
                }
            }

            SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
            EssenceType flowMateria = this.spell.flowPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
            MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, this.spell.flowPart().mutationEnabled());
            if (flowHandler != null) {
                flowHandler.onProjectileTickFromFlow(serverLevel, this, this.spell.flowPart().flow());
            }

            EssenceType formMateria = slot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
            MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
            if (formHandler != null) {
                formHandler.onProjectileTickFromForm(serverLevel, this, slot);
            }

            // Section 3.8 Organic Elemental Velocity Bias — Frozen / Glacial: leaves a chilling wake that slows nearby projectiles!
            EssenceType visMat = this.getVisualEssenceType();
            if (this.tickCount % 2 == 0
                    && (MateriaFamilyTier.isFrozenLineage(visMat)
                        || MateriaFamilyTier.isFrozenLineage(flowMateria)
                        || MateriaFamilyTier.isFrozenLineage(formMateria))) {
                for (net.minecraft.world.entity.projectile.Projectile otherProj : serverLevel.getEntitiesOfClass(
                        net.minecraft.world.entity.projectile.Projectile.class,
                        this.getBoundingBox().inflate(2.75D),
                        p -> p != this && p.isAlive())) {
                    if (this.getOwner() != null && otherProj.getOwner() == this.getOwner()) continue;
                    Vec3 pVel = otherProj.getDeltaMovement();
                    if (pVel.lengthSqr() > 1.0E-4D) {
                        otherProj.setDeltaMovement(pVel.scale(0.62D));
                        otherProj.hasImpulse = true;
                        serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                                otherProj.getX(), otherProj.getY(), otherProj.getZ(), 2, 0.12D, 0.12D, 0.12D, 0.01D);
                    }
                }
            }

            // Section 6.6 Glacial Ice-Shard, Section 6.16 Glacial Ice-Lance & Permafrost Creep water-surface interaction:
            // Permafrost Creep (SLOW_HEAVY) glides steadily across water while permanently freezing it into Blue Ice;
            // Glacial Ice-Shard / Ice-Lance / Icicle Sentry darts flash-freeze hit water blocks into Packed Ice!
            if (this.isInWater() && !this.sporeOrbitalActive) {
                boolean permafrostCreep = this.spell.flowPart().flow() == SpellFlowModifier.SLOW_HEAVY
                        && this.spell.flowPart().mutationEnabled()
                        && (flowMateria == EssenceType.FROZEN || flowMateria == EssenceType.GLACIAL);
                if (permafrostCreep) {
                    Vec3 cur = this.getDeltaMovement();
                    if (cur.lengthSqr() > 1.0E-4D && this.cruiseSpeed > 0.05D && cur.length() < this.cruiseSpeed * 0.92D) {
                        this.setDeltaMovement(cur.normalize().scale(this.cruiseSpeed * 0.95D));
                        this.hasImpulse = true;
                    }
                    BlockPos cPos = this.blockPosition();
                    for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-1, -1, -1), cPos.offset(1, -1, 1))) {
                        if (serverLevel.getBlockState(p).is(Blocks.WATER)) {
                            serverLevel.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                        }
                    }
                } else if (slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                        && slot.formPart().mutationEnabled()
                        && formMateria == EssenceType.GLACIAL) {
                    BlockPos waterHitPos = this.blockPosition();
                    boolean entityInWaterAhead = !serverLevel.getEntitiesOfClass(
                            LivingEntity.class,
                            this.getBoundingBox().inflate(1.75D),
                            e -> e.isAlive() && e != this.getOwner() && !this.alreadyHitEntities.contains(e.getUUID())
                    ).isEmpty();
                    if (entityInWaterAhead) {
                        for (BlockPos p : BlockPos.betweenClosed(waterHitPos.offset(-1, -1, -1), waterHitPos.offset(1, -1, 1))) {
                            if (serverLevel.getBlockState(p).is(Blocks.WATER)) {
                                serverLevel.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                            }
                        }
                    } else {
                        for (BlockPos p : BlockPos.betweenClosed(waterHitPos.offset(-2, -1, -2), waterHitPos.offset(2, 0, 2))) {
                            if (p.distSqr(waterHitPos) <= 5.0D && serverLevel.getBlockState(p).is(Blocks.WATER)) {
                                serverLevel.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                            }
                        }
                        this.onHitBlock(new BlockHitResult(this.position(), net.minecraft.core.Direction.UP, waterHitPos, false));
                        if (!this.isRemoved()) {
                            this.discard();
                        }
                        return;
                    }
                } else if ((slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                        && slot.formPart().mutationEnabled()
                        && formMateria == EssenceType.FROZEN)
                        || this.sentryBoltMateria == EssenceType.FROZEN
                        || this.sentryBoltMateria == EssenceType.GLACIAL) {
                    BlockPos waterHitPos = this.blockPosition();
                    for (BlockPos p : BlockPos.betweenClosed(waterHitPos.offset(-2, -1, -2), waterHitPos.offset(2, 0, 2))) {
                        if (p.distSqr(waterHitPos) <= 5.0D && serverLevel.getBlockState(p).is(Blocks.WATER)) {
                            serverLevel.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                        }
                    }
                    this.onHitBlock(new BlockHitResult(this.position(), net.minecraft.core.Direction.UP, waterHitPos, false));
                    if (!this.isRemoved()) {
                        this.discard();
                    }
                    return;
                } else if ((slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                        && slot.formPart().mutationEnabled()
                        && formMateria == EssenceType.NETHER)
                        || this.sentryBoltMateria == EssenceType.NETHER) {
                    // Section 6.7 Netherfire Comet & Infernal Magma Mortar: flashes surface water into scalding steam on impact!
                    BlockPos boilPos = this.blockPosition();
                    for (BlockPos p : BlockPos.betweenClosed(boilPos.offset(-1, -1, -1), boilPos.offset(1, 0, 1))) {
                        if (serverLevel.getBlockState(p).is(Blocks.WATER)) {
                            serverLevel.removeBlock(p, false);
                            serverLevel.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.65D, p.getZ() + 0.5D,
                                    6, 0.25D, 0.25D, 0.25D, 0.04D);
                        }
                    }
                    serverLevel.playSound(null, boilPos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.85F, 1.18F);
                    this.onHitBlock(new BlockHitResult(this.position(), net.minecraft.core.Direction.UP, boilPos, false));
                    if (!this.isRemoved()) {
                        this.discard();
                    }
                    return;
                } else if ((slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                        && slot.formPart().mutationEnabled()
                        && formMateria == EssenceType.MAGMA)
                        || this.sentryBoltMateria == EssenceType.MAGMA) {
                    // Section 6.14 Molten Magma Boulder & Volcanic Magma Mortar: hisses and crusts into temporary magma on water impact!
                    BlockPos waterPos = this.blockPosition();
                    serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.35D, this.getZ(),
                            10, 0.35D, 0.25D, 0.35D, 0.04D);
                    serverLevel.playSound(null, waterPos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.85F, 0.95F);
                    this.onHitBlock(new BlockHitResult(this.position(), net.minecraft.core.Direction.UP, waterPos, false));
                    if (!this.isRemoved()) {
                        this.discard();
                    }
                    return;
                }
            }
        }

        if (!this.sporeOrbitalActive && (this.homingGuidance || this.assignedTargetUuid != null) && this.tickCount % 2 == 0) {
            steerTowardNearestHostile();
        }

        boolean reachedDistanceCap = !this.sporeOrbitalActive
                && !this.anchoredForDelayedBloom
                && this.originInitialized
                && this.position().distanceToSqr(this.originX, this.originY, this.originZ) >= (this.maxTravelDistance * this.maxTravelDistance);
        if (reachedDistanceCap || this.tickCount >= this.maxFlightTicks) {
            if (this.level() instanceof ServerLevel serverLevel) {
                if (reachedDistanceCap) {
                    Vec3 fromOrigin = this.position().subtract(this.originX, this.originY, this.originZ);
                    double d = fromOrigin.length();
                    if (d > this.maxTravelDistance && d > 1.0E-4D) {
                        Vec3 clamped = new Vec3(this.originX, this.originY, this.originZ)
                                .add(fromOrigin.scale(this.maxTravelDistance / d));
                        this.setPos(clamped.x, clamped.y, clamped.z);
                    }
                }
                SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
                LivingEntity caster = this.getOwner() instanceof LivingEntity l ? l : null;
                if (this.carrierPayload) {
                    if (SpellExecutionEngine.shouldDetonateCarrierAtMaxDistance(this.spell)) {
                        Vec3 dir = this.getDeltaMovement().lengthSqr() > 1.0E-4D ? this.getDeltaMovement().normalize() : new Vec3(0.0D, 1.0D, 0.0D);
                        triggerCarrierContactAndDiscard(
                                serverLevel, caster, this.position(), null, this.blockPosition(), net.minecraft.core.Direction.UP, dir
                        );
                        return;
                    }
                    if (this.spell.flowPart().usesDynamicTriggerBilling(this.spell.materiaSpecific(), this.fallbackMateria)
                            && !this.dynamicFeeCharged
                            && caster instanceof net.minecraft.world.entity.player.Player player) {
                        SpellExecutionEngine.scheduleMissFuelRefund(serverLevel, player, this.spell, this.fallbackMateria);
                    }
                }
                tryTriggerFlowSplit(serverLevel, caster, this.position(), slot);

                // Trigger open-air effects (Blink, Conjure, Gravity, Atmosphere, Sonar, Acoustic, Logistics, Diagnostic)
                // when a bolt reaches its maximum range in the open sky without hitting a solid block!
                if (!this.carrierPayload
                        && SpellExecutionEngine.hasOpenAirBlockVerb(slot)
                        && slot.primaryEffect().effect() != ddraig.net.entropica.spell.SpellCoreEffect.FLIGHT) {
                    SpellExecutionEngine.deliverSubSpellOnBlock(
                            serverLevel,
                            caster,
                            this.blockPosition(),
                            net.minecraft.core.Direction.UP,
                            this.spell,
                            slot,
                            this.fallbackMateria,
                            this.apparatusPotencyMult
                    );
                }

                EssenceType formMateria = slot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
                MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
                if (formHandler != null) {
                    formHandler.onProjectileExpiredFromForm(serverLevel, this, caster, this.spell, slot);
                }
            }
            this.discard();
        }
    }

    private void tickSporeOrbitalSpiral(ServerLevel level) {
        LivingEntity caster = this.getOwner() instanceof LivingEntity l && l.isAlive() ? l : null;
        if (caster == null) {
            this.sporeOrbitalActive = false;
            return;
        }

        EssenceType flowMateria = this.spell.flowPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);

        // Apply element-specific orbital ring contact & item attraction effects around the caster!
        if (this.tickCount % 6 == 0) {
            if (flowMateria == EssenceType.AURORA) {
                for (net.minecraft.world.entity.item.ItemEntity item : level.getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class, caster.getBoundingBox().inflate(5.5D))) {
                    if (item.isAlive()) {
                        Vec3 pull = caster.position().add(0.0D, 0.5D, 0.0D).subtract(item.position()).normalize().scale(0.35D);
                        item.setDeltaMovement(item.getDeltaMovement().scale(0.5D).add(pull));
                        item.hasImpulse = true;
                    }
                }
            }
            for (LivingEntity meleeFoe : level.getEntitiesOfClass(
                    LivingEntity.class,
                    caster.getBoundingBox().inflate(2.5D),
                    e -> e.isAlive()
                            && e != caster
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, this.spell.hasWardFriendlyFireSafeguard()))) {
                switch (flowMateria) {
                    case AIR -> {
                        if (this.spell.flowPart().mutationEnabled()) {
                            Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(0.75D);
                            SpellExecutionEngine.applySyncedMotion(meleeFoe, new Vec3(away.x, 0.30D, away.z));
                        }
                    }
                    case SPORE -> {
                        // Section 6.19 Drifting Spore-Pollen Orbit Flow: dense protective toxic shield around caster!
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(
                                level, meleeFoe, caster, 1.35F + oTier * 0.42F, false, true
                        );
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
                    }
                    case NULL_R -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 45, 0));
                        meleeFoe.setDeltaMovement(meleeFoe.getDeltaMovement().x, 0.42D, meleeFoe.getDeltaMovement().z);
                        meleeFoe.hasImpulse = true;
                    }
                    case NULL_U -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                        Vec3 inward = caster.position().subtract(meleeFoe.position()).normalize().scale(0.35D);
                        meleeFoe.setDeltaMovement(inward.x, 0.15D, inward.z);
                        meleeFoe.hasImpulse = true;
                    }
                    case DAWN -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                        if (meleeFoe.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                            meleeFoe.igniteForSeconds(3);
                        }
                    }
                    case ABYSS -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(
                                level, meleeFoe, caster, 1.35F + oTier * 0.42F, false, true
                        );
                        SpellLingeringZoneManager.applySoaked(meleeFoe, 120);
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(meleeFoe, level, 0.40D, 60L);
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                        Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(0.62D);
                        meleeFoe.setDeltaMovement(away.x, 0.22D, away.z);
                        meleeFoe.hasImpulse = true;
                    }
                    case AEGIS -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                                level, meleeFoe, caster, 1.50F + oTier * 0.45F, false, true
                        );
                        SpellCombatReactionEngine.applyTimedAttributeShred(meleeFoe, level, 2.0D, 0.0D, 80L);
                        Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(0.65D);
                        meleeFoe.setDeltaMovement(away.x, 0.24D, away.z);
                        meleeFoe.hasImpulse = true;
                    }
                    case AURORA -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(
                                level, meleeFoe, caster, 1.45F + oTier * 0.42F, false, true
                        );
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                        meleeFoe.setTicksFrozen(Math.min(meleeFoe.getTicksRequiredToFreeze() + 30, meleeFoe.getTicksFrozen() + 30));
                    }
                    case MIRAGE -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(
                                level, meleeFoe, caster, 1.45F + oTier * 0.42F, false, true
                        );
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                        SpellLingeringZoneManager.applyMirageTraitorFrenzy(meleeFoe, 50);
                    }
                    case WRAITH -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(
                                level, meleeFoe, caster, 1.45F + oTier * 0.42F, false, true
                        );
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0));
                    }
                    case BARROW -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(
                                level, meleeFoe, caster, 1.55F + oTier * 0.45F, false, true
                        );
                        SpellCombatReactionEngine.applyTimedAttributeShred(meleeFoe, level, 2.0D, 0.0D, 80L);
                        Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(0.65D);
                        meleeFoe.setDeltaMovement(away.x, 0.24D, away.z);
                        meleeFoe.hasImpulse = true;
                    }
                    case PYRE -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        meleeFoe.igniteForSeconds(4);
                        EntityHelper.getPersistentData(meleeFoe).putLong(
                                SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 80L
                        );
                        SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(
                                level, meleeFoe, caster, 1.75F + oTier * 0.48F, false, true
                        );
                    }
                    case PENUMBRA -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                                level, meleeFoe, caster, 1.80F + oTier * 0.50F, false, true
                        );
                    }
                    case RIME -> {
                        int oTier = Math.max(1, this.spell.getEffectiveTier());
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                        meleeFoe.setTicksFrozen(Math.min(meleeFoe.getTicksRequiredToFreeze() + 30, meleeFoe.getTicksFrozen() + 30));
                        SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(
                                level, meleeFoe, caster, 1.80F + oTier * 0.50F, false, true
                        );
                    }
                    case FROZEN, GLACIAL -> {
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(meleeFoe, level, 0.60D, 50L);
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 3));
                        int addFreeze = SpellLingeringZoneManager.isEntityWetOrSoaked(meleeFoe) ? 135 : 45;
                        meleeFoe.setTicksFrozen(Math.min(meleeFoe.getTicksRequiredToFreeze() + 60, meleeFoe.getTicksFrozen() + addFreeze));
                    }
                    case SPRING -> meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 4));
                    case STATIC -> meleeFoe.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.PARALYZED.get()), 20, 0));
                    case SYLVAN -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 5));
                        meleeFoe.addEffect(new MobEffectInstance(
                                net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.INERTIAL_ANCHOR.get()), 30, 0));
                    }
                    case GENESIS -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(1.25D);
                        meleeFoe.setDeltaMovement(away.x, 0.32D, away.z);
                        meleeFoe.hasImpulse = true;
                    }
                    case CHIMERA -> {
                        switch (level.random.nextInt(4)) {
                            case 0 -> {
                                Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(0.95D);
                                meleeFoe.setDeltaMovement(away.x, 0.35D, away.z);
                                meleeFoe.hasImpulse = true;
                            }
                            case 1 -> SpellCombatReactionEngine.applyTimedAttributeShred(meleeFoe, level, 2.0D, 0.0D, 80L);
                            case 2 -> SpellLingeringZoneManager.applySoaked(meleeFoe, 100);
                            default -> meleeFoe.igniteForSeconds(3);
                        }
                    }
                    default -> {
                    }
                }
            }
        }

        // After spiraling tightly around the caster, fling outward toward nearby hostiles (or radially at tick 40)!
        int minShieldTicks = (flowMateria == EssenceType.SPORE) ? 22 : 18;
        int maxShieldTicks = (flowMateria == EssenceType.SPORE) ? 42 : 36;
        double seekRad = (flowMateria == EssenceType.SPORE) ? 11.5D : 8.5D;
        if (this.tickCount >= minShieldTicks) {
            List<LivingEntity> targets = level.getEntitiesOfClass(
                    LivingEntity.class,
                    caster.getBoundingBox().inflate(seekRad),
                    e -> e.isAlive()
                            && e != caster
                            && (e instanceof Monster || e == caster.getLastHurtMob())
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, this.spell.hasWardFriendlyFireSafeguard())
            );
            if (!targets.isEmpty() || this.tickCount >= maxShieldTicks) {
                this.sporeOrbitalActive = false;
                this.homingGuidance = true;
                this.originX = this.getX();
                this.originY = this.getY();
                this.originZ = this.getZ();
                this.originInitialized = true;
                this.cruiseSpeed = 1.48D;
                this.maxFlightTicks = Math.max(this.tickCount + (int) Math.ceil(this.maxTravelDistance / 0.95D) + 25, this.maxFlightTicks);
                Vec3 flingDir;
                if (!targets.isEmpty()) {
                    LivingEntity tgt = targets.get(Math.floorMod(this.getId(), targets.size()));
                    this.assignedTargetUuid = tgt.getUUID();
                    flingDir = tgt.getEyePosition().subtract(this.position()).normalize();
                } else {
                    Vec3 radial = this.position().subtract(caster.getEyePosition());
                    flingDir = radial.lengthSqr() > 0.01D ? radial.normalize() : caster.getLookAngle().normalize();
                }
                this.setDeltaMovement(flingDir.scale(1.48D));
                this.hasImpulse = true;
                if (flowMateria == EssenceType.SPORE) {
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x1A5923, 22, 0.90F, 0.06F),
                            this.getX(), this.getY(), this.getZ(), 6, 0.18D, 0.18D, 0.18D, 0.04D
                    );
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xA88654, 22, 0.85F, 0.06F),
                            this.getX(), this.getY(), this.getZ(), 5, 0.18D, 0.18D, 0.18D, 0.04D
                    );
                    level.playSound(null, this.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.35F, 1.55F);
                } else {
                    level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
                }
                return;
            }
        }

        double angle = this.orbitPhaseRadians + (this.tickCount * (flowMateria == EssenceType.SPORE ? 0.36D : 0.30D));
        double radius = (flowMateria == EssenceType.SPORE ? 1.65D : 1.95D) + 0.22D * Math.sin(this.tickCount * 0.20D);
        Vec3 desiredPos = new Vec3(
                caster.getX() + Math.cos(angle) * radius,
                caster.getY(0.60D) + 0.20D * Math.cos(this.tickCount * 0.25D),
                caster.getZ() + Math.sin(angle) * radius
        );
        Vec3 stepVel = desiredPos.subtract(this.position());
        this.setDeltaMovement(stepVel);
        this.hasImpulse = true;
        var trailParticle = switch (flowMateria) {
            case SPORE -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case NULL_U, ABYSS -> ParticleTypes.SQUID_INK;
            case AURORA, RIME -> ParticleTypes.SNOWFLAKE;
            case WRAITH -> ParticleTypes.SOUL;
            case PYRE -> ParticleTypes.FLAME;
            case SPRING -> ParticleTypes.CHERRY_LEAVES;
            case STATIC -> ParticleTypes.ELECTRIC_SPARK;
            case SYLVAN -> ParticleTypes.HAPPY_VILLAGER;
            case CHIMERA -> ParticleTypes.ENCHANTED_HIT;
            default -> ParticleTypes.END_ROD;
        };
        level.sendParticles(trailParticle, this.getX(), this.getY(), this.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    private void applySentryBoltImpact(ServerLevel level, @Nullable LivingEntity caster, Vec3 impactPos, @Nullable Entity directHit) {
        if (this.sentryBoltMateria == null) return;
        Vec3 travel = this.getDeltaMovement().lengthSqr() > 0.001D ? this.getDeltaMovement().normalize() : new Vec3(0.0D, 0.2D, 0.0D);

        switch (this.sentryBoltMateria) {
            case AIR -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.setDeltaMovement(foe.getDeltaMovement().x, 0.72D, foe.getDeltaMovement().z);
                    foe.hasImpulse = true;
                    level.sendParticles(ParticleTypes.GUST, foe.getX(), foe.getY(0.2D), foe.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.02D);
                }
            }
            case WATER -> {
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.WATER, null, 2.2F, this.spell.getEffectiveTier()
                );
                level.sendParticles(ParticleTypes.SPLASH, impactPos.x, impactPos.y + 0.3D, impactPos.z, 18, 0.7D, 0.35D, 0.7D, 0.10D);
                level.playSound(null, BlockPos.containing(impactPos), net.minecraft.sounds.SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.75F, 1.25F);
                BlockPos hitPos = BlockPos.containing(impactPos);
                for (BlockPos p : BlockPos.betweenClosed(hitPos.offset(-2, -1, -2), hitPos.offset(2, 1, 2))) {
                    var bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                        level.removeBlock(p, false);
                    } else if (bs.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock
                            && bs.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
                        level.setBlockAndUpdate(p, bs.setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
                    } else if (bs.getBlock() instanceof net.minecraft.world.level.block.AbstractCandleBlock
                            && bs.getValue(net.minecraft.world.level.block.AbstractCandleBlock.LIT)) {
                        net.minecraft.world.level.block.AbstractCandleBlock.extinguish(null, bs, level, p);
                    }
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.25D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.clearFire();
                    SpellLingeringZoneManager.applySoaked(foe, 160);
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(travel.x * 0.72D, 0.25D, travel.z * 0.72D));
                    }
                }
            }
            case OASIS -> {
                int sTier = Math.max(1, this.getVisualPotencyTier());
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.hurtWithOasisMateriaAdvantage(
                            level, foe, caster, 1.45F + sTier * 0.42F, false, true
                    );
                    foe.clearFire();
                    SpellLingeringZoneManager.applySoaked(foe, 140);
                } else {
                    ddraig.net.entropica.spell.mutation.element.OasisSpellMutationHandler
                            .hydrateAndGrowFloraPublic(level, net.minecraft.core.BlockPos.containing(impactPos), 1);
                }
                level.sendParticles(ParticleTypes.SPLASH, impactPos.x, impactPos.y + 0.2D, impactPos.z, 8, 0.25D, 0.2D, 0.25D, 0.04D);
            }
            case NATURE, OVERGROWTH -> {
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), this.sentryBoltMateria, null, 0.90F, sTier
                );
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    if (this.sentryBoltMateria == EssenceType.OVERGROWTH) {
                        SpellCombatReactionEngine.hurtWithOvergrowthMateriaAdvantage(
                                level, foe, caster, 2.5F + sTier * 0.72F, false, true
                        );
                        EntityHelper.getPersistentData(foe).putLong(
                                SpellLingeringZoneManager.TAG_OVERGROWTH_BRIAR_ROOT_UNTIL, level.getGameTime() + 40L);
                    }
                    // Sylvan Thorn Sentry & Living Root Totem Thorn Dart: roots struck foe to the spot for 2.0s (40 ticks)!
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                    if (this.sentryBoltMateria == EssenceType.NATURE) {
                        foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                    }
                    EntityHelper.getPersistentData(foe).putLong(
                            SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 40L);
                    foe.setDeltaMovement(0.0D, Math.min(0.0D, foe.getDeltaMovement().y), 0.0D);
                    foe.hasImpulse = true;
                    SpellWorldVfxManager.broadcastProjectileImpact(
                            level, foe.position().add(0.0D, 0.12D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                            this.sentryBoltMateria, null, 1.15F, sTier
                    );
                }
            }
            case EARTH -> {
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.EARTH, null, 1.25F, sTier
                );
                level.playSound(null, BlockPos.containing(impactPos), net.minecraft.sounds.SoundEvents.DRIPSTONE_BLOCK_BREAK,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.80F, 0.82F);
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(
                            level, foe, caster, 2.5F + sTier * 0.75F, false, true
                    );
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 140L);
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(travel.x * 1.05D, 0.32D, travel.z * 1.05D));
                    }
                }
            }
            case FROZEN, GLACIAL -> {
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), this.sentryBoltMateria, null, 1.15F, sTier
                );
                level.playSound(null, BlockPos.containing(impactPos), net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.80F, 1.45F);
                BlockPos hitPos = BlockPos.containing(impactPos);
                for (BlockPos p : BlockPos.betweenClosed(hitPos.offset(-1, -1, -1), hitPos.offset(1, 1, 1))) {
                    if (level.getBlockState(p).is(Blocks.WATER)) {
                        level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    if (this.sentryBoltMateria == EssenceType.GLACIAL) {
                        SpellCombatReactionEngine.hurtWithGlacialMateriaAdvantage(
                                level, foe, caster, 2.5F + sTier * 0.72F, false, false, true
                        );
                    } else {
                        SpellCombatReactionEngine.hurtWithFrozenMateriaAdvantage(
                                level, foe, caster, 2.2F + sTier * 0.65F, false, true
                        );
                    }
                    boolean tripleFreeze = SpellLingeringZoneManager.isEntityWetOrSoaked(foe)
                            || SpellCombatReactionEngine.hasActiveMutatedHexPublic(foe, EssenceType.WATER);
                    int addFreeze = tripleFreeze ? 60 * 3 : 60;
                    foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 60, foe.getTicksFrozen() + addFreeze));
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.60D, 60L); // True -60% Speed!
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3)); // 60% Slowness for 3s!
                }
            }
            case NETHER -> {
                // Section 6.7 Infernal Magma Mortar: lobbed explosive fireball detonating in 3D molten splash & Nether Materia damage!
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcast(
                        level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                        impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.NETHER, null, 2.35F, 58, sTier
                );
                level.playSound(null, BlockPos.containing(impactPos), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.80F, 1.18F);
                level.sendParticles(ParticleTypes.FLAME, impactPos.x, impactPos.y + 0.3D, impactPos.z, 24, 0.9D, 0.45D, 0.9D, 0.09D);
                level.sendParticles(ParticleTypes.LAVA, impactPos.x, impactPos.y + 0.25D, impactPos.z, 12, 0.45D, 0.3D, 0.45D, 0.05D);
                ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.autoSmeltDroppedItems(
                        level, this.getBoundingBox().inflate(2.8D)
                );
                ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.spawnTemporaryFirePoolPublic(
                        level, caster, BlockPos.containing(impactPos), 1, 80, 0.55F
                );
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.8D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.igniteForSeconds(5);
                    SpellCombatReactionEngine.hurtWithNetherMateriaAdvantage(
                            level, foe, caster, 3.0F + sTier * 0.75F, true
                    );
                }
            }
            case RADIANT -> {
                // Section 6.8 Solar Seraph Agent: rains piercing golden light arrows down on foes (+1 vs Undead, -1 vs Umbral)!
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcast(
                        level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                        impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.RADIANT, null, 1.80F, 52, sTier
                );
                level.sendParticles(ParticleTypes.END_ROD, impactPos.x, impactPos.y + 0.25D, impactPos.z, 18, 0.35D, 0.35D, 0.35D, 0.09D);
                level.sendParticles(
                        ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                                ddraig.net.entropica.registry.ModParticles.RUNE_SOWILO.get(), 0xFFE278, 26, 1.05F, 0.05F),
                        impactPos.x, impactPos.y + 0.35D, impactPos.z, 4, 0.28D, 0.28D, 0.28D, 0.02D
                );
                level.playSound(null, BlockPos.containing(impactPos), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.88F, 1.48F);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.0D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.removeEffect(MobEffects.INVISIBILITY);
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                    boolean undeadFoe = foe.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)
                            || foe instanceof net.minecraft.world.entity.monster.Phantom;
                    if (undeadFoe) {
                        foe.igniteForSeconds(5);
                    }
                    float arrowSmite = (3.0F + sTier * 0.78F) * (undeadFoe ? 1.35F : 1.0F);
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                            level, foe, caster, arrowSmite, false, true
                    );
                }
            }
            case UMBRAL -> {
                // Section 6.9 Shadow Stalker Sentry Dart: silent light-absorbing shadow needle blinding & pinning struck foes!
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcast(
                        level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                        impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.UMBRAL, null, 1.35F, 48, sTier
                );
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    ddraig.net.entropica.spell.mutation.element.UmbralSpellMutationHandler.pinEntityShadowToFloor(
                            level, foe, 40, sTier
                    );
                    SpellCombatReactionEngine.hurtWithUmbralMateriaAdvantage(
                            level, foe, caster, 2.8F + sTier * 0.72F, false, true
                    );
                }
            }
            case UNDEAD -> {
                // Section 6.10 Cryptguard Ossuary Totem Bolt: seeking cursed skull that shatters on impact!
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcast(
                        level, ddraig.net.entropica.network.SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                        impactPos, travel, EssenceType.UNDEAD, null, 1.45F, 50, sTier
                );
                level.sendParticles(
                        new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.BONE_BLOCK.defaultBlockState()),
                        impactPos.x, impactPos.y + 0.2D, impactPos.z, 16, 0.32D, 0.28D, 0.32D, 0.10D
                );
                level.playSound(null, BlockPos.containing(impactPos), SoundEvents.SKELETON_HURT, SoundSource.PLAYERS, 0.75F, 0.85F);
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                    SpellCombatReactionEngine.hurtWithUndeadMateriaAdvantage(
                            level, foe, caster, 2.8F + sTier * 0.75F, false, false, false, true
                    );
                }
            }
            case VOID -> {
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.VOID, null, 1.15F, sTier
                );
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, impactPos.x, impactPos.y + 0.3D, impactPos.z,
                        12, 0.25D, 0.25D, 0.25D, 0.06D);
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    SpellCombatReactionEngine.hurtWithVoidMateriaAdvantage(
                            level, foe, caster, 2.2F + sTier * 0.70F, false, 0.25F, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        Vec3 inward = travel.scale(-0.75D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(inward.x, 0.20D, inward.z));
                    }
                }
            }
            case ARID -> {
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.ARID, null, 1.15F, sTier
                );
                level.sendParticles(
                        new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.FALLING_DUST, net.minecraft.world.level.block.Blocks.SANDSTONE.defaultBlockState()),
                        impactPos.x, impactPos.y + 0.25D, impactPos.z, 16, 0.30D, 0.25D, 0.30D, 0.08D
                );
                level.playSound(null, BlockPos.containing(impactPos), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.85F, 0.90F);
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                    SpellCombatReactionEngine.hurtWithAridMateriaAdvantage(
                            level, foe, caster, 2.3F + sTier * 0.72F, false, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(travel.x * 1.25D, 0.34D, travel.z * 1.25D));
                    }
                }
            }
            case LIGHTNING -> {
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.LIGHTNING, null, 1.15F, sTier
                );
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, impactPos.x, impactPos.y + 0.25D, impactPos.z,
                        18, 0.30D, 0.25D, 0.30D, 0.12D);
                level.playSound(null, BlockPos.containing(impactPos), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.55F, 1.65F);
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    SpellCombatReactionEngine.hurtWithLightningMateriaAdvantage(
                            level, foe, caster, 2.4F + sTier * 0.72F, false, true
                    );
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
                }
            }
            case VITAE -> {
                // Section 6.22 Cardiovascular Spire defensive vitality dart (#FF6B9D): deals Vitae Materia damage & disorients foes!
                int sTier = this.spell.getEffectiveTier();
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.VITAE, EssenceType.NATURE, 0.95F, sTier
                );
                level.sendParticles(ParticleTypes.HEART,
                        impactPos.x, impactPos.y + 0.15D, impactPos.z, 3, 0.22D, 0.22D, 0.22D, 0.02D);
                if (directHit instanceof LivingEntity foe
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(
                            level, foe, caster, 2.3F + sTier * 0.68F, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                    ddraig.net.entropica.util.EntityHelper.getPersistentData(foe)
                            .putLong(SpellLingeringZoneManager.TAG_VITAE_DISORIENTED_UNTIL, level.getGameTime() + 60L);
                }
            }
            case MAGMA -> {
                // Section 6.14 Volcanic Magma Mortar Sentry boulder splatter: 3D molten splash, 6s fire, pure Magma Materia damage & 1-block magma crust!
                int sTier = this.spell.getEffectiveTier();
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.MAGMA, EssenceType.EARTH, 1.45F, sTier
                );
                level.sendParticles(ParticleTypes.LAVA, impactPos.x, impactPos.y + 0.3D, impactPos.z, 18, 0.6D, 0.35D, 0.6D, 0.08D);
                ddraig.net.entropica.spell.mutation.element.NetherSpellMutationHandler.autoSmeltDroppedItems(
                        level, this.getBoundingBox().inflate(2.5D));
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.6D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.igniteForSeconds(6);
                    SpellCombatReactionEngine.hurtWithMagmaMateriaAdvantage(
                            level, foe, caster, 2.8F + sTier * 0.75F, false, true
                    );
                }
                BlockPos hitGround = BlockPos.containing(impactPos).below();
                if (!level.getBlockState(hitGround).isSolidRender() && level.getBlockState(BlockPos.containing(impactPos)).is(Blocks.WATER)) {
                    hitGround = BlockPos.containing(impactPos);
                }
                var orig = level.getBlockState(hitGround);
                if ((orig.isSolidRender() || orig.is(Blocks.WATER))
                        && !orig.is(Blocks.MAGMA_BLOCK)
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, hitGround, orig)) {
                    level.setBlockAndUpdate(hitGround, Blocks.MAGMA_BLOCK.defaultBlockState());
                    BlockPos savedPos = hitGround.immutable();
                    SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                            caster, Vec3.atCenterOf(savedPos), 1.0D, 80, level.getGameTime(), null, null, EssenceType.MAGMA, 1.0F,
                            (lvl, z, el) -> {
                                if (el >= z.maxDurationTicks) {
                                    if (lvl.getBlockState(savedPos).is(Blocks.MAGMA_BLOCK)) lvl.setBlockAndUpdate(savedPos, orig);
                                    return false;
                                }
                                return true;
                            }
                    ));
                }
            }
            case STORM -> {
                // Section 6.15 Tempest-Squall Spire Sentry pressurized water/wind slug: heavy 5m pushback, soaking & Storm Materia damage!
                int sTier = this.spell.getEffectiveTier();
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.STORM, EssenceType.AIR, 1.35F, sTier
                );
                level.sendParticles(ParticleTypes.SPLASH, impactPos.x, impactPos.y + 0.2D, impactPos.z, 18, 0.45D, 0.35D, 0.45D, 0.10D);
                if (directHit instanceof LivingEntity foe && foe.isAlive()
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) {
                    foe.clearFire();
                    SpellLingeringZoneManager.applySoaked(foe, 180);
                    SpellCombatReactionEngine.hurtWithStormMateriaAdvantage(
                            level, foe, caster, 2.6F + sTier * 0.72F, false, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(travel.x * 1.38D, 0.38D, travel.z * 1.38D));
                    }
                }
                BlockPos hitCenter = BlockPos.containing(impactPos);
                for (BlockPos p : BlockPos.betweenClosed(hitCenter.offset(-2, -1, -2), hitCenter.offset(2, 2, 2))) {
                    if (level.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                        level.removeBlock(p, false);
                    }
                }
            }
            case DUST -> {
                // Section 6.18 Abrasive Dust Pylon: high-pressure mineral dust slug (#C2B28F) with pushback, Blindness & Dust Materia damage!
                int sTier = this.spell.getEffectiveTier();
                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.DUST, EssenceType.AIR, 1.25F, sTier
                );
                level.sendParticles(
                        new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.FALLING_DUST, net.minecraft.world.level.block.Blocks.SANDSTONE.defaultBlockState()),
                        impactPos.x, impactPos.y + 0.25D, impactPos.z, 18, 0.35D, 0.30D, 0.35D, 0.08D
                );
                level.playSound(null, BlockPos.containing(impactPos), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.80F, 1.15F);
                ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.snuffTorchesAndFiresPublic(
                        level, BlockPos.containing(impactPos), 2
                );
                if (directHit instanceof LivingEntity foe && foe.isAlive()
                        && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true))) {
                    foe.clearFire();
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    ddraig.net.entropica.spell.mutation.element.DustSpellMutationHandler.stackArmorErosionPublic(foe, 1, 100);
                    SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(
                            level, foe, caster, 2.5F + sTier * 0.72F, false, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(travel.x * 1.32D, 0.38D, travel.z * 1.32D));
                    }
                }
            }
            case SPORE -> {
                // Section 6.19 Bio-Pollen Spire: buoyant Spore-Pod deals Spore Materia damage, sprouts mushrooms & bursts into a 3s toxic spore cloud!
                int sTier = Math.max(1, this.spell.getEffectiveTier());
                ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                        level, impactPos, travel, EssenceType.SPORE, EssenceType.AIR, 1.35F, sTier
                );
                if (directHit instanceof LivingEntity foe && foe != caster
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) {
                    SpellCombatReactionEngine.hurtWithSporeMateriaAdvantage(
                            level, foe, caster, 2.2F + sTier * 0.68F, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
                }
                ddraig.net.entropica.spell.mutation.element.SporeSpellMutationHandler.sproutMushroomsPublic(
                        level, net.minecraft.core.BlockPos.containing(impactPos), 2, false
                );
                SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, impactPos, 2.5D, 60, level.getGameTime(), this.spell, slot, EssenceType.SPORE, 1.0F,
                        (lvl, z, el) -> {
                            if (el % 10 == 0) {
                                lvl.sendParticles(
                                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0x1A5923, 24, 0.90F, 0.08F),
                                        z.center.x, z.center.y + 0.5D, z.center.z, 8, 1.1D, 0.4D, 1.1D, 0.03D
                                );
                                lvl.sendParticles(
                                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPORE.get(), 0xA88654, 24, 0.85F, 0.08F),
                                        z.center.x, z.center.y + 0.6D, z.center.z, 6, 1.1D, 0.4D, 1.1D, 0.03D
                                );
                                lvl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, z.center.x, z.center.y + 0.5D, z.center.z,
                                        8, 1.1D, 0.4D, 1.1D, 0.03D);
                                LivingEntity owner = z.resolveCaster(lvl);
                                for (LivingEntity foe : lvl.getEntitiesOfClass(LivingEntity.class,
                                        new net.minecraft.world.phys.AABB(z.center.x - 2.5D, z.center.y - 1.0D, z.center.z - 2.5D,
                                                z.center.x + 2.5D, z.center.y + 2.0D, z.center.z + 2.5D))) {
                                    if (!foe.isAlive() || foe == owner || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                                    foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
                                }
                            }
                            return el < z.maxDurationTicks;
                        }
                ));
            }
            case AURA -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = Math.max(1, this.getVisualPotencyTier());
                    SpellCombatReactionEngine.hurtWithAuraMateriaAdvantage(
                            level, foe, caster, 1.35F + sTier * 0.40F, false, true
                    );
                    if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(travel.x * 0.95D, 0.34D, travel.z * 0.95D));
                    }
                }
            }
            case AMBER -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = Math.max(1, this.getVisualPotencyTier());
                    SpellCombatReactionEngine.hurtWithAmberMateriaAdvantage(
                            level, foe, caster, 1.40F + sTier * 0.42F, false, true
                    );
                    SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.50D, 60L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 60L);
                }
            }
            case TAIGA -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = Math.max(1, this.getVisualPotencyTier());
                    SpellCombatReactionEngine.hurtWithTaigaMateriaAdvantage(
                            level, foe, caster, 1.35F + sTier * 0.40F, false, true
                    );
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                    if (foe.canFreeze()) {
                        foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 40, foe.getTicksFrozen() + 50));
                    }
                    net.minecraft.nbt.CompoundTag fTag = EntityHelper.getPersistentData(foe);
                    fTag.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, level.getGameTime() + 80L);
                    if (this.alreadyHitEntities.size() == 2) {
                        // Section 6.20 Hardened Frost-Resin Spike: pins the 2nd pierced victim in sticky freezing sap for 3.0s (60t)!
                        fTag.putLong(SpellLingeringZoneManager.TAG_TAIGA_SAP_PIN_UNTIL, level.getGameTime() + 60L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), 0x1A5923, 28, 1.05F, 0.06F),
                                foe.getX(), foe.getY(0.35D), foe.getZ(), 12, 0.35D, 0.25D, 0.35D, 0.04D);
                    }
                }
            }
            case ECLIPSE -> {
                if (directHit instanceof LivingEntity foe && foe.isAlive()) {
                    int sTier = Math.max(1, this.getVisualPotencyTier());
                    float shadowBoltDmg = 1.45F + sTier * 0.45F;
                    SpellCombatReactionEngine.hurtWithEclipseMateriaAdvantage(level, foe, caster, shadowBoltDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                    for (var eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                        if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                    }
                }
            }
            case BLIGHT -> {
                if (directHit instanceof LivingEntity foe && foe.isAlive()) {
                    int sTier = Math.max(1, this.getVisualPotencyTier());
                    float thornDartDmg = 1.45F + sTier * 0.44F;
                    SpellCombatReactionEngine.hurtWithBlightMateriaAdvantage(level, foe, caster, thornDartDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 1.0D, 0.0D, 100L);
                }
            }
            case ASTRAL -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float orbDmg = 1.50F + sTier * 0.45F;
                    SpellCombatReactionEngine.hurtWithAstralMateriaAdvantage(level, foe, caster, orbDmg, false, true);
                    foe.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.PARALYZED.get()), 15, 0));
                    foe.setDeltaMovement(foe.getDeltaMovement().x, 0.48D, foe.getDeltaMovement().z);
                    foe.hasImpulse = true;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, foe.getX(), foe.getY(0.85D), foe.getZ(), 10, 0.25D, 0.30D, 0.25D, 0.08D);
                }
            }
            case SOULFIRE -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float skullDmg = 1.55F + sTier * 0.45F;
                    float dealt = SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(level, foe, caster, skullDmg, false, true);
                    foe.igniteForSeconds(5);
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 100L);
                    if (dealt > 0.0F && caster != null && caster.isAlive()) {
                        float heal = dealt * 0.20F;
                        SpellCombatReactionEngine.notifyMagicalHeal(caster, heal);
                        caster.heal(heal);
                    }
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, foe.getX(), foe.getY(0.8D), foe.getZ(), 10, 0.25D, 0.30D, 0.25D, 0.04D);
                }
            }
            case VAPOR -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float steamDmg = 1.50F + sTier * 0.44F;
                    SpellCombatReactionEngine.hurtWithVaporMateriaAdvantage(level, foe, caster, steamDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    SpellLingeringZoneManager.applySoaked(foe, 120);
                }
            }
            case NULL_R -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float lanceDmg = 1.85F + sTier * 0.52F;
                    SpellCombatReactionEngine.hurtWithNullRMateriaAdvantage(level, foe, caster, lanceDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
                    ddraig.net.entropica.spell.mutation.element.EclipseSpellMutationHandler.purgeBeneficialEffects(foe);
                    level.sendParticles(ParticleTypes.END_ROD, foe.getX(), foe.getY(0.85D), foe.getZ(), 10, 0.24D, 0.28D, 0.24D, 0.04D);
                }
            }
            case NULL_U -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float dartDmg = 1.75F + sTier * 0.48F;
                    SpellCombatReactionEngine.hurtWithNullUMateriaAdvantage(level, foe, caster, dartDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 55);
                    Vec3 inward = travel.scale(-0.78D);
                    foe.setDeltaMovement(inward.x, 0.22D, inward.z);
                    foe.hasImpulse = true;
                    level.sendParticles(ParticleTypes.SQUID_INK, foe.getX(), foe.getY(0.8D), foe.getZ(), 8, 0.22D, 0.26D, 0.22D, 0.03D);
                }
            }
            case ABYSS -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float trenchDmg = 1.80F + sTier * 0.50F;
                    SpellCombatReactionEngine.hurtWithAbyssMateriaAdvantage(level, foe, caster, trenchDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 50);
                    SpellLingeringZoneManager.applySoaked(foe, 140);
                    Vec3 pull = travel.scale(-0.68D);
                    foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                    foe.hasImpulse = true;
                    level.sendParticles(ParticleTypes.SQUID_INK, foe.getX(), foe.getY(0.8D), foe.getZ(), 8, 0.22D, 0.25D, 0.22D, 0.03D);
                }
            }
            case DAWN -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float javelinDmg = 1.85F + sTier * 0.52F;
                    SpellCombatReactionEngine.hurtWithDawnMateriaAdvantage(level, foe, caster, javelinDmg, false, true);
                    foe.setDeltaMovement(travel.x * 1.45D, 0.40D, travel.z * 1.45D);
                    foe.hasImpulse = true;
                    foe.removeEffect(MobEffects.INVISIBILITY);
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                    if (foe.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                        foe.igniteForSeconds(5);
                    }
                    level.sendParticles(ParticleTypes.END_ROD, foe.getX(), foe.getY(0.85D), foe.getZ(), 10, 0.24D, 0.28D, 0.24D, 0.04D);
                }
            }
            case AEGIS -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float slugDmg = 2.05F + sTier * 0.55F;
                    SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(level, foe, caster, slugDmg, false, true);
                    foe.setDeltaMovement(travel.x * 1.25D, 0.32D, travel.z * 1.25D);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3));
                    level.sendParticles(ParticleTypes.WAX_ON, foe.getX(), foe.getY(0.85D), foe.getZ(), 8, 0.22D, 0.26D, 0.22D, 0.03D);
                }
            }
            case AURORA -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float lanceDmg = 1.95F + sTier * 0.52F;
                    SpellCombatReactionEngine.hurtWithAuroraMateriaAdvantage(level, foe, caster, lanceDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 40);
                    level.sendParticles(ParticleTypes.SNOWFLAKE, foe.getX(), foe.getY(0.85D), foe.getZ(), 8, 0.22D, 0.26D, 0.22D, 0.03D);
                    level.sendParticles(ParticleTypes.END_ROD, foe.getX(), foe.getY(0.90D), foe.getZ(), 6, 0.22D, 0.26D, 0.22D, 0.04D);
                }
            }
            case MIRAGE -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float dartDmg = 1.95F + sTier * 0.52F;
                    SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, foe, caster, dartDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    SpellLingeringZoneManager.applyMirageTraitorFrenzy(foe, 60);
                }
            }
            case WRAITH -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float skullDmg = 1.95F + sTier * 0.52F;
                    SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, foe, caster, skullDmg, false, true);
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                }
            }
            case BARROW -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float tombDmg = 2.05F + sTier * 0.54F;
                    SpellCombatReactionEngine.hurtWithBarrowMateriaAdvantage(level, foe, caster, tombDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                }
            }
            case PYRE -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float pyreBoltDmg = 2.20F + sTier * 0.58F;
                    foe.igniteForSeconds(5);
                    EntityHelper.getPersistentData(foe).putLong(
                            SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
                    );
                    SpellCombatReactionEngine.hurtWithPyreMateriaAdvantage(level, foe, caster, pyreBoltDmg, false, true);
                }
            }
            case PENUMBRA -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float lanceDmg = 2.15F + sTier * 0.56F;
                    SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, foe, caster, lanceDmg, false, true);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    Vec3 pull = travel.scale(-0.55D);
                    foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                    foe.hasImpulse = true;
                }
            }
            case RIME -> {
                if (directHit instanceof LivingEntity foe) {
                    int sTier = this.spell != null ? this.spell.getEffectiveTier() : 1;
                    float rimeBoltDmg = 2.25F + sTier * 0.58F;
                    SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, foe, caster, rimeBoltDmg, false, true);
                    foe.setTicksFrozen(foe.getTicksFrozen() + 60);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                }
            }
            case SPRING -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellLingeringZoneManager.applySoaked(foe, 140);
                    if (foe.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                        foe.hurtServer(level, level.damageSources().magic(), 3.0F);
                    }
                }
            }
            case STATIC -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.PARALYZED.get()), 20, 0));
                    foe.hurtServer(level, level.damageSources().lightningBolt(), 2.5F);
                }
            }
            case SYLVAN -> {
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, impactPos.x, impactPos.y + 0.3D, impactPos.z, 12, 0.6D, 0.3D, 0.6D, 0.04D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 5));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 45, 0));
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                }
            }
            case MIASMA -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.setDeltaMovement(foe.getDeltaMovement().x, 0.72D, foe.getDeltaMovement().z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    for (var eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                        if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                    }
                }
            }
            case GENESIS -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    boolean smiteTarget = foe.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)
                            || (foe instanceof ddraig.net.entropica.entity.SpellEidolonEntity se
                            && (se.getEffectiveMateria() == EssenceType.WRAITH || se.getEffectiveMateria() == EssenceType.OBLIVION))
                            || EntityHelper.getPersistentData(foe)
                            .getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L) > level.getGameTime()
                            || EntityHelper.getPersistentData(foe)
                            .getLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL).orElse(0L) > level.getGameTime();
                    if (smiteTarget) {
                        foe.hurtServer(level, level.damageSources().magic(), 3.5F + this.spell.getEffectiveTier() * 0.5F);
                    }
                }
            }
            case OBLIVION -> {
                level.sendParticles(ParticleTypes.SMOKE, impactPos.x, impactPos.y + 0.3D, impactPos.z, 14, 0.6D, 0.3D, 0.6D, 0.05D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.igniteForSeconds(5);
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 120L);
                }
            }
            case CHIMERA -> {
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, impactPos.x, impactPos.y + 0.3D, impactPos.z, 14, 0.6D, 0.3D, 0.6D, 0.08D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    switch (level.random.nextInt(4)) {
                        case 0 -> {
                            foe.setDeltaMovement(travel.x * 1.25D, 0.45D, travel.z * 1.25D);
                            foe.hasImpulse = true;
                        }
                        case 1 -> foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 3));
                        case 2 -> SpellLingeringZoneManager.applySoaked(foe, 140);
                        default -> foe.igniteForSeconds(4);
                    }
                }
            }
            case AETHER -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));
                }
            }
            case ENTROPIC -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                }
            }
            case CELESTIAL -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }
            }
            case EMPYREAN -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                }
            }
            case CATACLYSM -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    foe.igniteForSeconds(5);
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
                }
            }
            case TERMINUS -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 40);
                }
            }
            case ESCHATON -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    Vec3 pull = travel.scale(-0.65D);
                    foe.setDeltaMovement(pull.x, 0.15D, pull.z);
                    foe.hasImpulse = true;
                }
            }
            case APOTHEOSIS -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    if (caster != null) {
                        SpellCombatReactionEngine.notifyMagicalHeal(caster, 4.0F);
                        caster.heal(4.0F);
                    }
                }
            }
            case ENTROPICA -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
                    foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 3.0F);
                }
            }
            default -> {
            }
        }
    }

    private void tryTriggerFlowSplit(ServerLevel serverLevel, @Nullable LivingEntity caster, Vec3 splitOrigin, SubSpellSlot slot) {
        if (this.splitChild || this.flowSplitTriggered) {
            return;
        }
        this.flowSplitTriggered = true;
        EssenceType flowMateria = this.spell.flowPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        MateriaPartMutationHandler flowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, this.spell.flowPart().mutationEnabled());
        if (flowHandler != null) {
            flowHandler.onProjectileImpactOrExpireFromFlow(serverLevel, this, caster, splitOrigin, this.spell, slot);
        }
    }

    private void steerTowardNearestHostile() {
        Vec3 currentVel = this.getDeltaMovement();
        double speed = currentVel.length();
        if (speed < 0.05D) return;

        LivingEntity caster = this.getOwner() instanceof LivingEntity l ? l : null;

        // Smart dedicated target lock for Bifurcation / Trifurcation / Satellite Orbit!
        if (this.assignedTargetUuid != null && this.level() instanceof ServerLevel srv) {
            Entity assigned = srv.getEntity(this.assignedTargetUuid);
            if (assigned instanceof LivingEntity assignedLiv
                    && assignedLiv.isAlive()
                    && !this.alreadyHitEntities.contains(assignedLiv.getUUID())) {
                Vec3 desired = assignedLiv.getEyePosition().subtract(this.position()).normalize().scale(speed);
                Vec3 blended = currentVel.scale(0.70D).add(desired.scale(0.30D)).normalize().scale(speed);
                this.setDeltaMovement(blended);
                this.hasImpulse = true;
                return;
            }
            this.assignedTargetUuid = null;
            if (!this.homingGuidance) {
                return;
            }
        }
        EssenceType flowMateria = this.spell.flowPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        boolean vitaeMitosisOrb = this.splitChild && this.spell.flowPart().mutationEnabled() && flowMateria == EssenceType.VITAE;

        if (vitaeMitosisOrb && caster != null) {
            List<LivingEntity> injuredAllies = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(8.5D),
                    e -> e.isAlive()
                            && SpellExecutionEngine.isFriendlyAllyForOasis(caster, e)
                            && !this.alreadyHitEntities.contains(e.getUUID())
                            && e.getHealth() < e.getMaxHealth() - 0.5F
            );
            if (!injuredAllies.isEmpty()) {
                LivingEntity lowestAlly = injuredAllies.get(0);
                float minHp = lowestAlly.getHealth();
                for (int i = 1; i < injuredAllies.size(); i++) {
                    LivingEntity cand = injuredAllies.get(i);
                    if (cand.getHealth() < minHp) {
                        minHp = cand.getHealth();
                        lowestAlly = cand;
                    }
                }
                Vec3 desired = lowestAlly.getEyePosition().subtract(this.position()).normalize().scale(speed);
                Vec3 blended = currentVel.scale(0.68D).add(desired.scale(0.32D)).normalize().scale(speed);
                this.setDeltaMovement(blended);
                this.hasImpulse = true;
                return;
            }
        }

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(vitaeMitosisOrb ? 8.5D : 7.5D),
                e -> e.isAlive()
                        && e != caster
                        && !this.alreadyHitEntities.contains(e.getUUID())
                        && !(this.sentryBoltMateria == EssenceType.UNDEAD && e.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD))
                        && (e instanceof net.minecraft.world.entity.monster.Enemy || (caster != null && e == caster.getLastHurtMob()))
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, this.spell.hasWardFriendlyFireSafeguard())
        );
        if (candidates.isEmpty()) return;

        LivingEntity chosen = candidates.get(0);
        if (vitaeMitosisOrb) {
            // Section 6.22 Cellular Mitosis Split Flow: each sub-orb seeks out the lowest-health nearby ally or hostile!
            float lowestHp = chosen.getHealth();
            double tieDist = chosen.distanceToSqr(this);
            for (int i = 1; i < candidates.size(); i++) {
                LivingEntity cand = candidates.get(i);
                float hp = cand.getHealth();
                double d = cand.distanceToSqr(this);
                if (hp < lowestHp - 0.1F || (Math.abs(hp - lowestHp) <= 0.1F && ((this.getId() & 1) == 0 ? d < tieDist : d > tieDist))) {
                    lowestHp = hp;
                    tieDist = d;
                    chosen = cand;
                }
            }
        } else {
            double bestDist = chosen.distanceToSqr(this);
            for (int i = 1; i < candidates.size(); i++) {
                double d = candidates.get(i).distanceToSqr(this);
                if (d < bestDist) {
                    bestDist = d;
                    chosen = candidates.get(i);
                }
            }
        }

        Vec3 desired = chosen.getEyePosition().subtract(this.position()).normalize().scale(speed);
        Vec3 blended = currentVel.scale(vitaeMitosisOrb ? 0.68D : 0.75D)
                .add(desired.scale(vitaeMitosisOrb ? 0.32D : 0.25D)).normalize().scale(speed);
        this.setDeltaMovement(blended);
        this.hasImpulse = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity hit = result.getEntity();
        LivingEntity caster = this.getOwner() instanceof LivingEntity l ? l : null;
        EssenceType flowMateria = this.spell.flowPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        boolean vitaeMitosisHeal = this.splitChild
                && this.spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.VITAE
                && hit instanceof LivingEntity hitLiv
                && caster != null
                && SpellExecutionEngine.isFriendlyAllyForOasis(caster, hitLiv)
                && hitLiv.getHealth() < hitLiv.getMaxHealth() - 0.5F;

        if ((hit == this.getOwner() && !vitaeMitosisHeal) || this.alreadyHitEntities.contains(hit.getUUID())) {
            return;
        }
        // Section 3.5 On-Block-Collision: bypass entity detonation so the carrier strikes world geometry at 100% potency!
        if (this.spell.flowPart().trigger() == SpellTriggerType.ON_BLOCK_COLLISION && !vitaeMitosisHeal) {
            return;
        }
        this.alreadyHitEntities.add(hit.getUUID());

        if (vitaeMitosisHeal && hit instanceof LivingEntity allyTarget) {
            float mitosisHeal = 2.5F + this.spell.getEffectiveTier() * 0.85F;
            SpellCombatReactionEngine.notifyMagicalHeal(allyTarget, mitosisHeal);
            allyTarget.heal(mitosisHeal);
            serverLevel.sendParticles(ParticleTypes.HEART, allyTarget.getX(), allyTarget.getY(0.9D), allyTarget.getZ(),
                    6, 0.25D, 0.25D, 0.25D, 0.03D);
            serverLevel.sendParticles(
                    ddraig.net.entropica.client.particle.TimedTintableParticleOption.linear(
                            ddraig.net.entropica.registry.ModParticles.TINTABLE_SPARKLE.get(), 0xFF6B9D, 20, 0.90F, 0.05F),
                    allyTarget.getX(), allyTarget.getY(0.85D), allyTarget.getZ(), 8, 0.30D, 0.30D, 0.30D, 0.03D
            );
            this.discard();
            return;
        }

        SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
        EssenceType formMateria = slot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
        Vec3 travelDir = this.getDeltaMovement().lengthSqr() > 1.0E-4D
                ? this.getDeltaMovement().normalize()
                : (caster != null ? caster.getLookAngle().normalize() : new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 entityImpactCenter = hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D);

        if (this.carrierPayload) {
            if (!consumeCarrierDynamicTriggerFee(serverLevel, caster)) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, hit.getX(), hit.getY(0.5D), hit.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.03D);
                this.discard();
                return;
            }

            // Section 3.3 Accelerated Fast: +35% bonus kinetic knockback impulse on hit!
            if (this.spell.flowPart().flow() == SpellFlowModifier.FAST_DART
                    && hit instanceof LivingEntity fastTarget
                    && !SpellCombatReactionEngine.isBossEntity(serverLevel, fastTarget)
                    && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                            fastTarget, caster, this.spell.hasWardFriendlyFireSafeguard()))) {
                fastTarget.push(travelDir.x * 0.45D, 0.16D, travelDir.z * 0.45D);
                fastTarget.hurtMarked = true;
            }

            // Section 3.5 Corpse Bloom (On-Death Trigger): plant 3s death-mark FIRST (so lethal hits still trigger the full Multi-Cast bloom!),
            // then deliver Slot 0 onto the struck target.
            if (this.spell.flowPart().trigger() == SpellTriggerType.ON_DEATH && hit instanceof LivingEntity corpseTarget) {
                SpellExecutionEngine.markEntityForCorpseBloom(
                        serverLevel,
                        corpseTarget,
                        caster,
                        this.spell,
                        this.fallbackMateria,
                        this.apparatusPotencyMult,
                        this.apparatusRangeMult
                );
                SpellExecutionEngine.deliverSubSpellOnEntity(
                        serverLevel,
                        caster,
                        hit,
                        travelDir,
                        this.spell,
                        slot,
                        this.fallbackMateria,
                        this.apparatusPotencyMult
                );
                if (slot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.PROJECTILE && formHandler != null) {
                    formHandler.onProjectileHitEntityFromForm(serverLevel, this, caster, hit, this.spell, slot);
                }
            } else {
                SpellExecutionEngine.executeContactMultiCast(
                        serverLevel,
                        caster,
                        this,
                        entityImpactCenter,
                        hit,
                        hit.blockPosition(),
                        net.minecraft.core.Direction.UP,
                        travelDir,
                        this.spell,
                        this.fallbackMateria,
                        this.apparatusPotencyMult,
                        this.apparatusRangeMult
                );
            }
        } else {
            SpellExecutionEngine.deliverSubSpellOnEntity(
                    serverLevel,
                    caster,
                    hit,
                    travelDir,
                    this.spell,
                    slot,
                    this.fallbackMateria,
                    this.apparatusPotencyMult
            );
            if (formHandler != null) {
                formHandler.onProjectileHitEntityFromForm(serverLevel, this, caster, hit, this.spell, slot);
            }
        }

        MateriaPartMutationHandler hitFlowHandler = SpellMutationHandlerRegistry.resolveActive(flowMateria, this.spell.flowPart().mutationEnabled());
        if (hitFlowHandler != null) {
            hitFlowHandler.onProjectileHitEntityFromFlow(serverLevel, this, caster, hit, this.spell, slot);
        }

        // Vapor Hydro-Steam Split Flow (#29): secondary split steam bursts blind struck foes even when Form is unmutated or mixed-Materia!
        if (this.splitChild
                && this.spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.VAPOR
                && hit instanceof LivingEntity vaporSecondary
                && vaporSecondary.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(vaporSecondary, caster, this.spell.hasWardFriendlyFireSafeguard()))) {
            vaporSecondary.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 60, 0));
            serverLevel.sendParticles(ParticleTypes.CLOUD, vaporSecondary.getX(), vaporSecondary.getY(0.8D), vaporSecondary.getZ(),
                    10, 0.3D, 0.35D, 0.3D, 0.03D);
        }

        applySentryBoltImpact(serverLevel, caster, hit.position(), hit);

        // Released Orbiting Dart impacts (#40 Penumbra 3s blind, #41 Rime 2s root, #42 Spring 2s root + ally mend, #43 Static 2s root + 3-foe arc)
        if (!this.sporeOrbitalActive
                && this.spell.flowPart().flow() == SpellFlowModifier.ORBIT_SATELLITE
                && this.spell.flowPart().mutationEnabled()
                && hit instanceof LivingEntity orbitTarget
                && orbitTarget.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(orbitTarget, caster, this.spell.hasWardFriendlyFireSafeguard()))) {
            switch (flowMateria) {
                case PENUMBRA -> {
                    int dTier = Math.max(1, this.spell.getEffectiveTier());
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                            serverLevel, orbitTarget, caster, 2.25F + dTier * 0.55F, false, true
                    );
                }
                case RIME -> {
                    int dTier = Math.max(1, this.spell.getEffectiveTier());
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                    orbitTarget.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                    SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(
                            serverLevel, orbitTarget, caster, 2.25F + dTier * 0.55F, false, true
                    );
                }
                case SPRING -> {
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                    orbitTarget.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                    if (caster != null) {
                        for (LivingEntity ally : serverLevel.getEntitiesOfClass(LivingEntity.class, orbitTarget.getBoundingBox().inflate(4.0D))) {
                            if (ally.isAlive() && (ally == caster || ally.isAlliedTo(caster))) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ally, 2.0F);
                                ally.heal(2.0F);
                            }
                        }
                    }
                }
                case STATIC -> {
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                    orbitTarget.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                    int shocked = 0;
                    for (LivingEntity adj : serverLevel.getEntitiesOfClass(LivingEntity.class, orbitTarget.getBoundingBox().inflate(5.0D))) {
                        if (adj == orbitTarget || adj == caster || !adj.isAlive()) continue;
                        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, this.spell.hasWardFriendlyFireSafeguard())) continue;
                        adj.hurtServer(serverLevel, serverLevel.damageSources().lightningBolt(), 2.5F);
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, adj.getX(), adj.getY(0.7D), adj.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.06D);
                        if (++shocked >= 3) break;
                    }
                }
                case AETHER -> SpellCombatReactionEngine.applyTimedAttributeShred(orbitTarget, serverLevel, 2.0D, 0.0D, 100L);
                case CELESTIAL -> {
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    if (caster != null) {
                        SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.0F);
                        caster.heal(2.0F);
                    }
                }
                case PRISMATIC -> {
                    SpellCombatReactionEngine.applyTimedAttributeShred(orbitTarget, serverLevel, 2.0D, 0.0D, 100L);
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                }
                case EMPYREAN -> {
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                    orbitTarget.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                    SpellCombatReactionEngine.applyTimedAttributeShred(orbitTarget, serverLevel, 3.0D, 0.0D, 120L);
                    SpellLingeringZoneManager.grantPartyAbsorption(serverLevel, caster, 3.0F, 36.0F, 10.0D);
                }
                case CATACLYSM -> {
                    SpellCombatReactionEngine.applyPercentageArmorShred(orbitTarget, serverLevel, 0.0D, 0.40D, 120L);
                    orbitTarget.igniteForSeconds(4);
                    EntityHelper.getPersistentData(orbitTarget).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, serverLevel.getGameTime() + 80L);
                }
                case TERMINUS -> {
                    SpellCombatReactionEngine.applyPercentageArmorShred(orbitTarget, serverLevel, 0.0D, 0.40D, 120L);
                    SpellCombatReactionEngine.applyTimedAttackDebuff(orbitTarget, serverLevel, 0.0D, 0.40D, 80L);
                }
                case ESCHATON -> {
                    SpellCombatReactionEngine.applyPercentageArmorShred(orbitTarget, serverLevel, 0.0D, 0.50D, 140L);
                    orbitTarget.hurtServer(serverLevel, serverLevel.damageSources().fellOutOfWorld(), 4.0F);
                }
                case APOTHEOSIS -> {
                    SpellCombatReactionEngine.applyPercentageArmorShred(orbitTarget, serverLevel, 0.0D, 0.50D, 140L);
                    orbitTarget.igniteForSeconds(4);
                    if (caster != null) {
                        SpellCombatReactionEngine.notifyMagicalHeal(caster, 3.0F);
                        caster.heal(3.0F);
                    }
                }
                case ENTROPICA -> {
                    SpellCombatReactionEngine.applyPercentageArmorShred(orbitTarget, serverLevel, 0.0D, 0.50D, 140L);
                    orbitTarget.hurtServer(serverLevel, serverLevel.damageSources().fellOutOfWorld(), 4.0F);
                }
                default -> {
                }
            }
        }

        // Section 6.30 Hydro-Steam Split Flow: split steam bursts blind secondary enemies on hit!
        if (this.splitChild
                && this.spell.flowPart().mutationEnabled()
                && flowMateria == EssenceType.VAPOR
                && hit instanceof LivingEntity steamTarget
                && steamTarget.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(steamTarget, caster, this.spell.hasWardFriendlyFireSafeguard()))) {
            steamTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applySoaked(steamTarget, 120);
            serverLevel.sendParticles(ParticleTypes.CLOUD, steamTarget.getX(), steamTarget.getY(0.8D), steamTarget.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.04D);
        }

        EssenceType vPrimary = this.getVisualEssenceType();
        EssenceType vSecondary = this.getVisualSecondaryEssenceType();
        boolean umbralSnareHit = slot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.PROJECTILE
                && slot.formPart().mutationEnabled()
                && formMateria == EssenceType.UMBRAL;
        if (!umbralSnareHit) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                    serverLevel,
                    entityImpactCenter,
                    this.getDeltaMovement().scale(-1.0D),
                    vPrimary,
                    vSecondary,
                    this.splitChild ? 0.65F : 0.95F,
                    this.getVisualPotencyTier()
            );
        }
        if (!isSilentStealthProjectile()) {
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                    serverLevel,
                    entityImpactCenter,
                    vPrimary,
                    vSecondary,
                    this.splitChild ? 0.8D : 1.4D,
                    this.getVisualPotencyTier()
            );
        }

        tryTriggerFlowSplit(serverLevel, caster, entityImpactCenter, slot);

        boolean miasmaOrOblivionProjChain = this.spell.flowPart().mutationEnabled()
                && (flowMateria == EssenceType.MIASMA || flowMateria == EssenceType.OBLIVION || flowMateria == EssenceType.ENTROPIC);
        if ((this.spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP || miasmaOrOblivionProjChain)
                && hit instanceof LivingEntity livingHit
                && caster != null) {
            if (this.carrierPayload && this.spell.flowPart().trigger() != SpellTriggerType.ON_DEATH) {
                SpellExecutionEngine.chainLeapMultiCastFrom(
                        serverLevel,
                        caster,
                        livingHit,
                        this.spell,
                        this.fallbackMateria,
                        this.apparatusPotencyMult,
                        this.apparatusRangeMult
                );
            } else {
                SpellExecutionEngine.chainLeapFrom(
                        serverLevel,
                        caster,
                        livingHit,
                        this.spell,
                        slot,
                        this.fallbackMateria,
                        this.apparatusPotencyMult
                );
            }
        }

        // Sec 6.53 Prismatic Oscillating Light-Disc: up to 6 ricochets off entities & solid blocks (-4 armor per bounce, 5-heart party heal on final impact!)
        if (slot.formPart().mutationEnabled() && formMateria == EssenceType.PRISMATIC) {
            if (hit instanceof LivingEntity hitLiv) {
                SpellCombatReactionEngine.applyTimedAttributeShred(hitLiv, serverLevel, 4.0D, 0.0D, 120L);
            }
            if (this.remainingBounces > 0) {
                this.remainingBounces--;
                this.maxFlightTicks = Math.max(this.maxFlightTicks, this.tickCount + 30);
                redirectTowardNextUnhitHostileOrReflect(serverLevel, caster, this.getDeltaMovement().scale(-1.0D));
                return;
            } else {
                healWoundedPartyOnPrismaticFinalImpact(serverLevel, caster, hit.position());
                this.discard();
                return;
            }
        }

        if (this.remainingPierces > 0) {
            this.remainingPierces--;
            if (slot.formPart().mutationEnabled() && formMateria == EssenceType.APOTHEOSIS) {
                this.maxFlightTicks = Math.max(this.maxFlightTicks, this.tickCount + 25);
                redirectTowardNextUnhitHostileOrReflect(serverLevel, caster, this.getDeltaMovement());
            }
        } else {
            this.discard();
        }
    }

    private void redirectTowardNextUnhitHostileOrReflect(ServerLevel level, @Nullable LivingEntity caster, Vec3 fallbackDir) {
        double speed = Math.max(0.85D, this.getDeltaMovement().length());
        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(9.0D),
                e -> e.isAlive()
                        && e != caster
                        && !this.alreadyHitEntities.contains(e.getUUID())
                        && (e instanceof Monster || (caster != null && e == caster.getLastHurtMob()))
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, this.spell.hasWardFriendlyFireSafeguard())
        );
        if (!candidates.isEmpty()) {
            LivingEntity next = candidates.get(0);
            Vec3 aim = next.getEyePosition().subtract(this.position()).normalize().scale(speed);
            this.setDeltaMovement(aim);
        } else if (fallbackDir.lengthSqr() > 1.0E-4D) {
            this.setDeltaMovement(fallbackDir.normalize().scale(speed));
        }
        this.hasImpulse = true;
    }

    private static void healWoundedPartyOnPrismaticFinalImpact(ServerLevel level, @Nullable LivingEntity caster, Vec3 impactPos) {
        level.sendParticles(ParticleTypes.END_ROD, impactPos.x, impactPos.y + 0.5D, impactPos.z, 22, 0.8D, 0.5D, 0.8D, 0.06D);
        if (caster == null) return;
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(10.0D), LivingEntity::isAlive)) {
            if (ally instanceof Monster && ally != caster) continue;
            if (ally == caster || ally.isAlliedTo(caster)
                    || ally instanceof net.minecraft.world.entity.player.Player
                    || (ally instanceof ddraig.net.entropica.entity.SpellEidolonEntity eid && eid.isFriendlyTo(caster))) {
                SpellCombatReactionEngine.notifyMagicalHeal(ally, 10.0F);
                ally.heal(10.0F);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity caster = this.getOwner() instanceof LivingEntity l ? l : null;
        SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
        EssenceType formMateria = slot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
        Vec3 surfaceNormal = Vec3.atLowerCornerOf(result.getDirection().getUnitVec3i());
        Vec3 travelDir = this.getDeltaMovement().lengthSqr() > 1.0E-4D
                ? this.getDeltaMovement().normalize()
                : (caster != null ? caster.getLookAngle().normalize() : new Vec3(0.0D, 0.0D, 1.0D));

        boolean entityTriggerBlockPenalty = this.spell.flowPart().trigger() == SpellTriggerType.ON_ENTITY_HIT
                && SpellExecutionEngine.shouldPenalizeEntityTriggerBlockFallback(this.spell);
        float blockImpactPotency = entityTriggerBlockPenalty
                ? (this.apparatusPotencyMult * 0.75F)
                : this.apparatusPotencyMult;

        boolean honorBlockTrigger = this.spell.flowPart().trigger() == SpellTriggerType.ON_BLOCK_COLLISION
                || this.spell.flowPart().trigger() == SpellTriggerType.ON_TIMER;
        boolean voidThinObstaclePhase = !honorBlockTrigger
                && slot.formPart().form() == SpellDeliveryForm.PROJECTILE
                && slot.formPart().mutationEnabled()
                && formMateria == EssenceType.VOID
                && ddraig.net.entropica.spell.mutation.element.VoidSpellMutationHandler.isThinVoidPassableObstacle(
                        serverLevel, result.getBlockPos(), serverLevel.getBlockState(result.getBlockPos()), travelDir);
        boolean groundCometImpact = this.slightFluidArc && result.getDirection() == net.minecraft.core.Direction.UP;
        boolean effectiveWallPhase = ((this.wallPhasing && !honorBlockTrigger) || voidThinObstaclePhase) && !groundCometImpact;
        if (voidThinObstaclePhase && !this.wallPhasing) {
            // Section 6.11 Singularity Rift Orb: phases cleanly through thin obstacles (panes, bars, fences, gates, leaves, thin partitions)
            // without premature detonation!
            Vec3 nudge = travelDir.scale(1.18D);
            this.setPos(this.getX() + nudge.x, this.getY() + nudge.y, this.getZ() + nudge.z);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    result.getLocation().x, result.getLocation().y, result.getLocation().z,
                    8, 0.16D, 0.16D, 0.16D, 0.03D);
            return;
        }

        if (this.carrierPayload) {
            if (effectiveWallPhase) {
                Vec3 nudge = travelDir.scale(0.65D);
                this.setPos(this.getX() + nudge.x, this.getY() + nudge.y, this.getZ() + nudge.z);
                return;
            }
            if (this.remainingBounces > 0) {
                this.remainingBounces--;
                this.maxFlightTicks = Math.max(this.maxFlightTicks, this.tickCount + 30);
                Vec3 v = this.getDeltaMovement();
                Vec3 reflected = v.subtract(surfaceNormal.scale(2.0D * v.dot(surfaceNormal)));
                Vec3 hitLoc = result.getLocation().add(surfaceNormal.scale(0.22D));
                this.setPos(hitLoc.x, hitLoc.y, hitLoc.z);
                this.setDeltaMovement(reflected);
                this.hasImpulse = true;
                if (!isSilentStealthProjectile()) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, hitLoc.x, hitLoc.y, hitLoc.z, 8, 0.1D, 0.1D, 0.1D, 0.04D);
                    serverLevel.playSound(null, result.getBlockPos(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.4F);
                }
                return;
            }
            // Section 3.5 Timed Delay: anchor on struck surface until fuse reaches zero!
            if (this.spell.flowPart().trigger() == SpellTriggerType.ON_TIMER && this.tickCount < this.delayedFuseTicks) {
                Vec3 anchorPos = result.getLocation().add(surfaceNormal.scale(0.18D));
                this.setPos(anchorPos.x, anchorPos.y, anchorPos.z);
                this.setDeltaMovement(Vec3.ZERO);
                this.anchoredForDelayedBloom = true;
                this.anchoredBlockPos = result.getBlockPos().immutable();
                this.anchoredFace = result.getDirection();
                if (!isSilentStealthProjectile()) {
                    serverLevel.playSound(null, result.getBlockPos(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.75F, 1.45F);
                }
                return;
            }

            applySentryBoltImpact(serverLevel, caster, result.getLocation(), null);
            EssenceType vPrimary = this.getVisualEssenceType();
            EssenceType vSecondary = this.getVisualSecondaryEssenceType();
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                    serverLevel,
                    result.getLocation(),
                    surfaceNormal,
                    vPrimary,
                    vSecondary,
                    this.splitChild ? 0.65F : 1.0F,
                    this.getVisualPotencyTier()
            );
            if (!isSilentStealthProjectile()) {
                ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                        serverLevel,
                        result.getLocation(),
                        vPrimary,
                        vSecondary,
                        this.splitChild ? 0.8D : 1.5D,
                        this.getVisualPotencyTier()
                );
            }
            if (slot.formPart().form() == ddraig.net.entropica.spell.SpellDeliveryForm.PROJECTILE && formHandler != null) {
                formHandler.onProjectileHitBlockFromForm(serverLevel, this, caster, result, this.spell, slot);
            }
            if (slot.formPart().mutationEnabled() && formMateria == EssenceType.PRISMATIC) {
                healWoundedPartyOnPrismaticFinalImpact(serverLevel, caster, result.getLocation());
            }
            triggerCarrierContactAndDiscard(
                    serverLevel,
                    caster,
                    result.getLocation(),
                    null,
                    result.getBlockPos(),
                    result.getDirection(),
                    travelDir,
                    blockImpactPotency
            );
            return;
        }

        SpellExecutionEngine.deliverSubSpellOnBlock(
                serverLevel,
                caster,
                result.getBlockPos(),
                result.getDirection(),
                this.spell,
                slot,
                this.fallbackMateria,
                blockImpactPotency
        );

        // Section 3.5 Ground Collision Fallback for direct Single-Cast Projectiles (75% splash within 2.2m on near-miss!)
        boolean skipSplashSelfBlink = slot.primaryEffect().effect() == ddraig.net.entropica.spell.SpellCoreEffect.BLINK
                && !slot.primaryEffect().subMode().equalsIgnoreCase("swap")
                && !slot.primaryEffect().subMode().equalsIgnoreCase("transposition")
                && slot.secondaryEffect().isEmpty();
        if (entityTriggerBlockPenalty && !effectiveWallPhase && !skipSplashSelfBlink) {
            Vec3 hitPos = result.getLocation();
            net.minecraft.world.phys.AABB splashBox = new net.minecraft.world.phys.AABB(hitPos, hitPos).inflate(2.2D);
            for (LivingEntity splashTarget : serverLevel.getEntitiesOfClass(LivingEntity.class, splashBox, LivingEntity::isAlive)) {
                if (splashTarget == caster) continue;
                if (splashTarget.position().distanceToSqr(hitPos) > 4.84D) continue;
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        splashTarget, caster, this.spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                Vec3 outDir = splashTarget.position().distanceToSqr(hitPos) > 1.0E-4D
                        ? splashTarget.position().subtract(hitPos).normalize()
                        : travelDir;
                SpellExecutionEngine.deliverSubSpellOnEntity(
                        serverLevel, caster, splashTarget, outDir, this.spell, slot, this.fallbackMateria, blockImpactPotency
                );
            }
        }

        if (formHandler != null) {
            formHandler.onProjectileHitBlockFromForm(serverLevel, this, caster, result, this.spell, slot);
        }

        applySentryBoltImpact(serverLevel, caster, result.getLocation(), null);

        EssenceType vPrimary = this.getVisualEssenceType();
        EssenceType vSecondary = this.getVisualSecondaryEssenceType();
        if (!effectiveWallPhase) {
            ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                    serverLevel,
                    result.getLocation(),
                    surfaceNormal,
                    vPrimary,
                    vSecondary,
                    this.splitChild ? 0.65F : 1.0F,
                    this.getVisualPotencyTier()
            );
            ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                    serverLevel,
                    result.getLocation(),
                    vPrimary,
                    vSecondary,
                    this.splitChild ? 0.8D : 1.5D,
                    this.getVisualPotencyTier()
            );
        }

        if (effectiveWallPhase) {
            // Advance slightly through the block boundary so spectral / void-phased bolts keep flying
            Vec3 nudge = travelDir.scale(0.65D);
            this.setPos(this.getX() + nudge.x, this.getY() + nudge.y, this.getZ() + nudge.z);
        } else if (this.remainingBounces > 0) {
            // Sec 6.53 Prismatic Oscillating Light-Disc solid-block ricochet!
            this.remainingBounces--;
            this.maxFlightTicks = Math.max(this.maxFlightTicks, this.tickCount + 30);
            Vec3 v = this.getDeltaMovement();
            Vec3 n = Vec3.atLowerCornerOf(result.getDirection().getUnitVec3i());
            Vec3 reflected = v.subtract(n.scale(2.0D * v.dot(n)));
            Vec3 hitLoc = result.getLocation().add(n.scale(0.22D));
            this.setPos(hitLoc.x, hitLoc.y, hitLoc.z);
            this.setDeltaMovement(reflected);
            this.hasImpulse = true;
            if (!isSilentStealthProjectile()) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, hitLoc.x, hitLoc.y, hitLoc.z, 8, 0.1D, 0.1D, 0.1D, 0.04D);
                serverLevel.playSound(null, result.getBlockPos(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.4F);
            }
        } else {
            if (slot.formPart().mutationEnabled() && formMateria == EssenceType.PRISMATIC) {
                healWoundedPartyOnPrismaticFinalImpact(serverLevel, caster, result.getLocation());
            }
            tryTriggerFlowSplit(serverLevel, caster, result.getLocation(), slot);
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("SpellAssembly", SpellAssembly.CODEC, this.spell);
        output.store("SlotIndex", Codec.INT, this.slotIndex);
        if (this.fallbackMateria != null) {
            output.store("FallbackMateria", EssenceType.CODEC, this.fallbackMateria);
        }
        output.store("ApparatusPotencyMult", Codec.FLOAT, this.apparatusPotencyMult);
        output.store("ApparatusRangeMult", Codec.FLOAT, this.apparatusRangeMult);
        output.store("CarrierPayload", Codec.BOOL, this.carrierPayload);
        output.store("DelayedFuseTicks", Codec.INT, this.delayedFuseTicks);
        output.store("AnchoredDelayedBloom", Codec.BOOL, this.anchoredForDelayedBloom);
        output.store("DynamicFeeCharged", Codec.BOOL, this.dynamicFeeCharged);
        output.store("RemainingPierces", Codec.INT, this.remainingPierces);
        output.store("RemainingBounces", Codec.INT, this.remainingBounces);
        output.store("WallPhasing", Codec.BOOL, this.wallPhasing);
        output.store("HomingGuidance", Codec.BOOL, this.homingGuidance);
        output.store("SplitChild", Codec.BOOL, this.splitChild);
        output.store("FlowSplitTriggered", Codec.BOOL, this.flowSplitTriggered);
        output.store("MaxFlightTicks", Codec.INT, this.maxFlightTicks);
        output.store("OriginInitialized", Codec.BOOL, this.originInitialized);
        output.store("OriginX", Codec.DOUBLE, this.originX);
        output.store("OriginY", Codec.DOUBLE, this.originY);
        output.store("OriginZ", Codec.DOUBLE, this.originZ);
        output.store("MaxTravelDistance", Codec.DOUBLE, this.maxTravelDistance);
        output.store("CruiseSpeed", Codec.DOUBLE, this.cruiseSpeed);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("SpellAssembly", SpellAssembly.CODEC).ifPresent(s -> this.spell = s);
        this.slotIndex = input.read("SlotIndex", Codec.INT).orElse(0);
        this.fallbackMateria = input.read("FallbackMateria", EssenceType.CODEC).orElse(EssenceType.REGULAR);
        this.apparatusPotencyMult = input.read("ApparatusPotencyMult", Codec.FLOAT).orElse(1.0F);
        this.apparatusRangeMult = input.read("ApparatusRangeMult", Codec.FLOAT).orElse(1.0F);
        this.carrierPayload = input.read("CarrierPayload", Codec.BOOL).orElse(false);
        this.delayedFuseTicks = input.read("DelayedFuseTicks", Codec.INT).orElse(0);
        this.anchoredForDelayedBloom = input.read("AnchoredDelayedBloom", Codec.BOOL).orElse(false);
        this.dynamicFeeCharged = input.read("DynamicFeeCharged", Codec.BOOL).orElse(false);
        this.remainingPierces = input.read("RemainingPierces", Codec.INT).orElse(0);
        this.remainingBounces = input.read("RemainingBounces", Codec.INT).orElse(0);
        this.wallPhasing = input.read("WallPhasing", Codec.BOOL).orElse(false);
        this.homingGuidance = input.read("HomingGuidance", Codec.BOOL).orElse(false);
        this.splitChild = input.read("SplitChild", Codec.BOOL).orElse(false);
        this.flowSplitTriggered = input.read("FlowSplitTriggered", Codec.BOOL).orElse(false);
        this.maxFlightTicks = input.read("MaxFlightTicks", Codec.INT).orElse(60);
        this.originInitialized = input.read("OriginInitialized", Codec.BOOL).orElse(false);
        this.originX = input.read("OriginX", Codec.DOUBLE).orElse(0.0D);
        this.originY = input.read("OriginY", Codec.DOUBLE).orElse(0.0D);
        this.originZ = input.read("OriginZ", Codec.DOUBLE).orElse(0.0D);
        this.maxTravelDistance = input.read("MaxTravelDistance", Codec.DOUBLE).orElse(16.0D);
        this.cruiseSpeed = input.read("CruiseSpeed", Codec.DOUBLE).orElse(0.0D);
    }
}
