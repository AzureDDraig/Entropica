package ddraig.net.entropica.entity.frost_core;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Fauna 19 — Glacial Frost-Core (Floating Crystal Shard).
 * Neutral hovering Glacial construct native to 2.1 Blue-Glacier Shelf.
 */
public class GlacialFrostCoreEntity extends PathfinderMob implements FlyingAnimal {

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(GlacialFrostCoreEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_FROST_AURA_PULSE = 4;
    private static final int AURA_COOLDOWN_TICKS = 60;
    private static final int SHED_INTERVAL_TICKS = 12000;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int auraCooldown = 0;
    private int attackAnimTimer = 0;
    private int shedTimer = SHED_INTERVAL_TICKS;

    public GlacialFrostCoreEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 18, true);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FLYING_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean isGlacialSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        return belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.FROSTED_ICE)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.SNOW)
                || (ModBlocks.RUNIC_BLUE_ICE.isPresent() && belowState.is(ModBlocks.RUNIC_BLUE_ICE.get()))
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || belowState.is(BlockTags.BASE_STONE_OVERWORLD)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkBlueGlacierSpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason) || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos check = pos.below(dy);
            BlockState below = level.getBlockState(check);
            if (below.isAir() || below.is(Blocks.SNOW)) {
                continue;
            }
            return isGlacialSpawnSurface(level, check, below);
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState belowState = level.getBlockState(belowPos);
        if (isGlacialSpawnSurface(level, belowPos, belowState)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.BLUE_ICE) || below.is(Blocks.PACKED_ICE) || below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.ICE)) {
            return 10.0F;
        }
        return 0.0F;
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
        builder.define(ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FrostCorePursueTargetGoal(this));
        this.goalSelector.addGoal(2, new FrostCoreHoverDriftGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
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
        // Hovering construct never takes fall damage
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Glides silently through cold air currents
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        // Phases cleanly through snow layers and powder snow without slowing down
        if (state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockState state = this.level().getBlockState(this.blockPosition());
        if (state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return 1.0F;
        }
        return super.getBlockSpeedFactor();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_FROST_AURA_PULSE) {
            this.attackAnimationState.start(this.tickCount);
            for (int i = 0; i < 24; i++) {
                double angle = (Math.PI * 2.0D * i) / 24.0D;
                double speed = 0.22D + this.random.nextDouble() * 0.12D;
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + Math.cos(angle) * 0.4D,
                        this.getY() + 0.6D + (this.random.nextDouble() - 0.5D) * 0.3D,
                        this.getZ() + Math.sin(angle) * 0.4D,
                        Math.cos(angle) * speed,
                        0.02D,
                        Math.sin(angle) * speed
                );
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();

        // Phase smoothly through snow layers if intersecting one
        BlockState feetState = this.level().getBlockState(this.blockPosition());
        if (feetState.is(Blocks.SNOW) || feetState.getBlock() instanceof SnowLayerBlock) {
            Vec3 dm = this.getDeltaMovement();
            this.setPos(this.getX(), this.getY() + 0.08D, this.getZ());
            this.setDeltaMovement(dm.x, Math.max(dm.y, 0.05D), dm.z);
        }

        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);

            if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0012D) {
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
            }

            if (this.isAttacking()) {
                this.attackAnimationState.startIfStopped(this.tickCount);
            }

            // Subtle icy particles drifting off the sapphire-cyan core
            if (this.random.nextFloat() < 0.28F) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.75D,
                        this.getY() + 0.35D + this.random.nextDouble() * 0.65D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.75D,
                        (this.random.nextDouble() - 0.5D) * 0.015D,
                        -0.018D,
                        (this.random.nextDouble() - 0.5D) * 0.015D
                );
            }
        } else if (this.level() instanceof ServerLevel serverLevel) {
            maintainHoverAltitude();

            if (this.auraCooldown > 0) {
                this.auraCooldown--;
            }

            if (this.attackAnimTimer > 0) {
                this.attackAnimTimer--;
                if (this.attackAnimTimer <= 0) {
                    this.setAttacking(false);
                }
            }

            // Provoked 5-block Freezing Frost Aura (Slowness II 100t + 3.0F frost damage on 60t cooldown)
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (this.auraCooldown <= 0 && this.distanceToSqr(target) <= 25.0D) {
                    triggerFreezingFrostAura(serverLevel, target);
                }
            } else {
                // Rare periodic crystal shedding every ~12000 ticks if undisturbed
                if (this.hurtTime == 0) {
                    this.shedTimer--;
                    if (this.shedTimer <= 0) {
                        this.shedTimer = SHED_INTERVAL_TICKS + this.random.nextInt(2400);
                        this.spawnAtLocation(serverLevel, new ItemStack(ModItems.FROST_CORE_SHARD.get()));
                        serverLevel.playSound(
                                null,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                SoundEvents.AMETHYST_CLUSTER_BREAK,
                                SoundSource.NEUTRAL,
                                0.7F,
                                1.35F
                        );
                    }
                }
            }
        }
    }

    private void maintainHoverAltitude() {
        double groundY = findSolidSurfaceBelow();
        if (Double.isNaN(groundY)) {
            return;
        }
        double desiredHoverHeight = 2.0D + Math.sin(this.tickCount * 0.06D) * 0.18D;
        double targetY = groundY + desiredHoverHeight;

        LivingEntity combatTarget = this.getTarget();
        if (combatTarget != null && combatTarget.isAlive()) {
            targetY = Math.max(groundY + 1.6D, Math.min(groundY + 2.4D, combatTarget.getY() + 1.2D));
        }

        double diffY = targetY - this.getY();
        if (Math.abs(diffY) > 0.08D) {
            Vec3 vel = this.getDeltaMovement();
            double adjustY = Mth.clamp(diffY * 0.09D, -0.10D, 0.12D);
            this.setDeltaMovement(vel.x, Mth.lerp(0.35D, vel.y, adjustY), vel.z);
        }
    }

    private double findSolidSurfaceBelow() {
        BlockPos origin = this.blockPosition();
        for (int dy = 0; dy <= 8; dy++) {
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

    private void triggerFreezingFrostAura(ServerLevel serverLevel, LivingEntity primaryTarget) {
        this.auraCooldown = AURA_COOLDOWN_TICKS;
        this.attackAnimTimer = 20;
        this.setAttacking(true);
        serverLevel.broadcastEntityEvent(this, EVENT_FROST_AURA_PULSE);

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.PLAYER_HURT_FREEZE,
                SoundSource.HOSTILE,
                1.0F,
                1.2F
        );
        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.NEUTRAL,
                1.1F,
                0.75F
        );
        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                this.getX(),
                this.getY() + 0.6D,
                this.getZ(),
                32,
                1.8D,
                0.5D,
                1.8D,
                0.06D
        );

        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(5.0D),
                entity -> entity != this
                        && entity.isAlive()
                        && !(entity instanceof GlacialFrostCoreEntity)
                        && this.distanceToSqr(entity) <= 25.0D
        );

        for (LivingEntity victim : nearby) {
            if (victim instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            boolean isHostileParticipant = victim == primaryTarget
                    || victim instanceof Player
                    || (victim instanceof Mob mob && mob.getTarget() == this);
            if (!isHostileParticipant) {
                continue;
            }

            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            victim.setTicksFrozen(Math.min(victim.getTicksRequiredToFreeze() + 40, victim.getTicksFrozen() + 80));
            if (!victim.hurtServer(serverLevel, this.damageSources().freeze(), 3.0F)) {
                victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 3.0F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        this.spawnAtLocation(serverLevel, new ItemStack(ModItems.FROST_CORE_SHARD.get(), 1));
        int iceCount = 1 + this.random.nextInt(2);
        this.spawnAtLocation(serverLevel, new ItemStack(Items.PACKED_ICE, iceCount));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.auraCooldown = input.read("AuraCooldown", Codec.INT).orElse(0);
        this.shedTimer = input.read("ShedTimer", Codec.INT).orElse(SHED_INTERVAL_TICKS);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("AuraCooldown", Codec.INT, this.auraCooldown);
        output.store("ShedTimer", Codec.INT, this.shedTimer);
    }

    static class FrostCorePursueTargetGoal extends Goal {
        private final GlacialFrostCoreEntity core;

        FrostCorePursueTargetGoal(GlacialFrostCoreEntity core) {
            this.core = core;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.core.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.core.getTarget();
            return target != null && target.isAlive() && this.core.distanceToSqr(target) <= 256.0D;
        }

        @Override
        public void tick() {
            LivingEntity target = this.core.getTarget();
            if (target == null) {
                return;
            }
            this.core.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = this.core.distanceToSqr(target);
            if (distSqr > 9.0D) {
                this.core.getMoveControl().setWantedPosition(
                        target.getX(),
                        target.getY() + 1.8D,
                        target.getZ(),
                        1.15D
                );
            }
        }
    }

    static class FrostCoreHoverDriftGoal extends Goal {
        private final GlacialFrostCoreEntity core;
        private double wantedX;
        private double wantedY;
        private double wantedZ;
        private int driftTicks;

        FrostCoreHoverDriftGoal(GlacialFrostCoreEntity core) {
            this.core = core;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.core.getTarget() != null) {
                return false;
            }
            if (this.core.getRandom().nextInt(25) != 0) {
                return false;
            }
            Vec3 pos = this.core.position();
            double dx = (this.core.getRandom().nextDouble() - 0.5D) * 12.0D;
            double dz = (this.core.getRandom().nextDouble() - 0.5D) * 12.0D;
            BlockPos candidate = BlockPos.containing(pos.x + dx, pos.y, pos.z + dz);

            double surfaceY = pos.y;
            for (int dy = -4; dy <= 5; dy++) {
                BlockPos check = candidate.below(dy);
                BlockState state = this.core.level().getBlockState(check);
                if (!state.isAir() && !state.is(Blocks.SNOW) && state.blocksMotion()) {
                    BlockPos hoverPos = check.above(2);
                    if (this.core.level().getBlockState(hoverPos).isAir()) {
                        this.wantedX = hoverPos.getX() + 0.5D;
                        this.wantedY = check.getY() + 2.9D;
                        this.wantedZ = hoverPos.getZ() + 0.5D;
                        this.driftTicks = 60 + this.core.getRandom().nextInt(60);
                        return true;
                    }
                    break;
                }
            }
            this.wantedX = pos.x + dx * 0.5D;
            this.wantedY = surfaceY;
            this.wantedZ = pos.z + dz * 0.5D;
            this.driftTicks = 50;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.driftTicks > 0
                    && this.core.getTarget() == null
                    && this.core.position().distanceToSqr(this.wantedX, this.wantedY, this.wantedZ) > 1.0D;
        }

        @Override
        public void start() {
            this.core.getMoveControl().setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, 0.85D);
        }

        @Override
        public void tick() {
            this.driftTicks--;
            this.core.getMoveControl().setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, 0.85D);
        }
    }
}
