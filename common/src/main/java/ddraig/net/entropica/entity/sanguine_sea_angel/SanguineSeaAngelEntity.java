package ddraig.net.entropica.entity.sanguine_sea_angel;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.entity.projectile.BloodFrostDartEntity;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Biome #16 (2.16 Crimson Tide-Skerries) Hostile Fauna — Sanguine Sea-Angel (entropica:sanguine_sea_angel).
 * Deceptively luminous gymnosome pteropod predator that swims in 3D through crimson briny bays and
 * skims predatorily 0.5–1.4 blocks above Sanguine Ice shelves without flopping.
 * Splits its hooded head cowl during combat to fire Blood-Frost Darts at medium range (4.5–14.5 blocks)
 * or deliver a life-draining Split-Head Siphon Lunge at close range (< 4.5 blocks).
 */
public class SanguineSeaAngelEntity extends Monster {

    public static final int SPIT_DART_DURATION_TICKS = 24;     // 1.20s
    public static final int SPIT_DART_RELEASE_TICK = 9;        // ~0.45s windup peak
    public static final int SPIT_DART_COOLDOWN_TICKS = 50;

    public static final int LUNGE_ATTACK_DURATION_TICKS = 25;  // 1.25s
    public static final int LUNGE_STRIKE_PEAK_TICK = 10;       // ~0.50s strike peak
    public static final int LUNGE_COOLDOWN_TICKS = 34;

    public static final int DAMAGED_DURATION_TICKS = 11;       // 0.55s
    public static final int DEATH_DURATION_TICKS = 36;         // 1.80s

    public static final byte EVENT_SPIT_DART = 61;
    public static final byte EVENT_LUNGE_ATTACK = 62;
    public static final byte EVENT_DAMAGED = 63;

    private static final DustParticleOptions SANGUINE_HEART_DUST = new DustParticleOptions(0xD4223E, 0.72F);
    private static final DustParticleOptions ABYSSAL_GARNET_DUST = new DustParticleOptions(0x9E1B46, 0.72F);

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(SanguineSeaAngelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HUNTING =
            SynchedEntityData.defineId(SanguineSeaAngelEntity.class, EntityDataSerializers.BOOLEAN);

    // All 8 Blockbench AnimationStates
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState iceHoverAnimationState = new AnimationState();
    public final AnimationState iceGlideAnimationState = new AnimationState();
    public final AnimationState spitDartAnimationState = new AnimationState();
    public final AnimationState lungeAttackAnimationState = new AnimationState();
    public final AnimationState damagedAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int meleeCooldown = 0;
    private int dartCooldown = 20;

    public SanguineSeaAngelEntity(EntityType<? extends SanguineSeaAngelEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
        this.setNoGravity(true);
        this.moveControl = new SanguineSeaAngelMoveControl(this);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, SanguineSeaAngelVariant.POLAR_BLOOD_SHALLOWS.getId());
        builder.define(DATA_HUNTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SanguineSeaAngelCombatGoal(this));
        this.goalSelector.addGoal(2, new SanguineBayAndIcePatrolGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Cod.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Salmon.class, true));
    }

    public SanguineSeaAngelVariant getVariant() {
        return SanguineSeaAngelVariant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(SanguineSeaAngelVariant variant) {
        SanguineSeaAngelVariant safe = variant != null ? variant : SanguineSeaAngelVariant.POLAR_BLOOD_SHALLOWS;
        this.entityData.set(DATA_VARIANT, safe.getId());
    }

    public boolean isHunting() {
        return this.entityData.get(DATA_HUNTING);
    }

    public void setHunting(boolean hunting) {
        this.entityData.set(DATA_HUNTING, hunting);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant().getId());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(SanguineSeaAngelVariant.byId(input.read("Variant", Codec.INT).orElse(0)));
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        BlockPos pos = this.blockPosition();
        boolean abyssal = pos.getY() < 52
                || level.getLevel().isDarkOutside()
                || level.getLevel().isThundering();
        this.setVariant(abyssal
                ? SanguineSeaAngelVariant.ABYSSAL_GARNET
                : SanguineSeaAngelVariant.POLAR_BLOOD_SHALLOWS);
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    public static boolean checkSanguineSeaAngelSpawnRules(
            EntityType<SanguineSeaAngelEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        if (level.getBlockState(pos).blocksMotion()) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        return level.getFluidState(pos).is(FluidTags.WATER)
                || level.getFluidState(pos.below()).is(FluidTags.WATER)
                || below.is(BlockTags.ICE)
                || (ModBlocks.SANGUINE_ICE.isPresent() && below.is(ModBlocks.SANGUINE_ICE.get()))
                || (ModBlocks.RED_CLAY_SAND.isPresent() && below.is(ModBlocks.RED_CLAY_SAND.get()));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos pos = this.blockPosition();
        BlockState below = level.getBlockState(pos.below());
        if (level.getFluidState(pos).is(FluidTags.WATER)
                || level.getFluidState(pos.below()).is(FluidTags.WATER)
                || below.is(BlockTags.ICE)
                || (ModBlocks.SANGUINE_ICE.isPresent() && below.is(ModBlocks.SANGUINE_ICE.get()))
                || (ModBlocks.RED_CLAY_SAND.isPresent() && below.is(ModBlocks.RED_CLAY_SAND.get()))) {
            return level.getDifficulty() != Difficulty.PEACEFUL;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return 10.0F;
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockState below = level.getBlockState(pos.below(dy));
            if (below.getFluidState().is(FluidTags.WATER)
                    || below.is(BlockTags.ICE)
                    || (ModBlocks.SANGUINE_ICE.isPresent() && below.is(ModBlocks.SANGUINE_ICE.get()))
                    || (ModBlocks.RED_CLAY_SAND.isPresent() && below.is(ModBlocks.RED_CLAY_SAND.get()))) {
                return 8.5F;
            }
        }
        return 1.0F;
    }

    // --- Amphibious Respiration & Environmental Immunities ---

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    protected void handleAirSupply(int airSupply) {
        // Amphibious polar sea-angel never drowns in water or suffocates in air
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.getAirSupply() < this.getMaxAirSupply()) {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        this.resetFallDistance();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Silent buoyant swimmer and ice-skimming hoverer
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    // --- 3D Water-Swim + Ice-Skimming Hover Physics ---

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isDeadOrDying()) {
            super.travel(travelVector);
            return;
        }
        if (this.isEffectiveAi()) {
            if (this.isInWater()) {
                this.moveRelative(0.045F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.88D));
                this.calculateEntityAnimation(false);
                return;
            } else {
                this.moveRelative(0.05F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.86D));
                this.calculateEntityAnimation(false);
                return;
            }
        }
        super.travel(travelVector);
    }

    private double findSurfaceBelow(int maxDepth) {
        BlockPos origin = this.blockPosition();
        for (int dy = 0; dy <= maxDepth; dy++) {
            BlockPos check = origin.below(dy);
            BlockState state = this.level().getBlockState(check);
            if (state.isAir() || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
                continue;
            }
            if (!state.getFluidState().isEmpty() || state.blocksMotion()) {
                return check.getY() + 1.0D;
            }
        }
        return Double.NaN;
    }

    private void maintainIceSkimmingHover() {
        if (this.isInWater() || this.isDeadOrDying()) {
            return;
        }
        double surfaceY = this.findSurfaceBelow(8);
        Vec3 vel = this.getDeltaMovement();

        if (Double.isNaN(surfaceY)) {
            // High above open space — drift gently downward toward surface
            this.setDeltaMovement(vel.x, Mth.clamp(vel.y - 0.025D, -0.09D, 0.05D), vel.z);
            return;
        }

        double heightAbove = this.getY() - surfaceY;
        double wavePhase = this.tickCount * 0.085D + this.getId() * 0.91D;
        // Ideal predatory skim band: ~0.70 - 1.15 blocks above Sanguine Ice / shoreline (within 0.5 - 1.4 band)
        double desiredHeight = 0.92D + Math.sin(wavePhase) * 0.18D;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !target.isInWater()) {
            double targetHover = (target.getY() + 0.45D) - surfaceY;
            desiredHeight = Mth.clamp(targetHover, 0.55D, 1.40D);
        }

        double nextVy = vel.y;
        if (heightAbove < 1.4D) {
            double diff = desiredHeight - heightAbove;
            double lift = Mth.clamp(diff * 0.13D, -0.045D, 0.085D);
            if (heightAbove < 0.50D || this.onGround()) {
                lift = Math.max(lift, 0.055D);
            }
            nextVy = Mth.lerp(0.32D, vel.y, lift);
        } else if (heightAbove > 2.2D) {
            // Gentle downward drift back toward the ice shelf
            double driftDown = Mth.clamp((desiredHeight - heightAbove) * 0.075D, -0.085D, -0.025D);
            nextVy = Mth.lerp(0.28D, vel.y, driftDown);
        } else {
            double settle = Mth.clamp((desiredHeight - heightAbove) * 0.065D, -0.05D, 0.05D);
            nextVy = Mth.lerp(0.22D, vel.y, settle);
        }

        this.setDeltaMovement(vel.x, nextVy, vel.z);
    }

    // --- Combat & Damage Hooks ---

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        boolean hurt = super.doHurtTarget(serverLevel, target);
        if (hurt) {
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), this);
            }
            this.heal(2.5F);
            serverLevel.sendParticles(
                    SANGUINE_HEART_DUST,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.55D,
                    target.getZ(),
                    12,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.03D
            );
        }
        return hurt;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            serverLevel.broadcastEntityEvent(this, EVENT_DAMAGED);
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY() + 0.45D,
                    this.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK,
                    SoundSource.HOSTILE,
                    0.35F,
                    1.6F
            );
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setNoGravity(false);

        Vec3 vel = this.getDeltaMovement();
        if (this.isInWater()) {
            this.setDeltaMovement(vel.x * 0.80D, Mth.clamp(vel.y - 0.015D, -0.06D, 0.02D), vel.z * 0.80D);
        } else {
            this.setDeltaMovement(vel.x * 0.82D, vel.y, vel.z * 0.82D);
        }

        if (!this.level().isClientSide() && this.deathTime == 1 && this.level() instanceof ServerLevel serverLevel) {
            this.setHunting(false);
            serverLevel.sendParticles(
                    SANGUINE_HEART_DUST,
                    this.getX(),
                    this.getY() + 0.45D,
                    this.getZ(),
                    16,
                    0.32D,
                    0.28D,
                    0.32D,
                    0.03D
            );
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY() + 0.45D,
                    this.getZ(),
                    10,
                    0.30D,
                    0.25D,
                    0.30D,
                    0.03D
            );
        }

        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SPIT_DART) {
            this.lungeAttackAnimationState.stop();
            this.spitDartAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_LUNGE_ATTACK) {
            this.spitDartAnimationState.stop();
            this.lungeAttackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_DAMAGED) {
            this.damagedAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }
        if (this.isAlive()) {
            this.setNoGravity(true);
        } else {
            this.setNoGravity(false);
        }

        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.meleeCooldown > 0) {
                this.meleeCooldown--;
            }
            if (this.dartCooldown > 0) {
                this.dartCooldown--;
            }

            if (this.isAlive()) {
                LivingEntity target = this.getTarget();
                boolean huntingNow = target != null && target.isAlive();
                if (huntingNow != this.isHunting()) {
                    this.setHunting(huntingNow);
                }

                // Distinct hunting soundscape: rhythmic systolic heartbeat pulse every 24 ticks
                if (huntingNow && (this.tickCount + this.getId()) % 24 == 0) {
                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY() + 0.45D,
                            this.getZ(),
                            SoundEvents.WARDEN_HEARTBEAT,
                            SoundSource.HOSTILE,
                            0.45F,
                            1.55F
                    );
                }

                this.maintainIceSkimmingHover();
            }
        } else if (this.level().isClientSide()) {
            this.updateClientAnimationsAndParticles();
        }
    }

    private void updateClientAnimationsAndParticles() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.swimAnimationState.stop();
            this.iceHoverAnimationState.stop();
            this.iceGlideAnimationState.stop();
            this.spitDartAnimationState.stop();
            this.lungeAttackAnimationState.stop();
            this.damagedAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.spitDartAnimationState.isStarted()
                && this.spitDartAnimationState.getTimeInMillis(this.tickCount) >= SPIT_DART_DURATION_TICKS * 50L) {
            this.spitDartAnimationState.stop();
        }
        if (this.lungeAttackAnimationState.isStarted()
                && this.lungeAttackAnimationState.getTimeInMillis(this.tickCount) >= LUNGE_ATTACK_DURATION_TICKS * 50L) {
            this.lungeAttackAnimationState.stop();
        }
        if (this.damagedAnimationState.isStarted()
                && this.damagedAnimationState.getTimeInMillis(this.tickCount) >= DAMAGED_DURATION_TICKS * 50L) {
            this.damagedAnimationState.stop();
        }

        boolean inWater = this.isInWater();
        double speedSqr = inWater
                ? this.getDeltaMovement().lengthSqr()
                : this.getDeltaMovement().horizontalDistanceSqr();
        boolean moving = speedSqr > 2.2E-4D || this.walkAnimation.speed() > 0.05F;

        if (inWater) {
            this.iceHoverAnimationState.stop();
            this.iceGlideAnimationState.stop();
            if (moving) {
                this.idleAnimationState.stop();
                this.swimAnimationState.startIfStopped(this.tickCount);
            } else {
                this.swimAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.idleAnimationState.stop();
            this.swimAnimationState.stop();
            if (moving) {
                this.iceHoverAnimationState.stop();
                this.iceGlideAnimationState.startIfStopped(this.tickCount);
            } else {
                this.iceGlideAnimationState.stop();
                this.iceHoverAnimationState.startIfStopped(this.tickCount);
            }
        }

        // Subtle glowing crimson core & frost-mote trail
        int interval = this.isHunting() ? 4 : 9;
        if (this.tickCount % interval == 0) {
            DustParticleOptions dust = this.getVariant() == SanguineSeaAngelVariant.ABYSSAL_GARNET
                    ? ABYSSAL_GARNET_DUST
                    : SANGUINE_HEART_DUST;
            this.level().addParticle(
                    dust,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.45D,
                    this.getY() + 0.35D + this.random.nextDouble() * 0.40D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.45D,
                    0.0D,
                    -0.006D,
                    0.0D
            );
            if (!inWater && this.random.nextInt(3) == 0) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.50D,
                        this.getY() + 0.15D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.50D,
                        0.0D,
                        -0.01D,
                        0.0D
                );
            }
        }
    }

    public void shootBloodFrostDart(ServerLevel serverLevel, LivingEntity target) {
        Vec3 forward = this.getViewVector(1.0F);
        double muzzleX = this.getX() + forward.x * 0.45D;
        double muzzleY = this.getY() + this.getBbHeight() * 0.68D;
        double muzzleZ = this.getZ() + forward.z * 0.45D;

        BloodFrostDartEntity dart = new BloodFrostDartEntity(serverLevel, muzzleX, muzzleY, muzzleZ);
        dart.setOwner(this);

        double dx = target.getX() - muzzleX;
        double dy = target.getY(0.5D) - muzzleY;
        double dz = target.getZ() - muzzleZ;
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        dart.shoot(dx, dy + horizDist * 0.04D, dz, 1.15F, 1.0F);
        serverLevel.addFreshEntity(dart);

        serverLevel.playSound(
                null,
                muzzleX,
                muzzleY,
                muzzleZ,
                SoundEvents.SNOW_GOLEM_SHOOT,
                SoundSource.HOSTILE,
                0.90F,
                1.3F
        );
        serverLevel.playSound(
                null,
                muzzleX,
                muzzleY,
                muzzleZ,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE,
                0.85F,
                1.5F
        );
    }

    // --- Soundscape ---

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isHunting() ? SoundEvents.CONDUIT_AMBIENT_SHORT : SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    public float getVoicePitch() {
        if (!this.isHunting()) {
            return 1.35F + (this.random.nextFloat() - 0.5F) * 0.15F;
        }
        return 1.15F + (this.random.nextFloat() - 0.5F) * 0.15F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    // --- Custom Move Control & AI Goals ---

    static class SanguineSeaAngelMoveControl extends MoveControl {
        private final SanguineSeaAngelEntity angel;

        SanguineSeaAngelMoveControl(SanguineSeaAngelEntity angel) {
            super(angel);
            this.angel = angel;
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO || this.angel.isDeadOrDying()) {
                return;
            }
            this.operation = Operation.WAIT;

            Vec3 diff = new Vec3(
                    this.wantedX - this.angel.getX(),
                    this.wantedY - this.angel.getY(),
                    this.wantedZ - this.angel.getZ()
            );
            double dist = diff.length();
            if (dist < 0.05D) {
                return;
            }

            double horizDist = Math.hypot(diff.x, diff.z);
            if (horizDist > 1.0E-4D) {
                float targetYaw = (float) (Mth.atan2(diff.z, diff.x) * (180.0D / Math.PI)) - 90.0F;
                this.angel.setYRot(this.rotlerp(this.angel.getYRot(), targetYaw, 14.0F));
                this.angel.yBodyRot = this.angel.getYRot();
            }

            double baseSpeed = this.angel.getAttributeValue(Attributes.MOVEMENT_SPEED) * this.speedModifier;
            Vec3 dir = diff.scale(1.0D / dist);

            if (this.angel.isInWater()) {
                Vec3 accel = dir.scale(baseSpeed * 0.22D);
                Vec3 next = this.angel.getDeltaMovement().add(accel);
                double maxSpd = baseSpeed * 1.15D;
                if (next.lengthSqr() > maxSpd * maxSpd) {
                    next = next.normalize().scale(maxSpd);
                }
                this.angel.setDeltaMovement(next);
            } else {
                // Out-of-water predatory ice-skim glide (vertical altitude regulated by maintainIceSkimmingHover)
                Vec3 cur = this.angel.getDeltaMovement();
                double addX = dir.x * baseSpeed * 0.20D;
                double addZ = dir.z * baseSpeed * 0.20D;
                double addY = Mth.clamp(dir.y * baseSpeed * 0.10D, -0.04D, 0.05D);
                Vec3 nextHoriz = new Vec3(cur.x + addX, 0.0D, cur.z + addZ);
                double maxHoriz = baseSpeed * 1.10D;
                if (nextHoriz.lengthSqr() > maxHoriz * maxHoriz) {
                    nextHoriz = nextHoriz.normalize().scale(maxHoriz);
                }
                this.angel.setDeltaMovement(nextHoriz.x, cur.y + addY, nextHoriz.z);
            }
        }
    }

    /**
     * Dual-range predatory combat goal:
     * - < 4.5 blocks: Split-Head Siphon Lunge (5.0 melee damage + 60t Slowness I + 2.5 HP self-heal).
     * - 4.5–14.5 blocks with LOS: Blood-Frost Dart Spit (1.15F velocity cryo-blood needle).
     */
    static class SanguineSeaAngelCombatGoal extends Goal {
        private final SanguineSeaAngelEntity angel;
        private int actionTick = 0;
        private int currentAction = 0; // 0 = none, 1 = lunge, 2 = spit dart
        private boolean lungeStruck = false;

        SanguineSeaAngelCombatGoal(SanguineSeaAngelEntity angel) {
            this.angel = angel;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.angel.getTarget();
            return this.angel.isAlive() && target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.angel.isAlive()) {
                return false;
            }
            if (this.currentAction != 0 && this.actionTick > 0) {
                return true;
            }
            LivingEntity target = this.angel.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.currentAction = 0;
            this.actionTick = 0;
            this.lungeStruck = false;
        }

        @Override
        public void stop() {
            this.currentAction = 0;
            this.actionTick = 0;
            this.lungeStruck = false;
            this.angel.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.angel.getTarget();
            if (target == null || !target.isAlive()) {
                this.currentAction = 0;
                this.actionTick = 0;
                return;
            }

            this.angel.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = this.angel.distanceToSqr(target);
            boolean hasLos = this.angel.getSensing().hasLineOfSight(target);

            // Approach or hold optimal hunting distance
            double desiredY = target.isInWater()
                    ? target.getY(0.5D)
                    : target.getY() + 0.55D;
            if (this.currentAction == 2) {
                // Steady hover/drift while spitting dart
                this.angel.getMoveControl().setWantedPosition(
                        this.angel.getX(), desiredY, this.angel.getZ(), 0.35D
                );
            } else if (distSqr > 9.0D || !hasLos) {
                this.angel.getMoveControl().setWantedPosition(
                        target.getX(), desiredY, target.getZ(), 1.22D
                );
            } else {
                this.angel.getMoveControl().setWantedPosition(
                        target.getX(), desiredY, target.getZ(), 0.95D
                );
            }

            if (!(this.angel.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            // Advance active lunge or spit windup
            if (this.currentAction == 1) {
                this.actionTick++;
                // Strike at peak (~tick 10) or on close contact (tick >= 6 and within 3.2 blocks)
                if (!this.lungeStruck
                        && (this.actionTick == LUNGE_STRIKE_PEAK_TICK
                        || (this.actionTick >= 6 && this.actionTick <= 14 && distSqr <= 3.2D * 3.2D))) {
                    if (distSqr <= 3.6D * 3.6D) {
                        this.lungeStruck = true;
                        this.angel.doHurtTarget(serverLevel, target);
                    } else if (this.actionTick == LUNGE_STRIKE_PEAK_TICK) {
                        this.lungeStruck = true;
                    }
                }
                if (this.actionTick >= LUNGE_ATTACK_DURATION_TICKS) {
                    this.currentAction = 0;
                    this.actionTick = 0;
                    this.lungeStruck = false;
                }
                return;
            }

            if (this.currentAction == 2) {
                this.actionTick++;
                if (this.actionTick == SPIT_DART_RELEASE_TICK) {
                    this.angel.shootBloodFrostDart(serverLevel, target);
                }
                if (this.actionTick >= SPIT_DART_DURATION_TICKS) {
                    this.currentAction = 0;
                    this.actionTick = 0;
                }
                return;
            }

            // Initiate Split-Head Siphon Lunge (< 4.5 blocks)
            if (distSqr < 4.5D * 4.5D && this.angel.meleeCooldown <= 0 && hasLos) {
                this.currentAction = 1;
                this.actionTick = 0;
                this.lungeStruck = false;
                this.angel.meleeCooldown = LUNGE_COOLDOWN_TICKS;
                serverLevel.broadcastEntityEvent(this.angel, EVENT_LUNGE_ATTACK);

                serverLevel.playSound(
                        null,
                        this.angel.getX(),
                        this.angel.getY() + 0.45D,
                        this.angel.getZ(),
                        SoundEvents.SHULKER_OPEN,
                        SoundSource.HOSTILE,
                        0.85F,
                        1.55F
                );
                serverLevel.playSound(
                        null,
                        this.angel.getX(),
                        this.angel.getY() + 0.45D,
                        this.angel.getZ(),
                        SoundEvents.EVOKER_FANGS_ATTACK,
                        SoundSource.HOSTILE,
                        0.65F,
                        1.4F
                );

                // Propel slightly toward target on lunge initiation
                Vec3 toTarget = new Vec3(
                        target.getX() - this.angel.getX(),
                        (target.getY(0.5D) - this.angel.getY()) * 0.55D,
                        target.getZ() - this.angel.getZ()
                );
                if (toTarget.lengthSqr() > 1.0E-4D) {
                    Vec3 impulse = toTarget.normalize().scale(0.38D);
                    this.angel.setDeltaMovement(this.angel.getDeltaMovement().scale(0.45D).add(impulse));
                }
                return;
            }

            // Initiate Blood-Frost Dart Spit (4.5 - 14.5 blocks with LOS)
            if (distSqr >= 4.5D * 4.5D
                    && distSqr <= 14.5D * 14.5D
                    && this.angel.dartCooldown <= 0
                    && hasLos) {
                this.currentAction = 2;
                this.actionTick = 0;
                this.angel.dartCooldown = SPIT_DART_COOLDOWN_TICKS;
                serverLevel.broadcastEntityEvent(this.angel, EVENT_SPIT_DART);

                serverLevel.playSound(
                        null,
                        this.angel.getX(),
                        this.angel.getY() + 0.45D,
                        this.angel.getZ(),
                        SoundEvents.SHULKER_OPEN,
                        SoundSource.HOSTILE,
                        0.80F,
                        1.65F
                );
            }
        }
    }

    /**
     * Amphibious patrol goal: swims smoothly in 3D through polar bay water or glides low
     * across Sanguine Ice shelves and red-clay-sand shallows when calm.
     */
    static class SanguineBayAndIcePatrolGoal extends Goal {
        private final SanguineSeaAngelEntity angel;
        private double targetX;
        private double targetY;
        private double targetZ;
        private int patrolTicks = 0;

        SanguineBayAndIcePatrolGoal(SanguineSeaAngelEntity angel) {
            this.angel = angel;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.angel.getTarget() != null || !this.angel.isAlive()) {
                return false;
            }
            if (this.angel.getRandom().nextInt(28) != 0) {
                return false;
            }
            return this.pickWaypoint();
        }

        @Override
        public boolean canContinueToUse() {
            return this.angel.isAlive()
                    && this.angel.getTarget() == null
                    && this.patrolTicks > 0
                    && this.angel.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 1.2D;
        }

        @Override
        public void start() {
            this.patrolTicks = 80 + this.angel.getRandom().nextInt(60);
        }

        @Override
        public void tick() {
            this.patrolTicks--;
            this.angel.getMoveControl().setWantedPosition(this.targetX, this.targetY, this.targetZ, 0.85D);
        }

        private boolean pickWaypoint() {
            RandomSource rand = this.angel.getRandom();
            Level level = this.angel.level();

            for (int attempt = 0; attempt < 8; attempt++) {
                double dx = (rand.nextDouble() - 0.5D) * 14.0D;
                double dz = (rand.nextDouble() - 0.5D) * 14.0D;
                double dy = this.angel.isInWater()
                        ? (rand.nextDouble() - 0.5D) * 4.0D
                        : (rand.nextDouble() - 0.5D) * 2.0D;

                BlockPos candidate = BlockPos.containing(
                        this.angel.getX() + dx,
                        this.angel.getY() + dy,
                        this.angel.getZ() + dz
                );

                if (level.getBlockState(candidate).blocksMotion()) {
                    continue;
                }

                if (level.getFluidState(candidate).is(FluidTags.WATER)) {
                    this.targetX = candidate.getX() + 0.5D;
                    this.targetY = candidate.getY() + 0.4D;
                    this.targetZ = candidate.getZ() + 0.5D;
                    return true;
                }

                // Check for ice / bay surface within 3 blocks below for ice-skimming glide
                for (int depth = 1; depth <= 3; depth++) {
                    BlockPos belowPos = candidate.below(depth);
                    BlockState below = level.getBlockState(belowPos);
                    if (!below.getFluidState().isEmpty() || below.blocksMotion()) {
                        this.targetX = candidate.getX() + 0.5D;
                        this.targetY = belowPos.getY() + 1.0D + 0.90D;
                        this.targetZ = candidate.getZ() + 0.5D;
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
