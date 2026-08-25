package ddraig.net.entropica.entity.cryo_stalker;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CryoStalkerEntity extends Monster {

    private static final EntityDataAccessor<Boolean> IS_STALKING = SynchedEntityData.defineId(CryoStalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(CryoStalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_LEAPING = SynchedEntityData.defineId(CryoStalkerEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState swipeAnimationState = new AnimationState();
    public final AnimationState biteAnimationState = new AnimationState();

    public int leapCooldown = 0;
    private int attackAnimTimer = 0;

    public CryoStalkerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_STALKING, false);
        builder.define(IS_ATTACKING, false);
        builder.define(IS_LEAPING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CryoStalkerLeapGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isStalking() {
        return this.entityData.get(IS_STALKING);
    }

    public void setStalking(boolean stalking) {
        this.entityData.set(IS_STALKING, stalking);
    }

    public boolean isAttackingAnim() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttackingAnim(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    public boolean isLeaping() {
        return this.entityData.get(IS_LEAPING);
    }

    public void setLeaping(boolean leaping) {
        this.entityData.set(IS_LEAPING, leaping);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean success = super.doHurtTarget(level, target);
        if (success && target instanceof LivingEntity living) {
            this.setAttackingAnim(true);
            this.attackAnimTimer = 15;
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1), this);
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 100, living.getTicksFrozen() + 100));
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.HOSTILE, 1.0F, 0.9F);
        }
        return success;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.leapCooldown > 0) {
            this.leapCooldown--;
        }

        if (this.attackAnimTimer > 0) {
            this.attackAnimTimer--;
            if (this.attackAnimTimer == 0) {
                this.setAttackingAnim(false);
            }
        }

        if (this.level().isClientSide() && this.random.nextInt(3) == 0) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
            double py = this.getY() + 0.3D + this.random.nextDouble() * 0.6D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
            this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0.0D, -0.02D, 0.0D);
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean sprinting = this.isSprinting() || (moving && this.getTarget() != null);

        if (this.isLeaping()) {
            this.jumpAnimationState.startIfStopped(this.tickCount);
        } else {
            this.jumpAnimationState.stop();
        }

        if (this.isAttackingAnim()) {
            this.attackAnimationState.startIfStopped(this.tickCount);
            this.biteAnimationState.startIfStopped(this.tickCount);
        } else {
            this.attackAnimationState.stop();
            this.biteAnimationState.stop();
        }

        if (sprinting) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.startIfStopped(this.tickCount);
        } else if (moving) {
            this.idleAnimationState.stop();
            this.runAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(ModItems.CRYO_CRYSTALLINE_FANG.get(), 1));
        this.spawnAtLocation(level, new ItemStack(ModItems.FROST_VEINED_HIDE.get(), 1));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.POWDER_SNOW_STEP, 0.15F, 1.0F);
    }

    static class CryoStalkerLeapGoal extends Goal {
        private final CryoStalkerEntity stalker;
        private LivingEntity target;

        public CryoStalkerLeapGoal(CryoStalkerEntity stalker) {
            this.stalker = stalker;
        }

        @Override
        public boolean canUse() {
            this.target = this.stalker.getTarget();
            if (this.target == null || !this.target.isAlive() || this.stalker.leapCooldown > 0) {
                return false;
            }
            double distSq = this.stalker.distanceToSqr(this.target);
            return distSq >= 16.0D && distSq <= 64.0D && this.stalker.onGround() && this.stalker.getRandom().nextInt(5) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.stalker.onGround();
        }

        @Override
        public void start() {
            Vec3 targetPos = this.target.position();
            Vec3 stalkerPos = this.stalker.position();
            Vec3 diff = targetPos.subtract(stalkerPos).normalize().scale(1.2D);

            this.stalker.setDeltaMovement(diff.x, 0.45D, diff.z);
            this.stalker.setLeaping(true);
            this.stalker.leapCooldown = 80;
            this.stalker.level().playSound(null, this.stalker.getX(), this.stalker.getY(), this.stalker.getZ(), SoundEvents.POLAR_BEAR_WARNING, SoundSource.HOSTILE, 1.0F, 1.2F);
        }

        @Override
        public void stop() {
            this.stalker.setLeaping(false);
        }
    }
}
