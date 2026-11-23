package ddraig.net.entropica.entity.aurora_jelly;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Fauna 4 — Aurora Jelly (Magnetic Glow-Drifter).
 * Passive low-atmosphere translucent jellyfish native to 2.2 Auroral Skerries.
 * Drifts smoothly along sinusoidal magnetic currents above water channels, gravel, and glimmer-ice,
 * harmonizes magnetically with players holding Aegis Spire Petals or Aegis Crystal Shards to pull nearby
 * item drops toward them, and defends itself when struck with a blinding bioluminescent flash and electric sting.
 */
public class AuroraJellyEntity extends PathfinderMob implements FlyingAnimal {

    private static final EntityDataAccessor<Boolean> STARTLED =
            SynchedEntityData.defineId(AuroraJellyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MAGNETIZING =
            SynchedEntityData.defineId(AuroraJellyEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_BIOLUMINESCENT_FLASH = 4;
    private static final byte EVENT_ELECTRIC_STING = 5;

    private static final DustParticleOptions NEON_GREEN_DUST = new DustParticleOptions(0x4ADE80, 0.85F);
    private static final DustParticleOptions CYAN_AURORA_DUST = new DustParticleOptions(0x22D3EE, 0.80F);
    private static final DustParticleOptions INDIGO_AURORA_DUST = new DustParticleOptions(0x4F46E5, 0.80F);

    private static final int STARTLED_DURATION_TICKS = 80;
    private static final int STARTLED_FLASH_DURATION_TICKS = 16;
    private static final int ATTACK_DURATION_TICKS = 18;
    private static final int DEATH_DURATION_TICKS = 32;
    private static final int STING_COOLDOWN_TICKS = 20;
    private static final float ELECTRIC_STING_DAMAGE = 2.0F;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState magnetizeAnimationState = new AnimationState();
    public final AnimationState startledFlashAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int startledTicks = 0;
    private int stingCooldown = 0;
    private Vec3 startledEscapeDir = Vec3.ZERO;

    public AuroraJellyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 16, true);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean isSkerriesDriftSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (!belowState.getFluidState().isEmpty() && belowState.getFluidState().is(FluidTags.WATER)) {
            return true;
        }
        return belowState.is(Blocks.WATER)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.FROSTED_ICE)
                || belowState.is(BlockTags.ICE)
                || (ModBlocks.GLIMMER_ICE.isPresent() && belowState.is(ModBlocks.GLIMMER_ICE.get()))
                || (ModBlocks.RUNIC_BLUE_ICE.isPresent() && belowState.is(ModBlocks.RUNIC_BLUE_ICE.get()))
                || belowState.is(Blocks.GRAVEL)
                || (ModBlocks.COARSE_GRAVEL.isPresent() && belowState.is(ModBlocks.COARSE_GRAVEL.get()))
                || (ModBlocks.AURORAL_FEATHER_MOSS.isPresent() && belowState.is(ModBlocks.AURORAL_FEATHER_MOSS.get()))
                || (ModBlocks.AURORAL_LILY_PAD.isPresent() && belowState.is(ModBlocks.AURORAL_LILY_PAD.get()))
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.SNOW)
                || belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || belowState.is(BlockTags.BASE_STONE_OVERWORLD)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkAuroraJellySpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        BlockState atPos = level.getBlockState(pos);
        BlockState abovePos = level.getBlockState(pos.above());
        if (atPos.blocksMotion() || abovePos.blocksMotion()) {
            return false;
        }
        for (int dy = 1; dy <= 5; dy++) {
            BlockPos check = pos.below(dy);
            BlockState below = level.getBlockState(check);
            if (below.isAir() || below.is(Blocks.SNOW)) {
                continue;
            }
            return isSkerriesDriftSurface(level, check, below);
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        for (int dy = 1; dy <= 5; dy++) {
            BlockPos belowPos = this.blockPosition().below(dy);
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.isAir()) {
                continue;
            }
            if (isSkerriesDriftSurface(level, belowPos, belowState)) {
                return true;
            }
            break;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        for (int dy = 1; dy <= 4; dy++) {
            BlockState below = level.getBlockState(pos.below(dy));
            if (!below.getFluidState().isEmpty() && below.getFluidState().is(FluidTags.WATER)) {
                return 10.0F;
            }
            if ((ModBlocks.GLIMMER_ICE.isPresent() && below.is(ModBlocks.GLIMMER_ICE.get()))
                    || (ModBlocks.COARSE_GRAVEL.isPresent() && below.is(ModBlocks.COARSE_GRAVEL.get()))
                    || below.is(Blocks.GRAVEL)
                    || below.is(Blocks.ICE)) {
                return 9.0F;
            }
        }
        return 2.0F;
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
        builder.define(STARTLED, false);
        builder.define(MAGNETIZING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StartledDartAwayGoal(this));
        this.goalSelector.addGoal(2, new MagneticResonanceFollowGoal(this));
        this.goalSelector.addGoal(3, new SinusoidalAuroraDriftGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public boolean isStartled() {
        return this.entityData.get(STARTLED);
    }

    public void setStartled(boolean startled) {
        this.entityData.set(STARTLED, startled);
    }

    public boolean isMagnetizing() {
        return this.entityData.get(MAGNETIZING);
    }

    public void setMagnetizing(boolean magnetizing) {
        this.entityData.set(MAGNETIZING, magnetizing);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Buoyant low-atmosphere drifter never takes fall damage
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Floats silently along magnetic aurora currents
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.GLOW_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLOW_SQUID_DEATH;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    public static boolean isMagneticLureStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return (ModItems.AEGIS_SPIRE_PETAL.isPresent() && stack.is(ModItems.AEGIS_SPIRE_PETAL.get()))
                || (ModItems.AEGIS_CRYSTAL_SHARD.isPresent() && stack.is(ModItems.AEGIS_CRYSTAL_SHARD.get()));
    }

    public static boolean isHoldingMagneticLure(Player player) {
        return isMagneticLureStack(player.getMainHandItem()) || isMagneticLureStack(player.getOffhandItem());
    }

    public @Nullable Player findMagneticLurePlayer(double maxDistance) {
        double maxDistSqr = maxDistance * maxDistance;
        List<Player> candidates = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(maxDistance),
                p -> p.isAlive() && !p.isSpectator() && isHoldingMagneticLure(p) && this.distanceToSqr(p) <= maxDistSqr
        );
        return candidates.stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setNoGravity(false);

        Vec3 vel = this.getDeltaMovement();
        this.setDeltaMovement(vel.x * 0.80D, Mth.clamp(vel.y - 0.014D, -0.065D, 0.02D), vel.z * 0.80D);

        if (!this.level().isClientSide() && this.deathTime == 1 && this.level() instanceof ServerLevel serverLevel) {
            this.setStartled(false);
            this.setMagnetizing(false);
            serverLevel.sendParticles(
                    NEON_GREEN_DUST,
                    this.getX(),
                    this.getY() + 0.75D,
                    this.getZ(),
                    14,
                    0.35D,
                    0.30D,
                    0.35D,
                    0.03D
            );
            serverLevel.sendParticles(
                    CYAN_AURORA_DUST,
                    this.getX(),
                    this.getY() + 0.70D,
                    this.getZ(),
                    12,
                    0.35D,
                    0.30D,
                    0.35D,
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
        if (id == EVENT_BIOLUMINESCENT_FLASH) {
            this.startledFlashAnimationState.start(this.tickCount);
            for (int i = 0; i < 28; i++) {
                double angle = (Math.PI * 2.0D * i) / 28.0D;
                double speed = 0.18D + this.random.nextDouble() * 0.14D;
                DustParticleOptions dust = (i % 3 == 0) ? NEON_GREEN_DUST : ((i % 3 == 1) ? CYAN_AURORA_DUST : INDIGO_AURORA_DUST);
                this.level().addParticle(
                        dust,
                        this.getX() + Math.cos(angle) * 0.45D,
                        this.getY() + 0.95D + (this.random.nextDouble() - 0.5D) * 0.4D,
                        this.getZ() + Math.sin(angle) * 0.45D,
                        Math.cos(angle) * speed,
                        0.03D,
                        Math.sin(angle) * speed
                );
                if (i % 2 == 0) {
                    this.level().addParticle(
                            ParticleTypes.GLOW,
                            this.getX() + Math.cos(angle) * 0.35D,
                            this.getY() + 0.8D,
                            this.getZ() + Math.sin(angle) * 0.35D,
                            Math.cos(angle) * speed * 0.6D,
                            0.04D,
                            Math.sin(angle) * speed * 0.6D
                    );
                }
            }
            return;
        }
        if (id == EVENT_ELECTRIC_STING) {
            this.attackAnimationState.start(this.tickCount);
            for (int i = 0; i < 12; i++) {
                this.level().addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 0.25D + this.random.nextDouble() * 0.6D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        (this.random.nextDouble() - 0.5D) * 0.18D,
                        (this.random.nextDouble() - 0.5D) * 0.12D,
                        (this.random.nextDouble() - 0.5D) * 0.18D
                );
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }
        this.setNoGravity(this.isAlive());
        super.tick();

        if (this.level().isClientSide()) {
            if (this.isDeadOrDying()) {
                this.idleAnimationState.stop();
                this.flyAnimationState.stop();
                this.magnetizeAnimationState.stop();
                this.startledFlashAnimationState.stop();
                this.attackAnimationState.stop();
                this.deathAnimationState.startIfStopped(this.tickCount);
                return;
            }

            if (this.startledFlashAnimationState.isStarted()
                    && this.startledFlashAnimationState.getTimeInMillis(this.tickCount) >= STARTLED_FLASH_DURATION_TICKS * 50L) {
                this.startledFlashAnimationState.stop();
            }
            if (this.attackAnimationState.isStarted()
                    && this.attackAnimationState.getTimeInMillis(this.tickCount) >= ATTACK_DURATION_TICKS * 50L) {
                this.attackAnimationState.stop();
            }

            if (this.isMagnetizing()) {
                this.idleAnimationState.stop();
                this.flyAnimationState.stop();
                this.magnetizeAnimationState.startIfStopped(this.tickCount);
            } else {
                this.magnetizeAnimationState.stop();
                if (this.getDeltaMovement().horizontalDistanceSqr() > 2.5E-4D || this.isStartled()) {
                    this.idleAnimationState.stop();
                    this.flyAnimationState.startIfStopped(this.tickCount);
                } else {
                    this.flyAnimationState.stop();
                    this.idleAnimationState.startIfStopped(this.tickCount);
                }
            }

            // Soft ambient aurora luminescence drifting from bell and tendrils
            float particleChance = this.isStartled() ? 0.65F : (this.isMagnetizing() ? 0.45F : 0.28F);
            if (this.random.nextFloat() < particleChance) {
                int pick = this.random.nextInt(4);
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
                double py = this.getY() + 0.2D + this.random.nextDouble() * 1.15D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
                if (pick == 0) {
                    this.level().addParticle(NEON_GREEN_DUST, px, py, pz, 0.0D, -0.01D, 0.0D);
                } else if (pick == 1) {
                    this.level().addParticle(CYAN_AURORA_DUST, px, py, pz, 0.0D, -0.01D, 0.0D);
                } else if (pick == 2) {
                    this.level().addParticle(INDIGO_AURORA_DUST, px, py, pz, 0.0D, -0.01D, 0.0D);
                } else {
                    this.level().addParticle(ParticleTypes.END_ROD, px, py, pz,
                            (this.random.nextDouble() - 0.5D) * 0.01D, -0.012D, (this.random.nextDouble() - 0.5D) * 0.01D);
                }
            }
        } else if (this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
            if (this.stingCooldown > 0) {
                this.stingCooldown--;
            }

            if (this.startledTicks > 0) {
                this.startledTicks--;
                if (this.startledTicks <= 0) {
                    this.setStartled(false);
                }
            }

            checkTentacleTouchStingCollisions(serverLevel);

            Player lurePlayer = this.startledTicks <= 0 ? findMagneticLurePlayer(12.0D) : null;
            boolean activeMagnetism = lurePlayer != null && this.distanceToSqr(lurePlayer) <= 64.0D; // 8.0D blocks
            this.setMagnetizing(activeMagnetism);

            if (activeMagnetism) {
                pullNearbyItemDropsTowardPlayer(serverLevel, lurePlayer);
            }

            maintainBuoyantAltitude(lurePlayer);
        }
    }

    private void maintainBuoyantAltitude(@Nullable Player lurePlayer) {
        double surfaceY = findSurfaceBelow();
        double phase = this.getId() * 0.73D + this.tickCount * 0.048D;
        // Buoyant hover 1.5 - 4.0 blocks above surface along sinusoidal wave (centered ~2.65 +/- 0.95)
        double waveHeight = 2.65D + Math.sin(phase) * 0.95D;

        double targetY;
        if (lurePlayer != null) {
            double comfortY = lurePlayer.getY() + 1.45D + Math.sin(phase * 1.25D) * 0.25D;
            if (!Double.isNaN(surfaceY)) {
                targetY = Mth.clamp(comfortY, surfaceY + 1.5D, surfaceY + 4.0D);
            } else {
                targetY = comfortY;
            }
        } else if (!Double.isNaN(surfaceY)) {
            targetY = surfaceY + Mth.clamp(waveHeight, 1.5D, 4.0D);
        } else {
            return;
        }

        if (this.startledTicks > 0) {
            targetY += 0.55D;
        }

        double diffY = targetY - this.getY();
        if (Math.abs(diffY) > 0.06D) {
            Vec3 vel = this.getDeltaMovement();
            double adjustY = Mth.clamp(diffY * 0.085D, -0.09D, 0.11D);
            this.setDeltaMovement(vel.x, Mth.lerp(0.30D, vel.y, adjustY), vel.z);
        }
    }

    private double findSurfaceBelow() {
        BlockPos origin = this.blockPosition();
        for (int dy = 0; dy <= 10; dy++) {
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

    private void pullNearbyItemDropsTowardPlayer(ServerLevel serverLevel, Player player) {
        AABB pullBox = player.getBoundingBox().inflate(6.0D);
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                pullBox,
                item -> item.isAlive() && item.distanceToSqr(player) <= 36.0D
        );
        if (items.isEmpty()) {
            return;
        }

        Vec3 targetCenter = player.position().add(0.0D, 0.55D, 0.0D);
        for (ItemEntity item : items) {
            Vec3 toPlayer = targetCenter.subtract(item.position());
            double dist = toPlayer.length();
            if (dist > 0.2D) {
                Vec3 pullVel = toPlayer.normalize().scale(Math.min(0.42D, 0.14D + dist * 0.045D));
                Vec3 cur = item.getDeltaMovement();
                item.setDeltaMovement(
                        Mth.lerp(0.34D, cur.x, pullVel.x),
                        Mth.lerp(0.34D, cur.y, pullVel.y + 0.025D),
                        Mth.lerp(0.34D, cur.z, pullVel.z)
                );
                item.hurtMarked = true;

                if ((this.tickCount + item.getId()) % 3 == 0) {
                    DustParticleOptions trailDust = ((item.getId() & 1) == 0) ? CYAN_AURORA_DUST : NEON_GREEN_DUST;
                    serverLevel.sendParticles(
                            trailDust,
                            item.getX(),
                            item.getY() + 0.2D,
                            item.getZ(),
                            1,
                            0.06D,
                            0.06D,
                            0.06D,
                            0.004D
                    );
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        boolean hurt = super.hurtServer(serverLevel, damageSource, amount);
        if (hurt && this.isAlive()) {
            triggerBioluminescentFlashAndStartle(serverLevel, damageSource);
        }
        return hurt;
    }

    private void triggerBioluminescentFlashAndStartle(ServerLevel serverLevel, DamageSource damageSource) {
        this.startledTicks = STARTLED_DURATION_TICKS;
        this.setStartled(true);
        this.setMagnetizing(false);
        serverLevel.broadcastEntityEvent(this, EVENT_BIOLUMINESCENT_FLASH);

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GLOW_SQUID_SQUIRT,
                SoundSource.NEUTRAL,
                1.0F,
                1.25F
        );
        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.NEUTRAL,
                0.85F,
                1.4F
        );

        serverLevel.sendParticles(
                ParticleTypes.GLOW,
                this.getX(),
                this.getY() + 0.9D,
                this.getZ(),
                26,
                0.75D,
                0.55D,
                0.75D,
                0.08D
        );
        serverLevel.sendParticles(
                NEON_GREEN_DUST,
                this.getX(),
                this.getY() + 0.85D,
                this.getZ(),
                18,
                0.85D,
                0.5D,
                0.85D,
                0.04D
        );

        Entity attackerEntity = damageSource.getEntity();
        if (!(attackerEntity instanceof LivingEntity) && damageSource.getDirectEntity() instanceof LivingEntity directLiving) {
            attackerEntity = directLiving;
        }

        if (attackerEntity instanceof LivingEntity attacker
                && attacker != this
                && attacker.isAlive()
                && !(attacker instanceof AuroraJellyEntity)) {
            if (this.distanceToSqr(attacker) <= 64.0D && !attacker.isSpectator()) {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
            Vec3 away = this.position().subtract(attacker.position());
            Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
            if (horiz.lengthSqr() > 1.0E-4D) {
                this.startledEscapeDir = horiz.normalize();
            } else {
                double randAngle = this.random.nextDouble() * Math.PI * 2.0D;
                this.startledEscapeDir = new Vec3(Math.cos(randAngle), 0.0D, Math.sin(randAngle));
            }
        } else {
            double randAngle = this.random.nextDouble() * Math.PI * 2.0D;
            this.startledEscapeDir = new Vec3(Math.cos(randAngle), 0.0D, Math.sin(randAngle));
        }

        // Immediate startled dart impulse
        this.setDeltaMovement(
                this.startledEscapeDir.x * 0.36D,
                0.14D,
                this.startledEscapeDir.z * 0.36D
        );
        this.hasImpulse = true;
    }

    @Override
    public void push(Entity other) {
        super.push(other);
        if (!this.level().isClientSide()
                && this.level() instanceof ServerLevel serverLevel
                && this.isAlive()
                && this.stingCooldown <= 0
                && other instanceof LivingEntity living
                && shouldStingOnDirectTouch(living)) {
            tryApplyElectricSting(serverLevel, living);
        }
    }

    private boolean shouldStingOnDirectTouch(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity instanceof AuroraJellyEntity) {
            return false;
        }
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
            return this.startledTicks > 0 || !isHoldingMagneticLure(player);
        }
        return this.startledTicks > 0 || entity instanceof Monster;
    }

    private void checkTentacleTouchStingCollisions(ServerLevel serverLevel) {
        if (this.stingCooldown > 0) {
            return;
        }
        AABB tentacleZone = this.startledTicks > 0
                ? this.getBoundingBox().inflate(0.22D, 0.18D, 0.22D)
                : this.getBoundingBox().inflate(0.06D, 0.04D, 0.06D);
        List<LivingEntity> colliding = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                tentacleZone,
                this::shouldStingOnDirectTouch
        );
        if (!colliding.isEmpty()) {
            for (LivingEntity victim : colliding) {
                if (tryApplyElectricSting(serverLevel, victim)) {
                    break;
                }
            }
        }
    }

    private boolean tryApplyElectricSting(ServerLevel serverLevel, LivingEntity victim) {
        if (victim == this || !victim.isAlive() || victim instanceof AuroraJellyEntity) {
            return false;
        }
        if (victim instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        this.stingCooldown = STING_COOLDOWN_TICKS;
        boolean damaged = victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), ELECTRIC_STING_DAMAGE);
        if (damaged) {
            serverLevel.broadcastEntityEvent(this, EVENT_ELECTRIC_STING);
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.GUARDIAN_ATTACK,
                    SoundSource.NEUTRAL,
                    0.75F,
                    1.65F
            );
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    victim.getX(),
                    victim.getY() + victim.getBbHeight() * 0.5D,
                    victim.getZ(),
                    14,
                    0.3D,
                    0.35D,
                    0.3D,
                    0.12D
            );
        }
        return damaged;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.startledTicks = input.read("StartledTicks", Codec.INT).orElse(0);
        this.stingCooldown = input.read("StingCooldown", Codec.INT).orElse(0);
        this.setStartled(this.startledTicks > 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("StartledTicks", Codec.INT, this.startledTicks);
        output.store("StingCooldown", Codec.INT, this.stingCooldown);
    }

    static class StartledDartAwayGoal extends Goal {
        private final AuroraJellyEntity jelly;

        StartledDartAwayGoal(AuroraJellyEntity jelly) {
            this.jelly = jelly;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.jelly.startledTicks > 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.jelly.startledTicks > 0;
        }

        @Override
        public void tick() {
            Vec3 dir = this.jelly.startledEscapeDir;
            if (dir.lengthSqr() < 1.0E-4D) {
                double a = this.jelly.random.nextDouble() * Math.PI * 2.0D;
                dir = new Vec3(Math.cos(a), 0.0D, Math.sin(a));
                this.jelly.startledEscapeDir = dir;
            }
            // Slight sinusoidal weave while darting away
            double weave = Math.sin(this.jelly.tickCount * 0.35D + this.jelly.getId()) * 0.45D;
            Vec3 perp = new Vec3(-dir.z, 0.0D, dir.x);
            Vec3 target = this.jelly.position()
                    .add(dir.scale(4.5D))
                    .add(perp.scale(weave))
                    .add(0.0D, 0.35D, 0.0D);

            this.jelly.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.45D);
        }
    }

    static class MagneticResonanceFollowGoal extends Goal {
        private static final double COMFORT_DISTANCE = 3.5D;
        private final AuroraJellyEntity jelly;
        @Nullable
        private Player targetPlayer;

        MagneticResonanceFollowGoal(AuroraJellyEntity jelly) {
            this.jelly = jelly;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.jelly.startledTicks > 0) {
                return false;
            }
            this.targetPlayer = this.jelly.findMagneticLurePlayer(12.0D);
            return this.targetPlayer != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.jelly.startledTicks > 0 || this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
                return false;
            }
            return isHoldingMagneticLure(this.targetPlayer) && this.jelly.distanceToSqr(this.targetPlayer) <= 156.25D;
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.jelly.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }
            this.jelly.getLookControl().setLookAt(this.targetPlayer, 24.0F, 24.0F);

            Vec3 toJelly = this.jelly.position().subtract(this.targetPlayer.position());
            Vec3 horiz = new Vec3(toJelly.x, 0.0D, toJelly.z);
            double horizDist = horiz.length();

            double orbitAngle = (this.jelly.tickCount * 0.025D) + (this.jelly.getId() * 1.1D);
            Vec3 comfortOffset;
            if (horizDist > 0.05D) {
                Vec3 radial = horiz.scale(1.0D / horizDist);
                Vec3 tangent = new Vec3(-radial.z, 0.0D, radial.x).scale(Math.sin(orbitAngle) * 0.65D);
                comfortOffset = radial.scale(COMFORT_DISTANCE).add(tangent);
            } else {
                comfortOffset = new Vec3(Math.cos(orbitAngle) * COMFORT_DISTANCE, 0.0D, Math.sin(orbitAngle) * COMFORT_DISTANCE);
            }

            double wantedX = this.targetPlayer.getX() + comfortOffset.x;
            double wantedY = this.targetPlayer.getY() + 1.55D + Math.sin(orbitAngle * 2.0D) * 0.25D;
            double wantedZ = this.targetPlayer.getZ() + comfortOffset.z;

            this.jelly.getMoveControl().setWantedPosition(wantedX, wantedY, wantedZ, 0.95D);
        }
    }

    static class SinusoidalAuroraDriftGoal extends Goal {
        private final AuroraJellyEntity jelly;
        private double baseAngle;
        private int driftTicks;

        SinusoidalAuroraDriftGoal(AuroraJellyEntity jelly) {
            this.jelly = jelly;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.jelly.startledTicks > 0 || this.jelly.findMagneticLurePlayer(12.0D) != null) {
                return false;
            }
            if (this.jelly.getRandom().nextInt(18) != 0) {
                return false;
            }
            this.baseAngle = this.jelly.getRandom().nextDouble() * Math.PI * 2.0D;
            this.driftTicks = 80 + this.jelly.getRandom().nextInt(80);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.driftTicks > 0
                    && this.jelly.startledTicks <= 0
                    && this.jelly.findMagneticLurePlayer(12.0D) == null;
        }

        @Override
        public void tick() {
            this.driftTicks--;
            double t = (this.jelly.tickCount + this.jelly.getId() * 11) * 0.045D;
            double heading = this.baseAngle + Math.sin(t) * 0.65D;

            double stepDist = 3.2D;
            double candidateX = this.jelly.getX() + Math.cos(heading) * stepDist;
            double candidateZ = this.jelly.getZ() + Math.sin(heading) * stepDist;

            BlockPos checkCol = BlockPos.containing(candidateX, this.jelly.getY(), candidateZ);
            double targetY = this.jelly.getY();
            for (int dy = -3; dy <= 6; dy++) {
                BlockPos below = checkCol.below(dy);
                BlockState state = this.jelly.level().getBlockState(below);
                if (state.isAir() || state.is(Blocks.SNOW)) {
                    continue;
                }
                if (isSkerriesDriftSurface(this.jelly.level(), below, state)) {
                    double desiredHover = 2.65D + Math.sin(t * 1.2D) * 0.85D;
                    targetY = below.getY() + 1.0D + Mth.clamp(desiredHover, 1.5D, 4.0D);
                }
                break;
            }

            this.jelly.getMoveControl().setWantedPosition(candidateX, targetY, candidateZ, 0.78D);
        }
    }
}
