package ddraig.net.entropica.entity.storm_kite;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class StormKiteEntity extends Monster implements FlyingAnimal {

    private static final EntityDataAccessor<Boolean> DIVING = SynchedEntityData.defineId(StormKiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(StormKiteEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState moveAnimationState = new AnimationState();
    public final AnimationState flapAnimationState = new AnimationState();
    public final AnimationState diveAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int diveCooldown = 0;

    public StormKiteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 70.0D)
                .add(Attributes.FLYING_SPEED, 1.15D)
                .add(Attributes.MOVEMENT_SPEED, 0.40D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIVING, false);
        builder.define(ATTACKING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new StormKiteSwoopGoal(this));
        this.goalSelector.addGoal(3, new StormKiteWanderGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
        // Flying mob - immune to fall damage accumulation
    }

    public boolean isDiving() {
        return this.entityData.get(DIVING);
    }

    public void setDiving(boolean diving) {
        this.entityData.set(DIVING, diving);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public void tick() {
        super.tick();

        // Anti-ground lift: Maintain high-altitude soaring unless actively diving
        if (!this.level().isClientSide()) {
            if (this.diveCooldown > 0) this.diveCooldown--;

            if (!this.isDiving()) {
                int groundY = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) this.getX(), (int) this.getZ());
                if (this.getY() < groundY + 12.0D) {
                    this.setDeltaMovement(this.getDeltaMovement().x, Math.max(this.getDeltaMovement().y + 0.08D, 0.32D), this.getDeltaMovement().z);
                }
                if (this.onGround()) {
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.55D, this.getDeltaMovement().z);
                    this.setOnGround(false);
                }
            }
        }

        if (this.level().isClientSide()) {
            if (this.isDiving()) {
                this.idleAnimationState.stop();
                this.moveAnimationState.stop();
                this.diveAnimationState.startIfStopped(this.tickCount);
            } else if (this.getDeltaMovement().lengthSqr() > 0.005D) {
                this.diveAnimationState.stop();
                this.idleAnimationState.stop();
                this.moveAnimationState.startIfStopped(this.tickCount);
            } else {
                this.diveAnimationState.stop();
                this.moveAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }

            // Static storm particles and galvanic dive trail
            if (this.isDiving() || this.random.nextFloat() < (this.level().isThundering() ? 0.7F : 0.2F)) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 2.2D,
                        this.getY() + this.random.nextDouble() * 1.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 2.2D,
                        0, 0, 0);
            }
        }
    }

    static class StormKiteWanderGoal extends Goal {
        private final StormKiteEntity kite;

        public StormKiteWanderGoal(StormKiteEntity kite) {
            this.kite = kite;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.kite.getNavigation().isInProgress() && this.kite.getRandom().nextInt(15) == 0;
        }

        @Override
        public void start() {
            Vec3 pos = this.kite.position();
            double targetX = pos.x + (this.kite.getRandom().nextDouble() - 0.5D) * 48.0D;
            double targetZ = pos.z + (this.kite.getRandom().nextDouble() - 0.5D) * 48.0D;
            int groundY = this.kite.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) targetX, (int) targetZ);
            double targetY = groundY + 22.0D + this.kite.getRandom().nextDouble() * 14.0D;
            targetY = Math.min(targetY, this.kite.level().getMaxY() - 12);
            this.kite.getNavigation().moveTo(targetX, targetY, targetZ, 1.1D);
        }
    }

    static class StormKiteSwoopGoal extends Goal {
        private final StormKiteEntity kite;
        private int swoopPhase = 0; // 0 = climb/re-position, 1 = dive, 2 = ascend

        public StormKiteSwoopGoal(StormKiteEntity kite) {
            this.kite = kite;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.kite.getTarget();
            return target != null && target.isAlive() && this.kite.diveCooldown <= 0;
        }

        @Override
        public void start() {
            this.swoopPhase = 0;
            LivingEntity target = this.kite.getTarget();
            if (target != null) {
                // Position high above target
                this.kite.getNavigation().moveTo(target.getX(), target.getY() + 24.0D, target.getZ(), 1.4D);
            }
        }

        @Override
        public void tick() {
            LivingEntity target = this.kite.getTarget();
            if (target == null) return;

            this.kite.getLookControl().setLookAt(target, 35.0F, 35.0F);

            if (this.swoopPhase == 0) {
                // High altitude positioning phase
                double dy = this.kite.getY() - target.getY();
                double horizDistSq = this.kite.distanceToSqr(target.getX(), this.kite.getY(), target.getZ());
                if (dy >= 14.0D || horizDistSq < 100.0D) {
                    this.swoopPhase = 1;
                    this.kite.setDiving(true);
                    this.kite.level().playSound(null, this.kite.getX(), this.kite.getY(), this.kite.getZ(), SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE, 1.6F, 0.75F);
                } else {
                    this.kite.getNavigation().moveTo(target.getX(), target.getY() + 22.0D, target.getZ(), 1.4D);
                }
            } else if (this.swoopPhase == 1) {
                // Dive descent phase
                this.kite.getNavigation().moveTo(target.getX(), target.getY() + 0.4D, target.getZ(), 2.2D);

                if (this.kite.distanceToSqr(target) < 14.0D) {
                    target.hurt(this.kite.damageSources().mobAttack(this.kite), (float) this.kite.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    if (this.kite.level().isRaining() || this.kite.level().isThundering()) {
                        target.hurt(this.kite.damageSources().lightningBolt(), 4.0F);
                        this.kite.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0F, 1.4F);
                    }
                    this.kite.setDiving(false);
                    this.kite.diveCooldown = 120;
                    this.swoopPhase = 2;
                }
            } else if (this.swoopPhase == 2) {
                // Ascent phase back into the sky
                this.kite.getNavigation().moveTo(target.getX(), target.getY() + 26.0D, target.getZ(), 1.6D);
                if (this.kite.getY() >= target.getY() + 18.0D) {
                    this.swoopPhase = 0;
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.kite.getTarget();
            return target != null && target.isAlive() && (this.swoopPhase != 2 || this.kite.getY() < target.getY() + 18.0D);
        }

        @Override
        public void stop() {
            this.kite.setDiving(false);
            this.swoopPhase = 0;
        }
    }
}
