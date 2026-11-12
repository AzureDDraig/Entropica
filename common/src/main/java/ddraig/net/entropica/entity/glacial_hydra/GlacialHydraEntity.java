package ddraig.net.entropica.entity.glacial_hydra;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GlacialHydraEntity extends PathfinderMob implements Enemy {

    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(GlacialHydraEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int breathCooldown = 0;

    public GlacialHydraEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GlacialBreathGoal(this));
        this.goalSelector.addGoal(3, new GlacialMeleeAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (this.isAttacking()) {
                this.walkAnimationState.stop();
                this.idleAnimationState.stop();
                this.attackAnimationState.startIfStopped(this.tickCount);
            } else if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D) {
                this.attackAnimationState.stop();
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.attackAnimationState.stop();
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }

            // Snowflake ambient particles
            if (this.random.nextFloat() < 0.2F) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.6D,
                        this.getY() + this.random.nextDouble() * 2.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.6D,
                        0.0D, -0.05D, 0.0D);
            }
        } else {
            if (this.breathCooldown > 0) this.breathCooldown--;

            // Glacial regeneration on ice / snow
            if (this.tickCount % 40 == 0) {
                BlockPos pos = this.blockPosition().below();
                BlockState state = this.level().getBlockState(pos);
                if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
                    this.heal(3.0F);
                }
            }
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    // --- GOALS ---

    static class GlacialMeleeAttackGoal extends Goal {
        private final GlacialHydraEntity hydra;
        private int attackTick = 0;

        public GlacialMeleeAttackGoal(GlacialHydraEntity hydra) {
            this.hydra = hydra;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hydra.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.hydra.getTarget();
            if (target == null) return;

            this.hydra.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.hydra.getNavigation().moveTo(target, 1.15D);

            double distSq = this.hydra.distanceToSqr(target);
            if (distSq < 10.0D) {
                if (this.attackTick == 0) {
                    this.hydra.setAttacking(true);
                    this.attackTick = 20;
                } else if (this.attackTick == 10) {
                    if (this.hydra.distanceToSqr(target) < 12.0D) {
                        target.hurt(this.hydra.damageSources().mobAttack(this.hydra), (float) this.hydra.getAttributeValue(Attributes.ATTACK_DAMAGE));
                        target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 80, target.getTicksFrozen() + 80));
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this.hydra);
                        this.hydra.level().playSound(null, this.hydra.getX(), this.hydra.getY(), this.hydra.getZ(), SoundEvents.POLAR_BEAR_WARNING, SoundSource.HOSTILE, 1.2F, 0.7F);
                    }
                }
            }

            if (this.attackTick > 0) {
                this.attackTick--;
                if (this.attackTick == 0) {
                    this.hydra.setAttacking(false);
                }
            }
        }

        @Override
        public void stop() {
            this.hydra.setAttacking(false);
        }
    }

    static class GlacialBreathGoal extends Goal {
        private final GlacialHydraEntity hydra;
        private int breathTimer = 0;

        public GlacialBreathGoal(GlacialHydraEntity hydra) {
            this.hydra = hydra;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hydra.getTarget();
            return target != null && this.hydra.breathCooldown <= 0 && this.hydra.distanceToSqr(target) >= 9.0D && this.hydra.distanceToSqr(target) <= 64.0D;
        }

        @Override
        public void start() {
            this.breathTimer = 30;
            this.hydra.breathCooldown = 120;
            this.hydra.setAttacking(true);
            this.hydra.getNavigation().stop();
            this.hydra.level().playSound(null, this.hydra.getX(), this.hydra.getY(), this.hydra.getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5F, 0.5F);
        }

        @Override
        public void tick() {
            this.breathTimer--;
            LivingEntity target = this.hydra.getTarget();
            if (target != null) {
                this.hydra.getLookControl().setLookAt(target, 30.0F, 30.0F);

                Vec3 start = this.hydra.position().add(0, 1.4D, 0);
                Vec3 dir = target.position().add(0, 0.5D, 0).subtract(start).normalize();

                for (int i = 0; i < 4; i++) {
                    double spread = 0.2D;
                    Vec3 particleVec = dir.add((this.hydra.getRandom().nextDouble() - 0.5D) * spread, (this.hydra.getRandom().nextDouble() - 0.5D) * spread, (this.hydra.getRandom().nextDouble() - 0.5D) * spread).scale(0.8D);
                    this.hydra.level().addParticle(ParticleTypes.SNOWFLAKE, start.x, start.y, start.z, particleVec.x, particleVec.y, particleVec.z);
                }

                if (this.breathTimer % 6 == 0) {
                    if (this.hydra.distanceToSqr(target) <= 64.0D) {
                        target.hurt(this.hydra.damageSources().freeze(), 4.0F);
                        target.setTicksFrozen(target.getTicksFrozen() + 60);
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2), this.hydra);
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.breathTimer > 0;
        }

        @Override
        public void stop() {
            this.hydra.setAttacking(false);
        }
    }
}
