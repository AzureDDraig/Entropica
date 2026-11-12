package ddraig.net.entropica.entity.projectile;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellPrefix;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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

    private final java.util.ArrayDeque<Vec3> clientTrailPoints = new java.util.ArrayDeque<>(16);

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
    private float orbitPhaseRadians = 0.0F;
    @Nullable
    private EssenceType sentryBoltMateria = null;
    private int maxFlightTicks = 60;
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
        boolean voidFlowPhase = spell.flowPart().flow() == SpellFlowModifier.VOID_WARP
                && flowMutation.mutated()
                && flowMateria == EssenceType.VOID;
        this.wallPhasing = ((formMutation.mutated() && formMutation.wallPhasing()) || voidFlowPhase)
                && spell.prefix() != SpellPrefix.UTILITY;
        this.homingGuidance = (flowMutation.mutated() && (flowMateria == EssenceType.ASTRAL
                || flowMateria == EssenceType.ECLIPSE
                || flowMateria == EssenceType.CELESTIAL
                || flowMateria == EssenceType.WRAITH
                || flowMateria == EssenceType.APOTHEOSIS
                || flowMateria == EssenceType.ENTROPICA))
                || (formMutation.mutated() && (formMateria == EssenceType.ECLIPSE
                || formMateria == EssenceType.SOULFIRE
                || formMateria == EssenceType.WRAITH
                || formMateria == EssenceType.APOTHEOSIS));

        double maxBlocks = SpellDeliveryForm.getBeamReachBlocks(spell.getEffectiveTier(), false) * 1.5D
                * spell.flawProfile().getRangeAndRadiusMultiplier();
        this.maxFlightTicks = Math.max(15, (int) Math.ceil(maxBlocks / 1.15D));

        if (!this.splitChild
                && spell.flowPart().flow() == SpellFlowModifier.ORBIT_SATELLITE
                && flowMutation.mutated()
                && (flowMateria == EssenceType.SPORE
                || flowMateria == EssenceType.NULL_R
                || flowMateria == EssenceType.NULL_U
                || flowMateria == EssenceType.DAWN
                || flowMateria == EssenceType.ABYSS
                || flowMateria == EssenceType.AEGIS
                || flowMateria == EssenceType.AURORA
                || flowMateria == EssenceType.MIRAGE
                || flowMateria == EssenceType.WRAITH
                || flowMateria == EssenceType.BARROW
                || flowMateria == EssenceType.PYRE
                || flowMateria == EssenceType.PENUMBRA
                || flowMateria == EssenceType.RIME
                || flowMateria == EssenceType.SPRING
                || flowMateria == EssenceType.STATIC
                || flowMateria == EssenceType.SYLVAN
                || flowMateria == EssenceType.GENESIS
                || flowMateria == EssenceType.CHIMERA
                || flowMateria == EssenceType.AETHER
                || flowMateria == EssenceType.CELESTIAL
                || flowMateria == EssenceType.PRISMATIC
                || flowMateria == EssenceType.EMPYREAN
                || flowMateria == EssenceType.CATACLYSM
                || flowMateria == EssenceType.TERMINUS
                || flowMateria == EssenceType.ESCHATON
                || flowMateria == EssenceType.APOTHEOSIS
                || flowMateria == EssenceType.ENTROPICA)) {
            this.sporeOrbitalActive = true;
            this.orbitPhaseRadians = (float) ((this.getId() % 8) * (Math.PI * 0.25D));
            this.maxFlightTicks = Math.max(this.maxFlightTicks + 40, 88);
        }

        EssenceType primaryEffType = slot.primaryEffect().resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria);
        EssenceType tintType = formMateria != EssenceType.REGULAR ? formMateria : primaryEffType;
        EssenceType secondaryType = slot.secondaryEffect()
                .map(s -> s.resolveEffectiveMateria(spell.materiaSpecific(), fallbackMateria))
                .orElse(primaryEffType != tintType ? primaryEffType : null);

        boolean fastLance = spell.flowPart().flow() == SpellFlowModifier.FAST_DART
                || (formMutation.mutated() && formMutation.velocityMultiplier() >= 1.35F);
        boolean prismaticDisc = formMutation.mutated() && formMateria == EssenceType.PRISMATIC;
        byte archetype = ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(tintType, fastLance, prismaticDisc);

        long gt = this.level() != null ? this.level().getGameTime() : 0L;
        this.entityData.set(DATA_COLOR_TINT, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantPrimaryRgb(tintType, gt));
        this.entityData.set(DATA_ACCENT_COLOR, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantAccentRgb(tintType, secondaryType, gt));
        this.entityData.set(DATA_ESSENCE_ORDINAL, tintType.ordinal());
        this.entityData.set(DATA_SECONDARY_ORDINAL, secondaryType != null ? secondaryType.ordinal() : -1);
        this.entityData.set(DATA_ARCHETYPE, archetype);
        this.entityData.set(DATA_POTENCY_TIER, (byte) Math.clamp(spell.getEffectiveTier(), 1, 10));
        syncVisualFlags(formMutation.mutated() || flowMutation.mutated() || slot.primaryEffect().mutationEnabled());
    }

    private void syncVisualFlags(boolean anyMutated) {
        byte flags = 0;
        if (anyMutated) flags |= VFLAG_MUTATED;
        if (this.sporeOrbitalActive) flags |= VFLAG_ORBITAL;
        if (this.splitChild) flags |= VFLAG_SPLIT_CHILD;
        if (this.wallPhasing) flags |= VFLAG_WALL_PHASING;
        if (this.homingGuidance) flags |= VFLAG_HOMING;
        this.entityData.set(DATA_VISUAL_FLAGS, flags);
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

    public void setWallPhasing(boolean wallPhasing) {
        this.wallPhasing = wallPhasing;
    }

    public boolean isWallPhasing() {
        return this.wallPhasing;
    }

    public void setHomingGuidance(boolean homingGuidance) {
        this.homingGuidance = homingGuidance;
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

    public boolean isSplitChild() {
        return this.splitChild;
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
            proj.maxFlightTicks = Math.max(proj.tickCount + 45, proj.maxFlightTicks);
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
            this.entityData.set(DATA_ARCHETYPE, ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveProjectileArchetype(sentryBoltMateria, false, false));
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
        level.addFreshEntity(child);
        return child;
    }

    @Override
    public void tick() {
        if (this.wallPhasing) {
            this.noPhysics = false;
        }
        super.tick();

        if (this.level().isClientSide()) {
            Vec3 curPos = this.position();
            if (this.clientTrailPoints.isEmpty() || this.clientTrailPoints.peekFirst().distanceToSqr(curPos) > 0.0025D) {
                this.clientTrailPoints.addFirst(curPos);
                while (this.clientTrailPoints.size() > 14) {
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
            if (this.sporeOrbitalActive) {
                tickSporeOrbitalSpiral(serverLevel);
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
        }

        if (!this.sporeOrbitalActive && this.homingGuidance && this.tickCount % 2 == 0) {
            steerTowardNearestHostile();
        }

        if (this.tickCount >= this.maxFlightTicks) {
            if (this.level() instanceof ServerLevel serverLevel) {
                SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
                LivingEntity caster = this.getOwner() instanceof LivingEntity l ? l : null;
                tryTriggerFlowSplit(serverLevel, caster, this.position(), slot);

                // Trigger open-air effects (Blink, Conjure, Gravity, Atmosphere, Sonar, Acoustic, Logistics, Diagnostic)
                // when a bolt reaches its maximum range in the open sky without hitting a solid block!
                if (SpellExecutionEngine.hasOpenAirBlockVerb(slot)
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
                    case SPORE -> {
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
                        SpellLingeringZoneManager.applySoaked(meleeFoe, 120);
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                    }
                    case AEGIS -> {
                        SpellCombatReactionEngine.applyTimedAttributeShred(meleeFoe, level, 2.0D, 0.0D, 80L);
                        Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(0.55D);
                        meleeFoe.setDeltaMovement(away.x, 0.22D, away.z);
                        meleeFoe.hasImpulse = true;
                    }
                    case AURORA -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                        meleeFoe.setTicksFrozen(Math.min(meleeFoe.getTicksRequiredToFreeze() + 20, meleeFoe.getTicksFrozen() + 20));
                    }
                    case MIRAGE -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                        SpellLingeringZoneManager.applyMirageTraitorFrenzy(meleeFoe, 50);
                    }
                    case WRAITH -> meleeFoe.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0));
                    case BARROW -> {
                        SpellCombatReactionEngine.applyTimedAttributeShred(meleeFoe, level, 2.0D, 0.0D, 80L);
                        Vec3 away = meleeFoe.position().subtract(caster.position()).normalize().scale(0.65D);
                        meleeFoe.setDeltaMovement(away.x, 0.24D, away.z);
                        meleeFoe.hasImpulse = true;
                    }
                    case PYRE -> meleeFoe.igniteForSeconds(4);
                    case PENUMBRA -> meleeFoe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    case RIME -> {
                        meleeFoe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                        meleeFoe.setTicksFrozen(Math.min(meleeFoe.getTicksRequiredToFreeze() + 30, meleeFoe.getTicksFrozen() + 30));
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

        // After spiraling tightly around the caster, fling outward toward nearby hostiles (or radially at tick 36)!
        if (this.tickCount >= 18) {
            List<LivingEntity> targets = level.getEntitiesOfClass(
                    LivingEntity.class,
                    caster.getBoundingBox().inflate(8.5D),
                    e -> e.isAlive()
                            && e != caster
                            && (e instanceof Monster || e == caster.getLastHurtMob())
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, this.spell.hasWardFriendlyFireSafeguard())
            );
            if (!targets.isEmpty() || this.tickCount >= 36) {
                this.sporeOrbitalActive = false;
                this.homingGuidance = true;
                Vec3 flingDir;
                if (!targets.isEmpty()) {
                    LivingEntity tgt = targets.get(0);
                    flingDir = tgt.getEyePosition().subtract(this.position()).normalize();
                } else {
                    Vec3 radial = this.position().subtract(caster.getEyePosition());
                    flingDir = radial.lengthSqr() > 0.01D ? radial.normalize() : caster.getLookAngle().normalize();
                }
                this.setDeltaMovement(flingDir.scale(1.45D));
                this.hasImpulse = true;
                level.sendParticles(flowMateria == EssenceType.SPORE ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.END_ROD,
                        this.getX(), this.getY(), this.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
                return;
            }
        }

        double angle = this.orbitPhaseRadians + (this.tickCount * 0.30D);
        double radius = 1.95D + 0.25D * Math.sin(this.tickCount * 0.20D);
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
                level.sendParticles(ParticleTypes.SPLASH, impactPos.x, impactPos.y + 0.3D, impactPos.z, 14, 0.6D, 0.3D, 0.6D, 0.08D);
                BlockPos hitPos = BlockPos.containing(impactPos);
                for (BlockPos p : BlockPos.betweenClosed(hitPos.offset(-1, -1, -1), hitPos.offset(1, 1, 1))) {
                    if (level.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                        level.removeBlock(p, false);
                    }
                }
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.0D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.clearFire();
                    SpellLingeringZoneManager.applySoaked(foe, 140);
                    foe.setDeltaMovement(travel.x * 0.75D, 0.25D, travel.z * 0.75D);
                    foe.hasImpulse = true;
                }
            }
            case OASIS -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.clearFire();
                    SpellLingeringZoneManager.applySoaked(foe, 140);
                }
            }
            case RADIANT -> {
                level.sendParticles(ParticleTypes.END_ROD, impactPos.x, impactPos.y + 0.3D, impactPos.z, 8, 0.25D, 0.25D, 0.25D, 0.04D);
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    if (foe.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                        foe.igniteForSeconds(4);
                        foe.hurtServer(level, level.damageSources().magic(), 3.0F + this.spell.getEffectiveTier() * 0.65F);
                    }
                }
            }
            case NATURE, OVERGROWTH -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 5));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 45, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
                }
            }
            case EARTH -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.setDeltaMovement(travel.x * 0.95D, 0.25D, travel.z * 0.95D);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                }
            }
            case FROZEN, GLACIAL -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 40, foe.getTicksFrozen() + 55));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                }
            }
            case NETHER -> {
                // Magma Mortar lobbed explosive fireball!
                level.sendParticles(ParticleTypes.FLAME, impactPos.x, impactPos.y + 0.3D, impactPos.z, 18, 0.8D, 0.4D, 0.8D, 0.08D);
                level.sendParticles(ParticleTypes.EXPLOSION, impactPos.x, impactPos.y + 0.2D, impactPos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.igniteForSeconds(5);
                    foe.hurtServer(level, level.damageSources().onFire(), 2.5F + this.spell.getEffectiveTier() * 0.65F);
                }
            }
            case UNDEAD -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                }
            }
            case VOID -> {
                if (directHit instanceof LivingEntity foe) {
                    Vec3 inward = travel.scale(-0.75D);
                    foe.setDeltaMovement(inward.x, 0.20D, inward.z);
                    foe.hasImpulse = true;
                }
            }
            case ARID -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                    foe.setDeltaMovement(travel.x * 1.15D, 0.32D, travel.z * 1.15D);
                    foe.hasImpulse = true;
                }
            }
            case LIGHTNING -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.hurtServer(level, level.damageSources().lightningBolt(), 1.5F + this.spell.getEffectiveTier() * 0.4F);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 15, 0));
                }
            }
            case VITAE -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0));
                }
            }
            case MAGMA -> {
                // Magma-Brute Sentry molten boulder splatter: 2.5m fiery blast + temporary molten magma crust!
                level.sendParticles(ParticleTypes.LAVA, impactPos.x, impactPos.y + 0.3D, impactPos.z, 14, 0.6D, 0.3D, 0.6D, 0.08D);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D))) {
                    if (foe == caster || !foe.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.igniteForSeconds(5);
                    foe.hurtServer(level, level.damageSources().onFire(), 2.5F + this.spell.getEffectiveTier() * 0.65F);
                }
                BlockPos hitGround = BlockPos.containing(impactPos).below();
                if (level.getBlockState(hitGround).isSolidRender() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, hitGround, level.getBlockState(hitGround))) {
                    var orig = level.getBlockState(hitGround);
                    if (!orig.is(Blocks.MAGMA_BLOCK)) {
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
            }
            case STORM -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellLingeringZoneManager.applySoaked(foe, 160);
                    foe.setDeltaMovement(travel.x * 1.15D, 0.35D, travel.z * 1.15D);
                    foe.hasImpulse = true;
                }
            }
            case DUST -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    foe.setDeltaMovement(travel.x * 1.25D, 0.38D, travel.z * 1.25D);
                    foe.hasImpulse = true;
                }
            }
            case SPORE -> {
                // Lobbed Mycelial Spore-Pod bursts into a 3s toxic spore cloud!
                SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, impactPos, 2.5D, 60, level.getGameTime(), this.spell, slot, EssenceType.SPORE, 1.0F,
                        (lvl, z, el) -> {
                            if (el % 10 == 0) {
                                lvl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, z.center.x, z.center.y + 0.5D, z.center.z,
                                        10, 1.1D, 0.4D, 1.1D, 0.03D);
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
                    foe.setDeltaMovement(travel.x * 1.10D, 0.45D, travel.z * 1.10D);
                    foe.hasImpulse = true;
                }
            }
            case AMBER -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 60L);
                }
            }
            case TAIGA -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                    foe.setTicksFrozen(foe.getTicksFrozen() + 40);
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, level.getGameTime() + 60L);
                }
            }
            case ECLIPSE -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                    for (var eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                        if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                    }
                }
            }
            case BLIGHT -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 1.0D, 0.0D, 100L);
                }
            }
            case ASTRAL -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.PARALYZED.get()), 12, 0));
                    foe.setDeltaMovement(foe.getDeltaMovement().x, 0.45D, foe.getDeltaMovement().z);
                    foe.hasImpulse = true;
                }
            }
            case SOULFIRE -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.igniteForSeconds(5);
                    EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 100L);
                }
            }
            case VAPOR -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    SpellLingeringZoneManager.applySoaked(foe, 120);
                }
            }
            case NULL_R -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    for (var eff : new java.util.ArrayList<>(foe.getActiveEffects())) {
                        if (eff.getEffect().value().isBeneficial()) foe.removeEffect(eff.getEffect());
                    }
                }
            }
            case NULL_U, ABYSS -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 50);
                    if (this.sentryBoltMateria == EssenceType.NULL_U) {
                        Vec3 inward = travel.scale(-0.75D);
                        foe.setDeltaMovement(inward.x, 0.20D, inward.z);
                        foe.hasImpulse = true;
                    } else {
                        SpellLingeringZoneManager.applySoaked(foe, 140);
                    }
                }
            }
            case DAWN -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.setDeltaMovement(travel.x * 1.35D, 0.38D, travel.z * 1.35D);
                    foe.hasImpulse = true;
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    if (foe.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) foe.igniteForSeconds(4);
                }
            }
            case AEGIS -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.setDeltaMovement(travel.x * 1.15D, 0.32D, travel.z * 1.15D);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                }
            }
            case AURORA -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 40, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 40);
                }
            }
            case MIRAGE -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    SpellLingeringZoneManager.applyMirageTraitorFrenzy(foe, 60);
                }
            }
            case WRAITH -> {
                if (directHit instanceof LivingEntity foe) {
                    SpellLingeringZoneManager.applyArcaneSilence(foe, 60);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
            }
            case BARROW -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 100L);
                }
            }
            case PYRE -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.igniteForSeconds(5);
                }
            }
            case PENUMBRA -> {
                if (directHit instanceof LivingEntity foe) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    Vec3 pull = travel.scale(-0.55D);
                    foe.setDeltaMovement(pull.x, 0.18D, pull.z);
                    foe.hasImpulse = true;
                }
            }
            case RIME -> {
                if (directHit instanceof LivingEntity foe) {
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
        EssenceType flowMateria = this.spell.flowPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        boolean vitaeMitosisOrb = this.splitChild && this.spell.flowPart().mutationEnabled() && flowMateria == EssenceType.VITAE;

        if (vitaeMitosisOrb && caster != null) {
            List<LivingEntity> injuredAllies = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(7.5D),
                    e -> e.isAlive()
                            && (e == caster || e.isAlliedTo(caster))
                            && !this.alreadyHitEntities.contains(e.getUUID())
                            && e.getHealth() < e.getMaxHealth() - 0.5F
            );
            if (!injuredAllies.isEmpty()) {
                LivingEntity lowestAlly = injuredAllies.get(0);
                float minRatio = lowestAlly.getHealth() / Math.max(1.0F, lowestAlly.getMaxHealth());
                for (int i = 1; i < injuredAllies.size(); i++) {
                    LivingEntity cand = injuredAllies.get(i);
                    float ratio = cand.getHealth() / Math.max(1.0F, cand.getMaxHealth());
                    if (ratio < minRatio) {
                        minRatio = ratio;
                        lowestAlly = cand;
                    }
                }
                Vec3 desired = lowestAlly.getEyePosition().subtract(this.position()).normalize().scale(speed);
                Vec3 blended = currentVel.scale(0.70D).add(desired.scale(0.30D)).normalize().scale(speed);
                this.setDeltaMovement(blended);
                this.hasImpulse = true;
                return;
            }
        }

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(7.5D),
                e -> e.isAlive()
                        && e != caster
                        && !this.alreadyHitEntities.contains(e.getUUID())
                        && (e instanceof Monster || (caster != null && e == caster.getLastHurtMob()))
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, this.spell.hasWardFriendlyFireSafeguard())
        );
        if (candidates.isEmpty()) return;

        LivingEntity nearest = candidates.get(0);
        double bestDist = nearest.distanceToSqr(this);
        for (int i = 1; i < candidates.size(); i++) {
            double d = candidates.get(i).distanceToSqr(this);
            if (d < bestDist) {
                bestDist = d;
                nearest = candidates.get(i);
            }
        }

        Vec3 desired = nearest.getEyePosition().subtract(this.position()).normalize().scale(speed);
        Vec3 blended = currentVel.scale(0.75D).add(desired.scale(0.25D)).normalize().scale(speed);
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
                && (hitLiv == caster || hitLiv.isAlliedTo(caster))
                && hitLiv.getHealth() < hitLiv.getMaxHealth() - 0.5F;

        if ((hit == this.getOwner() && !vitaeMitosisHeal) || this.alreadyHitEntities.contains(hit.getUUID())) {
            return;
        }
        this.alreadyHitEntities.add(hit.getUUID());

        if (vitaeMitosisHeal && hit instanceof LivingEntity allyTarget) {
            float mitosisHeal = 2.0F + this.spell.getEffectiveTier() * 0.75F;
            SpellCombatReactionEngine.notifyMagicalHeal(allyTarget, mitosisHeal);
            allyTarget.heal(mitosisHeal);
            serverLevel.sendParticles(ParticleTypes.HEART, allyTarget.getX(), allyTarget.getY(0.9D), allyTarget.getZ(),
                    5, 0.25D, 0.25D, 0.25D, 0.03D);
            this.discard();
            return;
        }

        SubSpellSlot slot = this.spell.subSpells().get(Math.clamp(this.slotIndex, 0, this.spell.subSpells().size() - 1));

        SpellExecutionEngine.deliverSubSpellOnEntity(
                serverLevel,
                caster,
                hit,
                this.getDeltaMovement().normalize(),
                this.spell,
                slot,
                this.fallbackMateria,
                this.apparatusPotencyMult
        );

        EssenceType formMateria = slot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
        if (formHandler != null) {
            formHandler.onProjectileHitEntityFromForm(serverLevel, this, caster, hit, this.spell, slot);
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
                case PENUMBRA -> orbitTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                case RIME -> {
                    orbitTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
                    orbitTarget.addEffect(new MobEffectInstance(
                            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ddraig.net.entropica.registry.ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
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

        Vec3 entityImpactCenter = hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D);
        EssenceType vPrimary = this.getVisualEssenceType();
        EssenceType vSecondary = this.getVisualSecondaryEssenceType();
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.broadcastProjectileImpact(
                serverLevel,
                entityImpactCenter,
                this.getDeltaMovement().scale(-1.0D),
                vPrimary,
                vSecondary,
                this.splitChild ? 0.65F : 0.95F,
                this.getVisualPotencyTier()
        );
        ddraig.net.entropica.spell.vfx.SpellParticleHelper.spawnImpactBurst(
                serverLevel,
                entityImpactCenter,
                vPrimary,
                vSecondary,
                this.splitChild ? 0.8D : 1.4D,
                this.getVisualPotencyTier()
        );

        tryTriggerFlowSplit(serverLevel, caster, entityImpactCenter, slot);

        boolean miasmaOrOblivionProjChain = this.spell.flowPart().mutationEnabled()
                && (flowMateria == EssenceType.MIASMA || flowMateria == EssenceType.OBLIVION || flowMateria == EssenceType.ENTROPIC);
        if ((this.spell.flowPart().flow() == SpellFlowModifier.CHAIN_LEAP || miasmaOrOblivionProjChain)
                && hit instanceof LivingEntity livingHit
                && caster != null) {
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

        SpellExecutionEngine.deliverSubSpellOnBlock(
                serverLevel,
                caster,
                result.getBlockPos(),
                result.getDirection(),
                this.spell,
                slot,
                this.fallbackMateria,
                this.apparatusPotencyMult
        );

        EssenceType formMateria = slot.formPart().resolveEffectiveMateria(this.spell.materiaSpecific(), this.fallbackMateria);
        MateriaPartMutationHandler formHandler = SpellMutationHandlerRegistry.resolveActive(formMateria, slot.formPart().mutationEnabled());
        if (formHandler != null) {
            formHandler.onProjectileHitBlockFromForm(serverLevel, this, caster, result, this.spell, slot);
        }

        applySentryBoltImpact(serverLevel, caster, result.getLocation(), null);

        Vec3 surfaceNormal = Vec3.atLowerCornerOf(result.getDirection().getUnitVec3i());
        EssenceType vPrimary = this.getVisualEssenceType();
        EssenceType vSecondary = this.getVisualSecondaryEssenceType();
        if (!this.wallPhasing) {
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

        if (this.wallPhasing) {
            // Advance slightly through the block boundary so spectral / void-phased bolts keep flying
            Vec3 nudge = this.getDeltaMovement().normalize().scale(0.65D);
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
            serverLevel.sendParticles(ParticleTypes.END_ROD, hitLoc.x, hitLoc.y, hitLoc.z, 8, 0.1D, 0.1D, 0.1D, 0.04D);
            serverLevel.playSound(null, result.getBlockPos(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.4F);
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
        output.store("RemainingPierces", Codec.INT, this.remainingPierces);
        output.store("RemainingBounces", Codec.INT, this.remainingBounces);
        output.store("WallPhasing", Codec.BOOL, this.wallPhasing);
        output.store("HomingGuidance", Codec.BOOL, this.homingGuidance);
        output.store("SplitChild", Codec.BOOL, this.splitChild);
        output.store("FlowSplitTriggered", Codec.BOOL, this.flowSplitTriggered);
        output.store("MaxFlightTicks", Codec.INT, this.maxFlightTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("SpellAssembly", SpellAssembly.CODEC).ifPresent(s -> this.spell = s);
        this.slotIndex = input.read("SlotIndex", Codec.INT).orElse(0);
        this.fallbackMateria = input.read("FallbackMateria", EssenceType.CODEC).orElse(EssenceType.REGULAR);
        this.apparatusPotencyMult = input.read("ApparatusPotencyMult", Codec.FLOAT).orElse(1.0F);
        this.remainingPierces = input.read("RemainingPierces", Codec.INT).orElse(0);
        this.remainingBounces = input.read("RemainingBounces", Codec.INT).orElse(0);
        this.wallPhasing = input.read("WallPhasing", Codec.BOOL).orElse(false);
        this.homingGuidance = input.read("HomingGuidance", Codec.BOOL).orElse(false);
        this.splitChild = input.read("SplitChild", Codec.BOOL).orElse(false);
        this.flowSplitTriggered = input.read("FlowSplitTriggered", Codec.BOOL).orElse(false);
        this.maxFlightTicks = input.read("MaxFlightTicks", Codec.INT).orElse(60);
    }
}
